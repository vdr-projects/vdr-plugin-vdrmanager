package de.bjusystems.androvdr.data;


public class Channel {

	private final int number;
	private final String name;

	public Channel(final String channelData) {

		final String[] words = channelData.split(":");
		this.number = Integer.valueOf(words[0].substring(1));
		this.name = words[1];
	}

	public Channel() {
		this.number = 0;
		this.name = "Unknown";
	}

	public boolean isGroupSeparator() {
		return number == 0;
	}

	public int getNumber() {
		return number;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		final StringBuilder text = new StringBuilder();
		text.append(number);
		text.append(" - ");
		text.append(name);
		return text.toString();
	}
}
