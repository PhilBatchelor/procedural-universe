package rendercard;
public class GameLogic {
	Universe universe;
	public boolean updateTerrainGFX=false;
	
	public GameLogic (Universe u) {
		universe=u;
	}

	public void update() {	
		//System.out.println("Range to Lave: "+distance(universe.camera.INSPosition));
		float[] spherical=CoordinateConversion.getSpherical(universe.camera.INSPosition);
		//System.out.println("I think the camera spherical coordinates are:");
		//System.out.println("r, theta, phi:=:"+spherical[0]+","+spherical[1]+","+spherical[2]);

	    universe.camera.relativeTo.updateTerrain(spherical);
	    if (universe.camera.relativeTo.terrainUpdated) {
	    	universe.terrain=new  GLTerrainModel(universe.camera.relativeTo);
	    	updateTerrainGFX=true;
	    	universe.camera.relativeTo.terrainUpdated=false;
	    }
	}
	
	public GLTerrainModel getGLTerrainModel() {
		return universe.terrain;
	}
}
