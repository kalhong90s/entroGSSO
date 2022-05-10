package com.ais.eqx.gsso.utils;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.DestinationBean;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.MapDestinationBean;
import com.ais.eqx.gsso.interfaces.LogName;

import ais.mmt.sand.comlog.SummaryLogPrototype;
import ais.mmt.sand.comlog.exception.CommonLogException;
import ec02.af.abstracts.AbstractAF;
import ec02.af.utils.Log;

public class GssoComposeSummaryLog {

	private SummaryLogPrototype	summary;
	private String				initialInvoke;
	private String				beanNodeName		= "";
	private String				beanNodeCommand		= "";
	private String				beanNodeResultCode	= "";
	private String				beanNodeResultDesc	= "";
	private boolean				beanNodeWrite		= false;
	private boolean				writeSummary		= false;

	public GssoComposeSummaryLog(final AbstractAF abstractAF, String identity) {
		super();

		String appName = abstractAF.getEquinoxProperties().getApplicationName();
		String session = abstractAF.getEquinoxProperties().getSession();

		/** INITIATE APP-Summary **/
		this.summary = SummaryLogPrototype.getInstance(appName);
		this.summary.setSession(session);
		
		if(identity != null && !identity.isEmpty()){
			this.summary.setIdentity(identity);
		}
		else{
			this.summary.setIdentity("");
		}
	}

	public void addDestinationBean(final String nodeName, final String nodeCommand, final String nodeResultCode,
			final String nodeResultDesc) {
		this.beanNodeWrite = true;
		this.beanNodeName = nodeName;

		try {
			String[] nodeCommandSplit = nodeCommand.split("\\.");
			this.beanNodeCommand = nodeCommandSplit[1];
		}
		catch (Exception e) {
			this.beanNodeCommand = nodeCommand;
		}

		this.beanNodeResultCode = nodeResultCode;
		this.beanNodeResultDesc = nodeResultDesc;
	}

	public void initialSummary(APPInstance appInstance, final long reqTime, final String initialInvoke, final String eventLog, final String resultCode,
			final String resultDescription) {
		this.initialInvoke = initialInvoke;

		this.summary.setReqTimeStamp(reqTime);

		try {
			String[] invoke = initialInvoke.split("@");
			this.initialInvoke = invoke[0];
		}
		catch (Exception e) {
			this.initialInvoke = initialInvoke;
		}
		String commandName = eventLog.split("\\.")[1];

		/*
		 * oper
		 */
		String oper = appInstance.getProfile().getOper();
		oper = StringUtils.isEmpty(oper) ? "null" : oper;
		
		/*
		 * transactoinID
		 */
		String transactoinID = appInstance.getMapOrigInvokeTransactionID().get(initialInvoke);
		transactoinID = StringUtils.isEmpty(transactoinID) ? "null" : transactoinID;
		
		/*
		 * invoke
		 */
		String invoke = StringUtils.isEmpty(this.initialInvoke) ? "null" : this.initialInvoke;
		if(appInstance.isTransaction()){
			invoke = invoke.replace("invoke_unknown", "null");
		}
		
		this.summary.setInitInvoke(invoke + "-" + oper + "-" + transactoinID);
		this.summary.setCmdName(commandName);

		this.summary.setResultCode(resultCode);
		this.summary.setResultDesc(resultDescription);

	}

	public void getSummaryLog(HashMap<String, MapDestinationBean> mapDestinationBean, String origInvoke) throws Exception {
		try {
			if (mapDestinationBean != null && mapDestinationBean.size() > 0
					&& mapDestinationBean.get(origInvoke).getDestinationBeanList() != null) {
				for (DestinationBean destinationBean : mapDestinationBean.get(origInvoke).getDestinationBeanList()) {

					String nodeCommand = "";
					try {
						String[] nodeCommandSplit = destinationBean.getNodeCommand().split("\\.");
						nodeCommand = nodeCommandSplit[1];
					}
					catch (Exception e) {
						nodeCommand = destinationBean.getNodeCommand();
					}

					this.summary.addDestination(destinationBean.getNodeName(), nodeCommand, destinationBean.getNodeResultCode(),
							destinationBean.getNodeResultDesc());
				}
				mapDestinationBean.remove(origInvoke);
			}
		}
		catch (Exception e) {
			Log.e(e.getMessage());
		}

		if (beanNodeWrite) {
			try {
				this.summary.addDestination(this.beanNodeName, this.beanNodeCommand, this.beanNodeResultCode, this.beanNodeResultDesc);
				beanNodeWrite = false;
			}
			catch (CommonLogException e) {
				Log.e(e.getMessage());
			}
		}
	}

	public void writeSummaryLog(final EC02Instance ec02Instance, final long resTime) throws Exception {
		this.summary.setRespTimeStamp(resTime);

		ec02Instance.writeLog(true, ConfigureTool.getConfigureLogName(LogName.SUMMARY_LOG_NAME), this.summary.print());
	}

	public SummaryLogPrototype getSummaryLog() {
		return this.summary;
	}

	public boolean isWriteSummary() {
		return writeSummary;
	}

	public void setWriteSummary() {
		this.writeSummary = true;
	}

}
