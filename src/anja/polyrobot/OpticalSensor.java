package anja.polyrobot;

import anja.polyrobot.robots.*;
import anja.geom.*;
import anja.util.*;
import java.util.*;

//import apps.polyrobot.*; <---- What the hell was this for?????

/*
 *
 * @version 0.1 25.2.04
 * @author Bastian Demuth
 */

//****************************************************************
public class OpticalSensor
//****************************************************************
{

    //************************************************************
    // private variables
    //************************************************************

    private Robot _robot;

    // Sichtbarkeitsradius
    private float _vis_radius;

    private static final double	_EPSILON  = 0.01, _DELTA =0.1;

    /* radial sortierte Liste aller Ecken der polygonalen Szene, 
     * fuer Radialsweep
     */
    private Point2List _sorted_vertices = null;
    private Polygon2[] _polygons;

    public boolean sighted_new_obstacle;

    //************************************************************
    // constructors
    //************************************************************

    //============================================================
    public OpticalSensor(Robot _robot)
    //============================================================
    {
        this._robot =_robot;
        _polygons = new Polygon2 [_robot.getScene().getPolygons().length +1];
        
        System.arraycopy((_robot.getScene().getPolygons()),0,
                          _polygons,1,_polygons.length -1);
        
        _polygons[0] = _robot.getScene().getBoundingPolygon();

    } // OpticalSensor


    //	============================================================
    public OpticalSensor(OpticalSensor os)
    //============================================================
    {
        _robot = os._robot;
        _vis_radius = os._vis_radius;
        _polygons = os._polygons;
   
    } // OpticalSensor

    //************************************************************
    // public methods
    //************************************************************

    //============================================================
    public void setVisRadius(float radius)
    //============================================================
    {
        _vis_radius = radius;
    }// setVisRadius


    //============================================================
    public float getVisRadius()
    //============================================================
    {
        return _vis_radius;
    }// getVisRadius


    //============================================================
    public void setRobot(Robot robot)
    //============================================================
    {
        _robot = robot;
    }// setRobot

    /**
     *	Methode zur Berechnung des am naechsten am Zielpunkt 
     *	des uebergebenen Strahls liegenden sichtbaren Punktes auf dem Strahl	
     */


    //============================================================
    public Point2 furthestVisibleRayPoint(Ray2 ray)
    //============================================================
    {

        //System.out.println("");

        //System.out.println("furthestVisibleRayPoint");
        Polygon2Scene scene = _robot.getScene();
        Point2 position = _robot.getCoors();
        Point2 source= ray.source();
        Point2 target = ray.target();

        Point2 first_source = source, second_source = null;


        // Lage des Sichtbarkeitskreises relativ zum Strahl bestimmen

        Segment2 s;
        Point2 max_point = null;
        Intersection inters = new Intersection();


        Circle2 vis = new Circle2(position,_vis_radius);
        vis.intersection(ray,inters);

        if ( inters.result == Intersection.EMPTY )
            // kein Schnittpunkt => kein Punkt des Strahles sichtbar
        {
            return null;
        } // if

        if ( inters.result == Intersection.POINT2 )
            // ein Schnittpunkt => Zwei weitere Faelle moeglich
        {


            if ( position.distance(source) < _vis_radius + _EPSILON)
                // 1. Startpunkt liegt im Sichtkreis
            {
                //System.out.println("ein Schnittpunkt, Start in Reichweite");
                // Falls target innerhalb des Sichtkreises liegt, muessen spaeter
                // zwei Segmente ueberprueft werden: (source -> target) und (Schnittpunkt -> target)
                // Speichere hierzu Schnittpunkt als second_source
                second_source = inters.point2;

            } // if

            else
                // 2. Sichtkreis liegt tangential an dem Strahl an
                // Nur gefundener Schnittpunkt des Kreises mit dem Strahl kann
                // ueberhaupt sichtbar sein => Ueberpruefe Sichtbarkeit
            {
                Point2 only_candidate = inters.point2;
                if(isVisible(only_candidate)) return only_candidate;
                else {
                	return null;
                } 
            } // else


        }

        if ( inters.result == Intersection.LIST )
            // Zwei Schnittpunkte
        {

            Point2 p1 = (Point2) inters.list.Pop();

            Point2 p2 = (Point2) inters.list.Pop();

            // Waehle weiter vom Ziel entfernten Punkt als ersten Startpunkt der beiden Segmente,
            // die mit furthestVisibleSegmentpoint untersucht werden, den naeheren als zweiten
            if (target.distance(p1) > target.distance(p2) + _EPSILON) {
            	first_source = p1;
                second_source = p2;
            } // if
            else
            {
             	first_source = p2;
                second_source = p1;
             } // else
       
        } // if


        if(position.distance(target) > _vis_radius + _EPSILON)
            // Nur ein Segment muss untersucht werden
        {
        	//if( furthestVisibleSegmentPoint(new Segment2(first_source,target)) == null)
        	
            return furthestVisibleSegmentPoint(new Segment2(first_source,target));
        } // if

        else
            // Zielpunkt innerhalb des Sichtkreises
            // untersuche 2 Segmente
        {

            Point2 first_candidate = furthestVisibleSegmentPoint(new Segment2(first_source,target));
            Point2 second_candidate = furthestVisibleSegmentPoint(new Segment2(second_source,target));
            if(first_candidate != null && second_candidate != null)
            {
                return (target.distance(first_candidate) < target.distance(second_candidate))?
                       first_candidate : second_candidate;
            } // if
            else
            {
                if (first_candidate != null) return first_candidate;
                if (second_candidate != null) return second_candidate;
                return null;
            } // else


        } // else

    } // furthestVisibleRaypoint
    
    
    /**
     * Methode zur Berechnung des am naechsten am Zielpunkt 
     *	des uebergebenen Segments liegenden sichtbaren Punktes auf dem Segment	
     * @param segment Das zu verwendende Segment.
     * @return Dem Zielpunkt des Segments naechster sichtbarer Punkt auf dem Segment
     * oder NULL, falls kein Punkt des Segments sichtbar
     */

    //============================================================
    public Point2 furthestVisibleSegmentPoint(Segment2 segment)
    //============================================================
    {


        //System.out.println("furthestVisibleSegmentPoint");
        Point2 target = segment.target();
        Point2 furthest_point = null;


        SimpleList visible_segment_parts = visibleSegmentParts(segment);
        if (visible_segment_parts == null) return null;
        else
        {
            Segment2 furthest_visible_segment_part = (Segment2) visible_segment_parts.lastValue();
            return furthest_visible_segment_part.target();

        } // else



    } // furthestVisibleSegmentPoint


	/**
	 * Methode zur Berechnung der von der Roboterposition aus
	 * sichtbaren Stuecke eines uebergebenen Segmentes.
	 * @param segment Das zu verwendende Segment.
	 * @return Liste von sichtbaren Segmenten oder NULL, falls keine
	 *  existieren.
	 */

    //============================================================
    public SimpleList visibleSegmentParts(Segment2 segment)
    //============================================================
    {

        //System.out.println("visibleSegmentParts");
        Polygon2Scene scene = _robot.getScene();
        Point2 position = _robot.getCoors();
        Point2 source = segment.source();
        Point2 target = segment.target();


        SimpleList result = new SimpleList();


        // Ermittle Lage der Roboterposition zum Segment

        Intersection inters = new Intersection();


        // Welches Stueck des Segments liegt ueberhaupt im Sichtradius?
        // => speichere neue Enden des Segmentes
        Circle2 vis = new Circle2(position,_vis_radius);
        vis.intersection(segment,inters);

        if ( segment.distance(position) > _vis_radius + _EPSILON)
        {
            //System.out.println("Segment zu weit entfernt!");
            return null;
        } // if


        if ( inters.result == Intersection.POINT2 )
        {
            //System.out.println("Segment in einzelnem Punkt geschnitten");
            if ( position.distance(source) > _vis_radius + _EPSILON) 
            {
            	//System.out.println("deshalb source auf Schnittpkt gesetzt");
            	source = inters.point2;
            } 
            if ( position.distance(target) > _vis_radius + _EPSILON) 
            {
            	//System.out.println("deshalb target auf Schnittpkt gesetzt");
            	target = inters.point2;
            } 

        } // if

        if ( inters.result == Intersection.LIST )
        {

            //System.out.println("zwei Schnitte mit Sichtradius, verkuerze zu bearbeitendes Segment");
            Point2 p1 = (Point2) inters.list.Pop();
            Point2 p2 = (Point2) inters.list.Pop();

            if (target.distance(p1) > target.distance(p2) + _EPSILON) {
                source = p1;
                target = p2;
            } // if
            else
            {
                source = p2;
                target = p1;
            } // else

        } // if

        Segment2 being_sweeped = new Segment2(source,target);

		//System.out.println("source: " +source.toString());
		//System.out.println("target: " +target.toString());

        // Steht der Robot an einem Vertex?
        // Falls ja: Sicht eingeschraenkt
        ObstacleResult touched_or = obstacleAt(position);
        if (touched_or != null)
        {

            //System.out.println("stehe an Hindernis");
            Polygon2 touched_obstacle = touched_or.obstacle;

            Point2 vertex_right_hand = getNextVertex(touched_or.hitpoint,touched_obstacle,Robot.RIGHT_HAND);
            Point2 vertex_left_hand = getNextVertex(touched_or.hitpoint,touched_obstacle,Robot.LEFT_HAND);


            Ray2 right_hand = new Ray2 (touched_or.hitpoint,vertex_right_hand);
            Ray2 left_hand = new Ray2 (touched_or.hitpoint,vertex_left_hand);

			//System.out.println("position: "+position.toString());
			//System.out.println("vertex right hand: "+vertex_right_hand .toString());
            //System.out.println("vertex left hand: "+vertex_left_hand .toString());
            double inner_angle = touched_or.hitpoint.angle(vertex_right_hand,vertex_left_hand);

            Point2 new_segment_target = target, new_segment_source = source;

            right_hand.intersection(being_sweeped,inters);

            boolean right_hand_intersects = false;
            if (inters.result == Intersection.POINT2 && position.distance(inters.point2) > _EPSILON)
            {
                right_hand_intersects = true;
            } // if
            //System.out.println("rechte Hand auf Schnitt getestet");
            if(right_hand_intersects)
            {
                //System.out.println("Schneide Stueck des Segmentes ab: rechte hand");
                if (right_hand.orientation(source) == Point2.ORIENTATION_RIGHT)
                {
                    new_segment_source = inters.point2;
                    //System.out.println("new_segment_source geaendert");
                } // if
                if (right_hand.orientation(target) == Point2.ORIENTATION_RIGHT)
                {
                    new_segment_target = inters.point2;
                    //System.out.println("new_segment_target geaendert");
                } // if
            } // if

            left_hand.intersection(being_sweeped,inters);

            boolean left_hand_intersects = false;
            if (inters.result == Intersection.POINT2 && position.distance(inters.point2) > _EPSILON)
            {
                left_hand_intersects = true;
            } // if
            //System.out.println("linke Hand auf Schnitt getestet");
            if(left_hand_intersects)
            {
                //System.out.println("Schneide Stueck des Segmentes ab: linke hand");
                if (left_hand.orientation(source) == Point2.ORIENTATION_LEFT)
                {
                    //System.out.println("new_segment_source geaendert");
                    new_segment_source = inters.point2;
                } // if

                if (left_hand.orientation(target) == Point2.ORIENTATION_LEFT)
                {
                    new_segment_target = inters.point2;
                    //System.out.println("new_segment_target geaendert");
                } // if
            } // if

            if (right_hand_intersects && left_hand_intersects)
            {

                if( inner_angle > Math.PI + _EPSILON)
                    // stehe an spitzer Ecke, von der das Segment in zwei evtl. sichtbare Stuecke unterteilt wird
                {
                    //System.out.println("spitze Ecke => rufe visibleSegmentParts zweimal auf");
                    //System.out.println("position" + position.x+ ","+position.y);
                    //System.out.println("source: "+source.x+ ","+source.y);
                    //System.out.println("new_segment_target: "+new_segment_target.x+ ","+new_segment_target.y);
                    SimpleList first_result = visibleSegmentParts(new Segment2(source,new_segment_target));
                    SimpleList second_result = visibleSegmentParts(new Segment2(new_segment_source,target));

                    if(first_result != null)
                    {
                        first_result.addAll(second_result);
                        return first_result;
                    } // if
                    else return second_result;
                } // if
                else
                {
                    source = new_segment_source;
                    being_sweeped.setSource(source);

                    target = new_segment_target;
                    being_sweeped.setTarget(target);
                } // else

            } // if
            else
            {
                if (right_hand_intersects || left_hand_intersects)
                {
                    source = new_segment_source;
                    being_sweeped.setSource(source);

                    target = new_segment_target;
                    being_sweeped.setTarget(target);
                } // if


                else
                {
                    
                    boolean b1 = 
                    ((right_hand.orientation(source) == Point2.ORIENTATION_RIGHT)
                    && (right_hand.orientation(target) == Point2.ORIENTATION_RIGHT));
                    
                    boolean b2 = 
                    ((left_hand.orientation(source) == Point2.ORIENTATION_LEFT)
                    && (left_hand.orientation(target) == Point2.ORIENTATION_LEFT));
                    
                    if (b1 && b2) 
                    {
                    	//System.out.println("Segment unsichtbar, spitze Ecke");
                    	return null;
                    } 
                    else if ((b1 || b2) && inner_angle < Math.PI - _EPSILON) 
                    {
                    	//System.out.println("Segment unsichtbar, stumpfe Ecke");
                    	return null;
                    } 
                    //System.out.println("Segment wird von beruehrtem Hindernis nicht beeinflusst");
                } // else
            }


        } // if


        if ( source.equals(target) )
            // trivialer Fall
        {
            //System.out.println("trivialer Fall: source = target");
            if (isVisible(source)) {
                result.add(being_sweeped);
                return result;
            } // if
            else return null;
        } // if


        Point2List tripoints = new Point2List();
        tripoints.addPoint(position);
        tripoints.addPoint(source);
        tripoints.addPoint(target);
        Polygon2 triangle = new Polygon2(tripoints);

        int ori = being_sweeped.orientation(position);

        // Steht der Robotor collinear zu dem Segment?
        // vereinfacht die Betrachtung erheblich

        if (ori == Point2.ORIENTATION_COLLINEAR)
        {

            //System.out.println("Segment collinear");
            ObstacleResult or_target = null;
            ObstacleResult or_source = null;
            if(!position.equals(source)) or_source= obstacleInTheWayTo(source);
            if(!position.equals(target)) or_target= obstacleInTheWayTo(target);


            if(or_source != null && !isVisible(source))
            {
                if (being_sweeped.liesOn(position)
                        || position.distance(source)> position.distance(target) + _EPSILON)
                    being_sweeped.setSource(or_source.hitpoint);
                else return null;
            } // if


            if(or_target != null && !isVisible(target))
            {
                if (being_sweeped.liesOn(position)
                        || position.distance(source)< position.distance(target) - _EPSILON)
                    being_sweeped.setTarget(or_target.hitpoint);
                else return null;
            } // if



            result.add(being_sweeped);
            return result;

        }

        boolean COUNTERCLOCKWISE = Robot.COUNTERCLOCKWISE;
        boolean CLOCKWISE = !COUNTERCLOCKWISE;

        boolean sweep_direction;
        if (ori == Point2.ORIENTATION_LEFT) sweep_direction = COUNTERCLOCKWISE;
        else sweep_direction = CLOCKWISE;


        Segment2 position_source = new Segment2(position,source);
        Segment2 position_target = new Segment2(position,target);


        // Ermittle alle Kantenstuecke der Szene, die Stuecke des Segmentes verdecken


        boolean dir_move_around;
        if(sweep_direction == COUNTERCLOCKWISE)
        {
            dir_move_around = Robot.RIGHT_HAND;

            //System.out.println("counterclockwise");
        } // if
        else
        {
            dir_move_around = Robot.LEFT_HAND;
            //System.out.println("clockwise");
        } // else


        Point2[] vertices = _allVertices();
        Point2List relevant_edge_sources = new Point2List();
        Hashtable  edge_list = new Hashtable();
        Point2 current_vertex;



        //			durchlaufe alle Knoten
        for ( int count = 0; count < vertices.length; count ++ )

        {
            current_vertex = vertices[count];
            //System.out.println("aktueller Punkt: "+(int) current_vertex.x+ ","+(int) current_vertex.y);




            // Betrachte zugehoeriges Polygon
            Polygon2 obstacle = scene.getPolygonWithVertex(current_vertex);

            Point2 next_neighbour = getNextVertex(current_vertex,obstacle,dir_move_around );
            Segment2 next_edge = new Segment2(current_vertex, next_neighbour);


            //System.out.println("sein naechster Nachbar: "+(int) next_neighbour.x+ ","+(int) next_neighbour.y);


            // Spielt die Kante eine Rolle fuer die Sichtbarkeit?
            int relative_location = triangle.locatePoint(next_edge.source());
            triangle.intersection(next_edge,inters);
            if( inters.result == Intersection.EMPTY && relative_location != Polygon2.POINT_INSIDE)
            {
                //System.out.println("Kante nicht im relevanten Bereich");
                continue;
            } // if
            if (next_edge.distance(position) < _EPSILON)
            {
                //System.out.println("Robot steht auf Kante => Kante schon behandelt");
                continue;
            } // if
            if(!_viewedFromOutside(next_edge) )
            {
                //System.out.println("Kante ist vom Robot abgewandt");
                continue;
            } // if

            //System.out.println("Kante sichtbar");

            boolean target_covered_by_this_edge = false;

            next_edge.intersection(position_target,inters);

            if ( inters.result == Intersection.POINT2 && !inters.point2.equals(next_edge.target()))
            {
                //System.out.println("Kante wird wegen position-target-schnitt abgeschnitten");
                target_covered_by_this_edge = true;
                next_edge.setTarget(inters.point2);
            } // if






            next_edge.intersection(position_source,inters);
            if ( inters.result == Intersection.POINT2 && !inters.point2.equals(next_edge.source()))
            {

                //System.out.println("Kante wird wegen position-source-schnitt abgeschnitten");
                if (target_covered_by_this_edge) return null;

                next_edge.setSource(inters.point2);

            } // if



            being_sweeped.intersection(next_edge, inters);
            if ( inters.result == Intersection.POINT2)
            {
                //System.out.println("Kante wird wegen Segmentschnitt abgeschnitten");


                int position_ori = being_sweeped.orientation(position);
                int source_ori = being_sweeped.orientation(next_edge.source());
                int target_ori = being_sweeped.orientation(next_edge.target());

                if ( position_ori  != source_ori )
                {
                    next_edge.setSource(inters.point2);
                } // if
                if ( position_ori  != target_ori )
                {
                    next_edge.setTarget(inters.point2);
                } // if
            } // if


            Point2 edge_source = next_edge.source();
            relevant_edge_sources.addPoint(edge_source);
            edge_list.put(edge_source,next_edge);

        } // for



        // Eigentlicher Sweep durch die gefundenen relevanten Kantenstuecke der Szene
        //System.out.println("start Sweep");
        Point2List events = _sortPoints(source,relevant_edge_sources,sweep_direction);
        // Einige Hilfsvariablen

        // Abgeschlossener Teil des Segmentes beim Sweep
        Segment2 completed_segment_part = new Segment2(source,source);

        Point2 edge_source, edge_target;
        Ray2 ray_position_to_current_edge_source = null;
        Ray2 ray_position_to_current_edge_target = null;
        Segment2 next_result_segment = null;

        //System.out.println("Sweep geht ueber "+events.length()+ " Kanten.");
        for (int i = 0; i < events.length(); i++)
        {
            Point2 current_event = (Point2) events.points().getValueAt(i);

            Segment2 current_edge = (Segment2) edge_list.get(current_event);
            edge_source = current_edge.source();
            edge_target = current_edge.target();

            //System.out.println("aktuelle Kante: "+edge_source.toString()+","+edge_target.toString());

            // aktualisiere Strahl vom Roboterstandpunkt zur source des aktuellen Segmentes
            if (ray_position_to_current_edge_source == null)
                ray_position_to_current_edge_source = new Ray2(position,edge_source);
            else ray_position_to_current_edge_source.setTarget(edge_source);




            ray_position_to_current_edge_source.intersection(being_sweeped, inters);

            if (inters.result == Intersection.EMPTY)
            {
                if(ray_position_to_current_edge_source.liesOn(source))
                {
                    inters.result = Intersection.POINT2;
                    inters.point2 = source;
                } // if
                else
                {
                    //System.out.println("Error, unexpected empty intersection.");
                    break;
                }
            } // if



            if (inters.result == Intersection.POINT2)
            {

                if(completed_segment_part.distance(inters.point2) > _EPSILON)
                    // Luecke bei der Verdeckung von being_sweeped
                    // => neues sichtbares Segmentstueck wird erzeugt und in result eingefuegt
                {
                    //System.out.println("Luecke => neues sichtbares Segmentstueck");
                    next_result_segment
                    = new Segment2(completed_segment_part.target(),inters.point2);
                    if(!result.contains(next_result_segment)) result.add(next_result_segment);

                } // if
            } // if


            // Nun Verdeckung durch das aktuelle Segment bestimmen und
            // completed_segment_part ensprechend aktualisieren

            if (ray_position_to_current_edge_target == null)
                ray_position_to_current_edge_target = new Ray2(position,edge_target);
            else ray_position_to_current_edge_target.setTarget(edge_target);



            ray_position_to_current_edge_target.intersection(being_sweeped, inters);


            if (inters.result == Intersection.EMPTY)
            {
                if(ray_position_to_current_edge_target.liesOn(target))
                {
                    inters.result = Intersection.POINT2;
                    inters.point2 = target;
                } // if
                else
                {
                    //System.out.println("Error, unexpected empty intersection.");
                    break;
                }
            } // if

            if (inters.result == Intersection.POINT2)
            {
                if(!completed_segment_part.liesOn(inters.point2))
                {
                    //System.out.println("Kante verdeckt Segment ");
                    completed_segment_part.setTarget(inters.point2);
                } // if

            } // if

        } // for


		// Ueberpruefe, ob Source oder Target des Segmentes being_sweeped
		// sichtbar sind und evtl. zusaetzliche Segmente zum Resultat hinzugefuegt
		// werden muessen, die beim Sweep nicht beruecksichtigt werden

        if(isVisible(target))
            // sichtbares Stueck am Ende von being_sweeped
            // => neues sichtbares Segmentstueck wird erzeugt und in result eingefuegt
        {
            boolean already_contained = false;
            //System.out.println("Segment-Ziel sichtbar ");
            if(!result.empty() )
            {
                Segment2 last_result_segment = (Segment2) result.lastValue();
                already_contained = last_result_segment.target().equals(target);
            }

            next_result_segment = new Segment2(completed_segment_part.target(),target);
            if(!already_contained) result.add(next_result_segment);

        } // if

		//else System.out.println("Segment-Ziel unsichtbar ");

        if(isVisible(source)) 
        {
            //System.out.println("Segment-Start sichtbar");
            boolean already_contained = false;

            if(!result.empty() )
            {
                Segment2 first_result_segment = (Segment2) result.firstValue();
                already_contained = first_result_segment.source().equals(source);
            	if(!already_contained) result.insert(result.first(),new Segment2 (source,source));
            }
            else result.add(new Segment2 (source,source));
        } // if

		//else System.out.println("Segment-Start unsichtbar");

        if(!result.empty())
        {
            return result;
        } // if

        return null;


    } // visibleSegmentParts


   
     	
	
	/**
	 * Methode zur Berechnung des sichtbaren Anteils eines aktuell 
	 * zu umrundenden Polygons in Umrundungsrichtung. 
	 * Wird nach Bewegung des Roboters zur Aktualisierung der 
	 * Sichtbarkeitsinformationen verwendet. 
	 * Es wird ein Radialsweep durchgefuehrt.
	 * @param current_obstacle das zu umrundende Hindernis
	 * @param dir die gewuenschte Umlaufrichtung: 
	 * 	Robot.RIGHT_HAND oder Robot.LEFT_HAND
	 * @return offenes Polygon das den neu sichtbaren Rand 
	 * 	des uebergebenen Hindernisses beschreibt
	 */

    //============================================================
    public Polygon2 updateVisibleObstaclePart(ObstacleResult current_obstacle, boolean dir)

    //============================================================
    {


        //System.out.println("");
        //System.out.println("updateVisibleObstaclePart");
        Point2 position = _robot.getCoors();
        Point2 hitpoint = current_obstacle.hitpoint;

        Polygon2 currently_visible_obstacle = (Polygon2) current_obstacle.obstacle.clone();
        int edge_number = current_obstacle.edge_number;
        //System.out.println("aktuelle Kante: "+edge_number);

        Circle2 vis = new Circle2(position,_vis_radius);


        // Knoten des sichtbaren Polygonstueckes werden in Liste gespeichert
        Point2List result = new Point2List();


        // bekannte Teile des Hindernisses aus dem letzten Schritt werden vergessen,
        // letzter bekannter Punkt auf dem Hindernisrand
        // aus dem letzten Schritt ist der erste Punkt in aktuellem Schritt
        result.addPoint(hitpoint);

        // Richtung, in der das Polygonkanten-Array zu durchlaufen ist
        // (abhaengig von gewaehlter Umrundungsrichtung etc.)
        int succ = (dir == Robot.RIGHT_HAND) ? 1 : -1;

        int polygon_ori = _polygonOrientation(currently_visible_obstacle);

        succ *= polygon_ori;

        // alle Kanten des aktuellen Polygons in Array zwischengespeichert
        Segment2[] edges = currently_visible_obstacle.edges();

        // radialer Sweep beginnt bei letztem bekannten Punkt des Hindernisrandes
        Point2 sweep_point = hitpoint;

        if (sweep_point.equals(position))
        {
            // Roboter hat eine Ecke erreicht, die zuvor jegliche Sicht
            // auf weitere Hindernisteile versperrte

            // von der Ecke aus ist die naechste Hinderniskante einsehbar
            // Sweep beginnt in Richtung dieser Kante
            sweep_point = getNextVertex(position,currently_visible_obstacle,dir);

        } // if

        // sortiere alle Vertices der Szene radial in Richtung dir,
        // von Winkel des Segments zwischen aktueller Position und Sweeppoint beginnend
        if(dir == Robot.LEFT_HAND)_sceneSort(sweep_point,Robot.CLOCKWISE);
        if(dir == Robot.RIGHT_HAND)_sceneSort(sweep_point,Robot.COUNTERCLOCKWISE);
        SimpleList sv = _sorted_vertices.points();


        Point2 current_point;
        Segment2 s = null;
        Intersection inters = new Intersection();

        // durchlaufe sortierte Vertexliste
        while ( !sv.empty() ) {

            current_point = (Point2) sv.Pop();

            //System.out.println("aktueller Punkt: "+(int) current_point.x+ ","+(int) current_point.y);
            // ist aktueller Vertex entfernter Endpunkt der aktuellen Kante des zu umrundenden Hindernisses?
            if ( (succ == 1 && current_point.equals(edges[edge_number].target())
                    || succ == -1 && current_point.equals(edges[edge_number].source()))
               )
                // Ja
            {
				 //System.out.println("entfernter Endpunkt der aktuellen Kante entdeckt");
                // Ist dieser Endpunkt sichtbar?
                if ( position.distance(current_point) < _vis_radius + _EPSILON)
                {
                    //System.out.println("dieser Endpunkt ist sichtbar");
                    // Ja => ist ein Eckpunkt des sichtbaren Hindernisrandes
                    if (!result.points().contains(current_point) )
                        result.addPoint(current_point);

                    // springe zu naechster Hinderniskante
                    edge_number += succ;
                    if ( edge_number >= edges.length ) edge_number = 0;
                    if (edge_number < 0) edge_number = edges.length-1;

                    Segment2 current_edge = edges[edge_number];
                    //System.out.println("naechste Kante: "+edge_number);

                    // Bei collinearen Punkten kann es passieren, dass
                    // der andere Endpunkt der naechsten Kante schon
                    // abgearbeitet wurde. Daher muss in diesem Fall
                    // dieser Endpunkt noch einmal an den Anfang der Liste
                    // der zu bearbeitenden Vertices eingefuegt werden!
                    Point2 other_vertex_on_next_edge;
                    if ( succ == 1 )  other_vertex_on_next_edge = edges[edge_number].target();
                    else other_vertex_on_next_edge = edges[edge_number].source();

                    if(other_vertex_on_next_edge.isCollinear(position,current_point)
                            &&  !sv.contains(other_vertex_on_next_edge) )
                    {
                        //System.out.println("Knoten noch einmal eingefuegt");
                        sv.Push(other_vertex_on_next_edge);
                    } // if


                    if (!_viewedFromOutside(current_edge))
                    {

                        //System.out.println("sehe kante nicht");
                        break;
                    } // if


                } // if

                else
                {
                    //System.out.println("zu weit entfernter Kantenendpunkt ");
                    // Nein => Sichtbarkeitsradius muss aktuelle Kante schneiden,
                    // Schnittpunkt ist der letzte Punkt des sichtbaren Hindernisrandes
                    s = new Segment2(result.lastPoint(),current_point);
                    vis.intersection(s,inters);

                    // Bei nur einem Schnittpunkt ist der Fall klar
                    Point2 p = null;
                    if ( inters.result == Intersection.POINT2 ) 
                    {
                    	//System.out.println("ein Schnittpunkt");
                    	//System.out.println("Radius ="+vis.radius);
                    	//System.out.println("Pos ="+vis.centre.toString());
                    	p = inters.point2;
                    } 


                    else if (inters.result == Intersection.LIST)
                    {
                        // Bei zwei Schnittpunkten besteht sichtbarer Hindernisrand nur aus
                        // einem Teil dieser Kante und der eine Eckpunkt des Randes war schon bekannt
                        p = (Point2)inters.list.Pop();
                        if ( p.equals(result.lastPoint()) ) p = (Point2)inters.list.Pop();
                    } // else if

                    // Fuege gefundenen Endpunkt des sichtbaren Hindernisrandes zur Liste hinzu
                    if ( p!= null )
                    {
                        if (!result.points().contains(p) )
                            result.addPoint(p);

                    } // if
                    // Brich Sweep hier ab
                    break;
                } // else
            } // if
            else
            {
                // ist aktueller Vertex naher Endpunkt der aktuellen Kante
                // des zu umrundenden Hindernisses?
                if ( (succ == -1 && current_point.equals(edges[edge_number].target())
                        || succ == 1 && current_point.equals(edges[edge_number].source()))
                   )
                { // Ja

                    if (position.distance(current_point ) > position.distance(hitpoint))
                    {
                        //System.out.println("Hitpoint schon naeher als singulaerer Punkt");
                        continue;
                    } // if

                    if (!position.equals(current_point)) {
                        //System.out.println("Gehe auf singulaeren Punkt zu");
                        break;
                    } // if
                    else
                    {
                        //System.out.println("singulaerer Punkt erreicht");
                    } // else
                } // if

                else
                { // Nein, Vertex kann Sicht auf den Hindernisrand versperren

                    if (current_point.equals(position)) continue;
                    // Liegt der Vertex vom Roboter aus gesehen vor oder hinter der aktuellen Kante?
                    s = new Segment2(position,current_point);
                    s.intersection(edges[edge_number],inters);

                    if ( inters.result == Intersection.EMPTY )
                    {
                        // Vertex liegt vor der aktuellen Kante => versperrt Sicht auf diese Kante
                        // Punkt wird letzter Punkt des sichtbaren Hindernisrandes, wenn er sichtbar ist
                        //System.out.println("sichtversperrender Vertex");

                        Ray2 ray = new Ray2(position,current_point);

                        ray.intersection(edges[edge_number],inters);

                        Point2 vis_limit = null;
                        if ( inters.result == Intersection.POINT2 && inters.point2 != null)

                        {
                            vis_limit = inters.point2;
                        } // if
                        else if ( inters.result == Intersection.SEGMENT2 )
                            //aktuelle Kante wird ganz gesehen, da von Sichtbarkeitsstrahl ueberdeckt
                        {

                            if ( succ == 1 )
                            {
                                vis_limit = edges[edge_number].target();
                            } // if
                            else
                            {

                                vis_limit = edges[edge_number].source();
                            } // else
                        } // else if


                        Point2 real_vis_limit = vis_limit;

                        // Ermittelter Punkt ausser Sichtreichweite?
                        if (real_vis_limit.distance(position) > _vis_radius + _EPSILON)
                            // Ja
                        {
							//System.out.println("VisLimit ausser Sichtreichweite");
                            Point2 current_edge_start = null;
                            if ( succ == 1 ) current_edge_start = edges[edge_number].source();
                            else current_edge_start = edges[edge_number].target();


                            Segment2 last_visible_edge = new Segment2(current_edge_start,vis_limit);
                            
                            if(last_visible_edge. target().equals(last_visible_edge.source()))
                            {
                            	//System.out.println("entarteter Fall");
                            } // if
                            else
                            {

                                vis.intersection(last_visible_edge,inters);
                                if (inters.result == Intersection.POINT2)
                                {
									//System.out.println("Nur ein Schnittpunkt der Kante mit Sichtkreis");
                                    if (!inters.point2.equals(current_edge_start) )
                                    {
                                        real_vis_limit = inters.point2;
                                    } // if
                                } // if

                                if (inters.result == Intersection.LIST)
                                {
                                	//System.out.println("2 Schnittpunkte der Kante mit Sichtkreis");
                                    Point2 candidate1 = (Point2) inters.list.Pop();
                                    Point2 candidate2 = (Point2) inters.list.Pop();
                                    real_vis_limit = (real_vis_limit.distance(candidate1) < real_vis_limit.distance(candidate2)) ?
                                                     candidate1 :  candidate2;
                                } // if
                              
                                
                            }  // else
                            
                        } // if

                        if(!result.points().contains(real_vis_limit))
                        {
                            result.addPoint(real_vis_limit);
                        } // if
                        break;
                    } // if
                } // else
            } // else

        } // while

        //Speichere letzten Eckpunkt des sichtbaren Hindernisrandes als Startpunkt fuer naechsten Durchlauf
        current_obstacle.hitpoint = result.lastPoint();

        // Speicher Nummer der zuletzt betrachteten Kante
        current_obstacle.edge_number = edge_number;

        //gib offenes Ergebnispolygon zurueck
        Polygon2 result_poly = new Polygon2(result);
        result_poly.setOpen();

        return result_poly;


    } // updateVisibleObstaclePart


	/**
	 * Testet uebergebenen Punkt auf Sichtbarkeit unter Beruecksichtigung 
	 * der aktuellen Roboterposition, aller Hindernisse der Szene
	 * und des Sichtradius
	 * @param p der zu testende Punkt
	 * @return true falls Punkt sichtbar, sonst false
	 */

    //	============================================================
    public boolean isVisible(Point2 p)
    //============================================================
    {
    	//System.out.println("isVisible("+p.toString()+")");
        Point2 position = _robot.getCoors();
        if ( p == null ) return false;
        if (position.distance(p) > _vis_radius + _EPSILON) return false;

		
        ObstacleResult or = obstacleInTheWayTo(p);
        //System.out.println("point: "+p.x+","+p.y);
        //if(or!= null) System.out.println("sightblocking obstacle, hitpoint: "+or.hitpoint.toString());

        if( position.equals(p) // trivial
                || or == null
                || position.distance(p) <= position.distance(or.hitpoint) + _EPSILON)
            // p IST sichtbar
        {
            return true;
        } // if
        return false;
    } // isVisible


    /**
    *	Methode zur Berechnung desjenigen Hindernispolygons, auf dessen Rand
    *	der uebergebene Punkt liegt. Die Nummer der Kante, auf der der Punkt liegt, wird 
    *	im "ObstacleResult" mit zurueckgegeben um spaeter evtl. anfallende Berechnungen zu erleichtern.
    */


    //============================================================
    public ObstacleResult obstacleAt(Point2 p)
    //============================================================
    {


        for ( int i = 0; i < _polygons.length; i++ )
        {
            if (_polygons [i] != null) 
            {
                Segment2[] edges = _polygons[i].edges();
                for ( int j = 0; j < edges.length; j++ )
                {
                    if ( edges[j].distance(p) < _DELTA)
                    {
                        Point2 plumb = edges[j].plumb(p);
                        if (plumb == null)
                        {
                            if (p.distance(edges[j].source()) < p.distance(edges[j].target())) plumb = edges[j].source();
                            else plumb =	edges[j].target();
                        } // if
                        return new ObstacleResult(plumb,_polygons[i],j);
                    } // if

                } // for
            } // if
        } // for
        return null;

    }


    /**
     *	Methode zur Berechnung desjenigen Hindernispolygons, das vom Robot aus in Richtung 
     *	des uebergebenen Punktes als erstes sichtbar ist. Wird kein solches Hindernis innerhalb 
     *	des Sichtradius' des Robots entdeckt, so wird "null" zurueckgeliefert => Weg vorlaeufig frei
     *	Der Auftreffpunkt des Sichtstrahles auf dieses Hindernis und die Nummer der Kante, auf 
     *	der dieser Auftreffpunkt liegt, werden im "ObstacleResult" mit zurueckgegeben um spaeter 
     *	evtl. anfallende Berechnungen zu erleichtern.
     */



    //============================================================
    public ObstacleResult obstacleInTheWayTo(Point2 target)
    //============================================================
    {
        return obstacleInTheWayTo(target, true);

    }


    /**
     *	Allgemeine Methode zur Berechnung desjenigen sichtversperrenden Hindernispolygons, 
     * das vom Robot aus in Richtung des uebergebenen Punktes als erstes sichtbar ist.
     * Dieses kann auch HINTER dem uebergebenen Punkt liegen! 
     * Falls limited_sight den Wert false erhaelt, ist die Sicht dabei unbeschraenkt (kein Sichtradius).
     *	Wird kein solches Hindernis gefunden, so wird "null" zurueckgeliefert => Weg frei
     *	Der Auftreffpunkt des Sichtstrahles auf dieses Hindernis und die Nummer der Kante, auf 
     *	der dieser Auftreffpunkt liegt, werden im "ObstacleResult" mit zurueckgegeben um spaeter 
     *	evtl. anfallende Berechnungen zu erleichtern.
     */


    //============================================================
    public ObstacleResult obstacleInTheWayTo(Point2 target, boolean limited_sight)
    //============================================================
    {


        //System.out.println("obstacleInTheWayTo");
        Point2 position = _robot.getCoors();
        if(_blockedByTouchedObstacle(target))
        {
            //System.out.println("Robot steht an Hindernis");
            return obstacleAt(position);

        } // if

        // Strahl vom Robot zum Ziel wird erzeugt
        Ray2 _ray_to_target = new Ray2 ( position, target );

        Intersection inters = new Intersection();

        // Nur das vom Strahl geschnittene Polygon mit minimaler Distanz zum Robot wird gesucht
        double mindist = Double.MAX_VALUE;

        //Initialisierung der Rueckgabewerte
        Point2 obstacle_hitpoint = null;
        int edge_number = 0;
        Polygon2 obstacle = null;



        Segment2 current_edge = null;
        Segment2 previous_edge = null;
        Segment2 next_edge = null;

        int succ = 0;

        //System.out.println("position" + position.x+ ","+position.y);

        // Durchlaufe alle Polygone und checke auf Schnitt mit dem Strahl
        for ( int i = 0; i < _polygons.length; i++ ) 
        {
            if (_polygons [i] != null) 
            {

                // Durchlaufe alle Kanten des aktuellen Polygons nach
                // RECHTE-HAND-Regel um eventuelle Schnittkante zu finden
                Segment2[] edges = _polygons[i].edges();
                succ = _polygonOrientation(_polygons[i]);

                int index = 0;
                for ( int j = 0; j < edges.length; j++ ) 
                {
                    current_edge = edges[index];

                    Point2 current_source =
                        succ == -1 ? current_edge.target() : current_edge.source();

                    Point2 current_target =
                        succ == 1 ? current_edge.target() : current_edge.source();

                    //System.out.println("naechste Kante: "+index);
                    // setze Index auf naechste Hinderniskante
                    // nach RECHTE-HAND-Regel
                    index += succ;
                    if ( index >= edges.length ) index = 0;
                    if (index < 0) index = edges.length-1;
					next_edge =edges[index];

					Point2 next_target =
                                succ == 1 ? next_edge.target() : next_edge.source();


                    // Kante relevant fuer Sicht?
                    if(!_viewedFromOutside(current_edge))
                    {
                        //System.out.println("Kante unsichtbar");
                        //Nein
                        previous_edge = current_edge;
                        continue;
                    } // if

                    current_edge.intersection(_ray_to_target,inters);

                    double dist = Double.MAX_VALUE;
                    Point2 interspoint = null;


                    if ( inters.result == Intersection.POINT2
                            && inters.point2.distance(position) >_EPSILON)
                    {
                        //System.out.println("einzelner Punkt");
                        // Kante schneidet den Strahl in nur einem Punkt
                        interspoint = inters.point2;

                        // Ist dieser Punkt gerade der Endknoten der aktuellen Kante
                        // in Umlaufrichtung?
                        if ( interspoint.distance(current_target) < _EPSILON)
                        {
                            // Versperrt das Hindernis die Sicht an diesem Knoten?
                            // Pruefe hierzu Lage der naechsten Hinderniskante
                            if(!_viewedFromOutside(next_edge) || next_edge.isCollinear(position))
                            {
                                // Dies ist eine spitze Ecke, oder
                                // es muss noch einen naeheren Schnittpunkt mit dem
                                // Hindernis geben, dieser braucht also nicht beruecksichtigt
                                // werden
                                //System.out.println("Naechste Kante nicht von aussen gesehen");
                                previous_edge = current_edge;
                                continue;
                            } // if

                            

                        } // if
						
						// Ist dieser Punkt gerade der Endknoten der aktuellen Kante
                        // entgegen der Umlaufrichtung?
                        // => waere schon bearbeitet
                        boolean already_checked = false;
                        if(previous_edge != null )
                        {
                        	previous_edge.intersection(_ray_to_target,inters);
                        	already_checked = inters.result != Intersection.EMPTY;
                        }
                        if ( interspoint.distance(current_source) < _EPSILON && already_checked)
                        {
                        	previous_edge = current_edge;
                         	continue;
                        }
						
						//System.out.println("Pkt. ist Kandidat weil sichtblockierend");
                        dist = position.distance(interspoint);

                    } // if


                    else if ( inters.result == Intersection.SEGMENT2 )
                        // Bei Segmentschnitt kann noch die Sicht am Vertex
                        // zwischen dieser und der naechsten Kante versperrt werden.
                        // Dies muss beim Bearbeiten dieser Kante festgestellt werden!
                    {

                        //System.out.println("Segment");
                        // Versperrt das Hindernis die Sicht an diesem Knoten?
                        // Pruefe hierzu Lage der naechsten Hinderniskante

                        if(!_viewedFromOutside(edges[index]) )
                        {
                            // Es muss noch einen naeheren Schnittpunkt mit dem
                            // Hindernis geben, dieser braucht also nicht beruecksichtigt
                            // werden
                            previous_edge = current_edge;
                            continue;
                        } // if

                        
                        double inner_angle = current_target.angle(next_target,current_source);
                        if( inner_angle >= Math.PI) 
                        {
                        	previous_edge = current_edge;	
                        	continue;
                        } // if 


                        // Sicht WIRD versperrt, speichere besagten Vertex
                        interspoint = current_target;
                        dist = position.distance(interspoint);
                    } // if

                    if (  (!limited_sight || dist < _vis_radius + _EPSILON ) && dist < mindist )
                    {
                        // Punkt ist der naechste bisher gefundene Schnittpunkt und ist sichtbar (oder Sicht unbeschraenkt)
                        mindist = dist;
                        obstacle_hitpoint = interspoint;
                        obstacle = _polygons[i];
                        //System.out.println("bisher naechster gefundener sichtblockierender Pkt. aktualisiert");
                        // index wurde in der for-Schleife eins zu weit gezaehlt
                        edge_number = index - succ;
                        if ( edge_number >= edges.length ) edge_number = 0;
                        if (edge_number < 0) edge_number = edges.length-1;
                    } // if
					previous_edge = current_edge;
                } // for
            } // if
        } // for

        //if(obstacle_hitpoint != null) System.out.println("hitpoint" + obstacle_hitpoint.x+ ","+obstacle_hitpoint.y);

        // Gib Ergebnis zurueck
        if ( obstacle_hitpoint != null )	return new ObstacleResult(obstacle_hitpoint,obstacle,edge_number);
        return null;

    } // obstacleInTheWayTo



	/**
	 * Methode zur Bestimmung des naechsten Polygon-Eckpunktes
	 * vom Punkt hitpoint aus.
	 * @param hitpoint der Ausgangspunkt
	 * @param obstacle das Hindernis, auf dem der Ausgangspunkt liegen muss
	 * @param dir gewuenschte Richtung, moeglich sind
	 * 	Robot.RIGHT_HAND und Robot.LEFT_HAND
	 * @return naechster Eckpunkt 
	 */

    //============================================================
    public Point2 getNextVertex(Point2 hitpoint,Polygon2 obstacle,boolean dir)
    //============================================================
    {

        if(hitpoint == null || obstacle == null) return null;

        Segment2[] edges = obstacle.edges();

        int det = _polygonOrientation(obstacle);
        if(dir == Robot.LEFT_HAND) det *= -1;


        int j;
        for ( j = 0; j < edges.length; j++ ) {
            if(edges[j].liesOn(hitpoint)) break;
        } // for



        if( hitpoint.distance(edges[j].target()) < _EPSILON ) {
            if ( det == -1) return edges[j].source();
            else {
                j++;
                if(j >= edges.length) j = 0;
                return edges[j].target();
            }

        } // if

        if( hitpoint.distance(edges[j].source()) < _EPSILON ) 
        {
            if ( det == 1) return edges[j].target();
            else 
            {
                j--;
                if(j < 0) j = edges.length-1;
                return edges[j].source();
            } // else
        } // if 


        if ( det == -1 )return edges[j].source();
        else return edges[j].target();
        

    }// getNextVertex





    //************************************************************
    // private methods
    //************************************************************

	/**
	 * Methode sortiert alle Polygoneckpunkte der Szene nach Winkeln
	 * bezueglich der Linie "Roboterposition -> reference_point" und speichert
	 * sortierte Liste in dem globalen Array _sorted_vertices 
	 * @param reference_point die zu verwendende Referenz zur Winkelmessung
	 * @param dir die gewuenschte Richtung der Sortierung, moeglich sind:
	 * 			Robot.CLOCKWISE und Robot.COUNTERCLOCKWISE 
	 */
    //============================================================
    private void _sceneSort(Point2 reference_point,boolean dir) 
    //============================================================
	{
        Point2List unsorted_vertices = new Point2List();

        Point2 [] vertices = _allVertices();

        for ( int i = 0; i < vertices.length; i++ ) {
            unsorted_vertices.addPoint(vertices[i]);
        } // for

        _sorted_vertices = _sortPoints(reference_point,unsorted_vertices,dir);


    }
    // _sceneSort


	/**
	 * Methode sortiert uebergebene Punktliste nach Winkeln
	 * bezueglich der Linie "Roboterposition -> reference_point" und gibt 
	 * sortierte Liste zurueck 
	 * @param reference_point die zu verwendende Referenz zur Winkelmessung
	 * @param pointlist die zu sortierende Liste
	 * @param dir die gewuenschte Richtung der Sortierung, moeglich sind:
	 * 			Robot.CLOCKWISE und Robot.COUNTERCLOCKWISE 
	 */
    //============================================================
    private Point2List _sortPoints(Point2 reference_point, Point2List pointlist, boolean dir ) 
     //============================================================
	{
        SimpleList points = pointlist.points().copyList();
        Point2List result = new Point2List();
        Point2 position = _robot.getCoors();

        //System.out.println("Position: "+position.toString());
        //System.out.println("Referenzpkt: "+reference_point.toString());

        if(position.equals(reference_point))
        {
            //System.out.println("sortPoints mit entartetem Referenzpkt aufgerufen!");
            result.addPoint(reference_point);
            return result;
        } // if


        PointComparitor pc = new PointComparitor();
        pc.setAnglePoints(position,reference_point);
        pc.setOrder(PointComparitor.ANGULAR);


        if ( dir == Robot.CLOCKWISE ) points.sort(pc, Sorter.DESCENDING);
        else points.sort(pc, Sorter.ASCENDING);


        Ray2 reference = new Ray2(position,reference_point);
	

        SimpleList collinear_points = new SimpleList();

        // Zunaechst alle Vertices, die genau in Startrichtung der Sortierung
        // liegen finden und an den Anfang der sortierten Liste stellen
        Enumeration enm = points.values();

        while (enm.hasMoreElements()) 
        {

            Point2 current_point = (Point2) enm.nextElement();
            if ( reference.liesOn(current_point) )
            {
                collinear_points.add(current_point);
                points.remove(current_point);
                //System.out.println("Pkt. mit Winkel 0 zum Referenzpkt:");
                //System.out.println(current_point.toString());
            } // if

        }// while

        
        
        collinear_points.addAll(points);
        points = (SimpleList) collinear_points.clone();


        // Nachsortieren bezueglich Abstand zum Robot
        pc.setReferencePoint(position);
        pc.setOrder(PointComparitor.DISTANCE_ORDER);

        SimpleList result_points = new SimpleList();
        collinear_points = new SimpleList();

        enm = points.values();

        while (enm.hasMoreElements()) 
        {

            Point2 current_point = (Point2) enm.nextElement();

            if ( reference.liesOn(current_point) )
            {
                collinear_points.add(current_point);

            } // if
            else
            {
                collinear_points.sort(pc,Sorter.ASCENDING);
                result_points.addAll(collinear_points);

                collinear_points = new SimpleList();
                collinear_points.add(current_point);
                reference = new Ray2(position,current_point);
            } // else
        } // while

        collinear_points.sort(pc,Sorter.ASCENDING);
        result_points.addAll(collinear_points);


        result.appendCopy(result_points);
        return result;

    }


	/**
	 * Methode stellt eine Liste aller Polygonecken inklusive der des 
	 * Bounding-Polygons der Szene zur Verfuegung
	 * @return Array mit allen Vertices der Szene
	 */
    //============================================================
    private Point2[] _allVertices() 
    //============================================================
    {
        Point2[] vertices = null;

        // Knoten der inneren Polygone
        Point2[] interior_vertices = _robot.getScene().vertices();

        // Knoten des bounding Polygons, falls vorhanden
        Polygon2 bounding_poly = _robot.getScene().getBoundingPolygon();
       
        if (bounding_poly != null) 
        {
            SimpleList bounding_vertices = bounding_poly.points();
            int no_of_vertices = interior_vertices.length + bounding_vertices.length();
            vertices = new Point2[no_of_vertices];

            for (int i = 0; i < no_of_vertices; i ++) {
                if (i < interior_vertices.length) vertices[i] = interior_vertices[i];
                else vertices[i] = (Point2) bounding_vertices.getValueAt(i-interior_vertices.length);
            } // for
        } // if
        else vertices = interior_vertices;

        return vertices;
    } // _allVertices



    //============================================================
    private int _polygonOrientation(Polygon2 polygon)
    //============================================================
    {
        Polygon2Scene scene = _robot.getScene();
        int polygon_ori = (polygon.getOrientation() ==  Polygon2.ORIENTATION_RIGHT) ?
                          1 : -1;
        Polygon2 bounding = scene.getBoundingPolygon();

        // Ist aktuelles Polygon Bounding-Polygon?
        if ((bounding != null) && (bounding.distance(polygon.firstPoint()) < _EPSILON))
            // Ja => Orientierung ist anders zu interpretieren
        {
            //System.out.println("Bounding-Polygon");
            polygon_ori *= -1;
        } // if
        return polygon_ori;
    } //_poylgonOrientation




	/**
	 * Methode testet, ob die uebergebene Polygonkante auf der dem Roboter zugewandten
	 * Seite ihres Polygons liegt.
	 * @param edge zu testende Polygonkante
	 * @return true falls Kante auf dem Roboter zugewandter Polygonseite, false sonst
	 */

    //============================================================
    private boolean _viewedFromOutside(Segment2 edge) 
    //============================================================
	{
        if (edge == null) return false;
        Point2 position = _robot.getCoors();
        Polygon2Scene scene =_robot.getScene();
        Segment2 inverse = new Segment2(edge.target(),edge.source());


        Polygon2 containing_edge = obstacleAt(edge.source()).obstacle;



        // Kante moeglicherweise andersherum als im Polygon gespeichert

        Segment2[] edges = containing_edge.edges();
        for ( int i = 0; i < edges.length; i++ ) {
            if ( edges[i].equals(edge))	break;
            if (edges[i].equals(inverse)) {
                edge = inverse;
                break;
            } // if
        } // for


        int polygon_ori = _polygonOrientation(containing_edge);

        int edge_ori = edge.orientation(position);

        if ( polygon_ori == 1 &&  edge_ori == Point2.ORIENTATION_LEFT
                || polygon_ori == -1 && edge_ori == Point2.ORIENTATION_RIGHT
                || edge_ori == Point2.ORIENTATION_COLLINEAR)
            return true;

        return false;
    } // viewedFromOutside


	
	 
	 /**
	  * Methode testet, ob die Sicht in Richtung Ziel schon durch ein vom Roboter
	  * beruehrtes Hindernis versperrt wird.
	  * @param target Ziel, das Blickrichtung angibt
	  * @return	true, falls Sicht durch beruehrtes Hindernis versperrt, sonst false
	  */
    //============================================================
    private boolean _blockedByTouchedObstacle(Point2 target) 
    //============================================================
   { 
        Point2 position = _robot.getCoors();
        ObstacleResult touched_or = obstacleAt(position);
        if (touched_or == null) return false;
        else
        {
            Polygon2 touched_obstacle = touched_or.obstacle;

            Point2 vertex_right_hand = getNextVertex(touched_or.hitpoint,touched_obstacle,Robot.RIGHT_HAND);
            Point2 vertex_left_hand = getNextVertex(touched_or.hitpoint,touched_obstacle,Robot.LEFT_HAND);


            Ray2 right_hand = new Ray2 (touched_or.hitpoint,vertex_right_hand);
            Ray2 left_hand = new Ray2 (touched_or.hitpoint,vertex_left_hand);


            boolean right_hand_blocks = (right_hand.orientation(target) == Point2.ORIENTATION_RIGHT);
            boolean left_hand_blocks = (left_hand.orientation(target) == Point2.ORIENTATION_LEFT);

            double inner_angle = touched_or.hitpoint.angle(vertex_right_hand,vertex_left_hand);

            if (right_hand_blocks && left_hand_blocks)
                // Fall klar, egal ob Robot an spitzer Ecke steht oder nicht:
                // target unsichtbar
            {
                return true;
            } // if

            if ((right_hand_blocks || left_hand_blocks ) && ( inner_angle < Math.PI - 0.001))
            {
                return true;
            } // if

            return false;

        } // else


    } // _blockedByTouchedObstacle

} // OpticalSensor




