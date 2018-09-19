package rendercard;

import java.util.Random;

import rendercard.Terrain.level;
import rendercard.Terrain.terrainType;

public class TerrainGeometry {
	final double PI=Math.PI;
	final double PIBY2=(Math.PI/2);
	public float[] arrayTheta;
	public float[] arrayPhi;
	public float[] arrayDeviation;
	
	private Random random;
	Terrain terrain;

	public TerrainGeometry(Terrain t,level l) {
		random=new Random();
		terrain=t;
		arrayTheta=new float[terrain.globalRings*terrain.globalSectors];
		arrayPhi=new float[terrain.globalRings*terrain.globalSectors];
		arrayDeviation=new float[terrain.globalRings*terrain.globalSectors];
		
		if (l==Terrain.level.CRUDE) {
			
			double R=1.0f/(terrain.globalRings-1);
			double S=1.0f/(terrain.globalSectors-1);
			for(int r = 0; r < terrain.globalRings; r++) {
				for (int s = 0; s < terrain.globalSectors; s++) {
					float temp_theta = (float)(PI* r * R);
					float temp_phi = (float)(2*PI* s* S); 
					int counter=(r*terrain.globalSectors)+s;
					arrayTheta[counter]=temp_theta;
					arrayPhi[counter]=temp_phi;
					arrayDeviation[counter]=0.0f;			  
				}
			}
		} 
	}
	
	public void iterate(int iterations, double deviationFraction, boolean taper ) {

		// Create global terrain by Great Circles method
		for (int i=0;i<iterations;i++) {
			double theta1=random.nextDouble()*PIBY2;
			double phi1=random.nextDouble()*2*PI;

			double theta2=PI-theta1;
			double phi2=random.nextDouble()*2*PI;

			double theta3=random.nextDouble()*PIBY2;
			double phi3=random.nextDouble()*2*PI;

			double radius=1;

			double Ax=CoordinateConversion.getX(radius, theta1, phi1);
			double Ay=CoordinateConversion.getY(radius, theta1, phi1);
			double Az=CoordinateConversion.getZ(radius, theta1, phi1);

			double Bx=CoordinateConversion.getX(radius, theta2, phi2);
			double By=CoordinateConversion.getY(radius, theta2, phi2);
			double Bz=CoordinateConversion.getZ(radius, theta2, phi2);

			// Try changing C to a random point on the surface if the results below are too symmetrical (but note the theta problem with stretching longitudinally)
			double Cx=0;
			double Cy=0;
			double Cz=0;
			/*
			Cx=CoordinateConversion.getX(radius, theta3, phi3);
			Cy=CoordinateConversion.getY(radius, theta3, phi3);
			Cz=CoordinateConversion.getZ(radius, theta3, phi3);
			 */ 

			double BPx=Bx-Ax;	   
			double BPy=By-Ay;	   
			double BPz=Bz-Az;	   

			double CPx=Cx-Ax;	   
			double CPy=Cy-Ay;	   
			double CPz=Cz-Az;	   

			terrain.total_average_height=0;

			for (int counter=0;counter<(terrain.globalRings*terrain.globalSectors);counter++) {
				double Xx=CoordinateConversion.getX(radius, arrayTheta[counter], arrayPhi[counter]);
				double Xy=CoordinateConversion.getY(radius, arrayTheta[counter], arrayPhi[counter]);
				double Xz=CoordinateConversion.getZ(radius, arrayTheta[counter], arrayPhi[counter]);

				double XPx=Xx-Ax;	   
				double XPy=Xy-Ay;	   
				double XPz=Xz-Az;

				double det=(BPx*((CPy*XPz)-(CPz*XPy)))-(BPy*((CPx*XPz)-(CPz*XPx)))+(BPz*((CPx*XPy)-(CPy*XPx)));	 

				if (det>0) {
					arrayDeviation[counter]=arrayDeviation[counter]+(float)(deviationFraction);
				}
				else {
					arrayDeviation[counter]=arrayDeviation[counter]-(float)(deviationFraction);
				}
				

				if (arrayDeviation[counter]>terrain.max_height) terrain.max_height=arrayDeviation[counter];
				if (arrayDeviation[counter]<terrain.min_height) terrain.min_height=arrayDeviation[counter];		
				terrain.total_average_height=terrain.total_average_height+arrayDeviation[counter];
			}
			terrain.total_average_height=terrain.total_average_height/(terrain.globalRings*terrain.globalSectors);
		}
		deviationFraction=deviationFraction-(deviationFraction/iterations);
	}
	
	public void flattenSea() {
		// Flatten the sea
		float sea_level=terrain.terrainTypeBoundaries.get(terrainType.OCEAN);		
		for (int counter=0;counter<(terrain.globalRings*terrain.globalSectors);counter++) {
			if (arrayDeviation[counter]<sea_level) arrayDeviation[counter]=sea_level;		
		}
	}
	
	public double getHeight (double rho, double phi, double observerHeight) {
		return 0;
	}

	public float getQuickHeight (int ring, int sector) {
		return arrayDeviation[(ring*terrain.globalSectors)+sector];
	}

	public float getQuickTheta(int ring, int sector) {
		return arrayTheta[(ring*terrain.globalSectors)+sector];
	}
	
}


