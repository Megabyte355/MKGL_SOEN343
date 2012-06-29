import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL2.GL_POINTS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import javax.media.opengl.GL2;


public class SparkParticle extends Particle
{
	private double gravity = 0.05;
	private float fallRate = 0.0f;
	private static final double TOP_FALL_RATE = 5.0;
	
	private float[] color;
	
	public SparkParticle(float[] c, float[] t, float rotation, int duration, float[] color)
	{
		super(c, t, rotation, duration);
		
		this.color = color;
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			gl.glTranslatef(c[0], c[1], c[2]);
			gl.glRotatef(trajectory - 90, 0, 1, 0);
			gl.glScalef(0.5f, 0.5f, 0.5f);
			
			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);

			gl.glColor3f(color[0], color[1], color[2]);
			
			gl.glPointSize(1.3f);
			
			gl.glBegin(GL_POINTS);
			{
				gl.glVertex3f(0, 0, 0);
			}
			gl.glEnd();

			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			gl.glDepthMask(true);
		}
		gl.glPopMatrix();
	}
	
	@Override
	public void update()
	{
		super.update();
		
		if(fallRate < TOP_FALL_RATE) fallRate += gravity;
		c[1] -= fallRate;
	}
}