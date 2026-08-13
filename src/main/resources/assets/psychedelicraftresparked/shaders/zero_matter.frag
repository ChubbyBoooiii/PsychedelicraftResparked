#version 120

uniform sampler2D tex0;
uniform vec2 pixelSize;
uniform vec2 cellOffset;

varying vec4 vertexColor;

void main() {
    vec2 uv = fract((gl_FragCoord.xy + cellOffset) * pixelSize);
    vec4 texColor = texture2D(tex0, uv);
    gl_FragColor = texColor * vertexColor;
}