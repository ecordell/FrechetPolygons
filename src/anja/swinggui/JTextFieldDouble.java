package anja.swinggui;

import javax.swing.*;

import java.awt.AWTEvent;
import java.awt.event.*;

/**
 * Text field which represents a double variable.
 *
 * @version 0.2 18.02.2004
 * @author Ulrich Handel, Sascha Ternes
 */

//****************************************************************
public class JTextFieldDouble extends JTextField
//****************************************************************
{
    //************************************************************
    // private variables
    //************************************************************

    private double _value;

    private double _min_value = -Double.MAX_VALUE;
    private double _max_value = Double.MAX_VALUE;




    //************************************************************
    // constructors
    //************************************************************



    /**
     * Create a new JTextFieldDouble and set its initial value.
     */

    //============================================================
    public JTextFieldDouble( double value )
    //============================================================
    {
        setValue( value );

        setColumns( 4 );
        setHorizontalAlignment( JTextField.RIGHT );

        enableEvents( AWTEvent.FOCUS_EVENT_MASK |
                      AWTEvent.KEY_EVENT_MASK );
    } // JTextFieldDouble



    /**
     * Create a new JTextFieldDouble and set its initial value,
     * minimum and maximum.
     */

    //============================================================
    public JTextFieldDouble( double value,
                             double min,
                             double max )
    //============================================================
    {
        this( value );
        setValueLimits( min, max );
    } // JTextFieldDouble



    //************************************************************
    // public methods
    //************************************************************



    /**
     * Return the present value
     */

    //============================================================
    public double getValue()
    //============================================================
    {
        return _value;
    } // getValue



    /**
     * React to a KeyEvent.
     */

    //============================================================
    public void processKeyEvent( KeyEvent e )
    //============================================================
    {
        if(    e.getID() == KeyEvent.KEY_PRESSED
            && e.getKeyCode() == KeyEvent.VK_ENTER )
        {
            _refreshValue();
        } // if

        super.processKeyEvent( e );
    } // processKeyEvent



    /**
     * React to the focus loss.
     */

    //============================================================
    public void processFocusEvent( FocusEvent e )
    //============================================================
    {
        if( e.getID() == FocusEvent.FOCUS_LOST ) _refreshValue();
        super.processFocusEvent( e );
    } // processFocusEvent



    /**
     * Set new value.
     *
     * If the value is outside the limits, it will be changed
     * to the nearest legal value.
     */

    //============================================================
    public void setValue( double value )
    //============================================================
    {
        if( value < _min_value ) value = _min_value;
        if( value > _max_value ) value = _max_value;
        _value = value;

        // convert the new value to a string and set the textfield's
        // text to it, if it is different from the present text.
        String string = String.valueOf( _value );
        if( ! getText().equals( string ) ) setText( string );
    } // setValue



    /**
     * Get the minimal value limit.
     */
    //============================================================
    public double getMinValueLimit()
    //============================================================
    {
        return _min_value;
    } // getMinValueLimit


    /**
     * Get the maximum value limit.
     */
    //============================================================
    public double getMaxValueLimit()
    //============================================================
    {
        return _max_value;
    } // getMaxValueLimit


    /**
     * Set the minimal and maximal value.
     *
     * If min > max, the present values will not be changed.
     * If the present value is outside the limits, it will be changed
     * to the nearest legal value.
     */

    //============================================================
    public void setValueLimits( double min,
                                double max )
    //============================================================
    {
        if( min > max ) return;

        _min_value = min;
        _max_value = max;

        setValue( _value );
    } // setValueLimits



    //************************************************************
    // Private methods
    //************************************************************

    /**
     * Set the value according to the textfield's present text.
     */

    //============================================================
    private void _refreshValue()
    //============================================================
    {
        setText( getText().replace( ',', '.' ) );
        try
        {
            setValue( ( new Double( getText() ) ).doubleValue() );
        }
        catch( NumberFormatException exception )
        {
            setValue( _value );
        } // try
    } // _refreshValue

} // JTextFieldDouble
