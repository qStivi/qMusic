package de.qStivi;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class Util {

    public static boolean isValidLink(@NotNull @Nonnull String link) {
        return link.matches("(.*)open.spotify.com(.*)|spotify(.*)|(.*)youtube.com(.*)|(.*)youtu.be(.*)");
    }

}
