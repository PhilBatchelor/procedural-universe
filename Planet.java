package rendercard;
import java.util.Random;

public class Planet extends OrbitingBody {

	public CelestialBody parent;
	public PlanetaryClass planetaryClass;
	public Terrain terrain;
	private Random random;

	public Planet(PlanetaryClass c, double d) {
		super(CelestialBodyType.PLANET);
		planetaryClass=c;
		//diameter=Math.pow(10, 10);
		diameter=d;
		radius=diameter/2;
	}



}	

