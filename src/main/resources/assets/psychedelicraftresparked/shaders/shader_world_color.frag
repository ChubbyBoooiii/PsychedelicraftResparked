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

uniform int skyMode;
uniform float skyHorizonOffset;
uniform float fakeSkyboxDistance;

const int SKY_NONE = 0;
const int SKY_FAKE_BOX = 1;
const int SKY_SURFACE = 2;
const int SKY_END = 3;

vec3 unproject(mat4 invProjection, vec2 uv, float depth) {
    vec4 eye = invProjection * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return eye.xyz / eye.w;
}

float fractalCoord(float value) {
    float base = floor(value);
    return mix(mod(base, 4.0), mod(base + 1.0, 4.0), value - base) / 4.0;
}

bool hitSkyPlane(vec3 dir, float height, out vec3 hit) {
    if (dir.y * height <= 0.0) {
        return false;
    }
    hit = dir * (height / dir.y);
    return hit.x >= -384.0 && hit.x <= 448.0 && hit.z >= -384.0 && hit.z <= 448.0;
}

float skyPlaneDistance(vec3 hit, float height, bool reverseX) {
    float k = min(floor((hit.x + 384.0) / 64.0), 12.0) * 64.0 - 384.0;
    float l = min(floor((hit.z + 384.0) / 64.0), 12.0) * 64.0 - 384.0;
    float u = (hit.x - k) / 64.0;
    float v = (hit.z - l) / 64.0;

    float x0 = k;
    float x1 = k + 64.0;
    if (reverseX) {
        x0 = k + 64.0;
        x1 = k;
        u = 1.0 - u;
    }

    float d0 = length(vec3(x0, height, l));
    float d1 = length(vec3(x1, height, l));
    float d2 = length(vec3(x1, height, l + 64.0));
    float d3 = length(vec3(x0, height, l + 64.0));

    if (u >= v) {
        return d0 + u * (d1 - d0) + v * (d2 - d1);
    }
    return d0 + v * (d3 - d0) + u * (d2 - d3);
}

float skyDistance(vec3 dir, out bool colorSafe, out vec2 fractalUV) {
    float dist = fakeSkyboxDistance;
    colorSafe = true;
    fractalUV = vec2(0.0);

    if (skyMode == SKY_END) {
        colorSafe = false;
        return 100.0 * sqrt(3.0);
    }

    if (skyMode == SKY_SURFACE) {
        vec3 hit;
        if (hitSkyPlane(dir, 16.0, hit)) {
            dist = skyPlaneDistance(hit, 16.0, false);
            colorSafe = false;
        }

        if (skyHorizonOffset < 0.0) {
            if (hitSkyPlane(dir, -4.0, hit)) {
                dist = skyPlaneDistance(hit, -4.0, true);
                colorSafe = false;
            }
            if (dir.y < 0.0) {
                hit = dir * (-1.0 / dir.y);
                if (abs(hit.x) <= 1.0 && abs(hit.z) <= 1.0) {
                    dist = sqrt(3.0);
                    colorSafe = false;
                    fractalUV = vec2(1.0 - hit.x, 1.0 - hit.z) / 4.0;
                }
            }
        }

        if (hitSkyPlane(dir, -skyHorizonOffset, hit)) {
            dist = skyPlaneDistance(hit, -skyHorizonOffset, true);
            colorSafe = false;
            fractalUV = vec2(0.0);
        }
    }

    return dist;
}

void main() {
    vec2 uv = gl_TexCoord[0].st;
    vec4 color = texture2D(tex0, uv);

    float handDepth = texture2D(handDepthTex, uv).r;
    float worldDepth = texture2D(worldDepthTex, uv).r;

    vec2 fractalUV;
    float dist;
    bool colorSafe = false;

    if (handDepth < 1.0) {
        vec3 eyePos = unproject(invHandProjection, uv, handDepth);
        dist = length(eyePos);
        fractalUV = vec2(fractalCoord(eyePos.x + eyePos.y), fractalCoord(eyePos.z + eyePos.y));
    } else if (worldDepth < 1.0) {
        vec3 eyePos = unproject(invWorldProjection, uv, worldDepth);
        dist = length(eyePos);
        vec3 fractalPos = (invWorldModelView * vec4(eyePos, 1.0)).xyz + cameraPosMod4;
        fractalUV = vec2(fractalCoord(fractalPos.x + fractalPos.y), fractalCoord(fractalPos.z + fractalPos.y));
    } else if (skyMode != SKY_NONE) {
        vec3 dir = normalize(mat3(invWorldModelView) * unproject(invWorldProjection, uv, 1.0));
        dist = skyDistance(dir, colorSafe, fractalUV);
    } else {
        gl_FragColor = color;
        return;
    }

    if (surfaceFractal > 0.0) {
        vec2 fractalCoords = vec2(
            mix(fractal0TexCoords[0], fractal0TexCoords[2], fractalUV.x),
            mix(fractal0TexCoords[1], fractal0TexCoords[3], fractalUV.y)
        );
        vec4 fractalColor = texture2D(fractalTex, fractalCoords);
        float avg = (fractalColor.r + fractalColor.g + fractalColor.b) / 3.0;
        color.rgb = mix(color.rgb, getRotatedColor(color.rgb, avg * surfaceFractal), surfaceFractal);
    }

    if (!colorSafe && pulses.a > 0.0) {
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

        if (colorSafe) {
            harmonizedColor *= color.rgb;
        }

        color.rgb = mix(color.rgb, harmonizedColor, worldColorization.a);
    }

    gl_FragColor = clamp(color, 0.0, 1.0);
}