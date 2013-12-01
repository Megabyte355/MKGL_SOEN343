// ADS Point Lighting Shader (Phong)

// Color to fragment program
varying vec3 vNormal;
varying vec3 lightDir;
varying vec3 worldPos;

uniform mat4 ModelMatrix;

void main(void) 
{ 
	worldPos = vec3(ModelMatrix * gl_Vertex);  

	// Don't forget to transform the geometry!
    gl_Position = ftransform();
	
    // Get surface normal in eye coordinates
    vNormal = gl_NormalMatrix * gl_Normal;
	vNormal = normalize(vNormal);

    // Get vertex position in eye coordinates
    vec4 position4 = gl_ModelViewMatrix * gl_Vertex;
    vec3 position3 = (vec3(position4)) / position4.w;

    // Get vector to light source
    lightDir = normalize(gl_LightSource[0].position.xyz - position3);
	
	gl_FrontColor = gl_Color;

    gl_TexCoord[0] = gl_MultiTexCoord0;
}
