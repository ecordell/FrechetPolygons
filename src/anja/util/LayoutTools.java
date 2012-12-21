package anja.util;

/**
/*
/* LayoutTools
/*
/* Hilfsfunktionen fuer GridBagLayouts etc.
/*
/* PK 1996-09-03
/*
**/

import java.awt.*;

public class LayoutTools {
	/**
	* Fuegt die Komponente mit den passenden Constraints in den
	* GridBag - Container ein.
	* @param container Container mit GridBagLayout
	* @param component die neue Komponente, noch nicht mit add() eingefuegt
	* @param grid_x Spaltenposition
	* @param grid_y Zeilenposition
	* @param grid_width Breite der Komponente in Spalten
	* @param grid_height Hoehe der Komponente in Zeilen
	* @param fill Fuellung: GridBagConstraints.NONE, BOTH, HORIZONTAL, VERTICAL
	* @param anchor Position innerhalb der Zelle: CENTER oder Kompassrichtungen
	* @param weight_x Gewicht der Zelle bei der aufteilung von neuem Platz in der Zeile
	* @param weight_y Gewicht der Zelle bei der aufteilung von neuem Platz in der Spalte
	* @param top Abstand nach oben
	* @param left Abstand nach links
	* @param bottom Abstand nach unten
	* @param right Abstand nach rechts
	*/
	public static void gridBagConstrain(java.awt.Container container, 
				Component component, int grid_x, int grid_y,
				int grid_width, int grid_height, int fill, int anchor,
				double weight_x, double weight_y, int top, int left,
				int bottom, int right) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = grid_x;
		c.gridy = grid_y;
		c.gridwidth = grid_width;
		c.gridheight = grid_height;
		c.fill = fill;
		c.anchor = anchor;
		c.weightx = weight_x;
		c.weighty = weight_y;
		if(top+bottom+left+right > 0)
			c.insets = new Insets(top, left, bottom, right);

		((GridBagLayout) container.getLayout()).setConstraints(component, c);
		container.add(component);
	}
}

