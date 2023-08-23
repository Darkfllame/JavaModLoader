package com.darkfllame.modloader;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class JSONApi {
	private static JSONObject jsonFromJarFile(String path) throws IOException {
		URL indexUrl = new URL(path);
		JarURLConnection conn = (JarURLConnection) indexUrl.openConnection();
		
		InputStream is = conn.getInputStream();
		String content = "";
		for (int i=0;(i=is.read())!=-1;)
			content+=(char)i;
		is.close();
		
		return new JSONObject(content);
	}
	public static JSONObject jsonFromFile(String filename) throws IOException {
		if (filename.startsWith("jar:")) {
			return jsonFromJarFile(filename);
		} else if(!filename.endsWith(".json")) {
			throw new IllegalArgumentException("Filename must end with \".json\"");
		}
		
		FileReader reader = new FileReader(filename);
		String content = "";
		for (int i=0;(i=reader.read())!=-1;)
			content+=(char)i;
		reader.close();
		
		return new JSONObject(content);
	}
}