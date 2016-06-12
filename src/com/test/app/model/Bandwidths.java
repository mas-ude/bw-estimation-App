package com.test.app.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Bandwidths implements Serializable
{

	private static final long serialVersionUID = 1L;
	private int method;
	private ArrayList<Double> bandwidths;

	public Bandwidths(int method, ArrayList<Double> bandwidths)
	{
		this.method = method;
		this.bandwidths = bandwidths;
	}

	public int getMethod()
	{
		return method;
	}

	public double getAvgBandwidth()
	{
		double avgBandwidth = 0;
		for (int i = 0; i < bandwidths.size(); i++)
		{
			avgBandwidth = avgBandwidth + bandwidths.get(i);
		}

		avgBandwidth = avgBandwidth / bandwidths.size();
		return avgBandwidth;
	}

	public void addBandwidth(double b)
	{
		this.bandwidths.add(b);
	}

	public void deleteBandwidth(double b)
	{
		this.bandwidths.add(b);
	}

	public ArrayList<Double> getBandwidths()
	{
		return bandwidths;
	}

	public void setBandwidths(ArrayList<Double> bandwidths)
	{
		this.bandwidths = bandwidths;
	}

	public ArrayList<String> getDisplayInformation()
	{
		ArrayList<String> result = new ArrayList<String>(bandwidths.size());

		for (int i = 0; i < bandwidths.size(); i++)
		{
			result.add("" + bandwidths.get(i) + " KB/s");
		}

		return result;
	}

}
