package de.bjusystems.vdrmanager.utils.svdrp;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Timer;

/**
 * Class for retrieving informations about the running program
 * @author bju
 *
 */
public class SetTimerClient extends SvdrpClient<Timer> {

  public enum TimerOperation {
    CREATE("C"),//
    DELETE("D"),//
    MODIFY("M"),//
    TOGGLE("T"),//
    ;
    private String command;
    private TimerOperation(final String command){
      this.command = command;
    }
    public String getCommand(){
      return this.command;
    }

  }

  /** channel names for timer */
  Timer newTimer;

  Timer oldTimer;

  /** timer should be deleted */
  private final TimerOperation timerOperation;

  /**
   * @param newTimer Das was modifiziert angelegt wird
   */
  public SetTimerClient(final Timer newTimer, final TimerOperation op, final CertificateProblemListener certificateProblemListener) {
    this(newTimer, null, op, certificateProblemListener);
  }

  /**
   * @param newTimer
   * @param oldTimer this is original Timer, if any (modify)
   * @param op
   */
  public SetTimerClient(final Timer newTimer, final Timer oldTimer, final TimerOperation op, final CertificateProblemListener certificateProblemListener) {
    super(certificateProblemListener);
    this.newTimer = newTimer;
    this.oldTimer = oldTimer;
    this.timerOperation = op;
  }


  /**
   * Starts the request
   */
  @Override
  public void run()   {

    final StringBuilder command = new StringBuilder();

    command.append("timer ");
    command.append(timerOperation.getCommand());
    //command.append(oldTimer.getNumber());
    command.append(" ");
    command.append(newTimer.toCommandLine());
    if(timerOperation == TimerOperation.MODIFY){
      command.append("#|#|#").append(oldTimer.toCommandLine());
    }
    //timer D 1:1:2011-11-11:1513:1710:50:99:Mirrors 2
    //timer C 1:1:2011-11-11:2223:2250:50:99:Zapping
    //timer T 0:1:2011-11-11:2013:2230:50:99:So spielt das Leben
    //timer M
    runCommand(command.toString());
  }

  @Override
  public Timer parseAnswer(final String line) {
    return new Timer(line);
  }

  @Override
  public int getProgressTextId() {
    return R.string.progress_timer_save;
  }

}

