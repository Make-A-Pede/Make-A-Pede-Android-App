/*
 * MainActivity.java
 * Copyright (C) 2017 Automata Development
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.makeapede.make_a_pede.activities;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.makeapede.make_a_pede.R;
import com.makeapede.make_a_pede.bluetooth.BluetoothLeService;
import com.makeapede.make_a_pede.fragments.ArrowKeyFragment;
import com.makeapede.make_a_pede.fragments.ControllerFragment;
import com.makeapede.make_a_pede.fragments.JoystickFragment;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = MainActivity.class.getSimpleName();

	private static final String EXTRA_CURRENT_FRAGMENT = "current-fragment";

	private static final int FRAGMENT_JOYSTICK = 0;
	private static final int FRAGMENT_ARROWS = 1;

	private static final UUID DRIVE_UUID = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1214");

	private int currentFragment = FRAGMENT_JOYSTICK;
	private ControllerFragment controllerFragment;

	private ProgressDialog progress;

	private String mDeviceAddress;

	private BluetoothLeService bluetoothLeService;
	private BluetoothGattCharacteristic driveCharacteristic;
	private FragmentManager fragmentManager;
	private MenuItem joystickMenuItem;
	private MenuItem arrowsMenuItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ActionBar bar;
		if((bar = getSupportActionBar()) != null) {
			bar.setDisplayHomeAsUpEnabled(true);
		}

		final Intent intent = getIntent();
		mDeviceAddress = intent.getStringExtra(DeviceListActivity.EXTRAS_DEVICE_ADDRESS);

		// Set activity title to the name of the connected device
		setTitle(intent.getStringExtra(DeviceListActivity.EXTRAS_DEVICE_NAME));

		// Bind the BLE service
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);

		fragmentManager = getSupportFragmentManager();

		// Display the joystick fragment
		setCurrentFragment(FRAGMENT_JOYSTICK);

		progress = new ProgressDialog(this);
		progress.setTitle("Connecting...");
		progress.setIndeterminate(true);
		progress.show();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Register broadcast receiver to get GATT updates
		registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());

		if (bluetoothLeService != null) {
			final boolean result = bluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result = " + result);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Unregister GATT updates broadcast receiver
		unregisterReceiver(gattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unbindService(serviceConnection);
		bluetoothLeService = null;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Save which fragment is currently being displayed
		outState.putInt(EXTRA_CURRENT_FRAGMENT, currentFragment);

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		// Switch to whichever fragment was being displayed before configuration change
		if(savedInstanceState != null) {
			if (savedInstanceState.getInt(EXTRA_CURRENT_FRAGMENT, FRAGMENT_JOYSTICK) == FRAGMENT_JOYSTICK) {
				setCurrentFragment(FRAGMENT_JOYSTICK);
			} else {
				setCurrentFragment(FRAGMENT_ARROWS);
			}
		}
	}

	/**
	 * Set the left and right drive speeds of the Make-A-Pede over Bluetooth
	 *
	 * @param leftState left speed
	 * @param rightState right speed
	 */
	private void setDrive(int leftState, int rightState) {
		Log.i("MainActivity", "Left: " + leftState + ", Right: " + rightState);

		leftState += 127;
		rightState += 127;

		leftState = Math.min(leftState, 255);
		rightState = Math.min(rightState, 255);

		leftState = Math.max(leftState, 0);
		rightState = Math.max(rightState, 0);

		String left = Integer.toString(leftState);
		while (left.length() < 3) {
			left = "0" + left;
		}

		String right = Integer.toString(rightState);
		while (right.length() < 3) {
			right = "0" + right;
		}

		String message = left + ":" + right + ":";

		bluetoothLeService.writeCharacteristic(driveCharacteristic, message);
	}

	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

			if (!bluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}

			bluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			bluetoothLeService = null;
		}
	};

	// {@code BroadcastReceiver} for receiving Bluetooth GATT updates
	private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				// Hide the progress dialog on Bluetooth connect
				progress.hide();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				// Finish activity on Bluetooth disconnect
				finish();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				// Initialize GATT characteristics on GATT services discovered
				getGattCharacteristics(bluetoothLeService.getSupportedGattServices());
			}
		}
	};

	/**
	 * Search for the desired GATT characteristics inside the provided list of GATT services.
	 *
	 * @param gattServices a {@code List<BluetoothGattService>} to search for the characteristics
	 *                     within
	 */
	private void getGattCharacteristics(List<BluetoothGattService> gattServices) {
		if (gattServices == null) return;

		for (BluetoothGattService gattService : gattServices) {
			if(gattService.getCharacteristic(DRIVE_UUID) != null) {
				driveCharacteristic = gattService.getCharacteristic(DRIVE_UUID);
			}
		}

		if (driveCharacteristic == null) {
			Toast.makeText(this, "Incompatible Device", Toast.LENGTH_LONG).show();
			finish();
		}
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();

		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);

		return intentFilter;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_activity_menu, menu);

		joystickMenuItem = menu.findItem(R.id.action_joystick);
		arrowsMenuItem = menu.findItem(R.id.action_arrows);

		showMenuItems();

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_joystick:
				setCurrentFragment(FRAGMENT_JOYSTICK);
				break;

			case R.id.action_arrows:
				setCurrentFragment(FRAGMENT_ARROWS);
				break;

			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				break;
		}

		return true;
	}

	/**
	 * Shows/hides the proper action bar items based on the selected controller fragment
	 */
	private void showMenuItems() {
		if (joystickMenuItem != null && arrowsMenuItem != null) {
			if(currentFragment == FRAGMENT_JOYSTICK) {
				joystickMenuItem.setVisible(false);
				arrowsMenuItem.setVisible(true);
			} else {
				joystickMenuItem.setVisible(true);
				arrowsMenuItem.setVisible(false);
			}

			invalidateOptionsMenu();
		}
	}

	/**
	 * Shows the specified fragment
	 *
	 * @param fragment the fragment to display, either {@code FRAGMENT_JOYSTICK}
	 *                 or {@code FRAGMENT_ARROWS}
	 */
	private void setCurrentFragment(int fragment) {
		currentFragment = fragment;

		int speed = 100;
		if(controllerFragment != null) {
			speed = controllerFragment.getSpeedPercent();
		}

		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		if(currentFragment == FRAGMENT_JOYSTICK) {
			controllerFragment = new JoystickFragment();
		} else {
			controllerFragment = new ArrowKeyFragment();
		}

		controllerFragment.setMessageListener(this::setDrive);
		//controllerFragment.setSpeedPercent(speed);

		fragmentTransaction.replace(R.id.fragment_container, controllerFragment);
		fragmentTransaction.commit();

		showMenuItems();
	}
}
