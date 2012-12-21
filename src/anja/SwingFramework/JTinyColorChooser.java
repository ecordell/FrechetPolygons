/*
 * Created on Feb 3, 2005
 *
 * JTinyColorChooser.java
 * 
 */
package anja.SwingFramework;


import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import javax.swing.JPanel;
import javax.swing.event.*;
import anja.util.MathConstants;


/**
 * My own implementation of a color chooser with a minimal footprint (in terms
 * of display real estate) and an intuitive interface.
 * 
 * <p>The primary design consideration for this component is the ability to
 * operate well in a fully interactive environment, i.e. not as a dialog window
 * but as integral part of some UI.
 * 
 * @author Ibragim Kouliev
 * 
 *         <p>Please note that this component is not derived from
 *         {@link JColorChooser}and it doesn't provide any of its standard
 *         facilities (i.e. convenience functions for showing dialogs etc.).
 *         Another thing to keep in mind is that in certain situations the color
 *         manipulations might be somewhat imprecise, due to some idiosyncracies
 *         in java.awt.Color class implementation.
 * 
 */
public class JTinyColorChooser
		extends JPanel
		implements
		//ComponentListener,
		MouseListener, MouseMotionListener, MathConstants
{

	//*************************************************************************
	// 				  Public constants
	//*************************************************************************

	//*************************************************************************
	// 				  Private constants
	//*************************************************************************

	// fixed component width / height in pixels
	private static final int	COMPONENT_SIZE		= 160;			// no less that 170!

	// background color of main image buffer
	private static final Color	BACKGROUND_COLOR	= new Color(150, 150, 150,
															255);

	// number of color steps in the hue ring image 
	//(is also used for sliders)
	private static final int	HUE_STEPS			= 1000;
	private static final int	SLIDER_STEPS		= 300;

	// thickness of the hue ring in pixels
	private static final int	RING_WIDTH			= 17;

	// slider arc width in pixels
	private static final int	SLIDER_WIDTH		= 15;

	// spacing between hue ring and slider arcs in pixels
	private static final int	SLIDER_SPACING		= 5;

	private static final int	MARKER_RADIUS		= 4;
	private static final int	MARKER_SHIFT		= 2;

	// HSB mode flags
	private static final int	HUE_CHANGE			= 0x00;
	private static final int	SAT_CHANGE			= 0x01;
	private static final int	BRI_CHANGE			= 0x02;
	private static final int	COLOR_CHANGE		= 0x04;
	private static final int	NO_CHANGE			= 0x08;

	// mode flags
	private static final int	PALETTE_MODE		= 0x00;
	private static final int	HSB_MODE			= 0x01;
	private static final int	RGB_MODE			= 0x02;		// reserved!

	//*************************************************************************
	// 				   Class variables
	//*************************************************************************

	// precalculated trig values
	private static final float	_ring_sines[];
	private static final float	_ring_cosines[];

	private static final float	_b_slider_sines[];
	private static final float	_b_slider_cosines[];

	private static final float	_s_slider_sines[];
	private static final float	_s_slider_cosines[];

	// tool tip strings
	private static final String	_ringTip;
	private static final String	_SatSliderTip;
	private static final String	_BriSliderTip;
	private static final String	_pickerTip;
	private static final String	_modeIconTip;

	//*************************************************************************
	// 			       Private instance variables
	//*************************************************************************

	private BufferedImage		m_mainImage;						// Combined offscreen buffer
	private BufferedImage		m_ringImage;						// Offscreen HSB ring image
	private BufferedImage		m_sliderImage;						// B/S sliders image
	private BufferedImage		m_squareImage;						// Color square image
	private BufferedImage		m_paletteImage;					// Indexed color mode image	

	// Icons
	private BufferedImage		m_HSBicon;
	private BufferedImage		m_PalIcon;

	private int					m_iWidth;							// current dimensions of the component
	private int					m_iHeight;

	// Radii of the hue ring and sliders
	private float				m_fInnerRingRadius;
	private float				m_fOuterRingRadius;
	private float				m_fInnerSliderRadius;
	private float				m_fOuterSliderRadius;

	// helper variables
	private Point				m_Center;
	private Point				m_prevMousePosition;
	private Point				m_mousePosition;

	private Point				m_colorMarkerPosition;
	private Rectangle			m_colorRectangle;
	private Rectangle			m_modeIconRectangle;
	private Rectangle			m_paletteRectangle;

	/* Reference values for HSB parameters - i.e. the ones set 
	 * via color wheel and S/B sliders. These don't change 
	 * color picking via the square. 
	 */
	private float				m_fBaseHue;
	private float				m_fBaseSaturation;
	private float				m_fBaseBrightness;

	/* These are the actual HSB values picked from the square */
	private float				m_fHue;
	private float				m_fSaturation;
	private float				m_fBrightness;

	// same as RGB values , all in [0.0....1.0]
	private float				m_fRed;
	private float				m_fGreen;
	private float				m_fBlue;

	private boolean				m_bParamsChanged;
	private boolean				m_bMouseMoved;
	private boolean				m_bOpsEnabled;

	private int					m_iChangeType;						//  H/S/B change switch
	private int					m_iChooserMode;					// mode switch

	private Vector				m_changeListeners;

	//*************************************************************************
	// 			       Static Initialization
	//*************************************************************************

	static
	{
		// tooltip strings init
		_ringTip = "<html><b>Click</b> anywhere within "
				+ "<br>the ring to select a hue."
				+ "<br><b>Press and drag</b> the mouse to "
				+ "<br>gradually decrease or increase"
				+ "<br>the hue value.</html>";

		_SatSliderTip = "<html>This slider sets the maximum"
				+ "<br>saturation for the color picker."
				+ "<br><b>Click</b> inside the slider"
				+ "<br>to select a saturation value."
				+ "<br><b>Drag</b> the mouse within the slider"
				+ "<br>to icrease / decrease the"
				+ "<br>saturation value.</html>";

		_BriSliderTip = "<html>This slider sets the maximum"
				+ "<br>brightness for the color picker."
				+ "<br><b>Click</b> inside the slider"
				+ "<br>to select a brightness value."
				+ "<br><b>Drag</b> the mouse within the slider"
				+ "<br>to icrease / decrease the"
				+ "<br>brightness value.</html>";

		_pickerTip = "<html>In this area you can pick"
				+ "<br> any color interpolated between"
				+ "<br>current hue, max. S & B values. "
				+ "<br><b>Click</b> inside the rectangle to"
				+ "<br>pick a color, or <b>drag</b> to"
				+ "<br> slide the color picker.</html>";

		_modeIconTip = "<html>Switch between"
				+ "<br>HSB and Palette mode</html>";
	}

	static
	{
		/*
		 * Precalculate trigonometric values that will be used for
		 * drawing the hue selection ring and saturation/brightness
		 * sliders. Trigometry calculations on-the-fly are generally
		 * slow, thus precalculated values save a lot of time during
		 * redraws. Moreover, because the size for all color chooser
		 * instances is the same(and fixed), these variables are 
		 * declared static so that multiple color choosers can share
		 * them.
		 */

		_ring_sines = new float[HUE_STEPS];
		_ring_cosines = new float[HUE_STEPS];

		_b_slider_sines = new float[SLIDER_STEPS];
		_b_slider_cosines = new float[SLIDER_STEPS];

		_s_slider_sines = new float[SLIDER_STEPS];
		_s_slider_cosines = new float[SLIDER_STEPS];

		for (int i = 0; i < HUE_STEPS; i++)
		{
			_ring_sines[i] = (float) Math.sin((2 * PI / HUE_STEPS) * i);

			_ring_cosines[i] = (float) Math.cos((2 * PI / HUE_STEPS) * i);
		}

		for (int i = 0; i < SLIDER_STEPS; i++)
		{
			_b_slider_sines[i] = (float) Math.sin(((-PI / 2) / SLIDER_STEPS)
					* i);

			_b_slider_cosines[i] = (float) Math.cos(((-PI / 2) / SLIDER_STEPS)
					* i);
		}

		for (int i = 0; i < SLIDER_STEPS; i++)
		{
			_s_slider_sines[i] = (float) Math.sin(((PI / 2) / SLIDER_STEPS) * i
					+ (PI / 2));

			_s_slider_cosines[i] = (float) Math.cos(((PI / 2) / SLIDER_STEPS)
					* i + (PI / 2));
		}
	}


	//*************************************************************************
	// 				  Constructors
	//*************************************************************************

	/**
	 * Creates a new color chooser with default parameters, i.e. hue of 0, and
	 * saturation / brightness of 1 (fully saturated, pure red).
	 * 
	 */
	public JTinyColorChooser()
	{
		super();

		// initialize UI components
		init();

		// register event listeners
		addMouseListener(this);
		addMouseMotionListener(this);

	}


	//*************************************************************************
	// 			        Public instance methods
	//*************************************************************************

	//--------------------------- get / set methods ---------------------------

	//------------------------------- Color API -------------------------------

	/**
	 * Convenience wrapper for {@link #setColor(float,float,float)} which takes
	 * an argument of type {@link java.awt.Color} instead.
	 * 
	 * @param c
	 *            New color to be set.
	 */
	public void setColor(
			Color c)
	{
		setColor(c.getRed() / 255.0f, c.getGreen() / 255.0f,
				c.getBlue() / 255.0f);
	}


	//*************************************************************************

	/**
	 * Sets a new color for the color chooser and updates all UI elements to
	 * reflect the changes. <br>Can be used on-the-fly to dynamically modify the
	 * color chooser.
	 * 
	 * @param r
	 *            Red component, must be between 0.0 and 1.0
	 * @param g
	 *            Green component, must be between 0.0 and 1.0
	 * @param b
	 *            Blue component, must be between 0.0 and 1.0
	 */
	public void setColor(
			float r,
			float g,
			float b)
	{
		// set internal RGB values
		m_fRed = r;
		m_fGreen = g;
		m_fBlue = b;
		RGBtoHSB(); // sync with internal HSB values

		positionHSBColorPicker(); // reposition chooser in color rectangle
		m_fBaseHue = m_fHue;

		/* Everything else is positioned appropriately in draw_HSB_UI()
		 * Maybe I should factor out that code later!
		 */

		updateImages(); // update UI components
		repaint();
	}


	//*************************************************************************

	/**
	 * Another wrapper, this one taking a 32-bit integer color representation
	 * which is <br>usually produced by various {@link Color} methods, and also
	 * such methods as {@link BufferedImage#getRGB(int, int)}. <br>The value of
	 * alpha is ignored.
	 * 
	 * @param color
	 *            The new color to be set.
	 */
	public void setColor(
			int color)
	{
		//decode
		float r = ((color >> 16) & 0xFF) / 255.0f;
		float g = ((color >> 8) & 0xFF) / 255.0f;
		float b = ((color >> 0) & 0xFF) / 255.0f;

		setColor(r, g, b);
	}


	//*************************************************************************

	/**
	 * Gets the current color.
	 * 
	 * @return a new instance of <code>Color</code> initialized with current RGB
	 *         values.
	 */
	public Color getColor()
	{
		return new Color(m_fRed, m_fGreen, m_fBlue);
	}


	//*************************************************************************

	/**
	 * @return Red component of current color in [0...1]
	 */
	public float getRed()
	{
		return m_fRed;
	}


	//*************************************************************************

	/**
	 * @return Green component of current color in [0...1]
	 */
	public float getGreen()
	{
		return m_fGreen;
	}


	//*************************************************************************

	/**
	 * @return Blue component of current color in [0...1]
	 */
	public float getBlue()
	{
		return m_fBlue;
	}


	//*************************************************************************

	/**
	 * @return Hue value of current color in [0...1]
	 */
	public float getHue()
	{
		return m_fHue;
	}


	//*************************************************************************

	/**
	 * @return Saturation value of current color in [0...1]
	 */
	public float getSaturation()
	{
		return m_fSaturation;
	}


	//*************************************************************************

	/**
	 * @return Brightness value of current color in [0...1]
	 */
	public float getBrightness()
	{
		return m_fBrightness;
	}


	//*************************************************************************

	/**
	 * Add a change listener
	 * 
	 * @param l
	 *            The listener
	 */
	public void addChangeListener(
			ChangeListener l)
	{
		// add l if it's not already registered
		if (m_changeListeners.contains(l) == false)
			m_changeListeners.add(l);
	}


	//*************************************************************************

	/**
	 * Remove a change listener
	 * 
	 * @param l
	 *            The listener
	 */
	public void removeChangeListener(
			ChangeListener l)
	{
		// remove l if it's previously been registered
		if (m_changeListeners.contains(l))
			m_changeListeners.remove(l);
	}


	//*************************************************************************

	// layout functions

	/**
	 * Reimplemented from JComponent
	 * 
	 * @return The minimum size (x size and y size)
	 */
	public Dimension getMinimumSize()
	{
		return new Dimension(COMPONENT_SIZE, COMPONENT_SIZE);
	}


	//*************************************************************************

	/**
	 * Reimplemented from JComponent
	 * 
	 * @return The maximum size (x size and y size)
	 */
	public Dimension getMaximumSize()
	{
		return new Dimension(COMPONENT_SIZE, COMPONENT_SIZE);
	}


	//*************************************************************************

	/**
	 * Reimplemented from JComponent
	 * 
	 * @return The preferred size (x size and y size)
	 */
	public Dimension getPreferredSize()
	{
		return new Dimension(COMPONENT_SIZE, COMPONENT_SIZE);
	}


	//*************************************************************************

	/**
	 * Paints the color chooser.
	 * 
	 * @param g
	 *            The graphics object
	 */
	public void paintComponent(
			Graphics g)
	{
		switch (m_iChooserMode)
		{
			case HSB_MODE:
				draw_HSB_UI(g);
				break;

			case PALETTE_MODE:
				draw_PAL_UI(g);
				break;

			case RGB_MODE: // reserved!
				break;
		}

	}


	//*************************************************************************
	// 			           Event handlers
	//*************************************************************************

	/*
	public void componentHidden(ComponentEvent e)
	{}
	
	public void componentMoved(ComponentEvent e)
	{}
	
	public void componentResized(ComponentEvent e)
	{}
	
	public void componentShown(ComponentEvent e)
	{}*/

	@Override
	public void mouseClicked(
			MouseEvent e)
	{
		// not used here 
	}


	//*************************************************************************

	@Override
	public void mouseEntered(
			MouseEvent e)
	{
		/* This essentially disables handing of events
		 * when mouse is outside of the component. 
		 */
		m_bOpsEnabled = true;
	}


	//*************************************************************************

	@Override
	public void mouseExited(
			MouseEvent e)
	{
		m_bOpsEnabled = false;
	}


	//*************************************************************************

	@Override
	public void mousePressed(
			MouseEvent event)
	{
		Point mouse_position = event.getPoint();
		m_prevMousePosition.setLocation(mouse_position);

		// test for mode change
		if (m_modeIconRectangle.contains(mouse_position))
		{
			m_iChooserMode = (m_iChooserMode == HSB_MODE) ? PALETTE_MODE
					: HSB_MODE;
		}

		if (m_iChooserMode == HSB_MODE)
		{
			m_bMouseMoved = false;
			m_iChangeType = NO_CHANGE;

			// cursor in ring -> select hue
			/* +/-1 constants are necessary to exclude the black
			 * bounding circles from the test - otherwise hues
			 * might 'jump' to 0.0(red) in certain situations
			 * 
			 */
			if (pointInRing(mouse_position, m_fInnerRingRadius + 1,
					m_fOuterRingRadius - 1))
			{
				m_iChangeType = HUE_CHANGE;
			}

			// cursor is in saturation slider -> adjust S
			if (pointInArc(mouse_position, m_fInnerSliderRadius + 1,
					m_fOuterSliderRadius - 1, -1, 1))
			{
				m_iChangeType = SAT_CHANGE;
			}

			//cursor is in brightness slider -> adjust B 
			if (pointInArc(mouse_position, m_fInnerSliderRadius + 1,
					m_fOuterSliderRadius - 1, 1, -1))
			{
				m_iChangeType = BRI_CHANGE;
			}

			// cursor is in color square -> adjust B/S
			if (m_colorRectangle.contains(mouse_position))
			{
				m_iChangeType = COLOR_CHANGE;
			}
		}
		else if (m_iChooserMode == PALETTE_MODE)
		{

		}
	}


	//*************************************************************************

	@Override
	public void mouseReleased(
			MouseEvent event)
	{
		if (m_bOpsEnabled == false)
			return;

		Point position = event.getPoint();
		float hsb[] = new float[3];

		if (m_iChooserMode == HSB_MODE)
		{
			if (m_bMouseMoved == true)
			{
				//fireChangeEvent();
				/* if mouse was moved, do nothing*/
			}
			else
			{
				// get color from ring / sliders / triangle
				switch (m_iChangeType)
				{
					case HUE_CHANGE:
						interpolateHfromRing(position.x, position.y, hsb);
						m_fBaseHue = hsb[0];
						break;

					case SAT_CHANGE:
						interpolateSfromSlider(position.x, position.y, hsb);
						m_fBaseSaturation = hsb[1];
						break;

					case BRI_CHANGE:
						interpolateBfromSlider(position.x, position.y, hsb);
						m_fBaseBrightness = hsb[2];
						break;

					case COLOR_CHANGE:

						m_colorMarkerPosition.setLocation(position.x,
								position.y);
						break;

					default:
						//m_iChangeType = NO_CHANGE; // reset flag
						break;
				}
			}

			m_bMouseMoved = false;

			if (m_iChangeType != NO_CHANGE)
			{
				//always interpolate color from color square
				// to reflect the changes to HSB UI
				interpolateHSBinRectangle(m_colorMarkerPosition.x,
						m_colorMarkerPosition.y, hsb);

				m_fHue = hsb[0];
				m_fSaturation = hsb[1];
				m_fBrightness = hsb[2];

				HSBtoRGB(); // sync HSB and RGB values

				updateImages();
				fireChangeEvent();
			}
		}
		else if (m_iChooserMode == PALETTE_MODE)
		{
			if (m_paletteRectangle.contains(position))
			{
				//pick color from palette
				int color = m_paletteImage.getRGB(position.x, position.y);
				//decode and update
				m_fRed = ((color >> 16) & 0xFF) / 255.0f;
				m_fGreen = ((color >> 8) & 0xFF) / 255.0f;
				m_fBlue = ((color >> 0) & 0xFF) / 255.0f;

				RGBtoHSB(); // sync RGB and HSB values

				// sync HSB UI to palette UI
				m_fBaseHue = m_fHue;
				positionHSBColorPicker();
				updateImages();

				fireChangeEvent();
			}
		}

		repaint();
	}


	//*************************************************************************

	@Override
	public void mouseDragged(
			MouseEvent event)
	{
		if (m_bOpsEnabled == false)
			return; // cursor is not in window!

		Point mouse_position = event.getPoint();

		if (m_iChooserMode == HSB_MODE)
		{
			/* Drag events are handled by 'sliding' the
			 * HSB markers - i.e. during a drag event
			 * the markers can't be jerked into various
			 * positions but will instead smoothly follow
			 * the mouse movements. Some dumb math is
			 * required for this, though. See below. :]		 
			 */

			// determine direction of mouse movement
			int delta_x = mouse_position.x - m_prevMousePosition.x;
			int delta_y = mouse_position.y - m_prevMousePosition.y;

			// movement direction indicators
			boolean mouse_left = (delta_x < 0);
			boolean mouse_right = (delta_x > 0);
			boolean mouse_up = (delta_y > 0);
			boolean mouse_down = (delta_y < 0);

			float h_delta = 1.0f / (0.5f * COMPONENT_SIZE);
			float sb_delta = 1.0f / (0.5f * COMPONENT_SIZE);

			// now change one of H,S,B parameters
			switch (m_iChangeType)
			{
				case HUE_CHANGE:

					if (mouse_left | mouse_down)
						m_fBaseHue -= h_delta;

					if (mouse_right | mouse_up)
						m_fBaseHue += h_delta;

					// clamp hue in cyclic way
					if (m_fBaseHue > (1.0f))
						m_fBaseHue -= 1.0f;
					if (m_fBaseHue < (0.0f))
						m_fBaseHue += 1.0f;

					break;

				case SAT_CHANGE:

					if (mouse_left | mouse_down)
						m_fBaseSaturation += sb_delta;

					if (mouse_right | mouse_up)
						m_fBaseSaturation -= sb_delta;

					// clamp saturation
					if (m_fBaseSaturation > (1.0f))
						m_fBaseSaturation = 1.0f;
					if (m_fBaseSaturation < (0.0f))
						m_fBaseSaturation = 0.0f;

					break;

				case BRI_CHANGE:

					if (mouse_left | mouse_down)
						m_fBaseBrightness += sb_delta;

					if (mouse_right | mouse_up)
						m_fBaseBrightness -= sb_delta;

					// clamp brightness
					if (m_fBaseBrightness > (1.0f))
						m_fBaseBrightness = 1.0f;
					if (m_fBaseBrightness < (0.0f))
						m_fBaseBrightness = 0.0f;

					break;

				case COLOR_CHANGE:

					if (m_colorRectangle.contains(mouse_position))
					{
						//m_colorMarkerPosition.setLocation(mouse_position);

						if (mouse_left)
							m_colorMarkerPosition.x -= MARKER_SHIFT;
						if (mouse_right)
							m_colorMarkerPosition.x += MARKER_SHIFT;
						if (mouse_up)
							m_colorMarkerPosition.y += MARKER_SHIFT;
						if (mouse_down)
							m_colorMarkerPosition.y -= MARKER_SHIFT;

						// clamp marker coordinates to lie within the color rect
						if (m_colorMarkerPosition.x < m_colorRectangle.x)
							m_colorMarkerPosition.x = m_colorRectangle.x;

						if (m_colorMarkerPosition.x > m_colorRectangle.x
								+ m_colorRectangle.width)
							m_colorMarkerPosition.x = m_colorRectangle.x
									+ m_colorRectangle.width;

						if (m_colorMarkerPosition.y < m_colorRectangle.y)
							m_colorMarkerPosition.y = m_colorRectangle.y;

						if (m_colorMarkerPosition.y > m_colorRectangle.y
								+ m_colorRectangle.height)
							m_colorMarkerPosition.y = m_colorRectangle.y
									+ m_colorRectangle.height;

					}
					break;

				default:
					break;
			}

			if (m_iChangeType != NO_CHANGE)
			{
				//always update the color from color square
				float hsb[] = new float[3];
				interpolateHSBinRectangle(m_colorMarkerPosition.x,
						m_colorMarkerPosition.y, hsb);

				m_fHue = hsb[0];
				m_fSaturation = hsb[1];
				m_fBrightness = hsb[2];

				HSBtoRGB(); //sync HSB and RGB values

				updateImages();
				//fireChangeEvent();	
			}

			m_prevMousePosition.setLocation(mouse_position);
			m_bMouseMoved = true;
		}
		else if (m_iChooserMode == PALETTE_MODE)
		{
			// do nothing... yet!
		}

		repaint();
	}


	//*************************************************************************

	@Override
	public void mouseMoved(
			MouseEvent e)
	{
		//Point p = e.getPoint();

		// choose and display a tool tip based on mouse's position

		//FIXME: Temporarily disabled
		//showHelp(p);		
	}


	//*************************************************************************
	// 			      Protected instance methods
	//*************************************************************************

	//*************************************************************************
	// 			       Private instance methods
	//*************************************************************************

	private void showHelp(
			Point p)
	{
		switch (m_iChooserMode)
		{
			case HSB_MODE:

				if (pointInRing(p, m_fInnerRingRadius, m_fOuterRingRadius))
				{
					setToolTipText(_ringTip);
				}
				else if (pointInArc(p, m_fInnerSliderRadius,
						m_fOuterSliderRadius, -1, 1))
				{
					setToolTipText(_SatSliderTip);
				}

				else if (pointInArc(p, m_fInnerSliderRadius,
						m_fOuterSliderRadius, 1, -1))
				{
					setToolTipText(_BriSliderTip);
				}
				else if (m_colorRectangle.contains(p))
				{
					setToolTipText(_pickerTip);
				}

				break;

			case PALETTE_MODE:

				break;

			case RGB_MODE:
				break;

		}

		// always show this tip
		if (m_modeIconRectangle.contains(p))
		{
			// FIXME: There's a bug here....
			setToolTipText(_modeIconTip);
		}
	}


	//*************************************************************************

	/* Dispatch a change event to all registered listeners */
	private void fireChangeEvent()
	{
		ChangeEvent event = new ChangeEvent(this);

		Enumeration e = m_changeListeners.elements();
		while (e.hasMoreElements())
		{
			ChangeListener l = (ChangeListener) e.nextElement();
			l.stateChanged(event);
		}

	}


	//*************************************************************************

	/* Update HSB parameters based on color code and
	 * change code from mouse event handlers
	 */
	private void updateImages()
	{
		// update sliders & color square	
		drawSliders();
		drawColorRectangle();

		/*
		// combine everything into the main buffer
		Graphics g = m_mainImage.getGraphics();	
		g.drawImage(m_ringImage,   0, 0, null);
		g.drawImage(m_sliderImage, 0, 0, null);
		g.drawImage(m_squareImage, 0, 0, null);*/
	}


	//*************************************************************************

	// component initialization 
	private void init()
	{
		m_changeListeners = new Vector();

		m_bParamsChanged = true;
		m_fBaseHue = 0.0f;
		m_fBaseBrightness = m_fBaseSaturation = 1.0f;

		m_iChangeType = NO_CHANGE;
		m_iChooserMode = HSB_MODE; // in HSB mode per default

		// initialize all dimension variables based on component size
		m_iWidth = m_iHeight = COMPONENT_SIZE;

		m_fOuterSliderRadius = (COMPONENT_SIZE / 2.0f) - SLIDER_SPACING;
		m_fInnerSliderRadius = m_fOuterSliderRadius - SLIDER_WIDTH;

		m_fOuterRingRadius = m_fInnerSliderRadius - SLIDER_SPACING;
		m_fInnerRingRadius = m_fOuterRingRadius - RING_WIDTH;

		m_Center = new Point(m_iWidth / 2, m_iWidth / 2);

		// initialize helper variables
		m_colorMarkerPosition = new Point();
		m_prevMousePosition = new Point();
		m_mousePosition = new Point();

		m_colorRectangle = new Rectangle();
		m_paletteRectangle = new Rectangle();

		// set size & position of color selection rectangle
		float box_size = m_fInnerRingRadius - 13;
		m_colorRectangle.setRect(m_Center.x - box_size, m_Center.y - box_size,
				2 * box_size, 2 * box_size);

		// put color marker initially in the middle
		m_colorMarkerPosition.setLocation(m_Center);

		// set corresponding color
		float hsb[] = new float[3];
		interpolateHSBinRectangle(m_Center.x, m_Center.y, hsb);
		m_fSaturation = hsb[1];
		m_fBrightness = hsb[2];

		// initialize image components

		m_sliderImage = new BufferedImage(m_iWidth, m_iHeight,
				BufferedImage.TYPE_INT_ARGB);

		m_squareImage = new BufferedImage(m_iWidth, m_iHeight,
				BufferedImage.TYPE_INT_ARGB);

		// paint initial contents				
		makeHueRingImage();
		makePaletteImage();

		drawColorRectangle();
		drawSliders();

		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

		// create icons for color modes

		// init icon hit area rectangle
		m_modeIconRectangle = new Rectangle(2, m_iHeight - 26, 24, 24);
		// create "HSB mode icon"
		m_HSBicon = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
		{
			Image temp;
			Graphics g = m_HSBicon.getGraphics();

			temp = m_ringImage.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
			g.drawImage(temp, 0, 0, null);

			temp = m_squareImage.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
			g.drawImage(temp, 0, 0, null);

			g.setColor(Color.gray);
			g.draw3DRect(0, 0, 23, 23, true);
			g.dispose();
		}

		// create "Palette mode" icon
		m_PalIcon = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
		{
			Image temp;
			Graphics g = m_PalIcon.getGraphics();

			temp = m_paletteImage.getScaledInstance(24, 24,
					Image.SCALE_AREA_AVERAGING);
			g.drawImage(temp, 0, 0, null);

			g.setColor(Color.gray);
			g.draw3DRect(0, 0, 23, 23, true);
			g.dispose();
		}
	}


	//*************************************************************************

	/* 
	 * Create palette image 
	 */
	private void makePaletteImage()
	{
		m_paletteImage = new BufferedImage(m_iWidth, m_iHeight,
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = (Graphics2D) m_paletteImage.getGraphics();
		g.setBackground(BACKGROUND_COLOR);
		g.clearRect(0, 0, m_iWidth, m_iHeight);

		int spacing = 2; // pixels between adjacent colors
		int insets = 32; // insets from the component edges, in pixels
		int num_swatches = 8; // in one direction

		int x = insets + 1;
		int y = insets + 1;

		int swatch_size = (m_iWidth - 2 * insets - (num_swatches - 1) * spacing)
				/ num_swatches;

		float hue, sat, bri, bri_delta, hue_delta;

		hue = 0.0f;
		sat = bri = 1.0f;

		hue_delta = 1.0f / (6); // primary hues (R,Y,G,C,B,P)
		bri_delta = 1.0f / num_swatches;

		for (int i = 0; i < num_swatches - 1; i++)
		{
			hue = i * hue_delta;

			for (int j = 0; j < num_swatches; j++)
			{
				bri = 1.0f - j * bri_delta;

				g.setColor(new Color(Color.HSBtoRGB(hue, sat, bri)));
				g.fillRect(x, y, swatch_size, swatch_size);

				// draw frame around every swatch
				g.setColor(Color.black);
				g.drawRect(x, y, swatch_size, swatch_size);

				x += (swatch_size + spacing);
			}

			x = insets + 1; // return back
			y += (swatch_size + spacing);
		}

		// separate loop for shades of gray in the last row
		bri = 1.0f;

		/* this forces the very last swatch in the row
		 * to pure black, i.e R=G=B=0
		 */
		bri_delta = 1.0f / (num_swatches - 1);

		for (int j = 0; j < num_swatches; j++)
		{
			g.setColor(new Color(Color.HSBtoRGB(1.0f, 0.0f, bri)));
			g.fillRect(x, y, swatch_size, swatch_size);

			g.setColor(Color.black);
			g.drawRect(x, y, swatch_size, swatch_size);

			x += (swatch_size + spacing);
			bri -= bri_delta;
		}

		g.setColor(Color.lightGray);
		g.drawRect(insets - spacing, insets - spacing, m_iWidth - 2 * insets
				+ spacing + 2, m_iHeight - 2 * insets + spacing + 2);

		m_paletteRectangle.setRect(insets - spacing, insets - spacing, m_iWidth
				- 2 * insets + spacing + 2, m_iHeight - 2 * insets + spacing
				+ 2);

		g.dispose(); // done drawing
	}


	//*************************************************************************

	/* Draw the hue selector ring. Hue values are interpolated 
	 * linearly. 
	 */
	private void makeHueRingImage()
	{
		m_ringImage = new BufferedImage(m_iWidth, m_iHeight,
				BufferedImage.TYPE_INT_ARGB);

		//set background color
		Graphics2D g = (Graphics2D) m_ringImage.getGraphics();

		// gray color with alpha value of 255
		g.setBackground(new Color(150, 150, 150, 255));
		g.clearRect(0, 0, m_ringImage.getWidth(), m_ringImage.getHeight());

		float hue = 0.0f;
		float delta = 1.0f / HUE_STEPS;

		float radius;
		int x, y;

		// draw the hue ring
		for (radius = m_fInnerRingRadius; radius < m_fOuterRingRadius; radius++)
		{
			for (int i = 0; i < HUE_STEPS; i++)
			{
				x = Math.round(radius * _ring_cosines[i]) + m_Center.x;
				y = Math.round(radius * _ring_sines[i]) + m_Center.y;

				hue = 0.0f + i * delta;
				m_ringImage.setRGB(x, y, Color.HSBtoRGB(hue, 1.0f, 1.0f));
			}
		}

		// draw antialiased inner and outer circles
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		BasicStroke stroke = new BasicStroke(1.0f);
		g.setStroke(stroke);
		g.setColor(Color.black);

		radius = m_fOuterRingRadius;

		g.drawOval(m_Center.x - Math.round(radius),
				m_Center.y - Math.round(radius), 2 * Math.round(radius),
				2 * Math.round(radius));

		g.drawOval((m_Center.x - Math.round(radius) + RING_WIDTH), (m_Center.y
				- Math.round(radius) + RING_WIDTH),
				2 * (Math.round(radius) - RING_WIDTH),
				2 * (Math.round(radius) - RING_WIDTH));

		g.dispose(); // done
	}


	//*************************************************************************

	private void drawSliders()
	{
		float saturation, brightness, delta;

		saturation = 0.0f;
		brightness = 0.0f;

		delta = 1.0f / SLIDER_STEPS;

		//set background color
		Graphics2D g = (Graphics2D) m_sliderImage.getGraphics();

		// black color with alpha value of 0
		g.setBackground(new Color(150, 150, 150, 0));
		g.clearRect(0, 0, m_sliderImage.getWidth(), m_sliderImage.getHeight());

		// draw brightness and saturation sliders
		float radius;
		int x, y;

		for (radius = m_fInnerSliderRadius; radius < m_fOuterSliderRadius; radius++)
		{
			for (int i = 0; i < SLIDER_STEPS; i++)
			{
				// brightness slider
				x = Math.round(radius * _b_slider_cosines[i]) + m_Center.x;

				y = Math.round(radius * _b_slider_sines[i]) + m_Center.y;

				brightness = 0.0f + delta * i;

				m_sliderImage.setRGB(x, y,
						Color.HSBtoRGB(m_fBaseHue, 1.0f, brightness));

				//saturation slider	
				x = Math.round(radius * _s_slider_cosines[i]) + m_Center.x;

				y = Math.round(radius * _s_slider_sines[i]) + m_Center.y;

				saturation = 0.0f + delta * i;
				m_sliderImage.setRGB(x, y,
						Color.HSBtoRGB(m_fBaseHue, saturation, 1.0f));
			}
		}

		// draw antialiased arcs
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		BasicStroke stroke = new BasicStroke(1.0f);
		g.setStroke(stroke);
		g.setColor(Color.black);

		radius = m_fOuterSliderRadius;

		g.drawArc(m_Center.x - Math.round(radius),
				m_Center.y - Math.round(radius), 2 * Math.round(radius),
				2 * Math.round(radius), 0, 90);

		g.drawArc(m_Center.x - Math.round(radius) + SLIDER_WIDTH + 1,
				m_Center.y - Math.round(radius) + SLIDER_WIDTH + 1,
				2 * (Math.round(radius) - SLIDER_WIDTH) - 1,
				2 * (Math.round(radius) - SLIDER_WIDTH) - 1, 0, 90);

		g.drawArc(m_Center.x - Math.round(radius),
				m_Center.y - Math.round(radius), 2 * Math.round(radius),
				2 * Math.round(radius), 180, 90);

		g.drawArc(m_Center.x - Math.round(radius) + SLIDER_WIDTH + 1,
				m_Center.y - Math.round(radius) + SLIDER_WIDTH + 1,
				2 * (Math.round(radius) - SLIDER_WIDTH) - 1,
				2 * (Math.round(radius) - SLIDER_WIDTH) - 1, 180, 90);

		// draw caps
		g.drawLine(m_Center.x, m_Center.y - Math.round(m_fInnerSliderRadius),
				m_Center.x, m_Center.y - Math.round(m_fOuterSliderRadius));

		g.drawLine(m_Center.x, m_Center.y + Math.round(m_fInnerSliderRadius),
				m_Center.x, m_Center.y + Math.round(m_fOuterSliderRadius));

		g.drawLine(m_Center.x - Math.round(m_fInnerSliderRadius), m_Center.y,
				m_Center.x - Math.round(m_fOuterSliderRadius), m_Center.y);

		g.drawLine(m_Center.x + Math.round(m_fInnerSliderRadius), m_Center.y,
				m_Center.x + Math.round(m_fOuterSliderRadius), m_Center.y);

		g.dispose();
	}


	//*************************************************************************

	/* Draw the color selection rectangle. The S and B values
	 * are interpolated via interpolateHSBinRectangle()
	 * 
	 */
	private void drawColorRectangle()
	{
		Graphics2D g = (Graphics2D) m_squareImage.getGraphics();

		g.setBackground(new Color(0, 0, 0, 0));
		g.clearRect(0, 0, m_squareImage.getWidth(), m_squareImage.getHeight());

		int x = m_colorRectangle.x, y = m_colorRectangle.y;

		// hsb values for interpolation
		float hsb[] = new float[3];

		for (; y <= m_colorRectangle.y + m_colorRectangle.height; y++)
		{
			for (x = m_colorRectangle.x; x <= m_colorRectangle.x
					+ m_colorRectangle.width; x++)
			{
				interpolateHSBinRectangle(x, y, hsb);
				m_squareImage.setRGB(x, y,
						Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
			}
		}

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.lightGray);

		// make the outline wider, so the color picker can't
		// accidentally select a pixel on it.

		Rectangle temp = new Rectangle(m_colorRectangle);
		temp.x -= 1;
		temp.y -= 1;
		temp.width += 1;
		temp.height += 1;
		g.draw(temp);
		g.dispose();
	}


	//*************************************************************************

	/**
	 * Draws remaining UI elements. Please note that everything is drawn with
	 * absolute positioning and visual correctness relies on the fact that the
	 * entire component cannot be resized - the code won't work well with a
	 * resizable one.
	 * 
	 * @param g
	 */

	private void draw_HSB_UI(
			Graphics g)
	{
		//paint HSB UI components
		g.drawImage(m_ringImage, 0, 0, this);
		g.drawImage(m_squareImage, 0, 0, this);
		g.drawImage(m_sliderImage, 0, 0, this);

		Graphics2D g2d = (Graphics2D) g;

		//helper variables
		Point2D.Float marker_center = new Point2D.Float();
		float mid_radius = 0;

		// -------------------- MARKERS --------------------

		// Ring marker	

		/* change marker color for better constrast for 
		 * the following hue range. Similar logic controls the 
		 * colors of other markers, depending on saturation
		 * and brightness values.
		 */

		int hue_angle = Math.round(m_fBaseHue * 359);
		boolean light = ((hue_angle > 35) & (hue_angle < 215));
		g2d.setColor((light == false) ? Color.white : Color.black);

		/* Use the value of H/S/B as an angle to estimate
		 * the index into one of precalculated sine/cosine arrays
		 * and then use the retrieved trig values to specify
		 * markers' centers in polar coordinates. 
		 * The small fractional values (like 2pi/hue_steps)
		 * subtracted in conversion to an angle are required
		 * because the trig arrays are initialized with 
		 * values between 0 and x, EXCLUDING x.
		 */

		float temp = m_fBaseHue * (float) (2 * PI - 2 * PI / HUE_STEPS);
		temp *= (HUE_STEPS / (2 * PI));

		int idx = Math.round(temp);

		mid_radius = 0.5f * (m_fInnerRingRadius + m_fOuterRingRadius);

		marker_center.setLocation(mid_radius * _ring_cosines[idx] + m_Center.x,
				mid_radius * _ring_sines[idx] + m_Center.y);

		g2d.drawOval(Math.round(marker_center.x) - MARKER_RADIUS,
				Math.round(marker_center.y) - MARKER_RADIUS, 2 * MARKER_RADIUS,
				2 * MARKER_RADIUS);

		// Saturation marker
		boolean ss = light | (m_fBaseSaturation < 0.65);
		g.setColor((ss == false) ? Color.white : Color.black);

		temp = m_fBaseSaturation * (float) ((PI / 2) - (PI / 2) / SLIDER_STEPS);
		temp *= SLIDER_STEPS / (PI / 2);

		idx = Math.round(temp);

		mid_radius = 0.5f * (m_fInnerSliderRadius + m_fOuterSliderRadius);

		marker_center.setLocation(mid_radius * _s_slider_cosines[idx]
				+ m_Center.x, mid_radius * _s_slider_sines[idx] + m_Center.y);

		g2d.drawOval(Math.round(marker_center.x) - MARKER_RADIUS,
				Math.round(marker_center.y) - MARKER_RADIUS, 2 * MARKER_RADIUS,
				2 * MARKER_RADIUS);

		// Brightness marker
		boolean dd = light & (m_fBaseBrightness > 0.65);
		g.setColor((dd == false) ? Color.white : Color.black);

		temp = m_fBaseBrightness * (float) ((PI / 2) - (PI / 2) / SLIDER_STEPS);
		temp *= SLIDER_STEPS / (PI / 2);

		idx = Math.round(temp);

		mid_radius = 0.5f * (m_fInnerSliderRadius + m_fOuterSliderRadius);

		marker_center.setLocation(mid_radius * _b_slider_cosines[idx]
				+ m_Center.x, mid_radius * _b_slider_sines[idx] + m_Center.y);

		g2d.drawOval(Math.round(marker_center.x) - MARKER_RADIUS,
				Math.round(marker_center.y) - MARKER_RADIUS, 2 * MARKER_RADIUS,
				2 * MARKER_RADIUS);

		// Color rectangle marker	
		g.setColor(Color.white);

		g2d.drawOval(Math.round(m_colorMarkerPosition.x) - MARKER_RADIUS,
				Math.round(m_colorMarkerPosition.y) - MARKER_RADIUS,
				2 * MARKER_RADIUS, 2 * MARKER_RADIUS);

		// ---------------- COLOR SAMPLE --------------------
		g2d.setColor(Color.black);
		g2d.drawRect(4, 4, 31, 31);

		g2d.setColor(new Color(Color.HSBtoRGB(m_fHue, m_fSaturation,
				m_fBrightness)));
		g2d.fillRect(5, 5, 30, 30);

		// Draw mode icon
		g2d.drawImage(m_PalIcon, 2, m_iHeight - 26, this);

		// ------------- HSB STATUS INFORMATION --------------
		g2d.setColor(Color.white);

		FontMetrics ff = g2d.getFontMetrics();
		int string_width;

		String hue = "H:" + String.valueOf((int) (m_fHue * 359)) + "\u00BA";
		string_width = ff.stringWidth("H:360%");

		g2d.drawString(hue, COMPONENT_SIZE - string_width - 1,
				COMPONENT_SIZE - 28);

		String sat = "S:" + String.valueOf((int) (m_fSaturation * 100)) + "%";
		string_width = ff.stringWidth("S:100%");

		g2d.drawString(sat, COMPONENT_SIZE - string_width - 1,
				COMPONENT_SIZE - 15);

		String br = "B:" + String.valueOf((int) (m_fBrightness * 100)) + "%";
		string_width = ff.stringWidth("B:100%");

		g2d.drawString(br, COMPONENT_SIZE - string_width - 1,
				COMPONENT_SIZE - 3);

	}


	//*************************************************************************

	private void draw_PAL_UI(
			Graphics g)
	{
		// palette image 
		g.drawImage(m_paletteImage, 0, 0, this);

		// mode icon
		g.drawImage(m_HSBicon, 2, m_iHeight - 26, this);

		// RGB status info		
		g.setColor(Color.white);

		FontMetrics ff = g.getFontMetrics();
		int string_width;

		String rgb = " R:" + String.valueOf((int) (m_fRed * 255)) + " G:"
				+ String.valueOf((int) (m_fGreen * 255)) + " B:"
				+ String.valueOf((int) (m_fBlue * 255));

		string_width = ff.stringWidth(rgb);

		g.drawString(rgb, m_Center.x - string_width / 2, COMPONENT_SIZE - 12);

	}


	//*************************************************************************

	//----------------------------- Interpolators -----------------------------

	/* Repositions the HSB color picker circle based on the
	 * current S and B values. Basically this is the inverse
	 * op of the interpolateHSBinRectangle() method.
	 */
	private void positionHSBColorPicker()
	{
		int x = (int) Math.round((1.0f - m_fSaturation)
				* m_colorRectangle.getWidth())
				+ m_colorRectangle.x;

		int y = (int) Math.round((1.0f - m_fBrightness)
				* m_colorRectangle.getHeight())
				+ m_colorRectangle.y;

		m_colorMarkerPosition.setLocation(x, y);
	}


	//*************************************************************************

	/* Interpolates S and B values based on position of 
	 * a given point inside the color selection rectangle
	 * Here, the interpolation is intentionally non-linear,
	 * so that the useful range of tones/shades is increased.
	 * For the sake of keeping the code clean and concise,
	 * the hsb[] array actually contains all three color
	 * components, but the hue value is kept untouched.
	 */

	private void interpolateHSBinRectangle(
			int x,
			int y,
			float[] hsb)
	{
		float saturation = 0, brightness = 0;

		// for now, use sqrt - interpolation,
		// this seems to increase the dynamic range
		// of color shades

		float xx = x - m_colorRectangle.x;
		xx /= m_colorRectangle.width; // -> [0,1]

		//xx = (float)Math.sqrt(xx); 		
		saturation = m_fBaseSaturation * (1.0f - xx);

		float yy = y - m_colorRectangle.y;
		yy /= m_colorRectangle.height;

		//yy *= yy; //here, the interpolation is quadratic instead!
		brightness = m_fBaseBrightness * (1 - yy);

		hsb[0] = m_fBaseHue; //FIXME: this line could be wrong! 
		hsb[1] = saturation;
		hsb[2] = brightness;

		clampHSB(hsb);
		//done
	}


	//*************************************************************************

	/* Interpolates saturation values from the position
	 * of the cursor on the S.Slider. The coordinates
	 * are converted to polar representation, and the 
	 * resulting angle is then used to calculate the
	 * corresponding S value. hue and brightness are
	 * set to base values.
	 */

	private void interpolateSfromSlider(
			int x,
			int y,
			float hsb[])
	{
		float saturation;
		double temp = 0;

		double xx = x - m_Center.x;
		double yy = y - m_Center.y;
		double length = Math.sqrt(xx * xx + yy * yy);

		// convert to polar coordinates
		xx /= length;
		yy /= length;
		temp = Math.asin(yy); // map to angle (in radians)
		temp /= (PI / 2); // -> [0,1]		

		saturation = (float) (1.0 - temp);

		hsb[0] = m_fBaseHue; //FIXME: make sure this is correct!
		hsb[1] = saturation;
		hsb[2] = m_fBaseBrightness;

		clampHSB(hsb);
	}


	//*************************************************************************

	/* Interpolates brightness values from the
	 * B.Slider.
	 */
	private void interpolateBfromSlider(
			int x,
			int y,
			float hsb[])
	{
		float brightness;
		double temp = 0;

		double xx = x - m_Center.x;
		double yy = y - m_Center.y;
		double length = Math.sqrt(xx * xx + yy * yy);

		// convert to polar coordinates
		xx /= length;
		yy /= length;
		temp = Math.acos(xx); // map to angle (in radians)
		temp /= (PI / 2); // -> [0,1]	

		brightness = (float) (temp);

		hsb[0] = m_fBaseHue; //FIXME: make sure this is correct!
		hsb[1] = m_fBaseSaturation;
		hsb[2] = brightness;

		clampHSB(hsb);
	}


	//*************************************************************************

	/* Interpolates hue values from the hue selector ring 
	 */
	private void interpolateHfromRing(
			int x,
			int y,
			float hsb[])
	{
		float hue;
		double phi = 0;

		double xx = x - m_Center.x;
		double yy = -1.0 * (y - m_Center.y);
		double length = Math.sqrt(xx * xx + yy * yy);

		// convert to polar coordinates
		xx /= length;
		yy /= length;

		phi = Math.acos(xx);

		// shift angle in other to map the entire [0,2PI] range
		if ((yy < 0.0) | (Math.abs(yy) < DBL_EPSILON))
		{
			// here the acos directly maps [-1,1] to [0,PI] 
		}
		else
		{
			// here we need to map [PI,0] to [PI,2PI]
			phi = 2 * PI - phi;
		}

		hue = (float) (phi / (2 * PI)); // map angle to [0,1]

		hsb[0] = hue;
		hsb[1] = m_fBaseSaturation;
		hsb[2] = m_fBaseBrightness;

		clampHSB(hsb);
	}


	//*************************************************************************

	private void clampHSB(
			float[] hsb)
	{
		/* Due to some roundoff errors
		 * the saturation and brightness values never become
		 * strictly 0.0 or 1.0, so we need to persuade them
		 * a little... Another fact is that the color square
		 * is very small so there's some inherent imprecision
		 * when picking a specific pixel in it, and it gets
		 * worse near the edges. The S values are either
		 * near 0 or 1 -> the colors can be considered either
		 * fully saturated or completely gray. Similar 
		 * argumentation applies to brightness values.
		 * Since it is practically impossible to visually 
		 * distinguish such close shades / tones (especially
		 * in such a confined space), these values are clamped
		 * to 0 or 1, respectively.
		 */

		if (hsb[1] < 0.015f)
			hsb[1] = 0.0f;

		if (hsb[1] > 0.99f)
			hsb[1] = 1.0f;

		if (hsb[2] < 0.05f)
			hsb[2] = 0.0f;

		if (hsb[2] > 0.99f)
			hsb[2] = 1.0f;
	}


	//*************************************************************************

	/*
	 *  Maps internal values of HSB to RGB
	 * 
	 */
	private void HSBtoRGB()
	{
		/* ok, this double conversion is stupid. I guess I'll
		 * have to rip the HSB to RGB code from awt.Color and splice
		 * it here.
		 */

		int color = Color.HSBtoRGB(m_fHue, m_fSaturation, m_fBrightness);

		// decode
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = (color >> 0) & 0xFF;

		m_fRed = r / 255.0f;
		m_fGreen = g / 255.0f;
		m_fBlue = b / 255.0f;
	}


	//*************************************************************************

	private void RGBtoHSB()
	{
		float hsb[] = new float[3];

		Color.RGBtoHSB((int) (m_fRed * 255), (int) (m_fGreen * 255),
				(int) (m_fBlue * 255), hsb);

		m_fHue = hsb[0];
		m_fSaturation = hsb[1];
		m_fBrightness = hsb[2];
	}


	//*************************************************************************

	/* Point-in-ring test
	 * Circles are assumed to be centered in the component
	 * 
	 * test works like this:
	 * if(point is in outer circle) AND
	 *   (point is NOT in inner circle)
	 *   return TRUE;
	 *   otherwise return FALSE;
	 * 
	 */
	private boolean pointInRing(
			Point p,
			float inner_radius,
			float outer_radius)
	{
		boolean in_outer = (((p.x - m_Center.x) * (p.x - m_Center.x) + (p.y - m_Center.y)
				* (p.y - m_Center.y)) < (outer_radius * outer_radius));

		boolean in_inner = (((p.x - m_Center.x) * (p.x - m_Center.x) + (p.y - m_Center.y)
				* (p.y - m_Center.y)) < (inner_radius * inner_radius));

		if ((in_outer == true) && (in_inner == false))
			return true;
		else
			return false;
	}


	//*************************************************************************

	/**
	 * Tests whether a point lies in the given arc. WARNING: This test only
	 * works for arcs spanning exactly 90 degress - signs of point's coordinates
	 * are simply compared to the signs specifying the corresponding quadrant of
	 * the coordinate system (i.e. "+,+" for the first quadrant etc.). This test
	 * will fail for ANY other arc.
	 * 
	 * @param p
	 *            The point
	 * @param inner_radius
	 *            The inner radius
	 * @param outer_radius
	 *            The outer radius
	 * @param signx
	 *            sign of x coordinate, must be either 1 or -1
	 * @param signy
	 *            sign of y coordinate, must be either 1 or -1
	 * @return true, if the given point lies inside, false else
	 */

	private boolean pointInArc(
			Point p,
			float inner_radius,
			float outer_radius,
			int signx,
			int signy)
	{
		if (pointInRing(p, inner_radius, outer_radius))
		{
			// compare signs
			if ((sign(p.x - m_Center.x) == signx)
					&& (sign(p.y - m_Center.y) == signy))
				return true;
		}
		return false;
	}


	//*************************************************************************

	/* Used by isPointInArc() */
	private int sign(
			int arg)
	{
		if (arg > 0)
			return 1;
		else if (arg < 0)
			return -1;
		else
			return 0; // when arg == 0		
	}

	//*************************************************************************

}
