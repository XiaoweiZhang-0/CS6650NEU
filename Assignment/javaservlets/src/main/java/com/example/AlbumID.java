package com.example;

public class AlbumID {
    static long albumID = 0;
    static String getAlbumID(){
        albumID++;
        return String.valueOf(albumID);
    }
}
