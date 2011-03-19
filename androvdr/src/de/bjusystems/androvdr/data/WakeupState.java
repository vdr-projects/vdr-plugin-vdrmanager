package de.bjusystems.androvdr.data;

public class WakeupState {

	public static WakeupState OK = new WakeupState(0);
	public static WakeupState FAILED = new WakeupState(1);
	public static WakeupState ERROR = new WakeupState(2);

	private final int value;
	private static WakeupState state;

	private WakeupState(final int value) {
		this.value = value;
	}

	public static WakeupState getState() {
		return state;
	}

	public static void setState(final WakeupState state) {
		WakeupState.state = state;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof WakeupState)) {
			return false;
		}
		return this.value == ((WakeupState)o).value;
	}

	@Override
	public int hashCode() {
		return Integer.valueOf(value).hashCode();
	}
}
