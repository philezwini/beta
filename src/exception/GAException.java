package exception;

public class GAException extends Throwable {
	private static final long serialVersionUID = 1L;
	private String message;

	public GAException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
