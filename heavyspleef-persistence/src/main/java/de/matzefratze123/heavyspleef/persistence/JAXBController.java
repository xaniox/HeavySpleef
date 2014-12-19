package de.matzefratze123.heavyspleef.persistence;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.Validate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class JAXBController implements DatabaseController {
	
	private static final String DEFAULT_OBJECT_ROOT_ELEMENT_NAME = "element";
	private static final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	private static final XPathFactory xPathFactory = XPathFactory.newInstance();
	private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();
	
	private final JAXBContext context;
	private final File flatfile;
	private Document document;
	private String xmlRootElementName;
	private boolean autoFlush = true;
	
	public JAXBController(File flatfile, String rootElement, Class<?>... beanClasses) throws JAXBException, ParserConfigurationException,
			IOException, SAXException {
		this.context = JAXBContext.newInstance(beanClasses);
		this.flatfile = flatfile;
		this.xmlRootElementName = rootElement;

		initializeDocument();
	}
	
	public JAXBController(File flatfile, String rootElement, List<Class<?>> beanClasses) throws JAXBException, ParserConfigurationException,
			IOException, SAXException {
		this(flatfile, rootElement, beanClasses.toArray(new Class<?>[beanClasses.size()]));
	}

	public JAXBController(File flatfile, String rootElement, boolean autoFlush, Class<?>... beanClasses) throws JAXBException,
			ParserConfigurationException, IOException, SAXException {
		this(flatfile, rootElement, beanClasses);

		this.autoFlush = autoFlush;
	}
	
	private void initializeDocument() throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		
		if (flatfile.exists()) {
			document = builder.parse(flatfile);
		} else {
			document = builder.newDocument();
			
			Element rootElement = document.createElement(xmlRootElementName);
			document.appendChild(rootElement);
		}
	}
	
	public JAXBContext getContext() {
		return context;
	}
	
	public void setAutoFlush(boolean autoFlush) {
		this.autoFlush = autoFlush;
	}
	
	@Override
	public void update(Object object, Object cookie) {
		Map<String, Object> identifierMap = Maps.newHashMap();
		String objRootElementName = DEFAULT_OBJECT_ROOT_ELEMENT_NAME;
		
		Class<?> clazz = object.getClass();
		if (clazz.isAnnotationPresent(XmlRootElement.class)) {
			XmlRootElement rootElementAnnotation = clazz.getAnnotation(XmlRootElement.class);
			
			objRootElementName = rootElementAnnotation.name();
		}
		
		for (Field field : clazz.getDeclaredFields()) {
			if (!field.isAnnotationPresent(XmlAttribute.class)) {
				continue;
			}
			
			String xmlName;
			
			XmlAttribute attributeAnnotation = field.getAnnotation(XmlAttribute.class);
			if (!attributeAnnotation.name().isEmpty()) {
				xmlName = attributeAnnotation.name();
			} else {
				xmlName = field.getName();
			}
			
			Object value = null;
			
			boolean accessible = field.isAccessible();
			
			try {
				field.setAccessible(true);
				value = field.get(object);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Cannot get value for xml-object-id " + xmlName, e);
			} finally {
				field.setAccessible(accessible);
			}
			
			identifierMap.put(xmlName, value);
		}
		
		XPath path = xPathFactory.newXPath();
		StringBuilder builder = new StringBuilder("/*/*[");
		
		int index = 0;
		final int size = identifierMap.size();
		for (Entry<String, Object> entry : identifierMap.entrySet()) {
			builder.append(entry.getKey())
				.append("=")
				.append(entry.getValue());
			
			if (index + 1 < size) {
				builder.append(" and ");
			}
				
			index++;
		}
		
		builder.append(']');
		
		String expression = builder.toString();
		
		Element rootElement = document.getDocumentElement();
		Node objNode;
		
		try {
			objNode = (Node) path.evaluate(expression, rootElement, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			throw new RuntimeException("Illegal XPath expression \"" + expression + "\"", e);
		}
		
		if (objNode == null) {
			objNode = document.createElement(objRootElementName);
			rootElement.appendChild(objNode);
		}
		
		try {
			Marshaller marshaller = context.createMarshaller();
			marshaller.marshal(object, objNode);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		
		if (autoFlush) {
			try {
				flushXml();
			} catch (Exception e) {
				throw new RuntimeException("Failed to flush document to xml file", e);
			}
		}
	}

	@Override
	public List<Object> query(String key, Object value, Object cookie, String orderBy, int limit) {
		Element element = document.getDocumentElement();
		NodeList childNodes = element.getChildNodes();
		List<Node> selectedNodes = Lists.newArrayList();
		
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			
			NamedNodeMap attributes = node.getAttributes();
			Node idAttribute = attributes.getNamedItem(key);
			
			if (idAttribute == null || !idAttribute.getTextContent().equals(value.toString())) {
				continue;
			}
			
			//This child nodes seems to match so add it to our selected nodes
			selectedNodes.add(node);
		}
		
		Unmarshaller unmarshaller;
		List<Object> result;
		
		try {
			unmarshaller = context.createUnmarshaller();
			
			Function<? super Node, ? extends Object> mapFunction = n -> {
				try {
					return unmarshaller.unmarshal(n);
				} catch (JAXBException e) {
					throw new RuntimeException(e);
				}
			};
			
			Stream<Object> objStream = selectedNodes.stream().map(mapFunction);
			if (orderBy != null && !orderBy.isEmpty()) {
				Validate.isTrue(cookie instanceof Class<?>, "Cannot sort result when no class cookie is given");
				Class<?> objClass = (Class<?>) cookie;
				
				ObjectComparator comparator = new ObjectComparator(orderBy, objClass);
				objStream = objStream.sorted(comparator);
				
				comparator.release();
			}
			
			if (limit != NO_LIMIT) {
				objStream = objStream.limit(limit);
			}
			
			result = objStream.collect(Collectors.toList());
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		
		return result;
	}
	
	public void flushXml() throws Exception {
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(document);
		
		StreamResult output = new StreamResult(flatfile);
		transformer.transform(source, output);
	}
	
	private static class ObjectComparator implements Comparator<Object> {
		
		private String orderBy;
		private Class<?> clazz;
		private boolean criteriaFieldAccessible;
		private Field criteriaField;
		
		public ObjectComparator(String orderBy, Class<?> clazz) {
			this.orderBy = orderBy;
			this.clazz = clazz;
			
			fieldLookup();
		}
		
		private void fieldLookup() {
			for (Field field : clazz.getDeclaredFields()) {
				String name = field.getName();
				
				if (field.isAnnotationPresent(XmlAttribute.class)) {
					XmlAttribute xmlAttributeAnnotation = field.getAnnotation(XmlAttribute.class);
					if (!xmlAttributeAnnotation.name().isEmpty()) {
						name = xmlAttributeAnnotation.name();
					}
				} else if (field.isAnnotationPresent(XmlElement.class)) {
					XmlElement xmlElementAnnotation = field.getAnnotation(XmlElement.class);
					if (!xmlElementAnnotation.name().isEmpty()) {
						name = xmlElementAnnotation.name();
					}
				}
				
				if (!name.equals(orderBy)) {
					continue;
				}
				
				criteriaField = field;
				break;
			}
			
			criteriaFieldAccessible = criteriaField.isAccessible();
			criteriaField.setAccessible(true);
		}
		
		public void release() {
			criteriaField.setAccessible(criteriaFieldAccessible);
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(Object o1, Object o2) {
			Object value1;
			Object value2;
			
			try {
				value1 = criteriaField.get(o1);
				value2 = criteriaField.get(o2);
			} catch (Exception e) {
				throw new RuntimeException("Could not compare two objects", e);
			}
			
			int compareResult;
			
			if (value1 instanceof Comparable && value2 instanceof Comparable) {
				compareResult = ((Comparable)value1).compareTo(value2);
			} else {
				compareResult = value1.toString().compareTo(value2.toString());
			}
			
			return compareResult;
		}
		
	}

}
