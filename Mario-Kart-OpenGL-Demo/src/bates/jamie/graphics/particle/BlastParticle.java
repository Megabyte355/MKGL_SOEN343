package bates.jamie.graphics.particle;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import javax.media.opengl.GL2;


import static bates.jamie.graphics.util.Vector.multiply;


public class BlastParticle extends Particle
{
	public static boolean pointSprite = true;
	
	public BlastParticle(float[] c, float[] t, float rotation, int duration)
	{
		super(c, t, rotation, duration);
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{		
			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			
			if(pointSprite)
			{
				gl.glColor4f(0, 0, 0.5f, 0.25f);
				
				gl.glEnable(GL2.GL_POINT_SMOOTH);
				gl.glPointSize(60);
				
				gl.glEnable(GL_POINT_SPRITE);
				gl.glTexEnvi(GL_POINT_SPRITE, GL_COORD_REPLACE, GL_TRUE);
				
				float[] p = this.c;
				
				float c = 2.0f / (duration + 1);
				c = (1 - c) * 0.9f;

				gl.glColor4f(c, c, c, c);
					
				indigoFlare.bind(gl);
				current = indigoFlare;
				
				gl.glBegin(GL2.GL_POINTS);
				gl.glVertex3f(p[0], p[1], p[2]);
				gl.glEnd();
				
				gl.glDisable(GL2.GL_POINT_SPRITE);
				
				gl.glColor4f(1, 1, 1, 1);
			}
			else
			{
				gl.glTranslatef(c[0], c[1], c[2]);
				gl.glRotatef(trajectory - 90, 0, 1, 0);
				gl.glScalef(15, 15, 15);
				
				float c = 2.0f / (duration + 1);
				c = (1 - c) * 0.9f;

				gl.glColor4f(c, c, c, c);
				
				if(!current.equals(indigoFlare))
				{
					indigoFlare.bind(gl);
					current = indigoFlare;
				}
				
				gl.glBegin(GL_QUADS);
				{
					gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-0.5f, -0.5f, 0.0f);
					gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-0.5f,  0.5f, 0.0f);
					gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 0.5f,  0.5f, 0.0f);
					gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.5f, -0.5f, 0.0f);
				}
				gl.glEnd();
			}

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
		t = multiply(t, 0.9f);
	}
}