/*
 * DeviceListActivity.java
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

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.makeapede.make_a_pede.R;
import com.makeapede.make_a_pede.bluetooth.BluetoothNotSupportedException;
import com.makeapede.make_a_pede.bluetooth.BluetoothScanner;

import java.util.ArrayList;

public class DeviceListActivity extends AppCompatActivity implements BluetoothScanner.BluetoothScanEventListener {
	// Intent extra tags for BLE device name and device address
	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	public static final String EXTRAS_DEVICE_TYPE = "DEVICE_TYPE";

	// Location permission request code
	public static final int PERMISSION_REQUEST_LOCATION = 892;
	// Enable BT request code
	private static final int REQUEST_ENABLE_BT = 1;

	// Bluetooth scan period
	private static final long SCAN_PERIOD = 10000;

	private LeDeviceListAdapter leDeviceListAdapter;

	private BluetoothScanner btScanner;

	private ListView deviceList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_device_list);

		ActionBar bar = getSupportActionBar();
		if (bar != null) {
			bar.setDisplayHomeAsUpEnabled(true);
		}

		this.deviceList = findViewById(R.id.listView);

		try {
			btScanner = new BluetoothScanner(this);
		} catch (BluetoothNotSupportedException e) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.device_list_activity_menu, menu);
		if (!btScanner.isScanning()) {
			menu.findItem(R.id.menu_stop).setVisible(false);
			menu.findItem(R.id.menu_scan).setVisible(true);
			menu.findItem(R.id.menu_refresh).setActionView(null);
		} else {
			menu.findItem(R.id.menu_stop).setVisible(true);
			menu.findItem(R.id.menu_scan).setVisible(false);

			View indicatorLayout = getLayoutInflater().inflate(R.layout.actionbar_indeterminate_progress, null);
			ProgressBar indicator = indicatorLayout.findViewById(R.id.loading_indicator);

			indicator.setVisibility(View.VISIBLE);
			indicator.setIndeterminate(true);
			indicator.getIndeterminateDrawable().setColorFilter(0xFF323232, PorterDuff.Mode.MULTIPLY);

			menu.findItem(R.id.menu_refresh).setActionView(indicatorLayout);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_scan:
				leDeviceListAdapter.clear();
				scanLeDevice(true);
				break;
			case R.id.menu_stop:
				scanLeDevice(false);
				break;
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}

		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		leDeviceListAdapter = new LeDeviceListAdapter();
		deviceList.setAdapter(leDeviceListAdapter);
		deviceList.setOnItemClickListener(this::onListItemClick);

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {

			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
					Manifest.permission.ACCESS_COARSE_LOCATION)) {

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Location permission is required to connect to your Make-A-Pede");

				builder.setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
						PERMISSION_REQUEST_LOCATION));

				builder.create().show();
			} else {
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
						PERMISSION_REQUEST_LOCATION);
			}
		} else {
			connect();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
		leDeviceListAdapter.clear();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_REQUEST_LOCATION: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					connect();

				} else {
					finish();
				}
			}
		}
	}

	private void connect() {
		if (!btScanner.getBtAdapter().isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		scanLeDevice(true);
	}

	protected void onListItemClick(AdapterView l, View v, int position, long id) {
		final BluetoothDevice device = leDeviceListAdapter.getDevice(position);

		if (device == null) return;

		final Intent intent = new Intent(this, ControllerActivity.class);
		intent.putExtra(EXTRAS_DEVICE_NAME, device.getName());
		intent.putExtra(EXTRAS_DEVICE_ADDRESS, device.getAddress());
		intent.putExtra(EXTRAS_DEVICE_TYPE, device.getType());

		btScanner.stopScan();

		startActivity(intent);
	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			btScanner.startScan(this, SCAN_PERIOD);
		} else {
			btScanner.stopScan();
		}

		invalidateOptionsMenu();
	}

	@Override
	public void onDeviceFound(BluetoothDevice device) {
		leDeviceListAdapter.addDevice(device);
		leDeviceListAdapter.notifyDataSetChanged();
	}

	@Override
	public void onScanComplete() {
		invalidateOptionsMenu();
	}

	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> leDevices;
		private LayoutInflater inflater;

		LeDeviceListAdapter() {
			super();
			leDevices = new ArrayList<>();
			inflater = DeviceListActivity.this.getLayoutInflater();
		}

		void addDevice(BluetoothDevice device) {
			if(!leDevices.contains(device)) {
				leDevices.add(device);
			}
		}

		BluetoothDevice getDevice(int position) {
			return leDevices.get(position);
		}

		void clear() {
			leDevices.clear();
		}

		@Override
		public int getCount() {
			return leDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return leDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;

			if (view == null) {
				view = inflater.inflate(R.layout.listitem_device, null);
				viewHolder = new ViewHolder();
				viewHolder.deviceAddress = view.findViewById(R.id.device_address);
				viewHolder.deviceName = view.findViewById(R.id.device_name);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			BluetoothDevice device = leDevices.get(i);
			final String deviceName = device.getName();

			if (deviceName != null && deviceName.length() > 0)
				viewHolder.deviceName.setText(deviceName);
			else
				viewHolder.deviceName.setText(R.string.unknown_device);

			viewHolder.deviceAddress.setText(device.getAddress());

			return view;
		}
	}

	private static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
	}
}
