package com.test.app.save;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.test.app.model.DataModel;

public class ServerService extends Service
{

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Toast.makeText(this, "ServerService Started", Toast.LENGTH_SHORT)
				.show();

		// Get User Settings from Shared Preferences
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		// Init new DataModel-Object to save measurement results temporarily
		DataModel model = new DataModel(sharedPrefs, this);

		// Load possible saved data into Model
		try
		{
			model.loadDailyData();
		} catch (Exception e)
		{

		}

		// Only send Data to Server if data is available
		if (!model.getDailyResults().isEmpty())
		{
			// Start Thread
			new SendDataThread(model, this).start();
		} else
		{
			this.stopSelf();
		}

		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		Toast.makeText(this, "ServerService Destroyed", Toast.LENGTH_SHORT)
				.show();
	}

}
