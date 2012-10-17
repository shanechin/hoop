package hoop.g6;

import hoop.sim.Game;
import hoop.sim.Game.Round;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/* Note that the same instance of the class is used during the internal
 * tournament.
 */
public class Team implements hoop.sim.Team {
	public Team() {
		if(false)System.out.println("inited");
	}

	public static final int TEAM_SIZE = 5;
	private final int NUM_PASSES_INTERNAL = 5;

	private final Random rand = new Random();

	/* If not internal tournament, second team is null. */
	private int[] firstTeam, secondTeam;

	/* If internal tournament, the team that is up; else, the only team we have. */
	private int[] currentTeam;

	/* If internal tournament, is the first instance on the attack; else true. */
	private boolean firstTeamAttacking;

	/* Is the current round internal? */
	private boolean internal;

	private int totalPlayers;

	/* Did we pass last turn? Used to detect pass successes. */
	private boolean passed;
	private int passerId;
	
	private int ballHolderIdx;

	private boolean shot;
	private int shooterId;
	
	private DataRecorder internalDataRecorder;

	/* Number of passes in current play. */
	private int numPasses;

	private int shootingTeamScore, currentTeamScore;

	private boolean initialized = false;
	
	/* Defenders are the people we shoot against for each team. */
	/* Defender 1 = "defender on team 1" */
	private int defender1, defender2;
	
	/* Attackers are the people we've designated to shoot against the defender */
	/* Attacker 1 = "attacker of team 1" */
	private int attacker1, attacker2;
	private String opponent;
	private int[] opponentPlayers;
	
	private Analyzer analyzer;

	public int[] pickTeam(String opponent, int totalPlayers, Game[] history) {
		this.opponent = opponent;
		
		if(history.length >= 1) {
			int i = history.length - 1;
			if(false)System.out.println("Rounds: " + history[i].round(0));
			printArray(history[i].round(0).defenders());
			printArray(history[i].round(0).holders());
			if(false)System.out.println(history[i].round(0).lastAction());
		}
		
		if(false)System.out.println("===");
		if(history.length != 0)
			analyzer = new Analyzer(name(), history, totalPlayers);
		
		if(false)System.out.println("History: " + history.length);
		this.totalPlayers = totalPlayers;
		this.internal = opponent.equals(name());

		if (!initialized) {
			internalDataRecorder = new DataRecorder(totalPlayers);
			initialized = true;
		}

		final int[] team = new int[TEAM_SIZE];

		if (!internal) {
			secondTeam = null;

			ArrayList<Player> candidateList = new ArrayList<Player>();

//			/* Picking the 5 players with best shot ratio. */
//			for (int i = 0; i < totalPlayers; i++) {
//				if (!internalDataRecorder.hasShotInfo(i))
//					continue;
//
//				double shotRatio = internalDataRecorder.getShotSuccessRatio(i);
//				Player player = new Player(i);
//				player.setAttackRate(shotRatio);
//				candidateList.add(player);
//			}
//
//			Collections.sort(candidateList, new ShootingComparator());

			/* Picking the 5 players with best defend ratio. */
			ArrayList<Player> bestDef = analyzer.getBestDefenders();
			for(int i = 0; i < TEAM_SIZE; i++)
				candidateList.add(bestDef.get(i));
			ArrayList<Player> bestShoot = analyzer.getBestOffenders();
//			candidateList.add(bestShoot.get(0));
			
			for (int i = 0; i < TEAM_SIZE; i++) {
				Player player = candidateList.get(i);
				team[i] = player.getId();
				if(false)System.out.println("Selected player: " + player);
			}

			/* TODO: Figure out what all these variables do, refactor. */
			firstTeam = team;
			currentTeam = team;
		} else {
			int[] candidates = new int[totalPlayers];
			for(int i = 0; i < candidates.length; i++) {
				candidates[i] = i + 1;
			}
			List<Player> players = describeTeam(candidates);
			Collections.sort(players, new ShotsTakenOnComparator());
			if(false)System.out.println("STO: " + players);
			defender1 = players.get(0).getId();
			defender2 = players.get(1).getId();
			
			/* Reset state if we just finished an internal game */
			if (secondTeam != null)
				secondTeam = firstTeam = null;
			
			HashSet<Integer> used = new HashSet<Integer>();
			used.add(defender1);
			used.add(defender2);
			
			if(firstTeam != null)
				for(int player : firstTeam)
					used.add(player);
			
			ArrayList<Integer> teamList = new ArrayList<Integer>();
			if(firstTeam == null)
				teamList.add(defender1);
			else
				teamList.add(defender2);
			
			/* Just grab a few other players randomly */
			int[] randomTeam = randomTeam(used);
			for(int playerId = 0; playerId < randomTeam.length - 1; playerId++) {
				teamList.add(randomTeam[playerId]);
			}
		
			for(int i = 0; i < teamList.size(); i++) {
				team[i] = teamList.get(i);
			}
			
			if(firstTeam == null)
				firstTeam = team;
			else
				secondTeam = team;

			/* Initialize tracking of who is playing (yes, should be false) */
			/* Maybe refactor this */
			firstTeamAttacking = false;
			currentTeam = firstTeam;
		}

		return team;
	}

	private int[] randomTeam(Set<Integer> exclude) {
		HashSet<Integer> invalid = new HashSet<Integer>();
		invalid.addAll(exclude);
		exclude = null;

		final int[] team = new int[5];
		for (int i = 0; i < TEAM_SIZE; i++) {
			int player;

			do {
				player = 1 + rand.nextInt(totalPlayers);
			} while (invalid.contains(player));
			invalid.add(player);
			team[i] = player;
		}

		return team;
	}

	@Override
	/* This method is called once at the start of each game. */
	public void opponentTeam(int[] opponentPlayers) {
		if(false)System.out.println("Opponent team: ");
		printArray(opponentPlayers);
		this.opponentPlayers = opponentPlayers;
	}

	@Override
	public int action(int[] defenders) {
		/* actionIdx: 1 to 5 */
		/* lastPasserIdx: 0 to 4 */
		
		int actionIdx;
		
		if(internal) {
			if(numPasses >= NUM_PASSES_INTERNAL) {
				actionIdx = 0;
				
			} else {
				/* Pass to the player that has shot the last so he can shoot */
				
				if(firstTeamAttacking) {
//					if(false)System.out.println("Seeking: " + attacker1);
//					if(false)System.out.println("Our team: " );
					printArray(firstTeam);
					actionIdx = 1 + findIdx(firstTeam, attacker1);
//					if(false)System.out.println("HOLDERING: " + actionIdx);
				}
				else {
//					if(false)System.out.println("Seeking: " + attacker2);
					actionIdx = 1 + findIdx(secondTeam, attacker2);
//					if(false)System.out.println("HOLDERING: " + actionIdx);
				}
//				actionIdx = 1 + rand.nextInt(TEAM_SIZE);
			}
		} else {
			/* Actual game */
//			if(false)System.err.println("ACTUALGAMENUMPASSES" + numPasses);
			if(numPasses >= 1) {
				actionIdx = 0;
			} else {
				double bestShootRatioD1 = 0;
				
				// fix later
//				int bestShooter = -1;
//				
//				for(int i = 0; i < currentTeam.length; i++) {
//					double ratio = internalDataRecorder.getShotSuccessRatioAgainstD1(currentTeam[i]);
//					if(ratio > bestShootRatioD1) {
//						bestShootRatioD1 = ratio;
//						bestShooter = i;
//					}
//				}
//
//				if(false)System.out.println("BESTSHOOTER: " + bestShooter);
////				if(false)System.out.println("Our best shooter [" + currentTeam[bestShooter] + "] has ratio [G6-2]: " + bestShootRatioD1);
//				actionIdx = bestShooter + 1;
				
				List<Player> player = describeTeam(currentTeam);
				Collections.sort(player, new ShootingComparator());
				actionIdx = 1 + findIdx(currentTeam, player.get(0).getId());
			}
		}


		/* Only accounting information here -- no more decision-making code. */
		if (passed && internal) {
			/* We passed and were successful. We know this because we still
			 * have the ball (if we failed flag would be reset in pickTeam).
			 */
			internalDataRecorder.recordPass(passerId, true);
//			if(false)System.err.println("Pass succeeded.");
		}

		passed = actionIdx != 0;

		if (passed) {
			/* We are passing */
			passerId = currentTeam[ballHolderIdx];
			numPasses++;
			ballHolderIdx = actionIdx - 1;
//			if(false)System.err.println("Passed");
		} else {
			/* We're shooting */
			shot = true;
			shooterId = currentTeam[ballHolderIdx];
			shootingTeamScore = currentTeamScore;
		}
		return actionIdx;
	}

	@Override
	public String name() {
		return "G6-V4";
	}

	private void printArray(int[] array) {
		for (int i = 0; i < array.length; i++)
			if(false)System.out.print(array[i] + " ");
		if(false)System.out.println();
	}
	
	private void printArray2(int[] array) {
		for (int i = 0; i < array.length; i++)
			if(true)System.out.print(array[i] + " ");
		if(true)System.out.println();
	}

	@Override
	public int pickAttack(int yourScore, int opponentScore, Round previousRound) {
		currentTeamScore = yourScore;

//		if(false)System.out.println("Passing success rate:");
//		for (int i = 1; i < totalPlayers; i++)
//			if(false)System.out.printf("%-6d", i);
//		if(false)System.out.println();
//		for (int i = 1; i < totalPlayers; i++)
//			if(false)System.out.printf("%-6.2f", internalDataRecorder.getPassSuccessRatio(i));
//		if(false)System.out.println();
//
//		if(false)System.out.println("Shooting success rate:");
//		for (int i = 1; i < totalPlayers; i++)
//			if(false)System.out.printf("%-6d", i);
//		if(false)System.out.println();
//		for (int i = 1; i < totalPlayers; i++)
//			if(false)System.out.printf("%-6.2f", internalDataRecorder.getShotSuccessRatio(i));
//		if(false)System.out.println();
//		if(false)System.out.println("Shooting success rate: [vs d1]:");
//		for (int i = 1; i < totalPlayers; i++)
//			if(false)System.out.printf("%-6d", i);
//		if(false)System.out.println();
//		for (int i = 1; i < totalPlayers; i++)
//			if(false)System.out.printf("%-6.2f", internalDataRecorder.getShotSuccessRatioAgainstD1(i));	

		/* If internal and we shot and score increased (we made it), take note */
		if(internal && shot) {
			boolean success = opponentScore > shootingTeamScore;
			int defender = firstTeamAttacking ? defender2 : defender1;
			internalDataRecorder.recordShot(shooterId, defender, success);
			if(false)System.out.println("Shot taken on: " + defender);
		}

		/* If internal and we passed last turn, take note that our pass missed. */
		if (internal && passed) {
			internalDataRecorder.recordPass(passerId, false);
		}

		if(false)System.out.println("Score: Us-Them " + yourScore + "-" + opponentScore);

		/* Resetting some state. */
		passed = false;
		shot = false;
		numPasses = 0;
		firstTeamAttacking = !firstTeamAttacking;

		if (internal && !firstTeamAttacking) {
			currentTeam = secondTeam;
		} else {
			currentTeam = firstTeam;
		}

		/* Pick first selected player as initial ball holder. */
		if (internal) {
			ballHolderIdx = 0;
		} else {
			double bestPassRatio = 0;
			int bestPasser = 0;
			for (int i = 0; i < currentTeam.length; i++) {
				if(!internalDataRecorder.hasPassInfo(currentTeam[i]))
					continue;
				double ratio = internalDataRecorder.getPassSuccessRatio(currentTeam[i]);

				if (ratio > bestPassRatio) {
					bestPassRatio = ratio;
					bestPasser = i;
				}
			}
			ballHolderIdx = bestPasser;
		}

		/* convert to 1-5 */
		return ballHolderIdx + 1;

	}

	@Override
	public int[] pickDefend(int yourScore, int opponentScore, int ballHolder,
			Round previousRound) {
		
		if (!internal) {
//			int[] ret = analyzer.pickBestDefense(currentTeam, opponent, opponentPlayers);
//			return ret;
			
			LinkedList<Integer> mrul = analyzer.getMRU(opponent);
//			System.out.println("MRUL:" + mrul);
			ArrayList<Integer> unmatched = new ArrayList<Integer>();
			unmatched.add(1);
			unmatched.add(2);
			unmatched.add(3);
			unmatched.add(4);
			unmatched.add(5);
			
			/* Right side has lower priority */
			
//			System.out.println("MRUL: " + mrul);
//			System.out.println("Def: " + analyzer.getBestDefenders());
//			System.out.println("opp lineup: ");
//			printArray2(opponentPlayers);
//			System.out.println("out lineup: ");
//			printArray2(currentTeam);
			
			ArrayList<Integer> enemyLineup = new ArrayList<Integer>();
			for(int i = 0; i < opponentPlayers.length; i++) {
				enemyLineup.add(opponentPlayers[i]);
			}
			ArrayList<Integer> priorityList = new ArrayList<Integer>();
			for(int i : mrul) {
				if(enemyLineup.contains(i))
					priorityList.add(i);
			}
			for(int i : enemyLineup) {
				if(!priorityList.contains(i))
					priorityList.add(i);
			}
			
			/* assuming our guys are ordered by their efensive str in the arr.*/
			int[] defense = new int[TEAM_SIZE];
			int prio = 1;
			for(int i : priorityList) {
				defense[findIdx(opponentPlayers, i)] = prio;
				prio++;
			}
			
//			System.out.println("out2 lineup: ");
//			printArray2(defense);
			return defense;
			
//			ArrayList<Integer> assignments = new ArrayList<Integer>();
//			for (int i = 1; i <= TEAM_SIZE; i++)
//				assignments.add(i);
//			Collections.shuffle(assignments);
//
//			final int[] defend = new int[TEAM_SIZE];
//			for (int i = 0; i < TEAM_SIZE; i++)
//				defend[i] = assignments.get(i);
//			return defend;
		} else {
			/* Interestingly, in internal games, we determine the attacking team's
			 * attacker here. Set up matchups so that our designated defender
			 * covers their attacker.
			 */
			
			int[] attackingTeam;
			int[] defendingTeam;
			int defenderId;
			
			if(firstTeamAttacking) {
				attackingTeam = firstTeam;
				defenderId = defender2;
				defendingTeam = secondTeam;
			} else {
				attackingTeam = secondTeam;
				defenderId = defender1;
				defendingTeam = firstTeam;
			}
			List<Player> enemyPlayers = describeTeam(attackingTeam);
			Collections.sort(enemyPlayers, new ShotsTakenComparator());
			if(false)System.out.println("eP: " + enemyPlayers);
			
			int attackerId = enemyPlayers.get(0).getId();
			if(false)System.out.println("FirstInstance: " + firstTeamAttacking);
			if(firstTeamAttacking)
				attacker1 = attackerId;
			else
				attacker2 = attackerId;
			if(false)System.out.println("Defender will defend: " + defenderId);
			if(false)System.out.println("attackerId: " + attackerId);	
			int attackerIdx = findIdx(attackingTeam, attackerId);
			int defenderIdx = findIdx(defendingTeam, defenderId);

			if(false)System.out.println("Defender: " + defenderIdx);

			if(false)System.out.println("attackerIdx: " + attackerIdx);		
			
			final int[] defend = new int[TEAM_SIZE];
			defend[attackerIdx] = defenderIdx + 1;
				
			int j = 0;
			for(int i = 0; i < defend.length; i++) {
				if(i == attackerIdx) continue;
				if(j == defenderIdx) j++;
				defend[i] = j + 1;
				j++;
			}
			
			if(false)System.out.println("DEFN:");
			printArray(defend);
			
			return defend;
		}
	}
	
	/* OK */
	public List<Player> describeTeam(int[] team) {
		List<Player> players = new ArrayList<Player>();
		for(int teamIdx = 0; teamIdx < team.length; teamIdx++) {
			final int playerId = team[teamIdx];
			Player player = new Player(playerId);
			player.setShotsTaken(internalDataRecorder.countShotsTaken(playerId));
			player.setShotsTakenOn(internalDataRecorder.countShotsTakenOn(playerId));
			players.add(player);
		}
		return players;
	}
	
	/* OK */
	public int findIdx(int[] team, int playerId) {
		for(int i = 0; i < team.length; i++)
			if(team[i] == playerId)
				return i;
		throw new RuntimeException();
	}
}