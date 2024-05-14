#version 420 core

#ifdef VERTEX_SHADER
layout (location = 0) in vec3 in_pos;

uniform mat4 mvp;

out vec3 color;

void main() {
    gl_Position = mvp * vec4(in_pos * 0.5, 1.0);
    color       = in_pos * 0.5 + 0.5;
}
#endif

#ifdef FRAGMENT_SHADER
in vec3 color;
out vec4 frag_color;

void main() {
    frag_color = vec4(color, 1.0);
}
#endif
