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


public class SMUAddition_VideoBlobExtraction {


	//---------------------------------------------------------------------------//
	
	
    static int startingHour;static int startingMinute;static int startingSecond;
    static int endingHour;static int endingMinute;static int endingSecond;
    /*static int startTimeStamp; 
    static int endTimeStamp;
    static int experimentNumber = 0;*/
    static int numberOfWifi;
    static int[] numberOfVideoPerCamera = {0,12,9,2};
    static int[] fpPointInTheArea = {43,44,45,47,48,49,50,51,52,53,54,55,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155};
	
    //static String videoFolder = "src/SuntecRealData/Video/"; //local
	//static String fileOutputFolder = "src/SuntecRealData/VideoProcessResult/"; //local
    static String videoFolder = "SuntecRealData/SMUAdditional/Video/"; //remote    								     											
	static String fileOutputFolder = "SuntecRealData/SMUAdditional/VideoProcessResult/"; //remote

	static int frameNumber = 0; // Number of used frame
	static BoundingBoxImageCoor[][] blobImageCoor = new BoundingBoxImageCoor[Config.maximumBlobVideoCount][Config.maximumFrameCount]; // Coordinate of all the bounding box in the SMU video
	static Coordinate[][] blobPhysicalCoor = new Coordinate[Config.maximumBlobVideoCount][Config.maximumFrameCount];
	//static BoundingBoxImageCoor[][] blobImageCalibration = new BoundingBoxImageCoor[Config.maximumBlobCount][Config.maximumFrameCount]; // Coordinate of all the bounding box in the SMU video
	static timeWithLocationCoordinate[][] peopleVideo;
	static int[] countTimeVideoReport;
	static boolean[] isGoodSample;
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
		int beginTimeOfTheDay = 1504195200;
		result = beginTimeOfTheDay + hour * 3600 + minute * 60 + second;
		return result;
	}
	
	static void initializeTheArray(int fileIndex) {
		startingHour = SMUAddition_SuntecMatching.thisVideoStartHour[fileIndex];
		startingMinute = SMUAddition_SuntecMatching.thisVideoStartMinute[fileIndex];
		startingSecond = SMUAddition_SuntecMatching.thisVideoStartSecond[fileIndex];
		
		
		
		
		/*startingHour = 19; startingMinute = 28; startingSecond = 0;
		endingHour = 20; endingMinute = 00; endingSecond = 0;
		startTimeStamp = getTimeStamp(startingHour, startingMinute, startingSecond);
		endTimeStamp = getTimeStamp(endingHour, endingMinute, endingSecond);
	    experimentNumber = ((endingHour - startingHour) * 3600 + (endingMinute - startingMinute) * 60 + (endingSecond - startingSecond)) / 5 + 1;*/
		peopleVideo = new timeWithLocationCoordinate[Config.maximumBlobVideoCount][Config.maximumWifiReportCount];
		countTimeVideoReport = new int[Config.maximumBlobVideoCount];
		isGoodSample = new boolean[Config.maximumBlobVideoCount];
		for (int i = 0; i < Config.maximumBlobVideoCount; i++) {
			blobDuration[i] = new BlobDuration(Integer.MAX_VALUE,0);
			/*for (int j = 0; j < experimentNumber; j++) {
				peopleVideo[i][j] = -1;
				
			}*/
		}

	}
	
	static void readVideoFile(int fileIndex) {		
		Scanner scan;
		ParserVideo parser;
	    try {
	    	String fileVideoInput = "Cam" + fileIndex + ".txt"; // File location of the SMU video
	    	File file = new File(videoFolder+fileVideoInput);
	        scan = new Scanner(file);
	        String data;
	        while (scan.hasNextLine()) {
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
   		
   		blobVideoAppear = new boolean[blobVideoNumber];
   		for (int i = 0; i < blobVideoNumber; i++) {
   			blobVideoAppear[i] = false;
   		}
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }
	}
	
	public static Coordinate convertToPhysicalCoor(int level, int camera, Coordinate footLocation) {
		Coordinate result = new Coordinate(-1, -1);
		double minDistance = Integer.MAX_VALUE;
		for (int i = 0; i < map[camera].numberOfPhysicalVideoX; i++) {
			for (int j = 0; j < map[camera].numberOfPhysicalVideoY; j++) {
				if (map[camera].videoCoordinate[i][j] != null && distance(map[camera].videoCoordinate[i][j], footLocation) < minDistance) {
					minDistance = distance(map[camera].videoCoordinate[i][j], footLocation);
					result.x = i;
					result.y = j;
				}
			}
		}
		return result;
		
	}
	
	/*public static int getNearestLandMarkInt(int level, Coordinate location) {
		double minDistance = Integer.MAX_VALUE;
		int result = 0;
		for (int i = 0; i < map_level_cam[level].fpPointInThisMap; i++) {
			if (map_level_cam[level].fpCoordinate[i].x != Config.noPoint.x && map_level_cam[level].fpCoordinate[i].y != Config.noPoint.y) {
				if (distance(location, map_level_cam[level].fpCoordinate[i]) < minDistance) {
					minDistance = distance(location, map_level_cam[level].fpCoordinate[i]);
					result = i;
				}
			}
		}
		return result;
		
	}*/
	
	public static int getTimeStampVideo(int fileIndex, int frame) {
		int videoStartHour =SMUAddition_SuntecMatching.thisVideoStartHour[fileIndex];
		int videoStartMinute =SMUAddition_SuntecMatching.thisVideoStartMinute[fileIndex];
		int videoStartSecond =SMUAddition_SuntecMatching.thisVideoStartSecond[fileIndex]; 
		int startTimeStamp = getTimeStamp(videoStartHour, videoStartMinute, videoStartSecond);//time stamp of the beginning of video
		
		return (startTimeStamp + frame / Config.videoFPS) % 10000; // video is 30 fps and only take the last 4 numbers of the timeStamp
		
	}
	
	public static void convertToPhysicalCoordinate(int fileIndex) {
		//(1920 x 1080)		
		int level = SMUAddition_SuntecMatching.thisFileLevel[fileIndex];
		int camera =SMUAddition_SuntecMatching.thisFileCamera[fileIndex];
		int previousTimeStepSave = -1;
		for (int blobID = 0; blobID < blobVideoNumber; blobID++) {
			for (int frame = blobDuration[blobID].left; frame <= blobDuration[blobID].right; frame++) {
				if (blobImageCoor[blobID][frame] != null) {
					int timeStep =getTimeStampVideo(fileIndex, frame); //video is 30 fps
					Coordinate footLocation = new Coordinate((int)(blobImageCoor[blobID][frame].x + blobImageCoor[blobID][frame].w / 2), (int)(blobImageCoor[blobID][frame].y + blobImageCoor[blobID][frame].h));
					if (blobPhysicalCoor[blobID][timeStep] == null && timeStep != previousTimeStepSave) {
						blobPhysicalCoor[blobID][timeStep] = convertToPhysicalCoor(level,camera, footLocation);
						peopleVideo[blobID][countTimeVideoReport[blobID]] = new timeWithLocationCoordinate(blobPhysicalCoor[blobID][timeStep], timeStep);					
						countTimeVideoReport[blobID]++;
						previousTimeStepSave = timeStep;
					}
				}
			}
		}				
	}

	public static void convertToPhysicalCoordinateTemp(int fileIndex) {
		//(1920 x 1080)		
		int level =SMUAddition_SuntecMatching.thisFileLevel[fileIndex];
		int camera =SMUAddition_SuntecMatching.thisFileCamera[fileIndex];
		int previousTimeStepSave = -1;
		for (int timeStep = 0; timeStep < 96; timeStep++) {
			for (int blobID = 0; blobID < blobVideoNumber; blobID++) {
				int frame = 30 + timeStep * 300 ; 				
					if (blobImageCoor[blobID][frame] != null) {						
						Coordinate footLocation = new Coordinate((int)(blobImageCoor[blobID][frame].x + blobImageCoor[blobID][frame].w / 2), (int)(blobImageCoor[blobID][frame].y + blobImageCoor[blobID][frame].h));
						if (blobPhysicalCoor[blobID][timeStep] == null && timeStep != previousTimeStepSave) {
							blobPhysicalCoor[blobID][timeStep] = convertToPhysicalCoor(level,camera, footLocation);
							//peopleVideo[blobID][timeStep] = getNearestLandMarkInt(level,blobPhysicalCoor[blobID][timeStep]);
							if (level == 4) {
								//Reason is that the coordinate of level 4 is different, stupid mistake
								Coordinate changeOriginalBLobPhysicalCoor = new Coordinate(8 - blobPhysicalCoor[blobID][timeStep].x,blobPhysicalCoor[blobID][timeStep].y);
								peopleVideo[blobID][countTimeVideoReport[blobID]] = new timeWithLocationCoordinate(changeOriginalBLobPhysicalCoor, timeStep);
							}
							else {
								peopleVideo[blobID][countTimeVideoReport[blobID]] = new timeWithLocationCoordinate(blobPhysicalCoor[blobID][timeStep], timeStep);
							}
							countTimeVideoReport[blobID]++;
							previousTimeStepSave = timeStep;
						}
					}
				}
			}				
	}
	

	
 
    
    public static double getModulo(Coordinate vector) {
    	return Math.sqrt(vector.x * vector.x + vector.y * vector.y);
    }
    
    
    public static boolean similarCoordinate(Coordinate coor1, Coordinate coor2) {
    	if (coor1.x == coor2.x && coor1.y == coor2.y) {
    		return true;
    	}
    	return false;
    }
    
    public static void writeVideoFile(int fileIndex) {
    	try
    	{
    	    String filename= fileOutputFolder + "VideoAfterProcess" + fileIndex + ".csv";
    	    FileWriter fw = new FileWriter(filename,false); //the true will append the new data
    	    for (int blobID = 0; blobID < blobVideoNumber; blobID++) {
    	    	for (int i = 0; i < countTimeVideoReport[blobID]; i++) {
    	    		fw.write("" + blobID + "," + peopleVideo[blobID][i].time + "," + peopleVideo[blobID][i].location.locationCoor.x + "," + peopleVideo[blobID][i].location.locationCoor.y);
    	    		
    	    		fw.write("\n");
    	    	}
    	    }    	        	        	    
    	    fw.close();
    	}
    	catch(IOException ioe)
    	{
    	    System.err.println("IOException: " + ioe.getMessage());
    	}
    }
    
	public static void main( String[ ] args ) {
		createMap();		
		for (int fileIndex = 1; fileIndex <= 23; fileIndex++) {
				System.out.println("-------------------" + fileIndex);
				initializeTheArray(fileIndex);						
				readVideoFile(fileIndex);		
				convertToPhysicalCoordinate(fileIndex);			
				writeVideoFile(fileIndex);
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
	}
}
