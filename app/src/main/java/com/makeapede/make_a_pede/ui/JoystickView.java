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
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.makeapede.make_a_pede.R;
import com.makeapede.make_a_pede.utils.Timer;

public class JoystickView extends RelativeLayout {
	public static final int DIMENSION_NONE = 0;
	public static final int DIMENSION_HORIZONTAL = 1;
	public static final int DIMENSION_VERTICAL = 2;

	private static final int JOYSTICK_RESPONSE_TIME = 10;

	private ImageView dot;

	private Timer joystickTimer = new Timer();

	private int dotSize;
	private float joystickHeight;
	private float joystickWidth;

	private int primaryDimension = DIMENSION_NONE;

	private boolean measured = false;

	private JoystickTouchListener touchListener = ((event, x, y, width, height) -> {});

	public JoystickView(Context context) {
		this(context, null);
	}

	public JoystickView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public JoystickView(Context context, AttributeSet attrs, int defStyle) {
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

		initializeViews(context);
	}

	private void initializeViews(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.joystick_layout, this);

		dotSize = dpToPx(30);

		setBackgroundResource(R.drawable.border);
		setOnTouchListener(this::onJoystickTouch);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		dot = findViewById(R.id.dot);
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

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		if(!measured) {
			centerDot();

			measured = true;
		}
	}

	@Override
	public void invalidate() {
		measured = false;
		super.invalidate();
	}

	private boolean onJoystickTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				if (joystickTimer.elapsedTime() > JOYSTICK_RESPONSE_TIME) {
					float eventX = event.getX();
					float eventY = event.getY();

					moveDotToPos((int) eventX, (int) eventY);

					int x = (int) ((eventX - (joystickWidth/2.0)) * (100.0 / (joystickWidth/2.0)));
					int y = (int) (((joystickHeight/2.0) - eventY) * (100.0 / (joystickWidth/2.0)));

					touchListener.onTouch(event, x, y, joystickWidth, joystickHeight);

					joystickTimer.reset();
				}

				return true;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				centerDot();

				touchListener.onTouch(event,
						(int) joystickWidth/2,
						(int) joystickHeight/2,
						joystickWidth,
						joystickHeight);

				return true;
		}

		return false;
	}

	public void setPrimaryDimension(int dimension) {
		primaryDimension = dimension;
		invalidate();
	}

	public int getPrimaryDimension() {
		return primaryDimension;
	}

	public void setJoystickTouchListener(JoystickTouchListener listener) {
		touchListener = listener;
	}

	private void centerDot() {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dot.getLayoutParams();

		params.leftMargin = (int) ((joystickWidth / 2f) - (dotSize / 2f));
		params.topMargin = (int) ((joystickHeight / 2f) - (dotSize / 2f));

		dot.setLayoutParams(params);
	}

	private void moveDotToPos(int x, int y) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dot.getLayoutParams();

		params.leftMargin = x - (dotSize / 2);
		params.topMargin = y - (dotSize / 2);

		dot.setLayoutParams(params);
	}

	private int dpToPx(int dp) {
		return (int) TypedValue.applyDimension(1, (float) dp, getResources().getDisplayMetrics());
	}

	public interface JoystickTouchListener {
		void onTouch(MotionEvent event, int x, int y, float width, float height);
	}
}
