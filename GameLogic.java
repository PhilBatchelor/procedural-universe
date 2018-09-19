package rendercard;
public class GameLogic {
	Universe universe;
	
	public GameLogic (Universe u) {
		universe=u;
	}

	public void update() {	
		//System.out.println("Range to Lave: "+distance(universe.camera.INSPosition));
	    universe.camera.relativeTo.updateTerrain(distance(universe.camera.INSPosition));
	}
	
	private float distance(float[] p) {
		return (float) Math.sqrt( (p[0]*p[0])+ (p[1]*p[1])+ (p[2]*p[2]));
	}
}
