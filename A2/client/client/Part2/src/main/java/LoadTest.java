
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LoadTest {

    public static double loadTest(int threadGroupSize, int numThreadGroups, int delay, String ipAddr, ConcurrentLinkedQueue<Stats> reqStatsQueue, List<Long> allThreadsTimes){
        String baseUri = ipAddr;
        //hard initialization
        String postUri = baseUri + "/albums";
        String getUri = baseUri + "/albums?albumID=1";
        
        // Initialization phase: 10 threads, each calling POST and GET 100 times
        ExecutorService initExecutor = Executors.newFixedThreadPool(10);
        CountDownLatch initLatch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            initExecutor.execute(new ApiTask(postUri, getUri, 100, initLatch, false, reqStatsQueue ));
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
                executor.execute(new ApiTask(postUri, getUri, 1000, latch, true, reqStatsQueue));
            }

            try {
                Thread.sleep(delay * 1000L); // delay in seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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
        return 0;
    }
}
