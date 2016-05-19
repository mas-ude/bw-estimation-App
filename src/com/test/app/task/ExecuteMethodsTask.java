package com.test.app.task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.test.app.MainActivity;
import com.test.app.R;
import com.test.app.model.DataModel;
import com.test.app.model.DataObject;
import com.test.app.model.ResultObject;

public class ExecuteMethodsTask extends AsyncTask<Object, Integer, Void>
{
	private DataModel model;
	private MainActivity activity;
	private ProgressDialog progDailog;

	public ExecuteMethodsTask(MainActivity a)
	{
		this.activity = a;
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		progDailog = new ProgressDialog(this.activity);
		progDailog.setMessage("Execute...");
		progDailog.setIndeterminate(false);
		progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDailog.setCancelable(true);
		progDailog.show();
	}

	@Override
	protected Void doInBackground(Object... params)
	{
		this.model = (DataModel) params[0];
		ResultObject result = (ResultObject) params[1];

		// Get Settings
		int round = Integer.parseInt(model.getSharedPrefs().getString(
				model.getContext().getString(R.string.list_data_key), "3"));

		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

		if (model.getSharedPrefs().getBoolean(
				model.getContext().getString(R.string.packetpair_key),
				model.getContext().getResources()
						.getBoolean(R.bool.packetpair_default)))
		{
			map.put(DataModel.PACKETPAIR, round);
		}
		if (model.getSharedPrefs().getBoolean(
				model.getContext().getString(R.string.packettrain_key),
				model.getContext().getResources()
						.getBoolean(R.bool.packettrain_default)))
		{
			map.put(DataModel.PACKETTRAIN, round);
		}
		if (model.getSharedPrefs().getBoolean(
				model.getContext().getString(R.string.gping_key),
				model.getContext().getResources()
						.getBoolean(R.bool.gping_default)))
		{
			map.put(DataModel.GPING, round);
		}
		if (model.getSharedPrefs().getBoolean(
				model.getContext().getString(R.string.rtt_key),
				model.getContext().getResources()
						.getBoolean(R.bool.rtt_default)))
		{
			map.put(DataModel.RTT, round);
		}
		if (model.getSharedPrefs().getBoolean(
				model.getContext().getString(R.string.download_key),
				model.getContext().getResources()
						.getBoolean(R.bool.download_default)))
		{
			map.put(DataModel.DOWNLOAD, 1);
		}

		DataObject saveObject = null;
		ArrayList<Double> bandwidths = new ArrayList<Double>(5);
		Socket socket = new Socket();
		try
		{

			// TODO: GET VALUES FOR PORT AND TIMEOUT FROM MODEL (SAVE THIS
			// IN MODEL BEFORE)

			String serverAddress = model.getSharedPrefs().getString(
					model.getContext().getString(R.string.dev_server_ip_key),
					model.getContext()
							.getString(R.string.dev_server_ip_default));

			socket = new Socket(serverAddress, 2600);

			// Set Timeout if Server is not able to response
			socket.setSoTimeout(10000);

			socket.setSendBufferSize(1);
			socket.setReceiveBufferSize(1);

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
				publishProgress(count, map.keySet().size());
				for (int i = 0; i < map.get(method); i++)
				{
					if (method == DataModel.PACKETPAIR)
					{
						model.informConsoleListeners("Packet Pair Messung gestartet.");
						bandwidths.add(model.getMethods().packetPair(in, out));
					} else if (method == DataModel.PACKETTRAIN)
					{
						model.informConsoleListeners("Packet Train Messung gestartet.");
						bandwidths.add(model.getMethods().packetTrain(in, out));
					} else if (method == DataModel.GPING)
					{
						model.informConsoleListeners("GPing Messung gestartet.");
						bandwidths.add(model.getMethods().gPing(in, out));
					} else if (method == DataModel.TEST)
					{
						// this.test(in, out);
					}
				}
				model.informConsoleListeners("Werte für Methode " + method
						+ " abgespeichert!");
				saveObject = new DataObject(method, new ArrayList<Double>(
						bandwidths));
				// Clear Array because it is used again
				bandwidths.clear();
				// Short Interruption to get all Inputs from the Sniffer
				Thread.sleep(200);
				// Calculate Bandwidth from Sniffer-Informations
				if (method == DataModel.PACKETPAIR || method == DataModel.GPING)
				{
					DataObject object = model.calculateTimefromPackets(method);
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

			// Save Object in File
			FileOutputStream fos = model.getContext().openFileOutput(
					DataModel.SAVEFILE, Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(model.getResults());
			os.close();
			fos.close();

			// Inform Listeners that Measurement is over
			model.informMeasurementListeners("Messung beendet.");

		} catch (SocketException e)
		{
			System.err
					.println("SocketException: Keine Internetverbindung verfügbar.");
			model.informConsoleListeners("SocketException: Keine Internetverbindung verfügbar.");
		} catch (SocketTimeoutException e)
		{
			System.err.print("SocketTimeoutException: Server antwortet nicht.");
			model.informConsoleListeners("SocketTimeoutException: Server antwortet nicht.");
			System.err.println(e.getMessage());
		} catch (UnknownHostException e)
		{
			System.err
					.print("UnknowHostException: Client kann die Verbindung zum Server nicht aufbauen.");
			model.informConsoleListeners("UnknowHostException: Client kann die Verbindung zum Server nicht aufbauen.");
			System.out.println(e.getMessage());
		} catch (IOException e)
		{
			System.err.print("IOException: Genereller Eingabe-/Ausgabefehler.");
			model.informConsoleListeners("IOException: Genereller Eingabe-/Ausgabefehler.");
			System.out.println(e.getMessage());
		} catch (InterruptedException e)
		{
			// e.printStackTrace();
		} finally
		{
			try
			{
				socket.close();
			} catch (IOException e)
			{

			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values)
	{

		// Auf dem Bildschirm geben wir eine Statusmeldung aus, immer wenn
		// publishProgress(int...) in doInBackground(String...) aufgerufen wird
		progDailog
				.setMessage("Execute Method " + values[0] + "of " + values[1]);

	}

	@Override
	protected void onPostExecute(Void v)
	{
		super.onPostExecute(v);
		progDailog.dismiss();
	}

}
