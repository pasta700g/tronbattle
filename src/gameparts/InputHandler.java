package gameparts;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import mainparts.Define;

public final class InputHandler {
	private static long window;
	private static final int KEYBOARD_SIZE = 512;
	private static final int MOUSE_SIZE = 16;

	private static int[] keyStates = new int[KEYBOARD_SIZE];
	private static boolean[] activeKeys = new boolean[KEYBOARD_SIZE];

	private static int[] mouseButtonStates = new int[MOUSE_SIZE];
	private static boolean[] activeMouseButtons = new boolean[MOUSE_SIZE];
	private static long lastMouseNS = 0;
	private static long mouseDoubleClickPeriodNS = 1000000000 / 5;

	private static int NO_STATE = -1;

	private static float xpos, ypos;

	// キーボードの状態
	private static GLFWKeyCallback keyboard = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			activeKeys[key] = action != GLFW_RELEASE;
			keyStates[key] = action;
		}
	};

	// マウスの状態
	private static GLFWMouseButtonCallback mousebutton = new GLFWMouseButtonCallback() {
		@Override
		public void invoke(long window, int button, int action, int mods) {
			activeMouseButtons[button] = action != GLFW_RELEASE;
			mouseButtonStates[button] = action;
		}
	};

	// カーソルの状態
	private static GLFWCursorPosCallback mousepos = new GLFWCursorPosCallback() {
		// カーソル位置はウィンドウ左上が原点→↓
		@Override
		public void invoke(long window, double x, double y) {
			xpos = (float) x;
			ypos = (float) y;
		}
	};

	// 初期化
	public static void init(long window) {
		InputHandler.window = window;

		glfwSetKeyCallback(window, InputHandler.keyboard);
		glfwSetMouseButtonCallback(window, InputHandler.mousebutton);
		glfwSetCursorPosCallback(window, InputHandler.mousepos);

		resetKeyboard();
		resetMouse();
	}

	// リセット呼び出し
	public static void reset() {
		resetKeyboard();
		resetMouse();
		resetMousePos();
	}

	// リセット呼び出し(カーソル以外)
	public static void update() {
		resetKeyboard();
		resetMouse();
	}

	// キーボード入力状態のリセット
	private static void resetKeyboard() {
		for (int i = 0; i < keyStates.length; i++) {
			keyStates[i] = NO_STATE;
		}
	}

	// マウスの入力状態のリセット
	private static void resetMouse() {
		for (int i = 0; i < mouseButtonStates.length; i++) {
			mouseButtonStates[i] = NO_STATE;
		}

		long now = System.nanoTime();

		if (now - lastMouseNS > mouseDoubleClickPeriodNS)
			lastMouseNS = 0;
	}

	// カーソル位置のリセット
	private static void resetMousePos() {
		xpos = 0f;
		ypos = 0f;
	}

	// キーの状態を渡す
	public static boolean keyDown(int key) {
		return activeKeys[key];
	}

	// キーが押された
	public static boolean keyPressed(int key) {
		return keyStates[key] == GLFW_PRESS;
	}

	// キーが離された
	public static boolean keyReleased(int key) {
		return keyStates[key] == GLFW_RELEASE;
	}

	// マウスのボタンの状態を渡す
	public static boolean mouseButtonDown(int button) {
		return activeMouseButtons[button];
	}

	// マウスのボタンが押された
	public static boolean mouseButtonPressed(int button) {
		return mouseButtonStates[button] == GLFW_PRESS;
	}

	// マウスのボタンが離された
	public static boolean mouseButtonReleased(int button) {
		boolean flag = mouseButtonStates[button] == GLFW_RELEASE;

		if (flag)
			lastMouseNS = System.nanoTime();

		return flag;
	}

	// マウスのボタンがダブルクリックされた
	public static boolean mouseButtonDoubleClicked(int button) {
		long last = lastMouseNS;
		boolean flag = mouseButtonReleased(button);

		long now = System.nanoTime();

		if (flag && now - last < mouseDoubleClickPeriodNS) {
			lastMouseNS = 0;
			return true;
		}

		return false;
	}

	// ウィンドウ上のカーソルの位置を渡す
	public static Define.VEC getCursorPos() {
		return new Define.VEC(xpos - Define.width / 2, -ypos + Define.height / 2);
	}
}
