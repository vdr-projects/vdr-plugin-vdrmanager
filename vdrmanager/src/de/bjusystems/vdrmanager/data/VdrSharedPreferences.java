package de.bjusystems.vdrmanager.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.SharedPreferences;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.dao.RuntimeExceptionDao;

public class VdrSharedPreferences implements SharedPreferences {

	private static final String EMPTY_STRING = "";
	
	public RuntimeExceptionDao<Vdr, Integer> dao;
	
	public Vdr instance;
	
	protected List<OnSharedPreferenceChangeListener> listeners = new LinkedList<OnSharedPreferenceChangeListener>();

	Map<String, Object> map = new HashMap<String, Object>();
	
	public VdrSharedPreferences(){
		
	}
	public VdrSharedPreferences (Vdr vdr){
		map.putAll(vdr.toMap());
		instance = vdr;
	}
	
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
		return get(key, defValue);
	}

	public long getLong(String key, long defValue) {
		return get(key, defValue);
	}

	public <T> T get(String key, T defValue) {
		if (map.containsKey(key)) {
			return (T) map.get(key);
		}
		return defValue;
	}

	public String getString(String key, String defValue) {
		Object obj = get(key, defValue);
		if(obj == null){
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
	
	
	public class Editor implements SharedPreferences.Editor {
		
		Map<String, Object> data = new HashMap<String, Object>();

		public SharedPreferences.Editor clear() {
			data.clear();
			return this;
		}

		public boolean commit() {
			instance.init(map);
			CreateOrUpdateStatus custatus = dao.createOrUpdate(instance);


			boolean status = custatus.isCreated() || custatus.isUpdated();

			if(status == false)
				return false;
			
			// and update any listeners
			for (OnSharedPreferenceChangeListener listener : listeners) {
				listener.onSharedPreferenceChanged(
						VdrSharedPreferences.this, null);
			}

			return true;
		}


		public android.content.SharedPreferences.Editor put(
				String key, Object value) {
			 	map.put(key, value);
			 return this;
					 
		}
		public android.content.SharedPreferences.Editor putBoolean(
				String key, boolean value) {
			return put(key,value);
		}

		public android.content.SharedPreferences.Editor putFloat(
				String key, float value) {
			return put(key,value);
		}

		public android.content.SharedPreferences.Editor putInt(String key,
				int value) {
			return put(key,value);
		}

		public android.content.SharedPreferences.Editor putLong(String key,
				long value) {
			return put(key,value);
		}

		public android.content.SharedPreferences.Editor putString(
				String key, String value) {
			return put(key,value);
		}

		public android.content.SharedPreferences.Editor remove(String key) {
			map.remove(key);
			return this;
		}


		public void apply() {
			commit();
		}


		public android.content.SharedPreferences.Editor putStringSet(
				String arg0, Set<String> arg1) {
			// TODO Auto-generated method stub
			return null;
		}

	}

}
