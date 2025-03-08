package SuntecRealData;

import java.io.BufferedWriter;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Scanner;


public class Suntec_Level1_Calibration {
	//Setting for this file input
    static int startingHour;static int startingMinute;static int startingSecond;
    static int endingHour;static int endingMinute;static int endingSecond;
    static int experimentNumber = 0; // Experiment length from the SMU video dataset
	
	
	//For reading video input file
    static String videoWifiFolder = "src/SuntecRealData/Suntec_Video_Wifi_data/Study3Comex2017_Friday_01Sep/";
    static String fileVideoCalibration = "output_Level4_19h34_Dense0_thres02_YN270002/results.txt";
	static String fileVideoInput = "output_Level4_19h34_Dense0_thres02_YN270002/results.txt"; // File location of the SMU video
	static String fileWifiInput = "location_archival_rtls_2017_9_1.csv";
	static String fileOutputFolder = "output/";
	static int frameNumber = 0; // Number of frames in the SMU video dataset
	static int blobNumberGen = 0;
	static BoundingBoxImageCoor[][] blobImageCoor = new BoundingBoxImageCoor[Config.maximumBlobCount][Config.maximumFrameCount]; // Coordinate of all the bounding box in the SMU video
	static BoundingBoxImageCoor[][] blobImageCalibration = new BoundingBoxImageCoor[Config.maximumBlobCount][Config.maximumFrameCount]; // Coordinate of all the bounding box in the SMU video
	static int[][] peopleVideo;
	static Coordinate[][] peopleVideoCoor;
	static int[][] peopleWifi;
	static int[][] peopleWifiWithMissingData;
	static double[] percentOfMissingWifiData;
	static Coordinate[][] peopleWifiCoor;
	static boolean[] blobVideoAppear;
	static int blobVideoNumber = 0;  // Number of blobs in the SMU video dataset
	static int blobWifiNumber = 0;
	
	
	
	
	
	static DecimalFormat df2 = new DecimalFormat("#.##");
	
	static String fileSimulatedDataOutput;
	static String folderSimulatedDataOutput = "SMU_Synthetic/SimulatedData/";								
	static double[] calibrateY = new double[608];
	static int[] calibrateID = new int[] {1,104,218}; // The ID that used to move around for calibration
	
	static Coordinate[][] blobPhysicalCoor = new Coordinate[Config.maximumBlobCount][Config.maximumFrameCount];
			
	//For map generation
	static String parametersConfig;
	static int mapWidth; // The width of the area, determined by people location
	static int mapHeight;// The height of the area, determined by people location
	static MapRealLevel1 map;
	static String configContent;

	static String configAlgorithmContent = "";
	static boolean[] foundConvergeWifi;
	static boolean[] foundConvergeVideo;
	static int[] convergeLatencyForID;
	static int[] convergeVideoFinalForID;
	static int rankMatch[][];
	static double scoreMatch[][][]; //value from 1 to 9999 represents for 0.0001 to 0.9999
	static int countScoreMatch[][];
	static int timeStepRankMatch[][];
	static int timeStepFirstAppear[];
	static boolean stopLoopingAvoidOverlap;
	//static long[] BlobMovementlength;
	static int[] distributionOfTheError;
	static int numberCaseSolve = 0;
	public static Coordinate disappearWifi = new Coordinate(100000, 100000);
	

	static int countPercenTileCase = 0;
	
	
	
	static double percentile95 = 0;
	static double percentile90 = 0;
	static double percentile85 = 0;
	static int percentileTime = 0;
	
	//-----------------------------------Convert to physical coordinate----------
	static void readVideoFile() {		
		Scanner scan;
		ParserVideo parser;
	    try {
	    	File file = new File(fileVideoInput);
	        scan = new Scanner(file);
	        scan.nextLine();
	        String data;
	        while (scan.hasNextLine()) {
	        	data = scan.nextLine();
	        	parser = new ParserVideo(data);		
	        	if (parser.getFrameID() > frameNumber) {
	        		frameNumber = parser.getFrameID();
	        	}
	        	if (parser.getBlobID() > blobNumberGen) {
	        		blobNumberGen = parser.getBlobID();
	        	}
	        	blobImageCoor[parser.getBlobID()][parser.getFrameID()] = new BoundingBoxImageCoor(parser.getX(), parser.getY(), parser.getW(), parser.getH());
	        	
	        }
	       
   		scan.close();
   		peopleVideo = new int[blobNumberGen][experimentNumber];
   		blobVideoAppear = new boolean[blobNumberGen];
   		for (int i = 0; i < blobNumberGen; i++) {
   			blobVideoAppear[i] = false;
   		}
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }
	}

	static void readCalibrationFile() {		
		Scanner scan;
		ParserVideo parser;
	    try {
	    	File file = new File(fileVideoCalibration);
	        scan = new Scanner(file);
	        scan.nextLine();
	        String data;
	        while (scan.hasNextLine()) {
	        	data = scan.nextLine();
	        	parser = new ParserVideo(data);		
	        	blobImageCalibration[parser.getBlobID()][parser.getFrameID()] = new BoundingBoxImageCoor(parser.getX(), parser.getY(), parser.getW(), parser.getH());
	        	
	        }	       
   		scan.close();
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }
	}

	
	public static void getCalibrateInformation(int blobID) {
		//video resolution 1080 x 608
		//formula: imageHeight / focal = realHeight / distance
		double cameraHeight = 3; //meters
		double realHeight = 1.7; //meters
		double distance = 5.65; //meters
		double imageHeight = 143.1; //pixel
		double focalLength = imageHeight * distance / realHeight;
		for (int i = 0; i < Config.maximumFrameCount; i++) {
			if (blobImageCalibration[blobID][i] != null && (blobImageCalibration[blobID][i].y + blobImageCalibration[blobID][i].h) < 608 && (blobImageCalibration[blobID][i].x + blobImageCalibration[blobID][i].w) < 1080) {
				double distanceToCamera = focalLength * realHeight / blobImageCalibration[blobID][i].h;
				double distanceToCameraY = Math.sqrt(distanceToCamera * distanceToCamera - cameraHeight * cameraHeight);
				calibrateY[(int) (blobImageCalibration[blobID][i].y + blobImageCalibration[blobID][i].h)] = distanceToCameraY;
			}
 		}
	}
	
	public static Coordinate getPhysicalCoor(Coordinate foot, double aD, double bD, double aRightLine, double bRightLine) {
		Coordinate result = new Coordinate(0,0);
		result.y = calibrateY[(int)foot.y];
		double xPointOnRightLine = (foot.y - bRightLine) / aRightLine;
		double distance = foot.x - xPointOnRightLine;
		double pixelOfFixDistance = aD * foot.y + bD;
		double distancePhysicalMap = (distance / pixelOfFixDistance) * 5.6; //the width of path is 5.6 meters;
		result.x = -distancePhysicalMap;
		return result;
	}
	
	public static void convertToPhysicalCoordinate() {
		double width = 1080; double height = 608; // 1080 x 608 pixel ~ 24 x 13.5 
		double xBR = 12.8 / 24; double yBR = 12.1 / 13.5;
		double xBL = 1.3 / 24; double yBL = 11 / 13.5;
		double xTL = 11.2 / 24; double yTL = 4.8 / 13.5; 
		double xTR = 12.2 / 24; double yTR = 4.8 / 13.5;
		
		xBR = xBR * width; xBL = xBL * width; xTL = xTL * width; xTR = xTR * width;
		yBR = yBR * height; yBL = yBL * height; yTL = yTL * height; yTR = yTR * height;
		
		//linear regression y = ax + b for the distance
		double y1 = xBR - xBL; double y2 = xTR - xTL;
		double aD = (y1 - y2) / (yBR - yTR);
		double bD = y1 - aD * yBR;
		//line y = ax + b for the line (xBR, yBR, xTR, yTR)
		double aRightLine = (yTR - yBR) / (xTR - xBR);
		double bRightLine = yTR - aRightLine * xTR;
		
		mapWidth = 0;
		mapHeight = 0;
		for (int i = 0; i < frameNumber; i++) {
		//for (int i = 0; i <= 20000; i++) {	
			for (int j = 0; j < blobNumberGen; j++) {
				if (blobImageCoor[j][i] != null && (blobImageCoor[j][i].y + blobImageCoor[j][i].h) < 608 && (blobImageCoor[j][i].x + blobImageCoor[j][i].w) < 1080) {
					Coordinate footLocation = new Coordinate((int)(blobImageCoor[j][i].x + blobImageCoor[j][i].w / 2), (int)(blobImageCoor[j][i].y + blobImageCoor[j][i].h));
					blobPhysicalCoor[j][i] = getPhysicalCoor(footLocation, aD, bD, aRightLine, bRightLine);
					if (Math.abs(blobPhysicalCoor[j][i].x) > mapWidth) {
						mapWidth = (int)Math.abs(blobPhysicalCoor[j][i].x);
					}
					if (Math.abs(blobPhysicalCoor[j][i].y) > mapHeight) {
						mapHeight = (int)Math.abs(blobPhysicalCoor[j][i].y);
					}
				}
			}
		}
		mapWidth = (int)(mapWidth * 2); // Because the camera is put at the middle of the map
		mapHeight = (int)mapHeight;
	}
	
	public static Coordinate changeToGridCoordinate(Coordinate physicalCoor) {
		//31.2 meters are equal to 12 grids from practical area 
		Coordinate result = new Coordinate(physicalCoor.x * 12 / 31.2,physicalCoor.y * 12 / 31.2);		
		return result;
	}
	
	public static void generateVideoFromFile() {
		blobVideoNumber = 0;
		for (int blobID = 0; blobID < blobNumberGen; blobID++) {
			boolean[] gotSample = new boolean[Config.maximumFrameCount];
			for (int i = 0; i < experimentNumber; i++) {
				peopleVideo[blobID][i] = -1; 
				//peopleWifiFromFileInput[blobID][i] = -1; 
				gotSample[i] = false;
			}
			int timeStep = 0;
			for (int frameID = 0; frameID < frameNumber; frameID++) {
				timeStep = changeToTimeStep(frameID);
				if (timeStep != -1) {
					if (gotSample[timeStep] == false && blobPhysicalCoor[blobID][frameID] != null) {
						gotSample[timeStep] = true;
						blobVideoAppear[blobID] = true;
						Coordinate locationInCoordinate = changeToGridCoordinate(blobPhysicalCoor[blobID][frameID]);
						peopleVideoCoor[blobID][timeStep] = locationInCoordinate;
						peopleVideo[blobID][timeStep] = getNearestLandMarkInt(locationInCoordinate);
						
						//peopleWifiFromFileInput[blobID][timeStep] = generateGaussian(locationInNewCoordinate, findSD());					
					}
				}
			}							
			if (blobVideoAppear[blobID] == true) {
				blobVideoNumber++;
			}
			/*cut the too long movement; 
			for (int i = 0; i < experimentNumber; i++) {
				if (peopleVideo[blobID][i] != -1) {
					if (i + 2.5 * Config.historyWindowLength < experimentNumber) {//longer than 2.5 window length will be cut
						for (int j = (int)Math.floor(i + 2.5 * Config.historyWindowLength); j < experimentNumber; j++) {
							peopleVideo[blobID][j] = -1;
							//peopleWifiFromFileInput[blobID][j] = -1;
						}
					}
					break;
				}
			}*/
		}		
		blobVideoNumber = blobNumberGen;
		//Fill missing video with surrounding location: for video with format X -1 X --> -1 will be X
		for (int blobID = 0; blobID < blobNumberGen; blobID++) {
			if (blobID == 105) {
				blobID = 105;
			}
			for (int timestep = 0; timestep < experimentNumber; timestep++) {
				if (peopleVideo[blobID][timestep] == -1) {
					int fillingLocation = getSurroundingAppear(blobID, timestep);
					if (fillingLocation != -1) {//No filling found
						peopleVideo[blobID][timestep] = fillingLocation;
					}
				}
			}
		}
	}
	
	static int getSurroundingAppear (int blobID, int timestep) {
		if (timestep == 0 || timestep == experimentNumber -1) {
			return -1;
		}
		else {
			boolean leftSatisfy = false;
			for (int i = timestep -1; i >= 0; i--) {
				if (peopleVideo[blobID][i] != -1) {
					leftSatisfy = true;
					break;
				}
			}
			if (leftSatisfy) {
				for (int i = timestep + 1; i < experimentNumber; i++) {
					if (peopleVideo[blobID][i] != -1) {
						return peopleVideo[blobID][i];
					}
				}
				return -1;
			}
			else {
				return -1;
			}
		}
	}
	
	//---------------------Util------------------------------
	
	public static int changeToTimeStep(int frameID) {
		int startingVideoHour = 12;
		int startingVideoMinute = 04;
		int startingVideoSecond = 0;
		//450 because the clip start at 12:03:45, we want to start at 12:04:00
		int startingFrame = 450 + 30 * ((startingHour - startingVideoHour) * 3600 + (startingMinute - startingVideoMinute) * 60 + (startingSecond - startingVideoSecond));
		int endingFrame = 450 + 30 * ((endingHour - startingVideoHour) * 3600 + (endingMinute - startingVideoMinute) * 60 + (endingSecond - startingVideoSecond));
		if (frameID < startingFrame || frameID > endingFrame) {
			return -1;
		}
		else {
			return (frameID - startingFrame) / 150; // 30 fps for each 5 seconds timestep,
		}
	}
	
	static double distance(Coordinate point1, Coordinate point2) {
		return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
	}
	
	
	public static Coordinate getCoordinateTransfer(Coordinate input) {//(0,0) at the camera position
		Coordinate result = new Coordinate(0,0);
		result.x = mapWidth / 2 - input.x;
		result.y = mapHeight - input.y;
		//result.x = result.x * Config.pixelPerCell / Config.metersPerCell;
		//result.y = result.y * Config.pixelPerCell / Config.metersPerCell;
		return result;
	}
	
	public static int getNearestLandMarkInt(Coordinate location) {
		double minDistance = Integer.MAX_VALUE;
		int result = 0;
		for (int i = 0; i < map.pointInThisMap; i++) {
			if (map.landmarkType[i] == 1 || map.landmarkType[i] == 0) {
				if (distance(location, map.fpCoordinate[i]) < minDistance) {
					minDistance = distance(location, map.fpCoordinate[i]);
					result = i;
				}
			}
		}
		return result;
	}
	
	public static int getTimeStep(String time, int startingHour, int startingMinute, int startingSecond) {
		int currentHour = Integer.parseInt(time.substring(0, 2));
		int currentMinute = Integer.parseInt(time.substring(3, 5));
		int currentSecond = Integer.parseInt(time.substring(6, 8));
		return (currentHour - startingHour) * 3600 + (currentMinute - startingMinute) * 60 + (currentSecond - startingSecond);
		
	}
	
	static boolean surroundStillHaveWifiSignal(int indexWifi, int timestep) {
		boolean result = false;
		for (int i = timestep + 1; i < experimentNumber; i++) {
			if (peopleWifi[indexWifi][i] != -1) {
				for (int j = timestep -1; j > 0; j--) {
					if (peopleWifi[indexWifi][j] != -1) {
						result = true;
						break;
					}
				}
				break;
			}
		}
		return result;
	}
	
	static void readWifiData() {
	    Scanner scan;	    
	    ParserWifi parser;	
	    blobWifiNumber = 0;
	    try {
	    	File file = new File(fileWifiInput);
	        scan = new Scanner(file);
	        scan.nextLine();
	        while (scan.hasNextLine()) {
	        	String data = scan.nextLine();
	        	parser = new ParserWifi(data);
	        	String time = parser.getTime();
	        	int timeStep = getTimeStep(time, startingHour, startingMinute, startingSecond) / 5; //each time step is 5 seconds;
	        	int ID = parser.getID();
	        	if (ID > blobWifiNumber) {
	        		blobWifiNumber = ID;
	        	}
	        	if (timeStep >= experimentNumber) {
	        		break;
	        	}
	        	if (timeStep >= 0) {	        	
		        	if (parser.getLocation()  < map.pointInThisMap)  {
		        		peopleWifiCoor[ID][timeStep] = map.fpCoordinate[parser.getLocation()];
		        		peopleWifiWithMissingData[ID][timeStep] = parser.getLocation();
		        		peopleWifi[ID][timeStep] = parser.getLocation();
		        	}
	        	}
	        }
   		scan.close();
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }
	    percentOfMissingWifiData = new double[blobWifiNumber];
	    for (int indexWifi = 1; indexWifi < blobWifiNumber; indexWifi++) {
	    	if (indexWifi == 3) {
	    		indexWifi = 3;
	    	}
	    	int count = 0;
	    	for (int timestep = 0; timestep < experimentNumber; timestep++) {
	    		if (peopleWifi[indexWifi][timestep] == -1) {
	    			count++;
	    			if (surroundStillHaveWifiSignal(indexWifi, timestep)) {	    		
		    			for (int k = timestep - 1; k >= 0; k--) {
		    				if (peopleWifi[indexWifi][k] != -1) {
		    					peopleWifi[indexWifi][timestep] = peopleWifi[indexWifi][k];
		    					peopleWifiCoor[indexWifi][timestep] = map.fpCoordinate[peopleWifi[indexWifi][timestep]];
		    					break;
		    				}
		    			}
	    			}	    			
	    		}
	    	}
	    	percentOfMissingWifiData[indexWifi] = count * 1.0 / experimentNumber;
	    }
	}
	
	static int getComponentOfWifiFileData(int indexComponent) {
		int locationB1 = fileWifiInput.indexOf("B1");
		int index = 2;
		int start = 0;
		String result = "";
		for (int i = locationB1; i < fileWifiInput.length(); i++) {
			if (fileWifiInput.charAt(i) == '_') {
				index++;
				if (index == indexComponent) {
					start = i;
					break;
				}
			}
		}
		for (int i = start + 1; i < fileWifiInput.length(); i++) {
			if (fileWifiInput.charAt(i) == '_' || fileWifiInput.charAt(i) == '.') {
				break;
			}
			else {
				result += fileWifiInput.charAt(i);
			}
		}
		return Integer.parseInt(result);
	}
	
	static void initializeTheArray() {
	    //startingHour = getComponentOfWifiFileData(3); startingMinute = getComponentOfWifiFileData(4); startingSecond = getComponentOfWifiFileData(5);
	    //endingHour = getComponentOfWifiFileData(7); endingMinute = getComponentOfWifiFileData(8); endingSecond = getComponentOfWifiFileData(9);
		startingHour = 19; startingMinute = 34; startingSecond = 30;
		startingHour = 19; startingMinute = 43; startingSecond = 10;
	    experimentNumber = ((endingHour - startingHour) * 3600 + (endingMinute - startingMinute) * 60 + (endingSecond - startingSecond)) / 5 + 1;
		peopleVideo = new int[10000][experimentNumber];
		peopleVideoCoor = new Coordinate[10000][experimentNumber];
		peopleWifi = new int[10000][experimentNumber];
		peopleWifiWithMissingData = new int[10000][experimentNumber];
		peopleWifiCoor = new Coordinate [10000][experimentNumber];
		for (int i = 0; i < 10000; i++) {
			for (int j = 0; j < experimentNumber; j++) {
				peopleVideo[i][j] = -1;
				peopleWifi[i][j] = -1;
				peopleWifiWithMissingData[i][j] = -1;
				peopleVideoCoor[i][j] = new Coordinate(disappearWifi.x, disappearWifi.y);
				peopleWifiCoor[i][j] = new Coordinate(disappearWifi.x, disappearWifi.y);
			}
		}

	}
	
	public static void initializeForTheRun() {
		rankMatch = new int[blobWifiNumber][blobVideoNumber];
		scoreMatch = new double[blobWifiNumber][blobVideoNumber][experimentNumber];
		countScoreMatch = new int[blobWifiNumber][blobVideoNumber];
		timeStepRankMatch = new int[blobWifiNumber][blobVideoNumber];
		foundConvergeWifi = new boolean[blobWifiNumber];
		foundConvergeVideo = new boolean[blobVideoNumber];
		convergeLatencyForID = new int[blobWifiNumber];
		convergeVideoFinalForID = new int[blobWifiNumber];		
		distributionOfTheError = new int[11];
		for (int i = 0; i < blobWifiNumber; i++) {			 	 
			 foundConvergeWifi[i] = false;			 
			 convergeLatencyForID[i] = -1;
			 convergeVideoFinalForID[i] = -1;			 
			 for (int j = 0; j < blobVideoNumber; j++) {
				 for (int k = 0; k < experimentNumber; k++) {
					 scoreMatch[i][j][k] = -100;
				 }
			 }
			 //people[i] = new HumanStimulation();
		}	
		for (int i = 0; i < blobVideoNumber; i++) {
			foundConvergeVideo[i] = false;
		}
		timeStepFirstAppear = new int[blobVideoNumber];
		for (int i = 0; i < blobVideoNumber; i++) {
			timeStepFirstAppear[i] = -1;
		}
		for (int i = 0; i < blobVideoNumber; i++) {
			for (int j = 0; j < experimentNumber; j++) {
				if (peopleVideo[i][j] != -1) {
					timeStepFirstAppear[i] = j;
					break;
				}
			}
		}
	}
	
	static boolean causeByWifiDisappear(int indexWifi, int time) {
		if (time == 0 || time == experimentNumber) {
			return false;
		}
		//Check if the number of wifi appear in this window is bigger than 40%, then it is caused by wifi disappear
		int count = 0;
		for (int i = time; i >= time - Config.historyWindowLength; i--) {
			if (peopleWifi[indexWifi][i] != -1) {
				count++;
			}
		}
		if (count > Math.floor(0.5 * Config.historyWindowLength)) {
			return true;
		}
		else {
			return false;
		}
	}	
	
	public static boolean insideMap(Coordinate c) {
		if (c.y > 10000 || c.x > 10000 ) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public static double getScoreAverageDistance(Coordinate[] wifi, Coordinate[] video) {
		double averageDistance;	
		// 1 - (x^2) / (WifiError^2)
		averageDistance = 0;
		int averageCount = 0;
		for (int i = 0; i <= Config.historyWindowLength; i++) {
			if (insideMap(wifi[i]) && insideMap(video[i])) {
				averageDistance += distance(wifi[i],video[i]);
				averageCount++;
			}
		}
		averageDistance = averageDistance / averageCount;		
		double pivot = 14; //if distance bigger than 10 then score = 0;
		if (averageDistance < pivot) {									
			return (1 - (averageDistance * averageDistance) / (pivot * pivot));
		}
		return 0;
	}
	
	static double matchScore(Coordinate[] video, Coordinate wifi[]) {
		
		double scoreAverageDistance;
		scoreAverageDistance = getScoreAverageDistance(wifi,video);								
		return scoreAverageDistance;
	}
	
	/*static boolean appearInWifi(int i, int time) {
		if (peopleWifi[i][time] > -1) {
			return true;
		}
		else {
			return false;
		}
	}*/
	
	static int[] findVideoLeader(int indexWifi, int timeStep, boolean[] isVideoAwake) {
		int[] result;
		double max = -1;
		if (indexWifi == 3) {
			indexWifi = 3;
		}
		double[] avr = new double[blobVideoNumber];
		int tempp = 0;
		for (int indexVideo = 0; indexVideo < blobVideoNumber; indexVideo++) {
			if (isVideoAwake[indexVideo] == true) {
				double sum = 0;
				int count = 0;
				
				for (int j = 0; j <= timeStep; j++) {
					if (scoreMatch[indexWifi][indexVideo][j] >= 0) {
						sum += scoreMatch[indexWifi][indexVideo][j];
						count++;
					}
				}
				avr[indexVideo] = sum / count;
				if (avr[indexVideo] > max) {
					max = avr[indexVideo];
					tempp = indexVideo;
				}
			}
		}
		if (max > Config.averageScoreOfLeaderThreshold) {
			int count = 0;
			for (int indexVideo = 0; indexVideo < blobVideoNumber; indexVideo++) {
				if (avr[indexVideo] <= max + Config.rangeScoreConsiderAsLeaders && avr[indexVideo] >= max - Config.rangeScoreConsiderAsLeaders) {
					count++;
				}
			}
			result = new int[count];
			count = 0;
			for (int indexVideo = 0; indexVideo < blobVideoNumber; indexVideo++) {
				if (avr[indexVideo] <= max + Config.rangeScoreConsiderAsLeaders && avr[indexVideo] >= max - Config.rangeScoreConsiderAsLeaders) {
					result[count] = indexVideo;
					count++;
				}
			}		
			return result;
		}
		else {
			return new int[0];
		}
	}

	
	static void movingMatchAwakeLeader(int beginTimeOfHistory) {	
		Coordinate[] wifiCompare = new Coordinate[Config.historyWindowLength + 1];
		Coordinate[] videoCompare = new Coordinate[Config.historyWindowLength + 1];
		for (int indexWifi = 0; indexWifi < blobWifiNumber; indexWifi++) {
			if (indexWifi == 3 && beginTimeOfHistory == 12) {
				indexWifi = 3;
			}
			boolean[] isVideoAwake = new boolean[blobVideoNumber];
			if (foundConvergeWifi[indexWifi] == false && beginTimeOfHistory >= Config.historyWindowLength &&  percentOfMissingWifiData[indexWifi] < 0.45) {
				boolean skip = false;
				for (int i = 0; i <= Config.historyWindowLength; i++) {		
					if (peopleWifi[indexWifi][beginTimeOfHistory - i] != -1) {
						wifiCompare[i] = new Coordinate(map.fpCoordinate[peopleWifi[indexWifi][beginTimeOfHistory - i]].x,
											 map.fpCoordinate[peopleWifi[indexWifi][beginTimeOfHistory - i]].y);
					}
					else {
						if (causeByWifiDisappear(indexWifi, beginTimeOfHistory)) {
							wifiCompare[i] = new Coordinate(disappearWifi.x, disappearWifi.y);
						}
						else {
							skip = true;
							break;
						}
					}
				}
				if (skip) continue;
				for (int indexVideo = 0; indexVideo < blobVideoNumber; indexVideo++) {
					if (indexVideo == 105) {
						indexVideo = 105;
					}
					if (blobVideoAppear[indexVideo]) {
						videoCompare = new Coordinate[Config.historyWindowLength + 1];
						int count = 0;
						int historyTime = beginTimeOfHistory;
						skip = false;
						while (count <= Config.historyWindowLength) {
							if (peopleVideo[indexVideo][historyTime] != -1) {	
								videoCompare[count] = peopleVideoCoor[indexVideo][historyTime];
								historyTime--;							
							}
							else {
								if (peopleWifi[indexWifi][historyTime] == -1) {
									videoCompare[count] = new Coordinate(disappearWifi.x, disappearWifi.y);
									historyTime--;
								}
								else {
									skip = true;
									break;
								}
							}
							count++;
						}
						if (skip) { // Wifi has all data in the windowlength, but video does not
							continue;
						}
						else {
							isVideoAwake[indexVideo] = true;
						}
						double matchResult = matchScore(videoCompare,wifiCompare);
						scoreMatch[indexWifi][indexVideo][beginTimeOfHistory] = matchResult;
						countScoreMatch[indexWifi][indexVideo]++;				
					}
				}
				int[] videoLeader = findVideoLeader(indexWifi, beginTimeOfHistory, isVideoAwake); 
				if (videoLeader.length > 0) {
					if (indexWifi == 3) {
						indexWifi = 3;
					}
					for (int i = 0; i < videoLeader.length; i++) {
						rankMatch[indexWifi][videoLeader[i]]++;
						timeStepRankMatch[indexWifi][videoLeader[i]] = beginTimeOfHistory;
					}
				}
			}
		}
	}

	static double[] getOverlapAvrDistance(int wifi, int video) {//compare the distance between wifi and video if they both appear
		double sum = 0;
		int count = 0;
		double[] result = new double[2];
		for (int i = 0; i < experimentNumber; i++) {
			if (peopleWifi[wifi][i] != -1 && peopleVideo[video][i] != -1) {
				sum += distance(map.fpCoordinate[peopleWifi[wifi][i]],map.fpCoordinate[peopleVideo[video][i]]);
				count++;
			}
		}
		result[0] = sum / count;		
		int countVideoAppear = 0;
		int countWifiAppear = 0;
		for (int i = 0; i < experimentNumber; i++) {
			if (peopleVideo[video][i] != -1) {
				countVideoAppear++;
			}
			if (peopleWifi[wifi][i] != -1) {
				countWifiAppear++;
			}
		}
		result[1] = countWifiAppear * 1.0 / countVideoAppear;
		return result;
	}
	
	static double appearanceRatio(int wifi, int video) {
		int appearCount = 0;
		int coAppear = 0;
		for (int j = 0; j < experimentNumber; j++) {
			if (peopleWifi[wifi][j] != -1 || peopleVideo[video][j] != -1) {
				appearCount++;
			}
			if (peopleWifi[wifi][j] != -1 && peopleVideo[video][j] != -1) {
				coAppear++;;
			}
		}
		double ratio = (coAppear * 1.0 / appearCount);
		return ratio;
	}
	
	static int[] getNonOverlapMatchVideo(int[] convergeVideoForID, boolean requireRatioAppearance) {
		int[] result = new int[blobWifiNumber];
		boolean[] checkVideo = new boolean[blobVideoNumber];
		int[] duplicateMatchVideo = new int[blobVideoNumber];
		for (int i = 0; i < blobVideoNumber; i++) {
			checkVideo[i] = false;			
		}
		for (int i = 0; i < blobWifiNumber; i++) {
			result[i] = -1;
		}
		for (int indexWifi = 0; indexWifi < blobWifiNumber; indexWifi++) {
			if (convergeVideoForID[indexWifi] >= 0) {
				duplicateMatchVideo[convergeVideoForID[indexWifi]]++;
			}
		}
		for (int indexWifi = 0; indexWifi < blobWifiNumber; indexWifi++) {
			if (indexWifi == 848) {
				indexWifi = 848;
			}
			if (convergeVideoForID[indexWifi] >= 0 && checkVideo[convergeVideoForID[indexWifi]] == false) {
				if (indexWifi == 848) {
					indexWifi = 848;
				}
				checkVideo[convergeVideoForID[indexWifi]] = true;
				if (duplicateMatchVideo[convergeVideoForID[indexWifi]] == 1) {
					if (requireRatioAppearance == true) {						
						if (appearanceRatio(indexWifi, convergeVideoForID[indexWifi]) >= Config.rtRealWifi) {
							result[indexWifi] = convergeVideoForID[indexWifi];
						}
					}
					else {
						result[indexWifi] = convergeVideoForID[indexWifi];
					}
				}
				else {
					if (duplicateMatchVideo[convergeVideoForID[indexWifi]] > 1) {
						if (convergeVideoForID[indexWifi] == 105) {
							convergeVideoForID[indexWifi] = 105;
						}
						/*double minDistance = Integer.MAX_VALUE;
						int save = -1;
						for (int wifi = 0; wifi < blobWifiNumber; wifi++) {
							if (convergeVideoForID[wifi] == convergeVideoForID[indexWifi]) {
								double[] distanceResult = getOverlapAvrDistance(wifi, convergeVideoForID[wifi]);
								double avrDistance = distanceResult[0];
								double percentWifiAppear = distanceResult[1];
								if (avrDistance < minDistance && appearanceRatio(wifi, convergeVideoForID[wifi]) >= Config.ratioAppearance) {
									minDistance = avrDistance;
									save = wifi;
								}
							}
						}*/
						double maxRankMatch = 0;
						int save = -1;
						for (int wifi = 0; wifi < blobWifiNumber; wifi++) {
							if (convergeVideoForID[wifi] == convergeVideoForID[indexWifi]) {
								if (rankMatch[wifi][convergeVideoForID[wifi]] > maxRankMatch) {
									maxRankMatch = rankMatch[wifi][convergeVideoForID[wifi]];
									save = wifi;
								}
							}
						}

						if (save != -1) {
							if (requireRatioAppearance == true) { 	
								if (appearanceRatio(save, convergeVideoForID[save]) >= Config.rtRealWifi) {						
									result[save] = convergeVideoForID[save];
								}
							}
							else {
								result[save] = convergeVideoForID[save];
							}
						}
					}
				}
			}
		}
		return result;
	}

	
	public static void getRankMatchingResultAvoidOverlap(int stopRunningProcess, int type) {
		int[] convergeVideoForID = new int[blobWifiNumber];		
		for (int indexWifi = 0; indexWifi < blobWifiNumber; indexWifi++) {
			if (indexWifi == 3) {
				indexWifi = 3;
			}
			convergeVideoForID[indexWifi] = -1;
			int maxRank = 0;
			int videoFound = -1;
			for (int i = 0; i < blobVideoNumber; i++) {
				if (rankMatch[indexWifi][i] > maxRank) {
					maxRank = rankMatch[indexWifi][i];
				}
			}
			if (maxRank > 0) {
				double minDistance = Integer.MAX_VALUE;
				for (int i = 0; i < blobVideoNumber; i++) {
					if (rankMatch[indexWifi][i] == maxRank) {
						double distance = getOverlapAvrDistance(indexWifi, i)[0];
						if (distance < minDistance) {
							minDistance = distance;
							videoFound = i;
						}
					}
				}
				convergeVideoForID[indexWifi] = videoFound;				
			}
		}
		int[] result;
		if (type == 1) {
			result = getNonOverlapMatchVideo(convergeVideoForID, true);
		}
		else {
			result = getNonOverlapMatchVideo(convergeVideoForID, false);
		}
		for (int indexWifi = 0; indexWifi < blobWifiNumber; indexWifi++) {
			if (result[indexWifi] >= 0 && foundConvergeWifi[indexWifi] == false) {
				System.out.println(indexWifi + "---->" + result[indexWifi]);
				convergeVideoFinalForID[indexWifi] = result[indexWifi];
				foundConvergeWifi[indexWifi] = true;
				foundConvergeVideo[result[indexWifi]] = true;
				stopLoopingAvoidOverlap = false;
			}
		}	
		
	}
	
	public static void getRankMatchingResultAvoidOverlapNew() {
		int[] result = new int[blobWifiNumber];
		boolean markWifi[] = new boolean[blobWifiNumber];
		boolean markVideo[] = new boolean[blobVideoNumber];
		for (int indexWifi = 0; indexWifi < blobWifiNumber; indexWifi++) {
			markWifi[indexWifi] = false;
			result[indexWifi] = -1;
		}
		for (int indexVideo = 0; indexVideo < blobWifiNumber; indexVideo++) {
			markWifi[indexVideo] = false;
		}
		int max = 1;
		while (max != 0) {
			max = 0;
			int saveWifi = -1;
			int saveVideo = -1;
			for (int indexWifi = 0; indexWifi < blobWifiNumber; indexWifi++) {
				if (markWifi[indexWifi] == false) {
					for (int indexVideo = 0; indexVideo < blobVideoNumber; indexVideo++) {
						if (markVideo[indexVideo] == false) {
							if (rankMatch[indexWifi][indexVideo] > max) {
								if (indexVideo == 9) {
									indexVideo = 9;
								}
								max = rankMatch[indexWifi][indexVideo];
								saveWifi = indexWifi;
								saveVideo = indexVideo;
							}
						}
					}
				}
			}
			if (max != 0) {
				System.out.println(max);
				System.out.println(saveWifi);
				System.out.println(saveVideo);
				System.out.println("--------------");
				result[saveWifi] = saveVideo;
				markWifi[saveWifi] = true;
				markVideo[saveVideo] = true;
			}
		}
		for (int indexWifi = 0; indexWifi < blobWifiNumber; indexWifi++) {
			if (result[indexWifi] >= 0 && foundConvergeWifi[indexWifi] == false) {
				convergeVideoFinalForID[indexWifi] = result[indexWifi];
				foundConvergeWifi[indexWifi] = true;
				foundConvergeVideo[result[indexWifi]] = true;
				stopLoopingAvoidOverlap = false;
			}
		}		
		
	}

	
	static void eraseScoreMatch() {
		for (int i = 0; i < blobWifiNumber; i++) {
			for (int j = 0; j < blobVideoNumber; j++) {
				for (int k = 0; k < experimentNumber; k++) {
					scoreMatch[i][j][k] = -100;
				}
			}
		}		
	}
	
	static void movingMatchAwakeLeaderWithLimitedWifiVideo(int beginTimeOfHistory, int indexWifi) {		

		Coordinate[] wifiCompare = new Coordinate[Config.historyWindowLength + 1];
		Coordinate[] videoCompare = new Coordinate[Config.historyWindowLength + 1];		
		boolean[] isVideoAwake = new boolean[blobVideoNumber];
		if (foundConvergeWifi[indexWifi] == false && beginTimeOfHistory >= Config.historyWindowLength  && percentOfMissingWifiData[indexWifi] < 0.45) {
			boolean skip = false;
			for (int i = 0; i <= Config.historyWindowLength; i++) {		
				if (peopleWifi[indexWifi][beginTimeOfHistory - i] != -1) {
					wifiCompare[i] = new Coordinate(map.fpCoordinate[peopleWifi[indexWifi][beginTimeOfHistory - i]].x,
										 map.fpCoordinate[peopleWifi[indexWifi][beginTimeOfHistory - i]].y);
				}
				else {
					if (causeByWifiDisappear(indexWifi, beginTimeOfHistory)) {
						wifiCompare[i] = new Coordinate(disappearWifi.x, disappearWifi.y);
					}
					else {
						skip = true;
						break;
					}
				}
			}
			if (skip) return;

			for (int indexVideo = 0; indexVideo < blobVideoNumber; indexVideo++) {
				if (blobVideoAppear[indexVideo] && foundConvergeVideo[indexVideo] == false) {
					videoCompare = new Coordinate[Config.historyWindowLength + 1];
					int count = 0;
					int historyTime = beginTimeOfHistory;
					skip = false;
					while (count <= Config.historyWindowLength) {						
						if (peopleVideo[indexVideo][historyTime] != -1) {
							videoCompare[count] = peopleVideoCoor[indexVideo][historyTime];
							historyTime--;							
						}
						else {
							if (peopleWifi[indexWifi][historyTime] == -1) {
								videoCompare[count] = new Coordinate(disappearWifi.x, disappearWifi.y);
								historyTime--;
							}
							else {
								skip = true;
								break;
							}
						}
						count++;						
					}
					if (skip) { // Wifi has all data in the windowlength, but video does not
						continue;
					}
					else {
						isVideoAwake[indexVideo] = true;
					}
					if (indexWifi == 848 && indexVideo == 9) {
						indexWifi = 848;
					}
					double matchResult = matchScore(videoCompare,wifiCompare);
					scoreMatch[indexWifi][indexVideo][beginTimeOfHistory] = matchResult;
					countScoreMatch[indexWifi][indexVideo]++;		
				}
			}
			int[] videoLeader = findVideoLeader(indexWifi, beginTimeOfHistory, isVideoAwake); 
			if (videoLeader.length > 0) {
				for (int i = 0; i < videoLeader.length; i++) {
					rankMatch[indexWifi][videoLeader[i]]++;
					timeStepRankMatch[indexWifi][videoLeader[i]] = beginTimeOfHistory;
				}
			}
		}
	}
	
	static void matching() {
		initializeForTheRun();	
		for (int time = Config.historyWindowLength; time < experimentNumber; time++) {
			movingMatchAwakeLeader(time);
		}
		//System.out.println("--------");
		getRankMatchingResultAvoidOverlap(experimentNumber - 1, 1);
		//getRankMatchingResultAvoidOverlapNew();
		stopLoopingAvoidOverlap = false;
		
		while (stopLoopingAvoidOverlap == false) {										
			stopLoopingAvoidOverlap = true;
			rankMatch = new int[blobWifiNumber][blobVideoNumber];
			eraseScoreMatch();
			countScoreMatch = new int[blobWifiNumber][blobVideoNumber];
			for (int indexWifi = 0; indexWifi < blobWifiNumber; indexWifi++) {
				if (foundConvergeWifi[indexWifi] == false) {
					for (int time = Config.historyWindowLength; time < experimentNumber; time++) {						
						movingMatchAwakeLeaderWithLimitedWifiVideo(time,indexWifi);
					}
				}
			}
			getRankMatchingResultAvoidOverlap(experimentNumber - 1, 1);
			//getRankMatchingResultAvoidOverlapNew();
		}									
		// Continue to find match for the unfound
		int dem = 0;
		stopLoopingAvoidOverlap = false;
		while (stopLoopingAvoidOverlap == false) {																			
			stopLoopingAvoidOverlap = true;
			rankMatch = new int[blobWifiNumber][blobVideoNumber];
			eraseScoreMatch();
			countScoreMatch = new int[blobWifiNumber][blobVideoNumber];
			for (int indexWifi = 0; indexWifi < blobWifiNumber; indexWifi++) {
				if (foundConvergeWifi[indexWifi] == false) {
					for (int time = Config.historyWindowLength; time < experimentNumber; time++) {						
						movingMatchAwakeLeaderWithLimitedWifiVideo(time,indexWifi);
					}
				}
			}
			getRankMatchingResultAvoidOverlap(experimentNumber - 1, 2);
			//getRankMatchingResultAvoidOverlapNew();
		}																		
		printResult();

	}
	
	public static void printResult() {
		int numberOfFoundConverge = 0;
		int numberOfCorrectMatchAmongFoundConverge = 0;
		int numberOfWrongMatchAmongFoundConverge = 0;
		for (int i = 0; i < blobWifiNumber; i++) {
			if (foundConvergeWifi[i] == true) {
				numberOfFoundConverge++;
				if (convergeVideoFinalForID[i] == i) {
					numberOfCorrectMatchAmongFoundConverge++;
				}
				else {
					numberOfWrongMatchAmongFoundConverge++;
				}
			}
		}
		BufferedWriter writer;
		try {
			
			String fileOutput = fileOutputFolder + "output_" + startingHour + "_" + startingMinute + "_" + startingSecond + "_to_" + endingHour + "_" + endingMinute + "_" + endingSecond + ".csv";
			writer = new BufferedWriter(new FileWriter(fileOutput));
			int wrongMatchConsiderAsCorrect = 0;
			for (int i = 0; i < blobWifiNumber; i++) {
				if (foundConvergeWifi[i] == true) {					
					writer.write("Wifi " + i + ": match to video " + convergeVideoFinalForID[i]);
					//writer.write(" with latency: " + (timeStepRankMatch[i][convergeVideoFinalForID[i]] - timeStepFirstAppear[i]));
					//writer.write(" (Match at:" + timeStepRankMatch[i][convergeVideoFinalForID[i]] + " Appear at:" + timeStepFirstAppear[i] + ") ");
					//sumLattency += timeStepRankMatch[i][convergeVideoFinalForID[i]] - timeStepFirstAppear[i];					
					writer.write("\n");
				}
			}

			
			//writer.write("Average Lattency: " + df2.format(sumLattency * 1.0 / countLattency) + "\n");
			//writer.write("Average Distance between correct and incorrect match:" + df2.format(averageDistanceBetweenCorrectWrongMatch / countDistanceBetweenCorrectWrongMatch) + "\n");
			//writer.write("Correct Distance Much Better Incorrect Distance: " + df2.format(countCorrectDistanceMuchBetterIncorrectDistance * 1.0 / countDistanceBetweenCorrectWrongMatch) + "\n");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void findAdditionalWifiGroudTruth() {
		//int[] listVideo = new int[] {5, 105, 289, 350, 609, 666};
		int[] listVideo = new int[] {187};
		Coordinate videoCoor[] = new Coordinate[experimentNumber];
		int video[] = new int[experimentNumber];
		boolean acceptWifi[] = new boolean [blobWifiNumber]; 
		int duration = 6;
		for (int timestep = 0; timestep < experimentNumber; timestep++ ) {
			video[timestep] = -1;
		}
		for (int i = 0; i < blobWifiNumber; i++) {
			acceptWifi[i] = false;
		}
		for (int i = 0; i < listVideo.length; i++) {
			for (int timestep = 0; timestep < experimentNumber; timestep++) {
				if (peopleVideo[listVideo[i]][timestep] != -1) {
					video[timestep] = peopleVideo[listVideo[i]][timestep];
					videoCoor[timestep] = new Coordinate(peopleVideoCoor[listVideo[i]][timestep].x,peopleVideoCoor[listVideo[i]][timestep].y);
				}
			}
		}
		int timeCheck = experimentNumber / duration;
		for (int indexWifi = 1; indexWifi < blobWifiNumber; indexWifi++) {
			boolean accept = true;
			if (indexWifi == 27) {
				indexWifi = 27;
			}
			for (int i = 0; i < timeCheck; i++) {
				int satisfyCount = 0;
				int videoCount = 0;
				for (int time = i * duration; time < (i + 1) * duration; time++) {
					if (video[time] != -1) {
						videoCount++;
						if (peopleWifiWithMissingData[indexWifi][time] != -1) {
							double distance = distance(map.fpCoordinate[peopleWifiWithMissingData[indexWifi][time]], videoCoor[time]);
							if (distance < 8) {
								satisfyCount++;
							}
						}
					}
					else {
						videoCount++;
						satisfyCount++;
					}
				}
				if (satisfyCount < 0.5 * videoCount || satisfyCount == 0) {
					accept = false;
					break;
				}
			}			
			if (accept) {
				acceptWifi[indexWifi] = true;
			}
		}
		for (int indexWifi = 1; indexWifi < blobWifiNumber; indexWifi++) {
			if (acceptWifi[indexWifi]) {
				System.out.println(indexWifi);
			}
		}
		
	}
	
	//--------------------Main-------------------------------
	public static void main( String[ ] args ) {
		BufferedWriter writer;

			
		//fileWifiInput = "src/SMU_Synthetic/SMU_RealData/wifi/output_levelB1_12_" + begin + "_0_to_12_" + end + "_0.csv";
		
		initializeTheArray();
		map = new MapRealLevel1();	
		readVideoFile();
		readCalibrationFile();
		for (int k = 0; k < calibrateID.length; k++) {
			getCalibrateInformation(calibrateID[k]);
		}
		convertToPhysicalCoordinate();
		//createConfigInfo(2); //landmark distance = 2;
		
		generateVideoFromFile();
		readWifiData();
		matching();
	}
}
