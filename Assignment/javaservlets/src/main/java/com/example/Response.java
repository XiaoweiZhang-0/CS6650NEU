package com.example;

public abstract class Response {
    // String description;
}

class AlbumResponse extends Response{
        String albumID;
        String imageSize;

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

