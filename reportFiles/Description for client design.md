Description for client design

The main class of my client is the MultipartRequest class which consists most of the helper functions and subclasses to run the load test.

1. MultipartRequest class:
   1. MAX_RETRIES representing the number of retries the thread should execute when getting a response code other than 200.
   2. reqStatsQueue is a concurrentLinkedQueue to store the statistics including startTime, latency, responseCode and request type for each request. There is separate class called Stats for these information
   3. When the program starts, it will ask for the parameters: threadGroupSize, numThreadGroups, delay, ipAddr, and server type. Then it will construct the uri to be used to execute in the request
   4. Note numThreadGroups can have multiple entries to reflect the requirements of 10, 20, 30 load test
   5. the loadTest function is where the request is made and sent. For each loadTest, it will first execute 10 threads, each calling 100 POST and GET requests to warm up the server
   6. Then, the start time will be marked and threads are created to send the request. After all the requests are processed, the end time will be marked and wallTime will be calculated. The program will output the WallTime and calculated throughput
   7. Note in ApiTask, I created the request using apache httpclient and set the connectionTimeout, socketTimeout, and connectionRequestTimeout as I noticed on rare case there will be unresponsed request which take extremely long time and block the whole thread. Setting timeout would reduce this blocking heavily.
   8. For each request the program makes, it will record the Stats based on an indicator as I do not want to the stats from warmup session.
   9. Get and Post request method are fairly easy to read so I will cover them
      1. For Post request, the album image will be default under the same folder as the client
      2. For Get request, I'm not using random number but query the album ID=1
   10. I have helper functions including:
       1. plotThroughput to plot the throughput in a chart using JFrame
       2. recordReqStatstoCSV to record the stats the program get from each request and store them in a csv file
       3. processLatencies to calculate and return min, max, p99, mean, median latencies of the request for POST and GET
       4. plotThroughputPerSecond to plot the throughput over time from start to the end for step 6.
