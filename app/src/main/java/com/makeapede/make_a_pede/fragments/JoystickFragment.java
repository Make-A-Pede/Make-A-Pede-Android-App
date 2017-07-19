/*
 * JoystickFragment.java
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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.makeapede.make_a_pede.R;
import com.makeapede.make_a_pede.ui.JoystickView;
import com.makeapede.make_a_pede.utils.Timer;

public class JoystickFragment extends ControllerFragment {
	private Timer btTimer = new Timer();

	private SeekBar powerSlider;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.joystick_fragment_layout, container, false);

		JoystickView joystick = layout.findViewById(R.id.joystick);
		joystick.setJoystickTouchListener(this::processJoystickTouchEvent);

		powerSlider = layout.findViewById(R.id.power_slider);

		return layout;
	}

	private void processJoystickTouchEvent(MotionEvent event, int x, int y, float width, float height) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				int rightState;
				int leftState;

				if (btTimer.elapsedTime() > getBtSendInterval()) {
					int forwardValue = (int) (((height - y) / height) * 255.0f) - 127;
					int turnValue = (int) (((x - (width / 2.0d)) / (width / 2.0d)) * 255.0d);

					leftState = (int) ((forwardValue + turnValue) * ((powerSlider.getProgress()+40) / 100.0));
					rightState = (int) ((forwardValue - turnValue) * ((powerSlider.getProgress()+40) / 100.0));

					if (Integer.signum(forwardValue) < 0) {
						int temp = rightState;
						rightState = leftState;
						leftState = temp;
					}

					if (Integer.signum(leftState) != Integer.signum(rightState)) {
						if (Integer.signum(turnValue) > 0) {
							rightState = 0;
						} else {
							leftState = 0;
						}
					}

					if (Integer.signum(forwardValue) < 0) {
						leftState = -1 * Math.abs(leftState);
						rightState = -1 * Math.abs(rightState);
					}

					sendMessage(leftState, rightState);

					btTimer.reset();
				}

				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				leftState = 0;
				rightState = 0;

				sendMessage(leftState, rightState);

				break;
		}
	}

	@Override
	public void setSpeedPercent(int percent) {
		powerSlider.setProgress(Math.max(percent-40, 0));
	}

	@Override
	public int getSpeedPercent() {
		return powerSlider.getProgress()+40;
	}
}
