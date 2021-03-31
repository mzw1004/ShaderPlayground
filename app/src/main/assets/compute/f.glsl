#version 310 es
precision mediump float;
out vec4 color;
layout(binding=0, offset=0) uniform atomic_uint ac;
void main() {
    vec3 col = vec3(0.);
    uint counter = atomicCounter(ac);
    float r = float(counter)/10000.;
    col += r;
    color = vec4(col, 1.);
}
