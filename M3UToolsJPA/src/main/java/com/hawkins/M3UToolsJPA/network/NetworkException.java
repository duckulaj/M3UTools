package com.hawkins.M3UToolsJPA.network;

import java.io.IOException;

@SuppressWarnings("serial")
public class NetworkException extends IOException {
	public NetworkException(String msg) {
		super(msg);
	}
}