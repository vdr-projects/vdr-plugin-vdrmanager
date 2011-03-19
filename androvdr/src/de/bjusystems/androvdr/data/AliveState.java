package de.bjusystems.androvdr.data;

public class AliveState {

	public static AliveState ALIVE = new AliveState(0);
	public static AliveState DEAD = new AliveState(1);
	public static AliveState UNKNOWN = new AliveState(2);

	private final int value;
	private static AliveState state;

	private AliveState(final int value) {
		this.value = value;
	}

	public static AliveState getState() {
		return state;
	}

	public static void setState(final AliveState state) {
		AliveState.state = state;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof AliveState)) {
			return false;
		}
		return this.value == ((AliveState)o).value;
	}

	@Override
	public int hashCode() {
		return Integer.valueOf(value).hashCode();
	}
}
