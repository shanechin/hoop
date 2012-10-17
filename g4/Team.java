package hoop.g4;

import hoop.sim.Game;
import java.util.*;


public class Team implements hoop.sim.Team
{
	public String name()
	{
		return "G4" + (version == 1 ? "" : " v" + version);
	}


	private static int versions;
	private final int version = ++versions;
	private int holder = 0;
	private boolean pass = false;
	private Random gen = new Random();

	private HashMap<String, Stats> globalStats = new HashMap<String, Stats>();
	private String opponent;
	private int gameIndex = 0;
	private int[] oppPlayers = new int[5];
	private int[] myPlayers = new int[5];

	// To track internal tournaments.
	private int[] myPlayers1 = new int[5];
	private int[] myPlayers2 = new int[5];
	private int intPickTeamCount = 0;
	private int intPickAttackCount = 0;
	private int intPickDefendCount = 0;

	String o_team; //offending team
	String d_team; //defending team
	int[] o_players;
	int[] d_players;
    int passcount=0;


	private static boolean in(int[] a, int n, int x)
	{
		for (int i = 0 ; i != n ; ++i)
			if (a[i] == x) return true;
		return false;
	}


	private static void shuffle(int[] a, Random gen)
	{
		for (int i = 0 ; i != a.length ; ++i) {
			int r = gen.nextInt(a.length - i) + i;
			int t = a[i];
			a[i] = a[r];
			a[r] = t;
		}
	}


	/* given a single round, update stats */
	private void setAttackerForStats(Game.Round curr_round, String teamA, String teamB, int[] playersA, int[] playersB)
	{
		if (curr_round.attacksA) {
			o_team = teamA;
			o_players = playersA;
			d_team = teamB;
			d_players = playersB;
		}
		else {
			o_team = teamB;
			o_players = playersA;
			d_team = teamA;
			d_players = playersB;
		}
	}

	private void setAttackerForStats(String offending_team, String defending_team, int[] offending_players, int [] defending_players) {
		o_team = offending_team;
		d_team = defending_team;
		o_players = offending_players;
		d_players = defending_players;
	}

	/* make sure global attacker variables are set, i.e., by calling setAttackerForStats, before calling updateStatFromRound */
	private void updateStatFromRound(Game.Round curr_round) {
		//System.out.println(curr_round.lastAction());
		//System.out.println("holders:");
		//for (int i=0; i<curr_round.holders().length; i++)
			//System.out.println(curr_round.holders()[i]);
		//System.out.println("defenders:");
		//for (int i=0; i<curr_round.defenders().length; i++)
			//System.out.println(curr_round.defenders()[i]);

		//System.out.print(o_team);
		//System.out.print(":");
		globalStats.get(o_team).printStats();
		int holder; //id # of holder
		int defender; //id # of defender
		int num_holders = curr_round.holders().length;
		int holder_index = -1;
		for (int i = 0; i < num_holders - 1; i++) {
			holder_index = curr_round.holders()[i] - 1;
			holder = o_players[holder_index];
			defender = curr_round.defenders()[holder_index];
			globalStats.get(o_team).incStats(Stats.StatType.PASSES_S, holder);
			globalStats.get(d_team).incStats(Stats.StatType.PASS_BLOCKS_F, defender);
		}
		holder_index = curr_round.holders()[num_holders-1] - 1;
		holder = o_players[holder_index];
		defender = curr_round.defenders()[holder_index];
		if (curr_round.lastAction() == Game.Action.SCORED) {
			globalStats.get(o_team).incStats(Stats.StatType.SHOOTS_S, holder);
			globalStats.get(d_team).incStats(Stats.StatType.SHOOT_BLOCKS_F, defender);
		}
		else if (curr_round.lastAction() == Game.Action.MISSED) {
			globalStats.get(o_team).incStats(Stats.StatType.SHOOTS_F, holder);
			globalStats.get(d_team).incStats(Stats.StatType.SHOOT_BLOCKS_S, defender);
		}
		else if (curr_round.lastAction() == Game.Action.STOLEN) {
			globalStats.get(o_team).incStats(Stats.StatType.PASSES_F, holder);
			globalStats.get(d_team).incStats(Stats.StatType.PASS_BLOCKS_S, defender);
		}
	}


	private void updateStatFromHistory(int totalPlayers, Game[] history) {
		for (int g = gameIndex; g<history.length; g++) {
			Game curr_game = history[g];
			String teamA = curr_game.teamA;
			String teamB = curr_game.teamB;
			int rounds = curr_game.rounds();
			if (!globalStats.containsKey(curr_game.teamA))
				globalStats.put(teamA, new Stats(teamA, totalPlayers));
			if (!globalStats.containsKey(curr_game.teamB))
			   globalStats.put(teamB, new Stats(teamB, totalPlayers));
			/* first round has no lastAction */
			for (int r = 0; r < rounds; r++) {
				Game.Round curr_round = curr_game.round(r);
				setAttackerForStats(curr_round, teamA, teamB, curr_game.playersA(), curr_game.playersB());
				updateStatFromRound(curr_round);
			}
			gameIndex = g;
		}
	}


	public void opponentTeam(int[] opponentPlayers)
	{
		oppPlayers = new int[opponentPlayers.length];
		System.arraycopy(opponentPlayers, 0, oppPlayers, 0, opponentPlayers.length);
	}


	private void pickRandomTeam(int totalPlayers)
	{
		int[] randomTeam = new int[10];
		int i, p;

		for (i = 0; i < 10; i++) {
			while (true) {
				p = gen.nextInt(totalPlayers) + 1;
				if (! in(randomTeam, i, p)) {
					randomTeam[i] = p;
					break;
				}
			}
		}

		System.arraycopy(randomTeam, 0, myPlayers1, 0, 5);
		System.arraycopy(randomTeam, 5, myPlayers2, 0, 5);
	}


	private int[] pickStrongTeam(int totalPlayers, int passers, int shooters, int pass_blockers, int shoot_blockers)
	{
		if (passers + shooters + pass_blockers + shoot_blockers != 5)
			return null;

		int[] strongTeam = new int[5];
		float[] passes_w = new float[totalPlayers+1];
		float[] shoots_w = new float[totalPlayers+1];
		float[] pass_blocks_w = new float[totalPlayers+1];
		float[] shoot_blocks_w = new float[totalPlayers+1];
		int i, p, added, weightIndex;

		added = 0;
		passes_w[0] = shoots_w[0] = pass_blocks_w[0] = shoot_blocks_w[0] = -1f;

		for (i = 1; i <= totalPlayers; i++) {
			passes_w[i] = globalStats.get(name()).getWeights(Stats.WeightType.PASSES_W, i);
			shoots_w[i] = globalStats.get(name()).getWeights(Stats.WeightType.SHOOTS_W, i);
			pass_blocks_w[i] = globalStats.get(name()).getWeights(Stats.WeightType.PASS_BLOCKS_W, i);
			shoot_blocks_w[i] = globalStats.get(name()).getWeights(Stats.WeightType.SHOOT_BLOCKS_W, i);
		}

		// Finding passers first, preferring those with higher weights.
		for (i = 0; i < passers; i++) {
			weightIndex = 1;
			for (p = 2; p <= totalPlayers; p++) {
				if (passes_w[p] > passes_w[weightIndex])
					weightIndex = p;
			}
			strongTeam[added] = weightIndex;
			passes_w[weightIndex] = shoots_w[weightIndex] = pass_blocks_w[weightIndex] = shoot_blocks_w[weightIndex] = -1f;
			added++;
		}

		// Finding shooters second, preferring those with higher weights.
		for (i = 0; i < shooters; i++) {
			weightIndex = 1;
			for (p = 2; p <= totalPlayers; p++) {
				if (shoots_w[p] > shoots_w[weightIndex])
					weightIndex = p;
			}
			strongTeam[added] = weightIndex;
			passes_w[weightIndex] = shoots_w[weightIndex] = pass_blocks_w[weightIndex] = shoot_blocks_w[weightIndex] = -1f;
			added++;
		}

		// Finding pass blockers third, preferring those with lower weights.
		for (i = 0; i < pass_blockers; i++) {
			weightIndex = 1;
			for (p = 2; p <= totalPlayers; p++) {
				if (pass_blocks_w[p] < pass_blocks_w[weightIndex])
					weightIndex = p;
			}
			strongTeam[added] = weightIndex;
			passes_w[weightIndex] = shoots_w[weightIndex] = pass_blocks_w[weightIndex] = shoot_blocks_w[weightIndex] = 1f;
			added++;
		}

		// Finding shoot blockers fourth,, preferring those with lower weights.
		for (i = 0; i < shoot_blockers; i++) {
			weightIndex = 1;
			for (p = 2; p <= totalPlayers; p++) {
				if (shoot_blocks_w[p] > shoot_blocks_w[weightIndex])
					weightIndex = p;
			}
			strongTeam[added] = weightIndex;
			passes_w[weightIndex] = shoots_w[weightIndex] = pass_blocks_w[weightIndex] = shoot_blocks_w[weightIndex] = 1f;
			added++;
		}

		return strongTeam;
	}


	public int[] pickTeam(String opponent, int totalPlayers, Game[] history)
	{
		this.opponent = opponent;
		updateStatFromHistory(totalPlayers, history);

		if (!globalStats.containsKey(name()))
			globalStats.put(name(), new Stats(name(), totalPlayers));
		if (!globalStats.containsKey(opponent))
			globalStats.put(opponent, new Stats(opponent, totalPlayers));

		int[] result = new int [5];

		if (!opponent.equals(name())) {
			pickStrongTeam(totalPlayers, 0, 5, 0, 0);
			System.arraycopy(myPlayers1, 0, result, 0, 5);
			System.arraycopy(result, 0, myPlayers, 0, 5);
		} else {
			if (intPickTeamCount++ % 2 == 0) {
				pickRandomTeam(totalPlayers);
				System.arraycopy(myPlayers1, 0, result, 0, 5);
			} else {
				System.arraycopy(myPlayers2, 0, result, 0, 5);
			}
			return result;
		}

		//return result;
		return findBestShooters(5, totalPlayers);
	}


	/*public int pickAttack(int yourScore, int opponentScore, Game.Round previousRound)
	{
		
		holder = gen.nextInt(5) + 1;
		pass = true;
		return holder;
	}*/

	/* TO DO: 
	1. update stats with previousRound 
	2. return own player with highest pass percentages */
	public int pickAttack(int yourScore, int opponentScore, Game.Round previousRound)
	{
		if (opponent.equals(name())) {
			if (intPickAttackCount++ % 2 == 0)
				System.arraycopy(myPlayers1, 0, myPlayers, 0, 5);
			else
				System.arraycopy(myPlayers2, 0, myPlayers, 0, 5);
		}

		if(previousRound != null)
        {
				updateStatFromRound(previousRound);
        }
        pass = true;   
        setAttackerForStats(name(), opponent, myPlayers, oppPlayers);
		int myHolder = findBestPasser();
		return myHolder;
	}
   
    public int findBestPasser()
    {
        int pass_s = 0;		//number of successful pass
        int passfail = 0;	// number of pass fail
        float bestpass = 0;	//percentage of successful pass
        int bestpasser = 1;	//ID of best passer
        float percentage=0;
        int totalpass =0;


        for(int i = 1; i < 6; i++) {
                pass_s = globalStats.get(name()).getStats(Stats.StatType.PASSES_S, myPlayers[i-1]);
                //System.out.print("pass" +pass_s);
            
                passfail = globalStats.get(name()).getStats(Stats.StatType.PASSES_F, myPlayers[i-1]);
                //System.out.print("fails" +passfail );
                totalpass = pass_s + passfail;
                if(totalpass != 0)
                percentage = pass_s/(float)(totalpass);

                if(percentage > bestpass)
                {
                    bestpass = percentage;
                    bestpasser = i;
                }
        }

        return bestpasser;
    }

    public int[] findBestShooters(int num_shooters, int totalPlayers) {
    	ArrayList<Integer> shooters = new ArrayList<Integer>(num_shooters);
    	float shoot_percent = 0;
    	int[] shooter_array = new int[num_shooters];
    	for (int i = 1; i <= totalPlayers; i++) {
    		shoot_percent = globalStats.get(name()).getWeights(Stats.WeightType.SHOOTS_W, i) + (1 - globalStats.get(name()).getWeights(Stats.WeightType.SHOOT_BLOCKS_W, i));
			for (int j = 0; j < num_shooters; j++) {
				//System.out.print(shooters.size());
				if (shooters.size() > j) {
					float prev = globalStats.get(name()).getWeights(Stats.WeightType.SHOOTS_W, shooters.get(j))+ (1 - globalStats.get(name()).getWeights(Stats.WeightType.SHOOT_BLOCKS_W, shooters.get(j)));
					if (shoot_percent > prev) {
						shooters.add(j, i);
						break;
					}
				}
				else if (shooters.size() < num_shooters) { //fix so first 5 go in.
						shooters.add(shooters.size(), i);
						break;
				}
			}
		}
		for (int i = 0; i < num_shooters; i++) {
			shooter_array[i] = shooters.get(i);
		}
		return shooter_array;
    }


	/* TO DO:
	1. return own player with highest (own shot percentage + (1 - defender's shot-block percentage)) */
	public int action(int[] defenders)
	{
        if(pass==true)
        {passcount++;}
		if (!pass)
           return 0;
		int nextholder = findMaxMissMatch(defenders);
        
        //if the current holder has maximum missmatch
        //then pass randomly
        if(holder==nextholder)
        {
          
            if(holder==5)
                holder =1;
            else
                holder=holder+1;
            if(passcount<3)
            pass =true;
            else
            {
                passcount=0;
                pass=false;}
        }
        else
        {//pass to next holder have maximum missmatch
            //System.out.println("maxID " + holder+" vs "+nextholder);
            holder=nextholder;
            passcount=0;
            pass = false;
        }
        return holder;
        /*
        for(int i=0;i<defenders.length;i++)
        {
            System.out.println(defenders[i]);
        }
        System.out.println("end round");*/
	}
    
    //find maximum missmatch
    public int findMaxMissMatch(int[] defenders)
    {
        int maxID =1;
        float weight=0;
        float maxweight=0;
        for(int i=0;i<defenders.length;i++)
        {
            //System.out.println(defenders[i]);
            weight=globalStats.get(opponent).getWeights(Stats.WeightType.SHOOTS_W, oppPlayers[defenders[i]-1])+
            globalStats.get(name()).getWeights(Stats.WeightType.SHOOT_BLOCKS_W, myPlayers[i]);
            
           // System.out.println("defen "+globalStats.get(opponent).getWeights(Stats.WeightType.SHOOTS_W, oppPlayers[defenders[i]-1]));
            //System.out.println("us "+globalStats.get(name()).getWeights(Stats.WeightType.SHOOT_BLOCKS_W, myPlayers[i]));
            
          if(weight>maxweight)
          {
              maxweight = weight;
              maxID=i+1;
          }
        }
        return maxID;
    }
    
	/* TO DO: 
	1. update stats with previousRound 
	2. place best pass blocker in front of holder
	3. place best shot defender in front of opponent's best shooter, repeat with 2nd best, etc. */
	public int[] pickDefend(int yourScore, int oppScore, int holder, Game.Round previousRound)
	{
        if(previousRound != null)
        {
            updateStatFromRound(previousRound);
        }

        setAttackerForStats(opponent, name(), oppPlayers, myPlayers);

		if(previousRound != null) {
			updateStatFromRound(previousRound);
		}
        
		int[] defend = new int [5];
        
		int id = findBestMember(name(),Stats.WeightType.PASS_BLOCKS_W, myPlayers);
        //System.out.println(id);
        defend[holder-1]=id;

        int[] newmyplayer = new int[5];
        int[] newmyopp = new int[5];
		System.arraycopy(myPlayers, 0, newmyplayer, 0, 5);
        System.arraycopy(oppPlayers, 0, newmyopp, 0, 5);
        
        newmyopp[holder-1]=0;
        newmyplayer[id-1]=0;
       
        for(int i=0;i<5;i++)
        {
            int id1 = findBestMember(opponent,Stats.WeightType.SHOOTS_W, newmyopp);
            int id2 = findBestMember(name(),Stats.WeightType.SHOOT_BLOCKS_W, newmyplayer);
            defend[id1-1]=id2;
            newmyopp[id1-1]=0;
            newmyplayer[id2-1]=0;
        }
        
      // for(int i=0;i<5;i++)
            //System.out.println(defend[i]);
		return defend;
	}
    
      
    public int findBestMember(String teamname,Stats.WeightType actiontype, int[] member)
    {
        float percent =0;
        float maxPercent=0;
        int id=1;
        if((actiontype==Stats.WeightType.SHOOTS_W)||(actiontype==Stats.WeightType.PASSES_W))
    	for (int i = 0; i <member.length; i++)
        {//System.out.println("one");
            if(member[i]!=0)
            {
    		percent = globalStats.get(teamname).getWeights(actiontype, member[i]);
            if(percent>=maxPercent)
            {
                maxPercent=percent;
                id = i+1;
            }
            }
        }
        else
        {//System.out.println("two");
            maxPercent=1;
            percent=0;
            for (int i = 0; i <member.length; i++)
            {
                if(member[i]!=0)
                {
                percent = globalStats.get(teamname).getWeights(actiontype, member[i]);
                if(percent<=maxPercent)
                {
                    maxPercent=percent;
                    id = i+1;
                }
                }
            }
        }
        //System.out.println(id);
        return id;
    }
}
