package io.github.kevinmaggi.coin_collection_manager.business.transaction.exception;

/**
 * This exception is thrown when a database operation fails for some reason.
 */
public class DatabaseOperationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new exception with a cause.
	 *
	 * @param message	a message
	 * @param cause		the exception that cause the fail
	 */
	public DatabaseOperationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception.
	 *
	 * @param message	a message
	 */
	public DatabaseOperationException(String message) {
		super(message);
	}
}
