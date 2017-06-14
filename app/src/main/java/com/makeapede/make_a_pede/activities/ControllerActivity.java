/*
 * ControllerActivity.java
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
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.makeapede.make_a_pede.R;
import com.makeapede.make_a_pede.bluetooth.BluetoothActionConstants;
import com.makeapede.make_a_pede.bluetooth.BluetoothConnection;
import com.makeapede.make_a_pede.bluetooth.BluetoothDemoConnection;
import com.makeapede.make_a_pede.bluetooth.BluetoothLeConnection;
import com.makeapede.make_a_pede.bluetooth.BluetoothSppConnection;
import com.makeapede.make_a_pede.fragments.ArrowKeyFragment;
import com.makeapede.make_a_pede.fragments.ControllerFragment;
import com.makeapede.make_a_pede.fragments.JoystickFragment;

public class ControllerActivity extends AppCompatActivity implements BluetoothConnection.BluetoothConnectionEventListener,
																	 BluetoothActionConstants,
																	 LifecycleRegistryOwner {
	private static final String TAG = ControllerActivity.class.getSimpleName();

	private static final String EXTRA_CURRENT_FRAGMENT = "current-fragment";

	private static final int FRAGMENT_JOYSTICK = 0;
	private static final int FRAGMENT_ARROWS = 1;

	private LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

	private int currentFragment = FRAGMENT_JOYSTICK;

	private ProgressDialog progress;

	private BluetoothConnection bluetoothConnection;

	private FragmentManager fragmentManager;
	private MenuItem joystickMenuItem;
	private MenuItem arrowsMenuItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ActionBar bar = getSupportActionBar();
		if(bar != null) {
			bar.setDisplayHomeAsUpEnabled(true);
		}

		final Intent intent = getIntent();

		if(intent.hasExtra(HomeActivity.EXTRA_DEMO)) {
			setTitle("Demo");

			bluetoothConnection = new BluetoothDemoConnection(this, null, this);
		} else {
			String deviceAddress = intent
					.getStringExtra(DeviceListActivity.EXTRAS_DEVICE_ADDRESS);

			// Set activity title to the name of the connected device
			setTitle(intent.getStringExtra(DeviceListActivity.EXTRAS_DEVICE_NAME));

			int deviceType = intent.getIntExtra(DeviceListActivity.EXTRAS_DEVICE_TYPE,
												BluetoothDevice.DEVICE_TYPE_UNKNOWN);

			if (deviceType == BluetoothDevice.DEVICE_TYPE_LE) {
				bluetoothConnection = new BluetoothLeConnection(this, deviceAddress, this);
			} else if (deviceType == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
				bluetoothConnection = new BluetoothSppConnection(this, deviceAddress, this);
			} else {
				finish();
			}
		}

		getLifecycle().addObserver(bluetoothConnection);

		fragmentManager = getSupportFragmentManager();

		// Display the joystick fragment
		setCurrentFragment(FRAGMENT_JOYSTICK);

		progress = new ProgressDialog(this);
		progress.setTitle("Connecting...");
		progress.setIndeterminate(true);
		progress.show();
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
		Log.i(TAG, "Left: " + leftState + ", Right: " + rightState);

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

		bluetoothConnection.sendMessage(message);
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

		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		ControllerFragment controllerFragment;

		if(currentFragment == FRAGMENT_JOYSTICK) {
			controllerFragment = new JoystickFragment();
		} else {
			controllerFragment = new ArrowKeyFragment();
		}

		controllerFragment.setMessageListener(this::setDrive);

		fragmentTransaction.replace(R.id.fragment_container, controllerFragment);
		fragmentTransaction.commit();

		showMenuItems();
	}

	@Override
	public void onBluetoothConnectionEvent(String event) {
		switch (event) {
			case ACTION_CONNECTED:
				progress.hide();
				break;

			case ACTION_DISCONNECTED:
			case ACTION_ERROR:
				finish();
				break;
		}
	}

	@Override
	public LifecycleRegistry getLifecycle() {
		return lifecycleRegistry;
	}
}
