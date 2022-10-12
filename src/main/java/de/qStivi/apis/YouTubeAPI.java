package de.qStivi.apis;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import de.qStivi.Config;
import de.qStivi.NoResultsException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class YouTubeAPI {

    private static final YouTube API;

    // region Static Initializer
    // Using null for the httpRequestInitializer in the YouTube.Builder()
    // and setting the GoogleClientRequestInitializer to a YouTubeRequestInitializer with an API key
    // allows to use the API for global data without needing to authenticate a user via OAuth2 everytime
    static {
        NetHttpTransport tt;
        try {
            tt = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        YouTube.Builder builder = new YouTube.Builder(tt, GsonFactory.getDefaultInstance(), null);
        builder.setGoogleClientRequestInitializer(new YouTubeRequestInitializer(Config.get("YOUTUBE_KEY")));
        builder.setApplicationName("qMusic");
        API = builder.build();
    }
    // endregion Static Initializer

    /**
     * Retrieves the top <b>n</b> {@link YouTube} {@link SearchResult}s for the given search query.<br>
     * <br>
     * <b>A few things to note:</b><br>
     * - The results contain only videos<br>
     * - SafeSearch is disabled<br>
     * <br>
     * <b>Quota impact</b><br>
     * A call to this method has a <a href="https://developers-dot-devsite-v2-prod.appspot.com/youtube/v3/getting-started#quota">quota cost</a> of 100 units.
     *
     * @param searchQuery is what you would type in to the search bar on YouTube
     * @param maxResults  is <b>n</b>
     * @return a non-empty {@link List<SearchResult>} of {@link YouTube} {@link SearchResult}s
     * @throws IOException              see {@link YouTube.Search.List}
     * @throws NoResultsException       when the list returned by the API is empty
     * @throws IllegalArgumentException when <b>n</b> in smaller than 1 or greater than an unsigned int
     * @see <a href="https://developers.google.com/youtube/v3/determine_quota_cost">Quota Calculator</a>
     */
    @NotNull
    public static List<SearchResult> getSearchResults(@NotNull String searchQuery, long maxResults) throws IOException, NoResultsException, IllegalArgumentException {
        if (maxResults < 1 || maxResults > Math.pow(2, 32) - 1) {
            throw new IllegalArgumentException("'maxResults' must be greater than 0 and smaller than or equal to an unsigned int (Math.pow(2, 32) - 1)!");
        }
        var items = API.search().list(List.of("snippet")).setMaxResults(maxResults).setQ(searchQuery).setSafeSearch("none").setType(List.of("video")).execute().getItems();
        if (items.isEmpty()) {
            throw new NoResultsException(searchQuery);
        }
        return items;
    }

    /**
     * Retrieves the top {@link YouTube} {@link SearchResult}s for the given search query.<br>
     * <br>
     * <b>A few things to note:</b><br>
     * - The results contain only videos<br>
     * - SafeSearch is disabled<br>
     * <br>
     * <b>Quota impact</b><br>
     * A call to this method has a <a href="https://developers-dot-devsite-v2-prod.appspot.com/youtube/v3/getting-started#quota">quota cost</a> of 100 units.
     *
     * @param searchQuery is what you would type in to the search bar on YouTube
     * @return a non-empty {@link List<SearchResult>} of {@link YouTube} {@link SearchResult}s
     * @throws IOException        see {@link YouTube.Search.List}
     * @throws NoResultsException when the list returned by the API is empty
     * @see <a href="https://developers.google.com/youtube/v3/determine_quota_cost">Quota Calculator</a>
     */
    @NotNull
    public static List<SearchResult> getSearchResults(@NotNull String searchQuery) throws IOException, NoResultsException {
        var items = API.search().list(List.of("snippet")).setQ(searchQuery).setSafeSearch("none").setType(List.of("video")).execute().getItems();
        if (items.isEmpty()) {
            throw new NoResultsException(searchQuery);
        }
        return items;
    }

    /**
     * Retrieves all {@link PlaylistItem}s in a Playlist.<br>
     * <br>
     * <b>Quota impact</b><br>
     * A call to this method has a <a href="https://developers-dot-devsite-v2-prod.appspot.com/youtube/v3/getting-started#quota">quota cost</a> of 1 unit.
     *
     * @param playlistId is the id of the playlist
     * @return a non-empty {@link List<PlaylistItem>} of {@link YouTube} {@link PlaylistItem}s
     * @throws IOException        see {@link YouTube.Search.List}
     * @throws NoResultsException when the list returned by the API is empty
     * @see <a href="https://developers.google.com/youtube/v3/determine_quota_cost">Quota Calculator</a>
     */
    @NotNull
    public static List<PlaylistItem> getPlaylistItems(@NotNull String playlistId) throws IOException, NoResultsException {
        List<PlaylistItem> items;
        try {
            items = API.playlistItems().list(List.of("snippet", "contentDetails")).setPlaylistId(playlistId).execute().getItems();
        } catch (GoogleJsonResponseException e) {
            throw new NoResultsException(e.getDetails().getErrors().get(0).getReason());
        }
        if (items.isEmpty()) throw new NoResultsException("Playlist is empty!");
        return items;
    }

    /**
     * Retrieves information like statistics about video(s).<br>
     * <br>
     * <b>Quota impact</b><br>
     * A call to this method has a <a href="https://developers-dot-devsite-v2-prod.appspot.com/youtube/v3/getting-started#quota">quota cost</a> of 1 unit.
     *
     * @param videoId is the id(s) of the video(s)
     * @return a non-empty {@link List<PlaylistItem>} of {@link YouTube} {@link Video}s
     * @throws IOException        see {@link YouTube.Search.List}
     * @throws NoResultsException when the list returned by the API is empty
     * @see <a href="https://developers.google.com/youtube/v3/determine_quota_cost">Quota Calculator</a>
     */
    @NotNull
    public static List<Video> getVideoInfo(@NotNull String... videoId) throws IOException, NoResultsException {
        //noinspection ConstantConditions
        if (Arrays.stream(videoId).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("None of the strings can be null!");
        }
        var items = API.videos().list(List.of("snippet", "contentDetails", "statistics")).setId(Arrays.asList(videoId)).execute().getItems();
        if (items.isEmpty()) {
            throw new NoResultsException("How did this happen?!");
        }
        return items;
    }
}
