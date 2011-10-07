package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Preferences;

class ChannelAdapter extends BaseExpandableListAdapter implements Filterable// ,
																			// SectionIndexer
{

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	private Context context;

	Map<String, ArrayList<Channel>> channels = new HashMap<String, ArrayList<Channel>>();

	ArrayList<String> groups = new ArrayList<String>();

	private boolean showChannelNumber;

	public ChannelAdapter(Context context) {
		this.context = context;
		inflater = LayoutInflater.from(context);
		this.showChannelNumber = Preferences.get().isShowChannelNumbers();
	}

	private LayoutInflater inflater;

	private int groupBy = -1;

	public void fill(ArrayList<String> groups,
			Map<String, ArrayList<Channel>> data, int groupBy) {
		this.groupBy = groupBy;
		this.groups.clear();
		this.groups.addAll(groups);
		channels.clear();
		channels.putAll(data);
		notifyDataSetChanged();

	}

	public Object getChild(int groupPosition, int childPosition) {
		String gn = groups.get(groupPosition);
		ArrayList<Channel> channels = this.channels.get(gn);
		return channels.get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	// Return a child view. You can load your custom layout here.

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		Channel item = (Channel) getChild(groupPosition, childPosition);

		ChannelHolder itemHolder = new ChannelHolder();

		// recycle view?
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.child_layout, null);
			itemHolder = new ChannelHolder();

			itemHolder.name = (TextView) view.findViewById(R.id.channel_name);
			itemHolder.type = (ImageView) view.findViewById(R.id.channel_type);

			view.setTag(itemHolder);
		} else {
			itemHolder = (ChannelHolder) view.getTag();
		}

		view.setBackgroundColor(Color.BLACK);

		String name = item.getName();
		if (showChannelNumber) {
			name = item.getNumber() + " - " + name;
		}
		itemHolder.name.setText(name);

		return view;
	}

	public int getChildrenCount(int groupPosition) {
		String gn = groups.get(groupPosition);
		ArrayList<Channel> channels = this.channels.get(gn);
		return channels.size();
	}

	public Object getGroup(int groupPosition) {
		return groups.get(groupPosition);
	}

	public int getGroupCount() {
		return groups.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	// Return a group view. You can load your custom layout here.

	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		String group = (String) getGroup(groupPosition);
		GroupHolder itemHolder = new GroupHolder();

		// recycle view?
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.group_layout, null);
			itemHolder = new GroupHolder();

			itemHolder.name = (TextView) view.findViewById(R.id.group_name);
			itemHolder.count = (TextView) view.findViewById(R.id.channel_count);
			// itemHolder.type = (ImageView)
			// view.findViewById(R.id.channel_type);

			view.setTag(itemHolder);
		} else {
			itemHolder = (GroupHolder) view.getTag();
		}
		itemHolder.name.setText(group);
		itemHolder.count.setText(String.valueOf(this.channels.get(group).size()));
		return view;

	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isChildSelectable(int arg0, int arg1) {
		return true;
	}

	public Filter getFilter() {
		return new Filter() {

			private ArrayList<Channel> ALL = channels.get(0);

			@Override
			protected FilterResults performFiltering(CharSequence arg0) {
				ArrayList<Channel> c = new ArrayList<Channel>(ALL);
				Iterator<Channel> ci = c.iterator();
				while (ci.hasNext()) {
					if (ci.next().getName().startsWith(arg0.toString()) == false) {
						ci.remove();
					}
				}
				FilterResults fr = new FilterResults();
				return fr;
			}

			@Override
			protected void publishResults(CharSequence arg0, FilterResults arg1) {
				notifyDataSetChanged();
			}
		};
	}
}
