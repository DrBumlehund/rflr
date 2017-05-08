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
    private int LastGuessDieCount;
    private int LastGuessDieEyes;

    private BluetoothService mBTService;


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
        updateDiceImages();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "Whats up my dudes");
                if (mBtServiceBound) {
                    mBTService.setmHandler(mHandler);
                    mBTService.startConnected();
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


        if (mBtServiceBound) {
            // GUESS MESSAGE FOLLOWS FORMAT : DEVICE BLUETOOTH NAME ; MESSAGE TYPE ; AMOUNT OF DIES : DIE EYES
            String message = mBTService.getBluetoothName() + ";" + Constants.GUESS + ";" + numberOfDiceSpinner.getSelectedItem() + ":" + dieEyesSpinner.getSelectedItem();
            byte[] bytes = new byte[0];
            try {
                bytes = message.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Cant encode guess", e);
            }
            Log.v(TAG, message + " ByteLength: " + bytes.length);
            if (bytes != null) {
                mBTService.write(bytes);
            }
        }
    }

    private void sendCup() {

        if (mBtServiceBound) {
            // CUP MESSAGE FOLLOWS FORMAT : DEVICE BLUETOOTH NAME ; MESSAGE TYPE ; 1:#1's, 2:#2's, ...
            String message = mBTService.getBluetoothName() + ";" + Constants.CUP + ";" + cup.toString();
            try {
                mBTService.write(message.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "can't encode cup", e);
            }
            hasSentCup = true;
        }
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
                            String[] guess = readMessage[2].split(":");
                            LastGuessDieCount = Integer.parseInt(guess[0]);
                            LastGuessDieEyes = Integer.parseInt(guess[1]);
                            break;
                        case Constants.CUP:
                            // I received a cup...
                            if (hasSentCup) {
                                // ...and I lifted
                                hasSentCup = false;
                            } else {
                                // ...and the other player lifted
                                // Send my cup over, so he can calculate score as well.
                                sendCup();
                            }
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    if (!hasSentCup) {
                        TextView tv = (TextView) findViewById(R.id.last_call_value_label);
                        tv.setText("You : " + numberOfDiceSpinner.getSelectedItem() + ":" + dieEyesSpinner.getSelectedItem());
                        LastGuessDieCount = Integer.parseInt(numberOfDiceSpinner.getSelectedItem().toString());
                        LastGuessDieEyes = Integer.parseInt(dieEyesSpinner.getSelectedItem().toString());
                    } else {
                    }

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
        HashMap<Integer, Integer> otherCup = new HashMap<>();
        String[] cupContents = cupString.split(",");
        for (String i : cupContents) {
            String[] j = i.split(":");
            otherCup.put(Integer.parseInt(j[0]), Integer.parseInt(j[1]));
        }

        return false;
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
