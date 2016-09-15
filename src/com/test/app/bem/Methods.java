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

	public Double[] updatedRTT(int method, BufferedReader in, BufferedWriter out)
			throws IOException
	{
		long start = 0;
		long end = 0;
		int bytes = 0;
		if (method == DataModel.RTT)
		{
			bytes = 256000;
			out.write("RTT\n");
			out.flush();
		} else if (method == DataModel.DOWNLOAD)
		{
			bytes = 10240000;
			out.write("Download\n");
			out.flush();
		} else
		{
			Double[] t = { 0.0, 0.0 };
			return t;
		}

		String thisLine = "";
		start = System.nanoTime();
		while ((thisLine = in.readLine()) != null)
		{
			if (thisLine.equals("End"))
			{
				end = System.nanoTime();
				break;
			}
		}
		double rttBW = bytes / ((end - start) / 1000000000.0) / 1000;
		Double[] arr = { rttBW, (double) bytes };
		return arr;
	}

	/***************************************************************************
	 * Packet Pair
	 **************************************************************************/
	public Double[] packetPair(BufferedReader in, BufferedWriter out,
			int packetSize, DataModel model) throws IOException
	{
		char[] a = new char[packetSize];
		Arrays.fill(a, 'x');
		this.message = new String(a);
		this.message += "\n";

		out.write(this.message);
		out.flush();
		this.outStart = System.nanoTime();
		out.write(this.message);
		out.flush();
		this.outEnd = System.nanoTime();

		in.readLine();
		this.inStart = System.nanoTime();
		in.readLine();
		this.inEnd = System.nanoTime();

		this.deltaIn = (inEnd - inStart) / 1000000000.0;
		this.deltaOut = (outEnd - outStart) / 1000000000.0;

		model.sendMessagestoBroadcast(DataModel.MESSAGE, "DeltaOut: "
				+ deltaOut);
		model.sendMessagestoBroadcast(DataModel.MESSAGE, "DeltaIn: " + deltaIn);

		double delta = Math.abs((deltaOut - deltaIn));
		double ppBandwidth = packetSize / delta / 1000;
		// 2 Packets back-to-back (2*2) * Message-Length + 4 * 54 (Packet
		// Headers)
		int bytes = 4 * packetSize + 4 * 54;
		// DataObject data = new DataObject(DataModel.PACKETPAIR, ppBandwidth);
		// model.addBandwidth(data);
		Double[] arr = { ppBandwidth, (double) bytes };
		return arr;
	}

	/***************************************************************************
	 * GPing
	 **************************************************************************/
	public Double[] gPing(BufferedReader in, BufferedWriter out)
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

		// GPingLoops * (2 small Packets + 2 large Packets + 4(2*2) * 54 (Packet
		// Headers))
		int bytes = this.gPingLoops
				* (2 * smallPacketSize + 2 * largePacketSize + 4 * 54);
		Double[] arr = { gPingBandwidth, (double) bytes };
		return arr;
	}

	/***************************************************************************
	 * Packet Train
	 **************************************************************************/
	public Double[] packetTrain(BufferedReader in, BufferedWriter out)
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
				if (message == null)
				{
					continue;
				}
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

		// 65 Byte Message PacketTrain + Number of Packets in all
		// PacketTrains + 63 Byte Message Bandwidth + ~74 Byte Response
		int bytes = 65 + this.numberOfPackets * 1454 + 63 + 74;
		Double[] arr = { ptBandwidth, (double) bytes };

		return arr;
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
