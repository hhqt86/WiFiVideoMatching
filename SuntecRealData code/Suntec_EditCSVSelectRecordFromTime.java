package SuntecRealData;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
 
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

 
public class Suntec_EditCSVSelectRecordFromTime
{
	static String wifiFolder = "src/SuntecRealData/BigScreenExp/";
	static File inputFile = new File(wifiFolder + "location_archival_rtls_2017_7_23.csv");
	static int numberOfAttribute = 9;
    static String line = "";
    static String cvsSplitBy = ",";
    static String[] elements = new String[numberOfAttribute];
    static String[][] elementEditCSVInputs = new String[1000000][numberOfAttribute];
    static int[] id = new int[1000000];
    static String filenameFull;
    static String filenameRefineTime;
    
	public static int getTimeStep(String time, int startingHour, int startingMinute, int startingSecond) {
		int currentHour = Integer.parseInt(time.substring(0, 2));
		int currentMinute = Integer.parseInt(time.substring(3, 5));
		int currentSecond = Integer.parseInt(time.substring(6, 8));
		return (currentHour - startingHour) * 3600 + (currentMinute - startingMinute) * 60 + (currentSecond - startingSecond);
		
	}
    public static void selectFromTimeToTime () {    	
       int year = 2017;
       int month = 7;
       int day = 23;
	   int startingHour = 12;
	   int startingMinute = 28;
	   int startingSecond = 30;
	   int endingHour = 12;
	   int endingMinute = 33;
	   int endingSecond = 00;
       try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
    	   filenameFull = wifiFolder + "full_Suntec_" + startingHour + "_" + startingMinute + "_" + (startingSecond - 1) + "_to_" + endingHour + "_" + endingMinute + "_" + (endingSecond - 1) + ".csv";
    	   FileWriter outputFile = new FileWriter(filenameFull); 
    	   CSVWriter writer = new CSVWriter(outputFile);
    	   int iteration = 1;
    	   line = br.readLine();
    	   writer.writeNext(line.split(cvsSplitBy));
           while ((line = br.readLine()) != null) {
        	   String time = line.substring(11, 19);
        	   elements =line.split(cvsSplitBy);
        	   int startTimeStep = 0;
        	   int currentTimeStep = getTimeStep(time, startingHour, startingMinute, startingSecond) / 5; //each time step is 5 seconds;
        	   int endTimeStep = ((endingHour - startingHour) * 3600 + (endingMinute - startingMinute) * 60 + (endingSecond - startingSecond)) / 5;
        	   if (currentTimeStep >= startTimeStep && currentTimeStep <= endTimeStep) {
        		   String[] lineOut = new String[numberOfAttribute];
                   for (int i = 0; i <numberOfAttribute; i++) {
                	   lineOut[i] = elements[i].substring(0, elements[i].length());
                   }
                   writer.writeNext(lineOut); 
        	   }
        	   iteration++;
           }           
           writer.close(); 
           refineTime();

       } catch (IOException e) {
           e.printStackTrace();
       }	     
    }
    
    public static void refineTime() {
        try (BufferedReader br = new BufferedReader(new FileReader(filenameFull))) {
        	filenameRefineTime = filenameFull.replace(".csv", "_refineTime.csv");
        	//String filenameOutput = filenameFull.replace("full", "output");		
     	   FileWriter outputFile = new FileWriter(filenameRefineTime); 
     	   CSVWriter writer = new CSVWriter(outputFile);
     	   int iteration = 0;
     	   int countConnect = 0;
            while ((line = br.readLine()) != null) {
                // use comma as separator        	   
         	    elementEditCSVInputs[iteration] =line.split(cvsSplitBy);
                String[] lineOut = new String[numberOfAttribute];
                lineOut[0] = elementEditCSVInputs[iteration][0];
                if (iteration != 0) { //if time is :01 then minus 0 second; x is the minus time
                	int x = 0; 
                	lineOut[0] = elementEditCSVInputs[iteration][0].substring(1, elementEditCSVInputs[iteration][0].length() - 2) + (Integer.parseInt(elementEditCSVInputs[iteration][0].substring(elementEditCSVInputs[iteration][0].length() - 2,elementEditCSVInputs[iteration][0].length() - 1)) - x);
                }
                else {
                	lineOut[0] = elementEditCSVInputs[iteration][0].substring(1, elementEditCSVInputs[iteration][0].length() - 1);
                }
                for (int i = 1; i <numberOfAttribute; i++) {
             	   lineOut[i] = elementEditCSVInputs[iteration][i].substring(1, elementEditCSVInputs[iteration][i].length() - 1);
                }
                writer.writeNext(lineOut); 
                iteration++;
            }
            writer.close(); 

        } catch (IOException e) {
            e.printStackTrace();
        }	     
    	
    }
    
    public static void refineID () {
       int maxId = 0;
       try (BufferedReader br = new BufferedReader(new FileReader(filenameRefineTime))) {
       //try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
    	   String filenameOutput = filenameFull.replace("full", "output");
    	   //String filenameOutput = wifiFolder + "dataRefineID.csv";
    	   FileWriter outputFile = new FileWriter(filenameOutput); 
    	   //CSVWriter writer = new CSVWriter(outputFile);
    	   CSVWriter writer = new CSVWriter(outputFile, ',',
    			    CSVWriter.NO_QUOTE_CHARACTER,
    			    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
    			    CSVWriter.RFC4180_LINE_END);
    	   int iteration = 0;
    	   int countConnect = 0;
           while ((line = br.readLine()) != null) {
               // use comma as separator        	   
        	   elementEditCSVInputs[iteration] =line.split(cvsSplitBy);
        	   id[iteration] = maxId;
        	   if (elementEditCSVInputs[iteration][6].equals("\"t\"")) {
        		   countConnect++;
        	   }      
        	   for (int i = iteration - 1; i > 0; i--) {
        		   if (elementEditCSVInputs[iteration][1].equals(elementEditCSVInputs[i][1])) {
        			   id[iteration] = id[i];
        			   maxId--;
        			   if (elementEditCSVInputs[iteration][6].equals("\"t\"")) {
        				   countConnect--;
        			   }
        			   break;
        		   }  		   
        	   }
               String[] lineOut = new String[numberOfAttribute];
               lineOut[0] = elementEditCSVInputs[iteration][0].substring(1, elementEditCSVInputs[iteration][0].length() - 1);
               if (iteration != 0) {
            	   lineOut[1] = id[iteration]+"";
               }
               else {
            	   lineOut[1] = "id";
               }
               for (int i = 2; i <numberOfAttribute; i++) {
            	   lineOut[i] = elementEditCSVInputs[iteration][i].substring(1, elementEditCSVInputs[iteration][i].length() - 1);
               }
               writer.writeNext(lineOut); 
               iteration++;
               maxId++;
           }           
           writer.close(); 

       } catch (IOException e) {
           e.printStackTrace();
       }	     

    }

    
    public static void main(String[] args) {
    	selectFromTimeToTime ();    	
    	refineID ();
	}
}