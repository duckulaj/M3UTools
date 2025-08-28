package com.hawkins.m3utoolsjpa.xtream;

import com.hawkins.m3utoolsjpa.properties.DownloadProperties;

public class XtreamCredentials implements Runnable {
	
	public static String API_URL;
	public static String USERNAME;
	public static String PASSWORD;
	
	private static XtreamCredentials thisInstance = null;
	private static DownloadProperties dp = DownloadProperties.getInstance();
	
	public XtreamCredentials() {
		XtreamCredentials.API_URL = dp.getxTreamUrl();
		XtreamCredentials.USERNAME = dp.getxTreamUser();
		XtreamCredentials.PASSWORD = dp.getxTreamPassword();
	}

	public static synchronized XtreamCredentials getInstance()
	{
	
		if (XtreamCredentials.thisInstance == null)
		{
			XtreamCredentials.thisInstance = new XtreamCredentials();
		}

		return XtreamCredentials.thisInstance;
	}
	public static String getAPI_URL() {
		return API_URL;
	}

	public static void setAPI_URL(String aPI_URL) {
		API_URL = aPI_URL;
	}

	public static String getUSERNAME() {
		return USERNAME;
	}

	public static void setUSERNAME(String uSERNAME) {
		USERNAME = uSERNAME;
	}

	public static String getPASSWORD() {
		return PASSWORD;
	}

	public static void setPASSWORD(String pASSWORD) {
		PASSWORD = pASSWORD;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}
