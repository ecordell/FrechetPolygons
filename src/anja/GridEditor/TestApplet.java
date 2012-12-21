package anja.GridEditor;

import java.awt.*;
import javax.swing.*;

public class TestApplet extends JApplet {

    public TestApplet() {
    }
    
    private static JFrame _applet_window;
    /**
     * Main function for running this as standalone program instead of an applet
     */
    public static void main(String args[]) {
    	TestApplet applet = new TestApplet();
        _applet_window = new JFrame("GridEditor-Test");
        
        _applet_window.setLocation(100, 100 );
        _applet_window.setSize(800, 600);
        _applet_window.getContentPane().add(applet);
        applet.init();
        _applet_window.setVisible(true);
        applet.start();
    } // main
    
    public void init() {
    	GridEditor ge = new GridEditor();
    	// if running as standalone application the editor needs 
    	// the window listener to stop the animation thread when the
    	// window is closed
    	if (_applet_window != null) _applet_window.addWindowListener(ge);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(ge, BorderLayout.CENTER);
    	//this.getContentPane().add(ge);
        Robot r = new Robot();
        r.x = 0;
        r.y = 0;
        r.startX = 0;
        r.startY = 0;
        r.targetX = 3;
        r.targetY = 1;        
        r.strategy = new NewSearchStrategy();
        ge.AddRobot(r);
        ge.SetActiveRobot(r);
        Robot r2 = new Robot();
        r2.x = 4;
        r2.y = 4;
        r2.startX = 4;
        r2.startY = 4;
        r2.targetX = 1;
        r2.targetY = 3;        
        r2.strategy = new ShannonsMouseStrategy();
        ge.AddRobot(r2);
        Robot r3 = new Robot();
        r3.x = 4;
        r3.y = 0;
        r3.startX = 4;
        r3.startY = 0;
        r3.targetX = 3;
        r3.targetY = 3;        
        r3.strategy = new ShannonsMouseStrategy();
        ge.AddRobot(r3);
        Robot r4 = new Robot();
        r4.x = 0;
        r4.y = 4;
        r4.startX = 0;
        r4.startY = 4;
        r4.targetX = 1;
        r4.targetY = 1;        
        r4.strategy = new ShannonsMouseStrategy();
        ge.AddRobot(r4);
        
    }
}
