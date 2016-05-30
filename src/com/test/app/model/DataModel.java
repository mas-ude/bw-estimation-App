package com.test.app.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;

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

	public final static String SAVEFILE = "save";
	public final static String DATARTT = "512KB";
	public final static String DATADOWNLOAD = "10MB";
	public final static String PROTOCOL = "http://";

	private ArrayList<ResultObject> results;
	private ArrayList<DataModelListener> listeners;
	private ArrayList<Packet> packets;
	private SharedPreferences sharedPrefs;
	private Context context;
	private Methods methods;
	private boolean isRoot;

	public DataModel(SharedPreferences sharedPrefs, Context context)
	{
		this.results = new ArrayList<ResultObject>();
		this.listeners = new ArrayList<DataModelListener>();
		this.packets = new ArrayList<Packet>();
		this.sharedPrefs = sharedPrefs;
		this.context = context;
		this.methods = new Methods();
		this.isRoot = false;
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

	public void setResults(ArrayList<ResultObject> results)
	{
		this.results = results;
	}

	public ArrayList<ResultObject> getResults()
	{
		return results;
	}

	public void addResultObject(ResultObject result)
	{
		this.results.add(result);
	}

	public int getNumberOfMeasurements()
	{
		return results.size();
	}

	public long getTotalUsedData()
	{
		long usedData = 0;
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

	public boolean isRoot()
	{
		return isRoot;
	}

	public void setRoot(boolean isRoot)
	{
		this.isRoot = isRoot;
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
			this.informConsoleListeners(message);
			this.informConsoleListeners("Anzahl Pakete: " + packets.size());
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

	public DataObject calculateTimefromPackets(int method)
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
			for (int i = 0; i < sendPackets.size(); i += 2)
			{
				if (i >= sendPackets.size())
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
		DataObject data = null;
		if (method == DataModel.PACKETPAIR)
		{
			data = new DataObject(DataModel.PP_SNIFFER, bandwidths);
		} else if (method == DataModel.GPING)
		{
			data = new DataObject(DataModel.GPING_SNIFFER, bandwidths);
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
				Locale.US);
		DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM);

		// if User has enabled that time should be inexactly, than the time is
		// rounded to full hour
		if (round)
		{
			if (cal.get(Calendar.MINUTE) >= 30)
			{
				cal.set(Calendar.HOUR, (Calendar.HOUR + 1));
			}
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
		}
		return dt.format(cal.getTime());
	}
}
