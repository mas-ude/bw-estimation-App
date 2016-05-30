package com.test.app.bem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.test.app.model.DataModel;

public class Methods
{
	// General Attributes
	// private String serverAddress;
	private int packetSize;
	private String message;
	private int timeout;

	// Packet Pair Attributes
	private long inStart, inEnd, outStart, outEnd;
	private double deltaIn, deltaOut;
	private int[] messageLengths;

	// GPing Attributes
	private int smallPacketSize, largePacketSize, gPingLoops;
	private String smallPacketMessage, largePacketMessage;

	// Packet Train Attributes
	private int numberOfPackets;
	private int ptpacketLength;
	private int ptPackets;

	// RTT Attributes
	private int bytes = 0;

	public Methods()
	{
		this.packetSize = 1400;
		this.timeout = 15000;
		char[] a = new char[packetSize];
		Arrays.fill(a, 'x');
		this.message = new String(a);
		this.message += "\n";

		// Initialize Packet Pair Attributes
		this.messageLengths = new int[] { 1400 };
		this.inStart = 0;
		this.inEnd = 0;
		this.outStart = 0;
		this.outEnd = 0;
		this.deltaIn = 0;
		this.deltaOut = 0;

		// Initialize GPing Attributes
		this.smallPacketSize = 64;
		this.largePacketSize = 1064;
		this.gPingLoops = 1;

		// Create large Packet Message
		a = new char[this.largePacketSize];
		Arrays.fill(a, 'x');
		this.largePacketMessage = new String(a);
		this.largePacketMessage += "\n";

		// Create small Packet Message
		a = new char[this.smallPacketSize];
		Arrays.fill(a, 'x');
		this.smallPacketMessage = new String(a);
		this.smallPacketMessage += "\n";

		// Initialize Packet Train Attributes
		this.numberOfPackets = 0;
		this.ptpacketLength = 1400;
		// Number of Packets in the Packet Train (%2 == 0, and
		// 10<=ptPackets<=20)
		this.ptPackets = 10;

		// Initialize RTT Attributes
		// this.urlToRead = "http://" + this.serverAddress + "/512KB";
		this.bytes = 0;
	}

	/***************************************************************************
	 * Download
	 **************************************************************************/
	public double download(String urlToRead) throws ProtocolException,
			IOException
	{
		return this.rtt(DataModel.DOWNLOAD, urlToRead);
	}

	/***************************************************************************
	 * RTT
	 **************************************************************************/
	public double rtt(int method, String urlToRead) throws ProtocolException,
			IOException
	{
		bytes = 0;
		long start = 0;
		long end = 0;

		URL url = new URL(urlToRead);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod("GET");
		conn.setUseCaches(false);
		conn.setConnectTimeout(timeout);
		conn.addRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/39.0");

		start = System.nanoTime();
		InputStream in = conn.getInputStream();

		while (in.read() != -1)
		{
			bytes++;
		}
		end = System.nanoTime();

		double rttBW = bytes / ((end - start) / 1000000000.0) / 1000;
		// model.addBandwidth(new DataObject(method, rttBW));
		return rttBW;
	}

	/***************************************************************************
	 * Packet Pair
	 **************************************************************************/
	public double packetPair(BufferedReader in, BufferedWriter out)
			throws IOException
	{
		double[] bandwidths = new double[messageLengths.length];
		for (int t = 0; t < messageLengths.length; t++)
		{
			char[] a = new char[messageLengths[t]];
			Arrays.fill(a, 'x');
			this.message = new String(a);
			this.message += "\n";

			this.outStart = System.nanoTime();
			out.write(this.message);
			out.flush();
			out.write(this.message);
			out.flush();
			this.outEnd = System.nanoTime();

			this.inStart = System.nanoTime();
			in.readLine();
			in.readLine();
			this.inEnd = System.nanoTime();

			this.deltaIn = (inEnd - inStart) / 1000000000.0;
			this.deltaOut = (outEnd - outStart) / 1000000000.0;

			double delta = Math.abs((deltaOut - deltaIn));
			double ppBandwidth = packetSize / delta / 1000;
			bandwidths[t] = ppBandwidth;
		}

		double ppBandwidth = 0;
		for (int t = 0; t < bandwidths.length; t++)
		{
			ppBandwidth = ppBandwidth + bandwidths[t];
		}

		// DataObject data = new DataObject(DataModel.PACKETPAIR, ppBandwidth);
		// model.addBandwidth(data);
		return ppBandwidth;
	}

	/***************************************************************************
	 * GPing
	 **************************************************************************/
	public double gPing(BufferedReader in, BufferedWriter out)
			throws IOException
	{
		ArrayList<Double> smallRTTs = new ArrayList<Double>();
		ArrayList<Double> largeRTTs = new ArrayList<Double>();
		double rttLarge = 0;
		double rttSmall = 0;
		for (int j = 0; j < this.gPingLoops; j++)
		{
			// Send small Packet
			outStart = System.nanoTime();
			out.write(this.smallPacketMessage);
			out.flush();
			in.readLine();
			outEnd = System.nanoTime();
			rttSmall = (outEnd - outStart) / 1000000000.0;

			// Send large Packet
			outStart = System.nanoTime();
			out.write(this.largePacketMessage);
			out.flush();
			in.readLine();
			outEnd = System.nanoTime();
			rttLarge = (outEnd - outStart) / 1000000000.0;
			largeRTTs.add(rttLarge);
			smallRTTs.add(rttSmall);

			System.out.println("Small: " + rttSmall);
			System.out.println("Large: " + rttLarge);
		}
		double gPingBandwidth = 2
				* (this.largePacketSize - this.smallPacketSize)
				/ (Math.abs((Collections.min(largeRTTs) - Collections
						.min(smallRTTs))) / 2) / 1000;

		// model.addBandwidth(new DataObject(DataModel.GPING, gPingBandwidth));
		return gPingBandwidth;
	}

	/***************************************************************************
	 * Packet Train
	 **************************************************************************/
	public double packetTrain(BufferedReader in, BufferedWriter out)
			throws IOException
	{
		out.write("PacketTrain" + this.ptPackets + this.ptpacketLength + "\n");
		out.flush();

		String message = "";
		ArrayList<Double> deltas = new ArrayList<Double>();

		double ptBandwidth = 0;
		int factor = 0;
		boolean flag = true;
		int toHighBW = 0, toLowBW = 0;
		while (flag)
		{
			for (int i = 0; i < this.ptPackets; i++)
			{
				message = in.readLine();
				if (message.length() > this.ptpacketLength)
				{
					double timestamp = Double.valueOf(message.substring(
							this.ptpacketLength, message.length()));
					deltas.add((System.nanoTime() - timestamp) / 1000000000.0);
				}
				numberOfPackets++;
			}

			for (int i = 1; i < deltas.size() - 1; i++)
			{
				if (deltas.get(i) > deltas.get(i + 1))
				{
					toLowBW++;
				} else
				{
					toHighBW++;
				}
			}

			System.out.println("ToHigh: " + toHighBW);
			System.out.println("ToLow: " + toLowBW);
			if (toHighBW == toLowBW)
			{
				flag = false;
				out.write("Bandwidth\n");
				out.flush();

				ptBandwidth = Double.parseDouble(in.readLine());
				// model.addBandwidth(new DataObject(DataModel.PACKETTRAIN,
				// ptBandwidth));
			} else
			{
				factor = 0 - (toLowBW - toHighBW);
				deltas.clear();
				out.write("Value" + factor + "\n");
				out.flush();
				toHighBW = 0;
				toLowBW = 0;
			}
		}

		return ptBandwidth;
	}

	/***************************************************************************
	 * Test Method
	 **************************************************************************/
	public double test(BufferedReader in, BufferedWriter out)
			throws IOException
	{
		ArrayList<Double> deltas = new ArrayList<Double>();
		for (int t = 0; t < 2; t++)
		{
			out.write(message);
			out.flush();

			String response = in.readLine();
			double timestamp = Double.valueOf(response.substring(packetSize,
					response.length()));
			deltas.add((System.nanoTime() - timestamp) / 1000000000.0);
		}

		System.out.println("Delta 0: " + deltas.get(0));
		System.out.println("Delta 1: " + deltas.get(1));
		double delta = deltas.get(0) - deltas.get(1);
		if (delta < 0)
		{
			delta = Math.abs(delta);
		}
		System.out.println("Delta: " + delta);
		double bandwidth = (this.packetSize / delta / 1000);
		// model.addBandwidth(new DataObject(DataModel.TEST, bandwidth));
		return bandwidth;
	}

	public String getMethodName(int method)
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
		case DataModel.TEST:
			return "Test";
		default:
			return "Unknown";
		}
	}

}
