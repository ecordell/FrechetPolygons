package anja.lisp;

/**
* This class represents a list of Lisp objects. This may be an 
* unevaluated Lisp expression as returned by the Lisp parser; in this case,
* the expression can be evaluated using the eval method.
* 
* 
* @author Tom Kamphans
*/

public class LOList extends LispObj
{
    // ***********************************************************
    // Constants
    // ***********************************************************

    private static int _indent = 0;

    //************************************************************
    // private variables
    //************************************************************

    private LOList _firstSon = null;
    private LOList _rightBrother = null;
    private LOList _currentSon = null;
    private LispObj _content = null;
    private int _numSons = 0;

    // ************************************************************
    // Constructors
    // ************************************************************

    //============================================================
    public LOList()
    //============================================================
    {
    }

   //************************************************************
   // class methods
   //************************************************************


   // ************************************************************
   // Public methods
   // ************************************************************

    //============================================================
    public int eval()
    //============================================================
    {
	LOList c;
	int akku = 0;
	char operator;

	if ( this.isLeaf() ) {
	    akku = ((LONumber)_content)._contents;
	} else {
	    c = getFirstChild();
	    operator = (((LOSymbol) c._content)._contents).charAt(0);
	    System.out.println(">>" + operator);
	    c = getNextChild();
	    akku = c.eval();
	    while ( this.hasNextChild() ) {
		c = getNextChild();
		switch( operator ) {
		case '+': akku += c.eval(); break;
		case '*': akku *= c.eval(); break;
		case '-': akku -= c.eval(); break;
		}
	    }
	} // else

	return akku;

    } // eval

    /**
    * Add a subexpression. Note that this access is allowed only to
    * other classes from the anja.lisp package; that is, to
    * anja.lisp.parser.
    */

    //============================================================
    void addChild ( LOList in )
    //============================================================
    {
	if ( _firstSon == null) {
System.out.println("Down");
	    _firstSon = in;
	    _numSons++;
	    _currentSon = _firstSon;
	} else {
System.out.println("RIGHT");
	    _currentSon._rightBrother = in;
	    _numSons++;
	    _currentSon = _currentSon._rightBrother;
	}
    } // addChild


    /**
     * Return the first child.
     */
    
    //============================================================
    LOList getFirstChild()
    //============================================================
    {
	_currentSon = _firstSon;
	return _firstSon;
    } // getFirstChild
     

    /**
     * Return the next child or null
     */

    //============================================================
    LOList getNextChild()
    //============================================================
    {
	if ( _currentSon != null ) {
	    _currentSon = _currentSon._rightBrother;
	}
	return _currentSon;	
    } // getNextChild


    //============================================================
    boolean hasNextChild()
    //============================================================
    {
	if ( this.isLeaf() || _currentSon == null ){
	    return false;
	} else {
	    return _currentSon._rightBrother != null;
	}
    } // hasNextchild


    /**
     * True, if this node represents a leaf in the tree (i.e., if the
     * node has no children).
     */

    //============================================================
    boolean isLeaf()
    //============================================================
    {
	return _numSons == 0;
    } // isLeaf


    /**
    * Get/Set methods for the content. 
    */

    //============================================================
    void setContent ( LispObj in )
    //============================================================
    {
System.out.println("Adding " + in.toString());
	_content = in; 
    } // add


    //============================================================
    LispObj getContent ()
    //============================================================
    {
	return _content; 
    } // getContent



    //============================================================
    public int getType()
    //============================================================
    {
	return LispObj.LIST;
    } // getType

    //============================================================
    public String toString()
    //============================================================
    {
	StringBuffer buf = new StringBuffer();
	
	if ( this.isLeaf() ) {
	    buf.append ( _content.toString() );
	} else {
	    LOList c = this.getFirstChild();
	    buf.append ( " ( " );
	    buf.append( c.toString() );
	    while ( this.hasNextChild() ) {
		c = this.getNextChild();
		buf.append( c.toString() );
	    } // while
	    buf.append ( " ) " );
	}

	return new String( buf );

    } // toString



    //============================================================
    public void print()
    //============================================================
    {
	printTree( 0 );
    }

    //============================================================
    private void printTree( int indent )
    //============================================================
    {
	LOList t;
	int i;

	if ( _content != null ) {
	    // java.io.PrintStream.printf("%*c", indent, ' ');
	    for (i=0; i<indent; i++) System.out.print(" ");
	    System.out.println( _content.toString() );
	} else {
	    // java.io.PrintStream.printf("%*c\n", indent, '(');
	    for (i=0; i<indent; i++) System.out.print(" ");
	    System.out.println("(");
	    t = this._firstSon;
	    while ( t != null ) {
		t.printTree( indent + 3 );
		t = t._rightBrother;
	    }
	    // printf("%*c\n", indent, ')');
	    for (i=0; i<indent; i++) System.out.print(" ");
	    System.out.println(")");
	}
    } // printTree

    //============================================================
    private void printTree2( char h, int level )
    //============================================================
    {
	int i;
	
	if ( _content != null ) {
	    for (i = 0; i < _indent-1; i++) {
		if ( i == level ) {
		    System.out.print( "+--");
		} else {
		    System.out.print( "|  ");
		}
	    }
	    System.out.print ( h + "-- ");
	    System.out.print ( _content.toString() );
	    System.out.println( " " +  _indent );
	} else {
	    _indent += 1;
	    LOList c = this._firstSon;
	    c.printTree2( '+', level + 1 );
	    c = c._rightBrother;
	    while ( c != null ) {
		c.printTree2( '|', level + 1);
		c = c._rightBrother;
	    } // while
	    _indent -= 1;
//	    for (i = 0; i < _indent-1; i++) System.out.print("|  ");
//	    System.out.println ("|  ");
	}

    } // printTree


   // ************************************************************
   // Private methods
   // ************************************************************


}
