package mainparts;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL33.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL14;
import org.lwjgl.stb.STBImage;

public class TextureSet {
	// The window handle
	long window;

	int program = 0;

	int attrib_vertex, attrib_texcoord;

	float[] vertex;
	float[] texcoord;
	int[] index;

	String texturePath;

	int VArrayID = 0;
	int VBufferID = 0;
	int TBufferID = 0;
	int IndexID = 0;

	int SamplerID = 0;
	int textureID = -1;

	Matrix4f MatProjection, MatModel, MatView;
	public float x = 0, y = 0, z = 0;

	FloatBuffer fb = BufferUtils.createFloatBuffer(16);

	public int w, h;

	public TextureSet(int program, long window, int attrib_vertex, int attrib_texcoord, String texturePath,
			float[] vertex, float[] texcoord, int[] index) {
		this.program = program;
		this.window = window;
		this.attrib_vertex = attrib_vertex;
		this.attrib_texcoord = attrib_texcoord;
		this.texturePath = texturePath;
		this.vertex = vertex;
		this.texcoord = texcoord;
		this.index = index;

		initVAO();
		initTexture();

		MatModel = new Matrix4f().translate(x, y, z).scale(100);
	}

	private void initVAO() {
		glfwSetWindowSizeCallback(window, (window, width, height) -> {
			glViewport(0, 0, width, height);
		});

		FloatBuffer verticesbuffer = BufferUtils.createFloatBuffer(vertex.length);
		verticesbuffer.put(vertex).flip();

		FloatBuffer texcoordbuffer = BufferUtils.createFloatBuffer(texcoord.length);
		texcoordbuffer.put(texcoord).flip();

		IntBuffer indexbuffer = BufferUtils.createIntBuffer(index.length);
		indexbuffer.put(index).flip();

		VArrayID = glGenVertexArrays();
		glBindVertexArray(VArrayID);

		// Vertex Buffer Object
		VBufferID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, VBufferID);
		glBufferData(GL_ARRAY_BUFFER, verticesbuffer, GL_STATIC_DRAW);
		glVertexAttribPointer(attrib_vertex, 3, GL_FLOAT, false, 0, 0);

		// Texture Coordinate Buffer Object
		TBufferID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, TBufferID);
		glBufferData(GL_ARRAY_BUFFER, texcoordbuffer, GL_STATIC_DRAW);
		glVertexAttribPointer(attrib_texcoord, 2, GL_FLOAT, false, 0, 0);

		// Element Buffer Object
		IndexID = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IndexID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexbuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);

		// System.out.println(VArrayID+" "+VBufferID+ " "+TBufferID+"
		// "+IndexID);
	}

	private void initTexture() {
		// Load Image using stb
		ByteBuffer img;
		IntBuffer width, height, comp;
		width = BufferUtils.createIntBuffer(1);
		height = BufferUtils.createIntBuffer(1);
		comp = BufferUtils.createIntBuffer(1);

		img = STBImage.stbi_load(texturePath, width, height, comp, STBImage.STBI_default);
		if (img == null) {
			System.err.println("テクスチャを読み込めませんでした");
			System.exit(0);
		}

		w = width.get(0);
		h = height.get(0);

		// Generate Texture
		textureID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureID);
		if (comp.get(0) == 3) {
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width.get(0), height.get(0), 0, GL_RGB, GL_UNSIGNED_BYTE, img);
		} else if (comp.get(0) == 4) {
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, img);
		}
		STBImage.stbi_image_free(img);

		// set Sampler
		SamplerID = glGenSamplers();
		glSamplerParameteri(SamplerID, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glSamplerParameteri(SamplerID, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(SamplerID, GL_TEXTURE_WRAP_S, GL14.GL_MIRRORED_REPEAT);
		glSamplerParameteri(SamplerID, GL_TEXTURE_WRAP_T, GL14.GL_MIRRORED_REPEAT);
	}

	void initMatrix() {
		MatModel = new Matrix4f().identity();
		glUniform1i(glGetUniformLocation(Define.program, "coloring"), 0);
		glUniformMatrix4fv(glGetUniformLocation(program, "model"), false, MatModel.get(fb));
	}

	public void draw() {
		glUseProgram(program);
		 glEnable(GL_BLEND);
		 glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glActiveTexture(GL_TEXTURE0);
		// 第2引数のIDのテクスチャを第1引数の形式で処理することをバインド
		glBindTexture(GL_TEXTURE_2D, textureID);// *
		// サンプラーのバインド
		glBindSampler(0, SamplerID);// *

		glBindVertexArray(VArrayID);// *
		glEnableVertexAttribArray(attrib_vertex);
		glEnableVertexAttribArray(attrib_texcoord);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IndexID);// *

		glDrawElements(GL_QUADS, index.length, GL_UNSIGNED_INT, 0);

		glDisableVertexAttribArray(attrib_vertex);
		glDisableVertexAttribArray(attrib_texcoord);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glBindVertexArray(0);

		glBindSampler(0, 0);
		glBindTexture(GL_TEXTURE_2D, 0);

		// 行列初期化
		initMatrix();

		glUseProgram(0);
	}

	void finish() {

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDeleteBuffers(VBufferID);
		glDeleteBuffers(TBufferID);
		glDeleteBuffers(IndexID);

		glDeleteSamplers(SamplerID);

		glBindVertexArray(0);
		glDeleteVertexArrays(VArrayID);
	}

}
