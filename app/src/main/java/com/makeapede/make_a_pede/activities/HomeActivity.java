/*
 * HomeActivity.java
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

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.makeapede.make_a_pede.R;

import io.fabric.sdk.android.Fabric;

public class HomeActivity extends AppCompatActivity {
	public static final String EXTRA_DEMO = "demo";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Fabric.with(this, new Crashlytics());
		setContentView(R.layout.activity_home);

		setTitle("");
	}

	/**
	 * "Connect to Make-A-Pede" button click listener. Opens {@code DeviceListActivity}.
	 *
	 * @param view the clicked view
	 */
	public void openConnectActivity(View view) {
		Intent intent = new Intent(this, DeviceListActivity.class);
		startActivity(intent);
	}

	/**
	 * "Open Website" button click listener. Opens makeapede.com in a Chrome Custom Tab.
	 *
	 * @param view the clicked view
	 */
	public void openWebsite(View view) {
		String url = "http://makeapede.com/";
		CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
		CustomTabsIntent customTabsIntent = builder.build();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			builder.setToolbarColor(getColor(R.color.colorPrimary));
		} else {
			builder.setToolbarColor(0x91cf34);
		}
		customTabsIntent.launchUrl(this, Uri.parse(url));
	}

	public void openPrivacyPolicy() {
		String url = "http://makeapede.com/android/privacy";
		CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
		CustomTabsIntent customTabsIntent = builder.build();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			builder.setToolbarColor(getColor(R.color.colorPrimary));
		} else {
			builder.setToolbarColor(0x91cf34);
		}
		customTabsIntent.launchUrl(this, Uri.parse(url));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.home_activity_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.action_open_demo:
				Intent intent = new Intent(this, ControllerActivity.class);
				intent.putExtra(EXTRA_DEMO, true);
				startActivity(intent);
				break;
			case R.id.action_open_info:
				startActivity(new Intent(this, InfoActivity.class));
				break;
			case R.id.action_open_privacy_policy:
				openPrivacyPolicy();
				break;
		}

		return true;
	}
}
