package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Pair;
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
import de.bjusystems.vdrmanager.utils.svdrp.ChannelClient;

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
			itemHolder.aux = (TextView) view.findViewById(R.id.channel_aux);

			view.setTag(itemHolder);
		} else {
			itemHolder = (ChannelHolder) view.getTag();
		}

		//view.setBackgroundColor(Color.BLACK);

		CharSequence name = item.getName();
		name = Utils.highlight(String.valueOf(name), channelFilter);

		if (showChannelNumber) {
			name = item.getNumber() + " - " + name;
		}
		itemHolder.name.setText(name);

		if (groupBy == ChannelListActivity.MENU_PROVIDER) {
			itemHolder.aux.setText(item.getGroup());
		} else if(groupBy == ChannelListActivity.MENU_GROUP) {
			itemHolder.aux.setText(item.getProvider());
		} else {
			itemHolder.aux.setText(item.getProvider());
		}

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

		int channelCount = this.channels.get(group).size();

		CharSequence groupDisplay = Utils.highlight(group, groupFilter);

		ChannelHolder itemHolder = new ChannelHolder();

		// recycle view?
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.group_layout, null);
			itemHolder = new ChannelHolder();

			itemHolder.name = (TextView) view.findViewById(R.id.group_name);
			itemHolder.aux = (TextView) view.findViewById(R.id.channel_count);
			// itemHolder.type = (ImageView)
			// view.findViewById(R.id.channel_type);

			view.setTag(itemHolder);
		} else {
			itemHolder = (ChannelHolder) view.getTag();
		}
		itemHolder.name.setText(groupDisplay);
		itemHolder.aux.setText(String.valueOf(channelCount));

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

	private String groupFilter = null;

	private String channelFilter = null;

	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence arg0) {
				FilterResults fr = new FilterResults();
				String q = String.valueOf(arg0).toLowerCase();
				ArrayList<String> groups = new ArrayList<String>();
				HashMap<String, ArrayList<Channel>> groupChannels = new HashMap<String, ArrayList<Channel>>();
				if (groupBy == ChannelListActivity.MENU_GROUP) {
					groupFilter = String.valueOf(arg0).toLowerCase();
					for (String str : ChannelClient.getChannelGroups()) {
						String g = str.toLowerCase();
						if (g.indexOf(q) != -1) {
							groups.add(str);
							groupChannels.put(str, ChannelClient
									.getGroupChannels().get(str));
						}
					}
				
				} else if (groupBy == ChannelListActivity.MENU_PROVIDER) {
					groupFilter = String.valueOf(arg0).toLowerCase();
					for (Map.Entry<String, ArrayList<Channel>> p : ChannelClient
							.getProviderChannels().entrySet()) {
						String pr = p.getKey();
						String g = pr.toLowerCase();
						if (g.indexOf(q) != -1) {
							groups.add(pr);
							groupChannels.put(pr, p.getValue());
						}
					}

				} else {
					channelFilter = String.valueOf(arg0).toLowerCase();
					ArrayList<Channel> channels = new ArrayList<Channel>();
					for(Channel c : ChannelClient.getChannels()){
						String cname = c.getName();
						String tmp = cname.toLowerCase();
						if(tmp.indexOf(channelFilter) != -1){
							channels.add(c);
						}
					}
					String fakeGroup = context.getString(R.string.groupby_name_all_channels_group);
					groups.add	(fakeGroup);
					groupChannels.put(fakeGroup, channels);
				}
				fr.values = Pair.create(groups, groupChannels);
				return fr;
			}

			@Override
			protected void publishResults(CharSequence arg0, FilterResults arg1) {
				Pair<ArrayList<String>, HashMap<String, ArrayList<Channel>>> res = (Pair<ArrayList<String>, HashMap<String, ArrayList<Channel>>>) arg1.values;
				fill(res.first, res.second, groupBy);

			}
		};
	}
}
