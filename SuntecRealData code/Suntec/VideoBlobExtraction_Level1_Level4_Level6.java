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


public class VideoBlobExtraction_Level1_Level4_Level6 {


	//---------------------------------------------------------------------------//
	
	
    static int startingHour;static int startingMinute;static int startingSecond;
    static int endingHour;static int endingMinute;static int endingSecond;
    /*static int startTimeStamp; 
    static int endTimeStamp;
    static int experimentNumber = 0;*/
    static int numberOfWifi;
    static int[] fpPointInTheArea = {43,44,45,47,48,49,50,51,52,53,54,55,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155};
	
    //static String videoFolder = "src/SuntecRealData/Video/"; //local
	//static String fileOutputFolder = "src/SuntecRealData/VideoProcessResult/"; //local
    static String videoFolder = "../"; //remote
	static String fileOutputFolder = "SuntecRealData/VideoProcessResultFull2Hour/"; //remote

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
	
	static MapReal[][] map_level_cam;
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
		startingHour = SuntecMatching.thisVideoStartHour[fileIndex];
		startingMinute = SuntecMatching.thisVideoStartMinute[fileIndex];
		startingSecond = SuntecMatching.thisVideoStartSecond[fileIndex];
		
		
		
		
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
	    	String fileVideoInput = "VideoLevel" + SuntecMatching.thisFileLevel[fileIndex] + "_Cam" + SuntecMatching.thisFileCamera[fileIndex]
	    			+ "_" + SuntecMatching.thisVideoStartHour[fileIndex] + "_" + SuntecMatching.thisVideoStartMinute[fileIndex] + "_" 
	    			+ SuntecMatching.thisVideoStartSecond[fileIndex] + "/results.txt"; // File location of the SMU video
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
		for (int i = 0; i < map_level_cam[level][camera].numberOfPhysicalVideoX; i++) {
			for (int j = 0; j < map_level_cam[level][camera].numberOfPhysicalVideoY; j++) {
				if (map_level_cam[level][camera].videoCoordinate[i][j] != null && distance(map_level_cam[level][camera].videoCoordinate[i][j], footLocation) < minDistance) {
					minDistance = distance(map_level_cam[level][camera].videoCoordinate[i][j], footLocation);
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
		int videoStartHour = SuntecMatching.thisVideoStartHour[fileIndex];
		int videoStartMinute = SuntecMatching.thisVideoStartMinute[fileIndex];
		int videoStartSecond = SuntecMatching.thisVideoStartSecond[fileIndex]; 
		int startTimeStamp = getTimeStamp(videoStartHour, videoStartMinute, videoStartSecond);//time stamp of the beginning of video
		
		return (startTimeStamp + frame / Config.videoFPS) % 10000; // video is 30 fps and only take the last 4 numbers of the timeStamp
		
	}
	
	public static void convertToPhysicalCoordinate(int fileIndex) {
		//(1920 x 1080)		
		int level = SuntecMatching.thisFileLevel[fileIndex];
		int camera = SuntecMatching.thisFileCamera[fileIndex];
		int previousTimeStepSave = -1;
		for (int blobID = 0; blobID < blobVideoNumber; blobID++) {
			for (int frame = blobDuration[blobID].left; frame <= blobDuration[blobID].right; frame++) {
				if (blobImageCoor[blobID][frame] != null) {
					int timeStep =getTimeStampVideo(fileIndex, frame); //video is 30 fps
					Coordinate footLocation = new Coordinate((int)(blobImageCoor[blobID][frame].x + blobImageCoor[blobID][frame].w / 2), (int)(blobImageCoor[blobID][frame].y + blobImageCoor[blobID][frame].h));
					if (blobPhysicalCoor[blobID][timeStep] == null && timeStep != previousTimeStepSave) {
						blobPhysicalCoor[blobID][timeStep] = convertToPhysicalCoor(level,camera, footLocation);
						//peopleVideo[blobID][timeStep] = getNearestLandMarkInt(level,blobPhysicalCoor[blobID][timeStep]);
						if (level == 4) {
							//Reason is that the coordinate of level 4 is different, stupid mistake
							Coordinate changeOriginalBLobPhysicalCoor = new Coordinate(8 - blobPhysicalCoor[blobID][timeStep].x,blobPhysicalCoor[blobID][timeStep].y);
							peopleVideo[blobID][countTimeVideoReport[blobID]] = new timeWithLocationCoordinate(level, changeOriginalBLobPhysicalCoor, timeStep);
						}
						else {
							peopleVideo[blobID][countTimeVideoReport[blobID]] = new timeWithLocationCoordinate(level, blobPhysicalCoor[blobID][timeStep], timeStep);
						}
						countTimeVideoReport[blobID]++;
						previousTimeStepSave = timeStep;
					}
				}
			}
		}				
	}

	public static void convertToPhysicalCoordinateTemp(int fileIndex) {
		//(1920 x 1080)		
		int level = SuntecMatching.thisFileLevel[fileIndex];
		int camera = SuntecMatching.thisFileCamera[fileIndex];
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
								peopleVideo[blobID][countTimeVideoReport[blobID]] = new timeWithLocationCoordinate(level, changeOriginalBLobPhysicalCoor, timeStep);
							}
							else {
								peopleVideo[blobID][countTimeVideoReport[blobID]] = new timeWithLocationCoordinate(level, blobPhysicalCoor[blobID][timeStep], timeStep);
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
		for (int fileIndex = 0; fileIndex < SuntecMatching.numberOfFile; fileIndex++) {
			System.out.println("-------------------" + fileIndex);
			initializeTheArray(fileIndex);						
			readVideoFile(fileIndex);		
			convertToPhysicalCoordinate(fileIndex);			
			writeVideoFile(fileIndex);			
		}
	}
	
	public static void createMap() {
		map_level_cam = new MapReal[SuntecMatching.numberOfLevel][SuntecMatching.numberOfCamera]; //7 level from 0 -> 6 and 8 camera position from 0 -> 7
		for (int i = 0; i < SuntecMatching.numberOfLevel; i++) {
			for (int j = 0; j < SuntecMatching.numberOfCamera; j++)
				map_level_cam[i][j] = new MapReal();
		}
		// For map level 1 cam 1 year 2017;
		map_level_cam[1][10].fpPointInThisMap = 87;
		map_level_cam[1][10].numberOfPhysicalVideoX = 21;
		map_level_cam[1][10].numberOfPhysicalVideoY = 6;
		map_level_cam[1][10].upLevel = 4;
		map_level_cam[1][10].downLevel = 1;
		map_level_cam[1][10].videoCoordinate = new Coordinate[map_level_cam[1][10].numberOfPhysicalVideoX][map_level_cam[1][10].numberOfPhysicalVideoY];
		
		map_level_cam[1][10].videoCoordinate[20][2] = new Coordinate(0.3,14.5);
		map_level_cam[1][10].videoCoordinate[20][3] = new Coordinate(8.2,15.5);
		map_level_cam[1][10].videoCoordinate[20][4] = new Coordinate(16,16);
		map_level_cam[1][10].videoCoordinate[19][2] = new Coordinate(1.6,14);
		map_level_cam[1][10].videoCoordinate[19][3] = new Coordinate(9,15);
		map_level_cam[1][10].videoCoordinate[19][4] = new Coordinate(15.6,15.2);
		map_level_cam[1][10].videoCoordinate[18][2] = new Coordinate(3.6,13.6);
		map_level_cam[1][10].videoCoordinate[18][3] = new Coordinate(9.6,14.5);
		map_level_cam[1][10].videoCoordinate[18][4] = new Coordinate(15.4,13.8);
		map_level_cam[1][10].videoCoordinate[17][2] = new Coordinate(4.3,13.3);
		map_level_cam[1][10].videoCoordinate[17][3] = new Coordinate(10.2,14.1);
		map_level_cam[1][10].videoCoordinate[17][4] = new Coordinate(15.3,14.3);
		map_level_cam[1][10].videoCoordinate[16][2] = new Coordinate(5.4,13);
		map_level_cam[1][10].videoCoordinate[16][3] = new Coordinate(10.2,13.7);
		map_level_cam[1][10].videoCoordinate[16][4] = new Coordinate(15.2,13.9);
		map_level_cam[1][10].videoCoordinate[16][5] = new Coordinate(20.5,13.5);
		map_level_cam[1][10].videoCoordinate[15][2] = new Coordinate(6.4,12.8);
		map_level_cam[1][10].videoCoordinate[15][3] = new Coordinate(11.1,13.4);
		map_level_cam[1][10].videoCoordinate[15][4] = new Coordinate(15.2,13.5);
		map_level_cam[1][10].videoCoordinate[15][5] = new Coordinate(10.9,13.2);
		map_level_cam[1][10].videoCoordinate[14][2] = new Coordinate(7.3,12.6);
		map_level_cam[1][10].videoCoordinate[14][3] = new Coordinate(11.5,13.1);
		map_level_cam[1][10].videoCoordinate[14][4] = new Coordinate(15.2,13.2);
		map_level_cam[1][10].videoCoordinate[14][5] = new Coordinate(18.9,12.7);
		map_level_cam[1][10].videoCoordinate[13][2] = new Coordinate(8,12.4);
		map_level_cam[1][10].videoCoordinate[13][3] = new Coordinate(11.7,12.9);
		map_level_cam[1][10].videoCoordinate[13][4] = new Coordinate(15.1,13);
		map_level_cam[1][10].videoCoordinate[13][5] = new Coordinate(18,12.3);
		map_level_cam[1][10].videoCoordinate[12][2] = new Coordinate(8.7,12.2);
		map_level_cam[1][10].videoCoordinate[12][3] = new Coordinate(12,12.6);
		map_level_cam[1][10].videoCoordinate[12][4] = new Coordinate(15.1,12.7);
		map_level_cam[1][10].videoCoordinate[11][2] = new Coordinate(9.3,12);
		map_level_cam[1][10].videoCoordinate[11][3] = new Coordinate(12.3,12.4);
		map_level_cam[1][10].videoCoordinate[11][4] = new Coordinate(15.1,12.5);
		map_level_cam[1][10].videoCoordinate[10][2] = new Coordinate(9.9,11.9);
		map_level_cam[1][10].videoCoordinate[10][3] = new Coordinate(12.6,12.2);
		map_level_cam[1][10].videoCoordinate[10][4] = new Coordinate(15,12.3);
		map_level_cam[1][10].videoCoordinate[9][2] = new Coordinate(10.4,11.8);
		map_level_cam[1][10].videoCoordinate[9][3] = new Coordinate(12.8,12);
		map_level_cam[1][10].videoCoordinate[9][4] = new Coordinate(15,12);
		map_level_cam[1][10].videoCoordinate[8][2] = new Coordinate(10.9,11.7);
		map_level_cam[1][10].videoCoordinate[8][3] = new Coordinate(13,11.9);
		map_level_cam[1][10].videoCoordinate[8][4] = new Coordinate(15,11.8);
		map_level_cam[1][10].videoCoordinate[7][2] = new Coordinate(11.3,11.6);
		map_level_cam[1][10].videoCoordinate[7][3] = new Coordinate(13.2,11.7);
		map_level_cam[1][10].videoCoordinate[7][4] = new Coordinate(15,11.7);
		map_level_cam[1][10].videoCoordinate[6][2] = new Coordinate(11.6,11.5);
		map_level_cam[1][10].videoCoordinate[6][3] = new Coordinate(13.4,11.6);
		map_level_cam[1][10].videoCoordinate[6][4] = new Coordinate(14.9,11.5);
		for (int i = 0; i < map_level_cam[1][10].numberOfPhysicalVideoX; i++) {
			for (int j = 0; j < map_level_cam[1][10].numberOfPhysicalVideoY; j++) {
				if (map_level_cam[1][10].videoCoordinate[i][j] != null) {
					map_level_cam[1][10].videoCoordinate[i][j].x = (map_level_cam[1][10].videoCoordinate[i][j].x / map_level_cam[1][10].videoDimensionInCM.x) * map_level_cam[1][10].videoResolution_BoundingBox.x;
					map_level_cam[1][10].videoCoordinate[i][j].y = (map_level_cam[1][10].videoCoordinate[i][j].y / map_level_cam[1][10].videoDimensionInCM.y) * map_level_cam[1][10].videoResolution_BoundingBox.y;
				}
			}
		}
		//For level 4 cam 1
		map_level_cam[4][1].fpPointInThisMap = 620;
		map_level_cam[4][1].goesUpPoint = new int[] {595,566};
		map_level_cam[4][1].fpPointInTheCameraArea = new int[]{43,44,45,47,48,49,50,51,52,53,54,55,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155};		
		map_level_cam[4][1].numberOfPhysicalVideoX = 9;
		map_level_cam[4][1].numberOfPhysicalVideoY = 4;
		map_level_cam[4][1].upLevel = 6;
		map_level_cam[4][1].downLevel = 1;
		map_level_cam[4][1].videoCoordinate = new Coordinate[map_level_cam[4][1].numberOfPhysicalVideoX][map_level_cam[4][1].numberOfPhysicalVideoY];		

		
		map_level_cam[4][1].videoCoordinate[1][0] = new Coordinate(7.4,14.8);
		map_level_cam[4][1].videoCoordinate[1][1] = new Coordinate(15,14.8);
		map_level_cam[4][1].videoCoordinate[1][2] = new Coordinate(21.9,14.8);
		map_level_cam[4][1].videoCoordinate[2][0] = new Coordinate(9.2,10.1);
		map_level_cam[4][1].videoCoordinate[2][1] = new Coordinate(14,10.1);
		map_level_cam[4][1].videoCoordinate[2][2] = new Coordinate(18.2,10.1);
		map_level_cam[4][1].videoCoordinate[3][0] = new Coordinate(10.3,7.3);
		map_level_cam[4][1].videoCoordinate[3][1] = new Coordinate(13.3,7.3);
		map_level_cam[4][1].videoCoordinate[3][2] = new Coordinate(16,7.3);
		map_level_cam[4][1].videoCoordinate[3][3] = new Coordinate(18.4,7.3);
		map_level_cam[4][1].videoCoordinate[4][0] = new Coordinate(10.8,5.7);
		map_level_cam[4][1].videoCoordinate[4][1] = new Coordinate(12.9,5.7);
		map_level_cam[4][1].videoCoordinate[4][2] = new Coordinate(14.8,5.7);
		map_level_cam[4][1].videoCoordinate[4][3] = new Coordinate(16.7,5.7);
		map_level_cam[4][1].videoCoordinate[5][0] = new Coordinate(11.3,4.8);
		map_level_cam[4][1].videoCoordinate[5][1] = new Coordinate(12.6,4.8);
		map_level_cam[4][1].videoCoordinate[5][2] = new Coordinate(14.2,4.8);
		map_level_cam[4][1].videoCoordinate[6][0] = new Coordinate(11.4,4.2);
		map_level_cam[4][1].videoCoordinate[6][1] = new Coordinate(12.5,4.2);
		map_level_cam[4][1].videoCoordinate[6][2] = new Coordinate(13.7,4.2);
		map_level_cam[4][1].videoCoordinate[7][0] = new Coordinate(11.6,3.7);
		map_level_cam[4][1].videoCoordinate[7][1] = new Coordinate(12.4,3.7);
		map_level_cam[4][1].videoCoordinate[7][2] = new Coordinate(13.4,3.7);		
		map_level_cam[4][1].videoCoordinate[8][0] = new Coordinate(11.6,3.5);
		map_level_cam[4][1].videoCoordinate[8][1] = new Coordinate(12.3,3.5);
		map_level_cam[4][1].videoCoordinate[8][1] = new Coordinate(13.2,3.5);
		
		for (int i = 0; i < map_level_cam[4][1].numberOfPhysicalVideoX; i++) {
			for (int j = 0; j < map_level_cam[4][1].numberOfPhysicalVideoY; j++) {
				if (map_level_cam[4][1].videoCoordinate[i][j] != null) {
					map_level_cam[4][1].videoCoordinate[i][j].x = (map_level_cam[4][1].videoCoordinate[i][j].x / map_level_cam[4][1].videoDimensionInCM.x) * map_level_cam[4][1].videoResolution_BoundingBox.x;
					map_level_cam[4][1].videoCoordinate[i][j].y = (map_level_cam[4][1].videoCoordinate[i][j].y / map_level_cam[4][1].videoDimensionInCM.y) * map_level_cam[4][1].videoResolution_BoundingBox.y;
				}
			}
		}
		//------------------------*************************--------------------------------

		//For level 4 cam 2
		
		map_level_cam[4][2].fpPointInThisMap = 620;
		map_level_cam[4][2].goesUpPoint = new int[] {595,566};
		map_level_cam[4][2].fpPointInTheCameraArea = new int[]{43,44,45,47,48,49,50,51,52,53,54,55,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155};		
		map_level_cam[4][2].numberOfPhysicalVideoX = 9;
		map_level_cam[4][2].numberOfPhysicalVideoY = 4;
		map_level_cam[4][2].upLevel = 6;
		map_level_cam[4][2].downLevel = 1;
		map_level_cam[4][2].videoCoordinate = new Coordinate[map_level_cam[4][2].numberOfPhysicalVideoX][map_level_cam[4][2].numberOfPhysicalVideoY];		

		
		map_level_cam[4][2].videoCoordinate[8][0] = new Coordinate(14.5,15.8);
		map_level_cam[4][2].videoCoordinate[8][1] = new Coordinate(3.4,15.3);
		map_level_cam[4][2].videoCoordinate[7][0] = new Coordinate(14.5,13.6);
		map_level_cam[4][2].videoCoordinate[7][1] = new Coordinate(5.8,13.3);
		map_level_cam[4][2].videoCoordinate[6][0] = new Coordinate(14.5,11.9);
		map_level_cam[4][2].videoCoordinate[6][1] = new Coordinate(8,11.6);
		map_level_cam[4][2].videoCoordinate[6][2] = new Coordinate(4.6,11);
		map_level_cam[4][2].videoCoordinate[5][0] = new Coordinate(14.5,10.6);
		map_level_cam[4][2].videoCoordinate[5][1] = new Coordinate(9.4,10.5);
		map_level_cam[4][2].videoCoordinate[5][2] = new Coordinate(6.3,10);
		map_level_cam[4][2].videoCoordinate[5][3] = new Coordinate(4.2,9.5);
		map_level_cam[4][2].videoCoordinate[4][0] = new Coordinate(14.5,9.8);
		map_level_cam[4][2].videoCoordinate[4][1] = new Coordinate(10.4,9.7);
		map_level_cam[4][2].videoCoordinate[4][2] = new Coordinate(7.4,9.3);
		map_level_cam[4][2].videoCoordinate[4][3] = new Coordinate(5.3,9);
		map_level_cam[4][2].videoCoordinate[3][0] = new Coordinate(14.5,9);
		map_level_cam[4][2].videoCoordinate[3][1] = new Coordinate(11.1,9);
		map_level_cam[4][2].videoCoordinate[3][2] = new Coordinate(8.3,8.9);
		map_level_cam[4][2].videoCoordinate[3][3] = new Coordinate(6,8.6);
		map_level_cam[4][2].videoCoordinate[2][0] = new Coordinate(14.5,8.5);
		map_level_cam[4][2].videoCoordinate[2][1] = new Coordinate(11.8,8.6);
		map_level_cam[4][2].videoCoordinate[2][2] = new Coordinate(9,8.5);
		map_level_cam[4][2].videoCoordinate[2][3] = new Coordinate(7,8.3);
		map_level_cam[4][2].videoCoordinate[1][0] = new Coordinate(14.5,8.2);
		map_level_cam[4][2].videoCoordinate[1][1] = new Coordinate(12.1,8.3);
		map_level_cam[4][2].videoCoordinate[1][2] = new Coordinate(9.5,8.2);
		map_level_cam[4][2].videoCoordinate[1][3] = new Coordinate(7.8,8);
		map_level_cam[4][2].videoCoordinate[0][0] = new Coordinate(14.5,7.9);
		map_level_cam[4][2].videoCoordinate[0][1] = new Coordinate(12.6,7.9);
		
		for (int i = 0; i < map_level_cam[4][2].numberOfPhysicalVideoX; i++) {
			for (int j = 0; j < map_level_cam[4][2].numberOfPhysicalVideoY; j++) {
				if (map_level_cam[4][2].videoCoordinate[i][j] != null) {
					map_level_cam[4][2].videoCoordinate[i][j].x = (map_level_cam[4][2].videoCoordinate[i][j].x / map_level_cam[4][2].videoDimensionInCM.x) * map_level_cam[4][2].videoResolution_BoundingBox.x;
					map_level_cam[4][2].videoCoordinate[i][j].y = (map_level_cam[4][2].videoCoordinate[i][j].y / map_level_cam[4][2].videoDimensionInCM.y) * map_level_cam[4][2].videoResolution_BoundingBox.y;
				}
			}
		}
		//------------------------*************************--------------------------------
		
		//For level 4 cam 3
		
		map_level_cam[4][3].fpPointInThisMap = 620;
		map_level_cam[4][3].goesUpPoint = new int[] {595,566};
		map_level_cam[4][3].fpPointInTheCameraArea = new int[]{43,44,45,47,48,49,50,51,52,53,54,55,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155};		
		map_level_cam[4][3].numberOfPhysicalVideoX = 15;
		map_level_cam[4][3].numberOfPhysicalVideoY = 4;
		map_level_cam[4][3].upLevel = 6;
		map_level_cam[4][3].downLevel = 1;
		map_level_cam[4][3].videoCoordinate = new Coordinate[map_level_cam[4][3].numberOfPhysicalVideoX][map_level_cam[4][3].numberOfPhysicalVideoY];		

		
		map_level_cam[4][3].videoCoordinate[14][0] = new Coordinate(16.8,15.1);
		map_level_cam[4][3].videoCoordinate[14][1] = new Coordinate(13,15.1);
		map_level_cam[4][3].videoCoordinate[14][2] = new Coordinate(8.2,15.1);
		map_level_cam[4][3].videoCoordinate[14][3] = new Coordinate(2.3,15.1);
		map_level_cam[4][3].videoCoordinate[13][0] = new Coordinate(15.3,11.1);
		map_level_cam[4][3].videoCoordinate[13][1] = new Coordinate(13.1,11.1);
		map_level_cam[4][3].videoCoordinate[13][2] = new Coordinate(10.2,11.1);
		map_level_cam[4][3].videoCoordinate[13][3] = new Coordinate(5.8,11.1);
		map_level_cam[4][3].videoCoordinate[12][0] = new Coordinate(14.8,9.5);
		map_level_cam[4][3].videoCoordinate[12][1] = new Coordinate(13.2,9.5);
		map_level_cam[4][3].videoCoordinate[12][2] = new Coordinate(11.1,9.5);
		map_level_cam[4][3].videoCoordinate[12][3] = new Coordinate(8.8,9.5);
		map_level_cam[4][3].videoCoordinate[11][0] = new Coordinate(14.3,8.4);
		map_level_cam[4][3].videoCoordinate[11][1] = new Coordinate(13.2,8.4);
		map_level_cam[4][3].videoCoordinate[11][2] = new Coordinate(11.7,8.4);
		map_level_cam[4][3].videoCoordinate[11][3] = new Coordinate(10,8.4);
		map_level_cam[4][3].videoCoordinate[10][0] = new Coordinate(14,7.5);
		map_level_cam[4][3].videoCoordinate[10][1] = new Coordinate(13.2,7.5);
		map_level_cam[4][3].videoCoordinate[10][2] = new Coordinate(12.2,7.5);
		map_level_cam[4][3].videoCoordinate[10][3] = new Coordinate(11,7.5);
		map_level_cam[4][3].videoCoordinate[9][0] = new Coordinate(13.8,7);
		map_level_cam[4][3].videoCoordinate[9][1] = new Coordinate(13.2,7);
		map_level_cam[4][3].videoCoordinate[9][2] = new Coordinate(12.4,7);
		map_level_cam[4][3].videoCoordinate[9][3] = new Coordinate(11.5,7);
		map_level_cam[4][3].videoCoordinate[8][0] = new Coordinate(13.7,6.7);
		map_level_cam[4][3].videoCoordinate[8][1] = new Coordinate(13.2,6.7);
		map_level_cam[4][3].videoCoordinate[8][2] = new Coordinate(12.6,6.7);
		map_level_cam[4][3].videoCoordinate[8][3] = new Coordinate(11.9,6.7);
		
		for (int i = 0; i < map_level_cam[4][3].numberOfPhysicalVideoX; i++) {
			for (int j = 0; j < map_level_cam[4][3].numberOfPhysicalVideoY; j++) {
				if (map_level_cam[4][3].videoCoordinate[i][j] != null) {
					map_level_cam[4][3].videoCoordinate[i][j].x = (map_level_cam[4][3].videoCoordinate[i][j].x / map_level_cam[4][3].videoDimensionInCM.x) * map_level_cam[4][3].videoResolution_BoundingBox.x;
					map_level_cam[4][3].videoCoordinate[i][j].y = (map_level_cam[4][3].videoCoordinate[i][j].y / map_level_cam[4][3].videoDimensionInCM.y) * map_level_cam[4][3].videoResolution_BoundingBox.y;
				}
			}
		}
		//------------------------*************************--------------------------------
		
		// For map level 6, cam 4;
		//map_level_cam[6][4].videoStartHour = 19; map_level_cam[6][4].videoStartMinute = 27; map_level_cam[6][4].videoStartSecond = 58;
		map_level_cam[6][4].fpPointInThisMap = 160;
		map_level_cam[6][4].goesDownPoint = new int[] {136,103};
		map_level_cam[6][4].fpPointInTheCameraArea = new int[]{43,44,45,47,48,49,50,51,52,53,54,55,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155};		
		map_level_cam[6][4].numberOfPhysicalVideoX = 8;
		map_level_cam[6][4].numberOfPhysicalVideoY = 5;
		map_level_cam[6][4].upLevel = 6;
		map_level_cam[6][4].downLevel = 4;
		map_level_cam[6][4].videoCoordinate = new Coordinate[map_level_cam[6][4].numberOfPhysicalVideoX][map_level_cam[6][4].numberOfPhysicalVideoY];		

		map_level_cam[6][4].videoCoordinate[0][1] = new Coordinate(14.3,15.5);// unit: cm
		map_level_cam[6][4].videoCoordinate[0][2] = new Coordinate(27.5,14.5);
		map_level_cam[6][4].videoCoordinate[1][1] = new Coordinate(14.3,10);
		map_level_cam[6][4].videoCoordinate[1][2] = new Coordinate(22,9.9);
		map_level_cam[6][4].videoCoordinate[2][1] = new Coordinate(14.3,7.5);
		map_level_cam[6][4].videoCoordinate[2][2] = new Coordinate(19.6,7.8);
		map_level_cam[6][4].videoCoordinate[2][3] = new Coordinate(23.5,7.5);
		map_level_cam[6][4].videoCoordinate[2][4] = new Coordinate(24.9,7.3);
		map_level_cam[6][4].videoCoordinate[3][1] = new Coordinate(14.3,6.1);
		map_level_cam[6][4].videoCoordinate[3][2] = new Coordinate(18.2,6.6);
		map_level_cam[6][4].videoCoordinate[3][3] = new Coordinate(21.7,6.6);
		map_level_cam[6][4].videoCoordinate[3][4] = new Coordinate(23.2,6.5);
		map_level_cam[6][4].videoCoordinate[4][1] = new Coordinate(14.3,6.1);
		map_level_cam[6][4].videoCoordinate[4][2] = new Coordinate(17.4,5.9);
		map_level_cam[6][4].videoCoordinate[4][3] = new Coordinate(20.5,6);
		map_level_cam[6][4].videoCoordinate[4][4] = new Coordinate(21.8,5.9);
		map_level_cam[6][4].videoCoordinate[5][1] = new Coordinate(14.3,4.9);
		map_level_cam[6][4].videoCoordinate[5][2] = new Coordinate(16.6,5.3);
		map_level_cam[6][4].videoCoordinate[6][1] = new Coordinate(14.3,4.7);
		map_level_cam[6][4].videoCoordinate[6][2] = new Coordinate(16.1,4.8);
		map_level_cam[6][4].videoCoordinate[7][1] = new Coordinate(14.3,4.4);
		map_level_cam[6][4].videoCoordinate[7][2] = new Coordinate(15.9,4.6);
		//transfer the unit from cm to pixel
		for (int i = 0; i < map_level_cam[6][4].numberOfPhysicalVideoX; i++) {
			for (int j = 0; j < map_level_cam[6][4].numberOfPhysicalVideoY; j++) {
				if (map_level_cam[6][4].videoCoordinate[i][j] != null) {
					map_level_cam[6][4].videoCoordinate[i][j].x = (map_level_cam[6][4].videoCoordinate[i][j].x / map_level_cam[6][4].videoDimensionInCM.x) * map_level_cam[6][4].videoResolution_BoundingBox.x;
					map_level_cam[6][4].videoCoordinate[i][j].y = (map_level_cam[6][4].videoCoordinate[i][j].y / map_level_cam[6][4].videoDimensionInCM.y) * map_level_cam[6][4].videoResolution_BoundingBox.y;
				}
			}
		}
		//------------------------*************************--------------------------------
		
		// For map level 6, cam 7;
		//map_level_cam[6][7].videoStartHour = 19; map_level_cam[6][7].videoStartMinute = 28; map_level_cam[6][7].videoStartSecond = 00;
		map_level_cam[6][7].fpPointInThisMap = 160;
		map_level_cam[6][7].goesDownPoint = new int[] {136,103};
		map_level_cam[6][7].fpPointInTheCameraArea = new int[]{43,44,45,47,48,49,50,51,52,53,54,55,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155};		
		map_level_cam[6][7].numberOfPhysicalVideoX = 16;
		map_level_cam[6][7].numberOfPhysicalVideoY = 5;
		map_level_cam[6][7].upLevel = 6;
		map_level_cam[6][7].downLevel = 4;
		map_level_cam[6][7].videoCoordinate = new Coordinate[map_level_cam[6][7].numberOfPhysicalVideoX][map_level_cam[6][7].numberOfPhysicalVideoY];		

		
		map_level_cam[6][7].videoCoordinate[8][1] = new Coordinate(17.2,15.6);// unit: cm
		map_level_cam[6][7].videoCoordinate[8][2] = new Coordinate(28.4,15.6);
		map_level_cam[6][7].videoCoordinate[9][1] = new Coordinate(21.5, 9.1);
		map_level_cam[6][7].videoCoordinate[9][2] = new Coordinate(27.5, 9.1);
		map_level_cam[6][7].videoCoordinate[10][1] = new Coordinate(23.6, 6.1);
		map_level_cam[6][7].videoCoordinate[10][2] = new Coordinate(27.1, 6.1);
		map_level_cam[6][7].videoCoordinate[11][1] = new Coordinate(24.7, 4.5);
		map_level_cam[6][7].videoCoordinate[11][2] = new Coordinate(26.9, 4.5);
		map_level_cam[6][7].videoCoordinate[12][1] = new Coordinate(25.2, 3.6);
		map_level_cam[6][7].videoCoordinate[12][2] = new Coordinate(26.8, 3.6);
		map_level_cam[6][7].videoCoordinate[13][1] = new Coordinate(25.6, 3.1);
		map_level_cam[6][7].videoCoordinate[13][2] = new Coordinate(26.7, 3.1);
		map_level_cam[6][7].videoCoordinate[13][3] = new Coordinate(27.3, 3.1);
		map_level_cam[6][7].videoCoordinate[14][0] = new Coordinate(23.9, 2.6);
		map_level_cam[6][7].videoCoordinate[14][1] = new Coordinate(26, 2.6);
		map_level_cam[6][7].videoCoordinate[14][2] = new Coordinate(26.7, 2.6);
		map_level_cam[6][7].videoCoordinate[14][3] = new Coordinate(27.2, 2.6);
		map_level_cam[6][7].videoCoordinate[14][4] = new Coordinate(27.6, 2.6);
		map_level_cam[6][7].videoCoordinate[15][0] = new Coordinate(24.2, 2.2);
		map_level_cam[6][7].videoCoordinate[15][1] = new Coordinate(26.2, 2.2);
		map_level_cam[6][7].videoCoordinate[15][2] = new Coordinate(26.6, 2.2);
		map_level_cam[6][7].videoCoordinate[15][3] = new Coordinate(27, 2.2);
		map_level_cam[6][7].videoCoordinate[15][4] = new Coordinate(27.5, 2.2);
		//transfer the unit from cm to pixel
		for (int i = 0; i < map_level_cam[6][4].numberOfPhysicalVideoX; i++) {
			for (int j = 0; j < map_level_cam[6][4].numberOfPhysicalVideoY; j++) {
				if (map_level_cam[6][7].videoCoordinate[i][j] != null) {
					map_level_cam[6][7].videoCoordinate[i][j].x = (map_level_cam[6][7].videoCoordinate[i][j].x / map_level_cam[6][7].videoDimensionInCM.x) * map_level_cam[6][7].videoResolution_BoundingBox.x;
					map_level_cam[6][7].videoCoordinate[i][j].y = (map_level_cam[6][7].videoCoordinate[i][j].y / map_level_cam[6][7].videoDimensionInCM.y) * map_level_cam[6][7].videoResolution_BoundingBox.y;
				}
			}
		}



		
		
		
	}
}
