/**
 *   (Changes from original are) Copyright 2010 Guenther Hoelzl, Shawn Brown
 *
 *   This file is part of MINDdroid.
 *
 *   MINDdroid is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   MINDdroid is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with MINDdroid.  If not, see <http://www.gnu.org/licenses/>.
 *   
 * 
 * (original work is) Copyright (C) 2009 The Android Open Source Project
**/

package svb.nxt.robot.activity;

import java.util.Set;

import svb.nxt.robot.R;
import svb.nxt.robot.bt.BTCommunicator;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class DeviceListActivity extends Activity {
	
    public static final String PAIRING = "pairing";

    // Return Intent extra
    public static String DEVICE_NAME_AND_ADDRESS = "device_infos";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Member fields
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    
    //layout
    FrameLayout scanButton;
    ListView pairedListView;
    ListView newDevicesListView;
    TextView pairedDevicesTitle, newDevicesTitle ;
    RelativeLayout loadingRL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);        
        setupLayout();
 
        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize the button to perform device discovery        
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();                
            }
        });

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Find and set up the ListView for paired devices        
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        boolean legoDevicesFound = false;
        
        if (pairedDevices.size() > 0) {
        	pairedDevicesTitle.setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                // only add LEGO devices
                if (device.getAddress().startsWith(BTCommunicator.OUI_LEGO)) {
                    legoDevicesFound = true;
                    mPairedDevicesArrayAdapter.add(device.getName() + "-" + device.getAddress());
                }
            }
        }
        
        if (legoDevicesFound == false) {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }
    
    private void setupLayout(){
    	setContentView(R.layout.device_list);
    	
    	pairedListView = (ListView) findViewById(R.id.paired_devices);
        newDevicesListView = (ListView) findViewById(R.id.new_devices);
        scanButton = (FrameLayout) findViewById(R.id.button_scan);
        pairedDevicesTitle = (TextView)findViewById(R.id.title_paired_devices);
        newDevicesTitle = (TextView) findViewById(R.id.title_new_devices);
        loadingRL = (RelativeLayout) findViewById(R.id.rl);
    }

    private void showLoading(boolean isLoadingEnable){
    	int visibilitet = (isLoadingEnable)? View.GONE : View.VISIBLE;
    	int opaque = (!isLoadingEnable)? View.GONE : View.VISIBLE;
    	    	
    	pairedListView.setVisibility(visibilitet);
        newDevicesListView.setVisibility(visibilitet);
        scanButton.setVisibility(visibilitet);
        pairedDevicesTitle.setVisibility(visibilitet);
        newDevicesTitle.setVisibility(visibilitet);
        
        loadingRL.setVisibility(opaque);
    	
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter 
     */
    private void doDiscovery() {

        // show scanning dialog , hide views         
        showLoading(true);        

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            String info = ((TextView) v).getText().toString();
            // did we choose a correct name and address?
            if (info.lastIndexOf('-') != info.length()-18) 
                return;

            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();
            // Get the device MAC address, this is the text after the last '-' character
            String address = info.substring(info.lastIndexOf('-')+1);
            // Create the result Intent and include the infos
            Intent intent = new Intent();
            Bundle data = new Bundle();
            data.putString(DEVICE_NAME_AND_ADDRESS, info);
            data.putString(EXTRA_DEVICE_ADDRESS, address);
            data.putBoolean(PAIRING,av.getId()==R.id.new_devices);
            intent.putExtras(data);
            // Set result and finish this Activity
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "-" + device.getAddress());
                }
            // When discovery is finished, show views and hide loading
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {                
                showLoading(false);                
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

}
