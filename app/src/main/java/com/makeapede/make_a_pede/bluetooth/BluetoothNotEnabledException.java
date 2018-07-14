package com.makeapede.make_a_pede.bluetooth;

public class BluetoothNotEnabledException extends Exception {
	public BluetoothNotEnabledException() {
		super("Bluetooth not enabled");
	}
}
