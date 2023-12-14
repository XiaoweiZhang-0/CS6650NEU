
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;


public class Main {
    
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
        Utilities utils = new Utilities();

        for(int numThreadGroups : numThreadGroupsArray){
            throughputs.add(LoadTest.loadTest(threadGroupSize, numThreadGroups, delay, postUri));

        }

        //plot the throughput
        utils.plotThroughput(numThreadGroupsArray, throughputs, serverType);
        
    }

    





}