/*
    MythDroid: Android MythTV Remote
    Copyright (C) 2009-2010 foobum@gmail.com
   
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.bjusystems.vdrmanager.utils.wakeup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import de.bjusystems.vdrmanager.data.Preferences;

import android.text.TextUtils;
import android.util.Log;

/** Send WakeOnLan packets */
public class WakeOnLanClient {

	static private byte[] addr;
	static private byte[] buf = new byte[17 * 6];

	/**
	 * Send a wake on lan packet
	 * 
	 * @param hwaddr
	 *            String containing the MAC address of the target
	 */
	public static void wake(String hwaddr) throws Exception {
		addr = parseAddr(hwaddr);
		for (int i = 0; i < 6; i++)
			buf[i] = (byte) 0xff;
		for (int i = 6; i < buf.length; i += 6)
			System.arraycopy(addr, 0, buf, i, 6);
		// if (.debug)
		Log.d("WakeOnLAN", //$NON-NLS-1$
				"Sending WOL packets to 255.255.255.255 " + //$NON-NLS-1$
						"ports 7, 9 for MAC address " + hwaddr //$NON-NLS-1$
		);

		String broadcast = Preferences.get().getWolCustomBroadcast();
		if (TextUtils.isEmpty(broadcast) == true) {
			broadcast = "255.255.255.255";//$NON-NLS-1$
		}

		InetAddress address = InetAddress.getByName(broadcast);
		DatagramPacket dgram = new DatagramPacket(buf, buf.length, address, 9);
		DatagramSocket sock = new DatagramSocket();
		sock.setBroadcast(true);
		sock.send(dgram);
		dgram.setPort(7);
		sock.send(dgram);
		sock.close();
	}

	/**
	 * Try to extract a hardware MAC address from a given IP address using the
	 * ARP cache (/proc/net/arp).<br>
	 * <br>
	 * We assume that the file has this structure:<br>
	 * <br>
	 * IP address HW type Flags HW address Mask Device 192.168.18.11 0x1 0x2
	 * 00:04:20:06:55:1a * eth0 192.168.18.36 0x1 0x2 00:22:43:ab:2a:5b * eth0
	 * 
	 * @param ip
	 * @return the MAC from the ARP cache
	 */
	public static String getMacFromArpCache(String ip) {
		if (ip == null)
			return null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("/proc/net/arp"));
			String line;
			while ((line = br.readLine()) != null) {
				String[] splitted = line.split(" +");
				if (splitted != null && splitted.length >= 4
						&& ip.equals(splitted[0])) {
					// Basic sanity check
					String mac = splitted[3];
					if (mac.matches("..:..:..:..:..:..")) {
						return mac;
					} else {
						return null;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 
	 * Parse a MAC Addresse
	 * 
	 * @param addr
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static byte[] parseAddr(String addr) throws IllegalArgumentException {
		byte[] bytes = new byte[6];
		String[] hex = addr.split(":"); //$NON-NLS-1$
		if (hex.length != 6)
			throw new IllegalArgumentException("Invalid MAC address"); //$NON-NLS-1$
		try {
			for (int i = 0; i < 6; i++)
				bytes[i] = (byte) Integer.parseInt(hex[i], 16);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(
					"Invalid hex digit in MAC address" //$NON-NLS-1$
			);
		}
		return bytes;
	}

}
