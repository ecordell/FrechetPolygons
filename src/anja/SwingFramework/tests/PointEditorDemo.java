/*
 * File: PointEditorDemo.java
 * Created on Sep 1, 2006 by ibr
 *
 * TODO Write documentation
 */
package anja.SwingFramework.tests;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.UIManager;


/**
 * 
 * Demonstration app for the JPointScene / JPointEditor components.
 * Shows basic operation of the point editing system.
 * 
 * @author ibr
 *
 * TODO Write documentation
 */

public class PointEditorDemo
{        
    //*************************************************************************
    //                              Constructors
    //*************************************************************************
    
    public PointEditorDemo()
    {
        // stub
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
        JFrame frame = new JFrame("Point editing demo");
        
        PointEditorExample editor = new PointEditorExample(frame);
        
        frame.getContentPane().add(editor, BorderLayout.CENTER);
    
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);      
    }
}

		
  
    
    