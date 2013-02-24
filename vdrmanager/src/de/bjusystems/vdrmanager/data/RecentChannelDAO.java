package de.bjusystems.vdrmanager.data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class RecentChannelDAO extends BaseDaoImpl<RecenteChannel, String> {

	private static final String TAG = RecentChannelDAO.class.getName();

	public RecentChannelDAO(ConnectionSource connectionSource)
			throws SQLException {
		super(connectionSource, RecenteChannel.class);
	}

	public List<RecenteChannel> loadByLastAccess(long max) {
		try {
			return queryBuilder().orderBy("lastAccess", false).limit(max).query();
		} catch (SQLException e) {
			Log.w(TAG, e.getMessage(), e);
			return new ArrayList<RecenteChannel>(0);
		}
	}

	public List<RecenteChannel> loadByRecentUse(long max) {
		try {
			return queryBuilder().orderBy("count", false).limit(max).query();
		} catch (SQLException e) {
			Log.w(TAG, e.getMessage(), e);
			return new ArrayList<RecenteChannel>(0);
		}
	}

	public RecenteChannel queryForId(String id) {
		try {
			return super.queryForId(id);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public List<Channel> getRecentChannels(Map<String, Channel> all, List<RecenteChannel> recents){

		List<Channel> filtered = new ArrayList<Channel>();
		for(RecenteChannel rc : recents){
			Channel c = all.get(rc.getChannelId());
			if(c == null){
				try {
					delete(rc);
				} catch (SQLException e) {
					Log.w(TAG, e.getMessage(), e);
				}
			} else {
				filtered.add(c);
			}
		}
		return filtered;
	}

	public CreateOrUpdateStatus createOrUpdate(RecenteChannel data) {
		try {
			return super.createOrUpdate(data);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public void hit(String channelId) {
		RecenteChannel rc = queryForId(channelId);

		if (rc == null) {
			rc = new RecenteChannel();
			rc.setChannelId(channelId);
		}
		rc.touch();
		rc.incUse();
		createOrUpdate(rc);
	}

}
