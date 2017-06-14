/*
 * BluetoothSppConnection.java
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
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.UUID;

public class BluetoothSppConnection extends BluetoothConnection {
	private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private BluetoothSocket btSocket;
	private boolean connected = false;

	public BluetoothSppConnection(Context context, String address, BluetoothConnectionEventListener listener) {
		super(context, address, listener);

		new ConnectBTTask().execute();
	}

	@Override
	public void connect() {}
	@Override
	public void disconnect() {}

	@Override
	public void destroy() {
		if (btSocket != null) {
			try {
				btSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void sendMessage(String message) {
		if (btSocket != null) {
			try {
				byte[] bytes = message.getBytes();
				btSocket.getOutputStream().write(bytes);
			} catch (IOException e) {
				connected = false;

				new ConnectBTTask().execute();
			}
		}
	}

	private class ConnectBTTask extends AsyncTask<Void, Void, Void> {
		private boolean connectSuccess;

		private ConnectBTTask() {
			connectSuccess = true;
		}

		protected Void doInBackground(Void... devices) {
			try {
				if (btSocket == null || !connected) {
					btSocket = BluetoothAdapter.getDefaultAdapter()
											   .getRemoteDevice(address)
											   .createInsecureRfcommSocketToServiceRecord(uuid);

					BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

					btSocket.connect();
				}
			} catch (IOException e) {
				connectSuccess = false;
				connectionEventListener.onBluetoothConnectionEvent(ACTION_ERROR);
			}
			return null;
		}

		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (connectSuccess) {
				connected = true;
			}

			connectionEventListener.onBluetoothConnectionEvent(ACTION_CONNECTED);
		}
	}
}
