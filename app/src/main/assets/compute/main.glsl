#version 310 es
layout (local_size_x = 20, local_size_y = 20, local_size_z = 1) in;

layout(binding = 0, rgba32f) readonly uniform highp image2D input_image;
layout(binding = 1, offset = 0) uniform atomic_uint ac;

void main(void) {
    ivec2 pos = ivec2(gl_GlobalInvocationID.xy);
    vec4 color = imageLoad(input_image, pos);
    if (color.r < .5) {
        uint previous = atomicCounterIncrement(ac);
    }
}