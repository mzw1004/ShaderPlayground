#version 300 es

uniform vec3 u_Color;
uniform int u_Mask;

in vec3 v_Position;
in vec3 v_Normal;
in vec2 v_Texture;
out vec4 outColor;

void main() {
    vec3 viewDir = normalize(-v_Position);
    float v = dot(viewDir, v_Normal);
    if (u_Mask == 1) {
        // write to stencil buffer
        if (v > 0.) {
            outColor = vec4(1.);
        } else {
            discard;
        }
    } else {
        // render to color buffer
        if (v > 0.) {
            vec2 uv = v_Texture*2.-1.;

            if (length(uv) < .85) {
                discard;
            }
        }
        outColor = vec4(u_Color, 1.);
    }
}
