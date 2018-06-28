/*
 * ArrowKeyFragment.java
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
import com.makeapede.make_a_pede.utils.MotorValues;
import com.makeapede.make_a_pede.utils.PolarCoordinates;
import com.makeapede.make_a_pede.utils.Timer;

public class ArrowKeyFragment extends ControllerFragment {
	private Timer btTimer = new Timer();

	private View layout;

	private SeekBar powerSlider;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		layout = inflater.inflate(R.layout.arrows_fragment_layout, container, false);

		powerSlider = layout.findViewById(R.id.power_slider);

		setTouchListenerOnView(R.id.up_arrow, new OnArrowClickedListener(0, 100));
		setTouchListenerOnView(R.id.left_arrow, new OnArrowClickedListener(-100, 0));
		setTouchListenerOnView(R.id.right_arrow, new OnArrowClickedListener(100, 0));
		setTouchListenerOnView(R.id.down_arrow, new OnArrowClickedListener(0, -100));

		setTouchListenerOnView(R.id.up_left_arrow, new OnArrowClickedListener(-100, 100));
		setTouchListenerOnView(R.id.down_left_arrow, new OnArrowClickedListener(-100, -100));
		setTouchListenerOnView(R.id.up_right_arrow, new OnArrowClickedListener(100, 100));
		setTouchListenerOnView(R.id.down_right_arrow, new OnArrowClickedListener(100, -100));

		return layout;
	}

	private void setTouchListenerOnView(int id, View.OnTouchListener listener) {
		layout.findViewById(id).setOnTouchListener(listener);
	}

	private class OnArrowClickedListener implements View.OnTouchListener {
		private final int x;
		private final int y;

		OnArrowClickedListener(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if(btTimer.elapsedTime() > getBtSendInterval()) {
						double powerPercent = (powerSlider.getProgress() + 40) / 100.0;

						PolarCoordinates coords = PolarCoordinates.fromCartesian(x, y);

						coords.radius = coords.radius * powerPercent;

						MotorValues values = new MotorValues(coords);

						sendMessage(values.left, values.right);

						btTimer.reset();
					}

					break;

				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					sendMessage(255, 255);

					break;
			}

			return true;
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
