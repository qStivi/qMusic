package de.qStivi.apis;


import de.qStivi.Config;
import org.apache.hc.core5.http.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Spotify {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private static final String clientId = Config.get("SPOTIFY_ID");

    private static final String clientSecret = Config.get("SPOTIFY_SECRET");

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .build();

    public Spotify() throws IOException, SpotifyWebApiException, ParseException {
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
        ClientCredentials clientCredentials = clientCredentialsRequest.execute();

        spotifyApi.setAccessToken(clientCredentials.getAccessToken());

        LOGGER.info("Finished loading!");
    }

    /**
     * Get the name of a Spotify track.
     *
     * @param id The track id which is included in the song link and the Spotify URI.
     * @return String containing the name of the corresponding Song.
     * @throws IOException            In case of networking issues.
     * @throws SpotifyWebApiException The Web API returned an error further specified in this exception's root cause.
     * @throws ParseException         String parsing error.
     */
    public String getTrackName(String id) throws IOException, SpotifyWebApiException, ParseException {
        Track track = spotifyApi.getTrack(id).build().execute();
        return track.getName();
    }

    /**
     * Get the first Artist of a de.qStivi.APIs.Spotify track.
     *
     * @param id The track id which is included in the song link and the de.qStivi.APIs.Spotify URI.
     * @return String containing the name of the corresponding Song.
     * @throws IOException            In case of networking issues.
     * @throws SpotifyWebApiException The Web API returned an error further specified in this exception's root cause.
     * @throws ParseException         String parsing error.
     */
    public String getTrackArtists(String id) throws IOException, SpotifyWebApiException, ParseException {
        GetTrackRequest getTrackRequest = spotifyApi.getTrack(id).build();
        final Track track = getTrackRequest.execute();
        if (Arrays.stream(track.getArtists()).findFirst().isEmpty()) return "";
        return Arrays.stream(track.getArtists()).findFirst().get().getName();
    }

    /**
     * Returns {@link List<String>} of Strings where each entry contains the name and artist of a song in the following format: song+Name+artist1+name+Artist2+name... Example: Heathens+Twenty+One+Pilots
     *
     * @param id The playlist id which is included in the playlist link and the de.qStivi.APIs.Spotify URI.
     * @return A {@link List<String>} of Strings.
     * @throws IOException If an I/O error occurs while creating the input stream.
     */
    // https://developer.spotify.com/console/get-playlist-tracks/?playlist_id=3cEYpjA9oz9GiPac4AsH4n&market=ES&fields=items(added_by.id%2Ctrack(name%2Chref%2Calbum(name%2Chref)))&limit=10&offset=5&additional_types=
    public List<String> getPlaylist(String id) throws IOException {
        InputStream response;
        List<String> finalOutput = new ArrayList<>();
        URLConnection connection = new URL("https://api.spotify.com/v1/playlists/" + id + "/tracks?market=DE&fields=items(track(name%2C%20artists(name)))").openConnection();
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + spotifyApi.getAccessToken());
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
    }


}