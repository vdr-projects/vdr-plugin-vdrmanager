package de.bjusystems.vdrmanager.tasks;

import android.app.Activity;
import de.bjusystems.vdrmanager.gui.SvdrpProgressDialog;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;

public abstract class AsyncProgressTask<Result> {

  class AsyncProgress extends SvdrpProgressDialog<Result> {

    public AsyncProgress(final Activity activity,
        final SvdrpClient<Result> client) {
      super(activity, client);
    }

    @Override
    public void svdrpEvent(final SvdrpEvent event) {
      super.svdrpEvent(event);
        AsyncProgressTask.this.svdrpEvent(event);
      switch (event) {
      case ABORTED:
      case CONNECT_ERROR:
      case CONNECTION_TIMEOUT:
      case ERROR:
      case LOGIN_ERROR:
      case FINISHED_ABNORMALY:
      case FINISHED_SUCCESS:
      case CACHE_HIT:
        AsyncProgressTask.this.finished(event);
        break;
      }
    }

      @Override
      public void svdrpEvent(SvdrpEvent event, Throwable th) {
          super.svdrpEvent(event,th);
         AsyncProgressTask.this.svdrpEvent(event, th);
      }
  }

  Activity activity;
  SvdrpClient<Result> client;

  public AsyncProgressTask(final Activity activity,
      final SvdrpClient<Result> client) {
    this.activity = activity;
    this.client = client;
  }

  public void start() {

    // delete timer
    /*
     * final SetTimerClient client = new SetTimerClient(timer, true) {
     *
     * @Override public int getProgressTextId() { return
     * R.string.progress_timer_delete; } };
     */
    final SvdrpAsyncTask<Result, SvdrpClient<Result>> task = new SvdrpAsyncTask<Result, SvdrpClient<Result>>(
        client);
    final AsyncProgress progress = new AsyncProgress(activity, client);
    task.addSvdrpListener(progress);
    task.addSvdrpExceptionListener(progress);
    task.run();
  }

  public void svdrpEvent(final SvdrpEvent event){

  }


    public void svdrpEvent(final SvdrpEvent event, Throwable th){

    }


    public abstract void finished(SvdrpEvent event);
}
