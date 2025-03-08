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


public class WifiPreprocessAndInterpolate {
	
	
    static int startingHour;static int startingMinute;static int startingSecond;
    static int endingHour;static int endingMinute;static int endingSecond;
    static int startTimeStamp; 
    static int endTimeStamp;
    static int experimentNumber = 0; // Experiment length from the SMU video dataset
    static int numberOfWifi;
    static int[] fpPointInTheArea = {43,44,45,47,48,49,50,51,52,53,54,55,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155};
    static int[] interpolateMiddlePoint = {0,1,6,8,11,13,18,20,24,25,27,29,30,31,32,34,35,36,38,39};
	
    static String videoFolder = "src/SuntecRealData/Video/VideoLevel6Camera4R/";
	static String fileVideoInput = "resultRight.txt"; // File location of the SMU video
	static String wifiFolder = "SuntecRealData/RTLS_Comex2017_Sep_01_Study3/"; // remove src when remote
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
	
	static MapReal[] map;
	static int count14 = 0;
	static int count16 = 0;
	static double distance(Coordinate point1, Coordinate point2) {
		return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
	}
	
	static int getTimeStamp (int hour, int minute, int second) {
		int result = 0;
		int beginTimeOfTheDay = 1504195200;
		result = beginTimeOfTheDay + hour * 3600 + minute * 60 + second;
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
		for (int level = 1; level <= 6; level ++) {
			if (level == 1 || level == 4 || level == 6) {
				for (int i = 0; i < map[level].numberOfAP; i++) {
					if (map[level].apLocationAtFP[i].macAddress.equals(APMac)) {
						return map[level].apLocationAtFP[i].location;
					}
				}
			}
		}
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
	
	public static void countPercentEndAtLevel1() {
		Scanner scan;
		ParserWifiRTLS parser;
		String IDSave;
		int endLevelSave;
		int timeAppearEndOfLevel1 = 0;
		int timeAppearBeginOfLevel1 = 0;
		int timeAppearBeginOfLevel1InTimeRange = 0;
		int timeAppearBeginOfLevel1InTimeRangeAssociate = 0;
		int numberOfDevice = 0;
		int line = 0;
		try {
	    	File file = new File(wifiFolder + fileWifiInput);
	        scan = new Scanner(file);
	        scan.nextLine();
	        String dataSave = scan.nextLine();
	        parser = new ParserWifiRTLS(dataSave);
	        IDSave = parser.getIDstrRTLS();
	        if (getAPLocation(parser.getAttribute(5)) != null) {
	        	endLevelSave = getAPLocation(parser.getAttribute(5)).level;
	        }
	        else {
	        	endLevelSave = -1;
	        }
	        while (scan.hasNextLine()) {
		        dataSave = scan.nextLine();
        		line++;
        		if (line % 10000 == 0) {
        			System.out.println(line);
        		}
		        parser = new ParserWifiRTLS(dataSave);
		        String ID = parser.getIDstrRTLS();
		        if (parser.getAssociateStatus() == 2) { 
			        if (!ID.equals(IDSave)) {
			        	numberOfDevice++;
			        	IDSave = parser.getIDstrRTLS();
			        	if (endLevelSave == 1) {
			        		timeAppearEndOfLevel1++;
			        	}
			        	if (getAPLocation(parser.getAttribute(5)) != null && getAPLocation(parser.getAttribute(5)).level == 1) {
			        		timeAppearBeginOfLevel1++;
			        		if (Integer.parseInt(parser.getAttribute(0)) >= startTimeStamp && Integer.parseInt(parser.getAttribute(0)) <= endTimeStamp){
			        			timeAppearBeginOfLevel1InTimeRange++;
			        			if (Integer.parseInt(parser.getAttribute(6)) == 2) {
			        				timeAppearBeginOfLevel1InTimeRangeAssociate++;
			        			}
			        		}
			        	}
				        if (getAPLocation(parser.getAttribute(5)) != null) {
				        	endLevelSave = getAPLocation(parser.getAttribute(5)).level;
				        }
				        else {
				        	endLevelSave = -1;
				        }
			        }
			        else {
				        if (getAPLocation(parser.getAttribute(5)) != null) {
				        	endLevelSave = getAPLocation(parser.getAttribute(5)).level;
				        }
				        else {
				        	endLevelSave = -1;
				        }
			        }
		        }
	        }
	        System.out.println("NumberOfDevice: " + numberOfDevice);
	        System.out.println("Percent start: " + timeAppearBeginOfLevel1 + "--" + timeAppearBeginOfLevel1InTimeRange + "--"  + timeAppearBeginOfLevel1InTimeRangeAssociate + "--" + timeAppearBeginOfLevel1 * 1.0 / numberOfDevice);
	        System.out.println("Percent end: " + timeAppearEndOfLevel1 + " " + timeAppearEndOfLevel1 * 1.0 / numberOfDevice);
		}
		catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
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
	    	File file = new File(wifiFolder +  "WifiFrom" + startingHour + "_" + startingMinute + "_" + startingSecond + " to " +
					endingHour + "_" + endingMinute + "_" + endingSecond + ".csv");
	        scan = new Scanner(file);	        	        
	        while (scan.hasNextLine()) {
	        	String data = scan.nextLine();
	        	parser = new ParserWifiRTLS(data);
	        	ID = Integer.parseInt(parser.getAttribute(0));
	        	int time = Integer.parseInt(parser.getAttribute(1)) % 10000;
	        	int level = Integer.parseInt(parser.getAttribute(2).charAt(0) + "");
	        	int location = Integer.parseInt(parser.getAttribute(2).substring(1, 4));
	        	peopleWifi[ID][countTimeWifiReport[ID]] = new TimeWithLocation(level, location, time);
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
    
    public static double distanceToStair(int level, Coordinate currentLocation) {
    	NormalLocation stair = findNearestStair(level, currentLocation);
    	return distanceSameLevel(currentLocation, map[level].fpCoordinate[stair.locationID]);
    }
    
    public static NormalLocation findNearestStair(int level, Coordinate currentLocation) {
    	if (level == 1) {
    		if (currentLocation.x >= 14.5) {
    			return new NormalLocation(level, 27);
    		}
    		else {
    			return new NormalLocation(level, 42);
    		}
    	}
    	if (level == 4) {
    		if (currentLocation.x >= -9) {
    			return new NormalLocation(level, 595);
    		}
    		else {
    			return new NormalLocation(level, 566);
    		}
    	}
    	if (level == 6) {
    		if (currentLocation.x <= 17) {
    			return new NormalLocation(level, 136);
    		}
    		else {
    			return new NormalLocation(level, 103);
    		}    		
    	}
    	return null;
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

    
    public static double distanceMultiLevel(NormalLocationCoordinate startLoc, NormalLocationCoordinate endLoc) {
    	int currentLevel = startLoc.level;
    	Coordinate currentLocation = startLoc.locationCoor;
    	int endLevel = endLoc.level;
    	Coordinate endLocation = endLoc.locationCoor;
    	double distance = 0;
    	if (currentLevel < endLevel) {//go up
    		//move to the point to go up;
    		do {
	    		distance = distance + distanceToStair(currentLevel, currentLocation);
	    		//go upstair
	    		distance = distance + map[currentLevel].distanceGoStair; // set fixed to 5 units, since we don't interpolate from level 1 to 3
	    		currentLocation = getNextLocationOnUpLevel(currentLevel, currentLocation);
	    		currentLevel = map[currentLevel].upLevel;
	    		
    		}
    		while (currentLevel < endLevel);
    		//move to end Location
    		distance = distance + distanceSameLevel(currentLocation,endLocation);
    	}
    	else {
    		if (currentLevel > endLevel) {//go down
        		do {
    	    		distance = distance + distanceToStair(currentLevel, currentLocation);
    	    		//go downstair
    	    		distance = distance + map[currentLevel].distanceGoStair;
    	    		currentLocation = getNextLocationOnDownLevel(currentLevel, currentLocation);
    	    		currentLevel = map[currentLevel].downLevel;
        		}
        		while (currentLevel > endLevel);
        		//move to end Location
        		distance = distance + distanceSameLevel(currentLocation,endLocation);
    			
    		}
    		else { 
    			distance = distance + distanceSameLevel(currentLocation,endLocation);
    		}
    	}
    	return distance;
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
    
    public static double[] getNextLocation(int currentLevel, Coordinate currentLocation, double distanceAtChangeLevelPoint, int endLevel, Coordinate endLocation, double distanceMove) {
    	double[] result = new double[4]; 
    	Coordinate directionMove = new Coordinate(0,0);
    	Coordinate targetPoint = new Coordinate(0,0);
    	if (currentLevel == endLevel) {// same level, just move toward the target point
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
    	}
    	else {
    		if (currentLevel < endLevel) {
    			while (distanceMove > 0) {
    				NormalLocation stair = findNearestStair(currentLevel, currentLocation);
    				if (similarCoordinate(currentLocation, map[stair.level].fpCoordinate[stair.locationID])) {// already stay at the stair point
    					if (distanceMove - (map[currentLevel].distanceGoStair - distanceAtChangeLevelPoint) > 0){//enough to go up stair
    						distanceMove = distanceMove - (map[currentLevel].distanceGoStair - distanceAtChangeLevelPoint);
    						currentLocation = getNextLocationOnUpLevel(currentLevel, currentLocation);
        					currentLevel = map[currentLevel].upLevel;        					
        					if (currentLevel == 6) {
        						break;
        					}
    					}
    					else {
    						distanceAtChangeLevelPoint += distanceMove;
    						distanceMove = 0;
    					}
    				}
    				else {
    	    			targetPoint = new Coordinate(map[currentLevel].fpCoordinate[stair.locationID].x, map[currentLevel].fpCoordinate[stair.locationID].y);
    	        		directionMove = new Coordinate(targetPoint.x - currentLocation.x,
    							   					   targetPoint.y - currentLocation.y);
    	        		if (distanceMove < getModulo(directionMove)) {//Move toward the point to go up
    		        		Coordinate vectorMove = new Coordinate(directionMove.x * distanceMove / getModulo(directionMove),directionMove.y * distanceMove / getModulo(directionMove));
    		        		currentLocation = new Coordinate(currentLocation.x + vectorMove.x, currentLocation.y + vectorMove.y);
    		        		distanceMove = 0;
    	        		}
    	        		else {
    	        			currentLocation = new Coordinate(map[stair.level].fpCoordinate[stair.locationID]);
    	        			distanceMove = distanceMove - getModulo(directionMove);
    	        		}

    				}
    			}
    		}
    		else {
    			while (distanceMove > 0) {
    				NormalLocation stair = findNearestStair(currentLevel, currentLocation);
    				if (similarCoordinate(currentLocation, map[stair.level].fpCoordinate[stair.locationID])) {// already stay at the stair point
    					if (distanceMove - (map[currentLevel].distanceGoStair - distanceAtChangeLevelPoint) > 0){//enough to go down stair
    						distanceMove = distanceMove - (map[currentLevel].distanceGoStair - distanceAtChangeLevelPoint);
    						currentLocation = getNextLocationOnDownLevel(currentLevel, currentLocation);
        					currentLevel = map[currentLevel].downLevel;        					
        					if (currentLevel == 1) {
        						break;
        					}
    					}
    					else {
    						distanceAtChangeLevelPoint += distanceMove;
    						distanceMove = 0;
    					}
    				}
    				else {
    	    			targetPoint = new Coordinate(map[currentLevel].fpCoordinate[stair.locationID].x, map[currentLevel].fpCoordinate[stair.locationID].y);
    	        		directionMove = new Coordinate(targetPoint.x - currentLocation.x,
    							   					   targetPoint.y - currentLocation.y);
    	        		if (distanceMove < getModulo(directionMove)) {//Move toward the point to go down
    		        		Coordinate vectorMove = new Coordinate(directionMove.x * distanceMove / getModulo(directionMove),directionMove.y * distanceMove / getModulo(directionMove));
    		        		currentLocation = new Coordinate(currentLocation.x + vectorMove.x, currentLocation.y + vectorMove.y);
    		        		distanceMove = 0;
    	        		}
    	        		else {
    	        			currentLocation = new Coordinate(map[stair.level].fpCoordinate[stair.locationID]);
    	        			distanceMove = distanceMove - getModulo(directionMove);
    	        		}
    				}
    			}
    		}
    	}
    	result[0] = currentLevel; 
    	result[1] = currentLocation.x;
    	result[2] = currentLocation.y;
    	result[3] = distanceAtChangeLevelPoint;
    	return result;
    }
    
    public static timeWithLocationCoordinate[] fillInterpolateData(timeWithLocationCoordinate timeLocation1, timeWithLocationCoordinate timeLocation2) {
    	if (!(timeLocation1.location.level == 1 && timeLocation2.location.level == 4) || (timeLocation1.location.level == 4 && timeLocation2.location.level == 1)) { 
	    	double distance = distanceMultiLevel(timeLocation1.location, timeLocation2.location);
	    	int duration = timeLocation2.time - timeLocation1.time;
	    	if (duration <= 0) {
	    		return null;
	    	}
	    	double distanceMoveEachStep = distance / duration;
	    	int currentLevel = timeLocation1.location.level;
	    	Coordinate currentLocation = timeLocation1.location.locationCoor;
	    	double distanceAtChangeLevelPoint = 0;
	    	int endLevel = timeLocation2.location.level;
	    	Coordinate endLocation = timeLocation2.location.locationCoor;
	    	timeWithLocationCoordinate[] result = new timeWithLocationCoordinate[duration];
	    	result[0] = timeLocation1;
	    	for (int i = 1; i < duration; i++) {
	    		double[] newLoc = getNextLocation(currentLevel, currentLocation, distanceAtChangeLevelPoint, endLevel, endLocation, distanceMoveEachStep);
	    		if (Double.isNaN(newLoc[1])){
	    			System.out.println(newLoc[1]);
	    			
	    		}
	    		currentLevel = (int)newLoc[0];
	    		currentLocation = new Coordinate(newLoc[1], newLoc[2]);
	    		distanceAtChangeLevelPoint = newLoc[3];
	    		result[i] = new timeWithLocationCoordinate(currentLevel, currentLocation, timeLocation1.time + i);
	    	}
	    	return result;
    	}
    	else {
    		return null;
    	}
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
    
    public static void removeErrorFloor(int ID, boolean[] removeElement) {
    	TimeWithLocation[] tmp = new TimeWithLocation[Config.maximumWifiReportCount];    	
    	for (int i = 0; i < countTimeWifiReport[ID]; i++) {
    		tmp[i] = peopleWifi[ID][i];
    	}
    	peopleWifi[ID] = new TimeWithLocation[Config.maximumWifiReportCount];
    	int index = 0;
    	for (int i = 0; i < countTimeWifiReport[ID]; i++) {
    		if (removeElement[i] == false) {
    			peopleWifi[ID][index] = new TimeWithLocation(tmp[i].location.level, tmp[i].location.locationID, tmp[i].time);
    			index++;
    		}
    	}
    	countTimeWifiReport[ID] = index;
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
	        	int time = Integer.parseInt(parser.getAttribute(1)) % 10000;
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
		    			writer.write(newID + "," + peopleWifiAfterCleaning[ID][k].time + "," + peopleWifiAfterCleaning[ID][k].location.level + "," + peopleWifiAfterCleaning[ID][k].location.locationCoor.x + "," + peopleWifiAfterCleaning[ID][k].location.locationCoor.y);
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
	        	int time = Integer.parseInt(parser.getAttribute(1)) % 10000;
	        	int level = Integer.parseInt(parser.getAttribute(2));
	        	Coordinate location = new Coordinate(Double.parseDouble(parser.getAttribute(3)),Double.parseDouble(parser.getAttribute(4)));
	        	peopleWifiAfterCleaning[ID][countTimeWifiReportAfterCleaning[ID]] = new timeWithLocationCoordinate(level, location, time);
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
	    			writer.write(ID + "," + peopleWifiAfterInterpolate[ID][k].time + "," + peopleWifiAfterInterpolate[ID][k].location.level + "," + peopleWifiAfterInterpolate[ID][k].location.locationCoor.x + "," + peopleWifiAfterInterpolate[ID][k].location.locationCoor.y);
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
    
    public static void readAfterInterpolation() {
	    Scanner scan;	    
	    ParserWifiRTLS parser;
	    int ID = 0;	 
	    numberOfWifi = 0;
	    peopleWifiAfterInterpolate = new timeWithLocationCoordinate[Config.maximumBlobVideoCount][Config.maximumWifiReportCount];
	    countTimeWifiReportAfterInterpolate = new int[Config.maximumBlobVideoCount];
	    try {
	    	File file = new File(wifiFolder +  "afterInterpolation.csv");
	        scan = new Scanner(file);	        	        
	        while (scan.hasNextLine()) {
	        	String data = scan.nextLine();
	        	parser = new ParserWifiRTLS(data);
	        	ID = Integer.parseInt(parser.getAttribute(0));
	        	int time = Integer.parseInt(parser.getAttribute(1)) % 10000;
	        	int level = Integer.parseInt(parser.getAttribute(2));
	        	Coordinate location = new Coordinate(Double.parseDouble(parser.getAttribute(3)),Double.parseDouble(parser.getAttribute(4)));
	        	peopleWifiAfterInterpolate[ID][countTimeWifiReportAfterInterpolate[ID]] = new timeWithLocationCoordinate(level, location, time);
	        	countTimeWifiReportAfterInterpolate[ID]++;	
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
    
    public static void removeWrongLevelReport() { //Find the initial level of a person. If the difference between two level is >=3 then confirm the initial level.
    	 // count the appearance of each level
    	for (int ID = 0; ID < numberOfWifi; ID++) {
    		if (ID == 2) {
    			ID = 2;
    		}
    		int[] count = new int[7];
    		boolean[] removeElement = new boolean[10000];
    		for (int i = 0; i < countTimeWifiReport[ID]; i++) {
    			int level = peopleWifi[ID][i].location.level;
    			count[level]++;
    			int currentLevel = getInitialLevel(count); //person start at this level at startingAtThisLevel time
    			if (currentLevel > 0) { //found the initial level
        			int startingAtThisLevel = 0;
        			for (int j = i - 1; j >= 0; j--) {
        				if (peopleWifi[ID][j].location.level != level) {
        					startingAtThisLevel = j + 1;
        					break;
        				}
        			}
    				for (int j = 0; j < startingAtThisLevel; j++) {
    					removeElement[j] = true;
    				}    				
    				int[] thresholdToSwitchFloor = new int[7];
    				int lastSwitchStep = i;
    				for (int currentStep = i + 1; currentStep < countTimeWifiReport[ID]; currentStep++) { 
    					//check from that step, if different level then +1, else -1. If value thresholdToSwitchFloor >= 4 --> switch to new floor
    					if (peopleWifi[ID][currentStep].location.level != currentLevel) {
    						thresholdToSwitchFloor[peopleWifi[ID][currentStep].location.level]++; 
    						if (thresholdToSwitchFloor[peopleWifi[ID][currentStep].location.level] >= 4) {//Switch level    							
    							for (int k = lastSwitchStep + 1; k < currentStep - 3; k++) {
    								if (peopleWifi[ID][k].location.level != peopleWifi[ID][lastSwitchStep].location.level) {
    									//mark to remove all error level 
    									removeElement[k] = true;
    								}
    							}
    							currentLevel = peopleWifi[ID][currentStep].location.level;
    							thresholdToSwitchFloor = new int[7];
    							for (int k = currentStep - 3; k <= currentStep; k++) {
    								if (peopleWifi[ID][k].location.level != currentLevel) {
    									//mark to remove all error level
    									removeElement[k] = true;
    								}
    							}
    							lastSwitchStep = currentStep;
    						}
    					}
    					else {
    						for (int k = 1; k <= 6; k++) {
    							if (k != currentLevel && thresholdToSwitchFloor[k] > 0) {
    								thresholdToSwitchFloor[k]--;
    							}
    						}
    					}
    				}
					for (int k = lastSwitchStep + 1; k < countTimeWifiReport[ID]; k++) {
						if (peopleWifi[ID][k].location.level != peopleWifi[ID][lastSwitchStep].location.level) {
							//mark to remove all error level 
							removeElement[k] = true;
						}
					}
    				//Remove all error floor of ID
    				removeErrorFloor(ID, removeElement);
    				break;
    			}
    			
    		}
    	}    	
    }
    
    
    public static boolean numberOfElemenetInThisClusterLessThanEqual3(int ID, int[] elementList, int elementNumber, int left, int right) {    	
	    for(int i=left; i<right; i++){
	    	int locationNumber = peopleWifi[ID][i].location.level * 1000 + peopleWifi[ID][i].location.locationID;
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
    		if (list[i] != null && list[i].level == location.level && list[i].locationID == location.locationID) {
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
    		if (map[locationList[i].level].fpCoordinate[locationList[i].locationID].x != Config.noPoint.locationCoor.x &&
    				map[locationList[i].level].fpCoordinate[locationList[i].locationID].y != Config.noPoint.locationCoor.y) { //the location is far from observed area
    			sumWeight += weight[i];
    			result.x += map[locationList[i].level].fpCoordinate[locationList[i].locationID].x * weight[i];
    			result.y += map[locationList[i].level].fpCoordinate[locationList[i].locationID].y * weight[i];
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
	    			int level = peopleWifi[ID][startCluster].location.level;
	    			//smooth from (t0 + t1)/2
	    			if (startCluster == 0 && map[level].fpCoordinate[peopleWifi[ID][0].location.locationID].x != Config.noPoint.locationCoor.x &&
	    					map[level].fpCoordinate[peopleWifi[ID][0].location.locationID].y != Config.noPoint.locationCoor.y) {//For the first cluster
	    				peopleWifiAfterCleaning[ID][0] = new timeWithLocationCoordinate(level, map[level].fpCoordinate[peopleWifi[ID][0].location.locationID], peopleWifi[ID][0].time);
	    				countTimeWifiReportAfterCleaning[ID] = 1;
	    			}
	    			int time = (peopleWifi[ID][startCluster].time + peopleWifi[ID][i - 1].time) / 2;
	    			peopleWifiAfterCleaning[ID][countTimeWifiReportAfterCleaning[ID]] = new timeWithLocationCoordinate(level, meanCluster, time);
	    			countTimeWifiReportAfterCleaning[ID]++;
	    			
	    			/*for (int k = peopleWifi[ID][startCluster].time; k <= peopleWifi[ID][i - 1].time; k++) {//Assume person stay still at the mean location
	    				peopleWifiAfterCleaning[ID][countTimeWifiReportAfterCleaning[ID]] = new timeWithLocationCoordinate(level, meanCluster, k);
	    				countTimeWifiReportAfterCleaning[ID]++;
	    			}*/
	    			
    			}
    			startCluster = i;
    		}
    	}
    	//For the last cluster    	
	    int level = peopleWifi[ID][countTimeWifiReport[ID] - 1].location.level;
	    int time = peopleWifi[ID][countTimeWifiReport[ID] - 1].time;
	    if (map[level].fpCoordinate[peopleWifi[ID][countTimeWifiReport[ID] - 1].location.locationID].x != Config.noPoint.locationCoor.x &&
	    		map[level].fpCoordinate[peopleWifi[ID][countTimeWifiReport[ID] - 1].location.locationID].y != Config.noPoint.locationCoor.y) {	    	
	    	peopleWifiAfterCleaning[ID][countTimeWifiReportAfterCleaning[ID]] = new timeWithLocationCoordinate(level, map[level].fpCoordinate[peopleWifi[ID][countTimeWifiReport[ID] - 1].location.locationID], time);
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
	    				if (peopleWifi[ID][i].location.locationID == peopleWifi[ID][j].location.locationID && peopleWifi[ID][i].location.level == peopleWifi[ID][j].location.level) {
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
    	if (l1.location.level == l2.location.level && l1.location.locationCoor.x == l2.location.locationCoor.x && l1.location.locationCoor.y == l2.location.locationCoor.y) {
    		return false;
    	}
    	else {
    		return true;
    	}
    }
    
    public static timeWithLocationCoordinate getMiddlePoint(timeWithLocationCoordinate l1, timeWithLocationCoordinate l2) {
    	timeWithLocationCoordinate result;
    	if (l1.location.level != l2.location.level) {
    		int time = (l1.time + l2.time) / 2 + 1;
    		result = new timeWithLocationCoordinate(l1.location.level, l1.location.locationCoor, time);
    	}
    	else {
    		int time = (l1.time + l2.time) / 2 + 1;
    		Coordinate newCoordinate = new Coordinate((l1.location.locationCoor.x + l2.location.locationCoor.x) / 2, (l1.location.locationCoor.y + l2.location.locationCoor.y) / 2);
    		NormalLocationCoordinate newLocation = new NormalLocationCoordinate(l1.location.level, newCoordinate);
    		result = new timeWithLocationCoordinate(newLocation, time); 
    	}
    	return result;
    }
    
    public static boolean isdifferentLevel(timeWithLocationCoordinate l1, timeWithLocationCoordinate l2) {
    	if (l1.location.level != l2.location.level) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
    public static boolean interpolateMidPoint(int ID) {
    	for (int i = 0; i < interpolateMiddlePoint.length; i++) {
    		if (interpolateMiddlePoint[i] == ID) {
    			return true;
    		}
    	}
    	return true;
    }
    
    public static void interpolate() {
    	peopleWifiAfterInterpolate = new timeWithLocationCoordinate[Config.maximumBlobVideoCount][Config.maximumWifiReportCount];
    	countTimeWifiReportAfterInterpolate = new int[Config.maximumWifiReportCount];
    	for (int ID = 0; ID < numberOfWifi; ID++) {
    		if (interpolateMidPoint(ID)) {
	    		countTimeWifiReportAfterInterpolate[ID] = 0;
	    		if (peopleWifiAfterCleaning[ID][0] != null) {
		    		timeWithLocationCoordinate beginInterpolate = new timeWithLocationCoordinate(peopleWifiAfterCleaning[ID][0].location.level, peopleWifiAfterCleaning[ID][0].location.locationCoor, peopleWifiAfterCleaning[ID][0].time);
		    		timeWithLocationCoordinate endInterpolate;
		    		for (int i = 0; i < countTimeWifiReportAfterCleaning[ID] - 1; i++) {
		    			if (peopleWifiAfterCleaning[ID][i + 1].time - peopleWifiAfterCleaning[ID][i].time > 1) {
			    			if (isdifferentLocation(peopleWifiAfterCleaning[ID][i], peopleWifiAfterCleaning[ID][i + 1])) {
			    				endInterpolate = getMiddlePoint(peopleWifiAfterCleaning[ID][i], peopleWifiAfterCleaning[ID][i + 1]);
			    				System.out.println(ID);
			    				timeWithLocationCoordinate[] result = fillInterpolateData(beginInterpolate,endInterpolate);
				    			if (result != null) {
					    			for (int k = 0; k < result.length; k++) {
					    				peopleWifiAfterInterpolate[ID][countTimeWifiReportAfterInterpolate[ID]] = new timeWithLocationCoordinate(result[k].location.level, result[k].location.locationCoor, result[k].time);
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
		    					peopleWifiAfterInterpolate[ID][countTimeWifiReportAfterInterpolate[ID]] = new timeWithLocationCoordinate(peopleWifiAfterCleaning[ID][i].location.level, peopleWifiAfterCleaning[ID][i].location.locationCoor, peopleWifiAfterCleaning[ID][i].time);
		    					countTimeWifiReportAfterInterpolate[ID]++;
		    				}
		    			}
		
		    		}
		    		endInterpolate = new timeWithLocationCoordinate(peopleWifiAfterCleaning[ID][countTimeWifiReportAfterCleaning[ID] - 1].location, peopleWifiAfterCleaning[ID][countTimeWifiReportAfterCleaning[ID] - 1].time);
					timeWithLocationCoordinate[] result = fillInterpolateData(beginInterpolate,endInterpolate);
					if (result != null) {
		    			for (int k = 0; k < result.length; k++) {
		    				peopleWifiAfterInterpolate[ID][countTimeWifiReportAfterInterpolate[ID]] = new timeWithLocationCoordinate(result[k].location.level, result[k].location.locationCoor, result[k].time);
		    				countTimeWifiReportAfterInterpolate[ID]++;
		    			}
					}
	    		}
    		}
    		/*else {
	    		for (int i = 0; i < countTimeWifiReportAfterCleaning[ID] - 1; i++) {
	
	    			if (peopleWifiAfterCleaning[ID][i + 1].time - peopleWifiAfterCleaning[ID][i].time > 1) {
		    			timeWithLocationCoordinate[] result = fillInterpolateData(peopleWifiAfterCleaning[ID][i], peopleWifiAfterCleaning[ID][i+1]);
		    			if (result != null) {
			    			for (int k = 0; k < result.length; k++) {
			    				peopleWifiAfterInterpolate[ID][countTimeWifiReportAfterInterpolate[ID]] = new timeWithLocationCoordinate(result[k].location.level, result[k].location.locationCoor, result[k].time);
			    				countTimeWifiReportAfterInterpolate[ID]++;
			    			}
		    			}
	    			}
	    			else {
	    				if (peopleWifiAfterCleaning[ID][i + 1].time - peopleWifiAfterCleaning[ID][i].time == 1) {
	    					peopleWifiAfterInterpolate[ID][countTimeWifiReportAfterInterpolate[ID]] = new timeWithLocationCoordinate(peopleWifiAfterCleaning[ID][i].location.level, peopleWifiAfterCleaning[ID][i].location.locationCoor, peopleWifiAfterCleaning[ID][i].time);
	    					countTimeWifiReportAfterInterpolate[ID]++;
	    				}
	    			}
	    		}
    		}*/
    	}
    	writeAfterInterpolation();    	
    }
    
    public static void outputWifiLocationForManualTracing() {
		BufferedWriter writer;
	    try {
	    	writer = new BufferedWriter(new FileWriter(wifiFolder + "wifiLocationForManualTracing.csv"));
	    	int newID = 0;
	    	for (int ID = 0; ID < numberOfWifi; ID++) {
	    		if (isGoodSample[ID] == true) {	    
		    		for (int k = 0; k < countTimeWifiReport[ID]; k++) {	    	
		    			int time = peopleWifi[ID][k].time;
		    			int level = peopleWifi[ID][k].location.level;
		    			int locationID = peopleWifi[ID][k].location.locationID;
		    			writer.write(newID + "," + time + "," + level + "," + map[level].fpCoordinate[locationID].x + "," + map[level].fpCoordinate[locationID].y);
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
    
    public static void interpolateData() {
    	removeWrongLevelReport(); //apply heuristic
    	writeAfterRemoveWrongLevel();
    	//---------------------
    	readAfterRemoveWrongLevel();
    	selectGoodSamples();
    	outputWifiLocationForManualTracing();
    	inferLocationDueToBouncingSignal(); 
    	writeAfterInferLocationDueToBouncingSignal();
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
    
    public static int[] patternSatisfy(int ID) {
    	//nho doi lai peopleWifiAfterCleaning
    	//pattern to check: 1 --> 4 --> 6 --> 4 --> 1 --> 1000;
    	/*int[] result = new int[3];
    	result[0] = -1;
    	int patternToCheck = 1;
    	int type = 0;
    	for (int i = 0; i < countTimeWifiReportAfterCleaning[ID]; i++) {
    		if (peopleWifiAfterCleaning[ID][0].location.level != 1) {
    			result[0] = -1;
    			return result;
    		}
    		System.out.println(ID);
    		if (peopleWifiAfterCleaning[ID][i].location.level == patternToCheck){
    			if (peopleWifiAfterCleaning[ID][i + 1].location.level == nextPattern(patternToCheck, type)[0]) {
	    			int[] temp = nextPattern(patternToCheck, type);
	    			patternToCheck = temp[0];
	    			type = temp[1];
    			}
    			if (nextPattern(patternToCheck, type)[0] == 1000) {
    				result[0] = 1;
    			}
    		}
    	}*/
    	int[] result = new int[3];
    	result[0] = -1;
    	int patternToCheck = 1;
    	int type = 0;
    	
		if (peopleWifi[ID][0].location.level != 1) {
			result[0] = -1;
			return result;
		} 		
    	for (int i = 0; i < countTimeWifiReport[ID] - 1; i++) {   		
    		if (peopleWifi[ID][i].location.level == patternToCheck){
    			if (peopleWifi[ID][i + 1].location.level == nextPattern(patternToCheck, type)[0]) {
	    			int[] temp = nextPattern(patternToCheck, type);
	    			patternToCheck = temp[0];
	    			type = temp[1];
	    			if (patternToCheck == 4) {
	    				count14++;
	    				System.out.println(ID);
	    			}
    			}
    			if (nextPattern(patternToCheck, type)[0] == 1000) {
    				result[0] = 1;
    			}
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
    		isGoodSample[ID] = false;
    		String s = "";
    		if (countTimeWifiReport[ID] > 0) {
	    		int duration = peopleWifi[ID][countTimeWifiReport[ID] - 1].time - peopleWifi[ID][0].time;
	    		double frequency = 0;
	    		if (duration != 0) {
	    			frequency = countTimeWifiReport[ID] * 1.0 / duration;
	
	    		}
	    		for (int i = 0; i < countTimeWifiReport[ID]; i++) {
	    			if (i == 0 || peopleWifi[ID][i].location.level != peopleWifi[ID][i - 1].location.level) {
	    				s = s + peopleWifi[ID][i].location.level;
	    			}
	    		}
	    		if (s.length() >= 2 && duration >= Config.durationThresh && frequency >= Config.frequencyThresh) {
	    			isGoodSample[ID] = true;
	    		}
    		}
    	}
    }
    
    public static void countNumberOfEachPattern() {
    	int durationThresh = 240;
    	double frequencyThresh = 0.05;
    	int numberOfSatifsyCondition = 0;
    	String[] patternList = new String[100];
    	int numberOfPattern = 0;
    	int[] countPatternList = new int[100];
    	int[] duration = new int[Config.maximumWifiReportCount];
    	double durationAverage = 0;
    	double[] frequency = new double[Config.maximumWifiReportCount];
    	double frequencyAverage = 0;
    	int numberOfDeviceDurationBiggerZero = 0;
    	for (frequencyThresh = 0.04; frequencyThresh <= 0.1; frequencyThresh = frequencyThresh + 0.01) {
    		for (durationThresh = 60; durationThresh <= 180; durationThresh = durationThresh + 30) {    		
    			numberOfSatifsyCondition = 0;
		    	for (int ID = 0; ID < numberOfWifi; ID++) {
		    		String s = "";
		    		duration[ID] = peopleWifi[ID][countTimeWifiReport[ID] - 1].time - peopleWifi[ID][0].time;    	
		    		if (duration[ID] != 0) {
		    			frequency[ID] = countTimeWifiReport[ID] * 1.0 / duration[ID];
		    			durationAverage += duration[ID];
		    			frequencyAverage += frequency[ID];
		    			numberOfDeviceDurationBiggerZero++; 
		    		}
		    		for (int i = 0; i < countTimeWifiReport[ID]; i++) {
		    			if (i == 0 || peopleWifi[ID][i].location.level != peopleWifi[ID][i - 1].location.level) {
		    				s = s + peopleWifi[ID][i].location.level;
		    			}
		    		}
		    		for (int i = 1; i <= s.length(); i++) {
		    			if (i > 5) {
		    				break;
		    			}
		    			String pattern = s.substring(0, i);
		    			int index = findInPatternList(patternList, numberOfPattern, pattern);
		    			if (index == -1) {   
		    				countPatternList[numberOfPattern] = 1;
		    				patternList[numberOfPattern] = pattern;
		    				numberOfPattern++;
		    			}
		    			else {
		    				countPatternList[index]++;
		    			}
		    		}
		    		if (s.length() >= 2 && duration[ID] >= durationThresh && frequency[ID] >= frequencyThresh) {
		    			numberOfSatifsyCondition++;
		    		}
		    	}
		    	for (int i = 0; i < numberOfPattern - 1; i++) {
		    		for (int j = i + 1; j < numberOfPattern; j++) {
		    			if (patternBiggerThan(patternList[i], patternList[j])) {
		    				String t = patternList[i];
		    				patternList[i] = patternList[j];
		    				patternList[j] = t;
		    				int tmp = countPatternList[i];
		    				countPatternList[i] = countPatternList[j];
		    				countPatternList[j] = tmp;
		    			}
		    		}
		    	}
		    	/*for (int i = 0; i < numberOfPattern; i++) {
		    		System.out.println(patternList[i] + ": " + countPatternList[i]);
		    	}*/
		    	//System.out.println("Duration threshold: " + durationThresh);
		    	//System.out.println("Frequency threshold: " + frequencyThresh);
		    	System.out.print(numberOfSatifsyCondition + " ");
		    	//System.out.println("------------------------");
	    	}
    		System.out.println();
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
		selectWifiFromExaminedTime(); //only run when need to select at new time duration
		readWifiData();		
		//countNumberOfMacAddressInTheArea(); //only run for counting Mac address
		interpolateData();					
	}
	
	public static void createMap() {
		map = new MapReal[2];
		for (int i = 0; i < 2; i++) {
			map[i] = new MapReal();
		}
		// For map level 1;
		
		map[1].fpPointInThisMap = 160;	
		map[1].numberOfPhysicalVideoX = 0;
		map[1].numberOfPhysicalVideoY = 0;
		map[1].upLevel = 4;
		map[1].downLevel = 1;
		map[1].videoCoordinate = new Coordinate[map[1].numberOfPhysicalVideoX][map[1].numberOfPhysicalVideoY];		
		map[1].fpCoordinate = new Coordinate[map[1].fpPointInThisMap + 1];

		for (int i = 0; i < map[1].fpPointInThisMap; i++) {
			map[1].fpCoordinate[i] = new Coordinate(Config.noPoint.locationCoor.x, Config.noPoint.locationCoor.y);
		}
		map[1].numberOfAP = 13;
		map[1].apLocationAtFP = new APLocation[] {new APLocation("108.243.127.198.214.144", 1, 63),
				new APLocation("108.243.127.205.178.172", 1, 56),
				new APLocation("156.28.18.192.34.72", 1, 33),
				new APLocation("108.243.127.198.200.250", 1, 19),
				new APLocation("108.243.127.198.204.66", 1, 85),
				new APLocation("108.243.127.198.207.102", 1, 76),
				new APLocation("156.28.18.192.34.30", 1, 000),
				new APLocation("156.28.18.192.33.248", 1, 000),
				new APLocation("156.28.18.192.34.34", 1, 000),
				new APLocation("156.28.18.192.34.40", 1, 000),
				new APLocation("156.28.18.192.34.20", 1, 000),
				new APLocation("108.243.127.205.111.254", 1, 000),
				new APLocation("108.243.127.205.109.88", 1, 000)};
		map[1].fpCoordinate[63] = new Coordinate(1,5.5);
		map[1].fpCoordinate[56] = new Coordinate(7.1,3);
		map[1].fpCoordinate[33] = new Coordinate(15,5.2);
		map[1].fpCoordinate[19] = new Coordinate(19,3.5);
		map[1].fpCoordinate[85] = new Coordinate(25.3,5.2);
		map[1].fpCoordinate[76] = new Coordinate(28.5,5.4);
		map[1].fpCoordinate[42] = new Coordinate(13,4); //stair
		map[1].fpCoordinate[27] = new Coordinate(16,4); //stair
		
		// For map level 6;
		map[6].fpPointInThisMap = 160;
		map[6].goesDownPoint = new int[] {136,103};
		map[6].fpPointInTheCameraArea = new int[]{43,44,45,47,48,49,50,51,52,53,54,55,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155};		
		map[6].numberOfPhysicalVideoX = 8;
		map[6].numberOfPhysicalVideoY = 4;
		map[6].upLevel = 6;
		map[6].downLevel = 4;
		map[6].videoCoordinate = new Coordinate[map[6].numberOfPhysicalVideoX][map[6].numberOfPhysicalVideoY];		
		map[6].fpCoordinate = new Coordinate[map[6].fpPointInThisMap + 1];
		for (int i = 0; i < map[6].fpPointInThisMap; i++) {
			map[6].fpCoordinate[i] = new Coordinate(Config.noPoint.locationCoor.x, Config.noPoint.locationCoor.y);
		}
		map[6].numberOfAP = 27;
		map[6].apLocationAtFP = new APLocation[] {new APLocation("156.28.18.192.33.238", 6, 153),//151 -> 153
				//new APLocation("156.28.18.192.54.138", 6, 29),//corner
				//new APLocation("156.28.18.192.54.137", 6, 30),//corner
				new APLocation("156.28.18.192.54.136", 6, 28),//52 --> 28
				//new APLocation("156.28.18.192.33.230", 6, 46),//toilet
				new APLocation("156.28.18.192.53.240", 6, 154),//140 --> 154
				//new APLocation("156.28.18.192.33.216", 6, 42),//toilet
				//new APLocation("156.28.18.192.54.254", 6, 39),//toilet
				new APLocation("156.28.18.192.56.166", 6, 129),
				new APLocation("156.28.18.192.55.12", 6, 64),
				new APLocation("156.28.18.192.32.220", 6, 109),
				new APLocation("156.28.18.192.56.164", 6, 37),
				new APLocation("156.28.18.192.33.210", 6, 35),
				new APLocation("156.28.18.192.77.190", 6, 99),
				new APLocation("156.28.18.192.33.218", 6, 31),
				new APLocation("156.28.18.192.55.164", 6, 82),
				new APLocation("156.28.18.192.34.14", 6, 87),
				new APLocation("156.28.18.192.54.128", 6, 000),
				new APLocation("156.28.18.192.33.66", 6, 000),
				new APLocation("156.28.18.192.33.64", 6, 000),
				new APLocation("156.28.18.192.50.210", 6, 000),
				new APLocation("156.28.18.192.33.8", 6, 000),
				new APLocation("108.243.127.205.110.10", 6, 000),
				new APLocation("108.243.127.205.105.126", 6, 000),
				new APLocation("156.28.18.192.53.136", 6, 000),
				new APLocation("156.28.18.192.32.214", 6, 000),
				new APLocation("156.28.18.192.55.68", 6, 000),
				new APLocation("156.28.18.192.52.232", 6, 000),
				new APLocation("156.28.18.192.53.248", 6, 000),
				new APLocation("156.28.18.192.55.132", 6, 000),
				new APLocation("156.28.18.192.33.54", 6, 000),
				new APLocation("108.243.127.205.108.246", 6, 000)};
		
		map[6].fpCoordinate[28] = new Coordinate(4,4.5);//add new fp(4,3) -> (4,4.5)
		map[6].fpCoordinate[29] = new Coordinate(2.5,3); //add new fp
		map[6].fpCoordinate[30] = new Coordinate(5.7,3);//add new fp
		map[6].fpCoordinate[31] = new Coordinate(27,2.8);
		map[6].fpCoordinate[35] = new Coordinate(25.5,2.2);
		map[6].fpCoordinate[37] = new Coordinate(22,2.8);
		map[6].fpCoordinate[39] = new Coordinate(12.5,2.8);
		map[6].fpCoordinate[42] = new Coordinate(9,2.1);
		map[6].fpCoordinate[43] = new Coordinate(8.2,0.9);
		map[6].fpCoordinate[44] = new Coordinate(7.5,0.9);
		map[6].fpCoordinate[45] = new Coordinate(7.0,1.5);
		map[6].fpCoordinate[46] = new Coordinate(7.5,2.8);
		map[6].fpCoordinate[47] = new Coordinate(6,2);
		map[6].fpCoordinate[48] = new Coordinate(5,2.3);
		map[6].fpCoordinate[49] = new Coordinate(4,2.3);
		map[6].fpCoordinate[50] = new Coordinate(3.2,1.8);
		map[6].fpCoordinate[51] = new Coordinate(-0.3,1.5);
		map[6].fpCoordinate[52] = new Coordinate(4.5,1.8);
		map[6].fpCoordinate[53] = new Coordinate(4.8,1.2);
		map[6].fpCoordinate[54] = new Coordinate(3.9,1);
		map[6].fpCoordinate[55] = new Coordinate(5.8,1);
		map[6].fpCoordinate[64] = new Coordinate(17,1.2);
		map[6].fpCoordinate[82] = new Coordinate(30,1.4);
		map[6].fpCoordinate[87] = new Coordinate(33.2,0.5);
		map[6].fpCoordinate[99] = new Coordinate(26.2,0);
		map[6].fpCoordinate[103] = new Coordinate(24,0);
		map[6].fpCoordinate[109] = new Coordinate(20,0);
		map[6].fpCoordinate[129] = new Coordinate(15.5,-1.5);//(14,0) -> (15.5,-1.5)
		map[6].fpCoordinate[136] = new Coordinate(10,0);
		map[6].fpCoordinate[139] = new Coordinate(8.3,0.5);
		map[6].fpCoordinate[140] = new Coordinate(7.8,0);
		map[6].fpCoordinate[141] = new Coordinate(7,0.4);
		map[6].fpCoordinate[142] = new Coordinate(6.2,0);
		map[6].fpCoordinate[143] = new Coordinate(5.8,0.4);
		map[6].fpCoordinate[144] = new Coordinate(5,0);
		map[6].fpCoordinate[145] = new Coordinate(4.5,0.5);
		map[6].fpCoordinate[146] = new Coordinate(4,0);
		map[6].fpCoordinate[147] = new Coordinate(3.2,0.4);
		map[6].fpCoordinate[148] = new Coordinate(2.6,0);
		map[6].fpCoordinate[149] = new Coordinate(2,0.4);
		map[6].fpCoordinate[150] = new Coordinate(1.2,0);
		map[6].fpCoordinate[151] = new Coordinate(0.8,0.5);
		map[6].fpCoordinate[152] = new Coordinate(0,0.5);
		map[6].fpCoordinate[153] = new Coordinate(-1,-1);//(0,0) -> (-1,-1)
		map[6].fpCoordinate[154] = new Coordinate(7.5,-2);//(7.5,-1) -> (7.5,-2)
		map[6].fpCoordinate[155] = new Coordinate(6.5,-1);
		
		map[6].videoCoordinate[0][0] = new Coordinate(14.3,15.5);
		map[6].videoCoordinate[0][1] = new Coordinate(27.5,14.5);
		map[6].videoCoordinate[1][0] = new Coordinate(14.3,10);
		map[6].videoCoordinate[1][1] = new Coordinate(22,9.9);
		map[6].videoCoordinate[2][0] = new Coordinate(14.3,7.5);
		map[6].videoCoordinate[2][1] = new Coordinate(19.6,7.8);
		map[6].videoCoordinate[2][2] = new Coordinate(23.5,7.5);
		map[6].videoCoordinate[2][3] = new Coordinate(24.9,7.3);
		map[6].videoCoordinate[3][0] = new Coordinate(14.3,6.1);
		map[6].videoCoordinate[3][1] = new Coordinate(18.2,6.6);
		map[6].videoCoordinate[3][2] = new Coordinate(21.7,6.6);
		map[6].videoCoordinate[3][3] = new Coordinate(23.2,6.5);
		map[6].videoCoordinate[4][0] = new Coordinate(14.3,6.1);
		map[6].videoCoordinate[4][1] = new Coordinate(17.4,5.9);
		map[6].videoCoordinate[4][2] = new Coordinate(20.5,6);
		map[6].videoCoordinate[4][3] = new Coordinate(21.8,5.9);
		map[6].videoCoordinate[5][0] = new Coordinate(14.3,4.9);
		map[6].videoCoordinate[5][1] = new Coordinate(16.6,5.3);
		map[6].videoCoordinate[6][0] = new Coordinate(14.3,4.7);
		map[6].videoCoordinate[6][1] = new Coordinate(16.1,4.8);
		map[6].videoCoordinate[7][0] = new Coordinate(14.3,4.4);
		map[6].videoCoordinate[7][1] = new Coordinate(15.9,4.6);
		//transfer the unit from cm to pixel
		for (int i = 0; i < map[6].numberOfPhysicalVideoX; i++) {
			for (int j = 0; j < map[6].numberOfPhysicalVideoY; j++) {
				if (map[6].videoCoordinate[i][j] != null) {
					map[6].videoCoordinate[i][j].x = (map[6].videoCoordinate[i][j].x / map[6].videoDimensionInCM.x) * map[6].videoResolution_BoundingBox.x;
					map[6].videoCoordinate[i][j].y = (map[6].videoCoordinate[i][j].y / map[6].videoDimensionInCM.y) * map[6].videoResolution_BoundingBox.y;
				}
			}
		}
		//For level 4
		map[4].fpPointInThisMap = 620;
		map[4].goesUpPoint = new int[] {595,566};
		map[4].downLevel = 1;
		map[4].upLevel = 6;
		map[4].numberOfPhysicalVideoX = 9;
		map[4].numberOfPhysicalVideoY = 4;
		map[4].videoCoordinate = new Coordinate[map[4].numberOfPhysicalVideoX][map[4].numberOfPhysicalVideoY];		
		map[4].fpCoordinate = new Coordinate[map[4].fpPointInThisMap + 1];
		map[4].numberOfAP = 21;
		for (int i = 0; i < map[4].fpPointInThisMap; i++) {
			map[4].fpCoordinate[i] = new Coordinate(Config.noPoint.locationCoor.x, Config.noPoint.locationCoor.y);
		}
		map[4].apLocationAtFP = new APLocation[] {new APLocation("156.28.18.192.33.46", 4, 544),
				new APLocation("156.28.18.192.33.44", 4, 551),
				new APLocation("156.28.18.192.32.242", 4, 560),
				new APLocation("156.28.18.192.33.82", 4, 562),
				new APLocation("156.28.18.192.33.18", 4, 572),
				new APLocation("108.243.127.198.211.46", 4, 570),
				new APLocation("108.243.127.198.205.4", 4, 580),
				new APLocation("108.243.127.198.213.242", 4, 591),
				//new APLocation("108.243.127.198.206.72", 4, 620),//toilet
				//new APLocation("108.243.127.198.201.36", 4, 602),//toilet
				new APLocation("108.243.127.198.205.206", 4, 598),//599 -> 598
				new APLocation("108.243.127.198.205.68", 4, 612),//614 --> 612
				new APLocation("108.243.127.198.209.4", 4, 618),//617 --> 618
				//new APLocation("156.28.18.192.33.18", 4, 549),//corner
				//new APLocation("156.28.18.192.33.18", 4, 550),//corner
				new APLocation("108.243.127.198.204.76", 4, 000),
				new APLocation("108.243.127.198.160.200", 4, 000),
				new APLocation("156.28.18.192.35.56", 4, 000),
				new APLocation("156.28.18.192.34.204", 4, 000),
				new APLocation("156.28.18.192.34.152", 4, 000),
				new APLocation("156.28.18.192.34.252", 4, 000),
				new APLocation("156.28.18.192.35.12", 4, 000),
				new APLocation("108.243.127.205.106.240", 4, 000),
				new APLocation("108.243.127.205.109.206", 4, 000),
				new APLocation("108.243.127.205.108.98", 4, 000)};

		map[4].fpCoordinate[549] = new Coordinate(6.8,3); //add new fp
		map[4].fpCoordinate[550] = new Coordinate(3,3);//add new fp
		map[4].fpCoordinate[551] = new Coordinate(-24,2);
		map[4].fpCoordinate[560] = new Coordinate(-20,0.3);
		map[4].fpCoordinate[562] = new Coordinate(-18.5,2.5);
		map[4].fpCoordinate[566] = new Coordinate(-16,0);
		map[4].fpCoordinate[570] = new Coordinate(-13,0);
		map[4].fpCoordinate[572] = new Coordinate(-15,3.1);
		map[4].fpCoordinate[580] = new Coordinate(-9.5,2);
		map[4].fpCoordinate[591] = new Coordinate(-7.5,-1);//(-6.5,0) -> (-7.5,-1)
		map[4].fpCoordinate[595] = new Coordinate(-3,0);
		map[4].fpCoordinate[597] = new Coordinate(-1,0);
		map[4].fpCoordinate[598] = new Coordinate(1,-2); //(1,-1) -> (1,-2)
		map[4].fpCoordinate[599] = new Coordinate(0.6,1);
		map[4].fpCoordinate[600] = new Coordinate(1.7,0);
		map[4].fpCoordinate[601] = new Coordinate(-1,1.7);
		map[4].fpCoordinate[602] = new Coordinate(-1.5,2.5);
		map[4].fpCoordinate[603] = new Coordinate(3,0.6);
		map[4].fpCoordinate[604] = new Coordinate(4.5,0);
		map[4].fpCoordinate[605] = new Coordinate(6.1,0.5);
		map[4].fpCoordinate[606] = new Coordinate(6.2,-1);
		map[4].fpCoordinate[607] = new Coordinate(7.4,0);
		map[4].fpCoordinate[608] = new Coordinate(5.9,1.1);
		map[4].fpCoordinate[609] = new Coordinate(4,1.1);
		map[4].fpCoordinate[610] = new Coordinate(3.3,2);
		map[4].fpCoordinate[611] = new Coordinate(3,3);
		map[4].fpCoordinate[612] = new Coordinate(4.5,4.5);//(4.5,3.5) -> (4.5,4.5)
		map[4].fpCoordinate[613] = new Coordinate(5.5,1.6);
		map[4].fpCoordinate[614] = new Coordinate(5.5,2.5);
		map[4].fpCoordinate[615] = new Coordinate(6.4,3);
		map[4].fpCoordinate[616] = new Coordinate(6.5,2);
		map[4].fpCoordinate[617] = new Coordinate(8.6,0.5);
		map[4].fpCoordinate[618] = new Coordinate(10,-1); //(9,0) -> (10,-1)
		map[4].fpCoordinate[619] = new Coordinate(10,0.5);
		map[4].fpCoordinate[620] = new Coordinate(-4,3);
		
		map[4].videoCoordinate[0][0] = new Coordinate(14.5,15.8);
		map[4].videoCoordinate[0][1] = new Coordinate(3.4,15.3);
		map[4].videoCoordinate[1][0] = new Coordinate(14.5,13.6);
		map[4].videoCoordinate[1][1] = new Coordinate(5.8,13.3);
		map[4].videoCoordinate[2][0] = new Coordinate(14.5,11.9);
		map[4].videoCoordinate[2][1] = new Coordinate(8,11.6);
		map[4].videoCoordinate[2][2] = new Coordinate(4.6,11);
		map[4].videoCoordinate[3][0] = new Coordinate(14.5,10.6);
		map[4].videoCoordinate[3][1] = new Coordinate(9.4,10.5);
		map[4].videoCoordinate[3][2] = new Coordinate(6.3,10);
		map[4].videoCoordinate[3][3] = new Coordinate(4.2,9.5);
		map[4].videoCoordinate[4][0] = new Coordinate(14.5,9.8);
		map[4].videoCoordinate[4][1] = new Coordinate(10.4,9.7);
		map[4].videoCoordinate[4][2] = new Coordinate(7.4,9.3);
		map[4].videoCoordinate[4][3] = new Coordinate(5.3,9);
		map[4].videoCoordinate[5][0] = new Coordinate(14.5,9);
		map[4].videoCoordinate[5][1] = new Coordinate(11.1,9);
		map[4].videoCoordinate[5][2] = new Coordinate(8.3,8.9);
		map[4].videoCoordinate[5][3] = new Coordinate(6,8.6);
		map[4].videoCoordinate[6][0] = new Coordinate(14.5,8.5);
		map[4].videoCoordinate[6][1] = new Coordinate(11.8,8.6);
		map[4].videoCoordinate[6][2] = new Coordinate(9,8.5);
		map[4].videoCoordinate[6][3] = new Coordinate(7,8.3);
		map[4].videoCoordinate[7][0] = new Coordinate(14.5,8.2);
		map[4].videoCoordinate[7][1] = new Coordinate(12.1,8.3);
		map[4].videoCoordinate[7][2] = new Coordinate(9.5,8.2);
		map[4].videoCoordinate[7][3] = new Coordinate(7.8,8);
		map[4].videoCoordinate[8][0] = new Coordinate(14.5,7.9);
		map[4].videoCoordinate[8][1] = new Coordinate(12.6,7.9);
		
		for (int i = 0; i < map[4].numberOfPhysicalVideoX; i++) {
			for (int j = 0; j < map[4].numberOfPhysicalVideoY; j++) {
				if (map[4].videoCoordinate[i][j] != null) {
					map[4].videoCoordinate[i][j].x = (map[4].videoCoordinate[i][j].x / map[4].videoDimensionInCM.x) * map[4].videoResolution_BoundingBox.x;
					map[4].videoCoordinate[i][j].y = (map[4].videoCoordinate[i][j].y / map[4].videoDimensionInCM.y) * map[4].videoResolution_BoundingBox.y;
				}
			}
		}	

		
		
		
	}
}

