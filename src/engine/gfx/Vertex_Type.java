package engine.gfx;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_INT;

public enum Vertex_Type {
    None(0, 0, 0),
    
    Float(1, 4, GL_FLOAT),
    Float2(2, 8, GL_FLOAT),
    Float3(3, 12, GL_FLOAT),
    Float4(4, 16, GL_FLOAT),

    Int(1, 4, GL_INT),
    Int2(2, 8, GL_INT),
    Int3(3, 12, GL_INT),
    Int4(4, 16, GL_INT);

    public final int count;
    public final int size;
    public final int glType;

    Vertex_Type(int count, int size, int glType) {
        this.count = count;
        this.size = size;
        this.glType = glType;
    }
}
