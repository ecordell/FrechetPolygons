package anja.lisp;

/**
* Container for Lisp symbols.
* 
* @author Tom Kamphans
*/

public class LOSymbol extends LispObj
{

   String _contents;

   //============================================================
   public LOSymbol()
   //============================================================
   {
   }

   //============================================================
   public LOSymbol( String s )
   //============================================================
   {
      _contents = s;
   }

   //============================================================
   public int getType()
   //============================================================
   {
 	return LispObj.SYMBOL;
   }

   //============================================================
   public String toString()
   //============================================================
   {
      return "Symbol: " + _contents;
   }
}
