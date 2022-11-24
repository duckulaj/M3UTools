package com.hawkins.m3utoolsjpa.dmanager.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatUtilities {
	private static SimpleDateFormat _format;
	private static final int MB = 1024 * 1024, KB = 1024;

	public static String formatDate(long date) {
		if (_format == null) {
			_format = new SimpleDateFormat("yyyy-MM-dd");
		}
		Date dt = new Date(date);
		return _format.format(dt);
	}

	public static String formatSize(double length) {
		if (length < 0)
			return "---";
		if (length > MB) {
			return String.format("%.1f MB", (float) length / MB);
		} else if (length > KB) {
			return String.format("%.1f KB", (float) length / KB);
		} else {
			return String.format("%d B", (int) length);
		}
	}

	
	public static String getETA(double length, float rate) {
		if (length == 0)
			return "00:00:00";
		if (length < 1 || rate <= 0)
			return "---";
		int sec = (int) (length / rate);
		return hms(sec);
	}

	public static String hms(int sec) {
		int hrs = 0, min = 0;
		hrs = sec / 3600;
		min = (sec % 3600) / 60;
		sec = sec % 60;
		String str = String.format("%02d:%02d:%02d", hrs, min, sec);
		return str;
	}
}
