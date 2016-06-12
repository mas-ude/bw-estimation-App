package com.test.app.model;

public class Packet
{
	private String source, destination, timestampString;
	private Timestamp timestamp;
	private int length;

	public Packet()
	{
		this("", "", new Timestamp(), "", 0);
	}

	public Packet(String source, String destination, Timestamp timestamp,
			String timestampString, int length)
	{
		this.source = source;
		this.destination = destination;
		this.timestamp = timestamp;
		this.timestampString = timestampString;
		this.length = length;
	}

	public String getSource()
	{
		return source;
	}

	public void setSource(String source)
	{
		this.source = source;
	}

	public String getDestination()
	{
		return destination;
	}

	public void setDestination(String destination)
	{
		this.destination = destination;
	}

	public int getLength()
	{
		return length;
	}

	public void setLength(int length)
	{
		this.length = length;
	}

	public String getTimestampString()
	{
		return timestampString;
	}

	public void setTimestampString(String timestampString)
	{
		this.timestampString = timestampString;
	}

	public Timestamp getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp)
	{
		this.timestamp = timestamp;
	}

}
