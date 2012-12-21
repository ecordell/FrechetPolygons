package anja.swinggui;

import javax.swing.*;
//import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;


/**
 * AbstractPanel ist die abstrakte Basisklasse fuer Panels, die den
 * Dialog zwischen dem Benutzer und einem DisplayPanel unterstuetzen.
 *
 * Darueber hinaus werden Klassenmethoden bereitgestellt, die in
 * abgeleiteten Klassen nuetzlich sein koennen.
 *
 *
 * @version 0.1 23.01.02
 * @author      Ulrich Handel
 */

//****************************************************************
abstract public class AbstractPanel extends JPanel
                                    implements ActionListener,
                                               ItemListener,
                                               AdjustmentListener
//****************************************************************
{
    //************************************************************
    // public constants
    //************************************************************


    static public final int NO_ACTION = -1;


    //************************************************************
    // protected class methods
    //************************************************************


    /**
     */

    //============================================================
    protected static void setSelected( AbstractButton button,
                                       boolean select )
    //============================================================
    {
        if( button.isSelected() != select )
            button.setSelected( select );
    } // setSelected


    /**
     */

    //============================================================
    protected static void setEnabled( JComponent component,
                                      boolean    enable )
    //============================================================
    {
        if( component.isEnabled() != enable )
            component.setEnabled( enable );
    } // setEnabled


    /**
     */

    //============================================================
    protected static void setVisible( JComponent component,
                                      boolean    visible )
    //============================================================
    {
        if( component.isVisible() != visible )
            component.setVisible( visible );
    } // setEnabled


    /**
     */

    //============================================================
    protected static void setValue( JTextFieldInteger textfield,
                                    int value )
    //============================================================
    {
        if( textfield.getValue() != value )
            textfield.setValue( value );
    } // setValue


    /**
     */

    //============================================================
    protected static void setValues( JScrollBar bar,
                                     int        value,
                                     int        extend,
                                     int        min,
                                     int        max )
    //============================================================
    {
        if(    bar.getValue() != value
            || bar.getVisibleAmount() != extend
            || bar.getMinimum() != min
            || bar.getMaximum() != max )
        {
            bar.setValues( value, extend, min, max );
        } // if
    } // setValues


    /**
     */

    //============================================================
    protected static void setValue( JTextFieldDouble textfield,
                                    double value )
    //============================================================
    {
        if( textfield.getValue() != value )
            textfield.setValue( value );
    } // setValue


    /**
     */

    //============================================================
    protected static void setValue( JScrollBar scrollbar,
                                    int value )
    //============================================================
    {
        if( scrollbar.getValue() != value )
            scrollbar.setValue( value );
    } // setValue


    /**
     */

    //============================================================
    protected static void setText( AbstractButton button,
                                   String  text )
    //============================================================
    {
        if( button.getText() != text )
            button.setText( text );
    } // setText


    /**
     */

    //============================================================
    protected static void setSelectedItem( JComboBox box,
                                           String    text )
    //============================================================
    {
        if( box.getSelectedItem() != text )
            box.setSelectedItem( text );
    } // setSelectedItem


    /**
     */

    //============================================================
    protected static void refreshJComboBox( JComboBox box,
                                            String[]  list,
                                            String    selected )
    //============================================================
    {
        // Testen, ob sich die Liste list in ihren Eintraegen von
        // der Auswahlliste der JComboBox unterscheidet

        boolean list_changed = false;

        int no_of_items = box.getItemCount();
        if( no_of_items != list.length )
            list_changed = true;  // unterschiedliche Laenge
        else
        {
            for( int i = 0; i < no_of_items; i++ )
            {
                if( box.getItemAt( i ) != list[ i ] )
                {
                    list_changed = true; // unterschiedlicher Eintrag
                    break;
                } // if
            } // for
        } // if



        // Wenn sich die Liste list von der Auswahlliste unterscheidet,
        // Auswahlliste neu aufbauen.

        if( list_changed )
        {
            ActionListener[] listeners =
                ( ActionListener[] )
                ( box.getListeners( ActionListener.class ) );

            // Durch die evtl. erfolgenden Aenderungen an der box duerfen
            // keine ActionEvents ausgeloest werden.
            for( int i = 0; i < listeners.length; i++ )
                box.removeActionListener( listeners[ i ] );

            box.removeAllItems();

            for( int i = 0; i < list.length; i++ )
                box.addItem( list[ i ] );

            // Aenderungen an der Box sollen jetzt wieder ActionEvents
            // ausloesen
            for( int i = 0; i < listeners.length; i++ )
                box.addActionListener( listeners[ i ] );
        } // if


        // Wenn sich der Eintrag selected von der momentanen Auswahl
        // unterscheidet, Eintrag selected auswaehlen

        setSelectedItem( box, selected );

    } // refreshJComboBox




    //************************************************************
    // private variables
    //************************************************************

    private ActionListener _action_listener;
    private DisplayPanel   _display_panel;


    //************************************************************
    // constructors
    //************************************************************



    /**
     * Erzeugen des Panels
     */

    //============================================================
    public AbstractPanel( ActionListener action_listener,
                          DisplayPanel   display_panel )
    //============================================================
    {
        _action_listener = action_listener;
        _display_panel   = display_panel;


        if( _action_listener == null )
        {
            _action_listener = this;
            _display_panel.addActionListener( this );
        } // if

        setLayout( new GridBagLayout() );
    } // AbstractPanel



    /**
     * @see ActionListener
     */

    //============================================================
    public void actionPerformed( ActionEvent e ){}
    //============================================================

    /**
     * @see ItemListener
     */

    //============================================================
    public void itemStateChanged( ItemEvent e ){}
    //============================================================

    /**
     * @see AdjustmentListener
     */

    //============================================================
    public void adjustmentValueChanged( AdjustmentEvent e ){}
    //============================================================



    //************************************************************
    // public methods
    //************************************************************


    /**
     */

    //============================================================
    abstract public void refresh();
    //============================================================


    //************************************************************
    // protected methods
    //************************************************************

    /**
     */

    //============================================================
    protected void fireActionEvent( int action )
    //============================================================
    {
        if( _action_listener == null || action == NO_ACTION  )
            return;

        _action_listener.actionPerformed(
            new ActionEvent( this, action, "" ) );
    } // fireActionEvent


    /**
     */

    //============================================================
    protected DisplayPanelScene getScene()
    //============================================================
    {
        if( _display_panel == null )
            return null;

        return _display_panel.getScene();
    } // getScene


    /**
     */

    //============================================================
    protected DisplayPanel getDisplayPanel()
    //============================================================
    {
        return _display_panel;
    } // getDisplayPanel



    //************************************************************
    // private methods
    //************************************************************

} // AbstractPanel
