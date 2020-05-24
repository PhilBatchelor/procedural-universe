import java.util.Random;

public class LandscapeTile extends VObject {
  public int size;
  public double sea_level;

  private int r;
  private int g;
  private int b;
  private int offgrey;
  private int jj;
  private int ii;
  private int j;
  private int k;
  private int l;
  private int i;
  private int f;
  private int res=30;
  private int w;
  private int h;
  private double width;
  private double height;
  private double wf;
  private double hf;
  private double he;
  private double xc;
  private double zc;
  private World world;

  Random rand = new Random();

  public LandscapeTile(AbstractMesh mesh,int x, int z,double sea,World world, Boolean refresh) {
    double curTime=System.currentTimeMillis();		
	this.world=world;
    sea_level=sea;
    potentially_visible=true;
    this.local_textures=true;
    
    face_removal_angle=0;
    
	shininess=0.5; // Lower gives a shinier peak spot
	dullness=(1/shininess)-0.4;  // Lower is duller
	darkness=(double)(-5)/(Math.log(Math.cos(Math.pow(Math.PI/2,shininess))));
	
    int r=mesh.getSector(x,z);  // Find the relevant sector grid
    
    if (r!=0) {size=mesh.grid_step/mesh.getGridStep(r);}
        else  {size=1;
        System.out.println("Generating a tile for sector: "+r);
        System.out.println("x,z:="+x+","+z);
        System.out.println("Definition of this sector is "+mesh.checkSector(r));
    	}
    
      num_points=(size+1)*(size+1);
      num_faces=(size*size)*2;

      abs_point_x=new double[num_points];
      abs_point_y=new double[num_points];
      abs_point_z=new double[num_points];

      vertex_ref=new int[num_faces][2];
      face_texture=new int[num_faces];

      abs_face_red=new int[num_faces];
      abs_face_green=new int[num_faces];
      abs_face_blue=new int[num_faces];
      
 

      num_points=0;
      
      if (r!=0) {
        for (int j=0;j<(size+1);j++) {
          for (int i=0;i<(size+1);i++) {
            abs_point_x[num_points]=mesh.getX(r,i,j);   
            abs_point_y[num_points]=mesh.getY(r,i,j);   
            abs_point_z[num_points]=mesh.getZ(r,i,j);   
            num_points++;
          }
        }      
      }

      else {
        for (int j=0;j<(size+1);j++) {
          for (int i=0;i<(size+1);i++) {
            abs_point_x[num_points]=mesh.getX(r,x+i,z+j);   
            abs_point_y[num_points]=mesh.getY(r,x+i,z+j);   
            // if (abs_point_y[num_points]<(-1000.00)) {abs_point_y[num_points]=-1000;}   
            abs_point_z[num_points]=mesh.getZ(r,x+i,z+j);   
            num_points++;
          }
        }
      }


      num_faces=0;   

      for (jj=0;jj<(size);jj++) {
        for (ii=0;ii<(size);ii++) {
      
          j=(ii*(size+1))+jj;
          k=(ii*(size+1))+size+1+jj;
          l=j+1;
          vertex_ref[num_faces] = new int[] {j,l,k};
          width=abs_point_x[l]-abs_point_x[j];
          height=abs_point_z[j]-abs_point_z[k];
          wf=width/res;
          hf=height/res;
          num_faces++;
          j=(ii*(size+1))+size+1+jj;
          k=(ii*(size+1))+size+1+jj+1;
          l=(ii*(size+1))+jj+1;
          vertex_ref[num_faces] = new int[] {j,l,k};
          num_faces++;
       }
      }
      
      
    
  	face_texture=new int[num_faces];
  	face_texture_width=new int[num_faces];
  	face_texture_height=new int[num_faces];
  	texel_x=new int[num_faces][res];
  	texel_y=new int[num_faces][res];
  	
  	face_texture_red_raster=new int[num_faces][res*res];
  	face_texture_green_raster=new int[num_faces][res*res];
  	face_texture_blue_raster=new int[num_faces][res*res];
  	
  	System.out.println("Tile Faces Memory Allocation Complete in: "+(System.currentTimeMillis()-curTime));
	
	this.init(world);

	rand.setSeed(x*z); //Make sure the random number generator is based on the unique absoloute co-ordinates of this tile
    colourMap();
    if (refresh) refreshLandscapeTile(mesh,x,z,sea);
    
  }
  
  

  public void refreshLandscapeTile(AbstractMesh mesh,int x, int z,double sea) {

    sea_level=sea;
    potentially_visible=true;
    this.local_textures=true;
    
    face_removal_angle=0;
    
	shininess=0.5; // Lower gives a shinier peak spot
	dullness=(1/shininess)-0.4;  // Lower is duller
	darkness=(double)(-5)/(Math.log(Math.cos(Math.pow(Math.PI/2,shininess))));

    int r=mesh.getSector(x,z);  // Find the relevant sector grid
    
    if (r!=0) {size=mesh.grid_step/mesh.getGridStep(r);}
        else  {size=1;
        System.out.println("Generating a tile for sector: "+r);
        System.out.println("x,z:="+x+","+z);
        System.out.println("Definition of this sector is "+mesh.checkSector(r));
    	}
   
      num_points=0;
      if (r!=0) {
        for (int j=0;j<(size+1);j++) {
          for (int i=0;i<(size+1);i++) {
            abs_point_x[num_points]=mesh.getX(r,i,j);   
            abs_point_y[num_points]=mesh.getY(r,i,j);   
            abs_point_z[num_points]=mesh.getZ(r,i,j);   
            num_points++;
          }
        }      
      }

      else {
        for (int j=0;j<(size+1);j++) {
          for (int i=0;i<(size+1);i++) {
            abs_point_x[num_points]=mesh.getX(r,x+i,z+j);   
            abs_point_y[num_points]=mesh.getY(r,x+i,z+j);   
            // if (abs_point_y[num_points]<(-1000.00)) {abs_point_y[num_points]=-1000;}   
            abs_point_z[num_points]=mesh.getZ(r,x+i,z+j);   
            num_points++;
          }
        }
      }

      num_faces=0;
  
      for (jj=0;jj<(size);jj++) {
        for (ii=0;ii<(size);ii++) {
      
          j=(ii*(size+1))+jj;
          k=(ii*(size+1))+size+1+jj;
          l=j+1;

          width=abs_point_x[l]-abs_point_x[j];
          height=abs_point_z[j]-abs_point_z[k];
       
          wf=width/res;
          hf=height/res;
          face_texture[num_faces]=num_faces;
          face_texture_width[num_faces]=res;
          face_texture_height[num_faces]=res;

          for (w=0; w<res; w++) {
           for (h=0; h<res; h++) {
             xc=abs_point_x[k]+(w*wf);
             zc=abs_point_z[k]+(h*hf);
             he=mesh.getYFromPlanar(xc,zc);
             setTexel(num_faces,res-w-1,res-h-1,getColour(he,0),getColour(he,1),getColour(he,2));
           }
	}		
       
          num_faces++;       
          face_texture[num_faces]=face_texture[num_faces-1];
          face_texture_width[num_faces]=res;
          face_texture_height[num_faces]=res;
          num_faces++;
       }
      }

    normaliseVertices();
  	calculateAbsoluteCentroids();      
  	calculateAbsoluteFaceNormals();
  	if (texture_wrap==false) calculateTexels(world);
	rand.setSeed(x*z); //Make sure the random number generator is based on the unique absolute co-ordinates of this tile
  }

  private void colourMap() {

    for (f=0;f<num_faces;f++) {
          he=abs_centroid_y[f]+abs_y_pos; 
          abs_face_red[f]=getColour(he,0);
          abs_face_green[f]=getColour(he,1);
          abs_face_blue[f]=getColour(he,2);
    }
  }

  private int getColour(double h, int c) { 

	  if (c==0) {
		  // Sea Colour	
		  r=3;
		  g=65;//+(rand.nextInt(40)-20);
		  b=143;//+(rand.nextInt(40)-20);

		  // Beach Colour
		  if (h>(sea_level+1000)) {
			  r=180;//+(rand.nextInt(15)-7);;
			  g=166;//+(rand.nextInt(15)-7);;
			  b=83;//+(rand.nextInt(10)-5);;
		  }

		  // Grass colour
		  if (h>1250.00) {
			  r=5;//+rand.nextInt(3);
			  g=100;//+rand.nextInt(15)-7;
			  b=5;//+rand.nextInt(3);
		  }

		  // Rock colour
		  if (h>5000) {
			  offgrey=0;//(rand.nextInt(10)-5);
			  r=125;//+offgrey;
			  g=125;//+offgrey;
			  b=125;//+offgrey;
		  }

		  // Snow colour
		  if (h>8000) {
			  offgrey=0;//rand.nextInt(10);
			  r=255-offgrey;
			  g=255-offgrey;
			  b=255-offgrey;
		  }
	  }

	  if (c==0) return r;
	  if (c==1) return g;
	  if (c==2) return b;
	  return 0;
  }

}
