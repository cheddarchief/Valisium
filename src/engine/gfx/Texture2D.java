package engine.gfx;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.MissingResourceException;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture2D {
    private int id;
    private String type;

    public Texture2D(String path,
                     int filterMag,
                     int filterMin,
                     int wrapS,
                     int wrapT,
                     String type
    ) throws MissingResourceException {
        this.type = type;

        int w, h;

        ByteBuffer image;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer ch = stack.mallocInt(1);

            image = stbi_load(
                    path,
                    width,
                    height,
                    ch,
                    4
            );
            if (image == null)
                throw new MissingResourceException(
                        "failed to load texure from \"" + path + "\"",
                        getClass().toString(),
                        ""
                );

            w = width.get();
            h = height.get();
        }

        id = glGenTextures();

        setParameters(filterMag, filterMin, wrapS, wrapT);

        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA,
                w,
                h,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                image
        );
        glGenerateMipmap(GL_TEXTURE_2D);

        stbi_image_free(image);
    }

    public void setParameters(int filterMag, int filterMin, int wrapS, int wrapT) {
        glBindTexture(GL_TEXTURE_2D, id);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filterMag);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filterMin);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT);
    }

    /**
     *
     * @param slot must be an integer between 0 and 8
     */
    public void bind(int slot) {
        glActiveTexture(GL_TEXTURE0 + slot);
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void delete() {
        glDeleteTextures(id);
        id = -1;
    }
}