package de.bjusystems.vdrmanager.data.db;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import de.bjusystems.vdrmanager.app.VdrManagerApp;

public class DataProvider extends ContentProvider {

	/** Tag for output. */
	private static final String TAG = "dp";

	public static final String DATA_ON_SDCARD = "/sdcard/vdrmanager";
	// public static final String INVOICES_DIR = DATA_ON_SDCARD + "/invoices";
	// public static final String EXPORT_DIR = DATA_ON_SDCARD + "/export";
	// public static final String EXPORT_COMPLTED_DIR = EXPORT_DIR +
	// "/completed";
	// public static final String EXPORT_CANCELED_DIR = EXPORT_DIR +
	// "/canceled";

	static {
		File f = new File(DATA_ON_SDCARD);
		if (f.exists()) {
			if (f.isDirectory() == false) {
				Log.w(TAG, DATA_ON_SDCARD + " exists but is not a directory");
			}
		} else {
			if (f.mkdirs() == false) {
				Log.w(TAG, DATA_ON_SDCARD + " exists but is not a directory");
			}
		}
	}

	public static boolean checkDataFolderExists() {
		File f = new File(DATA_ON_SDCARD);
		return f.exists() && f.isDirectory();
	}

	/** Callmeter's package name. */
	public static final String PACKAGE = VdrManagerApp.class.getPackage()
			.getName();

	/** Authority. */
	public static final String AUTHORITY = PACKAGE + "de.bjusystems.vdrmanager.provider";

	/** Name of the {@link SQLiteDatabase}. */
	private static final String DATABASE_NAME = "vdrmanager.db";

	/** Version of the {@link SQLiteDatabase}. */
	private static final int DATABASE_VERSION = 1;

	/** Internal id: terms. */
	private static final int REMUX_PARAMS = 1;

	private static final int REMUX_PARAMS_ID = 2;
	
	private static final int VDR= 3;
	
	private static final int VDR_ID = 4;

	// /** Internal id: terms. */
	// private static final int TERMS_ID = 2;
	//
	// /** Internal id: terms. */
	// private static final int SERVICES = 3;
	//
	// /** Internal id: service id. */
	// private static final int SERVICES_ID = 4;
	//
	// private static final int PRODUCTS = 5;
	//
	// private static final int PRODUCTS_ID = 6;
	//
	// private static final int UOMS_ID = 7;
	//
	// private static final int UOMS = 8;
	//
	// private static final int JOBS = 9;
	//
	// private static final int JOBS_ID = 10;
	//
	// private static final int PRODUCT_CONFIG = 11;
	//
	// private static final int PRODUCT_CONFIG_ID = 12;
	//
	// private static final int PRODUCT_CONFIG_JOB = 13;
	//
	// private static final int SERVICE_CONFIG = 14;
	//
	// private static final int SERVICE_CONFIG_ID = 15;
	//
	// private static final int SERVICE_CONFIG_JOB = 16;
	//
	// private static final int MILEAGE = 17;
	//
	// private static final int MILEAGE_ID = 18;
	//
	// private static final int JOBS_P_S = 19;

	/** {@link UriMatcher}. */
	private static final UriMatcher URI_MATCHER;

	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, "remux_params", REMUX_PARAMS);
		URI_MATCHER.addURI(AUTHORITY, "remux_params/#", REMUX_PARAMS_ID);
		URI_MATCHER.addURI(AUTHORITY, "vdr", VDR);
		URI_MATCHER.addURI(AUTHORITY, "vdr/#", VDR_ID);
	}

	/** {@link DatabaseHelper}. */
	private DatabaseHelper mOpenHelper;

	public static final class RemuxParams {
		/** Table name. */
		public static final String TABLE = "remux_params";
		/** {@link HashMap} for projection. */
		private static final HashMap<String, String> PROJECTION_MAP;

		/** Index in projection: ID. */
		public static final int INDEX_ID = 0;
		/** Index in projection: name of hours group. */
		public static final int INDEX_NAME = 1;
		public static final int INDEX_VALUE = 2;

		/** ID. */
		public static final String ID = "_id";
		public static final String PARAM_NAME = "name";
		public static final String PARAM_VALUE = "value";

		/** Projection used for query. */
		public static final String[] PROJECTION = new String[] {//
		ID,//
				PARAM_NAME, PARAM_VALUE };

		/** Content {@link Uri}. */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + TABLE);
		/**
		 * The MIME type of {@link #CONTENT_URI} providing a list.
		 */
		public static final String CONTENT_TYPE = // .
		"vnd.android.cursor.dir/vnd.vdramager.remux_params";

		/**
		 * The MIME type of a {@link #CONTENT_URI} single entry.
		 */
		public static final String CONTENT_ITEM_TYPE = // .
		"vnd.android.cursor.item/vnd.vdramager.jobs";

		static {
			PROJECTION_MAP = new HashMap<String, String>();
			for (String s : PROJECTION) {
				PROJECTION_MAP.put(s, s);
			}
		}

		/**
		 * Get Name for id.
		 * 
		 * @param cr
		 *            {@link ContentResolver}
		 * @param id
		 *            id
		 * @return name
		 */
		// public static String getName(final ContentResolver cr, final long id)
		// {
		// final Cursor cursor = cr.query(
		// ContentUris.withAppendedId(CONTENT_URI, id),
		// new String[] { NAME }, null, null, null);
		// String ret = null;
		// if (cursor != null && cursor.moveToFirst()) {
		// ret = cursor.getString(0);
		// }
		// if (cursor != null && !cursor.isClosed()) {
		// cursor.close();
		// }
		// return ret;
		// }

		/**
		 * Create table in {@link SQLiteDatabase}.
		 * 
		 * @param db
		 *            {@link SQLiteDatabase}
		 */
		public static void onCreate(final SQLiteDatabase db) {
			Log.i(TAG, "create table: " + TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE);
			db.execSQL("CREATE TABLE " + TABLE + " (" // .
					+ ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " // .
					+ PARAM_NAME + " TEXT, "//
					+ PARAM_VALUE + ");");//
		}

		/**
		 * Upgrade table.
		 * 
		 * @param db
		 *            {@link SQLiteDatabase}
		 * @param oldVersion
		 *            old version
		 * @param newVersion
		 *            new version
		 */
		public static void onUpgrade(final SQLiteDatabase db,
				final int oldVersion, final int newVersion) {
			Log.w(TAG, "Upgrading table: " + TABLE);
			// if(newVersion == 2){
			// final ContentValues[] values = backup(db, TABLE, PROJECTION,
			// null);
			// onCreate(db);
			// reload(db, TABLE, values);
			// } else if (newVersion >)
			// db.execSQL("DROP INDEX IF EXISTS jobs.canceled_index");
			// db.execSQL("CREATE INDEX canceled_index ON jobs(canceled)");
			// db.execSQL("ALTER TABLE JOBS ADD " + EVENT_ID2 + " LONG");
		}

		/** Default constructor. */
		private RemuxParams() {
		}
	}

	
	public static final class Vdr {
		/** Table name. */
		public static final String TABLE = "vdr";
		/** {@link HashMap} for projection. */
		private static final HashMap<String, String> PROJECTION_MAP;

		/** Index in projection: ID. */
		public static final int INDEX_ID = 0;
		/** Index in projection: name of hours group. */
		public static final int INDEX_NAME = 1;
		public static final int INDEX_HOST = 2;
		public static final int INDEX_PORT = 3;
		public static final int INDEX_SECURE = 4;
		public static final int INDEX_FILTER_CHANNELS  = 5;
		public static final int INDEX_CHANNEL_FILTER = 6;
		public static final int INDEX_ENABLE_WAKEUP = 7;
		public static final int INDEX_WAKEUP_METHOD = 8;
		public static final int INDEX_WAKEUP_URL = 9;
		public static final int INDEX_WAKEUP_USER = 10;
		public static final int INDEX_WAKEUP_PASSWORD = 11;
		public static final int INDEX_VDR_MAC = 12;
		public static final int INDEX_ENABLE_ALIVE_CHECK = 13;
		public static final int INDEX_ALIVE_CHECK_INTERVAL = 14;
		public static final int INDEX_TIMER_PRE_MARGIN = 15;
		public static final int INDEX_TIMER_POST_MARGIN = 15;
		public static final int INDEX_TIMER_DEFAULT_PRIORITY = 16;
		public static final int INDEX_TIMER_DEFAULT_LIFETIME = 17;
		public static final int INDEX_STREAM_PORT = 18;
		public static final int INDEX_STREAM_USERNAME = 19;
		public static final int INDEX_STREAM_PASSWORD = 20;
		public static final int INDEX_STREAMFORMAT = 21;
		public static final int INDEX_WOL_CUSTOM_BROADCAST = 22;
		public static final int INDEX_ENABLE_REMUX = 23;
		public static final int INDEX_REMUX_PARAMETER = 24;
		
		
		
		/** ID. */
		public static final String ID = "_id";
		public static final String HOST = "host";
		public static final String PORT= "port";
		public static final String SECURE = "secure";
		/*
		public static final String  = "";
		public static final String  = "";
		public static final String  = "";
		public static final String  = "";
		public static final String  = "";
		public static final String  = "";
		public static final String  = "";
		public static final String  = "";
		public static final String  = "";
		
		public static final String  = "";
		public static final String  = "";
		*/
		

		/** Projection used for query. */
		public static final String[] PROJECTION = new String[] {//
		ID,//
				HOST, PORT, SECURE };

		/** Content {@link Uri}. */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + TABLE);
		/**
		 * The MIME type of {@link #CONTENT_URI} providing a list.
		 */
		public static final String CONTENT_TYPE = // .
		"vnd.android.cursor.dir/vnd.vdramager.remux_params";

		/**
		 * The MIME type of a {@link #CONTENT_URI} single entry.
		 */
		public static final String CONTENT_ITEM_TYPE = // .
		"vnd.android.cursor.item/vnd.vdramager.jobs";

		static {
			PROJECTION_MAP = new HashMap<String, String>();
			for (String s : PROJECTION) {
				PROJECTION_MAP.put(s, s);
			}
		}

		/**
		 * Get Name for id.
		 * 
		 * @param cr
		 *            {@link ContentResolver}
		 * @param id
		 *            id
		 * @return name
		 */
		// public static String getName(final ContentResolver cr, final long id)
		// {
		// final Cursor cursor = cr.query(
		// ContentUris.withAppendedId(CONTENT_URI, id),
		// new String[] { NAME }, null, null, null);
		// String ret = null;
		// if (cursor != null && cursor.moveToFirst()) {
		// ret = cursor.getString(0);
		// }
		// if (cursor != null && !cursor.isClosed()) {
		// cursor.close();
		// }
		// return ret;
		// }

		/**
		 * Create table in {@link SQLiteDatabase}.
		 * 
		 * @param db
		 *            {@link SQLiteDatabase}
		 */
		public static void onCreate(final SQLiteDatabase db) {
			Log.i(TAG, "create table: " + TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE);
			db.execSQL("CREATE TABLE " + TABLE + " (" // .
					+ ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " // .
					+ HOST + " TEXT, "//
					+ PORT + " INTEGER, "
					+ SECURE +" INTEGER" +
							" " +
							" " +
							
							
							
							
							");");//
		}

		/**
		 * Upgrade table.
		 * 
		 * @param db
		 *            {@link SQLiteDatabase}
		 * @param oldVersion
		 *            old version
		 * @param newVersion
		 *            new version
		 */
		public static void onUpgrade(final SQLiteDatabase db,
				final int oldVersion, final int newVersion) {
			Log.w(TAG, "Upgrading table: " + TABLE);
			// if(newVersion == 2){
			// final ContentValues[] values = backup(db, TABLE, PROJECTION,
			// null);
			// onCreate(db);
			// reload(db, TABLE, values);
			// } else if (newVersion >)
			// db.execSQL("DROP INDEX IF EXISTS jobs.canceled_index");
			// db.execSQL("CREATE INDEX canceled_index ON jobs(canceled)");
			// db.execSQL("ALTER TABLE JOBS ADD " + EVENT_ID2 + " LONG");
		}

		/** Default constructor. */
		private Vdr() {
		}
	}

	
	
	/**
	 * Try to backup fields from table.
	 * 
	 * @param db
	 *            {@link SQLiteDatabase}
	 * @param table
	 *            table
	 * @param cols
	 *            columns
	 * @param strip
	 *            column to forget on backup, eg. _id
	 * @return array of rows
	 */
	private static ContentValues[] backup(final SQLiteDatabase db,
			final String table, final String[] cols, final String strip) {
		ArrayList<ContentValues> ret = new ArrayList<ContentValues>();
		String[] proj = cols;
		if (strip != null) {
			proj = new String[cols.length - 1];
			int i = 0;
			for (String c : cols) {
				if (strip.equals(c)) {
					continue;
				}
				proj[i] = c;
				++i;
			}
		}
		final int l = proj.length;
		Cursor cursor = null;
		try {
			cursor = db.query(table, proj, null, null, null, null, null);
		} catch (SQLException e) {
			if (l == 1) {
				return null;
			}
			final String err = e.getMessage();
			if (!err.startsWith("no such column:")) {
				return null;
			}
			final String str = err.split(":", 3)[1].trim();
			return backup(db, table, proj, str);
		}
		if (cursor != null && cursor.moveToFirst()) {
			do {
				final ContentValues cv = new ContentValues();
				for (int i = 0; i < l; i++) {
					final String s = cursor.getString(i);
					if (s != null) {
						cv.put(proj[i], s);
					}
				}
				ret.add(cv);
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return ret.toArray(new ContentValues[0]);
	}

	/**
	 * Reload backup into table.
	 * 
	 * @param db
	 *            {@link SQLiteDatabase}
	 * @param table
	 *            table
	 * @param values
	 *            {@link ContentValues}[] backed up with backup()
	 */
	private static void reload(final SQLiteDatabase db, final String table,
			final ContentValues[] values) {
		if (values == null || values.length == 0) {
			return;
		}
		Log.d(TAG, "reload(db, " + table + ", cv[" + values.length + "])");
		for (ContentValues cv : values) {
			db.insert(table, null, cv);
		}
		return;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		final SQLiteDatabase db = this.mOpenHelper.getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		final int uid = URI_MATCHER.match(uri);
		String groupBy = null;
		switch (uid) {
		case REMUX_PARAMS_ID:
			qb.appendWhere(RemuxParams.ID + "=" + ContentUris.parseId(uri));
			qb.setTables(RemuxParams.TABLE);
			qb.setProjectionMap(RemuxParams.PROJECTION_MAP);
			break;
		case REMUX_PARAMS:
			sortOrder = RemuxParams.PARAM_NAME;
			qb.setTables(RemuxParams.TABLE);
			qb.setProjectionMap(RemuxParams.PROJECTION_MAP);
			break;

		default:
			throw new IllegalArgumentException("Unknown Uri " + uri);
		}
		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = null;
		} else {
			orderBy = sortOrder;
		}

		// Run the query
		final Cursor c = qb.query(db, projection, selection, selectionArgs,
				groupBy, null, orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(this.getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
		case REMUX_PARAMS:
			return RemuxParams.CONTENT_TYPE;
		case REMUX_PARAMS_ID:
			return RemuxParams.CONTENT_ITEM_TYPE;
		}
		throw new IllegalArgumentException("Unknown URI: " + uri);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
		long ret = -1;
		switch (URI_MATCHER.match(uri)) {
		case REMUX_PARAMS:
			ret = db.insert(RemuxParams.TABLE, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown Uri " + uri);
		}
		if (ret < 0) {
			return null;
		} else {
			this.getContext().getContentResolver().notifyChange(uri, null);
			return ContentUris.withAppendedId(uri, ret);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		final SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
		int ret = 0;
		long id;
		Cursor c;
		String w;
		switch (URI_MATCHER.match(uri)) {
		case REMUX_PARAMS:
			ret = db.delete(RemuxParams.TABLE, selection, selectionArgs);
			break;
		case REMUX_PARAMS_ID:
			ret = db.delete(RemuxParams.TABLE, DbUtils.sqlAnd(RemuxParams.ID
					+ "=" + ContentUris.parseId(uri), selection), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown Uri " + uri);

		}
		if (ret > 0) {
			this.getContext().getContentResolver().notifyChange(uri, null);
		}
		return ret;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		final SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
		long i;
		int ret = 0;
		switch (URI_MATCHER.match(uri)) {
		case REMUX_PARAMS:
			ret = db.update(RemuxParams.TABLE, values, selection, selectionArgs);
			break;
		case REMUX_PARAMS_ID:
			ret = db.update(RemuxParams.TABLE, values,
					DbUtils.sqlAnd(
							RemuxParams.ID + "=" + ContentUris.parseId(uri),
							selection), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown Uri " + uri);
		}
		if (ret > 0) {
			this.getContext().getContentResolver().notifyChange(uri, null);
		}
		return ret;
	}

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public synchronized SQLiteDatabase getReadableDatabase() {
			Log.d(TAG, "get readble db");
			SQLiteDatabase ret;
			try {
				ret = super.getReadableDatabase();
			} catch (IllegalStateException e) {
				Log.e(TAG, "could not open databse, try again", e);
				ret = super.getReadableDatabase();
			}
			if (!ret.isOpen()) { // a restore closes the db. retry.
				Log.w(TAG, "got closed database, try again");
				ret = super.getReadableDatabase();
			}
			return ret;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public synchronized SQLiteDatabase getWritableDatabase() {
			Log.d(TAG, "get writable db");
			SQLiteDatabase ret;
			try {
				ret = super.getWritableDatabase();
			} catch (IllegalStateException e) {
				Log.e(TAG, "could not open databse, try again", e);
				ret = super.getWritableDatabase();
			}
			if (!ret.isOpen()) { // a restore closes the db. retry.
				Log.w(TAG, "got closed database, try again");
				ret = super.getWritableDatabase();
			}
			return ret;
		}

		/** {@link Context} . */
		private final Context ctx;

		/**
		 * Default Constructor.
		 * 
		 * @param context
		 *            {@link Context}
		 */
		DatabaseHelper(final Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			this.ctx = context;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onCreate(final SQLiteDatabase db) {
			Log.i(TAG, "create database");
			// if (doRestore(db)) {
			// return; // skip create
			// }
			// Logs.onCreate(db);
			// WebSMS.onCreate(db);
			// SipCall.onCreate(db);
			// Plans.onCreate(db);
			// Rules.onCreate(db);
			// Numbers.onCreate(db);
			// NumbersGroup.onCreate(db);
			// Hours.onCreate(db);
			// HoursGroup.onCreate(db);
			RemuxParams.onCreate(db);

			// import default
			// BufferedReader reader = new BufferedReader(new InputStreamReader(
			// this.ctx.getResources()
			// .openRawResource(R.raw.default_setup)));
			// final ArrayList<String> sb = new ArrayList<String>();
			// try {
			// String line = reader.readLine();
			// while (line != null) {
			// sb.add(line);
			// line = reader.readLine();
			// }
			// } catch (IOException e) {
			// Log.e(TAG, "error reading raw data", e);
			// }
			// importData(db, sb.toArray(new String[] {}));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
				final int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			// ProductConfig.onUpgrade(db, oldVersion, newVersion);
			// ServiceConfig.onUpgrade(db, oldVersion, newVersion);
			// ConfiguredProduct.o
			// Products.onUpgrade(db, oldVersion, newVersion);
			// Jobs.onUpgrade(db, oldVersion, newVersion);
			// Logs.onUpgrade(db, oldVersion, newVersion);
			// WebSMS.onUpgrade(db, oldVersion, newVersion);
			// SipCall.onUpgrade(db, oldVersion, newVersion);
			// Plans.onUpgrade(db, oldVersion, newVersion);
			// Rules.onUpgrade(db, oldVersion, newVersion);
			// Numbers.onUpgrade(db, oldVersion, newVersion);
			// NumbersGroup.onUpgrade(db, oldVersion, newVersion);
			// Hours.onUpgrade(db, oldVersion, newVersion);
			// HoursGroup.onUpgrade(db, oldVersion, newVersion);
			// unmatch(db);
		}
	}

	// private static void unmatch(final SQLiteDatabase db) {
	// Log.d(TAG, "unmatch()");
	// if (db.isReadOnly()) {
	// Log.e(TAG, "Database is readonly, cann not unmatch on upgrade!");
	// return;
	// }
	// ContentValues cv = new ContentValues();
	// cv.put(DataProvider.Logs.PLAN_ID, DataProvider.NO_ID);
	// cv.put(DataProvider.Logs.RULE_ID, DataProvider.NO_ID);
	// db.update(DataProvider.Logs.TABLE, cv, null, null);
	// cv.clear();
	// cv.put(DataProvider.Plans.NEXT_ALERT, 0);
	// db.update(DataProvider.Plans.TABLE, cv, null, null);
	// }
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreate() {
		this.mOpenHelper = new DatabaseHelper(this.getContext()) {
			public void onOpen(SQLiteDatabase db) {
				super.onOpen(db);
				if (!db.isReadOnly()) {
					// Enable foreign key constraints
					db.execSQL("PRAGMA foreign_keys=ON;");
				}
			};

		};
		return true;
	}
}
