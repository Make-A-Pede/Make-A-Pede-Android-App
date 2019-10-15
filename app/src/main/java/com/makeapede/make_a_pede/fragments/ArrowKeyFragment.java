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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.makeapede.make_a_pede.R;
import com.makeapede.make_a_pede.ui.ArrowView;
import com.makeapede.make_a_pede.utils.MotorValues;
import com.makeapede.make_a_pede.utils.PolarCoordinates;
import com.makeapede.make_a_pede.utils.Timer;

import static java.lang.Math.max;

public class ArrowKeyFragment extends ControllerFragment {
	private Timer btTimer = new Timer();

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.arrows_fragment_layout, container, false);

		ArrowView joystick = layout.findViewById(R.id.joystick);
		joystick.setJoystickTouchListener(this::processJoystickTouchEvent);

		return layout;
	}

	private void processJoystickTouchEvent(MotionEvent event, PolarCoordinates coords) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				if (btTimer.elapsedTime() > getBtSendInterval()) {
					int left = 0;
					int right = 0;

					if (coords.radius > 30) {
						if (coords.angle >= 0 && coords.angle < 60) {
							left = 255;
							right = 80;
						} else if (coords.angle >= 60 && coords.angle < 120) {
							left = 255;
							right = 255;
						} else if (coords.angle >= 120 && coords.angle < 180) {
							left = 80;
							right = 255;
						} else if (coords.angle >= 180 && coords.angle < 240) {
							left = -80;
							right = -255;
						} else if (coords.angle >= 240 && coords.angle < 300) {
							left = -255;
							right = -255;
						} else {
							left = -255;
							right = -80;
						}
					}

					sendMessage(new MotorValues(left, right));

					btTimer.reset();
				}

				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				sendMessage(new MotorValues(0, 0));

				break;
		}
	}
}
