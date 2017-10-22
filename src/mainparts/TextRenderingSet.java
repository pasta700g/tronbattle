package mainparts;

import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

public class TextRenderingSet {
	private static String texturePath = Define.dir + "/assets/data/Font_Migu 1C Regular.png";
	// 16x10 cells
	// 1cell 64x32

	static float w = 32, h = 64;
	float realw = 32, realh = 64;

	static float[] vertex = { -w / 2f, h / 2f, 0f, w / 2f, h / 2f, 0f, w / 2f, -h / 2f, 0f, -w / 2f, -h / 2f, 0f };
	static int[] index = { 0, 1, 2, 3 };

	FloatBuffer fb = BufferUtils.createFloatBuffer(16);
	private Matrix4f MatModel;

	public static HashMap<String, TextureSet> font = new HashMap<String, TextureSet>();

	float x, y, z;
	float scale;

	String text;

	boolean coloring;

	float r, g, b, a;

	int count;

	public TextRenderingSet() {
		this("");
	}

	public TextRenderingSet(String text) {
		this.text = text;

		x = 0;
		y = 0;
		z = 0;
		scale = 1;

		coloring = false;
		r = 1.0f;
		g = 1.0f;
		b = 1.0f;
		a = 1.0f;

		count = 0;
	}

	public static void createTexture() {
		float[] texcoord = new float[8];

		int code;
		for (int i = 32; i < 128; i++) {
			code = i - 32;

			texcoord[0] = (code % 16 * w) / 512f;
			texcoord[1] = (code / 16 * h) / 1024f;

			texcoord[2] = (code % 16 + 1) * w / 512f;
			texcoord[3] = (code / 16 * h) / 1024f;

			texcoord[4] = (code % 16 + 1) * w / 512f;
			texcoord[5] = (code / 16 + 1) * h / 1024f;

			texcoord[6] = (code % 16 * w) / 512f;
			texcoord[7] = (code / 16 + 1) * h / 1024f;

			code += 32;

			// System.out.println((char) code);

			font.put(String.valueOf((char) code), new TextureSet(Define.program, Define.window, Define.attrib_vertex,
					Define.attrib_texcoord, texturePath, vertex, texcoord, index));
		}
	}

	public void setScalse(float scale) {
		this.scale = scale;
		realh = h * scale;
	}

	public void setTranslation(float x, float y, float z) {
		this.x = x / scale;
		this.y = y / scale;
		this.z = z / scale;
	}

	public void setColor4f(float r, float g, float b, float a) {
		coloring = true;
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public void setText(String text) {
		this.text = text;
	}

	public float getTextWidth() {
		return text.length() * (realw/2.0f);
	}

	public float getTextHeight() {
		return realh;
	}

	public void draw() {
		count++;

		for (int i = 0; i < text.length(); i++) {
			glUseProgram(Define.program);
			MatModel = new Matrix4f().scale(scale)
					.translate(x + (realw - 2) * i - text.length() * realw / 2.0f + realw / 2, y, z);
			glUniformMatrix4fv(glGetUniformLocation(Define.program, "model"), false, MatModel.get(fb));
			if (coloring) {
				glUniform1i(glGetUniformLocation(Define.program, "coloring"), 1);
			} else {
				glUniform1i(glGetUniformLocation(Define.program, "coloring"), 0);
			}
			glUniform4f(glGetUniformLocation(Define.program, "Color"), r, g, b, a);
			glUseProgram(0);
			font.get(String.valueOf(text.charAt(i))).draw();
		}

	}

}
