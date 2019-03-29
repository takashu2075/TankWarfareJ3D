package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.TransformGroup;

import com.sun.j3d.loaders.*;
import com.sun.j3d.loaders.objectfile.ObjectFile;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

import actors.Atmosphere;
import actors.Cannon;
import actors.Sun;
import actors.Tank;
import actors.Terrain;
import actors.Terrain;
import actors.Tower;
import controlers.AiTankController;
import controlers.PlayerTankController;
import makers.ExplosionMaker;
import tools.ObjLoader;

import com.bulletphysics.*;
import com.bulletphysics.dynamics.*;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.linearmath.*;
import com.bulletphysics.util.ObjectArrayList;
import com.bulletphysics.dynamics.vehicle.*;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;

import javax.imageio.ImageIO;
import javax.media.j3d.*;
import javax.vecmath.*;

public class MainFrame extends JFrame {
	static final int SCREEN_WIDTH = 1280; //1280;
	static final int SCREEN_HEIGHT = 720; //720;
	static final String TITLE = "TANK WARFARE";
	
	PlayerTankController playerController;
	
	Game game;
	
	String backgroundImagePath = "metal.jpg";
	BufferedImage backGroundImage = null;
	
	Color labelColor = new Color(0.8f, 0.8f, 0.8f);
	
	public MainFrame() {
		try {
//            this.backGroundImage = ImageIO.read(new File(backgroundImagePath));
			this.backGroundImage = ImageIO.read(getClass().getClassLoader().getResource(backgroundImagePath));
        } catch (IOException ex) {
            ex.printStackTrace();
            this.backGroundImage = null;
        }
		
		TitleScreen titleScreen = new TitleScreen(this);
		titleScreen.setVisible(true);
		this.add(titleScreen);
	}
	
	public static void main(String[] args) {
		MainFrame mainFrame = new MainFrame();
		
		mainFrame.setSize(SCREEN_WIDTH + 16, SCREEN_HEIGHT + 39);
		mainFrame.setTitle(TITLE);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setLocation(300, 150);
		
		mainFrame.setVisible(true);
	}
	
    public void showTitleScreen(JPanel currentPanel){
    	currentPanel.setVisible(false);
    	this.remove(currentPanel);
    	TitleScreen titleScreen = new TitleScreen(this);
    	titleScreen.setVisible(true);
    	this.add(titleScreen);
    }
    
    public void transferToStageScreen(JPanel currentPanel){
    	currentPanel.setVisible(false);
    	this.remove(currentPanel);
    	StageScreen stageScreen = new StageScreen(this);
        stageScreen.setVisible(true);
        this.add(stageScreen);
    }
    
    public void transferToLoadScreen(JPanel currentPanel){
    	currentPanel.setVisible(false);
    	this.remove(currentPanel);
    	final LoadScreen loadScreen = new LoadScreen(this);
    	loadScreen.setVisible(true);
        this.add(loadScreen);
        
        long LOADING_TIME = 1000;
        
        TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				transferToGameScreen(loadScreen);
			}
		};
        
        Timer timer = new Timer();
        timer.schedule(timerTask, LOADING_TIME);
    }
    
    public void transferToGameScreen(JPanel currentPanel){
    	currentPanel.setVisible(false);
    	this.remove(currentPanel);
    	GameScreen gameScreen = new GameScreen(this, true);
        gameScreen.setVisible(true);
        this.add(gameScreen);
    }
    
    public void transferToResultScreen(JPanel currentPanel){
    	currentPanel.setVisible(false);
    	this.remove(currentPanel);
    	ResultScreen resultScreen = new ResultScreen(this);
    	resultScreen.setVisible(true);
    	this.getContentPane().add(resultScreen);
    }
    
    public BufferedImage getBackgroundImage() {
    	return this.backGroundImage;
    }
    
    public Color getLabelColor() {
    	return this.labelColor;
    }
	
	public class TitleScreen extends JPanel {
		MainFrame mainFrame;
		Color labelColor;
		
		public TitleScreen(final MainFrame mainFrame) {
			this.mainFrame = mainFrame;
			this.labelColor = mainFrame.getLabelColor();
			
			setLayout(null);
			
			JLabel titleText = new JLabel();
			titleText.setText(TITLE);
			titleText.setHorizontalAlignment(JLabel.CENTER);
			titleText.setVerticalAlignment(JLabel.CENTER);
			titleText.setForeground(labelColor);
			titleText.setFont(new Font("Serif", Font.PLAIN, 100));
			
			add(titleText);
			titleText.setBounds((SCREEN_WIDTH / 2) - 400, ((SCREEN_HEIGHT / 11) * 5) - 50, 800, 100);
			
			Color startButtonColor = new Color(0.2f, 0.2f, 0.2f);
			
			JButton startButton = new JButton();
			startButton.setText("Start");
			startButton.setFont(new Font("Serif", Font.PLAIN, 30));
			startButton.setHorizontalAlignment(JLabel.CENTER);
			startButton.setForeground(labelColor);
			startButton.setBackground(startButtonColor);
			titleText.setVerticalAlignment(JLabel.CENTER);
			startButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					panelChangeToStage();
				}
			});
			
			add(startButton);
			startButton.setBounds((SCREEN_WIDTH / 2) - 75, ((SCREEN_HEIGHT / 4) * 3) - 25, 150, 50);
		}
		
		@Override
	    public void paintComponent(Graphics g) {
	        Graphics2D g2D = (Graphics2D) g;

	        BufferedImage backgroundImage = mainFrame.getBackgroundImage();
	        
	        double imageWidth = backgroundImage.getWidth();
	        double imageHeight = backgroundImage.getHeight();
	        double panelWidth = this.getWidth();
	        double panelHeight = this.getHeight();

	        // 画像がコンポーネントの何倍の大きさか計算
	        double sx = (panelWidth / imageWidth);
	        double sy = (panelHeight / imageHeight);

	        // スケーリング
	        AffineTransform af = AffineTransform.getScaleInstance(sx, sy);
	        g2D.drawImage(backgroundImage, af, this);
	    }
		
	    public void panelChangeToStage(){
	        mainFrame.transferToStageScreen(this);
	    }
	}
	
	public class StageScreen extends JPanel {
		MainFrame mainFrame;
		Color labelColor;
		
		public StageScreen(final MainFrame mainFrame) {
			this.mainFrame = mainFrame;
			this.labelColor = mainFrame.getLabelColor();
			
			setLayout(null);
			
			JLabel label = new JLabel();
			label.setText("Select a stage");
			label.setHorizontalAlignment(JLabel.CENTER);
			label.setVerticalAlignment(JLabel.CENTER);
			label.setFont(new Font("Serif", Font.PLAIN, 70));
			label.setBounds((SCREEN_WIDTH / 2) - 250, (SCREEN_HEIGHT / 6) - 50, 500, 100);
			label.setForeground(labelColor);
			add(label);
			
			Image townImage = null;
			try {
				townImage = ImageIO.read(getClass().getClassLoader().getResource("town.jpg"));
			} catch (IOException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}
			
			ImageIcon townIcon = new ImageIcon(townImage);
			
			int stageButtonSize = 300;
			int halfStageButtonSize = stageButtonSize / 2;
			
			JButton town = new JButton();
//			town.setText("Town");
			town.setFont(new Font("Serif", Font.PLAIN, 30));
			town.setHorizontalAlignment(JLabel.CENTER);
			town.setIcon(townIcon);
			town.addActionListener(new ActionListener() {
				@Override
	            public void actionPerformed(ActionEvent e){
					panelChangeToGame();
	            }
			});
			town.setBounds((SCREEN_WIDTH / 10 * 2) - halfStageButtonSize, 
					((SCREEN_HEIGHT / 5) * 3) - halfStageButtonSize, stageButtonSize, stageButtonSize);
			
			Color comingSoonColor = new Color(0.2f, 0.2f, 0.2f);
			Color comingSoonLabelColor = new Color(0.1f, 0.1f, 0.1f);
			
			JButton desert = new JButton();
			desert.setText("Coming soon...");
			desert.setHorizontalAlignment(JLabel.CENTER);
			desert.setBackground(comingSoonColor);
			desert.setForeground(comingSoonLabelColor);
			desert.setFont(new Font("Serif", Font.PLAIN, 30));
			desert.setBounds(((SCREEN_WIDTH / 10) * 5) - halfStageButtonSize, ((SCREEN_HEIGHT / 5) * 3) - halfStageButtonSize, stageButtonSize, stageButtonSize);
			
			JButton forest = new JButton();
			forest.setText("Coming soon...");
			forest.setHorizontalAlignment(JLabel.CENTER);
			forest.setBackground(comingSoonColor);
			forest.setForeground(comingSoonLabelColor);
			forest.setFont(new Font("Serif", Font.PLAIN, 30));
			forest.setBounds(((SCREEN_WIDTH / 10) * 8) - halfStageButtonSize, ((SCREEN_HEIGHT / 5) * 3) - halfStageButtonSize, stageButtonSize, stageButtonSize);
			
			add(town);
			add(desert);
			add(forest);
			
			JLabel townLabel = new JLabel();
			townLabel.setText("Town");
			townLabel.setHorizontalAlignment(JLabel.CENTER);
			townLabel.setVerticalAlignment(JLabel.CENTER);
			townLabel.setFont(new Font("Serif", Font.PLAIN, 50));
			townLabel.setBounds(((SCREEN_WIDTH / 10) * 2) - 150, ((SCREEN_HEIGHT / 6) * 2) - 50, 300, 100);
			townLabel.setForeground(labelColor);
			add(townLabel);
			
			JLabel desertLabel = new JLabel();
			desertLabel.setText("Desert");
			desertLabel.setHorizontalAlignment(JLabel.CENTER);
			desertLabel.setVerticalAlignment(JLabel.CENTER);
			desertLabel.setFont(new Font("Serif", Font.PLAIN, 50));
			desertLabel.setBounds(((SCREEN_WIDTH / 10) * 5) - 150, ((SCREEN_HEIGHT / 6) * 2) - 50, 300, 100);
			desertLabel.setForeground(labelColor);
			add(desertLabel);
			
			JLabel forestLabel = new JLabel();
			forestLabel.setText("Forest");
			forestLabel.setHorizontalAlignment(JLabel.CENTER);
			forestLabel.setVerticalAlignment(JLabel.CENTER);
			forestLabel.setFont(new Font("Serif", Font.PLAIN, 50));
			forestLabel.setBounds(((SCREEN_WIDTH / 10) * 8) - 150, ((SCREEN_HEIGHT / 6) * 2) - 50, 300, 100);
			forestLabel.setForeground(labelColor);
			add(forestLabel);
		}
		
		@Override
	    public void paintComponent(Graphics g) {
	        Graphics2D g2D = (Graphics2D) g;

	        BufferedImage backgroundImage = mainFrame.getBackgroundImage();
	        
	        double imageWidth = backgroundImage.getWidth();
	        double imageHeight = backgroundImage.getHeight();
	        double panelWidth = this.getWidth();
	        double panelHeight = this.getHeight();

	        // 画像がコンポーネントの何倍の大きさか計算
	        double sx = (panelWidth / imageWidth);
	        double sy = (panelHeight / imageHeight);

	        // スケーリング
	        AffineTransform af = AffineTransform.getScaleInstance(sx, sy);
	        g2D.drawImage(backgroundImage, af, this);
	    }
		
	    public void panelChangeToGame() {
	        mainFrame.transferToGameScreen(this);
	    }
	}
	
	public class LoadScreen extends JPanel {
		MainFrame mainFrame;
		Color labelColor;
		
		public LoadScreen(final MainFrame mainFrame) {
			this.mainFrame = mainFrame;
			this.labelColor = mainFrame.getLabelColor();
			
			setLayout(null);
			
			JLabel label = new JLabel();
			label.setText("Now loading...");
			label.setHorizontalAlignment(JLabel.CENTER);
			label.setVerticalAlignment(JLabel.CENTER);
			label.setFont(new Font("メイリオ", Font.PLAIN, 30));
			label.setBounds((SCREEN_WIDTH / 2) - 150, (SCREEN_HEIGHT / 2) - 50, 300, 50);
			add(label);
		}
		
		@Override
	    public void paintComponent(Graphics g) {
	        Graphics2D g2D = (Graphics2D) g;

	        BufferedImage backgroundImage = mainFrame.getBackgroundImage();
	        
	        double imageWidth = backgroundImage.getWidth();
	        double imageHeight = backgroundImage.getHeight();
	        double panelWidth = this.getWidth();
	        double panelHeight = this.getHeight();

	        // 画像がコンポーネントの何倍の大きさか計算
	        double sx = (panelWidth / imageWidth);
	        double sy = (panelHeight / imageHeight);

	        // スケーリング
	        AffineTransform af = AffineTransform.getScaleInstance(sx, sy);
	        g2D.drawImage(backgroundImage, af, this);
	    }
	}
	
	public class GameScreen extends JPanel {
		MainFrame mainFrame;
		Canvas3D canvas3D;
		
		Color labelColor;
		
		JLabel enemyCounter;
		JLabel damageMeter;
		int totalEnemyCount = 0;
		int currentEnemyCount = 0;
		
		public GameScreen(MainFrame mainFrame, boolean gameFlg) {
			this.mainFrame = mainFrame;
			this.labelColor = mainFrame.getLabelColor();
			
			setLayout(null);
			
			this.setBounds(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
			
			initCursor();
			initEnemyCounter();
			initDamageMeter();
			
			if (gameFlg) {
				transferToGame();
			}
		}
		
		public void transferToGame() {
			GraphicsConfiguration gConfig = SimpleUniverse.getPreferredConfiguration();
			canvas3D = new Canvas3D(gConfig);
			canvas3D.setBounds(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
			canvas3D.setBackground(new Color(0, 0, 0, 0));
			add(canvas3D);
			
			canvas3D.addKeyListener(playerController);
			
			game = new Game(this, canvas3D);
			
			canvas3D.getView().setFieldOfView(90);
			canvas3D.getView().setBackClipDistance(1000);
			
			Timer timer = new Timer();
			timer.schedule(new Run(), 0, 16);
		}
		
		public void complete() {
			JLabel label = new JLabel();
			label.setText("MISSION ACCOMPLISHED!");
			label.setHorizontalAlignment(JLabel.CENTER);
			label.setVerticalAlignment(JLabel.CENTER);
			label.setFont(new Font("Serif", Font.PLAIN, 30));
			label.setBounds(((SCREEN_WIDTH / 2) - 250), (SCREEN_HEIGHT / 4) * 3 - 50, 500, 100);
			
			add(label, 0);
		}
		
		public void gameOver() {
			JLabel label = new JLabel();
			label.setText("GAME OVER");
			label.setHorizontalAlignment(JLabel.CENTER);
			label.setVerticalAlignment(JLabel.CENTER);
			label.setFont(new Font("Serif", Font.PLAIN, 50));
			label.setBounds(((SCREEN_WIDTH / 2) - 250), (SCREEN_HEIGHT / 4) * 3 - 50, 500, 100);
			
			add(label, 0);
		}
		
		public void panelChangeToResult(String toPanelName){
	        mainFrame.transferToResultScreen(this);
	    }
		
		private void initCursor() {
			JLabel cursorUp = new JLabel();
			cursorUp.setBounds((SCREEN_WIDTH / 2) - 1, (SCREEN_HEIGHT / 2) + 12, 2, 10);
			cursorUp.setBackground(Color.GREEN);
			add(cursorUp, 0);
			
			JLabel cursorDown = new JLabel();
			cursorDown.setBounds((SCREEN_WIDTH / 2) - 1, (SCREEN_HEIGHT / 2) + 34, 2, 10);
			cursorDown.setBackground(Color.GREEN);
			add(cursorDown, 0);
			
			JLabel cursorRight = new JLabel();
			cursorRight.setBounds((SCREEN_WIDTH / 2) - 16, (SCREEN_HEIGHT / 2) + 27, 10, 2);
			cursorRight.setBackground(Color.GREEN);
			add(cursorRight, 0);
			
			JLabel cursorLeft = new JLabel();
			cursorLeft.setBounds((SCREEN_WIDTH / 2) + 6, (SCREEN_HEIGHT / 2) + 27, 10, 2);
			cursorLeft.setBackground(Color.GREEN);
			add(cursorLeft, 0);
		}
		
		private void initEnemyCounter() {
			enemyCounter = new JLabel();
			enemyCounter.setText("Enemies: ");
			enemyCounter.setHorizontalAlignment(JLabel.CENTER);
			enemyCounter.setVerticalAlignment(JLabel.CENTER);
			enemyCounter.setFont(new Font("Serif", Font.PLAIN, 30));
			enemyCounter.setBounds(0, 0, 200, 50);
			add(enemyCounter, 0);
		}
		
		private void initDamageMeter() {
			damageMeter = new JLabel();
			damageMeter.setText("HP: %");
			damageMeter.setHorizontalAlignment(JLabel.CENTER);
			damageMeter.setVerticalAlignment(JLabel.CENTER);
			damageMeter.setFont(new Font("Serif", Font.PLAIN, 30));
			damageMeter.setBounds(SCREEN_WIDTH - 200, 0, 200, 50);
			add(damageMeter, 0);
		}
		
		public void updEnemyCounter(int enemyCount, int totalEnemyCount) {
			enemyCounter.setText("Enemies: " + enemyCount + "/" + totalEnemyCount);
		}
		
		public void updDamageMeter(int life) {
			damageMeter.setText("HP: " + life + "%");
		}
	}
	
	public class ResultScreen extends JPanel {
		MainFrame mainFrame;
		Color labelColor;
		
		public ResultScreen(MainFrame mainFrame) {
			this.mainFrame = mainFrame;
			this.labelColor = mainFrame.getLabelColor();
			
			setLayout(null);
			
			JLabel label = new JLabel();
			label.setText("Result");
			label.setHorizontalAlignment(JLabel.CENTER);
			label.setVerticalAlignment(JLabel.CENTER);
			label.setFont(new Font("メイリオ", Font.PLAIN, 30));
			label.setBounds((SCREEN_WIDTH / 2) - 150, (SCREEN_HEIGHT / 9), 300, 50);
			add(label, 0);
			
			JButton returnButton = new JButton();
			returnButton.setText("Return to the title");
			returnButton.setHorizontalAlignment(JLabel.CENTER);
			returnButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					panelChangeToTitle();
				}
			});
			
			add(returnButton, 0);
			returnButton.setBounds((SCREEN_WIDTH / 2) - 100, ((SCREEN_HEIGHT / 4) * 3) - 15, 200, 30);
		}
		
		@Override
	    public void paintComponent(Graphics g) {
	        Graphics2D g2D = (Graphics2D) g;

	        BufferedImage backgroundImage = mainFrame.getBackgroundImage();
	        
	        double imageWidth = backgroundImage.getWidth();
	        double imageHeight = backgroundImage.getHeight();
	        double panelWidth = this.getWidth();
	        double panelHeight = this.getHeight();

	        // 画像がコンポーネントの何倍の大きさか計算
	        double sx = (panelWidth / imageWidth);
	        double sy = (panelHeight / imageHeight);

	        // スケーリング
	        AffineTransform af = AffineTransform.getScaleInstance(sx, sy);
	        g2D.drawImage(backgroundImage, af, this);
	    }
		
	    public void panelChangeToTitle(){
	        mainFrame.showTitleScreen(this);
	    }
	}
	
	class Run extends TimerTask {
		public void run() {
			if (game != null) {
				game.step();
			}
		}
	}
}
