package de.matzefratze123.api.hs.command.transform;

import org.bukkit.Material;

public class BlockDataTransformer implements Transformer<BlockDataTransformer.BlockData> {

	private static final String	INTEGER_REGEX	= "[+,-]?\\d*";

	@SuppressWarnings("deprecation")
	@Override
	public BlockData transform(String argument) throws TransformException {
		String[] data = argument.split(":");
		if (data.length <= 0)
			throw new TransformException();

		Material material = null;
		byte d = 0;

		// Material
		for (Material m : Material.values()) {
			if (m.name().equalsIgnoreCase(data[0])) {
				material = m;
				break;
			}

			if (m.name().replace("_", "").equalsIgnoreCase(data[0])) {
				material = m;
			}
		}

		if (material == null) {
			if (data[0].matches(INTEGER_REGEX)) {
				try {
					material = Material.getMaterial(Integer.parseInt(data[0]));
				} catch (Exception e) {
				}
			}

			if (material == null)
				throw new TransformException();
		}

		if (data.length > 1) {
			if (data[1].matches(INTEGER_REGEX)) {
				try {
					d = (byte) Integer.parseInt(data[1]);
				} catch (Exception e) {
				}
			}
		}

		return new BlockData(material, d);
	}

	public static class BlockData {

		private Material	material;
		private byte		data;

		public BlockData() {
			this.material = Material.AIR;
			this.data = 0;
		}

		public BlockData(Material material, byte data) {
			this.setMaterial(material);
			this.setData(data);
		}

		public byte getData() {
			return data;
		}

		public void setData(byte data) {
			this.data = data;
		}

		public Material getMaterial() {
			return material;
		}

		public void setMaterial(Material material) {
			this.material = material;
		}

	}

}
