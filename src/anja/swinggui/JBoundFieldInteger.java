package anja.swinggui;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import anja.analysis.Bound;

//import anja.swinggui.JTextFieldInteger;

import anja.swinggui.icon.Icons;


/**
 * Ein komfortables Eingabefeld fuer eine mathematische Schranke.
 * 
 * @version 0.7 20.02.2004
 * @author Sascha Ternes
 */

public class JBoundFieldInteger
		extends JPanel
		implements ActionListener
{

	// *************************************************************************
	// Public constants
	// *************************************************************************

	/**
	 * Button fuer Schrankentyp oder Unendlichkeit wird nicht dargestellt
	 */
	public static final int		NONE		= 0;
	/**
	 * Button fuer Schrankentyp wird links vom Eingabefeld dargestellt
	 */
	public static final int		LEFT		= 1;
	/**
	 * Button fuer Schrankentyp wird rechts vom Eingabefeld dargestellt
	 */
	public static final int		RIGHT		= 2;

	/**
	 * Button fuer negative Unendlichkeit wird dargestellt
	 */
	public static final int		NEGATIVE	= 1;
	/**
	 * Button fuer positive Unendlichkeit wird dargestellt
	 */
	public static final int		POSITIVE	= 2;
	/**
	 * Buttons fuer positive und negative Unendlichkeit werden dargestellt
	 */
	public static final int		BOTH		= 3;

	// *************************************************************************
	// Private constants
	// *************************************************************************

	// Actioncommands fuer die Buttons:
	private static final String	_DOWN		= "down";
	private static final String	_UP			= "up";
	private static final String	_POSINF		= "posinf";
	private static final String	_NEGINF		= "neginf";
	private static final String	_BOUND		= "bound";

	// *************************************************************************
	// Private variables
	// *************************************************************************

	// das Eingabefeld:
	private JTextFieldInteger	_field;
	// sein Panel:
	private JPanel				_field_panel;

	// die Schrittweite bei Veraenderung des Werts durch die Spinnerbuttons:
	private int					_step;

	// der aktuelle Typ der Schranke:
	private boolean				_inclusive;

	// Button fuer Schrankentyp (eingeschlossen oder ausgeschlossen):
	private JButton				_bound;
	// Position, an der der Button dargestellt wird:
	private int					_position;

	// Panel fuer die Unendlichkeitsbuttons:
	private JPanel				_inf_panel;
	// dargestellte Unendlichkeitsbuttons:
	private int					_which;
	// die Buttons:
	private JButton				_neginf;
	private JButton				_posinf;

	// die registrierten ChangeListener:
	private Vector				_listeners;


	// *************************************************************************
	// Constructors
	// *************************************************************************

	/**
	 * Erzeugt ein Eingabefeld ohne dargestellten Schrankentyp und
	 * Unendlichkeitsbuttons. Implizit wird eine eingeschlossene Schranke
	 * angenommen.
	 */
	public JBoundFieldInteger()
	{

		this(0, true, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, NONE, NONE);
	} // JBoundFieldInteger


	/**
	 * Erzeugt ein mit dem spezifizierten Wert initialisiertes Eingabefeld ohne
	 * dargestellten Schrankentyp und Unendlichkeitsbuttons. Implizit wird eine
	 * eingeschlossene Schranke angenommen.
	 * 
	 * @param value
	 *            der Anfangswert
	 */
	public JBoundFieldInteger(
			int value)
	{

		this(value, true, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, NONE, NONE);

	} // JBoundFieldInteger


	/**
	 * Erzeugt ein mit dem spezifizierten Wert initialisiertes, unbeschraenktes
	 * Eingabefeld mit den spezifizierten Buttons und der Schrittweite 1.
	 * 
	 * @param value
	 *            der Anfangswert
	 * @param inclusive
	 *            eingeschlossen (<code>true</code>) oder ausgeschlossen
	 *            (<code>false</code>); es koennen auch die entsprechenden
	 *            Konstanten von {@link anja.analysis.Bound Bound} verwendet
	 *            werden
	 * @param bound_position
	 *            die Position des Schrankenbuttons; erlaubte Werte sind
	 *            {@link #NONE NONE}, {@link #LEFT LEFT} und {@link #RIGHT
	 *            RIGHT}
	 * @param infinity
	 *            gibt an, welche Unendlichkeitsbuttons dargestellt werden;
	 *            erlaubte Werte sind {@link #NONE NONE}, {@link #NEGATIVE
	 *            NEGATIVE}, {@link #POSITIVE POSITIVE} und {@link #BOTH BOTH}
	 */
	public JBoundFieldInteger(
			int value,
			boolean inclusive,
			int bound_position,
			int infinity)
	{

		this(value, inclusive, Integer.MIN_VALUE, Integer.MAX_VALUE, 1,
				bound_position, infinity);

	} // JBoundFieldInteger


	/**
	 * Erzeugt ein mit Null initialisiertes, unbeschraenktes Eingabefeld mit den
	 * spezifizierten Buttons und der Schrittweite 1.
	 * 
	 * @param bound_position
	 *            die Position des Schrankenbuttons; erlaubte Werte sind
	 *            {@link #NONE NONE}, {@link #LEFT LEFT} und {@link #RIGHT
	 *            RIGHT}
	 * @param infinity
	 *            gibt an, welche Unendlichkeitsbuttons dargestellt werden;
	 *            erlaubte Werte sind {@link #NONE NONE}, {@link #NEGATIVE
	 *            NEGATIVE}, {@link #POSITIVE POSITIVE} und {@link #BOTH BOTH}
	 */
	public JBoundFieldInteger(
			int bound_position,
			int infinity)
	{

		this(0, true, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, bound_position,
				infinity);

	} // JBoundFieldInteger


	/**
	 * Erzeugt ein Eingabefeld mit den spezifizierten Werten und Buttons.
	 * 
	 * @param value
	 *            der Anfangswert
	 * @param inclusive
	 *            eingeschlossen (<code>true</code>) oder ausgeschlossen
	 *            (<code>false</code>); es koennen auch die entsprechenden
	 *            Konstanten von {@link anja.analysis.Bound Bound} verwendet
	 *            werden
	 * @param minimum
	 *            der kleinstmoegliche Wert
	 * @param maximum
	 *            der groesstmoegliche Wert
	 * @param step
	 *            die Schrittweite bei Veraendern des Wertes mit den Buttons
	 * @param bound_position
	 *            die Position des Schrankenbuttons; erlaubte Werte sind
	 *            {@link #NONE NONE}, {@link #LEFT LEFT} und {@link #RIGHT
	 *            RIGHT}
	 * @param infinity
	 *            gibt an, welche Unendlichkeitsbuttons dargestellt werden;
	 *            erlaubte Werte sind {@link #NONE NONE}, {@link #NEGATIVE
	 *            NEGATIVE}, {@link #POSITIVE POSITIVE} und {@link #BOTH BOTH}
	 */
	public JBoundFieldInteger(
			int value,
			boolean inclusive,
			int minimum,
			int maximum,
			int step,
			int bound_position,
			int infinity)
	{

		setLayout(new BorderLayout());
		JPanel spinner = new JPanel(new BorderLayout());
		_field_panel = new JPanel(new BorderLayout());
		_field = new JTextFieldInteger(value, minimum, maximum);
		_field.addActionListener(this);
		_field_panel.add(_field, BorderLayout.CENTER);
		Insets insets0 = new Insets(0, 0, 0, 1);
		Insets insets1 = new Insets(0, 0, 1, 1);
		Insets insets2 = new Insets(0, 0, 0, 1);
		_step = step;

		// Position des Schrankenbuttons ermitteln:
		_bound = new JButton(Icons.RIGHT_BRACKET);
		_bound.setMargin(insets0);
		_bound.setActionCommand(_BOUND);
		_bound.addActionListener(this);
		_position = 666;
		_setBoundButtonEnabled(bound_position);
		_setInclusive(inclusive);

		spinner.add(_field_panel, BorderLayout.CENTER);

		// Spinner-Buttons hinzufuegen:
		JPanel spins = new JPanel(new GridLayout(2, 1));
		JButton arrow = new JButton(Icons.UP_ARROWTIP);
		arrow.setMargin(insets1);
		arrow.setActionCommand(_UP);
		arrow.addActionListener(this);
		spins.add(arrow);
		arrow = new JButton(Icons.DOWN_ARROWTIP);
		arrow.setMargin(insets2);
		arrow.setActionCommand(_DOWN);
		arrow.addActionListener(this);
		spins.add(arrow);

		spinner.add(spins, BorderLayout.EAST);

		// Unendlichkeitsbuttons aufbauen:
		_neginf = new JButton(Icons.NEGATIVE_INFINITY);
		_neginf.setActionCommand(_NEGINF);
		_neginf.addActionListener(this);
		_posinf = new JButton(Icons.POSITIVE_INFINITY);
		_posinf.setActionCommand(_POSINF);
		_posinf.addActionListener(this);
		if (infinity == BOTH)
			_inf_panel = new JPanel(new GridLayout(2, 1));
		else
			_inf_panel = new JPanel(new GridLayout(1, 1));
		switch (infinity)
		{
			case BOTH:
			case NEGATIVE:
				if (infinity != BOTH)
					_neginf.setMargin(insets0);
				else
					_neginf.setMargin(insets1);
				_inf_panel.add(_neginf);
				if (infinity != BOTH)
					break;
			case POSITIVE:
				if (infinity != BOTH)
					_posinf.setMargin(insets0);
				else
					_posinf.setMargin(insets2);
				_inf_panel.add(_posinf);
		} // switch

		add(spinner, BorderLayout.CENTER);
		add(_inf_panel, BorderLayout.EAST);

		_listeners = new Vector();

	} // JBoundFieldInteger


	// *************************************************************************
	// Public methods
	// *************************************************************************

	/**
	 * Liefert die aktuelle Schranke.
	 * 
	 * @return die Schranke
	 */
	public Bound getBound()
	{

		return new Bound(new Integer(_field.getValue()), _inclusive);

	} // getBound


	/**
	 * Liefert den aktuellen Wert des Eingabefelds.
	 * 
	 * @return den Wert der Schranke
	 */
	public int getValue()
	{

		return _field.getValue();

	} // getValue


	/**
	 * Setzt den Wert des Eingabefeldes.
	 * 
	 * @param value
	 *            Der Wert der Schranke
	 */
	public void setValue(
			int value)
	{

		_field.setValue(value);

	} // setValue


	/**
	 * Setzt die Schranke gemaess dem spezifizierten Parameter inklusiv oder
	 * exklusiv.
	 * 
	 * @param inclusive
	 *            eingeschlossen (<code>true</code>) oder ausgeschlossen
	 *            (<code>false</code>); es koennen auch die entsprechenden
	 *            Konstanten von {@link anja.analysis.Bound Bound} verwendet
	 *            werden
	 */
	public void setInclusive(
			boolean inclusive)
	{

		_setInclusive(inclusive);
		repaint();

	} // setInclusive


	/**
	 * Setzt das Minimum fuer diese Schranke. Wenn das Minimum groesser als das
	 * aktuelle Maximum ist, erhaelt dieses den Wert des neuen Minimums. Falls
	 * danach der aktuelle Schrankenwert ausserhalb des erlaubten Bereichs
	 * liegt, wird er auf das Minimum bzw. Maximum gesetzt.
	 * 
	 * @param minimum
	 *            das neue Minimum dieser Schranke
	 */
	public void setMinimum(
			int minimum)
	{

		int max = _field.getMaxValueLimit();
		if (minimum > max)
			max = minimum;
		_field.setValueLimits(minimum, max);

	} // setMinimum


	/**
	 * Setzt das Maximum fuer diese Schranke. Wenn das Maximum kleiner als das
	 * aktuelle Minimum ist, erhaelt dieses den Wert des neuen Maximums. Falls
	 * danach der aktuelle Schrankenwert ausserhalb des erlaubten Bereichs
	 * liegt, wird er auf das Minimum bzw. Maximum gesetzt.
	 * 
	 * @param maximum
	 *            das neue Maximum dieser Schranke
	 */
	public void setMaximum(
			int maximum)
	{

		int min = _field.getMinValueLimit();
		if (maximum < min)
			min = maximum;
		_field.setValueLimits(min, maximum);

	} // setMaximum


	/**
	 * Setzt die erlaubten Extremwerte fuer den Schrankenwert. Wenn das
	 * spezifizierte Minimum groesser ist als das Maximum, werden die beiden
	 * Werte vertauscht.
	 * 
	 * @param minimum
	 *            das neue Minimum dieser Schranke
	 * @param maximum
	 *            das neue Maximum dieser Schranke
	 */
	public void setValueLimits(
			int minimum,
			int maximum)
	{

		if (minimum <= maximum)
			_field.setValueLimits(minimum, maximum);
		else
			_field.setValueLimits(maximum, minimum);

	} // setValueLimits


	/**
	 * Veraendert den Status des Schrankenbuttons.
	 * 
	 * @param position
	 *            {@link #NONE NONE} versteckt den Button, {@link #LEFT LEFT}
	 *            bzw. {@link #RIGHT RIGHT} lassen den Button links bzw. rechts
	 *            vom Eingabefeld erscheinen
	 */
	public void setBoundButtonEnabled(
			int position)
	{

		_setBoundButtonEnabled(position);
		_field_panel.repaint();

	} // setBoundButtonEnabled


	/**
	 * Registriert einen Listener, der Veraenderungen der Schranke ueberwacht.
	 * 
	 * @param listener
	 *            Der Listener
	 */
	public void addChangeListener(
			ChangeListener listener)
	{

		_listeners.add(listener);

	} // addChangeListener


	/**
	 * Entfernt einen vorher registrierten Listener.
	 * 
	 * @param listener
	 *            Der Listener
	 */
	public void removeChangeListener(
			ChangeListener listener)
	{

		_listeners.remove(listener);

	} // removeChangeListener


	// *************************************************************************
	// Interface ActionListener
	// *************************************************************************

	/**
	 * Reagiert auf das Anklicken der diversen Buttons.
	 * 
	 * @param e
	 *            das <code>ActionEvent</code>-Objekt
	 */
	public void actionPerformed(
			ActionEvent e)
	{

		String cmd = e.getActionCommand();

		// Spinnerbuttons:
		if (cmd.equals(_DOWN))
			_decreaseValue();
		else if (cmd.equals(_UP))
			_increaseValue();
		// Unendlichkeitsbuttons:
		else if (cmd.equals(_NEGINF))
			setValue(Integer.MIN_VALUE);
		else if (cmd.equals(_POSINF))
			setValue(Integer.MAX_VALUE);
		// Schrankenbutton:
		else if (cmd.equals(_BOUND))
			setInclusive(!_inclusive);

		// benachrichtige alle registrierten Listener:
		for (int i = 0; i < _listeners.size(); i++)
			((ChangeListener) _listeners.get(i)).stateChanged(new ChangeEvent(
					this));

	} // actionPerformed


	// *************************************************************************
	// Private methods
	// *************************************************************************

	/*
	* Reduziert den Wert um eine Schrittweite.
	*/
	private void _decreaseValue()
	{

		int i = _field.getValue() - _step;
		if (i > _field.getValue())
			return;
		_field.setValue(i);

	} // _decreaseValue


	/*
	* Erhoeht den Wert um eine Schrittweite.
	*/
	private void _increaseValue()
	{

		int i = _field.getValue() + _step;
		if (i < _field.getValue())
			return;
		_field.setValue(i);

	} // _increaseValue


	/*
	* Setzt die Schranke gemaess dem spezifizierten Parameter.
	*/
	private void _setInclusive(
			boolean inclusive)
	{

		_inclusive = inclusive;
		if (_position == NONE)
			return;
		if (_position == LEFT)
			if (inclusive)
				_bound.setIcon(Icons.LEFT_BRACKET);
			else
				_bound.setIcon(Icons.RIGHT_BRACKET);
		else if (inclusive)
			_bound.setIcon(Icons.RIGHT_BRACKET);
		else
			_bound.setIcon(Icons.LEFT_BRACKET);

	} // _setInclusive


	/*
	* Veraendert den Status des Schrankenbuttons.
	*/
	private void _setBoundButtonEnabled(
			int position)
	{

		if (_position == position)
			return;
		_position = position;
		_field_panel.remove(_bound);
		if (position == NONE)
			return;
		if (position == LEFT)
			_field_panel.add(_bound, BorderLayout.WEST);
		else
			_field_panel.add(_bound, BorderLayout.EAST);

	} // _setBoundButtonEnabled

} // JBoundFieldInteger
