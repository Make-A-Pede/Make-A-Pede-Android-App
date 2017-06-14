/*
 * BluetoothDemoConnection.java
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

import android.content.Context;

public class BluetoothDemoConnection extends BluetoothConnection {
	public BluetoothDemoConnection(Context context, String address, BluetoothConnectionEventListener listener) {
		super(context, address, listener);
	}

	@Override
	public void connect() {
		connectionEventListener.onBluetoothConnectionEvent(ACTION_CONNECTED);
	}

	@Override
	public void disconnect() {}

	@Override
	public void destroy() {}

	@Override
	public void sendMessage(String message) {}
}
