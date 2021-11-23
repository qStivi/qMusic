package de.qStivi.apis;


import de.qStivi.Config;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Spotify {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private static final String CLIENT_ID = Config.get("SPOTIFY_ID");

    private static final String CLIENT_SECRET = Config.get("SPOTIFY_SECRET");

    private static final SpotifyApi API = new SpotifyApi.Builder()
            .setClientId(CLIENT_ID)
            .setClientSecret(CLIENT_SECRET)
            .build();

    /**
     * Initializes the Spotify API.
     * <p>
     * Verifies that all credentials are right.
     */
    public Spotify() throws IOException, SpotifyWebApiException, ParseException {
        final var clientCredentialsRequest = API.clientCredentials().build();
        final var clientCredentials = clientCredentialsRequest.execute();

        API.setAccessToken(clientCredentials.getAccessToken());

        LOGGER.info("Spotify loaded!");
    }

    /**
     * Get the name of a track on Spotify.
     * <p>
     * Connects to the Spotify API to get the track name corresponding to the given track id.
     *
     * @param id The track id which is included in the song link and the Spotify URI.
     * @return String containing the name of the corresponding Song.
     * @throws IOException            In case of networking issues.
     * @throws SpotifyWebApiException The Web API returned an error further specified in this exception's root cause.
     * @throws ParseException         String parsing error.
     */
    public String getTrackName(String id) throws IOException, SpotifyWebApiException, ParseException {
        final var track = API.getTrack(id).build().execute();
        final var name = track.getName();
        LOGGER.info("Got Spotify track name: " + name);
        return name;
    }

    /**
     * Get artists of a Spotify track.
     * <p>
     * Connect to the Spotify API to get artists names of the song corresponding to the given id.
     *
     * @param id The track id which is included in the song link and the Spotify URI.
     * @return String containing the name of the corresponding Song.
     * @throws IOException            In case of networking issues.
     * @throws SpotifyWebApiException The Web API returned an error further specified in this exception's root cause.
     * @throws ParseException         String parsing error.
     */
    public ArrayList<String> getTrackArtists(String id) throws IOException, SpotifyWebApiException, ParseException {
        final var getTrackRequest = API.getTrack(id).build();
        final var track = getTrackRequest.execute();
        final var artistsSimplified = track.getArtists();
        var names = new ArrayList<String>();
        for (ArtistSimplified artistSimplified : artistsSimplified) {
            names.add(artistSimplified.getName());
        }
        return names;
    }

    /**
     * Returns a {@link List<String>} of Strings where each entry contains the name and artist(s) of a song in the following format: song+Name+artist1+name+Artist2+name... Example: Heathens+Twenty+One+Pilots
     *
     * @param id The playlist id which is included in the playlist link and the Spotify URI.
     * @return A {@link List<String>} of Strings.
     * @throws IOException If an I/O error occurs while creating the input stream.
     */
    // https://developer.spotify.com/console/get-playlist-tracks/?playlist_id=3cEYpjA9oz9GiPac4AsH4n&market=ES&fields=items(added_by.id%2Ctrack(name%2Chref%2Calbum(name%2Chref)))&limit=10&offset=5&additional_types=
    public List<String> getFormattedPlaylist(String id) throws IOException, ParseException, SpotifyWebApiException, InterruptedException {
        var tracks = API.getPlaylist(id).build().execute().getTracks().getItems();
        var output = new ArrayList<String>();
        for (PlaylistTrack playlistTrack : tracks) {
            var track = playlistTrack.getTrack();
            var name = track.getName().replace(" ", "+");
            var sb = new StringBuilder(name);
            var artists = API.getTrack(track.getId()).build().execute().getArtists();
            Thread.sleep(300);
            for (ArtistSimplified artist : artists) {
                var artistName = artist.getName().replace(" ", "+");
                sb.append("+").append(artistName);
            }
            output.add(sb.toString());
        }

        return output;

        /*
        InputStream response;
        List<String> finalOutput = new ArrayList<>();
        URLConnection connection = new URL("https://api.spotify.com/v1/playlists/" + id + "/tracks?market=DE&fields=items(track(name%2C%20artists(name)))").openConnection();
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + API.getAccessToken());
        response = connection.getInputStream();
        try (Scanner scanner = new Scanner(response)) {
            String responseBody = scanner.useDelimiter("\\A").next();
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray items = jsonObject.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                JSONObject track = item.getJSONObject("track");
                String name = track.getString("name");
//                System.out.println(name);
                String names = "";
                JSONArray artists = track.getJSONArray("artists");
                for (int j = 0; j < artists.length(); j++) {
                    JSONObject object = artists.getJSONObject(j);
                    names = names.concat("+" + object.getString("name"));
                }
//                System.out.println(names);
                String output = name + names;
                output = output.replace(" ", "+");
//                System.out.println(output);
                finalOutput.add(output);
            }
        }
        return finalOutput;

         */
    }


}