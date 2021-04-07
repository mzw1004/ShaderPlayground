#version 300 es

uniform vec3 u_Color;
in vec3 v_Position;
in vec3 v_Normal;
out vec4 outColor;

void main() {
    vec3 view = -v_Position;
    float v = dot(view, v_Normal);
    if (v > 0.) {
        discard;
    }
    outColor = vec4(u_Color, 1.);
}
