package SuntecRealData;



public class MapRealLevel6 extends MapReal{
	static int[] fpPointInTheArea = {43,44,45,47,48,49,50,51,52,53,54,55,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155};
	public Coordinate videoResolution = new Coordinate(1920, 1080);
	Coordinate videoDimensionInCM = new Coordinate(28.5, 16.1);
	public int fpPointInThisMap = 160;
	public Coordinate[] fpCoordinate;
	int numberOfPhysicalX = 8;
	int numberOfPhysicalY = 4;
	public Coordinate[][] videoCoordinate = new Coordinate[8][4];
	public MapRealLevel6() {
		fpCoordinate = new Coordinate[fpPointInThisMap + 1];
		
		for (int i = 0; i < fpPointInThisMap; i++) {
			fpCoordinate[i] = new Coordinate(Config.noPoint.x, Config.noPoint.y);
		}
		fpCoordinate[43] = new Coordinate(8.2,0.9);
		fpCoordinate[44] = new Coordinate(7.5,0.9);
		fpCoordinate[45] = new Coordinate(7.0,1.5);
		fpCoordinate[47] = new Coordinate(6,2);
		fpCoordinate[48] = new Coordinate(5,2.3);
		fpCoordinate[49] = new Coordinate(4,2.3);
		fpCoordinate[50] = new Coordinate(3.2,1.8);
		fpCoordinate[51] = new Coordinate(-0.3,1.5);
		fpCoordinate[52] = new Coordinate(4.5,1.8);
		fpCoordinate[53] = new Coordinate(4.8,1.2);
		fpCoordinate[54] = new Coordinate(3.9,1);
		fpCoordinate[55] = new Coordinate(5.8,1);
		fpCoordinate[139] = new Coordinate(8.3,0.5);
		fpCoordinate[140] = new Coordinate(7.8,0);
		fpCoordinate[141] = new Coordinate(7,0.4);
		fpCoordinate[142] = new Coordinate(6.2,0);
		fpCoordinate[143] = new Coordinate(5.8,0.4);
		fpCoordinate[144] = new Coordinate(5,0);
		fpCoordinate[145] = new Coordinate(4.5,0.5);
		fpCoordinate[146] = new Coordinate(4,0);
		fpCoordinate[147] = new Coordinate(3.2,0.4);
		fpCoordinate[148] = new Coordinate(2.6,0);
		fpCoordinate[149] = new Coordinate(2,0.4);
		fpCoordinate[150] = new Coordinate(1.2,0);
		fpCoordinate[151] = new Coordinate(0.8,0.5);
		fpCoordinate[152] = new Coordinate(0,0.5);
		fpCoordinate[153] = new Coordinate(0,0);
		fpCoordinate[154] = new Coordinate(7.5,-1);
		fpCoordinate[155] = new Coordinate(6.5,-1);
		
		videoCoordinate[0][0] = new Coordinate(14.3,15.5);
		videoCoordinate[0][1] = new Coordinate(27.5,14.5);
		videoCoordinate[1][0] = new Coordinate(14.3,10);
		videoCoordinate[1][1] = new Coordinate(22,9.9);
		videoCoordinate[2][0] = new Coordinate(14.3,7.5);
		videoCoordinate[2][1] = new Coordinate(19.6,7.8);
		videoCoordinate[2][2] = new Coordinate(23.5,7.5);
		videoCoordinate[2][3] = new Coordinate(24.9,7.3);
		videoCoordinate[3][0] = new Coordinate(14.3,6.1);
		videoCoordinate[3][1] = new Coordinate(18.2,6.6);
		videoCoordinate[3][2] = new Coordinate(21.7,6.6);
		videoCoordinate[3][3] = new Coordinate(23.2,6.5);
		videoCoordinate[4][0] = new Coordinate(14.3,6.1);
		videoCoordinate[4][1] = new Coordinate(17.4,5.9);
		videoCoordinate[4][2] = new Coordinate(20.5,6);
		videoCoordinate[4][3] = new Coordinate(21.8,5.9);
		videoCoordinate[5][0] = new Coordinate(14.3,4.9);
		videoCoordinate[5][1] = new Coordinate(16.6,5.3);
		videoCoordinate[6][0] = new Coordinate(14.3,4.7);
		videoCoordinate[6][1] = new Coordinate(16.1,4.8);
		videoCoordinate[7][0] = new Coordinate(14.3,4.4);
		videoCoordinate[7][1] = new Coordinate(15.9,4.6);
		//transfer the unit from cm to pixel
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
