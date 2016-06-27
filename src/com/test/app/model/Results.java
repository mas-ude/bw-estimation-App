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
	private long usedData;

	public Results(boolean wifi, boolean mobile, String date)
	{
		this.wifi = wifi;
		this.mobile = mobile;
		this.date = date;
		objects = new ArrayList<Bandwidths>(10);

		this.cid = 0;
		this.lac = 0;
		this.networkType = "";
		this.cCode = "";
		this.mcc = "";
		this.mnc = "";
		this.provider = "";
		this.latitude = 0;
		this.longitude = 0;
		this.usedData = 0;
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

	public long getUsedData()
	{
		return usedData;
	}

	public void setUsedData(long usedData)
	{
		this.usedData = usedData;
	}

	public void addUsedData(long data)
	{
		this.usedData = this.usedData + data;
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
		result.add("Details Messergebnisse");

		return result;
	}
}
