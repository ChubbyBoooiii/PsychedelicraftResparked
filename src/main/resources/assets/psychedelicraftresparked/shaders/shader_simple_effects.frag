#version 120

uniform sampler2D tex0;
uniform float colorIntensification;
uniform float desaturation;
uniform float ticks;
uniform float slowColorRotation;
uniform float quickColorRotation;

void main() {
    vec4 texColor = texture2D(tex0, gl_TexCoord[0].st);
    vec3 result = texColor.rgb;

    if (slowColorRotation > 0.0) {
        vec3 rotated = getRotatedColor(result, mod(ticks, 300.0) / 300.0);
        result = mix(result, rotated, slowColorRotation / 2.0);
    }

    if (quickColorRotation > 0.0) {
        vec3 rotated = getRotatedColor(result, mod(ticks, 50.0) / 50.0);
        result = mix(result, rotated, clamp(quickColorRotation * 1.5, 0.0, 1.0));
    }

    if (colorIntensification > 0.0) {
        vec3 intensified = getIntensifiedColor(result);
        result = mix(result, intensified, colorIntensification);
    }

    if (desaturation > 0.0) {
        vec3 desaturated = getDesaturatedColor(result);
        result = mix(result, desaturated, desaturation);
    }

    gl_FragColor = vec4(result, 1.0);
}