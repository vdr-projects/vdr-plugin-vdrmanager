/*
 * Copyrigsht (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.bjusystems.vdrmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;

import android.content.SharedPreferences;
import android.os.Looper;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import de.bjusystems.vdrmanager.data.Vdr;

public class VdrSharedPreferencesImpl implements SharedPreferences {

	private static final String TAG = "SharedPreferencesImpl";

	private static final boolean DEBUG = false;

	// Lock ordering rules:
	// - acquire SharedPreferencesImpl.this before EditorImpl.this
	// - acquire mWritingToDiskLock before EditorImpl.this

	Vdr mVdr;


	public Vdr getVdr(){
		return mVdr;
	}

	RuntimeExceptionDao<Vdr, Integer> dao;

	private Map<String, Object> mMap; // guarded by 'this'
	private int mDiskWritesInFlight = 0; // guarded by 'this'
	private boolean mLoaded = false; // guarded by 'this'

	private final Object mWritingToDiskLock = new Object();
	private static final Object mContent = new Object();
	private final WeakHashMap<OnSharedPreferenceChangeListener, Object> mListeners = new WeakHashMap<OnSharedPreferenceChangeListener, Object>();

	public VdrSharedPreferencesImpl(Vdr vdr, RuntimeExceptionDao<Vdr, Integer> dao) {
		mVdr = vdr;
		this.dao = dao;
		mLoaded = false;
		mMap = null;
		startLoadFromDisk();
	}

	private void startLoadFromDisk() {
		synchronized (this) {
			mLoaded = false;
		}
		new Thread("SharedPreferencesImpl-load") {
			public void run() {
				synchronized (VdrSharedPreferencesImpl.this) {
					loadFromDiskLocked();
				}
			}
		}.start();
	}

	private void loadFromDiskLocked() {
		if (mLoaded) {
			return;
		}

		Map map = mVdr.toMap();
		//StructStat stat = null;
		//try {
			//stat = Libcore.os.stat(mFile.getPath());
			//if (mFile.canRead()) {
				//BufferedInputStream str = null;
				//try {
					//str = new BufferedInputStream(new FileInputStream(mFile),
						//	16 * 1024);
					//map = XmlUtils.readMapXml(str);
				//} catch (XmlPullParserException e) {
//					Log.w(TAG, "getSharedPreferences", e);
	//			} catch (FileNotFoundException e) {
		//			Log.w(TAG, "getSharedPreferences", e);
			//	} catch (IOException e) {
				//	Log.w(TAG, "getSharedPreferences", e);
				//} finally {
					//IoUtils.closeQuietly(str);
				//}
			//}
		//} catch (ErrnoException e) {
		//}
		mLoaded = true;
		if (map != null) {
			mMap = map;
		} else {
			mMap = new HashMap<String, Object>();
		}
		notifyAll();
	}



	public void registerOnSharedPreferenceChangeListener(
			OnSharedPreferenceChangeListener listener) {
		synchronized (this) {
			mListeners.put(listener, mContent);
		}
	}

	public void unregisterOnSharedPreferenceChangeListener(
			OnSharedPreferenceChangeListener listener) {
		synchronized (this) {
			mListeners.remove(listener);
		}
	}

	private void awaitLoadedLocked() {
		// if (!mLoaded) {
		// // Raise an explicit StrictMode onReadFromDisk for this
		// // thread, since the real read will be in a different
		// // thread and otherwise ignored by StrictMode.
		// BlockGuard.getThreadPolicy().onReadFromDisk();
		// }
		while (!mLoaded) {
			try {
				wait();
			} catch (InterruptedException unused) {
			}
		}
	}

	public Map<String, ?> getAll() {
		synchronized (this) {
			awaitLoadedLocked();
			// noinspection unchecked
			return new HashMap<String, Object>(mMap);
		}
	}

	public String getString(String key, String defValue) {
		synchronized (this) {
			awaitLoadedLocked();
			String v = String.valueOf(mMap.get(key));
			return v != null ? v : defValue;
		}
	}

	public Set<String> getStringSet(String key, Set<String> defValues) {
		synchronized (this) {
			awaitLoadedLocked();
			Set<String> v = (Set<String>) mMap.get(key);
			return v != null ? v : defValues;
		}
	}

	public int getInt(String key, int defValue) {
		synchronized (this) {
			awaitLoadedLocked();
			Integer v = (Integer) mMap.get(key);
			return v != null ? v : defValue;
		}
	}

	public long getLong(String key, long defValue) {
		synchronized (this) {
			awaitLoadedLocked();
			Long v = (Long) mMap.get(key);
			return v != null ? v : defValue;
		}
	}

	public float getFloat(String key, float defValue) {
		synchronized (this) {
			awaitLoadedLocked();
			Float v = (Float) mMap.get(key);
			return v != null ? v : defValue;
		}
	}

	public boolean getBoolean(String key, boolean defValue) {
		synchronized (this) {
			awaitLoadedLocked();
			Boolean v = (Boolean) mMap.get(key);
			return v != null ? v : defValue;
		}
	}

	public boolean contains(String key) {
		synchronized (this) {
			awaitLoadedLocked();
			return mMap.containsKey(key);
		}
	}

	public Editor edit() {
		// TODO: remove the need to call awaitLoadedLocked() when
		// requesting an editor. will require some work on the
		// Editor, but then we should be able to do:
		//
		// context.getSharedPreferences(..).edit().putString(..).apply()
		//
		// ... all without blocking.
		synchronized (this) {
			awaitLoadedLocked();
		}

		return new EditorImpl();
	}

	// Return value from EditorImpl#commitToMemory()
	private static class MemoryCommitResult {
		public boolean changesMade; // any keys different?
		public List<String> keysModified; // may be null
		public Set<OnSharedPreferenceChangeListener> listeners; // may be null
		public Map<String, Object> mapToWriteToDisk;
		public final CountDownLatch writtenToDiskLatch = new CountDownLatch(1);
		public volatile boolean writeToDiskResult = false;

		public void setDiskWriteResult(boolean result) {
			writeToDiskResult = result;
			writtenToDiskLatch.countDown();
		}
	}

	public final class EditorImpl implements Editor {

		private final Map<String, Object> mModified = new HashMap<String, Object>();

		private boolean mClear = false;

		public Editor putString(String key, String value) {
			synchronized (this) {
				mModified.put(key, value);
				return this;
			}
		}

		public Editor putStringSet(String key, Set<String> values) {
			synchronized (this) {
				mModified.put(key, (values == null) ? null
						: new HashSet<String>(values));
				return this;
			}
		}

		public Editor putInt(String key, int value) {
			synchronized (this) {
				mModified.put(key, value);
				return this;
			}
		}

		public Editor putLong(String key, long value) {
			synchronized (this) {
				mModified.put(key, value);
				return this;
			}
		}

		public Editor putFloat(String key, float value) {
			synchronized (this) {
				mModified.put(key, value);
				return this;
			}
		}

		public Editor putBoolean(String key, boolean value) {
			synchronized (this) {
				mModified.put(key, value);
				return this;
			}
		}

		public Editor remove(String key) {
			synchronized (this) {
				mModified.put(key, this);
				return this;
			}
		}

		public Editor clear() {
			synchronized (this) {
				mClear = true;
				return this;
			}
		}

		public void apply() {
			final MemoryCommitResult mcr = commitToMemory();
			final Runnable awaitCommit = new Runnable() {
				public void run() {
					try {
						mcr.writtenToDiskLatch.await();
					} catch (InterruptedException ignored) {
					}
				}
			};

			QueuedWork.add(awaitCommit);

			Runnable postWriteRunnable = new Runnable() {
				public void run() {
					awaitCommit.run();
					QueuedWork.remove(awaitCommit);
				}
			};

			VdrSharedPreferencesImpl.this.enqueueDiskWrite(mcr, postWriteRunnable);

			// Okay to notify the listeners before it's hit disk
			// because the listeners should always get the same
			// SharedPreferences instance back, which has the
			// changes reflected in memory.
			notifyListeners(mcr);
		}

		// Returns true if any changes were made
		private MemoryCommitResult commitToMemory() {
			MemoryCommitResult mcr = new MemoryCommitResult();
			synchronized (VdrSharedPreferencesImpl.this) {
				// We optimistically don't make a deep copy until
				// a memory commit comes in when we're already
				// writing to disk.
				if (mDiskWritesInFlight > 0) {
					// We can't modify our mMap as a currently
					// in-flight write owns it. Clone it before
					// modifying it.
					// noinspection unchecked
					mMap = new HashMap<String, Object>(mMap);
				}
				mcr.mapToWriteToDisk = mMap;
				mDiskWritesInFlight++;

				boolean hasListeners = mListeners.size() > 0;
				if (hasListeners) {
					mcr.keysModified = new ArrayList<String>();
					mcr.listeners = new HashSet<OnSharedPreferenceChangeListener>(
							mListeners.keySet());
				}

				synchronized (this) {
					if (mClear) {
						if (!mMap.isEmpty()) {
							mcr.changesMade = true;
							mMap.clear();
						}
						mClear = false;
					}

					for (Map.Entry<String, Object> e : mModified.entrySet()) {
						String k = e.getKey();
						Object v = e.getValue();
						if (v == this) { // magic value for a removal mutation
							if (!mMap.containsKey(k)) {
								continue;
							}
							mMap.remove(k);
						} else {
							boolean isSame = false;
							if (mMap.containsKey(k)) {
								Object existingValue = mMap.get(k);
								if (existingValue != null
										&& existingValue.equals(v)) {
									continue;
								}
							}
							mMap.put(k, v);
						}

						mcr.changesMade = true;
						if (hasListeners) {
							mcr.keysModified.add(k);
						}
					}

					mModified.clear();
				}
			}
			return mcr;
		}

		public boolean commit() {
			MemoryCommitResult mcr = commitToMemory();
			VdrSharedPreferencesImpl.this.enqueueDiskWrite(mcr, null /*
																 * sync write on
																 * this thread
																 * okay
																 */);
			try {
				mcr.writtenToDiskLatch.await();
			} catch (InterruptedException e) {
				return false;
			}
			notifyListeners(mcr);
			return mcr.writeToDiskResult;
		}

		private void notifyListeners(final MemoryCommitResult mcr) {
			if (mcr.listeners == null || mcr.keysModified == null
					|| mcr.keysModified.size() == 0) {
				return;
			}
			//if (Looper.myLooper() == Looper.getMainLooper()) {
				for (int i = mcr.keysModified.size() - 1; i >= 0; i--) {
					final String key = mcr.keysModified.get(i);
					for (OnSharedPreferenceChangeListener listener : mcr.listeners) {
						if (listener != null) {
							listener.onSharedPreferenceChanged(
									VdrSharedPreferencesImpl.this, key);
						}
					}
				}
			//} else {
				// Run this function on the main thread.
				// VdrManagerApp.sMainThreadHandler.post(new Runnable() {
				// public void run() {
				// notifyListeners(mcr);
				// }
				// });
			//}
		}
	}

	/**
	 * Enqueue an already-committed-to-memory result to be written to disk.
	 *
	 * They will be written to disk one-at-a-time in the order that they're
	 * enqueued.
	 *
	 * @param postWriteRunnable
	 *            if non-null, we're being called from apply() and this is the
	 *            runnable to run after the write proceeds. if null (from a
	 *            regular commit()), then we're allowed to do this disk write on
	 *            the main thread (which in addition to reducing allocations and
	 *            creating a background thread, this has the advantage that we
	 *            catch them in userdebug StrictMode reports to convert them
	 *            where possible to apply() ...)
	 */
	private void enqueueDiskWrite(final MemoryCommitResult mcr,
			final Runnable postWriteRunnable) {
		final Runnable writeToDiskRunnable = new Runnable() {
			public void run() {
				synchronized (mWritingToDiskLock) {
					writeToFile(mcr);
				}
				synchronized (VdrSharedPreferencesImpl.this) {
					mDiskWritesInFlight--;
				}
				if (postWriteRunnable != null) {
					postWriteRunnable.run();
				}
			}
		};

		final boolean isFromSyncCommit = (postWriteRunnable == null);

		// Typical #commit() path with fewer allocations, doing a write on
		// the current thread.
		if (isFromSyncCommit) {
			boolean wasEmpty = false;
			synchronized (VdrSharedPreferencesImpl.this) {
				wasEmpty = mDiskWritesInFlight == 1;
			}
			if (wasEmpty) {
				writeToDiskRunnable.run();
				return;
			}
		}

		QueuedWork.singleThreadExecutor().execute(writeToDiskRunnable);
	}

	// Note: must hold mWritingToDiskLock
	private void writeToFile(MemoryCommitResult mcr) {
		// Rename the current file so it may be used as a backup during the next
		// read

		// Attempt to write the file, delete the backup and return true as
		// atomically as
		// possible. If any exception occurs, delete the new file; next time we
		// will restore
		// from the backup.
		// FileOutputStream str = createFileOutputStream(mFile);
		// if (str == null) {
		// mcr.setDiskWriteResult(false);
		// return;
		// }
		//
		// XmlUtils.writeMapXml(mcr.mapToWriteToDisk, str);
		mVdr.set(mcr.mapToWriteToDisk);
		dao.createOrUpdate(mVdr);
		// FileUtils.sync(str);
		// str.close();
		// ContextImpl.setFilePermissionsFromMode(mFile.getPath(), mMode, 0);
		// try {
		// final StructStat stat = Libcore.os.stat(mFile.getPath());
		// synchronized (this) {
		// mStatTimestamp = stat.st_mtime;
		// mStatSize = stat.st_size;
		// }
		// } catch (ErrnoException e) {
		// // Do nothing
		// }
		// Writing was successful, delete the backup file if there is one.
		mcr.setDiskWriteResult(true);
		return;

	}
}
