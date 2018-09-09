package rendercard;

public class GLTerrainModel {	
	final double PI=Math.PI;
	final double PIBY2=(Math.PI/2);

	private float[] vertexdata;
	private short[] elementdata;
	private int vertexref=0;
	private int elementref=0;
	public double radius;
	public double altitude;
	public double theta;
	public double phi;

	// Return vertex data from a terrain model
	public float[] getVertexData() {
		System.out.println(vertexdata);
		return vertexdata;
	}

	// Return element data from a terrain model
	public short[] getElementData() {
		System.out.println(elementdata);
		return elementdata;
	}

	GLTerrainModel(OrbitingBody body, float alt,double the, double ph, int rings,int sectors) {

		final double R=1.0f/(rings-1);
		final double S=1.0f/(sectors-1);

		altitude=alt;
		double factor=(altitude+0.00001)/body.radius;
		if (factor>1) factor=1;
		if (factor<0.00001) factor=0.00001; 
		System.out.println(factor);

		double delta_theta=factor*PI;
		double delta_phi=factor*PI;
		
		if (factor==1) delta_phi=delta_phi*2;

		theta=the;
		phi=ph;
		
		double start_theta=theta-(delta_theta/2);
		double start_phi=phi-(delta_phi/2);

		vertexdata=new float[(rings * sectors * 8)];
		elementdata=new short[(rings * sectors * 6)];

		radius=1; // Consider the radius to be 1 for the purposes of the vertex data;


		for(int r = 0; r < rings; r++) {
			for (int s = 0; s < sectors; s++) {

				double t = start_theta + (delta_theta * r * R); 
				double p = start_phi + (delta_phi* s* S);
				System.out.println(r);
				System.out.println(s);
				
				double rad = radius+(radius*body.getQuickHeight(r, s));

				double x=CoordinateConversion.getX(1,t,p);
				double y=CoordinateConversion.getY(1,t,p);
				double z=CoordinateConversion.getZ(1,t,p);

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

		int elerings=rings;
		int elesectors=sectors;
		if (factor<1.0) {
			elerings--;
			elesectors--;
		}

		for(int r = 0; r < elerings; r++) 
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

		radius=body.radius;
	}

}