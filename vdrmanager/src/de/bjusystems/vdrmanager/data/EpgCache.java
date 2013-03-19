package de.bjusystems.vdrmanager.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;

public class EpgCache {

	public static WeakHashMap<String, ArrayList<Epg>> CACHE = new WeakHashMap<String, ArrayList<Epg>>();

	public static WeakHashMap<String, Date> NEXT_REFRESH = new WeakHashMap<String, Date>();


}
