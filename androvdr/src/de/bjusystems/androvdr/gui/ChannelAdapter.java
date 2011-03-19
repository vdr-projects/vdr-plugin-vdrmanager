package de.bjusystems.androvdr.gui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.bjusystems.androvdr.data.Channel;
import de.bjusystems.androvdr.R;

class ChannelAdapter extends ArrayAdapter<Channel> {

	private final LayoutInflater inflater;
	private final List<Channel> items;

	public ChannelAdapter(final Context context) {
		super(context, R.layout.channel_item);
		inflater = LayoutInflater.from(context);
		items = new ArrayList<Channel>();
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {

		ChannelHolder itemHolder = new ChannelHolder();

		// recycle view?
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.channel_item, null);
			itemHolder = new ChannelHolder();

			itemHolder.name = (TextView) view.findViewById(R.id.channel_name);
			itemHolder.type = (ImageView) view.findViewById(R.id.channel_type);

			view.setTag(itemHolder);
		} else {
			itemHolder = (ChannelHolder) view.getTag();
		}

		// get item
		final Channel item = getItem(position);

		// fill item
		if (item.isGroupSeparator()) {
			view.setPadding(view.getPaddingLeft(), 0, view.getPaddingRight(), 0);
			view.setBackgroundColor(Color.DKGRAY);
			itemHolder.type.setVisibility(View.GONE);
			itemHolder.name.setVisibility(View.VISIBLE);
			itemHolder.name.setText(item.getName());
			itemHolder.name.setPadding(0, 0, 0, 0);
		} else {
			view.setBackgroundColor(Color.BLACK);
			itemHolder.type.setVisibility(View.VISIBLE);
			itemHolder.type.setVisibility(View.VISIBLE);
			itemHolder.name.setText(item.toString());
		}

		return view;
	}

	public void addItem(final Channel channel) {
		items.add(channel);
	}

	public void clearItems() {
		items.clear();
	}
}