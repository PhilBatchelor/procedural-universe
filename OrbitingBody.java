package rendercard;
import java.awt.image.BufferedImage;

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
	public boolean terrainUpdated=false;

	OrbitingBody(CelestialBodyType t) {
		super(t);
	}

	public double getDeviation(double altitude,double t,double p) {
		if (!hasTerrain) return 0.0;
		return 0.0;
	}

	public void makeTerrain(Terrain.level l,int iterations, float deviationFraction) {
		terrain=new Terrain(this,l,iterations, deviationFraction);

	}

	public void updateTerrain(float[] fs) {
		terrain.updateTerrain(fs);
	}

	public float getQuickHeight (int ring, int sector) {
		return terrain.getQuickHeight(ring, sector);
	}

	public float getQuickTheta (int ring, int sector) {
		return terrain.getQuickTheta(ring, sector);
	}

	public float getQuickPhi (int ring, int sector) {
		return terrain.getQuickPhi(ring, sector);
	}

	public int getRings() {
		return terrain.getRings();
	}


	public int getSectors() {
		return terrain.getSectors();
	}

	

	public BufferedImage getDiffuseTextureBufferedImage() {
		return terrain.getDiffuseTextureBufferedImage();
	}
}
