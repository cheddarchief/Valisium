package engine.gfx;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;

public enum GPU_Buffer_Type {
    Vertex_Buffer(GL_ARRAY_BUFFER),
    Index_Buffer(GL_ELEMENT_ARRAY_BUFFER);

    public final int rawBufferType;

    GPU_Buffer_Type(int rawBufferType) {
        this.rawBufferType = rawBufferType;
    }
}
