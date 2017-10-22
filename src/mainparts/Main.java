package mainparts;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.NumberFormat;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import gameparts.InputHandler;

public class Main {
	private String Title = "TronGame";

	float[] vertex = { -10f, 10f, 0f, 10f, 10f, 0f, 10f, -10f, 0f, -10f, -10f, 0f };

	float[] vertexalt = { 1.5f, 0.5f, -10f, 2.5f, 0.5f, -10f, 2.5f, -0.5f, -10f, 1.5f, -0.5f, -10f };

	float[] texcoord = { 0.f, 0.f, 1f, 0.f, 1.f, 1.f, 0.f, 1f };

	int[] index = { 0, 1, 2, 3 };

	// シェーダのパス
	private String shaderVPath = Define.dir + "/assets/shaders/V1.vert";
	private String shaderFPath = Define.dir + "/assets/shaders/F1.frag";

	// シェーダとプログラムを管理するID
	private int shaderV = 0;// vertex
	private int shaderF = 0;// fragment

	private Matrix4f MatProjection, MatModel, MatView;

	private float x = 0, y = 0, z = 0;

	// FPS制御用
	private long variableYieldTime, lastTime;
	private double last = 0, now = 0;
	private static final NumberFormat nf = NumberFormat.getInstance();
	private static final int divisor = 1000000000;
	private double delta = 0;
	private int frame = 1;

	// テクスチャ格納
	public static HashMap<String, TextureSet> textures = new HashMap<String, TextureSet>();

	public void run() {
		System.out.println("LWJGL " + Version.getVersion() + ".");

		init();
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(Define.window);
		glfwDestroyWindow(Define.window);

		finish();

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are
									// already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden
													// after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be
													// resizable
		// WindowのHintにOpenGLのコアバージョンを指定.MAJORが 3 ,MINORが 2
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);

		// Create the window
		Define.window = glfwCreateWindow(Define.width, Define.height, Title, NULL, NULL);
		if (Define.window == NULL) {
			JOptionPane.showMessageDialog(null, "ウィンドウの生成に失敗しました。\nOpenGLのバージョンが正しくありません", Title,
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		glfwSetWindowAspectRatio(Define.window, Define.width, Define.height);

		// Get the thread stack and push a new frame
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(Define.window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(Define.window, (vidmode.width() - pWidth.get(0)) / 2,
					(vidmode.height() - pHeight.get(0)) / 2);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(Define.window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(Define.window);

		nf.setMaximumFractionDigits(10);

	}

	private void loop() {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();

		initShader();

		// Set the clear color
		glClearColor(0.2f, 0.2f, 0.2f, 0.0f);

		FloatBuffer fb = BufferUtils.createFloatBuffer(16);
		MatProjection = new Matrix4f().ortho(-Define.width / 2, Define.width / 2, -Define.height / 2, Define.height / 2,
				1, -0);
		MatView = new Matrix4f().lookAt(0, 0, 1, 0, 0, 0, 0, 1, 0);

		SceneManager scenemanager = new SceneManager();

		TextRenderingSet.createTexture();

		InputHandler.init(Define.window);

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while (!glfwWindowShouldClose(Define.window)) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the
																// framebuffer

			// _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/

			// FPS制御
			sync(60);

			// 透視投影
			// ----------------------------------------------------------------------------------
			glUseProgram(Define.program);
			glUniformMatrix4fv(glGetUniformLocation(Define.program, "projection"), false, MatProjection.get(fb));
			glUniformMatrix4fv(glGetUniformLocation(Define.program, "view"), false, MatView.get(fb));
			glUseProgram(0);
			// ----------------------------------------------------------------------------------

			// やりたいことを書く

			scenemanager.exec();

			InputHandler.update();

			// _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/

			glfwSwapBuffers(Define.window); // swap the color buffers

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}
	}

	public static void main(String[] args) {
		new Main().run();
	}

	private void finish() {
		for (String key : textures.keySet()) {
			textures.get(key).finish();
		}

		glDisableVertexAttribArray(Define.attrib_vertex);
		glDisableVertexAttribArray(Define.attrib_texcoord);
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		glDeleteProgram(Define.program);
	}

	private void sync(int fps) {
		/**
		 * An accurate sync method that adapts automatically to the system it
		 * runs on to provide reliable results.
		 *
		 * @param fps
		 *            The desired frame rate, in frames per second
		 * @author kappa (On the LWJGL Forums)
		 */
		if (fps <= 0)
			return;

		long sleepTime = 1000000000 / fps; // nanoseconds to sleep this frame
		// yieldTime + remainder micro & nano seconds if smaller than sleepTime
		long yieldTime = Math.min(sleepTime, variableYieldTime + sleepTime % (1000 * 1000));
		long overSleep = 0; // time the sync goes over by

		try {
			while (true) {
				long t = System.nanoTime() - lastTime;

				if (t < sleepTime - yieldTime) {
					Thread.sleep(1);
				} else if (t < sleepTime) {
					// burn the last few CPU cycles to ensure accuracy
					Thread.yield();
				} else {
					overSleep = t - sleepTime;
					break; // exit while loop
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lastTime = System.nanoTime() - Math.min(overSleep, sleepTime);

			// auto tune the time sync should yield
			if (overSleep > variableYieldTime) {
				// increase by 200 microseconds (1/5 a ms)
				variableYieldTime = Math.min(variableYieldTime + 200 * 1000, sleepTime);
			} else if (overSleep < variableYieldTime - 200 * 1000) {
				// decrease by 2 microseconds
				variableYieldTime = Math.max(variableYieldTime - 2 * 1000, 0);
			}
		}

		now = System.nanoTime();
		delta += (now - last) / divisor;
		last = now;
		if (delta >= 1.0) {
			// System.out.println(frame);
			glfwSetWindowTitle(Define.window, Title + "     fps " + frame);
			delta = 0;
			frame = 1;
		} else {
			frame++;
		}
	}

	// シェーダを読み込む
	// コンパイル、プログラムにアタッチ
	// プログラムを使用
	private void initShader() {
		// Init Vertex Shader
		shaderV = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(shaderV, loadShader(shaderVPath));
		glCompileShader(shaderV);
		IntBuffer error1 = BufferUtils.createIntBuffer(1);
		glGetShaderiv(shaderV, GL_COMPILE_STATUS, error1);
		if (error1.get() != GL_TRUE) {
			JOptionPane.showMessageDialog(null, "頂点シェーダのコンパイルに失敗\nエラー表示\n" + glGetShaderInfoLog(shaderV), Title,
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		// Init Fragment Shader
		shaderF = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(shaderF, loadShader(shaderFPath));
		glCompileShader(shaderF);
		IntBuffer error2 = BufferUtils.createIntBuffer(1);
		glGetShaderiv(shaderF, GL_COMPILE_STATUS, error2);
		if (error2.get() != GL_TRUE) {
			JOptionPane.showMessageDialog(null, "フラグメントシェーダのコンパイルに失敗\nエラー表示\n" + glGetShaderInfoLog(shaderF), Title,
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		// set Shader to Program
		Define.program = glCreateProgram();

		glAttachShader(Define.program, shaderV);
		glAttachShader(Define.program, shaderF);

		glDeleteShader(shaderV);
		glDeleteShader(shaderF);

		glLinkProgram(Define.program);
		IntBuffer error3 = BufferUtils.createIntBuffer(1);
		glGetProgramiv(Define.program, GL_LINK_STATUS, error3);
		if (error3.get() != GL_TRUE) {
			JOptionPane.showMessageDialog(null, "プログラムのリンクに失敗", Title, JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		glUseProgram(Define.program);

		// get (Vertex) Attribute Location
		Define.attrib_vertex = glGetAttribLocation(Define.program, "vert");
		Define.attrib_texcoord = glGetAttribLocation(Define.program, "texcoord");

		// set Shader Uniforms
		glUniform1i(glGetUniformLocation(Define.program, "texSampler"), 0);

		glUseProgram(0);

	}

	private String loadShader(String path) {
		StringBuilder source = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			String line;
			while ((line = reader.readLine()) != null) {
				source.append(line);
				source.append(System.getProperty("line.separator"));
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return source.toString();
	}

}