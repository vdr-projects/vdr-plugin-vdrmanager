package de.bjusystems.androvdr.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.bjusystems.androvdr.data.EventFormatter;
import de.bjusystems.androvdr.data.EventListItem;
import de.bjusystems.androvdr.utils.date.DateFormatter;
import de.bjusystems.androvdr.R;

class EventAdapter extends ArrayAdapter<EventListItem> {

	private final LayoutInflater inflater;
	private final List<EventListItem> items;
	private final boolean sortByTime;

	public EventAdapter(final Context context, final boolean sortByTime) {
		super(context, R.layout.event_item);
		inflater = LayoutInflater.from(context);
		items = new ArrayList<EventListItem>();
		this.sortByTime = sortByTime;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {

		EventListItemHolder itemHolder = new EventListItemHolder();

		// recycle view?
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.event_item, null);
			itemHolder = new EventListItemHolder();

			itemHolder.state = (ImageView) view.findViewById(R.id.timer_item_state);
			itemHolder.time = (TextView) view.findViewById(R.id.timer_item_time);
			itemHolder.channel = (TextView) view.findViewById(R.id.timer_item_channel);
			itemHolder.title = (TextView) view.findViewById(R.id.timer_item_title);

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
			itemHolder.channel.setVisibility(View.GONE);
			itemHolder.title.setVisibility(View.GONE);
			itemHolder.time.setText(item.getHeader());
		} else {
			view.setBackgroundColor(Color.BLACK);
			itemHolder.channel.setVisibility(View.VISIBLE);
			itemHolder.title.setVisibility(View.VISIBLE);
			itemHolder.state.setVisibility(View.VISIBLE);
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
			final EventFormatter formatter = new EventFormatter(item.getEvent());
			itemHolder.time.setText(formatter.getTime());
			itemHolder.channel.setText(item.getChannelName());
			itemHolder.title.setText(formatter.getTitle());
		}

		return view;
	}

	public void addItem(final EventListItem item) {
		items.add(item);
	}

	public void sortItems() {
		if (sortByTime) {
			sortItemsByTime();
		} else {
			sortItemsByChannel();
		}
	}

	private void sortItemsByTime() {

		// sort by start time
		final EventListItem[] unsortedItems = items.toArray(new EventListItem[0]);
		final Comparator<EventListItem> comparator = new Comparator<EventListItem>() {

			public int compare(final EventListItem item1, final EventListItem item2) {
				return item1.getStart().compareTo(item2.getStart());
			}
		};
		Arrays.sort(unsortedItems, comparator);

		// insert daily headers
		final List<EventListItem> sortedItems = new ArrayList<EventListItem>();
		final GregorianCalendar itemCal = new GregorianCalendar();
		final GregorianCalendar lastHeaderCal = new GregorianCalendar();
		lastHeaderCal.set(Calendar.YEAR, 1970);

		for(final EventListItem item : unsortedItems) {
			itemCal.setTime(item.getStart());

			if (itemCal.get(Calendar.DAY_OF_YEAR) != lastHeaderCal.get(Calendar.DAY_OF_YEAR) ||
					itemCal.get(Calendar.YEAR) != lastHeaderCal.get(Calendar.YEAR)) {
				lastHeaderCal.setTimeInMillis(itemCal.getTimeInMillis());
				final DateFormatter dateFormatter = new DateFormatter(lastHeaderCal);
				sortedItems.add(new EventListItem(dateFormatter.getDailyHeader()));
			}

			sortedItems.add(item);
		}

		// fill adapter
		clear();
		for(final EventListItem item : sortedItems) {
			add(item);
		}
	}

	private void sortItemsByChannel() {

		final EventListItem[] sortedItems = items.toArray(new EventListItem[0]);
		final Comparator<EventListItem> comparator = new Comparator<EventListItem>() {

			public int compare(final EventListItem item1, final EventListItem item2) {
				return Integer.valueOf(item1.getChannelNumber()).compareTo(Integer.valueOf(item2.getChannelNumber()));
			}
		};
		Arrays.sort(sortedItems, comparator);

		// fill adapter
		clear();
		if (sortedItems.length > 0) {
			add(new EventListItem(new DateFormatter(sortedItems[0].getStart()).getDailyHeader()));
		}
		for(final EventListItem item : sortedItems) {
			add(item);
		}

	}

	public void clearItems() {
		items.clear();
	}
}