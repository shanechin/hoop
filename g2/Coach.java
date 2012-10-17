package hoop.g2;

import java.util.Random;

import hoop.sim.Game;
import hoop.sim.Game.Round;

public abstract class Coach {

	//for iterative stats
	protected int[][] shotsAgainstTable;
	protected int[][] passesAgainstTable;

	Random gen;

	public Coach(){
		gen = new Random();
	}

	public static final int PLAYERS_ON_TEAM = 5;

	public abstract int pickAttack(int yourScore, int opponentScore);

	public abstract int action(int[] defenders);

	public int[] pickTeam(String opponent, int totalPlayers, Game[] history){
		return null;
	}

	public Statistics getStat() {
		return stat;
	}

	public void opponentTeam(int[] opponentPlayers){}

	protected Statistics stat;	

	public int[] pickDefend(int yourScore, int opponentScore, int ballHolder, Round previousRound) {
		int[] defenders = new int [] {1,2,3,4,5};
		shuffle(defenders, gen);
		return defenders;
	}

	//shuffles the array
	private static void shuffle(int[] a, Random gen)
	{
		for (int i = 0 ; i != a.length ; ++i) {
			int r = gen.nextInt(a.length - i) + i;
			int t = a[i];
			a[i] = a[r];
			a[r] = t;
		}
	}

	public int[][] getShotsAgainstTable() {
		return this.shotsAgainstTable;
	}
}