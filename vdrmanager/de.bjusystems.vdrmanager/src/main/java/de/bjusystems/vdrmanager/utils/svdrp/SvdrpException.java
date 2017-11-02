package de.bjusystems.vdrmanager.utils.svdrp;

/**
 * Class for exception caused by SVDRP errors
 * @author bju
 *
 */
@SuppressWarnings("serial")
public class SvdrpException extends Exception {

	SvdrpEvent event;

	public SvdrpEvent getEvent() {
		return event;
	}

	public void setEvent(SvdrpEvent event) {
		this.event = event;
	}

	public SvdrpException(SvdrpEvent event, String text) {
		super(text);
		this.event = event;
	}

	public SvdrpException(String text, Throwable cause) {
		super(text, cause);
	}

	public SvdrpException(Throwable cause) {
		super(cause);
	}
}
