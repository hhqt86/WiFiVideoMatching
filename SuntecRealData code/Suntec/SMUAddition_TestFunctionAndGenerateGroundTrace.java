package SuntecRealData.Suntec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Scanner;

public class SMUAddition_TestFunctionAndGenerateGroundTrace {
	static SMUAddition_MapReal[] map;
	static String folderOutputMatching = "src/SuntecRealData/OutputResultNew40/OutputMatching40Trace/"; //insert "src/ when run at local
	static String folderOutputMatchingWithTimeMatch = "src/SuntecRealData/OutputResultNew40/OutputMatching40TraceWithTimeMatch/"; //insert "src/ when run at local
	//static String folderOutputMatching = "src/SuntecRealData/OutputResult/OutputMatchingDefault/";	
	static String wifiFolder = "src/SuntecRealData/SMUAdditional/"; //remove src when remote
	
	//For evaluate the traced video, and generateWifi
	static int numberOfTraceVideo = 20;
	static NormalLocationCoordinate[][] peopleWifi;
	static NormalLocationCoordinate[][]  tracePeopleVideo;
	static int[] peopleWifiStartTime = new int[Config.maximumWifiReportCount];
	static int[] peopleWifiEndTime = new int[Config.maximumWifiReportCount];
	static boolean isTracePeopleVideoAtEscalator[][];
	static RankTimeWithLocationCoordinate[][][] traceMatchBlob = new RankTimeWithLocationCoordinate[numberOfTraceVideo][SuntecMatching.numberOfFile][3];
	static timeWithLocationCoordinate[][]  tracePeopleWifiGenerate;
	static int countPeopleWifiGenerate[];
	static NormalLocationCoordinate[]  addedPeopleWifi;
	static int traceVideoStartTime[] = new int[numberOfTraceVideo];
	static int traceVideoEndTime[] = new int[numberOfTraceVideo];;
	static int beginWifiIndexOfTracing = 0;
	//-------------------------------------------------
	static DecimalFormat df2 = new DecimalFormat("#.##");
	//For evaluate error of original Wifi
	static int numberOfCoveredArea = 4;
	static boolean isTraceVideoAppearAtTheArea[][] = new boolean[numberOfTraceVideo][numberOfCoveredArea];
	static timeWithLocationCoordinate[][] peopleWifiOriginal = new timeWithLocationCoordinate[500][1000];
	static int[] countTimeWifiOriginal = new int[500];
	static int numberOfWifi;
	//------------------------------------------------------
	static int getTimeStamp (int hour, int minute, int second) {
		int result = 0;
		int beginTimeOfTheDay = 1504195200;
		result = beginTimeOfTheDay + hour * 3600 + minute * 60 + second;
		return result;
	}
	
    
	public static int getTimeStampVideo(int fileIndex, int frame) {
		int videoStartHour = SuntecMatching.thisVideoStartHour[fileIndex];
		int videoStartMinute = SuntecMatching.thisVideoStartMinute[fileIndex];
		int videoStartSecond = SuntecMatching.thisVideoStartSecond[fileIndex]; 
		int startTimeStamp = getTimeStamp(videoStartHour, videoStartMinute, videoStartSecond);//time stamp of the beginning of video
		
		return (startTimeStamp + frame / Config.videoFPS) % 10000; // video is 25 fps and only take the last 4 numbers of the timeStamp
		
	}

	
	public static int getFrameVideoAtTimeStamp(int fileIndex, int timeStamp) {
		int videoStartHour = SuntecMatching.thisVideoStartHour[fileIndex];
		int videoStartMinute = SuntecMatching.thisVideoStartMinute[fileIndex];
		int videoStartSecond = SuntecMatching.thisVideoStartSecond[fileIndex]; 
		int startTimeStamp = getTimeStamp(videoStartHour, videoStartMinute, videoStartSecond) % 10000;//time stamp of the beginning of video
		return ((timeStamp - startTimeStamp) * Config.videoFPS);
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
	
   

    
	
    public static Coordinate findTheNearestAP(NormalLocationCoordinate video) {
    	int level = 1;
    	double videoX = video.locationCoor.x;
    	double videoY = video.locationCoor.y;
    	double minDistance = Integer.MAX_VALUE;
    	Coordinate saveAPCoor = new Coordinate(0,0);
    	for (int i = 0; i < map[level].numberOfAP; i++) {
    		//int apLocation = map[level].apLocationAtFP
    		double apX = map[level].apLocationCoor[i].location.x;
    		double apY = map[level].apLocationCoor[i].location.y;
    		if (distanceInSameLevel(new Coordinate(videoX, videoY), new Coordinate(apX, apY)) < minDistance) {
    			minDistance = distanceInSameLevel(new Coordinate(videoX, videoY), new Coordinate(apX, apY));
    			saveAPCoor = new Coordinate(apX, apY);
    		}
    	}
    	return saveAPCoor;
    }
    
    public static void generateWifi() {
    	double[] distanceBetweenGrounthAndWifi = new double[numberOfTraceVideo];
    	int[] countDistanceBetweenGrounthAndWifi = new int[numberOfTraceVideo];
    	System.out.println("hhqt");
    	createMap();
    	for (int i = 0; i < numberOfTraceVideo; i++) {
    		int reportIn5Seconds = 0;
    		for (int time = traceVideoStartTime[i]; time <= traceVideoEndTime[i]; time++) {
    			if (reportIn5Seconds % 5 == 0) {
    				Coordinate nearestAPCoor = findTheNearestAP(tracePeopleVideo[i][time]);
    				distanceBetweenGrounthAndWifi[i] += distanceInSameLevel(nearestAPCoor, tracePeopleVideo[i][time].locationCoor);
    				tracePeopleWifiGenerate[i][countPeopleWifiGenerate[i]] = new timeWithLocationCoordinate(nearestAPCoor,time);
    				countPeopleWifiGenerate[i]++;
    				countDistanceBetweenGrounthAndWifi[i]++;
    			}
    			reportIn5Seconds++;
    		}
    		distanceBetweenGrounthAndWifi[i] = distanceBetweenGrounthAndWifi[i] / countDistanceBetweenGrounthAndWifi[i];
    		System.out.println(distanceBetweenGrounthAndWifi[i]);
    	}
    	
    	try
    	{
    	   	String filename= wifiFolder + "inferLocationDueToBouncingSignal.csv";
    	    FileWriter fw = new FileWriter(filename,false); //the true will append the new data
    	    for (int i = 0; i < numberOfTraceVideo; i++) {
    	    	for (int j = 0; j < countPeopleWifiGenerate[i]; j++) {
    	    		int wifiID = i + beginWifiIndexOfTracing;
    	    		int timestep = tracePeopleWifiGenerate[i][j].time;    	    		
    	    		double coorX = tracePeopleWifiGenerate[i][j].location.locationCoor.x;
    	    		double coorY = tracePeopleWifiGenerate[i][j].location.locationCoor.y;
    	    		fw.write(wifiID + "," + timestep + "," + coorX + "," + coorY + "\n");
    	    	}
    	    }
    	    fw.close();
    	}
    	catch(IOException ioe)
    	{
    	    System.err.println("IOException: " + ioe.getMessage());
    	}
    }
    
	public static void createMap() {
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
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
		map[1].numberOfAP = 8;
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
		
		map[1].apLocationCoor = new APLocation[] {new APLocation("108.243.127.198.214.144", 1, 24.5),
				new APLocation("108.243.127.205.178.172", 4, 21),
				new APLocation("156.28.18.192.34.72", 1, 20),
				new APLocation("108.243.127.198.200.250", 1, 12),
				new APLocation("108.243.127.198.204.66", 2, 8),
				new APLocation("108.243.127.198.207.102", -1, 4),
				new APLocation("156.28.18.192.34.30", 6, 1),
				new APLocation("156.28.18.192.33.248", 2, -1)};
		
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
		map[3].fpCoordinate[46] = new Coordinate(4,5.2);
		map[3].fpCoordinate[47] = new Coordinate(4.2,3.5);
		map[3].fpCoordinate[48] = new Coordinate(2,3.5); 
		map[3].fpCoordinate[49] = new Coordinate(-1.5,3.5); 
		
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
		
		/*map = new MapReal[7];
		for (int i = 0; i < 7; i++) {
			map[i] = new MapReal();
		}
		// For map level 1;
		
		map[1].fpPointInThisMap = 160;	
		map[1].numberOfPhysicalVideoX = 21;
		map[1].numberOfPhysicalVideoY = 6;
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
		map[1].fpCoordinate[1] = new Coordinate(28.5,2.5);
		map[1].fpCoordinate[2] = new Coordinate(27.5,2);
		map[1].fpCoordinate[3] = new Coordinate(26.8,2.5);
		map[1].fpCoordinate[4] = new Coordinate(26,2);
		map[1].fpCoordinate[5] = new Coordinate(25.2,2.5);
		map[1].fpCoordinate[6] = new Coordinate(24.5,2);
		map[1].fpCoordinate[7] = new Coordinate(23.5,2.5);
		map[1].fpCoordinate[8] = new Coordinate(23,2);
		map[1].fpCoordinate[9] = new Coordinate(22,2.5);
		map[1].fpCoordinate[10] = new Coordinate(22,3.5);
		map[1].fpCoordinate[11] = new Coordinate(21.2,2);
		map[1].fpCoordinate[12] = new Coordinate(21.2,3);
		map[1].fpCoordinate[13] = new Coordinate(21,3.5);
		map[1].fpCoordinate[14] = new Coordinate(20.5,2.5);
		map[1].fpCoordinate[15] = new Coordinate(20.2,3);
		map[1].fpCoordinate[16] = new Coordinate(19.8,2);
		map[1].fpCoordinate[17] = new Coordinate(19.8,3.5);
		map[1].fpCoordinate[18] = new Coordinate(19,2.5);
		map[1].fpCoordinate[19] = new Coordinate(19,3.5);
		map[1].fpCoordinate[20] = new Coordinate(18.2,2);
		map[1].fpCoordinate[21] = new Coordinate(18.2,3);
		map[1].fpCoordinate[22] = new Coordinate(17.5,2.5);
		map[1].fpCoordinate[23] = new Coordinate(17.5,3.5);
		map[1].fpCoordinate[24] = new Coordinate(16.5,2);
		map[1].fpCoordinate[25] = new Coordinate(16.5,3);
		map[1].fpCoordinate[26] = new Coordinate(15.8,2.5);
		map[1].fpCoordinate[27] = new Coordinate(15.8,3.8);
		map[1].fpCoordinate[28] = new Coordinate(15.8,5.4);
		map[1].fpCoordinate[29] = new Coordinate(15.2,3);
		map[1].fpCoordinate[30] = new Coordinate(15.2,4.5);
		map[1].fpCoordinate[31] = new Coordinate(14.8,2.3);
		map[1].fpCoordinate[32] = new Coordinate(14.8,3.8);
		map[1].fpCoordinate[33] = new Coordinate(14.8,5.3);
		map[1].fpCoordinate[34] = new Coordinate(14.5,3);
		map[1].fpCoordinate[35] = new Coordinate(14.5,4.5);
		map[1].fpCoordinate[36] = new Coordinate(14,2.3);
		map[1].fpCoordinate[37] = new Coordinate(14,3);
		map[1].fpCoordinate[38] = new Coordinate(14,5.3);
		map[1].fpCoordinate[39] = new Coordinate(13.5,3);
		map[1].fpCoordinate[40] = new Coordinate(13.5,4.5);
		map[1].fpCoordinate[41] = new Coordinate(13,2.3);
		map[1].fpCoordinate[42] = new Coordinate(13,3.8);
		map[1].fpCoordinate[43] = new Coordinate(12,5.3);
		map[1].fpCoordinate[44] = new Coordinate(12,2);
		map[1].fpCoordinate[45] = new Coordinate(12,3);
		map[1].fpCoordinate[46] = new Coordinate(11.3,2.5);
		map[1].fpCoordinate[47] = new Coordinate(11.3,3.5);
		map[1].fpCoordinate[48] = new Coordinate(10.5,2);
		map[1].fpCoordinate[49] = new Coordinate(10.5,3);
		map[1].fpCoordinate[50] = new Coordinate(9.6,2.5);
		map[1].fpCoordinate[51] = new Coordinate(9,2);
		map[1].fpCoordinate[52] = new Coordinate(9,3);
		map[1].fpCoordinate[53] = new Coordinate(8.2,2.5);
		map[1].fpCoordinate[54] = new Coordinate(8.2,3.5);
		map[1].fpCoordinate[55] = new Coordinate(7.5,2);
		map[1].fpCoordinate[56] = new Coordinate(7.5,3);
		map[1].fpCoordinate[57] = new Coordinate(6.5,1);
		map[1].fpCoordinate[58] = new Coordinate(6.5,2.5);
		map[1].fpCoordinate[59] = new Coordinate(5.8,1);
		map[1].fpCoordinate[60] = new Coordinate(5.8,2);
		map[1].fpCoordinate[61] = new Coordinate(1,5.5);
		map[1].fpCoordinate[62] = new Coordinate(1,5.5);
		map[1].fpCoordinate[63] = new Coordinate(1,5.5);
		map[1].fpCoordinate[64] = new Coordinate(1,5.5);
		map[1].fpCoordinate[65] = new Coordinate(1,5.5);
		map[1].fpCoordinate[66] = new Coordinate(1,5.5);
		map[1].fpCoordinate[67] = new Coordinate(1,5.5);
		map[1].fpCoordinate[76] = new Coordinate(27.5,4.5);
		map[1].fpCoordinate[77] = new Coordinate(27.5,4.5);
		map[1].fpCoordinate[78] = new Coordinate(27.5,4.5);
		map[1].fpCoordinate[79] = new Coordinate(27.5,4.5);
		map[1].fpCoordinate[80] = new Coordinate(27.5,4.5);
		map[1].fpCoordinate[81] = new Coordinate(27.5,4.5);
		map[1].fpCoordinate[82] = new Coordinate(27.5,4.5);
		map[1].fpCoordinate[83] = new Coordinate(27.5,4.5);
		map[1].fpCoordinate[84] = new Coordinate(25,4);
		map[1].fpCoordinate[85] = new Coordinate(25,5);
		map[1].fpCoordinate[86] = new Coordinate(23.5,5.2);
		map[1].fpCoordinate[87] = new Coordinate(23.5,5.2);
		
				
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
		}	*/

		
		
		
	}
    
    public static void setVideoLocationForAddedVideo() {
    	tracePeopleVideo = new NormalLocationCoordinate[numberOfTraceVideo][10000];
    	
    	tracePeopleWifiGenerate = new timeWithLocationCoordinate[numberOfTraceVideo][10000];
    	countPeopleWifiGenerate = new int[numberOfTraceVideo];
    	//-----------------*Long Trace*--------------------------
    	//0
    	int indexTracePeople = 0;
    	traceVideoStartTime[indexTracePeople] = 1406;
    	goStraightLine(indexTracePeople, new Coordinate(0,3), new Coordinate(0,3), 1406, 1466);
    	goStraightLine(indexTracePeople, new Coordinate(0,3), new Coordinate(1,25), 1406, 1617);
    	traceVideoEndTime[indexTracePeople] = 1616;
    	//1
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 1617;
    	goStraightLine(indexTracePeople, new Coordinate(1,25), new Coordinate(0,23), 1617, 1664);
    	goStraightLine(indexTracePeople, new Coordinate(0,23), new Coordinate(0,23), 1664, 1683);
    	goStraightLine(indexTracePeople, new Coordinate(0,23), new Coordinate(1,20), 1683, 1707);
    	goStraightLine(indexTracePeople, new Coordinate(1,20), new Coordinate(0,4), 1707, 1801);
    	goStraightLine(indexTracePeople, new Coordinate(0,4), new Coordinate(0,0), 1801, 1835);
    	traceVideoEndTime[indexTracePeople] = 1834;
    	//2
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 2291;
    	goStraightLine(indexTracePeople, new Coordinate(2,0), new Coordinate(2,2), 2291, 2342);
    	goStraightLine(indexTracePeople, new Coordinate(2,2), new Coordinate(2,7), 2342, 2351);
    	goStraightLine(indexTracePeople, new Coordinate(2,7), new Coordinate(2,7), 2351, 2357);
    	goStraightLine(indexTracePeople, new Coordinate(2,7), new Coordinate(2,21), 2357, 2499);    	
    	traceVideoEndTime[indexTracePeople] = 2498;
    	//3
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 2495;
    	goStraightLine(indexTracePeople, new Coordinate(0,26), new Coordinate(3,21), 2495, 2540);
    	goStraightLine(indexTracePeople, new Coordinate(3,21), new Coordinate(3,3), 2540, 2611);
    	goStraightLine(indexTracePeople, new Coordinate(3,3), new Coordinate(3,1), 2611, 2621);
    	traceVideoEndTime[indexTracePeople] = 2620;
    	//4
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 2661;
    	goStraightLine(indexTracePeople, new Coordinate(4,0), new Coordinate(2,3), 2661, 2692);
    	goStraightLine(indexTracePeople, new Coordinate(2,3), new Coordinate(1,-1), 2692, 2711);    	
    	traceVideoEndTime[indexTracePeople] = 2710;
    	//5
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 2731;
    	goStraightLine(indexTracePeople, new Coordinate(2,-1), new Coordinate(2,1), 2731, 2746);
    	goStraightLine(indexTracePeople, new Coordinate(2,1), new Coordinate(1,3), 2746, 2777);
    	goStraightLine(indexTracePeople, new Coordinate(1,3), new Coordinate(3,21), 2777, 2856);
    	goStraightLine(indexTracePeople, new Coordinate(3,21), new Coordinate(2,22), 2856, 2883);
    	goStraightLine(indexTracePeople, new Coordinate(2,22), new Coordinate(1,26), 2883, 2937);
    	traceVideoEndTime[indexTracePeople] = 2936;
    	//6
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 2947;
    	goStraightLine(indexTracePeople, new Coordinate(1,26), new Coordinate(2,22), 2947, 2989);
    	goStraightLine(indexTracePeople, new Coordinate(2,22), new Coordinate(2,2), 2989, 3097);
    	goStraightLine(indexTracePeople, new Coordinate(2,2), new Coordinate(1,-1), 3097, 3162);
    	traceVideoEndTime[indexTracePeople] = 3161;
    	//7
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 3185;
    	goStraightLine(indexTracePeople, new Coordinate(1,-1), new Coordinate(2,3), 3185, 3241);
    	goStraightLine(indexTracePeople, new Coordinate(2,3), new Coordinate(1,17), 3241, 3328);    	
    	traceVideoEndTime[indexTracePeople] = 3327;
    	//8
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 3988;
    	goStraightLine(indexTracePeople, new Coordinate(1,25), new Coordinate(1,3), 3988, 4100);
    	goStraightLine(indexTracePeople, new Coordinate(1,3), new Coordinate(2,1), 4100, 4133);
    	goStraightLine(indexTracePeople, new Coordinate(2,1), new Coordinate(4,3), 4133, 4150);
    	goStraightLine(indexTracePeople, new Coordinate(4,3), new Coordinate(4,0), 4150, 4166);
    	traceVideoEndTime[indexTracePeople] = 4165;
    	//9
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 4247;
    	goStraightLine(indexTracePeople, new Coordinate(1,-1), new Coordinate(2,1), 4247, 4260);
    	goStraightLine(indexTracePeople, new Coordinate(2,1), new Coordinate(2,1), 4260, 4318);
    	goStraightLine(indexTracePeople, new Coordinate(2,1), new Coordinate(1,25), 4318, 4455);
    	traceVideoEndTime[indexTracePeople] = 4454;
    	//10
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 4485;
    	goStraightLine(indexTracePeople, new Coordinate(1,25), new Coordinate(2,3), 4485, 4582);
    	goStraightLine(indexTracePeople, new Coordinate(2,3), new Coordinate(1,-1), 4582, 4631);    	
    	traceVideoEndTime[indexTracePeople] = 4630;
    	//11
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 4759;
    	goStraightLine(indexTracePeople, new Coordinate(1,-1), new Coordinate(2,2), 4759, 4783);
    	goStraightLine(indexTracePeople, new Coordinate(2,2), new Coordinate(2,3), 4783, 4807);
    	goStraightLine(indexTracePeople, new Coordinate(2,3), new Coordinate(2,22), 4807, 4929);
    	goStraightLine(indexTracePeople, new Coordinate(2,22), new Coordinate(1,25), 4929, 5007);
    	traceVideoEndTime[indexTracePeople] = 5006;
    	//12
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 5022;
    	goStraightLine(indexTracePeople, new Coordinate(1,25), new Coordinate(1,20), 5022, 5091);
    	goStraightLine(indexTracePeople, new Coordinate(1,20), new Coordinate(2,9), 5091, 5133);
    	goStraightLine(indexTracePeople, new Coordinate(2,9), new Coordinate(1,0), 5133, 5181);
    	goStraightLine(indexTracePeople, new Coordinate(1,0), new Coordinate(1,-1), 5181, 5221);
    	traceVideoEndTime[indexTracePeople] = 5220;
    	//13
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 5271;
    	goStraightLine(indexTracePeople, new Coordinate(1,-1), new Coordinate(2,2), 5271, 5295);
    	goStraightLine(indexTracePeople, new Coordinate(2,2), new Coordinate(2,3), 5295, 5326);
    	goStraightLine(indexTracePeople, new Coordinate(2,3), new Coordinate(3,21), 5326, 5432);
    	goStraightLine(indexTracePeople, new Coordinate(3,21), new Coordinate(1,25), 5432, 5504);
    	traceVideoEndTime[indexTracePeople] = 5503;
    	//14
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 5539;
    	goStraightLine(indexTracePeople, new Coordinate(1,25), new Coordinate(1,20), 5539, 5605);
    	goStraightLine(indexTracePeople, new Coordinate(1,20), new Coordinate(2,3), 5605, 5705);
    	goStraightLine(indexTracePeople, new Coordinate(2,3), new Coordinate(1,-1), 5705, 5767);
    	traceVideoEndTime[indexTracePeople] = 5766;
    	//15
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 6046;
    	goStraightLine(indexTracePeople, new Coordinate(1,-1), new Coordinate(2,3), 6046, 6086);
    	goStraightLine(indexTracePeople, new Coordinate(2,3), new Coordinate(2,20), 6086, 6156);
    	goStraightLine(indexTracePeople, new Coordinate(2,20), new Coordinate(1,25), 6156, 6238);
    	traceVideoEndTime[indexTracePeople] = 6237;
    	//16
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 6261;
    	goStraightLine(indexTracePeople, new Coordinate(1,25), new Coordinate(2,20), 6261, 6369);
    	goStraightLine(indexTracePeople, new Coordinate(2,20), new Coordinate(2,2), 6369, 6462);
    	goStraightLine(indexTracePeople, new Coordinate(2,2), new Coordinate(2,2), 6462, 6506);
    	goStraightLine(indexTracePeople, new Coordinate(2,2), new Coordinate(1,-1), 6506, 6521);
    	traceVideoEndTime[indexTracePeople] = 6520;
    	//17
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 6546;
    	goStraightLine(indexTracePeople, new Coordinate(1,-1), new Coordinate(2,3), 6546, 6597);
    	goStraightLine(indexTracePeople, new Coordinate(2,3), new Coordinate(1,20), 6597, 6671);
    	goStraightLine(indexTracePeople, new Coordinate(1,20), new Coordinate(1,25), 6671, 6715);
    	traceVideoEndTime[indexTracePeople] = 6714;
    	//18
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 6733;
    	goStraightLine(indexTracePeople, new Coordinate(1,25), new Coordinate(1,20), 6733, 6816);
    	goStraightLine(indexTracePeople, new Coordinate(1,20), new Coordinate(2,3), 6816, 6887);
    	goStraightLine(indexTracePeople, new Coordinate(2,3), new Coordinate(1,-1), 6887, 7054);
    	traceVideoEndTime[indexTracePeople] = 7053;
    	//19
    	indexTracePeople++;
    	traceVideoStartTime[indexTracePeople] = 6733;
    	goStraightLine(indexTracePeople, new Coordinate(1,25), new Coordinate(1,20), 6733, 6816);
    	goStraightLine(indexTracePeople, new Coordinate(1,20), new Coordinate(2,3), 6816, 6887);
    	goStraightLine(indexTracePeople, new Coordinate(2,3), new Coordinate(1,-1), 6887, 7054);
    	traceVideoEndTime[indexTracePeople] = 7053; 
    	



    	//---------------------------**------------------------------------

    	
    }
    
    static void goStraightLine(int indexTracePeople, Coordinate coor1, Coordinate coor2, int timestamp1, int timestamp2) {
    	double dx = (coor2.x - coor1.x) / (timestamp2 - timestamp1);
    	double dy = (coor2.y - coor1.y) / (timestamp2 - timestamp1);
    	for (int k = 0; k < timestamp2 - timestamp1; k++) {
    		tracePeopleVideo[indexTracePeople][timestamp1 + k] = new NormalLocationCoordinate(new Coordinate(coor1.x + dx * k,coor1.y + dy * k));
    	}
    }
    
	public static boolean isNoPoint (NormalLocationCoordinate video) {
		if (video == null) return true;
		else {
			return false;
		}
	}
    
	public static boolean similarLevel(NormalLocationCoordinate c1, NormalLocationCoordinate c2) {


			return true;

	}
	
	static double distanceInSameLevel(Coordinate point1, Coordinate point2) {
		return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
	}
	
	/*public static void findTheClosestWifiOfGivenVideoWithLimitDistance4Unit() {
		readAfterInterpolation();
		setVideoLocationForAddedVideo();
		double saveMinAverageDistance[] = new double[10000];
		int saveAverageCount[] = new int[10000];
		int saveWifiID[] = new int[10000];
		int elementCount = 0;
		for (int wifiID = 0; wifiID < numberOfWifi; wifiID++) {
			double averageDistance = 0;
			int averageCount = 0;
			for (int i = traceVideoStartTime; i <= traceVideoEndTime; i++) {
				if (!isNoPoint(peopleWifi[wifiID][i]) && !isNoPoint(peopleVideo[i])) {
					if (similarLevel(peopleWifi[wifiID][i],peopleVideo[i])) {
						double d = distanceInSameLevel(peopleWifi[wifiID][i].locationCoor,peopleVideo[i].locationCoor);
						if (d < 4) {
							averageDistance += d;
							averageCount++;
						}
					}					
				}
			}
			averageDistance = averageDistance / averageCount;	
			saveMinAverageDistance[elementCount] = averageDistance;
			saveAverageCount[elementCount] = averageCount;
			saveWifiID[elementCount] = wifiID;
			elementCount++;			
		}
		for (int i = 0; i < elementCount - 1; i++) {
			for (int j = i + 1; j < elementCount; j++) {
				if (saveAverageCount[i] < saveAverageCount[j]) {
					double tmp = saveMinAverageDistance[i];
					saveMinAverageDistance[i] = saveMinAverageDistance[j];
					saveMinAverageDistance[j] = tmp;
					int t = saveAverageCount[i];
					saveAverageCount[i] = saveAverageCount[j];
					saveAverageCount[j] = t;
					t = saveWifiID[i];
					saveWifiID[i] = saveWifiID[j];
					saveWifiID[j] = t;
				}
			}
		}
		for (int i = 0; i <5; i++)
			System.out.println("Final choice:" + "WifiID: " + saveWifiID[i] + " AverageCount: " + saveAverageCount[i] + " AverageDistance: " + saveMinAverageDistance[i]);

	}
	
	public static void findTheClosestWifiOfGivenVideo() {
		readAfterInterpolation();
		setVideoLocationForAddedVideo();
		double saveMinAverageDistance[] = new double[10000];
		int saveAverageCount[] = new int[10000];
		int saveWifiID[] = new int[10000];
		int elementCount = 0;
		for (int wifiID = 0; wifiID < numberOfWifi; wifiID++) {
			if (wifiID == 11) {
				wifiID = 11;
			}
			double averageDistance = 0;
			int averageCount = 0;
			for (int i = traceVideoStartTime; i <= traceVideoEndTime; i++) {

				if (!isNoPoint(peopleWifi[wifiID][i]) && !isNoPoint(peopleVideo[i])) {
					if (similarLevel(peopleWifi[wifiID][i],peopleVideo[i])) {
						
						averageDistance += distanceInSameLevel(peopleWifi[wifiID][i].locationCoor,peopleVideo[i].locationCoor);
						if (wifiID == 11) {
							System.out.println(i + " " + averageDistance);
						}
					}
					else {
						averageDistance += 35; //equal to the length of a floor
					}
					averageCount++;
				}
			}
			averageDistance = averageDistance / averageCount;	
			if (averageCount > 50) {
				saveMinAverageDistance[elementCount] = averageDistance;
				saveAverageCount[elementCount] = averageCount;
				saveWifiID[elementCount] = wifiID;
				elementCount++;
			}
			
		}
		for (int i = 0; i < elementCount - 1; i++) {
			for (int j = i + 1; j < elementCount; j++) {
				if (saveMinAverageDistance[i] > saveMinAverageDistance[j]) {
					double tmp = saveMinAverageDistance[i];
					saveMinAverageDistance[i] = saveMinAverageDistance[j];
					saveMinAverageDistance[j] = tmp;
					int t = saveAverageCount[i];
					saveAverageCount[i] = saveAverageCount[j];
					saveAverageCount[j] = t;
					t = saveWifiID[i];
					saveWifiID[i] = saveWifiID[j];
					saveWifiID[j] = t;
				}
			}
		}
		for (int i = 0; i <5; i++)
		System.out.println("Final choice:" + "WifiID: " + saveWifiID[i] + " AverageDistance: " + saveMinAverageDistance[i] + " AverageCount: " + saveAverageCount[i]);
	}
	
	public static int getMin(int x1, int x2) {
		if (x1 < x2) {
			return x1;
		}
		else {
			return x2;
		}
	}

	public static int getMax(int x1, int x2) {
		if (x1 > x2) {
			return x1;
		}
		else {
			return x2;
		}
	}

	
	public static void findTheClosestWifiOfGivenVideoConsiderAllDuration() {

		readAfterInterpolation();
		setVideoLocationForAddedVideo();
		double saveMinAverageDistance[] = new double[10000];
		int saveAverageCount[] = new int[10000];
		int saveWifiID[] = new int[10000];
		int elementCount = 0;
		for (int wifiID = 0; wifiID < numberOfWifi; wifiID++) {
			int startTime = getMin(peopleWifiStartTime[wifiID], traceVideoStartTime);
			int endTime = getMax(peopleWifiEndTime[wifiID], traceVideoEndTime);
			double averageDistance = 0;
			int averageCount = 0;
			for (int i = startTime; i <= endTime; i++) {
				if (peopleVideo[i] == null) {
					averageDistance = averageDistance + 17;
					averageCount++;
				}
				else {
					if (!isNoPoint(peopleVideo[i])) {
						if (peopleWifi[wifiID][i] == null) {
							averageDistance = averageDistance + 17;
							averageCount++;
						}
						else {
							if (!isNoPoint(peopleWifi[wifiID][i])){
								if (similarLevel(peopleVideo[i], peopleWifi[wifiID][i])) {									
									averageDistance += distanceInSameLevel(peopleWifi[wifiID][i].locationCoor,peopleVideo[i].locationCoor);
									averageCount++;
								}
								else {
									averageDistance += 35; //equal to the length of a floor
									averageCount++;
								}
							}
						}
					}
				}
			}
			averageDistance = averageDistance / averageCount;	
			
			saveMinAverageDistance[elementCount] = averageDistance;
			saveAverageCount[elementCount] = averageCount;
			saveWifiID[elementCount] = wifiID;
			elementCount++;
			
			
		}
		for (int i = 0; i < elementCount - 1; i++) {
			for (int j = i + 1; j < elementCount; j++) {
				if (saveMinAverageDistance[i] > saveMinAverageDistance[j]) {
					double tmp = saveMinAverageDistance[i];
					saveMinAverageDistance[i] = saveMinAverageDistance[j];
					saveMinAverageDistance[j] = tmp;
					int t = saveAverageCount[i];
					saveAverageCount[i] = saveAverageCount[j];
					saveAverageCount[j] = t;
					t = saveWifiID[i];
					saveWifiID[i] = saveWifiID[j];
					saveWifiID[j] = t;
				}
			}
		}
		for (int i = 0; i <5; i++)
		System.out.println("Final choice:" + "WifiID: " + saveWifiID[i] + " AverageDistance: " + saveMinAverageDistance[i] + " AverageCount: " + saveAverageCount[i]);
	}
	
	public static void printTestWifi(int wifiID) {
		readAfterInterpolation();
		setVideoLocationForAddedVideo();
		for (int i = traceVideoStartTime; i < traceVideoEndTime; i++) {
			System.out.print(i + ": ");
			if (isNoPoint(peopleWifi[wifiID][i])) {
				System.out.print("Wifi: No Point");
			}
			else {
				System.out.print("Wifi: " + peopleWifi[wifiID][i].level + "(" + peopleWifi[wifiID][i].locationCoor.x + "," + peopleWifi[wifiID][i].locationCoor.y + ") ");
			}
			if (isNoPoint(peopleVideo[i])) {
				System.out.println("Video: No Point");
			}
			else {
				System.out.println("Video: " + peopleVideo[i].level + "(" + peopleVideo[i].locationCoor.x + "," + peopleVideo[i].locationCoor.y + ')');
			}
		}
	}
	
	public static void getAverageDistance(int[] wifiIDinput) {
		for (int k = 0; k < 5; k++) {
			int wifiID = wifiIDinput[k];
			double averageDistance = 0;
			int averageCount = 0;
			for (int i = traceVideoStartTime; i <= traceVideoEndTime; i++) {
				if (!isNoPoint(peopleWifi[wifiID][i]) && !isNoPoint(peopleVideo[i])) {
					if (similarLevel(peopleWifi[wifiID][i],peopleVideo[i])) {						
						averageDistance += distanceInSameLevel(peopleWifi[wifiID][i].locationCoor,peopleVideo[i].locationCoor);
					}
					else {
						averageDistance += 35; //equal to the length of a floor
					}
					averageCount++;
				}
			}
			averageDistance = averageDistance / averageCount;	
			if (averageCount > 50) {
				System.out.println(wifiID + " AverageDistance: " + averageDistance);
			}
			
		}
	}*/
	
	public static String[] parseData(String data, char separate) {
		int numberOfComponent = 0;	
		for (int i = 0; i < data.length(); i++) {
			if (data.charAt(i) == separate) {
				numberOfComponent++;
			}
		}
		int index = 0;
		String[] result = new String[numberOfComponent];
		String component = "";
		int i = 0;
		while (i < data.length() - 1) {
			if (data.charAt(i) == separate) {
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
		/*if (index < numberOfComponent - 1) {
			return null;
		}*/
		return result;
	}
	
	public static int getNumberOfMatch(String data, char separate) {
		int numberOfComponent = 0;
		for (int i = 0; i < data.length(); i++) {
			if (data.charAt(i) == separate) {
				numberOfComponent++;
			}
		}
		return numberOfComponent / 6;
	}
	
	public static void readMatchingOutputFile() {
		Scanner scan;
	    for (int fileIndex = 0; fileIndex < SuntecMatching.numberOfFile - 1; fileIndex++) {	    	
	    	System.out.println(fileIndex);
		    try {
		    	File file = new File(folderOutputMatching + "OutputMatching" + fileIndex + ".txt");
		        scan = new Scanner(file);	  
		        scan.nextLine();
		        while (scan.hasNextLine()) {
		        	String data = scan.nextLine();
		        	String[] parseData = parseData(data,' ');
		        	if (parseData != null) {
			        	int wifiID = Integer.parseInt(parseData[0]);
			        	if (wifiID == 7 && fileIndex == 0) {
			        		wifiID = 7;
			        	}
			        	if (wifiID >= beginWifiIndexOfTracing) {
			        		int traceVideoCheck = wifiID - beginWifiIndexOfTracing;			        	
				        	for (int i = 0; i < getNumberOfMatch(data,' '); i++) {
				        		if (Integer.parseInt(parseData[6 * i + 1]) > 0) {
				        			int video = Integer.parseInt(parseData[6 * i + 1]);
				        			int level = Integer.parseInt(parseData[6 * i + 2]);
				        			double coorX = Double.parseDouble(parseData[6 * i + 3]);
				        			double coorY = Double.parseDouble(parseData[6 * i + 4]);
				        			int rankScore = Integer.parseInt(parseData[6 * i + 5]);
				        			int timeStamp = Integer.parseInt(parseData[6 * i + 6]);
				        			traceMatchBlob[traceVideoCheck][fileIndex][i] = new RankTimeWithLocationCoordinate(rankScore, timeStamp, coorX, coorY);
				        		}
				        	}
			        	}
		        	}
		        }
		        scan.close();
		    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
		    }
	   }

	}
	
	public static void readMatchingOutputFileWithTimeMatch() {
		Scanner scan;
	    for (int fileIndex = 0; fileIndex < SuntecMatching.numberOfFile - 1; fileIndex++) {	    	
	    	System.out.println(fileIndex);
		    try {
		    	File file = new File(folderOutputMatchingWithTimeMatch + "OutputMatching" + fileIndex + ".txt");
		        scan = new Scanner(file);	  
		        scan.nextLine();
		        while (scan.hasNextLine()) {
		        	String data = scan.nextLine();
		        	String[] parseData = parseData(data,' ');
		        	if (parseData != null) {
			        	int wifiID = Integer.parseInt(parseData[0]);
			        	if (wifiID == 7 && fileIndex == 0) {
			        		wifiID = 7;
			        	}
			        	if (wifiID >= beginWifiIndexOfTracing) {
			        		int traceVideoCheck = wifiID - beginWifiIndexOfTracing;			        	
				        	for (int i = 0; i < getNumberOfMatch(data,' '); i++) {
				        		if (Integer.parseInt(parseData[7 * i + 1]) > 0) {
				        			int video = Integer.parseInt(parseData[7 * i + 1]);
				        			int level = Integer.parseInt(parseData[7 * i + 2]);
				        			double coorX = Double.parseDouble(parseData[7 * i + 3]);
				        			double coorY = Double.parseDouble(parseData[7 * i + 4]);
				        			int rankScore = Integer.parseInt(parseData[7 * i + 5]);
				        			int timeStamp = Integer.parseInt(parseData[7 * i + 7]);
				        			traceMatchBlob[traceVideoCheck][fileIndex][i] = new RankTimeWithLocationCoordinate(rankScore, timeStamp, coorX, coorY);
				        		}
				        	}
			        	}
		        	}
		        }
		        scan.close();
		    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
		    }
	   }

	}
	
	static double getDistance(NormalLocationCoordinate matchedBlob, NormalLocationCoordinate groundTruth, boolean isOnEscalator) {

			return distanceInSameLevel(groundTruth.locationCoor, matchedBlob.locationCoor);

	}
	
    public static void readAfterInterpolation() {
	    Scanner scan;	    
	    
	    int ID = 0;	 
	    //numberOfWifi = 0;
	    peopleWifi = new NormalLocationCoordinate[Config.maximumWifiReportCount][10000];
		for (int i = 0; i < Config.maximumWifiReportCount; i++) {
			peopleWifiStartTime[i] = Integer.MAX_VALUE;
			peopleWifiEndTime[i] = 0;
 		}
	    try {
	    	File file = new File(wifiFolder +  "afterInterpolationNew40.csv");
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
	        	if (time < peopleWifiStartTime[ID]) {
	        		peopleWifiStartTime[ID] = time;
	        	}
	        	if (time > peopleWifiEndTime[ID]) {
	        		peopleWifiEndTime[ID] = time;
	        	}
	        	int level = Integer.parseInt(parseData[2]);
	        	Coordinate location = new Coordinate(Double.parseDouble(parseData[3]),Double.parseDouble(parseData[4]));
	        	peopleWifi[ID][time] = new NormalLocationCoordinate(location);
	        }
	        scan.close();
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }

    }
	
	public static void errorEvaluateGroundTruthAndMatchBlob() {
		readMatchingOutputFile();
		BufferedWriter writer;
		try {				
			writer = new BufferedWriter(new FileWriter(folderOutputMatching + "GroundTruthEvaluation.csv"));
			writer.write("TracePeople ");
			for (int i = 0; i < SuntecMatching.numberOfFile; i++) {
				for (int j = 0; j < 3; j++) {					
					writer.write("File" + i + "Rank"+ j + " ");
					writer.write("File" + i + "Distance"+ j + " ");
				}
			}
			writer.write("AverageDistanceExceptNomatchAndWrongfloor ");
			writer.write("\n");			
			for (int tracePeopleID = 0; tracePeopleID < numberOfTraceVideo; tracePeopleID++) {
				if (tracePeopleID == 7) {
					tracePeopleID = 7;
				}
				writer.write("People" + tracePeopleID + " ");
				double average = 0;
				int countAverage = 0;
				for (int indexFile = 0; indexFile < SuntecMatching.numberOfFile; indexFile++) {
					if (indexFile == 6) {
						indexFile = 6;
					}
					for (int top3 = 0; top3 < 3; top3++ ) {
						RankTimeWithLocationCoordinate matchInfo = traceMatchBlob[tracePeopleID][indexFile][top3];
						if (matchInfo != null) {
							int timeMatch = matchInfo.time;
							int matchRank = matchInfo.rank;					
							NormalLocationCoordinate matchLocation = matchInfo.location;
							NormalLocationCoordinate groundTruthLocation = tracePeopleVideo[tracePeopleID][timeMatch];
							boolean isOnEscalator = isTracePeopleVideoAtEscalator[tracePeopleID][timeMatch];
							double distance = getDistance(matchLocation, groundTruthLocation, isOnEscalator);
							if (distance != 35) {
								average += distance;
								countAverage++;
							}
							writer.write(matchRank + " " + distance + " ");
						}
						else {
							writer.write("NoMatch NoMatch ");
						}
					}
				}
				writer.write(df2.format(average / countAverage) + " ");
				writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void errorEvaluateGroundTruthAndInterpolateGeneratedWifi() {
		readAfterInterpolation();
		double averageDistance[] = new double[numberOfTraceVideo];
		int count[] = new int[numberOfTraceVideo];
		for (int ID = 0; ID < numberOfTraceVideo; ID++) {
			if (ID == 39) {
				ID = 39;
			}
			System.out.println("---------------: " + ID);	
			averageDistance[ID] = 0;
			count[ID] = 0;
			for (int time = peopleWifiStartTime[ID]; time < peopleWifiEndTime[ID]; time = time + 1) {
				Coordinate wifi = peopleWifi[ID][time].locationCoor;

				if (tracePeopleVideo[ID][time] != null) {
					Coordinate gt = tracePeopleVideo[ID][time].locationCoor;
					averageDistance[ID] += distanceInSameLevel(wifi, gt);																
					count[ID]++;

				}
			}
		}
		for (int ID = 0; ID < numberOfTraceVideo; ID++) {
			System.out.println(averageDistance[ID] / count[ID]);
		}
	}
	

	

	
	public static void readWifiReportLocation() {
		//This is the location reported by the RTLS, then location of the device is mounted to the associate AP
	    Scanner scan;	    
	    ParserWifiRTLS parser;
	    int ID = 0;	 
	    numberOfWifi = 0;	    
	    try {
	    	//File file = new File(wifiFolder +  "inferLocationDueToBouncingSignalNew40.csv");
	    	File file = new File(wifiFolder +  "wifiLocationForManualTracing.csv");
	        scan = new Scanner(file);	        	        
	        while (scan.hasNextLine()) {
	        	String data = scan.nextLine();
	        	parser = new ParserWifiRTLS(data);
	        	ID = Integer.parseInt(parser.getAttribute(0));
	        	int time = Integer.parseInt(parser.getAttribute(1)) % 10000;
	        	int level = Integer.parseInt(parser.getAttribute(2));
	        	Coordinate location = new Coordinate(Double.parseDouble(parser.getAttribute(3)),Double.parseDouble(parser.getAttribute(4)));
	        	peopleWifiOriginal[ID][countTimeWifiOriginal[ID]] = new timeWithLocationCoordinate(location, time);
	        	countTimeWifiOriginal[ID]++;	
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
	
	
	public static double getAverageDistance(int video, int wifi) {
		double averageDistance = 0;
		int countDistance = 0;
		for (int i = 0; i < countTimeWifiOriginal[wifi]; i++) {
			int wifiTime = peopleWifiOriginal[wifi][i].time;
			
			Coordinate wifiCoor = new Coordinate(peopleWifiOriginal[wifi][i].location.locationCoor.x,peopleWifiOriginal[wifi][i].location.locationCoor.y);
			if (tracePeopleVideo[video][wifiTime] != null && wifiCoor.x > -999) {
			
				Coordinate videoCoor = new Coordinate(tracePeopleVideo[video][wifiTime].locationCoor.x,tracePeopleVideo[video][wifiTime].locationCoor.y);
			
				countDistance++;
				averageDistance += getDistance(peopleWifiOriginal[wifi][i].location, tracePeopleVideo[video][wifiTime], false);
				//System.out.println(getDistance(peopleWifiOriginal[wifi][i].location, tracePeopleVideo[video][wifiTime], false));
				if (averageDistance > 10000) {
					System.out.println("More than 10000");
				}
			
			}
		}
		if (countDistance != 0) {
			return averageDistance / countDistance;
		}
		else {
			return -1;
		}
	}
	
	public static void errorEvaluateGroundTruthAndOriginalWifi() {
		double[] minimumDistanceOfTraceVideo = new double[numberOfTraceVideo];		
		readWifiReportLocation();
		for (int video = 0; video < numberOfTraceVideo; video++) {
			if (video == 22) {
				video = 22;
			}
			double minDistance = Double.MAX_VALUE;
			for (int wifi = 0; wifi < numberOfWifi; wifi++) {		
				double distance = getAverageDistance(video, wifi);
				if (distance != -1 && distance < minDistance) {
					minDistance = distance;
				}	
			}
			minimumDistanceOfTraceVideo[video] = minDistance;
		}
		for (int i = 0; i < numberOfTraceVideo; i++) {
			System.out.println(minimumDistanceOfTraceVideo[i]);
		}
	}
	
	public static void outputMatchingLattency() {
		readMatchingOutputFileWithTimeMatch();
		System.out.println("------------------");
		for (int traceVideoCheck = 0; traceVideoCheck < numberOfTraceVideo; traceVideoCheck++) {
			int minTimeStepMatch = Integer.MAX_VALUE;
			for (int indexFile = 0; indexFile < SuntecMatching.numberOfFile - 1; indexFile++) {
				for (int i = 0; i < 1; i++) {
					if (traceMatchBlob[traceVideoCheck][indexFile][i] != null) {
						int timeStepMatch = traceMatchBlob[traceVideoCheck][indexFile][i].time;
						if (timeStepMatch < minTimeStepMatch) {
							minTimeStepMatch = timeStepMatch;
						}
					}
				}
			}
			System.out.println(minTimeStepMatch - traceVideoStartTime[traceVideoCheck]);
		}		
	}
	
	public static void outputPeopleDuration() {
		for (int traceVideoCheck = 0; traceVideoCheck < numberOfTraceVideo; traceVideoCheck++) {
			System.out.println(traceVideoEndTime[traceVideoCheck] - traceVideoStartTime[traceVideoCheck]);
		}
	}
	
	public static void main(String args[]) {
		setVideoLocationForAddedVideo();
		generateWifi();		
		//errorEvaluateGroundTruthAndInterpolateGeneratedWifi();
		//errorEvaluateGroundTruthAndMatchBlob();
		//outputMatchingLattency();
		//outputPeopleDuration();
		//errorEvaluateGroundTruthAndOriginalWifi(); //For finding the min bounding error of Wifi Localization
		
	}
}
