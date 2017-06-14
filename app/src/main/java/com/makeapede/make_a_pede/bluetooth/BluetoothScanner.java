/*
 * BluetoothScanner.java
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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.widget.Toast;

import com.makeapede.make_a_pede.R;

public class BluetoothScanner {
	private BluetoothScanEventListener scanEventListener;
	private Context context;
	protected boolean scanning = false;

	protected BluetoothAdapter btAdapter;

	public BluetoothScanner(Context context) throws BluetoothNotSupportedException {
		this.context = context;

		final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
		btAdapter = bluetoothManager.getAdapter();

		if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(context, context.getText(R.string.ble_not_supported), Toast.LENGTH_SHORT).show();
		}

		if (btAdapter == null) {
			throw new BluetoothNotSupportedException();
		}
	}

	public void startScan(BluetoothScanEventListener scanEventListener, long period) {
		if (!isScanning()) {
			scanning = true;

			this.scanEventListener = scanEventListener;

			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			getContext().registerReceiver(deviceScanBroadcastReceiver, filter);

			btAdapter.startDiscovery();

			new Handler().postDelayed(this::stopScan, period);
		}
	}

	public void stopScan() {
		if (isScanning()) {
			scanning = false;

			getContext().unregisterReceiver(deviceScanBroadcastReceiver);

			scanEventListener.onScanComplete();
		}
	}

	public Context getContext() {
		return context;
	}

	public BluetoothAdapter getBtAdapter() {
		return btAdapter;
	}

	public boolean isScanning() {
		return scanning;
	}

	private final BroadcastReceiver deviceScanBroadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				scanEventListener.onDeviceFound(device);
			}
		}
	};

	public interface BluetoothScanEventListener {
		void onDeviceFound(BluetoothDevice device);
		void onScanComplete();
	}
}
