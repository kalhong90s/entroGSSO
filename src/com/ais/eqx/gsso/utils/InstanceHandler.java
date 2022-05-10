package com.ais.eqx.gsso.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;

import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.jaxb.InstanceContext;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import ec02.af.utils.Log;

public class InstanceHandler {

	// ENCODING
	public static String encode(APPInstance instance) {

		byte[] bytes = null;
		String encoded = null;

		try {
			bytes = Zip.compressBytes(InstanceContext.getGson().toJson(instance).getBytes());
			encoded = Base64.encode(bytes);

		}
		catch (UnsupportedEncodingException e) {
			Log.e(e.getMessage());

		}
		catch (IOException e) {
			Log.e(e.getMessage());
		}

		return encoded;
	}

	// DECODING
	public static APPInstance decode(String encoded) {

		byte[] decoded = Base64.decode(encoded);

		try {
			decoded = Zip.extractBytes(decoded);

		}
		catch (UnsupportedEncodingException e) {
			Log.e(e.getMessage());
		}
		catch (IOException e) {
			Log.e(e.getMessage());
		}
		catch (DataFormatException e) {
			Log.e(e.getMessage());
		}

		APPInstance appInstance = InstanceContext.getGson().fromJson(new String(decoded), APPInstance.class);

		return appInstance;
	}
}
