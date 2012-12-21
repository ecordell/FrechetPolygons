/*
 * Created on Apr 4, 2005
 *
 * JVirtualTrackball.java
 * 
 */
package anja.SwingFramework.GL;


import java.awt.*;
import java.awt.event.*;
//import net.java.games.jogl.*;


/**
 * This class implements a virtual trackball navigator for three-dimensional
 * scenes. The navigation is similar to using a real trackball. (This type has
 * also been sometimes referred to as a 'glass sphere' navigator, i.e. as if the
 * 3-d scene is encased inside a transparent sphere which you can rotate).
 * 
 * <br>Navigation controls are simple: <br><b>Left-drag </b> to rotate the
 * scene. <br><b>Right-drag </b> to zoom in / out. Alternatively, the mouse
 * wheel can be used if present. <br> <b>Press and hold SHIFT while you
 * left-drag </b> the mouse to pan around the scene.
 * 
 * <br>The trackball object itself is windowless - it's just an abstract
 * mathematical representation of the navigation tool. You provide it with the
 * basic information about the parent window (by passing parent window events to
 * it), and can at any time retrieve the resulting transformation matrix
 * (rotation OR rotation+translation) for further use. There's also an option to
 * automatically redraw the parent window, which is usually what you'll use if
 * you want to immediately see the results.
 * 
 * <br>How to use: construct the object, call allowAnimation(), setZoomRange()
 * etc. to customize the behavior. Then pass the necessary events (see event
 * handlers below) from your parent window to the trackball object. You can
 * retrieve the transformation matrix any time by calling getTransformMatrix().
 * See my sample application code for details.
 * 
 * @author Ibragim Kouliev
 * 
 *         <br>Note: Because this class has been ported to Java from C++/Qt, all
 *         comments were originally written for Doxygen documentation extractor,
 *         and as a result, some of them may look (or read)a little weird.
 */
public class JVirtualTrackball
		implements MouseListener, MouseMotionListener, MouseWheelListener,
		ComponentListener, Runnable // experimental
{

	//*************************************************************************
	// 			         Public constants
	//*************************************************************************

	// projection modes
	public static final int		PERSPECTIVE_PROJECTION	= 0x01;
	public static final int		ORTHO_PROJECTION		= 0x02;

	//*************************************************************************
	// 			         Private constants
	//*************************************************************************

	private static final float	TRACKBALLSIZE			= 0.8f;

	// ported from original m_enTrackballMode enum type

	private static final int	TRANSLATE				= 0x01;
	private static final int	ROTATE					= 0x02;
	private static final int	ZOOM					= 0x04;

	//*************************************************************************
	// 				  Class variables
	//*************************************************************************

	//*************************************************************************
	// 			      Private instance variables
	//*************************************************************************

	private Component			m_pParentWindow;				// Pointer to the parent object

	private boolean				m_bTrackballActive;			// Trackball idle / active status
	private boolean				m_bAnimationAllowed;			// Animation status

	private boolean				m_bHasChanged;					// Tracks internal state changes

	private int					m_iParentWidth, m_iParentHeight;	// Parent window extents	
	private int					m_iOldMouseX, m_iOldMouseY;		// current mouse coordinates

	/*
	 * used to detect changes in mouse position during mouse release
	 * event in order to activate/deactivate animation
	 */
	private int					m_iPrevOldMouseX, m_iPrevOldMouseY;

	//normalized mouse coords
	private float				m_fNormMouseX, m_fNormMouseY;

	//previous ---||----
	private float				m_fOldNormMouseX, m_fOldNormMouseY;

	private int					m_enTrackballMode;					// Current mode flag

	// x,y translation and zoom factors
	private float				m_fTranslateX;
	private float				m_fTranslateY;
	private float				m_fZoom;

	// zoom increment / decrement value
	private float				m_fZoomDelta;

	// projection type
	private int					m_uiProjectionType;

	// minimum / maximum zoom distances
	private float				m_fMinZoomDistance;
	private float				m_fMaxZoomDistance;

	// minimum / maximum ortho volume extents (assuming a symmetircal volume)
	private float				m_fMinOrthoVolumeExtents;
	private float				m_fMaxOrthoVolumeExtents;

	private float				m_fOrthoVolumeExtents;

	/*
	 * the following stuff is used to calculate the axis and angle of
	 * rotation for the scene while in trackball mode
	 */

	// direction vectors and resulting rotation axis
	private float				m_afTrackVector1[], m_afTrackVector2[];
	private float				m_afRotationAxis[];

	// current and previous rotation quaternions
	private float				m_afCurQuad[];
	private float				m_afLastQuad[];

	// current transformation matrix
	private float				m_afTransformationMatrix[];

	// temporary matrix for various purposes
	private float				m_afTempMatrix[];
	private float				m_afZeroMatrix[];						// for memset simulation

	// temporary storage for quaternion operations
	private float				_af_t1[], _af_t2[], _af_t3[], _af_delta[];

	private Thread				m_animationThread;
	private JGLDisplayPanel		_glPanel;									// temporary


	/*  disabled Qt stuff until I port the entire thing to Java 
	
	//QCursor m_ParentCursor;	  // Parent window's cursor
	// Own cursor object
	QCursor m_OwnCursor;
	// Visual aids and related variables
	
	// orbiting/rotation/fov state cursors and their icons
	QPixmap* m_pTranslationIcon;
	QCursor* m_pTranslationCursor;
	
	QPixmap* m_pRotationIcon;
	QCursor* m_pRotationCursor;
	
	QPixmap* m_pFovIcon;
	QCursor* m_pFovCursor;
	
	QTimer*  m_pAnimationTimer; // animation timer */

	//*************************************************************************
	// 				    Constructors
	//*************************************************************************

	/**
	 * Constructs a JVirtualTrackball object. The parent can be a reference to
	 * any subclass of {@link Component}. This will also set all initial
	 * states/params that are required.
	 * 
	 * @param parent
	 *            A pointer to a parent window (widget)
	 */

	public JVirtualTrackball(
			Component parent)
	{
		m_pParentWindow = parent;
		initialize();

		// hook up event listeners
		parent.addComponentListener(this);
		parent.addMouseListener(this);
		parent.addMouseMotionListener(this);
		parent.addMouseWheelListener(this);
	}


	/** temporary */
	public JVirtualTrackball(
			Component parent,
			JGLDisplayPanel panel)
	{
		this(parent);
		_glPanel = panel;
	}


	//*************************************************************************
	// 			      Public instance methods
	//*************************************************************************

	// ------------------------ Getters / setters ------------------------

	/**
	 * 
	 * Provided for convenience. Returns current 3x3 rotation matrix. The matrix
	 * format is <b>column-major </b>.
	 * 
	 * @return Current rotation matrix
	 */
	public float[] getRotationMatrix()
	{
		return m_afTempMatrix;
	}


	/**
	 * Returns current orientation in form of a unit quaternion stored in a
	 * 4-element floating point array.
	 * 
	 * @return Current orientation quaternion
	 * 
	 */
	public float[] getCurrentOrientation()
	{
		return m_afCurQuad;
	}


	/**
	 * Returns current compound (rotation + translation) 4x4 homogenous
	 * transformation matrix.
	 * 
	 * The matrix format is <b>column-major </b>.
	 * 
	 * @return Current transformation matrix
	 */
	public float[] getTransformMatrix()
	{
		return m_afTransformationMatrix;
	}


	/**
	 * Returns the current extends of ortho projection volume
	 * 
	 * @return Extents
	 */
	public float getOrthoVolumeExtents()
	{
		return m_fOrthoVolumeExtents;
	}


	/**
	 * Sets the projection type to perspective or orthographic.
	 * 
	 * @param type
	 *            The projection type can be PERSPECTIVE_PROJECTION or
	 *            ORTHO_PROJECTION
	 */
	public void setProjectionType(
			int type)
	{
		m_uiProjectionType = type;
	}


	// Set new zoom ranges...(both values are positive object-space z-coords)

	/**
	 * Sets the new zoom range.
	 * 
	 * @param minZoomDistance
	 *            - minimum zoom distance
	 * @param maxZoomDistance
	 *            - maximum zoom distance
	 * 
	 *            <br>Both parameters need to be specified in positive
	 *            object-space z-coordinates. This function <b>SHOULD</b> be
	 *            called before the trackball is first used. It <b>CAN</b> be
	 *            called also when thetrackball is idle. I wouldn't recommend
	 *            calling it on the fly (f.e. during animation) yet, as such
	 *            behaviour is undefined (for now).
	 * 
	 * */
	public void setZoomRange(
			float minZoomDistance,
			float maxZoomDistance)
	{
		m_fMinZoomDistance = minZoomDistance;
		m_fMaxZoomDistance = maxZoomDistance;

		// incomplete!

		/*
		 * This function is supposed to be called once after
		 * initialization to set the desired zoom range.... don't know
		 * what else to add here yet...
		 * 
		 * If the zoom range is modified on the fly, the translation
		 * transform will need to be updated as well!
		 *  
		 */
	}


	/**
	 * Sets the zoom range for the orthographic projection mode
	 * 
	 * @param minExtents
	 *            The minimum extent
	 * @param maxExtents
	 *            The maximum extent
	 */
	public void setOrthoVolumeRange(
			float minExtents,
			float maxExtents)
	{
		m_fMinOrthoVolumeExtents = minExtents;
		m_fMaxOrthoVolumeExtents = maxExtents;

		m_fOrthoVolumeExtents = minExtents;
	}


	/**
	 * Is the trackball active
	 * 
	 * @return <code>true</code> if the trackball is active, otherwise
	 *         <code>false</code>
	 */
	public boolean isOn()
	{
		return m_bTrackballActive;
	}


	// ------------------------- Interaction -----------------------------

	/**
	 * Toggles the trackball between <b>ON</b> and <b>OFF</b> states.
	 * 
	 * This is provided as a convenience function, since it can be easily
	 * connected to toggle-type buttons, check boxes etc. in Swing
	 * 
	 * 
	 */
	public void toggle()
	{
		m_bTrackballActive = !m_bTrackballActive;

		if (true == m_bTrackballActive)
		{
			turnOn();
		}
		else
		{
			turnOff();
		}
	}


	/**
	 * Enables trackball
	 * 
	 */
	public void turnOn()
	{
		m_bTrackballActive = true;
	}


	/**
	 * Disables trackball
	 */
	public void turnOff()
	{
		m_bTrackballActive = false;

		// TODO: stop animation timer...
		//if(m_bAnimationAllowed) m_pAnimationTimer->stop();
	}


	/**
	 * Performs a full reset of the trackball (all parameters), and stops scene
	 * animation if it was active.
	 * 
	 */
	public void fullReset()
	{
		//TODO: stop animation(if any)
		//if(m_bAnimationAllowed) m_pAnimationTimer->stop();

		resetParams();
		m_pParentWindow.repaint();
	}


	/**
	 * Resets the zoom factor.
	 * 
	 */
	public void resetZoom()
	{
		m_fZoom = m_fMaxZoomDistance;
		m_fZoomDelta = 0.5f;
		m_fOrthoVolumeExtents = m_fMaxOrthoVolumeExtents;

		this.update();
	}


	/**
	 * 
	 * Resets translation part of the transformation matrix and translation
	 * params.
	 * 
	 */
	public void resetPosition()
	{
		m_fTranslateX = m_fTranslateY = 0.0f;
		m_pParentWindow.repaint();
	}


	/**
	 * Resets the rotation part of transform matrix, as well as all pertinent
	 * internal states and parameters. Stops animation if it was active.
	 * 
	 */
	public void resetRotation()
	{
		//TODO: stop animation (if any)
		//if(m_bAnimationAllowed) m_pAnimationTimer->stop();

		// mouse coords
		m_iOldMouseX = m_iOldMouseY = -1;
		m_iPrevOldMouseX = m_iPrevOldMouseY = -1;

		m_afTrackVector1[0] = m_afTrackVector1[1] = m_afTrackVector1[2] = 0.0f;
		m_afTrackVector2[0] = m_afTrackVector2[1] = m_afTrackVector2[2] = 0.0f;
		m_afRotationAxis[0] = m_afRotationAxis[1] = m_afRotationAxis[2] = 0.0f;

		// set current quaternion to null transform
		m_afCurQuad[0] = m_afCurQuad[1] = m_afCurQuad[2] = 0.0f;
		m_afCurQuad[3] = 1.0f;

		//memset(m_afTransformationMatrix, 0x00, 16*sizeof(float));
		//memset(m_afTempMatrix, 0x00, 16*sizeof(float));
		System.arraycopy(m_afZeroMatrix, 0, m_afTempMatrix, 0, 16);
		System.arraycopy(m_afZeroMatrix, 0, m_afTransformationMatrix, 0, 16);

		m_pParentWindow.repaint();
	}


	/**
	 * Enables/disables animation.
	 * 
	 * @param animIsAllowed
	 *            Set to <b>true </b> to enable animation, or <b>false </b> to
	 *            disable it.
	 * 
	 *            <br>This function can be called at any time (even during
	 *            animation) to enable/disable continous scene rotation. The
	 *            changes will take effect the next time animation is
	 *            started/stopped. *
	 * 
	 */
	public void allowAnimation(
			boolean animIsAllowed)
	{
		//enable / disable animation when idle
		m_bAnimationAllowed = animIsAllowed;
	}


	/**
	 * Inherited from {@link java.lang.Thread}
	 */
	@Override
	public void run()
	{
		while (true)
		{
			animateRotation();
			//Thread.yield();

			try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException ex)
			{

			}
		}
		//Thread.yield();
	}


	//*************************************************************************
	//                          Event handlers
	//*************************************************************************

	/**
	 * 
	 * This uses the resize event to detect changes in the size of parent window
	 * and retrieves new parent width/height values. This is necessary to modify
	 * the internal normalized 2-d mouse coordinate system of the trackball.
	 * 
	 * Note: <br>As a general note, all event handlers within the trackball
	 * control <b>DO NOT </b> capture the parent window events - i.e. the events
	 * can be further processed by the parent after they have processed by the
	 * trackball.
	 * 
	 * @param event
	 *            Resize event from the parent window.
	 */
	public void componentResized(
			ComponentEvent event)
	{
		m_iParentWidth = m_pParentWindow.getWidth();
		m_iParentHeight = m_pParentWindow.getHeight();
	}


	/**
	 * 
	 * Processes mouse press event from the parent. The trackball mode
	 * (rotation/move/zoom) is selected depended on pressed mouse button (and
	 * some keyboard buttons as well).
	 * 
	 * @param mouseEvent
	 *            Mouse press event from the parent window.
	 */
	public void mousePressed(
			MouseEvent mouseEvent)
	{
		if (m_bTrackballActive == true)
		{
			if (mouseEvent.getButton() == MouseEvent.BUTTON1)
			{
				if ((mouseEvent.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK)
				{
					//m_pParentWindow->setCursor(*m_pTranslationCursor);
					m_enTrackballMode = TRANSLATE;
				}
				else
				{
					m_enTrackballMode = ROTATE;

					//m_pParentWindow->setCursor(*m_pRotationCursor);

					//get and normalize mouse coordinates to [-1...1]
					m_fNormMouseX = ((float) (mouseEvent.getX()) / m_iParentWidth) * 2.0f - 1.0f;

					/*
					 * Y-Coordinate has to be negated because the
					 * window coordinate system has the positive
					 * Y-axis 'looking down'
					 */

					m_fNormMouseY = -(((float) (mouseEvent.getY()) / m_iParentHeight) * 2.0f - 1.0f);

					// set "old" coordinates to current coordinates
					m_iOldMouseX = mouseEvent.getX();
					m_iOldMouseY = mouseEvent.getY();

					// save previous mouse coordinates
					m_iPrevOldMouseX = m_iOldMouseX;
					m_iPrevOldMouseY = m_iOldMouseY;

					// save previous normalized mouse coordinates
					m_fOldNormMouseX = m_fNormMouseX;
					m_fOldNormMouseY = m_fNormMouseY;
				}
			}
			// switch to zoom mode
			else if (mouseEvent.getButton() == MouseEvent.BUTTON3)
			{
				//m_pParentWindow->setCursor(*m_pFovCursor);
				m_enTrackballMode = ZOOM;
			}
		}
	}


	/**
	 * Processes mouse release events from the parent window. This also starts /
	 * stops the continous scene animation, if it is enabled, depending on
	 * distance between previous and current mouse coordinates.
	 * 
	 * The default parent cursor will be restored when the mouse is released.
	 * 
	 * @param mouseEvent
	 *            Mouse release events from the parent window
	 * 
	 */
	public void mouseReleased(
			MouseEvent mouseEvent)
	{
		if (m_bTrackballActive == true)
		{
			if (m_enTrackballMode == ROTATE)
			{
				// m_iPrevOldX and m_iPrevOldY changed during mouse move

				if ((Math.abs(m_iPrevOldMouseX - m_iOldMouseX) > 1)
						|| (Math.abs(m_iPrevOldMouseY - m_iOldMouseY) > 1))
				{
					// TODO: Animation handler
					// animate				
					if (m_bAnimationAllowed)
					{
						//m_animationThread = new Thread(this);
						//m_animationThread.start();
					}
					//m_pAnimationTimer->start(ANIMATION_INTERVAL, false);

				}
				else
				// or remained the same
				{
					m_iOldMouseX = -1;
					m_iOldMouseY = -1;

					//TODO: Animation handler
					// stop animation
					if (m_bAnimationAllowed)
					{
						//if(m_animationThread != null)
						//m_animationThread.stop();
					}
					// m_pAnimationTimer->stop();

				}
			}

			// TODO: Cursor change
			// restore default cursor
			//m_pParentWindow->setCursor(m_OwnCursor);
		}
	}


	/**
	 * Processes mouse move events from the parent window. The moving mouse
	 * coordinates are projected onto a 3-d unit sphere and the resulting
	 * vectors are used to construct the axis and angle of rotation. In
	 * translate and zoom modes the mouse coordinates are used to modify
	 * XY-translation and zoom factors.
	 * 
	 * @param mouseEvent
	 *            Mouse move event from the parent window
	 * 
	 *            <p>Note: Zoom speed is dependend on the current zoom distance,
	 *            and will be slower/faster if this distance is <
	 *            <b>(1/3)*(maxZoomDistance-minZoomDistance) </b> or >
	 *            <b>(1/3)*(maxZoomDistance-minZoomDistance) </b>, respectively.
	 * 
	 */
	public void mouseDragged(
			MouseEvent mouseEvent)
	{
		if (m_bTrackballActive == true)
		{
			//System.out.println("Drag");

			switch (m_enTrackballMode)
			{
				case TRANSLATE:

					/*
					 Ok, here's the deal:
					 Translation and zoom modes are probably not optimal
					 yet (in terms of intuitiveness of use),
					 but seem to be stable so far...
					 */

					// when in translation mode
					if (mouseEvent.getX() < m_iOldMouseX)
					{
						m_fTranslateX -= 0.05f;
					}
					else if (mouseEvent.getX() > m_iOldMouseX)
					{
						m_fTranslateX += 0.05f;
					}

					if (mouseEvent.getY() < m_iOldMouseY)
					{
						m_fTranslateY += 0.05f;
					}
					else if (mouseEvent.getY() > m_iOldMouseY)
					{
						m_fTranslateY -= 0.05f;
					}
					break;

				case ROTATE:

					// in rotation mode

					//get and normalize mouse coordinates to [-1...1]
					m_fNormMouseX = ((float) (mouseEvent.getX()) / m_iParentWidth) * 2.0f - 1.0f;

					m_fNormMouseY = -(((float) (mouseEvent.getY()) / m_iParentHeight) * 2.0f - 1.0f);

					setTrackball(m_afLastQuad, m_fOldNormMouseX,
							m_fOldNormMouseY, m_fNormMouseX, m_fNormMouseY);

					multQuads(m_afLastQuad, m_afCurQuad, m_afCurQuad);

					break;

				case ZOOM:

					// increase / decrease zoom factor
					if (m_iOldMouseX != -1)
					{
						// 'slow down' or 'speed up' zoom depending on Z distance...
						if (m_fZoom < 0.3333 * (m_fMaxZoomDistance - m_fMinZoomDistance))

						{
							m_fZoomDelta = 0.1f;
						}
						else
						{
							m_fZoomDelta = 0.5f;
						}

						if (mouseEvent.getY() > m_iOldMouseY)
						{
							m_fZoom += m_fZoomDelta;
						}
						else if (mouseEvent.getY() < m_iOldMouseY)
						{
							m_fZoom -= m_fZoomDelta;
						}

						if (m_fZoom < m_fMinZoomDistance)
							m_fZoom = m_fMinZoomDistance;

						if (m_fZoom > m_fMaxZoomDistance)
							m_fZoom = m_fMaxZoomDistance;
					}
					break;

				default: // do nothing
					break;

			} // switch statement ends

			// save previous mouse coordinates
			m_iPrevOldMouseX = m_iOldMouseX;
			m_iPrevOldMouseY = m_iOldMouseY;

			m_iOldMouseX = mouseEvent.getX();
			m_iOldMouseY = mouseEvent.getY();

			// save previous mouse coordinates
			m_fOldNormMouseX = m_fNormMouseX;
			m_fOldNormMouseY = m_fNormMouseY;

			this.update();
		}

	}


	public void mouseWheelMoved(
			MouseWheelEvent event)
	{

		if (m_bTrackballActive == true)
		{
			//m_pParentWindow->setCursor(*m_pFovCursor);

			switch (m_uiProjectionType)
			{
				case PERSPECTIVE_PROJECTION:

					// changes the zoom factor
					m_fZoom += (event.getWheelRotation() > 0) ? -m_fZoomDelta
							: m_fZoomDelta;

					// 'slow down' or 'speed up' zoom depending on Z distance...
					if (m_fZoom < 0.3333 * (m_fMaxZoomDistance - m_fMinZoomDistance))
					{
						m_fZoomDelta = 0.1f;
					}
					else
					{
						m_fZoomDelta = 0.5f;
					}

					if (m_fZoom < m_fMinZoomDistance)
						m_fZoom = m_fMinZoomDistance;

					if (m_fZoom > m_fMaxZoomDistance)
						m_fZoom = m_fMaxZoomDistance;

					update();

					break;

				case ORTHO_PROJECTION:

					m_fOrthoVolumeExtents += (event.getWheelRotation() > 0) ? -m_fZoomDelta
							: m_fZoomDelta;

					if (m_fOrthoVolumeExtents < m_fMinOrthoVolumeExtents)
						m_fOrthoVolumeExtents = m_fMinOrthoVolumeExtents;

					if (m_fOrthoVolumeExtents > m_fMaxOrthoVolumeExtents)
						m_fOrthoVolumeExtents = m_fMaxOrthoVolumeExtents;

					// TODO: Ortho mode resize
					//GLView* temp = (GLView*)m_pParentWindow;
					//temp->resizeOrtho();

					// test ortho resize
					//_glPanel.resizeOrtho();

					break;
			}

			m_pParentWindow.repaint();
		}
	}


	// ----------------- unused methods -----------------------
	public void mouseClicked(
			MouseEvent event)
	{}


	public void mouseEntered(
			MouseEvent event)
	{}


	public void mouseExited(
			MouseEvent event)
	{}


	public void mouseMoved(
			MouseEvent event)
	{}


	public void componentMoved(
			ComponentEvent event)
	{}


	public void componentShown(
			ComponentEvent event)
	{}


	public void componentHidden(
			ComponentEvent event)
	{}


	//*************************************************************************
	// 			     Protected instance methods
	//*************************************************************************

	// disabled default constructor
	protected JVirtualTrackball()
	{}


	protected void animateRotation()
	{
		// Animate the scene rotation
		multQuads(m_afLastQuad, m_afCurQuad, m_afCurQuad);
		this.update();
	}


	/**
	 * One-time initialization of all necessary internal states and parameters.
	 **/
	protected void initialize()
	{
		// init internal states and parameters
		m_bTrackballActive = false;
		m_bAnimationAllowed = false;

		/* Disabled Qt cursor stuff
		// create custom cursors for various trackball modes
		m_pTranslationIcon = new QPixmap(const_cast<const char**>
					  (TRACKBALL_TRANSLATE_CURSOR));
		m_pTranslationCursor = new QCursor(*m_pTranslationIcon);
		
		m_pRotationIcon = new QPixmap(const_cast<const char**>
					(TRACKBALL_ORBIT_CURSOR));
		m_pRotationCursor = new QCursor(*m_pRotationIcon);
		
		m_pFovIcon = new QPixmap(const_cast<const char**>
				      (TRACKBALL_FOV_CURSOR));
		m_pFovCursor = new QCursor(*m_pFovIcon);
		
		m_ParentCursor = m_pParentWindow->cursor();
		
		m_pAnimationTimer = new QTimer(this);
		
		connect(m_pAnimationTimer, SIGNAL(timeout()), 
			    this, SLOT(animateRotation()) );*/

		// init storage

		//TODO: check everything for correct dimensions
		m_afTransformationMatrix = new float[16];
		m_afTempMatrix = new float[16];
		m_afCurQuad = new float[4];
		m_afLastQuad = new float[4];
		m_afRotationAxis = new float[3];
		m_afTrackVector1 = new float[3];
		m_afTrackVector2 = new float[3];

		m_afZeroMatrix = new float[16];

		// init temporary storage
		_af_t1 = new float[4];
		_af_t2 = new float[4];
		_af_t3 = new float[4];
		_af_delta = new float[3];

		//set initial view position
		/*
		 * memset() is not necessary here, for, as opposed to C/C++,
		 * all arrays in Java are initialized to zero when they're
		 * first allocated
		 *  
		 */
		//memset(m_afTransformationMatrix, 0x00, 16*sizeof(float));

		// identity transformation
		m_afTransformationMatrix[0] = 1.0f; // [0][0]
		m_afTransformationMatrix[5] = 1.0f; // [1][1]
		m_afTransformationMatrix[10] = 1.0f; // [2][2]

		// and translation to max. zoom distance away from the origin
		m_afTransformationMatrix[12] = 0.0f; // [3][0]
		m_afTransformationMatrix[13] = 0.0f; // [3][1]
		m_afTransformationMatrix[14] = 0.0f; // [3][2]
		m_afTransformationMatrix[15] = 1.0f; // [3][3]

		m_uiProjectionType = PERSPECTIVE_PROJECTION;
		m_fOrthoVolumeExtents = 5.0f;

		resetParams();
	}


	// Resets the viewing transform parameters
	protected void resetParams()
	{
		// default zoom range (in object space Z-coordinates, of course)
		m_fMaxZoomDistance = 0.0f;
		m_fMinZoomDistance = 0.0f;

		// position / zoom coordinates
		m_fZoom = m_fMaxZoomDistance;
		m_fOrthoVolumeExtents = m_fMaxOrthoVolumeExtents;

		m_fTranslateX = 0.0f;
		m_fTranslateY = 0.0f;
		m_fZoomDelta = 0.5f;

		// mouse coords
		m_iOldMouseX = m_iOldMouseY = -1;
		m_iPrevOldMouseX = m_iPrevOldMouseY = -1;

		m_afTrackVector1[0] = m_afTrackVector1[1] = m_afTrackVector1[2] = 0.0f;
		m_afTrackVector2[0] = m_afTrackVector2[1] = m_afTrackVector2[2] = 0.0f;
		m_afRotationAxis[0] = m_afRotationAxis[1] = m_afRotationAxis[2] = 0.0f;

		// set current quaternion to null transform
		m_afCurQuad[0] = m_afCurQuad[1] = m_afCurQuad[2] = 0.0f;
		m_afCurQuad[3] = 1.0f;

		//memset(m_afTempMatrix,	0x00, 16*sizeof(float));		
		//memset(m_afTransformationMatrix, 0x00, 16*sizeof(float));

		System.arraycopy(m_afZeroMatrix, 0, m_afTempMatrix, 0, 16);
		System.arraycopy(m_afZeroMatrix, 0, m_afTransformationMatrix, 0, 16);

		//set initial view position

		// identity transformation
		m_afTransformationMatrix[0] = 1.0f; // [0][0]
		m_afTransformationMatrix[5] = 1.0f; // [1][1]
		m_afTransformationMatrix[10] = 1.0f; // [2][2]

		// and translation to max. zoom distance away from the origin
		m_afTransformationMatrix[12] = 0.0f; // [3][0]
		m_afTransformationMatrix[13] = 0.0f; // [3][1]
		m_afTransformationMatrix[14] = -m_fZoom; // [3][2]
		m_afTransformationMatrix[15] = 1.0f; // [3][3]

	}


	//*************************************************************************
	// 			      Private instance methods
	//*************************************************************************

	/*
	 * given the two (x,y) pairs of normalized mouse coordinates,
	 * generate the corresponding orientation quaternion, which is
	 * returned in pfQuad
	 */

	private void setTrackball(
			float[] pfQuad,
			float x1,
			float y1,
			float x2,
			float y2)
	{
		float f_phi; // How much to rotate about axis.

		//float	_af_delta[ 3 ]; // factored out as well...
		float f_dist;

		// nothing to do - return "null transform" quaternion
		if ((x1 == x2) && (y1 == y2))
		{
			pfQuad[0] = 0.0f;
			pfQuad[1] = 0.0f;
			pfQuad[2] = 0.0f;
			pfQuad[3] = 1.0f;
			return;
		}

		m_afTrackVector1[0] = x1; //V
		m_afTrackVector1[1] = y1; //V

		m_afTrackVector1[2] = projectToSphere(TRACKBALLSIZE,
				m_afTrackVector1[0], m_afTrackVector1[1]);

		m_afTrackVector2[0] = x2; //V
		m_afTrackVector2[1] = y2; //V

		m_afTrackVector2[2] = projectToSphere(TRACKBALLSIZE,
				m_afTrackVector2[0], m_afTrackVector2[1]);

		// Now, we want the cross product of P1 and P2.
		m_afRotationAxis[0] = (m_afTrackVector2[1] * m_afTrackVector1[2])
				- (m_afTrackVector2[2] * m_afTrackVector1[1]);

		m_afRotationAxis[1] = (m_afTrackVector2[2] * m_afTrackVector1[0])
				- (m_afTrackVector2[0] * m_afTrackVector1[2]);

		m_afRotationAxis[2] = (m_afTrackVector2[0] * m_afTrackVector1[1])
				- (m_afTrackVector2[1] * m_afTrackVector1[0]);

		f_dist = 1.0f / (float) Math.sqrt(m_afRotationAxis[0]
				* m_afRotationAxis[0] + m_afRotationAxis[1]
				* m_afRotationAxis[1] + m_afRotationAxis[2]
				* m_afRotationAxis[2]);

		// normalize the rotation axis vector
		m_afRotationAxis[0] *= f_dist;
		m_afRotationAxis[1] *= f_dist;
		m_afRotationAxis[2] *= f_dist;

		// Figure out how much to rotate around that axis.
		_af_delta[0] = m_afTrackVector1[0] - m_afTrackVector2[0];
		_af_delta[1] = m_afTrackVector1[1] - m_afTrackVector2[1];
		_af_delta[2] = m_afTrackVector1[2] - m_afTrackVector2[2];

		f_dist = (float) (Math.sqrt(_af_delta[0] * _af_delta[0] + _af_delta[1]
				* _af_delta[1] + _af_delta[2] * _af_delta[2]) / (2.0 * TRACKBALLSIZE));

		// Avoid problems with out-of-control values.
		if (f_dist > 1.0f)
		{
			f_dist = 1.0f;
		}
		else if (f_dist < -1.0f)
		{
			f_dist = -1.0f;
		}

		// get the rotation angle
		f_phi = 2.0f * (float) Math.asin(f_dist);

		// construct rotation quaternion
		f_dist = (float) Math.sin(f_phi * 0.5);
		pfQuad[0] = m_afRotationAxis[0] * f_dist;
		pfQuad[1] = m_afRotationAxis[1] * f_dist;
		pfQuad[2] = m_afRotationAxis[2] * f_dist;
		pfQuad[3] = (float) Math.cos(f_phi * 0.5);

		/*
		f_phi = f_dist;
		pfQuad[ 0 ] = m_afRotationAxis[ 0 ] * f_dist;
		pfQuad[ 1 ] = m_afRotationAxis[ 1 ] * f_dist;
		pfQuad[ 2 ] = m_afRotationAxis[ 2 ] * f_dist;
		pfQuad[ 3 ] = sqrt(1-f_phi*f_phi); */

		/* alternatively, one could do this....
		
		// get the cos(phi/2) by taking dot product of one of normalized 
		// direction vectors (either one), and the half-way vector between 
		// them
		
		pfQuad[ 0 ] = af_axis[ 0 ] * f_dist;
		pfQuad[ 1 ] = af_axis[ 1 ] * f_dist;
		pfQuad[ 2 ] = af_axis[ 2 ] * f_dist;
		pfQuad[ 3 ] = 1-(f_dist*f_dist);
		
		*/
	}


	/* projects a given (x,y) pair to a sphere of desired radius.
	 Returns the corresponding z coordinate */

	private float projectToSphere(
			float fRadius,
			float fX,
			float fY)
	{
		float f_dist;
		float f_t;
		float f_z;

		f_dist = (float) Math.sqrt(fX * fX + fY * fY);

		// apparently sqrt(2)/2 
		if (f_dist < fRadius * 0.70710678118654752440f)
		{
			// Inside sphere.
			f_z = (float) Math.sqrt(fRadius * fRadius - f_dist * f_dist);
		}
		else
		{
			// On hyperbola.
			f_t = fRadius / 1.41421356237309504880f; // sqrt(2)
			f_z = f_t * f_t / f_dist;
		}

		return f_z;
	}


	// Multiplies two quaternions and normalizes the result
	private void multQuads(
			float[] pfQuad1,
			float[] pfQuad2,
			float[] pfDest)
	{
		// these have been factored out...
		//float	af_t1[ 4 ], af_t2[ 4 ], af_t3[ 4 ];

		_af_t1[0] = pfQuad1[0] * pfQuad2[3];
		_af_t1[1] = pfQuad1[1] * pfQuad2[3];
		_af_t1[2] = pfQuad1[2] * pfQuad2[3];

		_af_t2[0] = pfQuad2[0] * pfQuad1[3];
		_af_t2[1] = pfQuad2[1] * pfQuad1[3];
		_af_t2[2] = pfQuad2[2] * pfQuad1[3];

		_af_t3[0] = (pfQuad2[1] * pfQuad1[2]) - (pfQuad2[2] * pfQuad1[1]);

		_af_t3[1] = (pfQuad2[2] * pfQuad1[0]) - (pfQuad2[0] * pfQuad1[2]);

		_af_t3[2] = (pfQuad2[0] * pfQuad1[1]) - (pfQuad2[1] * pfQuad1[0]);

		pfDest[0] = _af_t1[0] + _af_t2[0] + _af_t3[0];
		pfDest[1] = _af_t1[1] + _af_t2[1] + _af_t3[1];
		pfDest[2] = _af_t1[2] + _af_t2[2] + _af_t3[2];

		pfDest[3] = pfQuad1[3]
				* pfQuad2[3]
				- (pfQuad1[0] * pfQuad2[0] + pfQuad1[1] * pfQuad2[1] + pfQuad1[2]
						* pfQuad2[2]);

		normalizeQuad(pfDest);
	}


	//normalize a quaternion. Produces corresponding unit quaternion
	private void normalizeQuad(
			float[] pfDest)
	{
		float f_mag;

		f_mag = (float) Math.sqrt(pfDest[0] * pfDest[0] + pfDest[1] * pfDest[1]
				+ pfDest[2] * pfDest[2] + pfDest[3] * pfDest[3]);

		pfDest[0] /= f_mag;
		pfDest[1] /= f_mag;
		pfDest[2] /= f_mag;
		pfDest[3] /= f_mag;
	}


	// Builds a rotation matrix from a given quaternion
	private void buildRotationMatrix(
			float[] afMatrix,
			float[] pfQuad)
	{
		afMatrix[0] = 1.0f - 2.0f * (pfQuad[1] * pfQuad[1] + pfQuad[2]
				* pfQuad[2]);

		afMatrix[1] = 2.0f * (pfQuad[0] * pfQuad[1] - pfQuad[2] * pfQuad[3]);
		afMatrix[2] = 2.0f * (pfQuad[2] * pfQuad[0] + pfQuad[1] * pfQuad[3]);
		afMatrix[3] = 0.0f;

		afMatrix[4] = 2.0f * (pfQuad[0] * pfQuad[1] + pfQuad[2] * pfQuad[3]);

		afMatrix[5] = 1.0f - 2.0f * (pfQuad[2] * pfQuad[2] + pfQuad[0]
				* pfQuad[0]);

		afMatrix[6] = 2.0f * (pfQuad[1] * pfQuad[2] - pfQuad[0] * pfQuad[3]);
		afMatrix[7] = 0.0f;

		afMatrix[8] = 2.0f * (pfQuad[2] * pfQuad[0] - pfQuad[1] * pfQuad[3]);
		afMatrix[9] = 2.0f * (pfQuad[1] * pfQuad[2] + pfQuad[0] * pfQuad[3]);

		afMatrix[10] = 1.0f - 2.0f * (pfQuad[1] * pfQuad[1] + pfQuad[0]
				* pfQuad[0]);
		afMatrix[11] = 0.0f;

		afMatrix[12] = 0.0f;
		afMatrix[13] = 0.0f;
		afMatrix[14] = 0.0f;
		afMatrix[15] = 1.0f;
	}


	// Updates internal states and transformation matrices
	private void update()
	{
		buildRotationMatrix(m_afTransformationMatrix, m_afCurQuad);

		// simulate memcpy()		
		System.arraycopy(m_afTransformationMatrix, 0, m_afTempMatrix, 0, 16);
		//memcpy(m_afTempMatrix, m_afTransformationMatrix, 16*sizeof(float));

		// embed translation into the transformation matrix
		m_afTransformationMatrix[12] = m_fTranslateX; //  [3][0]
		m_afTransformationMatrix[13] = m_fTranslateY; // [3][1]
		m_afTransformationMatrix[14] = -m_fZoom; // [3][2]
		m_afTransformationMatrix[15] = 1.0f; // [3][3]

		/*
		 * Note: the z-coord translation is set to -m_fZoom here,
		 * since we have to translate the 'camera' away from the
		 * origin. Hence the negative factor!
		 */

		// update the parent window (essentially calls the rendering op)
		m_pParentWindow.repaint();

	}

} // JVirtualTrackball

