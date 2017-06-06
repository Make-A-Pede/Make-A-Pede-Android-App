/*
 * DemoActivity.java
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

package com.makeapede.make_a_pede.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.makeapede.make_a_pede.R;
import com.makeapede.make_a_pede.fragments.ArrowKeyFragment;

public class DemoActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo);

		FragmentManager fragmentManager = getSupportFragmentManager();

		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		ArrowKeyFragment arrowKeyFragment = new ArrowKeyFragment();
		arrowKeyFragment.setMessageListener(this::sendMessage);

		fragmentTransaction.replace(R.id.fragment_container, arrowKeyFragment);
		fragmentTransaction.commit();
	}

	private void sendMessage(int left, int right) {
		Log.i("DemoActivity", "Left: " + left + ", Right: " + right);
	}
}
