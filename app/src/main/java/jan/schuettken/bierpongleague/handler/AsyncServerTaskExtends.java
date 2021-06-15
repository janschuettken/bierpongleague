package jan.schuettken.bierpongleague.handler;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import jan.schuettken.bierpongleague.exceptions.NoConnectionException;

/**
 * Created by Jan on 06.06.2017.
 */

class AsyncServerTaskExtends extends AsyncTask<String, Integer, String> {

    private float timeOutSeconds = 5;
    private int timeOutMillis = (int) (timeOutSeconds * 1000);

    @Override
    protected String doInBackground(String... urls) {
        try {
            return getJsonFromServer(urls[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    private String getJsonFromServer(String url) throws NoConnectionException {
        try {
            BufferedReader inputStream;

            URL jsonUrl = new URL(url);
            Log.e("URLx", jsonUrl.toString());
            URLConnection dc = jsonUrl.openConnection();
            dc.setConnectTimeout(timeOutMillis);
            dc.setReadTimeout(timeOutMillis);
            inputStream = new BufferedReader(new InputStreamReader(dc.getInputStream()));
            // read the JSON results into a string
            return inputStream.readLine();
        } catch (Exception e) {
            Log.e("URL", url);
            e.printStackTrace();
            throw new NoConnectionException();
        }
    }
}