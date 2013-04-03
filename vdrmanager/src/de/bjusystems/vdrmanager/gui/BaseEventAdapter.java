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
import android.widget.ProgressBar;
import android.widget.TextView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.data.Recording;
import de.bjusystems.vdrmanager.data.TimerMatch;
import de.bjusystems.vdrmanager.data.Timerable;

abstract class BaseEventAdapter<T extends EventListItem> extends ArrayAdapter<T> implements
		Filterable
// , SectionIndexer
{

	protected final static int TYPE_ITEM = 0;
	protected final static int TYPE_HEADER = 1;
	protected final int layout;
	protected final LayoutInflater inflater;
	protected final List<T> items = new ArrayList<T>();

	protected boolean hideDescription = true;

	protected boolean hideChannelName = false;

	String highlight;

	/**
	 * Lock used to modify the content of {@link #mObjects}. Any write operation
	 * performed on the array should be synchronized on this lock. This lock is
	 * also used by the filter (see {@link #getFilter()} to make a synchronized
	 * copy of the original array of data.
	 */
	private final Object _Lock = new Object();

	public BaseEventAdapter(final Context context, int layout) {
		super(context, layout);
		this.layout = layout;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public void add(T object) {
		items.add(object);
		// if (object.isHeader()) {
		// sections.add(object.getHeader());
		// }
		super.add(object);
	}

	@Override
	public int getItemViewType(int position) {

		// get item
		final EventListItem item = getItem(position);

		if (item.isHeader()) {
			return TYPE_HEADER;
		}
		return TYPE_ITEM;
	}

	public static class EventListItemHeaderHolder {
		TextView header;
	}

	private boolean canReuseConvertView(View convertView, int itemViewType){
		if(convertView == null){
			return false;
		}
		Object o = convertView.getTag();
		if(itemViewType == TYPE_ITEM){
			return o instanceof EventListItemHolder;
		}

		if(itemViewType == TYPE_HEADER){
			return o instanceof EventListItemHeaderHolder;
		}

		return false;

	}


	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {

		// get item
		final EventListItem item = getItem(position);

		Object holder = null;
		int type = getItemViewType(position);
		if (canReuseConvertView(convertView, type) == false) {
			switch (type) {
			case TYPE_ITEM:
				convertView = inflater.inflate(layout, null);
				holder = getEventViewHolder(item, convertView);
				break;
			case TYPE_HEADER:
				convertView = inflater.inflate(R.layout.header_item, null);
				holder = getHeaderViewHolder(item, convertView);
				break;
			}
			convertView.setTag(holder);
		} else {
			holder = convertView.getTag();
		}

		if (type == TYPE_ITEM) {
			fillEventViewHolder((EventListItemHolder) holder, item);
		} else {
			((EventListItemHeaderHolder) holder).header.setText(item
					.getHeader());
		}
		return convertView;
	}

	protected EventListItemHolder getEventViewHolder(EventListItem item, View view) {

		EventListItemHolder itemHolder = new EventListItemHolder();

		itemHolder = new EventListItemHolder();

		itemHolder.state = (ImageView) view.findViewById(R.id.timer_item_state);
		itemHolder.time = (TextView) view.findViewById(R.id.timer_item_time);
		itemHolder.channel = (TextView) view
				.findViewById(R.id.timer_item_channel);
		itemHolder.title = (TextView) view.findViewById(R.id.timer_item_title);
		itemHolder.progress = (ProgressBar) view
				.findViewById(R.id.timer_progress);
		itemHolder.shortText = (TextView) view
				.findViewById(R.id.timer_item_shorttext);
		itemHolder.duration = (TextView) view
				.findViewById(R.id.timer_item_duration);
		itemHolder.description = (TextView) view
				.findViewById(R.id.event_item_description);
		return itemHolder;
	}

	public void fillEventViewHolder(EventListItemHolder itemHolder,
			EventListItem item) {

		itemHolder.state.setVisibility(View.VISIBLE);

		if (item.getEvent() instanceof Recording) {
			Recording r = (Recording) item.getEvent();
			if (r.getTimerStopTime() != null) {
				itemHolder.state.setImageResource(R.drawable.timer_recording);
			} else {
				itemHolder.state.setImageResource(R.drawable.timer_none);
			}
		} else if (item.getEvent() instanceof Timerable == true) {
			TimerMatch match = ((Timerable) item.getEvent()).getTimerMatch();
			switch (((Timerable) item.getEvent()).getTimerState()) {
			case Active:
				itemHolder.state.setImageResource(Utils.getTimerStateDrawable(
						match, R.drawable.timer_active,
						R.drawable.timer_active_begin,
						R.drawable.timer_active_end));
				break;
			case Inactive:
				itemHolder.state.setImageResource(Utils.getTimerStateDrawable(
						match, R.drawable.timer_inactive,
						R.drawable.timer_inactive_begin,
						R.drawable.timer_inactive_end));
				break;
			case Recording:
				itemHolder.state.setImageResource(Utils.getTimerStateDrawable(
						match, R.drawable.timer_recording,
						R.drawable.timer_recording_begin,
						R.drawable.timer_recording_end));
				break;
			case None:
				itemHolder.state.setImageResource(R.drawable.timer_none);
				break;
			}
		} else {
			itemHolder.state.setImageResource(R.drawable.timer_none);
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

		// TODO better render of duration
		int p = Utils.getProgress(item.getEvent());
		if (p == -1) {
			itemHolder.progress.setVisibility(View.GONE);
			int dura = Utils.getDuration(item);
			itemHolder.duration.setText(getContext().getString(
					R.string.epg_duration_template, dura));
		} else {
			itemHolder.progress.setVisibility(View.VISIBLE);
			itemHolder.progress.setProgress(p);
			int dura = Utils.getDuration(item.getEvent());
			int rest = dura - (dura * p / 100);
			// on live recordings the amount of already recorded time
			if (item.getEvent() instanceof Recording) {
				rest = dura - rest;
			}
			itemHolder.duration.setText(getContext().getString(
					R.string.epg_duration_template_live, rest, dura));
		}
	}


	protected EventListItemHeaderHolder getHeaderViewHolder(EventListItem item,
			View view) {
		EventListItemHeaderHolder itemHolder = new EventListItemHeaderHolder();
		itemHolder.header = (TextView) view.findViewById(R.id.header_item);
		return itemHolder;
	}

	protected EventFormatter getEventFormatter(Event event) {
		return new EventFormatter(event);
	}

	protected void addSuper(T item) {
		super.add(item);
	}

	protected void clearSuper() {
		super.clear();
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
	// http://stackoverflow.com/questions/5846385/how-to-update-android-listview-with-dynamic-data-in-real-time
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
				clearSuper();
				for (T item : (ArrayList<T>) arg1.values) {
					addSuper(item);
				}
				notifyDataSetChanged();
			}
		};
	}

	// @Override
	// public int getPositionForSection(int section) {
	// return 0;
	// }

	// @Override
	// public int getSectionForPosition(int position) {
	// TODO Auto-generated method stub
	// return 0;
	// }

	// ArrayList<String> sections = new ArrayList<String>();

	// @Override
	// public Object[] getSections() {
	// try {
	// return sections.toArray();
	// } finally {
	// sections.clear();
	// }
	// }

	@Override
	public void clear() {
		super.clear();
		items.clear();
	}
}
