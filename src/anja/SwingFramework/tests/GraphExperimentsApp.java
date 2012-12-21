/*
 * Created on Oct 12, 2005
 *
 * GraphExperimentsApp.java
 */
package anja.SwingFramework.tests;

import java.awt.BorderLayout;
import javax.swing.*;

/*
 *  Just a test application for various graph-related
 *  experiments.  Expect a whole lot of lazy copy-paste 
 *  stuff here :]
 * 
 */
public class GraphExperimentsApp
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
        
    //*************************************************************************
    //                              Constructors
    //*************************************************************************
    
    public GraphExperimentsApp()
    {
        
    }
    
    //*************************************************************************
    //                         Public instance methods
    //*************************************************************************
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {           
        try 
        {
            UIManager.setLookAndFeel(
             UIManager.getCrossPlatformLookAndFeelClassName());
        } 
        catch (Exception e) {}
    
        //Create the top-level container and add contents to it.
        
        JFrame frame = new JFrame("Graph experiments");
        //GraphExperimentsApp app = new GraphExperimentsApp();
        
        //Component contents = app.createComponents();
        GraphExperiments editor = new GraphExperiments(frame);
        
        frame.getContentPane().add(editor, BorderLayout.CENTER);
    
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        
     
        
    }
        
    //*************************************************************************
    //                       Protected instance methods
    //*************************************************************************
    
    //*************************************************************************
    //                        Private instance methods
    //*************************************************************************
     
}


