package com.hawkins.m3utoolsjpa.exception;

public class DownloadFailureException extends Exception {

	private static final long serialVersionUID = 1L;

	public DownloadFailureException(String errorMessage) {
		super(errorMessage);
	}

	public DownloadFailureException(String errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}

	public DownloadFailureException(Throwable cause) {
		super(cause);
	}

}
