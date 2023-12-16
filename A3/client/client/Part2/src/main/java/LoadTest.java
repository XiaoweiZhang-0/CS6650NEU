
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;



public class LoadTest {

    public static double loadTest(String fileName, int threadGroupSize, int numThreadGroups, int delay, String ipAddr, List<Long> allThreadsTimes){
        String baseUri = ipAddr;
        //hard initialization

        // Initialization phase: 10 threads, each calling POST and GET 100 times
        ExecutorService initExecutor = Executors.newFixedThreadPool(10);
        CountDownLatch initLatch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            initExecutor.execute(new ApiTask(fileName, baseUri, 100, initLatch, false));
        }
    
        try {
            initLatch.await(); // Wait for initialization phase to complete
            initExecutor.shutdown();
        } catch (InterruptedException e) {
            System.err.println("Initialization phase interrupted: " + e.getMessage());
        }




        // Executor service to manage threads
        ExecutorService executor = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(threadGroupSize * numThreadGroups);

        long allThreadsStartTime = System.currentTimeMillis();

        for (int i = 0; i < numThreadGroups; i++) {
            for (int j = 0; j < threadGroupSize; j++) {
                executor.execute(new ApiTask(fileName, baseUri, 100, latch, true));
            }

            try {
                Thread.sleep(delay * 1000L); // delay in seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            finally{

            }
        }

        try {
            latch.await(); // Wait for all threads to finish
            executor.shutdown();
            long endTime = System.currentTimeMillis();
            long allThreadsWallTime = endTime - allThreadsStartTime;
            long totalRequests = threadGroupSize * numThreadGroups * 2000L; // 1000 POST/GET pairs per thread

            System.out.println("Wall Time: " + allThreadsWallTime / 1000 + " seconds");
            System.out.println("Throughput: " + (double) totalRequests / (allThreadsWallTime / 1000) + " requests/second");


            allThreadsTimes.add(allThreadsStartTime);
            allThreadsTimes.add(allThreadsWallTime);

            return (double) totalRequests / (allThreadsWallTime / 1000);
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted: " + e.getMessage());
        }
        finally {
        }
        return 0;
    }
    // private static CloseableHttpClient createSharedHttpClient() {
    //         // Configure IO Reactor for asynchronous processing
    //         int connectionTimeout = 4000; 
    //         int socketTimeout = 3000;
    //         int connectionRequestTimeout = 2000; 
            
    //         // Create a custom RequestConfig
    //         RequestConfig requestConfig = RequestConfig.custom()
    //             .setConnectTimeout(connectionTimeout)
    //             .setSocketTimeout(socketTimeout)
    //             .setConnectionRequestTimeout(connectionRequestTimeout)
    //             .build();
            
            
    //         PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();

    //         // Set the maximum number of total open connections.
    //         cm.setMaxTotal(500); // Example, set to your requirement
    //         // Set the maximum number of concurrent connections per route, which is 20 in this case.
    //         cm.setDefaultMaxPerRoute(300); 
    //         CloseableHttpClient sharedHttpClient = HttpClients.custom()
    //         .setConnectionManager(cm)
    //         .setDefaultRequestConfig(requestConfig)
    //         .build();

    //         return sharedHttpClient;
    // }
    
}
