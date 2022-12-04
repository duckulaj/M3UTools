package com.hawkins.M3UToolsJPA.downloaders;

import java.io.IOException;

public interface SegmentListener {
	public void chunkInitiated(String id) throws IOException;

	public void chunkFailed(String id, String reason);

	public boolean chunkComplete(String id) throws IOException;

	public void chunkUpdated(String id);

	public void synchronize();

	public AbstractChannel createChannel(Segment segment);

	public void cleanup();

	public long getSize();

	public boolean shouldCleanup();

	public int getActiveChunkCount();
	
}
