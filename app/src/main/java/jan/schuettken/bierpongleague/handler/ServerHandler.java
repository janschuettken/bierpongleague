package jan.schuettken.bierpongleague.handler;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;

import jan.schuettken.bierpongleague.exceptions.NoConnectionException;

/**
 * Created by Jan Schüttken on 20.12.2017 at 21:10
 */

public class ServerHandler implements Serializable {

    public String getJsonFromServer(String url) throws NoConnectionException {
        try {
            return new AsyncServerTaskExtends().execute(url).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new NoConnectionException("Error while reading from Server");
        }
    }

    public boolean appendTextToFile(File file, String s) {
        return writeTextToFile(file, s, true);
    }

    public boolean writeTextToFile(File file, String s, boolean append) {
        if (!file.exists())
            return false;
        try {
            FileOutputStream outputStream = new FileOutputStream(file, append);
            outputStream.write(s.getBytes());
            outputStream.close();
            return true;
        } catch (Exception e) {
            Log.e("writeTextToFile", "write in File");
            e.printStackTrace();
            return true;
        }
    }

    public boolean overwriteTextInFile(File file, String s) {
        return writeTextToFile(file, s, false);
    }
}
