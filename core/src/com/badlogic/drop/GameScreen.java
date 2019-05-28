package com.badlogic.drop;

import java.util.Iterator;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class GameScreen implements Screen {
	final Drop game;
	
	Texture dropImage;
	Texture bucketImage;
	Sound dropSound;
	Music rainMusic;
	OrthographicCamera camera;
	SpriteBatch batch;
	Rectangle bucket;
	Vector3 touchPos;
	Array<DropSprite> raindrops;
	long lastDropTime;
	int dropsGathered;
	int dropsMissed;
	ArrayList<Texture> availableDropSprites;
	public GameScreen(final Drop passed_game) {
		game = passed_game; 
		availableDropSprites = new ArrayList<Texture>();
		// Load images, 64px each
		availableDropSprites.add(new Texture(Gdx.files.internal("droplet.png")));
		availableDropSprites.add(new Texture(Gdx.files.internal("dropletFire.png")));
		availableDropSprites.add(new Texture(Gdx.files.internal("ice.png")));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));
		
		// Load the drop sfx and the rain background music
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
		
		// Start playback of music in bg
		rainMusic.setLooping(true);
		rainMusic.play();
		
		// Init the camera objects.
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		touchPos = new Vector3();
		
		batch = new SpriteBatch();
		
		bucket = new Rectangle();
		// Screen Width - Image Width 
		bucket.width = 64;
		bucket.x = 800 / 2 - bucket.width / 2;
		bucket.y = 20;
		bucket.height = 64;
		
		// Create Raindrops and spawn the first one.
		raindrops = new Array<DropSprite>();
		spawnRaindrop();
	}

	@Override
	public void render(float delta) {
		/* Clear screen with a dark blue color.
		 * Arguments to ClearColor are r g b, alpha
		 */
		Gdx.gl.glClearColor(0, 0, .2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		
		game.batch.setProjectionMatrix(camera.combined);
		game.batch.begin();
		game.font.draw(game.batch, "Drops Collected: " + dropsGathered, 0, 400);
		game.font.draw(game.batch, "Drops Missed: " + dropsMissed, 0, 300);
		// Draw the bucket and all the drops.
		game.batch.draw(bucketImage, bucket.x, bucket.y);
		for (DropSprite raindrop: raindrops) {
			game.batch.draw(availableDropSprites.get(raindrop.spriteIdx), raindrop.rectangle.x, raindrop.rectangle.y);
		}
		game.batch.end();
		
		// Process any user input
		if (Gdx.input.isTouched()) {
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - bucket.width / 2;
		}
		
		// Ensure that the bucket's within the screen bounds
		if (Gdx.input.isKeyPressed(Keys.LEFT)) 
			bucket.x -= 200 * Gdx.graphics.getDeltaTime();
		if (Gdx.input.isKeyPressed(Keys.RIGHT)) 
			bucket.x += 200 * Gdx.graphics.getDeltaTime();
		if (bucket.x < 0) 
			bucket.x = 0;
		if (bucket.x > 800 - bucket.width) 
			bucket.x = 800 - bucket.width;
		
		// Check time since last raindrop. Do we need another?
		if (TimeUtils.nanoTime() - lastDropTime > 1000000000) 
			spawnRaindrop();
		
		// Update all the raindrops
		Iterator<DropSprite> iter = raindrops.iterator();
		while (iter.hasNext()) {
			DropSprite raindrop = iter.next();
			raindrop.rectangle.y -= 200 * Gdx.graphics.getDeltaTime();
			if (raindrop.rectangle.y + raindrop.rectangle.height < 0) {
				iter.remove();
				dropsMissed++;
			}
			if (raindrop.rectangle.overlaps(bucket)) {
				dropsGathered++;
				dropSound.play();
				iter.remove();
			}
		}
	}
	
	private void spawnRaindrop() {
		DropSprite raindrop = new DropSprite();
		raindrop.spriteIdx = MathUtils.random(0,availableDropSprites.size() - 1);
		raindrop.rectangle.x = MathUtils.random(0, 800-64);
		raindrop.rectangle.y = 480;
		raindrop.rectangle.width = 64;
		raindrop.rectangle.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}
	
	@Override
	public void dispose() {
		// Clear all the "native" resources
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show() {
		rainMusic.play();
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}
}
