package bates.jamie.graphics.particle;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.io.File;
import java.util.Random;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Vec3;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;


public class LightningParticle extends Particle
{
	private static Texture lightning;
	
	static
	{
		try { lightning = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "lightning.png"), true); }
		catch (Exception e) { e.printStackTrace(); }
	}

	public LightningParticle(Vec3 c)
	{
		super(c, new Vec3(), 0, 20);
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			Random generator = new Random();
			
			gl.glTranslatef(c.x, c.y, c.z);
			gl.glRotatef(trajectory, 0, -1, 0);
			gl.glScalef(15, 50, 5);
			
			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			
			float c = 2.0f / (duration + 1); 

			gl.glColor4f(1 - c, 1 - c, 1 - c, 1);
			
			lightning.bind(gl);

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
