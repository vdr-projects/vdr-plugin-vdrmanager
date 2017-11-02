package de.bjusystems.vdrmanager.gui;

import android.content.Context;
import android.view.View;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.data.Timer;

public class TimerEventAdapter extends TimeEventAdapter {

	public TimerEventAdapter(final Context context) {
		super(context);
	}

	public void sortItems() {
		// sortItemsByChannel();
	}

	@Override
	protected void handleState(EventListItemHolder itemHolder,
			EventListItem item) {
		super.handleState(itemHolder, item);
		Timer r = (Timer) item.getEvent();
		if (r.isVps()) {
			itemHolder.other.setVisibility(View.VISIBLE);
			itemHolder.other.setImageResource(R.drawable.timer_vps);
		} else {
			itemHolder.other.setVisibility(View.GONE);
		}
	}
}