package de.qStivi.apis;

import de.qStivi.Config;
import de.qStivi.Util;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YouTube {

    private static final String SECRET = Config.get("YOUTUBE_KEY");
    private static final Logger LOGGER = LoggerFactory.getLogger(YouTube.class);

    /**
     * Gets the id of the first video found given a specific search query.
     *
     * @param searchQuery something you want to search on YouTube
     * @return the video id
     */
    @Nullable
    public static String getVideoIdBySearchQuery(String searchQuery) {
        if (searchQuery == null || searchQuery.isEmpty() || searchQuery.equals(" ")) {
            LOGGER.error("YouTube search query null or empty!");
            return null;
        }
        searchQuery = Util.cleanForURL(searchQuery);
        String query = "https://youtube.googleapis.com/youtube/v3/search?part=id&maxResults=1&q=" + searchQuery + "&safeSearch=none&type=video&key=" + SECRET;
        JSONObject jsonObject = readJsonFromUrl(query);
        if (jsonObject == null) {
            LOGGER.error("Error while reading jason file!");
            return null;
        }
        String id = jsonObject.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");
        LOGGER.info(id);
        return id;
    }

    /**
     * Gets all video IDs in a playlist.
     *
     * @param link like of the playlist
     * @return {@link List} of video IDs
     */
    @Nullable
    public static List<String> getPlaylistItemsByLink(String link) {
        try {
            String[] strings = link.split("list=");
            String id = strings[1];
            String query = "https://youtube.googleapis.com/youtube/v3/playlistItems?part=contentDetails&maxResults=50&playlistId=" + id + "&key=" + SECRET;
            JSONObject jsonObject = readJsonFromUrl(query);
            if (jsonObject == null) {
                LOGGER.error("Error while reading jason file!");
                return null;
            }
            JSONArray items = jsonObject.getJSONArray("items");
            List<String> videoIds = new ArrayList<>();
            for (int i = 0; i < items.length(); i++) {
                videoIds.add(items.getJSONObject(i).getJSONObject("contentDetails").getString("videoId"));
            }
            if (videoIds.isEmpty()) {
                LOGGER.error("Playlist is empty!");
                return null;
            }
            LOGGER.info(String.valueOf(videoIds));
            return videoIds;
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            LOGGER.error(Arrays.deepToString(e.getStackTrace()));
            return null;
        }
    }

    /**
     * Gets the json object provided by the link.
     *
     * @param url link to json object
     * @return {@link JSONObject}
     */
    @Nullable
    public static JSONObject readJsonFromUrl(String url) {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            String jsonText = sb.toString();
            rd.close();
            return new JSONObject(jsonText);
        } catch (IOException e) {
            LOGGER.error(Arrays.deepToString(e.getStackTrace()));
        }
        return null;
    }
}
