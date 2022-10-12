package de.qStivi;

public class NoResultsException extends Exception {

    public NoResultsException(String searchQuery) {
        super(searchQuery);
    }
}
