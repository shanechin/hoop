package hoop.gi3;

import java.util.Comparator; 

public class ShootingComparator implements Comparator<Player> {
  public int compare(Player p1, Player p2) {
    if (p1.shooting() < p2.shooting())
      return 1;
    else if (p1.shooting() > p2.shooting())
      return -1;
    else
      return 0;
  }
}