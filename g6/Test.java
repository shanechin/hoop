package hoop.g6;

import java.util.Random;

public class Test {
	public static void main(String[] args) {
		final int tries = 100000;
		double successes = 0;
		for(int i = 0; i < tries; i++) {
			double count = 0;
			for(int j = 0; j < 15; j++) {
				if(Math.random() > .49)
					count++;
			}
			
			if(count >= 5)
				successes = successes + 1;
		}
		
		System.out.println(successes / tries);
	}
}
