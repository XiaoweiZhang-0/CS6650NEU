package com.example;


import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


public class DatabaseService {
    public String addAlbum(Profile profile, InputStream imageBytes) throws SQLException{
        // DataSource ds = DataConnectionPool.getDataSource();
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();
        String sql = "INSERT INTO CS6650.albums (id, artist, title, year, image) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = DataConnectionPool.getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuidAsString);
            pstmt.setString(2, profile.artist);
            pstmt.setString(3, profile.title);
            pstmt.setString(4, profile.year);
            pstmt.setBinaryStream(5, imageBytes);
    
            pstmt.executeUpdate();
            return uuidAsString;
        }
    }

    public Profile getAlbumByKey(String albumID){
        String sql = "SELECT * FROM CS6650.albums WHERE ID = ?;";
        Profile profile = null;
        try (Connection conn = DataConnectionPool.getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, albumID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                profile = new Profile(rs.getString("artist"), rs.getString("title"), rs.getString("year"));
            }
        } catch (SQLException e) {
            // Handle exceptions
        }
        return profile;
    }


}

