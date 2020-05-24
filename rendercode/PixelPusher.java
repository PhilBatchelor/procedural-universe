import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.JFrame;

public class PixelPusher implements Runnable {
	private World world;
	private Random random;

	private int height;
	private int width;
	private int pixels[];
	static private int zeros[];

	final int fog_red=100;				// RGB value of fog
	final int fog_green=100;		     // RGB value of fog
	final int fog_blue=200;				// RGB value of fog

	final int sky_red=100;				 
	final int sky_green=150;				
	final int sky_blue=200;	

	private int tri;
	private int i;
	private int index;
	private int ext;
	private int obj_ref;
	private int face_ref;
	private int x1;
	private int x2;
	private int x3;
	private int y1;
	private int y2;
	private int y3;	
	private int d_x;	
	private double max;
	private double factor;
	private int min_y;
	private int max_y;
	private int vertex_ref;
	private int num_shared_faces;
	private double w1;
	private double w2;
	private double w3;
	private int r1;
	private int g1;
	private int b1;
	private int r2;
	private int g2;
	private int b2;
	private int r3;
	private int g3;
	private int b3;

	private int t;
	private int tex_ref;
	private int tex_x;
	private int tex_y;
	private int tex_x1;
	private int tex_x2;
	private int tex_x3;
	private int tex_y1;
	private int tex_y2;
	private int tex_y3;
	private double depth;
	private double depth_1;
	private double depth_2;
	private double depth_3;

	private double invgrad_ac=Double.POSITIVE_INFINITY;
	private double invgrad_ab=Double.POSITIVE_INFINITY;
	private double invgrad_bc=Double.POSITIVE_INFINITY;
	private double totalweight;
	private int red;
	private int green;
	private int blue;
	private int subtract=0;
	private int totalcolour;


	private int startxtri=0;
	private int endxtri=0;

	private int startx=0;
	private int starty=0;
	private int endx;
	private int endy;

	private int la=99;

	CountDownLatch waitlatch;
	ReentrantReadWriteLock lock;

	private int layers;
	private int mylayer;
	private ArrayList<TriangleRef> new_list_of_triangles;

	public PixelPusher(World w,  int h, int wi, CountDownLatch l, ReentrantReadWriteLock lo, int s, int m, int p[]) {

		world=w;		// The world that this pixelpusher is rendering
		height=h;		// Height of the whole screen
		width=wi;		// Width of the whole screen
		waitlatch=l;	// Countdown latch
		lock=lo;		// Render lock
		layers=s;		// Total number of independent layers/layers the screen is broken up into. 
		mylayer=m;		// Which layer/layer this pixelpusher is pushing to
		pixels=p;
		endx=wi;
		endy=h;

		random=new Random();
		zeros=new int[width*height];	

	}

	public void run() {

		while (1==1) {
			lock.readLock().lock();
			System.arraycopy(zeros,0,pixels,0,width*height);
			if (mylayer==1) {drawBackgroundGouraudShaded();}
			drawTrianglesGouraudShaded();
			lock.readLock().unlock();
			waitlatch.countDown();
		}
	}


	public void resetWaitLatch(CountDownLatch l) {
		waitlatch=l;
	}

	public  void drawTrianglesGouraudShaded() {


		double curTime=System.currentTimeMillis();		
		double remove=0;
		if (mylayer>0) curTime=System.currentTimeMillis();
		if (mylayer>0) { /*System.out.println("Time at Mark A1 (pixel pushing starting): "+(System.currentTimeMillis()-curTime-remove));*/ remove=System.currentTimeMillis()-curTime; }

		int start=0;
		if (mylayer>1) start=world.scene.stop_point[mylayer-1];

		int end=world.scene.stop_point[mylayer];

		if (mylayer==layers) end=world.scene.list_of_triangles.size(); 

		//System.out.println("layer, start, end"+mylayer+","+start+","+end);

		if (end>world.scene.list_of_triangles.size()) end=world.scene.list_of_triangles.size();
		if (end<start) end=start;

		ArrayList<TriangleRef> new_list_of_triangles=new ArrayList<>(world.scene.list_of_triangles.subList(start,end) );

		if (mylayer>0) { /*System.out.println("Time at Mark A2 (Triangle split complete): "+(System.currentTimeMillis()-curTime-remove));*/ remove=System.currentTimeMillis()-curTime; }
		double aremove=System.currentTimeMillis()-curTime;

		ext=100;
		int c=0;
		int cost=0;
		double a3=0;
		double a4=0;

		for ( TriangleRef mytriref2 : new_list_of_triangles) {
			tri=mytriref2.ref;
			i=tri*3;
			obj_ref=world.scene.triangles_object_ref[tri];
			t=world.scene.triangles_face_ref[tri];


			// Check if this triangle will appear anywhere on screen
			if (((world.scene.triangles_x[i]<width+ext) && (world.scene.triangles_x[i]>0-ext)
					&& (world.scene.triangles_y[i]<height+ext) && (world.scene.triangles_y[i]>0-ext))
					|| ((world.scene.triangles_x[i+1]<width+ext) && (world.scene.triangles_x[i+1]>0-ext)
							&& (world.scene.triangles_y[i+1]<height+ext) && (world.scene.triangles_y[i+1]>0-ext))
					|| ((world.scene.triangles_x[i+2]<width+ext) && (world.scene.triangles_x[i+2]>0-ext)
							&& (world.scene.triangles_y[i+2]<height+ext) && (world.scene.triangles_y[i+2]>0-ext)))
			{

				// Recall the texture reference for this triangle
				tex_ref=world.vobject[obj_ref].face_texture[t];
			
				// Recall details for each vertex of this triangle
				for (int k=0; k<3; k++) {
					int vertex_ref=world.scene.triangles_vertex_ref[i+k];

					// Recall the lighting, depth and texel information for each vertex	
					red=	world.vobject[obj_ref].rel_vertex_red[vertex_ref];
					green=	world.vobject[obj_ref].rel_vertex_green[vertex_ref];
					blue=	world.vobject[obj_ref].rel_vertex_blue[vertex_ref];

					tex_x=world.vobject[obj_ref].texel_x[t][k];
					tex_y=world.vobject[obj_ref].texel_y[t][k];
					depth=world.vobject[obj_ref].rel_point_z[vertex_ref];

					if (red<1) red=1;
					if (green<1) green=1;
					if (blue<1) blue=1;

					if (red>255) red=255;
					if (green>255) green=255;
					if (blue>255) blue=255;

					if (k==0) {
						r1=red;
						g1=green;
						b1=blue;
						tex_x1=tex_x;
						tex_y1=tex_y;
						depth_1=depth;

					}

					if (k==1) {
						r2=red;
						g2=green;
						b2=blue;
						tex_x2=tex_x;
						tex_y2=tex_y;
						depth_2=depth;
					}

					if (k==2) {
						r3=red;
						g3=green;
						b3=blue;
						tex_x3=tex_x;
						tex_y3=tex_y;
						depth_3=depth;
					}

				}

				// Take vertex screen co-ordinates from stored triangle

				x1=world.scene.triangles_x[i];
				x2=world.scene.triangles_x[i+1];
				x3=world.scene.triangles_x[i+2];
				y1=world.scene.triangles_y[i];
				y2=world.scene.triangles_y[i+1];
				y3=world.scene.triangles_y[i+2];


				// Work out the area of the whole triangle
				max=triangle_area(x1,y1,x2,y2,x3,y3);

				// Find co-ordinates a, b, c by putting a, b, c in ascending order of y co-ordinate
				min_y=Math.min(Math.min(y1, y2), y3);
				max_y=Math.max(Math.max(y1, y2), y3);

				// Point A should be the one with the lowest y-co-ordinate
				int a_x=x1;	int a_y=y1;
				if (min_y==y2) {a_x=x2; a_y=y2;}
				if (min_y==y3) {a_x=x3; a_y=y3;}

				// Point C should be the one with the highest y-co-ordinate
				int c_x=x2; int c_y=y2;
				if (max_y==y1) {c_x=x1; c_y=y1;}
				if (max_y==y3) {c_x=x3; c_y=y3;}

				// Point B should be whichever co-ordinate is left over
				int b_x=x3; int b_y=y3;
				if ( ((a_x!=x1) || (a_y!=y1)) && ((c_x!=x1) || (c_y!=y1)) ) {b_x=x1; b_y=y1;}
				if ( ((a_x!=x2) || (a_y!=y2)) && ((c_x!=x2) || (c_y!=y2)) ) {b_x=x2; b_y=y2;}

				// Calculate the inverse gradients
				invgrad_ac=Double.POSITIVE_INFINITY;
				invgrad_ab=Double.POSITIVE_INFINITY;
				invgrad_bc=Double.POSITIVE_INFINITY;

				if (c_y-a_y!=0) {invgrad_ac=(double)(c_x-a_x)/(double)(c_y-a_y);}
				if (b_y-a_y!=0) {invgrad_ab=(double)(b_x-a_x)/(double)(b_y-a_y);}
				if (c_y-b_y!=0) {invgrad_bc=(double)(c_x-b_x)/(double)(c_y-b_y);}

				// Find point d in order to split the triangle in two
				int d_y=b_y;

				if (invgrad_ac!=Double.POSITIVE_INFINITY) {d_x=(int)(a_x+(invgrad_ac*(b_y-a_y)));}

				if ((mylayer>0) && (c==0)) { a3=a3+(System.currentTimeMillis()-curTime-remove); remove=System.currentTimeMillis()-curTime; }

				// Plot triangle ABD
				startxtri=0;
				endxtri=0;


				if (invgrad_ab!=Double.POSITIVE_INFINITY)  {
					for (int y=a_y; y<=d_y; y++) {
						if ((y>starty) && (y<endy)) {
							startxtri=(int)(a_x+((y-a_y)*invgrad_ab));
							endxtri=(int)(a_x+((y-a_y)*invgrad_ac));
							if (startxtri<endxtri) {
								for (int x=startxtri; x<=endxtri; x++) {
									if ((x>startx) && (x<endx)) {
										plotPixel(x,y);
										cost++;
									}
								}
							}

							else {		

								for (int x=endxtri; x<=startxtri; x++) {
									if ((x>startx) && (x<endx)) {
										plotPixel(x,y);
										cost++;
									}
								}	  
							}  
						}
					}
				}

				// Plot triangle BDC

				startxtri=0;
				endxtri=0;
				if (invgrad_bc!=Double.POSITIVE_INFINITY)  {
					for (int y=c_y; y>=b_y; y--) {
						if ((y>starty) && (y<endy)) {
							startxtri=(int)(c_x+((y-c_y)*invgrad_bc));
							endxtri=(int)(c_x+((y-c_y)*invgrad_ac));

							if (startxtri<endxtri) {
								for (int x=startxtri; x<=endxtri; x++) {
									if ((x>startx) && (x<endx)) {
										plotPixel(x,y);
										cost++;
									}
								}
							}
							else {			   
								for (int x=endxtri; x<=startxtri; x++) {
									if ((x>startx) && (x<endx)) {
										plotPixel(x,y);
										cost++;
									}
								}	  
							}  
						}            		  
					}
				}

				if ((mylayer>0) && (c==0)) { a4=a4+(System.currentTimeMillis()-curTime-remove); remove=System.currentTimeMillis()-curTime;  }

			}
		}

		//if (mylayer>0) System.out.println("LAYER: "+mylayer+"....A3,A4,A5, cost "+a3+","+a4+","+(System.currentTimeMillis()-curTime-aremove)+",..."+cost);
		if (a4>75) {
			System.out.println("THAT TOOK AGES!! ");
			System.out.println("-----------------");
			System.out.println("LAYER: "+mylayer+"....A3,A4,A5, cost "+a3+","+a4+","+(System.currentTimeMillis()-curTime-aremove)+",..."+cost);
		}
	}

	private void plotPixel(int x,int y) { 

		w1=triangle_area(x,y,x2,y2,x3,y3)/max;
		w2=triangle_area(x,y,x1,y1,x3,y3)/max;
		w3=triangle_area(x,y,x1,y1,x2,y2)/max;
		totalweight=w1+w2+w3;

		
		// Calculate the lighting for this pixel
		red=(int)(((w1*r1)+(w2*r2)+(w3*r3))/totalweight);
		green=(int)(((w1*g1)+(w2*g2)+(w3*g3))/totalweight);
		blue=((int)(((w1*b1)+(w2*b2)+(w3*b3))/totalweight));

		
		// Calculate the depth for this pixel
		depth=((w1*depth_1)+(w2*depth_2)+(w3*depth_3))/totalweight;

		// Alter the weightings based on the depth
		w1=w1/depth_1;
		w2=w2/depth_2;
		w3=w3/depth_3;
		//totalweight=w1+w2+w3;

		// Calculate the texel co-ordinates for this pixel
		if (tex_ref>0) {
	
			tex_x=(int)((((w1*tex_x1)+(w2*tex_x2)+(w3*tex_x3))/totalweight)*depth);
			tex_y=(int)((((w1*tex_y1)+(w2*tex_y2)+(w3*tex_y3))/totalweight)*depth);
			if (tex_x<0) tex_x=0;
			if (tex_y<0) tex_y=0;

			if (world.vobject[obj_ref].local_textures==false) {
				if (tex_x>world.texture[tex_ref].width-1) tex_x=world.texture[tex_ref].width-1;
				if (tex_y>world.texture[tex_ref].height-1) tex_y=world.texture[tex_ref].height-1;
			}
			else {
				
				if (tex_x>world.vobject[obj_ref].face_texture_width[tex_ref]-1) tex_x=world.vobject[obj_ref].face_texture_width[tex_ref]-1;
				if (tex_y>world.vobject[obj_ref].face_texture_height[tex_ref]-1) tex_y=world.vobject[obj_ref].face_texture_height[tex_ref]-1;
			}
		}

		/*
		index=(tex_y*world.vobject[obj_ref].face_texture_width[tex_ref])+tex_x;
		if (index<0) index=0;
		red=world.vobject[obj_ref].face_texture_red_raster[tex_ref][index];
		green=world.vobject[obj_ref].face_texture_green_raster[tex_ref][index];
		blue=world.vobject[obj_ref].face_texture_blue_raster[tex_ref][index];
*/


		// Apply the lighting to the texture, or if untextured, apply the lighting to the absolute face colour
		if (world.vobject[obj_ref].dynamic_lighting==true) {
			if (tex_ref>0) {
				if (world.vobject[obj_ref].local_textures==false) {
					if ((world.texture[tex_ref].width>0) && (world.texture[tex_ref].height>0)) {
						index=(tex_y*world.texture[tex_ref].width)+tex_x;
						if (index<0) index=0;
						red=(red*world.texture[tex_ref].red_raster[index])/255;
						green=(green*world.texture[tex_ref].green_raster[index])/255;
						blue=(blue*world.texture[tex_ref].blue_raster[index])/255;
					}

					else {
						red=(red*world.vobject[obj_ref].abs_face_red[t])/255;
						green=(green*world.vobject[obj_ref].abs_face_green[t])/255;
						blue=(blue*world.vobject[obj_ref].abs_face_blue[t])/255;
					}
				}
				else {
					if ((world.vobject[obj_ref].face_texture_width[tex_ref]>0) && (world.vobject[obj_ref].face_texture_height[tex_ref]>0)) {
						index=(tex_y*world.vobject[obj_ref].face_texture_width[tex_ref])+tex_x;
						if (index<0) index=0;
						red=(red*world.vobject[obj_ref].face_texture_red_raster[tex_ref][index])/255;
						green=(green*world.vobject[obj_ref].face_texture_green_raster[tex_ref][index])/255;
						blue=(blue*world.vobject[obj_ref].face_texture_blue_raster[tex_ref][index])/255;
					}

					else {

						red=(red*world.vobject[obj_ref].abs_face_red[t])/255;
						green=(green*world.vobject[obj_ref].abs_face_green[t])/255;
						blue=(blue*world.vobject[obj_ref].abs_face_blue[t])/255;
					}

				}
			}

			else {
				red=(red*world.vobject[obj_ref].abs_face_red[t])/255;
				green=(green*world.vobject[obj_ref].abs_face_green[t])/255;
				blue=(blue*world.vobject[obj_ref].abs_face_blue[t])/255;
			}

		}

		// Apply the fog factor
		if (depth>world.fog_distance) {
			factor=1-((world.horizon_distance-depth)/(world.horizon_distance-world.fog_distance));
			red=(int)(red+ (fog_red-(red))*factor);
			green=(int)(green+ (fog_green-(green))*factor);
			blue=(int)(blue+ (fog_blue-(blue))*factor);
		}

		int p=(256*256*red)+(256*green)+blue;;
		if (p==0) p++;
		pixels[((y*(width))+(x))]=p;
	}

	private void drawBackgroundGouraudShaded() {

		if (!world.scene.surface) {
			fillRect( startx, starty, endx,height,0, 0,0 );
			random.setSeed(0);
			for (int i=0;i<1000;i++) {
				int x=random.nextInt(endx-5);
				int y=random.nextInt(height-5);
				int w=random.nextInt(2)+1;
				int h=random.nextInt(2)+1;
				int bright=random.nextInt(200)+55;
				fillRect( x, y, x+w, y+h,bright,bright,bright);
			}
		}
	}

	private double triangle_area (int x1,int y1, int x2, int y2, int x3, int y3) {
		return Math.abs((double)(( (x1*(y2 -y3)) + (x2*(y3 -y1)) + (x3*(y1 - y2))))/2.000);
	}

	private void fillRect(int x1, int y1, int x2, int y2,int red, int green, int blue) {

		{

			for (int row=y1;row<y2;row++){
				if ((row<endy) && (row>starty)) {
					for (int col=x1;col<(x2);col++){
						if ((col<endx) && (col>startx)) {
							pixels[(row*(width)+col)]=(256*256*red)+(256*green)+(blue);
						}
					}
				}
			}
		}

	}
}
