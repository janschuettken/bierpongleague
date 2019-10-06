package jan.schuettken.bierpongleague.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jan.schuettken.bierpongleague.handler.DateFunctionHandler;

/**
 * Created by Jan Schüttken on 03.10.2019 at 14:21
 */
public class AreaData implements Serializable {
    private int id;
    private int parentId;
    private String name;
    private String entranceCode;
    private int type;
    private boolean confirmed;
    private Date StartDate;
    private Date EndDate;
    private List<UserData> admins = new ArrayList<>();
    private List<UserData> users = new ArrayList<>();

    public static int TYPE_PUBLIC = 1;
    public static int TYPE_PRIVATE = 2;
    public static int TYPE_TIME = 3;


    public AreaData(JSONObject j) throws JSONException {
        name = j.getString("Name");
        entranceCode = j.getString("EntranceCode");
        id = j.getInt("AreaId");
        type = j.getInt("Type");
        confirmed = j.getInt("confirmed") == 1;
        try {
            StartDate = DateFunctionHandler.convertTimestampToDate(j.getString("Start"));
        } catch (Exception ignored) {
        }

        try {
            EndDate = DateFunctionHandler.convertTimestampToDate(j.getString("End"));
        } catch (Exception ignored) {
        }
    }

    public AreaData() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public boolean isPublic() {
        return type == TYPE_PUBLIC;
    }

    public boolean isPrivate() {
        return type == TYPE_PRIVATE;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public Date getStartDate() {
        return StartDate;
    }

    public void setStartDate(Date startDate) {
        StartDate = startDate;
    }

    public Date getEndDate() {
        return EndDate;
    }

    public void setEndDate(Date endDate) {
        EndDate = endDate;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public List<UserData> getAdmins() {
        return admins;
    }

    public void setAdmins(List<UserData> admins) {
        this.admins = admins;
    }

    public boolean isAdmin(UserData userData) {
        if (admins == null)
            return false;
        for (UserData ud : admins)
            if (userData.getId() == ud.getId())
                return true;
        return false;
    }

    public List<UserData> getUsers() {
        return users;
    }

    public void setUsers(List<UserData> users) {
        this.users = users;
    }

    public String getEntranceCode() {
        return entranceCode;
    }

    public void setEntranceCode(String entranceCode) {
        this.entranceCode = entranceCode;
    }
}
