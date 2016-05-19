package com.test.app.bem;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RTT
{
	private long start;
	private long end;
	private double delta;
	private int packetSize;
	private String urlToRead;
	private double bandwidth;

	public RTT(String url)
	{
		this.start = 0;
		this.end = 0;
		this.delta = 0;
		this.packetSize = 0;
		this.urlToRead = url;
		bandwidth = 0;
	}

	public double calculateRTT() throws IOException
	{
		URL url = new URL(urlToRead);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod("GET");
		conn.setUseCaches(false);
		conn.addRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/39.0");

		start = System.nanoTime();
		InputStream in = conn.getInputStream();

		while (in.read() != -1)
		{
			packetSize++;
		}
		end = System.nanoTime();
		delta = (end - start) / 1000000000.0;

		return (packetSize / 1000 / delta);
	}

	public double getBandwidth()
	{
		return bandwidth;
	}

	public void setBandwidth(double bandwidth)
	{
		this.bandwidth = bandwidth;
	}

	public long getStart()
	{
		return start;
	}

	public void setStart(long start)
	{
		this.start = start;
	}

	public long getEnd()
	{
		return end;
	}

	public void setEnd(long end)
	{
		this.end = end;
	}

	public double getDelta()
	{
		return delta;
	}

	public void setDelta(double delta)
	{
		this.delta = delta;
	}

	public int getPacketSize()
	{
		return packetSize;
	}

	public void setPacketSize(int packetSize)
	{
		this.packetSize = packetSize;
	}

}
