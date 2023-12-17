package com.example;

import java.io.IOException;
// import java.util.logging.LogManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.google.gson.Gson;



@WebServlet(name="ReviewServlet", urlPatterns = {"/review/*"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10,
maxFileSize = 1024 * 1024 * 50,
maxRequestSize = 1024 * 1024 * 100)
public class ReviewServlet extends HttpServlet{
    // private Map<String, Profile> albums = new HashMap<>();
    // private ConcurrentHashMap<String, Profile> albums = new ConcurrentHashMap<>();
    private final static String NOTFOUND = "Album not found";
    private static final Logger logger = LogManager.getLogger(ReviewServlet.class);
    @Override
    public void init() throws ServletException{
        super.init();
    }

    //Post
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res){
        //set the response type
        res.setContentType("application/json");
        try{
            Gson gson = new Gson();
                //get the request parameters
            String pathInfo = req.getPathInfo();
            // logger.error(pathInfo);
            String[] urlParams = pathInfo.split("/");
            
            if(urlParams.length != 3){
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Response response = new ErrorResponse("invalid path " + pathInfo);
                res.getWriter().write(gson.toJson(response));
                return;
            }
            String isLike = urlParams[1];
            String albumID = urlParams[2];
            try{
                try (Send send = new Send()) {
                    String responseMessage = send.sendMsg(albumID + "::" + isLike);
                    if(responseMessage.equals(NOTFOUND)){
                        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        Response response = new ErrorResponse(NOTFOUND);
                        res.getWriter().write(gson.toJson(response));
                        return;
                    }
                    else{
                        res.setStatus(HttpServletResponse.SC_OK);
                        return;
                    }
                }
            }
            catch(Exception e){
                logger.error("ERROR FROM REVIEW SERVLET SEND MESSAGE PART------------------------------------------------------"+e.getMessage());
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                Response response = new ErrorResponse("Internal server error");
                res.getWriter().write(gson.toJson(response));
                return;
            }

        }catch(IOException e){}       
            // logger.error("ERROR FROM REVIEW SERVLET OTHER PART------------------------------------------------------"+e.getMessage());
            //send the information to message queue

    }


}
