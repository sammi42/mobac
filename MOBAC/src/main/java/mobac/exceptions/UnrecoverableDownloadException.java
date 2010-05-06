package mobac.exceptions;

/**
 * An {@link UnrecoverableDownloadException} indicates that there has been a
 * problem on client side that made it impossible to download a certain file
 * (usually a map tile image). Therefore the error is independent of the network
 * connection between client and server and the server itself.
 */
public class UnrecoverableDownloadException extends Exception {

	private static final long serialVersionUID = 1L;

	public UnrecoverableDownloadException(String message) {
		super(message);
	}

}
