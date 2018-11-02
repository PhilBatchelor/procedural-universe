package rendercard;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.EnumMap;

import javax.imageio.ImageIO;

public class Terrain {
	public static enum level {CRUDE,GLOBAL, CONTINENTAL, REGIONAL, LOCAL, VICINITY,  SURFACE};
	public static enum terrainType {OCEAN, SNOW, DESERT, ROCK, MOUNTAIN, GRASSLAND, FOREST, BEACH, ICE};
	final double PI=Math.PI;
	final double PIBY2=(Math.PI/2);
	private Random random;
	public int iterations=1;
	public double deviationFraction=0.01f;

	public OrbitingBody parent;
	public level currentLevel;

	public float max_height=0;
	public float min_height=0;
	public float total_average_height=0;

	public TerrainGeometry crudeGeometry;
	public TerrainGeometry globalGeometry;
	public TerrainGeometry continentalGeometry;
	public TerrainGeometry regionalGeometry;
	public TerrainGeometry localGeometry;
	public TerrainGeometry vicinityGeometry;
	public TerrainGeometry surfaceGeometry;

	public TerrainTexture crudeTexture;
	public TerrainTexture globalTexture;
	public TerrainTexture continentalTexture;
	public TerrainTexture regionalTexture;
	public TerrainTexture localTexture;
	public TerrainTexture vicinityTexture;
	public TerrainTexture surfaceTexture;

	public TerrainTexture terrainTexture=null;
	public EnumMap<level,Float> levelBoundaries=new EnumMap<level,Float>(level.class);
	public EnumMap<level,Float> levelPolygonFactors=new EnumMap<level,Float>(level.class);
	public EnumMap<level,Float> levelPortions=new EnumMap<level,Float>(level.class);
	public EnumMap<level,Integer> levelRings=new EnumMap<level,Integer>(level.class);
	public EnumMap<level,Integer> levelSegments=new EnumMap<level,Integer>(level.class);
	public EnumMap<level,TerrainGeometry> terrainGeometries=new EnumMap<level,TerrainGeometry>(level.class);
	public EnumMap<level,TerrainTexture> terrainTextures=new EnumMap<level,TerrainTexture>(level.class);
	public EnumMap<terrainType,Float> terrainTypeBoundaries=new EnumMap<terrainType,Float>(terrainType.class);
	private terrainType[] arrayTerrainType;

	float total_range;

	public Terrain(OrbitingBody p, level l,int iters, float devFrac) {
		currentLevel=l;
		parent=p;
		random=new Random();
		iterations=iters;
		deviationFraction=devFrac;

		// Set the boundaries between each level of detail, in radiuses from the surface
		levelBoundaries.put(level.CRUDE, 100f);
		levelBoundaries.put(level.GLOBAL, 10f);
		levelBoundaries.put(level.CONTINENTAL, .87f);
		levelBoundaries.put(level.REGIONAL, 0.5f);
		levelBoundaries.put(level.LOCAL, 0.27f);
		levelBoundaries.put(level.VICINITY, 0.15f);
		levelBoundaries.put(level.SURFACE, 0.08f);

		// Set the visible radius of each level
		levelPortions.put(level.CRUDE, (float)(.5*2*PI));
		levelPortions.put(level.GLOBAL, (float)(.5*2*PI));
		levelPortions.put(level.CONTINENTAL, (float)(.25*2*PI));
		levelPortions.put(level.REGIONAL, (float)(.18*2*PI));
		levelPortions.put(level.LOCAL, (float)(.125*2*PI));
		levelPortions.put(level.VICINITY, (float)(.087*2*PI));
		levelPortions.put(level.SURFACE, (float)(.06492*2*PI));

		int quality=5;

		// Set the number of rings at each level of detail
		levelRings.put(level.CRUDE, 20);
		levelRings.put(level.GLOBAL, (80*quality)+1);// SHOULD BE 80
		levelRings.put(level.CONTINENTAL, 80*quality);
		levelRings.put(level.REGIONAL, 80*quality);
		levelRings.put(level.LOCAL, 80*quality);
		levelRings.put(level.VICINITY,80*quality);
		levelRings.put(level.SURFACE, 80*quality);

		// Set the number of segments at each level of detail
		levelSegments.put(level.CRUDE, 40);
		levelSegments.put(level.GLOBAL, (160*quality)+1);// SHOULD BE 160
		levelSegments.put(level.CONTINENTAL, 80*quality);
		levelSegments.put(level.REGIONAL, 80*quality);
		levelSegments.put(level.LOCAL, 80*quality);
		levelSegments.put(level.VICINITY, 80*quality);
		levelSegments.put(level.SURFACE, 80*quality);

		// Generate the initial details for the whole body
		globalGeometry=new TerrainGeometry(this,0,0,level.GLOBAL);
		terrainGeometries.put(level.GLOBAL,globalGeometry);		
		System.out.println("created initial geometry");

		globalGeometry.greatCircle(iterations,(double)deviationFraction,true); // Apply the Great Circles Algorithm
		//globalGeometry.diamondSquare(1,0,0.01f); // Apply the Diamond Square
		//globalGeometry.tidySphere(); // Make sure the equator looks tidy by clearing up any rough edges

		globalTexture=new TerrainTexture(this,0,0,level.GLOBAL,globalGeometry);
		terrainTextures.put(level.GLOBAL,globalTexture);
		System.out.println("created initial texture");

		parent.terrainUpdated=false;

	}

	// Update the terrain objects based on the location of an observer in spherical coordinates relative to the centre of the planet
	public void updateTerrain(float[] spherical){
		level oldLevel=currentLevel;
		float range=spherical[0];
		float theta=spherical[1];
		float phi=spherical[2];
		boolean updated=false;
		//System.out.println("Range: "+range);

		// If we have moved to a new lat/long, do the the updates
		if ((Math.abs(theta-terrainGeometries.get(currentLevel).theta)>(levelPortions.get(currentLevel)/4)) || (Math.abs(phi-terrainGeometries.get(currentLevel).phi)>(levelPortions.get(currentLevel)/4))) {
			// Generate new Terrain Geometry and a new Terrain Texture
			switch (currentLevel) {
			case CONTINENTAL:
				System.out.println("This is a moved "+currentLevel);
				continentalGeometry=new TerrainGeometry(this,globalGeometry,theta,phi,currentLevel);
				terrainGeometries.put(level.CONTINENTAL,continentalGeometry);
				continentalTexture.update();
				terrainTextures.put(level.CONTINENTAL,continentalTexture);

				updated=true;
				break;
			case REGIONAL:
				System.out.println("This is a moved "+currentLevel);
				// Update the contiental tier geometry first, if required
				if ((Math.abs(theta-continentalGeometry.theta)>(levelPortions.get(level.CONTINENTAL)/4)) || (Math.abs(phi-continentalGeometry.phi)>(levelPortions.get(level.CONTINENTAL)/4))){
					System.out.println("First need to update CONTINENTAL TIER");
					continentalGeometry=new TerrainGeometry(this,globalGeometry,theta,phi,currentLevel);
					terrainGeometries.put(level.CONTINENTAL,continentalGeometry);
				}
				regionalGeometry=new TerrainGeometry(this,continentalGeometry,theta,phi,currentLevel);
				terrainGeometries.put(level.REGIONAL,regionalGeometry);
				regionalTexture=new TerrainTexture(this,theta,phi,currentLevel,regionalGeometry);
				terrainTextures.put(level.REGIONAL,regionalTexture);
				updated=true;
				break;
			case LOCAL:
				System.out.println("This is a moved "+currentLevel);
				// Update the contiental tier geometry first, if required
				if ((Math.abs(theta-continentalGeometry.theta)>(levelPortions.get(level.CONTINENTAL)/4)) || (Math.abs(phi-continentalGeometry.phi)>(levelPortions.get(level.CONTINENTAL)/4))){
					System.out.println("First need to update CONTINENTAL TIER");
					continentalGeometry=new TerrainGeometry(this,globalGeometry,theta,phi,level.CONTINENTAL);
					terrainGeometries.put(level.CONTINENTAL,continentalGeometry);
				}
				// Update the regional tier geometry first, if required
				if ((Math.abs(theta-regionalGeometry.theta)>(levelPortions.get(level.REGIONAL)/4)) || (Math.abs(phi-regionalGeometry.phi)>(levelPortions.get(level.REGIONAL)/4))){
					System.out.println("First need to update REGIONAL TIER");
					regionalGeometry=new TerrainGeometry(this,continentalGeometry,theta,phi,level.REGIONAL);
					terrainGeometries.put(level.REGIONAL,regionalGeometry);
				}
				localGeometry=new TerrainGeometry(this,regionalGeometry,theta,phi,currentLevel);
				terrainGeometries.put(level.LOCAL,localGeometry);
				localTexture=new TerrainTexture(this,theta,phi,currentLevel,localGeometry);
				terrainTextures.put(level.LOCAL,localTexture);
				updated=true;
				break;
			case VICINITY:
				System.out.println("This is a moved "+currentLevel);
				// Update the contiental tier geometry first, if required
				if ((Math.abs(theta-continentalGeometry.theta)>(levelPortions.get(level.CONTINENTAL)/4)) || (Math.abs(phi-continentalGeometry.phi)>(levelPortions.get(level.CONTINENTAL)/4))){
					System.out.println("First need to update CONTINENTAL TIER");
					continentalGeometry=new TerrainGeometry(this,globalGeometry,theta,phi,level.CONTINENTAL);
					terrainGeometries.put(level.CONTINENTAL,continentalGeometry);
				}
				// Update the regional tier geometry first, if required
				if ((Math.abs(theta-regionalGeometry.theta)>(levelPortions.get(level.REGIONAL)/4)) || (Math.abs(phi-regionalGeometry.phi)>(levelPortions.get(level.REGIONAL)/4))){
					System.out.println("First need to update REGIONAL TIER");
					regionalGeometry=new TerrainGeometry(this,continentalGeometry,theta,phi,level.REGIONAL);
					terrainGeometries.put(level.REGIONAL,regionalGeometry);
				}
				// Update the local tier geometry first, if required
				if ((Math.abs(theta-localGeometry.theta)>(levelPortions.get(level.LOCAL)/4)) || (Math.abs(phi-localGeometry.phi)>(levelPortions.get(level.LOCAL)/4))){
					System.out.println("First need to update LOCAL TIER");
					localGeometry=new TerrainGeometry(this,regionalGeometry,theta,phi,level.LOCAL);
					terrainGeometries.put(level.LOCAL,localGeometry);
				}
				vicinityGeometry=new TerrainGeometry(this,localGeometry,theta,phi,currentLevel);
				terrainGeometries.put(level.VICINITY,vicinityGeometry);
				vicinityTexture=new TerrainTexture(this,theta,phi,currentLevel,vicinityGeometry);
				terrainTextures.put(level.VICINITY,vicinityTexture);
				updated=true;
				break;
			case SURFACE:
				System.out.println("This is a moved "+currentLevel);
				// Update the contiental tier geometry first, if required
				if ((Math.abs(theta-continentalGeometry.theta)>(levelPortions.get(level.CONTINENTAL)/4)) || (Math.abs(phi-continentalGeometry.phi)>(levelPortions.get(level.CONTINENTAL)/4))){
					System.out.println("First need to update CONTINENTAL TIER");
					continentalGeometry=new TerrainGeometry(this,globalGeometry,theta,phi,level.CONTINENTAL);
					terrainGeometries.put(level.CONTINENTAL,continentalGeometry);
				}
				// Update the regional tier geometry first, if required
				if ((Math.abs(theta-regionalGeometry.theta)>(levelPortions.get(level.REGIONAL)/4)) || (Math.abs(phi-regionalGeometry.phi)>(levelPortions.get(level.REGIONAL)/4))){
					System.out.println("First need to update REGIONAL TIER");
					regionalGeometry=new TerrainGeometry(this,continentalGeometry,theta,phi,level.REGIONAL);
					terrainGeometries.put(level.REGIONAL,regionalGeometry);
				}
				// Update the local tier geometry first, if required
				if ((Math.abs(theta-localGeometry.theta)>(levelPortions.get(level.LOCAL)/4)) || (Math.abs(phi-localGeometry.phi)>(levelPortions.get(level.LOCAL)/4))){
					System.out.println("First need to update LOCAL TIER");
					localGeometry=new TerrainGeometry(this,regionalGeometry,theta,phi,level.LOCAL);
					terrainGeometries.put(level.LOCAL,localGeometry);
				}
				// Update the vicinity tier geometry first, if required
				if ((Math.abs(theta-vicinityGeometry.theta)>(levelPortions.get(level.VICINITY)/4)) || (Math.abs(phi-vicinityGeometry.phi)>(levelPortions.get(level.VICINITY)/4))){
					System.out.println("First need to update VICINITY TIER");
					vicinityGeometry=new TerrainGeometry(this,localGeometry,theta,phi,level.VICINITY);
					terrainTextures.put(level.VICINITY,vicinityTexture);
				}
				surfaceGeometry=new TerrainGeometry(this,vicinityGeometry,theta,phi,currentLevel);
				terrainGeometries.put(level.SURFACE,surfaceGeometry);
				surfaceTexture=new TerrainTexture(this,theta,phi,currentLevel,surfaceGeometry);
				terrainTextures.put(level.SURFACE,surfaceTexture);
				updated=true;
				break;
			}
			
		}

		// See which detail level we are currently at

		level newLevel=level.CRUDE;		
		for (level l:level.values()) {
			if (range<(parent.radius+(levelBoundaries.get(l)*parent.radius))) {
				newLevel=l;
			}
		}




		// If we have moved to a new Terrain Level, do the updates
		if (currentLevel!=newLevel){ 

			// If we have moved to a lower Terrain level
			if (levelBoundaries.get(newLevel)<levelBoundaries.get(currentLevel)) {
				currentLevel=newLevel;

				System.out.println("Update sequence initiated");

				// Generate new Terrain Geometry and a new Terrain Texture
				switch (newLevel) {
				case CRUDE:
					System.out.println("This is a new  "+newLevel);
					crudeGeometry=new TerrainGeometry(this,theta,phi,newLevel);
					terrainGeometries.put(level.CRUDE,crudeGeometry);
					crudeTexture=new TerrainTexture(this,theta,phi,newLevel,crudeGeometry);
					terrainTextures.put(level.CRUDE,crudeTexture);
					break;
				case GLOBAL:
					System.out.println("This is a new  "+newLevel);
					globalGeometry=new TerrainGeometry(this,theta,phi,newLevel);
					terrainGeometries.put(level.GLOBAL,globalGeometry);	
					globalTexture=new TerrainTexture(this,theta,phi,newLevel,globalGeometry);
					terrainTextures.put(level.GLOBAL,globalTexture);
					break;
				case CONTINENTAL:
					System.out.println("This is a new  "+newLevel);
					continentalGeometry=new TerrainGeometry(this,terrainGeometries.get(oldLevel),theta,phi,newLevel);
					terrainGeometries.put(level.CONTINENTAL,continentalGeometry);
					continentalTexture=new TerrainTexture(this,theta,phi,newLevel,continentalGeometry);
					terrainTextures.put(level.CONTINENTAL,continentalTexture);
					break;
				case REGIONAL:
					System.out.println("This is a new  "+newLevel);
					regionalGeometry=new TerrainGeometry(this,terrainGeometries.get(oldLevel),theta,phi,newLevel);
					terrainGeometries.put(level.REGIONAL,regionalGeometry);
					regionalTexture=new TerrainTexture(this,theta,phi,newLevel,regionalGeometry);
					terrainTextures.put(level.REGIONAL,regionalTexture);
					break;
				case LOCAL:
					System.out.println("This is a new "+newLevel);
					localGeometry=new TerrainGeometry(this,terrainGeometries.get(oldLevel),theta,phi,newLevel);
					terrainGeometries.put(level.LOCAL,localGeometry);
					localTexture=new TerrainTexture(this,theta,phi,newLevel,localGeometry);
					terrainTextures.put(level.LOCAL,localTexture);
					break;
				case VICINITY:
					System.out.println("This is a new "+newLevel);
					vicinityGeometry=new TerrainGeometry(this,terrainGeometries.get(oldLevel),theta,phi,newLevel);
					terrainGeometries.put(level.VICINITY,vicinityGeometry);
					vicinityTexture=new TerrainTexture(this,theta,phi,newLevel,vicinityGeometry);
					terrainTextures.put(level.VICINITY,vicinityTexture);
					break;
				case SURFACE:
					System.out.println("This is a new "+newLevel);
					surfaceGeometry=new TerrainGeometry(this,terrainGeometries.get(oldLevel),theta,phi,newLevel);
					terrainGeometries.put(level.SURFACE,surfaceGeometry);
					surfaceTexture=new TerrainTexture(this,theta,phi,newLevel,surfaceGeometry);
					terrainTextures.put(level.SURFACE,surfaceTexture);
					break;
				}
				updated=true;
			}

			// If we have moved to a higher Terrain level
			if (levelBoundaries.get(newLevel)>levelBoundaries.get(currentLevel)) {
				currentLevel=newLevel;
				System.out.println("We are ascending back to "+newLevel);
				terrainGeometries.put(newLevel,terrainGeometries.get(newLevel));
				terrainTextures.put(newLevel,terrainTextures.get(newLevel));
				updated=true;
			}


		}

		if (updated) {
			// Make sure the new Terrain object has a good resolution
			terrainGeometries.get(currentLevel).diamondSquare(1, iterations, deviationFraction);
			iterations++;
			// Update the GLTerrain object (or the backup flip)
			parent.terrainUpdated=true;
			System.out.println(currentLevel);
			System.out.println(range);
		}
	}

	public BufferedImage getDiffuseTextureBufferedImage() {
		return terrainTextures.get(currentLevel).getDiffuseTextureBufferedImage();
	}

	public void generateTerrain (level l, float rho1,float phi1, float rho2, float phi2) {
	}


	public void flattenSea() {
		globalGeometry.flattenSea();
	}


	// Return height above parent's body average diameter, with negative being below parent body average.
	// Accuracy is based on observer's height
	public double getHeight (double rho, double phi, double observerHeight) {
		return 0;
	}

	public float getQuickHeight (int ring, int sector) {
		return terrainGeometries.get(currentLevel).getQuickHeight(ring, sector);
	}

	public float getQuickTheta(int ring, int sector) {
		return terrainGeometries.get(currentLevel).getQuickTheta(ring, sector);
	}

	public float getQuickPhi(int ring, int sector) {
		return terrainGeometries.get(currentLevel).getQuickPhi(ring, sector);
	}

	public int getRings() {
		return terrainGeometries.get(currentLevel).rings;
	}


	public int getSectors() {
		return terrainGeometries.get(currentLevel).sectors;
	}



}
