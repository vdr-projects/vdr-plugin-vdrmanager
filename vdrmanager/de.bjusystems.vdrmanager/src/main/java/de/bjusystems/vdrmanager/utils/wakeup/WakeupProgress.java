package de.bjusystems.vdrmanager.utils.wakeup;

public class WakeupProgress {

	public WakeupProgress(WakeupProgressType state) {
		this(state, null);
	}

	public WakeupProgress(WakeupProgressType state, String info) {
		this.state = state;
		this.info = info;
	}

	private WakeupProgressType state;

	private String info;

	public WakeupProgressType getState() {
		return state;
	}

	public String getInfo() {
		return info;
	}

}
