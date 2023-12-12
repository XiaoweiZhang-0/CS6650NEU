package com.example;

import java.util.concurrent.atomic.AtomicLong;

public class AlbumID {
    static AtomicLong albumID = new AtomicLong();
    static String getAlbumID(){
        albumID.incrementAndGet();
        return String.valueOf(albumID.get());
    }
}
