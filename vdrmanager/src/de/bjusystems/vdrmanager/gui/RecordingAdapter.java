package de.bjusystems.vdrmanager.gui;

import android.content.Context;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventFormatter;

class RecordingAdapter extends EventAdapter
{


	public RecordingAdapter(final Context context) {
		super(context, R.layout.epg_event_item);
		hideChannelName = false;
	}

	@Override
	protected EventFormatter getEventFormatter(Event event) {
			return new EventFormatter(event, true);
	}


}