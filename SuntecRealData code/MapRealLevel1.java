package SuntecRealData;



public class MapRealLevel1 {
	public double mapWidth;  //in pixel
	public double mapHeight; //in pixel	
	int numberLandmarkWidth;
	int numberLandmarkHeight;
	public int pointInThisMap = 100;
	public Coordinate[] fpCoordinate;
	int[] landmarkType = new int[pointInThisMap];
	public MapRealLevel1() {
		fpCoordinate = new Coordinate[pointInThisMap + 1]; 
		for (int i = 0; i < pointInThisMap; i++) {
			fpCoordinate[i] = new Coordinate(Suntec_Level1_Calibration.disappearWifi.x, Suntec_Level1_Calibration.disappearWifi.y);
			landmarkType[i] = 1;
		}
		fpCoordinate[1] = new Coordinate(-11.5,-8.5);
		fpCoordinate[2] = new Coordinate(-12,-9);
		fpCoordinate[3] = new Coordinate(-11,-11);
		fpCoordinate[4] = new Coordinate(-9.8,-8.5);
		fpCoordinate[5] = new Coordinate(-6,-11);
		fpCoordinate[6] = new Coordinate(-8,-11);
		fpCoordinate[7] = new Coordinate(-7.5,-9);
		fpCoordinate[8] = new Coordinate(-7,-8);
		fpCoordinate[9] = new Coordinate(-3.5,-11);
		fpCoordinate[10] = new Coordinate(-4,-9);
		fpCoordinate[11] = new Coordinate(-3.5,-8);
		fpCoordinate[12] = new Coordinate(0.5,-11);
		fpCoordinate[13] = new Coordinate(-1,-9);
		fpCoordinate[14] = new Coordinate(0.5,-8.5);
		fpCoordinate[15] = new Coordinate(2,-9);
		fpCoordinate[16] = new Coordinate(3.5,-1);
		fpCoordinate[17] = new Coordinate(4,-8.5);
		fpCoordinate[18] = new Coordinate(6,-9);
		fpCoordinate[19] = new Coordinate(6.5,-11.5);
		fpCoordinate[20] = new Coordinate(9,-8);
		fpCoordinate[21] = new Coordinate(8.5,-9.5);
		fpCoordinate[22] = new Coordinate(10.5,-11);
		fpCoordinate[23] = new Coordinate(12,-8);
		fpCoordinate[24] = new Coordinate(13,-9);
		fpCoordinate[25] = new Coordinate(15,-11);
		fpCoordinate[26] = new Coordinate(15,-8.5);
		fpCoordinate[27] = new Coordinate(11,-9);
		fpCoordinate[28] = new Coordinate(7,-8);
		fpCoordinate[29] = new Coordinate(-11.5,-6.5);
		fpCoordinate[30] = new Coordinate(-9,-6.5);
		fpCoordinate[31] = new Coordinate(-6,-6.5);
		fpCoordinate[32] = new Coordinate(-4,-7);
		fpCoordinate[33] = new Coordinate(-1.5,-7);
		fpCoordinate[34] = new Coordinate(0.5,-6);
		fpCoordinate[35] = new Coordinate(1,-4);
		fpCoordinate[36] = new Coordinate(1.2,-1.2);
		fpCoordinate[37] = new Coordinate(3.3,-6.9);
		fpCoordinate[38] = new Coordinate(6.4,-6.5);
		fpCoordinate[39] = new Coordinate(4.3,-5.5);
		fpCoordinate[40] = new Coordinate(5.5,-4.5);
		fpCoordinate[41] = new Coordinate(3.4,-4.5);
		fpCoordinate[42] = new Coordinate(2.3,-2.2);
		fpCoordinate[43] = new Coordinate(3.5,-0.2);
		fpCoordinate[44] = new Coordinate(4.5,-1.2);
		fpCoordinate[45] = new Coordinate(0,1);
		fpCoordinate[46] = new Coordinate(2.2,2.8);
		fpCoordinate[47] = new Coordinate(2.2,1);
		fpCoordinate[48] = new Coordinate(5,1);
		fpCoordinate[49] = new Coordinate(8,1);
		fpCoordinate[50] = new Coordinate(4.5,3.2);
		fpCoordinate[51] = new Coordinate(6.5,4.1);
		fpCoordinate[52] = new Coordinate(7.5,6.5);
		fpCoordinate[53] = new Coordinate(4.5,6.5);
		fpCoordinate[54] = new Coordinate(5.5,7.7);
		fpCoordinate[55] = new Coordinate(4.7,10);
		fpCoordinate[56] = new Coordinate(6.2,10);
		fpCoordinate[57] = new Coordinate(6.2,11.5);
		fpCoordinate[58] = new Coordinate(6.5,13.5);
		fpCoordinate[59] = new Coordinate(4.5,15);
		fpCoordinate[60] = new Coordinate(6,16);
		fpCoordinate[61] = new Coordinate(4.5,17.5);
		fpCoordinate[62] = new Coordinate(6.4,19);
		fpCoordinate[63] = new Coordinate(6.2,21.1);
		fpCoordinate[64] = new Coordinate(3,19);
		fpCoordinate[65] = new Coordinate(0,17.8);
		fpCoordinate[66] = new Coordinate(0,21.2);
		fpCoordinate[67] = new Coordinate(3,21.2);
		fpCoordinate[68] = new Coordinate(1.4,22.5);
		fpCoordinate[69] = new Coordinate(4.5,22.5);
		fpCoordinate[70] = new Coordinate(3,23.5);
		fpCoordinate[71] = new Coordinate(5,24.5);
		fpCoordinate[72] = new Coordinate(9,15);
		fpCoordinate[73] = new Coordinate(7.5,-1);
		fpCoordinate[74] = new Coordinate(12.6,5.5);
		fpCoordinate[75] = new Coordinate(11.5,6.3);
		fpCoordinate[76] = new Coordinate(13.5,6.3);
		fpCoordinate[77] = new Coordinate(12.5,7.5);
		fpCoordinate[78] = new Coordinate(13.2,8.5);
		fpCoordinate[79] = new Coordinate(9.5,8.2);
		fpCoordinate[80] = new Coordinate(7.6,9.5);
		fpCoordinate[81] = new Coordinate(11.5,9.5);
		fpCoordinate[82] = new Coordinate(9.5,10.4);
		fpCoordinate[83] = new Coordinate(13.2,10.6);
		fpCoordinate[84] = new Coordinate(11.5,11.5);
		fpCoordinate[85] = new Coordinate(7.6,11.6);
		fpCoordinate[86] = new Coordinate(12.5,13);
		fpCoordinate[87] = new Coordinate(12.5,15.5);
		fpCoordinate[88] = new Coordinate(11.5,16.6);
		fpCoordinate[89] = new Coordinate(7.7,16.6);
		fpCoordinate[90] = new Coordinate(9.2,17.3);
		fpCoordinate[91] = new Coordinate(13.2,17.3);
		fpCoordinate[92] = new Coordinate(11.5,18);
		fpCoordinate[93] = new Coordinate(7.5,18);
		fpCoordinate[94] = new Coordinate(9,19);
		fpCoordinate[95] = new Coordinate(13.2,19);
		fpCoordinate[96] = new Coordinate(11.5,20);
		fpCoordinate[97] = new Coordinate(7.5,20);
		fpCoordinate[98] = new Coordinate(10.5,20.5);
		fpCoordinate[99] = new Coordinate(11.2,21.5);
		fpCoordinate[100] = new Coordinate(11.5,22.5);
		for (int i = 0; i < pointInThisMap; i++) {
			fpCoordinate[i].x = fpCoordinate[i].x - 4;
			fpCoordinate[i].y = fpCoordinate[i].y - 1;
		}
	}
}
