package SuntecRealData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class RTLSDataAnalyze {
	static String wifiFolder = "src/SuntecRealData/RTLS_Comex2017_Sep_01/";
	static String fileWifiInput = "WifiFrom19_28_0 to 20_0_0.csv";
	static String fileOutput = "output/Interpolate.csv";
    static int startingHour;static int startingMinute;static int startingSecond;
    static int endingHour;static int endingMinute;static int endingSecond;
    static MapRealLevel146 map;
    static int experimentNumber;
	static int temp;
    
    //For data sumary;
    static double[] distributionAverageUpdate = new double[10];    
    static double countSatisfyBigger10Sample = 0;
    static double[] distributionNumberSample = new double[12];        
    static double totalNumberSample = 0;
    static double totalAverageUpdate = 0;
    

    
	public static int getTimeStep(String time, int startingHour, int startingMinute, int startingSecond) {
		int currentHour = Integer.parseInt(time.substring(0, 2));
		int currentMinute = Integer.parseInt(time.substring(3, 5));
		int currentSecond = Integer.parseInt(time.substring(6, 8));
		return (currentHour - startingHour) * 3600 + (currentMinute - startingMinute) * 60 + (currentSecond - startingSecond);
		
	}
    
    
    public static double distanceSameLevel(Coordinate loc1, Coordinate loc2) {
    	return Math.sqrt(Math.pow(loc1.x - loc2.x, 2) + Math.pow(loc1.y - loc2.y, 2));
    }
    
    public static double distanceMultiLevel(int startLoc, int endLoc) {
    	int currentLevel = startLoc / 1000;
    	int currentLocation = startLoc % 1000;
    	int endLevel = endLoc / 1000;
    	int endLocation = endLoc % 1000;
    	double distance = 0;
    	if (currentLevel < endLevel) {//go up
    		//move to the point to go up;
    		do {
	    		distance = distance + distanceSameLevel(map.multiLevelFPCoordinate[currentLevel][currentLocation],map.multiLevelFPCoordinate[currentLevel][map.goUpPoint[currentLevel]]);
	    		//go upstair
	    		distance = distance + map.distanceGoStair[currentLevel];
	    		currentLevel = map.getUpLevel(currentLevel);
	    		currentLocation = map.goUpPoint[currentLevel];
    		}
    		while (currentLevel < endLevel);
    		//move to end Location
    		distance = distance + distanceSameLevel(map.multiLevelFPCoordinate[currentLevel][currentLocation],map.multiLevelFPCoordinate[currentLevel][endLocation]);
    	}
    	else {
    		if (currentLevel > endLevel) {//go down
        		do {
    	    		distance = distance + distanceSameLevel(map.multiLevelFPCoordinate[currentLevel][currentLocation],map.multiLevelFPCoordinate[currentLevel][map.goDownPoint[currentLevel]]);
    	    		//go downstair
    	    		distance = distance + map.distanceGoStair[currentLevel];
    	    		currentLevel = map.getDownLevel(currentLevel);
    	    		currentLocation = map.goDownPoint[currentLevel];
        		}
        		while (currentLevel > endLevel);
        		//move to end Location
        		distance = distance + distanceSameLevel(map.multiLevelFPCoordinate[currentLevel][currentLocation],map.multiLevelFPCoordinate[currentLevel][endLocation]);
    			
    		}
    		else { 
    			distance = distance + distanceSameLevel(map.multiLevelFPCoordinate[currentLevel][currentLocation],map.multiLevelFPCoordinate[currentLevel][endLocation]);
    		}
    	}
    	return distance;
    }
    
    public static int getLocation(int currentLevel, int currentLocation) {
    	return currentLevel * 1000 + currentLocation;
    }
    
    public static double getModulo(Coordinate vector) {
    	return Math.sqrt(vector.x * vector.x + vector.y * vector.y);
    }
    
    public static int getNearestFP(int currentLevel, Coordinate location) {
    	double minDistance = Integer.MAX_VALUE;
    	int result = -1;
    	for (int i = 0; i < map.fpPointAtEachLevel[currentLevel]; i++) {
    		if (distanceSameLevel(location, map.multiLevelFPCoordinate[currentLevel][i]) < minDistance) {
    			minDistance = distanceSameLevel(location, map.multiLevelFPCoordinate[currentLevel][i]);
    			result = i;
    		}
    	}
    	return result;
    }
    
    public static double[] getNextLocation(int currentLevel, int currentLocation, double distanceAtChangeLevelPoint, int endLevel, int endLocation, double distanceMove) {
    	double[] result = new double[3]; 
    	Coordinate directionMove = new Coordinate(0,0);
    	Coordinate targetPoint = new Coordinate(0,0);
    	if (currentLevel == endLevel) {// same level, just move toward the target point
    		targetPoint = new Coordinate(map.multiLevelFPCoordinate[endLevel][endLocation].x, map.multiLevelFPCoordinate[endLevel][endLocation].y);
    		directionMove = new Coordinate(targetPoint.x - map.multiLevelFPCoordinate[currentLevel][currentLocation].x,
    									   targetPoint.y - map.multiLevelFPCoordinate[currentLevel][currentLocation].y);
    		Coordinate vectorMove;
    		if (getModulo(directionMove) != 0) {
    			vectorMove = new Coordinate(directionMove.x * distanceMove / getModulo(directionMove),directionMove.y * distanceMove / getModulo(directionMove));
    		}
    		else {
    			vectorMove = new Coordinate(0,0);
    		}
    		currentLocation = getNearestFP(currentLevel,new Coordinate(map.multiLevelFPCoordinate[currentLevel][currentLocation].x + vectorMove.x,
    																   map.multiLevelFPCoordinate[currentLevel][currentLocation].y + vectorMove.y));
    	}
    	else {
    		if (currentLevel < endLevel) {
    			while (distanceMove > 0) {
    				if (currentLocation == map.goUpPoint[currentLevel]) {// already stay at the stair point
    					if (distanceMove - (map.distanceGoStair[currentLevel] - distanceAtChangeLevelPoint) > 0){//enough to go up stair
    						distanceMove = distanceMove - (map.distanceGoStair[currentLevel] - distanceAtChangeLevelPoint);
        					currentLevel = map.getUpLevel(currentLevel);
        					currentLocation = map.goUpPoint[currentLevel];
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
    	    			targetPoint = new Coordinate(map.multiLevelFPCoordinate[currentLevel][map.goUpPoint[currentLevel]].x, map.multiLevelFPCoordinate[currentLevel][map.goUpPoint[currentLevel]].y);
    	        		directionMove = new Coordinate(targetPoint.x - map.multiLevelFPCoordinate[currentLevel][currentLocation].x,
    							   					   targetPoint.y - map.multiLevelFPCoordinate[currentLevel][currentLocation].y);
    	        		if (distanceMove < getModulo(directionMove)) {//Move toward the point to go up
    		        		Coordinate vectorMove = new Coordinate(directionMove.x * distanceMove / getModulo(directionMove),directionMove.y * distanceMove / getModulo(directionMove));
    		        		currentLocation = getNearestFP(currentLevel,new Coordinate(map.multiLevelFPCoordinate[currentLevel][currentLocation].x + vectorMove.x,
    															   map.multiLevelFPCoordinate[currentLevel][currentLocation].y + vectorMove.y));
    		        		distanceMove = 0;
    	        		}
    	        		else {
    	        			currentLocation = map.goUpPoint[currentLevel];
    	        			distanceMove = distanceMove - getModulo(directionMove);
    	        		}

    				}
    			}
    		}
    		else {
    			while (distanceMove > 0) {
    				if (currentLocation == map.goDownPoint[currentLevel]) {// already stay at the stair point
    					if (distanceMove - (map.distanceGoStair[currentLevel] - distanceAtChangeLevelPoint) > 0){//enough to go down stair
    						distanceMove = distanceMove - (map.distanceGoStair[currentLevel] - distanceAtChangeLevelPoint);
        					currentLevel = map.getDownLevel(currentLevel);
        					currentLocation = map.goDownPoint[currentLevel];
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
    	    			targetPoint = new Coordinate(map.multiLevelFPCoordinate[currentLevel][map.goDownPoint[currentLevel]].x, map.multiLevelFPCoordinate[currentLevel][map.goDownPoint[currentLevel]].y);
    	        		directionMove = new Coordinate(targetPoint.x - map.multiLevelFPCoordinate[currentLevel][currentLocation].x,
    							   					   targetPoint.y - map.multiLevelFPCoordinate[currentLevel][currentLocation].y);
    	        		if (distanceMove < getModulo(directionMove)) {//Move toward the point to go down
    		        		Coordinate vectorMove = new Coordinate(directionMove.x * distanceMove / getModulo(directionMove),directionMove.y * distanceMove / getModulo(directionMove));
    		        		currentLocation = getNearestFP(currentLevel,new Coordinate(map.multiLevelFPCoordinate[currentLevel][currentLocation].x + vectorMove.x,
    															   map.multiLevelFPCoordinate[currentLevel][currentLocation].y + vectorMove.y));
    		        		distanceMove = 0;
    	        		}
    	        		else {
    	        			currentLocation = map.goDownPoint[currentLevel];
    	        			distanceMove = distanceMove - getModulo(directionMove);
    	        		}
    				}
    			}
    		}
    	}
    	result[0] = currentLevel; result[1] = currentLocation; result[2] = distanceAtChangeLevelPoint;
    	return result;
    }
    
    public static int[] fillInterpolateData(int[] wifiLocation, int startTime, int endTime) {
    	double distance = distanceMultiLevel(wifiLocation[startTime], wifiLocation[endTime]);
    	double distanceMoveEachStep = distance / (endTime - startTime);
    	int currentLevel = wifiLocation[startTime] / 1000;
    	int currentLocation = wifiLocation[startTime] % 1000;
    	double distanceAtChangeLevelPoint = 0;
    	int endLevel = wifiLocation[endTime] / 1000;
    	int endLocation = wifiLocation[endTime] % 1000;
    	int[] result = new int[endTime - startTime + 1];
    	result[0] = getLocation(currentLevel, currentLocation);
    	for (int i = 1; i < endTime - startTime + 1; i++) {
    		double[] newLoc = getNextLocation(currentLevel, currentLocation, distanceAtChangeLevelPoint, endLevel, endLocation, distanceMoveEachStep);
    		currentLevel = (int)newLoc[0];
    		currentLocation = (int)newLoc[1];
    		distanceAtChangeLevelPoint = newLoc[2];
    		result[i] = getLocation(currentLevel, currentLocation);
    	}
    	return result;
    }
    
    public static void interpolateData(BufferedWriter writer, int ID, int[] wifiLocation) {
    	int start0 = 0;
    	int start = 0;
    	int end = 0;
    	for (int i = 0; i < experimentNumber; i++) {
    		if (wifiLocation[i] != 0) {
    			start0 = i;
    			start = i;
    			break;
    		}
    	}
    	for (int i = start0 + 1; i < experimentNumber; i++) {
    		if (wifiLocation[i] != 0) {
    			end = i;
    			int[] result = fillInterpolateData(wifiLocation, start, end);
    			for (int k = 0; k < result.length; k++) {
    				wifiLocation[start + k] = result[k];
    			}
    			start = end;
    		}
    	}
    }
    
    public static double getAverageUpdate(String ID, int[] timeReport, int countTimeReport) {
    	int count = 0;
    	int sum = 0;
    	double result = 0;
    	for (int i = 1; i < countTimeReport; i++) {
    		int time = timeReport[i] - timeReport[i - 1];
    		if (time < 300) { //only consider bigger than 300 seconds
    			sum += time;
    			count++;
    		}
    	}
    	if (count > 0) {
    		result = sum * 1.0 / count;
    	}
    	return result;
    }
    
    
    public static boolean containLevel1(int[] wifiLocation) {
    	for (int i = 0; i < experimentNumber; i++) {
    		if (wifiLocation[i] / 1000 == 1) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public static int getDistrubuteGroup(double averageUpdate) {
    	int[] distributeBoundary = new int[] {5,10,15,20,30,40,50,70,100};
    	for (int i = 0; i < 9; i++) {
    		if (distributeBoundary[i] >= averageUpdate) {
    			return i;
    		}
    	}
    	return distributeBoundary.length;
    }
    
    public static int getNumberSampleGroup(int numberOfSample) {
    	int[] distributeBoundary = new int[] {5,10,20,40,80,120,150,200,250};
    	for (int i = 0; i < 9; i++) {
    		if (distributeBoundary[i] >= numberOfSample) {
    			return i;
    		}
    	}
    	return distributeBoundary.length;    	
    }
    
    public static void calculateAverageUpdate(String ID, int[] timeReport, int countTimeReport) {
    	//if (containLevel1(wifiLocation)){
    		//temp++;
			double averageUpdate = getAverageUpdate(ID, timeReport, countTimeReport);
			int numberOfSample = countTimeReport;
			if (averageUpdate > 0) { // only care about the MAC that has more than 5 samples, issued caused by Mac Randomize
				countSatisfyBigger10Sample++;
				distributionNumberSample[getNumberSampleGroup(numberOfSample)]++;				
				distributionAverageUpdate[getDistrubuteGroup(averageUpdate)]++;
				totalAverageUpdate += averageUpdate;
				totalNumberSample += numberOfSample;
			}
    	//}
    }
	public static void main(String args[]) {	
	    Scanner scan;	    
	    BufferedWriter writer;
	    ParserWifiRTLS parser1;
	    ParserWifiRTLS parser2;
	    int[] timeReport;
	    int countUnAssociate = 0;
	    int countAssociate = 0;
	    try {
	    	File file = new File(wifiFolder + fileWifiInput);
	    	//writer = new BufferedWriter(new FileWriter(wifiFolder + fileOutput));
	        scan = new Scanner(file);
	        //scan.nextLine();
	        String data1 = scan.nextLine();
	        String data2 = "";
	        while (scan.hasNextLine()) {
	        	// Do for each person which has been sorted by ID
	        	timeReport = new int[10000];
	        	parser1 = new ParserWifiRTLS(data1);
	        	int time = Integer.parseInt(parser1.getAttribute(1));
	        	timeReport[0] = time;
	        	int countTimeReport = 1;
	        	while (true)
	        	{	        
	        		if (!scan.hasNextLine()) {
	        			break;
	        		}
	        		data2 = scan.nextLine();
	        		parser2 = new ParserWifiRTLS(data2);	        		
	        		if (!parser1.getIDstr().equals(parser2.getIDstr())){
	        			break;
	        		}
	        		time = Integer.parseInt(parser2.getAttribute(1));
	        		if (time != timeReport[countTimeReport - 1]) {	        			
	        			timeReport[countTimeReport] = Integer.parseInt(parser2.getAttribute(1));
	        			countTimeReport++;
	        		}
	        	}
	        	data1 = data2;
	        	calculateAverageUpdate(parser1.getIDstr(),timeReport, countTimeReport);
	        	//get average update time of a MAC
	        	/*if (parser1.getAssociateStatus() == 2) { //only care the associate one
	        		calculateAverageUpdate(parser1.getIDstr(),timeReport, countTimeReport);
	        		countAssociate++;
	        	}
	        	else {
	        		countUnAssociate++;
	        	}*/
	        	//interpolate data
	        	//interpolateData(writer, ID, wifiLocation);	        		        		        	
	        	
	        }
	        scan.close();
	        System.out.println("Number Of Associate Sample: " + countAssociate);
	        System.out.println("Number Of Unassociate Sample: " + countUnAssociate);
	        System.out.println("Average Number Of Sample: " + (totalNumberSample * 1.0 / countSatisfyBigger10Sample));
	        for (int i = 0; i < 10; i++) {
	        	System.out.println(distributionNumberSample[i] * 100.0 / countSatisfyBigger10Sample);
	        }
	        System.out.println("--------------------------");
	        System.out.println("Average Update of all MAC: " + (totalAverageUpdate / countSatisfyBigger10Sample));
	        for (int i = 0; i < 10; i++) {
	        	System.out.println(distributionAverageUpdate[i] * 100.0 / countSatisfyBigger10Sample);
	        }
	        System.out.println("Number at level 1: " + temp);
	    } 
	    catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }
	    catch (IOException e) {
			e.printStackTrace();
		}
	}
}
