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
   }
   

   public void generateTerrain (level l, float rho1,float phi1, float rho2, float phi2) {
   }
   
   public void generateGlobalTerrain (level l,double radius) {
	   if (l==Terrain.level.CRUDE) {
		   globalRings=10;
		   globalSectors=10;
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
	   for (int i=0;i<iterations;i++) {
		   for (int counter=0;counter<(globalRings*globalSectors);counter++) {
			   double theta1=random.nextDouble()*PI;
			   double phi1=random.nextDouble()*2*PI;
			   double theta2=random.nextDouble()*PI;
			   double phi2=random.nextDouble()*2*PI;
			   double radius=parent.radius;
			   
			   double x1=CoordinateConversion.getX(radius, theta1, phi1);
			   double y1=CoordinateConversion.getY(radius, theta1, phi1);
			   double z1=CoordinateConversion.getZ(radius, theta1, phi1);
			   
			   double x2=CoordinateConversion.getX(radius, theta2, phi2);
			   double y2=CoordinateConversion.getY(radius, theta2, phi2);
			   double z2=CoordinateConversion.getZ(radius, theta2, phi2);
			   
			   
			   
			   
		   }
	   }
   }

   
   // Return height above parent's body average diameter, with negative being below parent body average.
   // Accuracy is based on observer's height
   public double getHeight (double rho, double phi, double observerHeight) {
    return 0;
   }
   
   public float getQuickHeight (int ring, int sector) {
	    return arrayDeviation[(ring*globalRings)+sector];
	   }

 }
