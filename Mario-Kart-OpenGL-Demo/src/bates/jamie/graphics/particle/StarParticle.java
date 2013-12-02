package bates.jamie.graphics.particle;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.util.Random;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Vec3;


public class StarParticle extends Particle
{
	private float scale;
	
	public StarParticle(Vec3 c, Vec3 t, int duration, float scale)
	{
		super(c, t, 0, duration);

		this.scale = scale;
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{	
			Random generator = new Random();
			
			gl.glTranslatef(c.x, c.y, c.z);
			gl.glRotatef(trajectory, 0, -1, 0);
			gl.glScalef(scale, scale, scale);
			
			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);

			gl.glColor4f(1, 1, 1, 1);
			
			whiteStar.bind(gl);

			gl.glBegin(GL_QUADS);
			{
				gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-0.5f, -0.5f, 0.0f);
				gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-0.5f,  0.5f, 0.0f);
				gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 0.5f,  0.5f, 0.0f);
				gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.5f, -0.5f, 0.0f);
			}
			gl.glEnd();

			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			gl.glDepthMask(true);
		}
		gl.glPopMatrix();
	}
}
