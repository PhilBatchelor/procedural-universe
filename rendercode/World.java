import java.util.List;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.Collections;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class World {

	public boolean exists;	
	public VObject[] vobject=new VObject[50]; // An array of all the VObjects currently defined in the World, each with an index number
	
	public LandscapeTile[] bufferedObject=new LandscapeTile[500]; // An array of all the VObjects waiting in a buffer, not to be rendered
	public int[] visibleSectorToBuffer=new int[26];				  // Map each visible sector to a VOBject in the buffer
	
	
	public Texture[] texture=new Texture[500000]; // An array of texture objects, each with an index number 

	public int num_vobjects;		  // The number of VObjects currently defined in the  World
	public int num_textures;		  // The number of Texture objects in the World

	public double sector_size=50200;        // Length of a side of cube, that defines an infinite 3D grid            
	public double sector_height=2000;
	public double horizon_distance=50200;   // Distance to the horizon (beyond which nothing is rendered)
	public double fog_distance=44200;       // Distance to the fog horizon (between the fog horizon and the horizon, faces are foggy)
	public double screen_distance;     		// Distance from the observer to the plane of rendering
	public double clip_z=screen_distance;   // Z-clipping distance
	public double tick_time_seconds=100;	// Time in seconds of one tick of the simulation

	public int grid_step=64;        // The square root of the maximum number of sub-squares in a sector
	public int fog_red=100;			// RGB value of fog
	public int fog_green=100;		// RGB value of fog
	public int fog_blue=200;		// RGB value of fog
	
	public int ambient_red=0;		 // RGB value of ambient light
	public int ambient_green=0;		 // RGB value of ambient light
	public int ambient_blue=0;		 // RGB value of ambient light

	public double roughness=0.38;
	public int default_iterations=6;
	public double sea_level=0;
	public double default_speed=100;
	public int command;
	
	boolean first_time=true;

	public RenderedScene scene;     // A scene with complete geometry, ready for rendering on screen
	public Camera camera;			// The Camera which observes the World
	public AbstractMesh worldgrid;	// The world-grid used to describe the surface of a planet

	private boolean drawit;
	private int c;
	private double tmpz;
	private double nearest;
	private double scaler;
	private int addit;
	private long total_cost;
	public ForeThought foreThought;
	public Thread foreThoughtThread;
	
	
	Planet myplanet;
	Sphere mysun;
	Cube myshape;
	SkyDome myskydome;

	public World(int width, int height,  File folder) {
		
	
		// generic constructor statements, defining how the World will appear
		scene=new RenderedScene(width,height);
	
		// Create the Camera which will observe the World
		screen_distance=width/2;
		camera=new Camera(screen_distance);    
		
		// Load in the textures
		num_textures=0;
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				num_textures++;
				texture[num_textures]=new Texture(file);
				System.out.println("Texture Loaded. This is texture number "+num_textures+" from file: "+file);
			}
		}
	
		num_vobjects=0;
		exists=true;

	}
	

	public int addTexture(Texture tex) {
		// Add a new texture to the world and return the index number
		num_textures++;
		texture[num_textures]=tex;
		return num_textures;
	}
		

	public void updateLighting() {
		// Method to update all potentially visible surfaces in the world based on defined lightsources, for each object that needs it
		int k;
		int i;

		for (k=0; k<num_vobjects;k++) {			// Loop through all the objects to be illuminated 
			
			if ((vobject[k].potentially_visible) && (vobject[k].needs_lighting) ) {
				vobject[k].resetLighting();
				for (i=0; i<num_vobjects;i++) {	// Loop through all the light sources that can illuminate this object
					if (vobject[i].light_source) {
						vobject[k].addLightSource(vobject[i].abs_x_pos, vobject[i].abs_y_pos, vobject[i].abs_z_pos, vobject[i].light_red, vobject[i].light_green, vobject[i].light_blue);
					}
				}
				vobject[k].calculateAverageLighting();
				vobject[k].needs_lighting=false;
			}
		}
	}

	// Method to do game-logic updates to this world
	
	public double update() {
		  double time=System.currentTimeMillis();
		  boolean update=false;
		  
		// Keyboard handling
		if (scene.ready) {
			if (command==KeyEvent.VK_D) 
				camera.y_rot=camera.y_rot+0.02;
			if (command==KeyEvent.VK_A)
				camera.y_rot=camera.y_rot-0.02;
			if (command==KeyEvent.VK_Q)
				camera.z_rot=camera.z_rot-0.02;
			if (command==KeyEvent.VK_E)
				camera.z_rot=camera.z_rot+0.02;
			if (command==KeyEvent.VK_R)
				camera.x_rot=camera.x_rot+0.02;
			if (command==KeyEvent.VK_F)
				camera.x_rot=camera.x_rot-0.02;

			if (command==KeyEvent.VK_W)
				camera.translateWithRespectToView(0,0,250);
			if (command==KeyEvent.VK_S)
				camera.translateWithRespectToView(0,0,-250);
			if (command==KeyEvent.VK_Z)
				camera.translateWithRespectToView(-250,0,0);
			if (command==KeyEvent.VK_X)
				camera.translateWithRespectToView(250,0,0);
			if (command==KeyEvent.VK_T)
				camera.translateWithRespectToView(0,250,0);
			if (command==KeyEvent.VK_G)
				camera.translateWithRespectToView(0,-250,0);

			if (command==KeyEvent.VK_J)
				vobject[0].rotate(0,-0.01,0);
			if (command==KeyEvent.VK_L)
				vobject[0].rotate(0,0.01,0);
			if (command==KeyEvent.VK_I)
				vobject[0].rotate(0.01,0.0,0);
			if (command==KeyEvent.VK_K)
				vobject[0].rotate(-0.01,0,0);
			if (command==KeyEvent.VK_U)
				vobject[0].rotate(0,0.00,-0.01);
			if (command==KeyEvent.VK_O)
				vobject[0].rotate(0.0,0,0.01);

			if (command==KeyEvent.VK_1) 
			{scene.dot_field=     !scene.dot_field;}
			if (command==KeyEvent.VK_2)
			{scene.wireframe=     !scene.wireframe;}
			if (command==KeyEvent.VK_3)
			{scene.solid_poly=    !scene.solid_poly;}
			if (command==KeyEvent.VK_4)
			{scene.hidden_face=   !scene.hidden_face;}
			if (command==KeyEvent.VK_5)
			{scene.simple_shaded= !scene.simple_shaded;}
			if (command==KeyEvent.VK_6)
			{scene.z_sorted=      !scene.z_sorted;}
			if (command==KeyEvent.VK_7)
			{scene.gouraud_shaded=      !scene.gouraud_shaded;}
			if (command==KeyEvent.VK_8)
			{scene.fullstats=      !scene.fullstats;}

			if (command==KeyEvent.VK_0 && scene.ready)
			{worldgrid.diamondSquare(1,roughness,1); 
			}

			if (command==KeyEvent.VK_9) 
			{scene.surface=      !scene.surface;}

			if (command==KeyEvent.VK_P && scene.ready) {
				//Planet myplanet=new Planet(60);
				myplanet.Iterate(0.1,1,1);
				//myplanet.scale(2000);
				vobject[0]=myplanet;
			}

			command=0;
		}

		// generic

		if (!scene.surface) {	
			if (!scene.test) {
				vobject[0].rotate(0.000,0.002,0.000);
				
			}
			else {
				vobject[0].rotate(0.008,0.005,0.01);
			}
		}


	
		if ((first_time) && (scene.surface)) {foreThoughtThread.start(); first_time=false; }
		updateLighting();
		return tick_time_seconds;
	}

	
	public int findFreeBuffer() {
		for (int i=1;i<26;i++) {
			boolean free=true;
			for (int j=1;j<26;j++) {
				//System.out.println("visSector: "+j+" is using buffered object: "+i);
				if (visibleSectorToBuffer[j]==i) free=false;  
			}
			if (free) return i;
		}
		System.out.println("no free buffer!");
		return 0;
	}

	public void keyboardInput (int k) {
		if ((k!=0) && (command==0)) command=k;
	}

	// This method renders the visible scene to a set of 2D co-ordinates, ready to be drawn on the screen 
	public void renderScene() {
		scene.ready=false;
		scene.num_points=0;
		scene.num_triangles=0;
		scene.num_tri_vertices=0;
		scene.list_of_triangles.clear(); // reset the List of Triangles (for z-sorting purposes)
		scene.num_objects_visible=0;
		total_cost=0;
		updateLighting();
		for (int i=0; i<num_vobjects;i++) {
			if (vobject[i].potentially_visible) {
				if (checkVisible(i)) {
					scene.num_objects_visible++;
					calculateRelativePoints(i);
					if (scene.dot_field) {defineDots(i);} 
					if (scene.wireframe) {defineTriangles(i);}
					if (scene.solid_poly) {defineTriangles(i);}
				}
			}
		}
	
		if (scene.z_sorted) {Collections.sort(scene.list_of_triangles);}
		scene.ready=true;
	}


	// This method checks if a given vobject in the scene is visible from the camera
	// It also recalculates the relative positions of each VObject, relative to the camera
	public boolean checkVisible(int i) {
		vobject[i].calculateRelativePosition(camera.x_pos,camera.y_pos,camera.z_pos,camera.x_rot,camera.y_rot,camera.z_rot);
		if (vobject[i].rel_z_pos+vobject[i].radius>0) {vobject[i].visible=true;}
		if (vobject[i].rel_z_pos+vobject[i].radius<0) {vobject[i].visible=false;}
		return vobject[i].visible;
	}

	// Calculate the position of each point in a VObject, relative to the camera
	public void calculateRelativePoints(int i) {
		vobject[i].calculateRelativePoints(camera.x_pos,camera.y_pos,camera.z_pos,camera.x_rot,camera.y_rot,camera.z_rot);
	}

	public void defineDots(int i) {
		for (int c=0;c<vobject[i].num_points;c++) {
			if (vobject[i].rel_point_z[c]>0.0000) {
				double scaler=(camera.screen_distance/vobject[i].rel_point_z[c]);
				scene.points_x[scene.num_points]=(int)((vobject[i].rel_point_x[c]*scaler)+(scene.width/2));
				scene.points_y[scene.num_points]=scene.height-(int)((vobject[i].rel_point_y[c]*scaler)+(scene.height/2));
				scene.points_red[scene.num_points]=vobject[i].abs_point_red[c];
				scene.points_green[scene.num_points]=vobject[i].abs_point_green[c];
				scene.points_blue[scene.num_points]=vobject[i].abs_point_blue[c];       
				scene.num_points++;
			}    
		} 
	}

	public void defineTriangles(int i) {
		
		int addit2=0;	
	
		for (int t=0;t<vobject[i].num_faces;t++) {

			drawit=true;
			if (vobject[i].rel_centroid_z[t]<horizon_distance) {              // remove faces beyond the horizon   
				if (scene.hidden_face) {                                      // remove hidden faces if specified in the scene
		
					if (vobject[i].faceAngle2(t,0,0,0)>vobject[i].face_removal_angle)  {drawit=false;}

				}
			}
			else {drawit=false;}

			if ((drawit) || (i==10)) {


				int sec1=0;
				int sec2=0;
				int sec3=0;
				int sec4=0;
				addit=0;                                     //Don't add a triangle that's fully behind the viewer or totally beyond the horizon
				addit2=0;									//Don't add a triangle totally off the screen

				// Reference the triangle back to the original object, face and vertices, again for Gourard shading purposes
				scene.triangles_object_ref[scene.num_triangles]=i;
				scene.triangles_face_ref[scene.num_triangles]=t;

				c=vobject[i].vertex_ref[t][0];                  //Retrieve the first vertex of the triangle
				scene.triangles_vertex_ref[scene.num_tri_vertices]=c;
				tmpz=vobject[i].rel_point_z[c];              //Retrieve the depth of vertex
				nearest=-vobject[i].rel_point_z[c];
				
				if (tmpz<clip_z) {addit++; tmpz=1;}
				if ((tmpz>horizon_distance) && (i!=10)) addit++;

				scaler=(camera.screen_distance/tmpz);
				scene.triangles_x[scene.num_tri_vertices]=(int)((vobject[i].rel_point_x[c]*scaler)+(scene.width/2));
				scene.triangles_y[scene.num_tri_vertices]=scene.height-(int)((vobject[i].rel_point_y[c]*scaler)+(scene.height/2));
				
				if ((scene.triangles_x[scene.num_tri_vertices]>scene.width) || (scene.triangles_x[scene.num_tri_vertices]<0) || (scene.triangles_y[scene.num_tri_vertices]>scene.height) || (scene.triangles_y[scene.num_tri_vertices]<0)) {
					addit2++;
					if ((scene.triangles_y[scene.num_tri_vertices]<0)  && (scene.triangles_x[scene.num_tri_vertices]>0) && (scene.triangles_x[scene.num_tri_vertices]<scene.width)) sec1=1;
					if ((scene.triangles_y[scene.num_tri_vertices]>scene.height)  && (scene.triangles_x[scene.num_tri_vertices]>0) && (scene.triangles_x[scene.num_tri_vertices]<scene.width)) sec3=1;
					if ((scene.triangles_y[scene.num_tri_vertices]>0)  &&  (scene.triangles_y[scene.num_tri_vertices]<scene.height)  && (scene.triangles_x[scene.num_tri_vertices]<0))  sec4=1;
					if ((scene.triangles_y[scene.num_tri_vertices]>0)  &&  (scene.triangles_y[scene.num_tri_vertices]<scene.height)  && (scene.triangles_x[scene.num_tri_vertices]>scene.width))  sec2=1;
				}
			
				scene.num_tri_vertices++;
				
				c=vobject[i].vertex_ref[t][1];
				scene.triangles_vertex_ref[scene.num_tri_vertices]=c;
				nearest=nearest+(-vobject[i].rel_point_z[c]);
				tmpz=vobject[i].rel_point_z[c];
				
				if (tmpz<clip_z) {addit++; tmpz=1;}
				if ((tmpz>horizon_distance) && (i!=10)) addit++;
				
				scaler=(camera.screen_distance/tmpz);
				scene.triangles_x[scene.num_tri_vertices]=(int)((vobject[i].rel_point_x[c]*scaler)+(scene.width/2));
				scene.triangles_y[scene.num_tri_vertices]=scene.height-(int)((vobject[i].rel_point_y[c]*scaler)+(scene.height/2));
				if ((scene.triangles_x[scene.num_tri_vertices]>scene.width) || (scene.triangles_x[scene.num_tri_vertices]<0) || (scene.triangles_y[scene.num_tri_vertices]>scene.height) || (scene.triangles_y[scene.num_tri_vertices]<0)) {
					addit2++;
					if ((scene.triangles_y[scene.num_tri_vertices]<0)  && (scene.triangles_x[scene.num_tri_vertices]>0) && (scene.triangles_x[scene.num_tri_vertices]<scene.width)) sec1=1;
					if ((scene.triangles_y[scene.num_tri_vertices]>scene.height)  && (scene.triangles_x[scene.num_tri_vertices]>0) && (scene.triangles_x[scene.num_tri_vertices]<scene.width)) sec3=1;
					if ((scene.triangles_y[scene.num_tri_vertices]>0)  &&  (scene.triangles_y[scene.num_tri_vertices]<scene.height)  && (scene.triangles_x[scene.num_tri_vertices]<0))  sec4=1;
					if ((scene.triangles_y[scene.num_tri_vertices]>0)  &&  (scene.triangles_y[scene.num_tri_vertices]<scene.height)  && (scene.triangles_x[scene.num_tri_vertices]>scene.width))  sec2=1;
				}
				
				scene.num_tri_vertices++;

				c=vobject[i].vertex_ref[t][2];
				scene.triangles_vertex_ref[scene.num_tri_vertices]=c;
				nearest=nearest+(-vobject[i].rel_point_z[c]);
				tmpz=vobject[i].rel_point_z[c];
		
				if (tmpz<clip_z) {addit++; tmpz=1;}
				if ((tmpz>horizon_distance) && (i!=10)) addit++;
				
				scaler=(camera.screen_distance/tmpz);

				scene.triangles_x[scene.num_tri_vertices]=(int)((vobject[i].rel_point_x[c]*scaler)+(scene.width/2));
				scene.triangles_y[scene.num_tri_vertices]=scene.height-(int)((vobject[i].rel_point_y[c]*scaler)+(scene.height/2));
				if ((scene.triangles_x[scene.num_tri_vertices]>scene.width) || (scene.triangles_x[scene.num_tri_vertices]<0) || (scene.triangles_y[scene.num_tri_vertices]>scene.height) || (scene.triangles_y[scene.num_tri_vertices]<0)) {
					addit2++;
					if ((scene.triangles_y[scene.num_tri_vertices]<0)  && (scene.triangles_x[scene.num_tri_vertices]>0) && (scene.triangles_x[scene.num_tri_vertices]<scene.width)) sec1=1;
					if ((scene.triangles_y[scene.num_tri_vertices]>scene.height)  && (scene.triangles_x[scene.num_tri_vertices]>0) && (scene.triangles_x[scene.num_tri_vertices]<scene.width)) sec3=1;
					if ((scene.triangles_y[scene.num_tri_vertices]>0)  &&  (scene.triangles_y[scene.num_tri_vertices]<scene.height)  && (scene.triangles_x[scene.num_tri_vertices]<0))  sec4=1;
					if ((scene.triangles_y[scene.num_tri_vertices]>0)  &&  (scene.triangles_y[scene.num_tri_vertices]<scene.height)  && (scene.triangles_x[scene.num_tri_vertices]>scene.width))  sec2=1;
				}
				scene.num_tri_vertices++;
				
				if (((addit<3) && (addit2<3))) {//|| (sec1+sec2+sec3+sec4>1)) {
					scene.list_of_triangles.add(new TriangleRef(scene.num_triangles,vobject[i].rel_centroid_z[t])); //Add Tri object to list of triangles
					scene.triangles_cost[scene.num_triangles]=25+(int)triangle_area(scene.triangles_x[scene.num_tri_vertices-1],scene.triangles_y[scene.num_tri_vertices-1],scene.triangles_x[scene.num_tri_vertices-2],scene.triangles_y[scene.num_tri_vertices-2],scene.triangles_x[scene.num_tri_vertices-3],scene.triangles_y[scene.num_tri_vertices-3]);
					total_cost=total_cost+(long)scene.triangles_cost[scene.num_triangles];
					//System.out.println(total_cost);
					scene.triangles_red[scene.num_triangles]=(vobject[i].rel_face_red[t]+ambient_red)*(vobject[i].abs_face_red[t])/255;
					scene.triangles_green[scene.num_triangles]=(vobject[i].rel_face_green[t]+ambient_green)*(vobject[i].abs_face_green[t])/255;
					scene.triangles_blue[scene.num_triangles]=(vobject[i].rel_face_blue[t]+ambient_blue)*(vobject[i].abs_face_blue[t])/255;
		
					scene.num_triangles++;
				}
				else {scene.num_tri_vertices=scene.num_tri_vertices-3;}
			}
		}

	}
	
	private double triangle_area (int x1,int y1, int x2, int y2, int x3, int y3) {
		return Math.abs((double)(( (x1*(y2 -y3)) + (x2*(y3 -y1)) + (x3*(y1 - y2)))))/5;
	}


}


