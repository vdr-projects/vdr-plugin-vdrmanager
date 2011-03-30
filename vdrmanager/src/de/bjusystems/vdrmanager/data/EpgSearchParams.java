package de.bjusystems.vdrmanager.data;

import java.util.Date;

/**
 * Class for EPG events
 * @author bju
 */
public class EpgSearchParams {

	private String channelNumber;
	private String title;
	private Date start;
	private Date end;

	public String getChannelNumber() {
		return channelNumber;
	}
	public void setChannelNumber(final String channelNumber) {
		this.channelNumber = channelNumber;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(final String title) {
		this.title = title;
	}
	public Date getStart() {
		return start;
	}
	public void setStart(final Date start) {
		this.start = start;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(final Date end) {
		this.end = end;
	}
	public String toCommandLine() {
		return title;
	}
}
