
public class BoundingBox {

   public int associated_object; // linked object reference in the world

   public int sector_x;           // sector reference
   public int sector_y;           // sector reference
   public int sector_z;           // sector reference

   public double abs_x_pos;       // position of the centre of the box
   public double abs_y_pos;      
   public double abs_z_pos; 

   public double x_radius;        // radius of the box
   public double y_radius;
   public double z_radius;

   public boolean object_defined; // is the associated VObject defined?     
   

   public BoundingBox(double rx, double ry, double rz){
      x_radius=rx;
      y_radius=ry;
      z_radius=rz;

      object_defined=false;
    
   } 


}


