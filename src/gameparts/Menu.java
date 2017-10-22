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

public class Menu {
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
	TextRenderingSet[] menuoption;

	public Menu() {
		title = new TextRenderingSet("Tron Game");
		title.setScalse(2.0f);
		title.setTranslation(0, 200, 0);
		title.setColor4f(0.0f, 0.0f, 0.0f, 1.0f);

		menuoption = new TextRenderingSet[4];
		for (int i = 0; i < 4; i++) {
			menuoption[i] = new TextRenderingSet();
			menuoption[i].setScalse(0.7f);
			menuoption[i].setColor4f(0.0f, 0.0f, 0.0f, 1.0f);
			menuoption[i].setTranslation(0, -i * menuoption[i].getTextHeight(), 0);
		}
		menuoption[0].setText("< 2 player mode >");
		menuoption[1].setText("< 4 player mode >");
		menuoption[2].setText("< config >");
		menuoption[3].setText("< quit >");

		Main.textures.put("menuback", new TextureSet(Define.program, Define.window, Define.attrib_vertex,
				Define.attrib_texcoord, texturePath, vertex, texcoord, index));

		count = 0;
		flag = false;

		nowoption = -1;

		currentstate = scene_state.Scene_running;
	}

	// 選択肢カーソルの移動
	private void move() {
		if (InputHandler.keyPressed(GLFW_KEY_DOWN)) {
			nowoption++;
			if (nowoption == 4) {
				nowoption = 0;
			}
		}

		if (InputHandler.keyPressed(GLFW_KEY_UP)) {
			nowoption--;
			if (nowoption <= -1) {
				nowoption = 3;
			}
		}
	}

	// 描画
	private void draw() {
		glUseProgram(Define.program);
		MatModel = new Matrix4f().translate(0, 0, 0);

		glUniformMatrix4fv(glGetUniformLocation(Define.program, "model"), false, MatModel.get(fb));
		glUseProgram(0);
		Main.textures.get("menuback").draw();

		title.draw();

		float h = menuoption[0].getTextHeight();
		for (int i = 0; i < 4; i++) {
			if (nowoption == i) {
				menuoption[i].setScalse(0.8f);
				menuoption[i].setColor4f(0.4f, 0.4f, 1.0f, 1.0f);
				menuoption[i].setTranslation(0, -i * h, 0);
				menuoption[i].draw();
				menuoption[i].setScalse(0.7f);
				menuoption[i].setColor4f(0.0f, 0.0f, 0.0f, 1.0f);
				menuoption[i].setTranslation(0, -i * h, 0);
			} else {
				menuoption[i].draw();
			}
		}

	}

	// シーンの状態を変更
	private void change() {
		if (InputHandler.keyPressed(GLFW_KEY_ENTER)) {
			switch (nowoption) {
			case (0):
				currentstate = scene_state.Scene_finish;
				nextscene = scene_name.Scene_2pGame;
				break;
			case (1):
				currentstate = scene_state.Scene_finish;
				nextscene = scene_name.Scene_4pGame;
				break;
			case (2):
				currentstate = scene_state.Scene_finish;
				nextscene = scene_name.Scene_Config;
				break;
			case (3):
				System.out.println("exit");
				glfwSetWindowShouldClose(Define.window, true);
				break;
			}
		}
	}

	// メイン
	public scene_state all() {
		if (InputHandler.keyPressed(GLFW_KEY_ESCAPE)) {
			System.out.println("exit");
			glfwSetWindowShouldClose(Define.window, true);
		}

		change();
		move();
		draw();

		count++;

		return currentstate;
	}

	// シーンの遷移先を渡す
	public scene_name getNextScece() {
		return nextscene;
	}

}
