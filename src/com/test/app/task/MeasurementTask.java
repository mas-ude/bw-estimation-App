package com.test.app.task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import com.test.app.R;
import com.test.app.model.Bandwidths;
import com.test.app.model.DataModel;
import com.test.app.model.Results;

public class MeasurementTask extends Thread
{
	private DataModel model;
	private Results result;
	private MyService service;
	private Process process;

	public MeasurementTask(DataModel model, Results result, MyService service)
	{
		this.model = model;
		this.service = service;
		this.result = result;

		// Start Sniffer if Root Access is granted
		if (model.getSharedPrefs().getBoolean("Root", false))
		{
			try
			{
				process = Runtime.getRuntime().exec("su");
				new SnifferThread(model, process).start();
			} catch (IOException e)
			{
				model.sendMessagestoBroadcast(
						DataModel.MESSAGE,
						model.getContext().getResources()
								.getString(R.string.snifferex));
			}
		}

		this.model.sendMessagestoBroadcast(DataModel.INITPROGRESS, null);
	}

	@Override
	public void run()
	{
		// Get Settings from SharedPreferences
		HashMap<Integer, Integer> map = model.getUserSettingsForMeasurement();

		Bandwidths saveObject = null;
		ArrayList<Double> bandwidths = new ArrayList<Double>(5);
		Socket socket = null;

		String serverAddress = model.getSharedPrefs().getString(
				model.getContext().getString(R.string.dev_server_ip_key),
				model.getContext().getString(R.string.dev_server_ip_default));
		try
		{

			// TODO: GET VALUES FOR PORT AND TIMEOUT FROM MODEL (SAVE THIS
			// IN MODEL BEFORE)

			socket = new Socket(serverAddress, 2600);
			// Set Timeout if Server is not able to response
			socket.setSoTimeout(10000);

			// Get Maximum Buffer Size to set it again
			int bufferSize = socket.getReceiveBufferSize();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));

			// Variable to update Progress
			int count = 1;
			// Execute Methods and Save Data
			for (Integer method : map.keySet())
			{
				// Refresh Progress-Dialog when new Method is called
				model.updateProgress(count, map.keySet().size(),
						DataModel.getMethodName(method));
				for (int i = 0; i < map.get(method); i++)
				{
					if (method == DataModel.PACKETPAIR)
					{
						model.sendMessagestoBroadcast(DataModel.MESSAGE,
								"Packet Pair Messung gestartet.");
						// PP get better results if BufferSize is set to minimum
						socket.setSendBufferSize(1);
						socket.setReceiveBufferSize(1);
						bandwidths.add(model.getMethods().packetPair(in, out,
								model));
						socket.setSendBufferSize(bufferSize);
						socket.setReceiveBufferSize(bufferSize);
					} else if (method == DataModel.PACKETTRAIN)
					{
						model.sendMessagestoBroadcast(DataModel.MESSAGE,
								"Packet Train Messung gestartet.");
						bandwidths.add(model.getMethods().packetTrain(in, out));
					} else if (method == DataModel.GPING)
					{
						model.sendMessagestoBroadcast(DataModel.MESSAGE,
								"GPing Messung gestartet.");
						bandwidths.add(model.getMethods().gPing(in, out));
					} else if (method == DataModel.RTT)
					{
						model.sendMessagestoBroadcast(DataModel.MESSAGE,
								"RTT Messung gestartet.");
						bandwidths.add(model.getMethods().updatedRTT(
								DataModel.RTT, in, out));
					} else if (method == DataModel.DOWNLOAD)
					{
						model.sendMessagestoBroadcast(DataModel.MESSAGE,
								"Download Messung gestartet.");
						bandwidths.add(model.getMethods().updatedRTT(
								DataModel.DOWNLOAD, in, out));
					}
				}
				model.sendMessagestoBroadcast(DataModel.MESSAGE,
						"Werte für Methode " + method + " abgespeichert!");
				// model.informConsoleListeners();
				saveObject = new Bandwidths(method, new ArrayList<Double>(
						bandwidths));
				// Clear Array because it is used again
				bandwidths.clear();
				// Calculate Bandwidth from Sniffer-Informations
				if (method == DataModel.PACKETPAIR || method == DataModel.GPING)
				{
					// Short Interruption to get all Inputs from the Sniffer
					Thread.sleep(200);
					Bandwidths object = model.calculateTimefromPackets(method);
					if (object != null)
					{
						result.addObject(object);
					}
				}
				// Remove all Packets after Calculation or if another method is
				// called
				model.deletePackets();
				result.addObject(saveObject);
				count++;
			}

			model.addResultObject(result);

			// Save Informations as JSON in File only if App is closed
			if (!model.getSharedPrefs().getBoolean("Active", false))
			{
				model.saveData();
			}

			// Inform Views that Measurement is over
			model.sendResultstoBroadcast();
			// Set Value to show that a new Measurement is available
			model.getSharedPrefs().edit().putBoolean("Measurement", true)
					.apply();
		} catch (SocketException e)
		{
			model.sendMessagestoBroadcast(DataModel.MESSAGE, model.getContext()
					.getResources().getString(R.string.socketex));
			return;
		} catch (SocketTimeoutException e)
		{
			model.sendMessagestoBroadcast(DataModel.MESSAGE, model.getContext()
					.getResources().getString(R.string.sockettimeoutex));
			return;
		} catch (UnknownHostException e)
		{
			model.sendMessagestoBroadcast(DataModel.MESSAGE, model.getContext()
					.getResources().getString(R.string.unknownhostex));
			return;
		} catch (ProtocolException e)
		{
			model.sendMessagestoBroadcast(DataModel.MESSAGE, model.getContext()
					.getResources().getString(R.string.protocolex));
			return;
		} catch (IOException e)
		{
			model.sendMessagestoBroadcast(DataModel.MESSAGE, model.getContext()
					.getResources().getString(R.string.ioex));
			return;
		} catch (InterruptedException e)
		{
			return;
		} finally
		{
			// Service doesn't stay in memory so stop it immediately after
			// Measurement is finished
			this.service.stopSelf();
			// Stop Sniffer
			if (process != null)
			{
				process.destroy();
			}
			// Send Message to Close ProgressDialog
			model.sendMessagestoBroadcast(DataModel.DISMISSPROGRESS, null);

			// Close Socket
			try
			{
				if (socket != null)
				{
					socket.close();
				}
			} catch (IOException e)
			{

			}
		}
	}

}