package anja.util.psgr;

import java.io.Writer;
import java.text.AttributedCharacterIterator;

/**
 * PSGr1 is a Graphics subclass for Java 1.1 that images to PostScript.
 * (C) 1996 E.J. Friedman-Hill and Sandia National Labs
 * @version 	2.1
 * @author 	Ernest Friedman-Hill
 * @author      ejfried@ca.sandia.gov
 * @author      http://herzberg.ca.sandia.gov
 */

public class PSGr1 extends PSGrBase
{
  /**
   * Constructs a new PSGr1 Object. Unlike regular Graphics objects,
   * PSGr contexts can be created directly.
   * @see #create()
   */

  public PSGr1()
  {
    super();
  }

  /**
   * Constructs a new PSGr1 Object. Unlike regular Graphics objects,
   * PSGr contexts can be created directly.
   * @param o Output stream for PostScript output
   * @see #create()
   */

  public PSGr1(Writer o)
  {
    super(o, true);
  }

  /**
   * Constructs a new PSGr1 Object. Unlike regular Graphics objects,
   * PSGr contexts can be created directly.
   * @param o Output stream for PostScript output
   * @see #create()
   */

  public PSGr1(Writer o, boolean emitProlog)
  {
    super(o, emitProlog);
  }

/* (non-Javadoc)
 * @see java.awt.Graphics#drawString(java.text.AttributedCharacterIterator, int, int)
 */
public void drawString(AttributedCharacterIterator arg0, int arg1, int arg2)
{
	// TODO Auto-generated method stub
	
}

}


  
