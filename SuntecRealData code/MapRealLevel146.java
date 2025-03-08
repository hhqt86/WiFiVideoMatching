package SuntecRealData;



public class MapRealLevel146 {
	public Coordinate videoResolution = new Coordinate(1920, 1080);
	Coordinate videoDimensionInCM = new Coordinate(28.5, 16.1);
	public int[] level = {1,4,6};
	public int[] fpPointAtEachLevel = {0,60,0,0,620,0,100};
	public Coordinate[][] multiLevelFPCoordinate = new Coordinate[7][1000];
	public int[] goUpPoint = new int[] {-1, 28, -1, -1, 617, -1, 152};
	public int[] goDownPoint = new int[] {-1, 243, -1, -1, 544, -1, 86};
	public int[] distanceGoStair = new int[]{-1, 12, -1, -1, 6, -1, 6};
	
	public int getUpLevel(int currentLevel) {
		if (currentLevel == 1) return 4;
		if (currentLevel == 4) return 6;
		return -1;
	}

	public int getDownLevel(int currentLevel) {
		if (currentLevel == 6) return 4;
		if (currentLevel == 4) return 1;
		return -1;
	}
	
	public MapRealLevel146() {
		for (int i = 0; i < 3; i++) {
			for (int k = 0; k < fpPointAtEachLevel[level[i]]; k++) {
				multiLevelFPCoordinate[level[i]][k] = new Coordinate(Config.noPoint.x, Config.noPoint.y);
			}
		}
		multiLevelFPCoordinate[1][1] = new Coordinate(28.5, 2.5);
		multiLevelFPCoordinate[1][2] = new Coordinate(27.5, 2);
		multiLevelFPCoordinate[1][3] = new Coordinate(27, 2.5);
		multiLevelFPCoordinate[1][4] = new Coordinate(26, 2);
		multiLevelFPCoordinate[1][5] = new Coordinate(25.1, 2.5);
		multiLevelFPCoordinate[1][6] = new Coordinate(24.5, 2);
		multiLevelFPCoordinate[1][7] = new Coordinate(23.8, 2.5);
		multiLevelFPCoordinate[1][8] = new Coordinate(23, 2);
		multiLevelFPCoordinate[1][9] = new Coordinate(22, 2.5);
		multiLevelFPCoordinate[1][10] = new Coordinate(22, 3);
		multiLevelFPCoordinate[1][11] = new Coordinate(21.3, 2);
		multiLevelFPCoordinate[1][12] = new Coordinate(21.3, 3);
		multiLevelFPCoordinate[1][13] = new Coordinate(21, 3.5);
		multiLevelFPCoordinate[1][14] = new Coordinate(20.5, 2);
		multiLevelFPCoordinate[1][15] = new Coordinate(20.2, 3);
		multiLevelFPCoordinate[1][16] = new Coordinate(19.8, 2);
		multiLevelFPCoordinate[1][17] = new Coordinate(19.8, 3.7);
		multiLevelFPCoordinate[1][18] = new Coordinate(19, 2.3);
		multiLevelFPCoordinate[1][19] = new Coordinate(19, 3.5);
		multiLevelFPCoordinate[1][20] = new Coordinate(18.1, 2);
		multiLevelFPCoordinate[1][21] = new Coordinate(18.2, 3);
		multiLevelFPCoordinate[1][22] = new Coordinate(17.5, 2.3);
		multiLevelFPCoordinate[1][23] = new Coordinate(17.5, 3.5);
		multiLevelFPCoordinate[1][24] = new Coordinate(16.5, 2);
		multiLevelFPCoordinate[1][25] = new Coordinate(16.5, 3);
		multiLevelFPCoordinate[1][26] = new Coordinate(15.8, 2.2);
		multiLevelFPCoordinate[1][27] = new Coordinate(15.8, 3.8);
		multiLevelFPCoordinate[1][28] = new Coordinate(15.8, 5.2);
		multiLevelFPCoordinate[1][29] = new Coordinate(15.2, 3);
		multiLevelFPCoordinate[1][30] = new Coordinate(15.2, 4.5);
		multiLevelFPCoordinate[1][31] = new Coordinate(14.8, 2.2);
		multiLevelFPCoordinate[1][32] = new Coordinate(14.8, 3.8);
		multiLevelFPCoordinate[1][33] = new Coordinate(14.8, 5.2);
		multiLevelFPCoordinate[1][34] = new Coordinate(14.3, 3);
		multiLevelFPCoordinate[1][35] = new Coordinate(14.3, 4.5);
		multiLevelFPCoordinate[1][36] = new Coordinate(14, 2.2);
		multiLevelFPCoordinate[1][37] = new Coordinate(14, 3.9);
		multiLevelFPCoordinate[1][38] = new Coordinate(14, 5.2);
		multiLevelFPCoordinate[1][39] = new Coordinate(13.3, 3);
		multiLevelFPCoordinate[1][40] = new Coordinate(13.3, 4.5);
		multiLevelFPCoordinate[1][41] = new Coordinate(13, 2.3);
		multiLevelFPCoordinate[1][42] = new Coordinate(13, 3.9);
		multiLevelFPCoordinate[1][43] = new Coordinate(13, 5.3);
		multiLevelFPCoordinate[1][44] = new Coordinate(12, 2);
		multiLevelFPCoordinate[1][45] = new Coordinate(12, 3);
		multiLevelFPCoordinate[1][46] = new Coordinate(11.3, 2.4);
		multiLevelFPCoordinate[1][47] = new Coordinate(11.3, 3.4);
		multiLevelFPCoordinate[1][48] = new Coordinate(10.5, 2);
		multiLevelFPCoordinate[1][49] = new Coordinate(10.5, 3.5);
		multiLevelFPCoordinate[1][50] = new Coordinate(9.8, 2.5);
		multiLevelFPCoordinate[1][51] = new Coordinate(8.9, 2);
		multiLevelFPCoordinate[1][52] = new Coordinate(8.9, 3);
		multiLevelFPCoordinate[1][53] = new Coordinate(8.1, 2.5);
		multiLevelFPCoordinate[1][54] = new Coordinate(8.1, 3.5);
		multiLevelFPCoordinate[1][55] = new Coordinate(7.3, 2);
		multiLevelFPCoordinate[1][56] = new Coordinate(7.3, 3);
		multiLevelFPCoordinate[1][57] = new Coordinate(6.5, 1);
		multiLevelFPCoordinate[1][58] = new Coordinate(6.5, 2.5);
		multiLevelFPCoordinate[1][59] = new Coordinate(5.8, 1);
		multiLevelFPCoordinate[1][60] = new Coordinate(5.8, 2);
		//------------------------------
		multiLevelFPCoordinate[4][541] = new Coordinate(-2, 0);
		multiLevelFPCoordinate[4][542] = new Coordinate(-1, 0.8);
		multiLevelFPCoordinate[4][543] = new Coordinate(0, 0);
		multiLevelFPCoordinate[4][544] = new Coordinate(0, 0.5);
		multiLevelFPCoordinate[4][545] = new Coordinate(1.4, 0);
		multiLevelFPCoordinate[4][546] = new Coordinate(2, 0.9);
		multiLevelFPCoordinate[4][547] = new Coordinate(3, 0.5);
		multiLevelFPCoordinate[4][548] = new Coordinate(4, 0);
		multiLevelFPCoordinate[4][549] = new Coordinate(5, 0.5);
		multiLevelFPCoordinate[4][550] = new Coordinate(1.5, 1.5);
		multiLevelFPCoordinate[4][551] = new Coordinate(2.5, 1.5);
		multiLevelFPCoordinate[4][552] = new Coordinate(3.5, 1.5);
		multiLevelFPCoordinate[4][553] = new Coordinate(4, 2);
		multiLevelFPCoordinate[4][554] = new Coordinate(4.5, 2.1);
		multiLevelFPCoordinate[4][555] = new Coordinate(4, 2.8);
		multiLevelFPCoordinate[4][556] = new Coordinate(2.5, 2.5);
		multiLevelFPCoordinate[4][557] = new Coordinate(1.5, 2.5);
		multiLevelFPCoordinate[4][558] = new Coordinate(5.5, -1);
		multiLevelFPCoordinate[4][559] = new Coordinate(5.5, 0.9);
		multiLevelFPCoordinate[4][560] = new Coordinate(6, 0.3);
		multiLevelFPCoordinate[4][561] = new Coordinate(6.5, 0.8);
		multiLevelFPCoordinate[4][562] = new Coordinate(7, 2);
		multiLevelFPCoordinate[4][563] = new Coordinate(8, 2.3);
		multiLevelFPCoordinate[4][564] = new Coordinate(7, 0);
		multiLevelFPCoordinate[4][565] = new Coordinate(7.5, 0.5);
		multiLevelFPCoordinate[4][566] = new Coordinate(9, 0);
		multiLevelFPCoordinate[4][567] = new Coordinate(9.7, 0.4);
		multiLevelFPCoordinate[4][568] = new Coordinate(10.5, 0);
		multiLevelFPCoordinate[4][569] = new Coordinate(12, -1);
		multiLevelFPCoordinate[4][570] = new Coordinate(11.6, 0);
		multiLevelFPCoordinate[4][571] = new Coordinate(11.5, 1.2);
		multiLevelFPCoordinate[4][572] = new Coordinate(10, 2.7);
		multiLevelFPCoordinate[4][573] = new Coordinate(13, 1);
		multiLevelFPCoordinate[4][574] = new Coordinate(12.5, 1.7);
		multiLevelFPCoordinate[4][575] = new Coordinate(13, 0.2);
		multiLevelFPCoordinate[4][576] = new Coordinate(13.6, 0.2);
		multiLevelFPCoordinate[4][577] = new Coordinate(13.5, 0.2);
		multiLevelFPCoordinate[4][578] = new Coordinate(14, 0.9);
		multiLevelFPCoordinate[4][579] = new Coordinate(15, 0.9);
		multiLevelFPCoordinate[4][580] = new Coordinate(14.4, 1.8);
		multiLevelFPCoordinate[4][581] = new Coordinate(15.2, 1.8);
		multiLevelFPCoordinate[4][582] = new Coordinate(16, 1.2);
		multiLevelFPCoordinate[4][583] = new Coordinate(13, 0.9);
		multiLevelFPCoordinate[4][584] = new Coordinate(17.3, 1.2);
		multiLevelFPCoordinate[4][585] = new Coordinate(15.8, 2.2);
		multiLevelFPCoordinate[4][586] = new Coordinate(16.3, 2.8);
		multiLevelFPCoordinate[4][587] = new Coordinate(15.5, 2.8);
		multiLevelFPCoordinate[4][588] = new Coordinate(14.5, 2.8);
		multiLevelFPCoordinate[4][589] = new Coordinate(13.5, 2.8);
		multiLevelFPCoordinate[4][590] = new Coordinate(12.2, 2.8);
		multiLevelFPCoordinate[4][591] = new Coordinate(17, 0);
		multiLevelFPCoordinate[4][592] = new Coordinate(17, -1);
		multiLevelFPCoordinate[4][593] = new Coordinate(18, 0);
		multiLevelFPCoordinate[4][594] = new Coordinate(18.5, 0.5);
		multiLevelFPCoordinate[4][595] = new Coordinate(19.5, 0);
		multiLevelFPCoordinate[4][596] = new Coordinate(20.3, 0.5);
		multiLevelFPCoordinate[4][597] = new Coordinate(21.8, 0);
		multiLevelFPCoordinate[4][598] = new Coordinate(23, -1);
		multiLevelFPCoordinate[4][599] = new Coordinate(23, 1);
		multiLevelFPCoordinate[4][600] = new Coordinate(23.8, 0);
		multiLevelFPCoordinate[4][601] = new Coordinate(21.9, 1.2);
		multiLevelFPCoordinate[4][602] = new Coordinate(21.2, 2.1);
		multiLevelFPCoordinate[4][603] = new Coordinate(25, 0.5);
		multiLevelFPCoordinate[4][604] = new Coordinate(26, 0);
		multiLevelFPCoordinate[4][605] = new Coordinate(27.4, 0.5);
		multiLevelFPCoordinate[4][606] = new Coordinate(27.5, -1);
		multiLevelFPCoordinate[4][607] = new Coordinate(28.3, 0);
		multiLevelFPCoordinate[4][608] = new Coordinate(27, 1);
		multiLevelFPCoordinate[4][609] = new Coordinate(25.5, 1);
		multiLevelFPCoordinate[4][610] = new Coordinate(25, 1.8);
		multiLevelFPCoordinate[4][611] = new Coordinate(25, 2.5);
		multiLevelFPCoordinate[4][612] = new Coordinate(26, 2.8);
		multiLevelFPCoordinate[4][613] = new Coordinate(27, 1.4);
		multiLevelFPCoordinate[4][614] = new Coordinate(27, 2);
		multiLevelFPCoordinate[4][615] = new Coordinate(27.5, 2.5);
		multiLevelFPCoordinate[4][616] = new Coordinate(27.5, 1.7);
		multiLevelFPCoordinate[4][617] = new Coordinate(30, 0.5);
		multiLevelFPCoordinate[4][618] = new Coordinate(31, 0);
		multiLevelFPCoordinate[4][619] = new Coordinate(31.4, 0.5);
		//------------------------------------------------
		multiLevelFPCoordinate[6][43] = new Coordinate(22.5, 1.9);
		multiLevelFPCoordinate[6][44] = new Coordinate(23.2, 1.9);
		multiLevelFPCoordinate[6][45] = new Coordinate(23.5, 2.2);
		multiLevelFPCoordinate[6][46] = new Coordinate(23.2, 3.6);
		multiLevelFPCoordinate[6][47] = new Coordinate(24.7, 3);
		multiLevelFPCoordinate[6][48] = new Coordinate(25.5, 3.2);
		multiLevelFPCoordinate[6][49] = new Coordinate(26.5, 32);
		multiLevelFPCoordinate[6][50] = new Coordinate(27, 2.7);
		multiLevelFPCoordinate[6][51] = new Coordinate(31, 2.4);
		multiLevelFPCoordinate[6][52] = new Coordinate(26, 2.7);
		multiLevelFPCoordinate[6][53] = new Coordinate(25.6, 2.2);
		multiLevelFPCoordinate[6][54] = new Coordinate(26.5, 1.9);
		multiLevelFPCoordinate[6][55] = new Coordinate(24.8, 1.9);
		multiLevelFPCoordinate[6][56] = new Coordinate(17.5, 2);
		multiLevelFPCoordinate[6][57] = new Coordinate(16.5, 2.4);
		multiLevelFPCoordinate[6][58] = new Coordinate(17, 2.9);
		multiLevelFPCoordinate[6][59] = new Coordinate(16, 2.9);
		multiLevelFPCoordinate[6][60] = new Coordinate(16.2, 3.5);
		multiLevelFPCoordinate[6][61] = new Coordinate(14.5, 3.5);
		multiLevelFPCoordinate[6][62] = new Coordinate(14.9, 3.1);
		multiLevelFPCoordinate[6][63] = new Coordinate(14.5, 2.5);
		multiLevelFPCoordinate[6][64] = new Coordinate(14.9, 2.1);
		multiLevelFPCoordinate[6][65] = new Coordinate(15.5, 2.1);
		multiLevelFPCoordinate[6][66] = new Coordinate(15.2, 1.8);
		multiLevelFPCoordinate[6][67] = new Coordinate(14.2, 1.8);
		multiLevelFPCoordinate[6][68] = new Coordinate(13.2, 1.8);
		multiLevelFPCoordinate[6][69] = new Coordinate(13.5, 2.2);
		multiLevelFPCoordinate[6][70] = new Coordinate(13.5, 3.1);
		multiLevelFPCoordinate[6][71] = new Coordinate(12.8, 3.6);
		multiLevelFPCoordinate[6][72] = new Coordinate(12.4, 2.8);
		multiLevelFPCoordinate[6][73] = new Coordinate(12.5, 2.5);
		multiLevelFPCoordinate[6][74] = new Coordinate(11.5, 2.5);
		multiLevelFPCoordinate[6][75] = new Coordinate(12, 2);
		multiLevelFPCoordinate[6][76] = new Coordinate(3.5, 3.3);
		multiLevelFPCoordinate[6][77] = new Coordinate(2.5, 3.3);
		multiLevelFPCoordinate[6][78] = new Coordinate(4.1, 2.9);
		multiLevelFPCoordinate[6][79] = new Coordinate(3.5, 2.6);
		multiLevelFPCoordinate[6][80] = new Coordinate(2.3, 2.6);
		multiLevelFPCoordinate[6][81] = new Coordinate(1.9, 2.6);
		multiLevelFPCoordinate[6][82] = new Coordinate(2.6, 2.1);
		multiLevelFPCoordinate[6][83] = new Coordinate(2.2, 1.8);
		multiLevelFPCoordinate[6][84] = new Coordinate(3.5, 1.8);
		multiLevelFPCoordinate[6][85] = new Coordinate(-2.5, 2.6);
		multiLevelFPCoordinate[6][86] = new Coordinate(-2, 1.2);
		multiLevelFPCoordinate[6][87] = new Coordinate(0, 1.2);
		multiLevelFPCoordinate[6][88] = new Coordinate(-1, 1);
		multiLevelFPCoordinate[6][89] = new Coordinate(1, 1.2);
		multiLevelFPCoordinate[6][90] = new Coordinate(0.5, 0);
		multiLevelFPCoordinate[6][91] = new Coordinate(1.5, 0);
		multiLevelFPCoordinate[6][92] = new Coordinate(2, 1.2);
		multiLevelFPCoordinate[6][93] = new Coordinate(2.7, 0);
		multiLevelFPCoordinate[6][94] = new Coordinate(3.2, 1.2);
		multiLevelFPCoordinate[6][95] = new Coordinate(4, 0);
		multiLevelFPCoordinate[6][96] = new Coordinate(4.5, 1.2);
		multiLevelFPCoordinate[6][97] = new Coordinate(5, 0);
		multiLevelFPCoordinate[6][98] = new Coordinate(5.2, 1.2);
		multiLevelFPCoordinate[6][99] = new Coordinate(6, 0);
		multiLevelFPCoordinate[6][100] = new Coordinate(7, 1.2);

		

		
		
		
	}
	
}
