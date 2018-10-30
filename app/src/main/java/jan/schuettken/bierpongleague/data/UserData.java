package jan.schuettken.bierpongleague.data;

/**
 * Created by Jan Schüttken on 30.10.2018 at 22:36
 */
public class UserData {
    private int id;
    private String username, firstName, lastName, nickName;

    //TODO es fehlen noch einige Infos - UserData sollte den gesamten Datenbank Eintrag abbilden

    public UserData() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}
