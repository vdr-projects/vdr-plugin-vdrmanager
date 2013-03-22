package org.fueri.reeldroid.network;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/***
 *
 * DeviceManager singleton
 *
 * @author Patrick Fürlinger fueri@fueri.ch
 *
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or (at
 *         your option) any later version.
 *
 *         This program is distributed in the hope that it will be useful, but
 *         WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *         General Public License for more details.
 *
 *         You should have received a copy of the GNU General Public License
 *         along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *
 */
public class DeviceManager {

	private static final int MAX_PING_TIMEOUT_MS = 40;

	public static interface ProgressListener {
		void publish(String currentIP);
	}

	/**
	 *
	 * @param defaultPort
	 * @return
	 */
	public static List<String> findVDRHosts(Context context,
			Integer defaultPort, ProgressListener listener) {

		if (defaultPort == null) {
			defaultPort = 6420;
		}

		List<String> list = new ArrayList<String>();
		ConnectivityManager conMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		boolean netStatus = conMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()
				|| conMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET)
						.isConnected();

		if (netStatus == false) {
			return list;
		}

		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String baseIp = intToBaseIp(wifiInfo.getIpAddress());

		for (Integer i = 1; i <= 254; i++) {
			InetAddress ia;
			try {
				String ipHost = baseIp + i.toString();
				if (listener != null) {
					listener.publish(ipHost);
				}
				ia = InetAddress.getByName(ipHost);
				if (findHost(ia, defaultPort, MAX_PING_TIMEOUT_MS) == false) {
					continue;
				}
				list.add(ipHost);
			} catch (UnknownHostException e) {
			}

		}

		return list;
	}

	/**
	 *
	 * @param ip
	 * @param port
	 * @return
	 */
	public static boolean findHost(String ip, int port, int pingTimeout) {
		InetAddress ia = null;
		try {
			ia = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			return false;
		}
		return findHost(ia, port, pingTimeout);
	}

	/**
	 *
	 * @param ip
	 * @param port
	 * @return
	 */
	public static boolean findHost(InetAddress ip, int port, int pingTimeout) {

		try {
			InetAddress address = ip;

			boolean reachable = address.isReachable(pingTimeout);

			if (reachable == false) {
				return false;
			}

			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port), 1000);
			socket.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * return base ip as a string.
	 *
	 * @param ip_address
	 * @return String
	 */
	public static String intToBaseIp(int ip_address) {
		return (ip_address & 0xFF) + "." + ((ip_address >> 8) & 0xFF) + "."
				+ ((ip_address >> 16) & 0xFF) + ".";

	}

}
