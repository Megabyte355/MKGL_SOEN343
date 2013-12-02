package bates.jamie.graphics.particle;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_LINE_LOOP;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Vec3;



public class ItemBoxParticle extends Particle
{
	private float[] color;
	private boolean fill;
	private boolean miniature;
	
	public ItemBoxParticle(Vec3 c, Vec3 t, float rotation, float[] color, boolean fill, boolean miniature)
	{
		super(c, t, rotation, 20);
		
		this.color = color;
		this.fill = fill;
		this.miniature = miniature;
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			gl.glTranslatef(c.x, c.y, c.z);
			gl.glRotatef(trajectory, 0, -1, 0);
			gl.glRotatef(rotation, 0, 0, 1);
			
			if(miniature) gl.glScalef(0.375f, 0.375f, 0.375f);
			else gl.glScalef(0.75f, 0.75f, 0.75f);
			
			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			gl.glDisable(GL_TEXTURE_2D);

			gl.glColor3fv(color, 0);

			if(fill)
			{
				gl.glBegin(GL_QUADS);
				{
					gl.glVertex3f(-0.5f, -0.5f, 0.0f);
					gl.glVertex3f(-0.5f,  0.5f, 0.0f);
					gl.glVertex3f( 0.5f,  0.5f, 0.0f);
					gl.glVertex3f( 0.5f, -0.5f, 0.0f);
				}
				gl.glEnd();
			}
			else
			{
				gl.glBegin(GL_LINE_LOOP);
				{
					gl.glVertex3f(-0.5f, -0.5f, 0.0f);
					gl.glVertex3f(-0.5f,  0.5f, 0.0f);
					gl.glVertex3f( 0.5f,  0.5f, 0.0f);
					gl.glVertex3f( 0.5f, -0.5f, 0.0f);
				}
				gl.glEnd();
			}

			gl.glEnable(GL_TEXTURE_2D);
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			gl.glDepthMask(true);
		}
		gl.glPopMatrix();
	}
}
