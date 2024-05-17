package engine.scene;

import engine.gfx.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO: rewrite this whole mess using MemoryUtils heap allocations

public class Obj_Model_Loader {
    private Obj_Model_Loader() {}

    public static Triangle_Mesh loadFrom(Path objPath) throws IOException {
        ArrayList<Float> positions = new ArrayList<>();
        ArrayList<Float> uvs = new ArrayList<>();
        ArrayList<Float> normals = new ArrayList<>();

        IndexGroups groups = new IndexGroups();

        List<String> lines = Files.readAllLines(objPath);

        for (String line : lines) {
            String[] tokens = line.split("\\s+");

            switch (tokens[0]) {
                case "v" -> {
                    positions.add(Float.parseFloat(tokens[1]));
                    positions.add(Float.parseFloat(tokens[2]));
                    positions.add(Float.parseFloat(tokens[3]));
                }
                case "vt" -> {
                    uvs.add(Float.parseFloat(tokens[1]));
                    uvs.add(Float.parseFloat(tokens[2]));
                }
                case "vn" -> {
                    normals.add(Float.parseFloat(tokens[1]));
                    normals.add(Float.parseFloat(tokens[2]));
                    normals.add(Float.parseFloat(tokens[3]));
                }
                case "f" -> {
                    groups.add(tokens[1]);
                    groups.add(tokens[2]);
                    groups.add(tokens[3]);
                }
                case "#" -> {} // comments
                case "g", "o", "s" -> {} // ignore
                case "usemtl", "mtllib" -> {} // ignore
                default -> {
                    System.err.println("Unexpected value: " + tokens[0] + ", { " + Arrays.toString(tokens[0].toCharArray()) + " }");
//                    throw new IllegalStateException("Unexpected value: " + tokens[0]);
                }
            }
        }

        int verticesCount = positions.size() / 3;

        float[] positionsArray = new float[positions.size()];
        for (int i = 0; i < positionsArray.length; i++) {
            positionsArray[i] = positions.get(i);
        }

        float[] uvsArray = new float[verticesCount * 2];
        float[] normalsArray = new float[positionsArray.length];

        int[] indices = new int[groups.size()];

        for (int i = 0; i < groups.size(); i++) {
            indices[i] = groups.getIndexPos(i);

            int uvPos = groups.getUvPos(i);
            int normPos = groups.getNormalPos(i);

            if (!uvs.isEmpty()) {
                uvsArray[indices[i] * 2] = uvs.get(uvPos * 2);
                uvsArray[indices[i] * 2 + 1] = uvs.get(uvPos * 2 + 1);
            }

            if (!normals.isEmpty()) {
                normalsArray[indices[i] * 3] = normals.get(normPos * 3);
                normalsArray[indices[i] * 3 + 1] = normals.get(normPos * 3 + 1);
                normalsArray[indices[i] * 3 + 2] = normals.get(normPos * 3 + 2);
            }
        }

        return new Triangle_Mesh(
                new int[] {
                        GPU_Buffer.allocHeap(positionsArray, GPU_Buffer_Type.Vertex_Buffer, GPU_Buffer_Usage.Immutable),
                        GPU_Buffer.allocHeap(uvsArray, GPU_Buffer_Type.Vertex_Buffer, GPU_Buffer_Usage.Immutable),
                        GPU_Buffer.allocHeap(normalsArray, GPU_Buffer_Type.Vertex_Buffer, GPU_Buffer_Usage.Immutable)
                },
                GPU_Buffer.allocHeap(indices, GPU_Buffer_Type.Index_Buffer, GPU_Buffer_Usage.Immutable),
                indices.length,
                Index_Type.UInt32,
                new Triangle_Mesh_Layout(
                        new Buffer_Layout[] {
                                new Buffer_Layout(Vertex_Type.Float3.size),
                                new Buffer_Layout(Vertex_Type.Float2.size),
                                new Buffer_Layout(Vertex_Type.Float3.size)
                        },
                        new Attribute_Layout[] {
                                new Attribute_Layout(0, Vertex_Type.Float3),
                                new Attribute_Layout(1, Vertex_Type.Float2),
                                new Attribute_Layout(2, Vertex_Type.Float3)
                        }
                )
        );
    }
}

