
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;



public class Main {
    private static long allThreadsStartTime;
    private static long allThreadsWallTime;
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
            
            postUri = baseUri + ipAddr + ":80/JavaServlets-1.0";
        }
        else{
            postUri = baseUri + ipAddr+":8080";
        }

        List<Double> throughputs = new ArrayList<>();
        List<Long> allThreadsTimes = new ArrayList<>();
        Utilities utils = new Utilities();
        List<String> fileNames = new ArrayList<>();

        for(int numThreadGroups : numThreadGroupsArray){
            allThreadsTimes.clear();
            String fileName;
            if(serverType.toLowerCase().equals("java")){
                fileName = "reqStatsJava" + numThreadGroups + ".csv";
            }
            else{
                fileName = "reqStatsGo" + numThreadGroups + ".csv";
            }
            throughputs.add(LoadTest.loadTest(fileName, threadGroupSize, numThreadGroups, delay, postUri, allThreadsTimes));
            fileNames.add(fileName);

            
            //print out the configuration of the test
            System.out.println("Number of thread groups: " + numThreadGroups);
            System.out.println("Thread group size: " + threadGroupSize);
            System.out.println("Delay: " + delay);

            // calculate mean, median, p99, min, max for POST
            List<Long> postLatencies = utils.processLatencies("POST", fileName);
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups POST mean response time: " + postLatencies.get(0) + " millisecs");
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups POST median response time: " + postLatencies.get(1) + " millisecs");
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups POST p99 response time: " + postLatencies.get(2) + " millisecs");
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups POST min response time: " + postLatencies.get(3) + " millisecs");
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups POST max response time: " + postLatencies.get(4) + " millisecs");

            //calculate mean, median, p99, min, max for GET
            List<Long> getLatencies = utils.processLatencies("GET", fileName);
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups GET mean response time: " + getLatencies.get(0) + " millisecs");
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups GET median response time: " + getLatencies.get(1) + " millisecs");
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups GET p99 response time: " + getLatencies.get(2) + " millisecs");
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups GET min response time: " + getLatencies.get(3) + " millisecs");
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups GET max response time: " + getLatencies.get(4) + " millisecs");

            //print out the number of successful and failed requests
            RequestCalculator reqCalc = new RequestCalculator(fileName);
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups successful requests: " + reqCalc.getSuccesRequests());
            System.out.println(serverType + " server @"+ numThreadGroups +" numThreadGroups failed requests: " + reqCalc.getFailedRequests());
        }

        //plot the throughput
        utils.plotThroughput(numThreadGroupsArray, throughputs, serverType);
        
        String fileName = fileNames.get(fileNames.size() - 1);
        //Step 6: plot the throughput per second
        allThreadsStartTime = allThreadsTimes.get(0);
        allThreadsWallTime = allThreadsTimes.get(1);
        utils.plotThroughputPerSecond(fileName, allThreadsStartTime, allThreadsWallTime);
    }

    





}