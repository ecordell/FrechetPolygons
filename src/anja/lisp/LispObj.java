package anja.lisp;

/**
* Container for a Lisp object (aka atom).
* 
* @author Tom Kamphans
*/

public abstract class LispObj
{
   // ************************************************************************
   // Constants
   // ************************************************************************

   /** Possible object types: */
   public static final int 	NIL	= 0;
   public static final int 	NUMBER	= 1;
   public static final int 	BOOLEAN	= 2;
   public static final int 	STRING	= 3;
   public static final int 	SYMBOL	= 4;
   public static final int	DPAIR	= 5;
   public static final int 	LEXPR 	= 6;
   public static final int 	LIST 	= 7;

   public static final int	TOKEN		= 20;	
   public static final int 	OPENTOKEN	= 21;
   public static final int	CLOSETOKEN	= 22;


   //************************************************************
   // private variables
   //************************************************************

   // ************************************************************************
   // Constructors
   // ************************************************************************

   //============================================================
   public LispObj()
   //============================================================
   {
   }

   //************************************************************
   // class methods
   //************************************************************


   //============================================================
   public static LispObj NewLispObj( String s )
   //============================================================
   {
      int i = 0;
      try {
         i = Integer.parseInt( s );
      }
      catch ( NumberFormatException e ) {    
          return new LOSymbol( s );
      }
      return new LONumber( i );
   }


   // ************************************************************************
   // Public methods
   // ************************************************************************


   //============================================================
   abstract public int getType();
   //============================================================

   //============================================================
   public String toString()
   //============================================================
    {
	return "Unknown Object";
    }


    public boolean isNil()     { return false; }
    public boolean isNumber()  { return false; }
    public boolean isBoolean() { return false; }
    public boolean isString()  { return false; }
    public boolean isSymbol()  { return false; }
    public boolean isDPair()   { return false; }
    public boolean isLExpr()   { return false; }
    public boolean isList()    { return false; }

    public boolean isToken()   { return false; }


   // ************************************************************************
   // Private methods
   // ************************************************************************

}
