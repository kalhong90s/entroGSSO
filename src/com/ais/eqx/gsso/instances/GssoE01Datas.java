package com.ais.eqx.gsso.instances;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

public class GssoE01Datas {

	private String							serviceKeyValue	= null;

	@SerializedName("e01datas")
	private ArrayList<GssoServiceTemplate>	serviceTemplate	= null;

	public ArrayList<GssoServiceTemplate> getServiceTemplate() {
		return serviceTemplate;
	}

	public void setServiceTemplate(ArrayList<GssoServiceTemplate> serviceTemplate) {
		this.serviceTemplate = serviceTemplate;
	}

	public String getServiceKey() {
		return serviceKeyValue;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKeyValue = serviceKey;
	}

}
