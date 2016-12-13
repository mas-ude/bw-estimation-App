package com.test.app.task;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.test.app.NumberPickerPreference;
import com.test.app.R;
import com.test.app.model.DataModel;
import com.test.app.save.ServerService;

public class AlarmReceiver extends BroadcastReceiver
{
	private PendingIntent pendingIntent;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		// Get User Settings from Shared Preferences
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		// Start automatic only if the Status is still true
		if (sharedPrefs.getBoolean("Status", false))
		{
			int repeatValue = NumberPickerPreference
					.getValueOfPicker(sharedPrefs.getInt(context
							.getString(R.string.number_key), context
							.getResources()
							.getInteger(R.integer.number_default)));

			Intent alarmIntent = new Intent(context, MyService.class);
			// Get PendingIntent with unique ID
			pendingIntent = PendingIntent.getService(context,
					DataModel.ALARMPID, alarmIntent, 0);

			AlarmManager manager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);

			// Milliseconds per day = 86400 * 1000
			int interval = 86400 * 1000 / repeatValue;
			manager.setRepeating(AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis(), interval, pendingIntent);

			// Start ServerService
			if (!sharedPrefs.getBoolean("Server", false))
			{
				// Initialize Service to send Data to Server
				Intent serverIntent = new Intent(context, ServerService.class);
				// Get PendingIntent with unique ID
				PendingIntent pendingIntent = PendingIntent.getService(context,
						0, serverIntent, 0);

				manager = (AlarmManager) context
						.getSystemService(Context.ALARM_SERVICE);

				manager.setRepeating(AlarmManager.RTC_WAKEUP,
						System.currentTimeMillis(), AlarmManager.INTERVAL_DAY,
						pendingIntent);
			}
		}

	}
}
