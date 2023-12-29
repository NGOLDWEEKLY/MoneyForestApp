package my.edu.utar.moneyforest.user;

import java.io.Serializable;

//done by Wong Tze-Qing, Sarah
public class User implements Serializable {
    private String id;
    private int xp;
    private String badge;
    private int coin;
    private String joinDate;
    private String name;
    private String pfp;
    private int streak;
    private int rank;

    public User(String id) {
        this.id = id;
    }

    public User(String id, int xp, String badge, int coin, String joinDate, String name,
                String pfp, int streak, int rank) {
        this.id = id;
        this.xp = xp;
        this.badge = badge;
        this.coin = coin;
        this.joinDate = joinDate;
        this.name = name;
        this.pfp = pfp;
        this.streak = streak;
        this.rank = rank;
    }

    //empty constructor
    public User() {
    }


    public String getId() {
        return id;
    }

    public int getXp() {
        return xp;
    }

    public String getBadge() {
        return badge;
    }

    public int getCoin() {
        return coin;
    }

    public String getName() {
        return name;
    }

    public String getJoinDate() {
        return joinDate;
    }

    public String getPfp() {
        return pfp;
    }

    public int getStreak() {
        return streak;
    }

    public int getRank() {
        return rank;
    }

}
