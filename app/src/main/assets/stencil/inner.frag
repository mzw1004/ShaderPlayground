#version 300 es

in vec3 v_Position;
in vec3 v_Normal;
out vec4 outColor;

void main() {
    outColor = vec4(v_Normal, 1.);
}
