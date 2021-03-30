#version 310 es

precision highp float;
out vec4 color;
uniform vec2 uResolution;
layout(binding=0, offset=0) uniform atomic_uint ac;

void main() {
    vec3 col = vec3(0.);
    uint counter = atomicCounterIncrement(ac);
    float r = float(counter)/float(uResolution.x * uResolution.y);
    col.r += r;
    color = vec4(col, 1.);
}
