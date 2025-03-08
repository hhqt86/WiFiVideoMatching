package SuntecRealData.Suntec;



import java.io.BufferedWriter;
import java.io.File;


import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Scanner;








public class SuntecMatching {
	
	//constant level and camera
	static int numberOfLevel = 7;
	static int numberOfCamera = 20;
	static int numberOfFile = 34;
	static int[] thisFileLevel =        {6 ,6 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,6 ,6 ,6 ,6 ,6 ,6 ,6 ,6 ,6 ,6 ,6 ,6 ,6 };
	static int[] thisFileCamera =       {4 ,7 ,1 ,1 ,2 ,2 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,4 ,4};
	static int[] thisVideoStartHour =   {19,19,19,19,19,19,19,19,18,18,18,18,18,18,19,19,19,19,19,19,19,18,18,18,18,18,18,18,19,19,19,19,19,19};
	static int[] thisVideoStartMinute = {27,28,24,33,25,34,28,28,10,18,27,36,44,53,2 ,10,19,28,37,45,54,0 ,9 ,18,27,44,35,53,1 ,10,54,19,36,45};
	static int[] thisVideoStartSecond = {58,0 ,53,39,44,27,27,25,3 ,44,25,6 ,48,30,14,57,41,25,9 ,54,39,54,36,17,3 ,26,47,6 ,47,30,9 ,14,39,23};


	//Wifi and Video 
	//static int startingExaminedWifi = 348;
	static int startingExaminedWifi = 0;
	static int numberOfWifi = 0;
	static int numberOfVideo = 0;
	static int startWifiTimeStep = Integer.MAX_VALUE;
	static int endWifiTimeStep = 0;
	static String wifiFolder = "SuntecRealData/RTLS_Comex2017_Sep_01_Study3/"; //remove src when remote
	static String videoFolder = "SuntecRealData/VideoProcessResultFull2Hour/";//remove src when remote
	static String fileOutputFolder = "SuntecRealData/OutputResultNew40/OutputMatching40TraceWithTimeMatch/";//remove src when remote
	//static String fileOutputFolder = "SuntecRealData/OutputResultFull2Hour/";
	//static String fileAfterInterpolate = "afterInterpolation.csv";
	static String fileAfterInterpolate = "afterInterpolationNew40.csv";
	

	static NormalLocationCoordinate[][] peopleWifi;
	static int[] peopleWifiStartTime;
	static int[] peopleWifiEndTime;
	static NormalLocationCoordinate[][] peopleVideo;
	static int[] peopleVideoStartTime;
	static int[] peopleVideoEndTime;

	static DecimalFormat df2 = new DecimalFormat("#.##");
	
	static double distanceInSameLevel(Coordinate point1, Coordinate point2) {
		return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
	}
	
	static void initializeTheArray() {
		peopleWifiStartTime = new int[Config.maximumWifiReportCount];
		peopleWifiEndTime = new int[Config.maximumWifiReportCount];
		peopleVideoStartTime = new int[Config.maximumBlobVideoCount];
		peopleVideoEndTime = new int[Config.maximumBlobVideoCount];
		for (int i = 0; i < Config.maximumWifiReportCount; i++) {
			peopleWifiStartTime[i] = Integer.MAX_VALUE;
			peopleWifiEndTime[i] = 0;
 		}
		for (int i = 0; i < Config.maximumBlobVideoCount; i++) {
			peopleVideoStartTime[i] = Integer.MAX_VALUE;
			peopleVideoEndTime[i] = 0;
 		}


	}
	
	
	
	static void readVideoFile(int fileIndex) {		
	    Scanner scan;	    	    
	    int ID = 0;	 
	    numberOfVideo = 0;
	    peopleVideo = new NormalLocationCoordinate[Config.maximumBlobVideoCount][10000];
	    try {
	    	File file = new File(videoFolder +  "VideoAfterProcess" + fileIndex + ".csv");
	        scan = new Scanner(file);	        	        
	        while (scan.hasNextLine()) {
	        	String data = scan.nextLine();
	        	if (Integer.parseInt(data.charAt(0)+"") == thisFileLevel[fileIndex] && Integer.parseInt(data.charAt(1)+"") == thisFileCamera[fileIndex]) {
	        		String[] parseData = parseData(data);
		        	ID = Integer.parseInt(parseData[0].substring(2, parseData[0].length()));
		        	int time = Integer.parseInt(parseData[1]) % 10000;
		        	if (time < peopleVideoStartTime[ID]) {
		        		peopleVideoStartTime[ID] = time;
		        	}
		        	if (time > peopleVideoEndTime[ID]) {
		        		peopleVideoEndTime[ID] = time;
		        	}

		        	int level = Integer.parseInt(parseData[2]);
		        	Coordinate location = new Coordinate(Double.parseDouble(parseData[3]),Double.parseDouble(parseData[4]));
		        	peopleVideo[ID][time] = new NormalLocationCoordinate(level, location);
		        	if (ID > numberOfVideo) {
		        		numberOfVideo = ID;
		        	}
	        	}
	        }
	        numberOfVideo++;
	        scan.close();
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }
	}
	

	
	//For matching algorithm


	//For algorithm
	static int timeStepRankMatch[][][];
	static boolean wifiAppearInTheArea[];
	//static boolean[][] isVideoAwakeAtTimeStep; 
	static boolean[] foundConvergeWifi;
	static boolean[] foundConvergeVideo;
	static int[] convergeLatencyForWifiID;
	static int[] convergeVideoFinalForWifiID;
	static boolean[] wifiDisappear;
	static int rankMatch[][];
	static double accScoreMatch[][];
	static double maxAccScoreMatch[][];
	static int timeStepMatchBasedOnMaxAccScoreMatch[][];
	static double minDistanceAtTimeMatch[][];
	static int timeStepMatchBasedOnMinDistanceMatch[][];
	
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
		rankMatch = new int[numberOfWifi][numberOfVideo];
		scoreMatch = new double[numberOfWifi][numberOfVideo];
		accScoreMatch = new double[numberOfWifi][numberOfVideo];
		maxAccScoreMatch = new double[numberOfWifi][numberOfVideo];
		timeStepMatchBasedOnMaxAccScoreMatch = new int[numberOfWifi][numberOfVideo];
		minDistanceAtTimeMatch = new double[numberOfWifi][numberOfVideo];
		timeStepMatchBasedOnMinDistanceMatch = new int[numberOfWifi][numberOfVideo];

		maxRankStatisticValue = new int[numberOfWifi][numberOfVideo][5];
		maxRankStatisticFrequency = new int[numberOfWifi][numberOfVideo][5];
		countScoreMatch = new int[numberOfWifi][numberOfVideo];
		lastTimeStepRankMatchUpdateOnAWifi = new int[numberOfWifi];
		foundConvergeWifi = new boolean[numberOfWifi];
		foundConvergeVideo = new boolean[numberOfVideo];
		convergeLatencyForWifiID = new int[numberOfWifi];
		convergeVideoFinalForWifiID = new int[numberOfWifi];
		timeStepRankMatch = new int[numberOfWifi][numberOfVideo][100];
		wifiDisappear = new boolean[numberOfWifi];
				
		for (int i = 0; i < numberOfWifi; i++) {			 	 
			 foundConvergeWifi[i] = false;			 
			 convergeLatencyForWifiID[i] = -1;
			 convergeVideoFinalForWifiID[i] = -1;
			 wifiDisappear[i] = false;
			 for (int j = 0; j < numberOfVideo; j++) {
				 minDistanceAtTimeMatch[i][j] = Integer.MAX_VALUE;
				 scoreMatch[i][j] = -100;
				 
			 }			 
		}	
		for (int i = 0; i < numberOfVideo; i++) {
			foundConvergeVideo[i] = false;
		}
		departingTimeOfVideo = new int[numberOfVideo];
		for (int i = 0; i < numberOfVideo; i++) {
			departingTimeOfVideo[i] = -1;
		}
	}
	
	static void eraseScoreMatch() {
		for (int i = 0; i < numberOfWifi; i++) {
			for (int j = 0; j < numberOfVideo; j++) {				
				scoreMatch[i][j] = -100;				
			}
		}		
	}
	
	public static boolean similarLevel(NormalLocationCoordinate c1, NormalLocationCoordinate c2) {
		if (c1 == null || c2 == null || c1.level == Config.noPoint.level || c2.level == Config.noPoint.level 
				|| c1.level != c2.level) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public static boolean isNoPoint (NormalLocationCoordinate video) {
		if (video.level == Config.noPoint.level) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static double getScoreAverageDistance(NormalLocationCoordinate[] video, NormalLocationCoordinate[] wifi) {
		double averageDistance;	
		// 1 - (x^2) / (WifiError^2)
		//check video
		int numberOfNoPoint = 0;
		for (int i = 0; i <= Config.historyWindowLength; i++) {
			if (isNoPoint(video[i])) {
				numberOfNoPoint++;
			}
		}
		if (numberOfNoPoint >= 0.75 * Config.historyWindowLength) {
			return 0;
		}
		//check wifi
		numberOfNoPoint = 0;
		for (int i = 0; i <= Config.historyWindowLength; i++) {
			if (isNoPoint(wifi[i])) {
				numberOfNoPoint++;
			}
		}
		if (numberOfNoPoint >= 0.75 * Config.historyWindowLength) {
			return 0;
		}
		//end checking
		averageDistance = 0;
		int averageCount = 0;
		for (int i = 0; i <= Config.historyWindowLength; i++) {
			if (!isNoPoint(wifi[i]) && !isNoPoint(video[i])) {
				if (similarLevel(wifi[i],video[i])) {
					averageDistance += distanceInSameLevel(wifi[i].locationCoor,video[i].locationCoor);
				}
				else {
					averageDistance += 35; //equal to the length of a floor
				}
				averageCount++;
			}
		}
		if (averageCount >= 0.5 * Config.historyWindowLength) {
			averageDistance = averageDistance / averageCount;	
			double pivot = 6;
			if (averageDistance < pivot) {									
				return (1 - (averageDistance * averageDistance) / (pivot * pivot));
			}
			else {
				return 0;
			}
		}
		else {
			return 0;
		}
	}
	
	static double matchScore(NormalLocationCoordinate[] video, NormalLocationCoordinate wifi[]) {
		
		double scoreAverageDistance;
		scoreAverageDistance = getScoreAverageDistance(wifi,video);								
		return scoreAverageDistance;
	}
	
	static int[] findVideoLeader(int indexWifi, int timeStep, boolean[] isVideoAwake) {
		int[] result;
		int save = -1;
		double max = -1;
		double[] avr = new double[numberOfVideo];
		for (int indexVideo = 0; indexVideo < numberOfVideo; indexVideo++) {
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
		if (max > 0.4) {//Fix here from max > 0 -> max > 0.4 to assure the good match
			int count = 0;
			for (int indexVideo = 0; indexVideo < numberOfVideo; indexVideo++) {
				if (avr[indexVideo] <= max + Config.rangeScoreConsiderAsLeaders && avr[indexVideo] >= max - Config.rangeScoreConsiderAsLeaders) {
					count++;
				}
			}
			result = new int[count];
			count = 0;
			for (int indexVideo = 0; indexVideo < numberOfVideo; indexVideo++) {
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
		if (beginTimeOfHistory - peopleWifiStartTime[indexWifi] >= Config.historyWindowLength) {
			NormalLocationCoordinate[] wifiCompare = new NormalLocationCoordinate[Config.historyWindowLength + 1];
			NormalLocationCoordinate[] videoCompare = new NormalLocationCoordinate[Config.historyWindowLength + 1];		
			boolean[] isVideoAvailable = new boolean[numberOfVideo];
			if (foundConvergeWifi[indexWifi] == false && beginTimeOfHistory >= Config.historyWindowLength) {
				for (int i = 0; i <= Config.historyWindowLength; i++) {		
					if (peopleWifi[indexWifi][beginTimeOfHistory - i] != null) {
						wifiCompare[i] = new NormalLocationCoordinate(peopleWifi[indexWifi][beginTimeOfHistory - i].level, 
																	  new Coordinate(peopleWifi[indexWifi][beginTimeOfHistory - i].locationCoor));
					}
					else {
						wifiCompare[i] = Config.noPoint;
					}
				}
	
				for (int indexVideo = 0; indexVideo < numberOfVideo; indexVideo++) {
					if (indexVideo == 2317) {
						indexVideo = 2317;
					}
					if (foundConvergeVideo[indexVideo] == false) {
						videoCompare = new  NormalLocationCoordinate[Config.historyWindowLength + 1];
						int count = 0;
						int historyTime = beginTimeOfHistory;
						while (count <= Config.historyWindowLength) {						
							if (peopleVideo[indexVideo][historyTime] != null) {
								videoCompare[count] = new NormalLocationCoordinate(peopleVideo[indexVideo][historyTime].level,
																				   new Coordinate(peopleVideo[indexVideo][historyTime].locationCoor));								
								historyTime--;							
							}
							else {								
									videoCompare[count] = Config.noPoint;
									historyTime--;
							}
							count++;						
						}
						isVideoAvailable[indexVideo] = true;
						
						double matchResult = getScoreAverageDistance(videoCompare,wifiCompare);
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
						timeStepRankMatch[indexWifi][videoLeader[i]][rankMatch[indexWifi][videoLeader[i]]] = beginTimeOfHistory;
					}
				}
			}
		}
	}
	
	static void movingMatchLeaderTakeTimeStepMatchAtMaxAccScore(int beginTimeOfHistory, int indexWifi) {			
		if (beginTimeOfHistory - peopleWifiStartTime[indexWifi] >= Config.historyWindowLength) {
			NormalLocationCoordinate[] wifiCompare = new NormalLocationCoordinate[Config.historyWindowLength + 1];
			NormalLocationCoordinate[] videoCompare = new NormalLocationCoordinate[Config.historyWindowLength + 1];		
			boolean[] isVideoAvailable = new boolean[numberOfVideo];
			if (foundConvergeWifi[indexWifi] == false && beginTimeOfHistory >= Config.historyWindowLength) {
				for (int i = 0; i <= Config.historyWindowLength; i++) {		
					if (peopleWifi[indexWifi][beginTimeOfHistory - i] != null) {
						wifiCompare[i] = new NormalLocationCoordinate(peopleWifi[indexWifi][beginTimeOfHistory - i].level, 
																	  new Coordinate(peopleWifi[indexWifi][beginTimeOfHistory - i].locationCoor));
					}
					else {
						wifiCompare[i] = Config.noPoint;
					}
				}
	
				for (int indexVideo = 0; indexVideo < numberOfVideo; indexVideo++) {
					if (foundConvergeVideo[indexVideo] == false) {
						videoCompare = new  NormalLocationCoordinate[Config.historyWindowLength + 1];
						int count = 0;
						int historyTime = beginTimeOfHistory;
						while (count <= Config.historyWindowLength) {						
							if (peopleVideo[indexVideo][historyTime] != null) {
								videoCompare[count] = new NormalLocationCoordinate(peopleVideo[indexVideo][historyTime].level,
																				   new Coordinate(peopleVideo[indexVideo][historyTime].locationCoor));								
								historyTime--;							
							}
							else {								
									videoCompare[count] = Config.noPoint;
									historyTime--;
							}
							count++;						
						}
						isVideoAvailable[indexVideo] = true;

						double matchResult = getScoreAverageDistance(videoCompare,wifiCompare);
						scoreMatch[indexWifi][indexVideo] = matchResult;
						countScoreMatch[indexWifi][indexVideo]++;						
						accScoreMatch[indexWifi][indexVideo] = (accScoreMatch[indexWifi][indexVideo] * (countScoreMatch[indexWifi][indexVideo] - 1) + scoreMatch[indexWifi][indexVideo])
								   / countScoreMatch[indexWifi][indexVideo];
						if (accScoreMatch[indexWifi][indexVideo] > maxAccScoreMatch[indexWifi][indexVideo]) {
							maxAccScoreMatch[indexWifi][indexVideo] = accScoreMatch[indexWifi][indexVideo];
							timeStepMatchBasedOnMaxAccScoreMatch[indexWifi][indexVideo] = beginTimeOfHistory;
						}
					}
				}
				int[] videoLeader = findVideoLeader(indexWifi, beginTimeOfHistory, isVideoAvailable); 
				if (videoLeader.length > 0) {
					for (int i = 0; i < videoLeader.length; i++) {
						rankMatch[indexWifi][videoLeader[i]]++;
						lastTimeStepRankMatchUpdateOnAWifi[indexWifi] = beginTimeOfHistory;
						timeStepRankMatch[indexWifi][videoLeader[i]][rankMatch[indexWifi][videoLeader[i]]] = beginTimeOfHistory;
					}
				}
			}
		}
	}

	static void movingMatchLeaderTakeTimeStepMatchAtMinDistance(int beginTimeOfHistory, int indexWifi) {			
		if (beginTimeOfHistory - peopleWifiStartTime[indexWifi] >= Config.historyWindowLength) {
			NormalLocationCoordinate[] wifiCompare = new NormalLocationCoordinate[Config.historyWindowLength + 1];
			NormalLocationCoordinate[] videoCompare = new NormalLocationCoordinate[Config.historyWindowLength + 1];		
			boolean[] isVideoAvailable = new boolean[numberOfVideo];
			if (foundConvergeWifi[indexWifi] == false && beginTimeOfHistory >= Config.historyWindowLength) {
				for (int i = 0; i <= Config.historyWindowLength; i++) {		
					if (peopleWifi[indexWifi][beginTimeOfHistory - i] != null) {
						wifiCompare[i] = new NormalLocationCoordinate(peopleWifi[indexWifi][beginTimeOfHistory - i].level, 
																	  new Coordinate(peopleWifi[indexWifi][beginTimeOfHistory - i].locationCoor));
					}
					else {
						wifiCompare[i] = Config.noPoint;
					}
				}
	
				for (int indexVideo = 0; indexVideo < numberOfVideo; indexVideo++) {
					if (foundConvergeVideo[indexVideo] == false) {
						videoCompare = new  NormalLocationCoordinate[Config.historyWindowLength + 1];
						int count = 0;
						int historyTime = beginTimeOfHistory;
						while (count <= Config.historyWindowLength) {						
							if (peopleVideo[indexVideo][historyTime] != null) {
								videoCompare[count] = new NormalLocationCoordinate(peopleVideo[indexVideo][historyTime].level,
																				   new Coordinate(peopleVideo[indexVideo][historyTime].locationCoor));								
								historyTime--;							
							}
							else {								
									videoCompare[count] = Config.noPoint;
									historyTime--;
							}
							count++;						
						}
						isVideoAvailable[indexVideo] = true;
						
						double matchResult = getScoreAverageDistance(videoCompare,wifiCompare);
						scoreMatch[indexWifi][indexVideo] = matchResult;
						countScoreMatch[indexWifi][indexVideo]++;						
						accScoreMatch[indexWifi][indexVideo] = (accScoreMatch[indexWifi][indexVideo] * (countScoreMatch[indexWifi][indexVideo] - 1) + scoreMatch[indexWifi][indexVideo])
								   / countScoreMatch[indexWifi][indexVideo];
						
						if (peopleWifi[indexWifi][beginTimeOfHistory] != null && peopleVideo[indexVideo][beginTimeOfHistory]!= null && peopleWifi[indexWifi][beginTimeOfHistory].level == peopleVideo[indexVideo][beginTimeOfHistory].level &&
								distanceInSameLevel(peopleWifi[indexWifi][beginTimeOfHistory].locationCoor, peopleVideo[indexVideo][beginTimeOfHistory].locationCoor) < minDistanceAtTimeMatch[indexWifi][indexVideo]) {
							minDistanceAtTimeMatch[indexWifi][indexVideo] = distanceInSameLevel(peopleWifi[indexWifi][beginTimeOfHistory].locationCoor, peopleVideo[indexVideo][beginTimeOfHistory].locationCoor);
							timeStepMatchBasedOnMinDistanceMatch[indexWifi][indexVideo] = beginTimeOfHistory;
						}
					}
				}
				int[] videoLeader = findVideoLeader(indexWifi, beginTimeOfHistory, isVideoAvailable); 
				if (videoLeader.length > 0) {
					for (int i = 0; i < videoLeader.length; i++) {
						rankMatch[indexWifi][videoLeader[i]]++;
						lastTimeStepRankMatchUpdateOnAWifi[indexWifi] = beginTimeOfHistory;
						timeStepRankMatch[indexWifi][videoLeader[i]][rankMatch[indexWifi][videoLeader[i]]] = beginTimeOfHistory;
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
			if (similarLevel(peopleWifi[wifi][i], peopleVideo[video][i])) {			
				sum += distanceInSameLevel(peopleWifi[wifi][i].locationCoor, peopleVideo[video][i].locationCoor);
				count++;
			}
		}
		result[0] = sum / count;
		int countWifiAppearInCaseVideoAppear = 0;
		int countVideoAppear = 0;
		int countWifiAppear = 0;
		for (int i = 0; i < limitExperimentTimeStep; i++) {
			if (peopleVideo[video][i] != null) {
				countVideoAppear++;
				/*if (peopleWifi[wifi][i] != -1) {
					countWifiAppearInCaseVideoAppear++;
				}*/
			}
			if (peopleWifi[wifi][i] != null) {
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
			if (peopleWifi[wifi][j] != null || peopleVideo[video][j] != null) {
				appearCount++;
			}
			if (peopleWifi[wifi][j] != null && peopleVideo[video][j] != null) {
				coAppear++;;
			}
		}
		double ratio = (coAppear * 1.0 / appearCount);
		return ratio;
	}
	
	static int[] getNonOverlapMatchVideo(int limitExperimentTimeStep, int[] convergeVideoForID, boolean requireRatioAppearance) {
		int[] result = new int[numberOfWifi];
		boolean[] checkVideo = new boolean[numberOfVideo];
		int[] duplicateMatchVideo = new int[numberOfVideo];
		for (int i = 0; i < numberOfVideo; i++) {
			checkVideo[i] = false;			
		}
		for (int i = 0; i < numberOfWifi; i++) {
			result[i] = -1;
		}
		
		for (int indexWifi = 0; indexWifi < numberOfWifi; indexWifi++) {
			if (convergeVideoForID[indexWifi] >= 0) {
				duplicateMatchVideo[convergeVideoForID[indexWifi]]++;
			}
		}
		for (int indexWifi = 0; indexWifi < numberOfWifi; indexWifi++) {
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
						for (int wifi = 0; wifi < numberOfWifi; wifi++) {
							if (convergeVideoForID[wifi] == convergeVideoForID[indexWifi]) {
								double[] distanceResult = getOverlapAvrDistance(limitExperimentTimeStep, wifi, convergeVideoForID[wifi]);
								double avrDistance = distanceResult[0];								
								if (accScoreMatch[wifi][convergeVideoForID[wifi]] > maxAccScoreMatch && existRT(limitExperimentTimeStep, wifi, convergeVideoForID[wifi]) >= Config.rtSimulator) {
									maxAccScoreMatch = accScoreMatch[wifi][convergeVideoForID[wifi]];
									save = wifi;
								}	
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
		int[] maxRankOfWifi = new int[numberOfWifi];
		for (int indexVideo = 0; indexVideo < numberOfVideo; indexVideo++) {
			maxRankOfWifi[indexVideo] = rankMatch[wifi][indexVideo];
		}
		int[] frequency = new int[4];
		int[] maxRankValue = new int[4];
		for (int i = 0; i < 4; i++) {
			int max = 0;
			for (int k = 0; k < numberOfWifi; k++) {
				if (maxRankOfWifi[k] > max && (i == 0 || maxRankOfWifi[k] < maxRankValue[i - 1])) {
					max = maxRankOfWifi[k];
				}
			}
			maxRankValue[i] = max;
		}
		for (int i = 0; i < 4; i++) {
			int freq = 0;
			for (int k = 0; k < numberOfWifi; k++) {
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
		int[] convergeVideoForID = new int[numberOfWifi];		
		for (int indexWifi = 0; indexWifi < numberOfWifi; indexWifi++) {
			convergeVideoForID[indexWifi] = -1;
			if (foundConvergeWifi[indexWifi] == false) {
				int maxRank = 0;
				int videoFound = -1;
				for (int i = 0; i < numberOfVideo; i++) {
					if (rankMatch[indexWifi][i] > maxRank) {
						maxRank = rankMatch[indexWifi][i];
					}
				}
				if (maxRank > 0) {					
					double maxAccScoreMatch = 0;
					double minDistance = Integer.MAX_VALUE;
					for (int i = 0; i < numberOfVideo; i++) {
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
		for (int indexWifi = 0; indexWifi < numberOfWifi; indexWifi++) {
			if (result[indexWifi] >= 0 && foundConvergeWifi[indexWifi] == false) {
				//getMaxRankStatistic(indexWifi, result[indexWifi]);
				//if (maxRankStatisticValue[indexWifi][result[indexWifi]][0] >= Config.maxRankThresholdSlidingWindow) {
				if (rankMatch[indexWifi][result[indexWifi]] >= Config.maxRankThresholdSlidingWindow) {
					convergeVideoFinalForWifiID[indexWifi] = result[indexWifi];
					foundConvergeWifi[indexWifi] = true;
					foundConvergeVideo[result[indexWifi]] = true;
					stopLoopingAvoidOverlap = false;
					convergeLatencyForWifiID[indexWifi] = slidingWindowTimeStep - departingTimeOfVideo[indexWifi];
				}
			}
		}	
		
	}
	
	public static void printResult(int fileIndex) {
		int numberOfFoundConverge = 0;

		for (int i = 0; i < numberOfWifi; i++) {
			if (foundConvergeWifi[i] == true) {
				numberOfFoundConverge++;
				
			}
		}
		BufferedWriter writer;
		try {				
			writer = new BufferedWriter(new FileWriter(fileOutputFolder + "OutputMatching" + fileIndex + ".txt"));
			for (int i = 0; i < numberOfWifi; i++) {
				if (foundConvergeWifi[i] == true) {
					numberOfFoundConverge++;
					writer.write(i + " " + convergeVideoFinalForWifiID[i] + "\n");
				}
			}
			System.out.println(numberOfFoundConverge);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void findCandidateForEachWifi(int fileIndex) {
		int[][] order = new int[numberOfWifi][numberOfVideo];
		for (int i = 0; i < numberOfWifi; i++) {
			for (int j = 0; j < numberOfVideo; j++) {
				order[i][j] = j;
			}
		}
		for (int indexWifi = 0; indexWifi < numberOfWifi; indexWifi++) {
			for (int i = 0; i < numberOfVideo - 1; i++) {
				for (int j = i + 1; j < numberOfVideo; j++) {
					if (rankMatch[indexWifi][i] < rankMatch[indexWifi][j]) {
						int tmp = rankMatch[indexWifi][i];
						rankMatch[indexWifi][i] = rankMatch[indexWifi][j];
						rankMatch[indexWifi][j] = tmp;
						tmp = order[indexWifi][i];
						order[indexWifi][i] = order[indexWifi][j];
						order[indexWifi][j] = tmp;
					}
				}
			}
		}
		
		BufferedWriter writer;
		try {				
			writer = new BufferedWriter(new FileWriter(fileOutputFolder + "OutputMatching" + fileIndex + ".txt"));
			writer.write("Wifi matchBlob1 level1 X1 Y1 rank1 time1 matchBlob2 level2 X2 Y2 rank2 time2 matchBlob3 level3 X3 Y3 rank3 time3 \n");
			for (int i = 0; i < numberOfWifi; i++) {
				if (i == 14) {
					i = 14;
				}
				if (rankMatch[i][0] >= Config.maxRankThresholdForMatching) {
					writer.write(i + " ");
					for (int j = 0; j < 3; j++) {
						int matchBlob = order[i][j];
						int rank = rankMatch[i][j];
						if (rank >= Config.maxRankThresholdForMatching) {
														
							//int timeStepMatch = findNearestTimeStepNotNull(matchBlob, timeStepRankMatch[i][order[i][j]][rankMatch[i][j]]);
							int timeStepMatch = findNearestTimeStepNotNull(matchBlob,timeStepMatchBasedOnMaxAccScoreMatch[i][matchBlob]);
							int timeStepMatchFound = timeStepRankMatch[i][matchBlob][rank];
							//int timeStepMatch = findNearestTimeStepNotNull(matchBlob,timeStepMatchBasedOnMinDistanceMatch[i][matchBlob]);
							int level = peopleVideo[matchBlob][timeStepMatch].level;
							String coorX = df2.format(peopleVideo[matchBlob][timeStepMatch].locationCoor.x);
							String coorY = df2.format(peopleVideo[matchBlob][timeStepMatch].locationCoor.y);
							writer.write(matchBlob + " " + level + " " + coorX + " " + coorY + " " + rank + " " + timeStepMatch + " " + timeStepMatchFound + " ");
						}
					}
					writer.write("\n");
				}
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
	}
	
	public static int findNearestTimeStepNotNull(int videoBlob, int timestep) {		
		System.out.println(videoBlob + " " + timestep);
		if (peopleVideo[videoBlob][timestep] != null) {
			return timestep;
		}
		else {
			int space = 0;
			do {
				space++;
				if (peopleVideo[videoBlob][timestep - space] != null) {
					return (timestep - space);
				}
				if (peopleVideo[videoBlob][timestep + space] != null) {
					return (timestep + space);
				}
			}
			while(true);
		}
	}
	
	static void matching(int fileIndex) {		
		System.out.println(startWifiTimeStep);
		System.out.println(endWifiTimeStep);
		for (int slidingWindow = startWifiTimeStep + 1; slidingWindow <= endWifiTimeStep; slidingWindow = slidingWindow + 4) {
			System.out.println(slidingWindow);
			if (slidingWindow == 5355) {
				slidingWindow = 5355;
			}
			stopLoopingAvoidOverlap = false;									
			eraseScoreMatch();
			countScoreMatch = new int[numberOfWifi][numberOfVideo];
			for (int indexWifi = startingExaminedWifi; indexWifi < numberOfWifi; indexWifi++) {
				if (foundConvergeWifi[indexWifi] == false && wifiDisappear[indexWifi] == false) {					
					//movingMatchLeader(slidingWindow - 1,indexWifi);
					movingMatchLeaderTakeTimeStepMatchAtMaxAccScore(slidingWindow - 1,indexWifi);
				}				
			}
			
			//getRankMatchingResultAvoidOverlap(slidingWindow, 1); old version
		}		
		//No finding match for each step, but run until the end to select the top rank match for each wifi
		findCandidateForEachWifi(fileIndex);
	}
	
	public static String[] parseData(String data) {
		int index = 0;
		String[] result = new String[5];
		String component = "";
		int i = 0;
		while (i < data.length()) {
			if (data.charAt(i) == ',') {
				result[index] = component;
				index++;
				component = "";
			}
			else {
				component = component + data.charAt(i);
			}
			i++;
		}
		result[index] = component;
		return result;
	}
	
    public static void readAfterInterpolation() {
	    Scanner scan;	    
	    
	    int ID = 0;	 
	    numberOfWifi = 0;
	    peopleWifi = new NormalLocationCoordinate[Config.maximumWifiReportCount][10000];
	    try {
	    	File file = new File(wifiFolder +  fileAfterInterpolate);
	    	//File file = new File(wifiFolder +  "afterInterpolation.csv");
	        scan = new Scanner(file);
	        int count = 0;
	        while (scan.hasNextLine()) {
	        	String data = scan.nextLine();
	        	count++;
	        	if (count % 10000 == 0) {
	        		System.out.println(count);
	        	}
	        	String[] parseData = parseData(data);
	    
	        	ID = Integer.parseInt(parseData[0]);
	        	int time = Integer.parseInt(parseData[1]) % 10000;
	        	if (time < startWifiTimeStep) {
	        		startWifiTimeStep = time;
	        	}
	        	if (time > endWifiTimeStep) {
	        		endWifiTimeStep = time;
	        	}
	        	if (time < peopleWifiStartTime[ID]) {
	        		peopleWifiStartTime[ID] = time;
	        	}
	        	if (time > peopleWifiEndTime[ID]) {
	        		peopleWifiEndTime[ID] = time;
	        	}
	        	int level = Integer.parseInt(parseData[2]);
	        	Coordinate location = new Coordinate(Double.parseDouble(parseData[3]),Double.parseDouble(parseData[4]));
	        	peopleWifi[ID][time] = new NormalLocationCoordinate(level, location);
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
	
	static int getTimeStamp (int hour, int minute, int second) {
		int result = 0;
		int beginTimeOfTheDay = 1504195200;
		result = beginTimeOfTheDay + hour * 3600 + minute * 60 + second;
		return result;
	}
    
	public static int getTimeStampVideoAtFrame(int fileIndex, int frame) {
		int videoStartHour = thisVideoStartHour[fileIndex];
		int videoStartMinute = thisVideoStartMinute[fileIndex];
		int videoStartSecond = thisVideoStartSecond[fileIndex]; 
		int startTimeStamp = getTimeStamp(videoStartHour, videoStartMinute, videoStartSecond);//time stamp of the beginning of video
		return (startTimeStamp + frame / Config.videoFPS) % 10000; // video is 25 fps and only take the last 4 numbers of the timeStamp
		
	}
	
	public static int getFrameVideoAtTimeStamp(int fileIndex, int timeStamp) {
		int videoStartHour = thisVideoStartHour[fileIndex];
		int videoStartMinute = thisVideoStartMinute[fileIndex];
		int videoStartSecond = thisVideoStartSecond[fileIndex]; 
		int startTimeStamp = getTimeStamp(videoStartHour, videoStartMinute, videoStartSecond) % 10000;//time stamp of the beginning of video
		return ((timeStamp - startTimeStamp) * Config.videoFPS);
	}
    

	
	public static void main( String[ ] args ) {	
		initializeTheArray();
		readAfterInterpolation();	
		for (int fileIndex = 0; fileIndex < numberOfFile - 1; fileIndex++) {		
			System.out.println("------------- file index: " + fileIndex);
			readVideoFile(fileIndex);		
			initializeForTheRun();
			matching(fileIndex);			
		}
	}
}

