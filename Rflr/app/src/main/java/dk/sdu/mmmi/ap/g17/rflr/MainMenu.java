package dk.sdu.mmmi.ap.g17.rflr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.logging.SocketHandler;

import static android.content.ContentValues.TAG;

public class MainMenu extends AppCompatActivity {


    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //BluetoothConnection
        System.out.println("dfs");
        if(mBluetoothAdapter == null){
            // Device does not support Bluetooth
        } if(!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if(pairedDevices.size() > 0){
            for (BluetoothDevice device : pairedDevices){
                String devicename = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReciver, filter);
        System.out.println("kfk");

    }

    private final BroadcastReceiver mReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK){
            //possibly call some method from here? ¯\_(ツ)_/¯
            Log.i("Result_OK" ,"You have bluetooth enabled");
        } if(resultCode == RESULT_CANCELED){
            Log.wtf("Result_CANCLED" ,"You DONT have bluetooth enabled");
        }
    }

    public void hostGame(View view){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
        // Start server stuff
        AcceptThread host = new AcceptThread();
        host.run();

    }

    public void connectToGame(View view){
        // Connect to Hosting device
        mBluetoothAdapter.startDiscovery(); 

        System.out.println(mBluetoothAdapter.isDiscovering());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReciver);
    }

    private void manageMyConnectedSocket(BluetoothSocket socket){

    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private final UUID myUUID = UUID.fromString("71dea7b0-26e8-4070-89b5-b68d4b91bda7");

        public AcceptThread(){
            BluetoothServerSocket tmp = null;
            try{
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Snyd", myUUID);
            } catch (IOException e){
                Log.e(TAG, "Socket's accept() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run(){
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try{
                    socket = mmServerSocket.accept();
                } catch (IOException e){
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if(socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    manageMyConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Could not close the connect socket", e);
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }


}
