package com.test.app.task;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.test.app.R;
import com.test.app.model.DataModel;

public class SnifferThread extends Thread
{
	private DataModel model;
	private Process process;

	public SnifferThread(DataModel model, Process process)
	{
		this.model = model;
		this.process = process;
	}

	@Override
	public void run()
	{

		final String setRights = "chmod 777 /data/data/com.test.app/cache/sniffer";
		final String startSniffer = "/data/data/com.test.app/cache/./sniffer "
				+ model.getSharedPrefs().getString(
						model.getContext()
								.getString(R.string.dev_server_ip_key),
						model.getContext().getString(
								R.string.dev_server_ip_default));

		final DataOutputStream os = new DataOutputStream(
				process.getOutputStream());
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				process.getInputStream()));
		try
		{
			os.writeBytes(setRights + "\n");
			os.flush();
			os.writeBytes(startSniffer + "\n");
			os.flush();

			String line = "";
			while ((line = reader.readLine()) != null)
			{
				model.addPacket(line);
			}

		} catch (IOException e)
		{

		}
	}
}
