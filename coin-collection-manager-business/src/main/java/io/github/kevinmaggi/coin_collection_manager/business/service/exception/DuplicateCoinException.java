package io.github.kevinmaggi.coin_collection_manager.business.service.exception;

/**
 * This exception will be thrown if will be found a {@code Coin} in the DB that interfere with current operation.
 */
public class DuplicateCoinException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new exception.
	 *
	 * @param message	the message
	 */
	public DuplicateCoinException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with a cause.
	 *
	 * @param message	the message
	 * @param cause		the exception that cause the fail
	 */
	public DuplicateCoinException(String message, Throwable cause) {
		super(message, cause);
	}

}
