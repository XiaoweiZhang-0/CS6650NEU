package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import javax.servlet.http.Part;

import java.util.concurrent.ConcurrentHashMap;

@WebServlet(name = "MusicAlbumServlet", urlPatterns = {"/albums/*"})
@MultipartConfig
public class MusicAlbumServlet extends HttpServlet{
    // private Map<String, Profile> albums = new HashMap<>();
    private ConcurrentHashMap<String, Profile> albums = new ConcurrentHashMap<>();
    //Get 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");

        //check if we have a parameter
        if(req.getQueryString() == null) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("missing paramterers");
            return;
        }
            
        //check if we have exactly one parameter
        String[] urlParams = req.getQueryString().split("&");
        if(urlParams.length > 1){
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("too many paramterers");
            return;
        }


        for(String param: urlParams) {
            String[] keyValue = param.split("=");
            if(keyValue[0].equals("albumID")) {
                String albumID = keyValue[1];
                if(albums.containsKey(albumID)) {
                    Profile profile = albums.get(albumID);
                    res.setStatus(HttpServletResponse.SC_OK);
                    Response response = new profileResponse(profile.artist, profile.title, profile.year);
                    Gson gson = new Gson();
                    res.getWriter().write(gson.toJson(response));
                    return;
                }
                else {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    res.getWriter().write("Key not found");
                    return;
                }
            }
            else{
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write("invalid parameter");
                return;
            }
        }

    }
   

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        //set the response type
        res.setContentType("application/json");

        String contentType = req.getContentType();
        Response response;
        Gson gson = new Gson();
        try{
            // Check if the request is multipart/form-data
            if (contentType != null && contentType.startsWith("multipart/form-data")) {
                // Handle the request as multipart/form-data
                Part imagePart = req.getPart("image");
                Part profilePart = req.getPart("profile");

                if(imagePart == null || !"image/png".equals(imagePart.getContentType())) {
                    response = new ErrorResponse("Image file is not of type image/png");
                    res.setStatus(res.SC_BAD_REQUEST);
                }
                else if(profilePart == null ) {
                    response = new ErrorResponse("Profile file does not exist");
                    res.setStatus(res.SC_BAD_REQUEST);
                }
                else {
                    //process the image
                    InputStream imageInputStream = imagePart.getInputStream();                
                    long imageSize = imagePart.getSize();

                    //process the profile
                    String profileJson = inputStreamToString(profilePart.getInputStream());
                    Profile profile = gson.fromJson(profileJson, Profile.class);


                    //info to store: albumID, profile
                    String albumID = AlbumID.getAlbumID();
                    albums.put(albumID, profile);
                    


                    response = new AlbumResponse(albumID, String.valueOf(imageSize));
                    res.setStatus(res.SC_OK);
                    
                }
            } else {
                // Handle other request types or respond with an error
                response = new ErrorResponse("Input data is not of type multipart/form-data");
                res.setStatus(res.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            response = new ErrorResponse(e.getMessage());
            res.setStatus(res.SC_BAD_REQUEST);
        }

        res.getOutputStream().print(gson.toJson(response));
        res.getOutputStream().flush();

    }


private String inputStreamToString(InputStream is) throws IOException {
    StringBuilder sb = new StringBuilder();
    String line;

    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
    }

    return sb.toString();
}


}


