public class SkyDome extends VObject {
	private double r;  //radius 
	private int texture;

	public double[] abs_point_theta;
	public double[] abs_point_phi;
	public double[] abs_point_r;

	public SkyDome(int s, int t, World world) {

		int steps=s;
		int steps_theta=steps*2;
		int steps_phi=steps+1;

		texture=t;
		texture_wrap=true;

		face_removal_angle=0; 

		potentially_visible=true;
		
		shininess=0.2; // Lower gives a shinier peak spot
		dullness=(1/shininess)+50;  // Lower is duller
		darkness=(double)(-10)/(Math.log(Math.cos(Math.pow(Math.PI/2,shininess))));

		num_points=(steps_theta*steps_phi)+2; 		//Two extra points for the North and South Pole
		num_faces=((steps_theta)*(steps_phi)*2)+10;
		System.out.println(num_faces);

		System.out.print("num points=");
		System.out.println(num_points);

		abs_point_x=new double[num_points];
		abs_point_y=new double[num_points];
		abs_point_z=new double[num_points];

		// The same points in spherical co-ordinates - note these have to be kept in sync
		abs_point_theta=new double[num_points];
		abs_point_phi=new double[num_points];
		abs_point_r=new double[num_points];

		vertex_ref=new int[num_faces][2];

		abs_face_red=new int[num_faces];
		abs_face_green=new int[num_faces];
		abs_face_blue=new int[num_faces];

		double delta_theta=(2*Math.PI)/(steps_theta); 
		double delta_phi=(Math.PI/2)/(steps_phi-1); 
		r=1.00;

		num_points=0;
		for (int i=0;i<steps_phi;i++) {
			for (int j=0;j<steps_theta;j++) {
				abs_point_theta[num_points]=delta_theta*j;
				abs_point_phi[num_points]=delta_phi*i;   
				abs_point_r[num_points]=r;
				num_points++;
			}
		}  

		// North pole
		abs_point_theta[num_points]=0;
		abs_point_phi[num_points]=2*(Math.PI);    
		abs_point_r[num_points]=r;
		num_points++;

		// South pole
		abs_point_theta[num_points]=0;
		abs_point_phi[num_points]=(Math.PI);   
		abs_point_r[num_points]=r;
		num_points++;

		convertPolarToCart();

		face_texture=new int[num_faces];
		num_faces=0;

		for (int i=(steps_theta*((int)(.08*steps)));i<( steps_theta*(steps_phi-1-(int)(.08*steps)));i++) {
			int j=i;
			int k=i+steps_theta;
			int l=i+1;

			if (texture!=0) {
				int startx=(int)(((2*Math.PI-abs_point_theta[l])/(2*Math.PI))*world.texture[texture].width);
				int endx=(int)(((2*Math.PI-abs_point_theta[j])/(2*Math.PI))*world.texture[texture].width)-1;
				if (endx<=0) endx=world.texture[texture].width;
				if (startx==world.texture[texture].width) startx=0;
				int starty=(int)( ((abs_point_phi[k])/(Math.PI))*world.texture[texture].height)-1;
				int endy=(int)(((abs_point_phi[j])/(Math.PI))*world.texture[texture].height);
				face_texture[num_faces]=world.addTexture(new Texture(world.texture[texture],startx,endx,starty,endy));
			}


			vertex_ref[num_faces] = new int[] {l,k,j};
			abs_face_red[num_faces]=255;
			abs_face_green[num_faces] =255;
			abs_face_blue[num_faces]=0;


			num_faces++;

			j=i+steps_theta-1;
			k=i+steps_theta;
			l=i;

			vertex_ref[num_faces] = new int[] {l,k,j};
			abs_face_red[num_faces]=255;
			abs_face_green[num_faces]=255;
			abs_face_blue[num_faces]=0;

			if (texture!=0) {
				if (abs_point_theta[l]==0) abs_point_theta[l]=2*Math.PI;
				int startx=(int)(((2*Math.PI-abs_point_theta[l])/(2*Math.PI))*world.texture[texture].width);
				int endx=(int)(((2*Math.PI-abs_point_theta[j])/(2*Math.PI))*world.texture[texture].width)-1;
				if (endx<=0) endx=world.texture[texture].width;
				if (startx==world.texture[texture].width) startx=0;
				int starty=(int)( ((abs_point_phi[k])/(Math.PI/2))*world.texture[texture].height)-1;
				int endy=(int)(((abs_point_phi[l])/(Math.PI/2))*world.texture[texture].height);
				face_texture[num_faces]=world.addTexture(new Texture(world.texture[texture],startx,endx,starty,endy));
			}

			num_faces++;
		}
		/*

		// Add the North Pole
	
		for (int i=(steps_theta*((int)(.08*steps)));i<(steps_theta*((int)(.08*steps)))+steps_theta;i++) {
			int j=num_points-2;
			int k=i;
			int l=i+1;
			
			vertex_ref[num_faces] = new int[] {l,k,j};
			abs_face_red[num_faces]=255;
			abs_face_green[num_faces] =255;
			abs_face_blue[num_faces]=0;
			
			if (texture!=0) {
			
	
				if (abs_point_theta[k]==0) abs_point_theta[k]=2*Math.PI;
			
				int startx=(int)(((2*Math.PI-abs_point_theta[l])/(2*Math.PI))*world.texture[texture].width);
				int endx=(int)(((2*Math.PI-abs_point_theta[k])/(2*Math.PI))*world.texture[texture].width)-1;
				
				if (endx<=0) endx=world.texture[texture].width-1;
				if (startx==world.texture[texture].width) startx=0;

				int starty=(int)( ((abs_point_phi[j])/(Math.PI/2))*world.texture[texture].height)-1;
				int endy=(int)(((abs_point_phi[k])/(Math.PI/2))*world.texture[texture].height);
				if (starty==world.texture[texture].width-1) starty=0;
			
				face_texture[num_faces]=world.addTexture(new Texture(world.texture[texture],startx,endx,starty,endy));
			}

			
			num_faces++;
		}

		// Add the very last triangle to the North Pole
		vertex_ref[num_faces] = new int[] {(steps_theta*((int)(.08*steps))),(steps_theta*((int)(.08*steps)))+steps_theta-1,num_points-2};
		abs_face_red[num_faces]=255;
		abs_face_green[num_faces] =255;
		abs_face_blue[num_faces]=0;
		num_faces++;

		// Add the South Pole
		System.out.println(steps_theta);
		for (int i=( steps_theta*(steps_phi-1-(int)(.08*steps)));i<( steps_theta*(steps_phi-1-(int)(.08*steps)))+steps_theta;i++) {
			int j=i+1;
			int k=i;
			int l=num_points-1;
			vertex_ref[num_faces] = new int[] {j,k,l};
			abs_face_red[num_faces]=255;
			abs_face_green[num_faces] =255;
			abs_face_blue[num_faces]=0;
			num_faces++;
			
			if (texture!=0) {
				System.out.println("start theta,phi= ("+((2*Math.PI-abs_point_theta[j])/(2*Math.PI))+","+abs_point_phi[j]/Math.PI);
				System.out.println("end theta, phi= ("+((2*Math.PI-abs_point_theta[k])/(2*Math.PI))+","+abs_point_phi[l]/Math.PI+")");
				
				if (abs_point_theta[k]==0) abs_point_theta[k]=2*Math.PI;
			
				int startx=(int)(((2*Math.PI-abs_point_theta[j])/(2*Math.PI))*world.texture[texture].width);
				int endx=(int)(((2*Math.PI-abs_point_theta[k])/(2*Math.PI))*world.texture[texture].width)-1;
				
				if (endx<=0) endx=world.texture[texture].width-1;
				if (startx==world.texture[texture].width) startx=0;

				int starty=(int)( ((abs_point_phi[j])/(Math.PI))*world.texture[texture].height)-1;
				int endy=(int)(((abs_point_phi[l])/(Math.PI))*world.texture[texture].height);
				if (starty==world.texture[texture].width-1) starty=0;
				
				System.out.println("startx,starty= ("+startx+","+starty+")");
				System.out.println("endx,endy= ("+endx+","+endy+")");
				face_texture[num_faces]=world.addTexture(new Texture(world.texture[texture],startx,endx,starty,endy));
			}

			
		}

		// Add the very last triangle to the South Pole
		vertex_ref[num_faces] = new int[] {num_points-1,( steps_theta*(steps_phi-1-(int)(.08*steps)))+steps_theta-1,steps_theta*(steps_phi-1-(int)(.08*steps))};
		abs_face_red[num_faces]=255;
		abs_face_green[num_faces] =255;
		abs_face_blue[num_faces]=0;
		num_faces++;
*/

		System.out.println("Number of faces used: "+num_faces);



		applyTextures(world);

		this.init(world);
		needs_lighting=false;
	}
	
	public void resetLighting() {
		// Reset the object's lighting to darkness
	}

	
	double getRadius() {
		return r;
	}

	public void convertPolarToCart() {
		double theta;
		double phi;
		double pr;

		for (int i=0;i<num_points;i++) {
			theta=abs_point_theta[i];
			phi=abs_point_phi[i];
			pr=abs_point_r[i];
			abs_point_x[i]=VObject.polarToCartX(theta, phi, pr);
			abs_point_y[i]=VObject.polarToCartY(theta, phi, pr);
			abs_point_z[i]=VObject.polarToCartZ(theta, phi, pr);

		}
	}

	public void applyTextures(World world) {

		texel_x=new int[num_faces][3];
		texel_y=new int[num_faces][3];
		double temp_texel_x[]=new double[3];
		double temp_texel_y[]=new double[3];

		if (face_texture==null) {face_texture=new int[num_faces];} 
		else
		{
			for (int t=0; t<num_faces;t++) {
				if (face_texture[t]>0) {
					int w=world.texture[face_texture[t]].width-1;
					int h=world.texture[face_texture[t]].height-1;
					double min_x=999999999;
					double max_x=-999999999;
					double min_y=999999999;
					double max_y=-999999999;

					for (int i=0; i<3; i++) {
						int vref=vertex_ref[t][i];
					
						int x=(int)((abs_point_theta[vref]/(2*Math.PI))*world.texture[texture].width);
						int y=(int)((abs_point_phi[vref]/(Math.PI/2))*world.texture[texture].height);

	
						if (x<min_x) min_x=x;
						if (y<min_y) min_y=y;
						if (x>max_x) max_x=x;
						if (y>max_y) max_y=y;
						temp_texel_x[i]=x;
						temp_texel_y[i]=y;
					}


					max_x=max_x-min_x;
					max_y=max_y-min_y;

					double xscale=w/max_x;
					double yscale=h/max_y;


					for (int i=0; i<3; i++) {

						temp_texel_x[i]=temp_texel_x[i]-min_x;
						temp_texel_x[i]=temp_texel_x[i]*xscale;

						temp_texel_y[i]=temp_texel_y[i]-min_y;
						temp_texel_y[i]=temp_texel_y[i]*yscale;

						texel_x[t][i]=(int)temp_texel_x[i];
						texel_y[t][i]=h-(int)temp_texel_y[i];

					}	
				}
			}
		}
	}	


}
