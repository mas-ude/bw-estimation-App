package com.test.app.task;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.test.app.R;
import com.test.app.model.DataModel;
import com.test.app.model.Results;

public class MyService extends Service
{
	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();

		// Get passed Results from Activity if App is open
		@SuppressWarnings("unchecked")
		ArrayList<Results> results = (ArrayList<Results>) intent
				.getSerializableExtra("Results");
		@SuppressWarnings("unchecked")
		ArrayList<Results> dailyResults = (ArrayList<Results>) intent
				.getSerializableExtra("DailyResults");

		// Get User Settings from Shared Preferences
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		// Init new DataModel-Object to save measurement results temporarily
		DataModel model = new DataModel(sharedPrefs, this);

		if (results != null)
		{
			model.setResults(results);
			if (dailyResults != null)
			{
				model.setDailyResults(dailyResults);
			}
		} else
		{
			// Load possible saved data
			try
			{
				model.loadData();
				model.loadDailyData();
			} catch (Exception e)
			{

			}
		}

		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		// Get Network-Status Information about mobile and Wifi
		NetworkInfo wifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobile = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		Results resultObject = model.getDeviceInformations(connManager, wifi,
				mobile);

		// User Setting
		boolean wifiSetting = sharedPrefs.getBoolean(
				getString(R.string.wifi_key),
				getResources().getBoolean(R.bool.wifi_default));

		// Check if wifi or mobile is connected and perform accordingly
		if ((wifiSetting && wifi.isConnected())
				|| (!wifiSetting && (mobile.isConnected() || wifi.isConnected())))
		{
			// Create Execution Task
			new MeasurementTask(model, resultObject, this).start();
		} else if (!mobile.isConnected() && !wifi.isConnected())
		{
			// No Connection available
			model.sendMessagestoBroadcast(DataModel.MESSAGE, getResources()
					.getString(R.string.noconnect));
			// Service doesn't stay in memory so stop it immediately
			this.stopSelf();
		} else if (wifiSetting && (!wifi.isConnected() || mobile.isConnected()))
		{
			// Send Message over BroadcastReceiver to show Dialog in
			// MainActivity
			model.sendMessagestoBroadcast(DataModel.DIALOG,
					DataModel.WIFIDIALOG);
			// Service doesn't stay in memory so stop it immediately
			this.stopSelf();
		}

		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
	}
}
