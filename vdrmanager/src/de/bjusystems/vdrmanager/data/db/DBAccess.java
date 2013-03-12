package de.bjusystems.vdrmanager.data.db;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.RecentChannelDAO;
import de.bjusystems.vdrmanager.data.RecenteChannel;
import de.bjusystems.vdrmanager.data.Vdr;

/**
 * Database helper class used to manage the creation and upgrading of your
 * database. This class also usually provides the DAOs used by the other
 * classes.
 */
public class DBAccess extends OrmLiteSqliteOpenHelper {

	public static final String TAG = DBAccess.class.getName();
	// name of the database file for your application -- change to something
	// appropriate for your app
	public static final String DATABASE_NAME = "vdrmanager.db";
	// any time you make changes to your database objects, you may have to
	// increase the database version
	// Version 3 since 0.6
	private static final int DATABASE_VERSION = 3;

	private RuntimeExceptionDao<Vdr, Integer> vdrDAO = null;

	private RecentChannelDAO recentChannelDAO = null;

	public static String getDataBaseFile() {
		return "/data/data/de.bjusystems.vdrmanager/databases/" + DATABASE_NAME;
	}

	public DBAccess(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION,
				R.raw.ormlite_config);
	}

	/**
	 * This is called when the database is first created. Usually you should
	 * call createTable statements here to create the tables that will store
	 * your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DBAccess.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, Vdr.class);
			TableUtils.createTable(connectionSource, RecenteChannel.class);
		} catch (SQLException e) {
			Log.e(DBAccess.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher
	 * version number. This allows you to adjust the various data to match the
	 * new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
			int oldVersion, int newVersion) {
		try {

			// Log.i(DBAccess.class.getName(), "onUpgrade");
			// TableUtils.dropTable(connectionSource, Vdr.class, true);
			// after we drop the old databases, we create the new ones
			// onCreate(db, connectionSource);

			if (oldVersion < 3) {
				TableUtils.createTable(connectionSource, RecenteChannel.class);
			}

		} catch (SQLException e) {
			Log.e(DBAccess.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		vdrDAO = null;
	}

	static volatile DBAccess helper;
	static volatile boolean created = false;
	static volatile boolean destroyed = false;

	/**
	 * Get a helper for this action.
	 */
	public static DBAccess get(Context ctx) {

		if (helper == null) {
			helper = getHelperInternal(ctx);
			created = true;
		}

		if (helper == null) {
			if (!created) {
				throw new IllegalStateException(
						"A call has not been made to onCreate() yet so the helper is null");
			} else if (destroyed) {
				throw new IllegalStateException(
						"A call to onDestroy has already been made and the helper cannot be used after that point");
			} else {
				throw new IllegalStateException(
						"Helper is null for some unknown reason");
			}
		} else {
			return helper;
		}
	}

	/**
	 * Get a connection source for this action.
	 */
	public ConnectionSource getConnectionSource(Context ctx) {
		return get(ctx).getConnectionSource();
	}

	/**
	 * This is called internally by the class to populate the helper object
	 * instance. This should not be called directly by client code unless you
	 * know what you are doing. Use {@link #getHelper()} to get a helper
	 * instance. If you are managing your own helper creation, override this
	 * method to supply this activity with a helper instance.
	 *
	 * <p>
	 * <b> NOTE: </b> If you override this method, you most likely will need to
	 * override the {@link #releaseHelper(OrmLiteSqliteOpenHelper)} method as
	 * well.
	 * </p>
	 */
	protected static DBAccess getHelperInternal(Context context) {
		@SuppressWarnings({ "unchecked", "deprecation" })
		DBAccess newHelper = (DBAccess) OpenHelperManager.getHelper(context,
				DBAccess.class);
		logger.trace("{}: got new helper {} from OpenHelperManager", "",
				newHelper);
		return newHelper;
	}

	/**
	 * Release the helper instance created in
	 * {@link #getHelperInternal(Context)}. You most likely will not need to
	 * call this directly since {@link #onDestroy()} does it for you.
	 *
	 * <p>
	 * <b> NOTE: </b> If you override this method, you most likely will need to
	 * override the {@link #getHelperInternal(Context)} method as well.
	 * </p>
	 */
	protected void releaseHelper(DBAccess helper) {
		OpenHelperManager.releaseHelper();
		logger.trace("{}: helper {} was released, set to null", this, helper);
		DBAccess.helper = null;
	}

	public RuntimeExceptionDao<Vdr, Integer> getVdrDAO() {
		if (vdrDAO == null) {
			vdrDAO = getRuntimeExceptionDao(Vdr.class);
		}
		return vdrDAO;
	}

	public RecentChannelDAO getRecentChannelDAO() {
		if (recentChannelDAO == null) {
			try {
				recentChannelDAO = getDao(RecenteChannel.class);
			} catch (SQLException e) {
				throw new RuntimeException(
						"Could not create RuntimeExcepitionDao for class "
								+ RecenteChannel.class, e);
			}

		}

		return recentChannelDAO;
	}

}
