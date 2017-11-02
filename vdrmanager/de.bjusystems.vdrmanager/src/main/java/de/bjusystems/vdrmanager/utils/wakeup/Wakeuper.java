package de.bjusystems.vdrmanager.utils.wakeup;

import android.content.Context;

/**
 * @author lado
 * Interface to implement several wakeup methods
 */
public interface Wakeuper {
	
	/**
	 * 
	 * 
	 * 
	 * @param context
	 * @throws Exception if the wakeup process is not ended ok. Please note, on a.e. WOL, there can
	 * not be no guarantee, that the host has been woken up.
	 */
	void wakeup(Context context) throws Exception;
}
