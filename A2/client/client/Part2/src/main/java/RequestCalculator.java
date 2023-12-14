package client;

import java.util.concurrent.ConcurrentLinkedQueue;
public class RequestCalculator {
    private int succesRequests;
    private int failedRequests;

    public RequestCalculator(ConcurrentLinkedQueue<Stats> statsQueue){
        for(Stats stats : statsQueue){
            if(stats.getResponseCode() == 200){
                succesRequests++;
            } else {
                failedRequests++;
            }
        }
    }

    public int getSuccesRequests(){
        return this.succesRequests;
    }

    public int getFailedRequests(){
        return this.failedRequests;
    }
}