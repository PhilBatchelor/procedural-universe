package rendercard;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.jogamp.opengl.GL4;

public class Application {
	public static void main(String[] args) {
		Universe universe=new Universe();
		
		new RenderEngine(universe.vertexDataSkybox,
				universe.elementDataSkybox,
				universe.vertexDataSurfaceObjects,
				universe.elementDataSurfaceObjects,
				universe.vertexDataTerrain,
				universe.elementDataTerrain,
				universe.terrain,
				universe.camera,
				universe.gameLogic,
				universe.skyboxfolder);
	}


}
