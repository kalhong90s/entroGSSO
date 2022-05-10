package com.ais.eqx.gsso.instances;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ListOfHop")
public class ListOfHop {

	@XmlElement(name = "Spname")
	String	spName;

	@XmlElement(name = "HopDateTime")
	String	hopDateTime;

	public String getSpName() {
		return spName;
	}

	public void setSpName(String spName) {
		this.spName = spName;
	}

	public String getHopDateTime() {
		return hopDateTime;
	}

	public void setHopDateTime(String hopDateTime) {
		this.hopDateTime = hopDateTime;
	}

}
