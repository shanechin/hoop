package hoop.g6;


import java.util.Comparator;

public class ShootingComparator implements Comparator<Player> {
	public int compare(Player p1, Player p2)
	{
			return p1.getAttackRate() < p2.getAttackRate() ? 1 : (p1.getAttackRate() == p2.getAttackRate()) ? 0 : -1;
	}

}