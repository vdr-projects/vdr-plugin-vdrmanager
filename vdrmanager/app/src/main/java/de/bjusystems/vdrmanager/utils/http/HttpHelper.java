package de.bjusystems.vdrmanager.utils.http;


import android.util.Base64;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Apache HttpClient helper class for performing HTTP requests.
 *
 * This class is intentionally *not* bound to any Android classes so that it is easier
 * to develop and test. Use calls to this class inside Android AsyncTask implementations
 * (or manual Thread-Handlers) to make HTTP requests asynchronous and not block the UI Thread.
 *
 * TODO cookies
 * TODO multi-part binary data
 * TODO follow 302s?
 * TODO shutdown connection mgr? - client.getConnectionManager().shutdown();
 *
 * @author ccollins
 *
 */
public class HttpHelper {



   /**
    * Perform an HTTP GET operation with user/pass and headers.
    *
    */
   public int performGet(final String url, final String user, final String pass,
            final Map<String, String> additionalHeaders) throws IOException {

      String authString = user + ":" + pass;

            byte[] authEncBytes = Base64.encode(authString.getBytes(), Base64.DEFAULT);
			String authStringEnc = new String(authEncBytes);

      URL u = new URL(url);

      HttpURLConnection con = (HttpURLConnection) u.openConnection();

      con.setRequestMethod("GET");
      con.setRequestProperty("Authorization", "Basic " + authStringEnc);
      return con.getResponseCode();
   }


}

