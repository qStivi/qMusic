package de.qStivi.apis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YouTubeTest {

    @Test
    void getVideoIdBySearchQuery() {
        assertEquals("", YouTube.getVideoIdBySearchQuery(""));
    }

    @Test
    void getPlaylistItemsByLink() {
        var items = YouTube.getPlaylistItemsByLink("https://youtu.be/KHIzQ_NobAk");
    }

    @Test
    void readJsonFromUrl() {
    }
}