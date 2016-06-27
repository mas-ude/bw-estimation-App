package com.test.app.save;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.google.gson.Gson;
import com.test.app.R;
import com.test.app.model.DataModel;

public class SendDataThread extends Thread
{
	private DataModel model;
	private ServerService service;

	public SendDataThread(DataModel model, ServerService service)
	{
		this.model = model;
		this.service = service;
	}

	@Override
	public void run()
	{
		Socket socket = null;

		String serverAddress = model.getSharedPrefs().getString(
				model.getContext().getString(R.string.dev_server_ip_key),
				model.getContext().getString(R.string.dev_server_ip_default));

		try
		{
			socket = new Socket(serverAddress, 2600);

			// Set Timeout if Server is not able to response
			socket.setSoTimeout(10000);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));

			out.write("data\n");
			out.flush();

			// Send Data in JSON-Format
			Gson gson = new Gson();

			out.write(gson.toJson(this.model.getDailyResults()));
			out.flush();
			out.write("\nEnd\n");
			out.flush();

			// Wait for answer
			String response = in.readLine();
			// React on answer
			if (response.equals("saved"))
			{
				// Delete sended Data
				this.model.sendMessagestoBroadcast(DataModel.MESSAGE,
						DataModel.DATASAVED);
				this.model.deleteSendedData();
			} else
			{
				// Problem with saving the Data on Serverside
			}

		} catch (IOException e)
		{

		} finally
		{
			// Service doesn't stay in memory so stop it immediately after
			// Datatransfer is finished
			this.service.stopSelf();

			// Close socket
			try
			{
				socket.close();
			} catch (IOException e)
			{

			}
		}
	}
}
