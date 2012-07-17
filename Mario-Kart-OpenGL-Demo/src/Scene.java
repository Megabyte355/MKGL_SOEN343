/** Utility imports **/
import static graphics.util.Renderer.*;

/** Native Java imports **/
import graphics.util.Face;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import static java.lang.Math.*;

/** OpenGL (JOGL) imports **/
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL2.*;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;


/**
 * @author Jamie Bates
 * @version 1.0 (22/02/2012)
 * 
 * This class creates a 3D scene which displays a car that can be moved along a
 * track by the use of hotkeys. Users can also interact with the car model and
 * manipulation the scene in a number of ways.
 */
public class Scene implements GLEventListener, KeyListener, MouseWheelListener
{
	private int canvasWidth = 840;
	private int canvasHeight = 680;
	private static final int FPS = 60;
	private final FPSAnimator animator;
	
	private GLU glu;
	private GLUT glut;
	
	private boolean enableAnimation = true;
	
	private int frames = 0;
	private int frameRate = 0;
	private long startTime = System.currentTimeMillis();
	
	private Texture speedometer;
	
	private Texture brickWall;
	private Texture brickWallTop;
	
	private Texture greenGranite;
	private Texture greenMetal;
	private Texture blueGranite;
	private Texture blueMetal;
	private Texture redGranite;
	private Texture redMetal;
	private Texture yellowGranite;
	private Texture yellowMetal;
	
	private TextRenderer renderer;

	
	/** Model Fields **/
	private boolean displayModels = true;
	
	private List<Face> environmentFaces;
	private List<Face> fortFaces;
	
	private int environmentList;
	private int fortList;
	
	private Car car;
	
	public static final float[] ORIGIN = {0.0f, 0.0f, 0.0f};

	private List<ItemBox> itemBoxes = new ArrayList<ItemBox>();
	
	
	/** Camera Fields **/
	private CameraMode camera = CameraMode.DYNAMIC_VIEW;
	
	private float xRotation_Camera = 0.0f;				
	private float yRotation_Camera = 0.0f;
	private float zRotation_Camera = 0.0f;
	
	private float zoom = 0.75f;
	
	
	/** Fog Fields **/
	private float fogDensity = 0.005f;
	private float[] fogColor = {1.0f, 1.0f, 1.0f, 1.0f};
	
	
	/** Lighting Fields **/
	private float[] global_specular = {1.0f, 1.0f, 1.0f};
	private float[] global_ambience = {0.8f, 0.8f, 0.8f, 1.0f};
	
	private float[] position = {0.0f, 100.0f, 0.0f, 1.0f};
	
    private float[] material_ambience  = {0.7f, 0.7f, 0.7f, 1.0f};
    private float[] material_shininess = {100.0f};
    
    private float cloud_density = 1.0f;
	
    
    /** Environment Fields **/
    private boolean enableSkybox = true;
	
	
	/** Collision Detection Fields **/	
	private boolean enableBoundVisuals     = true;
	private boolean enableOBBAxes          = false;
	private boolean enableOBBVertices      = false;
	private boolean enableOBBWireframes    = false;
	private boolean enableOBBSolids        = false;
	private boolean enableSphereWireframes = false;
	private boolean enableSphereSolids     = false;
	private boolean enableClosestPoints    = false;
	
	private boolean enableObstacles = true;
	
	private List<OBB> wallBounds;
	
	
	/** Music Fields **/
	private boolean musicPlaying = false;
	private static final String MUTE_CITY =
			"file:///" + System.getProperty("user.dir") + "//music//Mute City.mp3";
	
	
	private List<Particle> particles = new ArrayList<Particle>();

	
	private Queue<Integer> itemQueue = new ArrayBlockingQueue<Integer>(100);
	private List<Item> itemList = new ArrayList<Item>();
	
	
	public Scene()
	{
		Frame frame = new Frame();
		
		GLCanvas canvas = new GLCanvas();
		canvas.setPreferredSize(new Dimension(canvasWidth, canvasHeight));
		canvas.addGLEventListener(this);
		canvas.addKeyListener(this);
		canvas.addMouseWheelListener(this);
		canvas.setFocusable(true);
		canvas.requestFocus();
		
		frame.add(canvas);

		animator = new FPSAnimator(canvas, FPS, true);
		
		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				new Thread()
				{
					public void run()
					{
						animator.stop();
						System.exit(0);
					}
				}.start();
			}
		});

		frame.pack();
		frame.setTitle("Mario Kart OpenGL Demo");
		frame.setVisible(true);
		
		animator.start();
	}
	
	public static void main(String[] args) { new Scene(); }
	
	public void init(GLAutoDrawable drawable)
	{	
		Font font = new Font("Calibri", Font.PLAIN, 18);
		renderer = new TextRenderer(font);
		
		try
		{
			speedometer   = TextureIO.newTexture(new File("tex/speedometer.png"), true);
			
			brickWall     = TextureIO.newTexture(new File("tex/longBrick.jpg"), false);
			brickWallTop  = TextureIO.newTexture(new File("tex/longBrickTop.jpg"), false);
			
			greenGranite  = TextureIO.newTexture(new File("tex/greenGranite.jpg"), true);
			greenMetal    = TextureIO.newTexture(new File("tex/greenMetal.jpg"), true);
			blueGranite   = TextureIO.newTexture(new File("tex/blueGranite.jpg"), true);
			blueMetal     = TextureIO.newTexture(new File("tex/blueMetal.jpg"), true);
			redGranite    = TextureIO.newTexture(new File("tex/redGranite.jpg"), true);
			redMetal      = TextureIO.newTexture(new File("tex/redMetal.jpg"), true);
			yellowGranite = TextureIO.newTexture(new File("tex/yellowGranite.jpg"), true);
			yellowMetal   = TextureIO.newTexture(new File("tex/yellowMetal.jpg"), true);
		}
		catch (Exception e) { e.printStackTrace(); }
		
		GL2 gl = drawable.getGL().getGL2();
		glu = new GLU();
		glut = new GLUT();

		gl.glShadeModel(GL_SMOOTH);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		gl.glClearDepth(1.0f);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

		/** Texture Options **/
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,     GL_REPEAT);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,     GL_REPEAT);
		

		/** Fog Setup **/
		gl.glEnable(GL_FOG);
		gl.glFogi  (GL_FOG_MODE, GL_EXP2);
		gl.glFogfv (GL_FOG_COLOR, fogColor, 0);
		gl.glFogf  (GL_FOG_DENSITY, fogDensity);
		gl.glHint  (GL_FOG_HINT, GL_NICEST);
		
		
		/** Lighting Setup **/
		gl.glEnable(GL_LIGHTING);
	    gl.glEnable(GL_LIGHT0);
		
	    
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
		
		long startTime = System.currentTimeMillis();
		
		/** Model Setup **/
		environmentFaces = OBJParser.parseTriangles("obj/environment.obj");
		fortFaces		 = OBJParser.parseTriangles("obj/blockFort.obj");
	    
	    environmentList = gl.glGenLists(1);
	    gl.glNewList(environmentList, GL2.GL_COMPILE_AND_EXECUTE);
	    displayTexturedObject(gl, environmentFaces);
	    gl.glEndList();
	    
	    fortList = gl.glGenLists(4);
	    
	    gl.glNewList(fortList, GL2.GL_COMPILE_AND_EXECUTE);
	    displayWildcardObject(gl, fortFaces, new Texture[] {greenMetal, greenGranite});
	    gl.glEndList();
	    
	    gl.glNewList(fortList + 1, GL2.GL_COMPILE_AND_EXECUTE);
	    displayWildcardObject(gl, fortFaces, new Texture[] {blueMetal, blueGranite});
	    gl.glEndList();
	    
	    gl.glNewList(fortList + 2, GL2.GL_COMPILE_AND_EXECUTE);
	    displayWildcardObject(gl, fortFaces, new Texture[] {redMetal, redGranite});
	    gl.glEndList();
	    
	    gl.glNewList(fortList + 3, GL2.GL_COMPILE_AND_EXECUTE);
	    displayWildcardObject(gl, fortFaces, new Texture[] {yellowMetal, yellowGranite});
	    gl.glEndList();
	    
	    new GreenShell (gl, this, null, 0, false);
	    new RedShell   (gl, this, null, 0, false, null);
	    new BlueShell  (gl, this, null, 0);
	    new FakeItemBox(gl, this, null);
	    new Banana     (gl, this, null, 0);
	    
	    new BoostParticle(ORIGIN, null, 0, 0, 0, false, false);
	    new LightningParticle(ORIGIN);
	    new StarParticle(ORIGIN, null, 0, 0);
	    
	    car = new Car(gl, new float[] {0, 1.8f, 0}, 0, 0, 0, this);
	    
	    itemBoxes.addAll(ItemBox.generateDiamond( 56.25f, 30f, particles));
	    itemBoxes.addAll(ItemBox.generateSquare (101.25f, 60f, particles));
	    itemBoxes.addAll(ItemBox.generateSquare (123.75f, 30f, particles));
	    itemBoxes.addAll(ItemBox.generateDiamond(   180f,  0f, particles));
	    
	    wallBounds = BoundParser.parseOBBs("bound/blockFort.bound");
	   
	    long endTime = System.currentTimeMillis();
	    System.out.println(endTime - startTime);
	}
	
	public void display(GLAutoDrawable drawable)
	{		
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		gl.glLoadIdentity();
		gl.glMatrixMode(GL_MODELVIEW);

		setupCamera(gl);
		setupLights(gl);
		
		if(cloud_density < 1) cloud_density += 0.0125f;
		
		float c = cloud_density;
		gl.glColor3f(c, c, c);
		gl.glFogfv (GL_FOG_COLOR, new float[] {c, c, c, 1}, 0);
		
		while(!itemQueue.isEmpty())
		{
			int itemID = itemQueue.poll();
			
			if(itemID == 10) cloud_density = 0.5f;
			else car.registerItem(gl, itemID);
		}	
		
		if(enableAnimation)
		{	
			removeItems();
			
			removeParticles();
			
			for(ItemBox box : itemBoxes) box.update(car); 
			
			updateItems();
			
			ItemBox.increaseRotation();
			FakeItemBox.increaseRotation();
			
			for(Particle p : particles) p.update();
			
			car.update(); 
		}
		
		long start = System.currentTimeMillis();
		
		if(displayModels) render3DModels(gl);
		
		renderParticles(gl);
		
		gl.glAccum(GL_MULT, 0.5f);
		gl.glAccum(GL_ACCUM, 0.5f);
		
		if(car.isBoosting()) gl.glAccum(GL_RETURN, 1f);
		
		renderBounds(gl);
		renderHUD(gl);
		
		long end = System.currentTimeMillis();
//		System.out.println(end - start);
		
		calculateFPS();
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		GL2 gl = drawable.getGL().getGL2();
	
		if (height <= 0) height = 1;
		
		canvasHeight = height;
		canvasWidth = width;
		
		final float ratio = (float) width / (float) height;
		
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(100.0f, ratio, 2.0, 700.0);
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
		
		gl.glClearAccum(0, 0, 0, 1);
		gl.glClear(GL_ACCUM_BUFFER_BIT);
	}

	public void dispose(GLAutoDrawable drawable) {}

	public void sendItemCommand(int itemID) { itemQueue.add(itemID); }
	
	public void addItem(Item item) { itemList.add(item); }
	
	public void addParticle(Particle p) { particles.add(p); }
	
	public void addParticles(List<Particle> particles) { this.particles.addAll(particles); }
	
	private void updateItems()
	{
		List<Bound> bounds = getBounds();
		
		for(Item item : itemList)
		{
			item.update();
			item.update(car);
		}
		
		for(Item item : car.getItems()) item.hold();
		
		detectItemCollisions();
	}

	private void detectItemCollisions()
	{
		List<Item> allItems = new ArrayList<Item>();
		allItems.addAll(itemList);
		allItems.addAll(car.getItems());
		
		for(int i = 0; i < allItems.size() - 1; i++)
		{
			for(int j = i + 1; j < allItems.size(); j++)
			{
				Item a = allItems.get(i);
				Item b = allItems.get(j);
				
				if(a.getBound().testBound(b.getBound())) a.collide(b);
			}
		}
	}
	
	public void removeParticles()
	{
		List<Particle> toRemove = new ArrayList<Particle>();
		
		for(Particle particle : particles)
			if(particle.isDead()) toRemove.add(particle);
		
		particles.removeAll(toRemove);
	}
	
	public void removeItems()
	{
		List<Item> toRemove = new ArrayList<Item>();
		
		for(Item item : itemList)
			if(item.isDead()) toRemove.add((Item) item);
		
		itemList.removeAll(toRemove);
		
		car.removeItems();
	}
	
	public void renderParticles(GL2 gl)
	{
		for(Particle particle : particles)
			if(car.isSlipping()) particle.render(gl, car.slipTrajectory);
			else particle.render(gl, car.trajectory);
	}

	private void renderHUD(GL2 gl)
	{
		ortho2DBegin(gl);
		
		gl.glDisable(GL_LIGHTING);

		renderSpeedometer(gl);
		
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		
		ItemRoulette roulette = car.getRoulette();
		roulette.cursed = car.isCursed();
		if(roulette.isAlive()) roulette.render(gl);
		
		renderText();
		
		gl.glEnable(GL_LIGHTING);
		
		ortho2DEnd(gl);
	}

	private void renderSpeedometer(GL2 gl)
	{
		gl.glEnable(GL_TEXTURE_2D);
		gl.glEnable(GL_BLEND);

		speedometer.bind(gl);
		
		gl.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
		
		gl.glBegin(GL_QUADS);
		{
			gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(canvasWidth - 250, canvasHeight - 200);
			gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(canvasWidth - 250, canvasHeight      );
			gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(canvasWidth -  50, canvasHeight      );
			gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(canvasWidth -  50, canvasHeight - 200);
		}
		gl.glEnd();
		
		gl.glDisable(GL_BLEND);
		
		double speedRatio = abs(car.velocity) / (2 * Car.TOP_SPEED);
		float zRotation_Meter = (float) ((speedRatio * 240) + 60);
		
		gl.glDisable(GL_TEXTURE_2D);
		
		gl.glPushMatrix();
		{
			gl.glTranslatef(canvasWidth - 150, canvasHeight - 100, 0);
			gl.glRotatef(zRotation_Meter, 0.0f, 0.0f, 1.0f);

			gl.glBegin(GL_QUADS);
			{
				gl.glColor3f(1.0f, 0.0f, 0.0f);

				gl.glVertex2f(0, -10);
				gl.glVertex2f(-10, 0);
				gl.glVertex2f(0, 100);
				gl.glVertex2f(10, 0);
			}
			gl.glEnd();
		}
		gl.glPopMatrix();
		
		gl.glEnable(GL_TEXTURE_2D);
	}

	private void renderText()
	{
		renderer.beginRendering(canvasWidth, canvasHeight);
		renderer.setSmoothing(true);
		
		renderer.draw("Distance: " + (int) car.distance + " m", 40, 40);
		renderer.draw("FPS: " + frameRate, 40, 80);
		
		float[] p = car.getPosition();
		
		renderer.draw("x: " + String.format("%.2f", p[0]), 40, 200);
		renderer.draw("y: " + String.format("%.2f", p[1]), 40, 160);
		renderer.draw("z: " + String.format("%.2f", p[2]), 40, 120);
		
		renderer.draw("Colliding: " + car.colliding, 40, 240);
		renderer.draw("Falling: " + car.falling, 40, 280);
		renderer.draw("Items: " + itemList.size(), 40, 320);
		renderer.draw("Particle: " + particles.size(), 40, 360);
		
		renderer.endRendering();
	}

	private void render3DModels(GL2 gl)
	{	
		gl.glPushMatrix();
		{
			gl.glTranslatef(0, 0, 0);
			gl.glScalef(40.0f, 40.0f, 40.0f);

			if(enableSkybox)
				gl.glCallList(environmentList);
		}	
		gl.glPopMatrix();
		
		if(enableObstacles)
		{
			gl.glPushMatrix();
			{
				//Fort Transformations
				gl.glTranslatef(90, 30, 90);
				gl.glScalef(30.0f, 30.0f, 30.0f);
	
				gl.glCallList(fortList);
			}	
			gl.glPopMatrix();
	
			gl.glPushMatrix();
			{
				gl.glTranslatef(-90, 30, 90);
				gl.glRotatef(-90, 0, 1, 0);
				gl.glScalef(30.0f, 30.0f, 30.0f);
	
				gl.glCallList(fortList + 1);
			}	
			gl.glPopMatrix();
	
			gl.glPushMatrix();
			{
				gl.glTranslatef(-90, 30, -90);
				gl.glRotatef(-180, 0, 1, 0);
				gl.glScalef(30.0f, 30.0f, 30.0f);
	
				gl.glCallList(fortList + 2);
			}	
			gl.glPopMatrix();
			

			gl.glPushMatrix();
			{
				gl.glTranslatef(90, 30, -90);
				gl.glRotatef(-270, 0, 1, 0);
				gl.glScalef(30.0f, 30.0f, 30.0f);
	
				gl.glCallList(fortList + 3);
			}	
			gl.glPopMatrix();
		}
			
		Texture[] textures = {brickWall, brickWallTop, brickWall};
	    	 
		gl.glPushMatrix();
		{
			displayTexturedCuboid(gl,       0, 45,  206.25, 202.5, 45,  3.75,  0, textures);
			displayTexturedCuboid(gl,       0, 45, -206.25, 202.5, 45,  3.75,  0, textures);
	    	displayTexturedCuboid(gl,  206.25, 45,       0, 202.5, 45,  3.75, 90, textures);
	    	displayTexturedCuboid(gl, -206.25, 45,       0, 202.5, 45,  3.75, 90, textures);
		}
		gl.glPopMatrix();

		car.render(gl);

		for(ItemBox box : itemBoxes)
			if(!box.isDead()) box.render(gl, car.trajectory);

		for(Item item : itemList)
			if(!item.isDead()) item.render(gl, car.trajectory);
	}
	
	public List<Bound> getBounds()
	{
		List<Bound> bounds = new ArrayList<Bound>();
		
		if(enableObstacles) bounds.addAll(wallBounds);
		else bounds.addAll(wallBounds.subList(0, 5));
		
		return bounds;
	}

	private void renderBounds(GL2 gl)
	{
		gl.glDisable(GL_TEXTURE_2D);
		
		List<Bound> bounds = getBounds();
		
		if(enableClosestPoints)
			for(Bound bound : bounds)
				bound.displayClosestPtToPt(gl, glut, car.getPosition());
		
		if(enableOBBSolids)
			for(OBB wall : wallBounds)
				wall.displaySolid(gl, glut, new float[] {0, 0.67f, 0.94f, 0.5f});
		
		
		if(enableOBBVertices)
		{
			if(car.colliding)
				 car.bound.displayVertices(gl, glut, new float[] {1, 0, 0, 1});
			else car.bound.displayVertices(gl, glut, new float[] {1, 1, 1, 1});
		
			for(OBB wall : wallBounds)
				wall.displayVertices(gl, glut, new float[] {1, 1, 1, 1});
		}
		
		if(enableOBBWireframes)
		{
			if(car.colliding)
				 car.bound.displayWireframe(gl, glut, new float[] {1, 0, 0, 1});
			else car.bound.displayWireframe(gl, glut, new float[] {0, 0, 0, 1});
			
			for(OBB wall : wallBounds)
				if(car.collisions != null && car.collisions.contains(wall))
					 wall.displayWireframe(gl, glut, new float[] {1, 0, 0, 1});
				else wall.displayWireframe(gl, glut, new float[] {0, 0, 0, 1});
		}
		
		if(enableOBBAxes)
		{
			car.bound.displayAxes(gl, 10);
			
			for(OBB wall : wallBounds)
				wall.displayAxes(gl, 20);
		}
		
		for(Item item : car.getItems()) item.displayBoundVisuals(gl, glut, new float[] {0, 1, 0, 1});
		for(Item item : itemList) item.displayBoundVisuals(gl, glut, new float[] {0, 1, 0, 1});
		
		gl.glEnable(GL_TEXTURE_2D);
	}

	private void setupCamera(GL2 gl)
	{
		switch(camera)
		{	
			//Cause the camera to follow the car dynamically as it moves along the track 
			case DYNAMIC_VIEW:
			{
				car.displayModel = true;
				
				float[] p = car.getPosition();
				
				gl.glTranslatef(0, -15.0f * zoom, -30.0f * zoom);
				if(car.isSlipping()) gl.glRotated(car.slipTrajectory, 0.0f, -1.0f, 0.0f);
				else gl.glRotated(car.trajectory, 0.0f, -1.0f, 0.0f);
				gl.glRotatef(xRotation_Camera, 1.0f, 0.0f, 0.0f);
				gl.glRotatef(yRotation_Camera, 0.0f, 1.0f, 0.0f);
				gl.glRotatef(zRotation_Camera, 0.0f, 0.0f, 1.0f);

				glu.gluLookAt(p[0], p[1], p[2],
						p[0] - 10, p[1], p[2],
						0, 1, 0);

				break;
			}
			//Focus the camera on the centre of the track from a bird�s eye view
			case BIRDS_EYE_VIEW:
			{
				gl.glMatrixMode(GL_PROJECTION);
				gl.glLoadIdentity();
				gl.glOrtho(-200, 200, -200, 200, 1, 200);
				glu.gluLookAt(0, 150, 0,
					          0, 0, 0,
					          0, 0, 1);
				gl.glMatrixMode(GL_MODELVIEW);
				gl.glLoadIdentity();

				break;
			}
			//Setup the camera to view the scene from the driver's perspective
			case DRIVERS_VIEW:
			{
				car.displayModel = false;
				
				float[] p = car.getPosition();
				
				gl.glTranslatef(0, -3.0f, 0);
				
				gl.glRotated(car.trajectory, 0.0f, -1.0f, 0.0f);
				
				gl.glRotatef(xRotation_Camera, 1.0f, 0.0f, 0.0f);
				gl.glRotatef(yRotation_Camera, 0.0f, 1.0f, 0.0f);
				gl.glRotatef(zRotation_Camera, 0.0f, 0.0f, 1.0f);
				
				glu.gluLookAt(p[0], p[1], p[2],
							  p[0] - 10, p[1], p[2],
					          0, 1, 0);
				
				break;
			}
			
			default: break;
		}
	}

	private void setupLights(GL2 gl)
	{
	    gl.glLightfv(GL_LIGHT0, GL_SPECULAR, global_specular, 0);
	    gl.glLightfv(GL_LIGHT0, GL_POSITION, position, 0);
	    gl.glLightModelfv(GL_LIGHT_MODEL_AMBIENT, global_ambience, 0);
	        
	    gl.glMaterialfv(GL_FRONT, GL_AMBIENT, material_ambience, 0);
	    gl.glMaterialfv(GL_FRONT, GL_SHININESS, material_shininess, 0);
	}

	private void calculateFPS()
	{
		frames++;
		
		long timeElapsed = System.currentTimeMillis() - startTime;
		
		if(timeElapsed > 1000)
		{
			frameRate = frames;
			
			frames = 0;
			startTime = System.currentTimeMillis();
		}
	}

	private void ortho2DBegin(GL2 gl)
	{
	    gl.glMatrixMode(GL_PROJECTION);
	    gl.glLoadIdentity();
	    glu.gluOrtho2D(0, canvasWidth, canvasHeight, 0);
	    
	    gl.glMatrixMode(GL_MODELVIEW);
	    gl.glLoadIdentity();
	    gl.glDisable(GL_DEPTH_TEST);
	}
	
	private void ortho2DEnd(GL2 gl)
	{
		float ratio = (float) canvasWidth / (float) canvasHeight;
		gl.glViewport(0, 0, canvasWidth, canvasHeight);
		
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(100.0f, ratio, 2.0, 700.0);
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glEnable(GL_DEPTH_TEST);
	}
		
	public void keyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE: System.exit(0); break; //Close the application
	
			case KeyEvent.VK_H:     enableObstacles = !enableObstacles; break;
	
			case KeyEvent.VK_P:		 playMusic(); break;
	
			case KeyEvent.VK_9:		 if(camera != CameraMode.DRIVERS_VIEW) car.displayModel = !car.displayModel; break;
			case KeyEvent.VK_F1:     enableAnimation = !enableAnimation; break;
	
			case KeyEvent.VK_F2:     Item.toggleBoundSolids();     break;
			case KeyEvent.VK_F3:     Item.toggleBoundWireframes(); break;
	
			case KeyEvent.VK_8:		 displayModels          = !displayModels;          break;
			case KeyEvent.VK_1:		 enableOBBAxes          = !enableOBBAxes;          break;
			case KeyEvent.VK_2:		 enableOBBVertices      = !enableOBBVertices;      break;
			case KeyEvent.VK_3:      enableOBBWireframes    = !enableOBBWireframes;    break;
			case KeyEvent.VK_4:		 enableOBBSolids        = !enableOBBSolids;        break;
			case KeyEvent.VK_5:		 enableSphereSolids     = !enableSphereSolids;     break;
			case KeyEvent.VK_6:      enableSphereWireframes = !enableSphereWireframes; break;
			case KeyEvent.VK_7:		 enableClosestPoints    = !enableClosestPoints;    break;
	
			case KeyEvent.VK_0:		 enableBoundVisuals     = !enableBoundVisuals; toggleBoundVisuals(); break;
	
			case KeyEvent.VK_M:	     switchCamera(); break; //Cycle the camera mode
			case KeyEvent.VK_EQUALS: if(zoom < 1.0) zoom += 0.05; break; //Zoom in the camera
			case KeyEvent.VK_MINUS:  if(zoom > 0.5) zoom -= 0.05; break; //Zoom out the camera
			case KeyEvent.VK_X:      xRotation_Camera += 5; break; //Rotate camera downwards
			case KeyEvent.VK_Y:      yRotation_Camera -= 5; break; //Rotate camera rightwards
	
			case KeyEvent.VK_G:		 car.bound.c[1] += 10; break;
	
			default: car.keyPressed(e); break;
		}
	}
	
	public void keyReleased(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			default: car.keyReleased(e); break;
		}
	}

	public void keyTyped(KeyEvent e)
	{
		switch (e.getKeyChar())
		{
			default: break;
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if (e.getWheelRotation() < 0) { if(zoom < -10) zoom++; } //Zoom in the camera
		else if(zoom > -30) zoom--; //Zoom out the camera
	}

	public void playMusic()
	{
		if(!musicPlaying)
		{
			new MP3(this, MUTE_CITY).start();
			musicPlaying = true;
		}
	}
	
	public void stopMusic() { musicPlaying = false; }
	
	public void toggleBoundVisuals()
	{
		enableOBBAxes          = enableBoundVisuals;
		enableOBBVertices      = enableBoundVisuals;
		enableOBBWireframes    = enableBoundVisuals;
		enableOBBSolids    	   = enableBoundVisuals;
		enableSphereSolids 	   = enableBoundVisuals;
		enableSphereWireframes = enableBoundVisuals;
		enableClosestPoints    = enableBoundVisuals;
	}
	
	/**
	 * Switches the camera mode cyclically as follows:
	 * Dynamic -> Bird's Eye -> Model -> Dynamic
	 */
	private void switchCamera()
	{	
		enableSkybox = true;
		camera = CameraMode.cycle(camera);
	}
	
	private enum CameraMode
	{
		DYNAMIC_VIEW,
		BIRDS_EYE_VIEW,
		DRIVERS_VIEW;
		
		public static CameraMode cycle(CameraMode camera)
		{
			return values()[(camera.ordinal() + 1) % values().length];
		}
	}
}
