/*
 * Anmerkung: Zu diesem Projekt gehoeren die Dateien:
 * SeidelTriangulationEditorExtension.java, SeidelTriangulationProg.java und
 * SeidelTriangulationApplet.java
 */

package SeidelTriag;


import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import java.security.AccessControlException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JCheckBox;

import anja.swinggui.BufferedCanvas;
import anja.swinggui.DisplayPanel;


/**
 * Die Klasse SeidelTriangulationProg ist abgeleitet von JFrame. Er enthaelt
 * einen Editor zum Eingeben von einem Polygon und zeichnet die Triangulation
 * des Polygons ein. Die Klasse unterstützt außerdem die PSLG-Triangulierung und
 * bietet diese als Option an.
 * 
 * @author Marina Bachran, Andreas Lenerz
 * 
 * @see SeidelTriangulationEditorExtension SeidelTriangulationProg
 *      SeidelTriangulationApplet
 * @see anja.geom.triangulation.PSLGTriangulation
 */
public class SeidelTriangulationProg
		extends JFrame
		implements WindowListener, ItemListener
{

	// Position und Abmessungen des Hauptfensters beim Start als Applikation:
	private static final int					_DEFAULT_POS_X	= 10;
	private static final int					_DEFAULT_POS_Y	= 10;
	private static final int					_DEFAULT_WIDTH	= 640;
	private static final int					_DEFAULT_HEIGHT	= 480;

	// Editor mit Extension und DisplayPanel fuer das Polygon:
	private SeidelTriangulationEditorExtension	_editor;
	private DisplayPanel						_display;

	private JCheckBox							showTrapezoidation;
	private JCheckBox							showSeidelDiags;
	private JCheckBox							showTriangulationSeidel;
	private JCheckBox							showTriangulationPSLG;


	/**
	 * Der Konstruktor erzeugt einen PolygonEditor. Es wird ein DisplayPanel zum
	 * Zeichnen des Editorinhalts verwendet.
	 */
	public SeidelTriangulationProg()
	{
		super("Seidel-Triangulierung / PSLG Triangulierung");
		_display = new DisplayPanel();
		_editor = new SeidelTriangulationEditorExtension();
		_display.setScene(_editor);

		// registriere Maus-Listener des Editors beim DisplayPanel:
		BufferedCanvas b_canvas = _display.getCanvas();
		b_canvas.addMouseListener(_editor);
		b_canvas.addMouseMotionListener(_editor);

		setLocation(_DEFAULT_POS_X, _DEFAULT_POS_Y);
		setSize(_DEFAULT_WIDTH, _DEFAULT_HEIGHT);
		addWindowListener(this);

		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new GridBagLayout());
		
		showTrapezoidation = new JCheckBox("Show Trapezoids");
		showSeidelDiags = new JCheckBox("Show Diagonals (Seidel)");
		showTriangulationSeidel = new JCheckBox("Show Trianguliation (Seidel)");
		showTriangulationPSLG = new JCheckBox("Show Trianguliation (PSLG)");
		
		showTrapezoidation.addItemListener(this);
		showSeidelDiags.addItemListener(this);
		showTriangulationSeidel.addItemListener(this);
		showTriangulationPSLG.addItemListener(this);
		
		GridBagConstraints constraint_trap = new GridBagConstraints();
		constraint_trap.gridx = 0;
		constraint_trap.gridy = 0;
		constraint_trap.anchor = GridBagConstraints.LINE_START;
		
		GridBagConstraints constraint_diag = new GridBagConstraints();
		constraint_diag.gridx = 0;
		constraint_diag.gridy = 1;
		constraint_diag.anchor = GridBagConstraints.LINE_START;
		
		GridBagConstraints constraint_seidel = new GridBagConstraints();
		constraint_seidel.gridx = 0;
		constraint_seidel.gridy = 2;
		constraint_seidel.anchor = GridBagConstraints.LINE_START;
		
		GridBagConstraints constraint_pslg = new GridBagConstraints();
		constraint_pslg.gridx = 0;
		constraint_pslg.gridy = 3;
		constraint_pslg.anchor = GridBagConstraints.LINE_START;
		
		lowerPanel.add(showTrapezoidation, constraint_trap);
		lowerPanel.add(showSeidelDiags, constraint_diag);
		lowerPanel.add(showTriangulationSeidel, constraint_seidel);
		lowerPanel.add(showTriangulationPSLG, constraint_pslg);

		// initialisiere Zeichenflaeche:
		getContentPane().add(_display, BorderLayout.CENTER);
		getContentPane().add(lowerPanel, BorderLayout.SOUTH);
	}


	/**
	 * Faengt die Ereignisse für die Checkboxen zum Anzeigen der Trapezoide, der
	 * Seidel-Diagonalen und der Triangulierung ab und behandelt sie.
	 * 
	 * @param ItemEvent
	 *            e Ereignis, das behandelt wird.
	 */
	public void itemStateChanged(ItemEvent e)
	{
		Object source = e.getItemSelectable();
		if (source == showTrapezoidation)
		{
			_editor.showTrapezoidation = showTrapezoidation.isSelected();
			_editor.repaint();
		}
		else if (source == showSeidelDiags)
		{
			_editor.showSeidelDiags = showSeidelDiags.isSelected();
			_editor.repaint();
		}
		else if (source == showTriangulationSeidel)
		{
			_editor.showTriangulationSeidel = showTriangulationSeidel.isSelected();
			_editor.repaint();
		}
		else if (source == showTriangulationPSLG)
		{
			_editor.showTriangulationPSLG = showTriangulationPSLG.isSelected();
			_editor.repaint();
		}
	}


	/** Hauptprogramm fuer die Ausfuehrung als Applikation statt als Applet. */
	public static void main(String args[])
	{
		SeidelTriangulationProg seidel = new SeidelTriangulationProg();
		seidel.show();
	}


	/**
	 * Zum Schließen des Programms.
	 * 
	 * @param WindowEvent
	 *            e
	 */
	public void windowClosing(WindowEvent e)
	{
		dispose();
		try
		{
			System.exit(0);
		}
		catch (AccessControlException ace)
		{}
	}


	// folgende Methoden werden nicht verwendet:
	public void windowActivated(WindowEvent e)
	{}


	public void windowClosed(WindowEvent e)
	{}


	public void windowDeactivated(WindowEvent e)
	{}


	public void windowDeiconified(WindowEvent e)
	{}


	public void windowIconified(WindowEvent e)
	{}


	public void windowOpened(WindowEvent e)
	{}
}
