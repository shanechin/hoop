package hoop.g3;

public class Fraction {
  private int made;
  private int attempted;
  
  public Fraction (int initMade, int initAttempted) {
    made = initMade;
    attempted = initAttempted;
  }
  
  public void make() {
    made++;
    attempted++;
  }
  
  public void miss() {
    attempted++;
  }
  
  public int made() {
    return made;
  }
  
  public int missed() {
    return attempted - made;
  }
  
  public int attempted() {
    return attempted;
  }
}