import java.util.concurrent.*;


public class ForeThought implements Runnable {
	private World world;


	public ForeThought(World w) {
		world=w;		
	}

	public  void  run() {

		double curTime=System.currentTimeMillis();	
		//System.out.println("checking");

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		checkAll();
	}

	public void checkAll() {
		int x=world.camera.x_sector;
		int z=world.camera.z_sector;
		if (world.worldgrid.checkSector(10)==0) {
			world.worldgrid.newLandscapeSector(10,x-2,z+2,world.roughness,world.default_iterations);
			int index=world.findFreeBuffer();
			world.bufferedObject[index].refreshLandscapeTile(world.worldgrid, x-2,z+2,world.sea_level);
			world.visibleSectorToBuffer[10]=index;
		}


		if (world.worldgrid.checkSector(11)==0) {
			world.worldgrid.newLandscapeSector(11,x-1,z+2,world.roughness,world.default_iterations);
			int index=world.findFreeBuffer();
			world.bufferedObject[index].refreshLandscapeTile(world.worldgrid, x-1,z+2,world.sea_level);
			world.visibleSectorToBuffer[11]=index;
		}

		if (world.worldgrid.checkSector(12)==0) {
			world.worldgrid.newLandscapeSector(12,x,  z+2,world.roughness,world.default_iterations);
			int index=world.findFreeBuffer();
			world.bufferedObject[index].refreshLandscapeTile(world.worldgrid, x,z+2,world.sea_level);
			world.visibleSectorToBuffer[12]=index;
		}

		if (world.worldgrid.checkSector(13)==0) {
			world.worldgrid.newLandscapeSector(13,x+1,z+2,world.roughness,world.default_iterations);
			int index=world.findFreeBuffer();
			world.bufferedObject[index].refreshLandscapeTile(world.worldgrid, x+1,z+2,world.sea_level);
			world.visibleSectorToBuffer[13]=index;
		}

		if (world.worldgrid.checkSector(14)==0) {
			world.worldgrid.newLandscapeSector(14,x+2,z+2,world.roughness,world.default_iterations);
			int index=world.findFreeBuffer();
			world.bufferedObject[index].refreshLandscapeTile(world.worldgrid, x+2,z+2,world.sea_level);
			world.visibleSectorToBuffer[14]=index;

		}

		if (world.worldgrid.checkSector(25)==0) {
			world.worldgrid.newLandscapeSector(25,x-2,z+1,world.roughness,world.default_iterations);
			int index=world.findFreeBuffer();
			world.bufferedObject[index].refreshLandscapeTile(world.worldgrid, x-2,z+1,world.sea_level);
			world.visibleSectorToBuffer[25]=index;

		}	
		if (world.worldgrid.checkSector(15)==0) {
			world.worldgrid.newLandscapeSector(15,x+2,z+1,world.roughness,world.default_iterations);
			int index=world.findFreeBuffer();
			world.bufferedObject[index].refreshLandscapeTile(world.worldgrid, x+2,z+1,world.sea_level);
			world.visibleSectorToBuffer[15]=index;

		}
		if (world.worldgrid.checkSector(24)==0) {
			world.worldgrid.newLandscapeSector(24,x-2,z,world.roughness,world.default_iterations);
			int index=world.findFreeBuffer();
			world.bufferedObject[index].refreshLandscapeTile(world.worldgrid, x-2,z,world.sea_level);
			world.visibleSectorToBuffer[24]=index;

		}
		if (world.worldgrid.checkSector(16)==0) {
			world.worldgrid.newLandscapeSector(16,x+2,z,world.roughness,world.default_iterations);
			int index=world.findFreeBuffer();
			world.bufferedObject[index].refreshLandscapeTile(world.worldgrid, x+2,z,world.sea_level);
			world.visibleSectorToBuffer[16]=index;

		}
		if (world.worldgrid.checkSector(23)==0) {
			world.worldgrid.newLandscapeSector(23,x-2,z-1,world.roughness,world.default_iterations);
			int index=world.findFreeBuffer();
			world.bufferedObject[index].refreshLandscapeTile(world.worldgrid, x-2,z-1,world.sea_level);
			world.visibleSectorToBuffer[23]=index;

		}
		if (world.worldgrid.checkSector(17)==0) {
			world.worldgrid.newLandscapeSector(17,x+2,z-1,world.roughness,world.default_iterations);
			int index=world.findFreeBuffer();
			world.bufferedObject[index].refreshLandscapeTile(world.worldgrid, x+2,z-1,world.sea_level);
			world.visibleSectorToBuffer[17]=index;

		}

		if (world.worldgrid.checkSector(22)==0) {
			world.worldgrid.newLandscapeSector(22,x-2,z-2,world.roughness,world.default_iterations); 
			int index=world.findFreeBuffer();
			world.bufferedObject[index].refreshLandscapeTile(world.worldgrid, x-2,z-2,world.sea_level);
			world.visibleSectorToBuffer[22]=index;
		}	
		if (world.worldgrid.checkSector(21)==0) {
			world.worldgrid.newLandscapeSector(21,x-1,z-2,world.roughness,world.default_iterations);
			int index=world.findFreeBuffer();
			world.bufferedObject[index].refreshLandscapeTile(world.worldgrid, x-1,z-2,world.sea_level);
			world.visibleSectorToBuffer[21]=index;

		}
		if (world.worldgrid.checkSector(20)==0) {
			world.worldgrid.newLandscapeSector(20,x,  z-2,world.roughness,world.default_iterations);
			int index=world.findFreeBuffer();
			world.bufferedObject[index].refreshLandscapeTile(world.worldgrid, x,z-2,world.sea_level);
			world.visibleSectorToBuffer[20]=index;

		}
		if (world.worldgrid.checkSector(19)==0) {
			world.worldgrid.newLandscapeSector(19,x+1,z-2,world.roughness,world.default_iterations);
			int index=world.findFreeBuffer();
			world.bufferedObject[index].refreshLandscapeTile(world.worldgrid, x+1,z-2,world.sea_level);
			world.visibleSectorToBuffer[19]=index;
		}

		if (world.worldgrid.checkSector(18)==0) {
			world.worldgrid.newLandscapeSector(18,x+2,z-2,world.roughness,world.default_iterations);
			int index=world.findFreeBuffer();
			world.bufferedObject[index].refreshLandscapeTile(world.worldgrid, x+2,z-2,world.sea_level);
			world.visibleSectorToBuffer[18]=index;

		}
	}

}
