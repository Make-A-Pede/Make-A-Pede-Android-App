/*
 * BluetoothConnection.java
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

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import android.content.Context;

public abstract class BluetoothConnection implements BluetoothActionConstants, LifecycleObserver {
	protected Context context;
	protected String address;
	protected BluetoothConnectionEventListener connectionEventListener;

	public BluetoothConnection(Context context, String address, BluetoothConnectionEventListener listener) {
		this.context = context;
		this.address = address;
		connectionEventListener = listener;
	}

	@OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
	public abstract void connect();
	@OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
	public abstract void disconnect();
	@OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
	public abstract void destroy();

	public abstract void sendMessage(String message);
	public abstract void subscribeToHeadingNotifications(OnHeadingReadListener listener);

	public interface BluetoothConnectionEventListener {
		void onBluetoothConnectionEvent(String event);
	}

	public interface OnHeadingReadListener {
		void headingRead(String heading);
	}
}
