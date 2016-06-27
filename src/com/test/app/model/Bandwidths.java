package com.test.app.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Bandwidths implements Serializable
{

	private static final long serialVersionUID = 1L;
	private int method;
	private ArrayList<Double> bandwidths;
	private double avgBandwidth;

	public Bandwidths(int method, ArrayList<Double> bandwidths)
	{
		this.method = method;
		this.bandwidths = bandwidths;
		this.calculateAvgBandwidth();
	}

	public int getMethod()
	{
		return method;
	}

	public double getAvgBandwidth()
	{
		return this.avgBandwidth;
	}

	public double getStandardDeviation()
	{
		double result = 0;
		for (int i = 0; i < this.bandwidths.size(); i++)
		{
			result = result
					+ Math.pow((bandwidths.get(i) - this.getAvgBandwidth()), 2);
		}

		result = result / bandwidths.size();

		return Math.sqrt(result);
	}

	public void addBandwidth(double b)
	{
		this.bandwidths.add(b);
		this.calculateAvgBandwidth();
	}

	public void deleteBandwidth(double b)
	{
		this.bandwidths.remove(b);
	}

	public ArrayList<Double> getBandwidths()
	{
		return bandwidths;
	}

	public void setBandwidths(ArrayList<Double> bandwidths)
	{
		this.bandwidths = bandwidths;
		this.calculateAvgBandwidth();
	}

	public void calculateAvgBandwidth()
	{
		double avgBW = 0;
		for (int i = 0; i < bandwidths.size(); i++)
		{
			avgBW = avgBW + bandwidths.get(i);
		}

		this.avgBandwidth = (avgBW = avgBW / bandwidths.size());
	}

	public ArrayList<String> getDisplayInformation()
	{
		ArrayList<String> result = new ArrayList<String>(bandwidths.size() + 1);

		result.add("Standardabweichung: " + this.getStandardDeviation());
		for (int i = 0; i < bandwidths.size(); i++)
		{
			result.add("" + bandwidths.get(i) + " KB/s");
		}

		return result;
	}

}
