package com.test.app;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.test.app.model.DataModel;
import com.test.app.model.DataModelListener;
import com.test.app.model.DataObject;
import com.test.app.model.MyLocationListener;
import com.test.app.model.ResultObject;
import com.test.app.task.ExecuteMethodsTask;
import com.test.app.task.SnifferThread;

public class MainActivity extends Activity implements OnClickListener,
		DataModelListener, OnSharedPreferenceChangeListener
{
	private Button start;
	private Button reset;
	private Button result;
	private TextView information, console, measurements, usedData, period;
	private SharedPreferences sharedPrefs;
	private DataModel model;
	private Process process;
	// GPS
	private LocationListener mLocationListener;
	private LocationManager locationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		sharedPrefs.registerOnSharedPreferenceChangeListener(this);

		mLocationListener = new MyLocationListener();
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		setContentView(R.layout.activity_main);
	}

	@SuppressWarnings("unchecked")
	// Read ArrayList<ResultObject> from File
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Initialize Model
		this.model = new DataModel(sharedPrefs, this);
		this.model.addListener(this);

		// Ask wether Phone is rooted
		this.askForRoot();

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		// Get TextViews
		console = (TextView) findViewById(R.id.console);
		information = (TextView) findViewById(R.id.show_info);
		// Statistic Views
		measurements = (TextView) findViewById(R.id.measurements);
		usedData = (TextView) findViewById(R.id.usedData);
		period = (TextView) findViewById(R.id.period);

		// Get Buttons
		start = (Button) findViewById(R.id.start_button);
		start.setOnClickListener(this);

		reset = (Button) findViewById(R.id.reset_button);
		reset.setOnClickListener(this);

		result = (Button) findViewById(R.id.result_button);
		result.setOnClickListener(this);

		// Load possible saved data
		FileInputStream fis;
		ArrayList<ResultObject> results = null;
		try
		{
			fis = openFileInput(DataModel.SAVEFILE);
			ObjectInputStream input = new ObjectInputStream(fis);
			results = (ArrayList<ResultObject>) input.readObject();
			this.model.setResults(results);
			input.close();
			fis.close();
		} catch (ClassNotFoundException e)
		{
			model.informConsoleListeners("Keine gespeicherten Daten vorhanden.");
		} catch (IOException e1)
		{
			model.informConsoleListeners("Fehler beim Lesen der gespeicherten Dateien.");
		}

		// Write Information about statistical data
		measurements.setText(getResources().getString(R.string.measurements)
				+ "\n" + model.getNumberOfMeasurements() + "\n");
		usedData.setText(getResources().getString(R.string.useddata) + "\n"
				+ model.getTotalUsedData() + "\n");
		if (model.getResults().size() > 0)
		{
			period.setText(getResources().getString(R.string.period)
					+ "\n"
					+ model.getResults().get(0).getDate()
					+ "\n"
					+ model.getResults().get(model.getResults().size() - 1)
							.getDate());
		} else
		{
			period.setText(R.string.period + "\n-\n");
		}

		// Copy Sniffer from Assets Folder to App Directory if it isn't there
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

		// Display Last Measurement
		if (model.getNumberOfMeasurements() > 0)
		{
			ResultObject last = model.getResults().get(
					model.getResults().size() - 1);
			ArrayList<DataObject> measurements = last.getObjects();
			information.setText(getResources().getString(
					R.string.last_measurement)
					+ ": " + last.getDate() + "\n");
			for (int i = 0; i < measurements.size(); i++)
			{
				information.setText(information.getText()
						+ DataModel.getMethodName(measurements.get(i)
								.getMethod()) + ": "
						+ measurements.get(i).getAvgBandwidth() + " KB/s\n");
			}
		}

		// Start Sniffer if Root Access is granted
		if (model.isRoot())
		{
			try
			{
				process = Runtime.getRuntime().exec("su");
				new SnifferThread(model, process).start();

			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return true;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		// Kill Process that started C-Sniffer
		if (process != null)
		{
			process.destroy();
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

	public void onClick(View v)
	{
		// Start Button
		// Starts the calculation
		if (v.getId() == R.id.start_button)
		{
			// Get Network-Status Information about mobile and Wifi
			ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifi = connManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			NetworkInfo mobile = connManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

			// Create new ResultObject with general Data
			ResultObject resultObject = new ResultObject(wifi.isConnected(),
					mobile.isConnected(), DataModel.getActualDate(model
							.getSharedPrefs().getBoolean(
									getString(R.string.privacy_time_key),
									getResources().getBoolean(
											R.bool.privacy_time_default))));

			// Get Provider
			TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String provider = telephonyManager.getNetworkOperatorName();
			resultObject.setProvider(provider);

			// Get Country Code
			String cCode = Locale.getDefault().getCountry();
			resultObject.setcCode(cCode);

			// Get Cell ID
			GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager
					.getCellLocation();

			int cid = cellLocation.getCid();
			int lac = cellLocation.getLac();
			resultObject.setCid(cid);
			resultObject.setLac(lac);

			// Get MCC and MNC
			String networkOperator = telephonyManager.getNetworkOperator();
			if (networkOperator.length() > 3)
			{
				String mcc = networkOperator.substring(0, 3);
				String mnc = networkOperator.substring(3);
				resultObject.setMcc(mcc);
				resultObject.setMnc(mnc);
			}

			// Get GPS Data
			Location location = null;
			// Get User Setting
			boolean gps = sharedPrefs.getBoolean(
					getString(R.string.privacy_gps_key), getResources()
							.getBoolean(R.bool.privacy_gps_default));

			// gps = true = Network Access Point GPS
			// gps = false = Device GPS

			// Get exact Location of the Device
			// If User enabled use of GPS of Access Point you can get Location
			// from the Information of the Access Point (Cell ID...)
			if (!gps
					&& locationManager
							.isProviderEnabled(LocationManager.GPS_PROVIDER))
			{
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 1, 1, mLocationListener);

				location = locationManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (location == null
						&& locationManager
								.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
				{
					location = locationManager
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				}

			}

			// Save Location
			if (location != null)
			{
				resultObject.setLatitude(location.getLatitude());
				resultObject.setLongitude(location.getLongitude());
			}

			boolean wifiSetting = sharedPrefs.getBoolean(
					getString(R.string.wifi_key),
					getResources().getBoolean(R.bool.wifi_default));

			// Create Execution Task
			ExecuteMethodsTask t = new ExecuteMethodsTask(this);

			// Check if wifi or mobile is connected and perform accordingly
			if (wifiSetting && wifi.isConnected())
			{
				t.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, model,
						resultObject);
			} else if (!wifiSetting
					&& (mobile.isConnected() || wifi.isConnected()))
			{
				t.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, model,
						resultObject);
			} else if (!mobile.isConnected() && !wifi.isConnected())
			{
				model.informConsoleListeners(getResources().getString(
						R.string.noconnect));
			} else if (wifiSetting
					&& (!wifi.isConnected() || mobile.isConnected()))
			{
				final Intent intent = new Intent(this, SettingsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				new AlertDialog.Builder(this)
						.setTitle("Wifi Einstellungen")
						.setMessage(
								"Kein WLAN aktiv. Messungen nur im WLAN ausführbar. Einstellungen ändern?")
						.setPositiveButton(android.R.string.yes,
								new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog,
											int which)
									{
										// Go to Settings
										startActivity(intent);
									}
								})
						.setNegativeButton(android.R.string.no,
								new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog,
											int which)
									{

									}
								}).setIcon(android.R.drawable.ic_dialog_alert)
						.show();
				return;
			}

			// Update ScrollView to jump to bottom
			final ScrollView scrollview = ((ScrollView) findViewById(R.id.scrollView));
			scrollview.post(new Runnable()
			{
				public void run()
				{
					scrollview.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});

			// Reset Button
			// Reset the Views
		} else if (v.getId() == R.id.reset_button)
		{
			console.setText("");
		} else if (v.getId() == R.id.result_button)
		{
			Intent nextScreen = new Intent(getApplicationContext(),
					ResultActivity.class);
			nextScreen.putExtra("Result", model.getResults());
			startActivity(nextScreen);
		}
	}

	public void updateConsoleView(String message)
	{
		final String m = message;
		runOnUiThread(new Runnable()
		{

			public void run()
			{
				console.setText(console.getText() + m + "\n");
			}
		});
	}

	public void updateMeasurementView()
	{
		runOnUiThread(new Runnable()
		{

			public void run()
			{
				// Update Number of Measurements
				measurements.setText(getResources().getString(
						R.string.measurements)
						+ "\n" + model.getNumberOfMeasurements() + "\n");

				// Update Measurement Period
				period.setText(getResources().getString(R.string.period)
						+ "\n"
						+ model.getResults().get(0).getDate()
						+ "\n"
						+ model.getResults().get(model.getResults().size() - 1)
								.getDate());

				// Display Last Measurement
				ResultObject last = model.getResults().get(
						model.getResults().size() - 1);
				ArrayList<DataObject> measurements = last.getObjects();
				information.setText(getResources().getString(
						R.string.last_measurement)
						+ ": " + last.getDate() + "\n");
				for (int i = 0; i < measurements.size(); i++)
				{
					information
							.setText(information.getText()
									+ DataModel.getMethodName(measurements.get(
											i).getMethod()) + ": "
									+ measurements.get(i).getAvgBandwidth()
									+ " KB/s\n");
				}

			}
		});
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key)
	{
		Toast.makeText(this, "Einstellungen wurden geändert. " + key,
				Toast.LENGTH_SHORT).show();

		// New Server Address; restart Sniffer
		if (key.equals(getString(R.string.dev_server_ip_key)))
		{
			if (process != null)
			{
				process.destroy();
				try
				{
					process = Runtime.getRuntime().exec("su");
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				model.informConsoleListeners("C Sniffer beendet.");

				new SnifferThread(model, process).start();
				model.informConsoleListeners("C Sniffer neu gestartet.");
			}
		} else if (key.equals(getString(R.string.list_data_key)))
		{
			int value = Integer.parseInt(sharedPrefs.getString(
					getString(R.string.list_data_key), "3"));
			model.informConsoleListeners("Auswahl Datenaufkommen: " + value);
		}
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
					model.setRoot(true);
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

}
