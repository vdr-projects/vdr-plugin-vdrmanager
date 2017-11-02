package de.bjusystems.vdrmanager.data;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(daoClass=RecentChannelDAO.class)
public class RecenteChannel {

	@DatabaseField(id=true, generatedId = false)
	private String channelId;

	@DatabaseField
	private long count = 0;

	@DatabaseField
	private Date lastAccess;

	public Date getLastAccess() {
		return lastAccess;
	}

	public void setLastAccess(Date lastAccess) {
		this.lastAccess = lastAccess;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public void incUse(){
		count++;
	}

	public void touch(){
		lastAccess = new  Date();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("[id="+channelId +", count="+count+", lastAccess=" + lastAccess);
		return super.toString();
	}
}
