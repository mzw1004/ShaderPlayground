#version 300 es
precision mediump float;

uniform sampler2D u_Material;

in vec3 v_ViewPosition;
in vec3 v_ViewNormal;
in vec2 v_TexCoord;

out vec4 outColor;

void main() {
     outColor = texture(u_Material, vec2(v_TexCoord.x, 1.0 - v_TexCoord.y));
//    outColor = vec4(1., 0., 0., 1.);
}
