package rendercard;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.jogamp.opengl.math.FloatUtil;

import rendercard.Terrain.level;

public class Universe {
	
	private final double PI=Math.PI;
	
	private GLModel model;
	public GLTerrainModel terrain;
	public Camera camera;
	public GameLogic gameLogic;

	public float[] vertexDataSkybox;
	public short[] elementDataSkybox;

	public float[] vertexDataSurfaceObjects ;
	public short[] elementDataSurfaceObjects ;

	public float[] vertexDataTerrain;
    public short[] elementDataTerrain;
    public String skyboxfolder;

     
	public Universe() {
		camera=new Camera(0f,1f,0f,0f,1f,0f);
		initUniverse();
		initModel();
		gameLogic=new GameLogic(this);
	}
	
private void initUniverse() {
	
	Planet lave=new Planet(PlanetaryClass.M,2000);
	float initialAltitude=(float)lave.radius*2.1f;
	float[] initialCartesian=CoordinateConversion.getCartesian(new float[]{initialAltitude,(float)0,(float)0});
	lave.makeTerrain(Terrain.level.GLOBAL,800,.00055f);
	terrain=new GLTerrainModel(lave);
	camera.setINSRelativeTo((OrbitingBody)lave,initialCartesian[0],initialCartesian[1],initialCartesian[2]);
	skyboxfolder="spacebox";
}
	
private void initModel() {
		
		// ** LOAD SKYBOX MODEL
		String objPath="models/skybox.obj";
		String mtlPath="models/cube.mtl";
		try {
			FileInputStream r_pathSkybox = new FileInputStream(objPath);
			BufferedReader b_readSkybox = new BufferedReader(new InputStreamReader(
					r_pathSkybox));
			model = new GLModel(b_readSkybox, true,
					mtlPath);
			r_pathSkybox.close();
			b_readSkybox.close();
			vertexDataSkybox=model.getVertexData();
			elementDataSkybox=model.getElementData();		
		} catch (Exception e) {
			System.out.println("LOADING ERROR" + e);
		}
		
		// ** LOAD SURFACE OBJECT MODELS
		objPath="models/goat.obj";
		mtlPath="models/goat.mtl";
		
		try {
			FileInputStream r_path1 = new FileInputStream(objPath);
			BufferedReader b_read1 = new BufferedReader(new InputStreamReader(
					r_path1));

			model = new GLModel(b_read1, true,
					mtlPath);

			r_path1.close();
			b_read1.close();

			vertexDataSurfaceObjects=model.getVertexData();
			elementDataSurfaceObjects=model.getElementData();		
			//vertexDataSurfaceObjects=terrain.getVertexData();
			//elementDataSurfaceObjects=terrain.getElementData();

		} catch (Exception e) {
			System.out.println("LOADING ERROR" + e);
		}
		
		// ** GENERATE TERRAIN DATA
	    vertexDataTerrain=terrain.getVertexData();
	    elementDataTerrain=terrain.getElementData();
	    
	}
}
