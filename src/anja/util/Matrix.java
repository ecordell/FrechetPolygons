package anja.util;

import anja.geom.Point2;

/**
*  Matrix
*
*  @version 0.1 04 Feb 1997
*  @author     Norbert Selle
*/
public class Matrix
{

   // ************************************************************************
   // Constants
   // ************************************************************************


   // ************************************************************************
   // Variables
   // ************************************************************************


   float _matrix[][];   // die Matrix
   int      _rows;      // die Anzahl der Zeilen
   int      _columns;   // die Anzahl der Spalten


   // ************************************************************************
   // Constructors
   // ************************************************************************


   /**
   * Erzeugt eine Matrix mit der eingegebenen Zeilen- und Spaltenanzahl
   * und initialisiert alle Elemente mit 0.
   * Zeilen- und Spaltenanzahl muessen groesser gleich 1 sein.
   * Die Zeilen werden mit 0 bis Zeilenanzahl minus 1 indiziert,
   * die Spalten entsprechend.
   */
   public Matrix(
      int   input_rows,
      int   input_columns
   )
   {
      int   row_index;
      int   column_index;

      _rows = input_rows;
      _columns = input_columns;

      _matrix = new float[ _rows ][ _columns ];

      for ( row_index = 0; row_index < _rows; row_index++ )
      {
         for ( column_index = 0; column_index < _columns; column_index++ )
    {
       _matrix[ row_index ][ column_index ] = 0;
    } // for
      } // for

   } // Matrix


   // ************************************************************************
   // Class methods
   // ************************************************************************


   /**
   * Berechnung der Determinante einer 2x2 Matrix.
   */

   public static float det(
      float input_0_0,
      float input_0_1,
      float input_1_0,
      float input_1_1
   )
   {
      return(   ( input_0_0 * input_1_1 )
              - ( input_0_1 * input_1_0 ) );

   } // det


   // ********************************


   public static double det(
      double   input_0_0,
      double   input_0_1,
      double   input_1_0,
      double   input_1_1
   )
   {
      return(   ( input_0_0 * input_1_1 )
              - ( input_0_1 * input_1_0 ) );

   } // det


   //*********************************

   public static double det(
      Point2 input0,
      Point2 input1
   )
   {
      return det(input0.x, input0.y, input1.x, input1.y);

   } // det

   // ************************************************************************


   /**
   * Berechnung der Determinante einer 3x3 Matrix.
   */
   
   public static float det(
      double input_0_0, double input_0_1, double input_0_2,
	  double input_1_0, double input_1_1, double input_1_2,
	  double input_2_0, double input_2_1, double input_2_2
   )
   {
      return (float)(
      ( input_0_0 * input_1_1 * input_2_2 )
    + ( input_0_1 * input_1_2 * input_2_0 )
    + ( input_0_2 * input_1_0 * input_2_1 )
    - ( input_0_2 * input_1_1 * input_2_0 )
    - ( input_0_0 * input_1_2 * input_2_1 )
    - ( input_0_1 * input_1_0 * input_2_2 )
      );

   } // det


   // ************************************************************************
   // Public methods
   // ************************************************************************


   /**
   * Berechnung der Determinante.
   * Vorbedingung: die Matrix ist quadratisch und zwei- oder dreireihig.
   */
   public float det()
   {
      float output_det  = 0;

      if ( _rows != _columns )
      {
         System.out.println( "Matrix.det() nicht quadratisch" );
      }
      else if ( _rows == 2 )
      {
    // zweireihige Determinante

         output_det =   ( _matrix[ 0 ][ 0 ] * _matrix[ 1 ][ 1 ] )
                 - ( _matrix[ 1 ][ 0 ] * _matrix[ 0 ][ 1 ] );
      }
      else if ( _rows == 3 )
      {
    // dreireihige Determinante

    output_det =
         ( _matrix[ 0 ][ 0 ] * _matrix[ 1 ][ 1 ] * _matrix[ 2 ][ 2 ] )
       + ( _matrix[ 0 ][ 1 ] * _matrix[ 1 ][ 2 ] * _matrix[ 2 ][ 0 ] )
       + ( _matrix[ 0 ][ 2 ] * _matrix[ 1 ][ 0 ] * _matrix[ 2 ][ 1 ] )
       - ( _matrix[ 0 ][ 2 ] * _matrix[ 1 ][ 1 ] * _matrix[ 2 ][ 0 ] )
       - ( _matrix[ 0 ][ 0 ] * _matrix[ 1 ][ 2 ] * _matrix[ 2 ][ 1 ] )
       - ( _matrix[ 0 ][ 1 ] * _matrix[ 1 ][ 0 ] * _matrix[ 2 ][ 2 ] );

      }
      else
      {
         System.out.println( "Matrix.det() nicht zwei- oder dreireihig" );
      } // if

      return( output_det );

   } // det


   // ************************************************************************


   /**
   * Setzt das Matrix-Element an der eingegebenen Zeile und Spalte auf
   * den eingegebenen Wert.
   * Vorbedingung: Zeile und Spalte sind groesser gleich 0 und kleiner
   * als die Zeilen- bzw. Spaltenanzahl.
   */
   public void set(
      int   input_row,
      int   input_column,
      float input_value
   )
   {
      _matrix[ input_row ][ input_column ] = input_value;

   } // set


   // ************************************************************************
   // Private methods
   // ************************************************************************

} // class Matrix

