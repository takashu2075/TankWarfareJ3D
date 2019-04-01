package main;

import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.Canvas3D;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.universe.ViewingPlatform;

import actors.Atmosphere;
import actors.Sun;
import actors.Tank;
import actors.Terrain;
import controlers.AiTankController;
import controlers.PlayerTankController;
import main.Frame.GameScreen;
import makers.ExplosionMaker;
import makers.TankMaker;
import tools.TreePlanter;
import utils.EnemyCounter;

public class Game {
	World world;

	PlayerTankController playerController;

	Tank playerTank;

	List<AiTankController> aiTankControllers = new ArrayList<AiTankController>();

	ExplosionMaker explosionMaker;

	List<Vector3f> aiTankSpawnPositions = new ArrayList<Vector3f>();

	List<Vector3f[]> aiWayPoints = new ArrayList<Vector3f[]>();

	int totalEnemyCount = 0;
	int remainingEnemy = 0;

	GameScreen gameScreen;

	TankMaker tankMaker;

	boolean isAlive = false;
	boolean isGameEnded = false;

	int completeCount = 0;

	static List<Tank> tanks = new ArrayList<Tank>();

	public Game(GameScreen gameScreen, Canvas3D canvas3D) {
		this.gameScreen = gameScreen;
		world = new World(canvas3D);

		Terrain terrain = new Terrain();
		Sun sun = new Sun();
		Atmosphere atmosphere = new Atmosphere();

		Vector3f playerSpawnPosition = new Vector3f(0, 10, -210);
		int playerIff = 1;

		tankMaker = new TankMaker();

		playerTank = TankMaker.instantiate(playerSpawnPosition);
		playerTank.setIff(playerIff);
		tanks.add(playerTank);

		World.add(terrain);
		World.add(sun);
		World.add(atmosphere);
		World.add(playerTank);

		initTankSpawnPositions();
		initTankWayPoints();
		initAiTanks();

		ViewingPlatform viewingPlatform = World.getSimpleUniverse().getViewingPlatform();

		playerController = new PlayerTankController(playerTank, viewingPlatform.getViewPlatformTransform());

		world.setCamera(viewingPlatform);

		world.initMakers();

		canvas3D.addKeyListener(playerController);

		List<Vector3f> treePositions = new ArrayList<Vector3f>();
		treePositions.add(new Vector3f(-10, 0, 0));

		TreePlanter.plantTrees(terrain.getActorComps().get(0).getRigidBody(), treePositions);

		isAlive = true;
	}

	public void step() {
		if (isAlive) {
			world.step();

			if (World.getDynamicsWorld() != null) {
				for (AiTankController aiTankController : aiTankControllers) {
					aiTankController.step();
				}
			}

			gameScreen.updEnemyCounter(countEnemies(playerTank.getIff()), totalEnemyCount);
			gameScreen.updDamageMeter(playerTank.getLife());

			if (countEnemies(playerTank.getIff()) <= 0 && !isGameEnded) {
				gameScreen.complete();
				isGameEnded = true;
			}

			if (playerTank.isDestroyed() && !isGameEnded) {
				gameScreen.gameOver();
				isGameEnded = true;
			}

			playerController.setViewAngle();
			playerController.step();
		}
	}

	private void initTankSpawnPositions() {
		aiTankSpawnPositions.add(new Vector3f(-190, 10, -210));
		aiTankSpawnPositions.add(new Vector3f(0, 10, -70));
		aiTankSpawnPositions.add(new Vector3f(-190, 10, 210));
		aiTankSpawnPositions.add(new Vector3f(190, 10, -70));
		aiTankSpawnPositions.add(new Vector3f(0, 10, 70));
		aiTankSpawnPositions.add(new Vector3f(190, 10, 70));
	}

	private void initTankWayPoints() {
		Vector3f[] wayPoints1 = new Vector3f[4];
		wayPoints1[0] = new Vector3f(-190, 0, -210);
		wayPoints1[1] = new Vector3f(-190, 0, -70);
		wayPoints1[2] = new Vector3f(0, 0, -70);
		wayPoints1[3] = new Vector3f(0, 0, -210);
		aiWayPoints.add(wayPoints1);

		Vector3f[] wayPoints2 = new Vector3f[4];
		wayPoints2[0] = new Vector3f(0, 0, -70);
		wayPoints2[1] = new Vector3f(0, 0, 70);
		wayPoints2[2] = new Vector3f(-190, 0, 70);
		wayPoints2[3] = new Vector3f(-190, 0, -70);
		aiWayPoints.add(wayPoints2);

		Vector3f[] wayPoints3 = new Vector3f[4];
		wayPoints3[0] = new Vector3f(-190, 0, 210);
		wayPoints3[1] = new Vector3f(0, 0, 210);
		wayPoints3[2] = new Vector3f(0, 0, 70);
		wayPoints3[3] = new Vector3f(-190, 0, 70);
		aiWayPoints.add(wayPoints3);

		Vector3f[] wayPoints4 = new Vector3f[4];
		wayPoints4[0] = new Vector3f(190, 0, -70);
		wayPoints4[1] = new Vector3f(0, 0, -70);
		wayPoints4[2] = new Vector3f(0, 0, -210);
		wayPoints4[3] = new Vector3f(190, 0, -210);
		aiWayPoints.add(wayPoints4);

		Vector3f[] wayPoints5 = new Vector3f[4];
		wayPoints5[0] = new Vector3f(0, 0, 70);
		wayPoints5[1] = new Vector3f(190, 0, 70);
		wayPoints5[2] = new Vector3f(190, 0, -70);
		wayPoints5[3] = new Vector3f(0, 0, -70);
		aiWayPoints.add(wayPoints5);

		Vector3f[] wayPoints6 = new Vector3f[4];
		wayPoints6[0] = new Vector3f(190, 0, 70);
		wayPoints6[1] = new Vector3f(190, 0, 210);
		wayPoints6[2] = new Vector3f(0, 0, 210);
		wayPoints6[3] = new Vector3f(0, 0, 70);
		aiWayPoints.add(wayPoints6);
	}

	private void initAiTanks() {
		int enemyCount = 0;
		int aiTankDragFactor = 60;
		int enemyIff = 2;

		for (Vector3f tankSpawnPosition : aiTankSpawnPositions) {
			Tank aiTank = TankMaker.instantiate(tankSpawnPosition);
			World.add(aiTank);
			AiTankController aiTankController = new AiTankController(aiTank, aiWayPoints.get(enemyCount));
			aiTankControllers.add(aiTankController);
			EnemyCounter.increaseEnemyCount();

			aiTank.setIff(enemyIff);

			addTank(aiTank);

			aiTank.setDrag(aiTankDragFactor);

			enemyCount++;
		}

		this.totalEnemyCount = enemyCount;
	}

	public int getEnemyCount() {
		return EnemyCounter.getEnemyCount();
	}

	public static List<Tank> getTanks() {
		return tanks;
	}

	public void addTank(Tank tank) {
		tanks.add(tank);
	}

	public int countEnemies(int iff) {
		int enemyCount = 0;
		for (Tank tank : tanks) {
			if (tank.getIff() != iff && !tank.isDestroyed()) {
				enemyCount++;
			}
		}
		return enemyCount;
	}
}
