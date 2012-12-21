/*
 * NewgraphDebug.java
 */

package anja.graph.debug;

import java.awt.BorderLayout;
import javax.swing.*;

/*
 * This is a debug application based on the NewgraphEditor of the SwingFramework.
 * It shows a textual representation of the graph which is created with the editor
 * as well as the connections between the edges of a vertex graphically.
 */
public class GraphDebug
{

    public  GraphDebug()
    {
        
    }
    
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
        GraphDebugPanel panel = new GraphDebugPanel(frame);
        
        frame.getContentPane().add(panel, BorderLayout.CENTER);
    
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}



