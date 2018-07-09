/*
 * SlidersFragment.java
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import com.makeapede.make_a_pede.R;
import com.makeapede.make_a_pede.utils.CartesianCoordinates;
import com.makeapede.make_a_pede.utils.MotorValues;
import com.makeapede.make_a_pede.utils.Timer;

public class SlidersFragment extends ControllerFragment {
	private Timer btTimer = new Timer();

	private SeekBar turnSlider;
	private VerticalSeekBar speedSlider;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.sliders_fragment_layout, container, false);

		turnSlider = layout.findViewById(R.id.turnSlider);
		speedSlider = layout.findViewById(R.id.speedSlider);

		turnSlider.setOnSeekBarChangeListener(seekBarListener);
		speedSlider.setOnSeekBarChangeListener(seekBarListener);

		return layout;
	}

	private void slidersMoved() {
		if (btTimer.elapsedTime() > getBtSendInterval()) {
			int x = (int) ((turnSlider.getProgress() - 100) * (200/180.0));
			int y = (int) ((speedSlider.getProgress() - 100) * (200/160.0));

			MotorValues values = new MotorValues(new CartesianCoordinates(x, y));

			sendMessage(values);
			btTimer.reset();
		}
	}

	private SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
			int topLimit = seekBar instanceof VerticalSeekBar ? 180 : 190;
			int bottomLimit = seekBar instanceof VerticalSeekBar ? 20 : 10;

			if (seekBar.getProgress() < bottomLimit) {
				seekBar.setProgress(bottomLimit);
			} else if (seekBar.getProgress() > topLimit) {
				seekBar.setProgress(topLimit);
			}

			slidersMoved();
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			seekBar.setProgress(100);
			sendMessage(new MotorValues(0, 0));
		}
	};
}
