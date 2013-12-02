package bates.jamie.graphics.scene;

import static bates.jamie.graphics.util.Renderer.displayTexturedCuboid;
import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_LEQUAL;
import static javax.media.opengl.GL.GL_NICEST;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL2.GL_ACCUM;
import static javax.media.opengl.GL2.GL_ACCUM_BUFFER_BIT;
import static javax.media.opengl.GL2.GL_LOAD;
import static javax.media.opengl.GL2.GL_MULT;
import static javax.media.opengl.GL2.GL_RETURN;
import static javax.media.opengl.GL2ES1.GL_EXP2;
import static javax.media.opengl.GL2ES1.GL_FOG;
import static javax.media.opengl.GL2ES1.GL_FOG_COLOR;
import static javax.media.opengl.GL2ES1.GL_FOG_DENSITY;
import static javax.media.opengl.GL2ES1.GL_FOG_END;
import static javax.media.opengl.GL2ES1.GL_FOG_HINT;
import static javax.media.opengl.GL2ES1.GL_FOG_MODE;
import static javax.media.opengl.GL2ES1.GL_FOG_START;
import static javax.media.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.gl2.GLUgl2;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bates.jamie.graphics.collision.Bound;
import bates.jamie.graphics.collision.BoundParser;
import bates.jamie.graphics.collision.OBB;
import bates.jamie.graphics.collision.Sphere;
import bates.jamie.graphics.entity.BillBoard;
import bates.jamie.graphics.entity.BlockFort;
import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.entity.GrassPatch;
import bates.jamie.graphics.entity.LightingStrike;
import bates.jamie.graphics.entity.Quadtree;
import bates.jamie.graphics.entity.SkyBox;
import bates.jamie.graphics.entity.Terrain;
import bates.jamie.graphics.entity.TerrainPatch;
import bates.jamie.graphics.entity.Water;
import bates.jamie.graphics.io.Console;
import bates.jamie.graphics.io.GamePad;
import bates.jamie.graphics.io.ModelSelecter;
import bates.jamie.graphics.item.Banana;
import bates.jamie.graphics.item.BlueShell;
import bates.jamie.graphics.item.FakeItemBox;
import bates.jamie.graphics.item.GreenShell;
import bates.jamie.graphics.item.Item;
import bates.jamie.graphics.item.ItemBox;
import bates.jamie.graphics.item.RedShell;
import bates.jamie.graphics.particle.Blizzard;
import bates.jamie.graphics.particle.Blizzard.StormType;
import bates.jamie.graphics.particle.BoostParticle;
import bates.jamie.graphics.particle.LightningParticle;
import bates.jamie.graphics.particle.Particle;
import bates.jamie.graphics.particle.ParticleGenerator;
import bates.jamie.graphics.particle.StarParticle;
import bates.jamie.graphics.scene.ShadowCaster.ShadowQuality;
import bates.jamie.graphics.sound.MP3;
import bates.jamie.graphics.util.Face;
import bates.jamie.graphics.util.Matrix;
import bates.jamie.graphics.util.OBJParser;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Renderer;
import bates.jamie.graphics.util.Shader;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.TimeQuery;
import bates.jamie.graphics.util.Vec3;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

//TODO Seidel's polygon triangulation algorithm

/**
 * @author Jamie Bates
 * @version 1.0 (22/02/2012)
 * 
 * This class creates a 3D scene which displays a car that can be moved along a
 * track by the use of hotkeys. Users can also interact with the car model and 
 * manipulation the scene in a number of ways.
 */
public class Scene implements GLEventListener, KeyListener, MouseWheelListener, MouseListener, MouseMotionListener, ActionListener, ItemListener, ListSelectionListener
{
	private static final boolean SMOOTH_FPS = true;
	
	public boolean enableNimbus = false;
	
	public JFrame frame;
	public GLCanvas canvas;
	
	private Console console;
	private JTextField command;
	
	private JMenuBar menuBar;
	
	private JMenu menu_file;
	private JMenu menu_load;
	private JMenuItem menuItem_project;
	private JMenuItem menuItem_game;
	private JMenuItem menuItem_close;
	
	private JMenu menu_camera;
	private JCheckBoxMenuItem menuItem_shaking;
	private JCheckBoxMenuItem menuItem_trackball;
	private JCheckBoxMenuItem menuItem_focal_blur;
	private JCheckBoxMenuItem menuItem_mirage;
	
	private JMenu menu_vehicle;
	private JCheckBoxMenuItem menuItem_reverse;
	private JCheckBoxMenuItem menuItem_tag;
	private JMenu menu_car_material;
	private JMenuItem menuItem_car_color;
	private JCheckBoxMenuItem menuItem_cubemap;
	
	private JMenu menu_render;
	private JCheckBoxMenuItem menuItem_shaders;
	private JMenu menu_quality;
	private JCheckBoxMenuItem menuItem_multisample;
	private JCheckBoxMenuItem menuItem_anisotropic;
	private JMenu menu_effects;
	private JCheckBoxMenuItem menuItem_motionblur;
	private JCheckBoxMenuItem menuItem_aberration;
	private JCheckBoxMenuItem menuItem_bloom;
	private JCheckBoxMenuItem menuItem_reflect;
	private JMenu menu_shadows;
	private JCheckBoxMenuItem menuItem_shadows;
	private JMenu menu_shadow_quality;
	private JRadioButtonMenuItem menuItem_shadow_poor;
	private JRadioButtonMenuItem menuItem_shadow_norm;
	private JRadioButtonMenuItem menuItem_shadow_high;
	private JRadioButtonMenuItem menuItem_shadow_best;
	
	private JMenu menu_environment;
	private JMenuItem menuItem_background;
	private JCheckBoxMenuItem menuItem_fog;
	private JMenuItem menuItem_fogcolor;
	private JMenuItem menuItem_skycolor;
	private JMenuItem menuItem_horizon;
	private JMenu menu_weather;
	private JRadioButtonMenuItem menuItem_none;
	private JRadioButtonMenuItem menuItem_rain;
	private JRadioButtonMenuItem menuItem_snow;
	private JCheckBoxMenuItem menuItem_settle;
	private JCheckBoxMenuItem menuItem_splash;
	
	private JMenu menu_light;
	private JMenuItem menuItem_ambience;
	private JMenuItem menuItem_emission;
	private JMenuItem menuItem_specular;
	private JMenuItem menuItem_diffuse;
	
	private JCheckBoxMenuItem menuItem_normalize;
	private JCheckBoxMenuItem menuItem_smooth;
	private JCheckBoxMenuItem menuItem_local;
	private JCheckBoxMenuItem menuItem_secondary;
	
	private JMenu menu_terrain;
	private JCheckBoxMenuItem menuItem_water;
	
	private JMenu menu_quadtree;
	private JMenu menu_geometry;
	private JCheckBoxMenuItem menuItem_solid;
	private JCheckBoxMenuItem menuItem_frame;
	private JMenu menu_material;
	private JCheckBoxMenuItem menuItem_texturing;
	private JCheckBoxMenuItem menuItem_malleable;
	private JCheckBoxMenuItem menuItem_bumpmaps;
	private JCheckBoxMenuItem menuItem_caustics;
	private JMenuItem menuItem_shininess;
	private JMenu menu_coloring;
	private JCheckBoxMenuItem menuItem_vcoloring;
	private JMenu menu_normals;
	private JCheckBoxMenuItem menuItem_shading;
	private JMenuItem menuItem_tangent;
	private JMenuItem menuItem_recalculate;
	private JCheckBoxMenuItem menuItem_vnormals;
	private JCheckBoxMenuItem menuItem_vtangents;
	private JMenu menu_heights;
	private JCheckBoxMenuItem menuItem_elevation;
	private JMenuItem menuItem_height;
	private JMenuItem menuItem_reset;
	
	public static int canvasWidth  = 860;
	public static int canvasHeight = 640;
	
	public float far = 2000.0f;
	
	public static final int FPS     = 60;
	public static final int MIN_FPS = 30;
	
	private final FPSAnimator animator;
	
	public float fov = 90.0f;
	
	private GLU glu;
	private GLUT glut;
	
	public float[] background = RGB.SKY_BLUE_3F;
	
	public static boolean enableAnimation = true;
	private boolean normalize = true;
	
	private Calendar execution;
	
	private int frames = 0;      // frame counter for current second
	private int frameRate = FPS; // FPS of previous second
	private long startTime;      // start time of current second
	private long renderTime;     // time at which previous frame was rendered 
	
	public static int frameIndex = 0;   //time independent frame counter (for FT graph)
	public long[] frameTimes;    //buffered frame times (for FT graph)
	public long[][] renderTimes; //buffered times for rendering each set of components
	public long[][] updateTimes; //buffered times for collision detection tests (for CT graph)
	
	public static final String[] RENDER_HEADERS =
		{"Terrain", "Foliage", "Vehicles", "Items", "Particles", "Bounds", "HUD"};
	
	public static final String[] UPDATE_HEADERS =
		{"Collision", "Weather", "Deformation", "Buffers"};
	
	public boolean enableCulling = false;
	
	
	/** Texture Fields **/
	private Texture brickWall;
	private Texture brickWallTop;
	
	private Texture cobble;
	
	
	/** Model Fields **/	
	private List<Face> floorFaces;
	private int floorList;
	
	private List<Car> cars = new ArrayList<Car>();
	public boolean enableQuality = true;
	
	public static final float[] ORIGIN = {0.0f, 0.0f, 0.0f};
	public static final float GLOBAL_RADIUS = 300;

	private List<ItemBox> itemBoxes = new ArrayList<ItemBox>();
	public boolean enableItemBoxes = false;
	
	
	/** Light Fields **/
	public Light light;
	
	public boolean enableLight = true;
	public boolean moveLight = false;
	public boolean headlight = false;
	public boolean displayLight = true;
	
	
	/** Shadow Fields **/
	public ShadowCaster caster;
	public static boolean enableShadow = true;
	public boolean shadowMap   = false;
	public boolean resetShadow = false;
	
	/** Fog Fields **/
	public boolean enableFog = true;
	public float fogDensity = 0.004f;
	public float[] fogColor = {1.0f, 1.0f, 1.0f, 1.0f};
	
	private float cloudDensity = 1.0f;
    private static final float MIN_DENSITY = 0.5f;
    private static final float DENSITY_INC = 0.0125f;
	
    
    /** Environment Fields **/
    public SkyBox skybox;
    public boolean displaySkybox = true;
    public Reflector reflector;
	public boolean sphereMap = false;
	
	/** Collision Detection Fields **/	
	private boolean enableOBBAxes       = false;
	private boolean enableOBBVertices   = false;
	private boolean enableOBBWireframes = false;
	private boolean enableOBBSolids     = false;
	private boolean enableClosestPoints = false;
	
	public boolean enableObstacles = false;
	
	private List<OBB> wallBounds;
	public BlockFort fort;
	
	
	/** Music Fields **/
	private boolean musicPlaying = false;
	private static final String MUTE_CITY =
			"file:///" + System.getProperty("user.dir") + "//music//Mute City.mp3";
	
	
	private List<Particle> particles = new ArrayList<Particle>();
	public List<ParticleGenerator> generators = new ArrayList<ParticleGenerator>();

	
	private boolean enableItems = true;
	private Queue<Integer> itemQueue = new ArrayBlockingQueue<Integer>(100);
	private List<Item> itemList = new ArrayList<Item>();
	
	
	private int boostCounter = 0;
	public boolean enableBlur = true;
	
	
	public boolean enableTerrain = true;
	
	private Terrain terrain;
	private TerrainPatch[] terrainPatches;
	public List<BillBoard> foliage;
	public GrassPatch grassPatch;
	
	public String terrainCommand = "";
	public static final String DEFAULT_TERRAIN = "128 1000 20 6 18 0.125 1.0";
	
	public boolean enableReflection = false;
	public float opacity = 0.50f;
	
	public boolean smoothBound = false;
	public boolean multisample = true;
	
	public boolean testMode = false;
	public boolean printVersion = true;
	public boolean printErrors = false;
	
	public ModelSelecter selecter;
	
	public boolean mousePressed  = false;
	public boolean enableRetical = true;
	public float retical = 50;
	
	public float[] quadratic;
	
	public boolean enableLightning = false;
	public Blizzard blizzard;
	public int flakeLimit = 10000;
	public boolean enableBlizzard = false;
	
	public JList<String> quadList;
	public DefaultListModel<String> listModel;

	public Water water;
	public BloomStrobe bloom;
	public FocalBlur focalBlur;
	
	public boolean enableBloom = false;
	public static boolean enableParallax = true;
	public static boolean enableFocalBlur = true;
	
	private LightingStrike[] bolts;
	
	public Scene()
	{
		try
		{	
			if(enableNimbus)
			{
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
				{
			        if ("Nimbus".equals(info.getName()))
			        {
			            UIManager.setLookAndFeel(info.getClassName());
			            break;
			        }
				}
			}
			else UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) { e.printStackTrace(); }
		
		frame = new JFrame();
		
		GLCapabilities capabilities = new GLCapabilities(GLProfile.getDefault());
		capabilities.setStencilBits(8);
		capabilities.setDoubleBuffered(true);
		capabilities.setSampleBuffers(true);
		
		canvas = new GLCanvas(capabilities);
		canvas.setPreferredSize(new Dimension(canvasWidth, canvasHeight));
		canvas.addGLEventListener(this);
		canvas.addKeyListener(this);
		canvas.addMouseWheelListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.setFocusable(true);
		canvas.requestFocus();

		animator = new FPSAnimator(canvas, FPS, !SMOOTH_FPS);
		
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
		
		// ensure that menu items are not displayed above the GL canvas
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		
		setupMenu();
		
		frame.setJMenuBar(menuBar);
		
		console = new Console(this);
		
		command = new JTextField();
		command.addActionListener(this);
		command.setActionCommand("console");
		
		command.setBackground(Color.DARK_GRAY);
		command.setForeground(Color.LIGHT_GRAY);
		command.setCaretColor(Color.WHITE);
		
		listModel = new DefaultListModel<String>();
		
		quadList = new JList<String>(listModel);
		quadList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		quadList.addListSelectionListener(this);
        
		JScrollPane pane = new JScrollPane(quadList);
		
		Container content = frame.getContentPane();
		
		content.setLayout(new BorderLayout());
		
		content.add(pane,    BorderLayout.EAST  );
		content.add(canvas,  BorderLayout.CENTER);
		content.add(command, BorderLayout.SOUTH );

		frame.pack();
		frame.setTitle("OpenGL Project Demo");
		frame.setVisible(true);
		
		animator.start();
	}

	private void setupMenu()
	{
		menuBar = new JMenuBar();
		
		/** File Menu **/
		menu_file = new JMenu("File");
		menu_file.setMnemonic(KeyEvent.VK_F);

		menu_load = new JMenu("Open");
		menu_load.setMnemonic(KeyEvent.VK_O);
		
		menuItem_project = new JMenuItem("Project", KeyEvent.VK_P);
		menuItem_project.addActionListener(this);
		menuItem_project.setActionCommand("load_project");
		
		menuItem_game = new JMenuItem("Game", KeyEvent.VK_G);
		menuItem_game.addActionListener(this);
		menuItem_game.setActionCommand("load_game");
		
		menuItem_close = new JMenuItem("Close", KeyEvent.VK_C);
		menuItem_close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		menuItem_close.addActionListener(this);
		menuItem_close.setActionCommand("close");
		
		menu_load.add(menuItem_project);
		menu_load.add(menuItem_game);
		
		menu_file.add(menu_load);
		menu_file.addSeparator();
		menu_file.add(menuItem_close);
		
		menuBar.add(menu_file);
		/**----------------**/
		
		/** Camera Menu **/
		menu_camera = new JMenu("Camera");
		menu_camera.setMnemonic(KeyEvent.VK_C);
		
		menuItem_shaking = new JCheckBoxMenuItem("Camera Shake");
		menuItem_shaking.addItemListener(this);
		menuItem_shaking.setMnemonic(KeyEvent.VK_S);
		
		menuItem_trackball = new JCheckBoxMenuItem("Enable Trackball");
		menuItem_trackball.addItemListener(this);
		menuItem_trackball.setMnemonic(KeyEvent.VK_T);
		
		menuItem_focal_blur = new JCheckBoxMenuItem("Focal Blur");
		menuItem_focal_blur.addItemListener(this);
		menuItem_focal_blur.setMnemonic(KeyEvent.VK_B);
		
		menuItem_mirage = new JCheckBoxMenuItem("Heat Haze");
		menuItem_mirage.addItemListener(this);
		menuItem_mirage.setMnemonic(KeyEvent.VK_H);
		
		menu_camera.add(menuItem_shaking);
		menu_camera.add(menuItem_trackball);
		menu_camera.add(menuItem_focal_blur);
		menu_camera.add(menuItem_mirage);
		
		menuBar.add(menu_camera);
		/**----------------**/
		
		/** Controls Menu **/
		menu_vehicle = new JMenu("Vehicle");
		menu_vehicle.setMnemonic(KeyEvent.VK_V);
		
		menuItem_reverse = new JCheckBoxMenuItem("Invert Reverse");
		menuItem_reverse.addItemListener(this);
		menuItem_reverse.setMnemonic(KeyEvent.VK_I);
		menuItem_reverse.setSelected(false);
		
		menu_vehicle.add(menuItem_reverse);
		menu_vehicle.addSeparator();
		
		menuItem_tag = new JCheckBoxMenuItem("Show Label");
		menuItem_tag.addItemListener(this);
		menuItem_tag.setMnemonic(KeyEvent.VK_L);
		menuItem_tag.setSelected(false);
		
		menu_vehicle.add(menuItem_tag);
		
		menu_car_material = new JMenu("Material");
		menu_car_material.setMnemonic(KeyEvent.VK_M); 
		
		menu_vehicle.add(menu_car_material);
		
		menuItem_car_color = new JMenuItem("Body Color", KeyEvent.VK_B);
		menuItem_car_color.addActionListener(this);
		menuItem_car_color.setActionCommand("car_color");
		
		menuItem_cubemap = new JCheckBoxMenuItem("Enable Chrome");
		menuItem_cubemap.addItemListener(this);
		menuItem_cubemap.setMnemonic(KeyEvent.VK_C);
		
		menu_car_material.add(menuItem_car_color);
		menu_car_material.add(menuItem_cubemap);
		
		menuBar.add(menu_vehicle);
		/**----------------**/
		
		/** Render Menu **/
		menu_render = new JMenu("Render");
		menu_render.setMnemonic(KeyEvent.VK_R);
		
		menuItem_shaders = new JCheckBoxMenuItem("Enable Shaders");
		menuItem_shaders.addItemListener(this);
		menuItem_shaders.setMnemonic(KeyEvent.VK_S);
		menuItem_shaders.setSelected(Shader.enabled);
		
		menu_render.add(menuItem_shaders);
		
		menu_quality = new JMenu("Quality");
		menu_quality.setMnemonic(KeyEvent.VK_Q);
		
		menuItem_multisample = new JCheckBoxMenuItem("Multisample");
		menuItem_multisample.addItemListener(this);
		menuItem_multisample.setMnemonic(KeyEvent.VK_M);
		menuItem_multisample.setSelected(multisample);
		
		menuItem_anisotropic = new JCheckBoxMenuItem("Anisotropic Filter");
		menuItem_anisotropic.addItemListener(this);
		menuItem_anisotropic.setMnemonic(KeyEvent.VK_A);
		menuItem_anisotropic.setSelected(Renderer.anisotropic);
		
		menu_quality.add(menuItem_multisample);
		menu_quality.add(menuItem_anisotropic);
		
		menu_render.add(menu_quality);
		
		menu_effects = new JMenu("Effects");
		menu_effects.setMnemonic(KeyEvent.VK_E);
		
		menuItem_motionblur = new JCheckBoxMenuItem("Motion Blur");
		menuItem_motionblur.addItemListener(this);
		menuItem_motionblur.setMnemonic(KeyEvent.VK_M);
		menuItem_motionblur.setSelected(enableBlur);
		
		menuItem_aberration = new JCheckBoxMenuItem("Chromatic Aberration");
		menuItem_aberration.addItemListener(this);
		menuItem_aberration.setMnemonic(KeyEvent.VK_A);
		
		menuItem_bloom = new JCheckBoxMenuItem("HDR Bloom");
		menuItem_bloom.addItemListener(this);
		menuItem_bloom.setMnemonic(KeyEvent.VK_B);
		
		menu_effects.add(menuItem_motionblur);
		menu_effects.add(menuItem_aberration);
		menu_effects.add(menuItem_bloom);
		
		menu_render.add(menu_effects);
		
		menuItem_reflect = new JCheckBoxMenuItem("Reflection");
		menuItem_reflect.addItemListener(this);
		menuItem_reflect.setMnemonic(KeyEvent.VK_R);
		menuItem_reflect.setSelected(enableReflection);
		
		menu_render.addSeparator();
		menu_render.add(menuItem_reflect);
		
		menu_shadows = new JMenu("Shadows");
		
		menuItem_shadows = new JCheckBoxMenuItem("Enable Shadows");
		menuItem_shadows.addItemListener(this);
		menuItem_shadows.setMnemonic(KeyEvent.VK_S);
		menuItem_shadows.setSelected(enableShadow);
		
		menu_shadows.add(menuItem_shadows);
		menu_shadows.addSeparator();
		
		menu_shadow_quality = new JMenu("Quality");
		menu_shadow_quality.setMnemonic(KeyEvent.VK_Q);
		
		ButtonGroup group_shadow = new ButtonGroup();
		
		menuItem_shadow_poor = new JRadioButtonMenuItem("Low");
		menuItem_shadow_poor.addActionListener(this);
		menuItem_shadow_poor.setSelected(false);
		menuItem_shadow_poor.setMnemonic(KeyEvent.VK_L);
		menuItem_shadow_poor.setActionCommand("shadow_low");

		menuItem_shadow_norm = new JRadioButtonMenuItem("Medium");
		menuItem_shadow_norm.addActionListener(this);
		menuItem_shadow_norm.setSelected(false);
		menuItem_shadow_norm.setMnemonic(KeyEvent.VK_M);
		menuItem_shadow_norm.setActionCommand("shadow_medium");
		
		menuItem_shadow_high = new JRadioButtonMenuItem("High");
		menuItem_shadow_high.addActionListener(this);
		menuItem_shadow_high.setSelected(false);
		menuItem_shadow_high.setMnemonic(KeyEvent.VK_H);
		menuItem_shadow_high.setActionCommand("shadow_high");
		
		menuItem_shadow_best = new JRadioButtonMenuItem("Best");
		menuItem_shadow_best.addActionListener(this);
		menuItem_shadow_best.setSelected(true);
		menuItem_shadow_best.setMnemonic(KeyEvent.VK_B);
		menuItem_shadow_best.setActionCommand("shadow_best");
		
		group_shadow.add(menuItem_shadow_poor);
		group_shadow.add(menuItem_shadow_norm);
		group_shadow.add(menuItem_shadow_high);
		group_shadow.add(menuItem_shadow_best);
		
		menu_shadow_quality.add(menuItem_shadow_poor);
		menu_shadow_quality.add(menuItem_shadow_norm);
		menu_shadow_quality.add(menuItem_shadow_high);
		menu_shadow_quality.add(menuItem_shadow_best);
		
		menu_shadows.add(menu_shadow_quality);
		
		menu_render.add(menu_shadows);
		
		menuBar.add(menu_render);
		/**-------------------**/
		
		/** Environment Menu **/
		menu_environment = new JMenu("Environment");
		menu_environment.setMnemonic(KeyEvent.VK_E);
		
		menuItem_background = new JMenuItem("Background Color", KeyEvent.VK_B);
		menuItem_background.addActionListener(this);
		menuItem_background.setActionCommand("bg_color");
		
		menuItem_fog = new JCheckBoxMenuItem("Fog");
		menuItem_fog.addItemListener(this);
		menuItem_fog.setMnemonic(KeyEvent.VK_F);
		menuItem_fog.setSelected(enableFog);
		
		menuItem_fogcolor = new JMenuItem("Fog Color", KeyEvent.VK_C);
		menuItem_fogcolor.addActionListener(this);
		menuItem_fogcolor.setActionCommand("fog_color");
		
		menuItem_skycolor = new JMenuItem("Sky Color", KeyEvent.VK_S);
		menuItem_skycolor.addActionListener(this);
		menuItem_skycolor.setActionCommand("sky_color");
		
		menuItem_horizon = new JMenuItem("Horizon Color", KeyEvent.VK_H);
		menuItem_horizon.addActionListener(this);
		menuItem_horizon.setActionCommand("horizon");
		
		menu_weather = new JMenu("Weather");
		menu_weather.setMnemonic(KeyEvent.VK_W);
		
		ButtonGroup group_weather = new ButtonGroup();
		
		menuItem_none = new JRadioButtonMenuItem("None");
		menuItem_none.addActionListener(this);
		menuItem_none.setSelected(true);
		menuItem_none.setMnemonic(KeyEvent.VK_N);
		menuItem_none.setActionCommand("no_weather");

		menuItem_snow = new JRadioButtonMenuItem("Snow");
		menuItem_snow.addActionListener(this);
		menuItem_snow.setSelected(false);
		menuItem_snow.setMnemonic(KeyEvent.VK_S);
		menuItem_snow.setActionCommand("snow");
		
		menuItem_rain = new JRadioButtonMenuItem("Rain");
		menuItem_rain.addActionListener(this);
		menuItem_rain.setSelected(false);
		menuItem_rain.setMnemonic(KeyEvent.VK_R);
		menuItem_rain.setActionCommand("rain");
		
		group_weather.add(menuItem_none);
		group_weather.add(menuItem_snow);
		group_weather.add(menuItem_rain);
		
		menuItem_settle = new JCheckBoxMenuItem("Snow Settles");
		menuItem_settle.addItemListener(this);
		
		menuItem_splash = new JCheckBoxMenuItem("Rain Splashes");
		menuItem_splash.addItemListener(this);
		
		menu_weather.add(menuItem_none);
		menu_weather.add(menuItem_snow);
		menu_weather.add(menuItem_rain);
		menu_weather.addSeparator();
		menu_weather.add(menuItem_settle);
		menu_weather.add(menuItem_splash);
		
		menu_environment.add(menuItem_background);
		menu_environment.addSeparator();
		menu_environment.add(menuItem_fog);
		menu_environment.add(menuItem_fogcolor);
		menu_environment.add(menuItem_skycolor);
		menu_environment.add(menuItem_horizon);
		menu_environment.addSeparator();
		menu_environment.add(menu_weather);
		
		menuBar.add(menu_environment);
		/**-------------------**/
		
		/** Light Menu **/
		menu_light = new JMenu("Light");
		menu_light.setMnemonic(KeyEvent.VK_L);
		
		menuItem_ambience = new JMenuItem("Set Ambience", KeyEvent.VK_A);
		menuItem_ambience.addActionListener(this);
		menuItem_ambience.setActionCommand("set_ambience");
		
		menuItem_emission = new JMenuItem("Set Emission", KeyEvent.VK_E);
		menuItem_emission.addActionListener(this);
		menuItem_emission.setActionCommand("set_emission");
		
		menuItem_specular = new JMenuItem("Set Specular", KeyEvent.VK_C);
		menuItem_specular.addActionListener(this);
		menuItem_specular.setActionCommand("set_specular");
		
		menuItem_diffuse = new JMenuItem("Set Diffuse", KeyEvent.VK_D);
		menuItem_diffuse.addActionListener(this);
		menuItem_diffuse.setActionCommand("set_diffuse");
		
		menuItem_normalize = new JCheckBoxMenuItem("Normalize");
		menuItem_normalize.addItemListener(this);
		menuItem_normalize.setMnemonic(KeyEvent.VK_N);
		menuItem_normalize.setSelected(normalize);
		
		menuItem_smooth = new JCheckBoxMenuItem("Smooth");
		menuItem_smooth.addItemListener(this);
		menuItem_smooth.setMnemonic(KeyEvent.VK_S);
		
		menuItem_local = new JCheckBoxMenuItem("Local Specularity");
		menuItem_local.addItemListener(this);
		menuItem_local.setMnemonic(KeyEvent.VK_L);
		
		menuItem_secondary = new JCheckBoxMenuItem("Specular Texture");
		menuItem_secondary.addItemListener(this);
		
		menu_light.add(menuItem_ambience);
		menu_light.add(menuItem_emission);
		menu_light.add(menuItem_specular);
		menu_light.add(menuItem_diffuse);
		menu_light.addSeparator();
		menu_light.add(menuItem_normalize);
		menu_light.add(menuItem_smooth);
		menu_light.add(menuItem_local);
		menu_light.add(menuItem_secondary);
		
		menuBar.add(menu_light);
		/**------------------**/
		
		/** Terrain Menu **/
		menu_terrain = new JMenu("Terrain");
		menu_terrain.setMnemonic(KeyEvent.VK_T);
		
		menuItem_water = new JCheckBoxMenuItem("Display Water");
		menuItem_water.addItemListener(this);
		menuItem_water.setMnemonic(KeyEvent.VK_W);
		menuItem_water.setSelected(false);

		menu_terrain.add(menuItem_water);
		
		menuBar.add(menu_terrain);
		/**------------------**/
		
		/** Quadtree Menu **/
		menu_quadtree = new JMenu("Quadtree");
		menu_quadtree.setMnemonic(KeyEvent.VK_Q);
		
		menu_geometry = new JMenu("Geometry");
		menu_geometry.setMnemonic(KeyEvent.VK_G);	
		
		menuItem_solid = new JCheckBoxMenuItem("Show Geometry");
		menuItem_solid.addItemListener(this);
		menuItem_solid.setMnemonic(KeyEvent.VK_G);
		
		menuItem_frame = new JCheckBoxMenuItem("Show Wireframe");
		menuItem_frame.addItemListener(this);
		menuItem_frame.setMnemonic(KeyEvent.VK_W);
		
		menu_geometry.add(menuItem_solid);
		menu_geometry.add(menuItem_frame);
		
		menu_material = new JMenu("Material");
		menu_material.setMnemonic(KeyEvent.VK_M);
		
		menuItem_texturing = new JCheckBoxMenuItem("Enable Texture");
		menuItem_texturing.addItemListener(this);
		menuItem_texturing.setMnemonic(KeyEvent.VK_T);
		
		menuItem_bumpmaps = new JCheckBoxMenuItem("Bump Mapping");
		menuItem_bumpmaps.addItemListener(this);
		menuItem_bumpmaps.setMnemonic(KeyEvent.VK_B);
		
		menuItem_caustics = new JCheckBoxMenuItem("Water Caustics");
		menuItem_caustics.addItemListener(this);
		menuItem_caustics.setMnemonic(KeyEvent.VK_C);
		
		menuItem_malleable = new JCheckBoxMenuItem("Malleable");
		menuItem_malleable.addItemListener(this);
		menuItem_malleable.setMnemonic(KeyEvent.VK_M);
		
		menuItem_shininess = new JMenuItem("Set Specular", KeyEvent.VK_S);
		menuItem_shininess.addActionListener(this);
		menuItem_shininess.setActionCommand("material_specular");
		
		menu_material.add(menuItem_texturing);
		menu_material.add(menuItem_bumpmaps );
		menu_material.add(menuItem_caustics );
		menu_material.add(menuItem_malleable);
		menu_material.add(menuItem_shininess);
		
		menu_normals = new JMenu("Normals");
		menu_normals.setMnemonic(KeyEvent.VK_N);
		
		menuItem_shading = new JCheckBoxMenuItem("Enable Shading");
		menuItem_shading.addItemListener(this);
		menuItem_shading.setMnemonic(KeyEvent.VK_S);
		
		menuItem_vnormals = new JCheckBoxMenuItem("Show Vertex Normals");
		menuItem_vnormals.addItemListener(this);
		menuItem_vnormals.setMnemonic(KeyEvent.VK_V);
		
		menuItem_vtangents = new JCheckBoxMenuItem("Show Vertex Tangents");
		menuItem_vtangents.addItemListener(this);
		
		menuItem_recalculate = new JMenuItem("Recalculate Normals", KeyEvent.VK_R);
		menuItem_recalculate.addActionListener(this);
		menuItem_recalculate.setActionCommand("recalc_norms");
		
		menuItem_tangent = new JMenuItem("Recalculate Tangents", KeyEvent.VK_T);
		menuItem_tangent.addActionListener(this);
		menuItem_tangent.setActionCommand("recalc_tangs");
		
		menu_normals.add(menuItem_vnormals );
		menu_normals.add(menuItem_vtangents);
		menu_normals.addSeparator();
		menu_normals.add(menuItem_shading);
		menu_normals.addSeparator();
		menu_normals.add(menuItem_recalculate);
		menu_normals.add(menuItem_tangent);
		
		menu_coloring = new JMenu("Coloring");
		menu_coloring.setMnemonic(KeyEvent.VK_C);
		
		menuItem_vcoloring = new JCheckBoxMenuItem("Color Vertices");
		menuItem_vcoloring.addItemListener(this);
		menuItem_vcoloring.setMnemonic(KeyEvent.VK_C);
		
		menu_coloring.add(menuItem_vcoloring);
		
		menu_heights = new JMenu("Heights");
		menu_heights.setMnemonic(KeyEvent.VK_H);
		
		menuItem_elevation = new JCheckBoxMenuItem("Show Elevation");
		menuItem_elevation.addItemListener(this);
		menuItem_elevation.setMnemonic(KeyEvent.VK_E);
		
		menuItem_height = new JMenuItem("Save Heights", KeyEvent.VK_H);
		menuItem_height.addActionListener(this);
		menuItem_height.setActionCommand("save_heights");
		
		menuItem_reset = new JMenuItem("Reset Heights", KeyEvent.VK_R);
		menuItem_reset.addActionListener(this);
		menuItem_reset.setActionCommand("reset_heights");
		
		menu_heights.add(menuItem_elevation);
		menu_heights.addSeparator();
		menu_heights.add(menuItem_height);
		menu_heights.add(menuItem_reset);
		
		menu_quadtree.add(menu_geometry);
		menu_quadtree.add(menu_material);
		menu_quadtree.add(menu_normals);
		menu_quadtree.add(menu_coloring);
		menu_quadtree.add(menu_heights);
		
		menuBar.add(menu_quadtree);
		/**------------------**/
	}
	
	public static void main(String[] args) { new Scene(); }
	
	public int getFrameRate() { return frameRate; }
	
	public int getHeight() { return canvasHeight; }
	
	public int getWidth()  { return canvasWidth;  }
	
	private void printVersion(GL2 gl)
	{
		System.out.println();
		
		System.out.println("OpenGL Version: " + gl.glGetString(GL2.GL_VERSION));
	    System.out.println("OpenGL Vendor : " + gl.glGetString(GL2.GL_VENDOR) + "\n");
	    
	    System.out.println("GL Shading Language Version: " + gl.glGetString(GL2.GL_SHADING_LANGUAGE_VERSION) + "\n");
	    
	    String extensions = gl.glGetString(GL2.GL_EXTENSIONS);
	    
	    System.out.println("Multitexture Support: " + extensions.contains("GL_ARB_multitexture"));
	    
	    int[] textureUnits = new int[3];
	    gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_UNITS, textureUnits, 0);
	    gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_IMAGE_UNITS, textureUnits, 1);
	    gl.glGetIntegerv(GL2.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, textureUnits, 2);
	    System.out.println("Number of Texture Units: " + textureUnits[0]);
	    System.out.println("Number of Texture Image Units: " + textureUnits[1]);
	    System.out.println("Number of Vertex Texture Image Units: " + textureUnits[2] + "\n");
	    
	    boolean anisotropic = gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic");
	    System.out.println("Anisotropic Filtering Support: " + anisotropic);
	    
	    if(anisotropic)
	    {
		    float[] anisotropy = new float[1];
		    gl.glGetFloatv(GL2.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, anisotropy, 0);
		    System.out.println("Maximum Anisotropy: " + anisotropy[0] + "\n");
	    } 
	    
	    float[] data = new float[3];
	    
	    gl.glGetFloatv(GL2.GL_POINT_SIZE_RANGE, data, 0);
	    gl.glGetFloatv(GL2.GL_POINT_SIZE_GRANULARITY, data, 2);
	    
	    System.out.println("Point Size Range: " + data[0] + " -> " + data[1]);
	    System.out.println("Point Size Granularity: " + data[2] + "\n");
	    
	    gl.glGetFloatv(GL2.GL_ALIASED_POINT_SIZE_RANGE, data, 0);
	    System.out.println("Aliased Point Size Range: " + data[0] + " -> " + data[1] + "\n");
	    
	    gl.glGetFloatv(GL2.GL_LINE_WIDTH_RANGE, data, 0);
	    gl.glGetFloatv(GL2.GL_LINE_WIDTH_GRANULARITY, data, 2);
	    
	    System.out.println("Line Width Range: " + data[0] + " -> " + data[1]);
	    System.out.println("Line Width Granularity: " + data[2] + "\n");
	    
	    gl.glGetFloatv(GL2.GL_MAX_VERTEX_UNIFORM_COMPONENTS, data, 0);
	    System.out.println("Vertex Uniform Components: " + data[0] + "\n");
	}

	public void printErrors(GL2 gl)
	{
		int error = gl.glGetError();
		
		while(error != GL2.GL_NO_ERROR)
		{
			System.out.println("OpenGL Error: " + glu.gluErrorString(error));
			error = gl.glGetError();
		}
	}

	public void init(GLAutoDrawable drawable)
	{
		execution = Calendar.getInstance();
		
		GL2 gl = drawable.getGL().getGL2();
		
		glu = new GLUgl2();
		glut = new GLUT();
		
		loadTextures(gl);
		Shader.loadShaders(gl);
		
		// may provide extra speed depending on machine
		gl.setSwapInterval(0);
		
		gl.glClearStencil(0);
		gl.glEnable(GL2.GL_STENCIL_TEST);
		
		quadratic = new float[] {0.0f, 0.0f, 0.005f};
		
		gl.glPointSize(3);
		gl.glPointParameterfv(GL2.GL_POINT_DISTANCE_ATTENUATION, quadratic, 0);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

		/** Texture Options **/
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_BASE_LEVEL, 0);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MAX_LEVEL,  4);
		
		// For toon shading
		gl.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_WRAP_S,     GL2.GL_CLAMP);

		/** Fog Setup **/
		gl.glEnable(GL_FOG);
		gl.glFogi  (GL_FOG_MODE, GL_EXP2);
		gl.glFogfv (GL_FOG_COLOR, fogColor, 0);
		gl.glFogf  (GL_FOG_DENSITY, fogDensity);
		gl.glFogf  (GL_FOG_START, 100.0f);
		gl.glFogf  (GL_FOG_END, 250.0f);
		gl.glHint  (GL_FOG_HINT, GL_NICEST);
		
		/** Lighting Setup **/	
	    light = new Light(gl);
	    menuItem_smooth.setSelected(light.smooth);
	    menuItem_secondary.setSelected(light.secondary);
	    
	    caster = new ShadowCaster(this, light);
	    
	    reflector = new Reflector(this, 0.75f);
	    reflector.setup(gl);
	    
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		long setupStart = System.currentTimeMillis();
		
		/** Model Setup **/
		skybox = new SkyBox(gl);
		
		floorFaces = OBJParser.parseTriangles("floor");
	    
	    floorList = gl.glGenLists(1);
	    gl.glNewList(floorList, GL2.GL_COMPILE);
	    Renderer.displayTexturedObject(gl, floorFaces);
	    gl.glEndList();
	    
	    if(enableItems) loadItems(gl);
	    loadParticles();
	    
	    setupGenerators();
	    
	    loadPlayers(gl);
		    
	    itemBoxes.addAll(ItemBox.generateDiamond( 56.25f, 30f, particles));
	    itemBoxes.addAll(ItemBox.generateSquare (101.25f, 60f, particles));
	    itemBoxes.addAll(ItemBox.generateSquare (123.75f, 30f, particles));
	    itemBoxes.addAll(ItemBox.generateDiamond(   180f,  0f, particles));
	    
	    fort = new BlockFort(gl);
	    
	    if(enableTerrain)
	    {
	    	generateTerrain(gl, DEFAULT_TERRAIN);
	    	setCheckBoxes();
	    	
	    	Set<String> trees = terrain.trees.keySet();
		    
		    for(String tree : trees)
				listModel.addElement(tree);
		    
		    grassPatch = new GrassPatch(gl, terrain.trees.get("Base"), ORIGIN, 128, 1.0f);
	    }
	    
	    if(enableShadow) caster.setup(gl);
	    
	    wallBounds = BoundParser.parseOBBs("bound/environment.bound");
	    
	    selecter = new ModelSelecter(this, glu);
	    
	    bloom = new BloomStrobe(gl, this);
	    
	    focalBlur = new FocalBlur(this);
	    
	    water = new Water(this);
	    water.createTextures(gl);
	    
	    bolts = new LightingStrike[3];
	    bolts[0] = new LightingStrike(new float[] {0, 50, 0}, new float[] {0, 5, 0});
	    bolts[1] = new LightingStrike(new float[] {0, 50, 0}, new float[] {0, 5, 0});
	    bolts[2] = new LightingStrike(new float[] {0, 50, 0}, new float[] {0, 5, 0});
	    
	    frameTimes  = new long[240];
	    renderTimes = new long[240][RENDER_HEADERS.length];
	    updateTimes = new long[240][UPDATE_HEADERS.length];
	    
	    focalBlur.setup(gl);
	    
	    if(printVersion) printVersion(gl);
	    
	    console.parseCommand("profile project");
	    
	    long setupEnd = System.currentTimeMillis();
	    System.out.println("\nSetup Time: " + (setupEnd - setupStart) + " ms" + "\n");
	    
	    startTime = System.currentTimeMillis();
	    //records the time prior to the rendering of the first frame after initialization
	}
	
	private void setCheckBoxes()
	{
		if(terrain == null) return;
		
		menuItem_solid.setSelected(terrain.tree.solid);
		menuItem_frame.setSelected(terrain.tree.frame);
		
		menuItem_elevation.setSelected(terrain.tree.reliefMap);
		menuItem_vnormals .setSelected(terrain.tree.vNormals );
		menuItem_vtangents.setSelected(terrain.tree.vTangents);
		
		menuItem_shading.setSelected(terrain.tree.enableShading);
		
		menuItem_vcoloring.setSelected(terrain.tree.enableColoring);
		menuItem_texturing.setSelected(terrain.tree.enableTexture );
		menuItem_bumpmaps .setSelected(terrain.tree.enableBumpmap );
		menuItem_caustics .setSelected(terrain.tree.enableCaustic );
		menuItem_malleable.setSelected(terrain.tree.malleable     );
		
		menuItem_bloom.setSelected(enableBloom);
		menuItem_focal_blur.setSelected(enableFocalBlur);
		if(focalBlur != null) menuItem_mirage.setSelected(focalBlur.enableMirage);
		
		Car car = cars.get(0);
		
		menuItem_cubemap.setSelected(car.enableChrome);
		menuItem_aberration.setSelected(car.enableAberration);
		
	}

	private void setupGenerators()
	{
//		generators.add(new ParticleGenerator(1, 10, ParticleGenerator.GeneratorType.SPARK, new float[] {0, 30, 0}));
	}

	private void loadPlayers(GL2 gl)
	{
		cars.add(new Car(gl, new Vec3( 78.75f, 1.8f, 0), 0,   0, 0, this));
	    
	    if(GamePad.numberOfGamepads() > 1)
	    	cars.add(new Car(gl, new Vec3( -78.75f, 1.8f, 0), 0, 180, 0, this));
	    
	    if(GamePad.numberOfGamepads() > 2)
	    	cars.add(new Car(gl, new Vec3( 0, 1.8f,  78.75f), 0, 270, 0, this));
	    	
	    if(GamePad.numberOfGamepads() > 3)
		    cars.add(new Car(gl, new Vec3( 0, 1.8f, -78.75f), 0,  90, 0, this));
	}

	private void loadTextures(GL2 gl)
	{
		try
		{			
			brickWall    = TextureLoader.load(gl, "tex/longBrick.jpg");
			
			brickWallTop = TextureIO.newTexture(new File("tex/longBrickTop.jpg"), false);
			cobble       = TextureIO.newTexture(new File("tex/cobbles.jpg"     ), true );
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public static float attenuation = 0.1f;

	private void loadParticles()
	{
		new BoostParticle(new Vec3(), null, 0, 0, 0, false, false);
	    new LightningParticle(new Vec3());
	    new StarParticle(new Vec3(), null, 0, 0);
	}

	private void loadItems(GL2 gl)
	{
		new GreenShell (gl, this, null, 0, false);
	    new RedShell   (gl, this, null, 0, false); 
	    new BlueShell  (gl, this, null, 0);
	    new FakeItemBox(gl, this, null);
	    new Banana     (gl, this, null, 0);
	}

	public void display(GLAutoDrawable drawable)
	{	
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glClearDepth(1.0f);
		gl.glClearColor(background[0], background[1], background[2], 0.0f);
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL2.GL_STENCIL_BUFFER_BIT);
		
		resetView(gl);
		
		if(multisample) gl.glEnable(GL2.GL_MULTISAMPLE);
		else gl.glDisable(GL2.GL_MULTISAMPLE);
		
		if(normalize) gl.glEnable(GL2.GL_NORMALIZE);
		else gl.glDisable(GL2.GL_NORMALIZE);
		
		gl.glPointParameterfv(GL2.GL_POINT_DISTANCE_ATTENUATION, quadratic, 0);
		
		setupFog(gl);
		
		registerItems(gl);
		
		if(enableAnimation) update(gl);
		else cars.get(0).updateController();
		
		if(enableTerrain && !terrainCommand.equals(""))
		{	
			generateTerrain(gl, terrainCommand);	
			terrainCommand = "";
		}
		
		renderTime = System.currentTimeMillis();
		
		caster.disable(gl); // prevent reflections from being darkened by shadow
		reflector.update(gl, cars.get(0).getPosition());
		
		     if(sphereMap) reflector.displayMap(gl);
//		else if(shadowMap) displayMap(gl, bloom.getTexture(texture), -1.0f, -1.0f, 1.0f, 1.0f);
		else render(gl);

		gl.glFlush();
		
		if(printErrors) printErrors(gl);
		
		calculateFPS();

		if(enableShadow) caster.update(gl);
	}
	
	private int texture = 0;
	
	public void displayMap(GL2 gl, int texture, float x, float y, float w, float h)
    {
        gl.glMatrixMode(GL_PROJECTION); gl.glLoadIdentity();
        gl.glMatrixMode(GL_MODELVIEW ); gl.glLoadIdentity();
        
        gl.glEnable(GL2.GL_BLEND);
        gl.glDisable(GL2.GL_DEPTH_TEST);
        
        gl.glMatrixMode(GL2.GL_TEXTURE);
        gl.glPushMatrix();
        {
	        gl.glLoadIdentity();
	           
	        gl.glEnable(GL_TEXTURE_2D);
	        gl.glBindTexture(GL_TEXTURE_2D, texture);
	        gl.glDisable(GL2.GL_LIGHTING);
	        
	        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
	        gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_COMPARE_MODE, GL2.GL_NONE);
	        
	        // Show the shadow map at its actual size relative to window
	        gl.glBegin(GL2.GL_QUADS);
	        {
	            gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(x + 0, y + 0);
	            gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(x + w, y + 0);
	            gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(x + w, y + h);
	            gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(x + 0, y + h);
	        }
	        gl.glEnd();
	        
	        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
	        gl.glEnable(GL2.GL_LIGHTING);
        }
        gl.glPopMatrix();
        
        gl.glDisable(GL2.GL_BLEND);
        gl.glEnable(GL2.GL_DEPTH_TEST);
		
        resetView(gl);
    }
	
	public static boolean enableOcclusion = true;

	private void render(GL2 gl)
	{	
		TimeQuery.resetCache();
		
		int[] order = new int[cars.size()];
		
		int i = orderRender(order);
		int _i = i; //temporary variable _i used to store the boost count this frame
		
		if(mousePressed) selecter.selectModel(gl);

		for(int index : order)
		{
			Car car = cars.get(index);
			
			setupViewport(gl, index);
			car.setupCamera(gl, glu);
			
			light.setup(gl, headlight);
				
			if(headlight)
			{
				Vec3[] vectors = car.getLightVectors();
					
				light.direction = vectors[1];
				light.setPosition(vectors[0]);
			}
			
			if(enableShadow && Shader.enabled)
			{
				caster.displayShadow(gl);
				
				if(resetShadow) // required if shadow quality is altered
				{
					caster.setup(gl);
					focalBlur.setup(gl);
					resetShadow = false;
				}
			}
			
			if(enableFocalBlur)
			{
				focalBlur.display(gl);
				focalBlur.update(gl);
			}
			
			bananasRendered = 0;
			
//			if(enableBloom) bloom.render(gl);
//			else
			{
				if(enableReflection) displayReflection(gl, car);
				
				if(terrain != null && terrain.enableWater) renderWater(gl, car);
				
				renderWorld(gl);
				render3DModels(gl, car);
				
				renderTimes[frameIndex][4] = renderParticles(gl, car);
				Particle.resetTexture();
				
				if(enableTerrain) renderTimes[frameIndex][1] = renderFoliage(gl, car);
				else renderTimes[frameIndex][1] = 0;
				
				if(terrain != null && terrain.enableWater) 
				{
					water.setRefraction(gl);
					water.render(gl, car.camera.getPosition());
				}
			}

			if(enableBloom) bloom.render(gl);
			
			if(enableFocalBlur)
			{
				focalBlur.guassianPass(gl);
				focalBlur.disable(gl);
			}
			
			if(enableShadow) caster.disable(gl);
			
			gl.glDisable(GL2.GL_CLIP_PLANE2);
			
			/*
			 * The condition (i == 1) means that the frames stored in the accumulation
			 * are displayed once the last boosting car has been rendered
			 * The condition (_i = boostCounter) means that the number of vehicles
			 * boosting is consistent with the previous frame; hence, the accumulation
			 * buffer is in a stable state (will not produce visual artifacts) 
			 */
			if(enableBlur && car.isBoosting() && i == 1 && _i == boostCounter)
			{
				gl.glAccum(GL_MULT , 0.5f);
				gl.glAccum(GL_ACCUM, 0.5f);
		
				gl.glAccum(GL_RETURN, 1.0f);
			}
			else i--;
			
			if(displayLight && !moveLight) light.render(gl, glu);
			
			renderTimes[frameIndex][5] = renderBounds(gl);
			renderTimes[frameIndex][6] = car.renderHUD(gl, glu);
		}
		
		/* 
		 * Loads the current frame into the accumulation buffer; this is so that if
		 * motion blur occurs in the next frame, any old frames stored in the buffer
		 * will be over-written to avoid displaying visual artifacts.
		 * */
		if(enableBlur && _i != boostCounter) gl.glAccum(GL_LOAD, 1.0f);
		boostCounter = _i;
		
		if(shadowMap) displayMap(gl, focalBlur.getDepthTexture(), 0.0f, 0.0f, 1.0f, 1.0f);
	}
	
	public static boolean shadowMode = false;
	public static boolean reflectMode = false;
	public static boolean depthMode = false;
	public static boolean environmentMode = false;
	
	/**
	 * This method renders the world as reflected by the surface of the water.
	 * A new clipping plane is defined so that any primitives drawn below water
	 * level are clipped (the primitives clipped are actually above water level
	 * as the reflected environment is rendered upside down).
	 */
	public void renderWater(GL2 gl, Car car)
	{
		int aboveWater = car.camera.getPosition().y >= 0 ? -1 : 1;  
		
		gl.glEnable(GL2.GL_CLIP_PLANE1);
		double equation[] = {0, aboveWater, 0, 0}; // primitives above water are clipped
		gl.glClipPlane(GL2.GL_CLIP_PLANE1, equation, 0);
		
		reflectMode = true;

		gl.glPushMatrix();
		{
			gl.glScalef(1.0f, -1.0f, 1.0f); // render environment upside down

			renderWorld(gl);
			render3DModels(gl, car);
		}
		gl.glPopMatrix();
		
		reflectMode = false;

		gl.glDisable(GL2.GL_CLIP_PLANE1);
		water.setReflection(gl);
		
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		// removed reflected geometry from final render
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		GL2 gl = drawable.getGL().getGL2();
	
		if (height <= 0) height = 1;
		
		canvasHeight = height;
		canvasWidth  = width;
		
		cars.get(0).camera.setDimensions(width, height);
		bloom.changeSize(gl);
		resetShadow = true;
		
		final float ratio = (float) width / (float) height;
		
		gl.glViewport(0, 0, width, height);
		
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(fov, ratio, 1.0, far);
		
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
		
		gl.glClearAccum(0, 0, 0, 0);
		gl.glClear(GL_ACCUM_BUFFER_BIT);
	}

	public void dispose(GLAutoDrawable drawable) {}

	private void setupFog(GL2 gl)
	{
		if(enableLightning)
		{
			if(cloudDensity < 1) cloudDensity += DENSITY_INC;
			
			float c = cloudDensity;
			gl.glColor3f(c, c, c); // affect the color of the environment in addition to fog
			gl.glFogfv(GL_FOG_COLOR, new float[] {c, c, c, 1}, 0);
		}
		else gl.glFogfv(GL_FOG_COLOR, fogColor, 0);
		
		if(enableFog) gl.glFogf(GL_FOG_DENSITY, fogDensity);
		else gl.glFogf(GL_FOG_DENSITY, 0);
	}

	private void registerItems(GL2 gl)
	{
		for(Car car : cars)
		{
			while(!car.getItemCommands().isEmpty())
			{
				int itemID = car.getItemCommands().poll();
				
				if(itemID == 10) cloudDensity = MIN_DENSITY;
				else car.registerItem(gl, itemID);
			}
		}
	}

	private long update(GL2 gl)
	{	
		long start = System.currentTimeMillis();
		
		if(mousePressed) modifyTerrain(gl);
		
		removeItems();
		
		Particle.removeParticles(particles);
		
		if(enableItemBoxes)
			{ for(ItemBox box : itemBoxes) box.update(cars); } 
		
		updateItems();
		
		ItemBox.increaseRotation();
		FakeItemBox.increaseRotation();
		
		for(ParticleGenerator generator : generators)
			if(generator.update()) particles.addAll(generator.generate());
		
		for(Particle p : particles) p.update();
		
		updateTimes[frameIndex][1] = (enableBlizzard) ? blizzard.update() : 0;
			
		vehicleCollisions();
		for(Car car : cars)
		{
			car.update();
			if(outOfBounds(car.getPosition())) car.reset();
		}
		
		if(enableTerrain)
		{			
			long _start = System.nanoTime();
			
//			List<BillBoard> toRemove = new ArrayList<BillBoard>();
//			
//			for(BillBoard b : foliage)
//			{
//				if(b.sphere.testOBB(cars.get(0).bound)) toRemove.add(b);
//			}
//			
//			foliage.removeAll(toRemove);
			
			updateTimes[frameIndex][0] = System.nanoTime() - _start;
				
			for(Car car : cars) terrainCollisions(car);
		}
		
		return System.currentTimeMillis() - start;
	}

	private void modifyTerrain(GL2 gl)
	{
		Camera camera = cars.get(0).camera;
		
		if(camera.isAerial())	
		{
			Point point = canvas.getMousePosition();
			if(point == null) return;
		
			int x = (int) point.getX();
			int y = (int) point.getY();
			
			float[] p = camera.to3DPoint(x, y, canvasWidth, canvasHeight);
			float   r = camera.getRadius(retical, canvasHeight);
			float   h = (rightClick ? -0.5f : 0.5f);
			
			terrain.tree.deform(p, r, h);
			grassPatch.updateHeights(gl);
		}
	}

	public void removeItems()
	{	
		Item.removeItems(itemList);
		
		for(Car car : cars) car.removeItems();
	}

	private void updateItems()
	{		
		for(Item item : itemList)
		{
			item.update();
			item.update(cars);
		}
		
		for(Car car : cars)
		{
			for(Item item : car.getItems())
			{
				item.hold();
				item.update(cars);
			}
		}
		
		itemCollisions();
	}

	private void itemCollisions()
	{
		List<Item> allItems = new ArrayList<Item>();
		
		allItems.addAll(itemList);
		
		for(Car car : cars)
			allItems.addAll(car.getItems());
		
		for(int i = 0; i < allItems.size() - 1; i++)
		{
			for(int j = i + 1; j < allItems.size(); j++)
			{
				Item a = allItems.get(i);
				Item b = allItems.get(j);
				
				//TODO can be optimised further by using spatial partitioning
				if(a.canCollide(b) && a.getBound().testBound(b.getBound())) a.collide(b);
			}
		}
	}

	private void vehicleCollisions()
	{
		for(int i = 0; i < cars.size() - 1; i++)
		{
			for(int j = i + 1; j < cars.size(); j++)
			{
				Car a = cars.get(i);
				Car b = cars.get(j);
				
				if(a.getBound().testBound(b.getBound())) a.collide(b);
			}
		}
	}

	private void terrainCollisions(Car car)
	{
		Vec3[] vertices = car.bound.getVertices();
		float[] frictions = {1, 1, 1, 1};
		
		cars.get(0).patch = null;
			
		for(TerrainPatch patch : terrainPatches)
		{
			patch.colliding = false;
			
			for(int v = 0; v < 4; v++)
			{
				if(patch.isColliding(vertices[v].toArray()))
				{
					patch.colliding = true;
					if(frictions[v] > patch.friction) frictions[v] = patch.friction;
					car.patch = patch;
				}
			}
		}
			
		float friction = (frictions[0] + frictions[1] + frictions[2] + frictions[3]) / 4;
		car.friction = friction;
	}

	private int orderRender(int[] order)
	{
		int i = 0;
		
		for(int j = 0; j < cars.size(); j++)
		{
			order[j] = j; 
			
			/*
			 * The vehicles that are boosting must come first in the rendering
			 * order so that the accumulative buffer does not store the frames
			 * rendered by other vehicles.
			 */
			if(cars.get(j).isBoosting())
			{
				int t = order[i];
				order[i] = j;
				order[j] = t;
				i++;
			}
		}
		return i;
	}
	
	/**
	 * This method minimises the viewport to create a split-screen effect.
	 * The size of the viewport is halved so that it occupies one fourth/corner
	 * of the screen; P1 is top-left, P2 is top-right, P3 is bottom-left and P4
	 * is bottom-right.
	 * 
	 * It uses the OpenGL method glViewport(x, y, w, h). (x, y) is the bottom-left
	 * corner of the viewport, while w and h are its width and height respectively. 
	 */
	private void setupViewport(GL2 gl, int playerID)
	{
		int h = canvasHeight / 2;
		int w = canvasWidth  / 2;
		
		if(cars.size() < 2) gl.glViewport(0, 0, canvasWidth, canvasHeight); //full-screen
		else
		{
			     if(playerID == 0) gl.glViewport(0, h, w, h); //top-left
			else if(playerID == 1) gl.glViewport(w, h, w, h); //top-right
			else if(playerID == 2) gl.glViewport(0, 0, w, h); //bottom-left
			else if(playerID == 3) gl.glViewport(w, 0, w, h); //bottom-right
		}
	}

	private void displayReflection(GL2 gl, Car car)
	{
		// TODO Weather effects may not be working correctly with reflection
		
		Vec3 p = light.getPosition(); 
		light.setPosition(new Vec3(p.x, -p.y, p.z));
		
		if(enableTerrain)
		{
			gl.glEnable(GL2.GL_CLIP_PLANE1);
			double equation[] = {0, -1, 0, 10}; // primitives above water are clipped
			gl.glClipPlane(GL2.GL_CLIP_PLANE1 , equation, 0);
		}
		
		gl.glPushMatrix();
		{
			if(enableTerrain) gl.glTranslatef(0.0f, 20, 0.0f);
			gl.glScalef(1.0f, -1.0f, 1.0f); // render environment upside down
			
			renderWorld(gl);
			render3DModels(gl, car);
			
			renderParticles(gl, car);
			Particle.resetTexture();
		}
		gl.glPopMatrix();
		
		gl.glDisable(GL2.GL_CLIP_PLANE1);
		
		if(!enableTerrain) renderFloor(gl, true);
		
		light.setPosition(p);
		
		gl.glColor3f(1, 1, 1);
		
		if(enableTerrain)
		{
			/*
			 * For the plane equation, the first three components represent a vector/axis
			 * and the fourth component is a translation along this vector.
			 */
			gl.glEnable(GL2.GL_CLIP_PLANE2);
			double[] equation = {0, 1, 0, -10};
			gl.glClipPlane(GL2.GL_CLIP_PLANE2 , equation, 0);
		}
	}

	public void renderFloor(GL2 gl, boolean reflect)
	{	
		if(reflect)
		{
			gl.glColor4f(0.75f, 0.75f, 0.75f, opacity);
			
			gl.glDisable(GL2.GL_LIGHTING);
			gl.glEnable(GL2.GL_BLEND);
			
			gl.glPushMatrix();
			{
				gl.glTranslatef(0, 0, 0);
				gl.glScalef(40.0f, 40.0f, 40.0f);
		
				gl.glCallList(floorList);
			}	
			gl.glPopMatrix();
			
			gl.glDisable(GL2.GL_BLEND);
			gl.glEnable(GL2.GL_LIGHTING);
		}
		else
		{
			gl.glPushMatrix();
			{
				gl.glTranslatef(0, 0, 0);
				gl.glScalef(40.0f, 40.0f, 40.0f);
				
				Shader shader = Shader.get("phong_shadow");
				
				if(enableShadow && shader != null)
				{
					float[] model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
					Matrix.scale(model, 40, 40, 40);
					
					shader.loadMatrix(gl, model);
				}
	
				gl.glCallList(floorList);
			}	
			gl.glPopMatrix();
		}
	}

	/**
	 * This method renders the world geometry that represents the artificial
	 * boundaries of the virtual environment. This consists of the skybox and
	 * the terrain itself.
	 */
	public long renderWorld(GL2 gl)
	{
		long start = System.nanoTime();
		
		int[] attachments = {GL2.GL_COLOR_ATTACHMENT0, GL2.GL_COLOR_ATTACHMENT1};
		
		// prevents shadow texture unit from being active
		if(enableShadow ) caster.disable(gl);
		if(displaySkybox || Shader.enabled) renderSkybox(gl);
		
		Shader shader = null;
		
		if(Shader.enabled)
		{
			if(enableShadow)
			{
				shader = Shader.get("phong_shadow");
				
				if(shader != null)
				{
					caster.enable(gl);
					shader.enable(gl);
					shader.setSampler(gl, "texture"  , 0);
					shader.setSampler(gl, "shadowMap", 2);
				}
			}
			else
			{
				shader = Shader.get("phong_texture");
				
				if(shader != null)
				{
					shader.enable(gl);
					shader.setSampler(gl, "texture", 0);
				}
			}
		}
		gl.glDrawBuffers(2, attachments, 0);
		if(enableTerrain) renderTimes[frameIndex][0] = renderTerrain(gl); 
		else if(!enableReflection)
		{
			renderFloor(gl, false);
			renderTimes[frameIndex][0] = 0;
		}
		gl.glDrawBuffers(1, attachments, 0);
		renderObstacles(gl);
		
		caster.disable(gl);
		Shader.disable(gl);
			
		if(displaySkybox) renderWalls(gl);
		
		return System.nanoTime() - start;
	}

	public void renderWalls(GL2 gl)
	{
		Texture[] textures = {cobble, brickWallTop, brickWall};
			 
		gl.glPushMatrix();
		{
			displayTexturedCuboid(gl,        0, 45,  206.25f, 202.5f, 45,  3.75f,  0, textures);
			displayTexturedCuboid(gl,        0, 45, -206.25f, 202.5f, 45,  3.75f,  0, textures);
			displayTexturedCuboid(gl,  206.25f, 45,        0, 202.5f, 45,  3.75f, 90, textures);
			displayTexturedCuboid(gl, -206.25f, 45,        0, 202.5f, 45,  3.75f, 90, textures);
		}
		gl.glPopMatrix();
	}

	public void renderSkybox(GL2 gl)
	{
		skybox.render(gl);
	}

	public long renderTerrain(GL2 gl)
	{
		long start = System.nanoTime();

		gl.glPushMatrix();
		{	
			terrain.render(gl, glut);
		}	
		gl.glPopMatrix();

		if(!terrain.enableQuadtree)
		{
			gl.glPushMatrix();
			{
				gl.glScalef(Terrain.sx, Terrain.sy, Terrain.sz);
				
				for(TerrainPatch shape : terrainPatches) shape.render(gl);
			}	
			gl.glPopMatrix();
		}
		
		return System.nanoTime() - start;
	}

	/**
	 * This method renders the 3D models that represent obstacles within the
	 * world. Obstacle may be rendered using colored and/or textured geometry,
	 * or simply as wireframes if obstacle wireframes are enabled for debugging.
	 */
	public long renderObstacles(GL2 gl)
	{	
		long start = System.nanoTime();

		fort.render(gl);

		return System.nanoTime() - start;
	}

	/**
	 * This method renders all of the dynamic 3D models within the world from the
	 * perspective of the car passed as a parameter. The term dynamic refers to
	 * objects that change position, rotation or state such as other vehicles, item
	 * boxes and world items.
	 */
	public void render3DModels(GL2 gl, Car car)
	{	
		renderTimes[frameIndex][2] = renderVehicles(gl, car, false);
	
		if(enableItemBoxes)
	    {
			for(ItemBox box : itemBoxes)
				if(!box.isDead()) box.render(gl, car.trajectory);
	    }
		
		renderTimes[frameIndex][3] = renderItems(gl, car);
	}

	public long renderVehicles(GL2 gl, Car car, boolean shadow)
	{
		long start = System.nanoTime();
		
		if(!shadow || !car.isInvisible())
		{
			boolean tag = car.displayTag;
			if(shadow) car.displayTag = false;
			car.render(gl);
			car.displayTag = tag;
		}
	
		for(Car c : cars)
		{
			if(!c.equals(car))
			{
				boolean visibility = c.displayModel;
				
				c.displayModel = true;
				
				if(!shadow || !car.isInvisible())
				{
					boolean tag = car.displayTag;
					if(shadow || car.isInvisible()) car.displayTag = false;
					car.render(gl);
					car.displayTag = tag;
				}
				c.displayModel = visibility;
			}
		}
		
		return System.nanoTime() - start;
	}
	
	public static int bananasRendered = 0;

	public long renderItems(GL2 gl, Car car)
	{
		long start = System.nanoTime();
		
		gl.glColor3f(1, 1, 1);
		
		if(enableCulling) gl.glEnable(GL2.GL_CULL_FACE);
	
		for(Item item : itemList)
			if(!item.isDead())
			{
				if(Item.renderMode == 1) item.renderWireframe(gl, car.trajectory);
				else item.render(gl, car.trajectory);
			}
		
		gl.glDisable(GL2.GL_CULL_FACE);
		
		return System.nanoTime() - start;
	}

	public long renderParticles(GL2 gl, Car car)
	{
		long start = System.nanoTime();
		
		if(enableBlizzard) blizzard.render(gl);
		
//		for(LightingStrike bolt : bolts) bolt.render(gl);
		
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
		
		for(Particle particle : particles)
		{
			if(car.isSlipping()) particle.render(gl, car.slipTrajectory);
			else particle.render(gl, car.trajectory);
		}
		
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		return System.nanoTime() - start;
	}
	
	public boolean enableFoliage = true;
	public int foliageMode = 0;

	public long renderFoliage(GL2 gl, Car car)
	{
		if(!enableFoliage) return 0;
		
		long start = System.nanoTime();
		
		switch(foliageMode)
		{
			case 0: 
			{
				gl.glPushMatrix();
				{	
					for(BillBoard b : foliage)
					{
						if(car.isSlipping()) b.render(gl, car.slipTrajectory);
						else b.render(gl, car.trajectory);
					}
				}	
				gl.glPopMatrix();
				
				break;
			}
			case 1: BillBoard.renderPoints(gl, foliage); break;
			case 2: BillBoard.renderQuads(gl, foliage, car.trajectory); break;
		}
		
		return System.nanoTime() - start;
	}

	private long renderBounds(GL2 gl)
	{
		long start = System.nanoTime();
		
		gl.glDisable(GL_TEXTURE_2D);
		gl.glDisable(GL2.GL_LIGHTING);
		
		if(testMode) test(gl);
		
		List<Bound> bounds = getBounds();
		
		if(enableClosestPoints)
			for(Bound bound : bounds)
				bound.displayClosestPtToPt(gl, glut, cars.get(0).getPosition(), smoothBound);
		
		if(enableOBBVertices)
		{
			for(Car car : cars)
			{
				if(car.colliding)
					 car.bound.displayVertices(gl, glut, RGB.PURE_RED_3F, smoothBound);
				else car.bound.displayVertices(gl, glut, RGB.WHITE_3F, smoothBound);
			}
		
			for(OBB wall : wallBounds) wall.displayVertices(gl, glut, RGB.WHITE_3F, smoothBound);
			for(OBB wall : fort.getBounds()) wall.displayVertices(gl, glut, RGB.WHITE_3F, smoothBound);
		}
		
		if(enableOBBWireframes)
		{
			for(Car car : cars)
			{
				if(car.colliding)
					 car.bound.displayWireframe(gl, RGB.PURE_RED_3F, smoothBound);
				else car.bound.displayWireframe(gl, RGB.BLACK_3F, smoothBound);
			}
			
			for(Bound bound : bounds)
				for(Car car : cars)
				{
					if(car.collisions != null && car.collisions.contains(bound))
					{
						 bound.displayWireframe(gl, RGB.PURE_RED_3F, smoothBound);
						 break;
					}
					else bound.displayWireframe(gl, RGB.BLACK_3F, smoothBound);
				}
		}
		
		if(enableOBBAxes)
		{
			for(Car car : cars) car.bound.displayAxes(gl, 10);
			for(OBB wall : wallBounds) wall.displayAxes(gl, 20);
			for(OBB wall : fort.getBounds()) wall.displayAxes(gl, 20);
		}
		
		if(enableOBBSolids)
			for(Bound bound : bounds)
				bound.displaySolid(gl, RGB.toRGBAi(RGB.VIOLET, 0.1f));
		
		for(Car car : cars)
			for(Item item : car.getItems())
				item.renderBound(gl);
		
		for(Item item : itemList) item.renderBound(gl);
		
		gl.glEnable(GL_TEXTURE_2D);
		gl.glEnable(GL2.GL_LIGHTING);
		
		return System.nanoTime() - start;
	}

	private void test(GL2 gl)
	{	
		
	}

	public void resetView(GL2 gl)
	{
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(this.fov, (float) canvasWidth / (float) canvasHeight, 1.0, far);
		
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	    gl.glViewport(0, 0, canvasWidth, canvasHeight);
	}

	/**
	 * This method calculates the frames per second (FPS) of the application by
	 * incrementing a frame counter every time the scene is displayed. Once the
	 * time elapsed is >= 1000 milliseconds (1 second), the current FPS is set
	 * equal to the frame counter which is then reset.
	 */
	private void calculateFPS()
	{
		frames++;
		frameIndex++;
		
		long currentTime = System.currentTimeMillis();
		long timeElapsed = currentTime - startTime;
		
		if(timeElapsed >= 1000)
		{
			frameRate = frames;
			
			frames = 0;
			startTime = currentTime + (timeElapsed % 1000);
		}
		
		if(frameIndex >= frameTimes.length) frameIndex = 0;
		
		frameTimes[frameIndex] = currentTime - renderTime;
	}
	
	public static boolean outOfBounds(Vec3 p)
	{
		return p.dot() > GLOBAL_RADIUS * GLOBAL_RADIUS;
	}

	public List<Car> getCars() { return cars; }

	public void sendItemCommand(int itemID) { itemQueue.add(itemID); }
	
	public void addItem(Item item) { itemList.add(item); }
	
	public void addItem(int item, Vec3 c, float trajectory)
	{
		switch(item)
		{
			case GreenShell.ID  : itemList.add(new  GreenShell(this, c, trajectory)); break;
			case RedShell.ID    : itemList.add(new    RedShell(this, c, trajectory)); break;
			case FakeItemBox.ID : itemList.add(new FakeItemBox(this, c, trajectory)); break;
			case Banana.ID      : itemList.add(new      Banana(this, c)); break;
			case BlueShell.ID   : itemList.add(new   BlueShell(this, c)); break;
			
			default: break;
		}
	}

	public void spawnItemsInBound(int item, int quantity, Bound b)
	{
		Random random = new Random();
		
		for(int i = 0; i < quantity; i++)
		{
			addItem(item, b.randomPointInside(), random.nextInt(360));
		}
	}

	public void spawnItemsInSphere(int item, int quantity, Vec3 c, float r)
	{
		spawnItemsInBound(item, quantity, new Sphere(c, r));
	}

	public void spawnItemsInOBB(int item, int quantity, float[] c, float[] u, float[] e)
	{
		spawnItemsInBound(item, quantity, new OBB(c, u, e, null));
	}

	public void clearItems() { itemList.clear(); }

	public List<Item> getItems() { return itemList; }
	
	public void addItemBox(float x, float y, float z)
	{
		itemBoxes.add(new ItemBox(x, y, z, particles));
	}

	public void addParticle(Particle p) { particles.add(p); }
	
	public void addParticles(List<Particle> particles) { this.particles.addAll(particles); }
	
	public List<Particle> getParticles() { return particles; }
	
	public List<Bound> getBounds()
	{
		List<Bound> bounds = new ArrayList<Bound>();
		
		bounds.addAll(wallBounds);
		if(enableObstacles) bounds.addAll(fort.getBounds());
		
		return bounds;
	}

	public Terrain getTerrain() { return terrain; }
	
	public void generateTerrain(GL2 gl, String command)
	{
		Random generator = new Random();
		
		String[] args = command.trim().split(" ");
		
		if(args.length > 3)
		{
			int length     = Integer.parseInt(args[0]);
			int iterations = Integer.parseInt(args[1]);
			int splashes   = Integer.parseInt(args[2]);
			
			int r0 = Integer.parseInt(args[3]);
			int r1 = Integer.parseInt(args[4]);
			
			float p = Float.parseFloat(args[5]);
			float h = Float.parseFloat(args[6]);
			
			long start = System.currentTimeMillis();
			
			terrain = new Terrain(gl, length, iterations, r0, r1, p, h);
			
			long end = System.currentTimeMillis();
			
			System.out.printf("Terrain Generated: (%d) %d ms\n", terrain.length * terrain.length, (end - start));
			
			terrainPatches = new TerrainPatch[splashes];
	    
			for (int i = 0; i < terrainPatches.length; i++)
				terrainPatches[i] = new TerrainPatch(null, terrain.heights, generator.nextInt(15) + 5);
			
			System.out.printf("Patches Generated: %d ms\n", (System.currentTimeMillis() - end));
		}
		else
		{
			int length     = Integer.parseInt(args[0]);
			int iterations = Integer.parseInt(args[1]);
			int splashes   = Integer.parseInt(args[2]);
			
			long start = System.currentTimeMillis();
			
			terrain = new Terrain(gl, length, iterations);
			
			long end = System.currentTimeMillis();
			
			System.out.printf("Terrain Generated: (%d) %d ms\n", terrain.length * terrain.length, (end - start));
			
			terrainPatches = new TerrainPatch[splashes];
	    
			for (int i = 0; i < terrainPatches.length; i++)
				terrainPatches[i] = new TerrainPatch(null, terrain.heights, generator.nextInt(15) + 5);
			
			System.out.printf("Patches Generated: %d ms\n", (System.currentTimeMillis() - end));
		}
		
		long start = System.currentTimeMillis();
			
		generateFoliage(60, 10, 30);
		
		System.out.printf("Foliage Generated: (%d) %d ms\n", foliage.size(), (System.currentTimeMillis() - start));
	}

	public void generateFoliage(int patches, float spread, int patchSize)
	{
		foliage = new ArrayList<BillBoard>();
		Random generator = new Random(); 
	    
	    for(int i = 0; i < patches; i++)
	    {
	    	int t = (i < patches * 0.5) ? 3 : generator.nextInt(3);
	    	
	    	Vec3 p = new Vec3();
	    	
	    	p.x = generator.nextFloat() * 360 - 180;
	    	p.z = generator.nextFloat() * 360 - 180;
	    	
	    	if(i > patches * 0.75)
	    	{
	    		p.y = (terrain.enableQuadtree) ? terrain.tree.getCell(p.toArray(), terrain.tree.detail).getHeight(p.toArray()) : terrain.getHeight(p.toArray());
	    		foliage.add(new BillBoard(p, 3, t));
	    	}
	    	else
	    	{    	
		    	int k = generator.nextInt(patchSize - 1) + 1; 
		    	
		    	for(int j = 0; j < k; j++)
		    	{
		    		Vec3 p0 = new Vec3(p);
		    		
		    		double incline = Math.toRadians(generator.nextInt(360));
		    		double azimuth = Math.toRadians(generator.nextInt(360));
		    		
		    		p0.x += (float) (Math.sin(incline) * Math.cos(azimuth) * spread);
		    		p0.z += (float) (Math.sin(incline) * Math.sin(azimuth) * spread);
		    		
		    		if(Math.abs(p0.x) < 200 && Math.abs(p0.z) < 200)
		    		{
		    			p0.y = (terrain.enableQuadtree) ? terrain.tree.getCell(p0.toArray(), terrain.tree.detail).getHeight(p0.toArray()) : terrain.getHeight(p0.toArray());
		    			foliage.add(new BillBoard(p0, 2, t));
		    		}
		    	}
	    	}
	    }
	    
	    for(int i = 0; i < 30; i++)
	    {
	    	int t = 4 + generator.nextInt(4);
	    	
	    	Vec3 p = new Vec3();
	    	
	    	p.x = generator.nextFloat() * 360 - 180;
	    	p.z = generator.nextFloat() * 360 - 180;
	    	
	    	p.y = (terrain.enableQuadtree) ? terrain.tree.getCell(p.toArray(), terrain.tree.detail).getHeight(p.toArray()) : terrain.getHeight(p.toArray());
	    	
	    	foliage.add(new BillBoard(p, 30, t));
	    }
	    
	    for(int i = 0; i < 30; i++)
	    {
	    	int t = 8 + generator.nextInt(2);
	    	
	    	Vec3 p = new Vec3();
	    	
	    	p.x = generator.nextFloat() * 360 - 180;
	    	p.z = generator.nextFloat() * 360 - 180;
	    	
	    	p.y = (terrain.enableQuadtree) ? terrain.tree.getCell(p.toArray(), terrain.tree.detail).getHeight(p.toArray()) : terrain.getHeight(p.toArray());

	    	foliage.add(new BillBoard(p, 4, t));
	    }
	}
	
	public void updateFoliage()
	{
		for(BillBoard board : foliage)
		{
			Vec3 p = board.sphere.c;
			board.sphere.c.y = (terrain.enableQuadtree) ? terrain.tree.getCell(p.toArray(), terrain.tree.detail).getHeight(p.toArray()) : terrain.getHeight(p.toArray());
		}
	}

	public void printDataToFile(String file, String[] headers, long[][] data)
	{
		try
		{
			Calendar now = Calendar.getInstance();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			
			String _execution = new SimpleDateFormat("yyyyMMddHHmmss").format(execution.getTime());
			
			FileWriter writer = new FileWriter("output/" + (file == null ? _execution : file) + ".txt");
			BufferedWriter out = new BufferedWriter(writer);
			
			out.write("Values recorded at: " + dateFormat.format(now.getTime()) + "\r\n\r\n ");
			
			for(int c = 0; c < headers.length; c++)
				out.write(String.format("%11s", headers[c]) + "\t");
			
			out.write("\r\n\r\n ");
			
			for(int j = 0; j < headers.length; j++)
			{
				long sum = 0;
				
				for(int i = 0; i < data.length; i++) sum += data[i][j];
				
				sum /= data.length;
				
				out.write(String.format("%11.3f", sum / 1E6) + "\t");
			}	
			
			out.write("\r\n\r\n ");
			
			for(int i = 0; i < data.length; i++)
			{
				for(int j = 0; j < headers.length; j++)
				{
					out.write(String.format("%11.3f", data[i][j] / 1E6) + "\t");
				}	
				
				out.write("\r\n ");
			}
	
			out.close();
		}
		
		catch(Exception e)
		{
			System.err.println(e.getMessage());
		}
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

	/**
	 * Invoked when a key has been pressed to make global alterations to the scene
	 * such as toggling the display of certain visualizations intended to aid the
	 * debugging process. By default, if one of the keys pressed is not a global
	 * control, the key press event is passed on to each vehicle within the scene.
	 */
	public void keyPressed(KeyEvent e)
	{	
		if(e.isShiftDown()) { shiftEvent(e); return; }
		
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE: System.exit(0); break;
			
			case KeyEvent.VK_H:  enableObstacles = !enableObstacles; break;
			
			case KeyEvent.VK_T:   enableTerrain = !enableTerrain; for(Car car : cars) car.friction = 1; break;
			case KeyEvent.VK_F10: enableItemBoxes = !enableItemBoxes; break;
			
			case KeyEvent.VK_BACK_SLASH: shadowMap = !shadowMap; break;
			case KeyEvent.VK_F9: sphereMap = !sphereMap; break;
			case KeyEvent.VK_P: bloom.cycleMode(); break;
			case KeyEvent.VK_O: texture++; if(texture > 6) texture = 0; System.out.println(texture); break;
			
			case KeyEvent.VK_L        : printDataToFile(null, RENDER_HEADERS, renderTimes); break;
			case KeyEvent.VK_SEMICOLON: printDataToFile(null, UPDATE_HEADERS, updateTimes); break;
			
			case KeyEvent.VK_DELETE: clearItems(); break;
			
			case KeyEvent.VK_X:  moveLight = !moveLight; break;
	 
			case KeyEvent.VK_F12: enableAnimation = !enableAnimation; break;
	
			case KeyEvent.VK_1:  enableOBBAxes       = !enableOBBAxes;       break;
			case KeyEvent.VK_2:  enableOBBVertices   = !enableOBBVertices;   break;
			case KeyEvent.VK_3:  enableOBBWireframes = !enableOBBWireframes; break;
			case KeyEvent.VK_4:	 enableOBBSolids     = !enableOBBSolids;     break;
			case KeyEvent.VK_5:	 enableClosestPoints = !enableClosestPoints; break;
			
			case KeyEvent.VK_F1 : fort.renderMode++; fort.renderMode %= 4; break;
			case KeyEvent.VK_F3 : Item.renderMode++; Item.renderMode %= 2; break;
			case KeyEvent.VK_Z  : foliageMode++; foliageMode %= 3; break;
			
			case KeyEvent.VK_6:  Item.toggleBoundSolids(); break;
			case KeyEvent.VK_7:  Item.toggleBoundFrames(); break;
			
			case KeyEvent.VK_8:  fort.displayModel = !fort.displayModel; break;
			case KeyEvent.VK_0:  displaySkybox = !displaySkybox; break;

			case KeyEvent.VK_PERIOD       :
			case KeyEvent.VK_EQUALS       :
			case KeyEvent.VK_MINUS        : terrain.keyPressed(e); updateFoliage(); break;
			case KeyEvent.VK_J            :
			case KeyEvent.VK_K            :
			case KeyEvent.VK_I            :
			case KeyEvent.VK_U            : // TODO temporary for demonstration
			case KeyEvent.VK_OPEN_BRACKET :
			case KeyEvent.VK_CLOSE_BRACKET:
			case KeyEvent.VK_QUOTE        :
			case KeyEvent.VK_NUMBER_SIGN  :
			case KeyEvent.VK_SLASH        : terrain.keyPressed(e); break; 
			case KeyEvent.VK_COMMA        : terrain.keyPressed(e); generateFoliage(60, 10, 30); break;
			
			case KeyEvent.VK_SPACE: console.parseCommand(command.getText()); break;
	
			default: for(Car car : cars) car.keyPressed(e); break;
		}
	}
	
	public static boolean occludeSphere = true;
	
	public void shiftEvent(KeyEvent e)
	{
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_1: spawnItemsInSphere(GreenShell.ID, 10, new Vec3(0, 100, 0), 50); break;
			case KeyEvent.VK_2: spawnItemsInSphere(RedShell.ID, 10, new Vec3(0, 100, 0), 50); break;
			case KeyEvent.VK_3: spawnItemsInOBB(FakeItemBox.ID, 10, new float[] {0, 100, 0}, ORIGIN, new float[] {150, 50, 150}); break;
			case KeyEvent.VK_4: spawnItemsInSphere(Banana.ID, 10, new Vec3(0, 100, 0), 50); break;
			case KeyEvent.VK_5: spawnItemsInOBB(BlueShell.ID, 10, new float[] {0, 100, 0}, ORIGIN, new float[] {150, 50, 150}); break;
			
			case KeyEvent.VK_D: cars.get(0).enableDeform = !cars.get(0).enableDeform; break;
			case KeyEvent.VK_P: enableParallax = !enableParallax; break;
			case KeyEvent.VK_I: occludeSphere = !occludeSphere; break;
			case KeyEvent.VK_W: water.frozen = !water.frozen; break;
			case KeyEvent.VK_M: water.magma = !water.magma; break;
			case KeyEvent.VK_F: enableFoliage = !enableFoliage; break;
			
			case KeyEvent.VK_O: enableOcclusion = !enableOcclusion; break;
			
			case KeyEvent.VK_S: ShadowCaster.cycle(); break;
			
			default: break;
		}
	}
	
	public void keyReleased(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			default: for(Car car : cars) car.keyReleased(e); break;
		}
	}

	public void keyTyped(KeyEvent e)
	{
		switch (e.getKeyChar())
		{
			default: break;
		}
	}

	public KeyEvent pressKey(char c)
	{
		long when = System.nanoTime();
		int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
		
		return new KeyEvent(command, KeyEvent.KEY_PRESSED, when, 0, keyCode, c);
	}
	
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		Camera camera = cars.get(0).camera;
		
		if(camera.isAerial())
		{
			retical += e.getWheelRotation() * 3;
			if(retical < 1) retical = 1;
		}
		else if(camera.isDynamic()) camera.zoom(e.getWheelRotation());
	}

	public void mouseClicked(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited (MouseEvent e) {}

	public boolean rightClick;
	
	public void mousePressed(MouseEvent e)
	{
		selecter.setSelection(e.getX(), e.getY());
		
		rightClick = SwingUtilities.isRightMouseButton(e);
		mousePressed = true;
		
		bloom.increaseIncrement();
	}

	public void mouseReleased(MouseEvent e)
	{
		mousePressed = false;
		terrain.tree.setHeights();
	}

	public void actionPerformed(ActionEvent event)
	{
		     if(event.getActionCommand().equals("console"      )) console.parseCommand(command.getText());
		else if(event.getActionCommand().equals("load_project" )) console.parseCommand("profile project");
		else if(event.getActionCommand().equals("load_game"    )) console.parseCommand("profile game");
		     
		else if(event.getActionCommand().equals("no_weather"   )) enableBlizzard = false;
		     
		else if(event.getActionCommand().equals("snow"         )) { enableBlizzard = true; blizzard = new Blizzard(this, flakeLimit, new Vec3(0.2f, -1.5f, 0.1f), StormType.SNOW); }
		else if(event.getActionCommand().equals("rain"         )) { enableBlizzard = true; blizzard = new Blizzard(this, flakeLimit, new Vec3(0.0f, -4.0f, 0.0f), StormType.RAIN); }
		     
		else if(event.getActionCommand().equals("shadow_low"   )) { caster.setQuality(ShadowQuality.LOW);  resetShadow = true; }
		else if(event.getActionCommand().equals("shadow_medium")) { caster.setQuality(ShadowQuality.MED);  resetShadow = true; }
		else if(event.getActionCommand().equals("shadow_high"  )) { caster.setQuality(ShadowQuality.HIGH); resetShadow = true; }
		else if(event.getActionCommand().equals("shadow_best"  )) { caster.setQuality(ShadowQuality.BEST); resetShadow = true; }
		     
		else if(event.getActionCommand().equals("recalc_norms" )) terrain.tree.resetNormals();
		else if(event.getActionCommand().equals("recalc_tangs" )) terrain.tree.resetTangent();    
		else if(event.getActionCommand().equals("save_heights" ))
		{
			Quadtree tree = terrain.tree;
			tree.setHeights();
			tree.setGradient(tree.gradient);
		}
		else if(event.getActionCommand().equals("reset_heights")) terrain.tree.resetHeights();
		     
		else if(event.getActionCommand().equals("set_ambience" )) light.setAmbience(chooseColor(light.getAmbience(), "Ambient Lighting Color" ));
		else if(event.getActionCommand().equals("set_emission" )) light.setEmission(chooseColor(light.getEmission(), "Emissive Material Color"));
		else if(event.getActionCommand().equals("set_specular" )) light.setSpecular(chooseColor(light.getSpecular(), "Specular Lighting Color"));
		else if(event.getActionCommand().equals("set_diffuse"  )) light.setDiffuse (chooseColor(light.getDiffuse() , "Diffuse Lighting Color" )); 
		     
		else if(event.getActionCommand().equals("material_specular")) terrain.tree.specular = chooseColor(terrain.tree.specular, "Specular Reflectivity");
		
		else if(event.getActionCommand().equals("fog_color")) fogColor   = chooseColor(fogColor, "Fog Color");
		else if(event.getActionCommand().equals("bg_color" )) background = chooseColor(background, "Background Color");
		else if(event.getActionCommand().equals("sky_color")) skybox.setSkyColor(chooseColor(skybox.getSkyColor(), "Sky Color"));
		else if(event.getActionCommand().equals("horizon"  )) skybox.setHorizonColor(chooseColor(skybox.getHorizonColor(), "Horizon Color"));
		     
		else if(event.getActionCommand().equals("car_color")) cars.get(0).setColor(chooseColor(cars.get(0).getColor(), "Body Color"));
		     
		else if(event.getActionCommand().equals("close"        )) System.exit(0);
	}
	
	public float[] chooseColor(float[] color, String title)
	{
		Color oldColor = color.length > 3 ? new Color(color[0], color[1], color[2], color[3]) :
											new Color(color[0], color[1], color[2]);
		Color newColor = JColorChooser.showDialog(frame, title, oldColor);
		
		if(newColor == null) return color;
		
		if(color.length > 3) color = RGB.toRGBA(newColor);
		else color = RGB.toRGB(newColor);
		
		return color;
	}

	public void itemStateChanged(ItemEvent ie)
	{
		Object source = ie.getItemSelectable();
		boolean selected = (ie.getStateChange() == ItemEvent.SELECTED);
		
		     if(source.equals(menuItem_multisample)) multisample                  = selected;
		else if(source.equals(menuItem_anisotropic)) Renderer.anisotropic         = selected; 
		else if(source.equals(menuItem_bloom      )) enableBloom                  = selected;  
		else if(source.equals(menuItem_motionblur )) enableBlur                   = selected;
		else if(source.equals(menuItem_fog        )) enableFog                    = selected;    
		else if(source.equals(menuItem_normalize  )) normalize                    = selected;
		else if(source.equals(menuItem_smooth     )) light.smooth                 = selected;
		else if(source.equals(menuItem_secondary  )) light.secondary              = selected;
		else if(source.equals(menuItem_local      )) light.local                  = selected;    
		else if(source.equals(menuItem_water      )) terrain.enableWater          = selected;
		else if(source.equals(menuItem_reflect    )) enableReflection             = selected;    
		else if(source.equals(menuItem_solid      )) terrain.tree.solid           = selected;
		else if(source.equals(menuItem_elevation  )) terrain.tree.reliefMap       = selected;  
		else if(source.equals(menuItem_frame      )) terrain.tree.frame           = selected;
		else if(source.equals(menuItem_texturing  )) terrain.tree.enableTexture   = selected;
		else if(source.equals(menuItem_bumpmaps   )) terrain.tree.enableBumpmap   = selected;
		else if(source.equals(menuItem_caustics   )) terrain.tree.enableCaustic   = selected;   
		else if(source.equals(menuItem_malleable  )) terrain.tree.malleable       = selected;     
		else if(source.equals(menuItem_vnormals   )) terrain.tree.vNormals        = selected; 
		else if(source.equals(menuItem_vtangents  )) terrain.tree.vTangents       = selected;     
		else if(source.equals(menuItem_shading    )) terrain.tree.enableShading   = selected;
		else if(source.equals(menuItem_vcoloring  )) terrain.tree.enableColoring  = selected;
		else if(source.equals(menuItem_reverse    )) cars.get(0).invertReverse    = selected;
		else if(source.equals(menuItem_shaking    )) cars.get(0).camera.shaking   = selected;
		else if(source.equals(menuItem_trackball  )) cars.get(0).camera.trackball = selected;
		else if(source.equals(menuItem_focal_blur )) enableFocalBlur              = selected;
		else if(source.equals(menuItem_mirage     )) focalBlur.enableMirage       = selected;    
		else if(source.equals(menuItem_tag        )) cars.get(0).displayTag       = selected;    
		else if(source.equals(menuItem_settle     )) blizzard.enableSettling      = selected;
		else if(source.equals(menuItem_splash     )) blizzard.enableSplashing     = selected;
		else if(source.equals(menuItem_shaders    )) Shader.enabled               = selected;  
		else if(source.equals(menuItem_shadows    )) enableShadow                 = selected;   
		else if(source.equals(menuItem_aberration )) cars.get(0).enableAberration = selected;     
		else if(source.equals(menuItem_cubemap    ))
		{
			cars.get(0).enableChrome = selected;
			cars.get(0).resetGraph();
		}
	}

	public void valueChanged(ListSelectionEvent e)
	{
		int index = quadList.getSelectedIndex();
		terrain.selectQuadtree(listModel.elementAt(index));
		
		setCheckBoxes();
	}

	public void mouseMoved(MouseEvent e)
	{
		if(cars != null && !cars.isEmpty()) cars.get(0).camera.mouseMoved(e);
	}
	
	public void mouseDragged(MouseEvent e)
	{
		if(cars != null && !cars.isEmpty()) cars.get(0).camera.mouseDragged(e);
	}
}
