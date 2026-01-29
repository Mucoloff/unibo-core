package dev.sweety.unibo.feature.info;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class Stats {

    private double elo;

    @Setter
    private int wins, losses;
    private int winStreak, loseStreak;

    @Setter
    private boolean combat;

    public void addWin() {
        this.wins++;
        this.winStreak++;
        this.loseStreak = 0;
    }

    public void addLoss() {
        this.losses++;
        this.loseStreak++;
        this.winStreak = 0;
    }

    public void updateElo(final double value) {
        this.elo += value;
    }

    public double getKd() {
        if (this.losses == 0) return this.wins;
        return (double) this.wins / (double) this.losses;
    }

    public void apply(final Stats copy) {
        this.elo = copy.elo;
        this.wins = copy.wins;
        this.losses = copy.losses;
        this.winStreak = copy.winStreak;
        this.loseStreak = copy.loseStreak;
        this.combat = copy.combat;
    }
}
