package rendercard;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Random;

import javax.imageio.ImageIO;

import rendercard.Terrain.terrainType;

public class TerrainTexture {
	Terrain terrain;
	EnumMap<terrainType,AuxilaryTexture> terrainTypeAuxilaryTextures=new EnumMap<terrainType,AuxilaryTexture>(terrainType.class);
	private Random random;
	private String texture_filename="terrain.png";

	public TerrainTexture(Terrain t, Terrain.level l) {
		terrain=t;
		random=new Random();

		if ((l==Terrain.level.CRUDE) || (l==Terrain.level.GLOBAL)) {
			//set the image dimensions
			int xstep = 5;
			int ystep = 5;
			int width = (xstep*(terrain.globalSectors))-xstep;
			int height = (ystep*(terrain.globalRings))-ystep;

			//create buffered image object img
			BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			//file object
			File f = null;

			int xincrement=0;
			int yincrement=0;
			int currentRing=0;
			int currentSector=0;
			int refRing1=0;
			int refSector1=0;
			int refRing2=0;
			int refSector2=0;

			float[] refWeight1=new float[xstep*ystep];
			float[] refWeight2=new float[xstep*ystep];
			float[] refWeightM=new float[xstep*ystep];
			char[] refPoint1=new char[xstep*ystep];
			char[] refPoint2=new char[xstep*ystep];

			float xm=xstep/2;
			float ym=ystep/2;
			float hm=(terrain.getQuickHeight(currentRing,currentSector)+terrain.getQuickHeight(currentRing+1,currentSector)+terrain.getQuickHeight(currentRing,currentSector+1)+terrain.getQuickHeight(currentRing+1,currentSector+1))/4;
			float tm=(terrain.getQuickTheta (currentRing,currentSector)+terrain.getQuickTheta (currentRing+1,currentSector)+terrain.getQuickTheta (currentRing,currentSector+1)+terrain.getQuickTheta (currentRing+1,currentSector+1))/4;

			// Pre-calculate the closest established reference point to each point on the texture, and the relative weighting
			for(int y = 0; y < (ystep); y++){
				for(int x = 0; x < (xstep); x++){

					// Calculate the nearest two reference points and store them for later
					float oa=distance(x,y,0,0);
					float ob=distance(x,y,xstep,0);
					float oc=distance(x,y,0,ystep);
					float od=distance(x,y,xstep,ystep);

					float tmp;
					float a=oa; float b=ob;float c=oc;float d=od;			
					float x1=0; float x2=0; float y1=0; float y2=0;

					if (a > b) { tmp = a; a = b; b = tmp; }
					if (c > d) { tmp = c; c = d; d = tmp; }
					if (a > c) { tmp = a; a = c; c = tmp; }
					if (b > d) { tmp = b; b = d; d = tmp; }
					if (b > c) { tmp = b; b = c; c = tmp; }

					if (oa==a) {  	refPoint1[(y*xstep)+x]='a'; x1=0; 		y1=0;}
					if (ob==a) {  	refPoint1[(y*xstep)+x]='b'; x1=xstep; 	y1=0;}
					if (oc==a) {  	refPoint1[(y*xstep)+x]='c'; x1=0; 		y1=ystep;}
					if (od==a) {  	refPoint1[(y*xstep)+x]='d'; x1=xstep; 	y1=ystep;}

					if (od==b) {  	refPoint2[(y*xstep)+x]='d'; x2=xstep; 	y2=ystep;}
					if (oc==b) {  	refPoint2[(y*xstep)+x]='c'; x2=0; 		y2=ystep;}
					if (ob==b) {  	refPoint2[(y*xstep)+x]='b'; x2=xstep; 	y2=0;}				
					if (oa==b) {  	refPoint2[(y*xstep)+x]='a'; x2=0; 		y2=0;}

					// Calculate and store the weights of the reference points
					float[] weights=preTriangulate(x,y,x1,y1,x2,y2,xm,ym);
					refWeight1[(y*xstep)+x]=weights[0];
					refWeight2[(y*xstep)+x]=weights[1];
					refWeightM[(y*xstep)+x]=weights[2];
				}
			}


			// Setup the colour map
			initiateColour();

			// Generate the texture image file, pixel by pixel
			for(int y = 0; y < (height); y++){
				for(int x = 0; x < (width); x++){

					int index=(yincrement*xstep)+xincrement;

					if (refPoint1[index]=='a') {refRing1=currentRing; refSector1=currentSector;}
					if (refPoint1[index]=='b') {refRing1=currentRing; refSector1=currentSector+1;}
					if (refPoint1[index]=='c') {refRing1=currentRing+1; refSector1=currentSector;}
					if (refPoint1[index]=='d') {refRing1=currentRing+1; refSector1=currentSector+1;}

					if (refPoint2[index]=='a') {refRing2=currentRing; refSector2=currentSector;}
					if (refPoint2[index]=='b') {refRing2=currentRing; refSector2=currentSector+1;}
					if (refPoint2[index]=='c') {refRing2=currentRing+1; refSector2=currentSector;}
					if (refPoint2[index]=='d') {refRing2=currentRing+1; refSector2=currentSector+1;}

					float height1=terrain.getQuickHeight(refRing1,refSector1);
					float theta1=terrain.getQuickTheta(refRing1,refSector1);

					float height2=terrain.getQuickHeight(refRing2,refSector2);
					float theta2=terrain.getQuickTheta(refRing2,refSector2);

					float terrain_height=fastTriangulate(refWeight1[index],refWeight2[index],refWeightM[index],height1,height2,hm);
					float terrain_theta=fastTriangulate(refWeight1[index],refWeight2[index],refWeightM[index],theta1,theta2,tm);

					if ((xincrement==xstep/2) && (yincrement==ystep/2)) {terrain_height=hm; terrain_theta=tm;}

					int[] Colour=getColour(x,y,terrain_height,terrain_theta);

					int alpha = 255; //alpha
					int red=Colour[0];
					int green=Colour[1];
					int blue=Colour[2];

					// Gridlines
					/*
				if (!((xincrement==0) && (yincrement==0))) {
					if ((xincrement==0) ||(yincrement==0)) {red=255; green=0;}
				}
					 */

					int p = (alpha<<24) | (red<<16) | (green<<8) | blue; //pixel
					img.setRGB(x, height-y-1, p);
					xincrement++;
					if (xincrement==(xstep)) {
						xincrement=0; 
						currentSector++;

						if (currentSector<terrain.globalSectors-1) {
							xm=(currentSector*xstep)+(xstep/2);
							hm=(terrain.getQuickHeight(currentRing,currentSector)+terrain.getQuickHeight(currentRing+1,currentSector)+terrain.getQuickHeight(currentRing,currentSector+1)+terrain.getQuickHeight(currentRing+1,currentSector+1))/4;
							tm=(terrain.getQuickTheta(currentRing,currentSector)+terrain.getQuickTheta(currentRing+1,currentSector)+terrain.getQuickTheta(currentRing,currentSector+1)+terrain.getQuickTheta(currentRing+1,currentSector+1))/4;
						}
					}
				}
				yincrement++;
				xincrement=0;
				currentSector=0;
				xm=xstep/2;

				if (yincrement==(ystep)) { 
					yincrement=0; 
					currentRing++;
					ym=(currentRing*ystep)+(ystep/2);
				}
				if (currentRing<terrain.globalRings-1) {
					hm=(terrain.getQuickHeight(currentRing,currentSector)+terrain.getQuickHeight(currentRing+1,currentSector)+terrain.getQuickHeight(currentRing,currentSector+1)+terrain.getQuickHeight(currentRing+1,currentSector+1))/4;
					tm=(terrain.getQuickTheta(currentRing,currentSector)+terrain.getQuickTheta(currentRing+1,currentSector)+terrain.getQuickTheta(currentRing,currentSector+1)+terrain.getQuickTheta(currentRing+1,currentSector+1))/4;
				}
			}

			try {
				ImageIO.write(img, "png", new File("models/"+texture_filename));
			} catch (IOException e) {
				e.printStackTrace();
			}

			terrain.flattenSea();
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


		// COLOUR ACCORDING TO TERRAIN TYPE

		switch (thisTerrainType) {
		case OCEAN:
			red=0;//(random.nextInt(5));;
			green =0;//+(random.nextInt(30)-15);
			blue=255;//(random.nextInt(5));		
		case FOREST: 
			red=10;//(random.nextInt(5));;
			green =108;//+(random.nextInt(30)-15);
			blue=10;//(random.nextInt(5));
		case DESERT: 
			red=100;
			green =100;
			blue=10;
		case ROCK: 
			red=140+(random.nextInt(30)-15);
			green =125+(random.nextInt(30)-15);
			blue=110+(random.nextInt(30)-15);
		case MOUNTAIN:
			red=125+(random.nextInt(30)-15);
			green =125+(random.nextInt(30)-15);
			blue=125+(random.nextInt(30)-15);
		case SNOW:
			red=230+(random.nextInt(30)-15);
			green =230+(random.nextInt(30)-15);
			blue=230+(random.nextInt(30)-15);
		case ICE: 
			red=230+(random.nextInt(30)-15);
			green =230+(random.nextInt(30)-15);
			blue=230+(random.nextInt(30)-15);
		}

		int pixel=0;


		pixel=terrainTypeAuxilaryTextures.get(thisTerrainType).getRGB(x,y);

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


	private float[] preTriangulate (float x, float y, float tx1, float ty1, float tx2, float ty2,  float tx3, float ty3) {
		// Takes a point inside a triangle, and returns the relative weighting of each vertex
		float w1=triangle_area(x,y,tx2,ty2,tx3,ty3);
		float w2=triangle_area(x,y,tx1,ty1,tx3,ty3);
		float w3=triangle_area(x,y,tx1,ty1,tx2,ty2);
		float[] weights={w1,w2,w3};
		return weights;
	}

	private float fastTriangulate (float w1, float w2, float w3, float tv1, float tv2, float tv3 ) {
		// Takes a point inside a triangle, and calculates the value at that point based on the relative weighting of each vertex and the value at each vertex
		return ((w1*tv1)+(w2*tv2)+(w3*tv3))/(w1+w2+w3);
	}


	private float triangle_area (float x1,float y1, float x2, float y2, float x3, float y3) {
		return (float) Math.abs((( (x1*(y2 -y3)) + (x2*(y3 -y1)) + (x3*(y1 - y2))))/2.000);
	}

	public String getTextureFilename() {
		return texture_filename;
	}

	private float distance(float x1,float y1,float x2,float y2) {
		return (float)Math.hypot(x1-x2, y1-y2);
	}


}
