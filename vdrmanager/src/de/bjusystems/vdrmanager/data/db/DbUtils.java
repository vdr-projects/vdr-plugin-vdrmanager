package de.bjusystems.vdrmanager.data.db;

/**
 * @author flx
 */
public class DbUtils {

	/**
	 * Default Constructor.
	 */
	private DbUtils() {

	}

	/**
	 * SQL AND for where clause.
	 * 
	 * @param arg0
	 *            arg0
	 * @param arg1
	 *            arg1
	 * @return ( arg0 ) AND ( arg1 )
	 */
	public static String sqlAnd(final String arg0, final String arg1) {
		if (arg0 != null && arg1 != null) {
			return "( " + arg0 + " ) AND ( " + arg1 + " )";
		} else if (arg0 != null) {
			return arg0;
		} else if (arg1 != null) {
			return arg1;
		} else {
			return null;
		}
	}
}
