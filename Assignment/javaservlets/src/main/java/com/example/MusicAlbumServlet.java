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
import java.util.logging.Logger;
import com.google.gson.Gson;
import javax.servlet.http.Part;

@WebServlet(name = "MusicAlbumServlet", urlPatterns = {"/albums/*"})
@MultipartConfig
public class MusicAlbumServlet extends HttpServlet{
    //Get 
    // @Override
    // protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    //     res.setContentType("text/plain");
    //     String urlPath = req.getPathInfo();
    
    //     // check we have a URL!
    //     if (urlPath == null || urlPath.isEmpty()) {
    //         res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    //         res.getWriter().write("missing paramterers");
    //         return;
    //     }
    
    //     String[] urlParts = urlPath.split("/");
    //     // and now validate url path and return the response status code
    //     // (and maybe also some value if input is valid)
    
    //     if (!isGetUrlValid(urlParts)) {
    //         res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    //         res.getWriter().write("not valid");
    //         // res.getWriter().write(urlParts[0]);
    //     } else {
    //         res.setStatus(HttpServletResponse.SC_OK);
    //         // do any sophisticated processing with urlParts which contains all the url params
    //         // TODO: process url params in `urlParts`
    //         res.getWriter().write("It works!");
    //     }
    //     return;
    // }
   
    // private boolean isGetUrlValid(String[] urlParts) {
    //     // TODO: validate the request url path according to the API spec
    //     // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    //     if(urlParts.length != 1) {
    //         return false;
    //     }
    //     return true;
    // }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        //set the response type
        res.setContentType("application/json");
        // String urlPath = req.getPathInfo();
    
        // // check we have a URL!
        // if (urlPath == null || urlPath.isEmpty()) {
        //     res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        //     res.getWriter().write("missing paramterers");
        //     return;
        // }
        // StringBuilder requestBody = new StringBuilder();
        // String line;
        // try (BufferedReader reader = req.getReader()) {
        //     while ((line = reader.readLine()) != null) {
        //         requestBody.append(line);
        //     }
        // }
        String contentType = req.getContentType();
        Response response;
        // Check if the request is multipart/form-data
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            // Handle the request as multipart/form-data
            for (Part part : req.getParts()) {
                // System.out.println(part.getName() + ": " + part.getSize() + " bytes");
                res.getOutputStream().println(part.getName() + ": " + part.getSize() + " bytes");
            }
            Part imagePart = req.getPart("image");
            Part profilePart = req.getPart("profile");
            // res.getOutputStream().println(imagePart.getContentType());
            // res.getOutputStream().println(profilePart.getContentType());

            if(imagePart == null || !"image/png".equals(imagePart.getContentType())) {
                response = new ErrorResponse("Image file is not of type image/jpeg");
                res.setStatus(res.SC_BAD_REQUEST);
            }
            else if(profilePart == null ) {
                response = new ErrorResponse("Profile file does not exist");
                res.setStatus(res.SC_BAD_REQUEST);
            }
            else {
                //process the request
                //process the image
                InputStream imageInputStream = imagePart.getInputStream();
                // long imageSize = getStreamSize(imageInputStream);
                
                long imageSize = imagePart.getSize();

                //process the profile
                String profileJson = inputStreamToString(profilePart.getInputStream());
                Gson gson = new Gson();
                Profile profile = gson.fromJson(profileJson, Profile.class);

                response = new AlbumResponse(AlbumID.getAlbumID(), String.valueOf(imageSize));
                res.setStatus(res.SC_OK);
                
                //TODO: store the image and profile in a file and associate it with the album ID
            }
        } else {
            // Handle other request types or respond with an error
            response = new ErrorResponse("Input data is not of type multipart/form-data");
            res.setStatus(res.SC_BAD_REQUEST);
        }


 

        Gson gson = new Gson();
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


