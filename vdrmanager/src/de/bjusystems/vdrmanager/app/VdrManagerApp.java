package de.bjusystems.vdrmanager.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Epg;
import de.bjusystems.vdrmanager.data.EpgSearchParams;
import de.bjusystems.vdrmanager.data.Timer;

public class VdrManagerApp extends Application {

	public enum EpgListState {
		EPG_TIME,
		EPG_CHANNEL,
		EPG_SEARCH
	}

	private EpgListState epgListState;
	private Epg currentEvent;
	private Timer currentTimer;
	private Channel currentChannel;
	
	private ArrayList<Epg> currentEpgList = new ArrayList<Epg>();
	public ArrayList<Epg> getCurrentEpgList() {
		return currentEpgList;
	}

	public void setCurrentEpgList(ArrayList<Epg> currentEpgList) {
		this.currentEpgList = currentEpgList;
	}

	private EpgSearchParams currentSearch;
	private Class<? extends Activity> nextActivity;
	private final List<Activity> activitiesToFinish = new ArrayList<Activity>();
	private boolean reload;

	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	public void clear() {
		this.currentEvent = null;
		this.currentTimer = null;
		this.currentChannel = null;
		this.currentSearch = null;
		this.currentEpgList = null;
		this.epgListState = EpgListState.EPG_TIME;
	}

	public Epg getCurrentEvent() {
		return currentEvent;
	}

	public void setCurrentEvent(final Epg currentEvent) {
		clear();
		this.currentEvent = currentEvent;
		if (currentEvent.getTimer() != null) {
			this.currentTimer = currentEvent.getTimer();
		} else {
			this.currentTimer = new Timer(currentEvent);
		}
	}

	public Timer getCurrentTimer() {
		return currentTimer;
	}

	public void setCurrentTimer(final Timer currentTimer) {
		clear();
		this.currentTimer = currentTimer;
	}

	public Channel getCurrentChannel() {
		return currentChannel;
	}

	public void setCurrentChannel(final Channel currentChannel) {
		clear();
		this.currentChannel = currentChannel;
		this.epgListState = EpgListState.EPG_CHANNEL;
	}


	public EpgSearchParams getCurrentSearch() {
		return currentSearch;
	}

	public void setCurrentSearch(final EpgSearchParams currentSearch) {
		clear();
		this.currentSearch = currentSearch;
		this.epgListState = EpgListState.EPG_SEARCH;
	}

	public EpgListState getEpgListState() {
		return epgListState;
	}

	public Class<? extends Activity> getNextActivity() {
		return nextActivity;
	}

	public void setNextActivity(final Class<? extends Activity> nextActivity) {
		this.nextActivity = nextActivity;
	}

	public List<Activity> getActivitiesToFinish() {
		return activitiesToFinish;
	}

	public void clearActivitiesToFinish() {
		activitiesToFinish.clear();
	}

	public void addActivityToFinish(final Activity activityToFinish) {
		activitiesToFinish.add(activityToFinish);
	}

	public void finishActivities() {
		for(final Activity activity : activitiesToFinish) {
			activity.finish();
		}
		activitiesToFinish.clear();
	}

	public boolean isReload() {
		return reload;
	}

	public void setReload(final boolean reload) {
		this.reload = reload;
	}
}
