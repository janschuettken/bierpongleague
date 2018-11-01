package jan.schuettken.bierpongleague.handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Map;

import jan.schuettken.bierpongleague.exceptions.EmptyPreferencesException;

/**
 * Created by Jan Schüttken on 01.11.2018 at 14:42
 */
public class PreferencesHandler {

    public static final String EMPTY = "#empty#";
    private transient Context context;
    private transient SharedPreferences prefs;
    public static final String prefContext = "de.schuettken.jan.bierpongleague";

    public final static String USERNAME_PREF = "username";
    public final static String PASSWORD_PREF = "password";
    public final static String USER_ID_PREF = "userId";
    public final static String SESSION_PREF = "sessionId";
    public final static String SESSION_TIME_PREF = "sessionTime";//store the timestamp of the last session request > 5 min del session
    private final static int LOGOUT_TIME = 5 * 1000;//5 min in ms

    /**
     * @param context the Activity context
     */
    public PreferencesHandler(Context context) {
        this.context = context;
        checkForPref();
    }

//REGION ############# pref direct functions

    public String getUsername() throws EmptyPreferencesException {
        return getPrefWithException(USERNAME_PREF);
    }

    public String getPassword() throws EmptyPreferencesException {
        return getPrefWithException(PASSWORD_PREF);
    }

    public String getUserId() throws EmptyPreferencesException {
        return getPrefWithException(USER_ID_PREF);
    }

    public String getSessionId() throws EmptyPreferencesException {

        long timestamp = getDataFromPrefsLong(SESSION_TIME_PREF, Long.MIN_VALUE);
        if (timestamp == Long.MIN_VALUE || (System.currentTimeMillis() - timestamp) > LOGOUT_TIME) {
            deletePrefData(SESSION_PREF);
            deletePrefData(SESSION_TIME_PREF);
            throw new EmptyPreferencesException("Session timeout");
        }
        return getPrefWithException(USER_ID_PREF);
    }

    public void setSessionId(String sessionId) {
        saveDataToPrefs(SESSION_PREF, sessionId);
        saveDataToPrefs(SESSION_TIME_PREF, System.currentTimeMillis());
    }
//REGION ############# pref basic functions

    private String getPrefWithException(String pref) throws EmptyPreferencesException {
        String prefText = getDataFromPrefs(pref);
        if (prefText.equals(EMPTY))
            throw new EmptyPreferencesException(pref);
        return prefText;
    }

    /**
     * Re-initializes the prefs
     */
    private void checkForPref() {
        if (prefs == null)
            prefs = context.getSharedPreferences(prefContext, Context.MODE_PRIVATE);
    }

    private boolean saveDataToPrefs(String pref, String text) {
        return saveDataToPrefs(pref, text, false);
    }

    private boolean saveDataToPrefs(String pref, String text, boolean force) {
        if (context == null)
            return false;
        checkForPref();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(pref, text);
        if (force)
            return editor.commit();
        editor.apply();
        return true;
    }

    private boolean saveDataToPrefs(String pref, boolean state) {
        return saveDataToPrefs(pref, state + "", false);
    }

    private boolean saveDataToPrefs(String pref, long state) {
        if (context == null)
            return false;
        checkForPref();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(pref, state);
        editor.apply();
        return true;
    }

    private boolean delAllPrefData() {
        if (context == null)
            return false;
        checkForPref();
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        return true;
    }

    private boolean deletePrefData(String key) {
        if (context == null)
            return false;
        checkForPref();
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.apply();
        return true;
    }

    private String getDataFromPrefs(String pref) throws RuntimeException {
        return getDataFromPrefs(pref, EMPTY);
    }

    private String getDataFromPrefs(String pref, String empty) throws RuntimeException {
        if (context == null)
            return null;
        checkForPref();
        try {
            return prefs.getString(pref, empty);
        } catch (Exception e) {
            throw new RuntimeException("Error while reading from prefs: " + pref);
        }
    }

    /**
     * @param pref The pref to read
     * @return the parsed boolean
     * @throws RuntimeException if parsing crashes an exception is thrown
     */
    private boolean getDataFromPrefsBool(String pref) throws RuntimeException {
        try {
            return Boolean.parseBoolean(getDataFromPrefs(pref));
        } catch (Exception e) {
            throw new RuntimeException("Parsing error with a boolean");
        }
    }

    /**
     * @param pref         The pref to read
     * @param defaultState if parsing crashes you will get this boolean back
     * @return the parsed boolean
     */
    private boolean getDataFromPrefsBool(String pref, boolean defaultState) {
        try {
            return Boolean.parseBoolean(getDataFromPrefs(pref));
        } catch (Exception e) {
            return defaultState;
        }
    }

    /**
     * @param pref the pref to read out
     * @return tries to parse an int to return. If not it will return Integer.MIN_VALUE
     */
    private int getDataFromPrefsInt(String pref) {
        try {
            return Integer.parseInt(getDataFromPrefs(pref));
        } catch (Exception e) {
            return Integer.MIN_VALUE;
        }
    }

    private long getDataFromPrefsLong(String pref, long defaultState) {
        try {
            return Long.parseLong(getDataFromPrefs(pref));
        } catch (Exception e) {
            return defaultState;
        }
    }

    private void printAllPrefs() {
        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.e("map values", entry.getKey() + ": " + entry.getValue().toString());
        }
    }

}
