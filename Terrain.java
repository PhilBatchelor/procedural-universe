package rendercard;
import java.util.Random;

public class Terrain {
	public static enum level {CRUDE,GLOBAL, CONTINENTAL, REGIONAL, LOCAL, SURFACE};
	public static enum terrainType {OCEAN, SNOW, DESERT, ROCK, GRASSLAND, FOREST, BEACH};
	final double PI=Math.PI;
	final double PIBY2=(Math.PI/2);
	private Random random;

	public OrbitingBody parent;
	public level currentLevel;
	public int globalRings;
	public int globalSectors;
	public float[] arrayTheta;
	public float[] arrayPhi;
	public float[] arrayDeviation;

	public Terrain(int rings,int segments, OrbitingBody p) {
		globalRings=rings;
		globalSectors=segments;
		arrayTheta=new float[rings*segments];
		arrayPhi=new float[rings*segments];
		arrayDeviation=new float[rings*segments];
		parent=p;
		random=new Random();
	}


	public void generateTerrain (level l, float rho1,float phi1, float rho2, float phi2) {
	}

	public void generateGlobalTerrain (level l) {
		if (l==Terrain.level.CRUDE) {
			//globalRings=10;
			//globalSectors=10;
			double R=1.0f/(globalRings-1);
			double S=1.0f/(globalSectors-1);
			for(int r = 0; r < globalRings; r++) {
				for (int s = 0; s < globalSectors; s++) {
					float temp_theta = (float)(PI* r * R);
					float temp_phi = (float)(2*PI* s* S); 
					int counter=(r*globalRings)+s;
					arrayTheta[counter]=temp_theta;
					arrayPhi[counter]=temp_phi;
					arrayDeviation[counter]=0.0f;			  
				}
			}
		} 
	}

	public void iterateGlobalTerrain (int iterations, double deviationFraction, boolean taper ) {

		// Create global terrain by Great Circles method
		for (int i=0;i<iterations;i++) {
			double theta1=random.nextDouble()*PI;
			double phi1=random.nextDouble()*2*PI;
			double theta2=random.nextDouble()*PI;
			double phi2=random.nextDouble()*2*PI;
			double radius=parent.radius;

			double Ax=CoordinateConversion.getX(radius, theta1, phi1);
			double Ay=CoordinateConversion.getY(radius, theta1, phi1);
			double Az=CoordinateConversion.getZ(radius, theta1, phi1);

			double Bx=CoordinateConversion.getX(radius, theta2, phi2);
			double By=CoordinateConversion.getY(radius, theta2, phi2);
			double Bz=CoordinateConversion.getZ(radius, theta2, phi2);

			// Try changing C to a random point on the surface if the results below are too symmetrical
			double Cx=0;
			double Cy=0;
			double Cz=0;

			double BPx=Bx-Ax;	   
			double BPy=By-Ay;	   
			double BPz=Bz-Az;	   

			double CPx=Cx-Ax;	   
			double CPy=Cy-Ay;	   
			double CPz=Cz-Az;	   
			int neg=0;
			int pos=0;
			
			for (int counter=0;counter<(globalRings*globalSectors);counter++) {
				double Xx=CoordinateConversion.getX(radius, arrayTheta[counter], arrayPhi[counter]);
				double Xy=CoordinateConversion.getY(radius, arrayTheta[counter], arrayPhi[counter]);
				double Xz=CoordinateConversion.getZ(radius, arrayTheta[counter], arrayPhi[counter]);

				double XPx=Xx-Ax;	   
				double XPy=Xy-Ay;	   
				double XPz=Xz-Az;

				double det=(BPx*((CPy*XPz)-(CPz*XPy)))-(BPy*((CPx*XPz)-(CPz*XPx)))+(BPz*((CPx*XPy)-(CPy*XPx)));	 
				
				if (det>0) arrayDeviation[counter]=arrayDeviation[counter]+(float)(deviationFraction);
	
			}

		}
	}


	// Return height above parent's body average diameter, with negative being below parent body average.
	// Accuracy is based on observer's height
	public double getHeight (double rho, double phi, double observerHeight) {
		return 0;
	}

	public float getQuickHeight (int ring, int sector) {
		System.out.println("hey there");
		return arrayDeviation[(ring*globalRings)+sector];
	}

}
