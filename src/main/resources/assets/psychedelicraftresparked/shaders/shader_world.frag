#version 120

uniform sampler2D texture;
uniform sampler2D lightmapTex;

uniform sampler2D texFractal0;
varying vec2 texFractal0Coords;

const int GL_LINEAR = 9729;
const int GL_EXP = 2048;

uniform float ticks;
uniform vec2 pixelSize;

uniform int fogMode;
uniform int fogEnabled;
uniform int lightmapEnabled;
uniform int lightingEnabled;
uniform int texture2DEnabled;
uniform vec4 overrideColor;
uniform int useScreenTexCoords;
uniform ivec4 texGenMode;

uniform vec4 pulses;
uniform float surfaceFractal;
uniform vec4 worldColorization;

varying vec3 relativeVertex;
varying vec3 normalVector;
varying vec4 projGLPos;

uniform float depthMultiplier;

uniform int colorSafeMode;

void main() {
    if (texture2DEnabled == 1) {
        if (useScreenTexCoords == 1) {
            gl_FragColor = texture2D(texture, (gl_FragCoord.xy + gl_TexCoord[0].st) * pixelSize);
        } else if (any(notEqual(texGenMode, ivec4(0)))) {
            gl_FragColor = texture2DProj(texture, gl_TexCoord[0]);
        } else {
            gl_FragColor = texture2D(texture, gl_TexCoord[0].st);
        }
    } else {
        gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
    }

    gl_FragColor *= gl_Color;
    gl_FragColor *= overrideColor;

    if (lightmapEnabled == 1) {
        vec4 lighting = texture2D(lightmapTex, gl_TexCoord[1].st);
        gl_FragColor.rgb *= lighting.rgb;
    }

    if (lightingEnabled == 1) {
        vec3 finalLight = gl_LightModel.ambient.rgb;
        for (int i = 0; i < 2; i++) {
            vec3 lightVec = normalize(gl_LightSource[i].position.xyz);
            finalLight += gl_FrontLightProduct[i].diffuse.rgb * max(dot(normalVector, lightVec), 0.0);
        }
        gl_FragColor.rgb *= clamp(finalLight, 0.0, 1.0);
    }

    gl_FragColor = clamp(gl_FragColor, 0.0, 1.0);

    if (fogEnabled == 1) {
        if (fogMode == GL_EXP) {
            gl_FragColor.rgb = mix(gl_FragColor.rgb, gl_Fog.color.rgb, 1.0 - clamp(exp(-gl_Fog.density * gl_FogFragCoord), 0.0, 1.0));
        } else if (fogMode == GL_LINEAR) {
            gl_FragColor.rgb = mix(gl_FragColor.rgb, gl_Fog.color.rgb, clamp((gl_FogFragCoord - gl_Fog.start) * gl_Fog.scale, 0.0, 1.0));
        }
    }

    if (surfaceFractal > 0.0) {
        vec4 fractalColor = texture2D(texFractal0, texFractal0Coords);
        float avg = (fractalColor.r + fractalColor.g + fractalColor.b) / 3.0;
        gl_FragColor.rgb = mix(gl_FragColor.rgb, getRotatedColor(gl_FragColor.rgb, avg * surfaceFractal), surfaceFractal);
    }

    if (colorSafeMode == 0 && pulses.a > 0.0) {
        float pulseA = (sin((gl_FogFragCoord - ticks) / 5.0) - 0.4) * pulses.a;
        if (pulseA > 0.0) {
            gl_FragColor.rgb = mix(gl_FragColor.rgb, (gl_FragColor.rgb + 1.0) * pulses.rgb, pulseA);
        }
    }

    if (worldColorization.a > 0.0) {
        vec3 c1 = gl_FragColor.rgb;
        vec3 c2 = worldColorization.rgb;

        float distR = sqrt((c1.r - c2.r) * (c1.r - c2.r));
        float distG = sqrt((c1.g - c2.g) * (c1.g - c2.g));
        float distB = sqrt((c1.b - c2.b) * (c1.b - c2.b));

        float dist = clamp((distR + distG + distB), 0.0, 1.0);
        for (int i = 0; i < 4; i++) {
            dist *= dist;
        }
        float harmonizeStrength = dist * 3.0;

        float disk = (sin((gl_FogFragCoord - ticks) * 0.1434234) - 0.4) * 0.8;
        harmonizeStrength += disk;

        float disk2 = (sin((gl_FogFragCoord - ticks) * -0.12313) - 0.2) * 0.8;
        harmonizeStrength += disk2;

        float disk3 = sin((gl_FogFragCoord - ticks) * -0.051233) * 0.2 * sin(ticks * 0.1321334);
        harmonizeStrength += disk3;

        harmonizeStrength = clamp(harmonizeStrength, 0.0, 3.0);

        vec3 harmonizedColor;

        if (harmonizeStrength < 1.0) {
            harmonizedColor = mix(worldColorization.rgb, vec3(0.5), harmonizeStrength); // Max of 1.0
        } else {
            harmonizedColor = mix(vec3(0.5), vec3(1.0) - worldColorization.rgb, (harmonizeStrength - 1.0) * 0.5); // Max of 2.0
        }

        if (colorSafeMode != 0) { // Make sure we don't add brightness
            harmonizedColor *= gl_FragColor.rgb;
        }

        gl_FragColor.rgb = mix(gl_FragColor.rgb, harmonizedColor, worldColorization.a);
    }

    gl_FragColor = clamp(gl_FragColor, 0.0, 1.0);
    gl_FragDepth = gl_FragCoord.z * depthMultiplier;
}