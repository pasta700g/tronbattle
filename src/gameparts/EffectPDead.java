package gameparts;

import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import mainparts.Define;
import mainparts.Main;
import mainparts.TextureSet;

public class EffectPDead {
	private String texturePath = Define.dir + "/assets/images/deadeffect.png";

	float[] vertex = { -60f, -60f, 0f, 60f, -60f, 0f, 60f, 60f, 0f, -60f, 60f, 0f };

	float[][] texcoord;
	int[] index = { 0, 1, 2, 3 };

	FloatBuffer fb = BufferUtils.createFloatBuffer(16);
	private Matrix4f MatModel;

	float x, y;

	int temp;

	// カウント
	int count;

	// 実行中かどうかのフラグ
	boolean flag;
	boolean dead;

	int id;

	public EffectPDead() {
		float w = 120, ww = 600;
		texcoord = new float[5][8];
		for (int i = 0; i < 5; i++) {
			texcoord[i][0] = w * i / ww;
			texcoord[i][1] = 0.0f;
			texcoord[i][2] = w * (i + 1) / ww;
			texcoord[i][3] = 0.0f;
			texcoord[i][4] = w * (i + 1) / ww;
			texcoord[i][5] = 1.0f;
			texcoord[i][6] = w * i / ww;
			texcoord[i][7] = 1.0f;

			Main.textures.put("effectpdead" + String.valueOf(i), new TextureSet(Define.program, Define.window,
					Define.attrib_vertex, Define.attrib_texcoord, texturePath, vertex, texcoord[i], index));
		}

		count = 0;

		temp = 0;

		flag = false;
		dead = false;
	}

	//描画
	private void draw() {
		temp = count % 18 / 3;

		if (temp == 5) {
			flag = false;
			temp = 0;
		}

		if (flag) {
			glUseProgram(Define.program);
			MatModel = new Matrix4f().translate(x, y, 0.1f);
			glUniformMatrix4fv(glGetUniformLocation(Define.program, "model"), false, MatModel.get(fb));
			glUseProgram(0);
			Main.textures.get("effectpdead" + String.valueOf(temp)).draw();
		}
	}

	//プレイヤー消滅エフェクトの開始
	public void setFlag(float x, float y) {
		count = 0;
		flag = true;
		this.x = x;
		this.y = y;
		this.id = id;
	}

	//メイン
	public void all() {
		if (flag) {
			draw();
			count++;
		}

		if (count > 20) {
			dead = true;
		}
	}

}
