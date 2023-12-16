package com.example;

public abstract class Response {
    // String description;
}

class AlbumResponse extends Response{
        String albumID;
        String imageSize;
        String description;
        AlbumResponse(String albumID, String imageSize) {
            this.albumID = albumID;
            this.imageSize = imageSize;
            // this.description = description;
        }
}

class ErrorResponse extends Response{
    String message;

    ErrorResponse(String message) {
        this.message = message;
        // this.description = description;
    }
}

class profileResponse extends Response{
    String artist;
    String title;
    String year;
    // String description;
    profileResponse(String artist, String title, String year) {
        this.artist = artist;
        this.title = title;
        this.year = year;
    }

}

