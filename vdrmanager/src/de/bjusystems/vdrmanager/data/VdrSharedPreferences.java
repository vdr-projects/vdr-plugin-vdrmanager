package de.bjusystems.vdrmanager.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.SharedPreferences;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.dao.RuntimeExceptionDao;

@Deprecated
public class VdrSharedPreferences implements SharedPreferences {

	private static final String EMPTY_STRING = "";

	public int commits = 0;

	public RuntimeExceptionDao<Vdr, Integer> dao;

	private Vdr instance;

	public Vdr getInstance() {
		return instance;
	}

	public void setInstance(Vdr instance) {
		this.instance = instance;
		map.putAll(instance.toMap());
	}

	protected List<OnSharedPreferenceChangeListener> listeners = new LinkedList<OnSharedPreferenceChangeListener>();

	Map<String, Object> map = new HashMap<String, Object>();

	public VdrSharedPreferences() {

	}

	// public VdrSharedPreferences(Vdr vdr) {

	// }

	public boolean contains(String key) {
		return map.containsKey(key);
	}

	public Editor edit() {
		return new Editor();
	}

	public Map<String, Object> getAll() {
		return map;
	}

	public boolean getBoolean(String key, boolean defValue) {
		return get(key, defValue);
	}

	public float getFloat(String key, float defValue) {
		return get(key, defValue);
	}

	public int getInt(String key, int defValue) {
		String val = get(key, String.valueOf(defValue));
		try {
			return Integer.valueOf(val);
		} catch (Exception ex) {
			return defValue;
		}
	}

	public long getLong(String key, long defValue) {
		String val = get(key, String.valueOf(defValue));
		try {
			return Long.valueOf(val);
		} catch (Exception ex) {
			return defValue;
		}
	}

	public <T> T get(String key, T defValue) {
		if (map.containsKey(key)) {
			return (T) map.get(key);
		}
		return defValue;
	}

	public String getString(String key, String defValue) {
		Object obj = get(key, defValue);
		if (obj == null) {
			return EMPTY_STRING;
		}
		return String.valueOf(obj);
	}

	public Set<String> getStringSet(String arg0, Set<String> arg1) {
		return null;
	}

	public void registerOnSharedPreferenceChangeListener(
			OnSharedPreferenceChangeListener listener) {
		listeners.add(listener);
	}

	public void unregisterOnSharedPreferenceChangeListener(
			OnSharedPreferenceChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @author lado
	 *
	 */
	public class Editor implements SharedPreferences.Editor {

		Map<String, Object> modified = new HashMap<String, Object>();

		private boolean clear = false;

		public SharedPreferences.Editor clear() {
			clear = true;
			modified.clear();
			return this;
		}

		public boolean commit() {

			if (instance == null) {
				map.putAll(modified);
				return true;
			}

			instance.set(modified);

			CreateOrUpdateStatus custatus = dao.createOrUpdate(instance);

			boolean status = custatus.isCreated() || custatus.isUpdated();

			if (status == false)
				return false;

			map.putAll(modified);

			++commits;

			// and update any listeners
			for (String key : modified.keySet()) {
				for (OnSharedPreferenceChangeListener listener : listeners) {
					listener.onSharedPreferenceChanged(
							VdrSharedPreferences.this, key);
				}
			}

			modified.clear();

			return true;
		}

		public android.content.SharedPreferences.Editor put(String key,
				Object value) {
			synchronized (this) {
				modified.put(key, value);
				return this;
			}
		}

		public android.content.SharedPreferences.Editor putBoolean(String key,
				boolean value) {
			return put(key, value);
		}

		public android.content.SharedPreferences.Editor putFloat(String key,
				float value) {
			return put(key, value);
		}

		public android.content.SharedPreferences.Editor putInt(String key,
				int value) {
			return put(key, value);
		}

		public android.content.SharedPreferences.Editor putLong(String key,
				long value) {
			return put(key, value);
		}

		public android.content.SharedPreferences.Editor putString(String key,
				String value) {
			return put(key, value);
		}

		public android.content.SharedPreferences.Editor remove(String key) {
			synchronized (this) {
				modified.remove(key);
				return this;
			}
		}

		public void apply() {
			commit();
		}

		public android.content.SharedPreferences.Editor putStringSet(
				String key, Set<String> values) {
			synchronized (this) {
				modified.put(key, (values == null) ? null
						: new HashSet<String>(values));
				return this;
			}
		}

	}

}
