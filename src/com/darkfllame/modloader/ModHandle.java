package com.darkfllame.modloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;

import org.json.JSONObject;

/**
 * This class give you an handle on a specific external jar file.
 * You can just use the constructor to make a new handle.
 * Give it a main class type and a filename.
 * 
 * Use: {@code new ModHandle<MainClassType>(filename)}
 * 
 * @author Darkfllame
 *
 * @param <T> The main class' type
 */
public class ModHandle<T> {
	private URLClassLoader loader;
	private Class<T> mainClass;

	@SuppressWarnings("unchecked")
	public ModHandle(String filename) throws ClassNotFoundException, IOException {
		if (!filename.endsWith(".jar"))
			throw new IllegalArgumentException("Filename must finish with \".jar\"");
		
		// Get the external jar file : filename
		File file = new File(filename);
		if (!file.exists())
			throw new FileNotFoundException("Cannot find " + filename);
		URL fileUrl = file.toURI().toURL();

		// Make a connection with the internal /mod.json object
		URL indexUrl = new URL("jar:" + fileUrl.toString() + "!/mod.json");
		JarURLConnection conn = (JarURLConnection) indexUrl.openConnection();
		
		// Gather the content of /mod.json
		InputStream is = conn.getInputStream();
		String content = "";
		for (int i = 0; (i = is.read()) != -1;)
			content += (char) i;
		is.close();
		
		// Transform the content of /mod.json to a org.JSONObject
		JSONObject jo = new JSONObject(content);
		String mainClass = jo.getString("mainclass");

		// Finally get the class loader and main class
		loader = new URLClassLoader(new URL[] { fileUrl });
		this.mainClass = (Class<T>) Class.forName(mainClass, true, loader);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void finalize() throws Throwable {
		try {
			loader.close();
		} finally {
			super.finalize();
		}
	}
	
	/* Return the URLClassLoader for this mod */
	public URLClassLoader GetLoader() {
		return loader;
	}
	/* Return the main class of the mod a.k.a the class defined by the mod.json in the mod jar file */
	public Class<T> GetMainClass() {
		return mainClass;
	}
	
	private Constructor<T> GetMainClassCtor(Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
		return mainClass.getConstructor(parameterTypes);
	}
	/* Return a MainClass method from the name and parameters types */
	public Method GetMainClassMethod(String name, Class<?> parameterTypes) throws NoSuchMethodException, SecurityException {
		return mainClass.getMethod(name, parameterTypes);
	}
	public T NewInstance(Object... parameters) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<?>[] paramTypes = new Class<?>[parameters.length];
		for (int i = 0; i < parameters.length; i++) 
			paramTypes[i] = parameters[i].getClass();
		return GetMainClassCtor(paramTypes).newInstance(parameters);
	}
}