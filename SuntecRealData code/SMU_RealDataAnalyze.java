package SuntecRealData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class SMU_RealDataAnalyze {
	static String wifiFolder = "src/SuntecRealData/SMU_16_Apr/";
	static String fileWifiInput = "data_SMU_16_Apr_sort.csv";
	static String fileOutput = "output/Interpolate.csv";
    static int startingHour;static int startingMinute;static int startingSecond;
    static int endingHour;static int endingMinute;static int endingSecond;
    static MapRealLevel146 map;
    static int experimentNumber;
	static int temp;
    
    //For data sumary;
    static double[] distributionAverageUpdate = new double[10];    
    static double countDistributeAverageUpdate = 0;
    static double[] distributionNumberSample = new double[12];    
    static double countDistributeNumberSample = 0;
    static double totalNumberSample = 0;
    static double totalAverageUpdate = 0;
    static double countAverageUpdate = 0;

    
	public static int getTimeStep(String time, int startingHour, int startingMinute, int startingSecond) {
		int currentHour = Integer.parseInt(time.substring(0, 2));
		int currentMinute = Integer.parseInt(time.substring(3, 5));
		int currentSecond = Integer.parseInt(time.substring(6, 8));
		return (currentHour - startingHour) * 3600 + (currentMinute - startingMinute) * 60 + (currentSecond - startingSecond);
		
	}
    
    public static void initialize() {
    	map = new MapRealLevel146();
    	startingHour = 18; startingMinute = 11; startingSecond = 10;
    	endingHour = 23; endingMinute = 59; endingSecond = 0;
    	experimentNumber = ((endingHour - startingHour) * 3600 + (endingMinute - startingMinute) * 60 + (endingSecond - startingSecond)) / 5 + 1;
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
    
    public static double[] getAverageUpdate(String ID, int wifiLocation[]) {
    	int start0 = 0;
    	int start = 0;
    	double result[] = new double[2]; //0: average, 1:count
    	int countAvr = 0;
    	int countSample = 0;
    	for (int i = 0; i < experimentNumber; i++) {
    		if (wifiLocation[i] != 0) {
    			start0 = i;
    			start = i;
    			countSample = 1;
    			break;
    		}
    	}
    	for (int i = start0 + 1; i < experimentNumber; i++) {
    		if (wifiLocation[i] != 0) {
    			//if (i - start < 50) {
    				//System.out.println(ID);
    				result[0] += i - start;
    				countAvr++;
    				countSample++;
    			//}
    			start = i;
    		}
    	}
    	if (countAvr != 0) {
    		result[0] = result[0] / countAvr;
    	}
    	else {
    		result[0] = 0;
    	}
    	result[1] = countSample;
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
    	int[] distributeBoundary = new int[] {0,2,5,8,10,20,30,50,80};
    	for (int i = 0; i < 9; i++) {
    		if (distributeBoundary[i] >= averageUpdate) {
    			return i;
    		}
    	}
    	return 9;
    }
    
    public static int getNumberSampleGroup(int numberOfSample) {
    	int[] distributeBoundary = new int[] {1,2,3,4,5,8,10,15,20,40,60};
    	for (int i = 0; i < 11; i++) {
    		if (distributeBoundary[i] >= numberOfSample) {
    			return i;
    		}
    	}
    	return 11;    	
    }
    
    public static void calculateAverageUpdate(String ID, int[] wifiLocation) {
    	//if (containLevel1(wifiLocation)){
    		//temp++;
			double averageUpdate = getAverageUpdate(ID, wifiLocation)[0];
			int numberOfSample = (int)getAverageUpdate(ID, wifiLocation)[1];
			distributionNumberSample[getNumberSampleGroup(numberOfSample)]++;
			countDistributeNumberSample++;
			totalNumberSample += numberOfSample;
			distributionAverageUpdate[getDistrubuteGroup(averageUpdate)]++;
			countDistributeAverageUpdate++;
			if (averageUpdate != 0) {				
				totalAverageUpdate += averageUpdate;
				countAverageUpdate++;
			}
    	//}
    }
	public static void main(String args[]) {
		initialize();		
	    Scanner scan;	    
	    BufferedWriter writer;
	    ParserWifi parser1;
	    ParserWifi parser2;
	    int ID = 0;
	    try {
	    	File file = new File(wifiFolder + fileWifiInput);
	    	writer = new BufferedWriter(new FileWriter(wifiFolder + fileOutput));
	        scan = new Scanner(file);
	        String data1 = scan.nextLine();
	        String data2 = "";
	        while (scan.hasNextLine()) {
	        	// Do for each person which has been sorted by ID
	        	ID++;
	        	parser1 = new ParserWifi(data1);
	        	String time = parser1.getTime();
	        	int location = parser1.getLocation();
	        	int level = parser1.getLocationLevel();
	        	int timeStep = getTimeStep(time, startingHour, startingMinute, startingSecond) / 5; //each time step is 5 seconds;
	        	int[] wifiLocation = new int[10000];	        	
	        	wifiLocation[timeStep] = level * 1000 + location;
	        	while (true)
	        	{	        
	        		if (!scan.hasNextLine()) {
	        			break;
	        		}
	        		data2 = scan.nextLine();
	        		parser2 = new ParserWifi(data2);
	        		if (!parser1.getIDstr().equals(parser2.getIDstr())){
	        			break;
	        		}
		        	time = parser2.getTime();
		        	location = parser2.getLocation();
		        	level = parser2.getLocationLevel();
		        	timeStep = getTimeStep(time, startingHour, startingMinute, startingSecond) / 5; //each time step is 5 seconds;
		        	wifiLocation[timeStep] = level * 1000 + location;
	        	}
	        	data1 = data2;
	        	//get average update time of a MAC
	        	calculateAverageUpdate(parser1.getIDstr(),wifiLocation);
	        	//interpolate data
	        	//interpolateData(writer, ID, wifiLocation);	        		        		        	
	        	
	        }
	        scan.close();
	        System.out.println("Number Of Sample");
	        System.out.println("Average Number Of Sample: " + (totalNumberSample * 1.0 / countDistributeNumberSample));
	        for (int i = 0; i < 12; i++) {
	        	System.out.println(distributionNumberSample[i] * 100.0 / countDistributeNumberSample);
	        }
	        System.out.println("--------------------------");
	        System.out.println("Average Update of all MAC: " + (totalAverageUpdate / countAverageUpdate));
	        for (int i = 0; i < 10; i++) {
	        	System.out.println(distributionAverageUpdate[i] * 100.0 / countDistributeAverageUpdate);
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
