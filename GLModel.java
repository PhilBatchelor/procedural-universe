package rendercard;
import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Objects;

public class GLModel{

	private boolean verbose=false;
	private ArrayList vertexsets;
	private ArrayList vertexsetsnorms;
	private ArrayList vertexsetstexs;
	
	
	private ArrayList faces;
	private ArrayList facestexs;
	private ArrayList facesnorms;
	private ArrayList mattimings;
	private MtlLoader materials;
	private int objectlist;
	private int numpolys;
	public float toppoint;
	public float bottompoint;
	public float leftpoint;
	public float rightpoint;
	public float farpoint;
	public float nearpoint;

	private float[] vertexdata;
	
	private float[] tempvertexdata;
	private int tempvertexref;
	
	private short[] elementdata;
	private int vertexref=0;
	private int elementref=0;

	private String mtl_path;

	// Return vertex data from an imported and converted model
	public float[] getVertexData() {
		System.out.println(vertexdata);
		return vertexdata;
	}

	// Return element (face index) data from an imported and converted model
	public short[] getElementData() {
		System.out.println(elementdata);
		return elementdata;
	}


	// Converts a model held in state to an elementdata array and a vertexdata array. Also outputs code for pasting.
	public void convertModel() {

		int[] tempfaces=new int[3];
		int[] tempfacetexs=new int[3];
		int[] tempfacenorms=new int[3];

		float[] tempverts=new float[4];
		float[] temptexes=new float[4];
		float[] tempnorms=new float[4];

		String[] newVertices= new String[faces.size()*8];
		String[] newIndices= new String[faces.size()*3];

		vertexdata= new float[faces.size()*3*8];
		elementdata = new short[faces.size()*3];

		int newVertexCounter=0;
		int newIndexCounter=0;

		// Consider each face in turn
		for (int i=0;i<faces.size();i++) {

			tempfaces = (int[])(faces.get(i));
			tempfacetexs = (int[])(facestexs.get(i));
			tempfacenorms = (int[])(facesnorms.get(i));

			newVertices[newVertexCounter]="";
			newIndices[newVertexCounter]="";

			// Consider each vertex of each face in turn (triangular faces only supported)
			for (int j=0;j<3;j++) {
				tempvertexdata=new float[8];
				tempvertexref=0;

				tempverts = (float[])(vertexsets.get(tempfaces[j]-1));

				for (int k=0; k<3; k++) {
					newVertices[newVertexCounter]=newVertices[newVertexCounter]+String.valueOf(tempverts[k]);
					newVertices[newVertexCounter]=newVertices[newVertexCounter]+"f";
					newVertices[newVertexCounter]=newVertices[newVertexCounter]+",";

					tempvertexdata[tempvertexref]=tempverts[k];
					tempvertexref++;

				}
				
				boolean normals=true;
				if (vertexsetsnorms.size()==0) normals=false;
				if (normals) vertexsetstexs.get(1);

				if (normals) tempnorms= (float[])(vertexsetsnorms.get(tempfacenorms[j]-1));
				if (!normals) {tempnorms=new float[3];tempnorms[0]=0;tempnorms[1]=0;tempnorms[2]=0;}


				for (int k=0; k<3; k++) {
					newVertices[newVertexCounter]=newVertices[newVertexCounter]+String.valueOf(tempnorms[k]);
					newVertices[newVertexCounter]=newVertices[newVertexCounter]+"f";
					newVertices[newVertexCounter]=newVertices[newVertexCounter]+",";

					tempvertexdata[tempvertexref]=tempnorms[k];
					tempvertexref++;
	
				}

				boolean uvdata=true;
				if (vertexsetstexs.size()==0) uvdata=false;
				if (uvdata) vertexsetstexs.get(1);

				if (uvdata) temptexes = (float[])(vertexsetstexs.get(tempfacetexs[j]-1));
				if (!uvdata) {temptexes=new float[3];temptexes[0]=0;temptexes[1]=0;temptexes[2]=0;}



				for (int k=0; k<2; k++) {
					newVertices[newVertexCounter]=newVertices[newVertexCounter]+String.valueOf(temptexes[k]);


					tempvertexdata[tempvertexref]=temptexes[k];
					tempvertexref++;


					newVertices[newVertexCounter]=newVertices[newVertexCounter]+"f";

					if ((k<1) || (i<faces.size()-1) || (j<2))   {
						newVertices[newVertexCounter]=newVertices[newVertexCounter]+",";
					}
				}

				boolean unique=true;
				int tempCounter=0;

				if (newVertexCounter>0) {
					for (int k=0; k<newVertexCounter; k++) {
						if (Objects.equals(newVertices[newVertexCounter],newVertices[k])) unique=false;
						if (Objects.equals(newVertices[newVertexCounter],newVertices[k])) tempCounter=k;
					}
				}

				// Note the optimisation is switched off if the below is true 
				unique=true;

				if (unique) {
					newIndices[newIndexCounter]=newIndices[newIndexCounter]+String.valueOf(newVertexCounter);
					elementdata[elementref]=(short)newVertexCounter;
					elementref++;

					for (float vert:tempvertexdata) {
						vertexdata[vertexref]=vert;
						vertexref++;
					}

					if ((j<2) || (i<faces.size()-1)) newIndices[newIndexCounter]=newIndices[newIndexCounter]+",";
					newVertexCounter++;
					newVertices[newVertexCounter]="";
				}

				if (!unique) {
					newIndices[newIndexCounter]=newIndices[newIndexCounter]+String.valueOf(tempCounter);
					elementdata[elementref]=(short)tempCounter;
					elementref++;

					if ((j<2) || (i<faces.size()-1)) newIndices[newIndexCounter]=newIndices[newIndexCounter]+",";
					newVertices[newVertexCounter]="";
				} 

			}
			newIndexCounter++;
			newIndices[newIndexCounter]="";

		}

		if (verbose){
			System.out.println("private float[] vertexData = {");
			for (String v:newVertices) {
				if ((v!=null) && (!Objects.equals(v,""))) {	
					System.out.print(v);
					System.out.println();
				}
			}
			System.out.println("};");

			System.out.println("private short[] elementData = {");
			for (String v:newIndices) {	
				if ((v!=null) && (!Objects.equals(v,""))) {	
					System.out.print(v);
					System.out.println();
				}
			}
			System.out.println("};");
		}
	}


	//THIS CLASS LOADS THE MODELS	
	public GLModel(BufferedReader ref, boolean centerit, String path){
		mtl_path=path;
		vertexsets = new ArrayList();
		vertexsetsnorms = new ArrayList();
		vertexsetstexs = new ArrayList();
		faces = new ArrayList();
		facestexs = new ArrayList();
		facesnorms = new ArrayList();
		mattimings = new ArrayList();
		numpolys = 0;
		toppoint = 0.0F;
		bottompoint = 0.0F;
		leftpoint = 0.0F;
		rightpoint = 0.0F;
		farpoint = 0.0F;
		nearpoint = 0.0F;
		loadobject(ref);
		if(centerit)
			centerit();
		normaliseit();
		numpolys = faces.size();
		convertModel();
		//cleanup();
	}

	private void cleanup(){
		vertexsets.clear();
		vertexsetsnorms.clear();
		vertexsetstexs.clear();
		faces.clear();
		facestexs.clear();
		facesnorms.clear();
	}

	private void loadobject(BufferedReader br){
		int linecounter = 0;
		int facecounter = 0;
		try{
			boolean firstpass = true;
			String newline;
			while((newline = br.readLine()) != null){
				linecounter++;
				if(newline.length() > 0){
					newline = newline.trim();

					//LOADS VERTEX COORDINATES
					if(newline.startsWith("v ")){
						float coords[] = new float[4];
						String coordstext[] = new String[4];
						newline = newline.substring(2, newline.length());
						StringTokenizer st = new StringTokenizer(newline, " ");
						for(int i = 0; st.hasMoreTokens(); i++)
							coords[i] = Float.parseFloat(st.nextToken());

						if(firstpass){
							rightpoint = coords[0];
							leftpoint = coords[0];
							toppoint = coords[1];
							bottompoint = coords[1];
							nearpoint = coords[2];
							farpoint = coords[2];
							firstpass = false;
						}
						if(coords[0] > rightpoint)
							rightpoint = coords[0];
						if(coords[0] < leftpoint)
							leftpoint = coords[0];
						if(coords[1] > toppoint)
							toppoint = coords[1];
						if(coords[1] < bottompoint)
							bottompoint = coords[1];
						if(coords[2] > nearpoint)
							nearpoint = coords[2];
						if(coords[2] < farpoint)
							farpoint = coords[2];
						vertexsets.add(coords);
					}
					else

						//LOADS VERTEX TEXTURE COORDINATES
						if(newline.startsWith("vt")){
							float coords[] = new float[4];
							String coordstext[] = new String[4];
							newline = newline.substring(3, newline.length());
							StringTokenizer st = new StringTokenizer(newline, " ");
							for(int i = 0; st.hasMoreTokens(); i++)
								coords[i] = Float.parseFloat(st.nextToken());

							vertexsetstexs.add(coords);
						}
						else

							//LOADS VERTEX NORMALS COORDINATES
							if(newline.startsWith("vn")){
								float coords[] = new float[4];
								String coordstext[] = new String[4];
								newline = newline.substring(3, newline.length());
								StringTokenizer st = new StringTokenizer(newline, " ");
								for(int i = 0; st.hasMoreTokens(); i++)
									coords[i] = Float.parseFloat(st.nextToken());

								vertexsetsnorms.add(coords);
							}
							else

								//LOADS FACES COORDINATES
								if(newline.startsWith("f ")){
									facecounter++;
									newline = newline.substring(2, newline.length());
									StringTokenizer st = new StringTokenizer(newline, " ");
									int count = st.countTokens();
									int v[] = new int[count];
									int vt[] = new int[count];
									int vn[] = new int[count];
									for(int i = 0; i < count; i++){
										char chars[] = st.nextToken().toCharArray();
										StringBuffer sb = new StringBuffer();
										char lc = 'x';
										for(int k = 0; k < chars.length; k++){
											if(chars[k] == '/' && lc == '/')
												sb.append('0');
											lc = chars[k];
											sb.append(lc);
										}

										StringTokenizer st2 = new StringTokenizer
												(sb.toString(), "/");
										int num = st2.countTokens();
										v[i] = Integer.parseInt(st2.nextToken());
										if(num > 1)
											vt[i] = Integer.parseInt(st2.nextToken());
										else
											vt[i] = 0;
										if(num > 2)
											vn[i] = Integer.parseInt(st2.nextToken());
										else
											vn[i] = 0;
									}

									faces.add(v);
									facestexs.add(vt);
									facesnorms.add(vn);
								}
								else

									//LOADS MATERIALS
									if (newline.charAt(0) == 'm' && newline.charAt(1) == 't' && newline.charAt(2) == 'l' && newline.charAt(3) == 'l' && newline.charAt(4) == 'i' && newline.charAt(5) == 'b') {
										String[] coordstext = new String[3];
										coordstext = newline.split("\\s+");
										if(mtl_path!=null)
											loadmaterials();
									}
									else

										//USES MATELIALS
										if (newline.charAt(0) == 'u' && newline.charAt(1) == 's' && newline.charAt(2) == 'e' && newline.charAt(3) == 'm' && newline.charAt(4) == 't' && newline.charAt(5) == 'l') {
											String[] coords = new String[2];
											String[] coordstext = new String[3];
											coordstext = newline.split("\\s+");
											coords[0] = coordstext[1];
											coords[1] = facecounter + "";
											mattimings.add(coords);
											//System.out.println(coords[0] + ", " + coords[1]);
										}
				}
			}
		}
		catch(IOException e){
			System.out.println("Failed to read file: " + br.toString());
		}
		catch(NumberFormatException e){
			System.out.println("Malformed OBJ file: " + br.toString() + "\r \r"+ e.getMessage());
		}
	}

	private void loadmaterials() {
		FileReader frm;
		String refm = mtl_path;

		try {
			frm = new FileReader(refm);
			BufferedReader brm = new BufferedReader(frm);
			materials = new MtlLoader(brm,mtl_path);
			frm.close();
		} catch (IOException e) {
			System.out.println("Could not open file: " + refm);
			materials = null;
		}
	}



	private void normaliseit(){
		float biggest=0;

		for(int i = 0; i < vertexsets.size(); i++){
			float coords[] = new float[4];
			coords = ((float[])vertexsets.get(i));
			for (float coord:coords) {
				if (Math.abs(coord)>biggest) biggest=coord;
			}
		}

		for(int i = 0; i < vertexsets.size(); i++){
			float coords[] = new float[4];
			coords[0] = ((float[])vertexsets.get(i))[0]/biggest;
			coords[1] = ((float[])vertexsets.get(i))[1]/biggest;
			coords[2] = ((float[])vertexsets.get(i))[2]/biggest;
			vertexsets.set(i, coords);
		}
	}

	private void centerit(){
		float xshift = (rightpoint - leftpoint) / 2.0F;
		float yshift = (toppoint - bottompoint) / 2.0F;
		float zshift = (nearpoint - farpoint) / 2.0F;
		for(int i = 0; i < vertexsets.size(); i++){
			float coords[] = new float[4];
			coords[0] = ((float[])vertexsets.get(i))[0] - leftpoint - xshift;
			coords[1] = ((float[])vertexsets.get(i))[1] - bottompoint - yshift;
			coords[2] = ((float[])vertexsets.get(i))[2] - farpoint - zshift;
			vertexsets.set(i, coords);
		}

	}

	public float getXWidth(){
		float returnval = 0.0F;
		returnval = rightpoint - leftpoint;
		return returnval;
	}

	public float getYHeight(){
		float returnval = 0.0F;
		returnval = toppoint - bottompoint;
		return returnval;
	}

	public float getZDepth(){
		float returnval = 0.0F;
		returnval = nearpoint - farpoint;
		return returnval;
	}

	public int numpolygons(){
		return numpolys;
	}
}
