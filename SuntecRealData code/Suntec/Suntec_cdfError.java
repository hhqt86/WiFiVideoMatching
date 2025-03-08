package SuntecRealData.Suntec;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
 
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

class ID_Coordinate {
	public int ID;
	public Coordinate coordinate;
	public ID_Coordinate(int ID, Coordinate coordinate) {
		this.ID = ID;
		this.coordinate = new Coordinate(coordinate.x, coordinate.y);
	}
}

public class Suntec_cdfError{
	static Coordinate[] fpCoordinate = new Coordinate[100];
	static String wifiFolder = "src/SuntecRealData/SuntecCDFError/";
	static File inputFile = new File(wifiFolder + "location_rtls_2017_7_22_13_sort.csv");
	static int numberOfAttribute = 9;
    static String line = "";
    static String cvsSplitBy = ",";
    static String[] elements = new String[numberOfAttribute];
    static String[][] elementEditCSVInputs = new String[1000000][numberOfAttribute];
    static int[] id = new int[1000000];
    static String filenameFull;
    static String filenameRefineTime;
    int year = 2017;
    int month = 7;
    int day = 22;
	static int startingHour = 13;
	static int startingMinute = 19;
	static int startingSecond = 28;
	static int endingHour = 13;
	static int endingMinute = 27;
	static int endingSecond = 55;
	static ID_Coordinate[][] peopleWifi = new ID_Coordinate[1000][200];
	static ID_Coordinate[][] peopleVideo = new ID_Coordinate[1000][200];
	static int numberPeopleVideoAtLevel1AtEachTimeStep[] = new int[1000];
	static int numberPeopleWifiAtLevel1AtEachTimeStep[] = new int[1000];
	static int durationTimeStep = 0;
	public static int getTimeStep(String time, int startingHour, int startingMinute, int startingSecond) {
		int currentHour = Integer.parseInt(time.substring(0, 2));
		int currentMinute = Integer.parseInt(time.substring(3, 5));
		int currentSecond = Integer.parseInt(time.substring(6, 8));
		return (currentHour - startingHour) * 3600 + (currentMinute - startingMinute) * 60 + (currentSecond - startingSecond);
		
	}
    public static void selectFromTimeToTime () {    	

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

    public static void createMap() {
		fpCoordinate[63] = new Coordinate(1,5.5);
		fpCoordinate[56] = new Coordinate(7.1,3);
		fpCoordinate[33] = new Coordinate(15,5.2);
		fpCoordinate[19] = new Coordinate(19,3.5);
		fpCoordinate[85] = new Coordinate(25.3,5.2);
		fpCoordinate[76] = new Coordinate(28.5,5.4);
		fpCoordinate[42] = new Coordinate(13,4); //stair
		fpCoordinate[27] = new Coordinate(16,4); //stair
		fpCoordinate[1] = new Coordinate(28.5,2.5);
		fpCoordinate[2] = new Coordinate(27.5,2);
		fpCoordinate[3] = new Coordinate(26.8,2.5);
		fpCoordinate[4] = new Coordinate(26,2);
		fpCoordinate[5] = new Coordinate(25.2,2.5);
		fpCoordinate[6] = new Coordinate(24.5,2);
		fpCoordinate[7] = new Coordinate(23.5,2.5);
		fpCoordinate[8] = new Coordinate(23,2);
		fpCoordinate[9] = new Coordinate(22,2.5);
		fpCoordinate[10] = new Coordinate(22,3.5);
		fpCoordinate[11] = new Coordinate(21.2,2);
		fpCoordinate[12] = new Coordinate(21.2,3);
		fpCoordinate[13] = new Coordinate(21,3.5);
		fpCoordinate[14] = new Coordinate(20.5,2.5);
		fpCoordinate[15] = new Coordinate(20.2,3);
		fpCoordinate[16] = new Coordinate(19.8,2);
		fpCoordinate[17] = new Coordinate(19.8,3.5);
		fpCoordinate[18] = new Coordinate(19,2.5);
		fpCoordinate[19] = new Coordinate(19,3.5);
		fpCoordinate[20] = new Coordinate(18.2,2);
		fpCoordinate[21] = new Coordinate(18.2,3);
		fpCoordinate[22] = new Coordinate(17.5,2.5);
		fpCoordinate[23] = new Coordinate(17.5,3.5);
		fpCoordinate[24] = new Coordinate(16.5,2);
		fpCoordinate[25] = new Coordinate(16.5,3);
		fpCoordinate[26] = new Coordinate(15.8,2.5);
		fpCoordinate[27] = new Coordinate(15.8,3.8);
		fpCoordinate[28] = new Coordinate(15.8,5.4);
		fpCoordinate[29] = new Coordinate(15.2,3);
		fpCoordinate[30] = new Coordinate(15.2,4.5);
		fpCoordinate[31] = new Coordinate(14.8,2.3);
		fpCoordinate[32] = new Coordinate(14.8,3.8);
		fpCoordinate[33] = new Coordinate(14.8,5.3);
		fpCoordinate[34] = new Coordinate(14.5,3);
		fpCoordinate[35] = new Coordinate(14.5,4.5);
		fpCoordinate[36] = new Coordinate(14,2.3);
		fpCoordinate[37] = new Coordinate(14,3);
		fpCoordinate[38] = new Coordinate(14,5.3);
		fpCoordinate[39] = new Coordinate(13.5,3);
		fpCoordinate[40] = new Coordinate(13.5,4.5);
		fpCoordinate[41] = new Coordinate(13,2.3);
		fpCoordinate[42] = new Coordinate(13,3.8);
		fpCoordinate[43] = new Coordinate(12,5.3);
		fpCoordinate[44] = new Coordinate(12,2);
		fpCoordinate[45] = new Coordinate(12,3);
		fpCoordinate[46] = new Coordinate(11.3,2.5);
		fpCoordinate[47] = new Coordinate(11.3,3.5);
		fpCoordinate[48] = new Coordinate(10.5,2);
		fpCoordinate[49] = new Coordinate(10.5,3);
		fpCoordinate[50] = new Coordinate(9.6,2.5);
		fpCoordinate[51] = new Coordinate(9,2);
		fpCoordinate[52] = new Coordinate(9,3);
		fpCoordinate[53] = new Coordinate(8.2,2.5);
		fpCoordinate[54] = new Coordinate(8.2,3.5);
		fpCoordinate[55] = new Coordinate(7.5,2);
		fpCoordinate[56] = new Coordinate(7.5,3);
		fpCoordinate[57] = new Coordinate(6.5,1);
		fpCoordinate[58] = new Coordinate(6.5,2.5);
		fpCoordinate[59] = new Coordinate(5.8,1);
		fpCoordinate[60] = new Coordinate(5.8,2);
		fpCoordinate[61] = new Coordinate(1,5.5);
		fpCoordinate[62] = new Coordinate(1,5.5);
		fpCoordinate[63] = new Coordinate(1,5.5);
		fpCoordinate[64] = new Coordinate(1,5.5);
		fpCoordinate[65] = new Coordinate(1,5.5);
		fpCoordinate[66] = new Coordinate(1,5.5);
		fpCoordinate[67] = new Coordinate(1,5.5);
		fpCoordinate[76] = new Coordinate(27.5,4.5);
		fpCoordinate[77] = new Coordinate(27.5,4.5);
		fpCoordinate[78] = new Coordinate(27.5,4.5);
		fpCoordinate[79] = new Coordinate(27.5,4.5);
		fpCoordinate[80] = new Coordinate(27.5,4.5);
		fpCoordinate[81] = new Coordinate(27.5,4.5);
		fpCoordinate[82] = new Coordinate(27.5,4.5);
		fpCoordinate[83] = new Coordinate(27.5,4.5);
		fpCoordinate[84] = new Coordinate(25,4);
		fpCoordinate[85] = new Coordinate(25,5);
		fpCoordinate[86] = new Coordinate(23.5,5.2);
		fpCoordinate[87] = new Coordinate(23.5,5.2);
    }
    
    public static void readWifiFile() {
   	 int count = 0;
   	 String filenameInput = wifiFolder + "output_Suntec_" + startingHour + "_" + startingMinute + "_" + startingSecond + "_to_" + endingHour + "_" + endingMinute + "_" + endingSecond + ".csv";
	     try (BufferedReader br = new BufferedReader(new FileReader(filenameInput))) {
	    	 while ((line = br.readLine()) != null) {
	    		 count++;
	      	   String time = line.substring(11, 19);
	      	   elements =line.split(cvsSplitBy);
	      	   int currentTimeStep = getTimeStep(time, startingHour, startingMinute, startingSecond) / 5; //each time step is 5 seconds;
	      	   int ID = Integer.parseInt(elements[1]);
	      	   if (elements[2].charAt(4) == '1') {//is at level 1
	      		   Coordinate location = fpCoordinate[Integer.parseInt(elements[2].substring(elements[2].length()-3, elements[2].length()))];	      	   
	      		   peopleWifi[currentTimeStep][numberPeopleWifiAtLevel1AtEachTimeStep[currentTimeStep]] = new ID_Coordinate(ID,location);
	      		   numberPeopleWifiAtLevel1AtEachTimeStep[currentTimeStep]++;
	      		   
	      	   }
	
	    	 }
	     }
	     catch (IOException e) {
	         e.printStackTrace();
	     }	
    }
    
    public static void readVideoFile() {
      	 
      	 String filenameInput = wifiFolder + "VideoAfterProcess7.csv";
   	     try (BufferedReader br = new BufferedReader(new FileReader(filenameInput))) {
   	    	 while ((line = br.readLine()) != null) {   	      	   
   	      	   elements =line.split(cvsSplitBy);
   	      	   if ((Integer.parseInt(elements[1]) - 3169) % 5 == 0) {
	   	      	   int currentTimeStep = (Integer.parseInt(elements[1]) - 3169) / 5;
	   	      	   if (currentTimeStep > durationTimeStep) {
	   	      		   durationTimeStep = currentTimeStep;
	   	      	   }
	   	      	   int ID = Integer.parseInt(elements[0].substring(3));
	   	      	   Coordinate location = new Coordinate(Double.parseDouble(elements[3]),Double.parseDouble(elements[4]));
	   	      	   peopleVideo[currentTimeStep][numberPeopleVideoAtLevel1AtEachTimeStep[currentTimeStep]] = new ID_Coordinate(ID,location);
	   	      	   numberPeopleVideoAtLevel1AtEachTimeStep[currentTimeStep]++;
   	      	   }
   	    	 }
   	     }
   	     catch (IOException e) {
   	         e.printStackTrace();
   	     }	
       }
    
    public static double distance (ID_Coordinate video, ID_Coordinate wifi) {
    	return Math.sqrt(Math.pow(video.coordinate.x - wifi.coordinate.x, 2) + Math.pow(video.coordinate.y - wifi.coordinate.y, 2)); 
    }
    
    public static void findBoundingError() {
    	double boundingError[][] = new double[2][100]; //2: min and max, 100: number of signal;
    	int countBoundingError[][] = new int[2][100];
    	boolean dau[] = new boolean [10000];
    	for (int timeStep = 0; timeStep < durationTimeStep; timeStep++) {
    		//for minDistance
    		for (int k = 0; k < dau.length; k++) {
    			dau[k] = false;
    		}
    		double minDistance = 0;  
    		int countDistance = 0;
    		int save = -1;
    		for (int i = 0; i < numberPeopleWifiAtLevel1AtEachTimeStep[timeStep]; i++) {
    			ID_Coordinate wifi = peopleWifi[timeStep][i];
    			Double min = Double.MAX_VALUE;
    			for (int j = 0; j < numberPeopleVideoAtLevel1AtEachTimeStep[timeStep]; j++) {
    				ID_Coordinate video = peopleVideo[timeStep][j];
    				if (dau[video.ID] == false && distance(video, wifi) < min) {
    					min = distance(video,wifi);    					
    					save = j;
    				}
    			}
    			dau[save] = true;
    			minDistance += min;
    			countDistance++;
    		}
    		minDistance = minDistance / countDistance;
    		boundingError[0][numberPeopleWifiAtLevel1AtEachTimeStep[timeStep]] += minDistance;
    		countBoundingError[0][numberPeopleWifiAtLevel1AtEachTimeStep[timeStep]]++;
    		//for maxDistance
    		for (int k = 0; k < dau.length; k++) {
    			dau[k] = false;
    		}
    		double maxDistance = 0;   
    		countDistance = 0;
    		save = -1;
    		for (int i = 0; i < numberPeopleWifiAtLevel1AtEachTimeStep[timeStep]; i++) {
    			ID_Coordinate wifi = peopleWifi[timeStep][i];
    			Double max = 0.0;
    			for (int j = 0; j < numberPeopleVideoAtLevel1AtEachTimeStep[timeStep]; j++) {
    				ID_Coordinate video = peopleVideo[timeStep][j];
    				if (dau[video.ID] == false && distance(video, wifi) > max) {
    					max = distance(video,wifi);
    					save = j;
    				}
    			}
    			dau[save] = true;
    			maxDistance += max;
    			countDistance++;
    		}
    		maxDistance = maxDistance / countDistance;
    		boundingError[1][numberPeopleWifiAtLevel1AtEachTimeStep[timeStep]] += maxDistance;
    		countBoundingError[1][numberPeopleWifiAtLevel1AtEachTimeStep[timeStep]]++;    		
    	}
		//Write output
		BufferedWriter writer;
		try {				
			writer = new BufferedWriter(new FileWriter(wifiFolder + "ErrorOutput.csv"));
			for (int i = 0; i < 30; i++) {
				if (countBoundingError[0][i] > 0 && countBoundingError[1][i] > 0) {
					boundingError[0][i] = boundingError[0][i] / countBoundingError[0][i];
					boundingError[1][i] = boundingError[1][i] / countBoundingError[1][i];
					writer.write(i + " " + countBoundingError[0][i] + " " + boundingError[0][i] * 3 + " " + boundingError[1][i] * 3 + "\n");
				}
			}    				    		
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void cdfError() {
    	createMap();
    	readWifiFile();
    	readVideoFile();
    	findBoundingError();
	     /*for (int i = 0; i < endTimeStep; i++) {
	    	 System.out.println(numberPeopleAtLevel1AtEachTimeStep[i]);
	     }*/
    }
    
    
    public static void main(String[] args) {
    	//selectFromTimeToTime ();    	
    	//refineID ();
    	cdfError();
	}
}