import java.util.List;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.Collections;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public final class SystemWorld extends World {
double maxspeed=0;

	public SystemWorld(int width, int height,  File folder) {
		super(width,height,folder);
		
		sector_size=50200;        // Length of a side of cube, that defines an infinite 3D grid            
		sector_height=2000;
		horizon_distance=999999999999.0;   // Distance to the horizon (beyond which nothing is rendered)
		fog_distance=999999999999.0;       // Distance to the fog horizon (between the fog horizon and the horizon, faces are foggy)

		grid_step=64;        // The square root of the maximum number of sub-squares in a sector
		fog_red=100;			// RGB value of fog
		fog_green=100;		// RGB value of fog
		fog_blue=200;		// RGB value of fog
		
		ambient_red=40;			// RGB value of ambient light
		ambient_green=40;		// RGB value of ambient light
		ambient_blue=40;		    // RGB value of ambient light


		roughness=0.38;
		default_iterations=6;
		sea_level=0;
		default_speed=10000000;
		tick_time_seconds=1000;

		
		// generic constructor statements, defining how the World will appear
		scene.dot_field=     false;
		scene.wireframe=     false;
		scene.solid_poly=    true;
		scene.gouraud_shaded=false;
		scene.hidden_face=   true;
		scene.simple_shaded= true;
		scene.z_sorted=      true;
		scene.surface=	     false;
		scene.test=	    	 true;


				myplanet=new Planet(30, 2, this);				
				myplanet.Iterate(0.01,250,0.98);
				myplanet.scale(6371000);
				myplanet.setMass(5.972*Math.pow(10, 24));
				myplanet.rotate(0,0,0);
				myplanet.abs_x_rot=0;
				myplanet.abs_z_pos=(double)6371000*5;
				myplanet.abs_x_vel=-966*((7.34767309*Math.pow(10, 22))/(5.972*Math.pow(10, 24)));
				vobject[0]=myplanet;
				camera.y_pos=0;
				camera.x_rot=0;
				num_vobjects=1;
				
				Planet mymoon=new Planet(10, 2, this);	
				mymoon.scale(1737000);
				mymoon.setMass(7.34767309*Math.pow(10, 22));
				mymoon.place(0,myplanet.abs_y_pos+406700000,0);
				mymoon.abs_x_vel=966;
				mymoon.abs_z_vel=0;
				vobject[1]=mymoon;
				num_vobjects=2;	
				
				mysun=new Sphere(10, 2, this);
				mysun.makeLightSource(6500,6500,6500);
				mysun.scale(696340000);
				mysun.place(-151300000000.0,0,0);
				vobject[2]=mysun;
				num_vobjects=3;	
				mysun.potentially_visible=true;
				updateLighting();
		
	
			double curTime=System.currentTimeMillis();	
		
			/*
			myskydome=new SkyDome(20,2,this);
			myskydome.scale(200000);
			myskydome.rotate(Math.PI/2,0,0);
			
			myskydome.abs_x_pos=0;
			myskydome.abs_z_pos=0;
			myskydome.abs_y_pos=0;
			
			vobject[2]=myskydome;	   
			num_vobjects=3;
			*/
			
			updateLighting();
			foreThought=new ForeThought(this);
			foreThoughtThread=new Thread(foreThought);
			foreThoughtThread.setPriority(Thread.MIN_PRIORITY);
		 
	
		exists=true;

	}
	

	// Method to do game-logic updates to this world
	
	public double update() {
		  double time=System.currentTimeMillis();
		  
		  boolean update=false;
		  
		  
		  for (int i=0; i<num_vobjects;i++) {
	    	   vobject[i].resetAcc();
	    	   if (vobject[i].gravitational) {
	    		   for (int j=0; j<num_vobjects;j++) {
	        	   if ((vobject[j].gravitational) && (i!=j)) {
	        		   vobject[i].gravitateTowards(vobject[j]);
	        	   }
	           }
	    	   }
	           vobject[i].move(tick_time_seconds);
	       }
		  
		  if (camera.tracking!=-1) {
			  camera.track(vobject[camera.tracking].abs_x_pos,vobject[camera.tracking].abs_y_pos,vobject[camera.tracking].abs_z_pos);
		  }
		
	       
	    	   
		// Keyboard handling
		if (scene.ready) {
			if (command==KeyEvent.VK_0) {
				  if (num_vobjects>=1) camera.tracking=0; 
				}
			if (command==KeyEvent.VK_1) {
			  if (num_vobjects>=2) camera.tracking=1; 
			}
			if (command==KeyEvent.VK_3) {
				  if (num_vobjects>=3) camera.tracking=2; 
				}
			if (command==KeyEvent.VK_4) {
				  if (num_vobjects>=4) camera.tracking=3; 
				}
			
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
				camera.translateWithRespectToView(0,0,default_speed);
			if (command==KeyEvent.VK_S)
				camera.translateWithRespectToView(0,0,-default_speed);
			if (command==KeyEvent.VK_Z)
				camera.translateWithRespectToView(-default_speed,0,0);
			if (command==KeyEvent.VK_X)
				camera.translateWithRespectToView(default_speed,0,0);
			if (command==KeyEvent.VK_T)
				camera.translateWithRespectToView(0,default_speed,0);
			if (command==KeyEvent.VK_G)
				camera.translateWithRespectToView(0,-default_speed,0);

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

			if (command==KeyEvent.VK_F1) 
			{scene.dot_field=     !scene.dot_field;}
			if (command==KeyEvent.VK_F2)
			{scene.wireframe=     !scene.wireframe;}
			if (command==KeyEvent.VK_F3)
			{scene.solid_poly=    !scene.solid_poly;}
			if (command==KeyEvent.VK_F4)
			{scene.hidden_face=   !scene.hidden_face;}
			if (command==KeyEvent.VK_F5)
			{scene.simple_shaded= !scene.simple_shaded;}
			if (command==KeyEvent.VK_F6)
			{scene.z_sorted=      !scene.z_sorted;}
			if (command==KeyEvent.VK_F7)
			{scene.gouraud_shaded=      !scene.gouraud_shaded;}
			if (command==KeyEvent.VK_F8)
			{scene.fullstats=      !scene.fullstats;}

			if (command==KeyEvent.VK_F10 && scene.ready)
			{worldgrid.diamondSquare(1,roughness,1); 
			}

			if (command==KeyEvent.VK_F9) 
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
				vobject[0].rotate(0.0008,0.0005,0.001);
			}
		}


	
		if ((first_time) && (scene.surface)) {foreThoughtThread.start(); first_time=false; }
		
		
		updateLighting();
		return tick_time_seconds;
	}

	
	
	public void keyboardInput (int k) {
		if ((k!=0) && (command==0)) command=k;
	}




}


