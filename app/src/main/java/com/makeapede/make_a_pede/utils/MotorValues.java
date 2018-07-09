package com.makeapede.make_a_pede.utils;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.signum;

public class MotorValues {
	public int left;
	public int right;

	public MotorValues(int left, int right) {
		this.left = left;
		this.right = right;
	}

	public MotorValues(PolarCoordinates coords) {
		this(CartesianCoordinates.fromPolar(coords));
	}

	public MotorValues(CartesianCoordinates coords) {
		int x = coords.x;
		int y = coords.y;

		left = y+x;
		right = y-x;

		if (signum(left) != signum(right)) {
			if (y >= 0) {
				left = max(0, left);
				right = max(0, right);
			} else {
				left = min(0, left);
				right = min(0, right);
			}
		}

		if (y < 0) {
			int temp = left;
			left = right;
			right = temp;
		}

		left = left * (255 / 50);
		right = right * (255 / 50);

		left = min(left, 255);
		left = max(left, -255);

		right = min(right, 255);
		right = max(right, -255);
	}
}
