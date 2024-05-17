import engine.scene.Model;
import engine.misc.Cursor;
import engine.misc.Direction_Mask;
import engine.scene.Flying_Camera;
import engine.gfx.*;

import engine.scene.Obj_Model_Loader;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.file.Path;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {
    private static long windowHandle;
    private static int width = 1600;
    private static int height = 900;

    private static final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private static final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private static final String glslVersion = "#version 460 core";

    private static final boolean[] keys = new boolean[GLFW_KEY_LAST];
    private static final float[] bgColor = new float[]{0.05f, 0.1f, 0.15f};

    private static Model model;
    private static Triangle_Mesh mesh;
    private static Shader_Program program;

    private static Flying_Camera camera;
    private static final Direction_Mask directionMask = new Direction_Mask();

    private static Cursor cursor;
    private static boolean cursorLocked = false;

    private static float deltaTime;

    // DEBUG DEBUG DEBUG
    private static int polygonMode = GL_FILL;

    private static float[] cameraSpeed;
    private static float[] cameraSensitivity;

    public static void main(String[] args) {
        init();

        run();

        cleanup();
    }

    public static void init() {
        initGlfw();
        initImGui();

        camera = new Flying_Camera(
                new Vector3f(0.0f, 0.0f, -2.0f),
                90.0f,
                0.0f,
                0.4f,
                20.0f
        );

        cameraSpeed = new float[]{camera.speed};
        cameraSensitivity = new float[]{camera.sensitivity};

        initRenderPrimitives();
    }

    private static void initRenderPrimitives() {
        float[] quadVertices = {
                -1.0f, -1.0f, -1.0f, 0.0f, 0.0f,
                 1.0f, -1.0f, -1.0f, 1.0f, 0.0f,
                -1.0f,  1.0f, -1.0f, 0.0f, 1.0f,
                 1.0f,  1.0f, -1.0f, 1.0f, 1.0f,

                -1.0f, -1.0f,  1.0f, 0.0f, 0.0f,
                 1.0f, -1.0f,  1.0f, 1.0f, 0.0f,
                -1.0f,  1.0f,  1.0f, 0.0f, 1.0f,
                 1.0f,  1.0f,  1.0f, 1.0f, 1.0f,

                -1.0f, -1.0f, -1.0f, 0.0f, 0.0f,
                 1.0f, -1.0f, -1.0f, 1.0f, 0.0f,
                -1.0f, -1.0f,  1.0f, 0.0f, 1.0f,
                 1.0f, -1.0f,  1.0f, 1.0f, 1.0f,

                -1.0f,  1.0f, -1.0f, 0.0f, 0.0f,
                 1.0f,  1.0f, -1.0f, 1.0f, 0.0f,
                -1.0f,  1.0f,  1.0f, 0.0f, 1.0f,
                 1.0f,  1.0f,  1.0f, 1.0f, 1.0f,

                -1.0f, -1.0f, -1.0f, 0.0f, 0.0f,
                -1.0f,  1.0f, -1.0f, 1.0f, 0.0f,
                -1.0f, -1.0f,  1.0f, 0.0f, 1.0f,
                -1.0f,  1.0f,  1.0f, 1.0f, 1.0f,

                 1.0f, -1.0f, -1.0f, 0.0f, 0.0f,
                 1.0f,  1.0f, -1.0f, 1.0f, 0.0f,
                 1.0f, -1.0f,  1.0f, 0.0f, 1.0f,
                 1.0f,  1.0f,  1.0f, 1.0f, 1.0f,
        };

        short[] quadIndices = {
                0, 1, 2, 1, 3, 2,
                4, 6, 5, 5, 6, 7,
                8, 10, 9, 9, 10, 11,
                12, 13, 14, 13, 15, 14,
                16, 17, 18, 17, 19, 18,
                20, 22, 21, 21, 22, 23,
        };

        try {
            mesh = Obj_Model_Loader.loadFrom(Path.of("res/models/homer.obj"));
        } catch (IOException e) {
            System.err.println(e);
        }
        Texture2D texture = new Texture2D_Builder()
                .setPath("res/models/boxy/zzz_0.png")
                .createTexture2D();

        model = new Model(mesh, texture);

        try {
            program = new Shader_Program(
                    Path.of("res/shaders/default.vert"),
                    Path.of("res/shaders/default.frag"),
                    List.of("mvp", "mv", "v", "tex"));
        } catch (IOException e) {
            System.err.println("Failed to create shader program. Too bad");
        }

        program.setUniform("tex", 0);
    }

    private static void initGlfw() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("failed to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        windowHandle = glfwCreateWindow(width, height, "Default Window", NULL, NULL);
        if (windowHandle == NULL)
            throw new IllegalStateException("failed to create a new window");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            DoubleBuffer cursorPos = stack.mallocDouble(2);

            glfwGetCursorPos(windowHandle, cursorPos, cursorPos.position(1));

            cursor = new Cursor((float) cursorPos.get(0), (float) cursorPos.get(1));
        }

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();

        glfwSwapInterval(-1);

        glfwSetFramebufferSizeCallback(windowHandle, (win , w, h) -> {
            width = w;
            height = h;
            glViewport(0, 0, w, h);
        });
        glfwSetKeyCallback(windowHandle, (w, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS)
                keys[key] = true;
            else if (action == GLFW_RELEASE)
                keys[key] = false;

            if (key == GLFW_KEY_L && action == GLFW_PRESS) {
                cursorLocked = !cursorLocked;

                glfwSetInputMode(windowHandle, GLFW_CURSOR, cursorModes[cursorLocked ? 1 : 0]);
            }
        });
        glfwSetCursorPosCallback(windowHandle, (w, x, y) -> {
            cursor.isDirty = true;
        });
    }

    private static void initImGui() {
        ImGui.createContext();

        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init(glslVersion);

        ImGui.getIO().setConfigFlags(ImGuiConfigFlags.DockingEnable);
        ImGui.getStyle().setWindowRounding(7.5f);
    }

    private static final int[] cursorModes = new int[] {
            GLFW_CURSOR_NORMAL,
            GLFW_CURSOR_DISABLED
    };

    private static final double[] cursorPosXBuffer = new double[1];
    private static final double[] cursorPosYBuffer = new double[1];

    private static boolean frameFlush() {
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();

        directionMask.reset();
        handleKeyboardInput();

        camera.updatePosition(directionMask, deltaTime);

        if (cursor.isDirty) {
            glfwGetCursorPos(windowHandle, cursorPosXBuffer, cursorPosYBuffer);
            cursor.move((float) cursorPosXBuffer[0], (float) cursorPosYBuffer[0]);

            if (cursorLocked)
                camera.updateDirection(cursor.dx, cursor.dy);

            cursor.flush();
        }

        if (keys[GLFW_KEY_R]) {
            try {
                program.reloadIfChanged(List.of("mvp", "mv", "v", "tex"));
            } catch (IOException e) {
                System.err.println("failed to update Shader Program");
            }
        }

        return !glfwWindowShouldClose(windowHandle);
    }

    private static void handleKeyboardInput() {
        if (keys[GLFW_KEY_Q])
            glfwSetWindowShouldClose(windowHandle, true);

        if (keys[GLFW_KEY_W])
            directionMask.forward();
        if (keys[GLFW_KEY_S])
            directionMask.backward();
        if (keys[GLFW_KEY_D])
            directionMask.right();
        if (keys[GLFW_KEY_A])
            directionMask.left();
        if (keys[GLFW_KEY_LEFT_SHIFT])
            directionMask.down();
        if (keys[GLFW_KEY_SPACE])
            directionMask.up();
    }

    private static final Matrix4f mvp = new Matrix4f();
    private static final Matrix4f mv = new Matrix4f();

    private static void frame() {
        glClearColor(bgColor[0], bgColor[1], bgColor[2], 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glEnable(GL_DEPTH_TEST);
        glFrontFace(GL_CCW);
        glEnable(GL_CULL_FACE);

        program.use();

        mv.identity().mul(camera.viewMatrix())
                .rotate((float) glfwGetTime(), 0.0f, 1.0f, 0.0f);

        mvp.identity()
                .perspective((float) Math.toRadians(70.0f), (float) width / (float) height, 0.01f, 100.f)
                .mul(mv);

        program.setUniform("mvp", mvp);
        program.setUniform("mv", mv);
        program.setUniform("v", camera.viewMatrix());

        model.render();
    }

    private static void run() {
        double start = glfwGetTime();
        while (frameFlush()) {
            deltaTime = (float) (glfwGetTime() - start);
            start = glfwGetTime();

            glPolygonMode(GL_FRONT_AND_BACK, polygonMode);
            frame();
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

            imGuiGlfw.newFrame();
            ImGui.newFrame();

            renderImGui();

            ImGui.render();
            imGuiGl3.renderDrawData(ImGui.getDrawData());
        }
    }

    private static void renderImGui() {
        ImGui.setNextWindowPos(0.0f, 20.0f, ImGuiCond.Always);
        ImGui.setNextWindowSize(300.0f, ImGui.getMainViewport().getSizeY(), ImGuiCond.Appearing);
        if (ImGui.begin("LeftMenuBar", ImGuiWindowFlags.NoTitleBar)) {
            ImGui.text(String.format("FPS = %.2f", (1.0f / deltaTime)));
            ImGui.text(String.format("Delta time = %.4f", deltaTime));

            if (ImGui.button("Wireframe"))
                polygonMode = GL_LINE;
            ImGui.sameLine();
            if (ImGui.button("Fill"))
                polygonMode = GL_FILL;

            if (ImGui.collapsingHeader("Camera")) {
                ImGui.text(String.format(
                        "Position = (%.2f, %.2f, %.2f)", camera.position.x, camera.position.y, camera.position.z));
                ImGui.text(String.format("Yaw = %.2f, Pitch = %.2f", camera.yaw, camera.pitch));

                if (ImGui.sliderFloat("Speed", cameraSpeed, 0.0f, 50.0f)) {
                    camera.speed = cameraSpeed[0];
                }
                if (ImGui.sliderFloat("Sensitivity", cameraSensitivity, 0.0f, 5.0f)) {
                    camera.sensitivity = cameraSensitivity[0];
                }
            }
        }
        ImGui.end();

        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("file")) {

                ImGui.menuItem("new");
                ImGui.menuItem("open");

                ImGui.endMenu();
            }

            ImGui.endMainMenuBar();
        }
    }

    private static void cleanup() {
        program.delete();
        model.delete();

        Callbacks.glfwFreeCallbacks(windowHandle);

        imGuiGl3.dispose();
        imGuiGlfw.dispose();

        ImGui.destroyContext();

        glfwDestroyWindow(windowHandle);
        glfwTerminate();
    }
}
