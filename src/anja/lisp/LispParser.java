package anja.lisp;


/**
* Parser for Lisp expressions: converts a sequence of Token returned by
* the LispScanner into a Lisp Expression (@see anja.lisp.LispExpr).
* (It is not really necessary to make a class for this functionality, but
* it resembles the classic scanner-parser-evaluator principle).
* 
* @author Tom Kamphans
*/

public class LispParser
{

   //============================================================
   public static LOList parse( LispScanner scanner )
   //============================================================
   {
       return parseSubExpr( scanner );

   } // parse



   // ************************************************************
   // Private methods
   // ************************************************************


   //============================================================
   private static LOList parseSubExpr( LispScanner scanner )
   //============================================================
   {
       LOList expr, root = null;
       LispObj t;

       if ( scanner.hasNextToken() ) {

	   t = scanner.getNextToken();

	   if ( t.getType() == LispObj.OPENTOKEN ) {
       System.out.println("(");
	       root = new LOList();
	       while ( ( expr = parseSubExpr( scanner ) ) != null ) {
		   root.addChild( expr );
	       } // while
	   } else if ( t.getType() == LispObj.CLOSETOKEN ) {
       System.out.println(")");
	       return null;
	   } else {
	       System.out.println("ELSE"+t.toString());

	       root = new LOList();
	       root.setContent( t );
	   } // else
       } // if hasNextToken


       return root;
   } // parseSubExpr
}
