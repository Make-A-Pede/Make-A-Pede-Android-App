/*
 * Joystick.java
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

package com.makeapede.make_a_pede.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.makeapede.make_a_pede.R;
import com.makeapede.make_a_pede.utils.PolarCoordinates;
import com.makeapede.make_a_pede.utils.Timer;

public class ArrowView extends RelativeLayout {
	public static final int DIMENSION_NONE = 0;
	public static final int DIMENSION_HORIZONTAL = 1;
	public static final int DIMENSION_VERTICAL = 2;

	private static final int JOYSTICK_RESPONSE_TIME = 10;

	private Timer joystickTimer = new Timer();

	private float joystickHeight;
	private float joystickWidth;

	private int primaryDimension = DIMENSION_NONE;

	private JoystickTouchListener touchListener = ((event, coords) -> {});

	public ArrowView(Context context) {
		this(context, null);
	}

	public ArrowView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ArrowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.JoystickView,
				0, 0);

		try {
			primaryDimension = a.getInt(R.styleable.JoystickView_primaryDimension, 0);
		} finally {
			a.recycle();
		}

		invalidate();
		requestLayout();

		setBackgroundResource(R.mipmap.arrows_background);
		setOnTouchListener(this::onJoystickTouch);
	}

	@SuppressWarnings("SuspiciousNameCombination")
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (primaryDimension == DIMENSION_VERTICAL) {
			super.onMeasure(heightMeasureSpec, heightMeasureSpec);

			joystickHeight = joystickWidth = MeasureSpec.getSize(heightMeasureSpec);
		} else if (primaryDimension == DIMENSION_HORIZONTAL) {
			super.onMeasure(widthMeasureSpec, widthMeasureSpec);

			joystickWidth = joystickHeight = MeasureSpec.getSize(widthMeasureSpec);
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);

			joystickHeight = MeasureSpec.getSize(heightMeasureSpec);
			joystickWidth = MeasureSpec.getSize(widthMeasureSpec);
		}
	}

	private boolean onJoystickTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				if (joystickTimer.elapsedTime() > JOYSTICK_RESPONSE_TIME) {
					float eventX = event.getX();
					float eventY = event.getY();

					int x = (int) ((eventX - (joystickWidth/2.0)) * (100.0 / (joystickWidth/2.0)));
					int y = (int) (((joystickHeight/2.0) - eventY) * (100.0 / (joystickWidth/2.0)));

					PolarCoordinates coords = PolarCoordinates.fromCartesian(x, y);

					touchListener.onTouch(event, coords);

					joystickTimer.reset();
				}

				return true;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				touchListener.onTouch(event, new PolarCoordinates(0, 0));

				return true;
		}

		return false;
	}

	public void setJoystickTouchListener(JoystickTouchListener listener) {
		touchListener = listener;
	}

	public void setPrimaryDimension(int dimension) {
		primaryDimension = dimension;
		invalidate();
	}

	public int getPrimaryDimension() {
		return primaryDimension;
	}

	public interface JoystickTouchListener {
		void onTouch(MotionEvent event, PolarCoordinates coords);
	}
}
