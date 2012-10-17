package hoop.g5;

import hoop.sim.Game;
import hoop.sim.Hoop;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.Set;
import javax.swing.JOptionPane;

public class Team implements hoop.sim.Team {
   

        public String name()
	{
		return "Group 5" + (version == 1 ? "" : " v" + version);
	}

	private static int versions;
	private final int version = ++versions;
	private int[] last = null;
	private int holder = 0;
	private boolean pass = false;
	private Random gen = new Random();

        //Tianchen variables        
        private boolean selfGame=false; 
        private int trainingTurn=0; //the current turn in a training game
        private int countPickTeam=0; //the number of times calling pickteam 
        private boolean selfEstimated=false; //whether we have estimated our own players' stats
        private int totalNumberOfPlayers=12;
        private int[][] selfPlayerSuccessActionCount= new int [totalNumberOfPlayers][4];
        private int[][] selfPlayerActionCount=new int [totalNumberOfPlayers][4];
        private double[][] selfPlayerStats=new double [totalNumberOfPlayers][4];
        
        /* Jeremys variables */
        private double[][] OpponentStat=new double [totalNumberOfPlayers][4];
        
        private int [] current_defenders; 
        private int [] ourTeam;
        private int [] opponentTeam;
        private int ourShooter;
        private int ourPasser;
        final int SHOOTING = 0;
        final int BLOCKING = 1;
        final int PASSING = 2;
        final int INTERCEPTING = 3;

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

	public void opponentTeam(int[] opponentPlayers) {}

        String curOpponent;
	public int[] pickTeam(String opponent, int totalPlayers, Game[] history)
	{	
                curOpponent = opponent;
                if(opponentStatsMap.get(opponent) == null)
                    OpponentStat = null;
                else
                    OpponentStat = opponentStatsMap.get(opponent);
	
                totalNumberOfPlayers=totalPlayers;

                // IF in training (i.e. playing against self)
                if (opponent.equals(name()))
                {                  
                    trainingTurn=0;
                    selfGame=true;
                    //System.out.println("inpickteam training");
                    return pickTrainingTeam(totalPlayers);
                    //last = null;
                }
                // ELSE if in normal games (playing against others)
                else{
                    // estimate our players stats at end of training
                    //   should activate once at beginning of normal season
		    if(!selfEstimated)
                    {
                         estimateTrainingStats(history);
                         selfEstimated=true;                
                    }
                    selfGame=false;

                    /* in the tournament */
                    
                    // Update opposing stats from history
                    //     important for getting info from games we weren't a part of
                    //    (own and direct-opponent stats updated continuously during play)
                    updateOpponentStats(history);

                    // Conclude pre-processing and pick next team to play
                    int [] team = getBestShooters();
                    ourTeam = team;
                    return ourTeam;
		}
        }

	public int pickAttack(int yourScore, int opponentScore, Game.Round previousRound)
	{
                if(selfGame)
                {
                    return pickTrainingAttack();
                }
                
                /* in the tournament */


                //update stats from previous round
                if(previousRound != null)
		{
		    // IF last round opponent attacked and A attacked, so opponent -> A
		    if(previousRound.attacksA)
			updateStatsFromRound(previousRound, curOpponent, this.name());

                    // ELSE //previousRound.attacksB  //last round opponent attacked and B attacked, so opponent -> B
                    else
                        updateStatsFromRound(previousRound, this.name(), curOpponent);
                }
                


                if(OpponentStat != null)
                {
                    /* see who was defending us and determine 
                     the best miss match */
                    //ourShooter = pickShooter(1,ourTeam);
                    ourPasser = pickPasser(0);
                    pass = true;
                    return ourPasser+1;
                }
                /* first time we play this team we use our best overall
                 players */

                ourShooter = pickShooter(0);
                ourPasser = pickPasser(0);
                
                pass = true;
                return ourPasser+1;
	}

	public int action(int[] defenders)
	{
                if(selfGame)
                {
                    return trainingAction();
                }
                current_defenders = defenders;
                if(OpponentStat != null)
                    ourShooter = 0;
                
                if (!pass) return 0;
		pass = false;
		return ourShooter+1;
	}

	public int[] pickDefend(int yourScore, int oppScore, int holder, Game.Round previousRound)
	{
                if(previousRound != null)
                    opponentTeam = previousRound.defenders();
                if (selfGame)
                {                  
                    return pickTrainingDefend();     
                }
            
            //update stats from previous round
            if(previousRound != null)
	    {
		// IF last round opponent attacked and A attacked, so opponent -> A
		if(previousRound.attacksA)
		    updateStatsFromRound(previousRound, curOpponent, this.name());

                // ELSE //previousRound.attacksB  //last round opponent attacked and B attacked, so opponent -> B
                else
                    updateStatsFromRound(previousRound, this.name(), curOpponent);
            }
            if(OpponentStat == null)
            {
                int[] defenders = new int [] {1,2,3,4,5};
                shuffle(defenders, gen);
                return defenders;
            }
	    return getDefensiveLineup();
	}

        int pickTrainingTeamCount=0;

        private int[] pickTrainingTeam(int totalPlayers) 
        {
//             //teamA remains the same for every 15 games, teamB changes every game.
//            boolean isTeamA = (pickTrainingTeamCount%2)==0 ? true : false;  
//            int trainingGameCount=pickTrainingTeamCount/2;
//            trainingGameCount=trainingGameCount%45;
//            pickTrainingTeamCount++;
//            if(isTeamA)
//            {
//                 System.out.println("teamA");
//                switch(trainingGameCount)
//                {
//                    case 0:  return last=new int[] {1,2,3,4,5};
//                    case 15: return last=new int[] {6,7,8,9,10};
//                    case 30: return last=new int[] {11,12,1,2,3};
//                    default: if(trainingGameCount>0 && trainingGameCount<15)
//                                return last=new int[] {1,2,3,4,5};
//                             if(trainingGameCount>15 && trainingGameCount<30)
//                                return last=new int[] {6,7,8,9,10};
//                             if(trainingGameCount>30)
//                                return last=new int[] {11,12,1,2,3};                       
//                }                          
//            }
//            
//            //if teamB
//            int[][] hardcodedTeamB=new int[][]
//            {{1,2,3,4,5}, {2,3,4,5,1},
//            {3,4,5,1,2},  {4,5,1,2,3},
//            {5,1,2,3,4},  {6,7,8,9,10},
//            {7,8,9,10,6},  {8,9,10,6,7},
//            {9,10,6,7,8}, {10,6,7,8,9},
//            {11,12,1,2,3}, {12,1,2,3,11},
//            {1,2,3,11,12},{2,3,11,12,1},
//            {3,11,12,1,2}};
//
//            int innerCount=trainingGameCount%15;
//            System.out.println("innerCount"+innerCount);
//            return last=hardcodedTeamB[innerCount];           	
            
//            int totalCount=0; 
//            for(int i=0; i<selectCount.length; i++)
//            {
//                totalCount+=selectCount[i];                
//            }
            
            //the average number of time when a player got selected into the training team
//            double averageCount=totalCount/selectCount.length;  
            
            
            int lastLen = last == null ? 0 : last.length;
		int[] result = new int [5];
		for (int i = 0 ; i != 5 ; ++i) {
			int x = gen.nextInt(totalPlayers) + 1;
			
                        if (in(last, lastLen, x)) 
                        {   if(countPickTeam%2==0) //if it's the second team
                            {    
                                i--;
                                continue;
                            }
                        }
                        if( in(result, i, x))
                            i--;                             
			else 
                        {
//                            if(selectCount[x-1]>averageCount)
//                            {
//                                int modifier=(int)(selectCount[x-1]/averageCount);
//                                int z=gen.nextInt(2);
//                                
//                            }
                            result[i] = x;
                            //selectCount[x-1]+=1;
                        }
		}
		last = result;
		return result;
        }

    private int[] pickTrainingDefend() {
        int[] defenders = new int [] {1,2,3,4,5};
		shuffle(defenders, gen);
		return defenders;
    }

    private int pickTrainingAttack() {
        
       // System.out.println("gameTurns!!:"+trainingTurn);
        holder=((trainingTurn)/2)%5+1;
        pass = true;
        trainingTurn++;
        return holder;
    }

    private int trainingAction() {
            if (!pass) return 0;
         
            //always pass to the next player, and the next player will shoot
            holder = (holder)%5+1; 
            pass = false; 
            
            return holder;
    }

    private void estimateTrainingStats(Game[] history) 
    {
        
        for(Game g: history)
        {    for(int i=0; i<g.rounds() ;i++ )
            {
                Game.Round r=g.round(i);
                int[] holders=r.holders();
                
                    for(int h=0; h<holders.length; h++)
                    {
                        int len=holders.length;
                        int player;
                        int OpponentPlayer;
                        
                        if(r.attacksA)
                        {                          
                            player=g.playersA()[holders[h]-1]-1;
                            OpponentPlayer=g.playersB()[holders[h]-1]-1;
                        }
                        else
                        {
                            player=g.playersB()[holders[h]-1]-1;
                            OpponentPlayer=g.playersA()[holders[h]-1]-1;
                        }
                        
                        //if reached the last holder
                        if ((h+1)==len)
                        {
                            switch(r.lastAction())
                            {
                                case MISSED: 
                                    selfPlayerActionCount[player][0]++;
                                    selfPlayerSuccessActionCount[OpponentPlayer][1]++;
                                    selfPlayerActionCount[OpponentPlayer][1]++; 
                                    break;
                                case SCORED:
                                    selfPlayerActionCount[player][0]++;
                                    selfPlayerSuccessActionCount[player][0]++;
                                    selfPlayerActionCount[OpponentPlayer][1]++; 
                                    break;
                                case STOLEN:
                                    selfPlayerActionCount[player][2]++;
                                    selfPlayerSuccessActionCount[OpponentPlayer][3]++;
                                    selfPlayerActionCount[OpponentPlayer][3]++; 
                                    break;
                            }
                        }
                        
                        //if he is not the last holder, which means he passed the ball
                        else
                        {
                             selfPlayerActionCount[player][2]++;
                             selfPlayerSuccessActionCount[player][2]++;
                             selfPlayerActionCount[OpponentPlayer][3]++;
                        }        
                }
            }
        }
        
        // assume the average strength is 0.5
        double aveBlock=0.5;
        double aveIntercept=0.5;
        double aveShoot=0.5;
        double avePass=0.5;       
         
        double sumBlock=0;
        double sumIntercept=0;
        double sumShoot=0;
        double sumPass=0;
        int countBlock=0;
        int countIntercept=0;
        int countShoot=0;
        int countPass=0;

        for(int i=0; i<totalNumberOfPlayers; i++) //calculate the stats
        {    
            for(int j=0; j<4; j++)
            {
                double prob=0;
                if(selfPlayerActionCount[i][j]!=0)
                     prob=((double)selfPlayerSuccessActionCount[i][j])/selfPlayerActionCount[i][j];
                       //System.out.println("RAWWWselfPlayerStats["+i+"]"+"["+j+"]"+temp);
                switch(j)
                {
                    case BLOCKING: 
                        selfPlayerStats[i][j]=2*(1-prob)-aveShoot; //assume the average strength of shooting is 0.5
                       // System.out.println("selfPlayerStats["+i+"]"+"["+j+"]"+selfPlayerStats[i][j]);
                        break;
                    case INTERCEPTING:
                        selfPlayerStats[i][j]=10*(1-prob)-8-avePass; //assume the average strength of passing is 0.5
                      //  System.out.println("selfPlayerStats["+i+"]"+"["+j+"]"+selfPlayerStats[i][j]);
                        break;                        
                    case SHOOTING: 
                        selfPlayerStats[i][j]=2*(prob)-aveBlock; //assume the average strength of shooting is 0.5
                        //System.out.println("selfPlayerStats["+i+"]"+"["+j+"]"+selfPlayerStats[i][j]);
                        break;
                    case PASSING:
                        selfPlayerStats[i][j]=10*(prob)-8-aveIntercept; //assume the average strength of passing is 0.5 
                        //System.out.println("selfPlayerStats["+i+"]"+"["+j+"]"+selfPlayerStats[i][j]);
                        break;
                }
                   // System.out.println(selfPlayerStats[i][j]);
            
            }
        }

         //calculate the average strength of each stats 
            for(int l=0; l<totalNumberOfPlayers; l++)
            {    
                if(selfPlayerActionCount[l][BLOCKING]!=0){
                    sumBlock+=selfPlayerStats[l][BLOCKING];
                    countBlock++;
                }           
                if(selfPlayerActionCount[l][INTERCEPTING]!=0){
                    sumIntercept+=selfPlayerStats[l][INTERCEPTING];
                    countIntercept++;
                }
                if(selfPlayerActionCount[l][SHOOTING]!=0){
                    sumShoot+=selfPlayerStats[l][SHOOTING];
                    countShoot++;
                }           
                if(selfPlayerActionCount[l][PASSING]!=0){
                    sumPass+=selfPlayerStats[l][PASSING];
                    countPass++;
                }
            }
            /*
            aveBlock=sumBlock/countBlock;
            aveIntercept=sumIntercept/countIntercept;
            aveShoot=sumShoot/countShoot;
            avePass=sumPass/countPass;
            *
            */
            

        //make data valid ( 0 <= stat <= 1 )
        for(int i=0; i<totalNumberOfPlayers; i++)
        {
            for(int j=0; j<4; j++)
            {
                 if(selfPlayerStats[i][j]>1)
                      selfPlayerStats[i][j]=1;
                 if(selfPlayerStats[i][j]<0)
                      selfPlayerStats[i][j]=0;
            }             
        }
    }

//* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * BEGIN NEW BLOCK FOR OPPONENT STATS
    // Store action counts and estimated stats for opponents
    private HashMap<String,int[][]> opponentSuccessActionMap = new HashMap<String,int[][]>();
    private HashMap<String,int[][]> opponentActionMap = new HashMap<String,int[][]>();
    private HashMap<String,double[][]> opponentStatsMap = new HashMap<String,double[][]>();

    // Update stats by vewing games we didn't play in
    HashMap<Game,Boolean> gamesProcessed = new HashMap<Game,Boolean>();

    private void updateOpponentStats(Game[] history) {
        final String MY_NAME = name();
        //for each game 
        for(Game g: history)
        {
            int[][] emptySized1 = new int[totalNumberOfPlayers][4];
            int[][] emptySized2 = new int[totalNumberOfPlayers][4];
            double[][] emptySized3 = new double[totalNumberOfPlayers][4];
            int[][] emptySized4 = new int[totalNumberOfPlayers][4];
            int[][] emptySized5 = new int[totalNumberOfPlayers][4];
            double[][] emptySized6 = new double[totalNumberOfPlayers][4];

            //initialize arrays if hashmap arrays are there yet
            if(!opponentSuccessActionMap.containsKey(g.teamA))
            {
                opponentSuccessActionMap.put (g.teamA, emptySized1);
                opponentActionMap.put        (g.teamA, emptySized2);
                opponentStatsMap.put         (g.teamA, emptySized3);
            }

            //initialize arrays if hashmap arrays are there yet
            if(!opponentSuccessActionMap.containsKey(g.teamB))
            {
                opponentSuccessActionMap.put (g.teamB, emptySized4);
                opponentActionMap.put        (g.teamB, emptySized5);
                opponentStatsMap.put         (g.teamB, emptySized6);
            }

            //Process each game that has not been processed
            //  And that we didn't play in
            if( !gamesProcessed.containsKey(g) && !g.teamA.equals(MY_NAME) && !g.teamB.equals(MY_NAME) )
            {
                //flag game as processed (assuming body completes succesfully)
                gamesProcessed.put(g, true);

		//for each round 
		for(int i=0; i<g.rounds() ;i++ )
		{
		    Game.Round r=g.round(i);
		    int[] holders=r.holders();
		    
		    //for each holder (attacking team)
		    for(int h=0; h<holders.length; h++)
		    {
		        int len=holders.length;
		        int player;
		        int OpponentPlayer;
                        String attackTeam;
                        String defendTeam;
		        
		        //get matchup by determining which team is attacking and which is defending
		        player=         ((r.attacksA) ? g.playersA() : g.playersB()) [holders[h]-1]-1;
		    	OpponentPlayer= ((r.attacksB) ? g.playersA() : g.playersB()) [holders[h]-1]-1;

                        attackTeam=     (r.attacksA) ? g.teamA : g.teamB;
                        defendTeam=     (r.attacksB) ? g.teamA : g.teamB;

		        //if reached the last holder
		        if ((h+1)==len)
		        {
		    	  //process last action and update stats of players in matchup
		    	  switch(r.lastAction())
		    	  {
		    	    case MISSED:
                                opponentActionMap            .get(attackTeam)[player][0]++;
                                opponentSuccessActionMap     .get(defendTeam)[OpponentPlayer][1]++;
                                opponentActionMap            .get(defendTeam)[OpponentPlayer][1]++;

		    		//selfPlayerActionCount        [player][0]++;
		    		//selfPlayerSuccessActionCount [OpponentPlayer][1]++;
		    		//selfPlayerActionCount        [OpponentPlayer][1]++; 
		    		break;
		    	    case SCORED:
                                opponentActionMap            .get(attackTeam)[player][0]++;
                                opponentSuccessActionMap     .get(attackTeam)[player][0]++;
                                opponentActionMap            .get(defendTeam)[OpponentPlayer][1]++;
                                break;
		    		//selfPlayerActionCount        [player][0]++;
		    		//selfPlayerSuccessActionCount [player][0]++;
		    		//selfPlayerActionCount        [OpponentPlayer][1]++; 
		    	    case STOLEN:
                                opponentActionMap            .get(attackTeam)[player][2]++;
                                opponentSuccessActionMap     .get(defendTeam)[OpponentPlayer][3]++;
                                opponentActionMap            .get(defendTeam)[OpponentPlayer][3]++;
                                break;
		    		//selfPlayerActionCount        [player][2]++;
		    		//selfPlayerSuccessActionCount [OpponentPlayer][3]++;
		    		//selfPlayerActionCount        [OpponentPlayer][3]++;
		    	//  case PASSED:
		    	//      can't happen because looking at last holder                              
		    	  }
		        }
		        
		        //He is not the last holder, which means he passed the ball
		        else
		        {
			    opponentActionMap            .get(attackTeam)[player][2]++;
                            opponentSuccessActionMap     .get(attackTeam)[player][2]++;
                            opponentActionMap            .get(defendTeam)[OpponentPlayer][3]++;

	    		    //selfPlayerActionCount        [player][2]++;
	    		    //selfPlayerSuccessActionCount [player][2]++;
	    		    //selfPlayerActionCount        [OpponentPlayer][3]++;
                        }
                   }  
		
		}
	    
            }
        }
        //re-calc stats of all opponents using new data by dividing (# successes) / (# actions)
        recalcOpponentStats();
    }

    public void recalcOpponentStats()
    {
	Set<String> allOpponents = opponentActionMap.keySet();

	for(String s: allOpponents)
	{
	    for(int i=0; i<totalNumberOfPlayers; i++)
	    {    
		for(int j=0; j<4; j++)
		{
		    if(opponentActionMap.get(s)[i][j]!=0)
		    {
			int successes = opponentSuccessActionMap.get(s)[i][j];
			int actions  = opponentActionMap.get(s)[i][j];

			opponentStatsMap.get(s)[i][j]=((double)successes)/actions;


                        String info = "";
                        info += ("Team: "+s+",  Player#: "+i+",  Stat#: "+j+",  StatVal: ");
			info += (opponentStatsMap.get(s)[i][j]);

                        //System.err.println(info + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        //JOptionPane.showMessageDialog(null,info);
		    }
		}	
	    }
	}
    }

    public void recalcAllStats()
    {
        //determine stat at selfPlayerStats[i][j] by dividing (# successes) / (# actions)
        for(int i=0; i<totalNumberOfPlayers; i++)
        {    
            for(int j=0; j<4; j++)
            {
                if(selfPlayerActionCount[i][j]!=0)
                    selfPlayerStats[i][j]=((double)selfPlayerSuccessActionCount[i][j])/selfPlayerActionCount[i][j];
                   // System.out.println(selfPlayerStats[i][j]);
            }
        }
        
        recalcOpponentStats();
    }

    public void updateStatsFromRound(Game.Round r, String teamA, String teamB)
    {
	int[] holders=r.holders();

        //TODO: FIX!!!, ugly as hell
	int[][] emptySized1 = new int[totalNumberOfPlayers][4];
	int[][] emptySized2 = new int[totalNumberOfPlayers][4];
	double[][] emptySized3 = new double[totalNumberOfPlayers][4];
	int[][] emptySized4 = new int[totalNumberOfPlayers][4];
	int[][] emptySized5 = new int[totalNumberOfPlayers][4];
	double[][] emptySized6 = new double[totalNumberOfPlayers][4];

	//initialize arrays if hashmap arrays are there yet
	if(!opponentSuccessActionMap.containsKey(teamA))
	{
	    opponentSuccessActionMap.put (teamA, emptySized1);
	    opponentActionMap.put        (teamA, emptySized2);
	    opponentStatsMap.put         (teamA, emptySized3);
	}

	//initialize arrays if hashmap arrays are there yet
	if(!opponentSuccessActionMap.containsKey(teamB))
	{
	    opponentSuccessActionMap.put (teamB, emptySized4);
	    opponentActionMap.put        (teamB, emptySized5);
	    opponentStatsMap.put         (teamB, emptySized6);
	}

	
	//for each holder (attacking team)
	for(int h=0; h<holders.length; h++)
	{
	    int len=holders.length;
	    int player;
	    int OpponentPlayer;
	    String attackTeam;
	    String defendTeam;
	    
	    //get matchup by determining which team is attacking and which is defending
	    player=         holders[h];
	    OpponentPlayer= r.defenders()[holders[h]-1];

	    attackTeam=     (r.attacksA) ? teamA : teamB;
	    defendTeam=     (r.attacksB) ? teamA : teamB;

	    //if reached the last holder
	    if ((h+1)==len)
	    {
	      //process last action and update stats of players in matchup
	      switch(r.lastAction())
	      {
		case MISSED:
                    if(attackTeam.equals(this.name()))
                    {
		        selfPlayerActionCount        [player][0]++;
		        opponentSuccessActionMap     .get(defendTeam)[OpponentPlayer][1]++;
		        opponentActionMap            .get(defendTeam)[OpponentPlayer][1]++;
                    }
                    else if(defendTeam.equals(this.name()))
                    {
                    	opponentActionMap            .get(defendTeam)[player][0]++;
		        selfPlayerSuccessActionCount [OpponentPlayer][1]++;
		        selfPlayerActionCount        [OpponentPlayer][1]++;
                    }
		    break;
		case SCORED:
                    if(attackTeam.equals(this.name()))
                    {
		        selfPlayerActionCount        [player][0]++;
		        selfPlayerSuccessActionCount [player][0]++;
		        opponentActionMap            .get(defendTeam)[OpponentPlayer][1]++;
                    }
                    else if(defendTeam.equals(this.name()))
                    {
                    	opponentActionMap            .get(attackTeam)[player][0]++;
		        opponentSuccessActionMap     .get(attackTeam)[player][0]++;
		        selfPlayerActionCount        [OpponentPlayer][1]++;
                    }
		    break;
		case STOLEN:
                    if(attackTeam.equals(this.name()))
                    {
		        selfPlayerActionCount        [player][2]++;
		        opponentSuccessActionMap     .get(defendTeam)[OpponentPlayer][3]++;
		        opponentActionMap            .get(defendTeam)[OpponentPlayer][3]++;
                    }
                    else if(defendTeam.equals(this.name()))
                    {
                    	opponentActionMap            .get(defendTeam)[player][2]++;
		        selfPlayerSuccessActionCount [OpponentPlayer][3]++;
		        selfPlayerActionCount        [OpponentPlayer][3]++;
                    }
		    break;
	    //  case PASSED:
	    //      can't happen because looking at last holder                              
	      }
	    }
	    
	    //He is not the last holder, which means he passed the ball
	    else
	    {
                    if(attackTeam.equals(this.name()))
                    {
		        selfPlayerActionCount        [player][2]++;
		        selfPlayerSuccessActionCount [player][2]++;
		        opponentActionMap            .get(defendTeam)[OpponentPlayer][3]++;
                    }
                    else if(defendTeam.equals(this.name()))
                    {
                    	opponentActionMap            .get(attackTeam)[player][2]++;
		        opponentSuccessActionMap     .get(attackTeam)[player][2]++;
		        selfPlayerActionCount        [OpponentPlayer][3]++;
                    }

		//opponentActionMap            .get(attackTeam)[player][2]++;
		//opponentSuccessActionMap     .get(attackTeam)[player][2]++;
		//opponentActionMap            .get(defendTeam)[OpponentPlayer][3]++;

		//selfPlayerActionCount        [player][2]++;
		//selfPlayerSuccessActionCount [player][2]++;
		//selfPlayerActionCount        [OpponentPlayer][3]++;
	    }
       }
       
       //IMPORTANT
       recalcAllStats();
    }
//* * * * * * * * * * * * * * * * * * * * * * * * * * * * *  * * * * * END NEW BLOCK FOR OPPONENT STATS

		
    public int[] getDefensiveLineup()
    {
        
        /* get opponents best passer */
        double max = 0;
        int opponentsBestPasser = 0;
        for(int i = 0; i < 5; i++)
        {
            double prob = OpponentStat[opponentTeam[i]-1][PASSING];
            if( prob > max)
            {
                opponentsBestPasser = i;
                max = prob;
            }
        }
        /* get a list of their 4 best shooters in order 
            excluding their best passer
        */
        int [] opponentsBestShooters = new int[4];
        ArrayList<Integer> exclude = new ArrayList<Integer>();
        exclude.add(opponentsBestPasser);
        for(int i = 0; i < 4; i++)
        {
            max = 0;
            int tempBestShooter = 0;
            for(int j = 0; j < 4; j++)
            {
                if(exclude.contains(j))
                    continue;
                double prob = OpponentStat[opponentTeam[j]-1][SHOOTING];
                if(prob > max)
                {
                    tempBestShooter = i;
                    max = prob;
                }
            }
            opponentsBestShooters[i] = tempBestShooter+1;
            exclude.add(tempBestShooter);
        }

        /* now pair our best interceptor with their best passer */
        int [] defensiveLineup = new int[5];

        /* get our best interceptor */
        max = 0;
        int ourBestInterceptor = 0;
        for(int i = 0; i < 5; i++)
        {
            double prob = selfPlayerStats[ourTeam[i]-1][INTERCEPTING];
            if(prob > max)
            {
                ourBestInterceptor = i;
                max = prob;
            }
        }
        /* pair their best passer with our best interceptor */
        defensiveLineup[opponentsBestPasser] = ourBestInterceptor;


        /* get our top 4 blockers excluding our best 
            * interceptor
            */

        ArrayList<Integer> exclude2 = new ArrayList<Integer>();
        int [] ourBestBlockers = new int[4];
        exclude2.add(ourBestInterceptor);
        for(int i = 0; i < 4; i++)
        {
            double min = 10000;
            int tempBestBlocker = 0;
            for(int j = 0; j < 4; j++)
            {
                if(exclude.contains(j))
                    continue;
                double prob = selfPlayerStats[ourTeam[i]-1][BLOCKING];
                if(prob < min)
                {
                    tempBestBlocker = i;
                    min = prob;
                }
            }
            ourBestBlockers[i] = tempBestBlocker;
            exclude2.add(tempBestBlocker);
        }


        /* match our  blockers against their shooters */
        for(int i = 0; i < 4; i++)
        {
            if(i == opponentsBestPasser)
                continue;
            defensiveLineup[opponentsBestShooters[i]] = ourBestBlockers[i];
        }
        for(int i =0; i < 5; i++)
            defensiveLineup[i] = defensiveLineup[i]+1;

        return defensiveLineup;
    }       

    public int pickShooter(int type)
    {
        if(type == 0)
        {
            /* pick our best shooter */
            double max = 0;
            int bestShooter = 0;
            for(int i = 0; i < 5; i++)
            {
                double prob = selfPlayerStats[ourTeam[i]-1][SHOOTING];
                if(prob > max)
                {
                    bestShooter = i;
                    max = prob;
                }
            }
            return bestShooter;        
        }
        /* based on the previous defensive lineup, pick our next
            * shooter with the best miss-match against a defensive 
            * player
            */
        int shooter = 0;
        double max_prob = 0;
        for(int i = 0; i < 5; i++)
        {
            /* selfPlayerStats only contains the states of the players
                * we selected for this game 
                */
            if(i == ourPasser)
                continue;
            double prob = selfPlayerStats[ourTeam[i]-1][SHOOTING] + OpponentStat[current_defenders[i]-1][BLOCKING];
            if(prob > max_prob)
            {
                shooter = i;
                max_prob = prob;
            }
        }
        return shooter;
    }
    /* get the best passer that not next player we decide is going to shoot */
    public int pickPasser(int type)
    {
        int passer = 0;
        if(type == 0)
        {
            /* pick our best passer */
            double max = 0;
            int bestPasser = 0;
            for(int i = 0; i < 5; i++)
            {
                if(i == ourShooter)
                    continue;
                double prob = selfPlayerStats[ourTeam[i]-1][PASSING];
                if(prob > max)
                {
                    bestPasser = i;
                    max = prob;
                }
            }
            return bestPasser;        
        }

        /* pick best passer based on miss-match */
        double lowest_prob = 1000;
        for(int i = 0; i < 5; i++)
        {
            double prob = selfPlayerStats[ourTeam[i]-1][INTERCEPTING] + OpponentStat[i][PASSING];
            if(prob < lowest_prob)
            {
                passer = i;
                lowest_prob = prob;
            }
        }
        return passer;
    }




    /* pick player strategies */



    /* strategy to pick the 5 best shooters for our starting lineup */
    public int [] getBestShooters()
    {

        int bestShooters[] = new int[5];
        ArrayList<Integer> exclude = new ArrayList<Integer>();
        for(int i = 0; i < 5; i++)
        {
            double max = 0;
            int tempBestShooter = 0;
            for(int j = 0; j < totalNumberOfPlayers; j++)
            {
                if(exclude.contains(j))
                    continue;
                double prob = selfPlayerStats[j][SHOOTING];
                if(prob > max)
                {
                    tempBestShooter = j;
                    max = prob;
                }
            }
            bestShooters[i] = tempBestShooter + 1;
            exclude.add(tempBestShooter);
        }
        return bestShooters;
    }
}
