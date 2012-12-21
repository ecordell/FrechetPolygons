/*
 * File: JGLSystemHub.java
 * Created on May 23, 2006 by ibr
 *
 * TODO Write documentation
 */
package anja.SwingFramework.GL;


import org.jdom.Element; // for xml methods...

import java.awt.geom.AffineTransform;
import anja.SwingFramework.JSystemHub;


public class JGLSystemHub
		extends JSystemHub
{

	//*************************************************************************
	//                             Public constants
	//*************************************************************************

	//*************************************************************************
	//                             Private constants
	//*************************************************************************

	//*************************************************************************
	//                             Class variables
	//*************************************************************************

	//*************************************************************************
	//                        Public instance variables
	//*************************************************************************

	//*************************************************************************
	//                       Protected instance variables
	//*************************************************************************

	//*************************************************************************
	//                        Private instance variables
	//*************************************************************************

	private JGLDisplayPanel	m_GLDisplayPanel;


	//*************************************************************************
	//                              Constructors
	//*************************************************************************

	public JGLSystemHub()
	{
		super();
		m_GLDisplayPanel = null;
	}


	//*************************************************************************
	//                         Public instance methods
	//*************************************************************************

	/**
	 * 
	 * @return Reference to the GL display panel.
	 * @throws NullPointerException
	 *             if the GL display hasn't been set.
	 */
	public JGLDisplayPanel getGLDisplayPanel()
	{
		if (m_GLDisplayPanel == null)
		{
			new NullPointerException("GL display hasn't been initialized!");
		}
		return m_GLDisplayPanel;
	}


	//*************************************************************************

	/**
	 * Sets the GL display panel. Can be a reference to any subclass of
	 * {@link JGLDisplayPanel}
	 * 
	 * @param displayPanel
	 */
	public void setGLDisplayPanel(
			JGLDisplayPanel displayPanel)
	{
		m_GLDisplayPanel = displayPanel;
	}


	//*************************************************************************

	/**
	 * Used by JAbstractEditor to determine whether a GL display panel component
	 * is present, for loading/saving XML settings.
	 * 
	 * @return always <code><b>true</b></code> here to indicate that the
	 *         JGLDisplayPanel component is present!
	 */
	public boolean hasGLDisplayPanel()
	{
		return true;
	}


	//*************************************************************************

	/**
	 * This function updates affine transforms throughout the entire system.
	 * Used primarily by various {@link anja.SwingFramework.CoordinateSystem} system methods to
	 * synchronize transforms in other components after a coordinate system has
	 * been modified.
	 * 
	 * @param tx
	 *            New affine transform to be used
	 */
	public void updateTransform(
			AffineTransform tx)
	{
		super.updateTransform(tx); // everything else...

		if (m_GLDisplayPanel != null)
		{
			// sync affine transform changes to JGLDisplayPanel
			m_GLDisplayPanel.updateAffineTransform(tx);
		}
	}


	//*************************************************************************

	/**
	 * Reimplemented from {@link JSystemHub#saveGLSettingsToXML()}
	 * 
	 * 
	 */
	public Element saveGLSettingsToXML()
	{
		return m_GLDisplayPanel.saveSettingsToXML();
	}


	//*************************************************************************

	/**
	 * Reimplemented from {@link JSystemHub#loadGLSettingsFromXML(Element)}
	 * 
	 */
	public void loadGLSettingsFromXML(
			Element subRoot)
	{
		m_GLDisplayPanel.loadSettingsFromXML(subRoot);
	}

	//*************************************************************************
	//                       Protected instance methods
	//*************************************************************************

	//*************************************************************************
	//                        Private instance methods
	//*************************************************************************

}
