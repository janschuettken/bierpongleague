package jan.schuettken.bierpongleague.handler;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jan.schuettken.bierpongleague.data.EloData;
import jan.schuettken.bierpongleague.data.GameData;
import jan.schuettken.bierpongleague.data.UserData;
import jan.schuettken.bierpongleague.exceptions.DatabaseException;
import jan.schuettken.bierpongleague.exceptions.InvalidLoginException;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;
import jan.schuettken.bierpongleague.exceptions.UsernameTakenException;

/**
 * Created by Jan Schüttken on 30.10.2018 at 21:23
 * All Api functions are stored here
 */
public class ApiHandler {
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
     * @param session Sets the session id<br>You can get one from: {@link #login(String, String) login}
     */
    public void setSession(@NonNull String session) {
        this.session = session;
    }

    /**
     * For an description look at  {@link #login(String, String) login}
     */
    public ApiHandler(@NonNull String username, @NonNull String password) throws InvalidLoginException, NoConnectionException, DatabaseException {
        this();
        login(username, password);
    }

    /**
     * @param session A session id is required - if you have non use: {@link #ApiHandler(String, String) ApiHandler}
     */
    public ApiHandler(@NonNull String session) {
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
     * @param game the filled game. For futher information look at {@link #addGame(UserData[], int[], int, String[]) addGame}
     * @return true: the game is confirmed
     * @throws SessionErrorException session is not set or outdated
     * @throws NoConnectionException Connection Timeout
     * @throws RuntimeException      in here the server response is stored
     */
    public boolean addGame(GameData game) throws NoConnectionException, SessionErrorException, RuntimeException {
        String[] des = new String[game.getDescription() == null ? 0 : 1];
        if (game.getDescription() != null)
            des[0] = game.getDescription();
        return addGame(game.getParticipants(), game.getScores(), game.getAreaId(), des);
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
    public boolean addGame(@NonNull UserData[] participants, @NonNull int[] scores, @IntRange(from = 1) int areaId, String... description)
            throws SessionErrorException, NoConnectionException, RuntimeException {//TODO Funktion ändern, wenn mehere Areas möglich sind

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
        if (description.length > 0)
            fileUrl += "&description=" + description[0];
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response.equalsIgnoreCase("#fail#session error")) {
            Log.e("ADD_GAME", fileUrl);
            throw new SessionErrorException(response);
        }
        if (response.startsWith("#fail#"))
            throw new RuntimeException(response);
        return true;
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
    public boolean confirmGame(@IntRange(from = 1) int gameId, boolean confirm)
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
     * @param username the Username of the UserData
     * @param password the matching Password
     * @return true: the new SessionId is set in the {@link #getSession() session} Parameter of this class
     * @throws NoConnectionException Connection Timeout
     * @throws InvalidLoginException Password does not match the Username
     * @throws DatabaseException     Internal server error - maybe the server crashed?
     */
    public boolean login(@NonNull String username, @NonNull String password)
            throws NoConnectionException, InvalidLoginException, DatabaseException {
        String fileUrl = SERVER_URL + "login.php?user=" + username + "&password=" + password;
        Log.e("LOGIN", fileUrl);
        String response = serverHandler.getJsonFromServer(fileUrl);
        Log.e("LOGIN", response);
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
    public boolean register(@NonNull UserData user) throws NoConnectionException, UsernameTakenException {
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
    public List<GameData> getGames(@NonNull UserData user, boolean youAreInTeamOne)
            throws NoConnectionException, SessionErrorException, RuntimeException, JSONException {
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
    @NonNull
    public List<GameData> getGames(@IntRange(from = 0) int userId, boolean youAreInTeamOne)
            throws SessionErrorException, NoConnectionException, RuntimeException, JSONException {
        String fileUrl = SERVER_URL + "getGame.php?session=" + session;
        fileUrl += "&userId=" + userId;
        String response = serverHandler.getJsonFromServer(fileUrl);

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
    public List<UserData> getScoreboard() throws JSONException, SessionErrorException, RuntimeException, NoConnectionException {
        String fileUrl = SERVER_URL + "getScoreboard.php?session=" + session;
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
     * @return All EloLogs from the logged in user
     * @throws SessionErrorException session is not set or outdated
     * @throws NoConnectionException Connection Timeout
     * @throws JSONException         the file is bad - might be an server problem
     * @throws RuntimeException      in here the server response is stored
     */
    public List<EloData> getEloLog() throws JSONException, SessionErrorException, RuntimeException, NoConnectionException {
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
    public List<EloData> getEloLog(int userId) throws JSONException, SessionErrorException, RuntimeException, NoConnectionException {
        String fileUrl = SERVER_URL + "getEloLog.php?session=" + session;
        if (userId > 0)
            fileUrl += "&userId=" + userId;
        Log.e("CALL", fileUrl);
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response.equalsIgnoreCase("#fail#session error"))
            throw new SessionErrorException(response);
        if (response.startsWith("#fail#"))
            throw new RuntimeException(response);

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
}
