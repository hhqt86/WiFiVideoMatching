package SuntecRealData.Suntec;



public class MapReal {
	/*int videoStartHour; 
	int videoStartMinute; 
	int videoStartSecond;*/
	int[] fpPointInTheCameraArea;
	int[] goesUpPoint;
	int[] goesDownPoint;
	int numberOfAP;
	APLocation[] apLocationAtFP;
	public Coordinate videoResolution_BoundingBox = new Coordinate(1920, 1080);
	public Coordinate videoResolution_NoBoundingBox = new Coordinate(3840, 2160);
	Coordinate videoDimensionInCM = new Coordinate(28.5, 16.1);
	int distanceGoStair = 5;
	int upLevel;
	int downLevel;
	public int fpPointInThisMap;
	public Coordinate[] fpCoordinate;
	int numberOfPhysicalVideoX;
	int numberOfPhysicalVideoY;
	public Coordinate[][] videoCoordinate;
}
