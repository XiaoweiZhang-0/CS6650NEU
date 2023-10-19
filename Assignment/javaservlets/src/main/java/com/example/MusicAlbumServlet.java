package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

@WebServlet(name = "MusicAlbumServlet", urlPatterns = {"/albums/*"})
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
        res.setContentType("application/json");
        // String urlPath = req.getPathInfo();
    
        // // check we have a URL!
        // if (urlPath == null || urlPath.isEmpty()) {
        //     res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        //     res.getWriter().write("missing paramterers");
        //     return;
        // }
        StringBuilder requestBody = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }
        System.out.println(requestBody.toString());
        
        // String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)
    
        // if (!isPostUrlValid(urlParts)) {
        //     res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        // } else {
        //     res.setStatus(HttpServletResponse.SC_OK);
        //     // do any sophisticated processing with urlParts which contains all the url params
        // }
    }
    // private boolean isPostUrlValid(String[] urlParts) {
    //     return true;
    // }



}
