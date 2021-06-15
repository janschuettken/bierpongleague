package jan.schuettken.bierpongleague.handler;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jan.schuettken.bierpongleague.data.AreaData;
import jan.schuettken.bierpongleague.data.EloData;
import jan.schuettken.bierpongleague.data.GameData;
import jan.schuettken.bierpongleague.data.UserData;
import jan.schuettken.bierpongleague.exceptions.DatabaseException;
import jan.schuettken.bierpongleague.exceptions.InvalidLoginException;
import jan.schuettken.bierpongleague.exceptions.MailNotTakenException;
import jan.schuettken.bierpongleague.exceptions.MailTakenException;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.NoGamesException;
import jan.schuettken.bierpongleague.exceptions.NoScoreboardException;
import jan.schuettken.bierpongleague.exceptions.NotEnoughPowerException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;
import jan.schuettken.bierpongleague.exceptions.UserAlreadyInAreaException;
import jan.schuettken.bierpongleague.exceptions.UserNotAdminException;
import jan.schuettken.bierpongleague.exceptions.UserNotInAreaException;
import jan.schuettken.bierpongleague.exceptions.UsernameTakenException;
import jan.schuettken.bierpongleague.exceptions.WrongOldPasswordException;

/**
 * Created by Jan Schüttken on 30.10.2018 at 21:23
 * All Api functions are stored here
 */
public class ApiHandler {
    private static final String FAIL = "#fail#";
    private final String SERVER_URL = "https://diewodesch.de/api/bierpong/";
    private ServerHandler serverHandler;
    private String session;

    /**
     * @return The current session id - can't be null
     */
    public String getSession() {
        return session;
    }

    public boolean hasSession() {
        return session == null;
    }

    /**
     * @param session Sets the session id<br>You can get one from: {@link #login(String, String, String) login}
     */
    public void setSession(@NotNull String session) {
        this.session = session;
    }

    /**
     * For an description look at  {@link #login(String, String, String) login}
     *
     * @param context is used to get The Version Code from The App
     */
    public ApiHandler(@NotNull String username, @NotNull String password, @NotNull Context context) throws InvalidLoginException, NoConnectionException, DatabaseException {
        this();
        login(username, password, getVersionText(context));
    }

    private String getVersionText(@NotNull Context context) {
        PackageManager manager = context.getPackageManager();
        String versionName;
        int code;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
            code = info.versionCode;
            versionName += " (" + code + ")";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionName = "unknown";
        }

        return versionName;
    }

    /**
     * @param session A session id is required - if you have non use: {@link #ApiHandler(String, String, Context) ApiHandler}
     */
    public ApiHandler(@NotNull String session) {
        this();
        setSession(session);
    }

    /**
     * ONLY USE FOR REGISTER
     */
    public ApiHandler() {
        this.serverHandler = new ServerHandler();
    }

    /**
     * @param game the filled game. For futher information look at {@link #addGame(UserData[], int[], int, String[]) addArea}
     * @return true: the game is confirmed
     * @throws SessionErrorException session is not set or outdated
     * @throws NoConnectionException Connection Timeout
     * @throws RuntimeException      in here the server response is stored
     */
    public boolean addGame(GameData game) throws NoConnectionException, SessionErrorException, RuntimeException, UserNotInAreaException {
        String[] des = new String[game.getDescription() == null ? 0 : 1];
        if (game.getDescription() != null)
            des[0] = game.getDescription();
        return addGame(game.getParticipants(), game.getScores(), game.getAreaId(), des);
    }

    /**
     * @param area the filled are.
     * @return true: the game is confirmed
     * @throws SessionErrorException session is not set or outdated
     * @throws NoConnectionException Connection Timeout
     * @throws RuntimeException      in here the server response is stored
     */
    public boolean addArea(AreaData area, UserData user) throws NoConnectionException, SessionErrorException, RuntimeException {
        String fileUrl = SERVER_URL + "addArea.php?session=" + session;
        fileUrl += "&name=" + area.getName();
        fileUrl += "&parentId=" + "-1";
//        fileUrl += "&start=" +;
//        fileUrl += "&end=" + ;
        fileUrl += "&type=" + area.getType();
        fileUrl += "&admin=" + user.getId();
        Log.e("addArea", fileUrl);
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response.equalsIgnoreCase("#fail#session error")) {
            Log.e("ADD_GAME", fileUrl);
            throw new SessionErrorException(response);
        }
        if (response.startsWith("#fail#"))
            throw new NoConnectionException(response);
        return true;
    }

    public void addUserToArea(AreaData area, UserData user) throws NoConnectionException, SessionErrorException, NotEnoughPowerException, UserAlreadyInAreaException {
        String fileUrl = SERVER_URL + "addUserToArea.php?session=" + session;
        fileUrl += "&area=" + area.getId();
        fileUrl += "&user=" + user.getId();
        Log.e("addUserToArea", fileUrl);
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response.equalsIgnoreCase("#fail#session error")) {
            Log.e("addUserToArea", fileUrl);
            throw new SessionErrorException(response);
        }
        if (response.equalsIgnoreCase("#fail#you must be admin")) {
            throw new NotEnoughPowerException();
        }
        if (response.equalsIgnoreCase("#fail#user already in")) {
            throw new UserAlreadyInAreaException();
        }

        if (response.startsWith("#fail#"))
            throw new RuntimeException(response);
    }

    public void addUserToAreaWithCode(String code) throws NoConnectionException, SessionErrorException, UserAlreadyInAreaException {
        String fileUrl = SERVER_URL + "addUserViaCode.php?session=" + session;
        fileUrl += "&code=" + code;
        Log.e("addUserToAreaWithCode", fileUrl);
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response.equalsIgnoreCase("#fail#session error")) {
            Log.e("addUserToAreaWithCode", fileUrl);
            throw new SessionErrorException(response);
        }
//        if (response.equalsIgnoreCase("#fail#you must be admin")) {
//            throw new NotEnoughPowerException();
//        }
        if (response.equalsIgnoreCase("#fail#user already in")) {
            throw new UserAlreadyInAreaException();
        }

        if (response.startsWith("#fail#"))
            throw new RuntimeException(response);
    }

    /**
     * @param participants <p>required array lengh = 4
     *                     <br>Position 0 is player A from team A
     *                     <br>Position 1 is player B from team A
     *                     <br>Position 2 is player C from team B
     *                     <br>Position 3 is player D from team B</p>
     * @param scores       <p>required array lengh = 2
     *                     <br>Position 0 is the score from team A, score > 0 if this team won
     *                     <br>Position 1 is the score from team B, score > 0 if this team won</p>
     * @param areaId       The Area/League the game is placed in - currently only id 1
     * @param description  optional text description
     * @return true: the game is confirmed
     * @throws SessionErrorException session is not set or outdated
     * @throws NoConnectionException Connection Timeout
     * @throws RuntimeException      in here the server response is stored
     */
//    public boolean addGame(@NotNull UserData[] participants, @NotNull int[] scores, @IntRange(from = 1) int areaId, String... description)
    public boolean addGame(@NotNull UserData[] participants, @NotNull int[] scores, int areaId, String... description)
            throws SessionErrorException, NoConnectionException, RuntimeException, UserNotInAreaException {

        if (participants.length != 4 || scores.length != 2)
            throw new IllegalArgumentException("participants.lengh must be 4, is: " + participants.length + " and scores.lengh must be 2, is: " + scores.length);
        if (scores[0] > 0 && scores[1] > 0)
            throw new IllegalArgumentException("One team scores must be 0");
        String fileUrl = SERVER_URL + "addGame.php?session=" + session;
        fileUrl += "&playerA=" + participants[0].getId();
        fileUrl += "&playerB=" + participants[1].getId();
        fileUrl += "&playerC=" + participants[2].getId();
        fileUrl += "&playerD=" + participants[3].getId();
        fileUrl += "&scoreA=" + scores[0];
        fileUrl += "&scoreB=" + scores[1];
        fileUrl += "&areaId=" + areaId;

        Log.e("addGame", fileUrl);

        if (description.length > 0)
            fileUrl += "&description=" + description[0];
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response.equalsIgnoreCase("#fail#user not in area")) {
            Log.e("ADD_GAME", fileUrl);
            throw new UserNotInAreaException();
        }
        if (response.equalsIgnoreCase("#fail#session error")) {
            Log.e("ADD_GAME", fileUrl);
            throw new SessionErrorException(response);
        }
        if (response.startsWith("#fail#"))
            throw new RuntimeException(response);
        return true;
    }

    public void deleteGame(@NotNull GameData game, @NotNull UserData admin) throws NoConnectionException, UserNotAdminException, SessionErrorException {
        String fileUrl = SERVER_URL + "deleteGame.php?session=" + session;
        fileUrl += "&game=" + game.getGameId();
        fileUrl += "&user=" + admin.getId();

        Log.e("deleteGame", fileUrl);

        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response.equalsIgnoreCase("#fail#you are not the admin")) {
            Log.e("deleteGame", fileUrl);
            throw new UserNotAdminException();
        }
        if (response.equalsIgnoreCase("#fail#session error")) {
            Log.e("deleteGame", fileUrl);
            throw new SessionErrorException(response);
        }
        if (response.startsWith("#fail#"))
            throw new NoConnectionException(response);
    }

    public void deleteGame(int game, int admin) throws NoConnectionException, UserNotAdminException, SessionErrorException {
        String fileUrl = SERVER_URL + "deleteGame.php?session=" + session;
        fileUrl += "&game=" + game;
        fileUrl += "&user=" + admin;

        Log.e("deleteGame", fileUrl);

        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response.equalsIgnoreCase("#fail#you are not the admin")) {
            Log.e("deleteGame", fileUrl);
            throw new UserNotAdminException();
        }
        if (response.equalsIgnoreCase("#fail#session error")) {
            Log.e("deleteGame", fileUrl);
            throw new SessionErrorException(response);
        }
        if (response.startsWith("#fail#"))
            throw new NoConnectionException(response);
    }

    /**
     * The session must be set before!
     *
     * @param gameId  the id of the Game that should be confirmed
     * @param confirm true: to confirm, false: increase error count with 1
     * @return true: the game is confirmed
     * @throws SessionErrorException session is not set or outdated
     * @throws NoConnectionException Connection Timeout
     * @throws RuntimeException      in here the server response is stored
     */
    public boolean confirmGame(int gameId, boolean confirm)
//    public boolean confirmGame(@IntRange(from = 1) int gameId, boolean confirm)
            throws SessionErrorException, NoConnectionException, RuntimeException {

        String fileUrl = SERVER_URL + "updateConfirm.php?session=" + session;
        fileUrl += "&gameId=" + gameId + "&confirm=" + (confirm ? 1 : 0);
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response.equalsIgnoreCase("#fail#session error"))
            throw new SessionErrorException(response);
        if (response.startsWith("#fail#"))
            throw new RuntimeException(response);
        return true;
    }

    /**
     * @param username the username of the UserData
     * @param password the matching Password
     * @param version  the Version text of the app
     * @return true: the new SessionId is set in the {@link #getSession() session} Parameter of this class
     * @throws NoConnectionException Connection Timeout
     * @throws InvalidLoginException Password does not match the Username
     * @throws DatabaseException     Internal server error - maybe the server crashed?
     */
    public boolean login(@NotNull String username, @NotNull String password, @NotNull String version)
            throws NoConnectionException, InvalidLoginException, DatabaseException {
        String fileUrl = SERVER_URL + "login.php?user=" + username + "&password=" + password;// + "&version=Android " + version;
        //Log.e("LOGIN", fileUrl);
        String response = serverHandler.getJsonFromServer(fileUrl);
        //Log.e("LOGIN", response);
        if (response == null)
            throw new NoConnectionException();
        if (response.startsWith("#fail#wrongPassword") || response.startsWith("#fail#usernameNotTaken"))
            throw new InvalidLoginException(response);
        if (response.startsWith("#fail#"))
            throw new DatabaseException(response);

        session = response;
        return true;
    }

    /**
     * Registers a new User to the Game. Username and Email must be unique
     *
     * @param user the Userdata
     * @return true if a new user has been created
     * @throws NoConnectionException  Connection Timeout
     * @throws UsernameTakenException The filled in Username or Email are already taken
     */
    public boolean register(@NotNull UserData user) throws NoConnectionException, UsernameTakenException, MailTakenException {
        String fileUrl = SERVER_URL + "addUser.php";
        fileUrl += "?username=" + user.getUsername();
        fileUrl += "&password=" + user.getPassword();
        fileUrl += "&email=" + user.getEmail();
        fileUrl += "&firstName=" + user.getFirstName();
        fileUrl += "&lastName=" + user.getLastName();
        fileUrl += "&nickName=" + user.getNickName();
        fileUrl += "&gender=" + user.getGender();
        //Log.e("REGISTER", fileUrl);
        String response = serverHandler.getJsonFromServer(fileUrl);
        //Log.e("REGISTER", response);
        if (response.equalsIgnoreCase("#fail#user exists already"))
            throw new UsernameTakenException(response);
        if (response.equalsIgnoreCase("#fail#mailIsTaken"))
            throw new MailTakenException();
        if (response.startsWith("#fail#"))
            throw new RuntimeException(response);

        return response.equalsIgnoreCase("#success#");
    }

    /**
     * @param user            the user
     * @param youAreInTeamOne <p>if true, the logged in user is always in the first team (ordered by userId)<br>if false, the winning team will be in the first team (ordered by userId)</p>
     * @return a list of all Games from the user with userId
     * @throws SessionErrorException session is not set or outdated
     * @throws NoConnectionException Connection Timeout
     * @throws JSONException         the file is bad - might be an server problem
     * @throws RuntimeException      in here the server response is stored
     */
    public List<GameData> getGames(@NotNull UserData user, boolean youAreInTeamOne)
            throws NoConnectionException, SessionErrorException, RuntimeException, JSONException, NoGamesException {
        return getGames(user.getId(), youAreInTeamOne);
    }

    /**
     * @param userId          the id for the user
     * @param youAreInTeamOne <p>if true, the logged in user is always in the first team (ordered by userId)<br>if false, the winning team will be in the first team (ordered by userId)</p>
     * @return a list of all Games from the user with userId
     * @throws SessionErrorException session is not set or outdated
     * @throws NoConnectionException Connection Timeout
     * @throws JSONException         the file is bad - might be an server problem
     * @throws RuntimeException      in here the server response is stored
     */
    @NotNull
//    public List<GameData> getGames(@IntRange(from = 0) int userId, boolean youAreInTeamOne)
    public List<GameData> getGames(int userId, boolean youAreInTeamOne)
            throws SessionErrorException, NoConnectionException, RuntimeException, JSONException, NoGamesException {
        String fileUrl = SERVER_URL + "getGame.php?session=" + session;
        fileUrl += "&userId=" + userId;
        String response = serverHandler.getJsonFromServer(fileUrl);
        Log.e("getGames", fileUrl);
        if (response == null)
            throw new NoGamesException("No games played jet");
        if (response.equalsIgnoreCase("#fail#session error"))
            throw new SessionErrorException(response);
        if (response.startsWith("#fail#"))
            throw new RuntimeException(response);

        //start parsing the JSON
        //TODO ich bin mir nich sicher, ob der Code so funktioniert! Er ist nicht getestet!
        JSONArray gameObjects = new JSONArray(response);
        ArrayList<GameData> games = new ArrayList<>();
        int playerCounter = 0, teamCounter = 0;
        int lastGameId = -1, lastScore = -1;
        int gameId, score;//set to 1 for Weiderhold
        GameData currentGame = null;
        boolean firstRun = true;
        for (int i = 0; i < gameObjects.length(); i++) {
            JSONObject c = gameObjects.getJSONObject(i);
            // Storing each json item in variable
            gameId = c.getInt("GameId");
            //reset the playerCounter, if the a new game starts
            if (gameId != lastGameId) {
                if (currentGame != null) {//only add "filled" games
                    if (youAreInTeamOne) {
                        for (int n = 0; n < currentGame.getParticipants().length; n++) {
                            if (currentGame.getParticipant(n).getId() == userId && n > 1) {
                                currentGame.swopTeams();
                                break;
                            }
                        }
                    }
                    games.add(currentGame);
                }
                playerCounter = teamCounter = 0;//reset all counter
                currentGame = new GameData();//create a new game - all variables are set to not null - arrays are initialized
                firstRun = true;
            }

            assert currentGame != null;//a game cant be null at this point
            currentGame.setGameId(gameId);//will be overwritten 4 times (if no guest takes place)
            currentGame.setAreaId(c.getInt("AreaId"));
            currentGame.setDate(c.getString("Date"));//will be overwritten 4 times (if no guest takes place)
            currentGame.getParticipant(playerCounter).setId(c.getInt("Id"));
            score = c.getInt("Score");
            if (currentGame.getScores()[teamCounter] == -1) {

                currentGame.getScores()[teamCounter] = score;//save the score to check if the team changes (only with a guest possible)
            }
            currentGame.getParticipant(playerCounter).setFirstName(c.getString("FirstName"));
            currentGame.getParticipant(playerCounter).setLastName(c.getString("LastName"));
            currentGame.addConfirmed(c.getInt("Confirmed") == 1);
            if (c.getInt("Confirmed") == 1)
                currentGame.setConfirmed(playerCounter, c.getInt("Id"));
            currentGame.setDescription(c.getString("Confirmed"));

            //update counter vars
            if (score != lastScore && !firstRun) {
                teamCounter++;
                if (teamCounter == 2)
                    teamCounter = 0;
            }

            playerCounter++;
            lastGameId = gameId;
            lastScore = score;
            firstRun = false;
        }
        return games;
    }

    public List<GameData> getGameAdmins(List<GameData> games) throws JSONException, SessionErrorException, NoConnectionException {
        String fileUrl = SERVER_URL + "getGameAdmin.php?session=" + session;
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response == null)
            throw new JSONException("Nothing to show here");
        if (response.equalsIgnoreCase("#fail#session error"))
            throw new SessionErrorException(response);
        if (response.startsWith("#fail#"))
            throw new RuntimeException(response);

        JSONArray gameObjects = new JSONArray(response);
        for (int i = 0; i < gameObjects.length(); i++) {
            JSONObject c = gameObjects.getJSONObject(i);
            int gameId = c.getInt("GameId");
            int userId = c.getInt("Id");
            UserData admin = new UserData().setId(userId);
            for (GameData gd : games) {
                if (gd.getGameId() == gameId) {
                    gd.setAdmin(admin);
                    break;
                }
            }
        }
        return games;
    }

    /**
     * @return a list of all Areas
     * @throws SessionErrorException session is not set or outdated
     * @throws NoConnectionException Connection Timeout
     * @throws JSONException         the file is bad - might be an server problem
     * @throws RuntimeException      in here the server response is stored
     */
    @NotNull
    public List<AreaData> getAreas()
            throws SessionErrorException, NoConnectionException, RuntimeException, JSONException {
        String fileUrl = SERVER_URL + "getMyAreas.php?session=" + session;
        String response = serverHandler.getJsonFromServer(fileUrl);
        Log.e("getAreas", fileUrl);
        if (response == null)
            throw new JSONException("Nothing to show here");
        if (response.equalsIgnoreCase("#fail#session error"))
            throw new SessionErrorException(response);
        if (response.startsWith("#fail#"))
            throw new RuntimeException(response);

        JSONArray gameObjects = new JSONArray(response);
        List<AreaData> areas = new ArrayList<>();
        for (int i = 0; i < gameObjects.length(); i++) {
            JSONObject c = gameObjects.getJSONObject(i);
            areas.add(new AreaData(c));
        }
        getAreaAdmins(areas);
        return areas;
    }

    public List<AreaData> getAreaAdmins(List<AreaData> areas) throws JSONException, SessionErrorException, NoConnectionException {
        String fileUrl = SERVER_URL + "getAreaAdmin.php?session=" + session;
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response == null)
            throw new JSONException("Nothing to show here");
        if (response.equalsIgnoreCase("#fail#session error"))
            throw new SessionErrorException(response);
        if (response.startsWith("#fail#"))
            throw new RuntimeException(response);

        JSONArray gameObjects = new JSONArray(response);
        for (int i = 0; i < gameObjects.length(); i++) {
            JSONObject c = gameObjects.getJSONObject(i);
            int areaId = c.getInt("AreaId");
            int userId = c.getInt("Id");
            UserData admin = new UserData().setId(userId);
            for (AreaData ad : areas) {
                if (ad.getId() == areaId) {
                    ad.getAdmins().add(admin);
                    break;
                }
            }
        }
        return areas;
    }

    public List<AreaData> getAreaUsers(List<AreaData> areas) throws JSONException, SessionErrorException, NoConnectionException {
        String fileUrl = SERVER_URL + "getAreaUser.php?session=" + session;
        String response = serverHandler.getJsonFromServer(fileUrl);
        Log.e("getAreaUsers", fileUrl);
        if (response == null)
            return areas;
//            throw new JSONException("Nothing to show here");
        if (response.equalsIgnoreCase("#fail#session error"))
            throw new SessionErrorException(response);
        if (response.startsWith("#fail#"))
            throw new RuntimeException(response);

        JSONArray gameObjects = new JSONArray(response);
        for (int i = 0; i < gameObjects.length(); i++) {
            JSONObject c = gameObjects.getJSONObject(i);
            int areaId = c.getInt("AreaId");
            int userId = c.getInt("Id");
            UserData user = new UserData().setId(userId);
            for (AreaData ad : areas) {
                if (ad.getId() == areaId) {
                    ad.getUsers().add(user);
                    break;
                }
            }
        }
        return areas;
    }

    /**
     * @return All User from the database
     * @throws SessionErrorException session is not set or outdated
     * @throws NoConnectionException Connection Timeout
     * @throws JSONException         the file is bad - might be an server problem
     * @throws RuntimeException      in here the server response is stored
     */
    public List<UserData> getUser() throws JSONException, SessionErrorException, RuntimeException, NoConnectionException {
        String fileUrl = SERVER_URL + "getUser.php?session=" + session;
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response.equalsIgnoreCase("#fail#session error"))
            throw new SessionErrorException(response);
        if (response.startsWith("#fail#"))
            throw new RuntimeException(response);

        JSONArray gameObjects = new JSONArray(response);

        ArrayList<UserData> user = new ArrayList<>();

        for (int i = 0; i < gameObjects.length(); i++) {
            JSONObject c = gameObjects.getJSONObject(i);
            user.add(new UserData(c));
        }
        return user;
    }

    /**
     * @return All User from the database
     * @throws SessionErrorException session is not set or outdated
     * @throws NoConnectionException Connection Timeout
     * @throws JSONException         the file is bad - might be an server problem
     * @throws RuntimeException      in here the server response is stored
     */
    public List<UserData> getScoreboard() throws JSONException, SessionErrorException, RuntimeException, NoConnectionException, NoScoreboardException {
        String fileUrl = SERVER_URL + "getMyScoreboard.php?session=" + session;
        String response = serverHandler.getJsonFromServer(fileUrl);

        if (response == null)
            throw new NoScoreboardException();
        if (response.startsWith("#fail#"))
            Log.e("getScoreboard", fileUrl);
        if (response.equalsIgnoreCase("#fail#session error"))
            throw new SessionErrorException(response);
        if (response.startsWith("#fail#"))
            throw new NoConnectionException(response);

        JSONArray gameObjects = new JSONArray(response);

        ArrayList<UserData> user = new ArrayList<>();

        for (int i = 0; i < gameObjects.length(); i++) {
            JSONObject c = gameObjects.getJSONObject(i);
            user.add(new UserData(c));
        }
        return user;
    }

    /**
     * @return All EloLogs from the logged in user
     * @throws SessionErrorException session is not set or outdated
     * @throws NoConnectionException Connection Timeout
     * @throws JSONException         the file is bad - might be an server problem
     * @throws RuntimeException      in here the server response is stored
     */
    public List<EloData> getEloLog() throws JSONException, SessionErrorException, RuntimeException, NoConnectionException, NoGamesException {
        return getEloLog(0);
    }

    /**
     * @param userId The userId which elo will be returned - power >= 100 is required to chose from all users
     * @return All EloLogs from the logged in user
     * @throws SessionErrorException session is not set or outdated
     * @throws NoConnectionException Connection Timeout
     * @throws JSONException         the file is bad - might be an server problem
     * @throws RuntimeException      in here the server response is stored
     */
    public List<EloData> getEloLog(int userId) throws JSONException, SessionErrorException, RuntimeException, NoConnectionException, NoGamesException {
        String fileUrl = SERVER_URL + "getEloLog.php?session=" + session;
        if (userId > 0)
            fileUrl += "&userId=" + userId;
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response == null)
            throw new NoGamesException();
        if (response.equalsIgnoreCase("#fail#session error"))
            throw new SessionErrorException(response);
        if (response.startsWith("#fail#"))
            throw new NoConnectionException(response);

        JSONArray gameObjects = new JSONArray(response);

        ArrayList<EloData> eloLog = new ArrayList<>();

        for (int i = 0; i < gameObjects.length(); i++) {
            JSONObject c = gameObjects.getJSONObject(i);
            eloLog.add(new EloData(c));
        }
        return eloLog;
    }

    /**
     * @return Last 5 EloLogs from the logged in user in reverse order
     * @throws SessionErrorException session is not set or outdated
     * @throws NoConnectionException Connection Timeout
     * @throws JSONException         the file is bad - might be an server problem
     * @throws RuntimeException      in here the server response is stored
     */
    public List<EloData> getEloPreview() throws JSONException, SessionErrorException, RuntimeException, NoConnectionException {
        String fileUrl = SERVER_URL + "getEloPreview.php?session=" + session;
        Log.e("CALL", fileUrl);
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response.equalsIgnoreCase("#fail#session error"))
            throw new SessionErrorException(response);
        if (response.startsWith("#fail#"))
            throw new RuntimeException(response);

        JSONArray gameObjects = new JSONArray(response);

        ArrayList<EloData> eloLog = new ArrayList<>();

        for (int i = gameObjects.length() - 1; i >= 0; i--) {
            JSONObject c = gameObjects.getJSONObject(i);
            eloLog.add(new EloData(c));
        }
        return eloLog;
    }

    /**
     * @return this will get a UserData of the current logged in User
     * @throws SessionErrorException session is not set or outdated
     * @throws NoConnectionException Connection Timeout
     * @throws JSONException         the file is bad - might be an server problem
     */
    public UserData getYourself() throws NoConnectionException, SessionErrorException, JSONException {
        String fileUrl = SERVER_URL + "getYourself.php?session=" + session;
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response.equalsIgnoreCase("#fail#session error"))
            throw new SessionErrorException(response);
        if (response.startsWith("#fail#"))
            throw new RuntimeException(response);

        JSONObject gameObject = new JSONObject(response);

        return new UserData(gameObject);
    }

    /**
     * @param mail the mail to send a ne new password to
     * @throws MailNotTakenException the mail is not taken
     * @throws NoConnectionException no connection to the database
     */
    public void forgotPassword(String mail) throws MailNotTakenException, NoConnectionException {
        String fileUrl = SERVER_URL + "forgotPassword.php?mail=" + mail;
        Log.e("FORGOT", fileUrl);
        String response = serverHandler.getJsonFromServer(fileUrl);
        Log.e("FORGOT", response);
        if (response.startsWith("#fail#"))
            throw new MailNotTakenException();
    }

    /**
     * @return the mail off the logged in user
     * @throws NoConnectionException no connection to the database
     * @throws SessionErrorException session is not set or outdated
     * @throws JSONException         the file is bad - might be an server problem
     */
    public String getMail() throws NoConnectionException, SessionErrorException, JSONException {
        String url = SERVER_URL + "getMail.php?session=" + session;
        Log.e("Mail", url);
        String response = serverHandler.getJsonFromServer(url);
        if (response == null)
            throw new NoConnectionException();
        if (response.equalsIgnoreCase("#fail#session error"))
            throw new SessionErrorException(response);
        //TODO addErrorCases
        JSONArray gameObjects = new JSONArray(response);

        if (gameObjects.length() > 0) {
            JSONObject o = gameObjects.getJSONObject(0);
            return o.get("mail").toString();
        }
        return "";
    }

    public void changeUsername(String username) throws NoConnectionException, UsernameTakenException {
        String fileUrl = SERVER_URL + "changeUsername.php?session=" + session + "&username=" + username;
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response.startsWith(FAIL + "usernameTaken"))
            throw new UsernameTakenException();
        if (response.startsWith(FAIL))
            throw new NoConnectionException();
    }

    public void changePassword(String password, String oldPassword) throws NoConnectionException, WrongOldPasswordException {
        String fileUrl = SERVER_URL + "changePassword.php?session=" + session + "&password=" + password + "&old_password=" + oldPassword;
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response.startsWith(FAIL + "wrongOldPassword")) {
            Log.e("changePassword", fileUrl);
            throw new WrongOldPasswordException();
        }
        if (response.startsWith(FAIL))
            throw new NoConnectionException();
    }

    public void changeMail(String mail) throws NoConnectionException, MailTakenException {
        String fileUrl = SERVER_URL + "changeMail.php?session=" + session + "&mail=" + mail;
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response.startsWith(FAIL + "usernameTaken"))
            throw new MailTakenException();
        if (response.startsWith(FAIL))
            throw new NoConnectionException();
    }
}
