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
		// sections = new Object[groups.size()];
		// for(int i = 0; i < groups.size(); ++i){
		// String g = groups.get(i);
		// /if(g.length() > 0){
		// sections[i] = g.subSequence(0, 1);
		// } else {
		// sections[i] = "";
		// }
		// }
		notifyDataSetChanged();

	}

	// public ChannelAdapter(final Context context) {
	// super(context, R.layout.channel_item);
	// inflater = LayoutInflater.from(context);
	// // items = new ArrayList<Channel>();
	// }

	// @Override
	// public View getView(final int position, final View convertView,
	// final ViewGroup parent) {
	//
	// ChannelHolder itemHolder = new ChannelHolder();
	//
	// // recycle view?
	// View view = convertView;
	// if (view == null) {
	// view = inflater.inflate(R.layout.channel_item, null);
	// itemHolder = new ChannelHolder();
	//
	// itemHolder.name = (TextView) view.findViewById(R.id.channel_name);
	// itemHolder.type = (ImageView) view.findViewById(R.id.channel_type);
	//
	// view.setTag(itemHolder);
	// } else {
	// itemHolder = (ChannelHolder) view.getTag();
	// }
	//
	// // get item
	// final Channel item = getItem(position);
	//
	// // fill item
	// if (item.isGroupSeparator()) {
	// view.setPadding(view.getPaddingLeft(), 0, view.getPaddingRight(), 0);
	// view.setBackgroundColor(Color.DKGRAY);
	// itemHolder.type.setVisibility(View.GONE);
	// itemHolder.name.setVisibility(View.VISIBLE);
	// itemHolder.name.setText(item.getName());
	// itemHolder.name.setPadding(0, 0, 0, 0);
	// } else {
	// view.setBackgroundColor(Color.BLACK);
	// itemHolder.type.setVisibility(View.VISIBLE);
	// itemHolder.type.setVisibility(View.VISIBLE);
	// itemHolder.name.setText(item.toString());
	// }
	//
	// return view;
	// }

	//
	// public void addItem(final Channel channel) {
	// items.add(channel);
	// }
	//
	// public void clearItems() {
	// items.clear();
	// }

	// public int getPositionForSection(int section) {
	// // Log.v("getPositionForSection", ""+section);
	// String letter = sections[section];
	//
	// return alphaIndexer.get(letter);
	// }
	//
	// public int getSectionForPosition(int position) {
	//
	// // you will notice it will be never called (right?)
	// Log.v("getSectionForPosition", "called");
	// return 0;
	// }
	//
	// public Object[] getSections() {
	//
	// if (sections == null) {
	// alphaIndexer = new HashMap<String, Integer>();
	// int size = getCount();
	// for (int i = 0; i < size; ++i) {
	// Channel element = getItem(i);
	// alphaIndexer.put(element.getName().substring(0, 1), i);
	// // We store the first letter of the word, and its index.
	// // The Hashmap will replace the value for identical keys are
	// // putted in
	// }
	//
	// // now we have an hashmap containing for each first-letter
	// // sections(key), the index(value) in where this sections begins
	//
	// // we have now to build the sections(letters to be displayed)
	// // array .it must contains the keys, and must (I do so...) be
	// // ordered alphabetically
	//
	// Set<String> keys = alphaIndexer.keySet(); // set of letters ...sets
	// // cannot be sorted...
	//
	// Iterator<String> it = keys.iterator();
	// ArrayList<String> keyList = new ArrayList<String>(); // list can be
	// // sorted
	//
	// while (it.hasNext()) {
	// String key = it.next();
	// keyList.add(key);
	// }
	//
	// Collections.sort(keyList);
	//
	// sections = new String[keyList.size()]; // simple conversion to an
	// // array of object
	// keyList.toArray(sections);
	// }
	//
	// return sections; // to string will be called each object, to display
	// // the letter
	// }

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

		//
		// // fill item
		// if (item.isGroupSeparator()) {
		// view.setPadding(view.getPaddingLeft(), 0, view.getPaddingRight(), 0);
		// view.setBackgroundColor(Color.DKGRAY);
		// itemHolder.type.setVisibility(View.GONE);
		// itemHolder.name.setVisibility(View.VISIBLE);
		// itemHolder.name.setText(item.getName());
		// itemHolder.name.setPadding(0, 0, 0, 0);
		// } else {
		view.setBackgroundColor(Color.BLACK);

		String name = item.getName();
		if (showChannelNumber) {
			name = item.getNumber() + " - " + name;
		}
		itemHolder.name.setText(name);
		// }

		return view;

		// if (convertView == null) {
		// LayoutInflater infalInflater = (LayoutInflater) context
		// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// convertView = infalInflater.inflate(R.layout.child_layout, null);
		// }
		// TextView tv = (TextView) convertView.findViewById(R.id.tvChild);
		// tv.setText("   " + vehicle.getName());
		//
		// // Depending upon the child type, set the imageTextView01
		// tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		// // if (vehicle instanceof Car) {
		// // tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.car, 0, 0,
		// 0);
		// // } else if (vehicle instanceof Bus) {
		// // tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.bus, 0, 0,
		// 0);
		// // } else if (vehicle instanceof Bike) {
		// // tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.bike, 0, 0,
		// 0);
		// // }
		// return convertView;
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
		ChannelHolder itemHolder = new ChannelHolder();

		// recycle view?
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.group_layout, null);
			itemHolder = new ChannelHolder();

			itemHolder.name = (TextView) view.findViewById(R.id.channel_name);
			// itemHolder.type = (ImageView)
			// view.findViewById(R.id.channel_type);

			view.setTag(itemHolder);
		} else {
			itemHolder = (ChannelHolder) view.getTag();
		}
		itemHolder.name.setText(group);
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

	// public int getPositionForSection(int arg0) {
	// return arg0;
	// }
	//
	// public int getSectionForPosition(int arg0) {
	// return arg0;
	// }
	//
	// public Object[] getSections() {
	// return sections;
	// }

}