import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class RenderEngine  {

	private Random random;
	boolean running=true;

	final int width=1280;
	final int height=800;

	final int fog_red=100;				 // RGB value of fog
	final int fog_green=100;		     // RGB value of fog
	final int fog_blue=200;				 // RGB value of fog

	final int sky_red=100;				 
	final int sky_green=150;				
	final int sky_blue=200;	
	
	double simulation_time_seconds=0;

	int tri;
	int i;
	int ext;
	int obj_ref;
	int face_ref;
	int x1;
	int x2;
	int x3;
	int y1;
	int y2;
	int y3;	
	double max;
	double factor;
	int min_y;
	int max_y;
	int vertex_ref;
	int num_shared_faces;
	

	private final GraphicsEnvironment ge;
	private final GraphicsDevice gd ;
	private final GraphicsConfiguration gc;
	private BufferedImage bi;
	private ColorModel colorModel;
	private WritableRaster raster;
	private DataBuffer databuffer;
	private BufferStrategy buffer;
	private Graphics graphics;
	private Graphics2D g2d;

	
	private JFrame app;

	// Variables for counting frames per seconds
	int fps = 0;
	int frames = 0;
	double totalTime = 0;
	double curTime = System.currentTimeMillis();
	double lastTime = curTime;
    public SystemWorld world;

	public RenderEngine()  {
		
		// Load textures
		File folder = new File("./img/");
		// Create the world!

		world=new SystemWorld(width,height,folder);
		
		// Create a random number generator
		random = new Random();

		// Create game window...
		app = new JFrame();
		app.setIgnoreRepaint( true );
		app.setUndecorated( true );
		app.setResizable(true);
		//app.setExtendedState(JFrame.MAXIMIZED_BOTH);
		app.setAlwaysOnTop(true);
		// Add ESC listener to quit...
		app.addKeyListener( new KeyAdapter() {
			public void keyPressed( KeyEvent e ) {
				if( e.getKeyCode() == KeyEvent.VK_ESCAPE )
					running = false;
				if( e.getKeyCode() != 0 )
					world.keyboardInput(e.getKeyCode());
			}
		});
		app.pack();

		//app.setVisible(true);

		// Get graphics configuration...
		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gd = ge.getDefaultScreenDevice();
		gc = gd.getDefaultConfiguration();

		// Change to full screen
		gd.setFullScreenWindow( app );
		if( gd.isDisplayChangeSupported() ) {
			gd.setDisplayMode( 
					new DisplayMode( width, height, DisplayMode.BIT_DEPTH_MULTI,DisplayMode.REFRESH_RATE_UNKNOWN )
					);
		}

		// Create BackBuffer...
		app.createBufferStrategy( 2 );
		buffer = app.getBufferStrategy();

		// Create off-screen drawing surface
		bi = gc.createCompatibleImage( width, height,BufferedImage.TYPE_INT_RGB);
		colorModel = bi.getColorModel();
		raster = (WritableRaster) bi.getData();

		databuffer = raster.getDataBuffer();

		bi=new BufferedImage(colorModel, raster, false, null);
		int[] p=new int[width*height];
	

		// Objects needed for rendering...
		graphics = null;
		g2d = null;
	}

	public void run() {

		running = true;
	
		//PixelPusher pusher=new PixelPusher(world,height,width,p);
		
	
		while( running ) {
			try {
				// count Frames per second...
				lastTime = curTime;
				curTime = System.currentTimeMillis();
				totalTime += curTime - lastTime;
				if( totalTime > 1000 ) {
					totalTime -= 1000;
					fps = frames;
					frames = 0;
				}
				++frames;
				

				//System.out.println("Time at Mark A: "+(System.currentTimeMillis()-curTime));
				double remove=System.currentTimeMillis()-curTime;
				
				// Update the game world using the logic in the World class
				//System.out.println("go to update");
				simulation_time_seconds=simulation_time_seconds+world.update();
				//System.out.println("update complete");
				
				//System.out.println("Time at Mark B (world updates complete): "+(System.currentTimeMillis()-curTime-remove));
				remove=System.currentTimeMillis()-curTime;
				
	
				// Render the whole visible scene to a set of arrays ready for drawing
				world.renderScene();  
				//System.out.println("Time at Mark C (geometry complete): "+(System.currentTimeMillis()-curTime-remove));
				remove=System.currentTimeMillis()-curTime;

				// Plot the arrays onto the screen, using one of the pre-set methods
				g2d = bi.createGraphics();

				if (world.scene.gouraud_shaded) {
				}


				//System.out.println("Time at Mark D (pixel pushing complete): "+(System.currentTimeMillis()-curTime-remove));
				remove=System.currentTimeMillis()-curTime;
				/*
				if (world.scene.gouraud_shaded) {
					
					int add;
					int pp1=0;
					int x;
					for (int row=0;row<height;row++){
						for (int col=0;col<(width);col++){
							add= row*(width)+col;
							pp1=p[add];
							x=pp1;
							if (x!=0) databuffer.setElem(add,x);	
						}
					}
				}
				*/

				//System.out.println("Time at Mark E (plotting complete): "+(System.currentTimeMillis()-curTime-remove));
				remove=System.currentTimeMillis()-curTime;
				
				drawHorizon();
				if (world.scene.dot_field) drawSceneDotField();
				if (world.scene.simple_shaded) drawSceneSimpleShaded();
				if (world.scene.wireframe) drawSceneWireframe();

				
				drawStatistics(); 
				
				//System.out.println("Time at Mark F (printing complete): "+(System.currentTimeMillis()-curTime-remove));
				remove=System.currentTimeMillis()-curTime;

				// Flip the double buffer so the user can see the scene
				graphics = buffer.getDrawGraphics();
				try {
					graphics.drawImage( bi, 0, 0, null );}
				finally {};
				if( !buffer.contentsLost() )
					buffer.show();
				
				//System.out.println("Time at Mark G (flipping complete): "+(System.currentTimeMillis()-curTime-remove));
				remove=System.currentTimeMillis()-curTime;
				
			} 

			finally {
				// release resources
				if( graphics != null ) 
					graphics.dispose();
				if( g2d != null ) 
					g2d.dispose();
			}
		}

		gd.setFullScreenWindow( null );
		System.exit(0);
	}


	public void drawSceneSimpleShaded() {
		int tri;
		int i;
		int ext;
		//g2d.setColor( new Color(sky_red, sky_green,sky_blue));

		for (TriangleRef mytriref : world.scene.list_of_triangles) {
			tri=mytriref.ref;
			i=tri*3;
			// Draw a triangle if any vertex is within the screen bounds
			ext=50;
			if (((world.scene.triangles_x[i]<world.scene.width+ext) && (world.scene.triangles_x[i]>0-ext)
					&& (world.scene.triangles_y[i]<world.scene.height+ext) && (world.scene.triangles_y[i]>0-ext))
					|| ((world.scene.triangles_x[i+1]<world.scene.width+ext) && (world.scene.triangles_x[i+1]>0-ext)
							&& (world.scene.triangles_y[i+1]<world.scene.height+ext) && (world.scene.triangles_y[i+1]>0-ext))
					|| ((world.scene.triangles_x[i+2]<world.scene.width+ext) && (world.scene.triangles_x[i+2]>0-ext)
							&& (world.scene.triangles_y[i+2]<world.scene.height+ext) && (world.scene.triangles_y[i+2]>0-ext)))
			{
				// Draw the triangle using the built-in fill polygon feature if it is just a flat one
				if ((world.scene.simple_shaded) && (world.scene.gouraud_shaded==false)) {
					g2d.setColor( new Color(Math.min(255,world.scene.triangles_red[tri]),Math.min(255,world.scene.triangles_green[tri]),Math.min(255,world.scene.triangles_blue[tri])) );

					g2d.fillPolygon(new int[] {world.scene.triangles_x[i],world.scene.triangles_x[i+1],world.scene.triangles_x[i+2]},
							new int[] {world.scene.triangles_y[i],world.scene.triangles_y[i+1],world.scene.triangles_y[i+2]},
							3);
				}
			}
		}
	}

	public void drawSceneWireframe() {
		// Draw wireframe triangles, if defined in the scene
		for (int i=0; i<world.scene.num_tri_vertices;i=i+3) {
			int r = 255;
			int g = 0;
			int b = 0;
			g2d.setColor( new Color(r,g,b) );
			g2d.drawPolyline(new int[] {world.scene.triangles_x[i],world.scene.triangles_x[i+1],world.scene.triangles_x[i+2]},
					new int[] {world.scene.triangles_y[i],world.scene.triangles_y[i+1],world.scene.triangles_y[i+2]},
					3);
		}
	}

	public void drawSceneDotField() {
		// Plot a dotfield, if defined in the scene
		if (world.scene.dot_field) {
			for( int k = 0; k < world.scene.num_points; k++ ) {
				int r = world.scene.points_red[k];
				int g = world.scene.points_green[k];
				int b = world.scene.points_blue[k];
				g2d.setColor( new Color(r,g,b) );
				int x = world.scene.points_x[k];
				int y = world.scene.points_y[k];
				int w = 4;
				int h = 4;
				g2d.fillRect( x, y, w, h );
			}
		}
	}
	
	public void drawHorizon() {
		if (world.scene.surface) 
		 fillRect(0,0,width,height,fog_red, fog_green, fog_blue);
		else
		 fillRect(0,0,width, height, 0, 0, 0);
	}
	
	public void plotLine (int x,int y,int x2, int y2,int r, int g, int b) {
	
		int w = x2 - x ;
		int h = y2 - y ;
		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
		if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
		if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
		if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
		int longest = Math.abs(w) ;
		int shortest = Math.abs(h) ;
		if (!(longest>shortest)) {
			longest = Math.abs(h) ;
			shortest = Math.abs(w) ;
			if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
			dx2 = 0 ;            
		}
		int numerator = longest >> 1 ;
		for (int i=0;i<=longest;i++) {
			if ((x>=0) && (x<width) && (y>=0) && (y<height)) {
				databuffer.setElem((y*(width)+x),(256*256*r)+(256*g)+(b));
			}
			numerator += shortest ;
			if (!(numerator<longest)) {
				numerator -= longest ;
				x += dx1 ;
				y += dy1 ;
			} else {
				x += dx2 ;
				y += dy2 ;
			}
		}
	}

	

	public void fillRaster(int x1, int y1, int x2, int y2,int red, int green, int blue) {
		for (int i=( (y1*(width))+(x1));i<( (y2-1)*(width))+(x2);i++){
			databuffer.setElem(i,(256*256*red)+(256*green)+(blue));
		}
	}

	public void fillRect(int x1, int y1, int x2, int y2,int red, int green, int blue) {

		for (int row=y1;row<y2;row=row+1){
			for (int col=x1;col<(x2);col++){
				databuffer.setElem((row*(width)+col),(256*256*red)+(256*green)+(blue));	
			}
		}
	}


	public void drawStatistics() {

		// display frames per second and other statistics		


		g2d.setFont( new Font( "Courier New", Font.PLAIN, 12 ) );
		if (world.scene.surface) g2d.setColor( Color.BLACK );
		if (!world.scene.surface) g2d.setColor( Color.WHITE );
		g2d.drawString( String.format( "FPS: %s", fps ), 20, 20 );

		if (world.scene.fullstats) {
			g2d.drawString( String.format( "Camera x_rot: %s", world.camera.x_rot ), 20, 34 );
			g2d.drawString( String.format( "Camera y_rot: %s", world.camera.y_rot  ), 20, 48 );
			g2d.drawString( String.format( "Camera z_rot: %s", world.camera.z_rot ), 20, 62 );
			g2d.drawString( String.format( "Camera z_pos: %s", world.camera.x_pos ), 20, 76 );
			g2d.drawString( String.format( "Camera y_pos: %s", world.camera.y_pos ), 20, 90 );
			g2d.drawString( String.format( "Camera z_pos: %s", world.camera.z_pos ), 20, 104 );
			g2d.drawString( String.format( "Camera x sector: %s", world.camera.x_sector ), 20, 118 );
			g2d.drawString( String.format( "Camera y sector: %s", world.camera.y_sector ), 20, 132 );
			g2d.drawString( String.format( "Camera z sector: %s", world.camera.z_sector ), 20, 146 );
			g2d.drawString( String.format( "Simulation Time in Days: %s", simulation_time_seconds/(3600*24) ), 20, 160 );

			g2d.drawString( String.format( "Number objects: %s", world.scene.num_objects_visible ), 400, 34 );
			g2d.drawString( String.format( "Number triangles: %s", world.scene.list_of_triangles.size() ), 400, 48 );
			g2d.drawString( String.format( "Object x_rot: %s", world.vobject[0].abs_x_rot ), 400, 76 );
			g2d.drawString( String.format( "Object y_rot: %s", world.vobject[0].abs_y_rot ), 400, 90 );
			g2d.drawString( String.format( "Object z_yot: %s", world.vobject[0].abs_z_rot ), 400, 104 );
			
			g2d.drawString( String.format( "Object x_pos: %s", world.vobject[1].abs_x_pos ), 400, 118 );
			g2d.drawString( String.format( "Object y_pos: %s", world.vobject[1].abs_y_pos ), 400, 132 );
			g2d.drawString( String.format( "Object z_pos: %s", world.vobject[1].abs_z_pos ), 400, 146 );


			//g2d.drawString( String.format( "Vertex x: %s", world.vobject[0].rel_point_x[0]), 400, 76 );
			//g2d.drawString( String.format( "Vertex y: %s", world.vobject[0].rel_point_y[0] ), 400, 90 );
			//g2d.drawString( String.format( "Vertex z: %s", world.vobject[0].rel_point_z[0] ), 400, 104 );
			//g2d.drawString( String.format( "Centroid x: %s", world.vobject[0].rel_centroid_x[0]), 400, 118 );
			//g2d.drawString( String.format( "Centroid y: %s", world.vobject[0].rel_centroid_y[0] ), 400,132 );
			//g2d.drawString( String.format( "Centroid z: %s", world.vobject[0].rel_centroid_z[0] ), 400,146 );
			//g2d.drawString( String.format( "Is object visible: %s", world.vobject[0].rel_centroid_z[0] ), 400,160 );
		     g2d.drawString( String.format( "Face Factor: %s", world.vobject[0].face_factor), 400,174 );


			//g2d.drawString( String.format( "Plot x: %s", world.scene.triangles_x[0] ), 800, 34 );
			//g2d.drawString( String.format( "Plot y: %s", world.scene.triangles_y[0] ), 800, 48 );

			// Blit image and flip...
		}
	}

}
