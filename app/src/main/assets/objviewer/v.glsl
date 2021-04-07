#version 300 es

uniform mat4 u_MVMatrix;
uniform mat4 u_MVPMatrix;
uniform vec3 u_Light;

layout(location = 0) in vec4 a_Position;
layout(location = 1) in vec2 a_TexCoord;
layout(location = 2) in vec3 a_Normal;

out vec3 v_ViewPosition;
out vec3 v_ViewNormal;
out vec2 v_TexCoord;
out vec3 v_Light;

void main() {
    v_ViewPosition = (u_MVMatrix*a_Position).xyz;
    v_ViewNormal = (u_MVMatrix*vec4(a_Normal, 0.)).xyz;
    v_TexCoord = a_TexCoord;
    v_Light = (u_MVMatrix*vec4(u_Light, 1.)).xyz;
    gl_Position = u_MVPMatrix*a_Position;
}
