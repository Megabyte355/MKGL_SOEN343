package bates.jamie.graphics.util;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;
import static java.lang.Math.toRadians;

public class RotationMatrix
{
	public Vec3 xAxis = Vec3.POSITIVE_X_AXIS;
	public Vec3 yAxis = Vec3.POSITIVE_Y_AXIS;
	public Vec3 zAxis = Vec3.POSITIVE_Z_AXIS;
	
	public RotationMatrix() {}
	
	public RotationMatrix(float[][] matrix)
	{
		xAxis = new Vec3(matrix[0]);
		yAxis = new Vec3(matrix[1]);
		zAxis = new Vec3(matrix[2]);
	}
	
	public RotationMatrix(float x, float y, float z)
	{
		x = (float) toRadians(x);
		y = (float) toRadians(y);
		z = (float) toRadians(z);
		
		float[][] rx =
			{{    1    ,    0    ,    0    },
			 {    0    ,  cosf(x), -sinf(x)},
			 {    0    ,  sinf(x),  cosf(x)}};
		
		float[][] ry =
			{{  cosf(y),    0    ,  sinf(y)},
			 {    0    ,    1    ,    0    },
			 { -sinf(y),    0    ,  cosf(y)}};
		
		float[][] rz =
			{{  cosf(z), -sinf(z),    0    },
			 {  sinf(z),  cosf(z),    0    },
			 {    0    ,    0    ,    1    }};
		
		float[][] u = multiply(multiply(rz, rx), ry);
		
		xAxis = new Vec3(u[0]);
		yAxis = new Vec3(u[1]);
		zAxis = new Vec3(u[2]);
	}
	
	public static void main(String[] args)
	{
		RotationMatrix m = new RotationMatrix(45, 90, 0);
		
		System.out.println(m.xAxis);
		System.out.println(m.yAxis);
		System.out.println(m.zAxis);
	}
	
	public RotationMatrix(Vec3 axis, float theta)
	{
		Vec3 u = axis;
		
		float  c = cosf(toRadians(theta));
		float _c = 1 - c;
		float  s = sinf(toRadians(theta));
		
		float u0 = u.x * u.x;
		float u1 = u.y * u.y;
		float u2 = u.z * u.z;

		xAxis = new Vec3(             u0 * _c + c, u.x * u.y * _c + u.z * s, u.x * u.z * _c - u.y * s);
	    yAxis = new Vec3(u.x * u.y * _c - u.z * s,              u1 * _c + c, u.y * u.z * _c + u.x * s);
	    zAxis = new Vec3(u.x * u.z * _c + u.y * s, u.y * u.z * _c - u.x * s,              u2 * _c + c);
	}
	
	public static float[][] multiply(float[][] a, float[][] b)
	{
		int rows = a.length;
		int columns = b[0].length;
		int n = a[0].length;
		
		float[][] c = new float[columns][rows];
		
		for(int i = 0; i < rows; i++)
		{
			for(int j = 0; j < columns; j++)
			{
				c[i][j] = 0;
				
				for(int k = 0; k < n; k++)
					c[i][j] += (a[i][k] * b[k][j]);
			}
		}
		
		return c;
	}
	
	public Vec3 getAxis(int i)
	{
		switch(i)
		{
			case 0: return xAxis;
			case 1: return yAxis;
			case 2: return zAxis;
		}
		
		return new Vec3();
	}
	
	public float[] toArray()
	{
		return new float[]
		{
			xAxis.x, xAxis.y, xAxis.z,   0   ,
			yAxis.x, yAxis.y, yAxis.z,   0   ,
		    zAxis.x, zAxis.y, zAxis.z,   0   ,
			   0   ,    0   ,    0   ,   1
		};
	}
	
//	public float[] toArray()
//	{	
//		return new float[]		
//		{
//			xAxis.x, yAxis.x, zAxis.x,   0   ,
//			xAxis.y, yAxis.y, zAxis.y,   0   ,
//		    xAxis.z, yAxis.z, zAxis.z,   0   ,
//			   0   ,    0   ,    0   ,   1
//		};
//	}
	
	public static float sinf(double a) { return (float) sin(a); }
	public static float cosf(double a) { return (float) cos(a); }
	public static float tanf(double a) { return (float) tan(a); }
}
