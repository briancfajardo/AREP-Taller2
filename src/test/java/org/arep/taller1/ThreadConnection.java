package org.arep.taller1;

import org.arep.taller1.api.HttpConnection;

import java.io.IOException;



public class ThreadConnection extends Thread{
    private String movieTitle;
    private String movieInfo;

    public ThreadConnection(String movieTitle){
        super();
        this.movieTitle = movieTitle;
    }

    @Override
    public void run() {
        try {
            movieInfo = HttpConnection.getMovie(movieTitle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getMovieInfo() {
        return movieInfo;
    }
}
