/*
 * BluetoothActionConstants.java
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

public interface BluetoothActionConstants {
	String ACTION_CONNECTED = "com.makeapede.bluetooth.ACTION_CONNECTED";
	String ACTION_DISCONNECTED = "com.makeapede.bluetooth.ACTION_DISCONNECTED";
	String ACTION_SERVICES_DISCOVERED = "com.makeapede.bluetooth.ACTION_SERVICES_DISCOVERED";
	String ACTION_DATA_AVAILABLE = "com.makeapede.bluetooth.ACTION_DATA_AVAILABLE";
	String ACTION_ERROR = "com.makeapede.bluetooth.ACTION_ERROR";
	String EXTRA_DATA = "com.makeapede.bluetooth.EXTRA_DATA";
	String EXTRA_UUID = "com.makeapede.bluetooth.EXTRA_UUID";
}
