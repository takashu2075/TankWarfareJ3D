package utils;

public class EnemyCounter {
	static int enemyCount = 0;
	
	public EnemyCounter() {
		
	}
	
	public static void increaseEnemyCount() {
		enemyCount++;
	}
	
	public static void decreaseEnemyCount() {
		enemyCount--;
	}
	
	public static int getEnemyCount() {
		return enemyCount;
	}
}
