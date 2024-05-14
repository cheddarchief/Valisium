package camera

import "core:math"
import glm "core:math/linalg/glsl"

sin :: math.sin
cos :: math.cos

vec3 :: glm.vec3
mat4 :: glm.mat4

isnan :: math.is_nan_f32
clamp :: math.clamp

@(private)
GLOBAL_WORLD_UP_DIRECTION: vec3 : {0.0, 1.0, 0.0}

Camera :: struct {
    position:    vec3,
    direction:   vec3,
    right:       vec3,

    yaw:         f32,
    pitch:       f32,

    speed:       f32,
    sensetivity: f32,

    /* cached values */
    view_mat:    mat4,
}

flycam_init :: proc(
    cam:                           ^Camera,
    position:                       vec3,
    yaw, pitch, speed, sensetivity: f32)
{
    cam.position    = position
    cam.yaw         = yaw
    cam.pitch       = clamp(pitch, -89.0, 89.0)
    cam.speed       = speed
    cam.sensetivity = sensetivity

    flycam_recalculate_basis(cam)
}

Direction :: enum {
    Forward,
    Backward,
    Right,
    Left,
    Up,
    Down,
}

flycam_update_position :: proc(
    cam:      ^Camera,
    direction: bit_set[Direction; u32],
    dt:        f32
) {
    final_direction := vec3 {0.0, 0.0, 0.0}

    if .Forward in direction {
        final_direction += cam.direction
    }
    if .Backward in direction {
        final_direction -= cam.direction
    }
    if .Right in direction {
        final_direction += cam.right
    }
    if .Left in direction {
        final_direction -= cam.right
    }
    if .Up in direction {
        final_direction.y += 1
    }
    if .Down in direction {
        final_direction.y -= 1
    }

    final_direction = glm.normalize_vec3(final_direction)

    if isnan(final_direction[0]) |
           isnan(final_direction[1]) |
           isnan(final_direction[2]) {
       return
    }

    cam.position += final_direction * dt * cam.speed

    flycam_recalculate_view_mat(cam)
}

flycam_update_direction :: proc(
    using cam: ^Camera,
    dx, dy: f32
) {
    yaw  += dx * sensetivity
    pitch = clamp(pitch - dy * sensetivity, -89.0, 89.0)

    flycam_recalculate_basis(cam)
}

@(private)
flycam_recalculate_basis :: proc(using cam: ^Camera) {
    rad_yaw   := math.to_radians_f32(yaw)
    rad_pitch := math.to_radians_f32(pitch)

    direction = {
        cos(rad_yaw) * cos(rad_pitch),
        sin(rad_pitch),
        sin(rad_yaw) * cos(rad_pitch)
    }

    right = glm.cross(direction, GLOBAL_WORLD_UP_DIRECTION)

    // basis change means view matrix needs to change
    flycam_recalculate_view_mat(cam)
}

@(private)
flycam_recalculate_view_mat :: proc(using cam: ^Camera) {
    cam.view_mat = glm.mat4LookAt(
        cam.position,
        cam.position + cam.direction,
        GLOBAL_WORLD_UP_DIRECTION
    )
}