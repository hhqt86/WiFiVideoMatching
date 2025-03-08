package SuntecRealData.Suntec;



public class SMUAddition_MapReal {
	/*int videoStartHour; 
	int videoStartMinute; 
	int videoStartSecond;*/
	int upLevel;
	int downLevel;
	int distanceGoStair = 0;
	int[] fpPointInTheCameraArea;
	int numberOfAP;
	APLocation[] apLocationCoor;
	public Coordinate videoResolution_BoundingBox = new Coordinate(1920, 1080);
	Coordinate videoDimensionInCM = new Coordinate(28.5, 17.9);
	public int fpPointInThisMap;
	public Coordinate[] fpCoordinate;
	int numberOfPhysicalVideoX;
	int numberOfPhysicalVideoY;
	public Coordinate[][] videoCoordinate;
	public SMUAddition_MapReal() {
		
	}
}
