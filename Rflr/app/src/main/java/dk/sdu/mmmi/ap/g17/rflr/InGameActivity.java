package dk.sdu.mmmi.ap.g17.rflr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

public class InGameActivity extends AppCompatActivity {

    private boolean diceShown;
    private Spinner numberOfDiceSpinner, dieEyesSpinner;
    private Integer[] eyesAray, numberOfDiceArray;
    private ImageAdapter imageAdapter;
    private final int EYES = 6;
    private int totalNumberOfDice = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);

        diceShown = true;
        fillSpinnerArrays();

        GridView imageGridView = (GridView) findViewById(R.id.image_grid_view);
        imageAdapter = new ImageAdapter(this);
        imageGridView.setAdapter(imageAdapter);
    }

    /**
     * Change the total number of dice displayed in the spinner for guessing
     * @param totalNumberOfDice the total number of dice. +2 is added in the
     *                          method as both players having the stair results in a total of +2.
     */
    public void setTotalNumberOfDice(int totalNumberOfDice){
        //+2 as if both players has a stair (trappe) the total amount will be each cup + 1
        this.totalNumberOfDice = totalNumberOfDice + 2;
    }

    /**
     * Fills in the integers to be present in the spinners.
     * Populates the spinners by calling populateSpinners()
     */
    private void fillSpinnerArrays(){
        eyesAray = new Integer[EYES];
        for (int i = 1; i<=EYES; i++){
            eyesAray[i-1] = i;
        }
        numberOfDiceArray = new Integer[totalNumberOfDice];
        for (int i = 1; i<= totalNumberOfDice; i++){
            numberOfDiceArray[i-1] = i;
        }
        populateSpinners();
    }

    /**
     * Find spinners by id.
     * Uses an arrayadapter to populate spinners with integer values for
     * both the eyes and the number of dice to guess.
     */
    private void populateSpinners(){
        //Find spinners
        dieEyesSpinner = (Spinner) findViewById(R.id.die_eyes_spinner);
        numberOfDiceSpinner = (Spinner) findViewById(R.id.number_of_dice_spinner);

        ArrayAdapter<Integer> eyeAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, eyesAray);
        dieEyesSpinner.setAdapter(eyeAdapter);

        ArrayAdapter<Integer> numberOfDiceAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, numberOfDiceArray);
        numberOfDiceSpinner.setAdapter(numberOfDiceAdapter);
    }

    /**
     * Controls whether to hide or show the dice rolled.
     * @param v
     */
    public void hideShowDiceBtnHandler(View v){
        Button btn = (Button) findViewById(R.id.hide_show_btn); //Find button to change text on
        GridView gridview = (GridView) findViewById(R.id.image_grid_view);  //Find gridview
        if (diceShown){ //Changes button text hide/show
            btn.setText(getResources().getString(R.string.in_game_show_btntxt));
            diceShown = false;
            //Hide pictures of dice
            gridview.setVisibility(View.INVISIBLE);
        }
        else {
            btn.setText(getResources().getString(R.string.in_game_hide_btntxt));
            diceShown = true;
            //Show pictures of dice
            gridview.setVisibility(View.VISIBLE);
        }
    }

    public void makeGuessBtnHandler (View v){
        int eyes = (int) dieEyesSpinner.getSelectedItem();
        int numberOfDice = (int) numberOfDiceSpinner.getSelectedItem();
        Toast.makeText(getApplicationContext(), "Eyes: " + eyes + " Dice: " + numberOfDice, Toast.LENGTH_SHORT).show();
        //Test image updating
        Cup cup = new Cup(5);
        imageAdapter.setDiceImages(cup);
        imageAdapter.notifyDataSetChanged();    //must be called when images change!
    }

    public void liftBtnHandler(View v){
        Toast.makeText(getApplicationContext(), "We have liftoff!", Toast.LENGTH_SHORT).show();
    }
}
