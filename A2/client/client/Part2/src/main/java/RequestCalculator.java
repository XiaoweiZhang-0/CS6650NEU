

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class RequestCalculator {
    private int succesRequests;
    private int failedRequests;

    public RequestCalculator(String fileName){
        try{
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while((line = bufferedReader.readLine()) != null){
                String[] lineArray = line.split(",");
                if(lineArray[3].equals("200") || lineArray[3].equals("201")){
                    this.succesRequests++;
                }
                else{
                    this.failedRequests++;
                }
            }
            bufferedReader.close();
            fileReader.close();
        }
        catch(IOException e){
            System.out.println("Error reading file");
        }
    }

    public int getSuccesRequests(){
        return this.succesRequests;
    }

    public int getFailedRequests(){
        return this.failedRequests;
    }
}