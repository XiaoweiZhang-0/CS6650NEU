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


public class ApiTask implements Runnable{
    private final String baseUrl;
    private final CountDownLatch latch;
    private final int numIterations;
    private boolean isRecord;
    private static final int MAX_RETRIES = 5;
    private String fileName;


    public ApiTask(String fileName, String baseUrl,  int numIterations, CountDownLatch latch, boolean isRecord) {
        this.baseUrl = baseUrl;
        this.latch = latch;
        this.numIterations = numIterations;
        this.isRecord = isRecord;
        this.fileName = fileName;
    }


    @Override
    public void run() {
        DefaultApi apiInstance = new DefaultApi();
        apiInstance.getApiClient().setBasePath(baseUrl);
        File image = new File("nmtb.png");
        AlbumsProfile profile = new AlbumsProfile("Xiaowei", "test", "2023"); // AlbumsProfile |
        for (int i = 0; i < numIterations; i++) { // 1000 POST/GET pairs
                    // Perform POST request
            this.performPostRequest(apiInstance, image, profile, isRecord);
                    // Perform GET request
            this.performGetRequest(apiInstance);

        }
        latch.countDown();
    }
    
    //Sync post request
    private void performPostRequest(DefaultApi apiInstance, File image, AlbumsProfile profile, boolean isRecord) {
        
        long startTime = System.currentTimeMillis();
        String requestType = "POST";
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
                
            }
        }
        if(isRecord){
                long latency = System.currentTimeMillis() - startTime;
                Utilities.recordReqStatstoCSV(this.fileName, startTime, requestType, latency, responseCode);
        }
        return;
    }

    //Sych get request
    private void performGetRequest(DefaultApi apiInstance) {
        long startTime = System.currentTimeMillis();
        String requestType = "GET";
        int responseCode = 500;

        for(int i = 0; i < MAX_RETRIES; i++){
            // Execute the request
            try {
                int status = apiInstance.getAlbumByKeyWithHttpInfo("8671f879-72cd-4d1b-bb6e-431a9bb5935b").getStatusCode();
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
            catch(ApiException e){

            }
        }
        if(isRecord){
                long latency = System.currentTimeMillis() - startTime;
                Utilities.recordReqStatstoCSV(this.fileName, startTime, requestType, latency, responseCode);
        }
        return;
    }

}
