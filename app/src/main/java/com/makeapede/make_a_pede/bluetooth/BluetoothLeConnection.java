/*
 * BluetoothLeConnection.java
 * Copyright (C) 2017  Automata Development
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

package com.makeapede.make_a_pede.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

public class BluetoothLeConnection extends BluetoothConnection {
	private static final UUID DRIVE_UUID_CURIE = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1214");
	private static final UUID DRIVE_UUID_HC08 = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");

	private BluetoothLeService bluetoothLeService;
	private BluetoothGattCharacteristic driveCharacteristic;

	public BluetoothLeConnection(Context context, String address, BluetoothConnectionEventListener listener) {
		super(context, address, listener);

		Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
		context.bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void connect() {
		context.registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());

		if (bluetoothLeService != null) {
			bluetoothLeService.connect(address);
		}
	}

	@Override
	public void disconnect() {
		context.unregisterReceiver(gattUpdateReceiver);
	}

	@Override
	public void destroy() {
		context.unbindService(serviceConnection);
		bluetoothLeService = null;
	}

	@Override
	public void sendMessage(String message) {
		bluetoothLeService.writeCharacteristic(driveCharacteristic, message);
	}

	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

			if (!bluetoothLeService.initialize()) {
				connectionEventListener.onBluetoothConnectionEvent(BluetoothLeService.ACTION_ERROR);
			}

			bluetoothLeService.connect(address);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			bluetoothLeService = null;
		}
	};

	private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			connectionEventListener.onBluetoothConnectionEvent(action);

			if (ACTION_SERVICES_DISCOVERED.equals(action)) {
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
			if(gattService.getCharacteristic(DRIVE_UUID_CURIE) != null) {
				driveCharacteristic = gattService.getCharacteristic(DRIVE_UUID_CURIE);
			} else if(gattService.getCharacteristic(DRIVE_UUID_HC08) != null) {
				driveCharacteristic = gattService.getCharacteristic(DRIVE_UUID_HC08);
			}
		}

		if (driveCharacteristic == null) {
			Toast.makeText(context, "Incompatible Device", Toast.LENGTH_LONG).show();
			connectionEventListener.onBluetoothConnectionEvent(BluetoothLeService.ACTION_ERROR);
		}
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();

		intentFilter.addAction(ACTION_CONNECTED);
		intentFilter.addAction(ACTION_DISCONNECTED);
		intentFilter.addAction(ACTION_SERVICES_DISCOVERED);

		return intentFilter;
	}
}
