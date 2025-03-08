package SuntecRealData;



public class MapReal {
	int[] fpPointInTheCameraArea;
	int[] goesUpPoint;
	int[] goesDownPoint;
	int numberOfAP;
	APLocation[] apLocationAtFP;
	public Coordinate videoResolution = new Coordinate(1920, 1080);
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
