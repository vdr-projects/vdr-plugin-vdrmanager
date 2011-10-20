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
		MODIFY("M");//
		private String command;
		private TimerOperation(String command){
			this.command = command;
		}
		public String getCommand(){
			return this.command;
		}
		
	}
	
	/** channel names for timer */
	Timer timer;
	/** timer should be deleted */
	private TimerOperation timerOperation;

	/**
	 * Constructor
	 * @param host host
	 * @param port port
	 * @param ssl use ssl
	 */
	public SetTimerClient(final Timer timer, TimerOperation op) {
		super();
		this.timer = timer;
		this.timerOperation = op;
	}

	/**
	 * Starts the request
	 */
	@Override
	public void run() throws SvdrpException {

		final StringBuilder command = new StringBuilder();

		command.append("timer ");
		command.append(timerOperation.getCommand());
		
		command.append(timer.getNumber());
		command.append(" ");
		command.append(timer.toCommandLine(timerOperation));

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

