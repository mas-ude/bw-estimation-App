package com.test.app.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class ResultObject implements Serializable
{
	private static final long serialVersionUID = 1L;
	private ArrayList<DataObject> objects;
	private String date;
	private boolean wifi, mobile;
	private int cid, lac;
	private String cCode, mcc, mnc, provider;
	private double latitude, longitude;
	private long usedData;

	public ResultObject(boolean wifi, boolean mobile, String date)
	{
		this.wifi = wifi;
		this.mobile = mobile;
		this.date = date;
		objects = new ArrayList<DataObject>(10);

		this.cid = 0;
		this.lac = 0;
		this.cCode = "";
		this.mcc = "";
		this.mnc = "";
		this.provider = "";
		this.latitude = 0;
		this.longitude = 0;
		this.usedData = 0;
	}

	public void addObject(DataObject object)
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

	public ArrayList<DataObject> getObjects()
	{
		return objects;
	}

	public void setObjects(ArrayList<DataObject> objects)
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

	public JSONObject getJSON() throws JSONException
	{
		JSONObject result = new JSONObject();

		result.put("Wifi", this.wifi);
		result.put("Mobile", this.mobile);
		result.put("Provider", this.provider);
		result.put("Country Code", this.cCode);
		result.put("MCC", this.mcc);
		result.put("MNC", this.mnc);
		result.put("LAC", this.lac);
		result.put("CID", this.cid);
		result.put("Latitude", this.latitude);
		result.put("Longitude", this.longitude);

		// Add dataObjects to JSONObject
		ArrayList<JSONObject> dataObjects = new ArrayList<JSONObject>();

		for (int i = 0; i < this.objects.size(); i++)
		{
			JSONObject methodObject = new JSONObject();

			methodObject.put("Method", this.objects.get(i).getMethod());
			methodObject.put("AvgBandwidth", this.objects.get(i)
					.getAvgBandwidth());

			ArrayList<Double> bandwidths = new ArrayList<Double>();
			for (int k = 0; k < this.objects.get(i).getBandwidths().size(); k++)
			{
				bandwidths.add(this.objects.get(i).getBandwidths().get(k));
			}

			methodObject.put("Bandwidths", bandwidths);
			dataObjects.add(methodObject);
		}

		result.put("Objects", dataObjects);

		return result;
	}
}
