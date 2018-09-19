package rendercard;

public class Planet extends OrbitingBody {

	public CelestialBody parent;
	public PlanetaryClass planetaryClass;
	public Terrain terrain;

	public Planet(PlanetaryClass c, double r) {
		super(CelestialBodyType.PLANET);
		planetaryClass=c;
		//diameter=Math.pow(10, 10);
		radius=r;
	}

}	

