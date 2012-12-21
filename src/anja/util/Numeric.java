package anja.util;


/**
 * This class provides several methods for comparing floating-point numbers.
 * The imprecesion of floating-point numbers is considered according to
 * the IEEE standard 754.
 * 
 * @author Marina Bachran
 */
public class Numeric {

  /** If the test of the Double.doubleToLongBits(d) fails, a test with a 
   *  fixed epsilon is performed. */
  public static double epsilon = 0.0000000001;  
  
  /** If between two numbers lay less than 64 other numbers, the two 
   *  numbers are considered to be equal. */
  public static long maxDist = 64; 
  
  

  /**
   * Compares the parameter numbers and takes the double imprecesion 
   * into account.
   * 
   * @see Numeric#isEqual(double, double)
   * 
   * @param     d1 - the first double to compare
   * @param     d2 - the second double to compare
   * 
   * @return  1   - <i>d1</i> is greater than <i>d2</i>   <br>
   *          0   - <i>d1</i> and <i>d2</i> are equal     <br>
   *         -1   - <i>d1</i> is less than <i>d2</i>
   */
  public static int compare(double d1, double d2) {
    if (isEqual(d1, d2))
      return 0;
    return (d1 < d2) ? -1 : 1;
  }
  
  

  /**
   * Compares the two parameter numbers on taking the double imprecision into
   * account according to the IEEE standard 754.
   * 
   * @param     d1 - the first double to compare
   * @param     d2 - the second double to compare
   * 
   * @return true, if the both numbers are equal under consideration of the
   *         double imprecision, false otherwise
   */
  public static boolean isEqual(double d1, double d2) {
    if (Double.isNaN(d1) || Double.isNaN(d2))
      return false;
    if (d1 == Double.POSITIVE_INFINITY)
      return (d2 == Double.POSITIVE_INFINITY);
    if (d1 == Double.NEGATIVE_INFINITY)
      return (d2 == Double.NEGATIVE_INFINITY);
    long a = Double.doubleToLongBits(d1);
    long b = Double.doubleToLongBits(d2);
    if (a == b)
      return true;
    if (a < 0)
      a = Long.MIN_VALUE - a;
    if (b < 0)
      b = Long.MIN_VALUE - b;
    long dist = Math.abs(a - b);
    if (dist == Long.MIN_VALUE)
      return false;
    if (dist <= maxDist)
      return true;
    double diff = Math.abs(d2 - d1);
    return diff < epsilon; 
  }
  
  
}
