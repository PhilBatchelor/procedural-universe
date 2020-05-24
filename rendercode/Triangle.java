public class Triangle extends VObject {
  public Triangle(World world) {
    potentially_visible=true;
    num_points=4;
    num_faces=2; 
 
    abs_point_x=new double[] { -2,   2, -2, 2}; 
    abs_point_y=new double[] { 2,  -2, -2, 2};
    abs_point_z=new double[] { 0,   0,  0, 0};
    
    vertex_ref=new int[][] {{2,1,0},{3,0,1}};
    
    abs_face_red=  new int[] {255,0};       
    abs_face_green=new int[] {0,255};
    abs_face_blue= new int[] {  0,0};

    face_texture=new int[] {1,1};
             
    this.init(world);
  }
}
