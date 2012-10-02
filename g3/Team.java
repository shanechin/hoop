package hoop.g3;

import hoop.sim.Game;
import java.util.*;

public class Team implements hoop.sim.Team {
  
  public String name()
  {
    return "Group 2";
  }
  
  /* return 5 players in 1,2,3,4,...,P where P total players */
  public int[] pickTeam(String opponent, int totalPlayers, Game[] history) {
  }
  
  /* get players of opponent team */
  public void opponentTeam(int[] opponentPlayers) {
  }
  
  /* return one of 1,2,3,4,5 to pick initial ball holder */
  public int pickAttack(int yourScore, int opponentScore) {
  }
  
  /* if 0 then shoot, otherwise pass to 1,2,3,4,5 */
  public int action(int[] defenders) {
  }
  
  /* pick defenders, use 1,2,3,4,5 for players */
  public int[] pickDefend(int yourScore, int opponentScore, int ballHolder) {
  }
  
}