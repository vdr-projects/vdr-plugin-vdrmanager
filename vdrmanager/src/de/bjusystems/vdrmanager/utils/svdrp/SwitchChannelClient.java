package de.bjusystems.vdrmanager.utils.svdrp;

import de.bjusystems.vdrmanager.R;


/**
 * Class for switching a channel by SETCHANNEL <NR|CHID>
 *
 * @author lado
 *
 */
public class SwitchChannelClient extends SvdrpClient<String> {

	private Integer nr;

	private String chid;

	public SwitchChannelClient(Integer nr){
		this.nr = nr;
	}

	public SwitchChannelClient(String chid){
		this.chid = chid;
	}

	/**
	 * Constructor
	 */
	public SwitchChannelClient() {
		super();
	}

	/**
	 * Starts the wakeup request
	 */
	@Override
	public void run()   {
		if(nr != null){
			runCommand("SETCHANNEL " + String.valueOf(nr));
		} else {
			runCommand("SETCHANNEL " + chid);
		}
	}

	@Override
	public String parseAnswer(final String line) {
		return line;
	}

	@Override
	public int getProgressTextId() {
		return R.string.progress_switching;
	}

}