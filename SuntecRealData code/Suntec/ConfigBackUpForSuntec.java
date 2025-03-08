/*package SuntecRealData.Suntec;



public class ConfigBackUpForSuntec {
	static int startHour = 10;
	static int startMinute = 6;
	static int startSecond = 30;
	static int endHour = 10;
	static int endMinute = 30;
	static int endSecond = 0;
	static int maximumBlobVideoCount = 3500;  // Maximum blob count in the Suntec video
	static int maximumFrameCount = 20000;// Maximum frame count in the Suntec video, 30 fps
	static int maximumWifiReportCount = 400;
	static double averageScoreOfLeaderThreshold = 0.7;
	static double rtRealWifi = 0.25;
	static NormalLocationCoordinate noPoint = new NormalLocationCoordinate(-1000, new Coordinate(-1000,-1000));
	static int videoFPS = 30;
	
	//For select good samples
	static int durationThresh = 120;
	static double frequencyThresh = 0.05;

	
	//For the algorithm matching
	static double rangeScoreConsiderAsLeaders = 0.1;
	//static double rangeScoreConsiderAsLeaders = 0.05;
	static int historyWindowLength = 2; //unit: seconds
	//static int historyWindowLength = 5; //unit: seconds
	static int maxRankThresholdSlidingWindow = 5;
	static int maxRankThresholdForMatching = 10;

	static double rtSimulator = 0.5;
	
	//For the old algorithm 
	static int consecutiveMatch; 
	static double weightScore;
	static double thresholdMatching;
}

class MultiLevelLocation {
	int level;
	int fpID;
	Coordinate location;
	public MultiLevelLocation(int level, int fpID, Coordinate location) {
		this.level = level;
		this.fpID = fpID;
		this.location = new Coordinate(location.x, location.y);
	}
}

class BlobDuration{
	int left, right;
	public BlobDuration(int left, int right) {
		this.left = left;
		this.right = right;
	}
}

class BoundingBoxImageCoor {
	double x,y,w,h;
	public BoundingBoxImageCoor(double x0, double y0, double w0, double h0) {
		x = x0; y = y0; w = w0; h = h0;
	}
}

class BoundingBoxImageCoorWithID {
	int ID;
	double x,y,w,h;
	public BoundingBoxImageCoorWithID(int ID0, double x0, double y0, double w0, double h0) {
		ID = ID0;
		x = x0; y = y0; w = w0; h = h0;
	}
}

class NormalLocation{
	int locationID;
	int level;
	public NormalLocation(int level, int location) {
		this.locationID = location;
		this.level = level;	
	}
	
	
	public NormalLocation(String location) {
		this.level = Integer.parseInt(location.charAt(0)+"");
		this.locationID = Integer.parseInt(location.substring(1, 4));
	}
	public String toString() {
		String result = level + "";
		if (locationID < 10) {
			result += "00" + locationID;
		}
		else {
			if (locationID < 100) {
				result += "0" + locationID;
			}
			else {
				result += locationID;
			}
		}
		return result;
	}
}
class NormalLocationCoordinate {
	int level;
	Coordinate locationCoor;
	public NormalLocationCoordinate(int level, Coordinate coor) {
		this.level = level;
		locationCoor = new Coordinate(coor.x, coor.y);
	}
}

class APLocation {
	String macAddress;
	NormalLocation location;
	public APLocation(String macAddress, NormalLocation location) {
		this.macAddress = macAddress;
		this.location = location;
	}
	public APLocation(String macAddress, int level, int location) {
		this.macAddress = macAddress;
		this.location = new NormalLocation(level, location);
	}

}

class TimeWithLocation {
	NormalLocation location;
	int time;
	public TimeWithLocation(String location, int time) {
		this.time = time;
		this.location = new NormalLocation(location);
	}
	public TimeWithLocation(int level, int location, int time) {
		this.time = time;
		this.location = new NormalLocation(level, location);
	}

}

class RankTimeWithLocationCoordinate {
	int rank;
	int time;
	NormalLocationCoordinate location;
	
	public RankTimeWithLocationCoordinate(int rank, int time, int level, double coorX, double coorY) {
		this.rank = rank;
		this.time = time;
		this.location = new NormalLocationCoordinate(level, new Coordinate(coorX, coorY));
	}

}

class timeWithLocationCoordinate {
	NormalLocationCoordinate location;
	int time;
	public timeWithLocationCoordinate(int level, Coordinate coor, int time) {
		this.time = time;
		this.location = new NormalLocationCoordinate(level, coor);
	}
	
	public timeWithLocationCoordinate(NormalLocationCoordinate location, int time) {
		this.time = time;
		this.location = location;
	}

}


class ParserWifiRTLS{
	String data;
	public ParserWifiRTLS(String data) {
		this.data = data;
	}
	
	public String getAttribute(int indexInput) {
		int index = 0;
		int start = 0;
		String result = "";
		if (indexInput > 0) {
			for (int i = 0; i < data.length(); i++) {
				if (data.charAt(i) == ',') {
					index++;
					if (index == indexInput) {
						start = i + 1;
						break;
					}
				}
			}
		}
		for (int i = start; i < data.length(); i++) {
			if (data.charAt(i) == ',') {
				break;
			}
			result = result + data.charAt(i);			
		}
		return result;
	}
	
	public int getTime() {
		return Integer.parseInt(data.substring(0,10));
	}
	
	public String getIDstr() {
		int commaIndex = data.indexOf(',');
		return data.substring(0,commaIndex);
	}
	
	public String getIDstrRTLS() {
		int commaIndex = data.lastIndexOf(',');		
		return data.substring(commaIndex + 1, data.length());
	}
	
	public int getAssociateStatus() {
		return Integer.parseInt(getAttribute(6));
	}
	
}

class ParserWifi{
	String data;
	public ParserWifi(String data) {
		this.data = data;
	}
	public String getTime() {
		return data.substring(11, 19);
	}
	public int getID() {
		int index = 0;
		for (int i = 0; i < data.length(); i++) {
			if (data.charAt(i) == ',') {
				index++;
				if (index == 1) { //this is for the ID part
					int beginIndex = i + 1;
					int endIndex = i + 1;
					for (int j = i + 1; j < data.length(); j++) {
						if (data.charAt(j) == ',') {
							endIndex = j;
							return Integer.parseInt(data.substring(beginIndex, endIndex));
						}
					}
				}
			}
		}
		return -1;
	}
	
	public String getIDstr() {
		int index = 0;
		for (int i = 0; i < data.length(); i++) {
			if (data.charAt(i) == ',') {
				index++;
				if (index == 1) { //this is for the ID part
					int beginIndex = i + 1;
					int endIndex = i + 1;
					for (int j = i + 1; j < data.length(); j++) {
						if (data.charAt(j) == ',') {
							endIndex = j;
							return data.substring(beginIndex, endIndex);
						}
					}
				}
			}
		}		
		return "";
	}
	
	public int getLocation() {
		int index = 0;
		for (int i = 0; i < data.length(); i++) {
			if (data.charAt(i) == ',') {
				index++;
				if (index == 2) {
					int beginIndex = i + 1;
					int endIndex = i + 1;
					for (int j = i + 1; j < data.length(); j++) {
						if (data.charAt(j) == ',') {
							endIndex = j;
							return Integer.parseInt(data.substring(endIndex - 4, endIndex));
						}
					}
				}
			}
		}
		return -1;
	}
	
	public int getLocationLevel() {
		int index = 0;
		for (int i = 0; i < data.length(); i++) {
			if (data.charAt(i) == ',') {
				index++;
				if (index == 2) {
					int beginIndex = i + 1;
					int endIndex = i + 1;
					for (int j = i + 1; j < data.length(); j++) {
						if (data.charAt(j) == ',') {
							endIndex = j;
							return Integer.parseInt(data.charAt(endIndex - 6) + "");
						}
					}
				}
			}
		}
		return -1;
	}
	
	public String getLocationBuilding() {
		int index = 0;
		for (int i = 0; i < data.length(); i++) {
			if (data.charAt(i) == '"') {
				index++;
				if (index == 5) {
					return data.substring(i+1,i+7);
				}
			}
		}
		return null;
	}
	
}

class ParserVideo{
	String data;
	public int getCommaLocation(int indexth) {
		int tmp = indexth;
		for (int i = 0; i < data.length(); i++) {
			if (data.charAt(i) == ',') {
				tmp--;
				if (tmp == 0) {
					return i;
				}
			}
		}
		return -1;
	}
	public ParserVideo(String data) {
		this.data = data;
	}
	
	public int getFrameID() {
		return Integer.parseInt(data.substring(0, getCommaLocation(1)));
	}

	public int getBlobID() {
		return Integer.parseInt(data.substring(getCommaLocation(1) + 1, getCommaLocation(2)));
	}

	public double getX() {
		return Double.parseDouble(data.substring(getCommaLocation(2) + 1, getCommaLocation(3)));
	}

	public double getY() {
		return Double.parseDouble(data.substring(getCommaLocation(3) + 1, getCommaLocation(4)));
	}

	public double getW() {
		return Double.parseDouble(data.substring(getCommaLocation(4) + 1, getCommaLocation(5)));
	}
	
	public double getH() {
		return Double.parseDouble(data.substring(getCommaLocation(5) + 1, getCommaLocation(6)));
	}
	
}

class Coordinate{
	double x;
	double y;
	Coordinate(double x0, double y0) {
		x = x0; y = y0;
	}
	Coordinate(Coordinate input){
		this.x = input.x;
		this.y = input.y;
	}
	void sum(Coordinate k) {
		this.x += k.x;
		this.y += k.y;	
	}
	void subtract(Coordinate k) {
		this.x -= k.x;
		this.y -= k.y;			
	}	
	void mul(double k) {
		this.x *= k;
		this.y *= k;				
	}
	void sumX(double k) {
		this.x += k;		
	}
	void sumY(double k) {
		this.y += k;		
	}
	void subX(double k) {
		this.x -= k;		
	}
	void subY(double k) {
		this.y -= k;		
	}
	
}*/
