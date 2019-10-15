/*
 * InfoActivity.java
 * Copyright (C) 2018  Automata Development
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

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;

import com.makeapede.make_a_pede.R;

public class InfoActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);

		Button sourceButton = findViewById(R.id.source_button);
		Button licenseButton = findViewById(R.id.license_button);
		Button creditsButton = findViewById(R.id.credits_button);

		sourceButton.setOnClickListener(view -> openLink("https://github.com/Make-A-Pede/Make-A-Pede-Android-App"));
		licenseButton.setOnClickListener(view -> openLink("https://github.com/Make-A-Pede/Make-A-Pede-Android-App/blob/master/LICENSE"));
		creditsButton.setOnClickListener(view -> openLink("https://github.com/Make-A-Pede/Make-A-Pede-Android-App/blob/master/CREDITS.md"));
	}

	public void openLink(String url) {
		CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
		CustomTabsIntent customTabsIntent = builder.build();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			builder.setToolbarColor(getColor(R.color.colorPrimary));
		} else {
			builder.setToolbarColor(0x91cf34);
		}
		customTabsIntent.launchUrl(this, Uri.parse(url));
	}
}
