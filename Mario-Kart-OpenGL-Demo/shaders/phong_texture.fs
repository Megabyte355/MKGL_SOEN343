// ADS Point Lighting Shader (Phong)

varying vec3 vNormal;
varying vec3 lightDir;

uniform sampler2D texture;

void main(void)
{ 
    // Dot product gives us diffuse intensity
    float diff = max(0.0, dot(normalize(vNormal), normalize(lightDir)));

    // Multiply intensity by diffuse color, force alpha to 1.0
    vec4 vFragColor = diff * gl_LightSource[0].diffuse;

    // Add in ambient light
    vFragColor += gl_LightSource[0].ambient;
	
	vFragColor *= texture2D(texture, gl_TexCoord[0].st);

    // Specular Light
	vec3 vReflection = normalize(reflect(-normalize(lightDir), normalize(vNormal)));
    float spec = max(0.0, dot(normalize(vNormal), vReflection));
    if(diff != 0.0)
	{
        float fSpec = pow(spec, 128.0);
        vFragColor.rgb += gl_LightSource[0].specular.rgb * fSpec;
    }
	
	vFragColor.rgb *= gl_Color.rgb;
	
	gl_FragColor = vFragColor;
}
