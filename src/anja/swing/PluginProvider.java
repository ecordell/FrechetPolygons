package anja.swing;


/**
* Klassen, die dieses Interface implementieren, koennen einem Editor als Quelle
* fuer Szenenobjekt-Plugins dienen.<br>
* Standardmaessig ist jeder Editor bereits ein <code>PluginProvider</code>. Der
* Editor kann jedoch eine alternative Quelle verwenden, um so spezialisierte
* Plugins zu benutzen.
*
* @version 0.1 22.10.2004
* @author Sascha Ternes
*/

public interface PluginProvider {

/**
* Erzeugt ein neues Plugin fuer ein neues Szenenobjekt.
*
* @param register das <code>Register</code>-Objekt fuer das Plugin
* @param scene_object_class der Klassenname der Klasse des neuen Szenenobjekts
* @return das Plugin
*/
public JPluginDialog createPlugin( Register register,
                                   String scene_object_class );

/**
* Erzeugt ein neues Plugin fuer ein bereits vorhandenes Szenenobjekt.
*
* @param register das <code>Register</code>-Objekt fuer das Plugin
* @param object das Szenenobjekt, mit dem das Plugin initialisiert wird
* @return das Plugin
*/
public JPluginDialog createPlugin( Register register, SceneObject object );

} // PluginProvider
