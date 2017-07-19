/*
 * BluetoothLeService.java
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

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BluetoothLeService extends Service implements BluetoothActionConstants {
	private final static String TAG = BluetoothLeService.class.getSimpleName();

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	private BluetoothManager bluetoothManager;
	private BluetoothAdapter bluetoothAdapter;
	private String bluetoothDeviceAddress;
	private BluetoothGatt bluetoothGatt;
	private int connectionState = STATE_DISCONNECTED;

	private ArrayList<BleWriteOperation> writeOpQueue = new ArrayList<>();
	private boolean writing = false;

	private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			String intentAction;
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				intentAction = ACTION_CONNECTED;
				connectionState = STATE_CONNECTED;
				broadcastUpdate(intentAction);
				Log.i(TAG, "Connected to GATT server.");
				Log.i(TAG, "Attempting to start service discovery:" + bluetoothGatt.discoverServices());

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				intentAction = ACTION_DISCONNECTED;
				connectionState = STATE_DISCONNECTED;
				Log.i(TAG, "Disconnected from GATT server.");
				broadcastUpdate(intentAction);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_SERVICES_DISCOVERED);
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if(status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			} else {
				Log.w(TAG, "onCharacteristicWrite received: " + status);
			}

			writeNextCharacteristic();
		}
	};

	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
		final Intent intent = new Intent(action);

		final byte[] data = characteristic.getValue();
		if (data != null && data.length > 0) {
			intent.putExtra(EXTRA_DATA, new String(data));
		}

		intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());

		sendBroadcast(intent);
	}

	public class LocalBinder extends Binder {
		public BluetoothLeService getService() {
			return BluetoothLeService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		close();
		return super.onUnbind(intent);
	}

	private final IBinder mBinder = new LocalBinder();

	public boolean initialize() {
		if (bluetoothManager == null) {
			bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (bluetoothManager == null) {
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		bluetoothAdapter = bluetoothManager.getAdapter();
		if (bluetoothAdapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;
	}

	public boolean connect(final String address) {
		if (bluetoothAdapter == null || address == null) {
			Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		if (bluetoothDeviceAddress != null && address.equals(bluetoothDeviceAddress)
				&& bluetoothGatt != null) {
			Log.d(TAG, "Trying to use an existing bluetoothGatt for connection.");
			if (bluetoothGatt.connect()) {
				connectionState = STATE_CONNECTING;
				return true;
			} else {
				return false;
			}
		}

		final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
		if (device == null) {
			Log.w(TAG, "Device not found.  Unable to connect.");
			return false;
		}

		bluetoothGatt = device.connectGatt(this, false, gattCallback);
		Log.d(TAG, "Trying to create a new connection.");
		bluetoothDeviceAddress = address;
		connectionState = STATE_CONNECTING;

		return true;
	}

	public void disconnect() {
		if (bluetoothAdapter == null || bluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}

		bluetoothGatt.disconnect();
	}

	public void close() {
		if (bluetoothGatt == null) {
			return;
		}

		bluetoothGatt.close();
		bluetoothGatt = null;
	}

	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (bluetoothAdapter == null || bluetoothGatt == null || characteristic == null) {
			return;
		}
		bluetoothGatt.readCharacteristic(characteristic);
	}

	public void writeCharacteristic(BluetoothGattCharacteristic characteristic, String value) {
		writeCharacteristic(characteristic, value.getBytes());
	}

	public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
		BleWriteOperation op = new BleWriteOperation(characteristic, value);
		writeOpQueue.add(op);

		if(!writing) {
			writeNextCharacteristic();
		}
	}

	private boolean writeNextCharacteristic() {
		if(writeOpQueue.isEmpty()) {
			writing = false;
			return false;
		}

		writing = true;

		BleWriteOperation op = writeOpQueue.get(0);
		writeOpQueue.remove(0);

		op.characteristic.setValue(op.value);

		return bluetoothGatt.writeCharacteristic(op.characteristic);
	}

	public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (bluetoothAdapter == null || bluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}

		bluetoothGatt.setCharacteristicNotification(characteristic, enabled);

		for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
			descriptor.setValue(
					enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[] {0x00, 0x00});
			bluetoothGatt.writeDescriptor(descriptor);
		}
	}

	public List<BluetoothGattService> getSupportedGattServices() {
		if (bluetoothGatt == null) return null;

		return bluetoothGatt.getServices();
	}

	private class BleWriteOperation {
		BluetoothGattCharacteristic characteristic;
		byte[] value;

		BleWriteOperation(BluetoothGattCharacteristic characteristic, byte[] value) {
			this.characteristic = characteristic;
			this.value = value;
		}
	}
}
