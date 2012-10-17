package hoop.g2;

import hoop.sim.Game;
import hoop.sim.Game.Round;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class RegSeasonCoach extends Coach{

	private Player holder = null;
	private boolean pass = false;
	private String ourName;
	private String theirName;
	private LinkedList<Player> ourRoster;
	private LinkedList<Player> theirRoster;
	private HashMap<Player,Integer> ourPlayerToPositionMap;
	private HashMap<Integer, Player> theirPositionToPlayerMap;
	private HashMap<Integer, Player> ourPositionToPlayerMap;
	

	public RegSeasonCoach(Statistics statistics, String name, int[][] table){
		super();
		this.stat = statistics;
		this.ourName = name;
		this.shotsAgainstTable = table;
	}

	@Override
	public int[] pickTeam(String opponent, int totalPlayers, Game[] history) {
		theirName = opponent;
		int[] allStarsInts = new int[PLAYERS_ON_TEAM];
		ourRoster = new LinkedList<Player>();
		ourPlayerToPositionMap = new HashMap<Player,Integer>();
		ourPositionToPlayerMap = new HashMap<Integer, Player>();


		List<Player> myPlayers = stat.getTeam(this.ourName).getPlayers();
		Collections.sort(myPlayers, Player.AggregateComparator);

		for(int i=0; i < PLAYERS_ON_TEAM; i++){
			allStarsInts[i] = myPlayers.get(i).getId();
			ourRoster.add(myPlayers.get(i));
			ourPlayerToPositionMap.put(myPlayers.get(i), i+1);
			ourPositionToPlayerMap.put(i+1, myPlayers.get(i));
		}
		return allStarsInts;
	}

	public void opponentTeam(int[] opponentPlayers){
		theirPositionToPlayerMap = new HashMap<Integer,Player>();
		theirRoster = new LinkedList<Player>();
		for(int i=0; i<opponentPlayers.length; i++){
			Player player = this.stat.getPlayer(theirName, opponentPlayers[i]);
			theirRoster.add(player);
			theirPositionToPlayerMap.put(i+1, player);
		}
	}

	public int pickAttack(int yourScore, int opponentScore)
	{
		Collections.sort(ourRoster, Player.ShotsMadeComparator);
		holder = ourRoster.getLast();
		pass = true;
		return ourPlayerToPositionMap.get(holder);
	}

	public int action(int[] defenders)
	{
		
		if (!pass) return 0;
		
		double[] mismatch = new double[PLAYERS_ON_TEAM];
		for(int i = 0; i < PLAYERS_ON_TEAM; i++)
		{
			double theirBlocking = theirPositionToPlayerMap.get(i+1).getBlockPercent();
			double ourShooting = ourPositionToPlayerMap.get(i+1).getShotPercent();
			mismatch[i] = ourShooting - theirBlocking;
		}
		
		int maxI = -2;
		double max = -2; //goes from -1 to 1
		for(int i = 0; i < PLAYERS_ON_TEAM; i++)
		{
			if(mismatch[i] > max)
			{
				max = mismatch[i];
				maxI = i;
			}
		}
		pass = false;
		return maxI+1;

	}

	public int[] pickDefend(int yourScore, int opponentScore, int ballHolder, Round previousRound) {
		int[] defence = new int[PLAYERS_ON_TEAM];

		//sort our roster by blocking
		Collections.sort(ourRoster, Player.BlocksMadeComparator);
		//sort their roster by shooting
		Collections.sort(theirRoster, Player.ShotsMadeComparator);
		//move the ball holder to the back, so our worst blocker will defend him/her
		theirRoster.remove(theirPositionToPlayerMap.get(ballHolder));
		theirRoster.addLast(theirPositionToPlayerMap.get(ballHolder));
		
		for(int i=0; i<PLAYERS_ON_TEAM; i++){
			//get their player in the ith position
			Player player = theirPositionToPlayerMap.get(i+1);
			//find that player's rank as a shooter
			int rank = theirRoster.indexOf(player);
			//match them with our defender of equal rank
			defence[i] = ourPlayerToPositionMap.get(ourRoster.get(rank));
		}
		return defence;
	}
}