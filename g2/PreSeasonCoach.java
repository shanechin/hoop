package hoop.g2;

import hoop.sim.Game;

import java.util.*;

public class PreSeasonCoach extends Coach{

	private static final int SHOOT = 0;
	private String name;
	private Player[] playerList;
	private PreSeasonTeam bench;
	private PreSeasonTeam team1;
	private PreSeasonTeam team2;
	private PreSeasonTeam[] teams;
	private boolean needToPass = false; 
	private boolean isRotate = true;
	
	
	public PreSeasonCoach(int totalPlayers, String name){
		super();
		this.name = name;
		//for iterative stats
		shotsAgainstTable = new int[totalPlayers][totalPlayers];
		passesAgainstTable = new int[totalPlayers][totalPlayers];
		//end
		//'validate' the input
		if(totalPlayers < 2*PLAYERS_ON_TEAM) System.out.println("not enough players");
		// initialize stat class
		this.stat = new Statistics(totalPlayers); // initialize the stat class here		
		//create players and teams
		playerList = new Player[totalPlayers];
		bench = new PreSeasonTeam(gen, shotsAgainstTable, passesAgainstTable);
		team1 = new PreSeasonTeam(gen, shotsAgainstTable, passesAgainstTable);
		team2 = new PreSeasonTeam(gen, shotsAgainstTable, passesAgainstTable);
		teams = new PreSeasonTeam[]{team1, team2};
		for(int i=1; i<=PLAYERS_ON_TEAM; i++){
			Player player1 = new Player(i);
			team1.add(player1);
			playerList[i-1] = player1;
			Player player2 = new Player(i+PLAYERS_ON_TEAM);
			team2.add(player2);
			playerList[i+PLAYERS_ON_TEAM-1] = player2;
		}
		for(int i = 2*PLAYERS_ON_TEAM+1; i<= totalPlayers; i++){
			Player player = new Player(i);
			bench.add(player);
			playerList[i-1] = player;
		}
	}	
	
	@Override
	public int[] pickTeam(String opponent, int totalPlayers, Game[] history){
		//rotate players every other time this method is called
		if(isRotate) rotateTeams();
		isRotate = ! isRotate;
		//reset score at start of game
		teams[0].reset();
		//switch current team
		swapTeams();
		return teams[1].toIntArray();
	}
	
	public int pickAttack(int yourScore, int opponentScore){
		needToPass = true;
		swapTeams();
		teams[1].updateScore(opponentScore);
		return teams[0].pickAttacker();
	}
	
	/**
	 * Choose to pass or shoot. Return 0 (SHOOT) to shoot. Return player index to pass.
	 * @param defenders
	 * @return
	 */
	public int action(int[] defenders){
		if(!needToPass){
			teams[0].shoot(defenders);
			return SHOOT;
		}
		else{
			needToPass = false;
			return teams[0].pass(defenders);
		}
	}
	
	private void swapTeams(){
		PreSeasonTeam teamTemp = teams[0];
		teams[0] = teams[1];
		teams[1] = teamTemp;
	}
	
	//rotates players between teams
	private void rotateTeams(){
		//rotate the teams
		int benchSize = bench.size();
		for(int i=0; i<benchSize && i<4; i++){
			bench.addLast(team1.removeFirst());
			team1.addLast(team2.removeFirst());
			team2.addLast(bench.removeFirst());		
		}
	}

	public String toString(){
		StringBuffer buffer = new StringBuffer();
		for(Player p : playerList){
			buffer.append(p + "\n");
		}
		return buffer.toString();
	}
	
	public void printTeams(){
		System.out.println("\nteam1:");
		for(Player p : team1.asList()){
			System.out.println(p);
		}
		System.out.println("\nteam2:");
		for(Player p : team2.asList()){
			System.out.println(p);
		}
		System.out.println("\nbench:");
		for(Player p : bench.asList()){
			System.out.println(p);
		}		
	}
	
}