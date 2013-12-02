package bates.jamie.graphics.scene;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_TEXTURE_2D;

import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.Texture;

public class Model
{
	private List<float[]> _vertices;
	
	private FloatBuffer vertices;
	private FloatBuffer normals;
	private FloatBuffer texCoords;
	private FloatBuffer colors;
	
	int polygon;
	
	Texture texture;
	
	IntBuffer indices;
	
	int indexCount;
	
	public Model(List<float[]> vertices, List<float[]> normals, int[] vIndices, int[] nIndices, int type)
	{
		if(type == 3) polygon = GL2.GL_TRIANGLES;
		if(type == 4) polygon = GL2.GL_QUADS;
		
		float[] _normals = reorderNormals(vertices.size(), normals, vIndices, nIndices);
		
		this.vertices = Buffers.newDirectFloatBuffer(vertices.size() * 3);
		for(float[] vertex : vertices) this.vertices.put(vertex);
		this.vertices.position(0);  
		
		_vertices = vertices;
		
		indexCount = vIndices.length;
		
		this.normals = Buffers.newDirectFloatBuffer(vertices.size() * 3);
		this.normals.put(_normals);
		this.normals.position(0);
		
		System.out.printf("Indexed Model:\n{\n\tIndices:  %d\n\tVertices: %d\n\tNormals:  %d\n}\n", indexCount, vertices.size(), normals.size());
		
		indices = Buffers.newDirectIntBuffer(vIndices);
	}
	
	public Model(List<float[]> vertices, int[] vIndices, int type)
	{
		if(type == 3) polygon = GL2.GL_TRIANGLES;
		if(type == 4) polygon = GL2.GL_QUADS;
		
		this.vertices = Buffers.newDirectFloatBuffer(vertices.size() * 3);
		for(float[] vertex : vertices) this.vertices.put(vertex);
		this.vertices.position(0);  
		
		indexCount = vIndices.length;
		
		indices = Buffers.newDirectIntBuffer(vIndices);
	}
	
	public Model(List<float[]> vertices, List<float[]> texCoords, int[] vIndices, int[] tIndices, Texture texture, int type)
	{	
		this.texture = texture;
		
		if(type == 3) polygon = GL2.GL_TRIANGLES;
		if(type == 4) polygon = GL2.GL_QUADS;
		
		this.vertices = Buffers.newDirectFloatBuffer(vertices.size() * 3);
		for(float[] vertex : vertices) this.vertices.put(vertex);
		this.vertices.position(0);  
		
		indexCount = vIndices.length;
		
		this.texCoords = Buffers.newDirectFloatBuffer(vertices.size() * 2);
		for(float[] texCoord : texCoords) this.texCoords.put(texCoord);
		this.texCoords.position(0);
		
		System.out.printf("Indexed Model:\n{\n\tIndices:  %d\n\tVertices: %d\n\tTexture Coordinates: %d\n}\n", indexCount, vertices.size(), texCoords.size());
		
		indices = Buffers.newDirectIntBuffer(vIndices);
	}
	
	private float[] reorderNormals(int vertices, List<float[]> normals, int[] vIndices, int[] nIndices)
	{
		//each vertex has a normal that requires 3 components
		float[] _normals = new float[vertices * 3];
		
		//for each vertex
		for(int i = 0; i < nIndices.length; i++)
		{
			float[] normal = normals.get(nIndices[i]);
			
			int offset = vIndices[i] * 3;
			
			_normals[offset    ] = normal[0];
			_normals[offset + 1] = normal[1];
			_normals[offset + 2] = normal[2];
		}
		
		return _normals;
	}
	
	public float[] reorderTexCoords(int vertices, List<float[]> texCoords, int[] vIndices, int[] tIndices)
	{
		//each vertex has a texture coordinate that requires 2 components
		float[] _texCoords = new float[vertices * 2];
		
		for(int i = 0; i < vertices; i++)
		{
			float[] texCoord = texCoords.get(tIndices[i]);
			
			int offset = i * 2;
			
			_texCoords[offset    ] = texCoord[0];
			_texCoords[offset + 1] = texCoord[1];
		}
		
		return _texCoords;
	}
	
	public List<float[]> getVertices() { return _vertices; }
	
	public void setColorArray(List<float[]> colors)
	{
		this.colors = Buffers.newDirectFloatBuffer(colors.size() * 3);
		for(float[] color : colors) this.colors.put(color);
		this.colors.position(0);
	}
	
	public void render(GL2 gl)
	{
		if(texCoords == null) gl.glDisable(GL2.GL_TEXTURE_2D);
		else gl.glEnable(GL2.GL_TEXTURE_2D);
		
		gl.glEnableClientState(GL_VERTEX_ARRAY);
		if(normals   != null) gl.glEnableClientState(GL2.GL_NORMAL_ARRAY       );
		if(texCoords != null) gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		if(colors    != null) gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
		
		gl.glVertexPointer(3, GL2.GL_FLOAT, 0, vertices);
		if(normals   != null) gl.glNormalPointer(GL2.GL_FLOAT, 0, normals);
		if(colors    != null) gl.glColorPointer(3, GL2.GL_FLOAT, 0, colors);
		if(texCoords != null)
		{
			gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, texCoords);
			texture.bind(gl);
		}
		
		gl.glDrawElements(polygon, indexCount, GL2.GL_UNSIGNED_INT, indices);
		
		gl.glDisableClientState(GL_VERTEX_ARRAY);
		if(normals   != null) gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		if(texCoords != null) gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		if(colors    != null) gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
		
		gl.glEnable(GL_TEXTURE_2D);	
	}
	
	public void renderGlass(GL2 gl, float[] color)
	{
		gl.glDisable(GL_TEXTURE_2D);
		gl.glColor4f(color[0], color[1], color[2], 0.25f);
		
		gl.glDisable(GL_LIGHTING);
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glFrontFace(GL2.GL_CW ); render(gl);
		gl.glFrontFace(GL2.GL_CCW); render(gl);
		
		gl.glColor3f(1, 1, 1);
		gl.glEnable(GL_TEXTURE_2D);	
		
		gl.glDisable(GL_BLEND);
		gl.glEnable(GL_LIGHTING);
	}
}
