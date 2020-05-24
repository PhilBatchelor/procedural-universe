public class Cube extends VObject {
  public Cube(World world) {
    potentially_visible=true;
    num_points=8;
    num_faces=12; 
 
    abs_point_x=new double[] {-1,  1,  1, -1, -1,  1,  1, -1}; 
    abs_point_y=new double[] { 1,  1,  1,  1, -1, -1, -1, -1};
    abs_point_z=new double[] { 1,  1, -1, -1, -1, -1,  1,  1};

    vertex_ref=new int[][] {
                 {4,0,7},
                 {3,4,2},
                 {5,4,6},
                 {2,4,5},
                 {3,0,4},
                 {6,4,7},
                 {7,0,6},
                 {2,5,1},
                 {1,5,6},
                 {1,3,2},
                 {0,3,1},
                 {6,0,1}}; 

    abs_face_red=  new int[] {255, 255,   0, 255, 255,   0,   0, 255, 255, 255, 255,   0};       
    abs_face_green=new int[] {255,   0, 255,   0, 255, 255, 255,   0,   0, 255, 255, 255};
    abs_face_blue= new int[] {  0, 255, 255, 255,   0, 255,   0,   0,   0, 255, 255,   0};
  
    face_texture=new int[] {1,2,1,2,1,1,2,1,1,2,2,2};
    
    face_removal_angle=0;
      
    this.init(world);
  }
}
