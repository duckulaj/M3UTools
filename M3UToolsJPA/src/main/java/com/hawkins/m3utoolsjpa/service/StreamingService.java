package com.hawkins.m3utoolsjpa.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.stereotype.Service;

@Service
public class StreamingService {

	private static final long ChunkSize = 10000L;

	public byte[] getVideo(String streamUrl, String range, long fileSize) {
		byte[] data = null;

		String[] ranges = range.split("-");
		Long rangeStart = Long.parseLong(ranges[0].substring(6));
		Long rangeEnd = fileSize;
		if (ranges.length > 1) {
			rangeEnd = Long.parseLong(ranges[1]);
		} else {
			rangeEnd = fileSize - 1;
		}
		if (fileSize < rangeEnd) {
			rangeEnd = fileSize - 1;
		}
		String contentLength = String.valueOf((rangeEnd - rangeStart) + 1);
		try {
			data = readByteRange(streamUrl, rangeStart, rangeEnd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return  data;
	}

	public byte[] readByteRange(String streamUrl, long start, long end) throws IOException, URISyntaxException {
		try (InputStream inputStream = (new URI(streamUrl).toURL().openStream());
				ByteArrayOutputStream bufferedOutputStream = new ByteArrayOutputStream()) {
			byte[] data = new byte[128];
			int nRead;
			while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
				bufferedOutputStream.write(data, 0, nRead);
			}
			bufferedOutputStream.flush();
			byte[] result = new byte[(int) (end - start) + 1];
			System.arraycopy(bufferedOutputStream.toByteArray(), (int) start, result, 0, result.length);
			return result;
		}
	}

	public String getRangeStart(String rangeHeader) {

		String rangeStart = null;

		if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
			String[] rangeParts = rangeHeader.substring(6).split("-");
			rangeStart = rangeParts[0];
			// Now you have the range start
			System.out.println("Range start: " + rangeStart);
		} else {
			System.out.println("Range header not found or invalid.");
		}

		return rangeStart;
	}

	public String getRangeEnd(String rangeHeader) {

		String rangeEnd = String.valueOf(ChunkSize);

		if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
			String[] rangeParts = rangeHeader.substring(6).split("-");
			if (rangeParts.length > 1) {
				rangeEnd = rangeParts[1];
				// Now you have the range start
				System.out.println("Range end: " + rangeEnd);
			}
		} else {
			System.out.println("Range header not found or invalid.");
		}

		return rangeEnd;
	}

}