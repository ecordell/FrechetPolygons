package anja.swinggui;

import javax.swing.*;

import java.awt.AWTEvent;
import java.awt.event.*;

/**
 * Text field which represents a integer variable.
 *
 * @version 0.2 18.02.2004
 * @author      Ulrich Handel, Sascha Ternes
 */

//****************************************************************
public class JTextFieldInteger extends JTextField
//****************************************************************
{
    //************************************************************
    // private variables
    //************************************************************

    private int _value;

    private int _min_value = Integer.MIN_VALUE;
    private int _max_value = Integer.MAX_VALUE;




    //************************************************************
    // constructors
    //************************************************************



    /**
     * Create a new JTextFieldInteger and set its initial value.
     */

    //============================================================
    public JTextFieldInteger( int value )
    //============================================================
    {
        setValue( value );

        setColumns( 3 );
        setHorizontalAlignment( JTextField.RIGHT );

        enableEvents( AWTEvent.FOCUS_EVENT_MASK |
                      AWTEvent.KEY_EVENT_MASK );
    } // JTextFieldInteger



    /**
     * Create a new JTextFieldInteger and set its initial value,
     * minimum and maximum.
     */

    //============================================================
    public JTextFieldInteger( int value,
                              int min,
                              int max )
    //============================================================
    {
        this( value );
        setValueLimits( min, max );
    } // JTextFieldInteger



    //************************************************************
    // public methods
    //************************************************************



    /**
     * Return the present value
     */

    //============================================================
    public int getValue()
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
    public void setValue( int value )
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
    public int getMinValueLimit()
    //============================================================
    {
        return _min_value;
    } // getMinValueLimit


    /**
     * Get the maximum value limit.
     */
    //============================================================
    public int getMaxValueLimit()
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
    public void setValueLimits( int min,
                                int max )
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
        try
        {
            setValue( ( new Integer( getText() ) ).intValue() );
        }
        catch( NumberFormatException exception )
        {
            setValue( _value );
        } // try
    } // _refreshValue

} // JTextFieldInteger
