package de.bjusystems.vdrmanager.gui;

import android.content.Context;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventFormatter;

public class TimeEventAdapter extends EventAdapter {

	public TimeEventAdapter(final Context context) {
		super(context, R.layout.epg_event_item);
	}

	public void sortItems() {
	//	sortItemsByChannel();
	}

	@Override
	protected EventFormatter getEventFormatter(Event event) {
			return new EventFormatter(event,true);
	}
}