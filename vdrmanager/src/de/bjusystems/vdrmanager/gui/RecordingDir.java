package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import de.bjusystems.vdrmanager.data.Recording;

public class RecordingDir {

	public RecordingDir() {

	}

	public String name;

	public RecordingDir parent;

	public TreeMap<String, RecordingDir> dirs = new TreeMap<String, RecordingDir>();

	public List<Recording> recordings = new ArrayList<Recording>();

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(name).append(" => {");
		String sep = "";
		for (RecordingDir e : dirs.values()) {
			sb.append(sep).append(e.toString());
			sep = ", ";
		}

		sb.append("}\n-").append("\n");

		sb.append("{").append("");

		sep = "";
		for (Recording r : recordings) {
			sb.append(sep).append(r.toString());
		}
		sb.append("}]");
		return sb.toString();
	}

	public void clear() {
		for (RecordingDir dir : dirs.values()) {
			dir.clear();
		}
		recordings.clear();
	}

	public int size() {
		int sum = 0;
		for (RecordingDir d : dirs.values()) {
			sum += d.size();
		}
		sum += recordings.size();
		return sum;
	}
	
	public String getPath(){
		if(parent == null){
			return "/";
		}
		return parent.getPath()  + name + "/";
	}
}
