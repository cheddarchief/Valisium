package engine.gfx;

import org.joml.*;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.opengl.GL20.*;

public class Shader_Program {
    private int program;

    private final HashMap<String, Integer> uniforms = new HashMap<>();

    private long vertexModificationTime;
    private long fragmentModificationTime;

    public Path vertexFilePath;
    public Path fragmentFilePath;

    public Shader_Program(Path vertexPath, Path fragmentPath) throws IOException {
        vertexFilePath = vertexPath;
        fragmentFilePath = fragmentPath;

        int vertex = createShader(Files.readString(vertexFilePath), GL_VERTEX_SHADER);
        int fragment = createShader(Files.readString(fragmentFilePath), GL_FRAGMENT_SHADER);

        program = initProgram(vertex, fragment);

        updateFileChangedTime();
    }

    private static int initProgram(int vertex, int fragment) {
        int program = glCreateProgram();

        glAttachShader(program, vertex);
        glAttachShader(program, fragment);

        glLinkProgram(program);

        int success = glGetProgrami(program, GL_LINK_STATUS);
        if (success != 1) {
            System.err.printf("failed to link program: %s\n", glGetProgramInfoLog(program));
        }

        glDetachShader(program, vertex);
        glDetachShader(program, fragment);

        glDeleteShader(vertex);
        glDeleteShader(fragment);

        return program;
    }

    private static int createShader(String source, int type) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);

        int success = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (success != 1) {
            System.err.printf("failed to compile shader: %s\n", glGetShaderInfoLog(shader));
        }

        return shader;
    }

    public void updateFileChangedTime() throws IOException {
        vertexModificationTime = Files.getLastModifiedTime(vertexFilePath).toMillis();
        fragmentModificationTime = Files.getLastModifiedTime(fragmentFilePath).toMillis();
    }

    public Shader_Program(Path vertexPath, Path fragmentPath, List<String> uniformsNames) throws IOException {
        this(vertexPath, fragmentPath);

        updateUniformLocations(uniformsNames);
    }

    private void updateUniformLocations(List<String> uniformNames) {
        uniformNames.forEach((uniform) -> {
            int location = glGetUniformLocation(program, uniform);
            uniforms.put(uniform, location);
        });
    }

    public void reloadIfChanged() throws IOException {
        long vertexTime = Files.getLastModifiedTime(vertexFilePath).toMillis();
        long fragmentTime = Files.getLastModifiedTime(fragmentFilePath).toMillis();

        boolean isChanged = vertexTime > vertexModificationTime && fragmentTime > fragmentModificationTime;

        updateFileChangedTime();

        if (isChanged)
                return;

        int vertex = createShader(Files.readString(vertexFilePath), GL_VERTEX_SHADER);
        int fragment = createShader(Files.readString(fragmentFilePath), GL_FRAGMENT_SHADER);

        program = initProgram(vertex, fragment);
    }

    public void reloadIfChanged(List<String> uniformNames) throws IOException {
        reloadIfChanged();

        uniforms.clear();
        updateUniformLocations(uniformNames);
    }

    public void use() {
        glUseProgram(program);
    }

    public void delete() {
        glDeleteProgram(program);
        program = -1;
    }

    public void setUniform(String name, Matrix4f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(uniforms.get(name), false, matrix.get(stack.mallocFloat(16)));
        }
    }

    public void setUniform(String name, Matrix3f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix3fv(uniforms.get(name), false, matrix.get(stack.mallocFloat(9)));
        }
    }

    public void setUniform(String name, Matrix2f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix2fv(uniforms.get(name), false, matrix.get(stack.mallocFloat(4)));
        }
    }

    public void setUniform(String name, float v0) {
        glUniform1f(uniforms.get(name), v0);
    }

    public void setUniform(String name, float v0, float v1) {
        glUniform2f(uniforms.get(name), v0, v1);
    }

    public void setUniform(String name, float v0, float v1, float v2) {
        glUniform3f(uniforms.get(name), v0, v1, v2);
    }

    public void setUniform(String name, float v0, float v1, float v2, float v3) {
        glUniform4f(uniforms.get(name), v0, v1, v2, v3);
    }

    public void setUniform(String name, Vector2f v) {
        glUniform2f(uniforms.get(name), v.x, v.y);
    }

    public void setUniform(String name, Vector3f v) {
        glUniform3f(uniforms.get(name), v.x, v.y, v.z);
    }

    public void setUniform(String name, Vector4f v) {
        glUniform4f(uniforms.get(name), v.x, v.y, v.z, v.w);
    }

    public void setUniform(String name, int v0) {
        glUniform1i(uniforms.get(name), v0);
    }

    public void setUniform(String name, int v0, int v1) {
        glUniform2i(uniforms.get(name), v0, v1);
    }

    public void setUniform(String name, int v0, int v1, int v2) {
        glUniform3i(uniforms.get(name), v0, v1, v2);
    }

    public void setUniform(String name, int v0, int v1, int v2, int v3) {
        glUniform4i(uniforms.get(name), v0, v1, v2, v3);
    }

    public void setUniform(String name, Vector2i v) {
        glUniform2i(uniforms.get(name), v.x, v.y);
    }

    public void setUniform(String name, Vector3i v) {
        glUniform3i(uniforms.get(name), v.x, v.y, v.z);
    }

    public void setUniform(String name, Vector4i v) {
        glUniform4i(uniforms.get(name), v.x, v.y, v.z, v.w);
    }

}
