package anja.util;

//import java_ersatz.java2d.Rectangle2D;
//import java_ersatz.java2d.Graphics2D;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
*	Drawable
*
*	Interface for drawable Objects
*
*	@version	2.0 24 May 2004
*	@author	Ibragim Kouliev
*/

public interface Drawable {
/**
* Draw the Object.
* @param graphics The Graphics object to draw into
* @param graphicsContext The GraphicsContext to draw with
*/
	public abstract void draw(Graphics2D graphics, GraphicsContext graphicsContext);

/**
* Test if Object intersects with rectangle
* @param box The rectangle to be tested
* @return true if Object intersects, false otherwise
*/
	public abstract boolean intersects(Rectangle2D box);
}
