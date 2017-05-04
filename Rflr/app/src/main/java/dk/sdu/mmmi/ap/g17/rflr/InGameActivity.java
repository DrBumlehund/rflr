package dk.sdu.mmmi.ap.g17.rflr;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class InGameActivity extends AppCompatActivity {

    private static final String TAG = "IN_GAME_ACTIVITY";

    private boolean diceShown;
    private Spinner numberOfDiceSpinner, dieEyesSpinner;
    private Integer[] eyesArray, numberOfDiceArray;
    private ImageAdapter imageAdapter;
    private final int EYES = 6;
    private int totalNumberOfDice = 15;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private Cup cup;
    private boolean hasSentCup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);
        setupShakeDetector();
        diceShown = true;
        hasSentCup = false;

        fillSpinnerArrays();

        GridView imageGridView = (GridView) findViewById(R.id.image_grid_view);
        imageAdapter = new ImageAdapter(this);
        imageGridView.setAdapter(imageAdapter);

        cup = new Cup(6);
        updateDiceImages();

        BluetoothService.getInstance().setmHandler(mHandler);
        BluetoothService.getInstance().setmContext(getApplicationContext());
        BluetoothService.getInstance().startConnected();
    }

    private void setupShakeDetector() {
        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new OnShakeListener() {

            //Define what should be done on shake event
            @Override
            public void onShake() {
                cup.roolAllDice();
                updateDiceImages();
            }
        });
    }

    /**
     * Change the total number of dice displayed in the spinner for guessing
     *
     * @param totalNumberOfDice the total number of dice. +2 is added in the
     *                          method as both players having the stair results in a total of +2.
     */
    public void setTotalNumberOfDice(int totalNumberOfDice) {
        //+2 as if both players has a stair (trappe) the total amount will be each cup + 1
        this.totalNumberOfDice = totalNumberOfDice + 2;
    }

    /**
     * Fills in the integers to be present in the spinners.
     * Populates the spinners by calling populateSpinners()
     */
    private void fillSpinnerArrays() {
        eyesArray = new Integer[EYES];
        for (int i = 1; i <= EYES; i++) {
            eyesArray[i - 1] = i;
        }
        numberOfDiceArray = new Integer[totalNumberOfDice];
        for (int i = 1; i <= totalNumberOfDice; i++) {
            numberOfDiceArray[i - 1] = i;
        }
        populateSpinners();
    }

    /**
     * Find spinners by id.
     * Uses an arrayadapter to populate spinners with integer values for
     * both the eyes and the number of dice to guess.
     */
    private void populateSpinners() {
        //Find spinners
        dieEyesSpinner = (Spinner) findViewById(R.id.die_eyes_spinner);
        numberOfDiceSpinner = (Spinner) findViewById(R.id.number_of_dice_spinner);

        ArrayAdapter<Integer> eyeAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, eyesArray);
        dieEyesSpinner.setAdapter(eyeAdapter);

        ArrayAdapter<Integer> numberOfDiceAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, numberOfDiceArray);
        numberOfDiceSpinner.setAdapter(numberOfDiceAdapter);
    }

    private void updateDiceImages() {
        if (cup != null) {
            imageAdapter.setDiceImages(cup);
            imageAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Controls whether to hide or show the dice rolled.
     *
     * @param v
     */
    public void hideShowDiceBtnHandler(View v) {
        Button btn = (Button) findViewById(R.id.hide_show_btn); //Find button to change text on
        GridView gridview = (GridView) findViewById(R.id.image_grid_view);  //Find gridview
        if (diceShown) { //Changes button text hide/show
            btn.setText(getResources().getString(R.string.in_game_show_btntxt));
            diceShown = false;
            //Hide pictures of dice
            gridview.setVisibility(View.INVISIBLE);
        } else {
            btn.setText(getResources().getString(R.string.in_game_hide_btntxt));
            diceShown = true;
            //Show pictures of dice
            gridview.setVisibility(View.VISIBLE);
        }
    }

    public void makeGuessBtnHandler(View v) {
        int eyes = (int) dieEyesSpinner.getSelectedItem();
        int numberOfDice = (int) numberOfDiceSpinner.getSelectedItem();
        Toast.makeText(getApplicationContext(), "Eyes: " + eyes + " Dice: " + numberOfDice, Toast.LENGTH_SHORT).show();

        // GUESS MESSAGE FOLLOWS FORMAT : DEVICE BLUETOOTH NAME ; MESSAGE TYPE ; AMOUNT OF DIES : DIE EYES
        String message = BluetoothService.getInstance().getBluetoothName() + ";" + Constants.GUESS + ";" + numberOfDiceSpinner.getSelectedItem() + ":" + dieEyesSpinner.getSelectedItem();
        byte[] bytes = message.getBytes();
        Log.v(TAG, message + " ByteLength: " + bytes.length);
        if (bytes != null) {
            BluetoothService.getInstance().write(bytes);
        }
    }

    private void sendCup() {
        // GUESS MESSAGE FOLLOWS FORMAT : DEVICE BLUETOOTH NAME ; MESSAGE TYPE ; #1's : 1, #2's : 2, ...
        String message = BluetoothService.getInstance().getBluetoothName() + ";" + Constants.CUP + ";" + cup.toString();
        BluetoothService.getInstance().write(message.getBytes());
        hasSentCup = true;
    }

    public void liftBtnHandler(View v) {
        sendCup();
        Toast.makeText(getApplicationContext(), "We have liftoff!", Toast.LENGTH_SHORT).show();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    String[] readMessage = DecodeByteMessage((byte[]) msg.obj);
                    int readMessageContentType = Integer.parseInt(readMessage[1]);
                    switch (readMessageContentType) {
                        case Constants.GUESS:
                            TextView tv = (TextView) findViewById(R.id.last_call_value_label);
                            tv.setText(readMessage[0] + " : " + readMessage[2]);
                            break;
                        case Constants.CUP:
                            if (!hasSentCup) {
                                sendCup();
                            } else {
                                hasSentCup = false;
                            }
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    String[] writeMessage = DecodeByteMessage((byte[]) msg.obj);

                    int writeMessageContentType = Integer.parseInt(writeMessage[1]);
                    switch (writeMessageContentType) {
                        case Constants.GUESS:
                            TextView tv = (TextView) findViewById(R.id.last_call_value_label);
                            tv.setText("You : " + writeMessage[2]);
                            break;
                        case Constants.CUP:
                            break;
                    }

                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST_KEY), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };


    private String[] DecodeByteMessage(byte[] message) {
        String readMsg = new String(message);
        return readMsg.split(";");
    }

    @Override
    public void onResume() {
        super.onResume();
        //Register the Sensor Manager onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        //unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothService.getInstance().stop();
    }
}
