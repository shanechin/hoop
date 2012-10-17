package hoop.g2;

import java.util.ArrayList;
import java.util.List;

public class TeamStat {
	private List<Player> players;
	private String name;
	
	public TeamStat(String teamname, int playerCount) {
		players = new ArrayList<Player>(playerCount);
		for (int i = 1; i <= playerCount; i++) {
			players.add(new Player(i));
		}
		name = teamname; 
	}
	
	public String getTeamName() {
		return name;
	}
	
	public List<Player> getPlayers() {
		return players;
	}
	
	public Player getPlayer(int id) {
		return players.get(id-1);
	}
	
	public double getPlayerShotPercentage(int id) {
		return players.get(id).getShotPercent();
	}

	public double getPlayerBlockPercentage(int id) {
		return players.get(id).getBlockPercent();
	}
	
	public double getPlayerPassPercentage(int id) {
		return players.get(id).getPassPercent();
	}
	
	public double getPlayerStealPercentage(int id) {
		return players.get(id).getStealPercent();
	}
}
