package rendercard;


/** This class provides conversions related to <a
 * href="http://mathworld.wolfram.com/SphericalCoordinates.html">spherical coordinates</a>.
 * <p>
 * The conventions used here are the physical ones, i.e. spherical coordinates are
 * related to Cartesian coordinates as follows:
 * </p>
 * <ul>
 *   <li>x = r cos(phi) sin(theta)</li>
 *   <li>y = r sin(phi) sin(theta)</li>
 *   <li>z = r cos(theta)</li>
 * </ul>
 * <ul>
 *   <li>r        = &radic;(x<sup>2</sup>+y<sup>2</sup>+z<sup>2</sup>)</li>
 *   <li>phi    = atan2(y, x)</li>
 *   <li>theta  = acos(z/r)</li>
 * </ul>
 * <p>
 * r is the radius, phi is the azimuthal angle in the x-y plane and theta is the polar
 * (co-latitude) angle. These conventions are the same as from the conventions used
 * in physics (and in particular in spherical harmonics).

*/

public class CoordinateConversion {
	final static double PIBY2=(Math.PI/2);
	
	public static double getR(double x,double y, double z) {
		// r = &radic;(x<sup>2</sup>+y<sup>2</sup>+z<sup>2</sup>)</li>
		return Math.sqrt((x * x) + (y * y) + (z * z));
	}
	
	public static double getPhi(double x,double y, double z) {
		// phi    = atan2(y, x)
		return Math.atan2(y, x);
	}
	
	public static double getTheta(double x,double y, double z) {
		// theta  = acos(z/r)
		return Math.acos(z / Math.sqrt((x * x) + (y * y) + (z * z)));
	}
	
	public static double getX(double r,double theta, double phi) {
		//x = r cos(phi) sin(theta)
		return r * Math.cos(phi) * Math.sin(theta);
	}
	
	public static double getY(double r,double theta, double phi) {
		// y = r sin(phi) sin(theta)
		return r * Math.sin(theta) * Math.sin(phi);
	}
	
	public static double getZ(double r,double theta, double phi) {
		// z = r cos(theta)
		return r * Math.cos(theta);
	}
	
	public static float[] getSpherical(float[] cartesian) {
		double x=cartesian[0];
		double y=cartesian[1];
		double z=cartesian[2];
		
		float[] spherical=new float[3];
		
		spherical[0]=(float)getR(x,y,z);
		spherical[1]=(float)getTheta(x,y,z);
		spherical[2]=(float)getPhi(x,y,z);
		return spherical;
	}
	
	public static float[] getCartesian(float[] spherical) {
		double r=spherical[0];
		double theta=spherical[1];
		double phi=spherical[2];
		
		float[] cartesian=new float[3];
		
		cartesian[0]=(float)getX(r,theta,phi);
		cartesian[1]=(float)getY(r,theta,phi);
		cartesian[2]=(float)getZ(r,theta,phi);
		return cartesian;
	}
	
}



