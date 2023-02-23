package io.github.kevinmaggi.coin_collection_manager.business.service.exception;

/**
 * This exception will be thrown if will be invoke some operation on a non existing {@code Album}.
 */
public class AlbumNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new exception.
	 *
	 * @param message	the message
	 */
	public AlbumNotFoundException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with a cause.
	 *
	 * @param message	the message
	 * @param cause		the exception that cause the fail
	 */
	public AlbumNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
