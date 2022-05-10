package com.ais.eqx.gsso.instances;

public class GssoAuthOTP {

	private SendOneTimePWRequest	request			= null;

	private GssoServiceTemplate		serviceTemplate	= null;

	public SendOneTimePWRequest getRequest() {
		return request;
	}

	public void setRequest(SendOneTimePWRequest request) {
		this.request = request;
	}

	public GssoServiceTemplate getServiceTemplate() {
		return serviceTemplate;
	}

	public void setServiceTemplate(GssoServiceTemplate serviceTemplate) {
		this.serviceTemplate = serviceTemplate;
	}

}
