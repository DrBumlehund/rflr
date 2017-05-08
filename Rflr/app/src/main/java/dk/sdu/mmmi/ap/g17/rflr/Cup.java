package dk.sdu.mmmi.ap.g17.rflr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by drbum on 23-Mar-17.
 * Object to keep all dice etc. (like 'hand' in a game of cards).
 * Rules for the game, in danish:   https://da.wikipedia.org/wiki/T%C3%A6nkeboks_(terningspil)
 */

public class Cup {

    private static final int DEFAULT_N_DICE = 6;
    private HashMap<Integer, Integer> score;

    public Cup() {
        this(DEFAULT_N_DICE);
    }

    public Cup(int nDice) {
        hasRolledThisRound = false;
        dice = new ArrayList<>();
        setUpArray(nDice);
    }

    private void setUpArray(int nDice) {
        for (int i = 0; i < nDice; i++) {
            dice.add(new Die());
        }
    }

    private ArrayList<Die> dice;
    private boolean hasRolledThisRound;

    public void newRound() {
        // TODO: REMEMBER TO CALL THIS ON NEW ROUND!
        hasRolledThisRound = false;
    }

    public void roolAllDice() {
        if (!hasRolledThisRound) {
            for (Die d : dice) {
                d.roll();
            }
            hasRolledThisRound = true;
        }

    }

    private boolean hasStair() {
        Collections.sort(dice);
        for (int i = 0; i < dice.size(); i++) {
            if (dice.get(i).getValue() != i + 1) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String cup = "";
        HashMap<Integer, Integer> score = getScore();
        for (Integer die :
                score.keySet()) {
            cup += die + ":" + score.get(die) + ",";
        }
        cup = cup.substring(cup.length() - 1);
        return cup;
    }

    public void removeDie() {
        dice.remove(dice.size() - 1);
    }

    public ArrayList<Die> getDice() {
        return dice;
    }

    /**
     * Key = Die eyes, Value = amount of that Die.
     *
     * @return
     */
    public HashMap<Integer, Integer> getScore() {
        score = new HashMap<>();

        if (hasStair()) {
            score.put(1, dice.size() + 1);
        } else {
            for (Die d : dice) {
                if (score.containsKey(d.getValue())) {
                    score.put(d.getValue(), score.get(d.getValue()) + 1);
                } else {
                    score.put(d.getValue(), 1);
                }
            }
        }

        return score;
    }
}
