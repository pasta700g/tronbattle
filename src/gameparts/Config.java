package gameparts;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import mainparts.Define;
import mainparts.Define.scene_name;
import mainparts.Define.scene_state;
import mainparts.Main;
import mainparts.TextRenderingSet;
import mainparts.TextureSet;

public class Config {
	private String texturePath = Define.dir + "/assets/images/menuback.png";

	float[] vertex = { -Define.width / 2, Define.height / 2, 0, Define.width / 2, Define.height / 2, 0,
			Define.width / 2, -Define.height / 2, 0, -Define.width / 2, -Define.height / 2, 0 };
	float[] texcoord = { 0.f, 0.f, 1f, 0.f, 1.f, 1.f, 0.f, 1f };
	int[] index = { 0, 1, 2, 3 };

	int count;
	boolean flag;
	float xclick, yclick;

	FloatBuffer fb = BufferUtils.createFloatBuffer(16);
	private Matrix4f MatModel;

	int nowoption;

	scene_name nextscene;
	scene_state currentstate;

	TextRenderingSet title;
	TextRenderingSet[] configoption;
	TextRenderingSet[] lang;
	TextRenderingSet arrow;
	TextRenderingSet note1;
	TextRenderingSet note2;

	boolean arrowflag;

	public Config() {
		title = new TextRenderingSet("Config");
		title.setScalse(1.0f);
		title.setTranslation(0, 300, 0);
		title.setColor4f(0.0f, 0.0f, 0.0f, 1.0f);

		lang = new TextRenderingSet[4];
		for (int i = 0; i < lang.length; i++) {
			lang[i] = new TextRenderingSet(Define.files[0].getName());
			lang[i].setScalse(0.5f);
			lang[i].setTranslation(0, 190 - i * lang[i].getTextHeight()*(0.7f/0.5f) * 2.5f, 0);
			lang[i].setColor4f(0.0f, 0.7f, 0.5f, 1.0f);
		}

		configoption = new TextRenderingSet[5];
		for (int i = 0; i < configoption.length; i++) {
			configoption[i] = new TextRenderingSet();
			configoption[i].setScalse(0.7f);
			configoption[i].setColor4f(0.0f, 0.0f, 0.0f, 1.0f);
			configoption[i].setTranslation(0, 230 - i * configoption[i].getTextHeight() * 2.5f, 0);
		}
		configoption[0].setText("< AI's language (player 0) >");
		configoption[1].setText("< AI's language (player 1) >");
		configoption[2].setText("< AI's language (player 2) >");
		configoption[3].setText("< AI's language (player 3) >");
		configoption[4].setText("< back >");

		arrow = new TextRenderingSet("<-             ->");
		arrow.setScalse(0.7f);
		arrow.setColor4f(0.4f, 0.4f, 1.0f, 1.0f);
		arrowflag = true;

		note1 = new TextRenderingSet("location of AI : TronGame/assets/AI/");
		note1.setScalse(0.5f);
		note1.setColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		note1.setTranslation(-300 + note1.getTextWidth() / 2, -290, 0);
		note2 = new TextRenderingSet("filename : only alphanumeric and symbols");
		note2.setScalse(0.5f);
		note2.setColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		note2.setTranslation(-300 + note2.getTextWidth() / 2, -320, 0);

		Main.textures.put("menuback", new TextureSet(Define.program, Define.window, Define.attrib_vertex,
				Define.attrib_texcoord, texturePath, vertex, texcoord, index));

		count = 0;
		flag = false;

		nowoption = -1;

		currentstate = scene_state.Scene_running;
	}

	//選択肢カーソルの移動
	private void move() {
		if (InputHandler.keyPressed(GLFW_KEY_DOWN)) {
			nowoption++;
			if (nowoption == configoption.length) {
				nowoption = 0;
			}
		}

		if (InputHandler.keyPressed(GLFW_KEY_UP)) {
			nowoption--;
			if (nowoption <= -1) {
				nowoption = configoption.length - 1;
			}
		}

		if (nowoption >= 0 && nowoption <= configoption.length - 2 && InputHandler.keyPressed(GLFW_KEY_RIGHT)) {
			Define.langtype[nowoption]++;
			if (Define.langtype[nowoption] == Define.files.length) {
				Define.langtype[nowoption] = 0;
			}
		}

		if (nowoption >= 0 && nowoption <= configoption.length - 2 && InputHandler.keyPressed(GLFW_KEY_LEFT)) {
			Define.langtype[nowoption]--;
			if (Define.langtype[nowoption] <= -1) {
				Define.langtype[nowoption] = Define.files.length - 1;
			}
		}

	}

	//描画
	private void draw() {
		glUseProgram(Define.program);
		MatModel = new Matrix4f().translate(0, 0, 0);

		glUniformMatrix4fv(glGetUniformLocation(Define.program, "model"), false, MatModel.get(fb));
		glUseProgram(0);
		Main.textures.get("menuback").draw();

		title.draw();

		float h = configoption[0].getTextHeight();

		for (int i = 0; i < configoption.length; i++) {
			if (nowoption == i) {
				configoption[i].setScalse(0.7f);
				configoption[i].setColor4f(0.4f, 0.4f, 1.0f, 1.0f);
				configoption[i].setTranslation(0, 230 - i * h * 2.5f, 0);
				configoption[i].draw();
				configoption[i].setScalse(0.7f);
				configoption[i].setColor4f(0.0f, 0.0f, 0.0f, 1.0f);
				configoption[i].setTranslation(0, 230 - i * h * 2.5f, 0);
			} else {
				configoption[i].draw();
			}
		}

		for (int i = 0; i < lang.length; i++) {
			lang[i].setText(Define.files[Define.langtype[i]].getName());
			if (nowoption == i) {
				lang[i].setScalse(0.5f);
				lang[i].setColor4f(0.4f, 0.4f, 1.0f, 1.0f);
				lang[i].setTranslation(0, 190 - i * h * 2.5f, 0);
				lang[i].draw();
				lang[i].setScalse(0.5f);
				lang[i].setColor4f(0.0f, 0.7f, 0.5f, 1.0f);
				lang[i].setTranslation(0, 190 - i * h * 2.5f, 0);
			} else {
				lang[i].draw();
			}
		}

		if (arrowflag) {
			arrow.setText("<-                    ->");
		} else {
			arrow.setText("<-                     ->");
		}
		arrow.setTranslation(0, 190 - nowoption * arrow.getTextHeight() * 2.5f, 0);
		if (nowoption >= 0 && nowoption <= configoption.length - 2) {
			arrow.draw();
		}

		note1.draw();
		note2.draw();
	}

	//シーンの状態変更
	private void change() {
		if (InputHandler.keyPressed(GLFW_KEY_ENTER)) {
			switch (nowoption) {
			case (0):
				break;
			case (4):
				currentstate = scene_state.Scene_finish;
				nextscene = scene_name.Scene_Menu;
				break;
			}
		}
	}

	//メイン
	public scene_state all() {
		change();
		move();
		draw();

		count++;
		if (count > 40) {
			count = 0;
			arrowflag = !arrowflag;
		}

		return currentstate;
	}

	//シーンの遷移先を渡す
	public scene_name getNextScece() {
		return nextscene;
	}

}
