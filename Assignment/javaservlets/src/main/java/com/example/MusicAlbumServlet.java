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
import java.util.logging.Logger;
import com.google.gson.Gson;
import javax.servlet.http.Part;

@WebServlet(name = "MusicAlbumServlet", urlPatterns = {"/albums/*"})
@MultipartConfig
public class MusicAlbumServlet extends HttpServlet{
    private Map<String, Profile> albums = new HashMap<>();
    //Get 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");

        // check we have a URL!
        // if (urlPath == null || urlPath.isEmpty()) {
        //     res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        //     res.getWriter().write("invalid request");
        //     return;
        // }
    

        String[] urlParams = req.getQueryString().split("&");
        if(urlParams.length < 1) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("missing paramterers");
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
        }


        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)
    
        // if (!isGetUrlValid(urlParts)) {
        //     res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        //     res.getWriter().write("not valid");
        //     // res.getWriter().write(urlParts[0]);
        // } else {
        //     res.setStatus(HttpServletResponse.SC_OK);
        //     // do any sophisticated processing with urlParts which contains all the url params
        //     // TODO: process url params in `urlParts`

        //     res.getWriter().write("It works!");
        // }
        // return;
    }
   
    // private boolean isGetUrlValid(String[] urlParts) {
    //     // TODO: validate the request url path according to the API spec
    //     // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    //     if(urlParts.length != 1 || urlParts[0].equals("albums") == false) {
    //         return false;
    //     }
    //     return true;
    // }

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
                    response = new ErrorResponse("Image file is not of type image/jpeg");
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


