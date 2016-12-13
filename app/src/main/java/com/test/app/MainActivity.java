package com.test.app;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.test.app.model.Bandwidths;
import com.test.app.model.DataModel;
import com.test.app.model.DataModelListener;
import com.test.app.model.Results;
import com.test.app.save.ServerService;
import com.test.app.task.AlarmReceiver;
import com.test.app.task.MyService;

public class MainActivity extends Activity implements OnClickListener,
		DataModelListener, OnSharedPreferenceChangeListener
{
	private Button start, clear, result, bottom, up, on, off;
	private TextView information, console, measurements, usedData, period,
			status;
	private SharedPreferences sharedPrefs;
	private DataModel model;

	private Intent intent;
	private ProgressDialog progDailog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Set Layout
		setContentView(R.layout.activity_main);

		// Initialize Shared Preferences
		sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		sharedPrefs.registerOnSharedPreferenceChangeListener(this);

		// Set default Value of Root Status to false
		sharedPrefs.edit().putBoolean("Root", false).apply();
		// Set Value to show that App is active (important for Service)
		sharedPrefs.edit().putBoolean("Active", true).apply();
		// Set Value to show that new measurement was made
		sharedPrefs.edit().putBoolean("Measurement", false).apply();

		LocalBroadcastManager.getInstance(this)
				.registerReceiver(mMessageReceiver,
						new IntentFilter(DataModel.BROADCASTRECEIVER));

		Intent serverIntent = new Intent(this, ServerService.class);
		// Get PendingIntent with unique ID
		PendingIntent pendingIntent = PendingIntent.getService(this, 0,
				serverIntent, 0);

		// Start ServerService
		// Initialize Service to send Data to Server
		// sharedPrefs.edit().putBoolean("Server", true).apply();
		if (!sharedPrefs.getBoolean("Server", false))
		{
			AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

			manager.setRepeating(AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis(), AlarmManager.INTERVAL_DAY,
					pendingIntent);
			sharedPrefs.edit().putBoolean("Server", true).apply();
		}

		// Initialize Model
		this.model = new DataModel(sharedPrefs, this);
		this.model.addListener(this);

		// Get TextViews
		console = (TextView) findViewById(R.id.console);
		information = (TextView) findViewById(R.id.show_info);
		// Statistic Views
		measurements = (TextView) findViewById(R.id.measurements);
		usedData = (TextView) findViewById(R.id.usedData);
		period = (TextView) findViewById(R.id.period);
		status = (TextView) findViewById(R.id.status_icon);

		// Get Buttons
		start = (Button) findViewById(R.id.start_button);
		start.setOnClickListener(this);

		clear = (Button) findViewById(R.id.clear_button);
		clear.setOnClickListener(this);

		bottom = (Button) findViewById(R.id.bottom_button);
		bottom.setOnClickListener(this);

		up = (Button) findViewById(R.id.maximize_console);
		up.setOnClickListener(this);

		result = (Button) findViewById(R.id.result_button);
		result.setOnClickListener(this);

		on = (Button) findViewById(R.id.automatic_button);
		on.setOnClickListener(this);

		off = (Button) findViewById(R.id.stop_button);
		off.setOnClickListener(this);

		// Initialize ProgressDialog
		progDailog = new ProgressDialog(this);

		// Initialize intent for Service
		intent = new Intent(this, MyService.class);

		// Load possible saved data
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					model.loadData();
					// Inform Views to display new Data
					model.informMeasurementListeners();
				} catch (FileNotFoundException e)
				{
					model.informConsoleListeners(model.getContext()
							.getResources().getString(R.string.noSavedData));
					model.informMeasurementListeners();
				} catch (IOException e1)
				{
					model.informConsoleListeners(model.getContext()
							.getResources().getString(R.string.readex));
					model.informMeasurementListeners();
				}
			}
		}).start();

		// Ask wether Phone is rooted
		this.askForRoot();

		// Set Status Icon if automatic Measurement is still activated
		if (sharedPrefs.getBoolean("Status", false))
		{
			status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ok,
					0);
		} else
		{
			status.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					R.drawable.not_ok, 0);
		}

		// If Root Access is granted you can use the Sniffer
		if (sharedPrefs.getBoolean("Root", false))
		{
			// Copy Sniffer from Assets Folder to App Directory if it isn't
			// there
			File f = new File(getCacheDir() + "/sniffer");

			if (!f.exists())
				try
				{
					InputStream is = getAssets().open("sniffer");
					int size = is.available();
					byte[] buffer = new byte[size];
					is.read(buffer);
					is.close();
					FileOutputStream fos = new FileOutputStream(f);
					fos.write(buffer);
					fos.flush();
					fos.close();
				} catch (Exception e)
				{
					console.setText(console.getText() + "RuntimeException");
					throw new RuntimeException(e);
				}
		}

		// Load trusted Keystore into App Directory
		File f = new File(getCacheDir() + "/keystore");

		if (!f.exists())
			try
			{
				InputStream is = getAssets().open("keystore");
				int size = is.available();
				byte[] buffer = new byte[size];
				is.read(buffer);
				is.close();
				FileOutputStream fos = new FileOutputStream(f);
				fos.write(buffer);
				fos.flush();
				fos.close();
			} catch (Exception e)
			{
				console.setText(console.getText() + "RuntimeException");
				throw new RuntimeException(e);
			}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		// Set Value to show that App is active (important for
		// Service)
		sharedPrefs.edit().putBoolean("Active", true).apply();
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		// Save only if new Data is available
		if (sharedPrefs.getBoolean("Measurement", false))
		{
			// Save Informations to File
			try
			{
				model.saveData();
				sharedPrefs.edit().putBoolean("Measurement", false).apply();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		// Set Value to show that App isn't active anymore (important for
		// Service)
		sharedPrefs.edit().putBoolean("Active", false).apply();
	};

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		// If a running Measurement is still in work stop Service
		stopService(intent);
		// Also stop Progress-Dialog
		if (progDailog != null && progDailog.isShowing())
		{
			progDailog.dismiss();
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}
		if (id == R.id.action_development_settings)
		{
			startActivity(new Intent(this, DevelopmentSettingsActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v)
	{
		// Start Button
		// Starts the calculation
		if (v.getId() == R.id.start_button)
		{
			intent.putExtra("Results", model.getResults());
			startService(intent);

			// Reset Button
			// Reset the Console View
		} else if (v.getId() == R.id.clear_button)
		{
			console.setText("");
			// Minimize Console
		} else if (v.getId() == R.id.bottom_button)
		{
			ViewGroup showPanel = (ViewGroup) findViewById(R.id.measurementView);
			showPanel.setVisibility(View.VISIBLE);
			// Maximize Console
		} else if (v.getId() == R.id.maximize_console)
		{
			ViewGroup hiddenPanel = (ViewGroup) findViewById(R.id.measurementView);
			hiddenPanel.setVisibility(View.GONE);
		}
		// Open View with Measurement Results
		else if (v.getId() == R.id.result_button)
		{
			Intent nextScreen = new Intent(getApplicationContext(),
					ResultActivity.class);
			// Pass Result-Objects to ResultActivity to display
			nextScreen.putExtra("Result", model.getResults());
			startActivity(nextScreen);
			// Start Automatic
		} else if (v.getId() == R.id.automatic_button)
		{
			if (sharedPrefs.getBoolean("Status", false))
			{
				// Display Message that automatic Measurement is already started
				model.informConsoleListeners(model.getContext().getResources()
						.getString(R.string.automaticMeasurementEvenStarted));
				return;
			}
			// Change Status in Preferences
			sharedPrefs.edit().putBoolean("Status", true).apply();
			// Display Message on Console
			model.informConsoleListeners(model.getContext().getResources()
					.getString(R.string.automaticMeasurementOn));

			// Change Icon
			status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ok,
					0);

			// Start automatic Measurement
			this.start();

			// Stop Automatic
		} else if (v.getId() == R.id.stop_button)
		{
			// Change Status in Preferences
			sharedPrefs.edit().putBoolean("Status", false).apply();
			// Display Message on Console
			model.informConsoleListeners(model.getContext().getResources()
					.getString(R.string.automaticMeasurementOff));
			// Change Icon
			status.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					R.drawable.not_ok, 0);

			// Stop automatic Measurement
			this.cancel();
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key)
	{
		if (key.equals(getString(R.string.number_key)))
		{
			Toast.makeText(this, "Change NumberPicker Value",
					Toast.LENGTH_SHORT).show();
			if (sharedPrefs.getBoolean("Status", false))
			{
				// Cancel and restart AlarmManager with new repeating Value
				this.cancel();
				this.start();
				Toast.makeText(this, "Restart AlarmManager with new Value",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	// Methods from own Interface (DataModelListener)
	@Override
	public void updateConsoleView(String message)
	{
		final String m = message;
		runOnUiThread(new Runnable()
		{

			public void run()
			{
				// Set Text
				console.setText(console.getText() + m + "\n");
				// Set ScrollView to bottom
				ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView_console);
				scrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

	@Override
	public void updateMeasurementView()
	{
		runOnUiThread(new Runnable()
		{

			public void run()
			{
				if (model.getResults() != null && !model.getResults().isEmpty())
				{
					// Update Number of Measurements
					measurements.setText(getResources().getString(
							R.string.measurements)
							+ "\n" + model.getNumberOfMeasurements() + "\n");

					// Update Used Data
					usedData.setText(getResources()
							.getString(R.string.useddata)
							+ "\n~ "
							+ model.getTotalUsedData() / 1000 + " KB\n");

					// Update Measurement Period
					period.setText(getResources().getString(R.string.period)
							+ "\n"
							+ model.getResults().get(0).getDate()
							+ "\n"
							+ model.getResults()
									.get(model.getResults().size() - 1)
									.getDate());

					// Display Last Measurement
					Results last = model.getResults().get(
							model.getResults().size() - 1);
					ArrayList<Bandwidths> measurements = last.getObjects();
					information.setText(getResources().getString(
							R.string.last_measurement)
							+ ": " + last.getDate() + "\n");
					for (int i = 0; i < measurements.size(); i++)
					{
						information.setText(information.getText()
								+ DataModel.getMethodName(measurements.get(i)
										.getMethod()) + ": "
								+ measurements.get(i).getAvgBandwidth()
								+ " KB/s\n");
					}
				} else
				{
					// Update Number of Measurements
					measurements.setText(getResources().getString(
							R.string.measurements)
							+ "\n0\n");

					// Update Used Data
					usedData.setText(getResources()
							.getString(R.string.useddata) + "\n~ 0 KB\n");

					// Update Measurement Period
					period.setText(getResources().getString(R.string.period)
							+ "\n-\n-");
					// Update last Measurement View
					information.setText("");
				}

			}
		});
	}

	public void askForRoot()
	{
		Process p;
		try
		{
			// Preform su to get root privledges
			p = Runtime.getRuntime().exec("su");

			// Attempt to write a file to a root-only
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			os.writeBytes("echo \"Do I have root?\" >/system/sd/temporary.txt\n");

			// Close the terminal
			os.writeBytes("exit\n");
			os.flush();
			try
			{
				p.waitFor();
				if (p.exitValue() != 255)
				{
					Toast.makeText(this, "Root. ", Toast.LENGTH_SHORT).show();
					sharedPrefs.edit().putBoolean("Root", true).apply();
				} else
				{
					Toast.makeText(this, "Not Root. ", Toast.LENGTH_SHORT)
							.show();
				}
			} catch (InterruptedException e)
			{
				Toast.makeText(this, "Not Root. ", Toast.LENGTH_SHORT).show();
			}
		} catch (IOException e)
		{
			Toast.makeText(this, "Not Root. ", Toast.LENGTH_SHORT).show();
		}
	}

	public void start()
	{
		new AlarmReceiver().onReceive(this, null);
		// Toast.makeText(this, "Automatic started", Toast.LENGTH_SHORT).show();
	}

	public void cancel()
	{
		AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent alarmIntent = new Intent(getApplicationContext(),
				MyService.class);
		// Get PendingIntent with unique ID
		PendingIntent pendingIntent = PendingIntent.getService(
				getApplication(), DataModel.ALARMPID, alarmIntent, 0);
		// Cancel PendingIntent
		pendingIntent.cancel();
		// Cancel Alarm for this PendingIntent
		manager.cancel(pendingIntent);

		// Toast.makeText(this, "Automatic stopped", Toast.LENGTH_SHORT).show();
	}

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String command = intent.getStringExtra(DataModel.COMMAND);

			if (command.equals(DataModel.RESULT))
			{
				// Get extra data included in the Intent
				@SuppressWarnings("unchecked")
				ArrayList<Results> results = (ArrayList<Results>) intent
						.getSerializableExtra(DataModel.INFORMATION);
				@SuppressWarnings("unchecked")
				ArrayList<Results> dailyResults = (ArrayList<Results>) intent
						.getSerializableExtra(DataModel.ADDITIONALINFO);

				// Set Results to Model
				model.setResults(results);

				// Inform Views
				model.informMeasurementListeners();

			} else if (command.equals(DataModel.MESSAGE))
			{
				String message = intent.getStringExtra(DataModel.INFORMATION);

				// If Data saved to Server clear Data in Model
				if (message.equals(DataModel.DATASAVED))
				{
					Toast.makeText(model.getContext(), "Data has been saved",
							Toast.LENGTH_SHORT).show();
					model.informMeasurementListeners();
				}
				// Display Information on Screen
				else
				{
					model.informConsoleListeners(message);
				}
			} else if (command.equals(DataModel.INITPROGRESS))
			{
				if (progDailog != null)
				{
					progDailog = new ProgressDialog(model.getContext());
					progDailog.setMessage("Execute...");
					progDailog.setIndeterminate(false);
					progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					progDailog.setCancelable(false);
					// Don't show if Activity isn't finished
					if (!((Activity) model.getContext()).isFinishing())
					{
						progDailog.show();
					}
				}
			} else if (command.equals(DataModel.PROGRESS))
			{
				int first = intent.getIntExtra("First", 0);
				int second = intent.getIntExtra("Second", 0);
				String method = intent.getStringExtra("Method");

				if ((progDailog != null) && progDailog.isShowing())
				{
					progDailog
							.setMessage(model.getContext().getResources()
									.getString(R.string.execute)
									+ " "
									+ first
									+ "/"
									+ second
									+ "\n["
									+ method + "]");
				}

			} else if (command.equals(DataModel.DISMISSPROGRESS))
			{
				if ((progDailog != null) && progDailog.isShowing())
				{
					progDailog.dismiss();
				}
			} else if (command.equals(DataModel.DIALOG))
			{
				// To differ possible different dialogs
				String dialogType = intent
						.getStringExtra(DataModel.INFORMATION);

				// Show Dialog, that Measurements are only execute with Wifi
				if (dialogType.equals(DataModel.WIFIDIALOG))
				{
					final Intent i = new Intent(model.getContext(),
							SettingsActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

					new AlertDialog.Builder(model.getContext())
							.setTitle(
									model.getContext()
											.getResources()
											.getString(
													R.string.wifiDialog_title))
							.setMessage(
									model.getContext()
											.getResources()
											.getString(
													R.string.wifiDialog_message))
							.setPositiveButton(android.R.string.yes,
									new DialogInterface.OnClickListener()
									{
										public void onClick(
												DialogInterface dialog,
												int which)
										{
											// Go to Settings
											startActivity(i);
										}
									})
							.setNegativeButton(android.R.string.no,
									new DialogInterface.OnClickListener()
									{
										public void onClick(
												DialogInterface dialog,
												int which)
										{
											return;
										}
									})
							.setIcon(android.R.drawable.ic_dialog_alert).show();
				}
			}

		}
	};
}