#version 420 core

layout (location = 0) in vec3 in_pos;

out vec3 color;

void main() {
    gl_Position = vec4(in_pos * 0.5, 1.0);
    color       = in_pos * 0.5 + 0.5;
}