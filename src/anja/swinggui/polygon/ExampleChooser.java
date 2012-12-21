package anja.swinggui.polygon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.net.URL;
import java.io.*;
import anja.geom.*;
import anja.util.*;

/**
 * This dialog shows a list of example files which the user can choose from to load in the Polygon2SceneEditor.
 * The dialog show a list of buttons, each should contain a short name for the example and a small preview image.
 * The images can be generated automatically if the example files contain only a Polygon2Scene, saved via Polygon2Scene.save().
 * (Every file saved by the Polygon2SceneEditor is such a file if the applet does not overwrite the save method.)
 * If the example files contain more than just a Polygon2Scene, an array containing the previews as Icons has to be supplied.
 * @author Joerg Wegener *
 */

public class ExampleChooser extends JDialog
                        implements ActionListener {
    private String value;
    
    JButton[] _buttons;
    String[] _fileNames;    //the list of file names the user can choose from
    String[] _descriptions; //descriptions shown on the buttons, may be null
    
    JPanel _buttonPanel;          //the panel containing the buttons
    JScrollPane _buttonScroller;  //the scroll pane for the buttons, dimension is set in setExamples()
    
    Component _locationComp;
    
    
    /**
     * Shows the dialog and returns the name of the chosen file.
     * @return The name of the file to load or null if cancel is pressed
     */
    public String showDialog() {
        value = null;
        this.setVisible(true);
        return value;
    }
    
    
    /**
     * Creates the buttons to choose the example file from.
     * @param fileNames A list with the files to choose from, loaded via Class.getResource(), look there on how filenames are treated
     * @param descriptions A list of descriptions which are shown on the buttons instead of the file names. (The button for fileNames[i] shows descriptions[i]). May be null, the buttons then show the file names. 
     * @param icons A list with icons to show on the button. An Icon should be 128x128 in size. icons[i] is displayed for fileNames[i]. May be null, the icons are then generated from the example file. Automatic generation only works if the example files contain a polygon scene only(e.g. saved via Polygon2Scene.save()).
     */
    public void setExamples(String[] fileNames, String[] descriptions, Icon[] icons) {
        _fileNames = fileNames;
        _descriptions = descriptions;
        _buttons = new JButton[fileNames.length];
        _buttonPanel.removeAll();
        for (int i=0;i<_fileNames.length;i++) {
            String description;
            if (descriptions == null || i >= descriptions.length) description = _fileNames[i];
            else description = descriptions[i];
            if (icons == null || icons.length >= i) {
                URL url = this.getClass().getResource(fileNames[i]);
                if (url != null) {
                    //Load the polygon scene file and draw it onto an icon, size 128x128
                    InputStream is = this.getClass().getResourceAsStream(fileNames[i]);
                    DataInputStream di = new DataInputStream(is);
                    Polygon2Scene scene = new Polygon2Scene(di);
                    Rectangle2 bb = scene.getBoundingBox();
                    Point2 t = new Point2(-bb.getMinX()+2, -bb.getMinY()+2);
                    bb.translate(t);
                    scene.translate(t);
                    Polygon2 boundingPolygon = scene.getBoundingPolygon();
                    if (boundingPolygon != null) boundingPolygon.translate(t);
                    
                    //Szene zeichnen in Größe 128x128, Antialiasing an und Linienstärke 4                       
                    BufferedImage bi = new BufferedImage(128,128, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = bi.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.fillRect(0, 0, 128, 128);
                    g2d.scale(128/(bb.getWidth()+4),128/(bb.getHeight()+4));                       
                    GraphicsContext gc = new GraphicsContext();
                    gc.setLineWidth(4);
                    scene.draw(g2d, gc);
                    
                    // The Polygon2Scene editor somehow displays the scene flipped vertically,
                    // so the image has to be flipped as well
                    AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
                    tx.translate(0, -bi.getHeight(null));
                    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                    bi = op.filter(bi, null);
                    
                    _buttons[i] = new JButton(description, new ImageIcon(bi.getSubimage(0,0,128,128)));
                }
            }
            else {
                _buttons[i] = new JButton(description, icons[i]);             
            }
            if (_buttons[i] != null) {
                _buttons[i].setVerticalTextPosition(AbstractButton.BOTTOM);
                _buttons[i].setHorizontalTextPosition(AbstractButton.CENTER);
                _buttonPanel.add(_buttons[i]);
                _buttons[i].addActionListener(this);
            }
        }
        if (_fileNames.length<4) {
            _buttonScroller.setPreferredSize(new Dimension(510,161));
        }
        else
        _buttonScroller.setPreferredSize(new Dimension(510, 319));
        pack();
        setLocationRelativeTo(_locationComp);
    }

    /**
     * Constructs most parts of the dialog. Needs a frame to belong to and a component for positioning.
     * @param frame The frame this dialog belongs to. The frame will be inaccessible while the dialog is shown.
     * @param locationComp The dialog is displayed on top of this component.
     */
    public ExampleChooser(Frame frame,
                       Component locationComp) {
        
        super(frame, "Choose an example scene", true);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);

        _buttonPanel = new JPanel();
        _buttonPanel.setLayout(new GridLayout(0,3));
        _buttonScroller = new JScrollPane(_buttonPanel);
        _buttonScroller.setPreferredSize(new Dimension(510, 280));
        _buttonScroller.setAlignmentX(LEFT_ALIGNMENT);
        _buttonScroller.getVerticalScrollBar().setUnitIncrement(16);

        JPanel cancelButtonPane = new JPanel();
        cancelButtonPane.setLayout(new BoxLayout(cancelButtonPane, BoxLayout.LINE_AXIS));
        cancelButtonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        cancelButtonPane.add(Box.createHorizontalGlue());
        cancelButtonPane.add(cancelButton);
        cancelButtonPane.add(Box.createRigidArea(new Dimension(10, 0)));

        Container contentPane = getContentPane();
        contentPane.add(_buttonScroller, BorderLayout.CENTER);
        contentPane.add(cancelButtonPane, BorderLayout.PAGE_END);
        
        setLocationRelativeTo(locationComp);
        _locationComp = locationComp;
    }


    public void actionPerformed(ActionEvent e) {
        for (int i=0; i<_buttons.length; i++) {
            if (e.getSource() == _buttons[i]) 
                value = _fileNames[i];
        }
        this.setVisible(false);
    }
}
