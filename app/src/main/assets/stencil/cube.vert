#version 300 es

uniform mat4 u_MVMatrix;
uniform mat4 u_MVPMatrix;

layout(location = 0) in vec4 a_Position;
layout(location = 1) in vec3 a_Normal;
layout(location = 2) in vec2 a_Texture;

out vec3 v_Position;
out vec3 v_Normal;
out vec2 v_Texture;

void main() {
    v_Position = (u_MVMatrix*a_Position).xyz;
    v_Normal = (u_MVMatrix*vec4(a_Normal, 0.)).xyz;
    v_Texture = a_Texture;
    gl_Position = u_MVPMatrix*a_Position;
}
