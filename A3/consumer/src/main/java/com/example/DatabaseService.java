package com.example;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class DatabaseService {

    public void postReview(String albumID, String likeorDislike) throws SQLException {
        System.out.println(likeorDislike);
        System.out.println(albumID);
        String sql = "";
        if(likeorDislike.equals("like")){
            sql = "UPDATE CS6650.albums SET likes = likes + 1 Where id = ?";
        }
        else if(likeorDislike.equals("dislike")){
            sql = "UPDATE CS6650.albums SET dislikes = dislikes + 1 Where id = ?";
        }
        try (Connection conn = DataConnectionPool.getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, albumID);
            int rowAffected = pstmt.executeUpdate();
            if (rowAffected != 1)
                throw new SQLException("Cannot update album");
        }
    }


}

