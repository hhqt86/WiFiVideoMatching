package SuntecRealData;


import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Scanner;


public class Suntec_Level4 {
    static int startingHour;static int startingMinute;static int startingSecond;
    static int endingHour;static int endingMinute;static int endingSecond;
    static int experimentNumber = 0; // Experiment length from the SMU video dataset

	
    static String videoWifiFolder = "src/SuntecRealData/OutputLevel4Camera2R/";
	static String fileVideoInput = "resultRight.txt"; // File location of the SMU video
	static String fileWifiInput = "location_archival_rtls_2017_9_1.csv";
	static String fileOutputFolder = "output/";

	static int frameNumber = 0; // Number of used frame
	static int blobNumberGen = 0; //Number of blob
	static BoundingBoxImageCoor[][] blobImageCoor = new BoundingBoxImageCoor[Config.maximumBlobCount][Config.maximumFrameCount]; // Coordinate of all the bounding box in the SMU video
	static Coordinate[][] blobPhysicalCoor = new Coordinate[Config.maximumBlobCount][Config.maximumFrameCount];
	//static BoundingBoxImageCoor[][] blobImageCalibration = new BoundingBoxImageCoor[Config.maximumBlobCount][Config.maximumFrameCount]; // Coordinate of all the bounding box in the SMU video
	static int[][] peopleVideo;
	static Coordinate[][] peopleVideoCoor;
	static int[][] peopleWifi;
	static int[][] peopleWifiWithMissingData;
	static double[] percentOfMissingWifiData;
	static Coordinate[][] peopleWifiCoor;
	static boolean[] blobVideoAppear;
	static int blobVideoNumber = 0;  // Number of blobs in the SMU video dataset
	static int blobWifiNumber = 0;
	static BlobDuration[] blobDuration = new BlobDuration[Config.maximumBlobCount];
	
	static MapRealLevel4 map;
	
	static double distance(Coordinate point1, Coordinate point2) {
		return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
	}
	
	static void initializeTheArray() {
	    //startingHour = getComponentOfWifiFileData(3); startingMinute = getComponentOfWifiFileData(4); startingSecond = getComponentOfWifiFileData(5);
	    //endingHour = getComponentOfWifiFileData(7); endingMinute = getComponentOfWifiFileData(8); endingSecond = getComponentOfWifiFileData(9);
		startingHour = 19; startingMinute = 34; startingSecond = 30;
		endingHour = 19; endingMinute = 43; endingSecond = 0;
	    experimentNumber = ((endingHour - startingHour) * 3600 + (endingMinute - startingMinute) * 60 + (endingSecond - startingSecond)) / 5 + 1;
		peopleVideo = new int[Config.maximumBlobCount][experimentNumber];
		peopleVideoCoor = new Coordinate[Config.maximumBlobCount][experimentNumber];
		peopleWifi = new int[Config.maximumBlobCount][experimentNumber];
		peopleWifiWithMissingData = new int[Config.maximumBlobCount][experimentNumber];
		peopleWifiCoor = new Coordinate [Config.maximumBlobCount][experimentNumber];
		for (int i = 0; i < Config.maximumBlobCount; i++) {
			blobDuration[i] = new BlobDuration(Integer.MAX_VALUE,0);
			for (int j = 0; j < experimentNumber; j++) {
				peopleVideo[i][j] = -1;
				peopleWifi[i][j] = -1;
				peopleWifiWithMissingData[i][j] = -1;
				peopleVideoCoor[i][j] = new Coordinate(Config.noPoint.x, Config.noPoint.y);
				peopleWifiCoor[i][j] = new Coordinate(Config.noPoint.x, Config.noPoint.y);
			}
		}

	}
	
	static void readVideoFile() {		
		Scanner scan;
		ParserVideo parser;
	    try {
	    	File file = new File(videoWifiFolder+fileVideoInput);
	        scan = new Scanner(file);
	        scan.nextLine();
	        String data;
	        while (scan.hasNextLine()) {
	        	data = scan.nextLine();
	        	parser = new ParserVideo(data);
	        	int frame = parser.getFrameID();
	        	int blobID = parser.getBlobID();
	        	if (frame > frameNumber) {
	        		frameNumber = frame;
	        	}
	        	if (blobID > blobNumberGen) {
	        		blobNumberGen = blobID;
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
   		peopleVideo = new int[blobNumberGen][experimentNumber];
   		blobVideoAppear = new boolean[blobNumberGen];
   		for (int i = 0; i < blobNumberGen; i++) {
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
	
	public static void convertToPhysicalCoordinate() {
		//(1920 x 1080)		
		for (int blobID = 0; blobID < blobNumberGen; blobID++) {
			if (blobID == 5) {
				blobID = 5;
				for (int frame = blobDuration[blobID].left; frame <= blobDuration[blobID].right; frame++) {
					if (blobImageCoor[blobID][frame] != null) {
						if (frame == 50) {
						frame = 50;						
						Coordinate footLocation = new Coordinate((int)(blobImageCoor[blobID][frame].x + blobImageCoor[blobID][frame].w / 2), (int)(blobImageCoor[blobID][frame].y + blobImageCoor[blobID][frame].h));
						blobPhysicalCoor[blobID][frame] = convertToPhysicalCoor(footLocation);
						}
					}
				}
			}
		}
		
	}
	
	public static void calculateNumberOfBlobIn40Seconds() {
		int frameCount = 1200;
		int result = 0;
		Coordinate centerPoint = new Coordinate(5, 1);
		for (int i = 0; i < frameNumber; i++) {
			for (int blobID = 0; blobID < blobNumberGen; blobID++) {
				if (blobPhysicalCoor[blobID][i] != null && distance(blobPhysicalCoor[blobID][i], centerPoint) < 1.5) {
					result++;
					break;
				}
			}
		}
		System.out.println(result);
	}
	
	public static void matching() {
		
	}
	
	public static void main( String[ ] args ) {
		map = new MapRealLevel4();
		initializeTheArray();
		readVideoFile();
		convertToPhysicalCoordinate();
		//readWifiData();
		calculateNumberOfBlobIn40Seconds();
		matching();
	}
}
