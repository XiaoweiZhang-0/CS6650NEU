import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


public class ApiTask implements Runnable{
    private final String postUri;
    private final String getUri;
    private final CountDownLatch latch;
    private final int numIterations;
    private boolean isRecord;
    private static final int MAX_RETRIES = 5;
    private ConcurrentLinkedQueue<Stats> reqStatsQueue;


    public ApiTask(String postUri, String getUri,  int numIterations, CountDownLatch latch, boolean isRecord, ConcurrentLinkedQueue<Stats> reqStatsQueue) {
        this.postUri = postUri;
        this.getUri = getUri;
        this.latch = latch;
        this.numIterations = numIterations;
        this.isRecord = isRecord;
        this.reqStatsQueue = reqStatsQueue;
    }

    @Override
    public void run() {
        List<Stats> reqStats = new ArrayList<>();
        int connectionTimeout = 20* 100; // milliseconds
        int socketTimeout = 30 * 100; //  milliseconds
        int connectionRequestTimeout = 2 * 100; //  milliseconds

        RequestConfig requestConfig = RequestConfig.custom()
        .setConnectTimeout(connectionTimeout)
        .setSocketTimeout(socketTimeout)
        .setConnectionRequestTimeout(connectionRequestTimeout)
        .build();
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            
            
            // Create MultipartEntityBuilder
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    
            // Add JSON part
            String json = "{\"artist\":\"Sex Pistols\", \"title\":\"Never Mind The Bollocks!\", \"year\":\"1977\"}";
            builder.addTextBody("profile", json, ContentType.APPLICATION_JSON);

            // Add file part
            File file = new File("nmtb.png");
            builder.addBinaryBody("image", file, ContentType.IMAGE_PNG, "image.png");

            // Build the multipart entity
            HttpEntity multipart = builder.build();


            for (int i = 0; i < numIterations; i++) { // 1000 POST/GET pairs
                // Perform POST request
                HttpPost uploadFile = new HttpPost(postUri);
                uploadFile.setEntity(multipart);
                HttpGet request = new HttpGet(getUri);

                if(isRecord){
                    Stats postStat = performPostRequest(httpClient, uploadFile);
                    // reqStatsQueue.add(postStat);
                    reqStats.add(postStat);

                    Stats getStat = performGetRequest(httpClient, request);
                    // reqStatsQueue.add(getStat);
                    reqStats.add(getStat);
                }
                else{
                    performPostRequest(httpClient, uploadFile);
                    performGetRequest(httpClient, request);
                }

            }
            reqStatsQueue.addAll(reqStats);
        } 
        catch(IOException e){

        }finally {
            latch.countDown();
        }
    }

    private Stats performPostRequest(CloseableHttpClient httpClient, HttpPost uploadFile) {

                // before sending out the request, mark the start time
                long startTime = System.currentTimeMillis();
                int responseCode = 0;
                long endTime;
                long latency;
                String requestType = "POST";

                // Create a POST request
              

                for(int i = 0; i < MAX_RETRIES; i++){
                    // Execute the request
                    try (CloseableHttpResponse response = httpClient.execute(uploadFile)) {
                        int status = response.getStatusLine().getStatusCode();
                        responseCode = status;
                        if (status >= 200 && status < 300) {
                            // Success
                            endTime = System.currentTimeMillis();
                            latency = endTime - startTime;
                            Stats stats = new Stats(startTime, requestType, latency, responseCode);
                            return stats;
                            

                        } else if (status >= 400 && status < 600) {
                            // Retry on 4XX and 5XX
                            continue;
                        }
                    }
                    catch(IOException e){

                    }
                }
                endTime = System.currentTimeMillis();
                latency = endTime - startTime;
                Stats stats = new Stats(startTime, requestType, latency, responseCode);
                return stats;

    }

    private Stats performGetRequest(CloseableHttpClient httpClient, HttpGet request) {
        long startTime = System.currentTimeMillis();
        int responseCode = 0;
        long endTime;
        long latency;
        String requestType = "GET";
        for (int i = 0; i < MAX_RETRIES; i++) {
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int status = response.getStatusLine().getStatusCode();
                    responseCode = status;

                    if (status >= 200 && status < 300) {
                        // Success, process response if needed
                        endTime = System.currentTimeMillis();
                        latency = endTime - startTime;
                        Stats stats = new Stats(startTime, requestType, latency, responseCode);
                        return stats;
                    } else if (status >= 400 && status < 600) {
                        // Retry on 4XX and 5XX
                        continue;
                    }
                }
                catch(IOException e){
                    
                }
        }
        endTime = System.currentTimeMillis();
        latency = endTime - startTime;
        Stats stats = new Stats(startTime, requestType, latency, responseCode);
        return stats;
    }
}
