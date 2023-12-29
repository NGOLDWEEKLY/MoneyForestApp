package my.edu.utar.moneyforest.challenge;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/*Done by Ng Jing Ying*/
/*This is a data model used to save the challenge result,
 * This class is used to assist Challenge Outcome Activity */
public class ChallengeResult implements Serializable {
    public static final int WIN = 0;
    public static final int LOSE = -1;
    private String chalName = "";
    private double happiLevel = 0;
    private double stableLevel = 0;
    private String apikey = "";
    private ArrayList<Map<String, String>> properActions = new ArrayList<>();
    private ArrayList<Map<String, String>> improperActions = new ArrayList<>();

    private int outcome = LOSE;

    public String getChalName() {
        return chalName;
    }

    public double getHappiLevel() {
        return happiLevel;
    }

    public void setHappiLevel(double happiLevel) {
        this.happiLevel = happiLevel;
    }

    public double getStableLevel() {
        return stableLevel;
    }

    // Financial Stability is assessed according to the accumulated money in the challenge
    public void calculateStableLevel(double moneyInBank) {
        if (moneyInBank < 0)
            this.stableLevel = 0.1;
        else if (moneyInBank > 2000)
            this.stableLevel = 0.9;
        else
            this.stableLevel = 0.5;
    }

    public void setChalName(String chalName) {
        this.chalName = chalName;
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public int getXP() {
        return (int) ((happiLevel + stableLevel) * 10);
    }

    public int getCoin() {
        return (int) ((stableLevel) * 10);
    }

    public int getOutcome() {
        return outcome;
    }

    public ChallengeResult() {
    }

    public ChallengeResult(ArrayList<Map<String, String>> properActions, ArrayList<Map<String, String>> improperActions) {
        this.properActions = properActions;
        this.improperActions = improperActions;
    }

    public ArrayList<Map<String, String>> getProperActions() {
        return properActions;
    }

    public void setProperActions(ArrayList<Map<String, String>> properActions) {
        this.properActions = properActions;
    }

    public ArrayList<Map<String, String>> getImproperActions() {
        return improperActions;
    }

    public void setImproperActions(ArrayList<Map<String, String>> improperActions) {
        this.improperActions = improperActions;
    }

    public void calculateOutcome() {
        if (happiLevel >= 0.5 && stableLevel >= 0.5) {
            outcome = WIN;
        } else
            outcome = LOSE;
    }
}
