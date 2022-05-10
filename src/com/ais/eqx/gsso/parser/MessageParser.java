package com.ais.eqx.gsso.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MessageParser {

	private static Gson gson;

	public static Gson getGsonContext() {
		if (gson == null) {
			gson = new GsonBuilder().disableHtmlEscaping().create();
		}
		return gson;
	}

	public static <T> Object fromJson(String strJson, Class<T> objClass) {
		Object obj = null;
		
		gson = getGsonContext();
		
		try {
			obj = gson.fromJson(strJson, objClass);
			return obj;
		} catch (Exception e) {
			return obj;
		}
	}

	public static String toJson(Object obj) {
		String strJson = "";
		
		gson = getGsonContext();
		
		try {
			strJson = gson.toJson(obj, obj.getClass());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return strJson;
	}

}
