package de.bjusystems.vdrmanager.data;

import java.util.ArrayList;
import java.util.List;

public class AudioTrack {

	private String cached = null;

	public int index;

	public String type;

	public String display;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type).append(", ").append(display).append("+").append(index);
		return sb.toString();
	};

	private static final ArrayList<AudioTrack> EMPTY = new ArrayList<AudioTrack>(
			0);

	/**
	 * a,1,deu|d,2,deu
	 *
	 * @param raw
	 * @return
	 */
	public static List<AudioTrack> getAudio(String rawAudio) {

		if(rawAudio == null){
			return EMPTY;
		}

		String[] splitted = rawAudio.split("\\|");

		if (splitted == null || splitted.length == 0) {
			return EMPTY;
		}

		ArrayList<AudioTrack> audio;
		audio = new ArrayList<AudioTrack>(splitted.length);
		for (String a : splitted) {
			String[] ar = a.split(",");
			if (ar == null || ar.length != 3) {
				continue;
			}
			AudioTrack track = new AudioTrack();
			track.type = ar[0];
			track.index = Integer.valueOf(ar[1]);
			track.display = ar[2];
			audio.add(track);
		}
		return audio;

	}

}
