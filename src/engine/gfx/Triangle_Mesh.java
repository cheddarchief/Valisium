package engine.gfx;

import java.util.Arrays;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class Triangle_Mesh {
    public static final int VERTEX_BUFFERS_COUNT = 8;

    private int vertexArray;
    private final int[] vertexBuffers;
    private final int indexBuffer;

    private final Index_Type indexType;

    private int indicesCount;

    public Triangle_Mesh(
            int[] vertexBuffers,
            int indexBuffer,
            int indicesCount,
            Index_Type indexType,
            Triangle_Mesh_Layout layout) {
        assert vertexBuffers.length <= VERTEX_BUFFERS_COUNT;

        vertexArray = glGenVertexArrays();
        glBindVertexArray(vertexArray);

        this.vertexBuffers = vertexBuffers;
        this.indexBuffer = indexBuffer;
        this.indicesCount = indicesCount;
        this.indexType = indexType;

        int currentBufferIndex = vertexBuffers.length + 1;
        int[] offsets = new int[vertexBuffers.length];

        Arrays.fill(offsets, 0);

        for (int i = 0; i < layout.attrs.length; ++i) {
            Attribute_Layout attr = layout.attrs[i];

            if (attr.bufferIndex() != currentBufferIndex) {
                currentBufferIndex = attr.bufferIndex();

                glBindBuffer(GL_ARRAY_BUFFER, vertexBuffers[currentBufferIndex]);
            }

            glVertexAttribPointer(
                    i,
                    attr.type().count,
                    attr.type().glType,
                    false,
                    layout.buffers[currentBufferIndex].stride(),
                    offsets[currentBufferIndex]
            );
            glEnableVertexAttribArray(i);

            offsets[currentBufferIndex] += attr.type().size;
        }

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
        glBindVertexArray(0);
    }

    public void render() {
        glBindVertexArray(vertexArray);
        glDrawElements(GL_TRIANGLES, indicesCount, indexType.glValue, 0);
    }

    public void delete() {
        glDeleteVertexArrays(vertexArray);

        vertexArray = -1;
    }

    public void deleteWithBuffers() {
        glDeleteBuffers(vertexBuffers);
        glDeleteBuffers(indexBuffer);

        delete();
    }

    public int getIndicesCount() {
        return indicesCount;
    }

    public void setIndicesCount(int indicesCount) {
        assert indicesCount >= 0;
        this.indicesCount = indicesCount;
    }
}

