package rendercard;

public class CoordinateConversion {
	final static double PIBY2=(Math.PI/2);
	
	public static double getR(double x,double y, double z) {
		return Math.sqrt((x * x) + (y * y) + (z * z));
	}
	
	public static double getTheta(double x,double y, double z) {
		return Math.atan2(y, x);
	}
	
	public static double getPhi(double x,double y, double z) {
		return Math.acos(z / Math.sqrt((x * x) + (y * y) + (z * z)));
	}
	
	public static double getX(double r,double theta, double phi) {
		return Math.cos(phi) * Math.sin(theta) * r;
	}
	
	public static double getY(double r,double theta, double phi) {
		return Math.sin(-PIBY2+theta) * r;
	}
	
	public static double getZ(double r,double theta, double phi) {
		return Math.sin(phi) * Math.sin(theta) * r;
	}
	
}



