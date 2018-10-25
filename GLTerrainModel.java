package rendercard;
import java.awt.image.BufferedImage;

public class GLTerrainModel {	
	final double PI=Math.PI;
	final double PIBY2=(Math.PI/2);

	private float[] vertexdata;
	private short[] elementdata;
	private int vertexref=0;
	private int elementref=0;
	public double radius;
	public double theta;
	public double phi;
	private BufferedImage img;

	// Return vertex data from a terrain model
	public float[] getVertexData() {
		return vertexdata;
	}

	// Return element data from a terrain model
	public short[] getElementData() {
		return elementdata;
	}

	public BufferedImage getDiffuseTextureBufferedImage() {
		return img;
	}

	GLTerrainModel(OrbitingBody body) {
		int inputRings=body.getRings();
		int inputSectors=body.getSectors();

		int rings=(inputRings/8)+1;
		int sectors=(inputSectors/8)+1;

		int ringStep=8;
		int sectorStep=8;

		img=body.getDiffuseTextureBufferedImage();

		vertexdata=new float[(rings * sectors * 8)];
		elementdata=new short[(rings * sectors * 6)];

		radius=1; // Consider the radius to be 1 for the purposes of the vertex data;

		double R=1.0f/(rings-1);
		double S=1.0f/(sectors-1);

		System.out.println("rings , sectors of TerrainGeomety: "+inputRings+" , "+ inputSectors);
		System.out.println("rings , sectors of GLTerrainModel: "+rings+" , "+ sectors);

		float low_phi=999999999999f;
		float low_theta=999999999999f;
		float hi_phi=-999999999999f;
		float hi_theta=-999999999999f;

		for(int r = 0; r < rings; r++) {
			for (int s = 0; s < sectors; s++) {

				double rad = radius+(radius*body.getQuickHeight(r*ringStep, s*sectorStep));
				double theta = body.getQuickTheta(r*ringStep, s*sectorStep);
				double phi = body.getQuickPhi(r*ringStep, s*sectorStep);

				double x=CoordinateConversion.getX(1,theta,phi);
				double y=CoordinateConversion.getY(1,theta,phi);
				double z=CoordinateConversion.getZ(1,theta,phi);

				if (phi  <  low_phi)   low_phi  =(float)phi;	
				if (theta<  low_theta) low_theta=(float)theta;	
				if (phi  >  hi_phi)    hi_phi=   (float)phi;	
				if (theta>  hi_theta)  hi_theta= (float)theta;	

				if (vertexref<vertexdata.length) {
					vertexdata[vertexref]=(float)(x*rad);
					vertexref++;
					vertexdata[vertexref]=(float)(y*rad);
					vertexref++;
					vertexdata[vertexref]=(float)(z*rad);
					vertexref++;

					vertexdata[vertexref]=(float)(x);
					vertexref++;
					vertexdata[vertexref]=(float)(y);
					vertexref++;
					vertexdata[vertexref]=(float)(z);
					vertexref++;

					vertexdata[vertexref]=(float)(s*S);
					vertexref++;
					vertexdata[vertexref]=(float)(r*R);
					vertexref++;
				}
			}
		}

		System.out.println("Terrain Segment Complete. Region Lo Phi, Low Theta, Hi Phi, Hi Theta: "+low_phi+" , "+low_theta+" , "+hi_phi+" , "+hi_theta);

		int elerings=rings;
		int elesectors=sectors;
		
		if( (body.terrain.currentLevel!=Terrain.level.GLOBAL) &&(body.terrain.currentLevel!=Terrain.level.CRUDE)) {
			elerings--;
			elesectors--;
		}

		for(int r = 0; r < elerings; r++) {
			for(int s = 0; s < elesectors; s++) {

				elementdata[elementref] = (short)(r * sectors + s);
				elementref++;
				elementdata[elementref] = (short)(r * sectors + (s+1));
				elementref++;
				elementdata[elementref] = (short)((r+1) * sectors + (s+1));
				elementref++;

				elementdata[elementref] = (short)(r * sectors + s);
				elementref++;
				elementdata[elementref] = (short)((r+1) * sectors + (s+1));
				elementref++;
				elementdata[elementref] = (short)((r+1) * sectors + s);
				elementref++;
			}
		}
		radius=body.radius;
	}
}