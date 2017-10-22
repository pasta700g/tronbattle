package mainparts;

import gameparts.Config;
import gameparts.InputHandler;
import gameparts.Menu;
import gameparts.TronDrawing;
import mainparts.Define.scene_name;
import mainparts.Define.scene_state;

public class SceneManager {

	scene_name currentscene;
	scene_name nextscene;
	scene_name pastscene;

	scene_state currentstate;

	Menu menu;
	Config config;
	TronDrawing tron;

	SceneManager() {
		nextscene = scene_name.Scene_Menu;
		currentstate = scene_state.Scene_running;
	}

	void exec() {
		pastscene = currentscene;
		currentscene = nextscene;

		switch (currentscene) {
		case Scene_Menu:
			switch (currentstate) {
			case Scene_running:
				if (menu == null) {
					System.out.println("scene MENU");
					menu = new Menu();
					currentstate = menu.all();
				} else {
					currentstate = menu.all();
				}
				break;
			case Scene_finish:
				nextscene = menu.getNextScece();
				menu = null;

				InputHandler.reset();
				currentstate = scene_state.Scene_running;
				break;
			}
			break;

		case Scene_2pGame:
			switch (currentstate) {
			case Scene_running:
				if (tron == null) {
					Define.getDate();

					System.out.println("scene GAME(2 player)");
					tron = new TronDrawing(2);
					currentstate = tron.all();
				} else {
					currentstate = tron.all();
				}
				break;
			case Scene_finish:
				nextscene = tron.getNextScece();
				tron = null;

				InputHandler.reset();
				currentstate = scene_state.Scene_running;
				break;
			}
			break;
		case Scene_4pGame:
			switch (currentstate) {
			case Scene_running:
				if (tron == null) {
					Define.getDate();
					System.out.println("scene GAME(4 player)");
					tron = new TronDrawing(4);
					currentstate = tron.all();
				} else {
					currentstate = tron.all();
				}
				break;
			case Scene_finish:
				nextscene = tron.getNextScece();
				tron = null;

				InputHandler.reset();
				currentstate = scene_state.Scene_running;
				break;
			}
			break;
		case Scene_Config:
			switch (currentstate) {
			case Scene_running:
				if (config == null) {
					System.out.println("scene CONFIG");
					config = new Config();
					currentstate = config.all();
				} else {
					currentstate = config.all();
				}
				break;
			case Scene_finish:
				nextscene = config.getNextScece();
				config = null;

				InputHandler.reset();
				currentstate = scene_state.Scene_running;
				break;
			}
			break;

		}

	}

}
