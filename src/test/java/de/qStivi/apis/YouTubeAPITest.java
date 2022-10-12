package de.qStivi.apis;

import de.qStivi.NoResultsException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class YouTubeAPITest {

    @Test
    void getSearchResults() throws NoResultsException, IOException {
        var oneResult = YouTubeAPI.getSearchResults("qStivi dev", 1);
        assertEquals(1, oneResult.size());
        assertEquals("KHIzQ_NobAk", oneResult.get(0).getId().getVideoId());

        var multipleResults = YouTubeAPI.getSearchResults("qStivi dev");
        assertFalse(multipleResults.isEmpty());
        assertEquals("KHIzQ_NobAk", multipleResults.get(0).getId().getVideoId());

        var checkForAllVideos = YouTubeAPI.getSearchResults("Imagine Dragons");
        assertTrue(checkForAllVideos.stream().map(searchResult -> searchResult.getId().getKind()).allMatch(s -> s.equals("youtube#video")));

        //noinspection ConstantConditions
        assertThrows(IllegalArgumentException.class, () -> YouTubeAPI.getSearchResults(null));

        assertThrows(IllegalArgumentException.class, () -> YouTubeAPI.getSearchResults("yo", -1));

        assertThrows(IllegalArgumentException.class, () -> YouTubeAPI.getSearchResults("yo", (long) (Math.pow(2, 32) - 0)));
    }

    @Test
    void getPlaylistItems() throws IOException, NoResultsException {
        var multipleItems = YouTubeAPI.getPlaylistItems("PLPoS_0I9SFOML7F4yoEflduzK65Xzqfd-");
        assertEquals(3, multipleItems.size());
        assertEquals("KHIzQ_NobAk", multipleItems.get(0).getContentDetails().getVideoId());
        assertEquals("9bZkp7q19f0", multipleItems.get(1).getContentDetails().getVideoId());
        assertEquals("4hpEnLtqUDg", multipleItems.get(2).getContentDetails().getVideoId());

        var oneItem = YouTubeAPI.getPlaylistItems("PLPoS_0I9SFOP9S6-XBphzT43ZSCEwmKDX");
        assertEquals(1, oneItem.size());
        assertEquals("KHIzQ_NobAk", oneItem.get(0).getContentDetails().getVideoId());

        assertThrows(NoResultsException.class, () -> YouTubeAPI.getPlaylistItems("PLPoS_0I9SFONyANDxfvk-HASIUpTw2eqM"), "Playlist is empty!");

        assertThrows(NoResultsException.class, () -> YouTubeAPI.getPlaylistItems("PLPoS_0I9SFOP8lzaAf9JsOkntEJZSOZCP"), "playlistNotFound");

        //noinspection ConstantConditions
        assertThrows(IllegalArgumentException.class, () -> YouTubeAPI.getPlaylistItems(null));
    }

    @Test
    void getVideoInfo() throws IOException, NoResultsException {
        var info = YouTubeAPI.getVideoInfo("KHIzQ_NobAk", "9bZkp7q19f0", "sdfsdfsdfgwfwnfjiwnifbwieufbisubf");
        assertEquals(2, info.size());
        assertEquals("Dev", info.get(0).getSnippet().getTitle());
        assertEquals("PSY - GANGNAM STYLE(강남스타일) M/V", info.get(1).getSnippet().getTitle());

        //noinspection ConstantConditions
        assertThrows(IllegalArgumentException.class, () -> YouTubeAPI.getVideoInfo(null, null));
    }
}
