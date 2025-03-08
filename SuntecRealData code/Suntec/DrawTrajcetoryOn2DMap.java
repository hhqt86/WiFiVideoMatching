package SuntecRealData.Suntec;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class DrawTrajcetoryOn2DMap {
	public static int wifiIDInput = 28;
	static String wifiFolder = "src/SuntecRealData/RTLS_Comex2017_Sep_01/";
	//static String folderBlobAndFrameCoor = "src/SuntecRealData/"; //for local
	public static String folderOutputMatching = "src/SuntecRealData/OutputResult/";
	
	static MatchResult[][][] matchWifiResult = new MatchResult[Config.maximumWifiReportCount][SuntecMatching.numberOfFile][3];
	static NormalLocationCoordinate[][] peopleWifi;
	static int[][] matchWifiCount = new int[Config.maximumWifiReportCount][SuntecMatching.numberOfFile];
	static int numberOfMatchedWifi = 0;
	static int numberOfWifi = 0;
	public static String[] parseData(String data, int numberOfComponent, char separate) {
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
		return result;
	}
	
	public static void readOutputMatchingResult() {
	    Scanner scan;	    	  	    
	    for (int fileIndex = 0; fileIndex < SuntecMatching.numberOfFile; fileIndex++) {	    	
	    	
		    try {
		    	MatchResult[][][] matchVideoResult = new MatchResult[Config.maximumBlobVideoCount][SuntecMatching.numberOfFile][1000];
		    	int[][] matchVideoCount = new int[Config.maximumBlobVideoCount][SuntecMatching.numberOfFile];
		    	File file = new File(folderOutputMatching + "OutputMatching" + fileIndex + ".txt");
		        scan = new Scanner(file);	        	        
		        while (scan.hasNextLine()) {
		        	String data = scan.nextLine();
		        	String[] parseData = parseData(data, 10,' ');
		        	int wifiID = Integer.parseInt(parseData[0]);
		        	if (wifiID > numberOfMatchedWifi) {
		        		numberOfMatchedWifi = wifiID;
		        	}
		        	for (int i = 0; i < 3; i++) {
		        		if (Integer.parseInt(parseData[3 * i + 1]) > 0) {
		        			int video = Integer.parseInt(parseData[3 * i + 1]);
		        			int rankScore = Integer.parseInt(parseData[3 * i + 2]);
		        			int timeStamp = Integer.parseInt(parseData[3 * i + 3]);
		        			matchWifiResult[wifiID][fileIndex][matchWifiCount[wifiID][fileIndex]] = new MatchResult(video, rankScore, timeStamp);
		        			matchWifiCount[wifiID][fileIndex]++;
		        			matchVideoResult[video][fileIndex][matchVideoCount[video][fileIndex]] = new MatchResult(wifiID, rankScore, timeStamp);
		        			matchVideoCount[video][fileIndex]++;
		        		}
		        	}
		        }
		        scan.close();
		        
		    }
		    catch (FileNotFoundException e1) {
	            e1.printStackTrace();
		    }
	    }
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
	
    public static void readWifiAfterInterpolation() {
	    Scanner scan;	    
	    
	    int ID = 0;	 
	    numberOfWifi = 0;
	    peopleWifi = new NormalLocationCoordinate[Config.maximumWifiReportCount][10000];
	    try {
	    	File file = new File(wifiFolder +  "afterInterpolation.csv");
	        scan = new Scanner(file);
	        int count = 0;
	        while (scan.hasNextLine()) {
	        	String data = scan.nextLine();
	        	count++;

	        	String[] parseData = parseData(data);
	    
	        	ID = Integer.parseInt(parseData[0]);
	        	int time = Integer.parseInt(parseData[1]) % 10000;
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
	
    public static int[] transferToPixelCoordinate(int level, double X, double Y){
    	int[] result = new int[2];
    	Coordinate DimensionInPixel = new Coordinate(8476, 11982);
    	Coordinate DimensionInCM = new Coordinate(10.1, 14.3);
    	Coordinate oneCell = new Coordinate(0.23, 0.22);
    	Coordinate originalCoor = new Coordinate(1.01 * DimensionInPixel.x / DimensionInCM.x, 10.55 * DimensionInPixel.y / DimensionInCM.y);
    	
    	if (level == 4) {
    		result[0] = (int)((X + 29) * (oneCell.x * DimensionInPixel.x / DimensionInCM.x) + originalCoor.x);
    	}
    	else {
    		if (level == 6) {
    			result[0] = (int)((35 - X) * (oneCell.x * DimensionInPixel.x / DimensionInCM.x) + originalCoor.x);
    		}
    	}
    	result[1] = (int)(originalCoor.y - Y * (oneCell.y * DimensionInPixel.y /DimensionInCM.y));
    	return result;
    }
    
	public static void drawTrajectoryOn2DMap() {
		for (int fileIndex = 0; fileIndex < SuntecMatching.numberOfFile; fileIndex++) {
			System.out.println(fileIndex);
			for (int k = 0; k < matchWifiCount[wifiIDInput][fileIndex]; k++) {
				int matchedTime = matchWifiResult[wifiIDInput][fileIndex][k].timeStamp;
				int matchedFrame = TestFunctionAndGenerateGroundTrace.getFrameVideoAtTimeStamp(fileIndex, matchedTime);
				int blobID = matchWifiResult[wifiIDInput][fileIndex][k].signal;
				int rankScore = matchWifiResult[wifiIDInput][fileIndex][k].rankScore;
				int blobLevel = SuntecMatching.thisFileLevel[fileIndex];
				File fileMap2D = new File(folderOutputMatching + "/Suntec_L" + blobLevel + "_Coordinate_Small.jpg");
        		BufferedImage img;
				try {															
					img = ImageIO.read(fileMap2D);
			        Graphics2D graph = img.createGraphics();
			        int startDraw = matchedTime - 30;
			        int finishDraw = matchedTime + 30;
			        int alphaColor = 100;
			        
			        for (int time = startDraw; time < finishDraw; time++) {
			        	alphaColor = alphaColor + 2;
			        	graph.setColor(new Color(255,0,0,alphaColor));
			        	graph.setStroke(new java.awt.BasicStroke(5));					        
			        	double X = peopleWifi[wifiIDInput][time].locationCoor.x;
			        	double Y = peopleWifi[wifiIDInput][time].locationCoor.y;
			        	int wifiLevel = peopleWifi[wifiIDInput][time].level;			        	
			        	int newX = transferToPixelCoordinate(wifiLevel, X,Y)[0];
			        	int newY = transferToPixelCoordinate(wifiLevel, X,Y)[1];
			        	if (wifiLevel == blobLevel) {
			        		graph.fillOval(newX, newY, 50,50);			        		
			        	}
			        }			        	
			        graph.dispose();
	        		String outputPath = folderOutputMatching + "OutputBlob_NoBoudingBox/" + wifiIDInput + "/MatchedFrame"
	        		+ "/Trajectory"+ fileIndex + "_blob" + blobID + "_rank" + rankScore + "_frame" + matchedFrame + ".jpg";					        		
	        		File outputFile = new File(outputPath);
	        		if (!outputFile.getParentFile().exists()){
	        			outputFile.getParentFile().mkdirs();
	        		}
	        		ImageIO.write(img, "jpg", outputFile);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}
	
	public static void main(String args[]) {
		readOutputMatchingResult();
		readWifiAfterInterpolation();
		drawTrajectoryOn2DMap();
	}
}
