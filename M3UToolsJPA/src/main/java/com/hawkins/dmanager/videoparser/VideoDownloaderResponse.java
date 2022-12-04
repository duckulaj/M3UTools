package com.hawkins.dmanager.videoparser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hawkins.M3UToolsJPA.network.http.HttpHeader;
import com.hawkins.dmanager.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VideoDownloaderResponse {
	

	public static final int DASH_HTTP = 99, HTTP = 98, HLS = 97, HDS = 96;
	private static int DASH_VIDEO_ONLY = 23, DASH_AUDIO_ONLY = 24;

	public static ArrayList<YdlVideo> parse(InputStream in) throws Exception {
		
		JsonObject obj = (JsonObject) JsonParser.parseReader(new InputStreamReader(in));
		JsonArray entries = (JsonArray) obj.get("entries");
		
		if (log.isDebugEnabled()) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			log.debug(gson.toJson(entries));
		}
		
		if (entries == null) {
			// its a playlist
			entries = new JsonArray();
			entries.add(obj);
		}
		ArrayList<YdlVideo> playList = new ArrayList<>();
		for (int i = 0; i < entries.size(); i++) {
			JsonObject jsobj = (JsonObject) entries.get(i);
			playList.add(getPlaylist(jsobj));
		}
		return playList;
	}

	public static YdlVideo getPlaylist(JsonObject obj) {

		List<YdlFormat> formatList = new ArrayList<YdlFormat>();
		YdlVideo pl = new YdlVideo();
		String protocol = (obj.get("protocol").getAsString());
		YdlFormat format = new YdlFormat();
		format.protocol = protocol;
		format.url = ((obj.get("url") == null ? ""  : obj.get("url").getAsString())); 
		format.acodec =  ((obj.get("acodec") == null ? ""  : obj.get("acodec").getAsString()));
		format.vcodec =  ((obj.get("vcodec") == null ? ""  : obj.get("vcodec").getAsString()));
		format.width =  ((obj.get("width") == null ? 0  : obj.get("width").getAsInt()));
		format.height =  ((obj.get("height") == null ? 0  : obj.get("height").getAsInt()));
		format.ext =  ((obj.get("ext") == null ? ""  : obj.get("ext").getAsString()));
		format.formatNote =  ((obj.get("format_note") == null ? ""  : obj.get("format_note").getAsString()));
		format.format =  ((obj.get("format") == null ? "" : obj.get("format").getAsString()));
		JsonObject jsHeaders = (JsonObject) obj.get("http_headers");
		if (jsHeaders != null) {
			format.headers = new ArrayList<>();
			Iterator<String> headerIter = jsHeaders.keySet().iterator();
			while (headerIter.hasNext()) {
				String key = headerIter.next();
				String value = jsHeaders.get(key).getAsString();
				format.headers.add(new HttpHeader(key, value));
			}
		}
		if (protocol.equals("http_dash_segments")) {
			String baseUrl = obj.get("fragment_base_url").getAsString();
			JsonArray fragmentArr = (JsonArray) obj.get("fragments");
			
			if (log.isDebugEnabled()) {
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				log.debug(gson.toJson(fragmentArr));
			}
			
			String[] fragments = new String[fragmentArr.size()];
			for (int j = 0; j < fragmentArr.size(); j++) {
				JsonObject frag = (JsonObject) fragmentArr.get(j);
				String url = frag.get("url").getAsString();
				if (url != null) {
					fragments[j] = url;
				} else {
					fragments[j] = baseUrl + frag.get("path").getAsString();
				}
			}
		}
		formatList.add(format);

		System.out.println(formatList.size());

		ArrayList<YdlMediaFormat> mediaList = new ArrayList<>();

		// System.out.println("fmt[" + i + "]: " + fmt.formatNote);
		int type = getVideoType(format);
		// System.out.println(fmt.acodec + " " + fmt.vcodec + " " +
		// fmt.formatNote + " " + type);

		if (type == DASH_VIDEO_ONLY) {
			// ((fmt.formatNote + "").toLowerCase().contains("dash video"))
			// {
			for (int j = 0; j < formatList.size(); j++) {
				formatList.get(j);
				YdlMediaFormat media = new YdlMediaFormat();
				media.type = DASH_HTTP;
				if (type != DASH_AUDIO_ONLY) {

					if ("m3u8".equals(format.protocol) || "m3u8_native".equals(format.protocol)) {
						media.type = HLS;
					} else if ("f4m".equals(format.protocol)) {
						media.type = HDS;
					} else if ("http".equals(format.protocol) || "https".equals(format.protocol)) {
						media.type = HTTP;
					} else {
						log.info("unsupported protocol: " + format.protocol);
						continue;
					}
					media.ext = format.ext;
					media.width = format.width;
					media.format = createFormat(media.ext, format.format, null, format.acodec, format.vcodec,
							format.width, format.height);
					// media.format = "[" + (media.ext + "]").toUpperCase() + " " +
					// " " + format.format
					// + " " + " (" + format.vcodec
					// + "+" + format.acodec + ") " + format.protocol;
					if (format.headers != null) {
						media.headers.addAll(format.headers);
					}

					mediaList.add(media);
				}

				System.out.println("VIDEO----" + obj.get("title"));
				for (int i = 0; i < mediaList.size(); i++) {
					System.out.println(mediaList.get(i).type + " " + mediaList.get(i).format);
				}

				pl.mediaFormats.addAll(mediaList);
				Collections.sort(pl.mediaFormats, new Comparator<YdlMediaFormat>() {

					@Override
					public int compare(YdlMediaFormat o1, YdlMediaFormat o2) {
						if (o1.width > o2.width) {
							return -1;
						}
						if (o1.width < o2.width) {
							return 1;
						}
						return 0;
					}
				});
				String stitle = obj.get("title").getAsString();
				if (!StringUtils.isNullOrEmptyOrBlank(stitle)) {
					pl.title = stitle;
				}
			}

		}
		return pl;
	}

	private static int getVideoType(YdlFormat fmt) {

		String fmtNote = null;
		String acodec = null;
		String vcodec = null;
		if (fmt.formatNote != null) {
			fmtNote = fmt.formatNote.toLowerCase();
			if (fmtNote.equals("none") || fmtNote.length() < 1) {
				fmtNote = null;
			}
		}
		if (fmtNote == null) {
			fmtNote = "";
		}
		if (fmt.acodec != null) {
			acodec = fmt.acodec.toLowerCase();
			if (acodec.equals("none") || acodec.length() < 1) {
				acodec = null;
			}
		}
		if (fmt.vcodec != null) {
			vcodec = fmt.vcodec.toLowerCase();
			if (vcodec.equals("none") || vcodec.length() < 1) {
				vcodec = null;
			}
		}

		if (fmtNote.contains("dash audio")) {
			return DASH_AUDIO_ONLY;
		}
		if (fmtNote.contains("dash video")) {
			return DASH_VIDEO_ONLY;
		}
		if (acodec == null && vcodec == null) {
			return -1;
		}
		if (acodec != null && vcodec != null) {
			return -1;
		}
		if (acodec != null && vcodec == null) {
			return DASH_AUDIO_ONLY;
		}
		if (vcodec != null && acodec == null) {
			return DASH_VIDEO_ONLY;
		}
		return -1;
	}

	/*
	 * private static int getInt(Object obj) { if (obj == null) { return -1; } if
	 * (obj.toString().contains("none")) return -1; return Integer.parseInt(obj +
	 * ""); }
	 * 
	 * private static String getString(Object obj) { return (String) obj; }
	 */
	public static class YdlVideo {
		public String title;
		public ArrayList<YdlMediaFormat> mediaFormats = new ArrayList<>();
		public int index;
	}

	public static String nvl(String str) {
		if (str == null)
			return "";
		return str;
	}

	public static String createFormat(String ext, String fmt1, String fmt2, String acodec, String vcodec, int width,
			int height) {
		StringBuffer sb = new StringBuffer();
		ext = nvl(ext);
		if (ext.length() > 0) {
			sb.append(ext.toUpperCase());
		}

		if (width > 0 && height > 0) {
			if (sb.length() > 0)
				sb.append(" ");
			sb.append(width + "x" + height);
		}

		// fmt1 = nvl(fmt1);
		// if (fmt1.length() > 0) {
		// if (sb.length() > 0)
		// sb.append(" ");
		// sb.append(fmt1);
		// }
		//
		// fmt2 = nvl(fmt2);
		// if (fmt2.length() > 0) {
		// if (sb.length() > 0)
		// sb.append(" ");
		// sb.append(fmt2);
		// }

		acodec = nvl(acodec);
		if (acodec.contains("none")) {
			acodec = "";
		}

		vcodec = nvl(vcodec);
		if (vcodec.contains("none")) {
			vcodec = "";
		}

		if (acodec.length() > 0) {
			if (sb.length() > 0)
				sb.append(" ");
			sb.append(acodec);
		}

		if (vcodec.length() > 0) {
			if (sb.length() > 0) {
				if (acodec.length() > 0) {
					sb.append("+");
				} else {
					sb.append(" ");
				}
			}
			sb.append(vcodec);
		}

		return sb.toString();
	}

	private static class YdlMediaFormat {
		public int type;
		public String format;
		public String ext;
		public ArrayList<HttpHeader> headers = new ArrayList<>();
		@SuppressWarnings("unused")
		public ArrayList<HttpHeader> headers2 = new ArrayList<>();
		public int width;

		@Override
		public String toString() {
			return format;
		}
	}

	private static class YdlFormat {
		@SuppressWarnings("unused")
		String url;
		String format;
		String formatNote;
		int width, height;
		String protocol;
		String ext;
		String acodec;
		String vcodec;
		ArrayList<HttpHeader> headers;
	}
}
