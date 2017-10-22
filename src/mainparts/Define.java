package mainparts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

public class Define {
	// 新規追加分
	// ---------------------------------------------------------------------------
	// The window handle
	public static long window;

	public static int program = 0;

	public static int attrib_vertex, attrib_texcoord;

	// ---------------------------------------------------------------------------

	// ウィンドウサイズ
	public static final int width = 1200;
	public static final int height = 768;
	public static final int wLeft_x = 0;
	public static final int wRight_x = 1200;
	public static final int wUp_y = 0;
	public static final int wDown_y = 768;

	public static final float PI = 3.14f;

	public static final float PLAYER_SPEED = 32;

	public static final float SCROLL_SPEED = 1;

	public static final int ROW = 20;
	public static final int COLUMN = 30;

	public static int g_count;

	public static int langtype[] = { 0, 0, 0, 0 };

	public static final String dir = System.getProperty("user.dir");

	public static final File file = new File(Define.dir + "/assets/AI");
	public static final File files[] = file.listFiles();

	public enum program_format {
		java, c, unexpected,
	}

	public static String exectime;

	public static void getDate() {
		Date d = new Date();

		SimpleDateFormat d1 = new SimpleDateFormat("yyyyMMdd_HHmmss");
		exectime = d1.format(d);
		// System.out.println(exectime);
	}

	public static void writeLog(String log, boolean newline) {
		File file = new File(Define.dir + "/assets/log/" + exectime + ".txt");

		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));

			if (newline)
				pw.println(log);
			else
				pw.print(log);

			pw.close();
		} catch (IOException e) {
			System.out.println("gameInput > ログの書き込みに失敗");
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "ログの書き込みに失敗しました", "Define",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	public static String convertInputStreamToString(InputStream is) throws IOException {
		InputStreamReader reader = new InputStreamReader(is);
		StringBuilder builder = new StringBuilder();
		char[] buf = new char[1024];
		int numRead;
		while (0 <= (numRead = reader.read(buf))) {
			builder.append(buf, 0, numRead);
		}
		return builder.toString();
	}

	public enum scene_name {
		Scene_Menu, // メニュー画面
		Scene_2pGame, // ゲーム画面
		Scene_4pGame, // ゲーム画面
		Scene_Config, // 設定画面

		Scene_None, // 無し
	}

	public enum scene_state {
		Scene_finish, Scene_running, Scene_test,
	}

	public enum cell_name {
		player0(0), player1(1), player2(2), player3(3), empty(9);

		private final int num;

		private cell_name(int n) {
			this.num = n;
		}

		public int getNum() {
			return this.num;
		}
	}

	public enum direction {
		left, right, up, down, unknown
	}

	public static class PANEL_INDEX {
		public PANEL_INDEX() {
		}

		public PANEL_INDEX(int row, int column) {
			this.row = row;
			this.column = column;
		}

		public int row;
		public int column;
	}

	public static class VEC {
		public VEC() {
		}

		public VEC(float x, float y) {
			this.x = x;
			this.y = y;
		}

		public float x;
		public float y;
	}

	// パネル情報
	public static class Cell {
		public Cell() {
			basepoint = new VEC();
		}

		// パネルの状態
		public cell_name state;

		public direction parent;
		public direction next;
		public VEC basepoint;
	}
}
