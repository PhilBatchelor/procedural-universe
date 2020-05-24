import java.util.List;
import java.util.ArrayList;

public class RenderedScene {

   public boolean ready;	    	// Boolean to indicate if scene is calculated and ready to be drawn
   public boolean push_ready;	   	// Boolean to indicate ready to push the pixels of the triangles
   public boolean user_ready;	    	// Boolean to indicate all rendering is done and the scene can be viewed by the user

   public boolean dot_field=true;   	// Render as chunky pixels
   public boolean wireframe=false;	// Render as wireframes
   public boolean hidden_face=false;	// Remove hidden faces
   public boolean solid_poly=false;	// Render with filled triangles
   public boolean gouraud_shaded=false;	// Render with triangles filled by individuals pixels with gouraud shading
   public boolean simple_shaded=false;	// Render using a simple shading model
   public boolean z_sorted=false;       // Z-sort the polygons in the scene
   public boolean surface=true;		// Render as though on the surface of a planet
   public boolean test=true;		// Debug mode rendering a test shape
   public boolean fullstats=true;	// Display all statistics when rendering this scene


   private int max_tri=100000;		// Maximum triangles in a scene
   private int max_point=max_tri*3;	// Maximum points in a scene
   private int max_face=100000;		// Maximum faces per vobject

   public int height;		       // Height in pixels of rendered scene
   public int width;          	       // Width in pixels of rendered scne
   public int num_points=0;  		// Number of chunky points to be plotted
   public int num_triangles=0;  	// Number of triangles to be drawn

   public int num_tri_vertices=0; 	// Total number of triangle vertices to be plotted
   public int layers=1;				// Number of layers the scene is divided into

   public int[] points_x=new int[max_point];  	// Centre location for plotted points
   public int[] points_y=new int[max_point]; 	// Centre location for ploted points
   
   public int[] points_red=new int[max_point];  // RGB values for drawing triangles
   public int[] points_green=new int[max_point];
   public int[] points_blue=new int [max_point];
  
   public int[] triangles_x=new int [max_point];  // Vertex positions for triangles, in sets of three
   public int[] triangles_y=new int [max_point];   
   
   public int[] triangles_cost=new int[max_tri];
   public int[] stop_point=new int[100]; // Where to break up the list of triangles

   public int[] triangles_red=new int[max_tri]; // RGB values for drawing triangles
   public int[] triangles_green=new int[max_tri];
   public int[] triangles_blue=new int[max_tri]; 
   
   public int[][] triangles_vertex_red=new int[max_tri][3]; // RGB values at each triangle vertex, for Gourard shading
   public int[][] triangles_vertex_green=new int[max_tri][3];
   public int[][] triangles_vertex_blue=new int[max_tri][3];

   // These two allow a reference back to the original vertices of the original object, useful for Gourard shading
   public int[] triangles_vertex_ref=new int[max_point]; //Original vertex ref, in sets of three
   public int[] triangles_object_ref=new int[max_tri];   // Original object ref
   public int[] triangles_face_ref=new int[max_face];   // Original object ref

   public double[] triangles_depth=new double[max_tri]; // depths of each triangle, for z-sorting

   public ArrayList<TriangleRef> list_of_triangles=new ArrayList<TriangleRef>();
   public int num_objects_visible=0;

   public RenderedScene(int w,int h) {
      ready=false;
      this.width=w;
      this.height=h;
   } 
}


