package engine.gfx;

import static org.lwjgl.opengl.GL15.*;

public enum GPU_Buffer_Usage {
    Immutable(GL_STATIC_DRAW),
    Dynamic(GL_DYNAMIC_DRAW),
    Stream(GL_STATIC_DRAW);

    public final int rawGlValue;
    GPU_Buffer_Usage(int raw_value) {
        rawGlValue = raw_value;
    }
}
