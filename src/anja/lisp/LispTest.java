package anja.lisp;

public class LispTest {

    public static void main(String args[]) 
    {
         LispScanner sc;
	 LOList expr;

         sc = new LispScanner(); 
         sc.scan( "(+ (+ (- 4 3) 1 (+ 1 2) 3) ( + 1 1 ) (+ 3 2))" );
         sc.toString();

	 System.out.println("PARSER:");
	 expr = LispParser.parse( sc );
	 System.out.println( expr.toString() );
	 expr.print();

	 System.out.println( expr.eval() );

    }

}
