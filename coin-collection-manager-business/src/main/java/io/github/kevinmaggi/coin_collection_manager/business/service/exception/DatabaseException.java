package io.github.kevinmaggi.coin_collection_manager.business.service.exception;

/**
 * This exception will be thrown whenever a database querying doesn't succeed.
 */
public class DatabaseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new exception.
	 *
	 * @param message	the message
	 */
	public DatabaseException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with a cause.
	 *
	 * @param message	the message
	 * @param cause		the exception that cause the fail
	 */
	public DatabaseException(String message, Throwable cause) {
		super(message, cause);
	}

}
