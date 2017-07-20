/*
 * PolarCoordinates.java
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

package com.makeapede.make_a_pede.utils;

public class PolarCoordinates {
	public double radius;
	public double angle;

	public PolarCoordinates(double radius, double angle) {
		this.radius = radius;
		this.angle = angle;
	}

	public static PolarCoordinates fromCartesian(int x, int y) {
		double radius = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		double angle = Math.toDegrees(Math.atan2(y, x));

		if (angle < 0) {
			angle = 360-Math.abs(angle);
		}

		return new PolarCoordinates(radius, angle);
	}

	@Override
	public String toString() {
		return "Radius: " + radius + ", Angle: " + angle;
	}
}