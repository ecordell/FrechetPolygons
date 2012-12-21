package ShortestPathLP;

import anja.swinggui.StartButtonApplet;

/**
 * This applet provides the start button that will start the offline algorithm
 * to calculate the shortest path in a simple polygon by Lee and Preparata.
 * 
 * @author Anja Haupts
 * @version 1.0.0
 * @since 28.10.07
 */

public class ShortestPathLPApplet extends StartButtonApplet
{
        // *************************************************************************
        // Private constants
        // *************************************************************************
        
        /** randomly chosen!! */
        private static final long serialVersionUID = 556L;


        // *************************************************************************
        // constructor
        // *************************************************************************

        /**
         * Starts the applet.
         * 
         * @see appsSwingGui.ShortestPathLP.ShortestPathLP
         */
        public ShortestPathLPApplet()
        {
                super("Start the algorithm!");
                addApplication("appsSwingGui.ShortestPathLP.ShortestPathLP");
        }// constructor

} // ShortestPathLPApplet
