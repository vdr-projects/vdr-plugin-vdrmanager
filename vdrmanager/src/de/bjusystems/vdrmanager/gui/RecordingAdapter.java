package de.bjusystems.vdrmanager.gui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.data.RecordingListItem;


class RecordingAdapter extends BaseEventAdapter<EventListItem> {

	protected final static int TYPE_FOLDER = 2;

	public RecordingAdapter(final Context context) {
		super(context, R.layout.epg_event_item);
		hideChannelName = false;
	}

	@Override
	protected EventFormatter getEventFormatter(Event event) {
		return new EventFormatter(event, true);
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public void add(EventListItem object) {
		items.add(object);
		// if (object.isHeader()) {
		// sections.add(object.getHeader());
		// }
		super.add(object);
	}

	@Override
	public int getItemViewType(int position) {

		// get item
		final RecordingListItem item = (RecordingListItem) getItem(position);

		if (item.isHeader()) {
			return TYPE_HEADER;
		} else if (item.isFolder()) {
			return TYPE_FOLDER;
		}
		return TYPE_ITEM;
	}



	class EventListItemFolderHolder {
		public TextView folder;
	}

	protected EventListItemFolderHolder getFolderViewHolder(EventListItem item,
			View view) {
		EventListItemFolderHolder itemHolder = new EventListItemFolderHolder();
		itemHolder.folder = (TextView) view.findViewById(R.id.header_item);
		return itemHolder;
	}


	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {

		// get item
		final RecordingListItem item = (RecordingListItem) getItem(position);

		if (item.isFolder() == false) {
			return super.getView(position, convertView, parent);
		}

		Object holder = null;
		if (convertView == null || (convertView != null && convertView.getTag() instanceof EventListItemFolderHolder) == false) {
			convertView = inflater.inflate(R.layout.folder_item, null);
			holder = getHeaderViewHolder(item, convertView);
			convertView.setTag(holder);
		} else {
			holder = convertView.getTag();
		}

		((EventListItemHeaderHolder) holder).header.setText(item.getHeader());
		return convertView;
	}

	@Override
	public RecordingListItem getItem(int position) {
		return (RecordingListItem) super.getItem(position);
	}
}