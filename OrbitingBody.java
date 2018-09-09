package rendercard;

public class OrbitingBody extends CelestialBody {

	public double phi;
	public double delta_phi;
	public double rho;
	public double radius;

	public double eccentricity;
	public double major_axis;
	public double minor_axis;
	public Terrain terrain;
	public boolean hasTerrain=false;

	OrbitingBody(CelestialBodyType t) {
		super(t);
	}

	public double getDeviation(double altitude,double t,double p) {
		if (!hasTerrain) return 0.0;
		return 0.0;
	}

	public void makeTerrain(int rings, int sectors,Terrain.level l,int iterations, float deviationFraction) {
		terrain=new Terrain(50,50,this);
		terrain.generateGlobalTerrain (l);
		terrain.iterateGlobalTerrain(iterations, deviationFraction, false);
	}

	public float getQuickHeight (int ring, int sector) {
		return terrain.getQuickHeight(ring, sector);
	}
}
