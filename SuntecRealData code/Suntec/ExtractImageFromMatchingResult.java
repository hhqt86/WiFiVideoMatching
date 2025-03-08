package SuntecRealData.Suntec;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;



/*import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;*/

class MatchResult {
	public int signal;
	public int rankScore;
	public int timeStamp;
	public MatchResult(int signal, int rankScore, int timeStamp) {
		this.signal = signal;
		this.rankScore = rankScore;
		this.timeStamp = timeStamp;
	}
}

public class ExtractImageFromMatchingResult {
	static String folderOutputMatching = "src/SuntecRealData/OutputResultNew40/OutputMatching40Trace/"; //insert "src/ when run at local 
	static String folderBlobAndFrameCoor = "../"; //for remote
	//static String folderBlobAndFrameCoor = "src/SuntecRealData/AllVideo/"; //for local
	static String outputPath = folderBlobAndFrameCoor + "OutputBlobNew40_NoBoudingBox/";
	static MatchResult[][][] matchWifiResult = new MatchResult[Config.maximumWifiReportCount][SuntecMatching.numberOfFile][3];
	static int[][] matchWifiCount = new int[Config.maximumWifiReportCount][SuntecMatching.numberOfFile];
	static int numberOfMatchedWifi = 0;
	
	
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
		/*if (index < numberOfComponent - 1) {
			return null;
		}*/
		return result;
	}
	
	public static String getFrameFile(String folder, int frameID) {
		//Use first one for Video with bounding box;
		/*String file = "";
		if (frameID < 10) {
			file = "0000" + frameID;
		}
		else {
			if (frameID < 100) {
				file = "000" + frameID;
			}
			else {
				if (frameID < 1000) {
					file = "00" + frameID;
				}
				else {
					if (frameID < 10000) {
						file = "0" + frameID;
					}
					else {
						file = "" + frameID;
					}
				}
			}
		}*/
		String file = "" + frameID;
		return folder + file + ".jpg";
	}
	
	public static void readOutputMatching() {
	    Scanner scan;	    	  	    
	    for (int fileIndex = 0; fileIndex < SuntecMatching.numberOfFile - 1; fileIndex++) {	    	
	    	System.out.println(fileIndex);
		    try {
		    	MatchResult[][][] matchVideoResult = new MatchResult[Config.maximumBlobVideoCount][SuntecMatching.numberOfFile][1000];
		    	int[][] matchVideoCount = new int[Config.maximumBlobVideoCount][SuntecMatching.numberOfFile];
		    	File file = new File(folderOutputMatching + "OutputMatching" + fileIndex + ".txt");
		        scan = new Scanner(file);	  
		        scan.nextLine();
		        while (scan.hasNextLine()) {
		        	String data = scan.nextLine();
		        	String[] parseData = parseData(data, 19,' ');
		        	if (parseData != null) {
			        	int wifiID = Integer.parseInt(parseData[0]);
			        	if (wifiID > numberOfMatchedWifi) {
			        		numberOfMatchedWifi = wifiID;
			        	}
			        	for (int i = 0; i < 3; i++) {
			        		if (parseData[6 * i + 1] != null) {
			        			int video = Integer.parseInt(parseData[6 * i + 1]);
			        			int rankScore = Integer.parseInt(parseData[6 * i + 5]);
			        			int timeStamp = Integer.parseInt(parseData[6 * i + 6]);
			        			matchWifiResult[wifiID][fileIndex][matchWifiCount[wifiID][fileIndex]] = new MatchResult(video, rankScore, timeStamp);
			        			matchWifiCount[wifiID][fileIndex]++;
			        			matchVideoResult[video][fileIndex][matchVideoCount[video][fileIndex]] = new MatchResult(wifiID, rankScore, timeStamp);
			        			matchVideoCount[video][fileIndex]++;
			        		}
			        	}
		        	}
		        }
		        scan.close();
		    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
		    }
	    }
		        //----------------------------------------
		        /*int[] countFile = new int[numberOfMatchedWifi + 1];
		    	File file1 = new File(folderBlobAndFrameCoor + "VideoLevel" + SuntecMatching.thisFileLevel[fileIndex] + "_Cam" + SuntecMatching.thisFileCamera[fileIndex] + 
		    			"_" + SuntecMatching.thisVideoStartHour[fileIndex] + "_" + SuntecMatching.thisVideoStartMinute[fileIndex] + "_" + SuntecMatching.thisVideoStartSecond[fileIndex]
		    			+ "/resultsUniqueBlobID.txt");
		        scan = new Scanner(file1);
		        Scanner scan2;
		        //Remove Original For the bounding box result;
		    	String folderfile2 = folderBlobAndFrameCoor + "File" + fileIndex + "_OriginalVideoLevel" + SuntecMatching.thisFileLevel[fileIndex] + "_Cam" + SuntecMatching.thisFileCamera[fileIndex] + 
		    			"_" + SuntecMatching.thisVideoStartHour[fileIndex] + "_" + SuntecMatching.thisVideoStartMinute[fileIndex] + "_" + SuntecMatching.thisVideoStartSecond[fileIndex]
		    			+ "/frame/";

		        while (scan.hasNextLine()) {
		        	String data = scan.nextLine();
		        	String[] parseData = parseData(data, 10,',');
		        	int blobID = Integer.parseInt(parseData[1]);

		        	if (matchVideoCount[blobID][fileIndex] > 0) {
		        		for (int k = 0; k < matchVideoCount[blobID][fileIndex]; k++) {
			        		int wifiID = matchVideoResult[blobID][fileIndex][k].signal;
			        		int rankScore = matchVideoResult[blobID][fileIndex][k].rankScore;
			        		int frameID = Integer.parseInt(parseData[0]);
			        		BoundingBoxImageCoor blobCoor = new BoundingBoxImageCoor(Double.parseDouble(parseData[2]), Double.parseDouble(parseData[3]), Double.parseDouble(parseData[4]), Double.parseDouble(parseData[5]));
			        		String file2 = getFrameFile(folderfile2, frameID);
			        		
			        		
			        		BufferedImage img;
							try {
								img = ImageIO.read(new File(file2));
							
								int X = (int)(blobCoor.x * 3840.0 / 1920 ) ;
								int Y = (int)(blobCoor.y * 2160.0 / 1080 );
								int width = (int)(blobCoor.w * 3840.0 / 1920);
								int height = (int)(blobCoor.h * 2160.0 / 1080);
								if (X + width >= new MapReal().videoResolution_NoBoundingBox.x) {
									width = (int)new MapReal().videoResolution_NoBoundingBox.x - X;
								}
								if (Y + height >= new MapReal().videoResolution_NoBoundingBox.y) {
									height = (int)new MapReal().videoResolution_NoBoundingBox.y - Y;
								}
								if (X > 0 && Y > 0 && X < new MapReal().videoResolution_NoBoundingBox.x && Y < new MapReal().videoResolution_NoBoundingBox.y) {
					        		BufferedImage subimage = img.getSubimage(X, Y, width, height);
					        		String outputPath = folderBlobAndFrameCoor + "OutputBlob_NoBoudingBox/" + wifiID + "/Cam" + fileIndex + "_" + blobID + "_" + rankScore + ".jpg";
					        		countFile[wifiID]++;
					        		File outputFile = new File(outputPath);
					        		if (!outputFile.getParentFile().exists()){
					        			outputFile.getParentFile().mkdirs();
					        		}
					        		ImageIO.write(subimage, "jpg", outputFile);
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		        		}
		        	}
		        }
		    } catch (FileNotFoundException e1) {
		            e1.printStackTrace();
		    }
	    	
		}*/
	}
	
	static int getTimeStamp (int hour, int minute, int second) {
		int result = 0;
		int beginTimeOfTheDay = 1504195200;
		result = beginTimeOfTheDay + hour * 3600 + minute * 60 + second;
		return result;
	}
	
	public static int getFrameVideoAtTimeStamp(int fileIndex, int timeStamp) {
		int videoStartHour = SuntecMatching.thisVideoStartHour[fileIndex];
		int videoStartMinute = SuntecMatching.thisVideoStartMinute[fileIndex];
		int videoStartSecond = SuntecMatching.thisVideoStartSecond[fileIndex]; 
		int startTimeStamp = getTimeStamp(videoStartHour, videoStartMinute, videoStartSecond) % 10000;//time stamp of the beginning of video
		return ((timeStamp - startTimeStamp) * Config.videoFPS);
	}
	
	public static void OutputFrameMatchingWithBBoxForAnalyse() {
		Scanner scan;	
		try {
			for (int wifiID = 0; wifiID < numberOfMatchedWifi; wifiID++) {	
			//for (int wifiID = 7; wifiID <= 7; wifiID++) {	
				System.out.println("Wifi: " + wifiID);
				for (int fileIndex = 0; fileIndex < SuntecMatching.numberOfFile - 1; fileIndex++) {					
					System.out.println("FileIndex:" + fileIndex);
					if (matchWifiCount[wifiID][fileIndex] > 0) {
						for (int k = 0; k < matchWifiCount[wifiID][fileIndex]; k++) {
							int matchBlobID = matchWifiResult[wifiID][fileIndex][k].signal;
							int matchRankScore = matchWifiResult[wifiID][fileIndex][k].rankScore;
							int matchTimeStamp = matchWifiResult[wifiID][fileIndex][k].timeStamp;
							int matchFrame = getFrameVideoAtTimeStamp(fileIndex, matchTimeStamp);
							int foundFrame = -1;
							int blobID = -1;
							BoundingBoxImageCoor blobCoor = new BoundingBoxImageCoor(-1, -1, -1, -1);
							///Read the blob information: frame, coordinate
							File fileToTakeCoor = new File(folderBlobAndFrameCoor + "VideoLevel" + SuntecMatching.thisFileLevel[fileIndex] + "_Cam" + SuntecMatching.thisFileCamera[fileIndex] + 
					    			"_" + SuntecMatching.thisVideoStartHour[fileIndex] + "_" + SuntecMatching.thisVideoStartMinute[fileIndex] + "_" + SuntecMatching.thisVideoStartSecond[fileIndex]
					    			+ "/results.txt");
					        scan = new Scanner(fileToTakeCoor);
					        int count = 0;
					        int minDistanceMatchAndFoundFrame = Integer.MAX_VALUE;
					        while (scan.hasNextLine()) {
					        	String data = scan.nextLine();
					        	count++;
					        	int frame = Integer.parseInt(data.substring(0, data.indexOf(',')));
					        	int secondCommaPosition = data.indexOf(',', data.indexOf(',') + 1);
					        	blobID = Integer.parseInt(data.substring(data.indexOf(',') + 1, secondCommaPosition));
					        	if (blobID == matchBlobID && Math.abs(frame - matchFrame) < minDistanceMatchAndFoundFrame) { 
					        		String[] parseData = parseData(data, 10,',');
					        		blobCoor = new BoundingBoxImageCoor(Double.parseDouble(parseData[2]), Double.parseDouble(parseData[3]), Double.parseDouble(parseData[4]), Double.parseDouble(parseData[5]));
					        		minDistanceMatchAndFoundFrame = Math.abs(frame - matchFrame);
					        		foundFrame = frame;
					        		if (minDistanceMatchAndFoundFrame < 20) {
					        			break;
					        		}
					        	}
					        	
					        }		        		      		        
					        scan.close();
					        //Take the frame and draw the bounding box//
					    	String folderToTakeOriginalFrame = folderBlobAndFrameCoor + "File" + fileIndex + "_OriginalVideoLevel" + SuntecMatching.thisFileLevel[fileIndex] + "_Cam" + SuntecMatching.thisFileCamera[fileIndex] + 
					    			"_" + SuntecMatching.thisVideoStartHour[fileIndex] + "_" + SuntecMatching.thisVideoStartMinute[fileIndex] + "_" + SuntecMatching.thisVideoStartSecond[fileIndex]
					    			+ "/frame/";
					    	String fileToTakeOriginalFrame = getFrameFile(folderToTakeOriginalFrame, foundFrame);
			        		BufferedImage img;
							try {															
								int X = (int)(blobCoor.x * 3840.0 / 1920 ) ;
								int Y = (int)(blobCoor.y * 2160.0 / 1080 );
								int width = (int)(blobCoor.w * 3840.0 / 1920);
								int height = (int)(blobCoor.h * 2160.0 / 1080);
								if (X + width >= new MapReal().videoResolution_NoBoundingBox.x) {
									width = (int)new MapReal().videoResolution_NoBoundingBox.x - X;
								}
								if (Y + height >= new MapReal().videoResolution_NoBoundingBox.y) {
									height = (int)new MapReal().videoResolution_NoBoundingBox.y - Y;
								}
								if (X > 0 && Y > 0 && X < new MapReal().videoResolution_NoBoundingBox.x && Y < new MapReal().videoResolution_NoBoundingBox.y) {
									img = ImageIO.read(new File(fileToTakeOriginalFrame));
							        Graphics2D graph = img.createGraphics();
							        graph.setColor(Color.RED);	
							        graph.setStroke(new java.awt.BasicStroke(15));
							        graph.drawRect(X - 3,Y - 3,width + 5,height + 7);
							        graph.dispose();
					        		String fileOutputPath = outputPath + wifiID + "/MatchedFrame"
					        		+ "/Cam"+ fileIndex + "_blob" + matchBlobID + "_rank" + matchRankScore + "_frame" + foundFrame + ".jpg";					        		
					        		File outputFile = new File(fileOutputPath);
					        		if (!outputFile.getParentFile().exists()){
					        			outputFile.getParentFile().mkdirs();
					        		}
					        		ImageIO.write(img, "jpg", outputFile);
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}					
				}
			}
		} catch (FileNotFoundException e1) {
            e1.printStackTrace();
		}
	}
	
	public static void countNumberOfMatchFound2HourVideo() {
		// Test on the real dataset to see how many percent we can find for the match.
		// Since it runs too slow with many blobs, so only tested with 2 cameras and the result is 146
		// Estimate the full number of match will be 146 * 2.5 * 3 (since whole level is 2.5 times the areas cover by 2 cameras, and we have 3 levels 1,4,6)
		// Found match = 83%
		boolean checkWiFi[] = new boolean[10000];
		int result = 0;
	    Scanner scan;	    	  	    
	    for (int fileIndex = 0; fileIndex < SuntecMatching.numberOfFile - 2; fileIndex++) {	    	
	    	System.out.println(fileIndex);
		    try {
		    	File file = new File(folderOutputMatching + "OutputResultFull2Hour/OutputMatching" + fileIndex + ".txt");
		        scan = new Scanner(file);	  
		        scan.nextLine();
		        while (scan.hasNextLine()) {
		        	String data = scan.nextLine();
		        	String[] parseData = parseData(data, 22,' ');
		        	if (parseData != null) {
			        	int wifiID = Integer.parseInt(parseData[0]);
			        	if (checkWiFi[wifiID] == false) {
			        		checkWiFi[wifiID] = true;
			        		result++;
			        	}
		        	}
		        }
		        scan.close();
		    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
		    }
	    }
	    System.out.println("Number of WiFi to be found matched: " + result);
	}
	
    public static void main( String[ ] args ) {
    	System.out.println("Working Directory = " + System.getProperty("user.dir"));    	
    	countNumberOfMatchFound2HourVideo(); 
    	//readOutputMatching();
    	//OutputFrameMatchingWithBBoxForAnalyse();
    	
    }
}
