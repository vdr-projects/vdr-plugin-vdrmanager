package de.bjusystems.vdrmanager.gui;

import android.content.Context;
import android.view.View;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.data.Timer;

public class TimeEventAdapter extends BaseEventAdapter<EventListItem> {

	public TimeEventAdapter(final Context context) {
		super(context, R.layout.epg_event_item);
	}

	public void sortItems() {
		// sortItemsByChannel();
	}

	@Override
	protected EventFormatter getEventFormatter(Event event) {
		return new EventFormatter(event, true);
	}
}