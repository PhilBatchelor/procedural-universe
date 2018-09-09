package rendercard;

import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.VectorUtil;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.GLBuffers;


import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;


import framework.Semantic;
import rendercard.Camera.cameraMovement;


import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_HIGH;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_MEDIUM;
import static com.jogamp.opengl.GL2ES3.*;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL4.GL_MAP_COHERENT_BIT;
import static com.jogamp.opengl.GL4.GL_MAP_PERSISTENT_BIT;
import static com.jogamp.opengl.GL2GL3.GL_TEXTURE_SWIZZLE_RGBA;

public class RenderEngine implements GLEventListener, KeyListener, MouseListener {

	private static FPSAnimator animator;
	private static Camera.cameraMovement event;
	private static GLWindow window;
	final int width=1920;
	final int height=1080;
	private GLTerrainModel terrain;

	Camera camera=new Camera(0f,1f,0f,0f,1f,0f);

	private float[] vertexDataSkybox;
	private short[] elementDataSkybox;
	private float[] vertexDataSurfaceObjects ;
	private short[] elementDataSurfaceObjects ;
	private float[] vertexDataTerrain;
	private short[] elementDataTerrain;

	private interface Buffer {
		int VERTEX_SKYBOX = 0;
		int ELEMENT_SKYBOX = 1;
		int GLOBAL_MATRICES1 = 2;
		int GLOBAL_MATRICES2 = 3;
		int MODEL_MATRIX = 4;
		int MODEL_MATRIX2 = 5;
		int VERTEX_SURFACE_OBJECTS=6;
		int ELEMENT_SURFACE_OBJECTS= 7;
		int VERTEX_TERRAIN=8;
		int ELEMENT_TERRAIN= 9;
		int MAX = 10;
	}

	private IntBuffer bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
	private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(10);

	private IntBuffer textureID = GLBuffers.newDirectIntBuffer(10);
	private IntBuffer samplerName = GLBuffers.newDirectIntBuffer(10);

	private FloatBuffer clearColor = GLBuffers.newDirectFloatBuffer(4);
	private FloatBuffer clearDepth = GLBuffers.newDirectFloatBuffer(1);

	private FloatBuffer matBuffer = GLBuffers.newDirectFloatBuffer(16);

	private ByteBuffer globalMatricesPointer1, globalMatricesPointerSkybox,modelMatrixPointer,modelMatrixPointer2;

	private Program basicShader;
	private Program skyboxShader;
	private Program lightCasterShader;

	private int skyboxTextureID;
	private int skyboxVertexArrayName;
	private int surfaceObjectsVertexArrayName;
	private int terrainVertexArrayName;

	private int lastx;
	private int lasty;
	private boolean mousing=false;

	private long start;

	@Override
	public void display(GLAutoDrawable drawable) {

		window.swapBuffers();

		GL4 gl = drawable.getGL().getGL4();
		gl.glClearBufferfv(GL_DEPTH, 0, clearDepth.put(0, 1f));
		gl.glViewport(0, 0, width, height);


		// *** CALCULATE GENERAL MATRICES

		// Calculate the Perspective matrix
		float aspect = (float) width/ height;
		float[] persp = FloatUtil.makePerspective(new float[16], 0, false, (float)Math.PI*0.2f, aspect, 0.1f, 5000f);
		globalMatricesPointer1.asFloatBuffer().put(persp);

		// Calculate the view matrix
		float[] view=camera.getViewMatrix();
		for (int i = 0; i < 16; i++) {globalMatricesPointer1.putFloat(16 * 4 + i * 4, view[i]);}


		gl.glBindBufferBase(
				GL_UNIFORM_BUFFER,
				Semantic.Uniform.TRANSFORM0,
				bufferName.get(Buffer.GLOBAL_MATRICES1));
		
		gl.glUseProgram(lightCasterShader.name);
	

		// *** DRAW SURFACE OBJECTS

		// Calculate the model matrix for the surface objects

		long now = System.currentTimeMillis();
		float diff = (float) (now - start) / 1_000;
		float[] scale = FloatUtil.makeScale(new float[16], true, 0.5f, 0.5f, 0.5f);
		float[] rotate = FloatUtil.makeRotationAxis(new float[16], 0, diff, 1f, 0f, 0f, new float[3]);
		//float[] rotate = FloatUtil.makeRotationEuler(new float[16], 0, diff,diff,diff);
		float[] translate = FloatUtil.makeTranslation(new float[16], 0,true, 0f,0f,-6f);
		float[] model = FloatUtil.multMatrix(scale, rotate);
		model = FloatUtil.multMatrix(translate, model);
		modelMatrixPointer.asFloatBuffer().put(model);

		gl.glBindBufferBase(
				GL_UNIFORM_BUFFER,
				Semantic.Uniform.TRANSFORM1,
				bufferName.get(Buffer.MODEL_MATRIX));

		// Draw the regular objects
		gl.glDepthMask(true);
	
		gl.glBindVertexArray(vertexArrayName.get(1));
		
		gl.glActiveTexture(GL_TEXTURE0 + Semantic.Sampler.DIFFUSE);
		gl.glBindSampler(Semantic.Sampler.DIFFUSE, samplerName.get(0));
		gl.glBindTexture(GL_TEXTURE_2D, textureID.get(0));
		
		gl.glActiveTexture(GL_TEXTURE0 + Semantic.Sampler.SPECULAR);
		gl.glBindSampler(Semantic.Sampler.SPECULAR, samplerName.get(0));
		gl.glBindTexture(GL_TEXTURE_2D, textureID.get(3));
		

        gl.glUniform3f(Semantic.Uniform.LIGHTDIRECTION, -0.2f, -1.0f, -0.3f);
        gl.glUniform3f(Semantic.Uniform.VIEWPOS, camera.position[0],camera.position[1],camera.position[2]);

        // light properties
        gl.glUniform3f(Semantic.Uniform.LIGHTAMBIENT, 0.5f, 0.5f, 0.5f);
        gl.glUniform3f(Semantic.Uniform.LIGHTDIFFUSE, 0.5f, 0.5f, 0.5f);
        gl.glUniform3f(Semantic.Uniform.LIGHTSPECULAR, 1.0f, 1.0f, 1.0f);

        // material properties
        gl.glUniform1f(Semantic.Uniform.MATERIALSHININESS , 90.0f);
		
		gl.glDrawElements(
				GL_TRIANGLES,
				elementDataSurfaceObjects.length,
				GL_UNSIGNED_SHORT,
				0);
        
	
		
		// ******** DRAW THE TERRAIN *********
		gl.glUseProgram(basicShader.name);
		
		// ** TRANSLATE TERRAIN AS IF WE ARE ON THE SURFACE
		/*
		float[] scale2 = FloatUtil.makeScale(new float[16], true, (float)terrain.radius, (float)terrain.radius,(float)terrain.radius);
		float[] rotate2 = FloatUtil.makeRotationEuler(new float[16], 0, 0f,0f,(float)terrain.theta);
		//float[] rotate2 = FloatUtil.makeRotationAxis(new float[16], 0, diff, 0f, 0f, 1f, new float[3]);
		float[] translate2 = FloatUtil.makeTranslation(new float[16], 0,true, 0f,-(float)(terrain.radius+terrain.altitude),0f);
		*/
		
		// TRANSLATIONS FOR BEING IN SPACE
		
		float[] scale2 = FloatUtil.makeScale(new float[16], true, (float)terrain.radius, (float)terrain.radius,(float)terrain.radius);
		//float[] rotate2 = FloatUtil.makeRotationAxis(new float[16], 0, diff, 0f, 0f, 1f, new float[3]);
		float[] rotate2 = FloatUtil.makeRotationEuler(new float[16], 0, 0f,0f,(float)terrain.theta);
		float[] translate2 = FloatUtil.makeTranslation(new float[16], 0,true, 0f,-(float)(terrain.radius+terrain.altitude),0f);
		
		//float[] scale2 = FloatUtil.makeScale(new float[16], true, 500.0f,500.0f,500.0f);
		//float[] rotate2 = FloatUtil.makeRotationAxis(new float[16], 0, diff, 0f, 1f, 0f, new float[3]);
		
		//scale2 = FloatUtil.makeScale(new float[16], true, 1f,1f,1f);
		//rotate2 = FloatUtil.makeRotationAxis(new float[16], 0, diff, 0f, 1f, 0f, new float[3]);
		//translate2 = FloatUtil.makeTranslation(new float[16], 0,true, 0f,0f,-5f);
		//rotate2 = FloatUtil.makeRotationEuler(new float[16], 0, 0f,0f,0f);


		float[] model2 = FloatUtil.multMatrix(scale2, rotate2);
		model2 = FloatUtil.multMatrix(translate2, model2);
		
		modelMatrixPointer2.asFloatBuffer().put(model2);
		gl.glBindBufferBase(
				GL_UNIFORM_BUFFER,
				Semantic.Uniform.TRANSFORM1,
				bufferName.get(Buffer.MODEL_MATRIX2));

		// Draw the terrain objects
	
		gl.glBindVertexArray(vertexArrayName.get(2));
		gl.glActiveTexture(GL_TEXTURE0 + Semantic.Sampler.DIFFUSE);
		gl.glBindSampler(Semantic.Sampler.DIFFUSE, samplerName.get(0));
		gl.glBindTexture(GL_TEXTURE_2D, textureID.get(2));
		
		gl.glDrawElements(
				GL_TRIANGLES,
				elementDataTerrain.length,
				GL_UNSIGNED_SHORT,
				0);

		// ******** DRAW SKYBOX **********

		globalMatricesPointerSkybox.asFloatBuffer().put(persp);
		// Now recalculate the view matrix
		view = new float[16];
		view=camera.getViewMatrixNoTranslation();
		for (int i = 0; i < 16; i++)
			globalMatricesPointerSkybox.putFloat(16 * 4 + i * 4, view[i]);

		// Apply transformations
		gl.glBindBufferBase(
				GL_UNIFORM_BUFFER,
				Semantic.Uniform.TRANSFORM0,
				bufferName.get(Buffer.GLOBAL_MATRICES2));

		// Draw the skybox
		gl.glDepthFunc(GL_LEQUAL);  // change depth function so depth test passes when values are equal to depth buffer's content
		gl.glUseProgram(skyboxShader.name);
		gl.glBindVertexArray(skyboxVertexArrayName);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, skyboxTextureID);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		gl.glDepthFunc(GL_LESS);  // change depth function so depth test passes when values are equal to depth buffer's content
 

		window.swapBuffers();
	}

	public RenderEngine(float[] vs, short[] es, float[] vo, short[] eo, float[] vt, short[] et, GLTerrainModel t) {
		vertexDataSkybox=vs;
		elementDataSkybox=es;
		vertexDataSurfaceObjects=vo;
		elementDataSurfaceObjects=eo;
		vertexDataTerrain=vt;
		elementDataTerrain=et;
		terrain=t;
		setup();
	}


	private void setup() {
		// Create canvas
		GLProfile glProfile = GLProfile.get(GLProfile.GL3);
		GLCapabilities glCapabilities = new GLCapabilities(glProfile);

		window = GLWindow.create(glCapabilities);
		window.setAutoSwapBufferMode(false);

		window.setTitle("Hello Triangle (simple)");

		window.setVisible(true);
		window.setFullscreen(true);
		window.setPointerVisible(false);
		window.confinePointer(true);
		window.warpPointer(width/2,height/2);

		window.addGLEventListener(this);
		window.addKeyListener(this);
		window.addMouseListener(this);

		animator = new FPSAnimator(window,65,true);

		animator.start();

		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowDestroyed(WindowEvent e) {
				animator.stop();
				System.exit(1);
			}
		});
	}

	private void initTexture(GL4 gl) {

		gl.glGenTextures(4, textureID);

		// Load Goat Texture (Texture ID 0)
		try {
			File texture = new File("models/"+"goat.png");

			/* Texture data is an object containing all the relevant information about texture.    */
			TextureData data = TextureIO.newTextureData(gl.getGLProfile(), texture, false, TextureIO.PNG);

			int level = 0;


			gl.glBindTexture(GL_TEXTURE_2D, textureID.get(0));

			{
				gl.glTexImage2D(GL_TEXTURE_2D,
						level,
						data.getInternalFormat(),
						data.getWidth(), data.getHeight(),
						data.getBorder(),
						data.getPixelFormat(), data.getPixelType(),
						data.getBuffer());

				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, level);

				IntBuffer swizzle = GLBuffers.newDirectIntBuffer(new int[]{GL_RED, GL_GREEN, GL_BLUE, GL_ONE});
				gl.glTexParameterIiv(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_RGBA, swizzle);

				//destroyBuffer(swizzle);
			}

			gl.glBindTexture(GL_TEXTURE_2D, 0);

		} catch (IOException ex) {
			System.out.println("arrggghhh");
			// Logger.getLogger(HelloTextureK.class.getName()).log(Level.SEVERE, null, ex);
		}

		// Load SkyBox Texture  (Texture ID 1)
		try {
			String[] skyboxPaths= new String[]{"right.jpg","left.jpg","top.jpg","bottom.jpg","back.jpg","front.jpg"};

			skyboxTextureID=textureID.get(1);
			gl.glBindTexture(GL_TEXTURE_CUBE_MAP, skyboxTextureID);


			for (int i=0;i<6;i++) {
				File texture = new File("models/"+skyboxPaths[i]);
				TextureData data = TextureIO.newTextureData(gl.getGLProfile(), texture, false, TextureIO.PNG);
				int level = 0;

				gl.glTexImage2D(
						GL_TEXTURE_CUBE_MAP_POSITIVE_X+i,
						level,
						data.getInternalFormat(),
						data.getWidth(), data.getHeight(),
						data.getBorder(),
						data.getPixelFormat(), data.getPixelType(),
						data.getBuffer());
			}

			gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);


		} catch (IOException ex) {
			System.out.println("arrggghhh");
			//Logger.getLogger(HelloTextureK.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		// Load Whatever Diffuse Texture (Texture ID2)
		try {
			File texture = new File("models/"+"door.png");

			/* Texture data is an object containing all the relevant information about texture.    */
			TextureData data = TextureIO.newTextureData(gl.getGLProfile(), texture, false, TextureIO.PNG);

			int level = 0;


			gl.glBindTexture(GL_TEXTURE_2D, textureID.get(2));

			{
				gl.glTexImage2D(GL_TEXTURE_2D,
						level,
						data.getInternalFormat(),
						data.getWidth(), data.getHeight(),
						data.getBorder(),
						data.getPixelFormat(), data.getPixelType(),
						data.getBuffer());

				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, level);

				IntBuffer swizzle = GLBuffers.newDirectIntBuffer(new int[]{GL_RED, GL_GREEN, GL_BLUE, GL_ONE});
				gl.glTexParameterIiv(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_RGBA, swizzle);

				//destroyBuffer(swizzle);
			}

			gl.glBindTexture(GL_TEXTURE_2D, 0);

		} catch (IOException ex) {
			System.out.println("arrggghhh");
			// Logger.getLogger(HelloTextureK.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		// Load Whatever Specular Texture (Texture ID3)
		try {
			File texture = new File("models/"+"goatspecular.png");

			/* Texture data is an object containing all the relevant information about texture.    */
			TextureData data = TextureIO.newTextureData(gl.getGLProfile(), texture, false, TextureIO.PNG);

			int level = 0;


			gl.glBindTexture(GL_TEXTURE_2D, textureID.get(3));

			{
				gl.glTexImage2D(GL_TEXTURE_2D,
						level,
						data.getInternalFormat(),
						data.getWidth(), data.getHeight(),
						data.getBorder(),
						data.getPixelFormat(), data.getPixelType(),
						data.getBuffer());

				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, level);

				IntBuffer swizzle = GLBuffers.newDirectIntBuffer(new int[]{GL_RED, GL_GREEN, GL_BLUE, GL_ONE});
				gl.glTexParameterIiv(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_RGBA, swizzle);

				//destroyBuffer(swizzle);
			}

			gl.glBindTexture(GL_TEXTURE_2D, 0);

		} catch (IOException ex) {
			System.out.println("arrggghhh");
			// Logger.getLogger(HelloTextureK.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	private void initSampler(GL4 gl) {

		gl.glGenSamplers(1, samplerName);

		gl.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		gl.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_MIN_FILTER, GL_NEAREST);

		gl.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}



	@Override
	public void init(GLAutoDrawable drawable) {

		GL4 gl = drawable.getGL().getGL4();
		initDebug(gl);

		initBuffers(gl);
		initVertexArray(gl);

		initTexture(gl);
		initSampler(gl);

		basicShader = new Program(gl, "shaders/gl4", "hello-texture", "hello-texture");
		skyboxShader = new Program(gl, "shaders/gl4", "skybox", "skybox");
		lightCasterShader = new Program(gl, "shaders/gl4", "light-caster", "light-caster");

		gl.glEnable(GL_DEPTH_TEST);

		//gl.glPolygonMode(GL_FRONT_AND_BACK,gl.GL_LINE);

		start = System.currentTimeMillis();
	}



	private void initDebug(GL4 gl) {

		gl.getContext().addGLDebugListener(new GLDebugListener() {
			@Override
			public void messageSent(GLDebugMessage event) {
				System.out.println(event);
			}
		});

		gl.glDebugMessageControl(
				GL_DONT_CARE,
				GL_DONT_CARE,
				GL_DONT_CARE,
				0,
				null,
				false);

		gl.glDebugMessageControl(
				GL_DONT_CARE,
				GL_DONT_CARE,
				GL_DEBUG_SEVERITY_HIGH,
				0,
				null,
				true);

		gl.glDebugMessageControl(
				GL_DONT_CARE,
				GL_DONT_CARE,
				GL_DEBUG_SEVERITY_MEDIUM,
				0,
				null,
				true);
	}

	private void initBuffers(GL4 gl) {


		// Import vertex buffer stuff
		FloatBuffer vertexBufferSkybox = GLBuffers.newDirectFloatBuffer(vertexDataSkybox);
		ShortBuffer elementBufferSkybox = GLBuffers.newDirectShortBuffer(elementDataSkybox);

		gl.glCreateBuffers(Buffer.MAX, bufferName);

		// Setup the first buffer, to hold Skybox VBO, EBO
		gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX_SKYBOX));
		gl.glBufferStorage(GL_ARRAY_BUFFER, vertexBufferSkybox.capacity() * Float.BYTES, vertexBufferSkybox, 0);
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT_SKYBOX));
		gl.glBufferStorage(GL_ELEMENT_ARRAY_BUFFER, elementBufferSkybox.capacity() * Short.BYTES, elementBufferSkybox, 0);

		// Import vertex buffer stuff for surface objects
		FloatBuffer vertexBufferSurfaceObjects = GLBuffers.newDirectFloatBuffer(vertexDataSurfaceObjects);
		ShortBuffer elementBufferSurfaceObjects = GLBuffers.newDirectShortBuffer(elementDataSurfaceObjects);

		// Setup the second buffers, to hold surface objects
		gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX_SURFACE_OBJECTS));
		gl.glBufferStorage(GL_ARRAY_BUFFER, vertexBufferSurfaceObjects.capacity() * Float.BYTES, vertexBufferSurfaceObjects, 0);
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT_SURFACE_OBJECTS));
		gl.glBufferStorage(GL_ELEMENT_ARRAY_BUFFER, elementBufferSurfaceObjects.capacity() * Short.BYTES, elementBufferSurfaceObjects, 0);

		// Import vertex buffer stuff for terrain
		System.out.print("Vertex Data Terrain Length: ");
		System.out.println(vertexDataTerrain.length);

		FloatBuffer vertexBufferTerrain = GLBuffers.newDirectFloatBuffer(vertexDataTerrain);
		ShortBuffer elementBufferTerrain  = GLBuffers.newDirectShortBuffer(elementDataTerrain);

		// Setup the third buffers, to hold terrain
		gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX_TERRAIN));
		gl.glBufferStorage(GL_ARRAY_BUFFER, vertexBufferTerrain .capacity() * Float.BYTES, vertexBufferTerrain , 0);
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT_TERRAIN));
		gl.glBufferStorage(GL_ELEMENT_ARRAY_BUFFER, elementBufferTerrain .capacity() * Short.BYTES, elementBufferTerrain , 0);


		// Set up other buffers for matrices
		IntBuffer uniformBufferOffset = GLBuffers.newDirectIntBuffer(1);
		gl.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset);
		int globalBlockSize = Math.max(16 * 4 * 2, uniformBufferOffset.get(0));
		int modelBlockSize = Math.max(16 * 4, uniformBufferOffset.get(0));

		gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.GLOBAL_MATRICES1));
		gl.glBufferStorage(GL_UNIFORM_BUFFER, globalBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
		gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);

		gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.GLOBAL_MATRICES2));
		gl.glBufferStorage(GL_UNIFORM_BUFFER, globalBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
		gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);

		gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.MODEL_MATRIX));
		gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
		gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);

		
		gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.MODEL_MATRIX2));
		gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
		gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);

		// map the transform buffers and keep them mapped
		globalMatricesPointer1 = gl.glMapNamedBufferRange(
				bufferName.get(Buffer.GLOBAL_MATRICES1),
				0,
				16 * 4 * 2,
				GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT); // flags

		// map the transform buffers and keep them mapped
		globalMatricesPointerSkybox = gl.glMapNamedBufferRange(
				bufferName.get(Buffer.GLOBAL_MATRICES2),
				0,
				16 * 4 * 2,
				GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT); // flags

		modelMatrixPointer = gl.glMapNamedBufferRange(
				bufferName.get(Buffer.MODEL_MATRIX),
				0,
				16 * 4,
				GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
		
		modelMatrixPointer2 = gl.glMapNamedBufferRange(
				bufferName.get(Buffer.MODEL_MATRIX2),
				0,
				16 * 4,
				GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
	}

	private void initVertexArray(GL4 gl) {

		gl.glCreateVertexArrays(3, vertexArrayName);

		skyboxVertexArrayName=vertexArrayName.get(0);
		surfaceObjectsVertexArrayName=vertexArrayName.get(1);
		terrainVertexArrayName=vertexArrayName.get(2);

		for (int i=0;i<3;i++) {
			gl.glBindVertexArray(vertexArrayName.get(i));
			gl.glVertexArrayAttribBinding(vertexArrayName.get(i), Semantic.Attr.POSITION, Semantic.Stream.A);
			gl.glVertexArrayAttribBinding(vertexArrayName.get(i), Semantic.Attr.NORMAL, Semantic.Stream.A);
			gl.glVertexArrayAttribBinding(vertexArrayName.get(i), Semantic.Attr.TEXCOORD, Semantic.Stream.A);
			
			gl.glVertexArrayAttribFormat(vertexArrayName.get(i), Semantic.Attr.POSITION, 3, GL_FLOAT, false, 0);
			gl.glVertexArrayAttribFormat(vertexArrayName.get(i), Semantic.Attr.NORMAL, 3, GL_FLOAT, false, (3*4));
			gl.glVertexArrayAttribFormat(vertexArrayName.get(i), Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false,(6*4));
			
			gl.glEnableVertexArrayAttrib(vertexArrayName.get(i), Semantic.Attr.POSITION);
			gl.glEnableVertexArrayAttrib(vertexArrayName.get(i), Semantic.Attr.NORMAL);
			gl.glEnableVertexArrayAttrib(vertexArrayName.get(i), Semantic.Attr.TEXCOORD);
		}

		// Map VAOs to VBOs for Skybox
		gl.glVertexArrayElementBuffer(vertexArrayName.get(0), bufferName.get(Buffer.ELEMENT_SKYBOX));
		gl.glVertexArrayVertexBuffer(vertexArrayName.get(0), Semantic.Stream.A, bufferName.get(Buffer.VERTEX_SKYBOX), 0, (3 + 3 +2) * 4);

		//  Map VAOs to VBOs for imported surface objects
		gl.glVertexArrayElementBuffer(vertexArrayName.get(1), bufferName.get(Buffer.ELEMENT_SURFACE_OBJECTS));
		gl.glVertexArrayVertexBuffer(vertexArrayName.get(1), Semantic.Stream.A, bufferName.get(Buffer.VERTEX_SURFACE_OBJECTS), 0, (3 + 3 +2) * 4);

		//  Map VAOs to VBOs for terrain 
		gl.glVertexArrayElementBuffer(vertexArrayName.get(2), bufferName.get(Buffer.ELEMENT_TERRAIN));
		gl.glVertexArrayVertexBuffer(vertexArrayName.get(2), Semantic.Stream.A, bufferName.get(Buffer.VERTEX_TERRAIN), 0, (3 + 3 +2) * 4);
	}



	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

		GL4 gl = drawable.getGL().getGL4();

		//Calculate the perspective matrix
		float aspect = (float) width / height;
		float[] persp = FloatUtil.makePerspective(new float[16], 0, false, (float)Math.PI*0.2f, aspect, 0.1f, 50f);
		globalMatricesPointer1.asFloatBuffer().put(persp);
		globalMatricesPointerSkybox.asFloatBuffer().put(persp);
		gl.glViewport(x, y, width, height);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {

		GL4 gl = drawable.getGL().getGL4();

		gl.glUnmapNamedBuffer(bufferName.get(Buffer.GLOBAL_MATRICES1));
		gl.glUnmapNamedBuffer(bufferName.get(Buffer.GLOBAL_MATRICES2));
		gl.glUnmapNamedBuffer(bufferName.get(Buffer.MODEL_MATRIX));

		gl.glDeleteProgram(basicShader.name);
		gl.glDeleteVertexArrays(1, vertexArrayName);
		gl.glDeleteBuffers(Buffer.MAX, bufferName);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_W) {
			camera.ProcessKeyboard(cameraMovement.FORWARD, 1);
		}

		if (e.getKeyCode() == KeyEvent.VK_S) {
			camera.ProcessKeyboard(cameraMovement.BACKWARD, 1);

		}
		if (e.getKeyCode() == KeyEvent.VK_A) {
			camera.ProcessKeyboard(cameraMovement.LEFT, 1);

		}
		if (e.getKeyCode() == KeyEvent.VK_D) {
			camera.ProcessKeyboard(cameraMovement.RIGHT, 1);

		}

		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			window.destroy();
			animator.stop();
			System.exit(1);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}


	private class Program {

		public int name = 0;

		public Program(GL4 gl, String root, String vertex, String fragment) {

			ShaderCode vertShader = ShaderCode.create(gl, GL_VERTEX_SHADER, this.getClass(), root, null, vertex,
					"vert", null, true);
			ShaderCode fragShader = ShaderCode.create(gl, GL_FRAGMENT_SHADER, this.getClass(), root, null, fragment,
					"frag", null, true);

			ShaderProgram shaderProgram = new ShaderProgram();

			shaderProgram.add(vertShader);
			shaderProgram.add(fragShader);

			System.out.println("about to initiate: "+vertex);
			shaderProgram.init(gl);
			System.out.println("Completed initiation of: "+vertex);

			name = shaderProgram.program();

			shaderProgram.link(gl, System.err);

		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.isAnyButtonDown()) {
			lastx=e.getX();
			lasty=e.getY();
			mousing=true;
		}
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		mousing=false;
	}


	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {

		if (mousing) {
			// Centre Mouse Pointer
			lastx=lastx+(width/2-e.getX());
			lasty=lasty+(height/2-e.getY());
			window.warpPointer(width/2,height/2);
			camera.ProcessMouseMovement(e.getX()-lastx, lasty-e.getY());
			lastx=e.getX();
			lasty=e.getY();

		}
	}


	@Override
	public void mouseEntered(MouseEvent e) {
		saySomething("Mouse entered", e);
	}
	@Override
	public void mouseExited(MouseEvent e) {
		saySomething("Mouse exited", e);
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		saySomething("Mouse clicked (# of clicks: "
				+ e.getClickCount() + ")", e);
	}

	@Override
	public void mouseWheelMoved(MouseEvent e) {
	}


	void saySomething(String eventDescription, MouseEvent e) {
		System.out.println(eventDescription);
	}

	private class GlDebugOutput implements GLDebugListener {

		private int source = 0;
		private int type = 0;
		private int id = 0;
		private int severity = 0;
		private int length = 0;
		private String message = null;
		private boolean received = false;

		public GlDebugOutput() {
		}

		public GlDebugOutput(int source, int type, int severity) {
			this.source = source;
			this.type = type;
			this.severity = severity;
			this.message = null;
			this.id = -1;
		}

		public GlDebugOutput(String message, int id) {
			this.source = -1;
			this.type = -1;
			this.severity = -1;
			this.message = message;
			this.id = id;
		}

		@Override
		public void messageSent(GLDebugMessage event) {

			if (event.getDbgSeverity() == GL_DEBUG_SEVERITY_LOW || event.getDbgSeverity() == GL_DEBUG_SEVERITY_NOTIFICATION)
				System.out.println("GlDebugOutput.messageSent(): " + event);
			else
				System.err.println("GlDebugOutput.messageSent(): " + event);

			if (null != message && message == event.getDbgMsg() && id == event.getDbgId())
				received = true;
			else if (0 <= source && source == event.getDbgSource() && type == event.getDbgType() && severity == event.getDbgSeverity())
				received = true;
		}
	}
}
