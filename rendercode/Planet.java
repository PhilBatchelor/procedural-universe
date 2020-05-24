import java.util.Random;


public class Planet extends Sphere {
	private Random random;                  // A random number generator
	public int iterations=1;
	public Planet(int s, int t, World world) {
		super(s, t, world);
		this.gravitational=true;
		random = new Random();
	}

	public void Iterate(double roughness, int steps, double taper) {

		random.setSeed(9);
	
		for (int i=0; i<steps;i++) {    	
			// Calculate a random plane through 0,0,0

			double a=random.nextDouble()-0.5;
			double b=random.nextDouble()-0.5;
			double c=random.nextDouble()-0.5;

			// Deform the points if they fall on one side of the plane
			int r=(int)(random.nextDouble()*255);
			int g=(int)(random.nextDouble()*255);
			int bl=(int)(random.nextDouble()*255);

			double deform=(random.nextDouble()-0.5)*roughness;
			int sign_x=1;
			int sign_y=1;
			int sign_z=1;

			for (int d=0; d<this.num_points;d++) {

				if ( ((a*this.abs_point_x[d]) + (b*this.abs_point_y[d])+ (c*this.abs_point_z[d])) >0) {

					//abs_point_r[d]=abs_point_r[d]+(abs_point_r[d]*deform);

					this.abs_point_red[d]=r;
					this.abs_point_green[d]=g;
					this.abs_point_blue[d]=bl;
				}
			}
	
			roughness=roughness*taper;
		}
		iterations=iterations+steps;

	    this.convertPolarToCart();
			// Recolour the faces according to height
			double total_average_height=0;
			double max_height=-100000;
			double min_height=100000;
			
			for (int f=0;f<num_faces;f++) {
				int ref_1=vertex_ref[f][0];
				int ref_2=vertex_ref[f][1];
				int ref_3=vertex_ref[f][2];

				
				double average_height=(VObject.cartToPolar_r(abs_point_x[ref_1],abs_point_y[ref_1],abs_point_z[ref_1])
				+ VObject.cartToPolar_r    (abs_point_x[ref_2],abs_point_y[ref_2],abs_point_z[ref_2])
					+VObject.cartToPolar_r    (abs_point_x[ref_3],abs_point_y[ref_3],abs_point_z[ref_3]))/3.000;
			
				if (average_height>max_height) {max_height=average_height;}
				if (average_height<min_height) {min_height=average_height;}
				
				
				total_average_height=total_average_height+average_height;

			}

			total_average_height=total_average_height/num_faces;
			double total_range=max_height-min_height;

			double sea_level=total_average_height+(total_range/20.0);
			double green_level=total_average_height+(total_range/16.0);
			double rock_level=total_average_height+(total_range/3);
			double snow_level=total_average_height+(total_range/2.2);
			
			// Flatten the sea
			for (int d=0; d<this.num_points;d++) {
				int sign_x=1;
				int sign_y=1;
				int sign_z=1;
				
				if (abs_point_x[d]<0) {sign_x=-1;} else sign_x=1;
				if (abs_point_y[d]<0) {sign_y=-1;} else sign_y=1;
				if (abs_point_z[d]<0) {sign_z=-1;} else sign_z=1;

				double p_theta=VObject.cartToPolar_theta(abs_point_x[d],abs_point_y[d],abs_point_z[d]);  
				double p_phi=  VObject.cartToPolar_phi  (abs_point_x[d],abs_point_y[d],abs_point_z[d]);     
				double p_r=    VObject.cartToPolar_r    (abs_point_x[d],abs_point_y[d],abs_point_z[d]); 

				if (p_r<sea_level) {p_r=sea_level;}

				abs_point_x[d]=VObject.polarToCartX(p_theta,p_phi,p_r);
				abs_point_y[d]=VObject.polarToCartY(p_theta,p_phi,p_r);
				abs_point_z[d]=VObject.polarToCartZ(p_theta,p_phi,p_r);

				// We need to re-introduce the sign of the x,y,z values as this is getting lost in the transformations    	    	
				if ((abs_point_x[d]<0) && (sign_x==1)) {abs_point_x[d]=abs_point_x[d]*-1;} 
				if ((abs_point_y[d]<0) && (sign_y==1)) {abs_point_y[d]=abs_point_y[d]*-1;} 
				if ((abs_point_z[d]<0) && (sign_z==1)) {abs_point_z[d]=abs_point_z[d]*-1;}

				if ((abs_point_x[d]>0) && (sign_x==-1)) {abs_point_x[d]=abs_point_x[d]*-1;} 
				if ((abs_point_y[d]>0) && (sign_y==-1)) {abs_point_y[d]=abs_point_y[d]*-1;} 
				if ((abs_point_z[d]>0) && (sign_z==-1)) {abs_point_z[d]=abs_point_z[d]*-1;} 
			}
			
			for (int f=0;f<num_faces;f++) {
				int ref_1=vertex_ref[f][0];
				int ref_2=vertex_ref[f][1];
				int ref_3=vertex_ref[f][2];
				
				double p_phi=  VObject.cartToPolar_phi  (abs_point_x[ref_1],abs_point_y[ref_1],abs_point_z[ref_1]);  
				
				
				double average_height=(VObject.cartToPolar_r(abs_point_x[ref_1],abs_point_y[ref_1],abs_point_z[ref_1])
						+ VObject.cartToPolar_r    (abs_point_x[ref_2],abs_point_y[ref_2],abs_point_z[ref_2])
						+VObject.cartToPolar_r    (abs_point_x[ref_3],abs_point_y[ref_3],abs_point_z[ref_3]))/3.000;
		
				
				// Default to sea colour
		          abs_face_red[f]=3;
		          abs_face_green[f]=50/*+(random.nextInt(20)-10)*/;
		          abs_face_blue[f]=210/*+(random.nextInt(50)-25)*/;

				if (average_height>green_level) {						
					abs_face_red[f]=0+5;//(random.nextInt(5));;
					abs_face_green[f] =100;//+(random.nextInt(30)-15);
					abs_face_blue[f]=0+5;//(random.nextInt(5));
				
					
	
				
				if (average_height>rock_level) {						
					abs_face_red[f]=125+(random.nextInt(30)-15);
					abs_face_green[f] =125+(random.nextInt(30)-15);
					abs_face_blue[f]=125+(random.nextInt(30)-15);
				}

				if (average_height>snow_level) {						
					abs_face_red[f]=230+(random.nextInt(30)-15);;
					abs_face_green[f] =230+(random.nextInt(30)-15);;
					abs_face_blue[f]=230+(random.nextInt(30)-15);;
				}
				
				if  ((p_phi<-((Math.PI/2)*(.9+( (random.nextDouble()-0.5)/10) )) ) || (p_phi>((Math.PI/2)*(.9+( (random.nextDouble()-0.5)/10)))))  {		
					{
						abs_face_red[f]=125;
						abs_face_green[f] =125;
						abs_face_blue[f]=50;
					}
				}

			}
				
				// Polar caps
				if ( (p_phi>-((Math.PI/2)*(.4+( (random.nextDouble()-0.5)/15) ))) && (p_phi<0)) {						
					abs_face_red[f]=240+(random.nextInt(20)-10);;
					abs_face_green[f] =240+(random.nextInt(20)-10);;
					abs_face_blue[f]=240+(random.nextInt(20)-10);;
				}
				
				
				if ( (p_phi<((Math.PI/2)*(.4+( (random.nextDouble()-0.5)/15) ))) && (p_phi>0)) {						
					abs_face_red[f]=230;
					abs_face_green[f] =230;
					abs_face_blue[f]=230;
				}


			}
			
			// Restore previous size and rotation data
		
			this.rotate(Math.PI/2,0,0);
			abs_x_rot=0;
			
			double i=abs_x_rot;
			double j=abs_y_rot;
			double k=abs_z_rot;
			
			abs_x_rot=0;
			abs_y_rot=0;			
			abs_z_rot=0;
			
			this.rotate(i,j,k);
			
		
	}
}	
	
