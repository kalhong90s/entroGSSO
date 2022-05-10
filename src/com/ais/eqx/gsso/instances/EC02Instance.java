package com.ais.eqx.gsso.instances;

import java.util.ArrayList;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxProperties;
import ec02.af.data.EquinoxRawData;

public class EC02Instance {

	private ArrayList<EquinoxRawData>	equinoxRawDatas;
	private String						ret;
	private String						timeout;
	private EquinoxProperties			equinoxProperties;
	private AbstractAF					abstractAF;
	
	private APPInstance					appInstance;

	public ArrayList<EquinoxRawData> getEquinoxRawDatas() {
		return equinoxRawDatas;
	}

	public void setEquinoxRawDatas(ArrayList<EquinoxRawData> equinoxRawDatas) {
		this.equinoxRawDatas = equinoxRawDatas;
	}

	public String getRet() {
		return ret;
	}

	public void setRet(String ret) {
		this.ret = ret;
	}

	public String getTimeout() {
		return timeout;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public EquinoxProperties getEquinoxProperties() {
		return equinoxProperties;
	}

	public void setEquinoxProperties(EquinoxProperties equinoxProperties) {
		this.equinoxProperties = equinoxProperties;
	}

	public AbstractAF getAbstractAF() {
		return abstractAF;
	}

	public void setAbstractAF(AbstractAF abstractAF) {
		this.abstractAF = abstractAF;
	}

	public APPInstance getAppInstance() {
		return appInstance;
	}

	public void setAppInstance(APPInstance appInstance) {
		this.appInstance = appInstance;
	}

	public void incrementsStat(String statFormat) {
		this.getAbstractAF().getEquinoxUtils().incrementStats(statFormat);
	}

	public void incrementsStat(String statFormat, String command) {
		this.getAbstractAF().getEquinoxUtils().incrementStats(String.format(statFormat, command));
	}

	public void writeLog(boolean isWrite, String logName, String log) {
		if (isWrite) {
			abstractAF.getEquinoxUtils().writeLog(logName, log);
		}
	}
	

}
