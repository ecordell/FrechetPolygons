package anja.lisp;

import java.util.LinkedList;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
* Scanner for Lisp expressions. Converts a sequence of strings to 
* a sequence of Lisp Objects and Tokens.
* 
* @author Tom Kamphans
*/

public class LispScanner
{
   // ***********************************************************
   // Constants
   // ***********************************************************

   /** Scanner state: */
   private final static int	_nowhere	= 0;
   private final static int 	_inExpr  	= 1;
   private final static int 	_inString	= 2;



   //************************************************************
   // private variables
   //************************************************************

   private LinkedList _tokens;
   private int _state;
   private int _exprs;
   private StringBuffer _sb;


   // ************************************************************
   // Constructors
   // ************************************************************

    //============================================================
    public LispScanner()
    //============================================================
    {
	_state = _nowhere;
	_exprs = 0;
	_tokens = new LinkedList();
    }

   //************************************************************
   // class methods
   //************************************************************


   // ************************************************************
   // Public methods
   // ************************************************************

   /**
    * Scan the given string s. Return true, iff the expression is closed
    * (i.e., the number of '(' is equal to the number of ')').
    * The list of tokens is stored internally.
    */

   //============================================================
   public boolean scan ( String inp )
   //============================================================
   {
      LispObj t;  
      StringCharacterIterator iterator = new StringCharacterIterator( inp );
      char c = iterator.first();

      _sb = new StringBuffer();

      do {
         if ( _state != _inString ) {
            switch ( c ) {
               case ' ':
               case '\t':
               case '\n':
               case '\r':
               case '\f': if ( _state != _nowhere ) {
                             _closeObj();
                             _state = _nowhere;
                          }
                          continue;

               case '(':  _exprs++;
                          t = new LispToken( LispObj.OPENTOKEN );
                          _tokens.addLast( t );
                          continue;

               case ')':  if ( _state != _nowhere ) {
                             _closeObj();
                             _state = _nowhere;
                          }
                          _exprs--;
                          t = new LispToken( LispObj.CLOSETOKEN );
                          _tokens.addLast( t );
                          continue;

               case '"':  _state = _inString;
                          continue;

               default:
                          _state = _inExpr;
                          break;
            } // switch	
         } // if inString

         if ( c == '"' ) { // close string
                _closeString();
                _state = _nowhere;
                continue;
         } // if close string

         _sb.append( c );

      } while ( ( c = iterator.next() ) != CharacterIterator.DONE );

      return (_exprs == 0);

   } // scan



   /**
    * Returns and deletes the next token (Package-friendly method!).
    */

   //============================================================
   LispObj getNextToken()
   //============================================================
   {
      return (LispObj) _tokens.removeFirst();
   } // getNextToken


   /**
    * Returns true iff scanner has a next token (Package-friendly method!).
    */

   //============================================================
   boolean hasNextToken()
   //============================================================
   {
       return !(_tokens.isEmpty());
   } // hasNextToken


   //============================================================
   public String toString()
   //============================================================
   {
      int i;
      StringBuffer buf = new StringBuffer();

      for (i = 0; i < _tokens.size(); i++) {
         buf.append( _tokens.get(i).toString() );
         System.out.println( _tokens.get(i).toString() );
      }
      return new String( buf );
   }


   // ************************************************************
   // Private methods
   // ************************************************************

   //============================================================
   private void _closeObj()
   //============================================================
   {
      String s;
      LispObj t;

      s = new String( _sb );
      _sb = new StringBuffer();
      t = LispObj.NewLispObj( s );
      _tokens.addLast( t );
   }

   //============================================================
   private void _closeString()
   //============================================================
   {
      String s;
      LispObj t;

      s = new String( _sb );
      _sb = new StringBuffer();
      t = new LOSymbol( s );
      _tokens.addLast( t );
   }

}
