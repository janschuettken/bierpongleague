package jan.schuettken.bierpongleague.data;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Jan Schüttken on 30.10.2018 at 22:36
 */
public class UserData implements Serializable {
    private int id;
    private double elo;
    private String username, firstName, lastName, nickName, password, email;
    private int gender;
    private int power = 0;
    public final static int GENDER_WITH_PENIS = 0;
    public final static int GENDER_WITHOUT_PENIS = 1;

    public UserData() {

    }

    public UserData(JSONObject userData) throws JSONException {
        setId(userData.getInt("Id"));
        try {
            setUsername(userData.getString("Username"));
        } catch (Exception ignored) {
        }
        try {
            power = (userData.getInt("Power"));
        } catch (Exception ignored) {
        }
        setFirstName(userData.getString("FirstName"));
        setLastName(userData.getString("LastName"));
        setElo(userData.getDouble("Elo"));
    }

    public int getId() {
        return id;
    }

    public UserData setId(int id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getElo() {
        return elo;
    }

    public void setElo(double elo) {
        this.elo = elo;
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    public int getGender() {
        return gender;
    }

    public boolean isWithPenis() {
        return getGender() == GENDER_WITH_PENIS;
    }

    public boolean isWithoutPenis() {
        return getGender() == GENDER_WITHOUT_PENIS;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof UserData))
            return false;
        return this.getFullName().equals(((UserData) obj).getFullName());
    }

    public int getPower() {
        return power;
    }

    @Override
    public String toString() {
        return super.toString() + "#" + getFirstName();
    }

    public boolean isSet() {
        return !(getFirstName() == null && getLastName() == null && getUsername() == null);
    }
}
