package de.bjusystems.vdrmanager.tasks;

import android.app.Activity;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.utils.svdrp.ChannelClient;

public abstract class ChannelsTask  extends AsyncProgressTask<Channel> {
  public ChannelsTask(final Activity activity, final ChannelClient client) {
    super(activity, client);
  }
}
