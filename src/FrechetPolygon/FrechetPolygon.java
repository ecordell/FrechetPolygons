package FrechetPolygon;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import anja.swinggui.BufferedCanvas;
import anja.swinggui.DisplayPanel;

/**
 * Provides the Swing applet for the application that computes the shortest path
 * according to the algorithm by Lee and Preparata.
 * 
 * @author Anja Haupts
 * @version 1.3 02.12.07
 * 
 */
public class FrechetPolygon extends JFrame implements WindowListener,
                ItemListener
{
        // *********************************************************************
        // Private constants
        // *********************************************************************

        /** randomly chosen!! */
        private static final long serialVersionUID = 555L;

        // position and size of the main window when the application starts:
        private static final int _DEFAULT_POS_X = 200;

        private static final int _DEFAULT_POS_Y = 100;

        private static final int _DEFAULT_WIDTH = 680;

        private static final int _DEFAULT_HEIGHT = 360;

        // *********************************************************************
        // Protected variables
        // *********************************************************************

        /** the lower panel, where the buttons and Checkboxes are placed */
        protected JPanel lowerPanel;

        // *********************************************************************
        // Private variables
        // *********************************************************************

        // editor with Extension and DisplayPanel for the polygon:
        private FrechetPolygonEditor _editor;

        private FrechetPolygonExtension _extension;

        protected DisplayPanel _display;

        /**
         * the Checkbox, which allows to select, whether the
         * Seidel-Triangulation should be visible
         */
        private JCheckBox _showTria;

        /** the Checkbox for the lower panel to show the DualGraph */
        private JCheckBox _showDualGraph;

        /**
         * the button for the lower panel to select wheter a automatic funnel
         * evolution should be shown
         */
        private JRadioButton _run;

        /**
         * the button for the lower panel to select wheter the funnel evolution
         * should be shown in steps
         */
        private JRadioButton _steps;

        /**
         * the button for the lower panel to select wheter the full shortest
         * path should be visible immediately
         */
        private JRadioButton _path;

        /** groups the radio buttons */
        private ButtonGroup _group;


        // *********************************************************************
        // Constructors
        // *********************************************************************

        /**
         * The constructor generates a ExtendablePolygonEditor with Extension.
         * This editor is subordinated to the DisplayPanel.
         */
        public FrechetPolygon()
        {

                super("The Algorithm by Lee and Preparata");

                _display = new DisplayPanel();
                _extension = new FrechetPolygonExtension();
                lowerPanel = new JPanel();
                _extension.panel = lowerPanel;
                _editor = new FrechetPolygonEditor(_extension);
                _display.setScene(_editor);
                _extension.display = _display;

                // registring the mouse-listener of the editor at the
                // DisplayPanel
                BufferedCanvas b_canvas = _display.getCanvas();
                b_canvas.addMouseListener(_editor);
                b_canvas.addMouseMotionListener(_editor);

                // structuring the layout of the applet
                setLocation(_DEFAULT_POS_X, _DEFAULT_POS_Y);
                setSize(_DEFAULT_WIDTH, _DEFAULT_HEIGHT);
                // adding a WindowListener
                addWindowListener(this);

                // adding the buttons to the lowerPanel
                _run = new JRadioButton("Run");
                _steps = new JRadioButton("Steps");
                _path = new JRadioButton("Path");

                _showTria = new JCheckBox("Show Triangulation");
                _showDualGraph = new JCheckBox("Show Dual Graph");

                _showTria.setSelected(false);
                _showDualGraph.setSelected(false);
                _steps.setSelected(true);

                _group = new ButtonGroup();

                _group.add(_run);
                _group.add(_steps);
                _group.add(_path);

                lowerPanel.add(_showDualGraph, 0);
                lowerPanel.add(_showTria, 0);
                lowerPanel.add(_path);
                lowerPanel.add(_run);
                lowerPanel.add(_steps);

                _path.addItemListener(this);
                _run.addItemListener(this);
                _steps.addItemListener(this);

                _showTria.addItemListener(this);
                _showDualGraph.addItemListener(this);

                // initialising the drawing area
                getContentPane().add(_display, BorderLayout.CENTER);

                // adding the lowerPanel
                getContentPane().add(lowerPanel, BorderLayout.SOUTH);

        } // ShortestPathLP constructor


        // *********************************************************************
        // Class methods
        // *********************************************************************

        /**
         * main method for the execution as an application instead of an applet
         */
        public static void main(String args[])
        {
                FrechetPolygon this_applet = new FrechetPolygon();
                this_applet.setVisible(true);

        } // main

        
        // *********************************************************************
        // Interface ItemListener
        // *********************************************************************
        /**
         * Catches the events for the checkboxes and handles them.
         * 
         * @param e
         *                the ItemEvent that is handled
         */
        public void itemStateChanged(ItemEvent e)
        {
                Object source = e.getItemSelectable();

                // if the user wants to see the triangulation
                if (source == _showTria)
                {
                        _editor.showTria = _showTria.isSelected();
                        _editor.recalculate();
                        return;

                }// if(source == _showTria)

                // if the user wants to see the dual graph
                if (source == _showDualGraph)
                {
                        _editor.showDualGraph = _showDualGraph.isSelected();
                        _editor.recalculate();
                        return;

                }// if (source == _showDualGraph)

                // if the user wants to see the complete shortest path at once
                // or the funnels automatically evolving
                if (source == _path || source == _run)
                {
                        _extension.setNext(false);
                        _extension.setPrev(false);

                }// if (source == _path)

                // if the user wants to see the shortest path evolution
                // step by step
                if (source == _steps)
                {
                        _extension.setNext(true);
                        _extension.setPrev(false);

                }// if (source == _path)

                _editor.showRun = _run.isSelected();
                _editor.showCompletePath = _path.isSelected();
                _editor.showSteps = _steps.isSelected();
                _editor.recalculate();
                return;

        }// public void itemStateChanged(ItemEvent e)
       

        // *********************************************************************
        // Interface WindowListener
        // *********************************************************************

        /**
         * If one of the windows is closed, they both (Start-Button-Applet and
         * ShortestPathLP-applet close).
         * 
         * @param e
         *                the WindowEvent
         */
        public void windowClosing(WindowEvent e)
        {
                System.exit(0);

        } // public void windowClosing(WindowEvent e)


        // *********************************************************************
        // unused methods of the Interface WindowListener
        // *********************************************************************

        // the following methods are unused:
        public void windowActivated(WindowEvent e)
        {
        }


        public void windowClosed(WindowEvent e)
        {
        }


        public void windowDeactivated(WindowEvent e)
        {
        }


        public void windowDeiconified(WindowEvent e)
        {
        }


        public void windowIconified(WindowEvent e)
        {
        }


        public void windowOpened(WindowEvent e)
        {
        }

} // ShortestPathLP
