/*
 * CartesianCoordinates.java
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

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

public class CartesianCoordinates {
	public int x;
	public int y;

	public static CartesianCoordinates fromPolar(PolarCoordinates coords) {
		int x = (int) (coords.radius * cos(toRadians(coords.angle)));
		int y = (int) (coords.radius * sin(toRadians(coords.angle)));

		return new CartesianCoordinates(x, y);
	}

	public CartesianCoordinates(int x, int y) {
		this.x = x;
		this.y = y;
	}


	@Override
	public String toString() {
		return "X: " + x + ", Y: " + y;
	}
}
