package hoop.g6;

import hoop.sim.Game;
import hoop.sim.Game.Round;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class Analyzer {
	private ArrayList<Player> bestOffenders;
	private ArrayList<Player> bestDefenders;
	private HashMap<Integer, Double> playeridToDefense;
	private HashMap<String, Integer> enemyIndices;
	private HashMap<Integer, String> enemyNames;
	private HashMap<EnemyPlayer, Double> estimatedStrengths;
	private int nextEnemyIndex = 0;
	
	private Game[] games;
	
	public LinkedList<Integer> getMRU(String teamName) {
		LinkedHashSet<Integer> lhs = new LinkedHashSet<Integer>();
		
		for(Game game : games) {
			if(!game.teamA.equals(teamName) && !game.teamB.equals(teamName))
				continue;
			boolean isTeamA = game.teamA.equals(teamName);
			
			int nRounds = game.rounds();
			for(int r = 0; r < nRounds; r++) {
				Round round = game.round(r);
				if(round.attacksA && !isTeamA) continue;
				else if(round.attacksB && isTeamA) continue;
				if(round.lastAction().equals(Game.Action.STOLEN)) continue;
				if(round.lastAction().equals(Game.Action.PASSED)) continue;
				int[] players = isTeamA ? game.playersA() : game.playersB();
				int[] holders = round.holders();
//				System.out.println("hl: " + holders.length);
//				System.out.println("pl: " + players.length);
				int holder = holders[holders.length - 1];
//				System.out.println("holder: " + holder);
				int lastHandler = players[holder - 1];
				lhs.add(lastHandler);
			}
		}
		
		LinkedList<Integer> lls = new LinkedList<Integer>();
		for(Integer i : lhs) {
			lls.addFirst(i);
		}
		return lls;
	}
	
	static class EnemyPlayer {
		private String teamName;
		private int playerId;
		
		public EnemyPlayer(String teamName, int playerId) {
			this.teamName =teamName;
			this.playerId =playerId;
		}
		public int hashCode() {
			return teamName.hashCode() ^ playerId;
		}
		
		public boolean equals(Object other) {
			if(!(other instanceof EnemyPlayer))
				return false;
			EnemyPlayer o = (EnemyPlayer) other;
			return o.playerId == playerId && o.teamName.equals(teamName);
		}
	}
	
	public Analyzer(String name, Game[] games, int totalPlayers) {
		this.games = games;
		int[][] scoreSuccesses = new int[totalPlayers + 1][totalPlayers + 1];
		int[][] scoreAttempts = new int[totalPlayers + 1][totalPlayers + 1];
		int[][][] enemyScoreAttempts = new int[7][totalPlayers + 1][totalPlayers + 1];
		int[][][] enemyScoreSuccesses = new int[7][totalPlayers + 1][totalPlayers + 1];
		enemyIndices = new HashMap<String, Integer>();
		enemyNames = new HashMap<Integer, String>();
		estimatedStrengths = new HashMap<EnemyPlayer, Double>();
		
		for(Game game : games) {
			if(!game.teamA.equals(name))
				continue;
			if(!game.teamB.equals(name))
				continue;
			for(int i = 0; i < game.rounds(); i++) {
				Round r = game.round(i);
				if(r.lastAction().equals(Game.Action.SCORED)
						|| r.lastAction().equals(Game.Action.MISSED)) {
					int[] holders = r.holders();
					int shooterIdx = holders[holders.length - 1] - 1;
					int defenderIdx = r.defenders()[shooterIdx] - 1;
					
					int[] attackTeam;
					int[] defendTeam;
					
					if(r.attacksA) {
						attackTeam = game.playersA();
						defendTeam = game.playersB();
					} else {
						attackTeam = game.playersB();
						defendTeam = game.playersA();
					}
					int shooterId = attackTeam[shooterIdx];
					int defenderId = defendTeam[defenderIdx];
					boolean success = r.lastAction().equals(Game.Action.SCORED);
					if(success) {
//						System.out.println(shooterId + " scored on " + defenderId);
						scoreSuccesses[shooterId][defenderId]++;
					} else {
//						System.out.println(shooterId + " failed on " + defenderId);
					}

					scoreAttempts[shooterId][defenderId]++;
				} else {
					continue;
				}
			}
		}
		
//		for(int i = 0; i < scoreAttempts.length; i++) {
//			for(int j = 0; j < scoreAttempts.length; j++) {
//				System.out.print(scoreAttempts[i][j] + "/" + scoreSuccesses[i][j] + " ");
//			}	
//			System.out.println();
//		}
		
		bestOffenders = new ArrayList<Player>();
		bestDefenders = new ArrayList<Player>();
		playeridToDefense = new HashMap<Integer, Double>();
		for (int defender = 1; defender <= totalPlayers; defender++) {
			int count = 0;
			int successes = 0;
			int attempts = 0;
			double d = 0;
			for (int j = 0; j < scoreAttempts.length; j++) {
				if (scoreAttempts[j][defender] == 0)
					continue;
				attempts += scoreAttempts[j][defender];
				successes += scoreSuccesses[j][defender];
				count++;
				d += 2 * scoreSuccesses[j][defender] / (double) scoreAttempts[j][defender];
			}
			 d -= (count / 2);
			d = d / (count);
			playeridToDefense.put(defender, d);
//			System.out.println("Estimated defender strength[" + defender +"]: " + d);
			
			if(Double.isNaN(d))
				continue;
			Player p = new Player(defender);
			p.setDefendRate(d);
			bestDefenders.add(p);
			
			for (int j = 0; j < scoreAttempts.length; j++) {
				if (scoreAttempts[j][defender] == 0)
					continue;
				double ratio = scoreSuccesses[j][defender] / (double) scoreAttempts[j][defender];
				double shooter = 2 * ratio - d;
				for(Player player : bestDefenders) {
					if(j == player.getId())
						player.setAttackRate(shooter);
					
					if(defender == 1)
						bestOffenders.add(player);
				}
				
					
//				System.out.println("Estimated shoot str[" + j + "]: " + shooter);
			}
		}
		
		Collections.sort(bestDefenders, new DefenderComparator());
//		System.out.println("DefenderS: " + bestDefenders);
		
		
		
		
		
		
		
		
		/* Testing enemy attacker strengths */
		for(Game game : games) {
			if(!game.teamA.equals(name) && !game.teamB.equals(name))
				continue;
			if(game.teamA.equals(game.teamB))
				continue;
			
			boolean weAreA = game.teamA.equals(name);
			String enemyName = weAreA ? game.teamB : game.teamA;
			if(!enemyIndices.containsKey(enemyName)) {
				enemyIndices.put(enemyName, nextEnemyIndex);
				enemyNames.put(nextEnemyIndex, enemyName);
				nextEnemyIndex++;
			}
			int enemyIndex = enemyIndices.get(enemyName);
			
			for(int i = 0; i < game.rounds(); i++) {
				Round r = game.round(i);
				if(r.lastAction().equals(Game.Action.SCORED)
						|| r.lastAction().equals(Game.Action.MISSED)) {
					int[] holders = r.holders();
					int shooterIdx = holders[holders.length - 1] - 1;
					int defenderIdx = r.defenders()[shooterIdx] - 1;
					
					int[] attackTeam;
					int[] defendTeam;
					
					if(r.attacksA) {
						if(weAreA) continue; // only interested in other team atkin us
						attackTeam = game.playersA();
						defendTeam = game.playersB();
					} else {
						if(!weAreA) continue;
						attackTeam = game.playersB();
						defendTeam = game.playersA();
					}
					
					int shooterId = attackTeam[shooterIdx];
					int defenderId = defendTeam[defenderIdx];
					double ourDefendRatio = playeridToDefense.get(defenderId);
					boolean success = r.lastAction().equals(Game.Action.SCORED);
					
					if(success) {
//						System.out.println(shooterId + " scored on " + defenderId);
						enemyScoreSuccesses[enemyIndex][shooterId][defenderId]++;
//						System.out.println("Enemy player " + shooterId + " of  " + enemyName + " scored on poor + " + defenderId);
					} else {
//						System.out.println(shooterId + " failed on " + defenderId);
					}

					enemyScoreAttempts[enemyIndex][shooterId][defenderId]++;
				} else {
					continue;
				}
			}
		}
		
//		for(int i = 0; i < scoreAttempts.length; i++) {
//			for(int j = 0; j < scoreAttempts.length; j++) {
//				System.out.print(scoreAttempts[i][j] + "/" + scoreSuccesses[i][j] + " ");
//			}	
//			System.out.println();
//		}
		
		for(int enemy = 0; enemy < enemyIndices.size(); enemy++) {
			for (int defender = 1; defender <= totalPlayers; defender++) {
				double d = playeridToDefense.get(defender);
				if(Double.isNaN(d))
					continue;
				
				for (int j = 0; j < enemyScoreAttempts[enemy].length; j++) {
					if(enemyScoreAttempts[enemy][j][defender] == 0)
						continue;

					double ratio = enemyScoreSuccesses[enemy][j][defender] / (double) enemyScoreAttempts[enemy][j][defender];
					double shooter = 2 * ratio - d;
					estimatedStrengths.put(new EnemyPlayer(enemyNames.get(enemy), j), shooter);
//					System.out.println("Estimated player " + j + " of " + enemyNames.get(enemy) + " at " + shooter);
					
					
					
	//				System.out.println("Estimated shoot str[" + j + "]: " + shooter);
				}
			}
		}
		
		Collections.sort(bestDefenders, new DefenderComparator());
//		System.out.println("DefenderS: " + bestDefenders);
		
		
	}
	
	public ArrayList<Player> getBestDefenders() {
		return (ArrayList) bestDefenders.clone();
	}
	
	public ArrayList<Player> getBestOffenders() {
		return (ArrayList) bestOffenders.clone();
	}
	
	public int[] pickBestDefense(int[] ourPlayers, String enemyName, int[] theirPlayers) {
		double[] theirStrengths = new double[theirPlayers.length];
		for(int i = 0; i < theirPlayers.length; i++) {
			EnemyPlayer key = new EnemyPlayer(enemyName, theirPlayers[i]);
			if(estimatedStrengths.containsKey(key)) {
				theirStrengths[i] = estimatedStrengths.get(key);
			}
		}
		
		double minMaxMismatch = Double.MAX_VALUE;
		int[] bestSoFar = null;
		
		
		ArrayList<int[]> found = new ArrayList<int[]>();
		permute(found, new ArrayList<Integer>(), new int[ourPlayers.length], ourPlayers, 0);
//		System.out.println("len: " + ourPlayers.length);
//		System.out.println("FUOND " + found.size() + " perms");
		
		for(int[] perm : found) {
//			System.out.println("perm: " + Arrays.toString(perm));
			
			int[] actualPerm = new int[perm.length];
			for(int i = 0; i < perm.length; i++) {
				actualPerm[i] = ourPlayers[perm[i]];
			}
//			System.out.println("aperm: " + Arrays.toString(actualPerm));
			
			double maxMismatch = evaluate(actualPerm, theirStrengths);
//			System.out.println("maxM: " + maxMismatch);
			if(maxMismatch < minMaxMismatch) {
				minMaxMismatch = maxMismatch;
				bestSoFar = perm;
			}
		}
		
		for(int i = 0; i < bestSoFar.length; i++)
			bestSoFar[i]++;
		
		return bestSoFar;
	}

	// some help from http://stackoverflow.com/questions/9632677/combinatorics-generate-all-states-array-combinations
	public static void permute(ArrayList<int[]> store, ArrayList<Integer> used, int[] space, int[] ourDefenders, int idx) {
//		System.out.println("Space: " + space.length);
//		System.out.println("RET: " + store.size());
	    
//		System.out.println("Idx: " + idx);
		if (idx == space.length) { 
			store.add(space.clone());
//			System.out.println("Stored: " + Arrays.toString(space));
	    	return;
	    }
	    
	    for (int i = 0; i < ourDefenders.length; i++) {
			if (used.contains(i))
				continue;
			space[idx] = i;
			ArrayList<Integer> nextUsed = (ArrayList) used.clone();
			nextUsed.add(i);
			permute(store, nextUsed, space, ourDefenders, idx + 1);
	    }
	}
	
	private double evaluate(int[] ourDefenders, double[] theirStrengths) {
		double maxMismatch = 0;
		double[] ourStrenths = new double[ourDefenders.length];
		for(int i = 0; i < ourStrenths.length; i++) {
//			System.out.println("i: " + i + "/" + ourDefenders[i]);
			ourStrenths[i] = playeridToDefense.get(ourDefenders[i]);
			double misMatch = theirStrengths[i] + ourStrenths[i];
			if(misMatch > maxMismatch)
				maxMismatch = misMatch;
		}
		return maxMismatch;
	}
}
