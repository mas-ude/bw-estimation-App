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

	public Double[] calculateRTT() throws IOException
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

		double rttBW = (packetSize / 1000 / delta);
		Double[] arr = { rttBW, (double) packetSize };
		return arr;
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

	public static void main(String args[]) throws IOException
	{
		// Video-Url from a Video with size of ca. 11,8 MB
		String video = "https://r6---sn-uxax4vopj5qx-q0nl.googlevideo.com/videoplayback?itag=135&signature=18D2016266EB53B296A092FC10AC3C58E56A48F2.8B07D494188B8100DE1AD4F19A6FDE7A7D00D5A3&ipbits=0&key=yt6&gir=yes&pl=23&dur=151.067&sver=3&expire=1467307384&pcm2cms=yes&initcwndbps=920000&id=o-AFZi_ywWRflbxWm06bM0UVPx0wb8bQIu74-JfGKeY9P1&upn=_Ki0oWAd1Hs&lmt=1458188492067953&ip=92.228.139.95&sparams=clen%2Cdur%2Cgir%2Cid%2Cinitcwndbps%2Cip%2Cipbits%2Citag%2Ckeepalive%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpcm2cms%2Cpl%2Crequiressl%2Csource%2Cupn%2Cexpire&fexp=9414875%2C9416126%2C9416891%2C9417701%2C9419451%2C9422596%2C9427378%2C9428398%2C9431012%2C9432373%2C9433096%2C9433223%2C9433946%2C9435526%2C9435696%2C9435706%2C9435876%2C9436811%2C9436926%2C9437066%2C9437403%2C9437553%2C9437627%2C9438660%2C9438662%2C9439471%2C9439561%2C9439652%2C9439892%2C9440436&mt=1467285487&mv=m&ms=au&source=youtube&keepalive=yes&mime=video%2Fmp4&clen=11872334&requiressl=yes&mm=31&mn=sn-uxax4vopj5qx-q0nl&cpn=J4Wvq3EkifyklHyJ&alr=yes&ratebypass=yes&c=WEB&cver=1.20160628&rn=5&rbuf=0";

		RTT rtt = new RTT("https://www.youtube.com/watch?v=fk4BbF7B29w");
		for (int i = 0; i < 5; i++)
		{
			System.out.println("Bandbreite: " + rtt.calculateRTT()
					+ " KB/s|Datenmenge: " + rtt.packetSize);
		}
	}
}
