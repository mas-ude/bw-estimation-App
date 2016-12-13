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
import com.test.app.bem.RTT;
import com.test.app.model.Bandwidths;
import com.test.app.model.DataModel;
import com.test.app.model.Results;

public class MeasurementTask extends Thread
{
	private DataModel model;
	private Results result;
	private MyService service;
	private Process process;
	private int[] messageLengths;

	public MeasurementTask(DataModel model, Results result, MyService service)
	{
		this.model = model;
		this.service = service;
		this.result = result;

		this.messageLengths = new int[] { 1400 };

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

		long end = 0, start = System.currentTimeMillis();

		Bandwidths saveObject = null;
		ArrayList<Double> bandwidths = new ArrayList<Double>(5);
		Socket socket = null;

		String serverAddress = model.getSharedPrefs().getString(
				model.getContext().getString(R.string.dev_server_ip_key),
				model.getContext().getString(R.string.dev_server_ip_default));
		try
		{

			int port = model.getSharedPrefs().getInt(
					model.getContext().getString(R.integer.port), 2600);

			int timeout = model.getSharedPrefs().getInt(
					model.getContext().getString(R.integer.timeout), 30000);

			socket = new Socket(serverAddress, port);
			// Set Timeout if Server is not able to response
			socket.setSoTimeout(timeout);

			// Get Maximum Buffer Size to set it again
			int bufferSize = socket.getReceiveBufferSize();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));

			// Variable to update Progress
			int count = 1;
			// To Save returned Values
			Double[] arr = new Double[2];
			// To Calculate Measurement Time
			long startMethod = 0, endMethod = 0;
			double usedData = 0;

			// Execute Methods and Save Data
			for (Integer method : map.keySet())
			{
				// Refresh Progress-Dialog when new Method is called
				model.updateProgress(count, map.keySet().size(),
						DataModel.getMethodName(method));
				for (int i = 0; i < map.get(method); i++)
				{
					startMethod = System.currentTimeMillis();
					if (method == DataModel.PACKETPAIR)
					{
						model.sendMessagestoBroadcast(DataModel.MESSAGE,
								"Packet Pair Messung gestartet.");
						// PP get better results if BufferSize is set to minimum
						socket.setSendBufferSize(1);
						socket.setReceiveBufferSize(1);
						for (int t = 0; t < messageLengths.length; t++)
						{
							arr = model.getMethods().packetPair(in, out,
									messageLengths[t], model);
							bandwidths.add(arr[0]);
							usedData = usedData + arr[1];
						}
						socket.setSendBufferSize(bufferSize);
						socket.setReceiveBufferSize(bufferSize);
					} else if (method == DataModel.PACKETTRAIN)
					{
						model.sendMessagestoBroadcast(DataModel.MESSAGE,
								"Packet Train Messung gestartet.");
						arr = model.getMethods().packetTrain(in, out);
						bandwidths.add(arr[0]);
						usedData = usedData + arr[1];
					} else if (method == DataModel.GPING)
					{
						model.sendMessagestoBroadcast(DataModel.MESSAGE,
								"GPing Messung gestartet.");
						arr = model.getMethods().gPing(in, out);
						bandwidths.add(arr[0]);
						usedData = usedData + arr[1];
					} else if (method == DataModel.RTT)
					{
						model.sendMessagestoBroadcast(DataModel.MESSAGE,
								"RTT Messung gestartet.");
						arr = model.getMethods().updatedRTT(DataModel.RTT, in,
								out);
						bandwidths.add(arr[0]);
						usedData = usedData + arr[1];
					} else if (method == DataModel.DOWNLOAD)
					{
						model.sendMessagestoBroadcast(DataModel.MESSAGE,
								"Download Messung gestartet.");
						arr = model.getMethods().updatedRTT(DataModel.DOWNLOAD,
								in, out);
						bandwidths.add(arr[0]);
						usedData = usedData + arr[1];
					} else if (method == DataModel.RTT_YOUTUBE)
					{
						model.sendMessagestoBroadcast(DataModel.MESSAGE,
								"RTT zu Youtube gestartet.");
						RTT rtt = new RTT("https://www.youtube.com");
						arr = rtt.calculateRTT();
						bandwidths.add(arr[0]);
						usedData = usedData + arr[1];
					} else if (method == DataModel.DOWNLOAD_YOUTUBE)
					{
						model.sendMessagestoBroadcast(DataModel.MESSAGE,
								"Download von Youtube gestartet.");
					}

					endMethod = System.currentTimeMillis();
				}

				if (!bandwidths.isEmpty())
				{
					saveObject = new Bandwidths(method, new ArrayList<Double>(
							bandwidths), usedData);
					// Set used Measurement Time
					saveObject
							.setMeasurementTime((endMethod - startMethod) / 1000.0F);
				}

				// Calculate Bandwidth from Sniffer-Informations (only if Root
				// Access is granted)
				if ((method == DataModel.PACKETPAIR || method == DataModel.GPING)
						&& model.getSharedPrefs().getBoolean("Root", false))
				{
					Bandwidths object = model.calculateTimefromPackets(method,
							usedData);
					if (object != null)
					{
						// Set Measurement Time
						object.setMeasurementTime((endMethod - startMethod) / 1000.0F);
						// Add Object to Result
						result.addObject(object);
					}
				}

				// Clear Array and Variable usedData because it is used again
				bandwidths.clear();
				usedData = 0;

				// Remove all Packets after Calculation or if another method is
				// called
				model.deletePackets();
				if (saveObject != null)
				{
					result.addObject(saveObject);
				}
				count++;
			}

			end = System.currentTimeMillis();
			result.setMeasurementTime((end - start) / 1000.0F);

			if (!result.getObjects().isEmpty())
			{
				model.addResultObject(result);
				// Save Informations as JSON in File only if App is closed
				if (!model.getSharedPrefs().getBoolean("Active", false))
				{
					model.sendMessagestoBroadcast(DataModel.MESSAGE, "Saved");
					model.saveData();
				}
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
			model.sendMessagestoBroadcast(DataModel.MESSAGE, e.getMessage());
			return;
		} catch (Exception e)
		{
			model.sendMessagestoBroadcast(DataModel.MESSAGE,
					"Exception: " + e.getMessage());
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

		return;
	}
}