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
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.widget.Toast;

import com.makeapede.make_a_pede.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothScanner extends ScanCallback {
	private static final UUID HC08_SERVICE_UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB");

	private BluetoothScanEventListener scanEventListener;
	private Context context;
	protected boolean scanning = false;

	protected BluetoothAdapter btAdapter;
	private BluetoothLeScanner leScanner;

	public BluetoothScanner(Context context) throws BluetoothNotSupportedException {
		this.context = context;

		final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
		btAdapter = bluetoothManager.getAdapter();
		leScanner = btAdapter.getBluetoothLeScanner();

		if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(context, context.getText(R.string.ble_not_supported), Toast.LENGTH_SHORT).show();
		}

		if (btAdapter == null || leScanner == null) {
			throw new BluetoothNotSupportedException();
		}
	}

	public void startScan(BluetoothScanEventListener scanEventListener, long period) {
		if (!isScanning()) {
			scanning = true;

			this.scanEventListener = scanEventListener;

			ScanFilter scanFilter = new ScanFilter.Builder()
					.setServiceUuid(new ParcelUuid(HC08_SERVICE_UUID))
					.build();

			List<ScanFilter> filters = new ArrayList<>();
			filters.add(scanFilter);

			ScanSettings settings = new ScanSettings.Builder()
					.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
					.build();

			leScanner.startScan(filters, settings, this);

			new Handler().postDelayed(this::stopScan, period);
		}
	}

	public void stopScan() {
		if (isScanning()) {
			scanning = false;

			scanEventListener.onScanComplete();

			leScanner.stopScan(this);
		}
	}

	@Override
	public void onScanResult(int callbackType, ScanResult result) {
		scanEventListener.onDeviceFound(result.getDevice());
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

	public interface BluetoothScanEventListener {
		void onDeviceFound(BluetoothDevice device);
		void onScanComplete();
	}
}
