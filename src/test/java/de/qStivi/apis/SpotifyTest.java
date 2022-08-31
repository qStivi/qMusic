package de.qStivi.apis;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SpotifyTest {

    private Spotify API;

    @BeforeEach
    void setUp() throws IOException, ParseException, SpotifyWebApiException {
        API = new Spotify();
    }

    //region getTrackName() tests
    @RepeatedTest(10)
    void testGetTrackName() {
        assertEquals("Darling.", API.getTrackName("02ffsB9p104aiZWgibNUZP"));
    }

    @RepeatedTest(10)
    void testGetTrackNameIfNull() {
        assertNull(API.getTrackName(null));
    }

    @RepeatedTest(10)
    void testGetTrackNameIfEmpty() {
        assertNull(API.getTrackName(""));
    }

    @RepeatedTest(10)
    void testGetTrackNameIfDoesNotExist() {
        assertNull(API.getTrackName("yee"));
    }
    //endregion

    //region getTrackArtists() tests
    @RepeatedTest(10)
    void testGetTrackArtistsSingle() {
        var names = API.getTrackArtists("02ffsB9p104aiZWgibNUZP");
        assertEquals("Heiakim", names[0]);
    }

    @RepeatedTest(10)
    void testGetTrackArtistsTwo() {
        var names = API.getTrackArtists("0u2P5u6lvoDfwTYjAADbn4");
        assertEquals("Billie Eilish", names[0]);
        assertEquals("Khalid", names[1]);
    }

    @RepeatedTest(10)
    void testGetTrackArtistsMultiple() {
        var names = API.getTrackArtists("4hKGICUB890arImAdWRaHQ");
        assertEquals("Frank Ocean", names[0]);
        assertEquals("Mick Jones", names[1]);
        assertEquals("Paul Simonon", names[2]);
    }

    @RepeatedTest(10)
    void testGetTrackArtistsIfNull() {
        var names = API.getTrackArtists(null);
        assertNull(names);
    }

    @RepeatedTest(10)
    void testGetTrackArtistsEmpty() {
        var names = API.getTrackArtists("");
        assertNull(names);
    }

    @RepeatedTest(10)
    void testGetTrackArtistsDoesNotExist() {
        var names = API.getTrackArtists("yee");
        assertNull(names);
    }
    //endregion

    //region getFormattedPlaylist() tests
    @RepeatedTest(10)
    void testGetFormattedPlaylist() {
        var songs = API.getFormattedPlaylist("63oHYk4u9biECTVr4F9g6i");
        assertEquals("Darling.+Heiakim", songs[0]);
        assertEquals("Eu+Mariana+Froes", songs[1]);
        assertEquals("失礼しますが、RIP♡+Mori+Calliope", songs[2]);
        assertThrowsExactly(ArrayIndexOutOfBoundsException.class, () -> System.out.println(songs[3]));
        assertEquals(3, songs.length);
    }

    @RepeatedTest(10)
    void testGetFormattedPlaylistIfSingleSong() {
        var songs = API.getFormattedPlaylist("3YjeihqodZcRYoN5VAs5k0");
        assertEquals("Darling.+Heiakim", songs[0]);
        assertThrowsExactly(ArrayIndexOutOfBoundsException.class, () -> System.out.println(songs[1]));
        assertEquals(1, songs.length);
    }

    @RepeatedTest(10)
    void testGetFormattedPlaylistIfNoSongs() {
        var songs = API.getFormattedPlaylist("6GEd5aYt8iGiABORzkh0nn");
        assertEquals(0, songs.length);
        assertThrowsExactly(ArrayIndexOutOfBoundsException.class, () -> System.out.println(songs[0]));
    }

    @RepeatedTest(10)
    void testGetFormattedPlaylistIfEmpty() {
        var songs = API.getFormattedPlaylist("");
        assertNull(songs);
    }

    @RepeatedTest(10)
    void testGetFormattedPlaylistIfNull() {
        var songs = API.getFormattedPlaylist(null);
        assertNull(songs);
    }

    @RepeatedTest(10)
    void testGetFormattedPlaylistIfDoesNotExist() {
        var songs = API.getFormattedPlaylist("yee");
        assertNull(songs);
    }
    //endregion
}
