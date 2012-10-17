package hoop.g2;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class PreSeasonTeam {

	private LinkedList<Player> team = new LinkedList<Player>();

	private int score;
	private Player holder;
	private Player shooter;
	private Player passer;
	private Random gen;
	private int[][] shotsAgainstTable;
	private int[][] passesAgainstTable;

	public PreSeasonTeam(Random gen, int[][] shotsAgainstTable, int[][] passesAgainstTable) {
		this.gen = gen;
		this.shotsAgainstTable = shotsAgainstTable;
		this.passesAgainstTable = passesAgainstTable;
	}

	/**
	 * Resets state for new game.
	 */
	public void reset(){
		score = 0;
		holder = null;
		shooter = null;
		passer = null;
	}

	/**
	 * Returns int array of player ids on this team.
	 * @return player ids.
	 */
	public int[] toIntArray() {
		return toIntArray(team);
	}

	//converts from LinkedList<Player> to int[]
	private int[] toIntArray(List<Player> list){		
		int[] output = new int[list.size()];
		Iterator<Player> iter = list.iterator();
		int i=0;
		while(iter.hasNext()){
			Player currentPlayer = iter.next();
			output[i] = currentPlayer.getId();
			i++;
		}
		return output;
	}

	/**
	 * If current score is one greater than this.score, this team just scored on the last play.
	 * Update the state of the object to reflect this.
	 * @param score current score.
	 */
	public void updateScore(int score) {
		if(this.score < score){
			shooter.iterateShotsMade();
			this.score = score;
		}
	}

	/**
	 * Randomly picks attacker.
	 * @return Position of the attacker.
	 */
	public int pickAttacker() {
		int attackerIndex = gen.nextInt(Coach.PLAYERS_ON_TEAM) + 1;
		holder = team.get(attackerIndex - 1);
		return attackerIndex;
	}

	/**
	 * Selects the player to pass to with the pickShooter method and passes to them.
	 * @param defenders 
	 * @return Position of new holder.
	 */
	public int pass(int[] defenders) {
		passer = holder;
		passer.iteratePassesAttempt();
		int holderIndex = pickShooter();
		holder = team.get(holderIndex - 1);		
		return holderIndex;	
	}

	public void shoot(int[] defenders) {
		shooter = holder;
		shooter.iterateShotsAttempt();
		passer.iteratePassesMade();
		int shooterId = shooter.getId();
		int shooterIndex = team.indexOf(shooter);
		int blockerId = defenders[shooterIndex];
		this.shotsAgainstTable[shooterId-1][blockerId-1]++;
	}
	
	public void add(Player player) {
		team.add(player);
	}

	public Player removeFirst() {
		return team.removeFirst();
	}

	public void addLast(Player player) {
		team.addLast(player);
	}

	public List<Player> asList() {
		return team;
	}

	public Player getHolder() {
		return holder;
	}
	
	public int size() {
		return team.size();
	}

	private int pickShooter() {
		return randomPickShooter();	
	}

	private int randomPickShooter() {
		int holder = team.indexOf(this.holder);
		int holderIndex = holder;
		while (holder == holderIndex){
			holderIndex = gen.nextInt(Coach.PLAYERS_ON_TEAM) + 1;
		}
		return holderIndex;
	}

}

//private int greedyPickShooter() {
//List<Player> players = stat.getTeam(name).getPlayers();		
//int holderIndex = -1;
//double maxrate = 0;
//for (int index = 0; index < PLAYERS_ON_TEAM; index++) {
//	if (currentTeam.get(index) == holder)
//		continue;
//	Player player = currentTeam.get(index);
//	if (player.getShotPercent() > maxrate) {					
//		maxrate = players.get(player.getId()-1).getShotPercent();
//		holderIndex = index + 1;
//	}
//}
//return holderIndex;
//}

//private int adaptivePickShooter() {
//int holderIndex = -1;
//
//List<Player> players = stat.getTeam(name).getPlayers();
//
//// get other four players and their shooting rate 
//double ratesum = 0;
//double[] rates = new double[4];
//double[] prefixsums = new double[4];
//int[] indices = new int[4];
//for (int index = 0, k = 0; index < PLAYERS_ON_TEAM; index++) {
//	if (currentTeam.get(index) == holder)
//		continue;
//	Player player = currentTeam.get(index);
//	rates[k] = players.get(player.getId()-1).getShotPercent();
//	ratesum += rates[k];
//	indices[k] = index;
//	k++;
//}
//// normalize
//for (int k = 0; k < 4; k++) {
//	rates[k] = rates[k] / ratesum;
//}
//// prefix sum
//prefixsums[0] = rates[0];
//for (int k = 1; k < 4; k++) {
//	prefixsums[k] = prefixsums[k-1] + rates[k];
//}
//
//double rand = gen.nextDouble();			
//for (int k = 0; k < 4; k++) {
//	if (k == 0 ) {
//		if (rand <= prefixsums[k]) {
//			holderIndex = indices[k] + 1;
//			break;
//		}
//	}
//	else if (rand > prefixsums[k-1] && rand <= prefixsums[k]) {
//		holderIndex = indices[k] + 1;
//		break;
//	}				
//}
//return holderIndex;
//}