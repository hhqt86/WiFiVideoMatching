package SuntecRealData;


import java.io.BufferedWriter;
import java.io.File;


import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Scanner;








public class Suntec_Level6 {
    static int startingHour;static int startingMinute;static int startingSecond;
    static int endingHour;static int endingMinute;static int endingSecond;
    static int experimentNumber = 0; // Experiment length from the SMU video dataset
    static int[] fpPointInTheArea = {43,44,45,47,48,49,50,51,52,53,54,55,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155};
	
    static String videoFolder = "src/SuntecRealData/OutputLevel6Camera4R/";
	static String fileVideoInput = "resultRight.txt"; // File location of the SMU video
	static String wifiFolder = "src/SuntecRealData/";
	static String fileWifiInput = "output_Suntec_19_28_0_to_19_38_0.csv";
	static String fileOutputFolder = "src/SuntecRealData/output/";

	static int frameNumber = 0; // Number of used frame
	static BoundingBoxImageCoor[][] blobImageCoor = new BoundingBoxImageCoor[Config.maximumBlobCount][Config.maximumFrameCount]; // Coordinate of all the bounding box in the SMU video
	static Coordinate[][] blobPhysicalCoor = new Coordinate[Config.maximumBlobCount][Config.maximumFrameCount];
	//static BoundingBoxImageCoor[][] blobImageCalibration = new BoundingBoxImageCoor[Config.maximumBlobCount][Config.maximumFrameCount]; // Coordinate of all the bounding box in the SMU video
	static int[][] peopleVideo;
	static Coordinate[][] peopleVideoCoor;
	static int[][] peopleWifi;
	static Coordinate[][] peopleWifiCoor;
	static boolean[] blobVideoAppear;
	static int blobVideoNumber = 0;  // Number of video blobs in the SMU video
	static int blobWifiNumber = 0;
	static int countWifiAppearInTheArea;
	static int[] indexOfTheRealWifi = new int[Config.maximumBlobCount];
	static BlobDuration[] blobDuration = new BlobDuration[Config.maximumBlobCount];
	static timeWithLocationCoordinate[][] peopleWifiAfterInterpolate;
	static int[] countTimeWifiReportAfterInterpolate;
	static timeWithLocationCoordinate[][] peopleVideoAfterInterpolate;
	static int[] countTimeVideoReportAfterInterpolate;

	static MapRealLevel6 map;
	
	static double distance(Coordinate point1, Coordinate point2) {
		return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
	}
	
	static void initializeTheArray() {
		startingHour = 19; startingMinute = 28; startingSecond = 0;
		endingHour = 19; endingMinute = 38; endingSecond = 0;
	    experimentNumber = ((endingHour - startingHour) * 3600 + (endingMinute - startingMinute) * 60 + (endingSecond - startingSecond)) / 5 + 1;
		peopleVideo = new int[Config.maximumBlobCount][experimentNumber];
		peopleVideoCoor = new Coordinate[Config.maximumBlobCount][experimentNumber];
		peopleWifi = new int[Config.maximumBlobCount][experimentNumber];
		peopleWifiCoor = new Coordinate [Config.maximumBlobCount][experimentNumber];
		for (int i = 0; i < Config.maximumBlobCount; i++) {
			blobDuration[i] = new BlobDuration(Integer.MAX_VALUE,0);
			for (int j = 0; j < experimentNumber; j++) {
				peopleVideo[i][j] = -1;
				peopleWifi[i][j] = -1;
				peopleVideoCoor[i][j] = new Coordinate(Config.noPoint.x, Config.noPoint.y);
				peopleWifiCoor[i][j] = new Coordinate(Config.noPoint.x, Config.noPoint.y);
			}
		}

	}
	
	static void readVideoFile() {		
		Scanner scan;
		ParserVideo parser;
	    try {
	    	File file = new File(videoFolder+fileVideoInput);
	        scan = new Scanner(file);
	        scan.nextLine();
	        String data;
	        while (scan.hasNextLine()) {
	        	if (frameNumber % 1000 == 0) {
	        		System.out.println(frameNumber);
	        	}
	        	data = scan.nextLine();
	        	parser = new ParserVideo(data);
	        	int frame = parser.getFrameID();
	        	int blobID = parser.getBlobID();
	        	if (frame > frameNumber) {
	        		frameNumber = frame;
	        	}
	        	if (blobID > blobVideoNumber) {
	        		blobVideoNumber = blobID;
	        	}
	        	blobImageCoor[blobID][frame] = new BoundingBoxImageCoor(parser.getX(), parser.getY(), parser.getW(), parser.getH());
	        	if (frame < blobDuration[blobID].left) {
	        		blobDuration[blobID].left = frame;
	        	}
	        	if (frame > blobDuration[blobID].right) {
	        		blobDuration[blobID].right = frame;
	        	}
	        }
	       
   		scan.close();
   		peopleVideo = new int[blobVideoNumber][experimentNumber];
   		blobVideoAppear = new boolean[blobVideoNumber];
   		for (int i = 0; i < blobVideoNumber; i++) {
   			blobVideoAppear[i] = false;
   		}
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }
	}
	
	public static Coordinate convertToPhysicalCoor(Coordinate footLocation) {
		Coordinate result = new Coordinate(-1, -1);
/*		int saveX = -10;
		int saveY = -10;
		
		//Find x
		for (int i = map.numberOfPhysicalX - 1; i >= 0; i--) {
			if (map.videoCoordinate[i][0].y > footLocation.y) {
				result.x = i; //in this cell
				saveX = i;				
				break;
			}
		}
		
		for (int j = 0; j < map.numberOfPhysicalY; j++) {
			if (map.videoCoordinate[saveX][j].x > footLocation.x) {
				result.y = j - 1;
				saveY = j - 1;
				break;
			}
		}
		if (saveX < map.numberOfPhysicalX - 1) {
			result.x += (map.videoCoordinate[saveX][saveY].y - footLocation.y) / (map.videoCoordinate[saveX][saveY].y - map.videoCoordinate[saveX + 1][saveY].y); // locate in a cell
		}
		if (saveY >= 0) {
			result.y += (footLocation.x - map.videoCoordinate[saveX][saveY].x) / (map.videoCoordinate[saveX][saveY + 1].x - map.videoCoordinate[saveX][saveY].x);
		}
		else {
			result.y = -0.2;
		}*/
		double minDistance = Integer.MAX_VALUE;
		for (int i = 0; i < map.numberOfPhysicalX; i++) {
			for (int j = 0; j < map.numberOfPhysicalY; j++) {
				if (map.videoCoordinate[i][j] != null && distance(map.videoCoordinate[i][j], footLocation) < minDistance) {
					minDistance = distance(map.videoCoordinate[i][j], footLocation);
					result.x = i;
					result.y = j;
				}
			}
		}
		return result;
	}
	
	public static int getNearestLandMarkInt(Coordinate location) {
		double minDistance = Integer.MAX_VALUE;
		int result = 0;
		for (int i = 0; i < map.fpPointInThisMap; i++) {
			if (map.fpCoordinate[i].x != Config.noPoint.x && map.fpCoordinate[i].y != Config.noPoint.y) {
				if (distance(location, map.fpCoordinate[i]) < minDistance) {
					minDistance = distance(location, map.fpCoordinate[i]);
					result = i;
				}
			}
		}
		return result;
	}
	
	public static void convertToPhysicalCoordinate() {
		//(1920 x 1080)		
		int level = 6;
		for (int blobID = 0; blobID < blobVideoNumber; blobID++) {
			for (int frame = blobDuration[blobID].left; frame <= blobDuration[blobID].right; frame++) {
				if (blobImageCoor[blobID][frame] != null) {
					int timeStep = frame / 125; //video is 25 fps, and each timestep is 5 seconds
					Coordinate footLocation = new Coordinate((int)(blobImageCoor[blobID][frame].x + blobImageCoor[blobID][frame].w / 2), (int)(blobImageCoor[blobID][frame].y + blobImageCoor[blobID][frame].h));
					if (blobPhysicalCoor[blobID][timeStep] == null) {
						blobPhysicalCoor[blobID][timeStep] = convertToPhysicalCoor(footLocation);
						peopleVideo[blobID][timeStep] = getNearestLandMarkInt(blobPhysicalCoor[blobID][timeStep]);
						Coordinate location = blobPhysicalCoor[blobID][timeStep];
						
					}
				}
			}
		}				
	}
	public static int getTimeStep(String time, int startingHour, int startingMinute, int startingSecond) {
		int currentHour = Integer.parseInt(time.substring(0, 2));
		int currentMinute = Integer.parseInt(time.substring(3, 5));
		int currentSecond = Integer.parseInt(time.substring(6, 8));
		return (currentHour - startingHour) * 3600 + (currentMinute - startingMinute) * 60 + (currentSecond - startingSecond);
		
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
	
	public static void readWifiData() {
	    Scanner scan;	    
	    ParserWifi parser;	
	    blobWifiNumber = 0;
	    int[][] peopleWifiTemp = new int[Config.maximumBlobCount][experimentNumber];
		for (int i = 0; i < Config.maximumBlobCount; i++) {			
			for (int j = 0; j < experimentNumber; j++) {
				peopleWifiTemp[i][j] = -1;
			}
		}
	    try {
	    	File file = new File(wifiFolder + fileWifiInput);
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
		        	if (parser.getLocationLevel() == 6)  {		        		
		        		peopleWifiTemp[ID][timeStep] = parser.getLocation();
		        	}
	        	}
	        }
   		scan.close();
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }
	    wifiAppearInTheArea = new boolean[blobWifiNumber];
	    double sumCountAppear = 0;
	    countWifiAppearInTheArea = 0;
	    for (int i = 0; i < blobWifiNumber; i++) {
	    	if (wifiAppearInTheExamineArea(peopleWifiTemp[i])) {
	    		wifiAppearInTheArea[i] = true;
	    		countWifiAppearInTheArea++;
	    		/*int start = 0; int end = experimentNumber - 1;
	    		for (int k = 0; k < experimentNumber; k++) {
	    			if (peopleWifi[i][k] != -1) {
	    				start = k;
	    				break;
	    			}
	    		}
	    		for (int k = experimentNumber - 1; k >=0; k--) {
	    			if (peopleWifi[i][k] != -1) {
	    				end = k;
	    				break;
	    			}
	    		}*/	    			    			    		    			    		
	    		int count = 0;
	    		for (int k = 0; k < experimentNumber; k++) {
	    			if (peopleWifiTemp[i][k] != -1) {
	    				count++;
	    			}
	    		}	    		
	    		sumCountAppear += count;
	    	}
	    }
	    int count = -1;
	    for (int i = 0; i < blobWifiNumber; i++) {
	    	if (wifiAppearInTheExamineArea(peopleWifiTemp[i])) {
	    		count++;
	    		for (int k = 0; k < experimentNumber; k++) {
	    			peopleWifi[count][k] = peopleWifiTemp[i][k];
	    			indexOfTheRealWifi[count] = i;
	    		}
	    	}
	    }
	    blobWifiNumber = countWifiAppearInTheArea;
		BufferedWriter writer;
		try {				
			writer = new BufferedWriter(new FileWriter(fileOutputFolder + "TestWifiAppear.txt"));
			for (int i = 0; i < blobWifiNumber; i++) {
				writer.write("Wifi " + i + "\n");
				for (int k = 0; k < experimentNumber; k++)					
					writer.write(peopleWifi[i][k] + " ");				
				writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    System.out.println("Average timestep wifi report for a signal: " + sumCountAppear * 1.0 / countWifiAppearInTheArea);
	    System.out.println("Wifi appear in the area: " + countWifiAppearInTheArea);
	}
	

	
	//For matching algorithm


	//For algorithm
	static int timeStepRankMatch[][];
	static boolean wifiAppearInTheArea[];
	//static boolean[][] isVideoAwakeAtTimeStep; 
	static int[] departingTimeOfWifi;
	static boolean[] foundConvergeWifi;
	static boolean[] foundConvergeVideo;
	static int[] convergeLatencyForWifiID;
	static int[] convergeVideoFinalForWifiID;
	static boolean[] wifiDisappear;
	static int rankMatch[][];
	static double accScoreMatch[][];
	static double scoreMatch[][]; 
	 
	static int maxRankStatisticValue[][][];
	static int maxRankStatisticFrequency[][][];
	static int countScoreMatch[][];
	static int lastTimeStepRankMatchUpdateOnAWifi[];
	static int departingTimeOfVideo[];
	static boolean stopLoopingAvoidOverlap;
	//static long[] BlobMovementlength;
	static int numberCaseSolve = 0;
	
	
	
	public static void initializeForTheRun() {
		rankMatch = new int[blobWifiNumber][blobVideoNumber];
		scoreMatch = new double[blobWifiNumber][blobVideoNumber];
		accScoreMatch = new double[blobWifiNumber][blobVideoNumber];
		departingTimeOfWifi = new int[blobWifiNumber];
		maxRankStatisticValue = new int[blobWifiNumber][blobVideoNumber][5];
		maxRankStatisticFrequency = new int[blobWifiNumber][blobVideoNumber][5];
		countScoreMatch = new int[blobWifiNumber][blobVideoNumber];
		lastTimeStepRankMatchUpdateOnAWifi = new int[blobWifiNumber];
		foundConvergeWifi = new boolean[blobWifiNumber];
		foundConvergeVideo = new boolean[blobVideoNumber];
		convergeLatencyForWifiID = new int[blobWifiNumber];
		convergeVideoFinalForWifiID = new int[blobWifiNumber];
		timeStepRankMatch = new int[blobWifiNumber][blobVideoNumber];
		wifiDisappear = new boolean[blobWifiNumber];
				
		for (int i = 0; i < blobWifiNumber; i++) {			 	 
			 foundConvergeWifi[i] = false;			 
			 convergeLatencyForWifiID[i] = -1;
			 convergeVideoFinalForWifiID[i] = -1;
			 wifiDisappear[i] = false;
			 for (int j = 0; j < blobVideoNumber; j++) {
				 scoreMatch[i][j] = -100;
				 
			 }			 
		}	
		for (int i = 0; i < blobVideoNumber; i++) {
			foundConvergeVideo[i] = false;
		}
		departingTimeOfVideo = new int[blobVideoNumber];
		for (int i = 0; i < blobVideoNumber; i++) {
			departingTimeOfVideo[i] = -1;
		}
		for (int i = 0; i < blobVideoNumber; i++) {
			for (int j = 0; j < experimentNumber; j++) {
				if (peopleVideo[i][j] != -1) {
					departingTimeOfVideo[i] = j;
					break;
				}
			}
		}
		for (int i = 0; i < blobWifiNumber; i++) {
			for (int j = 0; j < experimentNumber; j++) {
				if (peopleWifi[i][j] != -1) {
					departingTimeOfWifi[i] = j;
					break;
				}
			}
		}		
	}
	
	static void eraseScoreMatch() {
		for (int i = 0; i < blobWifiNumber; i++) {
			for (int j = 0; j < blobVideoNumber; j++) {				
				scoreMatch[i][j] = -100;				
			}
		}		
	}
	
	public static boolean insideMap(Coordinate c) {
		if (c == null || c.x == Config.noPoint.x || c.y == Config.noPoint.y) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public static boolean insideMap(int landmark) {
		if (landmark > map.fpPointInThisMap || map.fpCoordinate[landmark].x == Config.noPoint.x || map.fpCoordinate[landmark].y == Config.noPoint.y) {
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
		double pivot = 3;
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
	
	static int[] findVideoLeader(int indexWifi, int timeStep, boolean[] isVideoAwake) {
		int[] result;
		int save = -1;
		double max = -1;
		double[] avr = new double[blobVideoNumber];
		for (int indexVideo = 0; indexVideo < blobVideoNumber; indexVideo++) {
			if (isVideoAwake[indexVideo] == true) {
				double sum = 0;
				double count = 0;
				avr[indexVideo] = accScoreMatch[indexWifi][indexVideo];
				if (avr[indexVideo] > max) {
					max = avr[indexVideo];
					save = indexVideo;
				}
			}
		}
		if (max > 0) {
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
	
	static void movingMatchLeader(int beginTimeOfHistory, int indexWifi) {		

		//isScoreMatchUpdate = new boolean[blobNumber][blobNumber];
		if (beginTimeOfHistory - departingTimeOfWifi[indexWifi] >= Config.historyWindowLength) {
			if (indexWifi == 0) {
				indexWifi = 0;
			}
			Coordinate[] wifiCompare = new Coordinate[Config.historyWindowLength + 1];
			Coordinate[] videoCompare = new Coordinate[Config.historyWindowLength + 1];		
			boolean[] isVideoAvailable = new boolean[blobVideoNumber];
			if (foundConvergeWifi[indexWifi] == false && beginTimeOfHistory >= Config.historyWindowLength) {
				boolean skip = false;
				for (int i = 0; i <= Config.historyWindowLength; i++) {		
					if (peopleWifi[indexWifi][beginTimeOfHistory - i] != -1 && insideMap(peopleWifi[indexWifi][beginTimeOfHistory - i])) {
						wifiCompare[i] = new Coordinate(map.fpCoordinate[peopleWifi[indexWifi][beginTimeOfHistory - i]].x,
											 map.fpCoordinate[peopleWifi[indexWifi][beginTimeOfHistory - i]].y);
					}
					else {
						wifiCompare[i] = new Coordinate(Config.noPoint.x, Config.noPoint.y);
					}
				}
				if (skip) return;
	
				for (int indexVideo = 0; indexVideo < blobVideoNumber; indexVideo++) {
					if (foundConvergeVideo[indexVideo] == false) {
						videoCompare = new Coordinate[Config.historyWindowLength + 1];
						int count = 0;
						int historyTime = beginTimeOfHistory;
						skip = false;
						while (count <= Config.historyWindowLength) {						
							if (peopleVideo[indexVideo][historyTime] != -1) {							
								videoCompare[count] = new Coordinate(map.fpCoordinate[peopleVideo[indexVideo][historyTime]].x,map.fpCoordinate[peopleVideo[indexVideo][historyTime]].y);
								historyTime--;							
							}
							else {
								if (peopleWifi[indexWifi][historyTime] == -1) {
									videoCompare[count] = new Coordinate(Config.noPoint.x, Config.noPoint.y);
									historyTime--;
								}
								else {
									skip = true;
									break;
								}
							}
							count++;						
						}
						if (skip) { // 
							continue;
						}
						else {
							isVideoAvailable[indexVideo] = true;
						}
						
						double matchResult = matchScore(videoCompare,wifiCompare);
						scoreMatch[indexWifi][indexVideo] = matchResult;
						countScoreMatch[indexWifi][indexVideo]++;						
						accScoreMatch[indexWifi][indexVideo] = (accScoreMatch[indexWifi][indexVideo] * (countScoreMatch[indexWifi][indexVideo] - 1) + scoreMatch[indexWifi][indexVideo])
								   / countScoreMatch[indexWifi][indexVideo];
					}
				}
				int[] videoLeader = findVideoLeader(indexWifi, beginTimeOfHistory, isVideoAvailable); 
				if (videoLeader.length > 0) {
					for (int i = 0; i < videoLeader.length; i++) {
						rankMatch[indexWifi][videoLeader[i]]++;
						lastTimeStepRankMatchUpdateOnAWifi[indexWifi] = beginTimeOfHistory;
						timeStepRankMatch[indexWifi][videoLeader[i]] = beginTimeOfHistory;
					}
				}
			}
		}
	}
	
	static double[] getOverlapAvrDistance(int limitExperimentTimeStep, int wifi, int video) {//compare the distance between wifi and video if they both appear
		double sum = 0;
		int count = 0;
		double[] result = new double[2];
		for (int i = 0; i < limitExperimentTimeStep; i++) {
			if (peopleWifi[wifi][i] != -1 && peopleVideo[video][i] != -1) {
				sum += distance(map.fpCoordinate[peopleWifi[wifi][i]],map.fpCoordinate[peopleVideo[video][i]]);
				count++;
			}
		}
		result[0] = sum / count;
		int countWifiAppearInCaseVideoAppear = 0;
		int countVideoAppear = 0;
		int countWifiAppear = 0;
		for (int i = 0; i < limitExperimentTimeStep; i++) {
			if (peopleVideo[video][i] != -1) {
				countVideoAppear++;
				/*if (peopleWifi[wifi][i] != -1) {
					countWifiAppearInCaseVideoAppear++;
				}*/
			}
			if (peopleWifi[wifi][i] != -1) {
				countWifiAppear++;
			}
		}
		//result[1] = countWifiAppearInCaseVideoAppear * 1.0 / countVideoAppear;
		result[1] = countWifiAppear * 1.0 / countVideoAppear;
		return result;
	}
	
	static double existRT(int limitExperimentTimeStep, int wifi, int video) {
		int appearCount = 0;
		int coAppear = 0;
		for (int j = 0; j < limitExperimentTimeStep; j++) {
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
	
	static int[] getNonOverlapMatchVideo(int limitExperimentTimeStep, int[] convergeVideoForID, boolean requireRatioAppearance) {
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
			if (convergeVideoForID[indexWifi] >= 0 && checkVideo[convergeVideoForID[indexWifi]] == false) {
				checkVideo[convergeVideoForID[indexWifi]] = true;
				if (duplicateMatchVideo[convergeVideoForID[indexWifi]] == 1) {
					if (requireRatioAppearance == true) {						
						if (existRT(limitExperimentTimeStep, indexWifi, convergeVideoForID[indexWifi]) >= Config.rtSimulator) {
							result[indexWifi] = convergeVideoForID[indexWifi];
						}
					}
					else {
						result[indexWifi] = convergeVideoForID[indexWifi];
					}
				}
				else {
					if (duplicateMatchVideo[convergeVideoForID[indexWifi]] > 1) {
						double minDistance = Integer.MAX_VALUE;
						double maxAccScoreMatch = 0;
						double maxRankScore = 0;
						int save = -1;
						for (int wifi = 0; wifi < blobWifiNumber; wifi++) {
							if (convergeVideoForID[wifi] == convergeVideoForID[indexWifi]) {
								double[] distanceResult = getOverlapAvrDistance(limitExperimentTimeStep, wifi, convergeVideoForID[wifi]);
								double avrDistance = distanceResult[0];								
								/*if (accScoreMatch[wifi][convergeVideoForID[wifi]] > maxAccScoreMatch && existRT(limitExperimentTimeStep, wifi, convergeVideoForID[wifi]) >= Config.rtSimulator) {
									maxAccScoreMatch = accScoreMatch[wifi][convergeVideoForID[wifi]];
									save = wifi;
								}*/	
								if (avrDistance < minDistance && existRT(limitExperimentTimeStep, wifi, convergeVideoForID[wifi]) >= Config.rtSimulator) {
									minDistance = avrDistance;
									save = wifi;
								}
								/*if (rankMatch[wifi][convergeVideoForID[wifi]] >= maxRankScore) {									 
									maxRankScore = rankMatch[wifi][convergeVideoForID[wifi]];
									save = wifi;
								}*/
							}
						}
						if (save != -1) {
							result[save] = convergeVideoForID[save];
						}
					}
				}
			}
		}
		return result;
	}
	
	public static void getMaxRankStatistic(int wifi, int video) {
		int[] maxRankOfWifi = new int[blobWifiNumber];
		for (int indexVideo = 0; indexVideo < blobVideoNumber; indexVideo++) {
			maxRankOfWifi[indexVideo] = rankMatch[wifi][indexVideo];
		}
		int[] frequency = new int[4];
		int[] maxRankValue = new int[4];
		for (int i = 0; i < 4; i++) {
			int max = 0;
			for (int k = 0; k < blobWifiNumber; k++) {
				if (maxRankOfWifi[k] > max && (i == 0 || maxRankOfWifi[k] < maxRankValue[i - 1])) {
					max = maxRankOfWifi[k];
				}
			}
			maxRankValue[i] = max;
		}
		for (int i = 0; i < 4; i++) {
			int freq = 0;
			for (int k = 0; k < blobWifiNumber; k++) {
				if (maxRankOfWifi[k] == maxRankValue[i]) {
					freq++;
				}
			}
			frequency[i] = freq;
		}
		for (int i = 0; i < 4; i++) {
			maxRankStatisticValue[wifi][video][i] = maxRankValue[i];
			maxRankStatisticFrequency[wifi][video][i] = frequency[i];
		}
	}
	
	public static void getRankMatchingResultAvoidOverlap(int slidingWindowTimeStep, int type) {
		int[] convergeVideoForID = new int[blobWifiNumber];		
		for (int indexWifi = 0; indexWifi < blobWifiNumber; indexWifi++) {
			convergeVideoForID[indexWifi] = -1;
			if (foundConvergeWifi[indexWifi] == false) {
				int maxRank = 0;
				int videoFound = -1;
				for (int i = 0; i < blobVideoNumber; i++) {
					if (rankMatch[indexWifi][i] > maxRank) {
						maxRank = rankMatch[indexWifi][i];
					}
				}
				if (maxRank > 0) {					
					double maxAccScoreMatch = 0;
					double minDistance = Integer.MAX_VALUE;
					for (int i = 0; i < blobVideoNumber; i++) {
						if (rankMatch[indexWifi][i] == maxRank) {
							double distance = getOverlapAvrDistance(slidingWindowTimeStep, indexWifi, i)[0];
							if (distance < minDistance) {
								minDistance = distance;
								videoFound = i;
							}
							/*if (accScoreMatch[indexWifi][i] > maxAccScoreMatch) {
								maxAccScoreMatch = accScoreMatch[indexWifi][i];
								videoFound = i;
							}*/	
						}
					}
					convergeVideoForID[indexWifi] = videoFound;	
					
				}
			}
		}				
		int[] result = getNonOverlapMatchVideo(slidingWindowTimeStep,convergeVideoForID, true);			
		for (int indexWifi = 0; indexWifi < blobWifiNumber; indexWifi++) {
			if (result[indexWifi] >= 0 && foundConvergeWifi[indexWifi] == false) {
				getMaxRankStatistic(indexWifi, result[indexWifi]);
				/*int waitingTime;
				if (experimentNumber - lastTimeStepRankMatchUpdateOnAWifi[indexWifi] <= Config.waitingTime) {
					waitingTime = experimentNumber - lastTimeStepRankMatchUpdateOnAWifi[indexWifi] - 1;
				}
				else {
					waitingTime = Config.waitingTime;
				}*/
				/*if (slidingWindowTimeStep - lastTimeStepRankMatchUpdateOnAWifi[indexWifi] >= Config.waitingTime) {
					wifiDisappear[indexWifi] = true;
				}*/
				//if (maxRankStatisticValue[indexWifi][result[indexWifi]][0] >= Config.maxRankThresholdSlidingWindow || 
						//(maxRankStatisticValue[indexWifi][result[indexWifi]][0] > 0 && slidingWindowTimeStep - lastTimeStepRankMatchUpdateOnAWifi[indexWifi] >= waitingTime)){
				if (maxRankStatisticValue[indexWifi][result[indexWifi]][0] >= Config.maxRankThresholdSlidingWindow) {
					convergeVideoFinalForWifiID[indexWifi] = result[indexWifi];
					foundConvergeWifi[indexWifi] = true;
					foundConvergeVideo[result[indexWifi]] = true;
					stopLoopingAvoidOverlap = false;
					convergeLatencyForWifiID[indexWifi] = slidingWindowTimeStep - departingTimeOfVideo[indexWifi];
				}
			}
		}	
		
	}
	
	public static void printResult() {
		int numberOfFoundConverge = 0;

		for (int i = 0; i < blobWifiNumber; i++) {
			if (foundConvergeWifi[i] == true) {
				numberOfFoundConverge++;
				
			}
		}
		BufferedWriter writer;
		try {				
			writer = new BufferedWriter(new FileWriter(fileOutputFolder + "OutputMatching.txt"));
			for (int i = 0; i < blobWifiNumber; i++) {
				if (foundConvergeWifi[i] == true) {
					numberOfFoundConverge++;
					writer.write(i + " " + convergeVideoFinalForWifiID[i]);
				}
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void matching() {
		for (int slidingWindow = 2; slidingWindow < experimentNumber; slidingWindow = slidingWindow + 1) {
			System.out.println(slidingWindow);
			stopLoopingAvoidOverlap = false;									
			//rankMatch = new int[blobWifiNumber][blobVideoNumber];
			eraseScoreMatch();
			countScoreMatch = new int[blobWifiNumber][blobVideoNumber];
			for (int indexWifi = 0; indexWifi < blobWifiNumber; indexWifi++) {
				if (foundConvergeWifi[indexWifi] == false && wifiDisappear[indexWifi] == false) {					
					/*for (int time = 1; time < slidingWindow; time++) {						
						movingMatchLeader(time,indexWifi);
					}*/
					movingMatchLeader(slidingWindow - 1,indexWifi);
				}
			}
			getRankMatchingResultAvoidOverlap(slidingWindow, 1);
		}
		
		printResult();
	}
	
	public static void calculateNumberOfBlobIn40Seconds() {
		int frameCount = 1200;
		int result = 0;
		Coordinate centerPoint = new Coordinate(5, 1);
		for (int i = 0; i < frameNumber; i++) {
			for (int blobID = 0; blobID < frameCount; blobID++) {
				if (blobPhysicalCoor[blobID][i] != null && distance(blobPhysicalCoor[blobID][i], centerPoint) < 1.5) {
					result++;
					break;
				}
			}
		}
		System.out.println(result);
	}
	
    public static void readAfterInterpolation() {
	    Scanner scan;	    
	    ParserWifiRTLS parser;
	    int ID = 0;	 
	    blobWifiNumber = 0;
	    peopleWifiAfterInterpolate = new timeWithLocationCoordinate[Config.maximumBlobCount][5000];
	    countTimeWifiReportAfterInterpolate = new int[Config.maximumWifiReportCount];
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
	        	if (ID > blobWifiNumber) {
	        		blobWifiNumber = ID;
	        	}
	        }
	        blobWifiNumber++;
	        scan.close();
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }

    }
	
	public static void main( String[ ] args ) {
		map = new MapRealLevel6();
		initializeTheArray();
		readVideoFile();
		convertToPhysicalCoordinate();
		readAfterInterpolation();
		//calculateNumberOfBlobIn40Seconds();
		//Algorithm
		//initializeForTheRun();	
		matching();	
	}
}
