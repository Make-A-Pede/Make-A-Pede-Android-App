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
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeapede.make_a_pede.R;
import com.makeapede.make_a_pede.firebase.FbLinkAction;

import java.util.Random;

public class HomeActivity extends AppCompatActivity {
	private String fbLinkText;
	private String fbLinkUrl;
	private String fbLinkActionText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		setTitle("");

		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference buttonsRef = database.getReference("/buttons");

		buttonsRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				int childrenCount = (int) dataSnapshot.getChildrenCount();

				Random random = new Random(System.currentTimeMillis());
				int index = random.nextInt(childrenCount);

				FbLinkAction action = dataSnapshot.child(String.valueOf(index)).getValue(FbLinkAction.class);
				fbLinkText = action.text;
				fbLinkUrl = action.link;
				fbLinkActionText = action.action;

				Snackbar snackbar = Snackbar.make(
						findViewById(R.id.coordinator_layout),
						fbLinkText,
						Snackbar.LENGTH_INDEFINITE);

				snackbar.setAction(fbLinkActionText, HomeActivity.this::onFbLinkActionClicked);

				snackbar.show();
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {}
		});
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
			builder.setToolbarColor(Color.parseColor("#303030"));
		}
		customTabsIntent.launchUrl(this, Uri.parse(url));
	}

	public void onFbLinkActionClicked(View view) {
		CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
		CustomTabsIntent customTabsIntent = builder.build();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			builder.setToolbarColor(getColor(R.color.colorPrimary));
		} else {
			builder.setToolbarColor(Color.parseColor("#303030"));
		}
		customTabsIntent.launchUrl(this, Uri.parse(fbLinkUrl));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.home_activity_menu, menu);

		MenuItem item = menu.findItem(R.id.action_open_demo);
		SpannableString s = new SpannableString("Joystick Demo");
		s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
		item.setTitle(s);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_open_demo:
				Intent intent = new Intent(this, DemoActivity.class);
				startActivity(intent);
				break;
		}

		return true;
	}
}
