#version 120

uniform sampler2D tex0;
uniform sampler2D worldDepthTex;
uniform sampler2D handDepthTex;
uniform sampler2D fractalTex;

uniform mat4 invWorldProjection;
uniform mat4 invWorldModelView;
uniform mat4 invHandProjection;
uniform vec3 cameraPosMod4;

uniform float ticks;
uniform vec4 fractal0TexCoords;
uniform float surfaceFractal;
uniform vec4 pulses;
uniform vec4 worldColorization;

vec3 unproject(mat4 invProjection, vec2 uv, float depth) {
    vec4 eye = invProjection * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return eye.xyz / eye.w;
}

float fractalCoord(float value) {
    float base = floor(value);
    return mix(mod(base, 4.0), mod(base + 1.0, 4.0), value - base) / 4.0;
}

void main() {
    vec2 uv = gl_TexCoord[0].st;
    vec4 color = texture2D(tex0, uv);

    float handDepth = texture2D(handDepthTex, uv).r;
    float worldDepth = texture2D(worldDepthTex, uv).r;

    vec3 fractalPos;
    float dist;
    if (handDepth < 1.0) {
        vec3 eyePos = unproject(invHandProjection, uv, handDepth);
        dist = length(eyePos);
        fractalPos = eyePos;
    } else if (worldDepth < 1.0) {
        vec3 eyePos = unproject(invWorldProjection, uv, worldDepth);
        dist = length(eyePos);
        fractalPos = (invWorldModelView * vec4(eyePos, 1.0)).xyz + cameraPosMod4;
    } else {
        gl_FragColor = color;
        return;
    }

    if (surfaceFractal > 0.0) {
        vec2 fractalCoords = vec2(
            mix(fractal0TexCoords[0], fractal0TexCoords[2], fractalCoord(fractalPos.x + fractalPos.y)),
            mix(fractal0TexCoords[1], fractal0TexCoords[3], fractalCoord(fractalPos.z + fractalPos.y))
        );
        vec4 fractalColor = texture2D(fractalTex, fractalCoords);
        float avg = (fractalColor.r + fractalColor.g + fractalColor.b) / 3.0;
        color.rgb = mix(color.rgb, getRotatedColor(color.rgb, avg * surfaceFractal), surfaceFractal);
    }

    if (pulses.a > 0.0) {
        float pulseA = (sin((dist - ticks) / 5.0) - 0.4) * pulses.a;
        if (pulseA > 0.0) {
            color.rgb = mix(color.rgb, (color.rgb + 1.0) * pulses.rgb, pulseA);
        }
    }

    if (worldColorization.a > 0.0) {
        vec3 c1 = color.rgb;
        vec3 c2 = worldColorization.rgb;

        float distR = sqrt((c1.r - c2.r) * (c1.r - c2.r));
        float distG = sqrt((c1.g - c2.g) * (c1.g - c2.g));
        float distB = sqrt((c1.b - c2.b) * (c1.b - c2.b));

        float colorDist = clamp((distR + distG + distB), 0.0, 1.0);
        for (int i = 0; i < 4; i++) {
            colorDist *= colorDist;
        }
        float harmonizeStrength = colorDist * 3.0;

        float disk = (sin((dist - ticks) * 0.1434234) - 0.4) * 0.8;
        harmonizeStrength += disk;

        float disk2 = (sin((dist - ticks) * -0.12313) - 0.2) * 0.8;
        harmonizeStrength += disk2;

        float disk3 = sin((dist - ticks) * -0.051233) * 0.2 * sin(ticks * 0.1321334);
        harmonizeStrength += disk3;

        harmonizeStrength = clamp(harmonizeStrength, 0.0, 3.0);

        vec3 harmonizedColor;

        if (harmonizeStrength < 1.0) {
            harmonizedColor = mix(worldColorization.rgb, vec3(0.5), harmonizeStrength); // Max of 1.0
        } else {
            harmonizedColor = mix(vec3(0.5), vec3(1.0) - worldColorization.rgb, (harmonizeStrength - 1.0) * 0.5); // Max of 2.0
        }

        color.rgb = mix(color.rgb, harmonizedColor, worldColorization.a);
    }

    gl_FragColor = clamp(color, 0.0, 1.0);
}