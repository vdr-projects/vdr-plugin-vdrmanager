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
	protected boolean isHeader(EventListItem item) {
		if (item instanceof RecordingListItem == false) {
			return item.isHeader();
		}

		if (((RecordingListItem) item).isFolder()) {
			return false;
		}

		return item.isHeader();
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
		public TextView count;
	}

	protected EventListItemFolderHolder getFolderViewHolder(EventListItem item,
			View view) {
		EventListItemFolderHolder itemHolder = new EventListItemFolderHolder();
		itemHolder.folder = (TextView) view.findViewById(R.id.header_item);
		itemHolder.count = (TextView) view.findViewById(R.id.count);
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

		EventListItemFolderHolder holder = null;
		if (convertView == null
				|| (convertView != null && convertView.getTag() instanceof EventListItemFolderHolder) == false) {
			convertView = inflater.inflate(R.layout.folder_item, null);
			holder = getFolderViewHolder(item, convertView);
			convertView.setTag(holder);
		} else {
			holder = (EventListItemFolderHolder) convertView.getTag();
		}

		holder.folder
				.setText(Utils.highlight(item.folder.getName(), highlight));
		holder.count.setText(String.valueOf(item.folder.size()));
		return convertView;
	}

	@Override
	public RecordingListItem getItem(int position) {
		return (RecordingListItem) super.getItem(position);
	}
	//
	// protected void addSuper(RecordingListItem item) {
	// super.addSuper(item);
	// }
	//
	// protected void clearSuper() {
	// super.clear();
	// }

}