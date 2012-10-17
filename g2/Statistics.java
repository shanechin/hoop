package hoop.g2;

import hoop.sim.Game;
import hoop.sim.Game.Action;
import hoop.sim.Game.Round;

import java.util.HashMap;
import java.util.Map;

public class Statistics {
	private Map<String, TeamStat> teams;
	private int playerCount;	
	private int gameCounter = 0;
	
	public Statistics(int playerCount) {
		this.teams = new HashMap<String, TeamStat>();
		this.playerCount = playerCount;
	}
	
	public TeamStat getTeam(String tid){
		if (!teams.containsKey(tid))
			teams.put(tid, new TeamStat(tid, playerCount));
		return teams.get(tid);
	}
	
	public Player getPlayer(String tid, int pid) {
		TeamStat team = getTeam(tid);
		Player player =  team.getPlayer(pid);
		return player;
	}
	
	public double getPlayerShotPercentage(String tid, int pid) {
		return teams.get(tid).getPlayerShotPercentage(pid);
	}

	public double getPlayerBlockPercentage(String tid, int pid) {
		return teams.get(tid).getPlayerBlockPercentage(pid);
	}
	
	public double getPlayerPassPercentage(String tid, int pid) {
		return teams.get(tid).getPlayerPassPercentage(pid);
	}
	
	public double getPlayerStealPercentage(String tid, int pid) {
		return teams.get(tid).getPlayerStealPercentage(pid);
	}	
	
	public void update(Game[] history) {
		for (int i = gameCounter; i < history.length; i++) {
			updateGame(history[i]);
		}
		gameCounter = history.length;
	}
	
	private void updateGame(Game game) {
		String ta = game.teamA;
		String tb = game.teamB;
		if (!teams.containsKey(ta))
			teams.put(ta, new TeamStat(ta, playerCount));
		if (!teams.containsKey(tb))
			teams.put(tb, new TeamStat(tb, playerCount));
		
		int[] playersA = game.playersA();
		int[] playersB = game.playersB();
		
		for (int rid = 0; rid < game.rounds(); rid++) {
			Round round = game.round(rid);
			if (round.attacksA)
				updateRound(teams.get(ta), teams.get(tb), playersA, playersB, round);
			else
				updateRound(teams.get(tb), teams.get(ta), playersB, playersA, round);
		}
	}
	
	
	private void updateRound(TeamStat attackTeam, TeamStat defendTeam, int[] offendPlayers, int[] defendPlayers, Round round) {		
		int[] holders = round.holders();
		int[] defenders = round.defenders();
		for (int move = 0; move < holders.length; move++) {
			int position = holders[move];
			Player attacker = attackTeam.getPlayer(offendPlayers[position-1]);			
			Player defender = defendTeam.getPlayer(defendPlayers[defenders[position-1]-1]);
			if (move != holders.length - 1) { // not last player
				attacker.iteratePassesAttempt();
				attacker.iteratePassesMade();
				defender.iterateStealsAttempt();
			}
			else {				
				if (round.lastAction() == Action.SCORED) {
					attacker.iterateShotsAttempt();
					attacker.iterateShotsMade();
					defender.iterateBlocksAttempt();
				}
				else if (round.lastAction() == Action.MISSED) {
					attacker.iterateShotsAttempt();
					defender.iterateBlocksAttempt();
					defender.iterateBlocksMade();
				}
				else if (round.lastAction() == Action.STOLEN) {
					attacker.iteratePassesAttempt();
					defender.iterateStealsAttempt();
					defender.iterateStealsMade();
				}
				else
					throw new RuntimeException("Unexpected Action Type!");
			}
		}
	}
	
}
