package anja.swing;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.*;

import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;

import javax.swing.*;
import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JSeparator;
import javax.swing.JPanel;
import javax.swing.JToggleButton;


/**
 * 
 * The <code>CoordinateSystem</code> class defines a virtual cartesian
 * coordinate system that can be used in conjunction with JDisplayPanel or
 * similar classes.
 * 
 * <br><br>The coordinate system parameters can be controlled via a system of
 * menus, which can be generated with the {@link #createMenu(ActionListener)}
 * method. This method may also notify an action listener about the selected
 * menu item - the resulting action event contains one one of the constants
 * MENU_... which identifies the selected menu item.
 * 
 * <br><br>This class provides built-in mouse / keyboard event handlers which
 * allow easy modifications to the coordinate system. See method descriptions
 * for details.
 * 
 * @version 0.9 27.08.2004
 * @author Sascha Ternes, Ibragim Kuliyev
 * 
 *         <br> TODO: Allow finer grid subdivision<br> TODO: Improve and
 *         centralize event handling if possible<br> TODO: Add missing method
 *         documentation<br> TODO: Modify action event handler to use a switch
 *         statement<br>
 * 
 *         TODO: Misc.code cleanup. Any other suggestions?
 */

public class CoordinateSystem
		implements ActionListener, KeyListener
{

	// *************************************************************************
	// Public constants
	// *************************************************************************

	/**
	 * Konstante fuer <code>xaxis</code>, <code>yaxis</code>,
	 * <code>xmarkings</code>, <code>ymarkings</code>, <code>xlettering</code>,
	 * <code>ylettering</code>, und <code>grid</code>: nicht sichtbar
	 */
	public static final int			INVISIBLE	= 0;

	/**
	 * Konstante fuer <code>xaxis</code> und <code>yaxis</code>: einfache Gerade
	 */
	public static final int			LINE		= 11;
	/**
	 * Konstante fuer <code>xaxis</code> und <code>yaxis</code>: Gerade mit
	 * Pfeilspitze am positiven Ende
	 */
	public static final int			ARROW		= 12;
	/**
	 * Konstante fuer <code>xaxis</code> und <code>yaxis</code>: Gerade mit
	 * Pfeilspitzen an beiden Enden
	 */
	public static final int			DOUBLEARROW	= 13;

	/**
	 * Konstante fuer <code>xmarkings</code>, <code>ymarkings</code>,
	 * <code>xlettering</code>, <code>ylettering</code> und <code>grid</code>:
	 * Markierungen / Beschriftung / Gitter nur in groben Abstaenden
	 */
	public static final int			BROAD		= 21;
	/**
	 * Konstante fuer <code>xmarkings</code>, <code>ymarkings</code>,
	 * <code>xlettering</code>, <code>ylettering</code> und <code>grid</code>:
	 * Markierungen / Beschriftung / Gitter in groben und feinen Abstaenden
	 */
	public static final int			FINE		= 22;

	/**
	 * Text des Menuepunkts <i>Show</i>
	 */
	public static final String		ITEM_SHOW	= "Show";
	/**
	 * Text des Menuepunkts <i>Hide</i>
	 */
	public static final String		ITEM_HIDE	= "Hide";
	/**
	 * Text des Menuepunkts <i>Grid -> None</i>
	 */
	public static final String		ITEM_NONE	= "None";
	/**
	 * Text des Menuepunkts <i>Grid -> Broad</i>
	 */
	public static final String		ITEM_BROAD	= "Broad";
	/**
	 * Text des Menuepunkts <i>Grid -> Fine</i>
	 */
	public static final String		ITEM_FINE	= "Fine";

	/**
	 * Text des Menuepunkts <i>Grid -> Reset</i>
	 */
	public static final String		ITEM_RESET	= "Reset";

	/**
	 * ActionCommand des Menuepunkts <i>Show</i>
	 */
	public static final String		MENU_SHOW	= "1";
	/**
	 * ActionCommand des Menuepunkts <i>Hide</i>
	 */
	public static final String		MENU_HIDE	= "2";
	/**
	 * ActionCommand des Menuepunkts <i>Grid -> None</i>
	 */
	public static final String		MENU_NONE	= "3";
	/**
	 * ActionCommand des Menuepunkts <i>Grid -> Broad</i>
	 */
	public static final String		MENU_BROAD	= "4";
	/**
	 * ActionCommand des Menuepunkts <i>Grid -> Broad</i>
	 */
	public static final String		MENU_FINE	= "5";
	/**
	 * ActionCommand des Menuepunkts <i>Grid -> Reset</i>
	 */
	public static final String		MENU_RESET	= "6";

	private static final String		ITEM_DOTS	= "Dots";
	private static final String		ITEM_LINES	= "Lines";
	private static final String		ITEM_COLOR	= "Grid color..";

	private static final String		CMD_DOTS	= "7";
	private static final String		CMD_LINES	= "8";
	private static final String		CMD_COLOR	= "9";
	private static final String		CMD_SNAP	= "10";

	// *************************************************************************
	// Private constants
	// *************************************************************************

	// Laenge und Breite der Pfeilspitzen:
	private static final int		_RUW		= 8;
	private static final int		_RUH		= 4;
	// eine Pfeilspitze:
	private static final int[]		_RX			= { 0, _RUW, 0 };
	private static final int[]		_RY			= { -_RUH, 0, _RUH };

	// halbe Laenge der Markierungen:
	private static final int		_BROAD		= 3;
	private static final int		_FINE		= 1;

	// *************************************************************************
	// Public variables
	// *************************************************************************

	/**
	 * Aussehen der x-Achse
	 */
	public int						xaxis;
	/**
	 * Aussehen der y-Achse
	 */
	public int						yaxis;

	/**
	 * Art der Markierungen auf der x-Achse
	 */
	public int						xmarkings;
	/**
	 * Art der Markierungen auf der y-Achse
	 */
	public int						ymarkings;

	/**
	 * Art der Beschriftung auf der x-Achse
	 */
	public int						xlettering;
	/**
	 * Art der Beschriftung auf der y-Achse
	 */
	public int						ylettering;

	/**
	 * Art des Gitters
	 */
	public int						grid;

	/**
	 * legt fest, ob die x-Achse benannt werden soll
	 */
	public boolean					namex;
	/**
	 * legt fest, ob die y-Achse benannt werden soll
	 */
	public boolean					namey;

	/**
	 * Pixelposition des Koordinatenursprungs
	 */
	public Point					origin;

	/**
	 * Pixellaenge einer Einheit auf der x-Achse
	 */
	public double					ppx;
	/**
	 * Pixellaenge einer Einheit auf der y-Achse
	 */
	public double					ppy;

	/**
	 * Abstand der groben Markierungen auf der x-Achse
	 */
	public double					broadpitchx;
	/**
	 * Abstand der groben Markierungen auf der y-Achse
	 */
	public double					broadpitchy;
	/**
	 * Abstand der feinen Markierungen auf der x-Achse
	 */
	public double					finepitchx;
	/**
	 * Abstand der feinen Markierungen auf der y-Achse
	 */
	public double					finepitchy;

	// *************************************************************************
	// Private variables
	// *************************************************************************

	// common register
	private Register				_reg;

	// externally registered listener
	private ActionListener			_listener;

	/*-----------  flags  -------------*/
	private boolean					_drawGridLines;
	private boolean					_snapToGrid;
	private boolean					_is_visible;						// visibility flag

	/* ------- UI menus & menu elements -------- */
	private JMenu					_menu;

	// Show/hide menu item
	private JMenuItem				_show_hide_item;

	//snap toggle box
	private JCheckBoxMenuItem		_snap_to_grid_box;

	// Grid mode stuff (none, broad, fine)
	private JRadioButtonMenuItem	_grid_none_item;
	private JRadioButtonMenuItem	_grid_broad_item;
	private JRadioButtonMenuItem	_grid_fine_item;
	private ButtonGroup				_grid_mode_group;

	// grid type stuff
	private JMenu					_grid_type_menu;
	private JRadioButtonMenuItem	_grid_dots_item;
	private JRadioButtonMenuItem	_grid_lines_item;

	// toolbox stuff
	private JToolBar				_toolBox;
	private JToggleButton			_show_hide_button;
	private JToggleButton			_snap_button;

	/* ------- Attributes etc. --------- */

	// coordinate system colors
	private Color					_lineColor;
	private Color					_gridColor;

	// user-defined names for the coordinate axes
	private String					xAxisLabel;
	private String					yAxisLabel;

	// coordinate system transformation matrix
	private AffineTransform			_affine_transform;

	/* ---------- Various internal helper variables ----------- */

	// zoom center position in image and object spaces
	private Point					_zoom_pixel;
	private Point2D.Double			_zoom_point;
	private Point					_pressed_point;
	private int						_mouse_button;

	// Temporary vars for arrow drawing
	private int[]					_px;
	private int[]					_py;

	// Initial settings
	private boolean					_bak_is_visible;
	private Point					_bak_origin;
	private double					_bak_ppx;
	private double					_bak_ppy;
	private double					_bak_broadpitchx;
	private double					_bak_broadpitchy;
	private double					_bak_finepitchx;
	private double					_bak_finepitchy;
	private int						_bak_xaxis;
	private int						_bak_yaxis;
	private int						_bak_xmarkings;
	private int						_bak_ymarkings;
	private int						_bak_xlettering;
	private int						_bak_ylettering;
	private int						_bak_grid;
	private boolean					_bak_namex;
	private boolean					_bak_namey;

	private String					_bak_xAxisLabel;
	private String					_bak_yAxisLabel;


	// *************************************************************************
	// Constructors
	// *************************************************************************

	/*
	* Verbotener Konstruktor.
	*/
	private CoordinateSystem()
	{}


	/**
	 * Erzeugt ein unsichtbares Default-Koordinatensystem mit folgenden
	 * Parametern:<ul> <li>Der Ursprung des Koordinatensystems liegt in der
	 * oberen linken Ecke des Zeichenfensters auf Pixel (0/0).</li> <li>Die
	 * Pixellaengen <code>ppx</code> und <code>ppy</code> werden auf 1
	 * gesetzt.</li> <li>x- und y-Achse werden als Doppelpfeilgeraden angezeigt
	 * und benannt, jedoch wird kein Gitter gezeichnet.</li> <li>Grobe
	 * Markierungen werden alle 100 Einheiten und feine Markierungen alle 10
	 * Einheiten gesetzt. Die groben Markierungen werden beschriftet.</li></ul>
	 * Dieses Koordinatensystem registriert sich im uebergebenen
	 * Register-Objekt.
	 * 
	 * @param register
	 *            das Register-Objekt
	 */
	public CoordinateSystem(
			Register register)
	{

		_init(register);
		origin = new Point();
		ppx = 1.0;
		ppy = 1.0;
		broadpitchx = 100.0;
		broadpitchy = 100.0;
		finepitchx = 10.0;
		finepitchy = 10.0;

		//initial axes names
		xAxisLabel = "x";
		yAxisLabel = "f(x)";

		saveSettings();

	} // CoordinateSystem


	/**
	 * Erzeugt ein unsichtbares unverzerrtes Standard-Koordinatensystem mit dem
	 * Ursprung in der linken oberen Ecke des Zeichenfensters, bei dem die x-
	 * und y-Achse den gleichen Massstab verwenden und das gleiche Aussehen
	 * erhalten (benannte Doppelpfeilachsen mit groben beschrifteten und feinen
	 * unbeschrifteten Markierungen, kein Gitter).<br> Dieses Koordinatensystem
	 * registriert sich im uebergebenen Register-Objekt.
	 * 
	 * @param register
	 *            das Register-Objekt
	 * @param ppxy
	 *            Laenge einer Einheit in Pixeln
	 */
	public CoordinateSystem(
			Register register,
			double ppxy)
	{

		_init(register);
		origin = new Point();
		ppx = ppxy;
		ppy = ppxy;

		//initial axes names
		xAxisLabel = "x";
		yAxisLabel = "f(x)";

		adjustMarkings();
		saveSettings();

	} // CoordinateSystem


	/**
	 * Erzeugt ein unsichtbares unverzerrtes Standard-Koordinatensystem, bei dem
	 * die x- und y-Achse den gleichen Massstab verwenden und das gleiche
	 * Aussehen erhalten (benannte Doppelpfeilachsen mit groben beschrifteten
	 * und feinen unbeschrifteten Markierungen, kein Gitter).<br> Dieses
	 * Koordinatensystem registriert sich im uebergebenen Register-Objekt.
	 * 
	 * @param register
	 *            das Register-Objekt
	 * @param origin
	 *            die Position des Ursprungs in Pixelkoordinaten
	 * @param ppxy
	 *            Laenge einer Einheit in Pixeln
	 */
	public CoordinateSystem(
			Register register,
			Point origin,
			double ppxy)
	{

		_init(register);
		this.origin = origin;
		ppx = ppxy;
		ppy = ppxy;

		//initial axes names
		xAxisLabel = "x";
		yAxisLabel = "f(x)";

		adjustMarkings();
		saveSettings();

	} // CoordinateSystem


	/**
	 * Erzeugt ein unsichtbares unverzerrtes Standard-Koordinatensystem, bei dem
	 * die x- und y-Achse den gleichen Massstab verwenden und das gleiche
	 * Aussehen erhalten (benannte Doppelpfeilachsen mit groben beschrifteten
	 * und feinen unbeschrifteten Markierungen, kein Gitter).<br> Dieses
	 * Koordinatensystem registriert sich im uebergebenen Register-Objekt.
	 * 
	 * @param register
	 *            das Register-Objekt
	 * @param origin
	 *            die Position des Ursprungs in Pixelkoordinaten
	 * @param ppxy
	 *            Laenge einer Einheit in Pixeln
	 * @param finepitch
	 *            Abstand der feinen Markierungen
	 * @param broadpitch
	 *            Abstand der groben Markierungen als Vielfaches der feinen
	 *            Abstaende
	 */
	public CoordinateSystem(
			Register register,
			Point origin,
			double ppxy,
			double finepitch,
			int broadpitch)
	{

		_init(register);
		this.origin = origin;
		ppx = ppxy;
		ppy = ppxy;
		finepitchx = finepitch;
		finepitchy = finepitch;
		broadpitchx = broadpitch * finepitch;
		broadpitchy = broadpitch * finepitch;

		//initial axes names
		xAxisLabel = "x";
		yAxisLabel = "f(x)";

		saveSettings();

	} // CoordinateSystem


	/**
	 * FIXME: Unfinished constructor with JPanel argument
	 * 
	 */

	public CoordinateSystem(
			JPanel parent,
			Point origin,
			double ppxy)
	{

		//_init(); // without Register

		this.origin = origin;
		ppx = ppxy;
		ppy = ppxy;
		/*
		finepitchx = finepitch;
		finepitchy = finepitch;
		broadpitchx = broadpitch * finepitch;
		broadpitchy = broadpitch * finepitch;*/

		//initial axes names
		xAxisLabel = "X";
		yAxisLabel = "Y";

		//initial color etc.
		_lineColor = Color.black;
		_gridColor = Color.gray;
		_drawGridLines = false;

		saveSettings();

	}


	// *************************************************************************
	// Public methods
	// *************************************************************************

	// Getters / Setters

	/**
	 * Sets the name string for the <b>horizontal</b> (X) axis
	 * 
	 * @param label
	 *            New name for the axis
	 */

	public void setX_Label(
			String label)
	{
		xAxisLabel = label;
	}


	/**
	 * Sets the name string for the <b>vertical</b> (Y) axis
	 * 
	 * @param label
	 *            New name for the axis
	 */

	public void setY_Label(
			String label)
	{
		yAxisLabel = label;
	}


	/**
	 * Sets the color to be used when drawing the coordinate axes etc.
	 * @param c
	 *            new coordinate system color
	 */
	public void setColor(
			Color c)
	{
		_lineColor = c;
	}


	/**
	 * Sets the color of grid lines / dots.
	 * @param c
	 *            new color
	 */
	public void setGridColor(
			Color c)
	{
		_gridColor = c;
	}


	public void enableGridLines(
			boolean on)
	{
		_drawGridLines = on;

		if (on)
			_grid_lines_item.doClick();
		else
			_grid_dots_item.doClick();

	}


	public void toggleSnap()
	{
		_snapToGrid = !_snapToGrid;
		_snap_to_grid_box.doClick();
	}


	public boolean isGridEnabled()
	{
		return (grid == INVISIBLE) ? false : true;
	}


	public boolean gridTypeIsLines()
	{
		return _drawGridLines;
	}


	public void enableSnapToGrid(
			boolean on)
	{
		_snapToGrid = on;
	}


	public boolean isSnapEnabled()
	{
		return _snapToGrid;
	}


	/**
	 * Liefert den Sichtbarkeitsstatus dieses Koordinatensystems.
	 * 
	 * @return <code>true</code>, wenn der Status "sichtbar" ist, sonst
	 *         <code>false</code>
	 */
	public boolean isVisible()
	{
		return _is_visible;
	} // isVisible


	/**
	 * Setzt den Sichtbarkeitsstatus dieses Koordinatensystems.
	 * 
	 * @param visible
	 *            <code>true</code> setzt den Status auf "sichtbar",
	 *            <code>false</code> setzt den Status auf "unsichtbar"
	 */
	public void setVisible(
			boolean visible)
	{
		_is_visible = visible;
	} // setVisible


	/**
	 * Gets the current affine transform user space => screen space.
	 * 
	 * @return The transformation object
	 */

	public AffineTransform getWorldToScreenTransform()
	{
		return _affine_transform;
	}


	/*
	public AffineTransform getScreenToWorldTransform()
	{
		  // stub
	}*/

	/**
	 * Justiert die Abstaende der groben und feinen Markierungen basierend auf
	 * den aktuellen Pixellaengen neu. Dabei wird versucht, in einem Abstand von
	 * 20 bis 40 Pixeln feine Markierungen zu setzen. Davon ist jede zweite,
	 * vierte oder fuenfte eine grobe Markierung.
	 */
	public void adjustMarkings()
	{
		// Justierung der x-Achse:
		double a = 1.0;
		double b = 1.0;
		if (ppx / (a * b) < 20.0 /*20.0*/)
			while (ppx / (a * b) < 20.0 /*20.0*/)
			{
				if (a == 5.0)
					a = 4.0;
				else if (a == 1.0)
				{
					a = 5.0;
					b /= 10.0;
				} // if 
				else
					a /= 2.0;
			} // while
		else if (ppx / (a * b) >= 40.0 /*40.0*/)
			while (ppx / (a * b) >= 40.0 /*40.0*/)
			{
				if (a < 4.0)
					a *= 2.0;
				else if (a == 4.0)
					a = 5.0;
				else
				{
					a = 1.0;
					b *= 10.0;
				} // else
			} // while
		finepitchx = 1.0 / (a * b);
		if (a == 1.0)
			a = 2.0;
		broadpitchx = a * finepitchx;

		// Justierung der y-Achse:
		a = 1.0;
		b = 1.0;
		if (ppy / (a * b) < 20.0 /*20.0*/)
			while (ppy / (a * b) < 20.0 /*20.0*/)
			{
				if (a == 5.0)
					a = 4.0;
				else if (a == 1.0)
				{
					a = 5.0;
					b /= 10.0;
				} // if 
				else
					a /= 2.0;
			} // while
		else if (ppy / (a * b) >= 40.0 /*40.0*/)
			while (ppy / (a * b) >= 40.0 /*40.0*/)
			{
				if (a < 4.0)
					a *= 2.0;
				else if (a == 4.0)
					a = 5.0;
				else
				{
					a = 1.0;
					b *= 10.0;
				} // else
			} // while
		finepitchy = 1.0 / (a * b);
		if (a == 1.0)
			a = 2.0;
		broadpitchy = a * finepitchy;

	} // adjustMarkings


	/**
	 * Zentriert das Koordinatensystem mit seinem Ursprung in der Mitte des
	 * Anzeigepanels aus dem frueher registrierten <code>Register</code>-Objekt.
	 * Wenn dieses keinen Eintrag fuer das Anzeigepanel enthaelt, passiert
	 * nichts.
	 */
	public void center()
	{

		if (_reg.display == null)
			return;

		Dimension d = _reg.display.getCanvasSize();
		origin.x = d.width / 2;
		origin.y = d.height / 2;

		_makeXForm();

	} // center


	/**
	 * Zentriert das Koordinatensystem mit seinem Ursprung in der Mitte des
	 * angegebenen <code>Dimension</code>-Objekts.
	 * 
	 * @param size
	 *            das <code>Dimension</code>-Objekt
	 */
	public void center(
			Dimension size)
	{

		origin.x = size.width / 2;
		origin.y = size.height / 2;

		_makeXForm();

	} // center


	/**
	 * Convenience function, allows to use a <code>Rectangle</code> instead of a
	 * Dimension argument for centering the coordinate system.
	 * @see #center()
	 * 
	 * @param size
	 *            The new rectangle
	 */

	public void center(
			Rectangle size)
	{
		origin.x = size.width / 2; // + size.x;
		origin.y = size.height / 2; // + size.y;

		_makeXForm();

	}


	/**
	 * Liefert das Menue fuer dieses Koordinatensystem. Optional kann ein
	 * Listener registriert werden, der nach der Auswahl eines Menuepunkts
	 * benachrichtigt wird, beispielsweise um das Anzeigepanel neu zu zeichnen,
	 * das dieses Koordinatensystem enthaelt.
	 * 
	 * @param listener
	 *            der Listener, der nach der Auswahl eines Menuepunkts
	 *            benachrichtigt wird
	 */
	public JMenu createMenu(
			ActionListener listener)
	{

		if (_menu == null)
		{
			_listener = listener;
			_menu = new JMenu("Coordinate system");
			JMenu m = new JMenu("Grid");

			//m.setMnemonic('q');

			_grid_mode_group = new ButtonGroup();

			String text = ITEM_SHOW;
			String cmd = MENU_SHOW;

			if (_is_visible)
			{
				text = ITEM_HIDE;
				cmd = MENU_HIDE;
			} // if

			_show_hide_item = new JMenuItem(text);
			_show_hide_item.setActionCommand(cmd);
			_show_hide_item.addActionListener(this);
			_menu.add(_show_hide_item);

			_grid_none_item = new JRadioButtonMenuItem(ITEM_NONE, true);
			_grid_none_item.setActionCommand(MENU_NONE);
			_grid_none_item.addActionListener(this);
			_grid_mode_group.add(_grid_none_item);
			m.add(_grid_none_item);

			_grid_broad_item = new JRadioButtonMenuItem(ITEM_BROAD);
			_grid_broad_item.setActionCommand(MENU_BROAD);
			_grid_broad_item.addActionListener(this);
			_grid_mode_group.add(_grid_broad_item);
			m.add(_grid_broad_item);

			_grid_fine_item = new JRadioButtonMenuItem(ITEM_FINE);
			_grid_fine_item.setActionCommand(MENU_FINE);
			_grid_fine_item.addActionListener(this);
			_grid_mode_group.add(_grid_fine_item);
			m.add(_grid_fine_item);

			_menu.add(m);

			_menu.addSeparator();

			//added on 11.10.2004
			_grid_type_menu = new JMenu("Grid mode");

			ButtonGroup gg = new ButtonGroup();

			_grid_dots_item = new JRadioButtonMenuItem(ITEM_DOTS);

			_grid_dots_item.setActionCommand(CMD_DOTS);
			_grid_dots_item.addActionListener(this);

			_grid_lines_item = new JRadioButtonMenuItem(ITEM_LINES, true);

			_grid_lines_item.setActionCommand(CMD_LINES);
			_grid_lines_item.addActionListener(this);

			gg.add(_grid_dots_item);
			gg.add(_grid_lines_item);
			_grid_type_menu.add(_grid_dots_item);
			_grid_type_menu.add(_grid_lines_item);
			//dots.setSelected(true);

			_grid_type_menu.setEnabled(false);

			_menu.add(_grid_type_menu);
			_menu.add(new JSeparator());

			_snap_to_grid_box = new JCheckBoxMenuItem("Snap to grid", false);
			_snap_to_grid_box.setActionCommand(CMD_SNAP);
			_snap_to_grid_box.addActionListener(this);

			_snap_to_grid_box.setEnabled(false);

			_menu.add(_snap_to_grid_box);
			_menu.add(new JSeparator());

			JMenuItem color_item = new JMenuItem(ITEM_COLOR);
			color_item.setActionCommand(CMD_COLOR);
			color_item.addActionListener(this);

			_menu.add(color_item);

			_menu.add(new JSeparator());
			JMenuItem item = new JMenuItem(ITEM_RESET);
			item.setActionCommand(MENU_RESET);
			item.addActionListener(this);
			_menu.add(item);

		} // if

		return _menu;

	} // createMenu


	public JToolBar createToolBox()
	{
		if (_toolBox != null)
			return _toolBox;

		// create toolbar and buttons
		_toolBox = new JToolBar("Coordinate system", SwingConstants.HORIZONTAL);

		_toolBox.setFloatable(false); // can be overriden later
		_toolBox.setRollover(false);

		// show / hide
		_show_hide_button = new JToggleButton("S/H", true);
		_show_hide_button.setToolTipText("Show/hide coordinate system");

		String cmd = MENU_SHOW;
		if (_is_visible)
		{
			cmd = MENU_HIDE;
		}

		_show_hide_button.setActionCommand(cmd);
		_show_hide_button.addActionListener(this);

		_toolBox.add(_show_hide_button);
		_toolBox.add(new JToolBar.Separator());

		// snap to grid
		_snap_button = new JToggleButton("Snap", false);
		_snap_button.setToolTipText("Toggle snap to grid");
		_snap_button.setActionCommand(CMD_SNAP);
		_snap_button.addActionListener(this);

		_toolBox.add(_snap_button);

		return _toolBox;
	}


	/**
	 * Zeichnet das Koordinatensystem.
	 * 
	 * @param g
	 *            das <code>Graphics</code>-Objekt, in das gezeichnet werden
	 *            soll
	 * @param size
	 *            die Groesse der Zeichenflaeche von <code>g</code>
	 * 
	 *            FIXME: Optimize drawing code - it's kinda slow...
	 */
	public void draw(
			Graphics g,
			Dimension size)
	{

		int ax = 0;
		int ay = origin.y;
		int w = size.width;
		int h = size.height;
		// aeussere Grenzen fuer Markierungen und Gitter:
		int x1 = 0;
		int x2 = w - _RUW - 3;
		if (xaxis == DOUBLEARROW)
			x1 = _RUW + 3;
		if (xaxis == LINE)
			x2 = w - 1;
		int y1 = h - 1;
		int y2 = _RUW + 2;
		if (yaxis == DOUBLEARROW)
			y1 = h - _RUW - 2;
		if (yaxis == LINE)
			y2 = 0;

		//ggf. Gitter zeichnen:
		if (grid != INVISIBLE)
		{
			// Abstand der Gitterpunkte ermitteln:
			double spx = broadpitchx;
			double spy = broadpitchy;

			if (grid == FINE)
			{
				spx = finepitchx;
				spy = finepitchy;
			} // if

			// Grenzen der Gitterpunkte ermitteln:
			/*
			double begx = Math.ceil( transformX( x1 ) / spx ) * spx; // Start-x
			double endx = Math.floor( transformX( x2 ) / spx ) * spx; // End-x
			double begy = Math.ceil( transformY( y1 ) / spy ) * spy; // Start-y
			double endy = Math.floor( transformY( y2 ) / spy ) * spy; // End-y
			*/

			/*
			 * These have been modified to draw properly
			 * in JSimpleDisplayPanel as well. 
			 * 
			 */

			double begx = Math.ceil(transformX(0) / spx) * spx; // Start-x
			double endx = Math.floor(transformX(w - 1) / spx) * spx; // End-x
			double begy = Math.ceil(transformY(h - 1) / spy) * spy; // Start-y
			double endy = Math.floor(transformY(0) / spy) * spy; // End-y

			// temporary vars
			int x = 0, y = 0;
			double bx = begx;

			// Gitter zeichnen:
			if (_drawGridLines)
			{
				// draw grid as thin lines
				Color prev = g.getColor();
				g.setColor(_gridColor);

				//horizontal lines
				while (begy <= endy)
				{
					y = transformY(begy);
					g.drawLine(0, y, size.width - 1, y);
					begy += spy;
				}

				// vertical lines
				while (begx <= endx)
				{
					x = transformX(begx);
					g.drawLine(x, 0, x, size.height - 1);
					begx += spx;
				}
				g.setColor(prev);
			}
			else
				while (begy <= endy)
				{
					y = transformY(begy);
					bx = begx;

					while (bx <= endx)
					{
						x = transformX(bx);
						g.drawLine(x, y, x, y);
						bx += spx;
					} // while
					begy += spy;
				} // while

		} // if

		g.setColor(_lineColor);

		// zeichne x-Achse:
		if (xaxis != INVISIBLE)
		{
			g.drawLine(0, origin.y, w - 1, origin.y);

			// zeichne Markierungen mit Beschriftung:
			if ((xmarkings != INVISIBLE) && (origin.y - _BROAD < h)
					&& (origin.y + _BROAD >= 0))
			{
				double beg = Math.ceil(transformX(x1) / broadpitchx)
						* broadpitchx; // Startpunkt
				double end = Math.floor(transformX(x2) / broadpitchx)
						* broadpitchx; // Endpunkt
				boolean lettering = xlettering == BROAD;
				_drawXMarkings(g, size, beg, end, broadpitchx, _BROAD,
						lettering);
				if (xmarkings == FINE)
				{
					beg = Math.ceil(transformX(x1) / finepitchx) * finepitchx; // Startpunkt
					end = Math.floor(transformX(x2) / finepitchx) * finepitchx; // Endpunkt
					lettering = xlettering == FINE;
					_drawXMarkings(g, size, beg, end, finepitchx, _FINE,
							lettering);
				} // if
			} // if

			// zeichne rechte Pfeilspitze:
			if (xaxis != LINE)
			{
				ax = w - _RUW;
				_px[0] = ax + _RX[0];
				_px[1] = ax + _RX[1];
				_px[2] = ax + _RX[2];
				_py[0] = ay + _RY[0];
				_py[1] = ay + _RY[1];
				_py[2] = ay + _RY[2];
				g.fillPolygon(_px, _py, 3);
			} // if

			// benenne die x-Achse:
			if (namex)
			{
				if ((origin.y + _BROAD >= 0) && (origin.y - _BROAD < h))
				{
					g.drawString(xAxisLabel, ax - 2, ay + 15);
				} // if
			} // if

			// zeichne linke Pfeilspitze:
			if (xaxis == DOUBLEARROW)
			{
				ax = _RUW;
				_px[0] = ax - _RX[0];
				_px[1] = ax - _RX[1];
				_px[2] = ax - _RX[2];
				_py[0] = ay - _RY[0];
				_py[1] = ay - _RY[1];
				_py[2] = ay - _RY[2];
				g.fillPolygon(_px, _py, 3);
			} // if
		} // if

		// zeichne y-Achse:
		if (yaxis != INVISIBLE)
		{
			g.drawLine(origin.x, 0, origin.x, h - 1);

			// zeichne Markierungen mit Beschriftung:
			if ((ymarkings != INVISIBLE) && (origin.x + _BROAD >= 0)
					&& (origin.x - _BROAD < w))
			{
				double beg = Math.ceil(transformY(y1) / broadpitchy)
						* broadpitchy; // Startpunkt
				double end = Math.floor(transformY(y2) / broadpitchy)
						* broadpitchy; // Endpunkt
				boolean lettering = ylettering == BROAD;
				_drawYMarkings(g, size, beg, end, broadpitchy, _BROAD,
						lettering);
				if (ymarkings == FINE)
				{
					beg = Math.ceil(transformY(y1) / finepitchy) * finepitchy; // Startpunkt
					end = Math.floor(transformY(y2) / finepitchy) * finepitchy; // Endpunkt
					lettering = ylettering == FINE;
					_drawYMarkings(g, size, beg, end, finepitchy, _FINE,
							lettering);
				} // if
			} // if

			// zeichne obere Pfeilspitze:
			if (yaxis != LINE)
			{
				ax = origin.x;
				ay = _RUW;
				_px[0] = ax + _RY[0];
				_px[1] = ax + _RY[1];
				_px[2] = ax + _RY[2];
				_py[0] = ay - _RX[0];
				_py[1] = ay - _RX[1];
				_py[2] = ay - _RX[2];
				g.fillPolygon(_px, _py, 3);
			} // if

			// benenne die y-Achse:
			if (namey)
			{
				if ((origin.x + _BROAD >= 0) && (origin.x - _BROAD < w))
				{
					g.drawString(yAxisLabel, ax + 7, ay + 6);
				} // if
			} // if

			// zeichne untere Pfeilspitze:
			if (yaxis == DOUBLEARROW)
			{
				ay = h - _RUW;
				_px[0] = ax - _RY[0];
				_px[1] = ax - _RY[1];
				_px[2] = ax - _RY[2];
				_py[0] = ay + _RX[0];
				_py[1] = ay + _RX[1];
				_py[2] = ay + _RX[2];
				g.fillPolygon(_px, _py, 3);
			} // if
		} // if

	} // draw


	/**
	 * Sichert die aktuellen Einstellungen fuer einen Reset.
	 */
	public void saveSettings()
	{

		_bak_is_visible = _is_visible;
		_bak_origin.setLocation(origin);
		_bak_ppx = ppx;
		_bak_ppy = ppy;
		_bak_broadpitchx = broadpitchx;
		_bak_broadpitchy = broadpitchy;
		_bak_finepitchx = finepitchx;
		_bak_finepitchy = finepitchy;
		_bak_xaxis = xaxis;
		_bak_yaxis = yaxis;
		_bak_xmarkings = xmarkings;
		_bak_ymarkings = ymarkings;
		_bak_xlettering = xlettering;
		_bak_ylettering = ylettering;
		_bak_grid = grid;
		_bak_namex = namex;
		_bak_namey = namey;

		_bak_xAxisLabel = xAxisLabel;
		_bak_yAxisLabel = yAxisLabel;

	} // saveSettings


	/**
	 * Setzt die Einstellungen dieses Koordinatensystems auf die zuvor
	 * gesicherten bzw. die initialen Einstellungen zurueck.
	 */
	public void reset()
	{

		_is_visible = _bak_is_visible;
		origin.setLocation(_bak_origin);
		ppx = _bak_ppx;
		ppy = _bak_ppy;
		broadpitchx = _bak_broadpitchx;
		broadpitchy = _bak_broadpitchy;
		finepitchx = _bak_finepitchx;
		finepitchy = _bak_finepitchy;
		xaxis = _bak_xaxis;
		yaxis = _bak_yaxis;
		xmarkings = _bak_xmarkings;
		ymarkings = _bak_ymarkings;
		xlettering = _bak_xlettering;
		ylettering = _bak_ylettering;
		grid = _bak_grid;
		namex = _bak_namex;
		namey = _bak_namey;

		// Menue ggf. anpassen:
		if (_is_visible)
		{
			if (_show_hide_item.getActionCommand().equals(MENU_SHOW))
			{
				_show_hide_item.setText(ITEM_HIDE);
				_show_hide_item.setActionCommand(MENU_HIDE);
			} // if
		}
		else
		{ // if
			if (_show_hide_item.getActionCommand().equals(MENU_HIDE))
			{
				_show_hide_item.setText(ITEM_SHOW);
				_show_hide_item.setActionCommand(MENU_SHOW);
			} // if
		} // else
		if (grid == INVISIBLE)
			_grid_none_item.setSelected(true);
		else if (grid == BROAD)
			_grid_broad_item.setSelected(true);
		else
			_grid_fine_item.setSelected(true);

	} // reset


	/**
	 * Liefert zu einer Pixel-x-Koordinate in der Zeichenflaeche die zugehoerige
	 * x-Koordinate im Koordinatensystem.
	 * 
	 * @param x
	 *            die x-Koordinate eine Punktes in der Zeichenflaeche
	 * @return die korrespondierende x-Koordinate im Koordinatensystem
	 */
	public double transformX(
			int x)
	{

		return (x - origin.x) / ppx;

	} // transformX


	/**
	 * Liefert zu einer Pixel-y-Koordinate in der Zeichenflaeche die zugehoerige
	 * y-Koordinate im Koordinatensystem.
	 * 
	 * @param y
	 *            die y-Koordinate eine Punktes in der Zeichenflaeche
	 * @return die korrespondierende y-Koordinate im Koordinatensystem
	 */
	public double transformY(
			int y)
	{

		return (origin.y - y) / ppy;

	} // transformY


	/**
	 * Liefert zu einem Pixel in der Zeichenflaeche den zugehoerigen Punkt im
	 * Koordinatensystem.
	 * 
	 * @param point
	 *            der Zeichenflaechen-Punkt
	 * @return den zugehoerigen Koordinatensystem-Punkt
	 */
	public Point2D.Double transform(
			Point point)
	{

		return new Point2D.Double(transformX(point.x), transformY(point.y));

	} // transform


	/**
	 * Same thing as above, but doesn't create a new point to be returned.
	 * Should help in reusing same instances of the target object.
	 * 
	 * @param source
	 *            source point in image space
	 * @param target
	 *            target point in object space
	 */

	public void transform(
			Point source,
			Point2D.Double target)
	{
		target.x = transformX(source.x);
		target.y = transformY(source.y);
	}


	/**
	 * Same thing again, this time a convenience function for floats
	 * 
	 * @param source
	 * @param target
	 */

	public void transform(
			Point source,
			Point2D.Float target)
	{
		target.x = (float) transformX(source.x);
		target.y = (float) transformY(source.y);
	}


	/**
	 * Transforms a point from screen to world space coordinates, and
	 * additionally snaps it to the closest grid intersection. Can be used for
	 * precise drawing. <br>
	 * 
	 * Note: if grid is disabled (or snap is disabled), this function simply
	 * transforms the points but doesn't snap it. <br>
	 * 
	 * @param source
	 *            source point in screen space coordinates
	 * @param target
	 *            resulting point in object space coordinates
	 */

	public void transformAndSnapToGrid(
			Point source,
			Point2D.Double target)
	{
		//transform to object space
		target.x = transformX(source.x);
		target.y = transformY(source.y);

		if ((grid == INVISIBLE) || (_snapToGrid == false))
			return; // bail out

		/* snap to closest grid point.
		 * For this, we need to figure where the point is first,
		 * and then adjust its coordinates accordingly.
		 */
		int x_pos = 0, y_pos = 0;

		if (grid == BROAD)
		{
			x_pos = (int) Math.round(target.x / broadpitchx);
			y_pos = (int) Math.round(target.y / broadpitchy);

			target.x = x_pos * broadpitchx;
			target.y = y_pos * broadpitchy;
		}
		else if (grid == FINE)
		{
			x_pos = (int) Math.round(target.x / finepitchx);
			y_pos = (int) Math.round(target.y / finepitchy);

			target.x = x_pos * finepitchx;
			target.y = y_pos * finepitchy;
		}
	}


	public void transformAndSnapToGrid(
			Point source,
			Point2D.Float target)
	{
		// transform to object space
		target.x = (float) transformX(source.x);
		target.y = (float) transformY(source.y);

		if ((grid == INVISIBLE) || (_snapToGrid == false))
			return; // bail out

		/* snap to closest grid point.
		 * For this, we need to figure where the point is first,
		 * and then adjust its coordinates accordingly.
		 */

		int x_pos = 0, y_pos = 0;

		if (grid == BROAD)
		{
			x_pos = (int) Math.round(target.x / broadpitchx);
			y_pos = (int) Math.round(target.y / broadpitchy);

			target.x = (float) (x_pos * broadpitchx);
			target.y = (float) (y_pos * broadpitchy);
		}
		else if (grid == FINE)
		{
			x_pos = (int) Math.round(target.x / finepitchx);
			y_pos = (int) Math.round(target.y / finepitchy);

			target.x = (float) (x_pos * finepitchx);
			target.y = (float) (y_pos * finepitchy);
		}

	}


	/**
	 * Liefert zu einer x-Koordinate die Pixelkoordinate des entsprechenden
	 * naechstliegenden Punkts in der Zeichenflaeche.
	 * 
	 * @param x
	 *            die x-Koordinate im Koordinatensystem
	 * @return die x-Koordinate des korrespondierenden Pixels der Zeichenflaeche
	 */
	public int transformX(
			double x)
	{

		return (int) (origin.x + x * ppx + 0.5);

	} // transformX


	/**
	 * Liefert zu einer y-Koordinate die Pixelkoordinate des entsprechenden
	 * naechstliegenden Punkts in der Zeichenflaeche.
	 * 
	 * @param y
	 *            die y-Koordinate im Koordinatensystem
	 * @return die y-Koordinate des korrespondierenden Pixels der Zeichenflaeche
	 */
	public int transformY(
			double y)
	{

		return (int) (origin.y - y * ppy + 0.5);

	} // transformY


	/**
	 * Liefert zu einem Punkt des Koordinatensystems das entsprechende Pixel in
	 * der Zeichenflaeche.
	 * 
	 * @param point
	 *            der Punkt im Koordinatensystem
	 * @return den korrespondierenden Pixelpunkt in der Zeichenflaeche
	 */
	public Point transform(
			Point2D.Double point)
	{

		return new Point(transformX(point.x), transformY(point.y));

	} // transform


	// *************************************************************************
	// Interface ActionListener
	// *************************************************************************

	/**
	 * Leitet eine Menueauswahl an den registrierten Listener weiter.
	 * 
	 * @param e
	 *            das <code>ActionEvent</code>-Objekt
	 */
	public void actionPerformed(
			ActionEvent e)
	{
		String cmd = e.getActionCommand();

		if (cmd.equals(MENU_SHOW))
		{
			_show_hide_item.setText(ITEM_HIDE);
			_show_hide_item.setActionCommand(MENU_HIDE);
			_is_visible = true;
		}
		else if (cmd.equals(MENU_HIDE))
		{
			_show_hide_item.setText(ITEM_SHOW);
			_show_hide_item.setActionCommand(MENU_SHOW);
			_is_visible = false;
		}
		else if (cmd.equals(MENU_NONE))
		{
			grid = INVISIBLE;
			_grid_type_menu.setEnabled(false);
			_snap_to_grid_box.setEnabled(false);
		}
		else if (cmd.equals(MENU_BROAD))
		{
			grid = BROAD;
			_grid_type_menu.setEnabled(true);
			_snap_to_grid_box.setEnabled(true);
		}
		else if (cmd.equals(MENU_FINE))
		{
			grid = FINE;
			_grid_type_menu.setEnabled(true);
			_snap_to_grid_box.setEnabled(true);
		}
		else if (cmd.equals(MENU_RESET))
			reset();

		else if (cmd.equals(CMD_DOTS))
		{
			_drawGridLines = false;
		}
		else if (cmd.equals(CMD_LINES))
		{
			_drawGridLines = true;
		}
		else if (cmd.equals(CMD_SNAP))
		{
			_snapToGrid = _snap_to_grid_box.isSelected();
			System.out.println(_snapToGrid);

		}
		else if (cmd.equals(CMD_COLOR))
		{
			Color c = JColorChooser.showDialog(null, "Choose grid color",
					Color.black);
			if (c != null)
				this.setGridColor(c);
		}

		if (_listener != null)
			_listener.actionPerformed(e);

	} // actionPerformed


	// *************************************************************************
	// Keyboard event handlers
	// *************************************************************************    

	/**
	 * Handles keyboard shortcuts for various coordinate system options.
	 * 
	 * <br><br>Curent key mappings: <br><b>g</b> - switches grid between NONE,
	 * BROAD and FINE modes.
	 * 
	 * <br><br>Note: Currently, this is the only viable option for creating key
	 * mappings, since this class does not descend from JComponent and thus
	 * cannot employ the InputMap/ActionMap system.
	 * 
	 * 
	 */

	public void keyPressed(
			KeyEvent event)
	{
		switch (event.getKeyCode())
		{
			// grid mode switch - rotate grid modes
			case KeyEvent.VK_G:

				if (_grid_none_item.isSelected())
				{
					_grid_broad_item.doClick();
				}
				else if (_grid_broad_item.isSelected())
				{
					_grid_fine_item.doClick();
				}
				else
				{
					_grid_none_item.doClick();
				}
				break;

			case KeyEvent.VK_S: // Snap toggle
				_snap_to_grid_box.doClick();
				break;

			default:
				break;

		}
	}


	public void keyReleased(
			KeyEvent event)
	{}


	public void keyTyped(
			KeyEvent event)
	{}


	// *************************************************************************
	// Mouse event handlers
	// ************************************************************************* 

	public void mousePressed(
			MouseEvent event)
	{
		// store current cursor position
		_zoom_pixel = event.getPoint();
		_zoom_point = transform(_zoom_pixel);

		// store the pressed button
		_mouse_button = event.getButton();
		_pressed_point = event.getPoint();
	}


	public void mouseReleased(
			MouseEvent event)
	{

	}


	public void mouseDragged(
			MouseEvent event)
	{
		Point p = event.getPoint();

		// translate coordinate system
		if (_mouse_button == MouseEvent.BUTTON1)
		{
			origin.x += p.x - _pressed_point.x;
			origin.y += p.y - _pressed_point.y;

			_pressed_point = p;

		} // if

		// zoom coordinate system
		else
		{
			double factor = Math.pow(1.01, Math.abs(p.y - _pressed_point.y));
			if (p.y - _pressed_point.y < 0)
				factor = 1.0 / factor;

			ppx *= factor;
			ppy *= factor;

			if (ppx < 1.0e-4)
			{
				ppx = 1.0e-4;
				ppy = 1.0e-4;
			}
			else if (ppx > 1.0e+8)
			{
				ppx = 1.0e+8;
				ppy = 1.0e+8;
			}

			// adjust coordinate system origin and markings
			origin.x -= transformX(_zoom_point.x) - _zoom_pixel.x;
			origin.y -= transformY(_zoom_point.y) - _zoom_pixel.y;

			adjustMarkings();
			_pressed_point = p;

		} // else

		_makeXForm();

	}


	public void mouseWheelMoved(
			MouseWheelEvent event)
	{
		/*
		 *  mouse wheel zooming. same zoom as with
		 *  right mouse button
		 * 
		 */

		_zoom_pixel = event.getPoint(); //_cursor_position; 
		_zoom_point = transform(_zoom_pixel);

		int wheel_clicks = event.getWheelRotation();

		double factor = 1.1; //Math.pow( 1.2, Math.abs( wheel_clicks ) );
		if (wheel_clicks > 0)
			factor = 1.0 / factor;

		ppx *= factor;
		ppy *= factor;

		if (ppx < 1.0e-4)
		{
			ppx = 1.0e-4;
			ppy = 1.0e-4;
		}
		else if (ppx > 1.0e+8)
		{
			ppx = 1.0e+8;
			ppy = 1.0e+8;
		}

		// adjust coordinate system origin etc
		origin.x -= transformX(_zoom_point.x) - _zoom_pixel.x;
		origin.y -= transformY(_zoom_point.y) - _zoom_pixel.y;

		adjustMarkings();
		_makeXForm();

		System.out.print("Fine pitch: " + finepitchx);
		System.out.println("  Broad pitch: " + broadpitchx);

	}


	// *************************************************************************
	// Private methods
	// *************************************************************************

	private void _makeXForm()
	{
		_affine_transform.setTransform(ppx, 0.0, 0.0, -ppy, origin.x, origin.y);

	}


	/*
	* Gemeinsame Initialisierungen fuer alle Konstruktoren.
	*/
	private void _init(
			Register register)
	{

		register.cosystem = this;
		_reg = register;
		xaxis = DOUBLEARROW;
		yaxis = DOUBLEARROW;
		xmarkings = FINE;
		ymarkings = FINE;
		xlettering = BROAD;
		ylettering = BROAD;

		grid = INVISIBLE;

		namex = true;
		namey = true;

		_is_visible = false;
		_px = new int[3];
		_py = new int[3];
		_bak_origin = new Point();

		_affine_transform = new AffineTransform();

		// initial parameters
		_lineColor = Color.black;
		_gridColor = new Color(204, 204, 255);
		_drawGridLines = true;
		_snapToGrid = false;

	} // _init


	/*
	* Zeichnet Markierungen mit Beschriftung auf der x-Achse.
	*/
	private void _drawXMarkings(
			Graphics g,
			Dimension size,
			double beg,
			double end,
			double width,
			int l,
			boolean lettering)
	{

		double x = beg;
		int offset = g.getFont().getSize();
		int cw = (1 * offset) / 2;
		while (x <= end)
		{
			int i = transformX(x);
			g.drawLine(i, origin.y - l, i, origin.y + l);
			if (lettering && (Math.abs(i - origin.x) > l))
			{
				float x_f = (float) x;
				long x_l = (long) x_f;
				String s = null;
				if (x_f == x_l)
					s = String.valueOf(x_l);
				else
					s = String.valueOf(x_f);
				int len = ((s.length()) * cw) / 2;
				if ((i - len >= 0) && (i + len - cw < size.width - 21))
				{
					g.drawString(s, i - len + 1, origin.y + l + offset);
				} // if
			} // if
			x += width;
		} // while

	} // _drawXMarkings


	/*
	* Zeichnet Markierungen mit Beschriftung auf der y-Achse.
	*/
	private void _drawYMarkings(
			Graphics g,
			Dimension size,
			double beg,
			double end,
			double width,
			int l,
			boolean lettering)
	{

		double y = beg;
		int offset = g.getFont().getSize();
		int cw = (1 * offset) / 2;
		while (y <= end)
		{
			int j = transformY(y);
			g.drawLine(origin.x - l, j, origin.x + l, j);
			if (lettering && (Math.abs(j - origin.y) > l))
			{
				float y_f = (float) y;
				long y_l = (long) y_f;
				String s = null;
				if (y_f == y_l)
					s = String.valueOf(y_l);
				else
					s = String.valueOf(y_f);
				int len = ((s.length() - 1) * cw);
				if (y < 0.0)
					len -= 3;
				if ((j - offset / 2 - offset - 6 >= 0)
						&& (j + offset / 2 - 1 < size.height))
				{
					g.drawString(s, origin.x - l - len - 9, j + offset / 2 - 1);
				} // if
			} // if
			y += width;
		} // while

	} // _drawYMarkings

} // CoordinateSystem

