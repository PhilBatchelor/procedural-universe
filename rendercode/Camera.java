public final class Camera {

   public double x_pos=0;
   public double y_pos=0;
   public double z_pos=0;

   public double x_rot=0;
   public double y_rot=0;
   public double z_rot=0;

   public double x_vel=0;
   public double y_vel=0;
   public double z_vel=0;

   public int x_sector=0;
   public int y_sector=0;
   public int z_sector=0;
  
   public int old_x_sector=0;
   public int old_y_sector=0;
   public int old_z_sector=0;
   
   public int tracking=-1;
   public int moving_with=-1;
 
   public double screen_distance=1000;
  
   public Camera(double dist) {
    screen_distance=dist;
   }

   public boolean updateSector(double s) {
     boolean changed=false;

     int x=(int)Math.round(x_pos/s);
     int y=(int)Math.round(y_pos/s);
     int z=(int)Math.round(z_pos/s);
       old_x_sector=x_sector;
       old_y_sector=y_sector;
       old_z_sector=z_sector;

     if (x!=x_sector) {
       x_sector=x; 
       changed=true; }
     if (y!=y_sector) {
       y_sector=y;
       changed=true; }
     if (z!=z_sector) {
       z_sector=z;
       changed=true; }
     return changed;
   }
   
   public void track(double x, double y, double z) {
	   this.x_rot=Math.atan(((x-this.x_pos)/(z-this.z_pos)));
	   this.y_rot=Math.atan(((y-this.y_pos)/(z-this.z_pos)));
	  

   }

   public void translateWithRespectToView(double i, double j, double k) {

        double ti=i;
        double tj=j;
        double tk=k;
        
        j=((tj*Math.cos(-x_rot))+(tk*Math.sin(-x_rot)));
        k=((-tj*Math.sin(-x_rot))+(tk*Math.cos(-x_rot)));     
                    
        ti=i;    
        tj=j;
        tk=k;

        i=((ti*Math.cos(-y_rot))-(tk*Math.sin(-y_rot)));
        k=((ti*Math.sin(-y_rot))+(tk*Math.cos(-y_rot)));
                                         
        x_pos=x_pos+i;
        y_pos=y_pos+j;
        z_pos=z_pos+k;   
   }
}


