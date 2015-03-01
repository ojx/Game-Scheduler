import java.time.LocalDate;

/**
 * A class to represent a game and to store it's result.
 * The game can be scheduled by setting it's date.
 * The result can also be set and stored.
 */
public class Game {
    private LocalDate date;
    private String homeTeam;
    private String awayTeam;
    private boolean played;
    private int homeScore, awayScore;

    public Game(String homeTeam, String awayTeam) {
        this.date = null;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        played = false;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }
    
    public void setScore(int homeScore, int awayScore) {
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        played = true;
    }

    public int getAwayScore() {
        return awayScore;
    }

    public int getHomeScore() {
        return homeScore;
    }
    
    public boolean isScheduled() {
        return date != null;
    }

    public boolean isPlayed() {
        return played;
    }
    
    public boolean hasWinner() {
        return  isPlayed() && homeScore != awayScore;
    }
    
    public String getWinner() {
        if (hasWinner()) {
            if (homeScore > awayScore)
                return homeTeam;
            else
                return awayTeam;
        } else {
            return null;
        }
    }
    
    public boolean involves(String team) {
        return homeTeam.equals(team) || awayTeam.equals(team);
    } 
    
    //for europeans
    public boolean isDraw() {
        return isPlayed() && homeScore == awayScore;
    }
    
    //for north americans
    public boolean isTie() {
        return isDraw();
    } 
}
