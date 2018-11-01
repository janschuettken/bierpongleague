package jan.schuettken.bierpongleague.handler;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jan.schuettken.bierpongleague.data.GameData;
import jan.schuettken.bierpongleague.data.UserData;
import jan.schuettken.bierpongleague.exceptions.DatabaseException;
import jan.schuettken.bierpongleague.exceptions.InvalidLoginException;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;

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
        login(username, password);
    }

    /**
     * @param session A session id is required - if you have non use: {@link #ApiHandler(String, String) ApiHandler}
     */
    public ApiHandler(@NonNull String session) {
        this.session = session;
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
        String fileUrl = SERVER_URL + "updateConfirm.php?session=" + session;
        fileUrl += "?playerA=" + participants[0].getId();
        fileUrl += "&playerB=" + participants[1].getId();
        fileUrl += "&playerC=" + participants[2].getId();
        fileUrl += "&playerD=" + participants[3].getId();
        fileUrl += "&scoreA=" + scores[0];
        fileUrl += "&scoreB=" + scores[1];
        fileUrl += "&areaId=" + areaId;
        if (description.length > 0)
            fileUrl += "&description=" + description[0];
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response.equalsIgnoreCase("#fail#session error"))
            throw new SessionErrorException(response);
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
        fileUrl += "&addGame=" + gameId + "&confirm=" + confirm;
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

        String fileUrl = SERVER_URL + "login.php?username=" + username + "&password=" + password;
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response.startsWith("#fail#wrongPassword") || response.startsWith("#fail#usernameNotTaken"))
            throw new InvalidLoginException(response);
        if (response.startsWith("#fail#"))
            throw new DatabaseException(response);

        session = response;
        return true;
    }


    //TODO JAVADOC einfügen
    public List<GameData> getGames(@NonNull UserData user)
            throws NoConnectionException, SessionErrorException, RuntimeException, JSONException {
        return getGames(user.getId());
    }

    //TODO JAVADOC einfügen
    @NonNull
    public List<GameData> getGames(@IntRange(from = 0) int userId)
            throws SessionErrorException, NoConnectionException, RuntimeException, JSONException {
        String fileUrl = SERVER_URL + "updateConfirm.php?session=" + session;
        fileUrl += "?userId=" + userId;
        String response = serverHandler.getJsonFromServer(fileUrl);
        if (response.equalsIgnoreCase("#fail#session error"))
            throw new SessionErrorException(response);
        if (response.startsWith("#fail#"))
            throw new RuntimeException(response);

        //start parsing the JSON
        //TODO ich bin mir nich sicher, ob der Code so funktioniert! Er ist nicht getestet!
        JSONObject jObjects = new JSONObject(response);
        JSONArray gameObjects = jObjects.getJSONArray("");
        ArrayList<GameData> games = new ArrayList<>();
        int playerCounter = 0, teamCounter = 0;
        int lastGameId = -1, lastScore = -1;
        int gameId, score;//set to 1 for Weiderhold
        GameData currentGame = null;
        for (int i = 0; i < gameObjects.length(); i++) {
            JSONObject c = gameObjects.getJSONObject(i);

            // Storing each json item in variable
            gameId = c.getInt("GameId");
            //reset the playerCounter, if the a new game starts
            if (gameId != lastGameId) {
                if (currentGame != null)//only add "filled" games
                    games.add(currentGame);
                playerCounter = teamCounter = 0;//reset all counter
                currentGame = new GameData();//create a new game - all variables are set to not null - arrays are initialized
            }

            assert currentGame != null;//a game cant be null at this point
            currentGame.setGameId(gameId);//will be overwritten 4 times (if no guest takes place)
            currentGame.getParticipant(playerCounter).setId(c.getInt("Id"));
            currentGame.getScores()[teamCounter] = score = c.getInt("Score");//save the score to check if the team changes (only with a guest possible)
            currentGame.getParticipant(playerCounter).setFirstName(c.getString("FirstName"));
            currentGame.getParticipant(playerCounter).setLastName(c.getString("LastName"));
            currentGame.setConfirmed(c.getInt("Confirmed") == 1);
            currentGame.setDescription(c.getString("Confirmed"));

            //update counter vars
            playerCounter++;
            if (lastScore != score)
                teamCounter++;
            lastGameId = gameId;
            lastScore = score;

        }

        return games;
    }
}