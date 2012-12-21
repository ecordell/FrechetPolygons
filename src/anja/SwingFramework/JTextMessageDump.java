/*
 * Created on Mar 2, 2005
 *
 * JTextMessageDump.java
 * 
 */
package anja.SwingFramework;

import java.io.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

/**
 * A simple dump window that displays various text messages that can
 * be issued from anywhere in your application.
 * 
 * <p>Typical usage example: <br>Somewhere in an editor: <br><br>
 * m_Hub.getTextDump.println("Message text!"); <br><br>
 * 
 * <p>This window is displayed as a completely separate JFrame
 * instance so it will be easier to observe the messages. <br>The
 * print() and println() are respective counterparts of
 * System.out.print() and System.out.println(). <br><b>[However, keep
 * in mind that these methods only take arguments of type
 * <code>String</code>- no numbers etc!] </b>
 * 
 * <p>There's also an option to capture the standard System.out and
 * System.err text streams, so that all diagnostic messages, warnings
 * and/or exceptions will be relayed to this window, instead of the
 * console.
 * 
 * @author Ibragim Kouliev
 * <br>Note: I will add an option of saving the text output to a 
 * dump file when I have time... 
 */
public class JTextMessageDump
{	
    /**
     * Internal class used to redirect 
     * standard output and error stream to the 
     * text message dump.
     */ 
    private class MessageStream extends ByteArrayOutputStream
    {
    	public MessageStream()
    	{
    	    super();
    	}
    	
    	public MessageStream(int initialSize)
    	{
    	    super(initialSize);
    	}
    	
    	public void flush()
    	{
            //super.flush(); // see java.io.OutputStream#flush()
            
            /* copy buffer context to message dump
             * This uses the default character encoding on 
             * target platform for now!
             */
            JTextMessageDump.this.print(toString());
            
            // clear buffer
            reset();
    	}			
    }

    //*************************************************************************
    // 			      Private instance variables
    //*************************************************************************
	
    protected JCheckBox         m_uiRedirBox;
    
    private JFrame 	        m_Window;
    private JTextArea           m_Lister;
    private JScrollPane         m_scrollPane;	
    private int 		m_iMaxLines;
    
    // used for stdout redirection
    private PrintStream         m_stdOutStream;
    private PrintStream         m_stdErrStream;
    
    private PrintStream         m_printStream;
    private MessageStream       m_messageBuffer;
		
    //*************************************************************************
    // 				    Constructors
    //*************************************************************************
	
    /**
     * Constructs a new message dump window and makes it available
     * to other components accessible through JSystemHub.
     * 
     */
    public JTextMessageDump(JSystemHub hub)
    {
    	init();
    	hub.setTextDump(this);
    }
    
    //*************************************************************************
    // 			       Public instance methods
    //*************************************************************************
	
    public void show()
    {
    	m_Window.setVisible(true);		
    }
    
    //*************************************************************************
	
    public void hide()
    {
    	m_Window.setVisible(false);
    }
    
    //*************************************************************************
	
    /**
     * Sets the maximum number of text lines the dump should scroll before
     * the text buffer is cleared.
     * @param lines
     */
    public void setMaxNumberOfLines(int lines)
    {
    	m_iMaxLines = lines;
    	if(m_Lister.getLineCount() > lines)
    	{
    	    // dump previous text buffer
    	    m_Lister.setText("");
    	}
    }
    
    //*************************************************************************
	
    /**
     * Makes a line break and apppends the supplied text
     * to the contents of the window.
     * 
     * @param text string to be printed
     */
    public void println(String text)
    {
    	if(m_Lister.getLineCount() > m_iMaxLines)
    	{
    	    // remove all previous text
    	    m_Lister.setText("");
    	}
    	
    	m_Lister.append("\n" + text);
    }
    
    //*************************************************************************
	
    /**
     * Appends the supplied string to the contents
     * of the window. Does NOT issue a line break.
     * 
     * @param text
     */
    public void print(String text)
    {
    	if(m_Lister.getLineCount() > m_iMaxLines)
    	{
    	    // remove all previous text
    	    m_Lister.setText("");
    	}
    	
    	m_Lister.append(" "+text);
    }
    
    //*************************************************************************
	
    /**
     * Clears the contents of the message window.
     *
     */
    public void clear()
    {
    	m_Lister.setText("");
    	m_messageBuffer.reset();
    }
    
    //*************************************************************************

    public void enableStreamCapturing(boolean on)
    {
    	try
    	{			
            if(on)
            {						
            	m_stdErrStream = System.err;
            	m_stdOutStream = System.out;
            	
            	/* redirect standard output streams to
            	 * the message dump.
            	 */
            					
            	System.setOut(m_printStream);
            	System.setErr(m_printStream);					
            }
            else
            {
            	/* Redirect output back to standard
            	 * output streams 
            	 */
            	System.setOut(m_stdOutStream);
            	System.setErr(m_stdErrStream);					
            }	
            
            m_uiRedirBox.setSelected(on);
    	}
    	catch(SecurityException ex)
    	{
    		System.out.println(
    		"Cannot redirect stdout and stderr!");
    	}				
    }
	
    //*************************************************************************
    // 			       Private instance methods
    //*************************************************************************

    private void init()
    {
    	// Create a print stream for stdout redirection 		
    	m_messageBuffer = new MessageStream();
    	m_printStream = new PrintStream(m_messageBuffer, true);
    	
    	// save references to the standard streams
    	m_stdOutStream = System.out;
    	m_stdErrStream = System.err;
    					
    	// setup main window etc.		
    	m_Window = new JFrame();
    	m_Window.setDefaultCloseOperation(
    			 WindowConstants.DO_NOTHING_ON_CLOSE);
    	
    	m_uiRedirBox = new JCheckBox("Capture System.out and System.err");
    	m_uiRedirBox.setToolTipText("<html>Redirect standard stream output" +
    				    "<br>to the message window</html>");
    	
    	m_uiRedirBox.doClick();
    	enableStreamCapturing(true);
    			
    	// hook up the stream redirector
    	m_uiRedirBox.addActionListener(new ActionListener()
    	{
            public void actionPerformed(ActionEvent event)
            {
            	enableStreamCapturing(m_uiRedirBox.isSelected());
            }	
    	});
    	
    	m_Lister = new JTextArea();
    	m_Lister.setEditable(false);
    							
    	m_Lister.setBorder(BorderFactory.
    			    createBevelBorder(EtchedBorder.LOWERED));
    	
    	m_Lister.setAutoscrolls(true);
    	
    	// scroll pane
    	m_scrollPane = new JScrollPane(m_Lister);						
    	m_scrollPane.setMinimumSize(new Dimension(200,200));
    		
    	m_scrollPane.setBorder(
    		BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Text message dump"),
                BorderFactory.createEmptyBorder(5,5,5,5)),
                m_scrollPane.getBorder()));
    
    	m_Window.getContentPane().setLayout(new BorderLayout());
    	m_Window.getContentPane().add(m_scrollPane, BorderLayout.CENTER);
    	m_Window.getContentPane().add(m_uiRedirBox, BorderLayout.NORTH);
    	
    	// show the dump window
    	m_Window.pack();
    	m_Window.setVisible(true);		
    }
}


