package anja.util;

import java.lang.Math;

/**
* 3x3 Matrix for homogenous 2D-transformations 
*
* @version      0.1 08.09.98
* @author       Thomas Kamphans
*/

public class Matrix33 
	implements Cloneable
{
	//************************************************************
	// public variables
	//************************************************************

	public float m[][];

	//************************************************************
	// Constructors
	//************************************************************

	/**
	* create new matrix, initialize elements with 0
	*/

	//============================================================
	public Matrix33()
	//============================================================
	{
		int i, j;

		m = new float[3][3];

		for(i = 0; i < 3; i++)
			for(j = 0; j < 3; j++)
				m[i][j] = 0;

	} // Matrix33


	//************************************************************
	// Class Methods
	//************************************************************


	/**
	* Generate transformation matrix for translation
	*/

	//============================================================
	public static Matrix33 translate( float x, float y )
	//============================================================
	{
		Matrix33 a = new Matrix33();

		a.m[0][0] = 1;	a.m[0][2] = x;
		a.m[1][1] = 1;	a.m[1][2] = y;
		a.m[2][2] = 1;

		return a;

	} // translate


	/**
	* Generate transformation matrix for scaling
	*/

	//============================================================
	public static Matrix33 scale( float x, float y )
	//============================================================
	{
		Matrix33 a = new Matrix33();

		a.m[0][0] = x;
		a.m[1][1] = y;
		a.m[2][2] = 1;

		return a;

	} // scale


	/**
	* Generate transformation matrix for rotation
	*/

	//============================================================
	public static Matrix33 rotate( float angle )
	//============================================================
	{
		Matrix33 a = new Matrix33();

		float sin = (float) Math.sin( angle );
		float cos = (float) Math.cos( angle );
		a.m[0][0] = cos;	a.m[0][1] = -sin;
		a.m[1][0] = sin;	a.m[1][1] = cos;
		a.m[2][2] = 1;

		return a;

	} // rotate


	/**
	* Calculate C = A * B 
	*/

	//============================================================
	public static Matrix33 product ( Matrix33 a, Matrix33 b )
	//============================================================
	{
		int	 i, j, k;
		float	 sum;
		Matrix33 c = new Matrix33();

		for(i = 0; i < 3; i++) {
			for(j = 0; j < 3; j++) {
				sum = 0.0f;
				for(k = 0; k < 3; k++) {
					sum += a.m[i][k] * b.m[k][j];
				}
				c.m[i][j] = sum;
			}
		}

		return c;

	} // mult


	//************************************************************
	// Public Methods
	//************************************************************


	//============================================================
	public Object clone()
	//============================================================
	{
		int	 i, j;
		Matrix33 c = new Matrix33();

		for(i = 0; i < 3; i++) 
			for(j = 0; j < 3; j++) 
				c.m[i][j] = m[i][j];

		return c;

	} // clone


	//============================================================
	public String toString()
	//============================================================
	{
		int i, j;
		StringBuffer s = new StringBuffer();

		for(i = 0; i < 3; i++) {
			s.append("( ");
			for(j = 0; j < 3; j++) {
				if ( j != 0)
					s.append(", ");
				s.append( String.valueOf( m[i][j] ));
			}
			s.append(" )\n");
		}

		return s.toString();

	} // toString


	//============================================================
	public float get( int i, int j)
	//============================================================
	{
		return m[i][j];
	} // get


	//============================================================
	public void set( int i, int j, float f )
	//============================================================
	{
		m[i][j] = f;
	} // set


	/**
	* Add Matrix33
	*/

	//============================================================
	public void add ( Matrix33 a )
	//============================================================
	{
		int i,j;
		for(i = 0; i < 3; i++) 
			for(j = 0; j < 3; j++)
				m[i][j] += a.m[i][j];

	} // add


	/**
	* Calculate this = A * this
	*/

	//============================================================
	public void compose( Matrix33 a )
	//============================================================
	{
		int	 i, j, k;
		float	 sum ;
		Matrix33 b = (Matrix33) this.clone();

		for(i = 0; i < 3; i++) {
			for(j = 0; j < 3; j++) {
				sum = 0.0f;
				for(k = 0; k < 3; k++) {
					sum += a.m[i][k] * b.m[k][j];
				}
				m[i][j] = sum;
			}
		}


	} // compose
	


	/**
	* transpose matrix
	*/

	//============================================================
	public void transpose()
	//============================================================
	{
		int	 i, j;
		Matrix33 b = (Matrix33) this.clone();

		for(i = 0; i < 3; i++) {
			for(j = 0; j < 3; j++) {
				this.m[i][j] = b.m[j][i];
			}
		}


	} // transpose
	

	/**
	* return transposed matrix
	*/

	//============================================================
	public Matrix33 transposed()
	//============================================================
	{
		int	 i, j;
		Matrix33 b = new Matrix33();

		for(i = 0; i < 3; i++) {
			for(j = 0; j < 3; j++) {
				b.m[i][j] = this.m[j][i];
			}
		}

		return b;

	} // transposed
	

	/**
	* return determinant of the matrix
	*/

	//============================================================
	public float det()
	//============================================================
	{
		return
			  ( m[ 0 ][ 0 ] * m[ 1 ][ 1 ] * m[ 2 ][ 2 ] )
			+ ( m[ 0 ][ 1 ] * m[ 1 ][ 2 ] * m[ 2 ][ 0 ] )
			+ ( m[ 0 ][ 2 ] * m[ 1 ][ 0 ] * m[ 2 ][ 1 ] )
			- ( m[ 0 ][ 2 ] * m[ 1 ][ 1 ] * m[ 2 ][ 0 ] )
			- ( m[ 0 ][ 0 ] * m[ 1 ][ 2 ] * m[ 2 ][ 1 ] )
			- ( m[ 0 ][ 1 ] * m[ 1 ][ 0 ] * m[ 2 ][ 2 ] );
	} // det


} // Matrix33
