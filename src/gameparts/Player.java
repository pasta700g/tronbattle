package gameparts;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWKeyCallback;

import mainparts.Define;
import mainparts.Define.PANEL_INDEX;
import mainparts.Define.VEC;
import mainparts.Define.cell_name;
import mainparts.Define.direction;
import mainparts.Define.program_format;
import mainparts.Main;
import mainparts.TextureSet;

public class Player {
	private String player_texturePath;

	float[] player_vertex = { -10f, 10f, 0f, 10f, 10f, 0f, 10f, -10f, 0f, -10f, -10f, 0f };

	float[] player_texcoord = { 0.f, 0.f, 1f, 0.f, 1.f, 1.f, 0.f, 1f };

	int[] player_index = { 0, 1, 2, 3 };

	float x = 0;
	float y = 0;
	float z = 0;

	Define.VEC pos;
	double dx, dy;
	int width, height;
	// プレイヤー画像パターン
	int player_pattern;
	double move;
	int xcount, ycount;
	int ix, iy, result;
	boolean turn;

	int count;
	// 打ち始めからのカウント
	int s_count;
	// サウンドフラグ
	boolean s_shot;

	int life;
	boolean damageflag;
	boolean endflag;
	// ダメージ中のカウント
	int dcount;

	int wait;
	float lastx;
	float lasty;

	// 現在踏んでいるパネル
	Define.PANEL_INDEX current_location;

	Define.PANEL_INDEX next_location;

	int id;

	cell_name mine;

	int Color;

	String[] arguments;

	String ainame;

	String[] cmd = {};

	// We need to strongly reference callback instances.
	private GLFWKeyCallback keyCallback;

	FloatBuffer fb = BufferUtils.createFloatBuffer(16);
	private Matrix4f MatModel;

	public Player(int id, int initialrow, int initialcol, VEC position, String ainame) {
		player_texturePath = Define.dir + "/assets/images/player" + String.valueOf(id) + ".png";

		Main.textures.put("player" + String.valueOf(id),
				new TextureSet(Define.program, Define.window, Define.attrib_vertex, Define.attrib_texcoord,
						player_texturePath, player_vertex, player_texcoord, player_index));

		pos = new Define.VEC();
		current_location = new Define.PANEL_INDEX();
		next_location = new Define.PANEL_INDEX();

		move = 1.0;

		xcount = 0;
		ycount = 0;
		ix = 0;
		iy = 0;
		result = 0;

		x = position.x;
		y = position.y;

		dx = 0;
		dy = 0;

		life = 1;

		turn = false;

		count = 0;
		s_count = 0;
		s_shot = false;
		damageflag = false;
		endflag = false;
		dcount = 0;

		current_location.row = initialrow;
		current_location.column = initialcol;

		player_pattern = 1;

		wait = 2;
		lastx = x;
		lasty = y;

		this.id = id;
		this.ainame = ainame;

		switch (checkExtension(ainame)) {
		case c:
			cmd = new String[3];
			cmd[0] = "cmd";
			cmd[1] = "/c";
			cmd[2] = ainame;
			break;
		case java:
			cmd = new String[4];
			cmd[0] = "cmd";
			cmd[1] = "/c";
			cmd[2] = "java";
			cmd[3] = ainame.substring(0, ainame.length() - 6);
			break;
		}

		switch (id) {
		case (0):
			mine = cell_name.player0;
			break;
		case (1):
			mine = cell_name.player1;
			break;
		case (2):
			mine = cell_name.player2;
			break;
		case (3):
			mine = cell_name.player3;
			break;
		}

		TronDrawing.field[current_location.row][current_location.column].state = mine;
	}

	// 移動可能であるかの判定
	private boolean getLimitflag(Define.PANEL_INDEX current_location, Define.PANEL_INDEX next) {

		// 隣接セルとの交差判定
		if (next.row < 0 || next.row > Define.ROW - 1 || next.column < 0 || next.column > Define.COLUMN - 1) {
			// 場外
			return false;
		} else if (TronDrawing.field[next.row][next.column].state != cell_name.empty) {
			// 移動不可
			return false;
		} else if (current_location.row == next.row && current_location.column == next.column) {
			// 正常な応答なし
			return false;
		} else {
			return true;
		}

	}

	// 移動の処理
	private void moveProcedure(Define.VEC m, Define.PANEL_INDEX next, direction direction) {
		boolean temp;

		temp = getLimitflag(current_location, next);

		if (temp) {
			lastx = x;
			lasty = y;
			x += m.x;
			y += m.y;
			TronDrawing.field[next.row][next.column].parent = direction;
			current_location.row = next.row;
			current_location.column = next.column;
			TronDrawing.field[next.row][next.column].state = mine;
			count = 0;
		} else {// 衝突した時
			setDamageFlag();
			TronDrawing.playerDead(id, mine);
		}
	}

	// 拡張子の確認
	private program_format checkExtension(String ainame) {
		if (ainame == null)
			return program_format.unexpected;
		int point = ainame.lastIndexOf(".");
		if (point != -1) {
			switch (ainame.substring(point + 1)) {
			case "exe":
				return program_format.c;
			case "class":
				return program_format.java;
			}
		}
		return program_format.unexpected;
	}

	// キーボード入力による移動(未使用)
	private void moveByKeyboard() {
		Define.VEC m;
		Define.PANEL_INDEX next = new Define.PANEL_INDEX(-1, -1);

		m = new Define.VEC(0, 0);

		// キーボード入力
		// ------------------------------------------------------------------------------------------------------------------------------------
		if (InputHandler.keyPressed(GLFW_KEY_LEFT)) {
			m.x = -1 * Define.PLAYER_SPEED;
			m.y = 0;
			next.row = current_location.row;
			next.column = current_location.column - 1;

			moveProcedure(m, next, direction.right);
			if (xcount > 0)
				xcount = 0;
			--xcount;
		} else if (InputHandler.keyPressed(GLFW_KEY_RIGHT)) {
			m.x = Define.PLAYER_SPEED;
			m.y = 0;
			next.row = current_location.row;
			next.column = current_location.column + 1;
			moveProcedure(m, next, direction.left);
			if (xcount < 0)
				xcount = 0;
			++xcount;
		} else if (InputHandler.keyPressed(GLFW_KEY_UP)) {
			m.x = 0;
			m.y = 1 * Define.PLAYER_SPEED;
			next.row = current_location.row - 1;
			next.column = current_location.column;
			moveProcedure(m, next, direction.down);
			if (ycount > 0)
				ycount = 0;
			--ycount;
		} else if (InputHandler.keyPressed(GLFW_KEY_DOWN)) {
			m.x = 0;
			m.y = -1 * Define.PLAYER_SPEED;
			next.row = current_location.row + 1;
			next.column = current_location.column;
			moveProcedure(m, next, direction.up);
			if (ycount < 0)
				ycount = 0;
			++ycount;
		}

		// ------------------------------------------------------------------------------------------------------------------------------------

		if (InputHandler.keyDown(GLFW_KEY_LEFT) && InputHandler.keyDown(GLFW_KEY_RIGHT)) {
			xcount = 0;
		}
		if (InputHandler.keyDown(GLFW_KEY_UP) && InputHandler.keyDown(GLFW_KEY_DOWN)) {
			ycount = 0;
		}
	}

	// AIによる移動
	private void moveByAI() {
		int len = cmd.length + arguments.length;
		String[] outputcmd = new String[len];
		System.arraycopy(cmd, 0, outputcmd, 0, cmd.length);
		System.arraycopy(arguments, 0, outputcmd, cmd.length, arguments.length);

		boolean incorrect = true;

		// for (String s : outputcmd) {
		// System.out.println(s + " ");
		// }
		// System.out.println();
		// for (int i = 0; i < 3; i++) {
		// System.out.print(outputcmd[i] + " ");
		// }
		// System.out.println();

		File dir = new File(Define.dir + "/assets/AI");
		ProcessBuilder pb;
		try {
			// サブプロセス生成
			pb = new ProcessBuilder(outputcmd);
			// 作業ディレクトリ設定
			pb.directory(dir);
			// サブプロセス実行
			Process p = pb.start();

			InputStream is = p.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;

			Define.VEC m;
			Define.PANEL_INDEX next = new Define.PANEL_INDEX(-1, -1);

			m = new Define.VEC(0, 0);

			if ((line = br.readLine()) != null) {
				incorrect = false;

				// System.out.println(id + " " + line);

				switch (line) {
				case ("LEFT"):
					m.x = -1 * Define.PLAYER_SPEED;
					m.y = 0;
					next.row = current_location.row;
					next.column = current_location.column - 1;
					moveProcedure(m, next, direction.right);
					Define.writeLog("player " + id + " " + line, true);
					break;
				case ("RIGHT"):
					m.x = Define.PLAYER_SPEED;
					m.y = 0;
					next.row = current_location.row;
					next.column = current_location.column + 1;
					moveProcedure(m, next, direction.left);
					Define.writeLog("player " + id + " " + line, true);
					break;
				case ("UP"):
					m.x = 0;
					m.y = 1 * Define.PLAYER_SPEED;
					next.row = current_location.row - 1;
					next.column = current_location.column;
					moveProcedure(m, next, direction.down);
					Define.writeLog("player " + id + " " + line, true);
					break;
				case ("DOWN"):
					m.x = 0;
					m.y = -1 * Define.PLAYER_SPEED;
					next.row = current_location.row + 1;
					next.column = current_location.column;
					moveProcedure(m, next, direction.up);
					Define.writeLog("player " + id + " " + line, true);
					break;
				default:
					m.x = 0;
					m.y = 0;
					next.row = current_location.row;
					next.column = current_location.column;
					moveProcedure(m, next, direction.right);

					System.out.println("player" + id + " " + "--- incorrect response ---");

					Define.writeLog("", true);
					Define.writeLog("player" + id + " " + "--- incorrect response ---", true);
					Define.writeLog("input :", true);
					for (int i = 2; i < outputcmd.length; i++) {
						Define.writeLog(outputcmd[i] + " ", false);
					}
					Define.writeLog("", true);
					Define.writeLog("output :", true);
					Define.writeLog(line, true);
					Define.writeLog("", true);
					break;
				}
			}

			if (incorrect) {
				m.x = 0;
				m.y = 0;
				next.row = current_location.row;
				next.column = current_location.column;
				moveProcedure(m, next, direction.right);

				System.out.println("player" + id + " " + "--- no response ---");

				Define.writeLog("player" + id + " " + "--- no response ---", true);
				Define.writeLog("input :", true);
				for (int i = 2; i < outputcmd.length; i++) {
					Define.writeLog(outputcmd[i] + " ", false);
				}
				Define.writeLog("", true);
				Define.writeLog("output :", true);
				Define.writeLog(line, true);
				Define.writeLog("", true);
			}

			p.waitFor();

			p.destroy();

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// AIに渡すゲーム情報を作成
	public void createGameInput(int playercount, int[] initialrow, int[] initialcol, boolean[] pflag,
			PANEL_INDEX[] pindex) {

		arguments = new String[2 + playercount * 4 + 20];
		arguments[0] = String.valueOf(playercount);
		arguments[1] = String.valueOf(id);

		for (int i = 0; i < playercount; i++) {
			arguments[2 + i * 4] = String.valueOf(initialrow[i]);
			arguments[2 + i * 4 + 1] = String.valueOf(initialcol[i]);
			if (pflag[i]) {
				arguments[2 + i * 4 + 2] = String.valueOf(pindex[i].row);
				arguments[2 + i * 4 + 3] = String.valueOf(pindex[i].column);
			} else {
				arguments[2 + i * 4 + 2] = String.valueOf(-1);
				arguments[2 + i * 4 + 3] = String.valueOf(-1);
			}
		}

		String arg;

		for (int i = 0; i < Define.ROW; i++) {
			arg = "";
			for (int j = 0; j < Define.COLUMN; j++) {
				arg += String.valueOf(TronDrawing.field[i][j].state.getNum());
			}
			arguments[arguments.length - 20 + i] = arg;
		}
	}

	// 描画
	private void draw() {
		// 通常描画
		// 軌跡
		if (player_pattern == 1) {
			for (int i = 0; i < Define.ROW; i++) {
				for (int j = 0; j < Define.COLUMN; j++) {
					if (count <= wait && (i == current_location.row && j == current_location.column)) {
						glUseProgram(Define.program);
						switch (TronDrawing.field[i][j].parent) {
						case left:
							MatModel = new Matrix4f().translate(lastx + (32.0f / wait) * count, lasty, z);
							break;
						case right:
							MatModel = new Matrix4f().translate(lastx - (32.0f / wait) * count, lasty, z);
							break;
						case up:
							MatModel = new Matrix4f().translate(lastx, lasty - (32.0f / wait) * count, z);
							break;
						case down:
							MatModel = new Matrix4f().translate(lastx, lasty + (32.0f / wait) * count, z);
							break;
						case unknown:
							MatModel = new Matrix4f().translate(lastx, lasty, z);
							break;
						}

						glUniformMatrix4fv(glGetUniformLocation(Define.program, "model"), false, MatModel.get(fb));
						glUseProgram(0);
						Main.textures.get("player" + String.valueOf(id)).draw();
					}

					if (count > wait / 1.5 || !(i == current_location.row && j == current_location.column)) {
						if (TronDrawing.field[i][j].state == mine) {
							glUseProgram(Define.program);
							MatModel = new Matrix4f().translate(TronDrawing.field[i][j].basepoint.x,
									TronDrawing.field[i][j].basepoint.y, z);

							glUniformMatrix4fv(glGetUniformLocation(Define.program, "model"), false, MatModel.get(fb));
							Main.textures.get("player" + String.valueOf(id)).draw();

							glUseProgram(Define.program);
							if (TronDrawing.field[i][j].parent != direction.unknown) {
								switch (TronDrawing.field[i][j].parent) {
								case left:
									MatModel = new Matrix4f().translate(TronDrawing.field[i][j].basepoint.x - 16,
											TronDrawing.field[i][j].basepoint.y, z);
									break;
								case right:
									MatModel = new Matrix4f().translate(TronDrawing.field[i][j].basepoint.x + 16,
											TronDrawing.field[i][j].basepoint.y, z);
									break;
								case up:
									MatModel = new Matrix4f().translate(TronDrawing.field[i][j].basepoint.x,
											TronDrawing.field[i][j].basepoint.y + 16, z);
									break;
								case down:
									MatModel = new Matrix4f().translate(TronDrawing.field[i][j].basepoint.x,
											TronDrawing.field[i][j].basepoint.y - 16, z);
									break;
								}

								glUniformMatrix4fv(glGetUniformLocation(Define.program, "model"), false,
										MatModel.get(fb));
								glUseProgram(0);
								Main.textures.get("player" + String.valueOf(id)).draw();
							}
						}
					}
				}
			}
		}
	}

	// プレイヤの座標を渡す
	public Define.VEC getPosition() {
		return new Define.VEC(x, y);
	}

	// プレイヤが現在踏んでいるパネルを渡す
	public PANEL_INDEX getIndex() {
		return current_location;
	}

	// プレイヤ死亡の処理
	public void setDamageFlag() {
		damageflag = true;
		life = 0;
		System.out.println("< player " + String.valueOf(id) + " dead >");

		Define.writeLog("", true);
		Define.writeLog("< player " + String.valueOf(id) + " dead >", true);
		Define.writeLog("", true);
	}

	// メイン
	public boolean all() {

		if (count > wait) {
			// moveByKeyboard();
			moveByAI();
		}
		draw();

		count++;

		return life != 0;
	}

}
