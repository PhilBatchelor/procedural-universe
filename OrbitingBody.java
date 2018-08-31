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
	
}
