package com.test.app.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.test.app.R;
import com.test.app.bem.Methods;

public class DataModel
{
	public final static int PACKETPAIR = 0;
	public final static int PACKETTRAIN = 1;
	public final static int GPING = 2;
	public final static int SPRUCE = 3;
	public final static int RTT = 4;
	public final static int DOWNLOAD = 5;
	public final static int TEST = 10;
	public final static int PP_SNIFFER = 15;
	public final static int GPING_SNIFFER = 16;
	public final static int RTT_YOUTUBE = 20;
	public final static int DOWNLOAD_YOUTUBE = 21;

	public final static String SAVEFILE = "save";
	public final static String DATARTT = "512KB";
	public final static String DATADOWNLOAD = "10MB";
	public final static String PROTOCOL = "http://";
	// ID for AlarmManager PendingIntent to cancel even after reboot
	public final static int ALARMPID = 1234598760;

	// Messages for Broadcastreceiver
	public final static String BROADCASTRECEIVER = "localBroadcast";

	public final static String COMMAND = "command";
	public final static String RESULT = "result";
	public final static String MESSAGE = "message";
	public final static String INFORMATION = "information";
	public final static String ADDITIONALINFO = "addinfo";
	public final static String INITPROGRESS = "initprogress";
	public final static String PROGRESS = "progress";
	public final static String DISMISSPROGRESS = "dismissprogress";
	public final static String DIALOG = "dialog";
	public final static String WIFIDIALOG = "wifidialog";
	public final static String DATASAVED = "datasaved";

	private ArrayList<Results> results;
	private ArrayList<DataModelListener> listeners;
	private ArrayList<Packet> packets;
	private SharedPreferences sharedPrefs;
	private Context context;
	private Methods methods;

	public DataModel(SharedPreferences sharedPrefs, Context context)
	{
		this.results = new ArrayList<Results>();
		this.listeners = new ArrayList<DataModelListener>();
		this.packets = new ArrayList<Packet>();
		this.sharedPrefs = sharedPrefs;
		this.context = context;
		this.methods = new Methods();
	}

	public Methods getMethods()
	{
		return methods;
	}

	public void setMethods(Methods methods)
	{
		this.methods = methods;
	}

	public SharedPreferences getSharedPrefs()
	{
		return sharedPrefs;
	}

	public void setSharedPrefs(SharedPreferences sharedPrefs)
	{
		this.sharedPrefs = sharedPrefs;
	}

	public Context getContext()
	{
		return context;
	}

	public void setContext(Context context)
	{
		this.context = context;
	}

	public void setResults(ArrayList<Results> results)
	{
		this.results = results;
	}

	public ArrayList<Results> getResults()
	{
		return results;
	}

	public void addResultObject(Results result)
	{
		this.results.add(result);
	}

	public int getNumberOfMeasurements()
	{
		return results.size();
	}

	public double getTotalUsedData()
	{
		double usedData = 0;
		for (int i = 0; i < results.size(); i++)
		{
			usedData = usedData + results.get(i).getUsedData();
		}

		return usedData;
	}

	public void addListener(DataModelListener l)
	{
		listeners.add(l);
	}

	public void deleteListener(DataModelListener l)
	{
		listeners.remove(l);
	}

	public void addPacket(String message)
	{
		String[] parts = message.split("-");
		Packet p = null;
		if (parts.length >= 4)
		{
			p = new Packet(parts[0], parts[1],
					this.createTimestampFromString(parts[3]), parts[3],
					Integer.parseInt(parts[2]));
			this.packets.add(p);
		}

		// Show Message about sniffed Packets in Console if enabled
		if (sharedPrefs.getBoolean(
				this.getContext().getString(R.string.packets_key),
				this.getContext().getResources()
						.getBoolean(R.bool.packets_default)))
		{

			this.sendMessagestoBroadcast(DataModel.MESSAGE, message);
			this.sendMessagestoBroadcast(DataModel.MESSAGE, "Anzahl Pakete: "
					+ packets.size());
		}
	}

	public void deletePackets()
	{
		packets.clear();
	}

	public void informConsoleListeners(String message)
	{
		for (DataModelListener l : listeners)
		{
			l.updateConsoleView(message);
		}
	}

	public void informMeasurementListeners()
	{
		for (DataModelListener l : listeners)
		{
			l.updateMeasurementView();
		}
	}

	public static String getMethodName(int method)
	{
		switch (method)
		{
		case DataModel.PACKETPAIR:
			return "PacketPair";
		case DataModel.PACKETTRAIN:
			return "PacketTrain";
		case DataModel.GPING:
			return "GPing";
		case DataModel.SPRUCE:
			return "Spruce";
		case DataModel.RTT:
			return "RTT";
		case DataModel.DOWNLOAD:
			return "Download";
		case DataModel.PP_SNIFFER:
			return "PP-Sniffer";
		case DataModel.GPING_SNIFFER:
			return "Gping-Sniffer";
		case DataModel.RTT_YOUTUBE:
			return "Youtube-RTT";
		case DataModel.TEST:
			return "Test";
		default:
			return "Unknown";
		}
	}

	public Timestamp createTimestampFromString(String ts)
	{
		// Timestamp-Format = 31.01.2000,01:01:01.1247765465

		String[] datetime = ts.split(",");
		// need to escape the dot if you want to split on a literal dot
		String[] date = datetime[0].split("\\.");
		String[] time = datetime[1].split("\\:");

		Timestamp timestamp = new Timestamp();
		// Add Date
		timestamp.setYear(Integer.parseInt(date[2]));
		timestamp.setMonth(Integer.parseInt(date[1]));
		timestamp.setDay(Integer.parseInt(date[0]));

		// Add Time
		timestamp.setHour(Integer.parseInt(time[0]));
		timestamp.setMinute(Integer.parseInt(time[1]));
		timestamp.setSecond(Double.parseDouble(time[2]));

		return timestamp;
	}

	public Bandwidths calculateTimefromPackets(int method, double usedData)
	{
		if (this.packets.isEmpty())
		{
			return null;
		}

		ArrayList<Packet> sendPackets = new ArrayList<Packet>();
		ArrayList<Packet> receivedPackets = new ArrayList<Packet>();

		// Split Packets in sended and received
		for (int i = 0; i < packets.size(); i++)
		{
			if (packets
					.get(i)
					.getSource()
					.equals(this.getSharedPrefs().getString(
							this.getContext().getString(
									R.string.dev_server_ip_key),
							this.getContext().getString(
									R.string.dev_server_ip_default))))
			{
				receivedPackets.add(packets.get(i));
			} else
			{
				sendPackets.add(packets.get(i));
			}
		}

		int size = 0;
		if (sendPackets.size() > receivedPackets.size())
		{
			size = receivedPackets.size();
		} else
		{
			size = sendPackets.size();
		}

		ArrayList<Double> bandwidths = new ArrayList<Double>();
		if (method == DataModel.PACKETPAIR)
		{

			Double deltaOut, deltaIn, delta;
			for (int i = 0; i < size; i += 2)
			{
				if (i >= (size - 1))
				{
					break;
				}
				deltaOut = sendPackets.get(i + 1).getTimestamp().getSecond()
						- sendPackets.get(i).getTimestamp().getSecond();
				deltaIn = receivedPackets.get(i + 1).getTimestamp().getSecond()
						- receivedPackets.get(i).getTimestamp().getSecond();

				delta = Math.abs((deltaIn - deltaOut));
				bandwidths.add(sendPackets.get(i).getLength() / delta / 1000);
			}

		} else if (method == DataModel.GPING)
		{
			double rttSmall, rttLarge, bandwidth;
			for (int i = 0; i < size; i += 2)
			{
				if (i >= (size - 1))
				{
					break;
				}
				rttSmall = receivedPackets.get(i).getTimestamp().getSecond()
						- sendPackets.get(i).getTimestamp().getSecond();
				rttLarge = receivedPackets.get(i + 1).getTimestamp()
						.getSecond()
						- sendPackets.get(i).getTimestamp().getSecond();

				bandwidth = 2
						* (Math.abs((sendPackets.get(i + 1).getLength() - sendPackets
								.get(i).getLength())))
						/ (Math.abs((rttLarge - rttSmall))) / 1000;
				bandwidths.add(bandwidth);
			}
		}

		// Save calculated Bandwidhts in Object
		Bandwidths data = null;
		if (method == DataModel.PACKETPAIR)
		{
			data = new Bandwidths(DataModel.PP_SNIFFER, bandwidths, usedData);
		} else if (method == DataModel.GPING)
		{
			data = new Bandwidths(DataModel.GPING_SNIFFER, bandwidths, usedData);
		}
		return data;
	}

	public static ArrayList<String> convertDoubleToString(
			ArrayList<Double> input)
	{
		ArrayList<String> result = new ArrayList<String>(input.size());

		for (int i = 0; i < input.size(); i++)
		{
			result.add("" + input.get(i));
		}

		return result;
	}

	public static String getActualDate(boolean round)
	{
		// Get exakt Date
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dt = new SimpleDateFormat("d.M.yyyy,HH.mm.ss",
				Locale.GERMAN);
		DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM);

		// if User has enabled that time should be inexactly, than the time is
		// rounded to full hour
		if (round)
		{
			if (cal.get(Calendar.MINUTE) >= 30)
			{
				cal.set(Calendar.HOUR, (cal.get(Calendar.HOUR) + 1));
			}
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
		}
		return dt.format(cal.getTime());
	}

	public HashMap<Integer, Integer> getUserSettingsForMeasurement()
	{
		// Get Settings
		int round = Integer.parseInt(this.sharedPrefs.getString(
				this.context.getString(R.string.list_data_key), "3"));

		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

		if (this.sharedPrefs.getBoolean(this.context
				.getString(R.string.packetpair_key), this.context
				.getResources().getBoolean(R.bool.packetpair_default)))
		{
			map.put(DataModel.PACKETPAIR, round);
		}
		if (this.sharedPrefs.getBoolean(this.context
				.getString(R.string.packettrain_key), this.context
				.getResources().getBoolean(R.bool.packettrain_default)))
		{
			map.put(DataModel.PACKETTRAIN, round);
		}
		if (this.sharedPrefs.getBoolean(this.context
				.getString(R.string.gping_key), this.context.getResources()
				.getBoolean(R.bool.gping_default)))
		{
			map.put(DataModel.GPING, round);
		}
		if (this.sharedPrefs.getBoolean(this.context
				.getString(R.string.rtt_key), this.context.getResources()
				.getBoolean(R.bool.rtt_default)))
		{
			map.put(DataModel.RTT, round);
		}
		if (this.sharedPrefs.getBoolean(this.context
				.getString(R.string.download_key), this.context.getResources()
				.getBoolean(R.bool.download_default)))
		{
			map.put(DataModel.DOWNLOAD, 1);
		}
		if (this.sharedPrefs.getBoolean(this.context
				.getString(R.string.youtube_rtt_key), this.context
				.getResources().getBoolean(R.bool.youtube_rtt_default)))
		{
			map.put(DataModel.RTT_YOUTUBE, round);
		}
		if (this.sharedPrefs.getBoolean(
				this.context.getString(R.string.youtube_download_key),
				this.context.getResources().getBoolean(
						R.bool.youtube_download_default)))
		{
			map.put(DataModel.DOWNLOAD_YOUTUBE, 1);
		}
		return map;
	}

	public static String decodenetworktype(int networkType)
	{
		switch (networkType)
		{
		case (TelephonyManager.NETWORK_TYPE_CDMA):
			return "CDMA";
		case (TelephonyManager.NETWORK_TYPE_EDGE):
			return "EDGE";
		case (TelephonyManager.NETWORK_TYPE_GPRS):
			return "GPRS";
		case (TelephonyManager.NETWORK_TYPE_HSPA):
			return "HSPA";
		case (TelephonyManager.NETWORK_TYPE_HSDPA):
			return "HSDPA";
		case (TelephonyManager.NETWORK_TYPE_HSPAP):
			return "HSPA+";
		case (TelephonyManager.NETWORK_TYPE_HSUPA):
			return "HSUPA";
		case (TelephonyManager.NETWORK_TYPE_LTE):
			return "LTE";
		case (TelephonyManager.NETWORK_TYPE_UMTS):
			return "UMTS";
		case (TelephonyManager.NETWORK_TYPE_EVDO_0):
		case (TelephonyManager.NETWORK_TYPE_EVDO_A):
		case (TelephonyManager.NETWORK_TYPE_EVDO_B):
			return "EVDO";
		case (TelephonyManager.NETWORK_TYPE_UNKNOWN):
		default:
			return "unknown";
		}
	}

	public Results getDeviceInformations(ConnectivityManager connManager,
			NetworkInfo wifi, NetworkInfo mobile)
	{
		// Create new ResultObject with general Data
		Results resultObject = new Results(wifi.isConnected(),
				mobile.isConnected(), DataModel.getActualDate(this.sharedPrefs
						.getBoolean(
								this.context
										.getString(R.string.privacy_time_key),
								this.context.getResources().getBoolean(
										R.bool.privacy_time_default))));

		// Get Provider
		TelephonyManager telephonyManager = (TelephonyManager) this.context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String provider = telephonyManager.getNetworkOperatorName();
		resultObject.setProvider(provider);

		// Get used network type
		int networkType = telephonyManager.getNetworkType();
		resultObject.setNetworkType(DataModel.decodenetworktype(networkType));

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
		boolean gps = this.sharedPrefs.getBoolean(this.context
				.getString(R.string.privacy_gps_key), this.context
				.getResources().getBoolean(R.bool.privacy_gps_default));

		// gps = true = Network Access Point GPS
		// gps = false = Device GPS

		// Get exact Location of the Device
		// If User enabled use of GPS of Access Point you can get Location
		// from the Information of the Access Point (Cell ID...)
		LocationListener mLocationListener = new MyLocationListener();
		LocationManager locationManager = (LocationManager) this.context
				.getSystemService(Context.LOCATION_SERVICE);

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

		return resultObject;
	}

	public synchronized void saveData() throws FileNotFoundException,
			IllegalArgumentException, IOException
	{
		Gson gson = new Gson();

		// Transform Results into JSON-String
		String writeResultstoFile = gson.toJson(this.results);

		// Save all Results
		FileOutputStream fos = this.context.openFileOutput(DataModel.SAVEFILE,
				Context.MODE_PRIVATE);
		fos.write(writeResultstoFile.getBytes());
		fos.flush();
	}

	public synchronized void loadData() throws FileNotFoundException,
			IOException
	{
		FileInputStream fis;

		fis = context.openFileInput(DataModel.SAVEFILE);

		Reader reader = new InputStreamReader(fis);

		Gson gson = new Gson();

		Type collectionType = new TypeToken<ArrayList<Results>>()
		{
		}.getType();

		ArrayList<Results> results = gson.fromJson(reader, collectionType);
		if (results != null)
		{
			this.setResults(results);
		}
		fis.close();
	}

	public synchronized void deleteSendedData() throws FileNotFoundException,
			IOException
	{
		this.results.clear();

		FileOutputStream fos = this.context.openFileOutput(DataModel.SAVEFILE,
				Context.MODE_PRIVATE);

		fos.write("".getBytes());
		fos.flush();
		fos.close();
	}

	public void sendResultstoBroadcast()
	{
		Intent intent = new Intent(DataModel.BROADCASTRECEIVER);

		// Extra data
		intent.putExtra(DataModel.COMMAND, DataModel.RESULT);
		intent.putExtra(DataModel.INFORMATION, this.results);

		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	public void sendMessagestoBroadcast(String command, String message)
	{
		Intent intent = new Intent(DataModel.BROADCASTRECEIVER);

		// Extra data
		intent.putExtra(DataModel.COMMAND, command);
		intent.putExtra(DataModel.INFORMATION, message);

		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	public void updateProgress(int first, int second, String method)
	{
		Intent intent = new Intent(DataModel.BROADCASTRECEIVER);

		// Extra data
		intent.putExtra(DataModel.COMMAND, DataModel.PROGRESS);
		intent.putExtra("First", first);
		intent.putExtra("Second", second);
		intent.putExtra("Method", method);

		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
}
