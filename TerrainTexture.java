package rendercard;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Random;

import javax.imageio.ImageIO;

import rendercard.Terrain.terrainType;

public class TerrainTexture {
	Terrain terrain;
	EnumMap<terrainType,AuxilaryTexture> terrainTypeAuxilaryTextures=new EnumMap<terrainType,AuxilaryTexture>(terrainType.class);
	private Random random;
	private String texture_filename;
	private BufferedImage img;
	private int rings;
	private int sectors;
	private Terrain.level currentLevel;
	private TerrainGeometry geometry;

	public TerrainTexture(Terrain t, float theta,float phi,Terrain.level l,TerrainGeometry g) {
		terrain=t;
		geometry=g;
		random=new Random();
		rings=g.rings;
		sectors=g.sectors;
		currentLevel=l;
		texture_filename=l.toString()+".png";
		initiateColour();
		this.update();
		terrain.flattenSea();
	}

	public void update() {
		double time= System.currentTimeMillis();
		rings=geometry.rings;
		sectors=geometry.sectors;
	
		int width = sectors;
		int height = rings;
		
		float[] heights=new float[width*height];

		//create buffered image object img
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		//file object
		
		// Copy in from the TerrainGeometry object
		for(int y = 0; y<height ; y++) {
			for (int  x= 0; x < width; x++) {
				int counter=(y*width)+x;
				heights[counter]=geometry.getQuickHeight(y,x);
				int[] Colour=getColour(x,y,heights[counter],0.3f);
				int alpha = 255; //alpha
				int red=Colour[0];
				int green=Colour[1];
				int blue=Colour[2];
				int p = (alpha<<24) | (red<<16) | (green<<8) | blue; //pixel
				img.setRGB(x, y, p);
			}
		}
	}

	public void initiateColour(){		

		// Load in the auxilary texture files for each terrain type, which should be .bmp files named after each terrain type, eg beach.bmp
		for (terrainType t:terrainType.values()) {
			terrainTypeAuxilaryTextures.put(t,new AuxilaryTexture(new File("models/"+t.toString().toLowerCase()+".bmp")));
		}

		// Set the height boundaries between each type of terrain [NOTE this should probably go in a config file eventually]
		terrain.total_range=(terrain.max_height-terrain.min_height);
		terrain.terrainTypeBoundaries.put(terrainType.OCEAN,(float)(terrain.total_average_height+(terrain.total_range/20.0)));
		terrain.terrainTypeBoundaries.put(terrainType.GRASSLAND,(float)(terrain.total_average_height+(terrain.total_range/16.0)));
		terrain.terrainTypeBoundaries.put(terrainType.FOREST,(float)(terrain.total_average_height+(terrain.total_range/10.0)));
		terrain.terrainTypeBoundaries.put(terrainType.ROCK,(float)(terrain.total_average_height+(terrain.total_range/5.0)));
		terrain.terrainTypeBoundaries.put(terrainType.MOUNTAIN,(float)(terrain.total_average_height+(terrain.total_range/4.0)));
		terrain.terrainTypeBoundaries.put(terrainType.SNOW,(float)(terrain.total_average_height+(terrain.total_range/3.2)));
	}


	public int[] getColour(int x, int y,float height, float theta) {

		// Recolour the faces according to height
		double p_theta=(double)theta;
		double average_height=height;

		terrainType thisTerrainType;

		int red=3;
		int green=50;
		int blue=210;

		// SELECT TERAIN TYPE
		thisTerrainType=terrainType.OCEAN;

		if (average_height>terrain.terrainTypeBoundaries.get(terrainType.GRASSLAND)) {
			thisTerrainType=terrainType.GRASSLAND; 
			if (average_height>terrain.terrainTypeBoundaries.get(terrainType.FOREST)) thisTerrainType=terrainType.FOREST;
			if ((p_theta<((Math.PI/2)+(.1+( (random.nextDouble()-0.5)/10) )) ) && (p_theta>((Math.PI/2)-(.1+( (random.nextDouble()-0.5)/10))))) thisTerrainType=terrainType.DESERT;
			if (average_height>terrain.terrainTypeBoundaries.get(terrainType.ROCK)) thisTerrainType=terrainType.ROCK;
			if (average_height>terrain.terrainTypeBoundaries.get(terrainType.MOUNTAIN)) thisTerrainType=terrainType.MOUNTAIN;					
			if (average_height>terrain.terrainTypeBoundaries.get(terrainType.SNOW)) thisTerrainType=terrainType.SNOW;
		}

		if ( (p_theta<(.1+( (random.nextDouble()-0.5)/15) )))  thisTerrainType=terrainType.ICE;				
		if ( (p_theta>((Math.PI)-(.1+( (random.nextDouble()-0.5)/15) ))))  thisTerrainType=terrainType.ICE;						

		int pixel=terrainTypeAuxilaryTextures.get(thisTerrainType).getRGB(x,y);

		red = (pixel >>> 16) & 0xff;
		green = (pixel >>> 8) & 0xff;
		blue = pixel & 0xff;

		int[] C= {red,green,blue};
		return C;
	}

	private float triangulate(float x, float y, float tx1, float ty1, float tv1, float tx2, float ty2, float tv2, float tx3, float ty3, float tv3) {
		// Takes a point inside a triangle, and calculates the triangulated value at that point based on the distance to each vertex and the value at each vertex
		float w1=triangle_area(x,y,tx2,ty2,tx3,ty3);
		float w2=triangle_area(x,y,tx1,ty1,tx3,ty3);
		float w3=triangle_area(x,y,tx1,ty1,tx2,ty2);
		return ((w1*tv1)+(w2*tv2)+(w3*tv3))/(w1+w2+w3);
	}


	private float triangle_area (float x1,float y1, float x2, float y2, float x3, float y3) {
		return (float) Math.abs((( (x1*(y2 -y3)) + (x2*(y3 -y1)) + (x3*(y1 - y2))))/2.000);
	}

	public String getTextureFilename() {
		return texture_filename;
	}
	public BufferedImage getDiffuseTextureBufferedImage() {
		return img;
	}

	private float distance(float x1,float y1,float x2,float y2) {
		return (float)Math.hypot(x1-x2, y1-y2);
	}


}
