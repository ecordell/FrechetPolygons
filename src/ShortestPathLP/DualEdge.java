package ShortestPathLP;

import anja.geom.Point2;
import anja.geom.Segment2;


/**
 * This class implements the dual edges which form a dual graph. These edges
 * are undirected.
 * 
 * @author Anja Haupts
 * 
 */
public class DualEdge
{
        // *************************************************************************
        // Private instance variables
        // *************************************************************************

        /** two directed edges form the one undirected DualEdge*/
        private Segment2 _dualEdge1;
        /** two directed edges form the one undirected DualEdge*/
        private Segment2 _dualEdge2;
        
        /** one end point of the DualEdge, which lies in the one plane*/
        private Point2 _end1;        
        /** the other end point of the DualEdge, which lies in the other plane*/
        private Point2 _end2;
        
        /** the original_edge, so the dual to the dual edge*/
        private Segment2 _original_edge;
        
        // *************************************************************************
        // Constructors
        // *************************************************************************
        /**
         * Constructs a undirected (Dual) Edge from point end1 to point end2.
         * @param end1
         * @param end2
         */
        public DualEdge(Point2 end1, Point2 end2) 
        {                                
                this(end1, end2, null);
        }// public DualEdge(Point2 end1, Point2 end2)
        
        //*************************************************************************
        
        /**
         * Constructs a undirected (Dual) Edge from point end1 to point end2
         * and saves the original edge.
         * 
         * @param end1
         * @param end2
         * @param original_edge
         */
        public DualEdge(Point2 end1, Point2 end2, Segment2 original_edge) 
        {
                _dualEdge1 = new Segment2(end1, end2);
                _dualEdge2 = new Segment2(end2, end1);
                
                _end1 = end1;
                _end2 = end2;
                
                _original_edge = original_edge;
                                
        }// public DualEdge(Point2 end1, Point2 end2, Segment2 original_edge)
        
        // *************************************************************************
        // Public instance methods
        // *************************************************************************
        
        /**
         * Returns the first end of the DualEdge.
         * @return Point2
         */
        public Point2 get_end1()
        {
                return this._end1;
                
        }// public Point2 get_end1()
        
        //*************************************************************************
        
        /**
         * Returns the second end of the DualEdge.
         * @return Point2
         */
        public Point2 get_end2()
        {
                return this._end2;
                
        }// public Point2 get_end2()
        
        //*************************************************************************
        
        /**
         * Returns the original edge of the DualEdge.
         * @return Segment2
         */
        public Segment2 get_original_edge()
        {
                return this._original_edge;
                
        }// public Segment2 get_original_edge()
        
        //*************************************************************************
        
        /**
         * Returns the first directed edge of the DualEdge.
         * @return Segment2
         */
        public Segment2 get_dualEdge1()
        {
                return this._dualEdge1;
                
        }// public Segment2 get_dualEdge1()

        //*************************************************************************
        
        /**
         * Returns the second directed edge of the DualEdge.
         * @return Segment2
         */
        public Segment2 get_dualEdge2()
        {
                return this._dualEdge2;
                
        }// public Segment2 get_dualEdge2()
       
        //*************************************************************************
        
        /**
         * Returns true, if two dual edges are equal, false else.
         * @return boolean
         * @param secondDE
         */
        public boolean equals(DualEdge secondDE)
        {
                if (((this._end1 == secondDE._end1) && (this._end2 == secondDE._end2))
                                || ((this._end2 == secondDE._end1) && (this._end1 == secondDE._end2))) 
                {
                                return true;
                }//if

                return false;

        }// public boolean equals(DualEdge secondDE)
                
}// class DualEdge
