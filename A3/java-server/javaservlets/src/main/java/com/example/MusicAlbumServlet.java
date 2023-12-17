package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.DocFlavor.READER;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.gson.Gson;

import javax.servlet.http.Part;


@WebServlet(name="MusicAlbumServlet", urlPatterns = {"/albums/*"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10,
maxFileSize = 1024 * 1024 * 50,
maxRequestSize = 1024 * 1024 * 100)
public class MusicAlbumServlet extends HttpServlet{
    // private Map<String, Profile> albums = new HashMap<>();
    // private ConcurrentHashMap<String, Profile> albums = new ConcurrentHashMap<>();
    private DatabaseService dbService;
    private static final Logger logger = LogManager.getLogger(MusicAlbumServlet.class);
    private Pattern pattern;
    private Matcher matcher;
    @Override
    public void init() throws ServletException{
        super.init();
        dbService = new DatabaseService();
        this.pattern = Pattern.compile("artist:\\s*(\\w+)\\s*title:\\s*(\\w+)\\s*year:\\s*(\\d+)");

    }
    //Get 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
            
        //check if we have exactly one parameter
        // if(urlParams.length != 2){
        //     res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        //     res.getWriter().write("invalid path");
        //     return;
        // }
        Gson gson = new Gson();
        String albumID = req.getPathInfo().split("/")[1];
        

            Profile profile = dbService.getAlbumByKey(albumID);
            if(profile != null) {
                res.setStatus(HttpServletResponse.SC_OK);
                Response response = new profileResponse(profile.artist, profile.title, profile.year);
                res.getWriter().write(gson.toJson(response));
                return;
            }
            else {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                res.getWriter().write("Key not found");
                return;
            }

    }
   

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        //set the response type
        res.setContentType("application/json");

        String contentType = req.getContentType();
        Response response = null;
        Gson gson = new Gson();
            // Check if the request is multipart/form-data
            if (contentType != null && contentType.startsWith("multipart/form-data")) {
                // Handle the request as multipart/form-data
                Part imagePart = req.getPart("image");
                Part profilePart = req.getPart("profile");
                if(imagePart == null ) {
                    response = new ErrorResponse("Image file does not exist");
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                }

                else if(profilePart == null ) {
                    response = new ErrorResponse("Profile file does not exist");
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
                else {
                        //process the image
                        InputStream imageInputStream = imagePart.getInputStream();
                        long imageSize = imagePart.getSize();

                        //process the profile 
                        String artist = null;
                        String title = null;
                        String year = null;

                        try {
                                BufferedReader reader = new BufferedReader(
                                new InputStreamReader(profilePart.getInputStream()));
                                StringBuilder stringBuilder = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    stringBuilder.append(line);
                                }
                                String profileJson = stringBuilder.toString();
                                this.matcher = pattern.matcher(profileJson);
                                if (matcher.find()) {
                                    artist = matcher.group(1);
                                    title = matcher.group(2);
                                    year = matcher.group(3);
                                }
                                else{
                                    response = new ErrorResponse("Error processing profile");
                                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                }
                        }catch(Exception e){
                            logger.error("ERROR FROM ALBUM SERVLET PROCESSING ALBUM PART-----------------------------------------------"+e.getMessage());
                        }
                        finally{
                            profilePart.getInputStream().close();
                        }
                        try{
                            Profile profile = new Profile(artist, title, year);
                            String ID = dbService.addAlbum(profile, imageInputStream);

                            if(ID.equals("")){
                                response = new ErrorResponse("Error adding album to database");
                                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            }
                            else{
                                response = new AlbumResponse(ID, String.valueOf(imageSize));
                                res.setStatus(HttpServletResponse.SC_OK);
                            }
                        }catch(Exception e){
                            logger.error("ERROR FROM ALBUM SERVLET ADDING ALBUM PART-----------------------------------------------"+e.getMessage());
                            response = new ErrorResponse("Error adding album to database");
                            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        }
                        finally{
                            imagePart.getInputStream().close();
                        }


                }

            }
            else{
                response = new ErrorResponse("Invalid content type");
                res.setStatus(res.SC_BAD_REQUEST);
            } 
        res.getOutputStream().print(gson.toJson(response));
        res.getOutputStream().flush();
        res.getOutputStream().close();

    }


// private String inputStreamToString(InputStream is) throws IOException {
//     StringBuilder sb = new StringBuilder();
//     String line;

//     try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
//         while ((line = br.readLine()) != null) {
//             sb.append(line);
//         }
//         br.close();
//     }


//     return sb.toString();
// }



}


