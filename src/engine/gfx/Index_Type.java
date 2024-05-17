package engine.gfx;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;

public enum Index_Type {
    UInt16(GL_UNSIGNED_SHORT),
    UInt32(GL_UNSIGNED_INT);

    public final int glValue;

    Index_Type(int glValue) {
        this.glValue = glValue;
    }
}
