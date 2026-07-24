#version 120

uniform sampler2D tex0;
uniform vec2 pixelSize;
uniform int vertical;
uniform float totalAlpha;

void main()
{
    // Get original colour
    gl_FragColor = texture2D(tex0, gl_TexCoord[0].st);
    vec4 newColor = gl_FragColor;
    vec3 bloomColor = vec3(0.0);

    // Direction vector: horizontal (0) or vertical (1)
    vec2 dirVec = vec2((vertical == 0) ? pixelSize.x : 0.0, (vertical == 1) ? pixelSize.y : 0.0) * 3.0;
    
    // Calculate bloom intensity based on pixel brightness
    float colorInfluence = 15.0 / (5.0 + newColor.r + newColor.g + newColor.b);
    colorInfluence = colorInfluence * colorInfluence;
    
    // Sample neighbours in both directions
    for(float i = -1.0; i < 2.0; i += 2.0) {
        vec2 activeDirVec = i * dirVec;
        
        // Sample at increasing distances with decreasing weights
        bloomColor += texture2D(tex0, clamp(gl_TexCoord[0].st + 1.0 * activeDirVec, 0.0, 1.0)).rgb * 0.028 * colorInfluence;
        bloomColor += texture2D(tex0, clamp(gl_TexCoord[0].st + 2.0 * activeDirVec, 0.0, 1.0)).rgb * 0.020 * colorInfluence;
        bloomColor += texture2D(tex0, clamp(gl_TexCoord[0].st + 3.0 * activeDirVec, 0.0, 1.0)).rgb * 0.016 * colorInfluence;
        bloomColor += texture2D(tex0, clamp(gl_TexCoord[0].st + 4.0 * activeDirVec, 0.0, 1.0)).rgb * 0.012 * colorInfluence;
    }
    
    // Cube the bloom colour for more intensity
    newColor.rgb += bloomColor * bloomColor * bloomColor;
        
    // Mix with original based on totalAlpha
    gl_FragColor = mix(gl_FragColor, newColor, totalAlpha);
}