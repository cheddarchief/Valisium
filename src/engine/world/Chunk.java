package engine.world;

import org.joml.Vector3f;

public class Chunk {
    public static final int WORLD_SIDE_SIZE = 64;

    private final int[] raw = new int[WORLD_SIDE_SIZE * WORLD_SIDE_SIZE];
    private final Vector3f position = null; // FIXME:
}
