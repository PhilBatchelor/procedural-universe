package rendercard;

import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.VectorUtil;

public class Camera {

	// Defines several possible options for camera movement. Used as abstraction to stay away from window-system specific input methods
	public static enum cameraMovement {
		FORWARD,
		BACKWARD,
		LEFT,
		RIGHT
	};

	// Default camera values
	float dyaw         = -90.0f;
	float dpitch       =  0.0f;
	float dspeed       =  0.05f;
	float dsensitivity =  0.05f;
	float dzoom        =  45.0f;

	// Camera Attributes
	float[] position;
	float[] front;
	float[] up;
	float[] right;
	float[] worldUp;
	float[] nullPosition = new float[]{0f,0f,0f};

	OrbitingBody relativeTo;
	float[] INSPosition;

	// Euler Angles
	float yaw;
	float pitch;

	// Camera options
	float movementSpeed;
	float mouseSensitivity;
	float zoom;

	// Constructor with scalar values
	public Camera(float posX, float posY, float posZ, float upX, float upY, float upZ) 

	{
		position = new float[]{posX,posY,posZ};
		front=new float[]{0.0f, 0.0f, -1.0f};
		worldUp = new float[]{upX, upY, upZ};
		yaw = dyaw;
		pitch = dpitch;
		movementSpeed=dspeed;
		mouseSensitivity=dsensitivity;
		zoom=dzoom;
		updateCameraVectors();
	}

	// Set the Camera's Inertial Navigation System. This tracks its relative position against a fixed reference point
	public void setINSRelativeTo(OrbitingBody b,float x,float y,float z){
		relativeTo=b;
		INSPosition=new float[]{x,y,z};
	}

	// Returns the view matrix calculated using Euler Angles and the LookAt Matrix
	public float[] getViewMatrix()
	{
		return FloatUtil.makeLookAt(new float[16], 0, position, 0, VectorUtil.addVec3(new float[3], position, front), 0, up,0, new float[16]);
	}

	// Returns the view matrix calculated using Euler Angles and the LookAt Matrix, but from the origin ignoring any translations of the Camera
	public float[] getViewMatrixNoTranslation()
	{
		return FloatUtil.makeLookAt(new float[16], 0, nullPosition, 0, VectorUtil.addVec3(new float[3], nullPosition, front), 0, up,0, new float[16]);
	}

	// Processes input received from any keyboard-like input system. Accepts input parameter in the form of camera defined ENUM (to abstract it from windowing systems)
	public void ProcessKeyboard(cameraMovement direction, float deltaTime)
	{
		float velocity = movementSpeed * deltaTime;

		if (direction == cameraMovement.FORWARD) {
			position=VectorUtil.addVec3(new float[3], position, VectorUtil.scaleVec3(new float[3], front, velocity));
			INSPosition=VectorUtil.addVec3(new float[3], INSPosition, VectorUtil.scaleVec3(new float[3], front, velocity));
		}
		if (direction == cameraMovement.BACKWARD){
			position=VectorUtil.subVec3(new float[3], position, VectorUtil.scaleVec3(new float[3], front, velocity));
			INSPosition=VectorUtil.addVec3(new float[3], INSPosition, VectorUtil.scaleVec3(new float[3], front, velocity));
		}
		if (direction == cameraMovement.LEFT){
			position=VectorUtil.subVec3(new float[3], position, VectorUtil.scaleVec3(new float[3], right, velocity));
			INSPosition=VectorUtil.addVec3(new float[3], INSPosition, VectorUtil.scaleVec3(new float[3], front, velocity));
		}
		if (direction == cameraMovement.RIGHT){
			position=VectorUtil.addVec3(new float[3], position, VectorUtil.scaleVec3(new float[3], right, velocity));
			INSPosition=VectorUtil.addVec3(new float[3], INSPosition, VectorUtil.scaleVec3(new float[3], front, velocity));
		}
	}


	// Processes input received from a mouse input system. Expects the offset value in both the x and y direction.
	void ProcessMouseMovement(float xoffset, float yoffset)
	{
		boolean constrainPitch=true;

		xoffset *= mouseSensitivity;
		yoffset *= mouseSensitivity;

		yaw   += xoffset;
		pitch += yoffset;

		// Make sure that when pitch is out of bounds, screen doesn't get flipped
		if (constrainPitch)
		{
			if (pitch > 89.0f)
				pitch = 89.0f;
			if (pitch < -89.0f)
				pitch = -89.0f;
		}

		// Update Front, Right and Up Vectors using the updated Euler angles
		updateCameraVectors();
	}

	// Processes input received from a mouse scroll-wheel event. Only requires input on the vertical wheel-axis
	void ProcessMouseScroll(float yoffset)
	{
		if (zoom >= 1.0f && zoom <= 45.0f)
			zoom -= yoffset;
		if (zoom <= 1.0f)
			zoom = 1.0f;
		if (zoom >= 45.0f)
			zoom = 45.0f;
	}


	// Calculates the front vector from the Camera's (updated) Euler Angles
	private void updateCameraVectors()
	{
		// Calculate the new Front vector	        
		float x = (float)(Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));	   
		float y = (float)Math.sin(Math.toRadians(pitch));	        
		float z = (float)(Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));	        
		front=new float[]{x,y,z};	        
		VectorUtil.normalizeVec3(front);

		// Also re-calculate the Right and Up vector
		right=new float[3];
		VectorUtil.crossVec3(right, front, worldUp);
		VectorUtil.normalizeVec3(right);

		up=new float[3];
		VectorUtil.crossVec3(up, right, front);
		VectorUtil.normalizeVec3(up);

	}		
}
