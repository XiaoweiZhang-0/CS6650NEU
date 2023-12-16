package com.example;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;



@WebServlet(name="ReviewServlet", urlPatterns = {"/review/*"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10,
maxFileSize = 1024 * 1024 * 50,
maxRequestSize = 1024 * 1024 * 100)
public class ReviewServlet extends HttpServlet{
    // private Map<String, Profile> albums = new HashMap<>();
    // private ConcurrentHashMap<String, Profile> albums = new ConcurrentHashMap<>();
    private final static String NOTFOUND = "Album not found";
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
            String pathInfo = req.getQueryString();
            String[] urlParams = pathInfo.split("&");
            
            if(urlParams.length != 2){
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Response response = new ErrorResponse("invalid path " + pathInfo);
                res.getWriter().write(gson.toJson(response));
                return;
            }
            String isLike = urlParams[0].split("=")[1];
            String albumID = urlParams[1].split("=")[1];
            try{
                Send send = new Send();
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
            catch(Exception e){
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                Response response = new ErrorResponse("Internal server error");
                res.getWriter().write(gson.toJson(response));
                return;
            }

        }catch(IOException e){}       

            //send the information to message queue

    }


}
