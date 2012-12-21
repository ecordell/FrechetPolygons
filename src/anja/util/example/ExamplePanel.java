/**
 * 
 */
package anja.util.example;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
 * This panel connects the managers and listeners to a graphical instance.
 * 
 * <br>
 * 
 * The panel is able to receive file dialogs, send events, change a debug mode
 * to output errors to the console or manage the examples with an
 * <code>ExampleManager</code>.
 * 
 * <br>
 * 
 * To use the panel, create an instance of it in the application and add it to
 * the <code>Panel</code> of the application. Then the listeners can be
 * registered and the examples added which will automatically occur in the
 * combobox of the panel.
 * 
 * <br>
 * 
 * The debug mode enables the option, to write exceptions to the console. This
 * is useful if an object is not serializable and has to be changed. Most of the
 * objects in anja are serializable but quite not all of them. Just add the
 * implements command to the class if needed. The java code mode displays a
 * button in the panel, which shows a byte array of the current chosen example
 * in java code when pressed. It is now possible to use this byte array together
 * with the <code>loadExample(byte[])</code> method to load an example and
 * easily save the examples in a class and load at startup of the application.
 * By default jc mode is disabled but can be enabled via <code>set...()</code>.
 * 
 * @author Andreas Lenerz, 1.10.2011
 * @version 1.0
 * 
 * @see anja.util.example.Example
 * @see anja.util.example.ExampleListener
 * @see anja.util.example.ExampleChangeEvent
 * @see anja.util.example.ExampleSaveEvent
 * @see anja.util.example.ExampleManager
 */
public class ExamplePanel<E extends Example>
		extends JPanel
		implements ActionListener
{

	//************************************
	// VARIABLES
	//************************************

	/**
	 * Manages all examples the panel offers
	 */
	private ExampleManager<E>				_em				= null;

	/**
	 * Manages all listener
	 */
	private ArrayList<ExampleListener<E>>	_listener		= null;

	/**
	 * Chosen Example
	 */
	private E								_example		= null;

	/**
	 * FileChooser object that is used to load and save examples
	 */
	private JFileChooser					_filechooser	= null;

	/**
	 * The buttons the panel uses
	 */
	private JButton							_loadButton		= null,
			_saveButton = null;

	/**
	 * The combobox
	 */
	private JComboBox						_combo			= null;

	/**
	 * Enable/Disable the debug mode, default is enabled
	 */
	private boolean							_debugmode		= true;

	/**
	 * Enabled the compression mode, default is off
	 */
	private boolean							_compression	= false;

	/**
	 * The first entry in the list, will not cause an event call
	 */
	private String							_defaultEntry	= "--- choose example ---";

	/**
	 * The variables used by the JavaCode button
	 */
	private boolean							_javacode		= false;
	private JFrame							_javacodeFrame	= null;
	private JTextArea						_javacodeText	= null;
	private JButton							_javacodeButton	= null;


	//************************************
	// CONSTRUCTOR
	//************************************

	/**
	 * Default Constructor
	 */
	public ExamplePanel()
	{
		super();
		_initialize();
	}


	/**
	 * Creates a new <code>ExamplePanel</code> with a specific layout manager
	 * 
	 * @param layout
	 *            The layout manager
	 */
	public ExamplePanel(
			LayoutManager layout)
	{
		super(layout);
		_initialize();
	}


	/**
	 * Creates a buffered <code>ExamplePanel</code> with a
	 * <code>FlowLayout</code>.
	 * 
	 * @param isDoubleBuffered
	 *            db setting
	 * 
	 * @see java.awt.FlowLayout
	 */
	public ExamplePanel(
			boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		_initialize();
	}


	/**
	 * Creates an <code>ExamplePanel</code> with a specific layout manager and
	 * buffering strategy.
	 * 
	 * @param layout
	 *            The layout manager
	 * @param isDoubleBuffered
	 *            db setting
	 * 
	 * @see java.awt.FlowLayout
	 */
	public ExamplePanel(
			LayoutManager layout,
			boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		_initialize();
	}


	//************************************
	// PUBLIC METHODS
	//************************************

	/**
	 * Add an listener for example changes to this class
	 * 
	 * @param el
	 *            The listener to be added
	 * 
	 * @see anja.util.example.ExampleListener
	 */
	public void addExampleListener(
			ExampleListener<E> el)
	{
		if (this._listener != null && !this._listener.contains(el))
			this._listener.add(el);
	}


	/**
	 * Returns the currently chosen example
	 * 
	 * @return The example object or null if nothing was chosen
	 * 
	 * @see anja.util.example.Example
	 */
	public E getCurrentExample()
	{
		return this._example;
	}


	/**
	 * Removes an example listener
	 * 
	 * @param el
	 *            The example listener
	 * 
	 * @return true if the listener was deleted, false else
	 * 
	 * @see anja.util.example.ExampleListener
	 */
	public boolean removeExampleListener(
			ExampleListener<E> el)
	{
		return this._listener.remove(el);
	}


	/**
	 * Removes all listener for example changes
	 * 
	 * @see anja.util.example.ExampleListener
	 */
	public void removeAllExampleListener()
	{
		this._listener.clear();
	}


	/**
	 * Update the <code>JComboBox</code>
	 */
	public void addExampleToMenu(
			E e)
	{
		this._combo.addItem(e.toString());
	}


	/**
	 * Adds all examples from <code>em</code> to the panel
	 * 
	 * @param em
	 *            The <code>ExampleManager</code>
	 * 
	 * @see anja.util.example.ExampleManager
	 */
	public void addExampleManager(
			ExampleManager<E> em)
	{
		if (this._em == null)
			this._em = new ExampleManager<E>();
		this._em.addAll(em);

		//Add the example to the combo box
		for (E e : em)
			_combo.addItem(e.toString());
	}


	/**
	 * Returns a reference to the <code>ExampleManager</code> of this object
	 * 
	 * @return The <code>ExampleManager</code> of this object
	 * 
	 * @see anja.util.example.ExampleManager
	 */
	public ExampleManager<E> getExampleManager()
	{
		return this._em;
	}


	/**
	 * Loads an example from a file and adds the example to the manager. <br>
	 * The example must have been saved before and must not be a class file of
	 * an example class file. To add an instance of <code>Example</code> use the
	 * <code>ExampleManager</code>.
	 * 
	 * @param f
	 *            The file
	 * 
	 * @return true if the loading of the example was successful, false else
	 * @throws IOException
	 *             If the file could not be read, an <code>Exception</code> is
	 *             thrown
	 * 
	 * @see java.io.File
	 * @see anja.util.example.Example
	 * @see anja.util.example.ExampleManager
	 */
	public boolean loadExample(
			File f)
			throws IOException
	{
		/*
		 * Read all data from the file into an byte array
		 */
		FileInputStream fis = null;

		//Read length of file
		long flength = f.length();

		if (flength > Integer.MAX_VALUE)
			return false;

		//Create byte array
		byte[] b = new byte[(int) flength];
		int read = -1;

		try
		{
			fis = new FileInputStream(f);
			read = fis.read(b);
		}
		catch (IOException except)
		{
			if (this._debugmode)
				System.out.println(except);
			return false;
		}
		finally
		{
			if (fis != null)
			{
				try
				{
					fis.close();
				}
				catch (IOException except)
				{}
			}
		}

		/*
		 * If all bytes have been read, the example can be loaded, otherwise
		 * an IOException is thrown.
		 */
		if (read < flength)
		{
			throw new IOException("Could not completely read file "
					+ f.getName());
		}

		return loadExample(b);
	}


	/**
	 * Loads an example from a byte array and adds the example to the manager.
	 * <br> To add an instance of <code>Example</code> use the
	 * <code>ExampleManager</code>.
	 * 
	 * @param b
	 *            The byte array
	 * 
	 * @return true if the loading of the example was successful, false else
	 * 
	 * @see anja.util.example.Example
	 * @see anja.util.example.ExampleManager
	 */
	public boolean loadExample(
			byte[] b)
	{
		/*
		 * Create a ByteArrayIS and read all data into a ObjectIS -> create object
		 */
		ByteArrayInputStream bis = new ByteArrayInputStream(b);
		boolean result = loadExample(bis);
		try
		{
			bis.close();
		}
		catch (IOException except)
		{
			if (this._debugmode)
				System.out.println(except);
		}

		return result;
	}


	/**
	 * Loads an example from an <code>InputStream</code> and adds the example to
	 * the manager. <br> To add an instance of <code>Example</code> use the
	 * <code>ExampleManager</code>.
	 * 
	 * <br>
	 * 
	 * The stream is not closed after loading the example
	 * 
	 * @param is
	 *            The input stream
	 * 
	 * @return true if the loading of the example was successful, false else
	 * 
	 * @see anja.util.example.Example
	 * @see anja.util.example.ExampleManager
	 * @see java.io.InputStream
	 */
	@SuppressWarnings("unchecked")
	public boolean loadExample(
			InputStream is)
	{
		/*
		 * Create a ByteArrayIS and read all data into a ObjectIS -> create object
		 */
		ObjectInputStream ois = null;
		ZipInputStream zis = null;
		Object o = null;

		try
		{
			if (_compression)
			{
				zis = new ZipInputStream(is);
				zis.getNextEntry();
				//The ZipEntry is not neccessary
				ois = new ObjectInputStream(zis);
			}
			else
			{
				ois = new ObjectInputStream(is);
			}

			o = ois.readObject();
		}
		catch (IOException except)
		{
			if (this._debugmode)
				System.out.println(except);
			return false;
		}
		catch (ClassNotFoundException except)
		{
			if (this._debugmode)
				System.out.println(except);
			return false;
		}
		finally
		{
			if (ois != null)
			{
				try
				{
					ois.close();
				}
				catch (IOException except)
				{}
			}
		}

		/*
		 * If the object was read, cast the example and add it to the combo box
		 */
		if (o != null)
		{
			E example = null;

			try
			{
				example = (E) o;
			}
			catch (Exception except)
			{
				if (this._debugmode)
					System.out.println(except);
				return false;
			}

			if (example != null)
			{
				this._em.add(example);
				addExampleToMenu(example);
			}
		}

		return true;
	}


	/**
	 * Saves an example to a file.
	 * 
	 * <br>
	 * 
	 * The method doesn't add the result to the drop down menu. The file is
	 * overwritten.
	 * 
	 * @param f
	 *            The file
	 * @param e
	 *            The example
	 * @return true if the saving was successful, false else
	 * 
	 * @see java.io.File
	 * @see anja.util.example.Example
	 */
	public boolean saveExample(
			File f,
			E e)
	{
		//Create OS and use saveExample(OS)
		FileOutputStream fos = null;
		boolean result = false;

		try
		{
			//Save the object to file
			fos = new FileOutputStream(f);
			result = saveExample(fos, e);
			fos.flush();
		}
		catch (IOException except)
		{
			if (this._debugmode)
				System.out.println(except);
			return false;
		}
		finally
		{
			if (fos != null)
			{
				try
				{
					fos.close();
				}
				catch (IOException except)
				{}
			}
		}

		return result;
	}


	/**
	 * Saves an example to an output stream.
	 * 
	 * <br>
	 * 
	 * The method doesn't add the result to the drop down menu. The stream isn't
	 * closed after usage
	 * 
	 * @param os
	 *            The output stream
	 * @param e
	 *            The example
	 * @return true if the saving was successful, false else
	 * 
	 * @see java.io.OutputStream
	 * @see anja.util.example.Example
	 */
	public boolean saveExample(
			OutputStream os,
			E e)
	{
		//Try to save the object first before writing it to an OS
		ObjectOutputStream oos = null;
		ZipOutputStream zos = null;

		try
		{
			if (_compression)
			{
				zos = new ZipOutputStream(os);
				zos.putNextEntry(new ZipEntry(e.toString()));
				oos = new ObjectOutputStream(zos);
			}
			else
				oos = new ObjectOutputStream(os);

			oos.writeObject(e);
			oos.flush();
		}
		catch (IOException except)
		{
			if (this._debugmode)
				System.out.println(except);
			return false;
		}
		finally
		{
			if (oos != null)
			{
				try
				{
					oos.close();
				}
				catch (IOException except)
				{}
			}
			if (zos != null)
			{
				try
				{
					zos.close();
				}
				catch (IOException except)
				{}
			}
		}

		return true;
	}


	/**
	 * Enabled zip compression for examples.
	 * 
	 * <br />
	 * 
	 * The default setting is "compression disabled". In tests the size of
	 * compressed Polygon2Scenes was less than 30% of the same scene w/o
	 * compression.
	 * 
	 * @param enabled
	 *            true to enable compression
	 */
	public void setCompression(
			boolean enabled)
	{
		this._compression = enabled;
	}


	/**
	 * Sets the debug mode. In debug mode the exceptions during save and load
	 * are written to the console.
	 * 
	 * @param dm
	 *            The new setting for the debug mode, true is enabled
	 */
	public void setDebugMode(
			boolean dm)
	{
		this._debugmode = dm;
	}


	/**
	 * Sets the java code mode. In jc mode a button is added to the panel which
	 * allows to show the java code of the current chosen example.
	 * 
	 * <br />
	 * 
	 * By default this option is disabled.
	 * 
	 * @param jc
	 *            The new setting for the java code mode, true is enabled
	 */
	public void setJavaCode(
			boolean jc)
	{
		this._javacode = jc;
		_javacodeButton.setVisible(jc);
	}


	/**
	 * Enables to "Load" button.
	 * 
	 * <br />
	 * 
	 * By default this option is enabled.
	 * 
	 * @param lb
	 *            The new setting for the button, true is enabled
	 */
	public void setLoadButton(
			boolean lb)
	{
		_loadButton.setVisible(lb);
	}


	/**
	 * Enables to "Save" button.
	 * 
	 * <br />
	 * 
	 * By default this option is enabled.
	 * 
	 * @param sb
	 *            The new setting for the button, true is enabled
	 */
	public void setSaveButton(
			boolean sb)
	{
		_saveButton.setVisible(sb);
	}


	//************************************
	// INHERITED METHODS
	//************************************

	@Override
	public void actionPerformed(
			ActionEvent e)
	{
		//Load button
		if (e.getActionCommand().equals("load") && _loadButton.isVisible())
		{
			int returnVal = this._filechooser.showOpenDialog(this);

			//If file was chosen, load content
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = this._filechooser.getSelectedFile();
				try
				{
					this.loadExample(file);
				}
				catch (IOException except)
				{
					if (this._debugmode)
						System.out.println(except);
				}
			}
		} //load

		//Save button
		if (e.getActionCommand().equals("save") && _saveButton.isVisible())
		{
			boolean ready = false;
			File file = null;

			//If file exists, ask for overwrite or show dialog again
			while (!ready)
			{
				int returnVal = this._filechooser.showSaveDialog(this);

				//If file was chosen, check if file exists
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					file = this._filechooser.getSelectedFile();

					if (file.exists())
					{
						//Opens dialog overwrite yes/no
						int response = JOptionPane.showConfirmDialog(this,
								"Overwrite existing file?",
								"Confirm Overwrite",
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE);
						if (response == JOptionPane.OK_OPTION)
							ready = true;
					}
					else
						ready = true;
				}

				//If cancel pressed, no file has been chosen
				if (returnVal == JFileChooser.CANCEL_OPTION)
				{
					file = null;
					ready = true;
				}
			}

			//If file was chosen correctly, save it
			if (file != null)
				this._informSaveListener(file);
		} //save

		//Combo box
		if (e.getActionCommand().equals("combo"))
		{
			int index = this._combo.getSelectedIndex();
			if (index >= 1)
			{
				//The first example is the default text
				this._example = this._em.get(index - 1);
				this._informChangeListener(this._example);
			}
		} //combo

		//Java code button
		if (_javacode && e.getActionCommand().equals("javacode")
				&& _example != null)
		{
			_javacodeText.setText(Adapters.toJavaCode(_example));
			_javacodeFrame.setVisible(true);
		} //javacode
	}


	//************************************
	// PRIVATE METHODS
	//************************************

	/**
	 * Informs all listeners that an example change occured
	 * 
	 * @param ex
	 *            The new chosen example
	 * 
	 * @see anja.util.example.Example
	 * @see anja.util.example.ExampleListener
	 */
	private void _informChangeListener(
			E ex)
	{
		if (_listener != null && _listener.size() > 0)
			for (ExampleListener<E> el : _listener)
			{
				ExampleChangeEvent<E> ev = new ExampleChangeEvent<E>(this, ex);
				el.exampleChanged(ev);
			}
	}


	/**
	 * Informs all listeners that an example needs to be saved
	 * 
	 * @param f
	 *            The file to save to
	 * 
	 * @see java.io.File
	 * @see anja.util.example.ExampleListener
	 */
	private void _informSaveListener(
			File f)
	{
		if (_listener != null && _listener.size() > 0)
			for (ExampleListener<E> el : _listener)
			{
				ExampleSaveEvent ev = new ExampleSaveEvent(this, f);
				el.exampleSave(ev);
			}
	}


	/**
	 * Initializes the variables and creates the GUI of the panel
	 */
	private void _initialize()
	{
		//Standard objects
		this._em = new ExampleManager<E>();
		this._listener = new ArrayList<ExampleListener<E>>();

		//Set layout
		this.setLayout(new FlowLayout());

		//create filechooser for load and save operations
		this._filechooser = new JFileChooser();

		//The load button
		this._loadButton = new JButton("Load");
		this._loadButton.setActionCommand("load");
		this._loadButton
				.setToolTipText("Load the scene/example from a file. This only works if the applet has sufficient rights to do so.");

		//The save button
		this._saveButton = new JButton("Save");
		this._saveButton.setActionCommand("save");
		this._saveButton
				.setToolTipText("Save the scene/example to file. This only works if the applet has sufficient rights to do so.");

		//The java code button
		this._javacodeButton = new JButton("JavaCode");
		this._javacodeButton.setActionCommand("javacode");
		this._javacodeButton
				.setToolTipText("Display the java code of the current scene/example.");
		this._javacodeButton.setVisible(_javacode);

		//The combo box
		this._combo = new JComboBox();
		this._combo.setActionCommand("combo");
		this._combo.setToolTipText("Choose the example.");
		this._combo.addItem(this._defaultEntry);

		//Add ActionListener
		this._saveButton.addActionListener(this);
		this._loadButton.addActionListener(this);
		this._javacodeButton.addActionListener(this);
		this._combo.addActionListener(this);

		//Java code frame
		this._javacodeText = new JTextArea();
		this._javacodeText.setLineWrap(true);
		this._javacodeText.setWrapStyleWord(true);
		JScrollPane jsp = new JScrollPane(_javacodeText);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this._javacodeFrame = new JFrame("Java code");
		this._javacodeFrame.setSize(300, 300);
		this._javacodeFrame.setLayout(new BorderLayout());
		this._javacodeFrame.add(jsp, BorderLayout.CENTER);
		this._javacodeFrame.setVisible(false);

		//Add all components
		this.add(this._combo);
		this.add(this._loadButton);
		this.add(this._saveButton);
		this.add(this._javacodeButton);

	}

}
