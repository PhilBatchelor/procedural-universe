package rendercard;

import java.util.Random;

import rendercard.Terrain.level;
import rendercard.Terrain.terrainType;

public class TerrainGeometry {
	public static enum special {NORMAL, XZERO, YZERO, XMAX, YMAX};
	final float PI=(float)Math.PI;
	final float PIBY2=(float)(Math.PI/2);
	public float[] arrayTheta;
	public float[] arrayPhi;
	public float[] arrayDeviation;

	public double roughnessCurve=1.0001;
	private Random random;
	public int rings;
	public int sectors;
	public float theta;
	public float phi;
	private Terrain.level level;

	private int[] exportIndicies;
	int exportRings=0;
	int exportSectors=0;
	public int iteration;

	Terrain terrain;

	public TerrainGeometry(Terrain t,float the,float ph,level l) {
		random=new Random();
		terrain=t;
		level=l;
		theta=the;
		phi=ph;
		rings=t.levelRings.get(l);
		sectors=t.levelSegments.get(l);

		arrayTheta=new float[rings*sectors];
		arrayPhi=new float[rings*sectors];
		arrayDeviation=new float[rings*sectors];

		float delta_theta=PI;
		float delta_phi=PI;
		if ((l==Terrain.level.GLOBAL)||(l==Terrain.level.CRUDE)) delta_phi=delta_phi*2; // Render the whole sphere in the cases of CRUDE and GLOBAL terrains

		float start_theta=0;
		float start_phi=0;

		System.out.println("USING DEFAULT GEOMETRY CONSTRUCTOR");

		double R=1.0f/(rings-1);
		double S=1.0f/(sectors-1);

		float low_phi=999999999999f;
		float low_theta=999999999999f;
		float hi_phi=-999999999999f;
		float hi_theta=-999999999999f;

		for(int r = 0; r < rings; r++) {
			for (int s = 0; s < sectors; s++) {
				float temp_theta = start_theta+ (float)(delta_theta* r * R);
				float temp_phi = start_phi + (float)(delta_phi * s* S); 
				int counter=(r*sectors)+s;
				arrayTheta[counter]=temp_theta;
				arrayPhi[counter]=temp_phi;
				arrayDeviation[counter]=0.0f;		

				if (temp_phi  <  low_phi)   low_phi  =temp_phi;	
				if (temp_theta<  low_theta) low_theta=temp_theta;	
				if (temp_phi  >  hi_phi)    hi_phi=   temp_phi;	
				if (temp_theta>  hi_theta)  hi_theta= temp_theta;	
				/*
				// DIAGNOSTICS
				if ((counter==65535)) arrayDeviation[counter]=0.001f;
				//if ((s%2)==0) arrayDeviation[counter]=0.00099999f;
				// END DIAGNOSTICS
				 */
			}
		}

		System.out.println("BRAND NEW GEOMETRY COMPLETE. Region Lo Phi, Low Theta, Hi Phi, Hi Theta: "+low_phi+" , "+low_theta+" , "+hi_phi+" , "+hi_theta);
	}

	public void diamondSquare(int iterations, int start_iteration, float roughness) {
		System.out.println("RUNNING THE DIAMOND SQUARE ALGORITHM");
		float[] outputDeviation=new float[1];
		float[] outputTheta=new float[1];
		float[] outputPhi=new float[1];

		int start_width=sectors;
		int start_height=rings;
		int width=0;
		int height=0;

		for (int i=start_iteration;i<iterations+start_iteration;i++) {
			width=(start_width*2)-1;
			height=(start_height*2)-1;

			outputDeviation=new float[(width)*(height)];
			outputTheta=new float[(width)*(height)];
			outputPhi=new float[(width)*(height)];

			// Import Step
			for (int y=0;y<height;y=y+2) {
				for (int x=0;x<width;x=x+2) {
					int output_index=(y*width)+x;
					int input_index=((y/2)*(start_width))+(x/2);
					outputDeviation[output_index]=arrayDeviation[input_index];
					outputTheta[output_index]=arrayTheta[input_index];
					outputPhi[output_index]=arrayPhi[input_index];
				}
			}

			// Diamond Step
			for (int y=1;y<height-1;y=y+2) {
				for (int x=1;x<width-1;x=x+2) {
					int output_index=(y*width)+x;
					int x1=x-1; int y1=y-1; int index1=(y1*width)+x1;
					int x2=x+1; int y2=y-1; int index2=(y2*width)+x2;
					int x3=x-1; int y3=y+1; int index3=(y3*width)+x3;
					int x4=x+1; int y4=y+1; int index4=(y4*width)+x4;
					outputDeviation[output_index]=(outputDeviation[index1]+outputDeviation[index2]+outputDeviation[index3]+outputDeviation[index4])/4.00f;
					outputTheta[output_index]=(outputTheta[index1]+outputTheta[index2]+outputTheta[index3]+outputTheta[index4])/4.00f;
					outputPhi[output_index]=(outputPhi[index1]+outputPhi[index2]+outputPhi[index3]+outputPhi[index4])/4.00f;
					// NEED TO ADD DISPLACEMENT eg average=average+(2*random.nextDouble()*Math.pow(r,i))-Math.pow(r,i);
				}
			}

			//Square Step
			special specialCase=special.NORMAL;
			for (int y=0;y<height;y=y+1) {
				float tempTheta=outputTheta[(y*width)+1];
				int start_x=0; int end_x=width;
				if ((y%2)==0) {start_x=1; end_x=width-1;  tempTheta=outputTheta[y*width];}
				if (y==0) specialCase=special.YZERO;
				if (y==height-1) specialCase=special.YMAX;


				for (int x=start_x;x<end_x;x=x+2) {
					if (x==0) specialCase=special.XZERO;
					if (x==end_x-1) specialCase=special.XMAX;
					int output_index=(y*width)+x;

					int x1=x-1; int y1=y; 
					int x2=x;   int y2=y-1; 
					int x3=x+1; int y3=y;
					int x4=x;   int y4=y+1; 

					if (x1==-1) x1=1;
					if (y2==-1) y2=1;
					if (x3==width)  x3=width-2;
					if (y4==height) y4=height-2;

					int index1=(y1*width)+x1;
					int index2=(y2*width)+x2;
					int index3=(y3*width)+x3;
					int index4=(y4*width)+x4;

					select (specialCase) {
						CASE NORMAL: 
					outputDeviation[output_index]=((outputDeviation[index1]+outputDeviation[index2]+outputDeviation[index3]+outputDeviation[index4])/4.00f);
					outputTheta[output_index]=tempTheta;
					outputPhi[output_index]=outputPhi[index2];

					break;
					CASE XZERO:
					outputDeviation[output_index]=((outputDeviation[index2]+outputDeviation[index3]+outputDeviation[index4])/3.00f);
					outputTheta[output_index]=tempTheta;
					outputPhi[output_index]=outputPhi[index2];
					break;
					CASE YZERO: 
					outputDeviation[output_index]=((outputDeviation[index1]+outputDeviation[index3]+outputDeviation[index4])/3.00f);
					outputTheta[output_index]=tempTheta;
					outputPhi[output_index]=outputPhi[index4];
					break;
					CASE XMAX: 
					outputDeviation[output_index]=((outputDeviation[index1]+outputDeviation[index2]+outputDeviation[index4])/3.00f);
					outputTheta[output_index]=tempTheta;
					outputPhi[output_index]=outputPhi[index2];
					break;
					CASE YMAX: 
					outputDeviation[output_index]=((outputDeviation[index1]+outputDeviation[index2]+outputDeviation[index3])/3.00f);
					outputTheta[output_index]=tempTheta;
					outputPhi[output_index]=outputPhi[index2];
					break;
					}

					// NEED TO ADD DISPLACEMENT eg average=average+(2*random.nextDouble()*Math.pow(r,i))-Math.pow(r,i);
			        if ((specialCase==special.XZERO) || (specialCase=special.XMAX)) specialCase=special.NORMAL; 
				}	
                        specialCase=special.NORMAL;
			}



			/*
			//Final Smooth
			for (int y=0;y<height;y=y+2) {
				for (int x=0;x<width;x=x+2) {
					int output_index=(y*width)+x;
					int x1=x-1; int y1=y-1; 
					int x2=x+1; int y2=y-1;
					int x3=x-1; int y3=y+1; 
					int x4=x+1; int y4=y+1;

					if (x1==-1) x1=1;
					if (x3==-1) x3=1;
					if (y2==-1) y2=1;
					if (y1==-1) y1=1;
					if (x2==width)  x2=width-2;
					if (x4==width)  x4=width-2;
					if (y3==height) y3=height-2;
					if (y4==height) y4=height-2;

					int index1=(y1*width)+x1;
					int index2=(y2*width)+x2;
					int index3=(y3*width)+x3;
					int index4=(y4*width)+x4;

					output[output_index]=(output[index1]+output[index2]+output[index3]+output[index4])/4f;
					// NEED TO ADD DISPLACEMENT
				}
			}
			 */

			start_width=width;
			start_height=height;

			// Deep copy into arrayDeviation
			arrayDeviation=new float[start_width*start_height];
			arrayTheta=new float[start_width*start_height];
			arrayPhi=new float[start_width*start_height];
			for (int c=0;c<(start_width*start_height);c++) {
				arrayDeviation[c]=outputDeviation[c];
				arrayTheta[c]=outputTheta[c];
				arrayPhi[c]=outputPhi[c];
			}

		}

		rings=height;
		sectors=width;

	}

	public TerrainGeometry(Terrain t, TerrainGeometry parent,float the,float ph,level l) {
		random=new Random();
		terrain=t;
		level=l;
		rings=t.levelRings.get(l);
		sectors=t.levelSegments.get(l);
		theta=the;
		phi=ph;

		float delta_theta=t.levelPortions.get(l);
		float delta_phi=t.levelPortions.get(l);

		arrayTheta=new float[rings*sectors];
		arrayPhi=new float[rings*sectors];
		arrayDeviation=new float[rings*sectors];

		//if ((level==Terrain.level.GLOBAL)||(level==Terrain.level.CRUDE)) delta_phi=delta_phi*2; // Render the whole sphere in the cases of CRUDE and GLOBAL terrains

		System.out.println("----------------------------------");
		System.out.println("Making Geometry. Theta: "+theta);
		System.out.println("Making Geometry. Phi:"+phi);
		System.out.println("Making Geometry. Rings:"+rings);
		System.out.println("Making Geometry. Sectors:"+sectors);
		System.out.println("----------------------------------");

		parent.prepareForExport(theta,phi,delta_theta,delta_phi);

		rings=parent.getExportRings();
		sectors=parent.getExportSectors();

		arrayDeviation=parent.exportArrayDeviation();
		arrayTheta=parent.exportArrayTheta();
		arrayPhi=parent.exportArrayPhi();

		System.out.println("New Geometry has rings, sectors: "+rings+" , "+sectors);
		//if (level!=level.CONTINENTAL) diamondSquare(1,0,0.01f); // Apply the Diamond Square
		//diamondSquare(1,0,0.01f); // Apply the Diamond Square
		terrain.iterations++;
	}

	// Make the sphere join up properly with itself, by ensuring that terrain at theta=0 is the same as that at theta=PI
	public void tidySphere() {
		for (int r = 0; r < rings; r++) {
			int input_counter=r*sectors;
			int output_counter=(r*sectors)+(sectors-1);
			arrayDeviation[output_counter]=arrayDeviation[input_counter];
			//arrayTheta[output_counter]=arrayTheta[input_counter];
			arrayPhi[output_counter]=arrayPhi[input_counter];
		}
	}

	public void prepareForExport(float theta, float phi,float delta_theta,float delta_phi) {
		int counter=0;
		System.out.println("EXPORT FUNCTION: I have Rings, Sectors: "+rings+ ", "+sectors);
		System.out.println("EXPORT FUNCTION: I am targetting theta, phi: "+theta+ ", "+phi);

		float start_theta=theta-delta_theta;
		float start_phi=phi-delta_phi;
		float end_theta=theta+delta_theta;
		float end_phi=phi+delta_phi;	

		int minRing=9999999;
		int maxRing=-9999999;
		int minSector=99999999;
		int maxSector=-99999999;

		System.out.println("EXPORT FUNCTION: Before corrections, specific targetting range: start_theta, start phi. end theta, end phi :"+start_theta+" , "+start_phi+" , "+end_theta+" , "+end_phi);


		// COORECT FOR ANY NORTH POLE (THETA~0) WRAPPING ISSUES
		if ((start_theta<0) && (end_theta>0)) {
			if (-start_theta>end_theta) end_theta=-start_theta;
			start_theta=0f;
			start_phi=0f;
			end_phi=(2*PI);
		}

		// COORECT FOR ANY SOUTH POLE (THETA~PI) WRAPPING ISSUES
		if ((start_theta<PI) && (end_theta>PI)) {
			if ((end_theta-PI)>-(start_theta-PI)) start_theta=PI-(end_theta-PI);
			end_theta=PI;
			start_phi=0f;
			end_phi=(2*PI);
		}

		// CORRECT FOR ANY PHI~0 WRAPPING ISSUES
		boolean philoop=false;
		if ((start_phi<0f) && (end_phi>0f)) {
			start_phi=start_phi+(2*PI);
			philoop=true;
		}

		// CORRECT FOR ANY PHI~2*PI WRAPPING ISSUES
		if ((start_phi<(2*PI)) && (end_phi>(2*PI))) {
			end_phi=end_phi-(2*PI);
			philoop=true;
		}

		// CORRECT FOR ANY PHI<0  ISSUES
		if ((start_phi<(0) && (end_phi<0))) {
			end_phi=end_phi+(2*PI);
			start_phi=start_phi+(2*PI);
		}
		
		// CORRECT FOR ANY PHI>2*PI ISSUES
		if ((start_phi>(2*PI) && (end_phi>(2*PI)))) {
			end_phi=end_phi-(2*PI);
			start_phi=start_phi-(2*PI);
		}


		System.out.println("EXPORT FUNCTION: Specific targetting range: start_theta, start phi. end theta, end phi :"+start_theta+" , "+start_phi+" , "+end_theta+" , "+end_phi);


		if (philoop==false) {
			for (int r=0;r<rings;r++) {
				for (int s=0;s<sectors;s++) {
					if ((arrayTheta[(r*sectors)+s]>=start_theta) && (arrayTheta[(r*sectors)+s]<=end_theta) && (arrayPhi[(r*sectors)+s]>=start_phi)  && (arrayPhi[(r*sectors)+s]<=end_phi)) {
						if (r<minRing) minRing=r;
						if (r>maxRing) maxRing=r;
						if (s<minSector) minSector=s;
						if (s>maxSector) maxSector=s;
					}
				}
			}
		}


		// Particular way of handling Phi loop issues;
		if (philoop==true) {
			for (int r=0;r<rings;r++) {
				for (int s=0;s<sectors;s++) {
					if ((arrayTheta[(r*sectors)+s]>=start_theta) && (arrayTheta[(r*sectors)+s]<=end_theta) && ((arrayPhi[(r*sectors)+s]>=start_phi)  || (arrayPhi[(r*sectors)+s]<=end_phi))) {
						if (r<minRing) minRing=r;
						if (r>maxRing) maxRing=r;
						if (s<minSector) minSector=s;
						if (s>maxSector) maxSector=s;
					}
				}
			}
		}

		// EXPORT BASED ON A GIVEN NUMBER OF RINGS AND SEGMENTS AROUND THE TARGET CO-ORDINATE

		System.out.println("FOUND: Min Ring, min Sector, max ring, max sector: "+minRing+" , "+minSector+" , "+maxRing+" , "+maxSector);

		exportRings=maxRing-minRing+1;
		exportSectors=maxSector-minSector+1;

		if (exportRings%2==0) {maxRing--; exportRings--;}
		if (exportSectors%2==0) {maxSector++; exportSectors++;}

		System.out.println("EXPORT FUNCTION: I am exporting: Rings, Sectors: "+exportRings+ ", "+exportSectors);

		exportIndicies=new int[exportRings*exportSectors];

		float low_phi=999999999999f;
		float low_theta=999999999999f;
		float hi_phi=-999999999999f;
		float hi_theta=-999999999999f;

		for (int r=minRing;r<=maxRing;r++) {
			int tempR=r;
			//if (r<0) tempR=-r; // Wrap around the sphere
			//if (r>=rings)  tempR=rings-(r-rings); // Wrap around the sphere
			for (int s=minSector;s<=maxSector;s++) {
				int tempS=s;
				//if (r<0) tempS=(s-(sectors/2));
				//if (tempS<0) tempS=-s; // Wrap around the sphere
				//if (tempS>=sectors) tempS=tempS-sectors; // Wrap around the sphere

				exportIndicies[counter]=(tempR*sectors)+tempS;
				float tphi=arrayPhi[(tempR*sectors)+tempS];
				float ttheta=arrayPhi[(tempR*sectors)+tempS];

				if (tphi  <  low_phi)   low_phi  =tphi;	
				if (ttheta<  low_theta) low_theta=ttheta;	
				if (tphi  >  hi_phi)    hi_phi=   tphi;
				if (ttheta>  hi_theta)  hi_theta= ttheta;	
				counter++;
			}
		}

		System.out.println("EXPORT READY. Region Lo Phi, Low Theta, Hi Phi, Hi Theta: "+low_phi+" , "+low_theta+" , "+hi_phi+" , "+hi_theta);
	}

	public int getExportRings() {
		return exportRings;
	}

	public int getExportSectors() {
		return exportSectors;
	}

	public float[] exportArrayDeviation() {
		float exportArrayDeviation[]=new float[exportIndicies.length];
		for (int i=0;i<exportIndicies.length;i++) {
			exportArrayDeviation[i]=arrayDeviation[exportIndicies[i]];
		}
		return exportArrayDeviation;
	}

	public float[] exportArrayTheta() {
		float exportArrayTheta[]=new float[exportIndicies.length];
		for (int i=0;i<exportIndicies.length;i++) {
			exportArrayTheta[i]=arrayTheta[exportIndicies[i]];
		}
		return exportArrayTheta;
	}


	public float[] exportArrayPhi() {
		float exportArrayPhi[]=new float[exportIndicies.length];
		for (int i=0;i<exportIndicies.length;i++) {
			exportArrayPhi[i]=arrayPhi[exportIndicies[i]];
		}
		return exportArrayPhi;
	}

	public void greatCircle(int iterations, double deviationFraction, boolean taper ) {

		// Create global terrain by Great Circles method
		// NOTE - SHOULD ADD OFFSET AND USE MATH.POW(ROUGHNESS,ITERATION)

		System.out.println("RUNNING THE GREAT CIRCLE ALGORITHM");

		double Xx[]=new double[rings*sectors];
		double Xy[]=new double[rings*sectors];
		double Xz[]=new double[rings*sectors];

		for (int counter=0;counter<(rings*sectors);counter++) {
			Xx[counter]=CoordinateConversion.getX(1, arrayTheta[counter], arrayPhi[counter]);
			Xy[counter]=CoordinateConversion.getY(1, arrayTheta[counter], arrayPhi[counter]);
			Xz[counter]=CoordinateConversion.getZ(1, arrayTheta[counter], arrayPhi[counter]);
		}

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
			for (int counter=0;counter<(rings*sectors);counter++) {
				double TXx=Xx[counter];
				double TXy=Xy[counter];
				double TXz=Xz[counter];
				double XPx=TXx-Ax;	   
				double XPy=TXy-Ay;	   
				double XPz=TXz-Az;
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
			terrain.total_average_height=terrain.total_average_height/(rings*sectors);
			deviationFraction=Math.pow(deviationFraction,roughnessCurve);
		}
	}

	public void flattenSea() {
		// Flatten the sea
		float sea_level=terrain.terrainTypeBoundaries.get(terrainType.OCEAN);		
		for (int counter=0;counter<(rings*sectors);counter++) {
			if (arrayDeviation[counter]<sea_level) arrayDeviation[counter]=sea_level;		
		}
	}

	public double getHeight (double rho, double phi, double observerHeight) {
		return 0;
	}

	public float getQuickHeight (int ring, int sector) {
		//System.out.println("Uh-oh. Trouble at ring, sector: "+ring+", "+sector);
		return arrayDeviation[(ring*sectors)+sector];
	}

	public float getQuickTheta(int ring, int sector) {
		return arrayTheta[(ring*sectors)+sector];
	}

	public float getQuickPhi(int ring, int sector) {
		return arrayPhi[(ring*sectors)+sector];
	}

}

