public class VObject {

	public boolean exists;	
	public boolean visible;
	public boolean defined;
	public boolean potentially_visible;
	public boolean light_source=false;
	public boolean needs_lighting=true;
	public boolean texture_wrap=false;
	public boolean local_textures=false;
	public boolean dynamic_lighting=true;
	public boolean gravitational=false;
	public int visible_sector;

	public double radius=1;
	public double mass=1;
	
	public double shininess=0.9; // Lower gives a shinier peak spot
	public double dullness=(1/shininess)-0.8;  // Lower is duller
	public double darkness=(double)(-10)/(Math.log(Math.cos(Math.pow(Math.PI/2,shininess))));

	public int num_points;
	public int num_faces;

	public double[] abs_point_x;
	public double[] abs_point_y;
	public double[] abs_point_z;

	public double[] abs_centroid_x;
	public double[] abs_centroid_y;
	public double[] abs_centroid_z;
	
	public double[] abs_face_normal_x;
	public double[] abs_face_normal_y;
	public double[] abs_face_normal_z;

	// Each point of each face maps onto a particular texel co-ordinate of its associated texture image
	public int[] face_texture;
	public int[] face_texture_width;
	public int[] face_texture_height;
	public int[][] texel_x;
	public int[][] texel_y;
	
	public int[][] face_texture_red_raster;
	public int[][] face_texture_green_raster;
	public int[][] face_texture_blue_raster;

	public int[][] vertex_ref;
	public int[][] shared_faces_of_vertex;
	public int[] num_shared_faces_of_vertex;

	public boolean[] face_visible;
	public double[] face_normal;
	public double[] z_normal;

	public int[] abs_face_red;
	public int[] abs_face_green;
	public int[] abs_face_blue;  

	public double abs_x_pos;
	public double abs_y_pos;
	public double abs_z_pos;
	
	public double abs_x_vel=0;
	public double abs_y_vel=0;
	public double abs_z_vel=0;

	public double abs_x_acc=0;
	public double abs_y_acc=0;
	public double abs_z_acc=0;
	
	public double abs_x_acc_roc=0;
	public double abs_y_acc_roc=0;
	public double abs_z_acc_roc=0;
	
	public double abs_x_rot=0;
	public double abs_y_rot=0;
	public double abs_z_rot=0;

	public double[] rel_point_x;
	public double[] rel_point_y;
	public double[] rel_point_z;

	public double[] rel_centroid_x;
	public double[] rel_centroid_y; 
	public double[] rel_centroid_z;

	public double rel_x_pos;
	public double rel_y_pos;
	public double rel_z_pos;

	public double rel_x_rot;
	public double rel_y_rot;
	public double rel_z_rot;

	public double face_removal_angle=1; 
	
	public int[] abs_point_red;
	public int[] abs_point_green;
	public int[] abs_point_blue;

	// The total light falling on each face
	public int[] rel_face_red;
	public int[] rel_face_green;
	public int[] rel_face_blue;
	
	// The averaged light at each vertex
	public int[] rel_vertex_red;
	public int[] rel_vertex_green;
	public int[] rel_vertex_blue;
	
	// Light emmitted if this is a light-source
	public int light_red=0;
	public int light_green=0;
	public int light_blue=0;
	
	private int ref1;
	private int ref2;
	private int ref3;

	private int h;
	private int w;
	
	private double ux;
	private double uy;
	private double uz;


	private double vx;
	private double vy;
	private double vz;
	
	private double nx;
	private double ny;
	private double nz;
	
	private double cx;
	private double cy;
	private double cz;
	
	private double det_n;
	private double det_c;
	public double face_factor;
	public Texture[] texture;
	public int num_textures;

	public void VOBject() {
		exists=true;
		potentially_visible=false;
	}
	

	
	public int addTexture(Texture tex) {
		// Add a new texture to the world and return the index number
		num_textures++;
		texture[num_textures]=tex;
		return num_textures;
	}
		

	public void init(World world) {
		exists=true;
		defined=true;
		potentially_visible=true;
		rel_point_x=new double[num_points];      
		rel_point_y=new double[num_points];      
		rel_point_z=new double[num_points];      

		abs_point_red=new int[num_points];      
		abs_point_green=new int[num_points];      
		abs_point_blue=new int[num_points];
		
		rel_vertex_red=new int[num_points];  
		rel_vertex_green=new int[num_points];  
		rel_vertex_blue=new int[num_points];  
		
		rel_face_red=new int[num_faces];      
		rel_face_green=new int[num_faces];      
		rel_face_blue=new int[num_faces]; 

		rel_centroid_x=new double[num_faces];
		rel_centroid_y=new double[num_faces];
		rel_centroid_z=new double[num_faces];
		

		// normalise object so its absolute vertices are centred at the origin and adjust object co-ordinates to compensate
		// also calculates radius of object
		normaliseVertices();

		abs_centroid_x=new double[num_faces];
		abs_centroid_y=new double[num_faces];
		abs_centroid_z=new double[num_faces];
		
		abs_face_normal_x=new double[num_faces];
		abs_face_normal_y=new double[num_faces];
		abs_face_normal_z=new double[num_faces];

		calculateAbsoluteCentroids();      
		calculateAbsoluteFaceNormals();
		needs_lighting=true;
		
		face_normal=new double[num_faces];
		z_normal=new double[num_faces];

		// Store the texture information for each face
		if (texture_wrap==false) calculateTexels(world);

		// Work out which faces are shared by each vertex
		shared_faces_of_vertex=new int[num_points][250];
		num_shared_faces_of_vertex=new int[num_points];
		
	    for (int i=0;i<num_faces;i++) {
	    	shared_faces_of_vertex[ vertex_ref[i][0]] [num_shared_faces_of_vertex[vertex_ref[i][0]]]=i;
	    	num_shared_faces_of_vertex[vertex_ref[i][0]]++;
	    	shared_faces_of_vertex[ vertex_ref[i][1]] [num_shared_faces_of_vertex[vertex_ref[i][1]]]=i;
	    	num_shared_faces_of_vertex[vertex_ref[i][1]]++;
	    	shared_faces_of_vertex[ vertex_ref[i][2]] [num_shared_faces_of_vertex[vertex_ref[i][2]]]=i;
	    	num_shared_faces_of_vertex[vertex_ref[i][2]]++;
	    }
	}
	
	public void makeLightSource(int r,int g, int b) {
		// Turn this object into an active light source
		light_source=true;
		light_red=r;
		light_green=g;
		light_blue=b;
	}
	
	public void resetLighting() {
		// Reset the object's lighting to darkness
		rel_vertex_red=new int[num_points];  
		rel_vertex_green=new int[num_points];  
		rel_vertex_blue=new int[num_points];  
		
		rel_face_red=new int[num_faces];      
		rel_face_green=new int[num_faces];      
		rel_face_blue=new int[num_faces]; 
	}

	public void calculateTexels(World world) {

		texel_x=new int[num_faces][3];
		texel_y=new int[num_faces][3];
		double temp_texel_x[]=new double[3];
		double temp_texel_y[]=new double[3];

		if (face_texture==null) {face_texture=new int[num_faces];} 
		else
		{
			for (int t=0; t<num_faces;t++) {
				if (face_texture[t]>0) {
					double newyaxis[]=crossProduct(-1,0,0,abs_face_normal_x[t],abs_face_normal_y[t],abs_face_normal_z[t]);
					double newxaxis[]=crossProduct(abs_face_normal_x[t],abs_face_normal_y[t],abs_face_normal_z[t],newyaxis[0],newyaxis[1],newyaxis[2]);
					
					if (this.local_textures==false) {
						w=world.texture[face_texture[t]].width-1;
						h=world.texture[face_texture[t]].height-1;
					}
					else {
						w=this.face_texture_width[t]-1;
						h=this.face_texture_height[t]-1;
					}
				    
					double min_x=999999999;
					double max_x=-999999999;
					double min_y=999999999;
					double max_y=-999999999;
					//System.out.println("Face: "+t+" , Normal X: "+abs_face_normal_x[t]+" , Normal Y "+abs_face_normal_y[t]+" , Normal Z "+abs_face_normal_z[t]);
					for (int i=0; i<3; i++) {
						int vref=vertex_ref[t][i];
						//System.out.println("Face: "+t+" , Vertex: "+i+" , Orignal X: "+abs_point_x[vref]+" , Original Y: "+abs_point_y[vref]+" , Original Z: "+abs_point_z[vref]);
						double x=dotProduct(abs_point_x[vref],abs_point_y[vref],abs_point_z[vref],newxaxis[0],newxaxis[1],newxaxis[2]);
						double y=dotProduct(abs_point_x[vref],abs_point_y[vref],abs_point_z[vref],newyaxis[0],newyaxis[1],newyaxis[2]);

						//System.out.println("Face: "+t+" , Vertex: "+i+" , Flat X: "+x+" , Flat Y: "+y);
						if (x<min_x) min_x=x;
						if (y<min_y) min_y=y;
						if (x>max_x) max_x=x;
						if (y>max_y) max_y=y;
						temp_texel_x[i]=x;
						temp_texel_y[i]=y;
					}

					//System.out.println("Face: "+t+" Min X: "+min_x+" , Min Y: "+min_y);
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

						//System.out.println("Face: "+t+" , Vertex: "+i+" , Texel X: "+texel_x[t][i]+" , Texel Y: "+texel_y[t][i]);
					}	
				}
			}
		}
	}	

	public void addLightSource(double lx, double ly, double lz, double red, double green, double blue) {
		// Illuminate this object with a new light source
		for (int i=0;i<num_faces;i++) {
			double face_angle=faceAngle(i,lx,ly,lz);
			face_factor=0;
			if ((face_angle<Math.PI/2) && (face_angle>-Math.PI/2)) {
		
				face_factor=dullness*Math.pow(Math.cos(Math.pow(face_angle,shininess)),darkness);}	
			
				rel_face_red[i]=(int)(rel_face_red[i]+(face_factor*red));     
				rel_face_green[i]=(int)(rel_face_green[i]+(face_factor*green));     
				rel_face_blue[i]=(int)(rel_face_blue[i]+(face_factor*blue));
		}
	}
	


	public void calculateAverageLighting () {
		// For Gouraud shading, calculate the average lighting at each shared vertex
		for (int i=0;i<num_points;i++) {

			int red=0;
			int green=0;
			int blue=0;
			int num_shared_faces=num_shared_faces_of_vertex[i];

			int subtract=0; // Subtract faces that are black from the colouring process 
			int totalcolour=0; // Used to find black faces

			if (num_shared_faces>0) {
				for (int face=0; face<num_shared_faces; face++) {
					int face_ref=shared_faces_of_vertex[i][face];
					//totalcolour=red+green+blue;
					red=red+rel_face_red[face_ref];
					green=green+rel_face_green[face_ref];
					blue=blue+rel_face_blue[face_ref];
					//if (totalcolour==red+green+blue) subtract++;
				}

				//num_shared_faces=num_shared_faces-subtract;
				rel_vertex_red[i]=(int)(((double)(red)/(double)(num_shared_faces)));
				rel_vertex_green[i]=(int)(((double)(green)/(double)(num_shared_faces)));
				rel_vertex_blue[i]=(int)(((double)(blue)/(double)(num_shared_faces)));      	
			}
		}
	}


	protected void normaliseVertices() {
		double x_total=0;
		double y_total=0;
		double z_total=0;
		radius=0;

		for (int c=0; c<num_points;c++) {
			x_total=x_total+abs_point_x[c];
			y_total=y_total+abs_point_y[c];
			z_total=z_total+abs_point_z[c];
		}

		x_total=x_total/num_points;
		y_total=y_total/num_points;
		z_total=z_total/num_points;

		for (int c=0; c<num_points;c++) {
			abs_point_x[c]=abs_point_x[c]-x_total;
			if (Math.abs(abs_point_x[c])>radius) {radius=Math.abs(abs_point_x[c]);}
			abs_point_y[c]=abs_point_y[c]-y_total;
			if (Math.abs(abs_point_y[c])>radius) {radius=Math.abs(abs_point_y[c]);}
			abs_point_z[c]=abs_point_z[c]-z_total;
			if (Math.abs(abs_point_z[c])>radius) {radius=Math.abs(abs_point_z[c]);}
		}

		abs_x_pos=x_total;
		abs_y_pos=y_total;
		abs_z_pos=z_total;
	} 
	



	public double faceAngle(int t, double lx, double ly, double lz) {

		nx=abs_face_normal_x[t];
		ny=abs_face_normal_y[t];
		nz=abs_face_normal_z[t];

		// Calculate the vector from the centroid of this face to the light-source
		cx=lx-(abs_x_pos+this.abs_centroid_x[t]);
		cy=ly-(abs_y_pos+this.abs_centroid_y[t]);
		cz=lz-(abs_z_pos+this.abs_centroid_z[t]);
		
		double det_c=det(cx,cy,cz);
		cx=cx/det_c;
		cy=cy/det_c;
		cz=cz/det_c;

		double normal= ( (nx*cx)+(ny*cy)+(nz*cz));
		return Math.acos(normal); // Should be Acos(normal)
	}
	
	public double faceAngle2(int t, double lx, double ly, double lz) {
		// Return the angle between the absolute normal vector of this face and a vector joining it to an absolute position in the world (light-source)

		ref1=vertex_ref[t][0];
		ref2=vertex_ref[t][1];
		ref3=vertex_ref[t][2];

		ux=rel_point_x[ref2]-rel_point_x[ref3];
		uy=rel_point_y[ref2]-rel_point_y[ref3];
		uz=rel_point_z[ref2]-rel_point_z[ref3];

		vx=rel_point_x[ref2]-rel_point_x[ref1];
		vy=rel_point_y[ref2]-rel_point_y[ref1];
		vz=rel_point_z[ref2]-rel_point_z[ref1];
		
		double nx=((uy*vz)-(uz*vy)); 
		double ny=((uz*vx)-(ux*vz));
		double nz=((ux*vy)-(uy*vx));
		
		double det_n=det(nx,ny,nz);
		nx=nx/det_n;
		ny=ny/det_n;
		nz=nz/det_n;

		// Calculate the vector from the centroid of this face to the light-source
		cx=lx-this.rel_centroid_x[t];
		cy=ly-this.rel_centroid_y[t];
		cz=lz-this.rel_centroid_z[t];
		
		double det_c=det(cx,cy,cz);
		cx=cx/det_c;
		cy=cy/det_c;
		cz=cz/det_c;

		double normal1= ( (nx*cx)+(ny*cy)+(nz*cz));

		return normal1;

	}
	
	

	protected void  calculateAbsoluteFaceNormals() {
		// Calculate absolute normal vectors for each face of the object
		
		for (int d=0; d<num_faces;d++) { //loop through faces
			ref1=vertex_ref[d][0];
			ref2=vertex_ref[d][1];
			ref3=vertex_ref[d][2];

			ux=abs_point_x[ref2]-abs_point_x[ref1];
			uy=abs_point_y[ref2]-abs_point_y[ref1];
			uz=abs_point_z[ref2]-abs_point_z[ref1];

			vx=abs_point_x[ref2]-abs_point_x[ref3];
			vy=abs_point_y[ref2]-abs_point_y[ref3];
			vz=abs_point_z[ref2]-abs_point_z[ref3];
			
			double nx=((uy*vz)-(uz*vy)); 
			double ny=((uz*vx)-(ux*vz));
			double nz=((ux*vy)-(uy*vx));
			
			det_n=Math.sqrt((nx*nx)+(ny*ny)+(nz*nz)); // magnitude of normal vector
			
			abs_face_normal_x[d]=nx/det_n;
			abs_face_normal_y[d]=ny/det_n;
			abs_face_normal_z[d]=nz/det_n;

		}
	}

	public double face_normal(int t) {
		// Return the largest angle between the relative normal vector for this face and the vector from the face to the camera
		
		ref1=vertex_ref[t][0];
		ref2=vertex_ref[t][1];
		ref3=vertex_ref[t][2];

		ux=rel_point_x[ref2]-rel_point_x[ref1];
		uy=rel_point_y[ref2]-rel_point_y[ref1];
		uz=rel_point_z[ref2]-rel_point_z[ref1];

		vx=rel_point_x[ref2]-rel_point_x[ref3];
		vy=rel_point_y[ref2]-rel_point_y[ref3];
		vz=rel_point_z[ref2]-rel_point_z[ref3];
		
		double nx=((uy*vz)-(uz*vy)); 
		double ny=((uz*vx)-(ux*vz));
		double nz=((ux*vy)-(uy*vx));
		
		cx=-rel_point_x[ref2];
		cy=-rel_point_y[ref2];
		cz=-rel_point_z[ref2];

		det_n=(nx*nx)+(ny*ny)+(nz*nz); // magnitude of normal vector (should be SQRT of this but we do it later)
		det_c=(cx*cx)+(cy*cy)+(cz*cz);

		return Math.min((nx*cx),(ny*cy))/(Math.sqrt((det_n*det_c)));
	}

	public void calculateRelativePoints(double cam_x_pos,double cam_y_pos,double cam_z_pos,double i,double j,double k) {

		for (int c=0; c<num_points;c++) {

			// Translate relative to camera position, so that the camera's position becomes the origin for this vobject
			this.rel_point_x[c]=(this.abs_point_x[c]+this.abs_x_pos)-cam_x_pos;
			this.rel_point_y[c]=(this.abs_point_y[c]+this.abs_y_pos)-cam_y_pos;
			this.rel_point_z[c]=(this.abs_point_z[c]+this.abs_z_pos)-cam_z_pos;

			// Rotate vobject around the new origin to match the camera viewing angle

			// Rotate around roll

			double tx=this.rel_point_x[c];
			double ty=this.rel_point_y[c];
			double tz=this.rel_point_z[c];
		
      this.rel_point_x[c]=((tx*Math.cos(k))+(ty*Math.sin(k)));
      this.rel_point_y[c]=((-tx*Math.sin(k))+(ty*Math.cos(k)));
			 
			// Rotate around yaw
			tx=this.rel_point_x[c];
			ty=this.rel_point_y[c];
			tz=this.rel_point_z[c];

			this.rel_point_x[c]=((tx*Math.cos(j))-(tz*Math.sin(j)));
			this.rel_point_z[c]=((tx*Math.sin(j))+(tz*Math.cos(j)));

			// Rotate around pitch
			tx=this.rel_point_x[c];
			ty=this.rel_point_y[c];
			tz=this.rel_point_z[c];

			this.rel_point_y[c]=((ty*Math.cos(i))+(tz*Math.sin(i)));
			this.rel_point_z[c]=((-ty*Math.sin(i))+(tz*Math.cos(i)));

		}


		for (int d=0; d<num_faces;d++) { //calculate relative centroids

			// Translate relative to camera position, so that the camera's position becomes the origin for this vobject
			this.rel_centroid_x[d]=(this.abs_centroid_x[d]+this.abs_x_pos)-cam_x_pos;
			this.rel_centroid_y[d]=(this.abs_centroid_y[d]+this.abs_y_pos)-cam_y_pos;
			this.rel_centroid_z[d]=(this.abs_centroid_z[d]+this.abs_z_pos)-cam_z_pos;

			// Rotate vobject around the new origin to match the camera viewing angle
			double tx=this.rel_centroid_x[d];
			double ty=this.rel_centroid_y[d];
			double tz=this.rel_centroid_z[d];

			// Rotate around z axis   
			this.rel_centroid_x[d]=((tx*Math.cos(k))+(ty*Math.sin(k)));
			this.rel_centroid_y[d]=((-tx*Math.sin(k))+(ty*Math.cos(k)));

			tx=this.rel_centroid_x[d];
			ty=this.rel_centroid_y[d];
			tz=this.rel_centroid_z[d];

			// Rotate around y axis
			this.rel_centroid_x[d]=((tx*Math.cos(j))-(tz*Math.sin(j)));
			this.rel_centroid_z[d]=((tx*Math.sin(j))+(tz*Math.cos(j)));

			tx=this.rel_centroid_x[d];
			ty=this.rel_centroid_y[d];
			tz=this.rel_centroid_z[d];

			// Rotate around x axis
			this.rel_centroid_y[d]=((ty*Math.cos(i))+(tz*Math.sin(i)));
			this.rel_centroid_z[d]=((-ty*Math.sin(i))+(tz*Math.cos(i)));

		}

	}

	public void scale(double s) {
		for (int c=0; c<num_points;c++) {
			// scale vobject's absolute vertices by specified scale factor
			this.abs_point_x[c]=this.abs_point_x[c]*s;
			this.abs_point_y[c]=this.abs_point_y[c]*s;
			this.abs_point_z[c]=this.abs_point_z[c]*s;
		}
		// Recalculate the face centroids
		calculateAbsoluteCentroids();
		calculateAbsoluteFaceNormals();
		needs_lighting=true;

		// Recalculate radius
		radius=radius*s;
	}
	
	public void place(double x, double y, double z) {
		this.abs_x_pos=x;
		this.abs_y_pos=y;
		this.abs_z_pos=z;
	}
	
	public void gravitateTowards(VObject vobject) {
		double distance=this.distanceFrom(vobject);
		double G=6.67259*Math.pow(10,-11);
		double f=G*((this.mass*vobject.mass)/(distance*distance));

		double vec_x=-(this.abs_x_pos-vobject.abs_x_pos)/distance;
		double vec_y=-(this.abs_y_pos-vobject.abs_y_pos)/distance;
		double vec_z=-(this.abs_z_pos-vobject.abs_z_pos)/distance;
	

		this.abs_x_acc=this.abs_x_acc+((vec_x*f)/this.mass);		
		this.abs_y_acc=this.abs_y_acc+((vec_y*f)/this.mass);		
		this.abs_z_acc=this.abs_z_acc+((vec_z*f)/this.mass);	
	

	}
	
	public void resetAcc() {
		this.abs_x_acc=this.abs_x_acc_roc;	
		this.abs_y_acc=this.abs_y_acc_roc;	
		this.abs_z_acc=this.abs_z_acc_roc;	
			
	}
	
	public void move(double t) {
		this.abs_x_vel=	this.abs_x_vel+(abs_x_acc*t);
		this.abs_y_vel=	this.abs_y_vel+(abs_y_acc*t);
		this.abs_z_vel=	this.abs_z_vel+(abs_z_acc*t);
		
		this.abs_x_pos=	this.abs_x_pos+(abs_x_vel*t);
		this.abs_y_pos=	this.abs_y_pos+(abs_y_vel*t);
		this.abs_z_pos=	this.abs_z_pos+(abs_z_vel*t);
		
		
	}
	
	public void setMass(double m) {
		this.mass=m;
	}

	public void rotate(double i,double j,double k) {
		for (int c=0; c<num_points;c++) {
			// Rotate vobject's absolute vertices by specified angles
			double tx=this.abs_point_x[c];
			double ty=this.abs_point_y[c];
			double tz=this.abs_point_z[c];

			// Rotate around z axis   
			this.abs_point_x[c]=((tx*Math.cos(k))+(ty*Math.sin(k)));
			this.abs_point_y[c]=((-tx*Math.sin(k))+(ty*Math.cos(k)));

			tx=this.abs_point_x[c];
			ty=this.abs_point_y[c];
			tz=this.abs_point_z[c];

			// Rotate around y axis
			this.abs_point_x[c]=((tx*Math.cos(j))+(tz*Math.sin(j)));
			this.abs_point_z[c]=((-tx*Math.sin(j))+(tz*Math.cos(j)));

			tx=this.abs_point_x[c];
			ty=this.abs_point_y[c];
			tz=this.abs_point_z[c];

			// Rotate around x axis
			this.abs_point_y[c]=((ty*Math.cos(i))+(tz*Math.sin(i)));
			this.abs_point_z[c]=((-ty*Math.sin(i))+(tz*Math.cos(i)));

		}
		
		// Recalculate the face centroids and face normals
		calculateAbsoluteCentroids(); 
		calculateAbsoluteFaceNormals();
		needs_lighting=true;
		
		// Store new rotation values
		abs_x_rot=abs_x_rot+i;
		abs_y_rot=abs_y_rot+j;
		abs_z_rot=abs_z_rot+k;

	}


	public void calculateRelativePosition(double cam_x_pos,double cam_y_pos,double cam_z_pos,double i,double j,double k) {

		// Translate relative to camera position, so that the camera's position becomes the origin for this vobject
		this.rel_x_pos=this.abs_x_pos-cam_x_pos;
		this.rel_y_pos=this.abs_y_pos-cam_y_pos;
		this.rel_z_pos=this.abs_z_pos-cam_z_pos;

		// Rotate vobject around the new origin to match the camera viewing angle
		double tx=this.rel_x_pos;
		double ty=this.rel_y_pos;
		double tz=this.rel_z_pos;

		// Rotate around z axis	
		this.rel_x_pos=((tx*Math.cos(k))+(ty*Math.sin(k)));
		this.rel_y_pos=((-tx*Math.sin(k))+(ty*Math.cos(k)));

		tx=this.rel_x_pos;
		ty=this.rel_y_pos;
		tz=this.rel_z_pos;

		// Rotate around y axis
		this.rel_x_pos=((tx*Math.cos(j))-(tz*Math.sin(j)));
		this.rel_z_pos=((tx*Math.sin(j))+(tz*Math.cos(j)));


		tx=this.rel_x_pos;
		ty=this.rel_y_pos;
		tz=this.rel_z_pos;

		// Rotate around x axis
		this.rel_y_pos=((ty*Math.cos(i))+(tz*Math.sin(i)));
		this.rel_z_pos=((-ty*Math.sin(i))+(tz*Math.cos(i)));

	}
	public void calculateAbsoluteCentroids() {

		for (int d=0; d<num_faces;d++) { //loop through faces
			ref1=vertex_ref[d][0];
			ref2=vertex_ref[d][1];
			ref3=vertex_ref[d][2];

			abs_centroid_x[d]=(abs_point_x[ref1]+abs_point_x[ref2]+abs_point_x[ref3])/3.000;
			abs_centroid_y[d]=(abs_point_y[ref1]+abs_point_y[ref2]+abs_point_y[ref3])/3.000;
			abs_centroid_z[d]=(abs_point_z[ref1]+abs_point_z[ref2]+abs_point_z[ref3])/3.000;
		}
	}
	
	public double distanceFrom(VObject vobject) {
		double x1=this.abs_x_pos;
		double y1=this.abs_y_pos;
		double z1=this.abs_z_pos;
		double x2=vobject.abs_x_pos;
		double y2=vobject.abs_y_pos;
		double z2=vobject.abs_z_pos;

		return Math.sqrt( ((x1-x2)*(x1-x2)) + ((y1-y2)*(y1-y2)) + ((z1-z2)*(z1-z2)));
	}


	public static double polarToCartX(double theta, double phi, double r) {
		return (r*Math.sin(phi)*Math.cos(theta));
	}

	public static double polarToCartY(double theta, double phi, double r) {
		return (r*Math.sin(phi)*Math.sin(theta));
	}

	public static double polarToCartZ(double theta, double phi, double r) {
		return (r*Math.cos(phi));
	}

	public static double cartToPolar_theta(double x, double y, double z) {
		// Note this operation does not preserve the sign of the x,y,z values
		return  Math.atan(y/x);
	}

	public static double cartToPolar_phi(double x, double y, double z) {
		// Note this operation does not preserve the sign of the x,y,z values
		return  Math.atan( (Math.sqrt((x*x)+(y*y))/z) );
	}
	
	public static double cartToPolar_r(double x, double y, double z) {
		// Note this operation does not preserve the sign of the x,y,z values
		return  Math.sqrt((x*x)+(y*y)+(z*z));
	}

	private static double det(double x, double y, double z) {
		return  Math.sqrt((x*x)+(y*y)+(z*z));
	}


	private static double[] crossProduct(double ux, double uy, double uz, double vx, double vy, double vz) {
		double[] cross=new double[3];
		double nx=((uy*vz)-(uz*vy)); 
		double ny=((uz*vx)-(ux*vz));
		double nz=((ux*vy)-(uy*vx));
		cross=new double[]{nx,ny,nz};

		return cross;
	}

	private static double dotProduct(double ux, double uy, double uz, double vx, double vy, double vz) {
		return (ux*vx)+(uy*vy)+(uz*vz);
	}
	
	 protected void setTexel(int f,int x, int y, int r, int g, int b) {
			int index=(y*face_texture_width[f])+x;
			face_texture_red_raster[f][index] = r;
			face_texture_green_raster[f][index] = g;
			face_texture_blue_raster[f][index] = b;
		}

}


