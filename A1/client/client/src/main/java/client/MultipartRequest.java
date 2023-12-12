package client;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import main.java.client.Stats;

import org.jfree.chart.ChartUtils;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.axis.NumberTickUnit;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;




public class MultipartRequest {
    
    private static final int MAX_RETRIES = 5;
    private static ConcurrentLinkedQueue<Stats> reqStatsQueue = new ConcurrentLinkedQueue<>();

    private static long allThreadsWallTime = 0;
    private static long allThreadsStartTime = 0;
    public static void main(String[] args) {

        //Ask user for threadGroupSize, numThreadGroups, delay, ipAddr
        System.out.println("Enter threadGroupSize: ");
        Scanner scanner = new Scanner(System.in);
        int threadGroupSize = Integer.parseInt(scanner.nextLine());
        System.out.println("Enter numThreadGroups, separated by space:");
        String numThreadGroupsString = scanner.nextLine();
        String[] numThreadGroupsStringArray = numThreadGroupsString.split(" ");
        int[] numThreadGroupsArray = new int[numThreadGroupsStringArray.length];
        for(int i = 0; i < numThreadGroupsStringArray.length; i++){
            numThreadGroupsArray[i] = Integer.parseInt(numThreadGroupsStringArray[i]);
        }
        System.out.println("Enter delay: ");
        int delay = scanner.nextInt();
        System.out.println("Enter ipAddr: ");
        String ipAddr = scanner.next();
        System.out.println("Specify server type: Java or Go");
        String serverType = scanner.next();
        scanner.close();

        String baseUri = "http://";
        String postUri;
        if(serverType.toLowerCase().equals("java")){
            
            postUri = baseUri + ipAddr + ":8080/JavaServlets-1.0";
        }
        else{
            postUri = baseUri + ipAddr + ":8080";
        }

        List<Double> throughputs = new ArrayList<>();

        for(int numThreadGroups : numThreadGroupsArray){

            reqStatsQueue.clear();
            throughputs.add(loadTest(threadGroupSize, numThreadGroups, delay, postUri));
            
            String fileName;
            if(serverType.toLowerCase().equals("java")){
                fileName = "reqStatsJava" + numThreadGroups + ".csv";
            }
            else{
                fileName = "reqStatsGo" + numThreadGroups + ".csv";
            }
            //write out the request stats into a csv file
            recordReqStatstoCSV(fileName);

            // calculate mean, median, p99, min, max for POST
            List<Long> postLatencies = processLatencies("POST");
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups POST mean response time: " + postLatencies.get(0) + " millisecs");
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups POST median response time: " + postLatencies.get(1) + " millisecs");
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups POST p99 response time: " + postLatencies.get(2) + " millisecs");
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups POST min response time: " + postLatencies.get(3) + " millisecs");
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups POST max response time: " + postLatencies.get(4) + " millisecs");

            //calculate mean, median, p99, min, max for GET
            List<Long> getLatencies = processLatencies("GET");
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups GET mean response time: " + getLatencies.get(0) + " millisecs");
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups GET median response time: " + getLatencies.get(1) + " millisecs");
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups GET p99 response time: " + getLatencies.get(2) + " millisecs");
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups GET min response time: " + getLatencies.get(3) + " millisecs");
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups GET max response time: " + getLatencies.get(4) + " millisecs");

        }

        //plot the throughput
        plotThroughput(numThreadGroupsArray, throughputs, serverType);
        
        //Step 6: plot the throughput per second
        plotThroughputPerSecond();

    }

    private static double loadTest(int threadGroupSize, int numThreadGroups, int delay, String ipAddr){
        String baseUri = ipAddr;
        //hard initialization
        String postUri = baseUri + "/albums";
        String getUri = baseUri + "/albums?albumID=1";
        
        // Initialization phase: 10 threads, each calling POST and GET 100 times
        ExecutorService initExecutor = Executors.newFixedThreadPool(10);
        CountDownLatch initLatch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            initExecutor.execute(new ApiTask(postUri, getUri, 100, initLatch, false));
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

        allThreadsStartTime = System.currentTimeMillis();

        for (int i = 0; i < numThreadGroups; i++) {
            for (int j = 0; j < threadGroupSize; j++) {
                executor.execute(new ApiTask(postUri, getUri, 1000, latch, true));
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
            allThreadsWallTime = endTime - allThreadsStartTime;
            long totalRequests = threadGroupSize * numThreadGroups * 2000L; // 1000 POST/GET pairs per thread

            System.out.println("Wall Time: " + allThreadsWallTime / 1000 + " seconds");
            System.out.println("Throughput: " + (double) totalRequests / (allThreadsWallTime / 1000) + " requests/second");
            return (double) totalRequests / (allThreadsWallTime / 1000);
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted: " + e.getMessage());
        }
        return 0;
    }
    private static class ApiTask implements Runnable {
        private final String postUri;
        private final String getUri;
        private final CountDownLatch latch;
        private final int numIterations;
        private boolean isRecord;

        public ApiTask(String postUri, String getUri,  int numIterations, CountDownLatch latch, boolean isRecord) {
            this.postUri = postUri;
            this.getUri = getUri;
            this.latch = latch;
            this.numIterations = numIterations;
            this.isRecord = isRecord;
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

    //Function to plot the throughput and save it as a png file
    public static void plotThroughput(int[] numThreadGroupsArray, List<Double> throughputs, String serverType) {
        // Create a dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    
        for (int i = 0; i < numThreadGroupsArray.length; i++) {
            dataset.addValue(throughputs.get(i), "Throughput", Integer.toString(numThreadGroupsArray[i]));
        }
    
        // Create a chart
        JFreeChart chart = ChartFactory.createLineChart(
                "Load Test Throughput", // Chart title
                "Number of Thread Groups", // X-axis label
                "Throughput (requests/sec)", // Y-axis label
                dataset,
                PlotOrientation.VERTICAL,
                true,  // Show legend
                true,
                false
        );
        
        // Save the chart as a PNG
        try {
            ChartUtils.saveChartAsPNG(new File(serverType + "ThroughputChart.png"), chart, 800, 600);
            System.out.println("Chart saved as "+serverType+"'ThroughputChart.png'");
        } catch (IOException e) {
            System.err.println("Error saving chart: " + e.getMessage());
        }

        // Display the chart
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Throughput Chart");
            frame.setContentPane(new ChartPanel(chart));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
    }

    private static void recordReqStatstoCSV(String fileName){
        try {
            FileWriter csvWriter = new FileWriter(fileName);
            csvWriter.append("Start Time");
            csvWriter.append(",");
            csvWriter.append("Request Type");
            csvWriter.append(",");
            csvWriter.append("Latency");
            csvWriter.append(",");
            csvWriter.append("Response Code");
            csvWriter.append("\n");
            for (Stats stat : reqStatsQueue) {
                csvWriter.append(Long.toString(stat.getStartTime()));
                csvWriter.append(",");
                csvWriter.append(stat.getRequestType());
                csvWriter.append(",");
                csvWriter.append(Long.toString(stat.getLatency()));
                csvWriter.append(",");
                csvWriter.append(Integer.toString(stat.getResponseCode()));
                csvWriter.append("\n");
            }
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
        }
    }


    private static List<Long> processLatencies(String requestType){
        List<Long> latencies = new ArrayList<>();
        List<Long> toReturn = new ArrayList<>();
        for(Stats stat : reqStatsQueue){
            if(stat.getRequestType().equals(requestType)){
                latencies.add(stat.getLatency());
            }
        }
        Collections.sort(latencies);
        //get mean
        double sum = 0;
        int size = 0;
        for(Long latency : latencies){
            sum += latency;
            size++;
        }

        toReturn.add((long) (sum / size));
        //get median
        if(size % 2 == 0){
            toReturn.add((latencies.get(size / 2) + latencies.get(size / 2 - 1)) / 2);
        }
        else{
            toReturn.add(latencies.get(size / 2));
        }
        //get p99
        toReturn.add(latencies.get((int) Math.ceil(0.99 * size)));
        //get min
        toReturn.add(latencies.get(0));
        //get max
        toReturn.add(latencies.get(size - 1));

        return toReturn;
    }

    private static void plotThroughputPerSecond(){
        List<Long> completionTimes = new ArrayList<>();
        for(Stats stat : reqStatsQueue){
            completionTimes.add(stat.getStartTime() + stat.getLatency());
        }
        Collections.sort(completionTimes);
        long startTime = allThreadsStartTime;
        Map<Integer, Integer> requestsPerSecond = new TreeMap<>();
        for (long timestamp : completionTimes) {
            int second = (int)((timestamp - startTime) / 1000);
            requestsPerSecond.put(second, requestsPerSecond.getOrDefault(second, 0) + 1);
        }


        // Create a dataset
        XYSeries series = new XYSeries("Throughput");
        int endTimeInSeconds = (int) (allThreadsWallTime / 1000); 
        // Assuming you have a method to get throughput for each second
        for (int i = 0; i <= endTimeInSeconds; i++) {
            series.add(i, requestsPerSecond.getOrDefault(i, 0));
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);

        // Create a chart
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Load Test Throughput Per Second",
            "Time (Seconds)",
            "Throughput (requests/sec)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        //change settings for chart to make it more readable
        XYPlot plot = chart.getXYPlot();
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());


        // Save the chart as a PNG
        try {
            ChartUtils.saveChartAsPNG(new File("ThroughputPerSecondChart.png"), chart, 800, 600);
            System.out.println("Chart saved as 'ThroughputPerSecondChart.png'");
        } catch (IOException e) {
            System.err.println("Error saving chart: " + e.getMessage());
        }

        // Display the chart
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Throughput Per Second Chart");
            frame.setContentPane(new ChartPanel(chart));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });

    }


}