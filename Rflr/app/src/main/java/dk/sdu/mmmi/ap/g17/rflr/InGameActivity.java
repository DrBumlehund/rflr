package dk.sdu.mmmi.ap.g17.rflr;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Random;

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
    private boolean mBtServiceBound;
    private int LastGuessDieCount = -1;
    private int LastGuessDieEyes = -1;
    private BluetoothService mBTService;
    private boolean myTurn = false;
    private int myRandomNumber;
    private boolean connectionEstablished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);
        setupShakeDetector();
        diceShown = true;
        hasSentCup = false;

        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mBTServiceConnection, Context.BIND_IMPORTANT);
        startService(intent);

        fillSpinnerArrays();

        GridView imageGridView = (GridView) findViewById(R.id.image_grid_view);
        imageAdapter = new ImageAdapter(this);
        imageGridView.setAdapter(imageAdapter);

        cup = new Cup(6);
        updateGame();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mBtServiceBound) {
                    Log.v(TAG, "Connection to Bluetooth Service was established");
                    mBTService.setmHandler(mHandler);
                    mBTService.startConnected();
                    sendHandshake();
                }
            }
        }, 2500);
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
                updateGame();
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

    private void updateGame() {
        if (cup != null) {
            imageAdapter.setDiceImages(cup);
            imageAdapter.notifyDataSetChanged();

            if (cup.getDice().isEmpty()) {
                Toast.makeText(getApplicationContext(), "You win the Game, You are superior!", Toast.LENGTH_LONG);
                Toast.makeText(getApplicationContext(), "Go back to start new game", Toast.LENGTH_LONG);
            }
        }
        updateButtons();
    }

    /**
     * Hides/Shows the buttons based upon the myTurn variable.
     */
    private void updateButtons() {
        if (myTurn) {
            findViewById(R.id.lift_btn).setVisibility(View.VISIBLE);
            findViewById(R.id.make_guess_btn).setVisibility(View.VISIBLE);
            dieEyesSpinner.setVisibility(View.VISIBLE);
            numberOfDiceSpinner.setVisibility(View.VISIBLE);
            findViewById(R.id.die_eyes_label).setVisibility(View.VISIBLE);
            findViewById(R.id.number_of_dice_label).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.lift_btn).setVisibility(View.INVISIBLE);
            findViewById(R.id.make_guess_btn).setVisibility(View.INVISIBLE);
            dieEyesSpinner.setVisibility(View.INVISIBLE);
            numberOfDiceSpinner.setVisibility(View.INVISIBLE);
            findViewById(R.id.die_eyes_label).setVisibility(View.INVISIBLE);
            findViewById(R.id.number_of_dice_label).setVisibility(View.INVISIBLE);
        }

        // You should not be able to lift when there hasn't been any guess's
        if (LastGuessDieCount == -1 && LastGuessDieEyes == -1) {
            findViewById(R.id.lift_btn).setVisibility(View.INVISIBLE);
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


        if (mBtServiceBound) {
            // GUESS MESSAGE FOLLOWS FORMAT : DEVICE BLUETOOTH NAME ; MESSAGE TYPE ; AMOUNT OF DIES : DIE EYES ;
            String message = mBTService.getBluetoothName() + ";" + Constants.GUESS + ";" + numberOfDiceSpinner.getSelectedItem() + ":" + dieEyesSpinner.getSelectedItem() + ";";
            byte[] bytes = message.getBytes();
            Log.v(TAG, message + " ByteLength: " + bytes.length);
            if (bytes != null) {
                mBTService.write(bytes, Constants.GUESS);
            }
        }
    }

    private void sendCup() {

        if (mBtServiceBound) {
            LastGuessDieCount = -1;
            LastGuessDieEyes = -1;
            // CUP MESSAGE FOLLOWS FORMAT : DEVICE BLUETOOTH NAME ; MESSAGE TYPE ; 1:#1's, 2:#2's, ... , 6:#6's ;
            String message = mBTService.getBluetoothName() + ";" + Constants.CUP + ";" + cup.toString() + ";";
            mBTService.write(message.getBytes(), Constants.CUP);
            hasSentCup = true;
        }
    }

    public void liftBtnHandler(View v) {
        sendCup();
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    myTurn = true;
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.v(TAG, "Received message: " + readMessage);
                    String[] readMessageSplit = readMessage.split(";");
                    int readMessageContentType = Integer.parseInt(readMessageSplit[1]);
                    switch (readMessageContentType) {
                        case Constants.GUESS:
                            TextView tv = (TextView) findViewById(R.id.last_call_value_label);
                            tv.setText(readMessageSplit[0] + " : " + readMessageSplit[2]);
                            String[] guess = readMessageSplit[2].split(":");
                            LastGuessDieCount = Integer.parseInt(guess[0]);
                            LastGuessDieEyes = Integer.parseInt(guess[1]);
                            break;
                        case Constants.CUP:
                            // I received a cup...
                            if (hasSentCup) {
                                // ...and I lifted
                                if (!calculateWin(readMessageSplit[2])) {
                                    // I won, and get to remove a die.
                                    Toast.makeText(getApplicationContext(), "You Win!", Toast.LENGTH_SHORT).show();
                                    myTurn = false;
                                    cup.removeDie();
                                } else {
                                    Toast.makeText(getApplicationContext(), "You lost...", Toast.LENGTH_SHORT).show();
                                    myTurn = true;
                                }
                                hasSentCup = false;
                            } else {
                                // ...and the other player lifted
                                if (calculateWin(readMessageSplit[2])) {
                                    // I won, and get to remove a die.
                                    Toast.makeText(getApplicationContext(), "You Win!", Toast.LENGTH_SHORT).show();
                                    myTurn = false;
                                    cup.removeDie();
                                } else {
                                    Toast.makeText(getApplicationContext(), "You lost...", Toast.LENGTH_SHORT).show();
                                    myTurn = true;
                                }
                                // Send my cup over, so he can calculate score as well.
                                sendCup();
                            }
                            break;
                        case Constants.HANDSHAKE:
                            if (!connectionEstablished) {
                                int otherRandom = Integer.parseInt(readMessageSplit[2]);
                                // In case the two randoms are equal.
                                if (otherRandom == myRandomNumber) {
                                    // Send new random.
                                    sendHandshake();
                                    break;
                                }
                                myTurn = myRandomNumber > otherRandom;
                                connectionEstablished = true;
                                Log.v(TAG, "Connected to other device, myTurn = " + myTurn);
                            }
                            break;
                    }
                    updateGame();
                    break;
                case Constants.MESSAGE_WRITE:
                    switch (msg.arg2) {
                        case Constants.GUESS:
                            TextView tv = (TextView) findViewById(R.id.last_call_value_label);
                            tv.setText("You : " + numberOfDiceSpinner.getSelectedItem() + ":" + dieEyesSpinner.getSelectedItem());
                            LastGuessDieCount = Integer.parseInt(numberOfDiceSpinner.getSelectedItem().toString());
                            LastGuessDieEyes = Integer.parseInt(dieEyesSpinner.getSelectedItem().toString());
                            myTurn = false;
                            break;
                        case Constants.CUP:
                            break;
                        case Constants.HANDSHAKE:
                            // if I send handshake, I want to resend the handshake,
                            // as long as i don't receive any myself
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!connectionEstablished) {
                                        Log.v(TAG, "Trying to send Handshake");
                                        sendHandshake();
                                    }
                                }
                            }, 500);
                    }
                    updateGame();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST_KEY), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    /**
     * Calculates if the last guess wins the round
     *
     * @param cupString
     * @return
     */
    private boolean calculateWin(String cupString) {
        // Reconstruct the recieved cup, based on the format:
        // CUP MESSAGE FOLLOWS FORMAT : "1:x, 2:y, ..." Where x and y are number of dice of the given type.
        Log.v(TAG, "received CUP: " + cupString + " - MyCup = " + cup.toString());
        HashMap<Integer, Integer> myCup = this.cup.getScore();
        String[] cupContents = cupString.split(",");
        for (String i : cupContents) {
            String[] j = i.split(":");
            int eyes = Integer.parseInt(j[0]), number = Integer.parseInt(j[1]);
            if (myCup.containsKey(eyes)) {
                myCup.put(eyes, myCup.get(eyes) + number);
            } else {
                myCup.put(eyes, number);
            }
        }

        // Apply the rule of the die 1, being a joker, counting as any die
        if (myCup.containsKey(1)) {
            for (int d = 2; d <= 6; d++) {
                if (myCup.containsKey(d)) {
                    myCup.put(d, myCup.get(d) + myCup.get(1));
                } else {
                    myCup.put(d, myCup.get(1));
                }
            }
            myCup.remove(1);
        }
        cup.newRound();

        if (myCup.containsKey(LastGuessDieEyes)) {
            if (myCup.get(LastGuessDieEyes) >= LastGuessDieCount) {
                return true;
            }
        }
        return false;
    }


    private synchronized void sendHandshake() {
        if (mBtServiceBound) {
            // Send random integer over, biggest will start.
            myRandomNumber = new Random().nextInt();
            String message = mBTService.getBluetoothName() + ";" + Constants.HANDSHAKE + ";" + myRandomNumber + ";";
            Log.v(TAG, "sending message, for handshake : " + message);
            mBTService.write(message.getBytes(), Constants.HANDSHAKE);
        }
    }

    private ServiceConnection mBTServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(TAG, "BT Service Connected");
            BluetoothService.BluetoothServiceBinder binder = (BluetoothService.BluetoothServiceBinder) service;
            mBTService = binder.getService();
            mBtServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBtServiceBound = false;
        }
    };

    private String[] DecodeByteMessage(byte[] message) {
        String readMsg = null;
        try {
            readMsg = new String(message, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "can't decode message", e);
        }
        Log.v(TAG, "received message:" + readMsg);
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
        unbindService(mBTServiceConnection);
    }
}
