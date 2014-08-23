package com.mahfuz.basicbluetooth;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
/*
 * @Author Mahfuz @ mahfuj.islam28@yahoo.com
 */
/*
 * @Author Mahfuz @ mahfuj.islam28@yahoo.com
 */
public class MainActivity extends ActionBarActivity {

	
    private ProgressDialog mProgressDlg;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();

    // private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public ConnectThread mConnectThread  = null ;
    public ConnectedThread mConnectedThread = null ;
    
    
    
    Button bt_disable , bt_scan , bt_connect , bt_send ;
    TextView tx_data_recvd ;
    EditText edtx_data_to_send ;
    
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		bt_disable 	= (Button) findViewById(R.id.bt_disable);
		bt_scan		= (Button) findViewById(R.id.bt_scan);
		bt_connect	= (Button) findViewById(R.id.bt_connect);
		bt_send		= (Button) findViewById(R.id.bt_send);
		
		
		bt_disable.setEnabled(false);
		bt_scan.setEnabled(false);
		bt_connect.setEnabled(false);
		bt_send.setEnabled(false);
		
		
		edtx_data_to_send	= (EditText) findViewById(R.id.edtx_data);
		tx_data_recvd		= (TextView) findViewById(R.id.tx_data);
		tx_data_recvd.setText("");
		
		
        mProgressDlg 		= new ProgressDialog(this);
        mProgressDlg.setCancelable(false);
        mBluetoothAdapter	= BluetoothAdapter.getDefaultAdapter();
        
		checkBluetoothConnection();
		
		
		/* button events */
		
		bt_disable.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
               // Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_);
               // startActivityForResult(intent, 1000);
                if(mBluetoothAdapter.isEnabled()){
                	mBluetoothAdapter.disable();
                    /* turn off the button events */
    				bt_disable.setEnabled(false);
    				bt_scan.setEnabled(false);
    				bt_connect.setEnabled(false);
    				bt_send.setEnabled(false);
                }
			}
		});
		bt_scan.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mProgressDlg.setTitle("Scanning");
                mProgressDlg.setMessage("Please wait...");
                /* start the discovery */
                mBluetoothAdapter.startDiscovery();
				
			}
		});
		bt_connect.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showPairedDevices();				
			}
		});
		bt_send.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mConnectedThread!=null){
					String data = edtx_data_to_send.getText().toString().trim() ;
					mConnectedThread.write(data);
					edtx_data_to_send.setText("");
				}
			}
		});
		
	}

	public void setDataReveddText(final String data){
		MainActivity.this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				tx_data_recvd.append(data + "\n");
			}
		});
	}
	public void enableSendDataButton(final boolean b){
		MainActivity.this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				bt_send.setEnabled(b);
			}
		});
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	void showToast(final String str){
		MainActivity.this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
			}
		});
		
	}
	
	@Override
	protected void onDestroy() {
        unregisterReceiver(mReceiver);
        if(mConnectedThread!=null){ mConnectedThread.isConnectedThreaRunning = false ; mConnectedThread.cancel(); mConnectedThread = null ; }
        if(mConnectThread!=null){ mConnectThread.cancel(); mConnectThread= null;}
        Log.d("MainActivity onDestroy","Called");
		super.onDestroy();
	}

	@Override
	protected void onPause() {
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }
        Log.d("MainActivity onPause","Called");
		super.onPause();
	}
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

                // Setting Dialog Title
                alertDialog.setTitle("Exit");

                // Setting Dialog Message
                alertDialog.setMessage("Do you want to exit?");

                // Setting Icon to Dialog
                //alertDialog.setIcon(R.drawable.delete);

                // Setting Positive "Yes" Button
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        // Write your code here to invoke YES event
                        finish();
                    }
                });

                // Setting Negative "NO" Button
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event
                        dialog.cancel();
                    }
                });

                // Showing Alert Message
                alertDialog.show();
                //return true ;
        }
        return super.onKeyDown(keyCode, event);
    }
	void checkBluetoothConnection(){
        if(mBluetoothAdapter==null){
            showToast("Bluetooth is not supported by this device.");
        }
        else{
            if (mBluetoothAdapter.isEnabled()) {
                showToast("Bluetooth is on.");
                bt_disable.setEnabled(true);
                bt_scan.setEnabled(true);
                bt_connect.setEnabled(true);

            } else {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, 1000);
            }
        }

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
    }
    void showPairedDevices(){
        final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices == null || pairedDevices.size() == 0) {
            showToast("No Paired Devices Found");
        } else {

            List<String> listItems = new ArrayList<String>();

            for(BluetoothDevice device : pairedDevices)
            {
                listItems.add(device.getName());
                Log.d(" getPairedDevices()", device.getName());
            }

            final CharSequence[] btDeviceList = listItems.toArray(new CharSequence[listItems.size()]);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Connect to a Bluetooth Device.");
            builder.setItems(btDeviceList,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    showToast("Selected device : " + btDeviceList[i]);
                    for(BluetoothDevice device: pairedDevices){
                        String d = btDeviceList[i].toString().trim();
                        if(device.getName().contains(d)){
                            connect(device);
                            break;
                        }
                    }
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    showToast("Enabled");
                    
                    bt_disable.setEnabled(true);
                    bt_scan.setEnabled(true);
                    bt_connect.setEnabled(true);
                    
                    //showEnabled();
                }
                else {
                    showToast("Bluetooth is Off.");
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<BluetoothDevice>();
                mProgressDlg.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mProgressDlg.dismiss();


                Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);

                newIntent.putParcelableArrayListExtra("device.list", mDeviceList);

                startActivity(newIntent);


            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                mDeviceList.add(device);

                showToast("Found device " + device.getName());
            }
        }
    };
    public synchronized void connect(BluetoothDevice device){
        Log.d("connect function", "connect to device :" + device);

        if(mConnectThread!=null){
            mConnectThread.cancel();
            mConnectThread = null ;
        }

        if(mConnectedThread!=null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }
    public void connected(BluetoothDevice mmDevice, BluetoothSocket mmSocket) {
        // TODO Auto-generated method stub
        Log.d("Func: Connected", "Device Connected!");

		/* now close all the connection and start the data communication thread */
        
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.isConnectedThreaRunning = true ;
        mConnectedThread.start();

    }
    /* thread for making connection */
    public class ConnectThread extends Thread{

        private final BluetoothDevice mmDevice ;
        private final BluetoothSocket mmSocket ;

        public ConnectThread(BluetoothDevice device){
            this.mmDevice = device ;
            BluetoothSocket tmp = null ;
            try{

                //tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID2);
                Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                tmp = (BluetoothSocket) m.invoke(device, 1);

            }catch(Exception e){
                Log.e("ConnectThread", "create() failed", e);
            }
            mmSocket = tmp ;
        }
        @Override
        public void run() {
            // TODO Auto-generated method stub
            //mBluetoothAdapter.cancelDiscovery();
            Log.i("ConnectThread", "BEGIN mConnectThread");
            try{
                mmSocket.connect();
                enableSendDataButton(true);
            }catch(Exception e){
                Log.e("ConnectThread", "connection() failed", e);
                showToast("Connection Failed."); 
                try{
                	mmSocket.close();
                }catch(Exception ee){
                    Log.e("ConnectThread", "closed() failed", e);
                }

                return ;
            }
            connected(mmDevice,mmSocket);
        }
        public void cancel() {
            try {
               mmSocket.close();
            } catch (Exception e) {
                Log.e("ConnectThread", "close() of connect socket failed", e);
            }
        }

    }
    /* thread for maintaining connection */

    /*
     * This thread is called after a successful connection to bluetooth device.
     */
    public class ConnectedThread extends Thread {

        public  boolean isConnectedThreaRunning ;
        private final  BluetoothSocket mmSocket ;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            Log.d("ConnectedThread", "create ConnectedThread");
            mmSocket = socket ;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try{
                tmpIn 	= socket.getInputStream();
                tmpOut 	= socket.getOutputStream();

                showToast("Connected!");
                
                
                
            }catch(Exception e){
                Log.d("ConnectedThread", "Failed to init input output stream!");

            }
            mmInStream 	= tmpIn ;
            mmOutStream = tmpOut;
        }



        @Override
        public void run() {
            Log.i("ConnectedThread", "BEGIN mConnectedThread");
            int bytes;
            while (isConnectedThreaRunning) {
                try {
                    // Read from the InputStream
                    if(mmInStream.available()>0){
                        Thread.sleep(40);
                        byte[] buffer = new byte[1024];

                        bytes = mmInStream.read(buffer);
                        if(bytes>0){
                            Log.d("SIZE",""+bytes);
                            String data = new String(buffer);
                            Log.d("ConnectedThread:Message Received:","Size:" + bytes +  " \\ Data:" + data.trim());
                            setDataReveddText(data.trim());

                        }

                    }
                } catch (Exception e) {
                    Log.e("ConnectedThread", "disconnected", e);
                    cancel();
                    break;
                }
            }

        }
        public void write(String string) {
            try{
                mmOutStream.write(string.getBytes());
            }catch(Exception e){
                Log.e("ConnectedThread", "write failed", e);
            }

        }
        public void cancel() {
            // TODO Auto-generated method stub
            try{
                mmSocket.close();
                isConnectedThreaRunning = false ;
            }catch(Exception e){
                Log.e("Fuction:ConnedtedThread","Cancel",e);
            }
        }
    }


}
