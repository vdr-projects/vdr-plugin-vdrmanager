package de.bjusystems.vdrmanager.app;

import android.app.Activity;
import android.app.Application;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.EpgSearchParams;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.data.Timer;
import de.bjusystems.vdrmanager.data.Vdr;


public class VdrManagerApp extends Application {


  public VdrManagerApp() {
    super();
    initSessionKeyStore();
  }

  public enum EpgListState {
    EPG_TIME, EPG_CHANNEL, EPG_SEARCH
  }

  private EpgListState epgListState;
  private Event currentEvent;
  private Timer currentTimer;
  private Channel currentChannel;

  public static final Locale SYSTEM_LOCALE = Locale.getDefault();

  private Vdr currentVDR;

  public Vdr getCurrentVDR() {
    return currentVDR;
  }

  public void setCurrentVDR(final Vdr currentVDR) {
    this.currentVDR = currentVDR;
  }

  private List<Event> currentEpgList = new ArrayList<Event>();

  public List<Event> getCurrentEpgList() {
    return currentEpgList;
  }

  public void setCurrentEpgList(final List currentEpgList) {
    this.currentEpgList = currentEpgList;
  }

  private EpgSearchParams currentSearch;
  private Class<? extends Activity> nextActivity;
  private final List<Activity> activitiesToFinish = new ArrayList<Activity>();
  private boolean reload;

  @Override
  public void onCreate() {
    super.onCreate();
    Preferences.init(this);
  }

  public void clear() {
    this.currentEvent = null;
    this.currentTimer = null;
    this.currentChannel = null;
    this.currentSearch = null;
    this.currentEpgList = null;
    this.epgListState = EpgListState.EPG_TIME;
  }

  public Event getCurrentEvent() {
    return currentEvent;
  }

  public void setCurrentEvent(final Event currentEvent) {
    this.currentEvent = currentEvent;
  }

  public Timer getCurrentTimer() {
    return currentTimer;
  }

  public void setCurrentTimer(final Timer currentTimer) {
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

  public boolean isReload() {
    return reload;
  }

  public void setReload(final boolean reload) {
    this.reload = reload;
  }

  /** KeyStore for per app run accepted certificates */
  private KeyStore sessionKeyStore;

  /**
   * Gets the temporary accepted certificates
   * @return KeyStore
   */
  public KeyStore getSessionKeyStore() {
    return sessionKeyStore;
  }

  /**
   * Create a new and empty key store
   */
  public void initSessionKeyStore() {
    try {
      sessionKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      sessionKeyStore.load(null);
    } catch (final Exception e) {
      sessionKeyStore = null;
    }
  }
}
