/*
 * HeadingView.java
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
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.makeapede.make_a_pede.R;

public class HeadingView extends FrameLayout {
	public static final int DIMENSION_NONE = 0;
	public static final int DIMENSION_HORIZONTAL = 1;
	public static final int DIMENSION_VERTICAL = 2;

	private int primaryDimension = DIMENSION_NONE;

	private ImageView indicator;
	private int currentHeading;

	public HeadingView(Context context) {
		this(context, null);
	}

	public HeadingView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HeadingView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.JoystickView,
				0, 0);

		try {
			primaryDimension = a.getInt(R.styleable.HeadingView_primaryDimension, 0);
		} finally {
			a.recycle();
		}

		invalidate();
		requestLayout();

		initializeViews(context);
	}

	private void initializeViews(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.heading_indicator_layout, this);

		setBackgroundResource(R.drawable.border);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		indicator = findViewById(R.id.indicator);
	}

	@SuppressWarnings("SuspiciousNameCombination")
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (primaryDimension == DIMENSION_VERTICAL) {
			super.onMeasure(heightMeasureSpec, heightMeasureSpec);
		} else if (primaryDimension == DIMENSION_HORIZONTAL) {
			super.onMeasure(widthMeasureSpec, widthMeasureSpec);
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	public void setPrimaryDimension(int dimension) {
		primaryDimension = dimension;
		invalidate();
	}

	public int getPrimaryDimension() {
		return primaryDimension;
	}

	public void setHeading(int newHeading) {
		int headingDelta = newHeading-currentHeading;
		currentHeading = newHeading;

		indicator.animate()
				 .rotationBy(headingDelta)
				 .setDuration(99)
				 .setInterpolator(new LinearInterpolator())
				 .start();
	}
}
