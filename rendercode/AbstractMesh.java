import java.util.Random;

public class AbstractMesh {

	// DEFINE AN INFINITE 2-D GRID, AND RETURN DATA ON ANY POINT OR REGION OF THE GRID
	// The class allows for an arbitary number of well-defined (array) nested sectors, creation and disposal of these, or algorithmic production

	public int    grid_step;    // how many steps between successive grid-cordinates (allows for later filling-in on a fractal basis)
	public double grid_scale;   // alows for conversion between the abstract grid and real x and z co-ordinates
	public double height_scale; // allows for conversion between the abstract grid and real z co-ordinates

	private int[] sector_x_start;   // beginning of a fully-defined sector
	private int[] sector_z_start;   // beginning of a fully-defined sector
	private int[] sector_size;      // size of a fully-defined sector

	private double[][][] sector_y;          // a set of square arrays, one per sector, with height data
	
	private int[][][] colour_red;           // Red colour data for each point in each sector 
	private int[][][] colour_green;         // Green colour data for each point in each sector 
	private int[][][] colour_blue;          // Blue colour data for each point in each sector 

	private int[] sector_grid_step;         // Allow for sectors to have their own grid_step value
	private int[] sector_definition;		// Describes how well-defined each sector is, from 0 (undefined) up to i, in integer steps
	
	// In nested sectors, the sector with the lowest grid-step will be used

	private int num_sectors=0;              // Number of defined sectors
	private int max_sectors=100;            // Maximum number of sectors allowed
	private Random random;                  // A random number generator

	public AbstractMesh(double scale, int step) {
		random = new Random();
		grid_step=step;
		grid_scale=scale;
		height_scale=scale/2;

		sector_x_start    =new int[max_sectors];
		sector_z_start    =new int[max_sectors];
		sector_size       =new int[max_sectors];
		sector_grid_step  =new int[max_sectors];
		sector_definition =new int[max_sectors];
		sector_y          =new double[max_sectors][][];
	} 
	
	// Generate a VObject based on a defined sector of this landscape
	public LandscapeTile getLandscapeTile(int x, int z, double sealevel, World world) {
		return new LandscapeTile(this,x,z,sealevel,world,false);
	}
	

	public int getSector(int x,int z) { // Return the defined Region at x,z or 0 if no defined sector exists}
		for (int r=1;r<=num_sectors;r++) {
			if ((x==sector_x_start[r]) && (z==sector_z_start[r]))
			{return r;}
		}
		return 0;
	}
	
	public int checkSector(int r) {  // Return how well-defined this sector is
		return sector_definition[r];
	}
	
	public void resetSector(int r) {  // Reset a sector so the engine knows it will need to be re-created
		sector_definition[r]=0;
	}

	public int getGridStep(int r) {
		return sector_grid_step[r];
	}

	public int getIteration(int r) {
		return (int)(Math.log((grid_step/getGridStep(r)))/Math.log(2));
	}

	public double getYFromPlanar(double x, double z) { // Return the 'height' y-axis component from any planar co-ordinates

		int grid_x=(int)Math.round(x/grid_scale); 
		int grid_z=(int)Math.round(z/grid_scale); 
		int sector=getSector(grid_x,grid_z);

		double w1=0;
		double w2=0;
		double w3=0;

		double y1=0;
		double y2=0;
		double y3=0;

		double totalweight=0;

		if (sector !=0 ){
			double sub_scale=(grid_scale*((double)sector_grid_step[sector]))/(double)grid_step;
			double max=(sub_scale*sub_scale)/2;

			int lower_sub_x=(int)(((x-(grid_x*grid_scale))+0.5*grid_scale) /sub_scale);
			int lower_sub_z=(int)(((z-(grid_z*grid_scale))+0.5*grid_scale) /sub_scale);

			int upper_sub_x=lower_sub_x+1;
			int upper_sub_z=lower_sub_z+1;

			double minix=x-((grid_x)*(grid_scale))+(grid_scale*0.5)-(lower_sub_x*sub_scale);
			double miniz=z-((grid_z)*(grid_scale))+(grid_scale*0.5)-(lower_sub_z*sub_scale);

			if (minix>(sub_scale-miniz)) {
				w1=triangle_area(0,sub_scale,sub_scale,0,minix,miniz)/max;
				w2=triangle_area(sub_scale,0,sub_scale,sub_scale,minix,miniz)/max;
				w3=triangle_area(0,sub_scale,sub_scale,sub_scale,minix,miniz)/max;
				totalweight=w1+w2+w3;
				y1=getY(sector,upper_sub_x,upper_sub_z);
				y2=getY(sector,lower_sub_x,upper_sub_z);
				y3=getY(sector,upper_sub_x,lower_sub_z);
			}

			if (minix<=(sub_scale-miniz)) {
				w1=triangle_area(0,0,0,sub_scale,minix,miniz)/max;
				w2=triangle_area(0,0,sub_scale,0,minix,miniz)/max;
				w3=triangle_area(sub_scale,0,0,sub_scale,minix,miniz)/max;
				totalweight=w1+w2+w3;
				y1=getY(sector,upper_sub_x,lower_sub_z);
				y2=getY(sector,lower_sub_x,upper_sub_z);
				y3=getY(sector,lower_sub_x,lower_sub_z);
			}

			//System.out.println("minix, miniz, h="+minix+","+miniz+","+(((w1*y1)+(w2*y2)+(w3*y3))/totalweight));
			return ((w1*y1)+(w2*y2)+(w3*y3))/totalweight;
		}

		else { 
			return 0;
		}  
	}

	private double triangle_area (double x1,double y1, double x2, double y2, double x3, double y3) {
		return Math.abs((( (x1*(y2 -y3)) + (x2*(y3 -y1)) + (x3*(y1 - y2))))/2.000);
	}

	public double getY(int r,int x, int z) { // Return the 'height' (y-axis) component at the grid co-ordinates
		if (r==0) {return 0;}
		else {
			x=x*(sector_grid_step[r]);
			z=z*(sector_grid_step[r]);
			return sector_y[r][x][z]*height_scale;
		}
	}

	public double getX(int r,int x, int z) { // Return the real x-axis co-ordinate at the grid co-ordinates
		if (r==0) {

			return (x*grid_scale)-(0.5*grid_scale);
		}
		else {return ((sector_x_start[r]*grid_scale)-(0.5*grid_scale))+(x*grid_scale*((double)sector_grid_step[r])/(double)grid_step);}
	}

	public double getZ(int r,int x, int z) { // Return the real z-axis co-ordinate at the grid co-ordinates
		if (r==0) {return (z*grid_scale)-(0.5*grid_scale);}
		else {return ((sector_z_start[r]*grid_scale)-(0.5*grid_scale))+(z*grid_scale*((double)sector_grid_step[r])/(double)grid_step);}
	}

	public void copySector (int source, int dest) {

		sector_x_start[dest]=sector_x_start[source];   // beginning of a fully-defined sector
		sector_z_start[dest]=sector_z_start[source];   // beginning of a fully-defined sector
		
		sector_size[dest]=sector_size[source];         // size of a fully-defined sector

		sector_y[dest]=sector_y[source];         	 // a set of square arrays, one per sector, with height data
		sector_grid_step[dest]=sector_grid_step[source];         // Allow for sectors to have their own grid_step value
		sector_definition[dest]=sector_definition[source];         
		
		// In nested sectors, the sector with the lowest grid-step will be used
	}

	public void newRegion (int sector, int sx, int sz, int size, int stepsize) {
		// Create a new defined sector on the grid
		sector_x_start[sector]   =sx;   
		sector_z_start[sector]   =sz;   
		sector_size[sector]      =size;    
		sector_y[sector]         =new double[size][size];
		sector_grid_step[sector] =stepsize;     
		if (num_sectors<sector) {num_sectors=sector;}  
	}

	public void newLandscapeSector(int sector, int sx, int sz, double r, int iteration) {
		// Defines a brand new terrain on an empty sector using the diamond-step algorithm

		sector_x_start[sector]   =sx;   
		sector_z_start[sector]   =sz;   
		sector_size[sector]      =grid_step;
		int size=grid_step;
		if (num_sectors<sector) {num_sectors=sector;}  

		sector_y[sector]         =new double[grid_step+1][grid_step+1];
		sector_grid_step[sector] =grid_step;

		// Check to see if any neighbouring sectors are defined, and import boundary values if so
		int neighbour=getSector(sx-1,sz);
		if (neighbour!=0) {
			for (int count=0; count<grid_step; count++) {
				sector_y[sector][0][count]=sector_y[neighbour][grid_step][count];
			}
		}

		neighbour=getSector(sx+1,sz);
		if (neighbour!=0) {
			for (int count=0; count<grid_step; count++) {
				sector_y[sector][grid_step][count]=sector_y[neighbour][0][count];
			}
		}

		neighbour=getSector(sx,sz-1);
		if (neighbour!=0) {
			for (int count=0; count<grid_step; count++) {
				sector_y[sector][count][0]=sector_y[neighbour][count][grid_step];
			}
		}

		neighbour=getSector(sx,sz+1);
		if (neighbour!=0) {
			for (int count=0; count<grid_step; count++) {
				sector_y[sector][count][grid_step]=sector_y[neighbour][count][0];
			}
		}

		random.setSeed((101*sx)+sz);       
		// INITIAL STEP
		/*
     sector_y[sector] [0]      [0]      =random.nextDouble()*r;
     sector_y[sector] [size]   [0]      =random.nextDouble()*r;
     sector_y[sector] [size]   [size]   =random.nextDouble()*r;
     sector_y[sector] [0]      [size]   =random.nextDouble()*r;
		 */


		sector_y[sector] [0]      [0]      =0;
		sector_y[sector] [size]   [0]      =0;
		sector_y[sector] [size]   [size]   =0;
		sector_y[sector] [0]      [size]   =0;

		// Do as many diamond-steps as required
		diamondSquare(sector, r, iteration);
		//seaLevel(sector,sx,sz);
		
		// Record that this sector is well-defined
		sector_definition[sector]=iteration;
	}

	public void seaLevel(int sector,int sx, int sz){

		int size=     sector_size[sector];
		int stepsize= sector_grid_step[sector];

		for (int z=0; z<=size; z=z+stepsize) {
			for (int x=0; x<=size; x=x+stepsize) {
				if (sector_y[sector][x][z]<0.04) {
					random.setSeed(sx+sz+(1000*(x*stepsize))+10000*(z*stepsize));     
					sector_y[sector][x][z]=0.04-(random.nextDouble()*0.005);}
			}
		}


		// Check to see if any neighbouring sectors are defined, and import boundary values if so
		int neighbour=getSector(sx-1,sz);
		if (neighbour!=0) {
			for (int count=0; count<grid_step; count++) {
				sector_y[sector][0][count]=sector_y[neighbour][grid_step][count];
			}
		}

		neighbour=getSector(sx+1,sz);
		if (neighbour!=0) {
			for (int count=0; count<grid_step; count++) {
				sector_y[sector][grid_step][count]=sector_y[neighbour][0][count];
			}
		}

		neighbour=getSector(sx,sz-1);
		if (neighbour!=0) {
			for (int count=0; count<grid_step; count++) {
				sector_y[sector][count][0]=sector_y[neighbour][count][grid_step];
			}
		}

		neighbour=getSector(sx,sz+1);
		if (neighbour!=0) {
			for (int count=0; count<grid_step; count++) {
				sector_y[sector][count][grid_step]=sector_y[neighbour][count][0];
			}
		}

	}

	public void diamondSquare(int sector, double r, int iterations) {
		// Iterates the terrain on an existing landscape sector using the diamond-step algorithm
		for (int count=0; count<iterations;count++) {
			int size=     sector_size[sector];

			if (sector_grid_step[sector]>1)  // Can't proceed if the sector cannot be interpolated
			{  
				sector_grid_step[sector]=sector_grid_step[sector]/2;
				int stepsize= sector_grid_step[sector];
				int i=        getIteration(sector);      

				random.setSeed(((100*sector_x_start[sector])+sector_z_start[sector])*i);     

				//if (i==2) {sector_y[sector] [stepsize][stepsize]      =-random.nextDouble()*r;}
				double average=0;
				int zp=0;
				int zn=0;
				int xp=0;
				int xn=0;

				// DIAMOND STEP
				for (int z=stepsize; z<size; z=z+stepsize*2)
				{
					for (int x=stepsize; x<size; x=x+(stepsize*2))
					{
						average=( sector_y[sector] [x-stepsize] [z+stepsize] +          
								sector_y[sector] [x+stepsize] [z+stepsize] +          
								sector_y[sector] [x+stepsize] [z-stepsize] +          
								sector_y[sector] [x-stepsize] [z-stepsize] )/4.000;          
						average=average+(2*random.nextDouble()*Math.pow(r,i))-Math.pow(r,i);
						if (sector_y[sector][x][z]==0) {sector_y[sector][x][z]=average;}
					}
				}

				// SQUARE STEP
				for (int z=0; z<=size; z=z+stepsize)
				{
					zp=z-stepsize;
					zn=z+stepsize;
					if (zp==-stepsize)      {zp=stepsize;}
					if (zn==size+stepsize)   {zn=size-stepsize;}

					for (int x=stepsize; x<size; x=x+(stepsize*2))
					{
						xp=x-stepsize;
						xn=x+stepsize;
						if (xp==-stepsize)        {xp=stepsize;}
						if (xn==size+stepsize)    {xn=size-stepsize;}

						average=( sector_y[sector] [x]  [zn] +          
								sector_y[sector] [xp] [z] +          
								sector_y[sector] [x]  [zp] +          
								sector_y[sector] [xn] [z] )/4.000;          
						average=average+(2*random.nextDouble()*Math.pow(r,i))-Math.pow(r,i);
						if (sector_y[sector][x][z]==0) {sector_y[sector][x][z]=average;}
					}

					z=z+stepsize;
					zp=z-stepsize;
					zn=z+stepsize;
					if (zp==-stepsize)        {zp=stepsize;}
					if (zn==size+stepsize)    {zn=size-stepsize;}
					if (z<size) {
						for (int x=0; x<=size; x=x+(stepsize*2))
						{ 
							xp=x-stepsize;
							xn=x+stepsize;
							if (xp==-stepsize)      {xp=stepsize;}
							if (xn==size+stepsize)  {xn=size-stepsize;}
							average=( sector_y[sector] [x]  [zn] +
									sector_y[sector] [xp] [z] +
									sector_y[sector] [x]  [zp] +
									sector_y[sector] [xn] [z] )/4.000;
							average=average+(2*random.nextDouble()*Math.pow(r,i))-Math.pow(r,i);
							if (sector_y[sector][x][z]==0) {sector_y[sector][x][z]=average;}
						}
					}
				}
			}
		}
	}
}


