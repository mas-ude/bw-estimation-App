package com.test.app.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Results implements Serializable
{
	private static final long serialVersionUID = 1L;
	private ArrayList<Bandwidths> objects;
	private String date, networkType;
	private boolean wifi, mobile;
	private int cid, lac;
	private String cCode, mcc, mnc, provider;
	private double latitude, longitude;
	private float measurementTime;

	public Results(boolean wifi, boolean mobile, String date)
	{
		this.wifi = wifi;
		this.mobile = mobile;
		this.date = date;
		objects = new ArrayList<Bandwidths>(5);

		this.cid = 0;
		this.lac = 0;
		this.networkType = "";
		this.cCode = "";
		this.mcc = "";
		this.mnc = "";
		this.provider = "";
		this.latitude = 0;
		this.longitude = 0;
		this.measurementTime = 0;
	}

	public void addObject(Bandwidths object)
	{
		this.objects.add(object);
	}

	public String getDate()
	{
		return date;
	}

	public void setDate(String date)
	{
		this.date = date;
	}

	public boolean isWifi()
	{
		return wifi;
	}

	public void setWifi(boolean wifi)
	{
		this.wifi = wifi;
	}

	public boolean isMobile()
	{
		return mobile;
	}

	public void setMobile(boolean mobile)
	{
		this.mobile = mobile;
	}

	public ArrayList<Bandwidths> getObjects()
	{
		return objects;
	}

	public void setObjects(ArrayList<Bandwidths> objects)
	{
		this.objects = objects;
	}

	public int getCid()
	{
		return cid;
	}

	public void setCid(int cid)
	{
		this.cid = cid;
	}

	public int getLac()
	{
		return lac;
	}

	public void setLac(int lac)
	{
		this.lac = lac;
	}

	public String getNetworkType()
	{
		return networkType;
	}

	public void setNetworkType(String networkType)
	{
		this.networkType = networkType;
	}

	public String getcCode()
	{
		return cCode;
	}

	public void setcCode(String cCode)
	{
		this.cCode = cCode;
	}

	public String getMcc()
	{
		return mcc;
	}

	public void setMcc(String mcc)
	{
		this.mcc = mcc;
	}

	public String getMnc()
	{
		return mnc;
	}

	public void setMnc(String mnc)
	{
		this.mnc = mnc;
	}

	public String getProvider()
	{
		return provider;
	}

	public void setProvider(String provider)
	{
		this.provider = provider;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}

	public float getMeasurementTime()
	{
		return measurementTime;
	}

	public void setMeasurementTime(float measurementTime)
	{
		this.measurementTime = measurementTime;
	}

	public double getUsedData()
	{
		double usedData = 0;
		for (int i = 0; i < this.objects.size(); i++)
		{
			// Don't add used Data from the Measurements of the Sniffer because
			// these Data is already in the other Methods, because it is only
			// one Measurement for the Method with and without Sniffer
			if (this.objects.get(i).getMethod() != DataModel.PP_SNIFFER
					&& this.objects.get(i).getMethod() != DataModel.GPING_SNIFFER)
			{
				usedData = usedData + objects.get(i).getUsedData();
			}
		}
		return usedData;
	}

	public ArrayList<String> getDisplayInformation()
	{
		ArrayList<String> result = new ArrayList<String>();
		result.add("Wifi: " + this.wifi);
		result.add("Mobile: " + this.mobile);
		result.add("Provider: " + this.provider);
		result.add("Country Code: " + this.cCode);
		result.add("MCC: " + this.mcc);
		result.add("MNC: " + this.mnc);
		result.add("LAC: " + this.lac);
		result.add("CID: " + this.cid);
		result.add("GPS: " + this.latitude + "|" + this.longitude);
		result.add("Used Data: " + this.getUsedData() / 1000 + " KB");
		result.add("Time: " + this.measurementTime + " s");
		result.add("Details");

		return result;
	}
}
