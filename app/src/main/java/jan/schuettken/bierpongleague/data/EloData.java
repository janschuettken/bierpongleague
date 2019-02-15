package jan.schuettken.bierpongleague.data;

import android.annotation.SuppressLint;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Jan Schüttken on 15.02.2019 at 10:58
 */
public class EloData {

    private int Id, UserId, GameId;
    private double Value;
    private Date date;

    public EloData(int id, int userId, int gameId, double value, Date date) {
        Id = id;
        UserId = userId;
        GameId = gameId;
        Value = value;
        this.date = date;
    }

    public EloData(JSONObject c) throws JSONException {
        setUserId(c.getInt("Id"));
        try {
            setGameId(c.getInt("GameId"));
        } catch (Exception e) {
            setGameId(-1);
        }
        setId(c.getInt("LogId"));
        setDate(c.getString("Date"));
        setValue(c.getDouble("Value"));
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    public int getGameId() {
        return GameId;
    }

    public void setGameId(int gameId) {
        GameId = gameId;
    }

    public double getValue() {
        return Value;
    }

    public void setValue(double value) {
        Value = value;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDate(String date) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            this.date = format.parse(date);
        } catch (ParseException e) {
            this.date = null;
            e.printStackTrace();
        }
    }
}
