package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.EventListItem;

abstract class EventAdapter extends ArrayAdapter<EventListItem> {

	protected final int layout;
	protected final LayoutInflater inflater;
	//protected final List<EventListItem> items;
	
	protected boolean hideChannelName = false;

	public EventAdapter(final Context context, int layout) {
		super(context, layout);
		this.layout = layout;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {

		EventListItemHolder itemHolder = new EventListItemHolder();

		// recycle view?
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(layout, null);

			itemHolder = new EventListItemHolder();

			itemHolder.state = (ImageView) view
					.findViewById(R.id.timer_item_state);
			itemHolder.time = (TextView) view
					.findViewById(R.id.timer_item_time);
			itemHolder.channel = (TextView) view
					.findViewById(R.id.timer_item_channel);
			itemHolder.title = (TextView) view
					.findViewById(R.id.timer_item_title);
			itemHolder.progress = (ProgressBar) view
					.findViewById(R.id.timer_progress);
			itemHolder.shortText = (TextView) view
					.findViewById(R.id.timer_item_shorttext);
			itemHolder.duration = (TextView) view
					.findViewById(R.id.timer_item_duration);
			view.setTag(itemHolder);
		} else {
			itemHolder = (EventListItemHolder) view.getTag();
		}

		// get item
		final EventListItem item = getItem(position);

		// fill item
		if (item.isHeader()) {
			view.setPadding(view.getPaddingLeft(), 0, view.getPaddingRight(), 0);
			view.setBackgroundColor(Color.DKGRAY);
			itemHolder.state.setVisibility(View.GONE);
			itemHolder.channel.setText(item.getHeader());
			itemHolder.title.setVisibility(View.GONE);
			itemHolder.time.setVisibility(View.GONE);
			itemHolder.progress.setVisibility(View.GONE);
			itemHolder.shortText.setVisibility(View.GONE);
			itemHolder.duration.setVisibility(View.GONE);

		} else {

			view.setBackgroundColor(Color.BLACK);
			// itemHolder.channel.setVisibility(View.VISIBLE);
			itemHolder.time.setVisibility(View.VISIBLE);
			itemHolder.title.setVisibility(View.VISIBLE);
			itemHolder.state.setVisibility(View.VISIBLE);
			// itemHolder.channel.setVisibility(View.VISIBLE);
			itemHolder.shortText.setVisibility(View.VISIBLE);
			itemHolder.duration.setVisibility(View.VISIBLE);
			// itemHolder.state.setVisibility(View.);
			switch (item.getTimerState()) {
			case Active:
				itemHolder.state.setImageResource(R.drawable.timer_active);
				break;
			case Inactive:
				itemHolder.state.setImageResource(R.drawable.timer_inactive);
				break;
			case Recording:
				itemHolder.state.setImageResource(R.drawable.timer_recording);
				break;
			case None:
				itemHolder.state.setImageResource(R.drawable.timer_none);
				break;
			}
			final EventFormatter formatter = getEventFormatter(item.getEvent());
			itemHolder.time.setText(formatter.getTime());
			if(hideChannelName){
				itemHolder.channel.setVisibility(View.GONE);
			} else {
				itemHolder.channel.setText(item.getChannelName());
			}
			itemHolder.title.setText(formatter.getTitle());
			itemHolder.shortText.setText(TextUtils.isEmpty(formatter
					.getShortText()) ? " " : formatter.getShortText().trim());
			int p = Utils.getProgress(item);
			if (p == -1) {
				itemHolder.progress.setVisibility(View.GONE);
				// itemHolder.time.setTypeface(null, Typeface.NORMAL);
				// itemHolder.title.setTypeface(null, Typeface.NORMAL);
				// itemHolder.shortText.setTypeface(null, Typeface.NORMAL);
				int dura = Utils.getDuration(item);
				itemHolder.duration.setText(getContext().getString(
						R.string.epg_duration_template, dura));
			} else {
				itemHolder.progress.setVisibility(View.VISIBLE);
				itemHolder.progress.setProgress(p);
				// itemHolder.time.setTypeface(null, Typeface.BOLD);
				// itemHolder.title.setTypeface(null, Typeface.BOLD);
				// itemHolder.shortText.setTypeface(null, Typeface.BOLD);
				int dura = Utils.getDuration(item);
				int rest = dura - (dura * p / 100);
				itemHolder.duration.setText(getContext().getString(
						R.string.epg_duration_template_live, rest, dura));
			}
		}
		return view;
	}

	protected EventFormatter getEventFormatter(Event event) {
		return new EventFormatter(event);
	}

}