package main

import "core:log"
import "core:thread"
import "core:os"

import "core:math"
import glm "core:math/linalg/glsl"

import "vendor:glfw"
import gl "vendor:OpenGL"

import "engine/gfx"
import "camera"

OPENGL_VERSION_MAJOR :: 4
OPENGL_VERSION_MINOR :: 2

WINDOW_WIDTH  :: 1280
WINDOW_HEIGHT :: 720
WINDOW_TITLE  :: "Valisium"

Window :: struct {
    open:      bool,
    handle:    glfw.WindowHandle,
    resolution: struct {
        width:  i32,
        height: i32,
        dirty:  bool,
    },
    cursor:    struct {
        x, y:   f32,
        dx, dy: f32,
        dirty:  bool,
    },
}

state: struct {
    window:     Window,
    frame_time: f64,

    quad:       struct {
        mesh:    gfx.Triangle_Mesh(u16),
        program: gfx.Shader_Program,
    },

    flycam:         camera.Camera,
    move_direction: bit_set[camera.Direction; u32],

    keys: [glfw.KEY_LAST]bool,

    frame_duration: f32
}

init :: proc() -> (ok: bool) {
    if ok = glfw_init(); !ok {
        return
    }
    defer if !ok { glfw.Terminate() }

    if ok = window_init(WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_TITLE); !ok {
        return
    }

    camera.flycam_init(
        &state.flycam,
        glm.vec3 {0.0, 0.0, 2.0},
        -90.0,
        0.0,
        10.0,
        1.0,
    )

    {
        using state.quad

        vertices := [?]f32 {
            -1.0, -1.0, -1.0,
             1.0, -1.0, -1.0,
            -1.0,  1.0, -1.0,
             1.0,  1.0, -1.0,

            -1.0, -1.0,  1.0,
             1.0, -1.0,  1.0,
            -1.0,  1.0,  1.0,
             1.0,  1.0,  1.0,

            -1.0, -1.0, -1.0,
            -1.0,  1.0, -1.0,
            -1.0, -1.0,  1.0,
            -1.0,  1.0,  1.0,

             1.0, -1.0, -1.0,
             1.0,  1.0, -1.0,
             1.0, -1.0,  1.0,
             1.0,  1.0,  1.0,

            -1.0, -1.0, -1.0,
             1.0, -1.0, -1.0,
            -1.0, -1.0,  1.0,
             1.0, -1.0,  1.0,

            -1.0,  1.0, -1.0,
             1.0,  1.0, -1.0,
            -1.0,  1.0,  1.0,
             1.0,  1.0,  1.0,
        }

        indices := [?]u16 {
            0, 1, 2, 1, 3, 2,
            4, 6, 5, 5, 6, 7,
            8, 9, 10, 9, 11, 10,
            12, 14, 13, 13, 14, 15,
            16, 18, 17, 17, 18, 19,
            20, 21, 22, 21, 23, 22
        }

        mesh.indices.count = i32(size_of(indices) / size_of(indices[0]))

        mesh_layout: gfx.Triangle_Mesh_Layout

        mesh_layout.attrs[0] = gfx.Attribute_Layout {
            offset     = 0,
            buffer_idx = 0,
            format     = .Float3
        }
        mesh_layout.buffers[0] = gfx.Buffer_Layout {
            stride = u32(size_of(f32) * 3)
        }

        mesh.vertex_buffers[0] = gfx.make_buffer(&gfx.Buffer_Desc(f32) {
            data  = vertices[:],
            type  = .Vertex_Buffer,
            usage = .Immutable
        })

        mesh.index_buffer = gfx.make_buffer(&gfx.Buffer_Desc(u16) {
            data  = indices[:],
            type  = .Index_Buffer,
            usage = .Immutable
        })

        gfx.triangle_mesh_init_vao(&mesh, &mesh_layout)

        if ok = gfx.shader_program_load_from_path(
            &program,
            "res/shaders/default.glsl"
        ); !ok {
            return
        }
    }

    // To move opengl context to another thread, unbind it here
    glfw.MakeContextCurrent(nil)

    return ok
}

cleanup :: proc() {
    using state

    // needed to cleanup some OpenGL global resources
    glfw.MakeContextCurrent(window.handle)

    glfw.DestroyWindow(window.handle)
    glfw.Terminate()
}

update_camera_position_direction :: proc() {
    using state

/*
    move_direction[.Forward]  = state.keys[glfw.KEY_W]
    move_direction[.Backward] = state.keys[glfw.KEY_S]
    move_direction[.Right]    = state.keys[glfw.KEY_D]
    move_direction[.Left]     = state.keys[glfw.KEY_A]
    move_direction[.Up]       = state.keys[glfw.KEY_SPACE]
    move_direction[.Down]     = state.keys[glfw.KEY_LEFT_SHIFT]
*/

    if state.keys[glfw.KEY_W] {
        move_direction += {.Forward}
    }
    if state.keys[glfw.KEY_S] {
        move_direction += {.Backward}
    }
    if state.keys[glfw.KEY_D] {
        move_direction += {.Right}
    }
    if state.keys[glfw.KEY_A] {
        move_direction += {.Left}
    }
    if state.keys[glfw.KEY_SPACE] {
        move_direction += {.Up}
    }
    if state.keys[glfw.KEY_LEFT_SHIFT] {
        move_direction += {.Down}
    }
}

frame_flush :: proc() -> bool {
    using state

    glfw.SwapBuffers(window.handle)

    if state.keys[glfw.KEY_Q] {
        window.open = false
    }
    update_camera_position_direction()
    camera.flycam_update_position(&state.flycam, state.move_direction, frame_duration)

    if window.resolution.dirty {
        using window.resolution

        gl.Viewport(0, 0, width, height)

        dirty = false
    }

    if window.cursor.dirty {
        cursor_update()
        camera.flycam_update_direction(
            &state.flycam,
            window.cursor.dx,
            window.cursor.dy
        )

        log.info(window.cursor)

        window.cursor.dirty = false
    }

    move_direction = {}

    window.open &= !glfw.WindowShouldClose(window.handle)
    return window.open
}

frame :: proc() {
    using state

    gl.ClearColor(0.05, 0.1, 0.15, 1.0)
    gl.Clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)

    gl.Enable(gl.DEPTH_TEST)
    gl.Enable(gl.CULL_FACE)
    gl.CullFace(gl.BACK)
    gl.FrontFace(gl.CW)
    { // Rendering the object
        gfx.shader_program_apply(&state.quad.program)

        // TODO: not do it every frame
        proj := glm.mat4Perspective(
            math.to_radians_f32(70),
            f32(window.resolution.width) / f32(window.resolution.height),
            0.01,
            100.0
        )

        vp := proj * flycam.view_mat

        model := glm.mat4Rotate(
            {0.0, 1.0, 0.0},
            f32(glfw.GetTime()),
        ) * glm.mat4Rotate(
            {0.0, 0.0, 1.0},
            f32(glfw.GetTime() * 0.8953),
        )

        mvp := vp * model

        gl.UniformMatrix4fv(gl.GetUniformLocation(quad.program.id, "mvp"), 1, false, &mvp[0][0])

        gfx.triangle_mesh_draw(&state.quad.mesh)
    }
}

render_worker :: proc() {
    using state

    glfw.MakeContextCurrent(window.handle)
    defer glfw.MakeContextCurrent(nil)

    start := glfw.GetTime()
    for frame_flush() {
        frame_duration = f32(glfw.GetTime() - start)
        start = glfw.GetTime()

        frame()
    }
}

main :: proc() {
    context.logger = log.create_console_logger()

    if !init() {
        log.fatalf("failed to initialize global state")
        os.exit(-1)
    }
    defer cleanup()

    render_loop := thread.create_and_start(render_worker, context)

    thread.start(render_loop)
    defer thread.join(render_loop)

    for state.window.open {
        glfw.WaitEvents()
    }
}

glfw_init :: proc() -> (ok: bool) {
    if ok = cast(bool)glfw.Init(); !ok {
        log.errorf("failed to initialize GLFW")
        return
    }

    glfw.DefaultWindowHints()
    glfw.WindowHint(glfw.CONTEXT_VERSION_MAJOR, OPENGL_VERSION_MAJOR)
    glfw.WindowHint(glfw.CONTEXT_VERSION_MINOR, OPENGL_VERSION_MINOR)
    glfw.WindowHint(glfw.OPENGL_PROFILE, glfw.OPENGL_CORE_PROFILE)

    return
}

window_init :: proc(width, height: i32, title: cstring) -> (ok: bool) {
    using state.window

    handle = glfw.CreateWindow(width, height, title, nil, nil)
    if ok = handle != nil; !ok {
        log.errorf("failed to create a window")
        return
    }

    open = true
    resolution = {
        width  = width,
        height = height,
        dirty  = false,
    }

    cursor_x, cursor_y := glfw.GetCursorPos(handle)

    cursor = {
        x     = f32(cursor_x),
        y     = f32(cursor_y),
        dx    = 0,
        dy    = 0,
        dirty = false
    }
    framebuffer_callback :: proc "c" (_win: glfw.WindowHandle, width, height: i32) {
        state.window.resolution = {
            width  = width,
            height = height,
            dirty  = true,
        }
    }
    cursor_move_callback :: proc "c" (_win: glfw.WindowHandle, x, y: f64) {
        // Note: updating x and y directly doesn't yeild good result as we render and
        //       update the result on different threads, this leads to rece conditions
        state.window.cursor.dirty = true
    }
    keyboard_callback :: proc "c" (_win: glfw.WindowHandle, key, scancode, action, mods: i32) {
        state.keys[key] = action == glfw.PRESS
    }

    glfw.SetFramebufferSizeCallback(handle, framebuffer_callback)
    glfw.SetCursorPosCallback(handle, cursor_move_callback)
    glfw.SetKeyCallback(handle, keyboard_callback)

    glfw.MakeContextCurrent(handle)

    gl.load_up_to(
        OPENGL_VERSION_MAJOR,
        OPENGL_VERSION_MINOR,
        glfw.gl_set_proc_address,
    )

    return
}

cursor_update :: proc() {
    using state.window

    _cursor_x, _cursor_y := glfw.GetCursorPos(handle)
    cursor_x := f32(_cursor_x)
    cursor_y := f32(_cursor_y)

    cursor.dx = cursor_x - cursor.x
    cursor.dy = cursor_y - cursor.y

    cursor.x = cursor_x
    cursor.y = cursor_y
}