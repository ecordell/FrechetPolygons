package anja.lisp;

/**
* Container for lambda expressions.
* 
* @author Tom Kamphans
*/

public class LispLExpr extends LispObj
{
   
   boolean bool;

   //============================================================
   public LispLExpr()
   //============================================================
   {
      bool = false;
   }

   //============================================================
   public boolean Get()
   //============================================================
   {
      return bool;
   }

   //============================================================
   public void Set( boolean newValue )
   //============================================================
   {
      bool = newValue;
   }

   //============================================================
   public int getType()
   //============================================================
   {
 	return LispObj.BOOLEAN;
   }

   //============================================================
   public String toString()
   //============================================================
   {
       if ( bool ) 
          return "#t";
       else
          return "#f";
   }
}