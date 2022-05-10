package com.ais.eqx.gsso.jaxb;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import ec02.af.utils.Log;



public class JAXBHandler {

	// IMPORT MESSAGE TO INSTANCE [ UNMARSHALLING ]
	public static Object createInstance(JAXBContext jaxbContext, String message, Class<?> instanceClass) {

		Object instance = null;
		Unmarshaller unmarshaller;
		try {
			unmarshaller = jaxbContext.createUnmarshaller();
			instance = Class.forName(instanceClass.getCanonicalName()).newInstance();
			instance = unmarshaller.unmarshal(new StringReader(message));

		}
		catch (Exception e) {
			Log.e(e.getMessage());
			return null;

		}

		return instance;
	}
	
	// EXPORT INSTANCE TO MESSAGE [ MARSHALLING ]
	public static String composeMessage(JAXBContext context, Object instance) {

		StringWriter writer = new StringWriter();

		Marshaller marshaller;
		try {
			marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

			marshaller.marshal(instance, writer);

		}
		catch (JAXBException e) {
			Log.e(e.getMessage());
		}

		return writer.toString();
	}

}
