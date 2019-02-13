package jan.schuettken.bierpongleague.data;

import android.annotation.SuppressLint;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Jan Schüttken on 30.10.2018 at 22:37
 */
public class GameData {

    public static final int PLAYER_IN_GAME = 4;
    public static final int SCORES_IN_GAME = 2;

    /**
     * <p>required array lengh = 4
     * <br>Position 0 is player A from team A
     * <br>Position 1 is player B from team A
     * <br>Position 2 is player C from team B
     * <br>Position 3 is player D from team B</p>
     */
    private UserData[] participants;
    /**
     * <p>required array lengh = 2
     * <br>Position 0 is the score from team A, score > 0 if this team won
     * <br>Position 1 is the score from team B, score > 0 if this team won</p>
     */
    private int[] scores;
    /**
     * The Area/League the game is placed in - currently only id 1
     */
    private int areaId;
    /**
     * The Id of the game only for existing games
     */
    private int gameId;
    /**
     * optional text description
     */
    private String description = null;
    /**
     * True: all participants have confirmed the game, false: at least one does't confirmed
     */
    private boolean confirmed = false;
    private int confirmedUser[] = {-1, -1, -1, -1};

    /**
     * The Timestamp when the game was created
     */
    private Date date = null;

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
     * @param confirmed    True: all participants have confirmed the game, false: at least one does't confirmed
     * @param description  optional text description
     */
    public GameData(@NonNull UserData[] participants, @NonNull int[] scores, @IntRange(from = 1) int areaId, boolean confirmed, String... description) {
        this.participants = participants;
        this.scores = scores;
        this.areaId = areaId;
        this.confirmed = confirmed;
        if (description.length > 0)
            this.description = description[0];
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
     */
    public GameData(@NonNull UserData[] participants, @NonNull int[] scores, @IntRange(from = 1) int areaId, String... description) {
        this(participants, scores, areaId, false, description);
    }


    /**
     * Initialises all variables with defaults
     */
    public GameData() {
        this(true);
    }

    public GameData(boolean initUserData) {
        participants = new UserData[4];
        if (initUserData)
            for (int i = 0; i < participants.length; i++)
                participants[i] = new UserData();
        scores = new int[2];
        for (int i = 0; i < scores.length; i++)
            scores[i] = -1;
        areaId = 1;
        description = null;
    }

    /**
     * @return <p>Position 0 is player A from team A
     * <br>Position 1 is player B from team A
     * <br>Position 2 is player C from team B
     * <br>Position 3 is player D from team B</p>
     */
    public UserData[] getParticipants() {
        return participants;
    }

    public UserData getParticipant(int position) {
        return participants[position];
    }

    public void swopTeams() {
        int tmpScore = scores[0];
        scores[0] = scores[1];
        scores[1] = tmpScore;
        UserData tmpUser = participants[0];
        participants[0] = participants[2];
        participants[2] = tmpUser;
        tmpUser = participants[1];
        participants[1] = participants[3];
        participants[3] = tmpUser;
    }


    @NonNull
    @Override
    public String toString() {
        StringBuilder back = new StringBuilder(super.toString());
        back.append(" {participants:[");
        for (UserData u : participants) {
            back.append(u.getId()).append(", ").append(u.getFirstName()).append(" ").append(u.getLastName()).append(" | ");
        }
        back.append("], score:[").append(scores[0]).append(", ").append(scores[1]).append("]}");
        return back.toString();
    }

    /**
     * @return <p>required array lengh = 2
     * <br>Position 0 is the score from team A, score > 0 if this team won
     * <br>Position 1 is the score from team B, score > 0 if this team won</p>
     */
    public int[] getScores() {
        return scores;
    }

    /**
     * @return The Area/League the game is placed in - currently only id 1
     */
    public int getAreaId() {
        return areaId;
    }

    /**
     * @return optional text description (can be null)
     */
    public String getDescription() {
        return description;
    }

    /**
     * Default is false
     *
     * @return True: all participants have confirmed the game, false: at least one does't confirmed
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Default is false
     *
     * @param userId the User id
     * @return True: the participants has confirmed the game, false: he has not
     */
    public boolean hasConfirmed(int userId) {
        for (int i : confirmedUser)
            if (i == userId)
                return true;
        return false;
    }

    public void setParticipants(UserData[] participants) {
        this.participants = participants;
    }

    public void setScores(int... scores) {
        this.scores = scores;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addConfirmed(boolean confirmed) {
        this.confirmed &= confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public void setConfirmed(int playerPos, int userId) {
        confirmedUser[playerPos] = userId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    @Nullable
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
            this.date = format.parse ( date);
        } catch (ParseException e) {
            this.date = null;
            e.printStackTrace();
        }
    }
}
