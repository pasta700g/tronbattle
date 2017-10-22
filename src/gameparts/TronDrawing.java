package gameparts;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import mainparts.Define;
import mainparts.Define.PANEL_INDEX;
import mainparts.Define.VEC;
import mainparts.Define.cell_name;
import mainparts.Define.direction;
import mainparts.Define.scene_name;
import mainparts.Define.scene_state;
import mainparts.Main;
import mainparts.TextRenderingSet;
import mainparts.TextureSet;

public class TronDrawing {
	EffectPDead[] effect_pdead;

	Player[] player;
	VEC[] position;
	boolean[] pflag;
	int[] initialrow;
	int[] initialcol;

	private String texturePath = Define.dir + "/assets/images/cell.png";

	float[] vertex = { -16f, 16f, 0f, 16f, 16f, 0f, 16f, -16f, 0f, -16f, -16f, 0f };

	float[] texcoord = { 0.f, 0.f, 1f, 0.f, 1.f, 1.f, 0.f, 1f };

	int[] index = { 0, 1, 2, 3 };

	float x = 0;
	float y = 0;
	float z = 0;

	int w;
	int h;

	FloatBuffer fb = BufferUtils.createFloatBuffer(16);
	private Matrix4f MatModel;

	static Define.Cell field[][];

	scene_name nextscene;
	scene_state currentstate;

	int playercount;

	int survived;

	TextRenderingSet[] playername;
	TextRenderingSet rankingtitle;
	TextRenderingSet[] ranking;

	TextRenderingSet[] option;
	int nowoption;

	boolean winsetting;

	List<Integer> turn;

	public TronDrawing(int playercount) {
		this.playercount = playercount;

		effect_pdead = new EffectPDead[playercount];
		playername = new TextRenderingSet[playercount];
		position = new VEC[playercount];
		player = new Player[playercount];
		pflag = new boolean[playercount];
		initialrow = new int[playercount];
		initialcol = new int[playercount];
		boolean duplicating;
		do {
			duplicating = false;
			for (int i = 0; i < playercount; i++) {
				initialrow[i] = (int) (Math.random() * Define.ROW);
				initialcol[i] = (int) (Math.random() * Define.COLUMN);
			}
			for (int i = 0; i < playercount; i++) {
				for (int j = i + 1; j < playercount; j++) {
					if (initialrow[i] == initialrow[j] && initialcol[i] == initialcol[j])
						duplicating = true;
				}
			}
		} while (duplicating);

		Main.textures.put("cell", new TextureSet(Define.program, Define.window, Define.attrib_vertex,
				Define.attrib_texcoord, texturePath, vertex, texcoord, index));

		w = Main.textures.get("cell").w;
		h = Main.textures.get("cell").h;

		field = new Define.Cell[Define.ROW][Define.COLUMN];
		for (int i = 0; i < Define.ROW; i++) {
			for (int j = 0; j < Define.COLUMN; j++) {
				field[i][j] = new Define.Cell();
				field[i][j].state = Define.cell_name.empty;
				field[i][j].basepoint.x = -(Define.COLUMN / 2) * w + j * w - 100;
				field[i][j].basepoint.y = (Define.ROW / 2) * h - i * h + 40;
				field[i][j].parent = Define.direction.unknown;
				field[i][j].next = Define.direction.unknown;
			}
		}

		for (int i = 0; i < playercount; i++) {
			playername[i] = new TextRenderingSet(
					"player" + String.valueOf(i) + " : " + Define.files[Define.langtype[i]].getName());
			playername[i].setScalse(0.5f);
			switch (i) {
			case (0):
				playername[i].setColor4f(0.00f, 0.64f, 0.91f, 1.0f);
				playername[i].setTranslation(-Define.width / 2 + 20 + playername[i].getTextWidth() / 2f, -300, 0);
				break;
			case (1):
				playername[i].setColor4f(1.00f, 0.50f, 0.15f, 1.0f);
				playername[i].setTranslation(-Define.width / 2 + 20 + playername[i].getTextWidth() / 2f, -300 - 40, 0);
				break;
			case (2):
				playername[i].setColor4f(0.13f, 0.69f, 0.30f, 1.0f);
				playername[i].setTranslation(-Define.width / 2 + 500 + playername[i].getTextWidth() / 2f, -300, 0);
				break;
			case (3):
				playername[i].setColor4f(1.00f, 0.95f, 0.00f, 1.0f);
				playername[i].setTranslation(-Define.width / 2 + 500 + playername[i].getTextWidth() / 2f, -300 - 40, 0);
				break;
			}

			effect_pdead[i] = new EffectPDead();
			position[i] = new VEC();
			player[i] = new Player(i, initialrow[i], initialcol[i], field[initialrow[i]][initialcol[i]].basepoint,
					Define.files[Define.langtype[i]].getName());
			pflag[i] = true;
		}

		System.out.println("initial position :");
		Define.writeLog("initial position :", true);
		for (int i = 0; i < playercount; i++) {
			System.out.println("player " + i + " row=" + initialrow[i] + " , column=" + initialcol[i]);
			Define.writeLog("player " + i + " row=" + initialrow[i] + " , column=" + initialcol[i], true);
		}

		currentstate = scene_state.Scene_running;

		survived = playercount;

		rankingtitle = new TextRenderingSet("RANK");
		rankingtitle.setScalse(0.7f);
		rankingtitle.setTranslation(480, 320, 0);
		ranking = new TextRenderingSet[playercount];
		for (int i = 0; i < playercount; i++) {
			ranking[i] = new TextRenderingSet(String.valueOf(i + 1) + ". --------");
			ranking[i].setScalse(0.5f);
			ranking[i].setTranslation(480, 280 - i * (playername[i].getTextHeight()), 0);
		}

		option = new TextRenderingSet[2];
		option[0] = new TextRenderingSet("play new game");
		option[0].setScalse(0.5f);
		option[0].setTranslation(480, -310, 0);
		option[1] = new TextRenderingSet("back to menu");
		option[1].setScalse(0.5f);
		option[1].setTranslation(480, -310 - option[1].getTextHeight() * 1, 0);

		winsetting = false;

		nowoption = -1;

		if (playercount == 2) {
			turn = Stream.of(0, 1).collect(Collectors.toList());
		} else {
			turn = Stream.of(0, 1, 2, 3).collect(Collectors.toList());
		}
		Collections.shuffle(turn);
		System.out.print("turn : ");
		Define.writeLog("turn : ", false);
		for (int myturn : turn) {
			System.out.print(myturn + " -> ");
			Define.writeLog(myturn + " -> ", false);
		}
		System.out.println();
		Define.writeLog("", true);
	}

	// 選択肢カーソルの移動
	private void move() {
		if (InputHandler.keyPressed(GLFW_KEY_DOWN)) {
			nowoption++;
			if (nowoption == 2) {
				nowoption = 0;
			}
		}

		if (InputHandler.keyPressed(GLFW_KEY_UP)) {
			nowoption--;
			if (nowoption <= -1) {
				nowoption = 1;
			}
		}
	}

	// 描画
	private void draw() {
		for (int i = 0; i < playercount; i++) {
			playername[i].draw();
		}

		for (int row = 0; row < Define.ROW; row++) {
			for (int col = 0; col < Define.COLUMN; col++) {
				glUseProgram(Define.program);
				MatModel = new Matrix4f().translate(x + field[row][col].basepoint.x, y + field[row][col].basepoint.y,
						z);

				glUniformMatrix4fv(glGetUniformLocation(Define.program, "model"), false, MatModel.get(fb));
				glUseProgram(0);
				Main.textures.get("cell").draw();
			}
		}

		rankingtitle.draw();
		for (TextRenderingSet t : ranking) {
			t.draw();
		}

		float h = option[0].getTextHeight();
		for (int i = 0; i < 2; i++) {
			if (nowoption == i) {
				option[i].setScalse(0.6f);
				option[i].setColor4f(0.4f, 0.4f, 1.0f, 1.0f);
				option[i].setTranslation(480, -310 - i * h, 0);
				option[i].draw();
				option[i].setScalse(0.5f);
				option[i].setColor4f(1.0f, 1.0f, 1.0f, 1.0f);
				option[i].setTranslation(480, -310 - i * h, 0);
			} else {
				option[i].draw();
			}
		}

	}

	// シーンの状態を変更
	private void change() {
		if (InputHandler.keyPressed(GLFW_KEY_ENTER)) {
			switch (nowoption) {
			case (0):
				currentstate = scene_state.Scene_finish;
				if (playercount == 2) {
					nextscene = scene_name.Scene_2pGame;
				} else if (playercount == 4) {
					nextscene = scene_name.Scene_4pGame;
				}
				break;
			case (1):
				for (int i = 0; i < playercount; i++) {
					player[i] = null;
				}
				currentstate = scene_state.Scene_finish;
				nextscene = scene_name.Scene_Menu;
				break;
			}
		}
	}

	// シーンの遷移先を渡す
	public scene_name getNextScece() {
		return nextscene;
	}

	// プレイヤ死亡時の盤面一部初期化
	public static void playerDead(int id, cell_name mine) {
		for (int i = 0; i < Define.ROW; i++) {
			for (int j = 0; j < Define.COLUMN; j++) {
				if (field[i][j].state == mine) {
					field[i][j].state = cell_name.empty;
					field[i][j].parent = direction.unknown;
					field[i][j].next = direction.unknown;
				}
			}
		}
	}

	// マップの情報をテキストファイルに出力
	public void gameInputMap() {
		File file = new File("D:/hajime/desktop/fieldstate.txt");

		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			for (int i = 0; i < Define.ROW; i++) {
				for (int j = 0; j < Define.COLUMN; j++) {
					switch (field[i][j].state) {
					case player0:
						pw.print("0");
						break;
					case player1:
						pw.print("1");
						break;
					case player2:
						pw.print("2");
						break;
					case player3:
						pw.print("3");
						break;
					case empty:
						pw.print("9");
						break;
					}
				}
				pw.println();
			}
			pw.close();
		} catch (IOException e) {
			System.out.println("gameInput > ファイルの書き込みに失敗");
			e.printStackTrace();
		}
	}

	// AIに渡すゲーム情報を作成するメソッドを呼ぶ
	private void gameInput(int id) {
		PANEL_INDEX[] pindex;
		pindex = new PANEL_INDEX[playercount];
		for (int i = 0; i < playercount; i++) {
			if (pflag[i]) {
				pindex[i] = new PANEL_INDEX(player[i].getIndex().row, player[i].getIndex().column);
			} else {
				pindex[i] = new PANEL_INDEX(-1, -1);
			}
		}

		player[id].createGameInput(playercount, initialrow, initialcol, pflag, pindex);
	}

	// プレイヤのターンを呼び出す
	private void callPlayer() {

		for (int myturn : turn) {
			if (pflag[myturn]) {
				gameInput(myturn);
				pflag[myturn] = player[myturn].all();
				if (pflag[myturn] && survived == 1) {
					ranking[survived - 1].setText(String.valueOf(survived) + ". player " + myturn);
					switch (myturn) {
					case (0):
						ranking[survived - 1].setColor4f(0.00f, 0.64f, 0.91f, 1.0f);
						break;
					case (1):
						ranking[survived - 1].setColor4f(1.00f, 0.50f, 0.15f, 1.0f);
						break;
					case (2):
						ranking[survived - 1].setColor4f(0.13f, 0.69f, 0.30f, 1.0f);
						break;
					case (3):
						ranking[survived - 1].setColor4f(1.00f, 0.95f, 0.00f, 1.0f);
						break;
					}
					survived = 0;
				}
				position[myturn] = player[myturn].getPosition();
				if (!pflag[myturn]) {
					if (survived != 0) {
						ranking[survived - 1].setText(String.valueOf(survived) + ". player " + myturn);
						switch (myturn) {
						case (0):
							ranking[survived - 1].setColor4f(0.00f, 0.64f, 0.91f, 1.0f);
							break;
						case (1):
							ranking[survived - 1].setColor4f(1.00f, 0.50f, 0.15f, 1.0f);
							break;
						case (2):
							ranking[survived - 1].setColor4f(0.13f, 0.69f, 0.30f, 1.0f);
							break;
						case (3):
							ranking[survived - 1].setColor4f(1.00f, 0.95f, 0.00f, 1.0f);
							break;
						}
					}
					survived--;
					effect_pdead[myturn].setFlag(position[myturn].x, position[myturn].y);
				}
			} else {
				effect_pdead[myturn].all();
				player[myturn] = null;
			}
			// gameInputMap();
		}
	}

	// メイン
	public scene_state all() {
		change();

		if (currentstate != scene_state.Scene_finish) {

			move();
			draw();

			callPlayer();
		}

		return currentstate;
	}
}
