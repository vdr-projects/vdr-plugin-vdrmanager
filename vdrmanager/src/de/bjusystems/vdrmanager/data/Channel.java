package de.bjusystems.vdrmanager.data;

public class Channel {

	private final int number;
	private final String name;
	private final String provider;

	public Channel(final String channelData) {
		String[] words = channelData.split(":");
		this.number = Integer.valueOf(words[0].substring(1));
		if (words.length > 2) {
			this.name = words[1];
			this.provider = words[2];
		} else {
			this.name = words[1];
			this.provider = "Unknown";
		}
	}

	public Channel() {
		this.number = 0;
		this.name = "Unknown";
		this.provider = "Unknown";
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

	public String getProvider() {
		return provider;
	}

	@Override
	public String toString() {
		final StringBuilder text = new StringBuilder();
		text.append(number);
		text.append(" - ");
		text.append(name);
		text.append(" : ");
		text.append(provider);
		return text.toString();
	}
}
