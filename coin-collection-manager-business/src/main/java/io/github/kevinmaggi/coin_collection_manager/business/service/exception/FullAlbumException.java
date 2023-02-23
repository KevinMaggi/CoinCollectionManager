package io.github.kevinmaggi.coin_collection_manager.business.service.exception;

/**
 * This exception will be thrown if an operation try to add a {@code Coin} to a full {@code Album}.
 */
public class FullAlbumException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new exception.
	 *
	 * @param message	the message
	 */
	public FullAlbumException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with a cause.
	 *
	 * @param message	the message
	 * @param cause		the exception that cause the fail
	 */
	public FullAlbumException(String message, Throwable cause) {
		super(message, cause);
	}

}
