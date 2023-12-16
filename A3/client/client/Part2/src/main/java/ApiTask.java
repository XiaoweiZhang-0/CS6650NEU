import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.api.LikeApi;


public class ApiTask implements Runnable{
    private final String baseUrl;
    private final CountDownLatch latch;
    private final int numIterations;
    private boolean isRecord;
    private static final int MAX_RETRIES = 5;
    private String fileName;
    private String albumID = "c861f8d1-6791-466a-a3e9-49aee42d5e4a";

    public ApiTask(String fileName, String baseUrl,  int numIterations, CountDownLatch latch, boolean isRecord) {
        this.baseUrl = baseUrl;
        this.latch = latch;
        this.numIterations = numIterations;
        this.isRecord = isRecord;
        this.fileName = fileName;
        System.out.println("baseurl: " + baseUrl);
    }


    @Override
    public void run() {
        DefaultApi apiInstance = new DefaultApi();
        apiInstance.getApiClient().setBasePath(baseUrl);
        LikeApi likeApiInstance = new LikeApi();
        likeApiInstance.getApiClient().setBasePath(baseUrl);

        File image = new File("nmtb.png");
        AlbumsProfile profile = new AlbumsProfile("Xiaowei", "test", "2023"); // AlbumsProfile |
        for (int i = 0; i < numIterations; i++) { // 100 POSTs pairs
            // Perform POST Album request
            this.performPostAlbumRequest(apiInstance, image, profile, isRecord);
            // Perform Post review request
            this.performPostReviewRequest(likeApiInstance, "like", albumID, isRecord);
            this.performPostReviewRequest(likeApiInstance, "like", albumID, isRecord);
            this.performPostReviewRequest(likeApiInstance, "dislike", albumID, isRecord);

        }
        latch.countDown();
    }
    
    //Sync post request
    private void performPostAlbumRequest(DefaultApi apiInstance, File image, AlbumsProfile profile, boolean isRecord) {
        
        long startTime = System.currentTimeMillis();
        String requestType = "POSTAlbum";
        int responseCode = 500;
        for(int i = 0; i < MAX_RETRIES; i++){
            // Execute the request
            try {
                int status = apiInstance.newAlbumWithHttpInfo(image, profile).getStatusCode();
                if (status >= 200 && status < 300) {
                    if(isRecord){
                        long latency = System.currentTimeMillis() - startTime;
                        Utilities.recordReqStatstoCSV(this.fileName, startTime, requestType, latency, status);
                    }
                    return;                    
                } else if (status >= 400 && status < 600) {
                    // Retry on 4XX and 5XX
                    responseCode = status;
                    continue;
                }
            }
            catch(ApiException e){
                e.printStackTrace();
            }
        }
        if(isRecord){
                long latency = System.currentTimeMillis() - startTime;
                Utilities.recordReqStatstoCSV(this.fileName, startTime, requestType, latency, responseCode);
        }
        return;
    }

    //Sych get request
    private void performPostReviewRequest(LikeApi likeApiInstance, String likeOrDislike, String albumID, boolean isRecord) {
        long startTime = System.currentTimeMillis();
        String requestType = "POSTReview";
        int responseCode = 500;

        for(int i = 0; i < MAX_RETRIES; i++){
            // Execute the request
            try {
                int status = likeApiInstance.reviewWithHttpInfo(likeOrDislike, albumID).getStatusCode();
                // int status = response.getStatusLine().getStatusCode();

                if (status >= 200 && status < 300) {
                    if(isRecord){
                        long latency = System.currentTimeMillis() - startTime;
                        Utilities.recordReqStatstoCSV(this.fileName, startTime, requestType, latency, status);
                    }
                    return;                    
                } else if (status >= 400 && status < 600) {
                    // Retry on 4XX and 5XX
                    responseCode = status;
                    continue;
                }
            }
            catch(Exception e){
                e.printStackTrace(); 
            }
        }
        if(isRecord){
                long latency = System.currentTimeMillis() - startTime;
                Utilities.recordReqStatstoCSV(this.fileName, startTime, requestType, latency, responseCode);
        }
        return;
    }

}
