package engine.gfx;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;

/**
 * <h1>
 *     GPU_Buffer
 * </h1>
 * <h3>
 *     A simple shortcut class, must be used only on rare occasions and never inside loops
 * </h3>
 * <p>
 *     What these functions do is copy data from java heap for LWJGL compatible stack/heap, meaning it does double copy
 *     for every single byte. It's obviously very slow
 * </p>
 */
public class GPU_Buffer {
    public static int allocHeap(float[] data, GPU_Buffer_Type type, GPU_Buffer_Usage usage) {
        int buffer = glGenBuffers();
        glBindBuffer(type.rawBufferType, buffer);

        FloatBuffer memory = MemoryUtil.memAllocFloat(data.length);

        glBufferData(
                type.rawBufferType,
                memory.put(data).flip(),
                usage.rawGlValue
        );

        MemoryUtil.memFree(memory);

        return buffer;
    }

    public static int allocStack(float[] data, GPU_Buffer_Type type, GPU_Buffer_Usage usage) {
        int buffer = glGenBuffers();
        glBindBuffer(type.rawBufferType, buffer);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            glBufferData(
                    type.rawBufferType,
                    stack.mallocFloat(data.length).put(data).flip(),
                    usage.rawGlValue
            );
        }

        return buffer;
    }

    public static int allocHeap(int[] data, GPU_Buffer_Type type, GPU_Buffer_Usage usage) {
        int buffer = glGenBuffers();
        glBindBuffer(type.rawBufferType, buffer);

        var memory = MemoryUtil.memAllocInt(data.length);

        glBufferData(
                type.rawBufferType,
                memory.put(data).flip(),
                usage.rawGlValue
        );

        MemoryUtil.memFree(memory);

        return buffer;
    }

    public static int allocStack(int[] data, GPU_Buffer_Type type, GPU_Buffer_Usage usage) {
        int buffer = glGenBuffers();
        glBindBuffer(type.rawBufferType, buffer);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            glBufferData(
                    type.rawBufferType,
                    stack.mallocInt(data.length).put(data).flip(),
                    usage.rawGlValue
            );
        }

        return buffer;
    }

    public static int allocHeap(short[] data, GPU_Buffer_Type type, GPU_Buffer_Usage usage) {
        int buffer = glGenBuffers();
        glBindBuffer(type.rawBufferType, buffer);

        var memory = MemoryUtil.memAllocShort(data.length);

        glBufferData(
                type.rawBufferType,
                memory.put(data).flip(),
                usage.rawGlValue
        );

        MemoryUtil.memFree(memory);

        return buffer;
    }

    public static int allocStack(short[] data, GPU_Buffer_Type type, GPU_Buffer_Usage usage) {
        int buffer = glGenBuffers();
        glBindBuffer(type.rawBufferType, buffer);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            glBufferData(
                    type.rawBufferType,
                    stack.mallocShort(data.length).put(data).flip(),
                    usage.rawGlValue
            );
        }

        return buffer;
    }
}