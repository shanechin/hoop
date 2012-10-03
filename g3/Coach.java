package hoop.g3;

import hoop.sim.Game;

public abstract class Coach {
 
	protected int lineupSize = 5; 
	protected int teamSize;
	protected Player[] myTeam = new Player[lineupSize];
	
	/* return 5 players in 1,2,3,4,...,P where P total players */
	public abstract Player[] pickTeam(Player[] myRoster, Game[] history, int teamSize); 

	/* return one of 1,2,3,4,5 to pick initial ball holder */
	public abstract int pickAttack(int yourScore, int opponentScore);

	/* if 0 then shoot, otherwise pass to 1,2,3,4,5 */
	public abstract int action(int[] defenders);

	/* pick defenders, use 1,2,3,4,5 for players */
	public abstract int[] pickDefend(int yourScore, int opponentScore, int ballHolder);
	
}