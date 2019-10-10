package de.bjusystems.vdrmanager.utils;

import android.content.Context;
import android.content.Intent;

import java.util.logging.Logger;

/**
 * The type Vdr manager exception handler.
 */
public class VdrManagerExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Context context;

    /**
     * The Log.
     */
    Logger log = Logger.getLogger(VdrManagerExceptionHandler.class.getSimpleName());

    private Thread.UncaughtExceptionHandler previous;

    private static VdrManagerExceptionHandler INSTANCE;

    /**
     * The constant RECEIVER.
     */
    public static final String RECEIVER = "herrlado@gmail.com";

    private VdrManagerExceptionHandler(Context context,
                                       Thread.UncaughtExceptionHandler previous) {
        this.context = context;
        this.previous = previous;

    }

    /**
     * Get vdr manager exception handler.
     *
     * @param context  the context
     * @param previous the previous
     * @return the vdr manager exception handler
     */
    public static VdrManagerExceptionHandler get(Context context,
                                                 Thread.UncaughtExceptionHandler previous) {

        if (INSTANCE != null) {
            return INSTANCE;
        }
        INSTANCE = new VdrManagerExceptionHandler(context, previous);

        return INSTANCE;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable e) {

        log.warning(e.getMessage());

        StackTraceElement[] arr = e.getStackTrace();
        String report = e.toString() + "\n\n";
        report += "--------- Stack trace ---------\n\n";
        for (int i = 0; i < arr.length; i++) {
            report += "    " + arr[i].toString() + "\n";
        }
        report += "-------------------------------\n\n";

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        report += "--------- Cause ---------\n\n";
        Throwable cause = e.getCause();
        if (cause != null) {
            report += cause.toString() + "\n\n";
            arr = cause.getStackTrace();
            for (int i = 0; i < arr.length; i++) {
                report += "    " + arr[i].toString() + "\n";
            }
        }
        report += "-------------------------------\n\n";

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        String subject = "Force Close Report VDR Manager";
        String body = "Sendlog for VDR Manager\n\n\n" + report
                + "\n\n";

        sendIntent.putExtra(Intent.EXTRA_EMAIL,
                new String[]{RECEIVER});
        sendIntent.putExtra(Intent.EXTRA_TEXT, body);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.setType("message/rfc822");


        context.startActivity(Intent.createChooser(sendIntent,
                "Force Close Report VDR Manager"));

        previous.uncaughtException(thread, e);
    }
}
