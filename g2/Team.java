package hoop.g2;

import java.util.List;

import hoop.sim.Game;
import hoop.sim.Game.Round;

public class Team implements hoop.sim.Team {

	private static int versions;
	private final int version = ++versions;
	private Coach coach;

	public String name(){
		return "G2: JaMeR " + "v" + version;
	}

	//called once per game. lets us know which players are in for the other team.
	public void opponentTeam(int[] opponentPlayers) {
		coach.opponentTeam(opponentPlayers);
	}

	//called once per game. choose our players.
	public int[] pickTeam(String opponent, int totalPlayers, Game[] history)
	{
		//when playing against self, create a preseason coach before the first game
		if (opponent.equals(name())){
			if(coach == null) 
			{
				coach = new PreSeasonCoach(totalPlayers, name());	
			}
		}
		//when playing against others, make a regularseason coach.
		else{						
			if(coach instanceof PreSeasonCoach) coach = new RegSeasonCoach(coach.getStat(), name(), coach.getShotsAgainstTable());
			else if(coach == null){ //case of no preseason games
				coach = new RegSeasonCoach(new Statistics(totalPlayers), name(), null);
			}
		}		
		// update stats first, so that before current game
		// we have full information about previous games 
		coach.getStat().update(history);

		
		// print estimation about ability
		//printEstimation();
		
		return coach.pickTeam(opponent, totalPlayers, history);
	}

	//choose to pass or shoot when the ball is in our possession
	public int action(int[] defenders)
	{
		return coach.action(defenders);
	}

	public String toString(){
		return this.coach.toString();
	}

	@Override
	public int pickAttack(int yourScore, int opponentScore, Round previousRound) {
		return coach.pickAttack(yourScore, opponentScore);
	}

	@Override
	public int[] pickDefend(int yourScore, int opponentScore, int ballHolder, Round previousRound) {
		return coach.pickDefend(yourScore, opponentScore, ballHolder, previousRound);
	}
	
	private void printEstimation() {
		System.out.println("========start============");
		TeamStat stat = coach.getStat().getTeam(name());
		List<Player> players = stat.getPlayers();
		for (int p = 0; p < players.size(); p++) {
			Player player = players.get(p);
			System.out.println("Player " + p + ":[" 
					+ String.format("%.2f", player.getShotPercent()) + ","
					+ String.format("%.2f", player.getBlockPercent()) + ","
					+ String.format("%.2f", player.getPassPercent()) + ","
					+ String.format("%.2f", player.getStealPercent()) + "]" + "(" + player.getShotsAttempt() + ")");
		}
		System.out.println("========end============");
	}
	
	public double[][] getEstimation() {		
		TeamStat stat = coach.getStat().getTeam(name());
		List<Player> players = stat.getPlayers();
		double[][] estimation = new double[players.size()][4];
		for (int p = 0; p < players.size(); p++) {
			Player player = players.get(p);
			estimation[p][0] = player.getShotPercent();
			estimation[p][1] = player.getBlockPercent();
			estimation[p][2] = player.getPassPercent();
			estimation[p][3] = player.getStealPercent();
		}
		return estimation;
	}
}