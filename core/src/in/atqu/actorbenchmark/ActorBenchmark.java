package in.atqu.actorbenchmark;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;

public class ActorBenchmark extends ApplicationAdapter {
	Stage stage;
	ScreenViewport viewport;
	Level level;
	Label fpsLabel;

	@Override
	public void create () {
		fpsLabel = new Label("FPS", new Label.LabelStyle(new BitmapFont(), new Color(0x000000FF)));
		fpsLabel.setFontScale(2);
		viewport = new ScreenViewport();
		stage = new Stage(viewport);
		Gdx.input.setInputProcessor(stage);
		stage.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				Level.LENGTH += 5;
				newGame();
				return true;
			}
		});
		newGame();
	}

	void newGame() {
		if (level != null) {
			level.remove();
		}
		level = new Level(this);
		stage.addActor(level);
		level.rotateBy(90);
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	Color background = new Color(0xDDDDDDFF);

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
		if (Gdx.app.getType() != Application.ApplicationType.Desktop && Gdx.app.getType() != Application.ApplicationType.WebGL)
			viewport.setUnitsPerPixel(1/Gdx.graphics.getDensity());
		level.setPosition(viewport.getWorldWidth()/2, viewport.getWorldHeight()/2);
		fpsLabel.setPosition(15, viewport.getWorldHeight()-35);
		stage.addActor(fpsLabel);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(background.r, background.g, background.b, background.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		fpsLabel.setText(Gdx.graphics.getFramesPerSecond() + " fps, " + level.actorCount + " actors.");
	}
}

class Block extends Image {
	protected Color color;

	public Block(Color color, float width, float height) {
		setSize(width, height);
		this.color = color;
		Sprite sprite = new Sprite(getImage(color));
		sprite.setSize(getWidth(), getHeight());
		setDrawable(new SpriteDrawable(sprite));
	}

	Texture getImage(Color color) {
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(color);
		pixmap.fill();
		Texture texture = new Texture(pixmap);
		pixmap.dispose();
		return texture;
	}
}

class Level extends Group {
	float speed = .1f;
	static int LENGTH = 20;
	int actorCount = 1;
	Group path;
	ActorBenchmark game;

	public Level(ActorBenchmark game) {
		super();
		this.game = game;
		path = new Group();
		addActor(path);
		generateRandomLevel();
		//rotate();
	}

	void rotate() {
		float rotation = getRotation();
		float rotateTo = MathUtils.random(rotation-90, rotation+90);
		addAction(sequence(rotateTo(rotateTo, 2), run(new Runnable() {
			@Override
			public void run() {
				rotate();
			}
		})));
	}

	@Override
	public void act(float delta) {
		if (this.speed > 0) {
			this.path.setX(path.getX() - speed / delta);

			if (-this.path.getX() >= getWidth())
				gameOver();
		}
		super.act(delta);
	}

	private void gameOver() {
		this.speed = 0;
		addAction(sequence(scaleTo(3,3, 2), run(new Runnable() {
			@Override
			public void run() {
				game.newGame();
			}
		})));
	}

	private void generateRandomLevel() {
		for (int i = 0; i < LENGTH; i++) {
			float totalWidth = getWidth();
			Block block = randomBlock(MathUtils.random(2, 7) * 25);
			block.setX(totalWidth);
			path.addActor(block);
			actorCount ++;
			setWidth(getWidth() + block.getWidth());
		}
	}

	Block randomBlock(int width) {
		return new Block(new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1), width, MathUtils.random(30f, 150f));
	}
}