package SuntecRealData.Suntec;



import java.io.BufferedWriter;
import java.io.File;


import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class SMUAddition_WifiPreprocessAndInterpolate {
	
	
    static int startingHour;static int startingMinute;static int startingSecond;
    static int endingHour;static int endingMinute;static int endingSecond;
    static int startTimeStamp; 
    static int endTimeStamp;
    static int experimentNumber = 0; // Experiment length from the SMU video dataset
    static int numberOfWifi;
    static int[] fpPointInTheArea = {40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68};
    //static int[] interpolateMiddlePoint = {0,1,6,8,11,13,18,20,24,25,27,29,30,31,32,34,35,36,38,39};

    static String videoFolder = "src/SuntecRealData/Video/VideoLevel6Camera4R/";
	static String fileVideoInput = "resultRight.txt"; // File location of the SMU video
	static String wifiFolder = "src/SuntecRealData/SMUAdditional/"; // remove src when remote
	static String fileWifiInput = "RTLSSortByID_Time_RSSI.csv";
	static String apLocationFolder = "src/SuntecRealData/APLocation/";
	static String apLocationInput = "ap_landmark_location.csv";
	static String fileOutputFolder = "SuntecRealData/output/";// remove src when remote

	static int frameNumber = 0; // Number of used frame
	static BoundingBoxImageCoor[][] blobImageCoor = new BoundingBoxImageCoor[Config.maximumBlobVideoCount][Config.maximumFrameCount]; // Coordinate of all the bounding box in the SMU video
	static Coordinate[][] blobPhysicalCoor = new Coordinate[Config.maximumBlobVideoCount][Config.maximumFrameCount];
	//static BoundingBoxImageCoor[][] blobImageCalibration = new BoundingBoxImageCoor[Config.maximumBlobCount][Config.maximumFrameCount]; // Coordinate of all the bounding box in the SMU video
	static timeWithLocationCoordinate[][] peopleVideo;
	static boolean[] isGoodSample;
	static TimeWithLocation[][] peopleWifi;
	static timeWithLocationCoordinate[][] peopleWifiAfterCleaning;
	static timeWithLocationCoordinate[][] peopleWifiAfterInterpolate;
	static int[] countTimeWifiReport;
	static int[] countTimeWifiReportAfterCleaning;
	static int[] countTimeWifiReportAfterInterpolate;
	static int numberOfAP = 34;
	static boolean[] blobVideoAppear;
	static int blobVideoNumber = 0;  // Number of video blobs in the SMU video
	static int blobWifiNumber = 0;
	static int countWifiAppearInTheArea;
	static BlobDuration[] blobDuration = new BlobDuration[Config.maximumBlobVideoCount];
	
	static SMUAddition_MapReal[] map;
	static int count14 = 0;
	static int count16 = 0;
	static double distance(Coordinate point1, Coordinate point2) {
		return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
	}
	
	static int getTimeStamp (int hour, int minute, int second) {
		int result = 0;
		int beginTimeOfTheDay = 0;
		result = beginTimeOfTheDay + (hour - 12) * 3600 + minute * 60 + second;
		return result;
	}
	
	static void initializeTheArray() {
		startingHour = 18; startingMinute = 00; startingSecond = 0;
		endingHour = 20; endingMinute = 00; endingSecond = 0;
		startTimeStamp = getTimeStamp(startingHour, startingMinute, startingSecond);
		endTimeStamp = getTimeStamp(endingHour, endingMinute, endingSecond);
	    experimentNumber = ((endingHour - startingHour) * 3600 + (endingMinute - startingMinute) * 60 + (endingSecond - startingSecond)) / 5 + 1;
		peopleVideo = new timeWithLocationCoordinate[Config.maximumBlobVideoCount][experimentNumber];		
		peopleWifi = new TimeWithLocation[Config.maximumBlobVideoCount][Config.maximumWifiReportCount];
		isGoodSample = new boolean[Config.maximumBlobVideoCount];
		countTimeWifiReport = new int[Config.maximumBlobVideoCount * 100];
		countTimeWifiReportAfterCleaning = new int[Config.maximumBlobVideoCount * 100];
		for (int i = 0; i < Config.maximumBlobVideoCount; i++) {
			blobDuration[i] = new BlobDuration(Integer.MAX_VALUE,0);
			/*for (int j = 0; j < experimentNumber; j++) {
				peopleVideo[i][j] = -1;
				
			}*/
		}

	}
	
	
	public static Coordinate convertToPhysicalCoor(int level, Coordinate footLocation) {
		Coordinate result = new Coordinate(-1, -1);
		double minDistance = Integer.MAX_VALUE;
		for (int i = 0; i < map[level].numberOfPhysicalVideoX; i++) {
			for (int j = 0; j < map[level].numberOfPhysicalVideoY; j++) {
				if (map[level].videoCoordinate[i][j] != null && distance(map[level].videoCoordinate[i][j], footLocation) < minDistance) {
					minDistance = distance(map[level].videoCoordinate[i][j], footLocation);
					result.x = i;
					result.y = j;
				}
			}
		}
		return result;
		
	}
	
	public static int getNearestLandMarkInt(int level, Coordinate location) {
		double minDistance = Integer.MAX_VALUE;
		int result = 0;
		for (int i = 0; i < map[level].fpPointInThisMap; i++) {
			if (map[level].fpCoordinate[i].x != Config.noPoint.locationCoor.x && map[level].fpCoordinate[i].y != Config.noPoint.locationCoor.y) {
				if (distance(location, map[level].fpCoordinate[i]) < minDistance) {
					minDistance = distance(location, map[level].fpCoordinate[i]);
					result = i;
				}
			}
		}
		return result;
		
	}
	
	public static int getTimeStampVideo(int frame) {
		int videoStartHour = 19; int videoStartMinute = 28; int videoStartSecond = 0;
		int startTimeStamp = getTimeStamp(videoStartHour, videoStartMinute, videoStartSecond);//time stamp of the beginning of video
		
		return startTimeStamp + frame / 25; // video is 25 fps
		
	}
	

	
	public static boolean wifiAppearInTheExamineArea(int[] wifi) {
		for (int i = 0; i < experimentNumber; i++) {
			for (int k = 0; k < fpPointInTheArea.length; k++) {
				if (wifi[i] == fpPointInTheArea[k]) {
					return true;
				}
			}
		}
		return false;
	}
		
	public static NormalLocation getAPLocation(String APMac) {

		return null;
	}
	
	public static boolean wifiAppearInTheExamineTime(int[] time) {
		for (int i = 0; i < time.length; i++) {
			if (time[i] >= startTimeStamp && time[i] <= endTimeStamp) {
				return true;
			}
		}
		return false;
	}
	
	
	public static void selectWifiFromExaminedTime() {
	    Scanner scan;	    
	    ParserWifiRTLS parser1;
	    ParserWifiRTLS parser2;
	    int[] timeReport;
	    NormalLocation[] locationReport;
	    int[] rssi;
	    int[] association;
	    int ID = 0;
	    int line = 0;
	    int unasso = 0;
	    int asso = 0;
		BufferedWriter writer;
	    try {
	    	File file = new File(wifiFolder + fileWifiInput);
	    	writer = new BufferedWriter(new FileWriter(wifiFolder + "FullWifiFrom" + startingHour + "_" + startingMinute + "_" + startingSecond + " to " +
	    															endingHour + "_" + endingMinute + "_" + endingSecond + ".csv"));
	    	//writer = new BufferedWriter(new FileWriter(wifiFolder + fileOutput));
	        scan = new Scanner(file);
	        scan.nextLine();
	        String data1 = scan.nextLine();
	        String data2 = "";
	        while (scan.hasNextLine()) {
	        	// Do for each person which has been sorted by ID
	        	timeReport = new int[10000];
	        	rssi = new int[10000];
	        	association = new int[10000];
	        	locationReport = new NormalLocation[10000];
	        	parser1 = new ParserWifiRTLS(data1);
	        	if (parser1.getIDstrRTLS().equals("0019f54bb0e646648a4ec1f29f96775b221b8025")){
	        		System.out.println();
	        	}
	        	int time = Integer.parseInt(data1.substring(0, 10));
	        	if (getAPLocation(parser1.getAttribute(5)) != null && time >= startTimeStamp && time <= endTimeStamp) {
	        		timeReport[0] = time;
	        		locationReport[0] = getAPLocation(parser1.getAttribute(5));
	        		association[0] = Integer.parseInt(parser1.getAttribute(6));
	        		rssi[0] = Integer.parseInt(parser1.getAttribute(7));
	        		countTimeWifiReport[ID] = 1;
	        	}
	        	else {
	        		countTimeWifiReport[ID] = 0;
	        	}
	        	while (true)
	        	{	        
	        		if (!scan.hasNextLine()) {
	        			break;
	        		}
	        		data2 = scan.nextLine();
	        		line++;
	        		if (line % 10000 == 0) {
	        			System.out.println(line);
	        		}
	        		parser2 = new ParserWifiRTLS(data2);
	        		if (!parser1.getIDstrRTLS().equals(parser2.getIDstrRTLS())){
	        			break;
	        		}
	        		time = Integer.parseInt(data2.substring(0, 10));
	        		if (time >= startTimeStamp && time <= endTimeStamp) {
		        		if (getAPLocation(parser2.getAttribute(5)) != null && parser2.getAssociateStatus() == 2 && (countTimeWifiReport[ID] == 0 || time != timeReport[countTimeWifiReport[ID] - 1])) {	        			
		        			timeReport[countTimeWifiReport[ID]] = Integer.parseInt(data2.substring(0, 10));
		        			locationReport[countTimeWifiReport[ID]] = getAPLocation(parser2.getAttribute(5));
		        			association[countTimeWifiReport[ID]] = Integer.parseInt(parser2.getAttribute(6));
		        			rssi[countTimeWifiReport[ID]] = Integer.parseInt(parser2.getAttribute(7));
		        			countTimeWifiReport[ID]++;
		        		}
	        		}
	        	}
	        	data1 = data2;	    
	        	if (parser1.getAssociateStatus() == 1) {
	        		unasso++;
	        	}
	        	else {
	        		asso++;
	        	}
	        	if (parser1.getAssociateStatus() == 2 && wifiAppearInTheExamineTime(timeReport)) { //only care the associate one
	        		for (int i = 0; i < countTimeWifiReport[ID]; i++) {	        			
	        			if (locationReport[i] != null) {
	        				writer.write(ID + "," + timeReport[i] + "," + locationReport[i].toString()  + "\n");
	        			}
	        			else {
	        				writer.write(ID + "," + timeReport[i] + "," + "null" + "\n");
	        			}
	        		}

	        		ID++;
	        		/*for (int i = 0; i < countTimeReport[ID]; i++) {
	        			peopleWifi[ID][i] = new timeWithLocation(locationReport[i], timeReport[i]);
	        		}
	        		ID++;*/
	        	}
        	}
	        System.out.println("unassociate:" + unasso);
	        System.out.println("associate:" + asso);
	        scan.close();
	        writer.close();
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void readWifiData() {
	    Scanner scan;	    
	    ParserWifiRTLS parser;
	    int ID = 0;
	    countTimeWifiReport = new int[Config.maximumBlobVideoCount];
	    try {
	    	File file = new File(wifiFolder +  "wifiGroundTruth.csv");
	        scan = new Scanner(file);	        	        
	        while (scan.hasNextLine()) {
	        	String data = scan.nextLine();
	        	parser = new ParserWifiRTLS(data);
	        	String strTime = parser.getAttribute(0);
	        	int hour = Integer.parseInt(strTime.substring(11, 13));
	        	int minute = Integer.parseInt(strTime.substring(14, 16));
	        	int second = Integer.parseInt(strTime.substring(17, 19));
	        	int time = getTimeStamp(hour, minute, second);	      	        	
	        	int location = Integer.parseInt(parser.getAttribute(2).substring(8));
	        	peopleWifi[ID][countTimeWifiReport[ID]] = new TimeWithLocation(location, time);
	        	countTimeWifiReport[ID]++;	
	        	if (ID > numberOfWifi) {
	        		numberOfWifi = ID;
	        	}
	        }
	        numberOfWifi++;
	        scan.close();
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }
	}

	
	public static double distanceBetweenTwoLocation(String position1, String position2) {
		int level1 = Integer.parseInt(position1.charAt(4)+"");
		int location1 = Integer.parseInt(position1.substring(7, position1.length()));
		int level2 = Integer.parseInt(position2.charAt(4)+"");
		int location2 = Integer.parseInt(position2.substring(7, position2.length()));

		return 0;
	}
	
    public static double distanceSameLevel(Coordinate loc1, Coordinate loc2) {
    	return Math.sqrt(Math.pow(loc1.x - loc2.x, 2) + Math.pow(loc1.y - loc2.y, 2));
    }
    

    
    
    public static Coordinate getNextLocationOnUpLevel(int currentLevel, Coordinate currentLocation) {
    	if (currentLevel == 4) {
    		if (currentLocation.x >= -9) {
    			return map[6].fpCoordinate[136];
    		}
    		else {
    			return map[6].fpCoordinate[103];
    		}
    	}
    	else {
    		if (currentLevel == 1) {
    			if (currentLocation.x >= 14.5) {
    				return map[4].fpCoordinate[595];
    			}
    			else {
    				return map[4].fpCoordinate[566];
    			}
    		}
    	}
    	return null;
    }
    
    public static Coordinate getNextLocationOnDownLevel(int currentLevel, Coordinate currentLocation) {
    	if (currentLevel == 6) {
    		if (currentLocation.x <= 17) {
    			return map[4].fpCoordinate[595];
    		}
    		else {
    			return map[4].fpCoordinate[566];
    		}
    	}
    	else {
    		if (currentLevel == 4) {
    			if (currentLocation.x >= -10) {
    				return map[1].fpCoordinate[27];
    			}
    			else {
    				return map[1].fpCoordinate[42];
    			}
    		}
    	}
    	return null;
    }

    

    
    public static double getModulo(Coordinate vector) {
    	return Math.sqrt(vector.x * vector.x + vector.y * vector.y);
    }
    
    public static int getNearestFP(int currentLevel, Coordinate location) {
    	double minDistance = Integer.MAX_VALUE;
    	int result = -1;
    	for (int i = 0; i < map[currentLevel].fpPointInThisMap; i++) {
    		if (distanceSameLevel(location, map[currentLevel].fpCoordinate[i]) < minDistance) {
    			minDistance = distanceSameLevel(location, map[currentLevel].fpCoordinate[i]);
    			result = i;
    		}
    	}
    	return result;
    }
    
    public static boolean similarCoordinate(Coordinate coor1, Coordinate coor2) {
    	if (coor1.x == coor2.x && coor1.y == coor2.y) {
    		return true;
    	}
    	return false;
    }
    
    public static double[] getNextLocation(Coordinate currentLocation, double distanceAtChangeLevelPoint, Coordinate endLocation, double distanceMove) {
    	double[] result = new double[4]; 
    	Coordinate directionMove = new Coordinate(0,0);
    	Coordinate targetPoint = new Coordinate(0,0);

		//targetPoint = new Coordinate(map[endLevel].fpCoordinate[endLocation].x, map[endLevel].fpCoordinate[endLocation].y);
		directionMove = new Coordinate(endLocation.x - currentLocation.x,
									   endLocation.y - currentLocation.y);
		Coordinate vectorMove;
		if (getModulo(directionMove) != 0) {
			vectorMove = new Coordinate(directionMove.x * distanceMove / getModulo(directionMove),directionMove.y * distanceMove / getModulo(directionMove));
		}
		else {
			vectorMove = new Coordinate(0,0);
		}
		currentLocation = new Coordinate(currentLocation.x + vectorMove.x, currentLocation.y + vectorMove.y);


    	result[0] = 1; 
    	result[1] = currentLocation.x;
    	result[2] = currentLocation.y;
    	result[3] = distanceAtChangeLevelPoint;
    	return result;
    }
    
    public static timeWithLocationCoordinate[] fillInterpolateData(timeWithLocationCoordinate timeLocation1, timeWithLocationCoordinate timeLocation2) {
	    	double distance = distanceSameLevel(timeLocation1.location.locationCoor, timeLocation2.location.locationCoor);
	    	int duration = timeLocation2.time - timeLocation1.time;
	    	if (duration <= 0) {
	    		return null;
	    	}
	    	double distanceMoveEachStep = distance / duration;
	    	
	    	Coordinate currentLocation = timeLocation1.location.locationCoor;
	    	double distanceAtChangeLevelPoint = 0;
	    	
	    	Coordinate endLocation = timeLocation2.location.locationCoor;
	    	timeWithLocationCoordinate[] result = new timeWithLocationCoordinate[duration];
	    	result[0] = timeLocation1;
	    	for (int i = 1; i < duration; i++) {
	    		double[] newLoc = getNextLocation(currentLocation, distanceAtChangeLevelPoint, endLocation, distanceMoveEachStep);
	    		if (Double.isNaN(newLoc[1])){
	    			System.out.println(newLoc[1]);
	    			
	    		}	    		
	    		currentLocation = new Coordinate(newLoc[1], newLoc[2]);
	    		
	    		result[i] = new timeWithLocationCoordinate(currentLocation, timeLocation1.time + i);
	    	}
	    	return result;    	
    }
    
    public static int getInitialLevel(int[] count) {
    	int max0 = 0;
    	int max1 = 0;
    	int max2 = 0;
    	int level = -1;
    	
    	for (int i = 1; i <= 6; i++) {
    		if (count[i] > max0) {
    			max0 = count[i];
    			level = i;
    		}
    	}
    	
    	for (int i = 1; i <= 6; i++) {
    		if (count[i] > max1 && count[i] != max0) {
    			max1 = count[i];
    		}
    	}

    	for (int i = 1; i <= 6; i++) {
    		if (count[i] > max2 && count[i] != max0 && count[i] != max1) {
    			max2 = count[i];
    		}
    	}
    	if (max0 - max1 >= 4 && max0 - max2 >= 4) {
    		return level;
    	}
    	else {
    		return -1;
    	}
    }
    

    
    public static void writeAfterRemoveWrongLevel() {
		BufferedWriter writer;
	    try {
	    	
	    	writer = new BufferedWriter(new FileWriter(wifiFolder + "removeWrongLevel.csv"));
	    	//writer = new BufferedWriter(new FileWriter(wifiFolder + fileOutput));
	    	for (int ID = 0; ID < numberOfWifi; ID++) {
	    		for (int k = 0; k < countTimeWifiReport[ID]; k++) {
	    			if (peopleWifi[ID][k].location.locationID != 0) {
	    				writer.write(ID + "," + peopleWifi[ID][k].time + "," + peopleWifi[ID][k].location.toString());
	    				writer.write("\n");
	    			}
	    		}
	    	}
	  
	        writer.close();
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void readAfterRemoveWrongLevel() {
	    Scanner scan;	    
	    ParserWifiRTLS parser;
	    int ID = 0;	    
	    numberOfWifi = 0;
	    try {
	    	File file = new File(wifiFolder +  "removeWrongLevel.csv");
	        scan = new Scanner(file);	        	        
	        while (scan.hasNextLine()) {
	        	String data = scan.nextLine();
	        	parser = new ParserWifiRTLS(data);
	        	ID = Integer.parseInt(parser.getAttribute(0));
	        	int time = Integer.parseInt(parser.getAttribute(1)) % 100000;
	        	TimeWithLocation location = new TimeWithLocation(parser.getAttribute(2), time); 
	        	peopleWifi[ID][countTimeWifiReport[ID]] = location;		
	        	countTimeWifiReport[ID]++;
	        	if (ID > numberOfWifi) {
	        		numberOfWifi = ID;
	        	}
	        }
	        numberOfWifi++;
	        scan.close();
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }

    }
    
    public static void writeAfterInferLocationDueToBouncingSignal() {
		BufferedWriter writer;
	    try {
	    	writer = new BufferedWriter(new FileWriter(wifiFolder + "inferLocationDueToBouncingSignal.csv"));
	    	int newID = 0;
	    	for (int ID = 0; ID < numberOfWifi; ID++) {
	    		if (isGoodSample[ID] == true) {
	    			
		    		for (int k = 0; k < countTimeWifiReportAfterCleaning[ID]; k++) {	    	
		    			writer.write(newID + "," + peopleWifiAfterCleaning[ID][k].time + "," + peopleWifiAfterCleaning[ID][k].location.locationCoor.x + "," + peopleWifiAfterCleaning[ID][k].location.locationCoor.y);
		    			if (newID == 5) {
		    				newID = 5;
		    			}
		    			writer.write("\n");
		    		}
		    		newID++;
	    		}
	    	}	  
	        writer.close();
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    public static void readAfterInferLocationDueToBouncingSignal() {
	    Scanner scan;	    
	    ParserWifiRTLS parser;
	    int ID = 0;	 
	    numberOfWifi = 0;
	    peopleWifiAfterCleaning = new timeWithLocationCoordinate[Config.maximumBlobVideoCount][5000];
	    try {
	    	//File file = new File(wifiFolder +  "inferLocationDueToBouncingSignalNew40.csv");
	    	File file = new File(wifiFolder +  "inferLocationDueToBouncingSignal.csv");
	        scan = new Scanner(file);	        	        
	        while (scan.hasNextLine()) {
	        	String data = scan.nextLine();
	        	parser = new ParserWifiRTLS(data);
	        	ID = Integer.parseInt(parser.getAttribute(0));
	        	int time = Integer.parseInt(parser.getAttribute(1)) % 100000;
	        	Coordinate location = new Coordinate(Double.parseDouble(parser.getAttribute(2)),Double.parseDouble(parser.getAttribute(3)));
	        	peopleWifiAfterCleaning[ID][countTimeWifiReportAfterCleaning[ID]] = new timeWithLocationCoordinate(location, time);
	        	countTimeWifiReportAfterCleaning[ID]++;	
	        	if (ID > numberOfWifi) {
	        		numberOfWifi = ID;
	        	}
	        }
	        numberOfWifi++;
	        scan.close();
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }

    }
    
    
    public static void writeAfterInterpolation() {
		BufferedWriter writer;
	    try {
	    	//writer = new BufferedWriter(new FileWriter(wifiFolder + "afterInterpolationNew40.csv"));
	    	writer = new BufferedWriter(new FileWriter(wifiFolder + "afterInterpolation.csv"));
	    	for (int ID = 0; ID < numberOfWifi; ID++) {
	    		for (int k = 0; k < countTimeWifiReportAfterInterpolate[ID]; k++) {	    	
	    			writer.write(ID + "," + peopleWifiAfterInterpolate[ID][k].time + "," + peopleWifiAfterInterpolate[ID][k].location.locationCoor.x + "," + peopleWifiAfterInterpolate[ID][k].location.locationCoor.y);
	    			writer.write("\n");
	    		}
	    	}	  
	        writer.close();
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
 
    
    
    
    public static boolean numberOfElemenetInThisClusterLessThanEqual3(int ID, int[] elementList, int elementNumber, int left, int right) {    	
	    for(int i=left; i<right; i++){
	    	int locationNumber = 1000 + peopleWifi[ID][i].location.locationID;
	    	boolean isNew = true;
	    	for (int k = 0; k < elementNumber; k++) {
	    		if (elementList[k] == locationNumber) {
	    			isNew = false;
	    			break;
	    		}
	    	}
	    	if (isNew) {
	    		if (elementNumber >= 3) {
	    			return false;
	    		}
	    		elementList[elementNumber] = locationNumber;
	    		elementNumber++;
	    	}
	    }
	   return (elementNumber <= 3);
    }
    
    public static int findInLocationList(NormalLocation[] list, NormalLocation location) {
    	for (int i = 0; i < list.length; i++) {
    		if (list[i] != null && list[i].locationID == location.locationID) {
    			return i;
    		}
    	}
    	return -1;
    }
    
    public static Coordinate findWeightMeanCluster(int ID, int left, int right) {
    	NormalLocation[] locationList = new NormalLocation[3];
    	int countInList = 0;
    	int[] weight = new int[3];
    	for (int i = left; i <= right; i++) {
    		int index = findInLocationList(locationList, peopleWifi[ID][i].location);
    		if (index >= 0) {
    			weight[index]++;
    		}
    		else {
    			locationList[countInList] = peopleWifi[ID][i].location;
    			weight[countInList] = 1;
    			countInList++;
    		}
    	}
    	int sumWeight = 0;
    	Coordinate result = new Coordinate(0,0);
    	for (int i = 0; i < countInList; i++) {
    		if (map[1].fpCoordinate[locationList[i].locationID].x != Config.noPoint.locationCoor.x &&
    				map[1].fpCoordinate[locationList[i].locationID].y != Config.noPoint.locationCoor.y) { //the location is far from observed area
    			sumWeight += weight[i];
    			result.x += map[1].fpCoordinate[locationList[i].locationID].x * weight[i];
    			result.y += map[1].fpCoordinate[locationList[i].locationID].y * weight[i];
    		}
    	}
    	if (sumWeight == 0) {
    		return null;
    	}
    	result.x = result.x * 1.0 / sumWeight;
    	result.y = result.y * 1.0 / sumWeight;
    	return result;
    }
    
    public static void normalizeTheBouncingLocation(int ID, int[] clusterID) {
    	
    	int startCluster = 0;    	
    	for (int i = 1; i <= countTimeWifiReport[ID]; i++) {
    		if (clusterID[i] != clusterID[i - 1]) {
    			Coordinate meanCluster = findWeightMeanCluster(ID, startCluster, i - 1);
    			if (meanCluster != null) {
	    			
	    			//smooth from (t0 + t1)/2
	    			if (startCluster == 0 && map[1].fpCoordinate[peopleWifi[ID][0].location.locationID].x != Config.noPoint.locationCoor.x &&
	    					map[1].fpCoordinate[peopleWifi[ID][0].location.locationID].y != Config.noPoint.locationCoor.y) {//For the first cluster
	    				peopleWifiAfterCleaning[ID][0] = new timeWithLocationCoordinate(map[1].fpCoordinate[peopleWifi[ID][0].location.locationID], peopleWifi[ID][0].time);
	    				countTimeWifiReportAfterCleaning[ID] = 1;
	    			}
	    			int time = (peopleWifi[ID][startCluster].time + peopleWifi[ID][i - 1].time) / 2;
	    			peopleWifiAfterCleaning[ID][countTimeWifiReportAfterCleaning[ID]] = new timeWithLocationCoordinate(meanCluster, time);
	    			countTimeWifiReportAfterCleaning[ID]++;
	    			
	    			
    			}
    			startCluster = i;
    		}
    	}
    	//For the last cluster    	
	   
	    int time = peopleWifi[ID][countTimeWifiReport[ID] - 1].time;
	    if (map[1].fpCoordinate[peopleWifi[ID][countTimeWifiReport[ID] - 1].location.locationID].x != Config.noPoint.locationCoor.x &&
	    		map[1].fpCoordinate[peopleWifi[ID][countTimeWifiReport[ID] - 1].location.locationID].y != Config.noPoint.locationCoor.y) {	    	
	    	peopleWifiAfterCleaning[ID][countTimeWifiReportAfterCleaning[ID]] = new timeWithLocationCoordinate(map[1].fpCoordinate[peopleWifi[ID][countTimeWifiReport[ID] - 1].location.locationID], time);
			countTimeWifiReportAfterCleaning[ID]++;
    	}

    }
    
    public static boolean isInCluster(int element, int[] elementList, int elementNumber) {
    	for (int i = 0; i < elementNumber; i++) {
    		if (elementList[i] == element) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public static void inferLocationDueToBouncingSignal() {

    	peopleWifiAfterCleaning = new timeWithLocationCoordinate[numberOfWifi][50000];
    	for (int ID = 0; ID < numberOfWifi; ID++) {
    		System.out.println(ID);
    		if (countTimeWifiReport[ID] > 0) {
	    		int currentClusterIDForNewGroup = 1;
	        	int[] clusterID = new int[Config.maximumWifiReportCount];
	        	int[][] elementInACluster = new int[Config.maximumWifiReportCount][3];
	        	int[] numberOfDifferentElementInACluster = new int[Config.maximumWifiReportCount];
	        	clusterID[0] = currentClusterIDForNewGroup;
	        	elementInACluster[clusterID[0]][numberOfDifferentElementInACluster[clusterID[0]]] = Integer.parseInt(peopleWifi[ID][0].location.toString());
	        	numberOfDifferentElementInACluster[clusterID[0]] = 1;
	    		currentClusterIDForNewGroup++;
	    		for (int i = 1; i < countTimeWifiReport[ID]; i++) {
	    			boolean flag = true;
	    			for (int j = i - 1; j >= 0; j--) {
	    				if (peopleWifi[ID][i].location.locationID == peopleWifi[ID][j].location.locationID) {
	    					flag = false;
	    					if (numberOfElemenetInThisClusterLessThanEqual3(ID,elementInACluster[clusterID[j]],numberOfDifferentElementInACluster[clusterID[j]],j,i)) {
	    						for (int k = j; k <= i; k++) {
	    							clusterID[k] = clusterID[j];
	    							if (!isInCluster(Integer.parseInt(peopleWifi[ID][k].location.toString()),elementInACluster[clusterID[j]],numberOfDifferentElementInACluster[clusterID[j]])){
	    								elementInACluster[clusterID[j]][numberOfDifferentElementInACluster[clusterID[j]]] = Integer.parseInt(peopleWifi[ID][k].location.toString());
	    								numberOfDifferentElementInACluster[clusterID[j]]++;
	    							}
	    						}
	    					}
	    					else {
	    						clusterID[i] = currentClusterIDForNewGroup;
	    						numberOfDifferentElementInACluster[currentClusterIDForNewGroup] = 1;
	    						elementInACluster[currentClusterIDForNewGroup][0] = Integer.parseInt(peopleWifi[ID][i].location.toString());
	    						currentClusterIDForNewGroup++;
	    					}
	    					break;
	    				}
	    			}
	    			if (flag) {
						clusterID[i] = currentClusterIDForNewGroup;
						numberOfDifferentElementInACluster[currentClusterIDForNewGroup] = 1;
						elementInACluster[currentClusterIDForNewGroup][0] = Integer.parseInt(peopleWifi[ID][i].location.toString());
						currentClusterIDForNewGroup++;
	    			}
	    		}
	    		normalizeTheBouncingLocation(ID, clusterID);
    		}
    	}
    }
    
    public static boolean isdifferentLocation(timeWithLocationCoordinate l1, timeWithLocationCoordinate l2) {
    	if (l1.location.locationCoor.x == l2.location.locationCoor.x && l1.location.locationCoor.y == l2.location.locationCoor.y) {
    		return false;
    	}
    	else {
    		return true;
    	}
    }
    
    public static timeWithLocationCoordinate getMiddlePoint(timeWithLocationCoordinate l1, timeWithLocationCoordinate l2) {
    	timeWithLocationCoordinate result;
		int time = (l1.time + l2.time) / 2 + 1;
		Coordinate newCoordinate = new Coordinate((l1.location.locationCoor.x + l2.location.locationCoor.x) / 2, (l1.location.locationCoor.y + l2.location.locationCoor.y) / 2);
		NormalLocationCoordinate newLocation = new NormalLocationCoordinate(newCoordinate);
		result = new timeWithLocationCoordinate(newLocation, time); 

    	return result;
    }
    
    public static boolean isdifferentLevel(timeWithLocationCoordinate l1, timeWithLocationCoordinate l2) {

    		return false;

    }
    

    
    public static void interpolate() {
    	peopleWifiAfterInterpolate = new timeWithLocationCoordinate[Config.maximumBlobVideoCount][Config.maximumWifiReportCount];
    	countTimeWifiReportAfterInterpolate = new int[Config.maximumWifiReportCount];
    	for (int ID = 0; ID < numberOfWifi; ID++) {
    		System.out.println(ID);
    		countTimeWifiReportAfterInterpolate[ID] = 0;
    		if (peopleWifiAfterCleaning[ID][0] != null) {
	    		timeWithLocationCoordinate beginInterpolate = new timeWithLocationCoordinate(peopleWifiAfterCleaning[ID][0].location.locationCoor, peopleWifiAfterCleaning[ID][0].time);
	    		timeWithLocationCoordinate endInterpolate;
	    		for (int i = 0; i < countTimeWifiReportAfterCleaning[ID] - 1; i++) {
	    			if (peopleWifiAfterCleaning[ID][i + 1].time - peopleWifiAfterCleaning[ID][i].time > 1) {
		    			if (isdifferentLocation(peopleWifiAfterCleaning[ID][i], peopleWifiAfterCleaning[ID][i + 1])) {
		    				endInterpolate = getMiddlePoint(peopleWifiAfterCleaning[ID][i], peopleWifiAfterCleaning[ID][i + 1]);		    				
		    				timeWithLocationCoordinate[] result = fillInterpolateData(beginInterpolate,endInterpolate);
			    			if (result != null) {
				    			for (int k = 0; k < result.length; k++) {
				    				peopleWifiAfterInterpolate[ID][countTimeWifiReportAfterInterpolate[ID]] = new timeWithLocationCoordinate(result[k].location.locationCoor, result[k].time);
				    				countTimeWifiReportAfterInterpolate[ID]++;
				    			}
			    			}
		    				if (isdifferentLevel(peopleWifiAfterCleaning[ID][i], peopleWifiAfterCleaning[ID][i + 1])) {
		    					beginInterpolate = new timeWithLocationCoordinate(peopleWifiAfterCleaning[ID][i + 1].location, endInterpolate.time);
		    				}
		    				else {
		    					beginInterpolate = new timeWithLocationCoordinate(endInterpolate.location, endInterpolate.time);
		    				}
		    			}
	    			}
	    			else {
	    				if (peopleWifiAfterCleaning[ID][i + 1].time - peopleWifiAfterCleaning[ID][i].time == 1) {
	    					peopleWifiAfterInterpolate[ID][countTimeWifiReportAfterInterpolate[ID]] = new timeWithLocationCoordinate(peopleWifiAfterCleaning[ID][i].location.locationCoor, peopleWifiAfterCleaning[ID][i].time);
	    					countTimeWifiReportAfterInterpolate[ID]++;
	    				}
	    			}
	
	    		}
	    		endInterpolate = new timeWithLocationCoordinate(peopleWifiAfterCleaning[ID][countTimeWifiReportAfterCleaning[ID] - 1].location, peopleWifiAfterCleaning[ID][countTimeWifiReportAfterCleaning[ID] - 1].time);
				timeWithLocationCoordinate[] result = fillInterpolateData(beginInterpolate,endInterpolate);
				if (result != null) {
	    			for (int k = 0; k < result.length; k++) {
	    				peopleWifiAfterInterpolate[ID][countTimeWifiReportAfterInterpolate[ID]] = new timeWithLocationCoordinate( result[k].location.locationCoor, result[k].time);
	    				countTimeWifiReportAfterInterpolate[ID]++;
	    			}
				}
	    		
    		}
    	}
    	writeAfterInterpolation();    	
    }
    
 
    
    public static void interpolateData() {
    	 //apply heuristic
    	//writeAfterRemoveWrongLevel();
    	//---------------------
    	//readAfterRemoveWrongLevel();
    	//selectGoodSamples();    	
    	//inferLocationDueToBouncingSignal(); 
    	//writeAfterInferLocationDueToBouncingSignal();
    	//--------------------------
    	readAfterInferLocationDueToBouncingSignal();
    	interpolate();
    	//readAfterInterpolation();
    }

    public static int[] nextPattern(int pattern, int type) {
    	//type = 0: go up, type = 1: go down
    	//1 --> 4 --> 6 --> 4 --> 1 --> 1000;
    	int[] result = new int[2];
    	if (pattern == 1) {
    		if (type == 0) {
    			result[0] = 4; 
    			result[1] = 0;
    		}
    		else {
    			result[0] = 1000;
    			result[1] = 1;
    		}
    	}
    	else {
    		if (pattern == 4) {
    			if (type == 0) {
    				result[0] = 6;
    				result[1] = 0;
    			}    		
    			else {
    				result[0] = 1000;
    				result[1] = 1;
    			}

    		}    		
    		else {
    			result[0] = 4; 
    			result[1] = 1;
    		}
    	}
    	return result;
    }
    

    
    
    public static int findInPatternList(String[] patternList, int numberOfPattern, String pattern) {
    	for (int i = 0; i < numberOfPattern; i++) {
    		if (patternList[i].equals("")) {
    			break;
    		}
    		if (patternList[i].equals(pattern)){
    			return i;
    		}
    	}
    	return -1;
    }
    
    public static boolean patternBiggerThan (String t1, String t2) {
    	if (t1.length() < t2.length()) {
	    	for (int i = 0; i < t1.length(); i++) {
	    		if (Integer.parseInt(t1.charAt(i) + "") < Integer.parseInt(t2.charAt(i) + "")) {
	    			return false;
	    		}
	    		else {
	    			if (Integer.parseInt(t1.charAt(i) + "") > Integer.parseInt(t2.charAt(i) + "")) {
	    				return true;
	    			}
	    		}
	    	}
	    	return false;
    	}
    	else {
	    	for (int i = 0; i < t2.length(); i++) {
	    		if (Integer.parseInt(t1.charAt(i) + "") < Integer.parseInt(t2.charAt(i) + "")) {
	    			return false;
	    		}
	    		else {
	    			if (Integer.parseInt(t1.charAt(i) + "") > Integer.parseInt(t2.charAt(i) + "")) {
	    				return true;
	    			}
	    		}
	    	}
	    	return true;    		
    	}
    }
    
    public static void selectGoodSamples() {
    	for (int ID = 0; ID < numberOfWifi; ID++) {
    		isGoodSample[ID] = true;
    	}
    }
    

    public static void countNumberOfMacAddressInTheArea() {
    	int startHour = 19; int startMinute = 28; int startSecond = 25;
    	int endHour = 19; int endMinute = 37; int endSecond = 9;
    	int startTimeStep = getTimeStamp(startHour, startMinute, startSecond);
    	int endTimeStep = getTimeStamp(endHour, endMinute, endSecond);
    	countNumberOfMacAddressInTheArea(4, startTimeStep, endTimeStep);
    }
    
	public static void countNumberOfMacAddressInTheArea(int levelCheck, int startTimeStep, int EndTimeStep) {
		//count the number of different Mac that associated to the list of AP in that level, then divide by 8
		//since a camera cover around 1/8 of the level
		int result = 0;
		Scanner scan;
		ParserWifiRTLS parser;
		int line = 0;
		boolean currentMacAppearOnTheAreaLevel = false;
		try {
	    	File file = new File(wifiFolder + "FullWifiWithRSSI_ID_From19_28_0 to 20_0_0.csv");
	        scan = new Scanner(file);
	        scan.nextLine();
	        String data = scan.nextLine();
	        parser = new ParserWifiRTLS(data);
	        String IDsave = parser.getAttribute(0);
	        int apLocationLevel = Integer.parseInt(parser.getAttribute(2).charAt(1) + "");
	        int currentTimeStep = Integer.parseInt(parser.getAttribute(1).substring(1));
	        
	        if (apLocationLevel == levelCheck && currentMacAppearOnTheAreaLevel == false
	        		&& currentTimeStep < EndTimeStep && currentTimeStep > startTimeStep) {
	        	result++;
	        	currentMacAppearOnTheAreaLevel = true;
	        }
	        while (scan.hasNextLine()) {
		        data = scan.nextLine();
		        parser = new ParserWifiRTLS(data);
		        String ID = parser.getAttribute(0);
		        currentTimeStep = Integer.parseInt(parser.getAttribute(1).substring(1));
		        if (!ID.equals(IDsave)) {
		        	currentMacAppearOnTheAreaLevel = false;
		        	IDsave = parser.getIDstrRTLS();
		        }
		        apLocationLevel = Integer.parseInt(parser.getAttribute(2).charAt(1) + "");
		        if (apLocationLevel == levelCheck && currentMacAppearOnTheAreaLevel == false 
		        	&& currentTimeStep < EndTimeStep && currentTimeStep > startTimeStep) {
		        	result++;
		        	currentMacAppearOnTheAreaLevel = true;
		        }

	        }
		}
		catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		System.out.println("The number of Mac Address: " + result);
	} 
	public static void main( String[ ] args ) {
		createMap();
		initializeTheArray();
		//countPercentEndAtLevel1();
		//selectWifiFromExaminedTime(); //only run when need to select at new time duration
		//readWifiData();		
		//countNumberOfMacAddressInTheArea(); //only run for counting Mac address
		interpolateData();					
	}
	
	public static void createMap() {
		map = new SMUAddition_MapReal[4];
		for (int i = 0; i < 4; i++) {
			map[i] = new SMUAddition_MapReal();
		}
		// For camera 1 sau koufu;
		
		map[1].fpPointInThisMap = 100;	
		map[1].numberOfPhysicalVideoX = 5;
		map[1].numberOfPhysicalVideoY = 28;
		map[1].videoCoordinate = new Coordinate[map[1].numberOfPhysicalVideoX][map[1].numberOfPhysicalVideoY];		
		map[1].fpCoordinate = new Coordinate[map[1].fpPointInThisMap + 1];
		map[1].fpPointInTheCameraArea = new int[]{59,60,61,62,63,64,65,66,67,68,69,70};
		for (int i = 0; i < map[1].fpPointInThisMap; i++) {
			map[1].fpCoordinate[i] = new Coordinate(Config.noPoint.locationCoor.x, Config.noPoint.locationCoor.y);
		}
		map[1].fpCoordinate[40] = new Coordinate(1,-2);
		map[1].fpCoordinate[41] = new Coordinate(3,-2);
		map[1].fpCoordinate[42] = new Coordinate(4,0);
		map[1].fpCoordinate[43] = new Coordinate(3,2);
		map[1].fpCoordinate[44] = new Coordinate(2,1);
		map[1].fpCoordinate[46] = new Coordinate(4,5.5);
		map[1].fpCoordinate[47] = new Coordinate(4.2,3.5);
		map[1].fpCoordinate[48] = new Coordinate(2,3.5); 
		map[1].fpCoordinate[49] = new Coordinate(-1,3.5); 
		map[1].fpCoordinate[50] = new Coordinate(2,6);
		map[1].fpCoordinate[51] = new Coordinate(0,7);
		map[1].fpCoordinate[52] = new Coordinate(0,9.2);
		map[1].fpCoordinate[53] = new Coordinate(2,9.2); 
		map[1].fpCoordinate[54] = new Coordinate(1,11); 
		map[1].fpCoordinate[55] = new Coordinate(2,13); 
		map[1].fpCoordinate[56] = new Coordinate(0,13); 
		map[1].fpCoordinate[59] = new Coordinate(2,18);
		map[1].fpCoordinate[60] = new Coordinate(0.5,19.8);
		map[1].fpCoordinate[61] = new Coordinate(2,21);
		map[1].fpCoordinate[62] = new Coordinate(0,22.8);
		map[1].fpCoordinate[63] = new Coordinate(0.2,25);
		map[1].fpCoordinate[64] = new Coordinate(3.5,22.8);
		map[1].fpCoordinate[65] = new Coordinate(6.5,21.5);
		map[1].fpCoordinate[66] = new Coordinate(6.5,25); 
		map[1].fpCoordinate[67] = new Coordinate(3.5,25.2); 
		map[1].fpCoordinate[68] = new Coordinate(5,26.4); 
		map[1].fpCoordinate[69] = new Coordinate(2,26.4); 
		map[1].fpCoordinate[70] = new Coordinate(3.5,27.5); 
		
		map[1].videoCoordinate[0][21] = new Coordinate(4.1,12.6);
		map[1].videoCoordinate[1][21] = new Coordinate(8.5,14.7);
		map[1].videoCoordinate[2][21] = new Coordinate(10.3,15.3);
		map[1].videoCoordinate[3][21] = new Coordinate(13.3,16.1);
		map[1].videoCoordinate[4][21] = new Coordinate(16.9,16.8);
		map[1].videoCoordinate[0][22] = new Coordinate(9.5,11.5);
		map[1].videoCoordinate[1][22] = new Coordinate(12.2,12.8);
		map[1].videoCoordinate[2][22] = new Coordinate(13.4,13.1);
		map[1].videoCoordinate[3][22] = new Coordinate(15.5,13.5);
		map[1].videoCoordinate[4][22] = new Coordinate(18.2,13.7);
		map[1].videoCoordinate[0][23] = new Coordinate(13.4,10.8);
		map[1].videoCoordinate[1][23] = new Coordinate(14.9,11.4);
		map[1].videoCoordinate[2][23] = new Coordinate(15.5,11.6);
		map[1].videoCoordinate[3][23] = new Coordinate(16.9,11.8);
		map[1].videoCoordinate[4][23] = new Coordinate(18.8,11.8);
		map[1].videoCoordinate[0][24] = new Coordinate(15.9,10.3);
		map[1].videoCoordinate[1][24] = new Coordinate(16.5,10.5);
		map[1].videoCoordinate[2][24] = new Coordinate(16.9,10.6);
		map[1].videoCoordinate[3][24] = new Coordinate(17.9,10.7);
		map[1].videoCoordinate[4][24] = new Coordinate(19.3,10.6);
		map[1].videoCoordinate[0][25] = new Coordinate(16.9,10.1);
		map[1].videoCoordinate[1][25] = new Coordinate(17.2,10.2);
		map[1].videoCoordinate[2][25] = new Coordinate(17.4,10.3);
		map[1].videoCoordinate[3][25] = new Coordinate(18.2,10.2);
		map[1].videoCoordinate[4][25] = new Coordinate(19.4,10.2);

		for (int i = 0; i < map[1].numberOfPhysicalVideoX; i++) {
			for (int j = 0; j < map[1].numberOfPhysicalVideoY; j++) {
				if (map[1].videoCoordinate[i][j] != null) {
					map[1].videoCoordinate[i][j].x = (map[1].videoCoordinate[i][j].x / map[1].videoDimensionInCM.x) * map[1].videoResolution_BoundingBox.x;
					map[1].videoCoordinate[i][j].y = (map[1].videoCoordinate[i][j].y / map[1].videoDimensionInCM.y) * map[1].videoResolution_BoundingBox.y;
				}
			}
		}
		
		// For camera 2 truoc koufu;
		
		map[2].fpPointInThisMap = 100;	
		map[2].numberOfPhysicalVideoX = 5;
		map[2].numberOfPhysicalVideoY = 28;
		map[2].videoCoordinate = new Coordinate[map[2].numberOfPhysicalVideoX][map[2].numberOfPhysicalVideoY];		
		map[2].fpCoordinate = new Coordinate[map[2].fpPointInThisMap + 1];
		map[2].fpPointInTheCameraArea = new int[]{46,47,48,49,50,51,52,53,54,55,56};
		for (int i = 0; i < map[1].fpPointInThisMap; i++) {
			map[2].fpCoordinate[i] = new Coordinate(Config.noPoint.locationCoor.x, Config.noPoint.locationCoor.y);
		}
		map[2].fpCoordinate[46] = new Coordinate(4,5.5);
		map[2].fpCoordinate[47] = new Coordinate(4.3,3.5);
		map[2].fpCoordinate[48] = new Coordinate(2,3.5);
		map[2].fpCoordinate[49] = new Coordinate(-1,3.5);
		map[2].fpCoordinate[50] = new Coordinate(2,6);
		map[2].fpCoordinate[51] = new Coordinate(0,7);
		map[2].fpCoordinate[52] = new Coordinate(0,9.2);
		map[2].fpCoordinate[53] = new Coordinate(2,9.2); 
		map[2].fpCoordinate[54] = new Coordinate(1,11); 
		map[2].fpCoordinate[55] = new Coordinate(2,13); 
		map[2].fpCoordinate[56] = new Coordinate(0,13); 
		 
		
		map[2].videoCoordinate[0][3] = new Coordinate(4.3,15.9);
		map[2].videoCoordinate[1][3] = new Coordinate(8.4,16.5);
		map[2].videoCoordinate[2][3] = new Coordinate(15.4,16.7);
		map[2].videoCoordinate[3][3] = new Coordinate(23.3,15.4);
		map[2].videoCoordinate[4][3] = new Coordinate(28.5,13.2);
		map[2].videoCoordinate[0][4] = new Coordinate(5.7,14.8);
		map[2].videoCoordinate[1][4] = new Coordinate(9.2,15.3);
		map[2].videoCoordinate[2][4] = new Coordinate(15.1,15.4);
		map[2].videoCoordinate[3][4] = new Coordinate(21.7,14.6);
		map[2].videoCoordinate[4][4] = new Coordinate(26.6,12.9);
		map[2].videoCoordinate[0][5] = new Coordinate(5.8,14);
		map[2].videoCoordinate[1][5] = new Coordinate(9.9,14.3);
		map[2].videoCoordinate[2][5] = new Coordinate(14.9,14.5);
		map[2].videoCoordinate[3][5] = new Coordinate(20.4,13.8);
		map[2].videoCoordinate[4][5] = new Coordinate(25,12.5);
		map[2].videoCoordinate[0][6] = new Coordinate(8,13.2);
		map[2].videoCoordinate[1][6] = new Coordinate(10.6,13.4);
		map[2].videoCoordinate[2][6] = new Coordinate(14.6,13.5);
		map[2].videoCoordinate[3][6] = new Coordinate(19,13.1);
		map[2].videoCoordinate[4][6] = new Coordinate(23.3,12.1);
		map[2].videoCoordinate[0][7] = new Coordinate(8.9,12.6);
		map[2].videoCoordinate[1][7] = new Coordinate(11.1,12.7);
		map[2].videoCoordinate[2][7] = new Coordinate(14.4,12.7);
		map[2].videoCoordinate[3][7] = new Coordinate(18,12.5);
		map[2].videoCoordinate[4][7] = new Coordinate(22,11.8);
		map[2].videoCoordinate[0][8] = new Coordinate(9.7,12);
		map[2].videoCoordinate[1][8] = new Coordinate(11.6,12.1);
		map[2].videoCoordinate[2][8] = new Coordinate(14.2,12.1);
		map[2].videoCoordinate[3][8] = new Coordinate(17,12);
		map[2].videoCoordinate[4][8] = new Coordinate(20.4,11.5);
		map[2].videoCoordinate[0][9] = new Coordinate(10.4,11.5);
		map[2].videoCoordinate[1][9] = new Coordinate(11.9,11.6);
		map[2].videoCoordinate[2][9] = new Coordinate(14.1,11.6);
		map[2].videoCoordinate[3][9] = new Coordinate(16.3,11.5);
		map[2].videoCoordinate[4][9] = new Coordinate(19.2,11.3);
		map[2].videoCoordinate[0][10] = new Coordinate(11,11);
		map[2].videoCoordinate[1][10] = new Coordinate(12.3,11.1);
		map[2].videoCoordinate[2][10] = new Coordinate(13.9,11.2);
		map[2].videoCoordinate[3][10] = new Coordinate(15.5,11.1);
		map[2].videoCoordinate[4][10] = new Coordinate(17.7,10.9);
		map[2].videoCoordinate[0][11] = new Coordinate(11.5,10.6);
		map[2].videoCoordinate[1][11] = new Coordinate(12.6,10.7);
		map[2].videoCoordinate[2][11] = new Coordinate(13.8,10.9);
		map[2].videoCoordinate[3][11] = new Coordinate(14.9,10.8);
		map[2].videoCoordinate[4][11] = new Coordinate(16.2,10.5);
		map[2].videoCoordinate[0][12] = new Coordinate(11.9,10.3);
		map[2].videoCoordinate[1][12] = new Coordinate(12.9,10.4);
		map[2].videoCoordinate[2][12] = new Coordinate(13.7,10.5);
		map[2].videoCoordinate[3][12] = new Coordinate(14.3,10.5);
		map[2].videoCoordinate[4][12] = new Coordinate(15.3,10.4);
		map[2].videoCoordinate[0][13] = new Coordinate(12.2,10.1);
		map[2].videoCoordinate[1][13] = new Coordinate(13,10.2);
		map[2].videoCoordinate[2][13] = new Coordinate(13.6,10.3);
		map[2].videoCoordinate[3][13] = new Coordinate(14,10.3);
		map[2].videoCoordinate[4][13] = new Coordinate(14.7,10.3);

		for (int i = 0; i < map[2].numberOfPhysicalVideoX; i++) {
			for (int j = 0; j < map[2].numberOfPhysicalVideoY; j++) {
				if (map[2].videoCoordinate[i][j] != null) {
					map[2].videoCoordinate[i][j].x = (map[2].videoCoordinate[i][j].x / map[2].videoDimensionInCM.x) * map[2].videoResolution_BoundingBox.x;
					map[2].videoCoordinate[i][j].y = (map[2].videoCoordinate[i][j].y / map[2].videoDimensionInCM.y) * map[2].videoResolution_BoundingBox.y;
				}
			}
		}		
		// For camera 3 dien thoai;
		
		map[3].fpPointInThisMap = 100;	
		map[3].numberOfPhysicalVideoX = 5;
		map[3].numberOfPhysicalVideoY = 28;
		map[3].videoCoordinate = new Coordinate[map[3].numberOfPhysicalVideoX][map[3].numberOfPhysicalVideoY];		
		map[3].fpCoordinate = new Coordinate[map[3].fpPointInThisMap + 1];
		map[3].fpPointInTheCameraArea = new int[]{40,41,42,43,44,46,47,48,49};
		for (int i = 0; i < map[1].fpPointInThisMap; i++) {
			map[3].fpCoordinate[i] = new Coordinate(Config.noPoint.locationCoor.x, Config.noPoint.locationCoor.y);
		}
		map[3].fpCoordinate[40] = new Coordinate(1,-2);
		map[3].fpCoordinate[41] = new Coordinate(3,-2);
		map[3].fpCoordinate[42] = new Coordinate(4,0);
		map[3].fpCoordinate[43] = new Coordinate(3,2);
		map[3].fpCoordinate[44] = new Coordinate(2,1);
		map[3].fpCoordinate[46] = new Coordinate(4,5.5);
		map[3].fpCoordinate[47] = new Coordinate(4.2,3.5);
		map[3].fpCoordinate[48] = new Coordinate(2,3.5); 
		map[3].fpCoordinate[49] = new Coordinate(-1,3.5); 
		
		
		map[3].videoCoordinate[0][0] = new Coordinate(0,0);
		map[3].videoCoordinate[1][0] = new Coordinate(1.6,13.7);
		map[3].videoCoordinate[2][0] = new Coordinate(6.6,15.2);
		map[3].videoCoordinate[3][0] = new Coordinate(16.2,16.1);
		map[3].videoCoordinate[4][0] = new Coordinate(22.2,15.3);
		map[3].videoCoordinate[0][1] = new Coordinate(1.1,9.9);
		map[3].videoCoordinate[1][1] = new Coordinate(4.4,11.2);
		map[3].videoCoordinate[2][1] = new Coordinate(9,12.4);
		map[3].videoCoordinate[3][1] = new Coordinate(17.4,12.8);
		map[3].videoCoordinate[4][1] = new Coordinate(23.1,12.1);
		map[3].videoCoordinate[0][2] = new Coordinate(4.1,8.8);
		map[3].videoCoordinate[1][2] = new Coordinate(6.8,9.3);
		map[3].videoCoordinate[2][2] = new Coordinate(10.8,10.1);
		map[3].videoCoordinate[3][2] = new Coordinate(18.2,10.4);
		map[3].videoCoordinate[4][2] = new Coordinate(23.6,9.7);
		map[3].videoCoordinate[0][3] = new Coordinate(6,7.3);
		map[3].videoCoordinate[1][3] = new Coordinate(8.3,7.9);
		map[3].videoCoordinate[2][3] = new Coordinate(12.1,8.5);
		map[3].videoCoordinate[3][3] = new Coordinate(18.8,8.6);
		map[3].videoCoordinate[4][3] = new Coordinate(24.1,7.9);
		map[3].videoCoordinate[0][4] = new Coordinate(7.8,6.5);
		map[3].videoCoordinate[1][4] = new Coordinate(9.7,6.8);
		map[3].videoCoordinate[2][4] = new Coordinate(13.1,7.3);
		map[3].videoCoordinate[3][4] = new Coordinate(19.2,7.2);
		map[3].videoCoordinate[4][4] = new Coordinate(24.5,6.6);

		for (int i = 0; i < map[3].numberOfPhysicalVideoX; i++) {
			for (int j = 0; j < map[3].numberOfPhysicalVideoY; j++) {
				if (map[3].videoCoordinate[i][j] != null) {
					map[3].videoCoordinate[i][j].x = (map[3].videoCoordinate[i][j].x / map[3].videoDimensionInCM.x) * map[3].videoResolution_BoundingBox.x;
					map[3].videoCoordinate[i][j].y = (map[3].videoCoordinate[i][j].y / map[3].videoDimensionInCM.y) * map[3].videoResolution_BoundingBox.y;
				}
			}
		}
	}
}

