package SuntecRealData;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class DepthAlgo_DistanceToCameraLevel6 {
	static Coordinate videoResolution = new Coordinate(1920, 1080); 
	static String folderInput = "src/SuntecRealData/";
	//static String fileVideoInput = "OutputLevel6Camera4L/results.txt";
	static int startFrameLeft = 50; static int startFrameRight = 2136;
	static int endFrameLeft = 7550; static int endFrameRight = 9636; //examine in 5 minutes, and the analyzed video is 25 fps;
	static Coordinate whitePilarRight = new Coordinate(1060,335); //cot trang co dinh o phia xa
	static Coordinate trashBinRight = new Coordinate (1416, 618); //goc cua vector o day
	static int borderLineAtDifferentLevelXRight = 800;
	static int borderLineAtDifferentLevelXLeft = 840;
	static Coordinate whitePilarLeft = new Coordinate(1132,372); //cot trang co dinh o phia xa
	static Coordinate trashBinLeft = new Coordinate (1470, 662); //goc cua vector o day

	ParserVideo parser;
	static BoundingBoxImageCoorWithID[] blobImageCoorLeft = new BoundingBoxImageCoorWithID[Config.maximumBlobCount];
	static BoundingBoxImageCoorWithID[] blobImageCoorRight = new BoundingBoxImageCoorWithID[Config.maximumBlobCount];
	
	public static double getModule(Coordinate vector) {
		return Math.sqrt(vector.x * vector.x + vector.y * vector.y);
	}
	
	static double distance(Coordinate point1, Coordinate point2) {
		return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
	}
	
	public static double findRelativeAngleRight(BoundingBoxImageCoorWithID blob) {

		Coordinate blobCoor = new Coordinate(blob.x + (blob.w / 2), blob.y + blob.h);
		Coordinate vector1 = new Coordinate(whitePilarRight.x - trashBinRight.x,whitePilarRight.y - trashBinRight.y);
		Coordinate vector2 = new Coordinate(blobCoor.x - trashBinRight.x,blobCoor.y - trashBinRight.y);
		//a = atan2d(x1*y2-y1*x2,x1*x2+y1*y2);
		double angle = Math.atan2(vector1.x * vector2.y - vector1.y * vector2.x, vector1.x * vector2.x + vector1.y * vector2.y);
		return angle;
	}

	public static double findRelativeAngleLeft(BoundingBoxImageCoorWithID blob) {
		Coordinate blobCoor = new Coordinate(blob.x + (blob.w / 2), blob.y + blob.h);
		Coordinate vector1 = new Coordinate(whitePilarLeft.x - trashBinLeft.x,whitePilarLeft.y - trashBinLeft.y);
		Coordinate vector2 = new Coordinate(blobCoor.x - trashBinLeft.x,blobCoor.y - trashBinLeft.y);
		//a = atan2d(x1*y2-y1*x2,x1*x2+y1*y2);
		double angle = Math.atan2(vector1.x * vector2.y - vector1.y * vector2.x, vector1.x * vector2.x + vector1.y * vector2.y);
		return angle;
	}

	
	public static double depthAlgorithm(BoundingBoxImageCoorWithID left,BoundingBoxImageCoorWithID right) {
		Coordinate centerLeft = new Coordinate(left.x + left.w / 2, left.y + left.h / 2);
		Coordinate centerRight = new Coordinate(right.x + right.w / 2, right.y + right.h / 2);		
		double B = 0.107; //0.107m = 10.7cm
		double f = 0.00266; //0.00266m = 2.66mm;
		double xl = centerLeft.x - videoResolution.x / 2;
		double xr = centerRight.x - videoResolution.x / 2;
		double Z = (B * f) / (xl - xr);
		return Z;
	}
	
	public static boolean satisfyDistance(BoundingBoxImageCoorWithID left,BoundingBoxImageCoorWithID right) {
		Coordinate centerRight = new Coordinate(right.x + right.w / 2, right.y + right.h);
		Coordinate centerLeft = new Coordinate(left.x + left.w / 2, left.y + left.h);
		double distanceRight = distance(trashBinRight, centerRight);
		double distanceLeft = distance(trashBinLeft, centerLeft);
		if (Math.abs(distanceRight - distanceLeft) < 100) {//~1.5 cm  
			return true;
		}
		else {
			return false;
		}
	}
	
	public static void readVideoFile() {
		String fileVideoInput;
		Scanner scanL;
		Scanner scanR;
	    //initialize(people);
		ParserVideo parserL = new ParserVideo("");
		ParserVideo parserR = new ParserVideo("");
	    try {
	    	File fileL = new File(folderInput + "OutputLevel6Camera4R/resultRight.txt");
	        scanL = new Scanner(fileL);
	    	File fileR = new File(folderInput + "OutputLevel6Camera4L/resultLeft.txt");
	        scanR = new Scanner(fileR);
	        int currentFrameLeft = 1;
	        int currentFrameRight = 1;
	        String dataL = "";
	        String dataR = "";
	        while (currentFrameLeft < startFrameLeft) {
	        	dataL =  scanL.nextLine();
	        	parserL = new ParserVideo(dataL);
	        	currentFrameLeft = parserL.getFrameID();
	        }
	        while (currentFrameRight < startFrameRight) {
	        	dataR =  scanR.nextLine();
	        	parserR = new ParserVideo(dataR);
	        	currentFrameRight = parserR.getFrameID();
	        }
	        boolean finish = false;
	        while (!finish) {
	        	int countL = 0;
	        	do {
	        		currentFrameLeft = parserL.getFrameID();
	        		blobImageCoorLeft[countL] = new BoundingBoxImageCoorWithID(parserL.getBlobID(), parserL.getX(), parserL.getY(), parserL.getW(), parserL.getH());
	        		dataL = scanL.nextLine();
	        		parserL = new ParserVideo(dataL);
	        		countL++;
	        	}
	        	while(parserL.getFrameID() == currentFrameLeft);
	        	int countR = 0;
	        	do {
	        		currentFrameRight = parserR.getFrameID();
	        		if (currentFrameRight == endFrameRight) {
	        			finish = true;
	        		}
	        		blobImageCoorRight[countR] = new BoundingBoxImageCoorWithID(parserR.getBlobID(), parserR.getX(), parserR.getY(), parserR.getW(), parserR.getH());
	        		dataR = scanR.nextLine();
	        		parserR = new ParserVideo(dataR);
	        		countR++;
	        	}
	        	while(parserR.getFrameID() == currentFrameRight);
	        	for (int right = 0; right < countR; right++) {
	        		if (blobImageCoorRight[right].x + blobImageCoorRight[right].w / 2 > borderLineAtDifferentLevelXRight) {
		        		double minAngleDifference = Integer.MAX_VALUE;
		        		int saveLeft = -1;
		        		if (blobImageCoorRight[right].ID == 22) {
		        			blobImageCoorRight[right].ID = 22;
		        		}
		        		
		        		for (int left = 0; left < countL; left++) {
		        			if (blobImageCoorLeft[left].x + blobImageCoorLeft[left].w / 2 > borderLineAtDifferentLevelXLeft) {
				        		if (blobImageCoorLeft[left].ID == 37) {
				        			blobImageCoorLeft[left].ID = 37;
				        		}
				        		if (blobImageCoorLeft[left].ID == 40) {
				        			blobImageCoorLeft[left].ID = 40;
				        		}
			        			double angleLeft = findRelativeAngleLeft(blobImageCoorLeft[left]);
			        			double angleRight = findRelativeAngleRight(blobImageCoorRight[right]);
			        			if (Math.abs(angleLeft - angleRight) < minAngleDifference && satisfyDistance(blobImageCoorLeft[left],blobImageCoorRight[right])) {
			        				minAngleDifference = Math.abs(angleLeft - angleRight);
			        				saveLeft = left;
			        			}
		        			}
		        		}
		        		System.out.println(blobImageCoorLeft[saveLeft].ID + " " + blobImageCoorRight[right].ID);
		        		double distanceToCamera = depthAlgorithm(blobImageCoorLeft[saveLeft], blobImageCoorRight[right]);
		        		System.out.println(distanceToCamera);
		        		System.out.println("-------------------");
	        		}
	        	}
	        	countR = 0;
	        }
   		scanL.close();   		
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }
		
	}
	
	
	public static void main( String[ ] args ) {
		readVideoFile();
	}
	
}
