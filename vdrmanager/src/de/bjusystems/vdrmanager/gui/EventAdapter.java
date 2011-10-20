package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.EventListItem;

abstract class EventAdapter extends ArrayAdapter<EventListItem> implements
		Filterable {

	protected final int layout;
	protected final LayoutInflater inflater;
	protected final List<EventListItem> items = new ArrayList<EventListItem>();

	protected boolean hideDescription = true;

	protected boolean hideChannelName = false;

	String highlight;

	public EventAdapter(final Context context, int layout) {
		super(context, layout);
		this.layout = layout;
		inflater = LayoutInflater.from(context);
	}


	

	@Override
	public void add(EventListItem object) {
		items.add(object);
		super.add(object);
	}

	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {

		// get item
		final EventListItem item = getItem(position);

		if (item.isHeader()) {
			return getHeaderView(item, convertView, parent);
		} else {
			return getEventView(item, convertView, parent);
		}

	}

	private View getEventView(EventListItem item, View convertView,
			ViewGroup parent) {

		EventListItemHolder itemHolder = new EventListItemHolder();

		// recycle view?
		View view = convertView;
		if (view == null || view instanceof ListView == false) {
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
			itemHolder.description = (TextView) view
					.findViewById(R.id.event_item_description);
			view.setTag(itemHolder);
		} else {
			itemHolder = (EventListItemHolder) view.getTag();
		}


//		itemHolder.title.setVisibility(View.VISIBLE);
		itemHolder.state.setVisibility(View.VISIBLE);
		//itemHolder.shortText.setVisibility(View.VISIBLE);
		//itemHolder.duration.setVisibility(View.VISIBLE);
		// itemHolder.state.setVisibility(View.);
		switch (item.getTimerState()) {
		case Active:
			//itemHolder.state.setVisibility(View.VISIBLE);
			itemHolder.state.setImageResource(R.drawable.timer_active);
			break;
		case Inactive:
			//itemHolder.state.setVisibility(View.VISIBLE);
			itemHolder.state.setImageResource(R.drawable.timer_inactive);
			break;
		case Recording:
			//itemHolder.state.setVisibility(View.VISIBLE);
			itemHolder.state.setImageResource(R.drawable.timer_recording);
			break;
		case None:
			//itemHolder.state.setVisibility(View.GONE);
			itemHolder.state.setImageResource(R.drawable.timer_none);
			break;
		}
		
		final EventFormatter formatter = getEventFormatter(item);
		itemHolder.time.setText(formatter.getTime());
		if (hideChannelName) {
			itemHolder.channel.setVisibility(View.GONE);
		} else {
			itemHolder.channel.setText(item.getChannelName());
		}

		CharSequence title = Utils.highlight(formatter.getTitle(), highlight);
		CharSequence shortText = Utils.highlight(formatter.getShortText(),
				highlight);
		itemHolder.title.setText(title);
		itemHolder.shortText.setText(shortText);

		if (hideDescription == false) {
			Pair<Boolean, CharSequence> desc = Utils.highlight2(
					formatter.getDescription(), highlight);
			if (desc.first == true) {
				itemHolder.description.setVisibility(View.VISIBLE);
				itemHolder.description.setText(desc.second);
			}
		}

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

		return view;
	}

	class EventListItemHeaderHolder {
		public TextView header;
	}

	private View getHeaderView(EventListItem item, View convertView,
			ViewGroup parent) {

		EventListItemHeaderHolder itemHolder = new EventListItemHeaderHolder();

		// recycle view?
		View view = convertView;
		if (view == null || convertView instanceof TextView == false) {
			view = inflater.inflate(R.layout.header_item, null);

			itemHolder = new EventListItemHeaderHolder();

			itemHolder.header = (TextView) view.findViewById(R.id.header_item);
		} else {
			itemHolder = (EventListItemHeaderHolder) view.getTag();
		}
		itemHolder.header.setText(item.getHeader());
		return view;
	}

	protected EventFormatter getEventFormatter(Event event) {
		return new EventFormatter(event);
	}

	private void addSuper(EventListItem item) {
		super.add(item);
	}

	public boolean isHideDescription() {
		return hideDescription;
	}

	public void setHideDescription(boolean hideDescription) {
		this.hideDescription = hideDescription;
	}

	public boolean isHideChannelName() {
		return hideChannelName;
	}

	public void setHideChannelName(boolean hideChannelName) {
		this.hideChannelName = hideChannelName;
	}

	// TODO implement locking in performFiltering, check the parent class
	//
	public Filter getFilter() {
		return new Filter() {
			/**
			 * 
			 */
			EventListItem prevHead = null;

			@Override
			protected FilterResults performFiltering(CharSequence arg0) {
				highlight = arg0.toString().toLowerCase();
				ArrayList<EventListItem> result = new ArrayList<EventListItem>();
				for (EventListItem event : items) {
					if (event.isHeader()) {
						prevHead = event;
						// result.add(event);
						continue;
					}
					if (event.getTitle().toLowerCase()
							.indexOf(String.valueOf(arg0).toLowerCase()) != -1
							|| event.getShortText()
									.toLowerCase()
									.indexOf(String.valueOf(arg0).toLowerCase()) != -1) {
						if (prevHead != null) {
							result.add(prevHead);
							prevHead = null;
						}
						result.add(event);
					}
				}

				FilterResults fr = new FilterResults();
				fr.count = result.size();
				fr.values = result;
				return fr;
			}

			@Override
			protected void publishResults(CharSequence arg0, FilterResults arg1) {
				EventAdapter.this.clear();
				for (EventListItem item : (ArrayList<EventListItem>) arg1.values) {
					addSuper(item);
				}
				notifyDataSetChanged();
			}
		};
	}
}