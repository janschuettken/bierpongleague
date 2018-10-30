package jan.schuettken.bierpongleague.handler;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

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

    private String getJsonFromServer(String url) {
        try {
            BufferedReader inputStream;

            URL jsonUrl = new URL(url);
            URLConnection dc = jsonUrl.openConnection();
            dc.setConnectTimeout(timeOutMillis);
            dc.setReadTimeout(timeOutMillis);
            inputStream = new BufferedReader(new InputStreamReader(dc.getInputStream()));
            // read the JSON results into a string
            return inputStream.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return "#fail#-no-connection";
        }
    }
}