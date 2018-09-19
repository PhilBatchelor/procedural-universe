package rendercard;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.EnumMap;

import javax.imageio.ImageIO;

public class Terrain {
	public static enum level {CRUDE,GLOBAL, CONTINENTAL, REGIONAL, LOCAL, SURFACE};
	public static enum terrainType {OCEAN, SNOW, DESERT, ROCK, MOUNTAIN, GRASSLAND, FOREST, BEACH, ICE};
	final double PI=Math.PI;
	final double PIBY2=(Math.PI/2);
	private Random random;

	public OrbitingBody parent;
	public level currentLevel;
	public float max_height=0;
	public float min_height=0;
	public float total_average_height=0;
	public int globalRings;
	public int globalSectors;

	public TerrainGeometry crudeGeometry;
	public TerrainGeometry globalGeometry;
	public TerrainGeometry continentalGeometry;
	public TerrainGeometry regionalGeometry;
	public TerrainGeometry localGeometry;
	public TerrainGeometry surfaceGeometry;

	public TerrainTexture crudeTexture;
	public TerrainTexture globalTexture;
	public TerrainTexture continentalTexture;
	public TerrainTexture regionalTexture;
	public TerrainTexture localTexture;
	public TerrainTexture surfaceTexture;

	public TerrainTexture terrainTexture=null;

	EnumMap<level,Float> levelBoundaries=new EnumMap<level,Float>(level.class);
	EnumMap<terrainType,Float> terrainTypeBoundaries=new EnumMap<terrainType,Float>(terrainType.class);
	
	EnumMap<level,TerrainGeometry> terrainGeometries=new EnumMap<level,TerrainGeometry>(level.class);
	EnumMap<level,TerrainTexture> terrainTextures=new EnumMap<level,TerrainTexture>(level.class);


	private terrainType[] arrayTerrainType;

	float total_range;

	public Terrain(int rings,int segments, OrbitingBody p, level l) {
		level initialLevel=l;
		currentLevel=l;
		globalRings=rings;
		globalSectors=segments;

		parent=p;
		random=new Random();
		globalGeometry=new TerrainGeometry(this,initialLevel);
		
		levelBoundaries.put(level.CRUDE, 100f);
		levelBoundaries.put(level.GLOBAL, 10f);
		levelBoundaries.put(level.CONTINENTAL, 1f);
		levelBoundaries.put(level.REGIONAL, 0.5f);
		levelBoundaries.put(level.LOCAL, 0.1f);
		levelBoundaries.put(level.SURFACE, 0.05f);
		
		terrainGeometries.put(level.CRUDE,crudeGeometry);
		terrainGeometries.put(level.GLOBAL,globalGeometry);
		terrainGeometries.put(level.CONTINENTAL,continentalGeometry);
		terrainGeometries.put(level.REGIONAL,regionalGeometry);
		terrainGeometries.put(level.LOCAL,localGeometry);
		terrainGeometries.put(level.SURFACE,surfaceGeometry);
		
		terrainTextures.put(level.CRUDE,crudeTexture);
		terrainTextures.put(level.GLOBAL,globalTexture);
		terrainTextures.put(level.CONTINENTAL,continentalTexture);
		terrainTextures.put(level.REGIONAL,regionalTexture);
		terrainTextures.put(level.LOCAL,localTexture);
		terrainTextures.put(level.SURFACE,surfaceTexture);
		
	}

	public void updateTerrain(float range){
		
		// See which detail level we are currently at
		
		level newLevel=level.CRUDE;
		
		for (level l:level.values()) {
			if (range<(parent.radius+(levelBoundaries.get(l)*parent.radius))) {
				newLevel=l;
			}
		}
		
		if (currentLevel==newLevel) return;
		
		currentLevel=newLevel;
		
		// Generate new Terrain Geometry and a new Terrain Texture
		switch (newLevel) {
		case CRUDE:
			crudeGeometry=new TerrainGeometry(this,newLevel);
			crudeTexture=new TerrainTexture(this,newLevel);
		case GLOBAL:
			globalGeometry=new TerrainGeometry(this,newLevel);
			globalTexture=new TerrainTexture(this,newLevel);
		case REGIONAL:
			regionalGeometry=new TerrainGeometry(this,newLevel);
			regionalTexture=new TerrainTexture(this,newLevel);
		case LOCAL:
			localGeometry=new TerrainGeometry(this,newLevel);
			localTexture=new TerrainTexture(this,newLevel);
		case CONTINENTAL:
			continentalGeometry=new TerrainGeometry(this,newLevel);
			continentalTexture=new TerrainTexture(this,newLevel);
		case SURFACE:
			surfaceGeometry=new TerrainGeometry(this,newLevel);
			surfaceTexture=new TerrainTexture(this,newLevel);
		}
		
		// Iterate terrain as required
		// Update the GLTerrain object (or the backup flip)
		// Tell the RenderEngine to pull the new GLTerrain object into the Graphics Card
		
		System.out.println(currentLevel);
		System.out.println(range);
		
	}

	public String getDiffuseTexture() {
		if (terrainTexture==null) terrainTexture=new TerrainTexture(this,currentLevel);
		return terrainTexture.getTextureFilename();
	}

	public void generateTerrain (level l, float rho1,float phi1, float rho2, float phi2) {
	}

	public void iterateGlobalTerrain (int iterations, double deviationFraction, boolean taper ) {
		globalGeometry.iterate(iterations,deviationFraction,taper);
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
		return globalGeometry.getQuickHeight(ring, sector);
	}

	public float getQuickTheta(int ring, int sector) {
		return globalGeometry.getQuickTheta(ring, sector);
	}



}
