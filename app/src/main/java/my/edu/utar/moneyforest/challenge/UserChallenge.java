package my.edu.utar.moneyforest.challenge;

import java.io.Serializable;

import my.edu.utar.moneyforest.user.User;
/*This is a class which bridges the user and challenge data model. */
/*Done by Ng Jing Ying*/
public class UserChallenge implements Serializable {
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    private Challenge challenge;
}
