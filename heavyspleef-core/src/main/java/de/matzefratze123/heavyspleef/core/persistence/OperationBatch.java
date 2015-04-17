package de.matzefratze123.heavyspleef.core.persistence;

import java.util.List;
import java.util.concurrent.Callable;

import lombok.Setter;

import com.google.common.collect.Lists;

public abstract class OperationBatch implements Callable<OperationBatch.BatchResult> {
	
	@Setter
	private ReadWriteHandler handler;
	
	@Override
	public BatchResult call() throws Exception {
		BatchResult result = new BatchResult();
		executeBatch(handler, result);
		
		return result;
	}
	
	public abstract void executeBatch(ReadWriteHandler handler, BatchResult resultWriter) throws Exception;
	
	public static class BatchResult {
		
		@Setter
		private int readCursor;
		private final List<Object> results;
		
		public BatchResult() {
			results = Lists.newArrayList();
		}
		
		public void write(Object result) {
			results.add(result);
		}
		
		public Object read() {
			if (readCursor >= results.size()) {
				throw new IllegalStateException("No more elements in batch result");
			}
			
			return results.get(readCursor);
		}
		
	}
	
}
