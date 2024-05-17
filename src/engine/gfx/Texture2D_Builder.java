package engine.gfx;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_REPEAT;

public class Texture2D_Builder {
    private String path;
    private String type;
    private int filterMag = GL_NEAREST;
    private int filterMin = GL_NEAREST;
    private int wrapS     = GL_REPEAT;
    private int wrapT     = GL_REPEAT;

    public Texture2D_Builder setPath(String path) {
        this.path = path;
        return this;
    }

    public Texture2D_Builder setFilterMag(int filterMag) {
        this.filterMag = filterMag;
        return this;
    }

    public Texture2D_Builder setFilterMin(int filterMin) {
        this.filterMin = filterMin;
        return this;
    }

    public Texture2D_Builder setWrapS(int wrapS) {
        this.wrapS = wrapS;
        return this;
    }

    public Texture2D_Builder setWrapT(int wrapT) {
        this.wrapT = wrapT;
        return this;
    }

    // TODO: make type a enum, so that we could preallocate all needed texture type strings beforehand in a map
    public Texture2D_Builder setType(String type) {
        this.type = type;
        return this;
    }

    public Texture2D createTexture2D() {
        return new Texture2D(path, filterMag, filterMin, wrapS, wrapT, type);
    }
}