package dk.sdu.mmmi.ap.g17.rflr;


import android.support.annotation.NonNull;

import java.util.Random;

/**
 * Created by thlem14 on 23-Mar-17.
 * A Die object can be used to roll, to generate a new value.
 */

public class Die implements Comparable {

    protected Die() {
        rng = new Random();
        this.roll();
    }

    private Random rng;
    private int value;
    private int orientation;

    protected void roll() {
        value = rng.nextInt(6) + 1;
        orientation = rng.nextInt(4);
    }

    protected int getValue() {
        return value;
    }

    public int getOrientation() {
        return orientation;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        Die other = (Die) o;
        if (other.getValue() == this.value) {
            return 0;
        } else if (other.getValue() > this.getValue()) {
            return -1;
        } else {
            return 1;
        }

    }
}
