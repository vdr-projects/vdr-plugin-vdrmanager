package de.bjusystems.vdrmanager.data;


public interface Timerable {

	public enum TimerState {
		None,
		Active,
		Inactive,
		Recording,
		Recorded
		;
	}

	public Timer createTimer();

	public abstract Timer getTimer();

	public TimerState getTimerState();

	public TimerMatch getTimerMatch();
}
