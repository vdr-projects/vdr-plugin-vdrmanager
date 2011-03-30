package de.bjusystems.vdrmanager.utils.svdrp;

/**
 * Class for exception caused by SVDRP errors
 * @author bju
 *
 */
@SuppressWarnings("serial")
public class SvdrpException extends Exception {

	public SvdrpException(String text) {
		super(text);
	}
	
	public SvdrpException(String text, Throwable cause) {
		super(text, cause);
	}
	
	public SvdrpException(Throwable cause) {
		super(cause);
	}
}
