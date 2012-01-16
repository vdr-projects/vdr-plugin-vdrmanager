package de.bjusystems.vdrmanager.utils.crypt;

public class NativeDES {
	static {
	    System.loadLibrary("native_des");
	  }
	/**
	 * @param str
	 * @param key
	 * @return
	 */
	public native String encrypt( String str, String key);

	
	/**
	 * @param str
	 * @param key
	 * @return
	 */
	public native String decrypt(String str, String key);
}
