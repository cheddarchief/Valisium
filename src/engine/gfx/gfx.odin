package gfx

import "core:time"
import "core:mem"
import "core:os"
import "core:log"

import "core:runtime"

import gl "vendor:OpenGL"

GFX_DEBUG :: ODIN_DEBUG

/*
    Notes:
        1. Every @Hardcode tag indicates peaces of code that will be changed
           after the project will grow and implement more that one graphics
           API.
*/

// TODO:
//  - [X] Triangle Mesh
//  - [ ] Basic Shaders
//  - [ ] Multi texturing (with support for lighting maps)
//  - [ ] Font (? not sure if it goes to gfx and not separate module)
// well, i guess it's good enough

/*
 ██████╗ ██████╗ ██╗   ██╗    ███╗   ███╗███████╗███╗   ███╗ ██████╗ ██████╗ ██╗   ██╗
██╔════╝ ██╔══██╗██║   ██║    ████╗ ████║██╔════╝████╗ ████║██╔═══██╗██╔══██╗╚██╗ ██╔╝
██║  ███╗██████╔╝██║   ██║    ██╔████╔██║█████╗  ██╔████╔██║██║   ██║██████╔╝ ╚████╔╝
██║   ██║██╔═══╝ ██║   ██║    ██║╚██╔╝██║██╔══╝  ██║╚██╔╝██║██║   ██║██╔══██╗  ╚██╔╝
╚██████╔╝██║     ╚██████╔╝    ██║ ╚═╝ ██║███████╗██║ ╚═╝ ██║╚██████╔╝██║  ██║   ██║
 ╚═════╝ ╚═╝      ╚═════╝     ╚═╝     ╚═╝╚══════╝╚═╝     ╚═╝ ╚═════╝ ╚═╝  ╚═╝   ╚═╝
*/

Buffer :: distinct u32

Buffer_Usage :: enum {
    Immutable,
    Dynamic,
    Stream,
}

@(private)
buffer_usage_gl_map: [Buffer_Usage]u32 = {
    .Immutable = gl.STATIC_DRAW,
    .Dynamic   = gl.DYNAMIC_DRAW,
    .Stream    = gl.STREAM_DRAW,
}

Buffer_Type :: enum {
    Vertex_Buffer,
    Index_Buffer,
}

@(private)
buffer_type_gl_map: [Buffer_Type]u32 = {
    .Vertex_Buffer = gl.ARRAY_BUFFER,
    .Index_Buffer  = gl.ELEMENT_ARRAY_BUFFER,
}

/*
    # Buffer_Desc -- table to initialize buffers on gpu

    .data   - when not nil, data is used to allocate an initialize the buffer
              on gpu
    .length - used when data is nil to preallocate uinitialized buffer
    .type   - specified the type if the buffer (there are two possible options:
              Vertex_Buffer, and Index_Buffer
    .usage  - specifies how often will the data on gpu be updated
                  - .Immutable - once on creation
                  - .Stream    - every frame
                  - .Dynamic   - pretty often, but not every frame
*/
Buffer_Desc :: struct($T: typeid) {
    data:   []T,
    length: uint,
    type:   Buffer_Type,
    usage:  Buffer_Usage,
}

make_buffer :: proc(desc: ^Buffer_Desc($T)) -> Buffer {
    id: u32

    type   := buffer_type_gl_map[desc.type]
    length := i32(len(desc.data))

    if desc.data == nil {
        assert(desc.length > 0, "Buffer_Desc has not supplied data nor length")
        length = i32(desc.length)
    }

    gl.GenBuffers(1, &id)

    gl.BindBuffer(type, id)
    defer gl.BindBuffer(type, 0)

    gl.BufferData(
        type,
        int(length * size_of(T)),
        raw_data(desc.data),
        buffer_usage_gl_map[desc.usage]
    )

    return cast(Buffer)id
}

// @Unsafe: wrong type can be supplied
@(private)
bind_buffer :: #force_inline proc(buffer: Buffer, type: Buffer_Type) {
    gl.BindBuffer(buffer_type_gl_map[type], u32(buffer))
}

delete_buffer :: #force_inline proc(buffer: ^Buffer) {
    gl.DeleteBuffers(1, auto_cast buffer)
}

delete_buffers :: #force_inline proc(buffers: []Buffer) {
    gl.DeleteBuffers(i32(len(buffers)), auto_cast &buffers[0])
}

// @Hardcode: OpenGL only
VERTEX_BUFFERS_MAX_SIZE :: 8

Vertex_Format :: enum {
    None,

    Float,
    Float2,
    Float3,
    Float4,

    Int,
    Int2,
    Int3,
    Int4,

    UInt,
    UInt2,
    UInt3,
    UInt4,
}

@(private)
vertex_format_gl_count_map: [Vertex_Format]i32 = {
    .None   = 0,

    .Float  = 1,
    .Float2 = 2,
    .Float3 = 3,
    .Float4 = 4,

    .Int    = 1,
    .Int2   = 2,
    .Int3   = 3,
    .Int4   = 4,

    .UInt   = 1,
    .UInt2  = 2,
    .UInt3  = 3,
    .UInt4  = 4,
}

@(private)
vertex_format_gl_map: [Vertex_Format]u32 = {
    .None = 0,

    .Float  = gl.FLOAT,
    .Float2 = gl.FLOAT,
    .Float3 = gl.FLOAT,
    .Float4 = gl.FLOAT,

    .Int    = gl.INT,
    .Int2   = gl.INT,
    .Int3   = gl.INT,
    .Int4   = gl.INT,

    .UInt   = gl.UNSIGNED_INT,
    .UInt2  = gl.UNSIGNED_INT,
    .UInt3  = gl.UNSIGNED_INT,
    .UInt4  = gl.UNSIGNED_INT,
}

// Seems unncecesary, as all currently provided types have the same size, but it
// will change when types like short, byte, etc. will be added
@(private)
vertex_format_size_map: [Vertex_Format]i32 = {
    .None   = 0,

    .Float  = 1 * size_of(f32),
    .Float2 = 2 * size_of(f32),
    .Float3 = 3 * size_of(f32),
    .Float4 = 4 * size_of(f32),

    .Int    = 1 * size_of(i32),
    .Int2   = 2 * size_of(i32),
    .Int3   = 3 * size_of(i32),
    .Int4   = 4 * size_of(i32),

    .UInt   = 1 * size_of(u32),
    .UInt2  = 2 * size_of(u32),
    .UInt3  = 3 * size_of(u32),
    .UInt4  = 4 * size_of(u32),
}

Attribute_Layout :: struct {
    offset:     u32,
    buffer_idx: u32,
    format:     Vertex_Format,
}

Buffer_Layout :: struct {
    stride: u32,
}

Triangle_Mesh_Layout :: struct {
    attrs:   [VERTEX_BUFFERS_MAX_SIZE]Attribute_Layout,
    buffers: [VERTEX_BUFFERS_MAX_SIZE]Buffer_Layout,
}

/*
    Triangle_Mesh -- mesh encapsulation
*/
Triangle_Mesh :: struct($Index_Type: typeid) {
    vertex_array_id: u32,
    vertex_buffers:  [VERTEX_BUFFERS_MAX_SIZE]Buffer,
    index_buffer:    Buffer,

    indices: struct {
        count:  i32,
        offset: i32,
    }
}

// @Hardcode: OpenGL only
triangle_mesh_init_vao :: proc(
    using mesh: ^Triangle_Mesh($Index_Type),
    desc:       ^Triangle_Mesh_Layout
) {
    gl.GenVertexArrays(1, &vertex_array_id)

    gl.BindVertexArray(vertex_array_id)
    defer gl.BindVertexArray(0)

    offsets: [VERTEX_BUFFERS_MAX_SIZE]u32

    current_buffer_id: u32 = VERTEX_BUFFERS_MAX_SIZE + 1

    for attr, i in desc.attrs {
        if attr.format == .None {
            break
        }

        if attr.buffer_idx != current_buffer_id {
            current_buffer_id = attr.buffer_idx
            bind_buffer(vertex_buffers[current_buffer_id], .Vertex_Buffer)
        }

        gl.VertexAttribPointer(
            u32(i),
            vertex_format_gl_count_map[attr.format],
            vertex_format_gl_map[attr.format],
            false,
            i32(desc.buffers[attr.buffer_idx].stride),
            auto_cast offsets[attr.buffer_idx], // ? i'm not sure about this
        )
        gl.EnableVertexAttribArray(u32(i))

        offsets[attr.buffer_idx] += u32(vertex_format_size_map[attr.format])
    }

    // vertex array objest must know about the element buffer object
    bind_buffer(index_buffer, .Index_Buffer)
    defer gl.BindBuffer(gl.ELEMENT_ARRAY_BUFFER, 0)

    gl.BindVertexArray(0)
}

triangle_mesh_draw :: proc(using mesh: ^Triangle_Mesh($Index_Type)) {
    gl.BindVertexArray(mesh.vertex_array_id)

    when Index_Type == u16 {
        INDEX_TYPE :: gl.UNSIGNED_SHORT
    } else when Index_Type == u32 {
        INDEX_TYPE :: gl.UNSIGNED_INT
    } else {
        log.fatalf("Triangle_Mesh only supports u16 and u32 for index type")
        abort()
    }

    gl.DrawElements(gl.TRIANGLES, indices.count, INDEX_TYPE, auto_cast cast(uintptr) indices.offset)
}

/*
███████╗██╗  ██╗ █████╗ ██████╗ ███████╗██████╗ ███████╗
██╔════╝██║  ██║██╔══██╗██╔══██╗██╔════╝██╔══██╗██╔════╝
███████╗███████║███████║██║  ██║█████╗  ██████╔╝███████╗
╚════██║██╔══██║██╔══██║██║  ██║██╔══╝  ██╔══██╗╚════██║
███████║██║  ██║██║  ██║██████╔╝███████╗██║  ██║███████║
╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝ ╚══════╝╚═╝  ╚═╝╚══════╝
*/

Shader_Program :: struct {
    id:                u32,
    uniforms:          map[cstring]i32,

    last_changed_time: time.Time
}

shader_program_load_from_path :: proc(
    program:    ^Shader_Program,
    path:        string,
    allocator := context.allocator
) -> (ok: bool) {
    file, errno := os.open(path)
    if ok = errno == os.ERROR_NONE; !ok {
        log.errorf("failed to open shader's file at \"%s\"", path)
        return
    }

    return shader_program_load_from_handle(program, file, allocator)
}

shader_program_load_from_handle :: proc(
    using program: ^Shader_Program,
    handle:         os.Handle,
    allocator :=    context.allocator
) -> (ok: bool) {
    context.allocator = allocator

    file_info, errno := os.fstat(handle)
    if ok = errno == os.ERROR_NONE; !ok {
        log.errorf("failed to get file's size and modification time")
        return
    }

    defer os.file_info_delete(file_info)

    // getting the file size
    file_size                := file_info.size
    program.last_changed_time = file_info.modification_time

    // @Hardcode: for now, a small amout of bytes is need. just to fix one
    //            #define per shader, later it will change, and then it has
    //            to be calculated beforehand if we do not want to loose
    //            memory too much and/or get into segfaults
    PREFIX_SIZE :: 128  // should always be a power of 2

    buffer := make([]u8, file_size + PREFIX_SIZE + 1) // including '\0' bytes
    defer delete(buffer)

    buffer[len(buffer) - 1] = 0

    // Version always has a fixed length (strlen("version xxx core\n") = 18)
    // Note, that this gfx module is designed to be working with only core
    // opengl profile, compatibility profile is not supported

    // @Hardcode: version length is fixed, might have to change that later
    VERSION_LENGTH :: 18
    {
        read_bytes, errno := os.read(handle, buffer[:VERSION_LENGTH])
        if ok = errno == os.ERROR_NONE; !ok {
            log.errorf("failed to read from shaders file: %d", errno)
            return
        }
        if ok = VERSION_LENGTH == read_bytes; !ok {
            log.errorf("failed to read %d #VERSION bytes, read: %d", VERSION_LENGTH, read_bytes)
            return
        }

        // FIXME: add checks for cases when shader's file is in incorrect format
    }

    // now we preallocate PREFIX_SIZE of bytes with spaces
    // NOTE: it seems that we waste time filling first bytes that could be
    //       alredy filled with initial VERTEX_PREFIX, but it actually allows
    //       for SIMD optimizations, as PREFIX_SIZE supposed to be a power of 2
    mem.set(raw_data(buffer[VERSION_LENGTH:]), ' ', PREFIX_SIZE)

    //read the rest of the file
    {
        read_bytes, errno := os.read(handle, buffer[VERSION_LENGTH + PREFIX_SIZE:])
        if ok = errno == os.ERROR_NONE; !ok {
            log.errorf("failed to read from shaders file: %d", errno)
            return
        }
    }

    // @Hardcode
    PREFIX_VERTEX_SHADER   : string : "#define VERTEX_SHADER\n"
    PREFIX_FRAGMENT_SHADER : string : "#define FRAGMENT_SHADER\n"

    vertex_shader, fragment_shader: u32

    source_c_string := cstring(&buffer[0])

    // Compile Vertex Shader
    {
        mem.copy_non_overlapping(
            raw_data(buffer[VERSION_LENGTH:]),
            raw_data(PREFIX_VERTEX_SHADER[:]),
            len(PREFIX_VERTEX_SHADER),
        )

        vertex_shader, ok = create_shader(&source_c_string, gl.VERTEX_SHADER)

        if !ok {
            log.errorf("failed to compile Vertex Shader")
            return
        }
    }
    defer gl.DeleteShader(vertex_shader)

    mem.set(
        raw_data(buffer[VERSION_LENGTH:]),
        ' ',
        mem.align_forward_int(len(PREFIX_VERTEX_SHADER), 16)
    )

    // Compile Fragment Shader
    {
        mem.copy_non_overlapping(
            raw_data(buffer[VERSION_LENGTH:]),
            raw_data(PREFIX_FRAGMENT_SHADER[:]),
            len(PREFIX_FRAGMENT_SHADER),
        )

        fragment_shader, ok = create_shader(&source_c_string, gl.FRAGMENT_SHADER)

        if !ok {
            log.errorf("failed to compile Vertex Shader")
            return
        }
    }
    defer gl.DeleteShader(fragment_shader)

    id = gl.CreateProgram()

    gl.AttachShader(id, vertex_shader)
    defer gl.DetachShader(id, vertex_shader)
    gl.AttachShader(id, fragment_shader)
    defer gl.DetachShader(id, fragment_shader)

    gl.LinkProgram(id)

    check_for_gl_errors(
        id,
        gl.LINK_STATUS,
        gl.GetProgramiv,
        gl.GetProgramInfoLog
    ) or_return

    return
}

shader_program_init_uniforms :: proc(
    using program: ^Shader_Program,
    uniform_names: []cstring
) -> (ok: bool) {
    for i in 0..<len(uniform_names) {
        name             := uniform_names[i]
        uniform_location := gl.GetUniformLocation(id, name)

        program.uniforms[name] = uniform_location
    }
    ok = true // TODO: add error handling

    return
}

@(private)
create_shader :: proc(
    source:     ^cstring,
    type:        u32,
    allocator := context.allocator
) -> (id: u32, ok: bool) {
    id = gl.CreateShader(type)
    gl.ShaderSource(id, 1, source, nil)
    gl.CompileShader(id)

    ok = check_for_gl_errors(
        id,
        gl.COMPILE_STATUS,
        gl.GetShaderiv,
        gl.GetShaderInfoLog
    )

    return
}

when GFX_DEBUG {
    @(private)
    check_for_gl_errors :: proc(
        id:            u32,
        status:        u32,
        get_iv:        proc "c" (u32, u32, [^]i32, runtime.Source_Code_Location),
        get_info_log:  proc "c" (u32, i32, ^i32, [^]u8, runtime.Source_Code_Location),
        loc :=         #caller_location
    ) -> (ok: bool) {
        success: i32

        get_iv(id, status, &success, loc)

        ok = bool(success)
        if !ok {
            info_log_len: i32
            get_iv(id, gl.INFO_LOG_LENGTH, &info_log_len, loc)

            info_log := make([]u8, int(info_log_len))
            defer delete(info_log)

            get_info_log(id, info_log_len, nil, &info_log[0], loc)

            log.errorf("%s", info_log);
        }

        return
    }
} else {
    @(private)
    check_for_gl_errors :: proc(
        id:            u32,
        status:        u32,
        get_iv:        proc "c" (id: u32, pname: u32, params: [^]i32),
        get_info_log:  proc "c" (id: u32, bufSize: i32, length: ^i32, infoLog: [^]u8),
    ) -> (ok: bool) {
        success: i32

        get_iv(id, status, &success)

        ok = bool(success)
        if !ok {
            info_log_len: i32
            get_iv(id, gl.INFO_LOG_LENGTH, &info_log_len)

            info_log := make([]u8, int(info_log_len))
            defer delete(info_log)

            get_info_log(id, info_log_len, nil, &info_log[0])

            log.errorf("%s", info_log);
        }

        return
    }
}

shader_program_apply :: #force_inline proc(using program: ^Shader_Program) {
    gl.UseProgram(id)
}