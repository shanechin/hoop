package hoop.g6;

public class Statistics {
	public static void main(String[] args) {
		double d = prob(12, .5, 1);
		System.out.println("r: " + d);
		System.out.println(binomcdf(12, .5, 5));
		
		System.out.println(findp(15));
	}
	
	public static double findp(int nplayers) {
		for(double p = 0.01; p <= 1; p+=.001) {
			double d = binomcdf(nplayers, 1-p,nplayers-5);
//			System.out.println("p: " + (1-p) + "--" + d);
			if(d > .95) return (1-p);
		}
		throw new RuntimeException();
	}
	
	public static double binomcdf(int n, double p, int x) {
		double cdf = 0;
		for(int i = 0; i <= x; i++) {
			cdf += prob(n, p, i);
		}
		return cdf;
	}
	
	private static double prob(int n, double p, int successes) {
		double l = 1;
		for(int i = n - successes + 1; i <= n; i++) {
//			System.out.println(i + " " + l);
			l *= i;
		}
		
		for(int i = successes; i > 1; i--) {
//			System.out.println(i + " " + l);
			l /= i;
		}
		
		l *= Math.pow(p, successes);
		l *= Math.pow(1 - p, n - successes);
		return l;
	}
}
