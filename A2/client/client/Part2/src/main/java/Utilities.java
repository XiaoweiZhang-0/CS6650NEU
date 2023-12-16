
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class Utilities {

    public Utilities() {
    }
    
    //Function to record the request statistics to a csv file
    public static void recordReqStatstoCSV(String fileName, long StartTime, String requestType, long latency, int responseCode){
        Path path = Paths.get(fileName);
        FileWriter csvWriter;
        if(!Files.exists(path)){
            try {
                Files.createFile(path);
                csvWriter = new FileWriter(fileName);
                csvWriter.append("Start Time");
                csvWriter.append(",");
                csvWriter.append("Request Type");
                csvWriter.append(",");
                csvWriter.append("Latency");
                csvWriter.append(",");
                csvWriter.append("Response Code");
                csvWriter.append("\n");
                csvWriter.append(Long.toString(StartTime));
                csvWriter.append(",");
                csvWriter.append(requestType);
                csvWriter.append(",");
                csvWriter.append(Long.toString(latency));
                csvWriter.append(",");
                csvWriter.append(Integer.toString(responseCode));
                csvWriter.append("\n");
                csvWriter.flush();
                csvWriter.close();

            } catch (IOException e) {
            }
        }
        else{
            try{
                csvWriter = new FileWriter(fileName, true);
                csvWriter.append(Long.toString(StartTime));
                csvWriter.append(",");
                csvWriter.append(requestType);
                csvWriter.append(",");
                csvWriter.append(Long.toString(latency));
                csvWriter.append(",");
                csvWriter.append(Integer.toString(responseCode));
                csvWriter.append("\n");
                csvWriter.flush();
                csvWriter.close();
            }
            catch(IOException e){
            }
        }

    }

    //Function to plot the throughput and save it as a png file
    public void plotThroughput(int[] numThreadGroupsArray, List<Double> throughputs, String serverType) {
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






    //Plot throughput per second for particular setup
    public void plotThroughputPerSecond(String fileName, long allThreadsStartTime, long allThreadsWallTime){
        List<Long> completionTimes = new ArrayList<>();
        try{
                FileReader reader = new FileReader(fileName);
                BufferedReader br = new BufferedReader(reader);
                String line = "";
                //skip the first line
                line = br.readLine();
                while((line = br.readLine()) != null){
                    String[] values = line.split(",");
                    completionTimes.add(Long.parseLong(values[0]) + Long.parseLong(values[2]));
                }
                br.close();
            }
            catch(IOException e){
                System.out.println("Error reading file");
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

    //Process the latencies for given request type
    public List<Long> processLatencies(String requestType, String fileName){
        List<Long> latencies = new ArrayList<>();
        List<Long> toReturn = new ArrayList<>();
        
        try{
            FileReader reader = new FileReader(fileName);
            BufferedReader br = new BufferedReader(reader);
            String line = "";
            while((line = br.readLine()) != null){
                String[] values = line.split(",");
                if(values[1].equals(requestType)){
                    latencies.add(Long.parseLong(values[2]));
                }
            }
            br.close();
        }
        catch(IOException e){
            System.out.println("Error reading file");
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



}
