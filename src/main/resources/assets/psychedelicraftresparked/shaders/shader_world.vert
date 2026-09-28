#version 120

// Vertex-only program: fragment processing stays fixed-function, so everything that happens per pixel
const int GL_AMBIENT = 4608;
const int GL_DIFFUSE = 4609;
const int GL_SPECULAR = 4610;
const int GL_EMISSION = 5632;
const int GL_AMBIENT_AND_DIFFUSE = 5634;

uniform float ticks;

uniform float bigWaves;
uniform float smallWaves;
uniform float wiggleWaves;
uniform float distantWorldDeformation;

uniform vec3 playerPos;

uniform ivec4 texGenMode;

uniform int lightingEnabled;
uniform int lightEnabled[8];
uniform int colorMaterialMode;

float texGen(int mode, vec4 objectPlane, vec4 eyePlane, vec4 eyeVertex, float current) {
    if (mode == 1) return dot(objectPlane, gl_Vertex);
    if (mode == 2) return dot(eyePlane, eyeVertex);
    return current;
}

vec4 computeLighting(vec3 eyePos, vec3 normal) {
    vec4 ambientMat = gl_FrontMaterial.ambient;
    vec4 diffuseMat = gl_FrontMaterial.diffuse;
    vec4 specularMat = gl_FrontMaterial.specular;
    vec4 emissionMat = gl_FrontMaterial.emission;

    if (colorMaterialMode == GL_AMBIENT_AND_DIFFUSE) {
        ambientMat = gl_Color;
        diffuseMat = gl_Color;
    } else if (colorMaterialMode == GL_AMBIENT) {
        ambientMat = gl_Color;
    } else if (colorMaterialMode == GL_DIFFUSE) {
        diffuseMat = gl_Color;
    } else if (colorMaterialMode == GL_SPECULAR) {
        specularMat = gl_Color;
    } else if (colorMaterialMode == GL_EMISSION) {
        emissionMat = gl_Color;
    }

    vec3 color = emissionMat.rgb + gl_LightModel.ambient.rgb * ambientMat.rgb;

    for (int i = 0; i < 8; i++) {
        if (lightEnabled[i] == 0) {
            continue;
        }

        vec3 lightDir;
        float attenuation = 1.0;
        if (gl_LightSource[i].position.w == 0.0) {
            lightDir = normalize(gl_LightSource[i].position.xyz);
        } else {
            vec3 toLight = gl_LightSource[i].position.xyz / gl_LightSource[i].position.w - eyePos;
            float dist = length(toLight);
            lightDir = toLight / dist;
            attenuation = 1.0 / (gl_LightSource[i].constantAttenuation
                + gl_LightSource[i].linearAttenuation * dist
                + gl_LightSource[i].quadraticAttenuation * dist * dist);

            if (gl_LightSource[i].spotCutoff != 180.0) {
                float spotDot = dot(-lightDir, normalize(gl_LightSource[i].spotDirection));
                attenuation *= spotDot >= gl_LightSource[i].spotCosCutoff ? pow(max(spotDot, 0.0), gl_LightSource[i].spotExponent) : 0.0;
            }
        }

        float nDotL = max(dot(normal, lightDir), 0.0);
        vec3 contribution = gl_LightSource[i].ambient.rgb * ambientMat.rgb
            + nDotL * gl_LightSource[i].diffuse.rgb * diffuseMat.rgb;

        if (nDotL > 0.0) {
            // Non-local viewer, so the eye direction is always +Z
            vec3 halfVector = normalize(lightDir + vec3(0.0, 0.0, 1.0));
            float nDotH = max(dot(normal, halfVector), 0.0);
            float specular = gl_FrontMaterial.shininess > 0.0 ? pow(nDotH, gl_FrontMaterial.shininess) : 1.0;
            contribution += specular * gl_LightSource[i].specular.rgb * specularMat.rgb;
        }

        color += attenuation * contribution;
    }

    return vec4(clamp(color, 0.0, 1.0), clamp(diffuseMat.a, 0.0, 1.0));
}

void main() {
    gl_Position = ftransform();
    vec4 eyeVertex = gl_ModelViewMatrix * gl_Vertex;
    gl_ClipVertex = eyeVertex;

    if (lightingEnabled == 1) {
        gl_FrontColor = computeLighting(eyeVertex.xyz / eyeVertex.w, normalize(gl_NormalMatrix * gl_Normal));
    } else {
        gl_FrontColor = gl_Color;
    }
    gl_BackColor = gl_FrontColor;
    gl_FrontSecondaryColor = gl_SecondaryColor;
    gl_BackSecondaryColor = gl_SecondaryColor;

    vec4 texCoord0 = gl_MultiTexCoord0;
    texCoord0.s = texGen(texGenMode.x, gl_ObjectPlaneS[0], gl_EyePlaneS[0], eyeVertex, texCoord0.s);
    texCoord0.t = texGen(texGenMode.y, gl_ObjectPlaneT[0], gl_EyePlaneT[0], eyeVertex, texCoord0.t);
    texCoord0.p = texGen(texGenMode.z, gl_ObjectPlaneR[0], gl_EyePlaneR[0], eyeVertex, texCoord0.p);
    texCoord0.q = texGen(texGenMode.w, gl_ObjectPlaneQ[0], gl_EyePlaneQ[0], eyeVertex, texCoord0.q);
    gl_TexCoord[0] = gl_TextureMatrix[0] * texCoord0;
    gl_TexCoord[1] = gl_TextureMatrix[1] * gl_MultiTexCoord1;
    gl_TexCoord[2] = gl_TextureMatrix[2] * gl_MultiTexCoord2;
    gl_TexCoord[3] = gl_TextureMatrix[3] * gl_MultiTexCoord3;

    gl_FogFragCoord = length(eyeVertex.xyz);

    if (smallWaves > 0.0) {
        float w1 = 8.0;

        gl_Position[1] += sin((gl_Vertex[0] + ticks / 5.0) / w1 * 3.14159 * 2.0) * sin((gl_Vertex[2] + ticks / 5.0) / w1 * 3.14159 * 2.0) * smallWaves * 1.5;
        gl_Position[1] -= sin((playerPos.x + ticks / 5.0) / w1 * 3.14159 * 2.0) * sin((playerPos.z + ticks / 5.0) / w1 * 3.14159 * 2.0) * smallWaves * 1.5;

        float w2 = 16.0;

        gl_Position[1] += sin((gl_Vertex[0] + ticks / 8.0) / w2 * 3.14159 * 2.0) * sin((gl_Vertex[2]) / w2 * 3.14159 * 2.0) * smallWaves * 3.0;
        gl_Position[1] -= sin((playerPos.x + ticks / 8.0) / w2 * 3.14159 * 2.0) * sin((playerPos.z) / w2 * 3.14159 * 2.0) * smallWaves * 3.0;

        gl_Position[0] = mix(gl_Position[0], gl_Position[0] * (1.0 + gl_FogFragCoord / 20.0), smallWaves);
        gl_Position[1] = mix(gl_Position[1], gl_Position[1] * (1.0 + gl_FogFragCoord / 20.0), smallWaves);
    }

    if (wiggleWaves > 0.0) {
        float w1 = 8.0;

        gl_Position[0] += sin((gl_Vertex[1] + ticks / 8.0) / w1 * 3.14159 * 2.0) * sin((gl_Vertex[2] + ticks / 5.0) / w1 * 3.14159 * 2.0) * wiggleWaves;
    }

    if (distantWorldDeformation > 0.0 && gl_FogFragCoord > 5.0) {
        gl_Position[1] += (sin(gl_FogFragCoord / 8.0 * 3.14159 * 2.0) + 1.0) * distantWorldDeformation * (gl_FogFragCoord - 5.0) / 8.0;
    }

    if (bigWaves > 0.0) {
        if (gl_Position[2] > 0.1) {
            float dDist = (gl_Position[2] - 0.1) * bigWaves;
            if (gl_Position[2] > 20.0) {
                dDist = (20.0 - 0.1) * bigWaves + (gl_Position[2] - 20.0) * bigWaves * 0.3;
            }

            float inf1 = sin(ticks * 0.0086465563) * dDist;
            float inf2 = cos(ticks * 0.0086465563) * dDist;
            float inf3 = sin(ticks * 0.0091033941) * dDist;
            float inf4 = cos(ticks * 0.0091033941) * dDist;
            float inf5 = sin(ticks * 0.0064566190) * dDist;
            float inf6 = cos(ticks * 0.0064566190) * dDist;

            float pMul = 1.3;

            gl_Position[0] += sin(gl_Position[2] * 0.1 * sin(ticks * 0.001849328) + ticks * 0.014123412) * 0.5 * inf1 * pMul;
            gl_Position[1] += cos(gl_Position[2] * 0.1 * sin(ticks * 0.001234728) + ticks * 0.017481893) * 0.4 * inf1 * pMul;

            gl_Position[0] += sin(gl_Position[1] * 0.1 * sin(ticks * 0.001523784) + ticks * 0.021823911) * 0.2 * inf2 * pMul;
            gl_Position[1] += sin(gl_Position[0] * 0.1 * sin(ticks * 0.001472387) + ticks * 0.023193141) * 0.08 * inf2 * pMul;

            gl_Position[0] += sin(gl_Position[2] * 0.15 * sin(ticks * 0.001284923) + ticks * 0.019404289) * 0.25 * inf3 * pMul;
            gl_Position[1] += cos(gl_Position[2] * 0.15 * sin(ticks * 0.001482938) + ticks * 0.018491238) * 0.15 * inf3 * pMul;

            gl_Position[0] += sin(gl_Position[1] * 0.05 * sin(ticks * 0.001283942) + ticks * 0.012942342) * 0.4 * inf4 * pMul;
            gl_Position[1] += sin(gl_Position[0] * 0.05 * sin(ticks * 0.001829482) + ticks * 0.012981328) * 0.35 * inf4 * pMul;

            gl_Position[2] += sin(gl_Position[1] * 0.13 * sin(ticks * 0.02834472) + ticks * 0.023482934) * 0.1 * inf5 * pMul;
            gl_Position[2] += sin(gl_Position[0] * 0.124 * sin(ticks * 0.00184298) + ticks * 0.018394082) * 0.05 * inf6 * pMul;
            gl_Position[3] += sin(gl_Position[1] * 0.13 * sin(ticks * 0.02834472) + ticks * 0.023482934) * 0.1 * inf5 * pMul;
            gl_Position[3] += sin(gl_Position[0] * 0.124 * sin(ticks * 0.00184298) + ticks * 0.018394082) * 0.05 * inf6 * pMul;
        }
    }
}