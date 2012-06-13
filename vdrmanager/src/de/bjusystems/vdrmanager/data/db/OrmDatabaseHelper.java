package de.bjusystems.vdrmanager.data.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.AndroidCompiledStatement;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import de.bjusystems.vdrmanager.data.Vdr;

/**
 * Database helper class used to manage the creation and upgrading of your
 * database. This class also usually provides the DAOs used by the other
 * classes.
 */
public class OrmDatabaseHelper extends OrmLiteSqliteOpenHelper {

	public static final String TAG = OrmDatabaseHelper.class.getName();
	// name of the database file for your application -- change to something
	// appropriate for your app
	public static final String DATABASE_NAME = "vdrmanager.db";
	// any time you make changes to your database objects, you may have to
	// increase the database version
	private static final int DATABASE_VERSION = 2;

	private RuntimeExceptionDao<Vdr, Integer> vdrDAO = null;

	public OrmDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	
	public static String getDataBaseFile(){
		return "/data/data/de.bjusystems.vdrmanager/databases/" + DATABASE_NAME;
	}
	/**
	 * This is called when the database is first created. Usually you should
	 * call createTable statements here to create the tables that will store
	 * your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(OrmDatabaseHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, Vdr.class);
		} catch (SQLException e) {
			Log.e(OrmDatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}

		// // here we try inserting data in the on-create as a test
		// RuntimeExceptionDao<Note, Integer> dao = getSimpleDataDao();
		// long millis = System.currentTimeMillis();
		// // create some entries in the onCreate
		// Note simple = new Note();
		// dao.create(simple);
		// simple = new SimpleData(millis + 1);
		// dao.create(simple);
		// Log.i(DatabaseHelper.class.getName(),
		// "created new entries in onCreate: " + millis);
	}

	private List<ContentValues> backup() throws SQLException {
		List<ContentValues> values = new ArrayList<ContentValues>();
		Cursor c = getVdrCursor();
		try {
			if (c != null && c.getCount() == 0) {
				return values;
			}

			c.move(-1);

			while (c.moveToNext()) {
				ContentValues cv = new ContentValues();
				for (int i = 0; i < c.getColumnCount(); ++i) {
					cv.put(c.getColumnName(i), c.getString(i));
				}
				values.add(cv);
			}
			return values;
		} finally {
			if (c != null && c.isClosed() == false) {
				c.close();
			}
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
		// try {
		Log.i(TAG, "onUpgrade to " + newVersion + " from " + oldVersion);
		// List<ContentValues> b = backup();
		// Log.i(OrmDatabaseHelper.class.getName(), "backup");
		// Dao<Vdr, Integer> dao = getDao(Vdr.class);
		// List<Vdr> backup = dao.queryForAll();
		// if (backup != null) {
		// Log.i(OrmDatabaseHelper.class.getName(),
		// "backuped " + backup.size() + " instances");
		// }
		// TableUtils.dropTable(connectionSource, Vdr.class, true);
		// after we drop the old databases, we create the new ones
		// onCreate(db, connectionSource);
		if (oldVersion == 1) {

			String alter = "ALTER TABLE vdr add livePort INTEGER";
			Log.i(TAG, "exec: " + alter);
			int count = getVdrDAO().executeRaw(alter);
			Log.i(TAG, "alterd " + count + " rows");
			
			alter = "ALTER TABLE vdr add enableRecStreaming SMALLINT";
			Log.i(TAG, "exec: " + alter);
			count = getVdrDAO().executeRaw(alter);
			Log.i(TAG, "alterd " + count + " rows");
			
			alter = "ALTER TABLE vdr add recStreamMethod VARCHAR";
			Log.i(TAG, "exec: " + alter);
			count = getVdrDAO().executeRaw(alter);
			Log.i(TAG, "alterd " + count + " rows");

			
			String update = "Update vdr set livePort = 8008, enableRecStreaming = 0,  recStreamMethod = 'vdr-live'";
			Log.i(TAG, "exec: " + update);
			count = getVdrDAO().executeRaw(update);
			Log.i(TAG, "alterd " + count + " rows");
		}

		// if (backup != null) {
		// for (Vdr v : backup) {
		// dao.create(v);
		// Log.i(OrmDatabaseHelper.class.getName(),
		// "recovered " + v.getName());
		// }
		// }

		// getVdrDAO().updateRaw(statement, arguments)

		// } catch (SQLException e) {
		// Log.e(OrmDatabaseHelper.class.getName(), "Can't drop databases", e);
		// throw new RuntimeException(e);
		// }
	}

	/**
	 * Returns the Database Access Object (DAO) for our Label class. It will
	 * create it or just give the cached value.
	 */
	public RuntimeExceptionDao<Vdr, Integer> getVdrDAO() {
		if (vdrDAO == null) {
			vdrDAO = getRuntimeExceptionDao(Vdr.class);
		}
		return vdrDAO;
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		vdrDAO = null;
	}

	public Cursor getVdrCursor() throws SQLException {
		QueryBuilder<Vdr, Integer> qb = getVdrDAO().queryBuilder();

		PreparedQuery<Vdr> preparedQuery = qb.prepare();

		AndroidCompiledStatement compiledStatement = (AndroidCompiledStatement) preparedQuery
				.compile(getConnectionSource().getReadOnlyConnection(),
						StatementType.SELECT);

		return compiledStatement.getCursor();
	}
}