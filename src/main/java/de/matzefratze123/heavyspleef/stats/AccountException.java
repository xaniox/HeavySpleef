package de.matzefratze123.heavyspleef.stats;

public class AccountException extends Exception {

	private static final long	serialVersionUID	= 3259591029892667831L;

	private String				detailMessage;
	private Exception			parent;

	public AccountException(String message) {
		this.detailMessage = message;
	}

	public AccountException(Exception parent) {
		this.parent = parent;
	}

	public AccountException() {
	}

	@Override
	public String getMessage() {
		return parent != null ? parent.getMessage() : detailMessage == null ? super.getMessage() : detailMessage;
	}

	@Override
	public void printStackTrace() {
		if (parent != null) {
			parent.printStackTrace();
		} else {
			super.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return parent != null ? parent.toString() : toString();
	}

}
