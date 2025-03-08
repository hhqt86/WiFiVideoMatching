package SuntecRealData;



public class MapRealLevel4 extends MapReal{
	public Coordinate videoResolution = new Coordinate(1920, 1080);
	Coordinate videoDimensionInCM = new Coordinate(28.5, 16.1);
	public int pointInThisMap = 620;
	public int numberOfPhysicalX = 9;
	public int numberOfPhysicalY = 4;
	public Coordinate[] fpCoordinate;
	public int[] fpID;
	public Coordinate[][] videoCoordinate = new Coordinate[9][4];
	public MapRealLevel4() {
		fpCoordinate = new Coordinate[pointInThisMap + 1];
		fpID = new int[pointInThisMap + 1];
		for (int i = 0; i < pointInThisMap; i++) {
			fpCoordinate[i] = new Coordinate(Config.noPoint.x, Config.noPoint.y);
		}
		fpCoordinate[597] = new Coordinate(-1,0);
		fpCoordinate[598] = new Coordinate(1,-1);
		fpCoordinate[599] = new Coordinate(0.6,1);
		fpCoordinate[600] = new Coordinate(1.7,0);
		fpCoordinate[601] = new Coordinate(-1,1.7);
		fpCoordinate[603] = new Coordinate(3,0.6);
		fpCoordinate[604] = new Coordinate(4.5,0);
		fpCoordinate[605] = new Coordinate(6.1,0.5);
		fpCoordinate[606] = new Coordinate(6.2,-1);
		fpCoordinate[607] = new Coordinate(7.4,0);
		fpCoordinate[608] = new Coordinate(5.9,1.1);
		fpCoordinate[609] = new Coordinate(4,1.1);
		fpCoordinate[610] = new Coordinate(3.3,2);
		fpCoordinate[611] = new Coordinate(3,3);
		fpCoordinate[612] = new Coordinate(4.5,3.5);
		fpCoordinate[613] = new Coordinate(5.5,1.6);
		fpCoordinate[614] = new Coordinate(5.5,2.5);
		fpCoordinate[615] = new Coordinate(6.4,3);
		fpCoordinate[616] = new Coordinate(6.5,2);
		fpCoordinate[617] = new Coordinate(8.6,0.5);
		fpCoordinate[618] = new Coordinate(9.5,0);
		fpCoordinate[619] = new Coordinate(10,0.5);
		
		videoCoordinate[0][0] = new Coordinate(14.5,15.8);
		videoCoordinate[0][1] = new Coordinate(3.4,15.3);
		videoCoordinate[1][0] = new Coordinate(14.5,13.6);
		videoCoordinate[1][1] = new Coordinate(5.8,13.3);
		videoCoordinate[2][0] = new Coordinate(14.5,11.9);
		videoCoordinate[2][1] = new Coordinate(8,11.6);
		videoCoordinate[2][2] = new Coordinate(4.6,11);
		videoCoordinate[3][0] = new Coordinate(14.5,10.6);
		videoCoordinate[3][1] = new Coordinate(9.4,10.5);
		videoCoordinate[3][2] = new Coordinate(6.3,10);
		videoCoordinate[3][3] = new Coordinate(4.2,9.5);
		videoCoordinate[4][0] = new Coordinate(14.5,9.8);
		videoCoordinate[4][1] = new Coordinate(10.4,9.7);
		videoCoordinate[4][2] = new Coordinate(7.4,9.3);
		videoCoordinate[4][3] = new Coordinate(5.3,9);
		videoCoordinate[5][0] = new Coordinate(14.5,9);
		videoCoordinate[5][1] = new Coordinate(11.1,9);
		videoCoordinate[5][2] = new Coordinate(8.3,8.9);
		videoCoordinate[5][3] = new Coordinate(6,8.6);
		videoCoordinate[6][0] = new Coordinate(14.5,8.5);
		videoCoordinate[6][1] = new Coordinate(11.8,8.6);
		videoCoordinate[6][2] = new Coordinate(9,8.5);
		videoCoordinate[6][3] = new Coordinate(7,8.3);
		videoCoordinate[7][0] = new Coordinate(14.5,8.2);
		videoCoordinate[7][1] = new Coordinate(12.1,8.3);
		videoCoordinate[7][2] = new Coordinate(9.5,8.2);
		videoCoordinate[7][3] = new Coordinate(7.8,8);
		videoCoordinate[8][0] = new Coordinate(14.5,7.9);
		videoCoordinate[8][1] = new Coordinate(12.6,7.9);
		
		for (int i = 0; i < numberOfPhysicalX; i++) {
			for (int j = 0; j < numberOfPhysicalY; j++) {
				if (videoCoordinate[i][j] != null) {
					videoCoordinate[i][j].x = (videoCoordinate[i][j].x / videoDimensionInCM.x) * videoResolution.x;
					videoCoordinate[i][j].y = (videoCoordinate[i][j].y / videoDimensionInCM.y) * videoResolution.y;
				}
			}
		}	
	}
}
