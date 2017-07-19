/*
 * ControllerFragment.java
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

package com.makeapede.make_a_pede.fragments;

import android.support.v4.app.Fragment;

import com.makeapede.make_a_pede.utils.Timer;

/**
 * A fragment that provides a UI that can be used to control the Make-A-Pede.
 *
 * Subclasses should, in addition to providing a view, call {@code sendMessage} with the proper
 * left and right values based on user input to tell the hosting activity to send the values over
 * Bluetooth to the Make-A-Pede. Implementations should wait at least {@code btSendInterval}
 * milliseconds between sending messages.
 */
public abstract class ControllerFragment extends Fragment {
	protected OnShouldSendMessageListener messageListener;
	private int btSendInterval = 100;

	public abstract void setSpeedPercent(int percent);
	public abstract int getSpeedPercent();

	public void sendMessage(int left, int right) {
		if (messageListener != null) {
			messageListener.sendMessage(left, right);
		}
	}

	public void setMessageListener(OnShouldSendMessageListener messageListener) {
		this.messageListener = messageListener;
	}

	public int getBtSendInterval() {
		return btSendInterval;
	}

	public void setBtSendInterval(int btSendInterval) {
		this.btSendInterval = btSendInterval;
	}

	public interface OnShouldSendMessageListener {
		void sendMessage(int leftValue, int rightValue);
	}
}
