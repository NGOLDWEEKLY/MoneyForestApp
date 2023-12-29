package my.edu.utar.moneyforest.challenge;

import java.io.Serializable;
import java.util.ArrayList;

/*This is the data model of Challenge entity,
it is synchronized with the "user_challenge/<u_id>" data in database*/
/*Done by Ng Jing Ying*/
public class Challenge implements Serializable {
    public static final int CHALLENGE_NEW = 0;
    public static final int CHALLENGE_COMPLETE = 100;
    private int id;
    private String name;
    private String desc;
    private String content;
    private String brief;
    private String chalImg;
    private String apikey = "";

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    private boolean popular;
    private String chalHistory;
    private int progress;
    private float moneyInBank;
    private String currQues;
    private ArrayList<String> currOptions;


    private ArrayList<NPCHappiness> npcHappinesses = new ArrayList<NPCHappiness>();

    public boolean isPopular() {
        return popular;
    }

    public ArrayList<NPCHappiness> getNpcHappinesses() {
        return npcHappinesses;
    }

    public void addNPCHappiness(String name, double happilevel) {
        NPCHappiness npcHappiness = new NPCHappiness(name, happilevel);
        this.npcHappinesses.add(npcHappiness);
    }

    public String getNPCName(int index) {
        return npcHappinesses.get(index).getName();
    }

    public double getNPCHappinessLevel(int index) {
        return npcHappinesses.get(index).getHappiLevel();
    }

    public float getMoneyInBank() {
        return moneyInBank;
    }

    public void setMoneyInBank(float moneyInBank) {
        this.moneyInBank = moneyInBank;
    }

    public String getCurrQues() {
        return currQues;
    }

    public void setCurrQues(String currQues) {
        this.currQues = currQues;
    }

    public ArrayList<String> getCurrOptions() {
        return currOptions;
    }

    public void setCurrOptions(ArrayList<String> currOptions) {
        this.currOptions = currOptions;
    }

    public String getChalHistory() {
        return chalHistory;
    }

    public Challenge(int id, String name, String desc, String content, String brief,
                     int progress, String chalImg, String chalHistory, boolean popular) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.content = content;
        this.brief = brief;
        this.popular = popular;
        this.progress = progress;
        this.chalImg = chalImg;
        this.chalHistory = chalHistory;
    }

    public String getChalImg() {
        return chalImg;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getContent() {
        return content;
    }

    public String getBrief() {
        return brief;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }

    public void setChalHistory(String chalHistory) {
        this.chalHistory = chalHistory;
    }

    public void removeAllHappiness() {
        npcHappinesses.clear();
    }

    public class NPCHappiness implements Serializable {
        private String name;
        private double happiLevel;

        public NPCHappiness(String name, double happiLevel) {
            this.name = name;
            this.happiLevel = happiLevel;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getHappiLevel() {
            return happiLevel;
        }

        public void setHappiLevel(double happiLevel) {
            this.happiLevel = happiLevel;
        }
    }
}
