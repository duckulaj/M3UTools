package com.hawkins.m3utoolsjpa.xtream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.hawkins.m3utoolsjpa.data.M3UGroup;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.redis.M3UGroupRedisService;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@org.springframework.stereotype.Component
public class XtreamCodes {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final ObjectWriter PRETTY_WRITER = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
    private static final DownloadProperties dp = DownloadProperties.getInstance();
    private static final String API_URL = dp.getxTreamUrl();
    private static final String USERNAME = dp.getxTreamUser();
    private static final String PASSWORD = dp.getxTreamPassword();
    private static final String LIVE_CATEGORIES = API_URL + "/player_api.php?username=" + USERNAME + "&password=" + PASSWORD + "&action=get_live_categories";
    private static final String MOVIE_CATEGORIES = API_URL + "/player_api.php?username=" + USERNAME + "&password=" + PASSWORD + "&action=get_vod_categories";
    private static final String SERIES_CATEGORIES = API_URL + "/player_api.php?username=" + USERNAME + "&password=" + PASSWORD + "&action=get_series_categories";

@Autowired
private M3UGroupRedisService m3UGroupRedisService;

    private static void writeJsonToFile(String json, String fileName) throws IOException {
        try {
            Files.write(Paths.get(fileName), prettyPrintJson(json).getBytes(StandardCharsets.UTF_8));
            log.info("Output written to {}", fileName);
        } catch (IOException e) {
            log.error("Failed to write JSON to file: {}", fileName, e);
            throw e;
        }
    }

    /**
     * Fetches and writes Xtream Codes items (live, movie, series) to JSON files.
     */
    public static void getXtreamCodesItems() {
        try {
            String liveList = getXtreamCodesList(API_URL, USERNAME, PASSWORD, "live");
            writeJsonToFile(liveList, "live.json");
            String movieList = getXtreamCodesList(API_URL, USERNAME, PASSWORD, "movie");
            writeJsonToFile(movieList, "movie.json");
            String seriesList = getXtreamCodesList(API_URL, USERNAME, PASSWORD, "series");
            writeJsonToFile(seriesList, "series.json");
        } catch (IOException e) {
            log.error("Error retrieving or processing Xtream Codes items: {}", e.getMessage(), e);
        }
    }

    public static String buildXtreamCodesStreamUrl(String baseUrl, String username, String password, String streamId, String type) {
        String endpoint;
        switch (type) {
            case "live":
                endpoint = String.format("%s/live/%s/%s/%s.ts", baseUrl, username, password, streamId);
                break;
            case "movie":
                endpoint = String.format("%s/movie/%s/%s/%s.mp4", baseUrl, username, password, streamId);
                break;
            case "series":
                endpoint = String.format("%s/series/%s/%s/%s.mp4", baseUrl, username, password, streamId);
                break;
            default:
                throw new IllegalArgumentException("Unknown stream type: " + type);
        }
        return endpoint;
    }

    /**
     * Generic HTTP GET for Xtream API endpoints.
     */
    private static String httpGet(String endpoint) throws IOException {
        try {
            URL url = new URI(endpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "M3UToolsJPA-Downloader/1.0");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            try (InputStream in = conn.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } finally {
                conn.disconnect();
            }
        } catch (MalformedURLException | URISyntaxException e) {
            log.error("Invalid URL: {}", endpoint, e);
            throw new IOException("Invalid URL", e);
        }
    }

    public static String getXtreamCodesList(String baseUrl, String username, String password, String type) throws IOException {
        String endpoint;
        switch (type) {
            case "live":
                endpoint = String.format("%s/player_api.php?username=%s&password=%s&action=get_live_streams", baseUrl, username, password);
                break;
            case "movie":
                endpoint = String.format("%s/player_api.php?username=%s&password=%s&action=get_vod_streams", baseUrl, username, password);
                break;
            case "series":
                endpoint = String.format("%s/player_api.php?username=%s&password=%s&action=get_series", baseUrl, username, password);
                break;
            default:
                throw new IllegalArgumentException("Unknown stream type: " + type);
        }
        return httpGet(endpoint);
    }

    public static String getLiveCategoriesJson() throws IOException {
        return httpGet(LIVE_CATEGORIES);
    }

    public static String getCategoriesJson(String xTreamType) throws IOException {
        String endpoint;
        switch (xTreamType) {
            case "live":
                endpoint = LIVE_CATEGORIES;
                break;
            case "movie":
                endpoint = MOVIE_CATEGORIES;
                break;
            case "series":
                endpoint = SERIES_CATEGORIES;
                break;
            default:
                throw new IllegalArgumentException("Unknown xTreamType: " + xTreamType);
        }
        return httpGet(endpoint);
    }

    public static List<VOD> parseMovieList(String json) throws IOException {
        return OBJECT_MAPPER.readValue(json, new TypeReference<List<VOD>>() {});
    }

    public static List<LiveStream> parseLiveStreamList(String json) throws IOException {
        return OBJECT_MAPPER.readValue(json, new TypeReference<List<LiveStream>>() {});
    }

    public static List<Series> parseSeriesList(String json) throws IOException {
        return OBJECT_MAPPER.readValue(json, new TypeReference<List<Series>>() {});
    }

    public static String prettyPrintJson(String json) {
        try {
            Object obj = OBJECT_MAPPER.readValue(json, Object.class);
            return PRETTY_WRITER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to pretty print JSON", e);
            return json;
        }
    }

    public static void createM3UItemsFromLiveList(String liveJson) {
        try {
            List<LiveStream> liveStreams = parseLiveStreamList(liveJson);
            for (LiveStream stream : liveStreams) {
                String m3uItem = buildM3UItem(stream);
                log.info(m3uItem);
            }
        } catch (IOException e) {
            log.error("Failed to parse live stream list: {}", e.getMessage(), e);
        }
    }

    private static String buildM3UItem(LiveStream stream) {
        String extinf = String.format(
                "#EXTINF:-1 tvg-id=\"%s\" tvg-name=\"%s\" tvg-logo=\"%s\" group-title=\"%s\",%s",
                safe(String.valueOf(stream.getStream_id())),
                safe(stream.getName()),
                safe(stream.getStream_icon()),
                safe(stream.getCategory_id()),
                safe(stream.getName())
        );
        String url = buildXtreamCodesStreamUrl(API_URL, USERNAME, PASSWORD, String.valueOf(stream.getStream_id()), "live");
        return extinf + "\n" + url;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    /**
     * Converts live.json, movie.json, and series.json to M3U and appends all outputs into one M3U file.
     * The output file will contain all entries from the three sources.
     */
    public void convertAllJsonToSingleM3U(File liveJsonFile, File movieJsonFile, File seriesJsonFile, File m3uOutputFile) {
        try {
            StringBuilder m3u = new StringBuilder();
            m3u.append("#EXTM3U\n");
            java.util.function.BiConsumer<File, java.util.function.Function<File, String>> appendM3U = (jsonFile, converter) -> {
                if (jsonFile != null && jsonFile.exists()) {
                    String content = converter.apply(jsonFile);
                    if (content != null && !content.isEmpty()) {
                        String noHeader = content.replaceFirst("#EXTM3U\\s*", "");
                        m3u.append(noHeader);
                    }
                }
            };
            java.util.function.Function<File, String> liveConverter = (file) -> getM3UStringFromJson(file, "live");
            java.util.function.Function<File, String> movieConverter = (file) -> getM3UStringFromJson(file, "movie");
            java.util.function.Function<File, String> seriesConverter = (file) -> getM3UStringFromJson(file, "series");
            appendM3U.accept(liveJsonFile, liveConverter);
            appendM3U.accept(movieJsonFile, movieConverter);
            appendM3U.accept(seriesJsonFile, seriesConverter);
            Utils.writeToFile(m3uOutputFile, m3u.toString());
            log.info("Combined M3U written to {}", m3uOutputFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("Error combining JSON to M3U", e);
        }
    }

    private String getM3UStringFromJson(File jsonFile, String type) {
        try {
            File tempFile = File.createTempFile("temp", ".m3u");
            tempFile.deleteOnExit();
            if ("live".equals(type)) convertLiveJsonToM3U(jsonFile, tempFile);
            else if ("movie".equals(type)) convertMovieJsonToM3U(jsonFile, tempFile);
            else if ("series".equals(type)) convertSeriesJsonToM3U(jsonFile, tempFile);
            String content = new String(Files.readAllBytes(tempFile.toPath()), StandardCharsets.UTF_8);
            tempFile.delete();
            return content;
        } catch (Exception e) {
            log.error("Error getting M3U string from {}.json", type, e);
            return "";
        }
    }

    public void convertLiveJsonToM3U(File liveJsonFile, File m3uOutputFile) {
        if (!liveJsonFile.exists()) {
            log.error("live.json file not found: {}", liveJsonFile.getAbsolutePath());
            return;
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(liveJsonFile);
            if (!root.isArray()) {
                log.error("live.json is not an array");
                return;
            }
            StringBuilder m3u = new StringBuilder();
            m3u.append("#EXTM3U\n");
            for (JsonNode channel : root) {
                String name = channel.has("name") ? channel.get("name").asText() : "";
                String stream_id = channel.has("stream_id") ? channel.get("stream_id").asText() : "";
                String url = channel.has("stream_id") ? channel.get("stream_id").asText() : "";
                String logo = channel.has("stream_icon") ? channel.get("stream_icon").asText() : "";
                String group = channel.has("category_id") ? channel.get("category_id").asText() : "";
                M3UGroup m3uGroup = m3UGroupRedisService.findById(Long.valueOf(group));
                if (m3uGroup != null) group = m3uGroup.getName();
                String streamType = channel.has("stream_type") ? channel.get("stream_type").asText() : "";
                m3u.append("#EXTINF:-1 tvg-ID=\"").append(stream_id).append("\"");
                if (!logo.isEmpty()) m3u.append(" tvg-logo=\"").append(logo).append("\"");
                if (!group.isEmpty()) m3u.append(" group-title=\"").append(group).append("\"");
                if (!streamType.isEmpty()) m3u.append(" stream-type=\"").append(streamType).append("\"");
                if (!name.isEmpty()) m3u.append(" tvg-name=\"").append(name).append("\"\n");
                if (!url.isEmpty()) m3u.append(createPlayableUrl("live", url)).append("\n");
            }
            Utils.writeToFile(m3uOutputFile, m3u.toString());
            log.info("Converted {} to {}", liveJsonFile.getName(), m3uOutputFile.getName());
        } catch (IOException e) {
            log.error("Error converting live.json to M3U", e);
        }
    }

    public void convertMovieJsonToM3U(File movieJsonFile, File m3uOutputFile) {
        if (!movieJsonFile.exists()) {
            log.error("movie.json file not found: {}", movieJsonFile.getAbsolutePath());
            return;
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(movieJsonFile);
            if (!root.isArray()) {
                log.error("movie.json is not an array");
                return;
            }
            StringBuilder m3u = new StringBuilder();
            m3u.append("#EXTM3U\n");
            for (JsonNode movie : root) {
                String name = movie.has("name") ? movie.get("name").asText() : "";
                String stream_id = movie.has("stream_id") ? movie.get("stream_id").asText() : "";
                String url = movie.has("stream_id") ? movie.get("stream_id").asText() : "";
                String logo = movie.has("stream_icon") ? movie.get("stream_icon").asText() : "";
                String group = movie.has("category_id") ? movie.get("category_id").asText() : "";
                String streamType = movie.has("stream_type") ? movie.get("stream_type").asText() : "";
                m3u.append("#EXTINF:-1 tvg-ID=\"").append(stream_id).append("\"");
                if (!logo.isEmpty()) m3u.append(" tvg-logo=\"").append(logo).append("\"");
                if (!group.isEmpty()) m3u.append(" group-title=\"").append(group).append("\"");
                if (!streamType.isEmpty()) m3u.append(" stream-type=\"").append(streamType).append("\"");
                if (!name.isEmpty()) m3u.append(" tvg-name=\"").append(name).append("\"\n");
                if (!url.isEmpty()) m3u.append(createPlayableUrl("movie", url)).append("\n");
            }
            Utils.writeToFile(m3uOutputFile, m3u.toString());
            log.info("Converted {} to {}", movieJsonFile.getName(), m3uOutputFile.getName());
        } catch (IOException e) {
            log.error("Error converting movie.json to M3U", e);
        }
    }

    public void convertSeriesJsonToM3U(File seriesJsonFile, File m3uOutputFile) {
        if (!seriesJsonFile.exists()) {
            log.error("series.json file not found: {}", seriesJsonFile.getAbsolutePath());
            return;
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(seriesJsonFile);
            if (!root.isArray()) {
                log.error("series.json is not an array");
                return;
            }
            StringBuilder m3u = new StringBuilder();
            m3u.append("#EXTM3U\n");
            for (JsonNode series : root) {
                String name = series.has("name") ? series.get("name").asText() : "";
                String stream_id = series.has("series_id") ? series.get("series_id").asText() : "";
                String url = series.has("series_id") ? series.get("series_id").asText() : "";
                String logo = series.has("cover") ? series.get("cover").asText() : "";
                String group = series.has("category_id") ? series.get("category_id").asText() : "";
                String streamType = series.has("stream_type") ? series.get("stream_type").asText() : "";
                m3u.append("#EXTINF:-1 tvg-ID=\"").append(stream_id).append("\"");
                if (!logo.isEmpty()) m3u.append(" tvg-logo=\"").append(logo).append("\"");
                if (!group.isEmpty()) m3u.append(" group-title=\"").append(group).append("\"");
                if (!streamType.isEmpty()) m3u.append(" stream-type=\"").append(streamType).append("\"");
                if (!name.isEmpty()) m3u.append(" tvg-name=\"").append(name).append("\"\n");
                if (!url.isEmpty()) m3u.append(createPlayableUrl("series", url)).append("\n");
            }
            Utils.writeToFile(m3uOutputFile, m3u.toString());
            log.info("Converted {} to {}", seriesJsonFile.getName(), m3uOutputFile.getName());
        } catch (IOException e) {
            log.error("Error converting series.json to M3U", e);
        }
    }

    private static String createPlayableUrl(String type, String streamId) {
        StringBuilder url = new StringBuilder();
        url.append(API_URL).append("/")
           .append(type).append("/")
           .append(USERNAME).append("/")
           .append(PASSWORD).append("/")
           .append(streamId);
        switch (type) {
            case "movie":
                url.append(".mp4");
                break;
            case "series":
                url.append(".mkv");
                break;
            case "live":
                url.append(".ts");
                break;
            default:
                log.warn("Unknown type for stream URL: {}", type);
                break;
        }
        return url.toString();
    }

    public static Set<M3UGroup> getGroupsFromJson(String groupJson, String type) {
        Set<M3UGroup> groups = new java.util.HashSet<>();
        try {
            Set<LiveCategory> liveCategories =  OBJECT_MAPPER.readValue(groupJson, new TypeReference<Set<LiveCategory>>() {});
            for (LiveCategory category : liveCategories) {
                M3UGroup group = new M3UGroup(category.getCategory_name(), type, category.getCategory_id());
                groups.add(group);
            }
            return groups;
        } catch (IOException e) {
            log.error("Failed to parse group JSON", e);
            return Set.of();
        }
    }
}