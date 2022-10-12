package de.qStivi.apis;

import de.qStivi.NoResultsException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpotifyAPITest {

    @Test
    void getTrack() throws NoResultsException {
        var track = SpotifyAPI.getTrack(SpotifyAPI.getIDFromLink("https://open.spotify.com/track/17VP4tofJ3evJbtY8Tk1Qi?si=81d47eb212a04259"));
        assertEquals("17VP4tofJ3evJbtY8Tk1Qi", track.getId());

        assertThrows(NoResultsException.class, () -> SpotifyAPI.getTrack("yee"));

        assertThrows(IllegalArgumentException.class, () -> SpotifyAPI.getTrack(""));

        //noinspection ConstantConditions
        assertThrows(IllegalArgumentException.class, () -> SpotifyAPI.getTrack(null));
    }

    @Test
    void getPlaylist() throws NoResultsException {
        var track = SpotifyAPI.getPlaylist(SpotifyAPI.getIDFromLink("https://open.spotify.com/playlist/4E0PuP0vb9CH0pTpSv8DLI?si=17627b37933e4555"));
        assertEquals("4E0PuP0vb9CH0pTpSv8DLI", track.getId());


        assertThrows(NoResultsException.class, () -> SpotifyAPI.getPlaylist("yee"));

        assertThrows(IllegalArgumentException.class, () -> SpotifyAPI.getPlaylist(""));

        //noinspection ConstantConditions
        assertThrows(IllegalArgumentException.class, () -> SpotifyAPI.getPlaylist(null));
    }

    @Test
    void getIDFromLink() {
        assertEquals("4E0PuP0vb9CH0pTpSv8DLI", SpotifyAPI.getIDFromLink("https://open.spotify.com/playlist/4E0PuP0vb9CH0pTpSv8DLI?si=ce876f70ed1c45d9"));
        assertEquals("4E0PuP0vb9CH0pTpSv8DLI", SpotifyAPI.getIDFromLink("open.spotify.com/playlist/4E0PuP0vb9CH0pTpSv8DLI?si=ce876f70ed1c45d9"));
        assertEquals("17VP4tofJ3evJbtY8Tk1Qi", SpotifyAPI.getIDFromLink("https://open.spotify.com/track/17VP4tofJ3evJbtY8Tk1Qi?si=84b276c96d974b07"));
        assertEquals("17VP4tofJ3evJbtY8Tk1Qi", SpotifyAPI.getIDFromLink("open.spotify.com/track/17VP4tofJ3evJbtY8Tk1Qi?si=84b276c96d974b07"));
    }
}
