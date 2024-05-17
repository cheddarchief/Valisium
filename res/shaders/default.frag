#version 460 core

out vec4 frag_color;

in vec2 uv;
in vec3 color;
in vec3 normal;
in vec3 frag_pos;
in vec3 light_pos;

uniform sampler2D tex;

void main() {
    vec3 light_dir = normalize(light_pos - frag_pos);
    vec3 norm = normalize(normal);

    vec3 ambient = vec3(0.1);

    float diff = max(dot(light_dir, norm), 0.0);
    vec3 diffuse = vec3(diff) * vec3(1.0);

    frag_color = vec4(vec3(1.0) * (ambient + diffuse), 1.0);
}
