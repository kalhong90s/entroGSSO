package com.ais.eqx.gsso.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EquinoxEvent;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.TransactionData;
import com.ais.eqx.gsso.interfaces.EquinoxAttribute;
import com.ais.eqx.gsso.interfaces.EventAction;
import com.ais.eqx.gsso.interfaces.EventCtype;
import com.ais.eqx.gsso.interfaces.EventName;
import com.ais.eqx.gsso.interfaces.GlobaldataEventType;

import ais.mmt.sand.comlog.bean.DataBean;
import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;
import ec02.common.data.E01Data;
import ec02.common.data.GlobalData;

public class GssoComposeDebugLog {
	private boolean					isProcess			= false;																;

	private APPInstance				appInstance;
	private StringBuilder			listTransactionId;

	private StringBuilder			debugSubStateLog;

	private String					passkeyDecode		= "";
	private String					passkeyEncode		= "";

	private String					messageValidator	= "-";
	private String					failureAvp			= "-";
	private String					subState			= "";
	private String					confirmResult		= "";
	private String					drResult			= "";
	private String					messageResponse		= "";
	private String					resultFailed		= "\n     RESULT: Failed";
	private boolean					seedKeyNotFound		= false;

	private ArrayList<String>		statisticIn;
	private ArrayList<String>		statisticOut;

	private final ArrayList<String>	idleService			= ConfigureTool.getConfigureArray(ConfigName.IDLE_SERVICE);
	private final ArrayList<String>	drService			= ConfigureTool.getConfigureArray(ConfigName.SMPPGW_INTERFACE);
	private final ArrayList<String>	drRoamingService	= ConfigureTool.getConfigureArray(ConfigName.SMPPGW_ROAMING_INTERFACE);

	public GssoComposeDebugLog(final APPInstance appInstance, final boolean isProcess) {
		this.isProcess = isProcess;
		if (isProcess) {
			this.appInstance = appInstance;
			this.debugSubStateLog = new StringBuilder();
			this.statisticIn = new ArrayList<String>();
			this.statisticOut = new ArrayList<String>();
		}

	}

	public void initialGssoHandlerLog() {
		if (isProcess) {
			StringBuilder log = new StringBuilder();

			this.listTransactionId = new StringBuilder();
			Iterator<Entry<String, TransactionData>> transactionId = appInstance.getTransactionidData().entrySet().iterator();
			while (transactionId.hasNext()) {
				Entry<String, TransactionData> entry = (Entry<String, TransactionData>) transactionId.next();
				if (entry.getValue().isActive()) {
					if (((entry.getValue().getTransactionIdExpireTime() - System.currentTimeMillis()) / 1000.0) <= 0) {
						listTransactionId.append("\n");
						listTransactionId.append("     Transection Id: " + entry.getKey() + " : " + "Is Expire.");
					}
					else {
						listTransactionId.append("\n");
						listTransactionId.append("     Transection Id: " + entry.getKey() + " : "
								+ ((entry.getValue().getTransactionIdExpireTime() - System.currentTimeMillis()) / 1000.0) + " Sec.");
						if (((entry.getValue().getOtpExpireTime() - System.currentTimeMillis()) / 1000.0) <= 0) {
							listTransactionId.append(" | OTP: " + "Is Expire.");
						}
						else {
							listTransactionId.append(" | OTP: "
									+ ((entry.getValue().getOtpExpireTime() - System.currentTimeMillis()) / 1000.0) + " Sec.");
						}
					}
				}
			}

			int otherWaitSize = 0;
			int quiryServiceSize = appInstance.getMaplistWQuiryService().size();
			int processSize = appInstance.getListInvokeProcessing().size();
			int allWaitSize = 0;
			if (quiryServiceSize > 0 && processSize > 0) {
				Iterator<Entry<String, ArrayList<String>>> itrWQuiry = appInstance.getMaplistWQuiryService().entrySet().iterator();
				while (itrWQuiry.hasNext()) {
					Entry<String, ArrayList<String>> entry = (Entry<String, ArrayList<String>>) itrWQuiry.next();
					for (String eachProcess : appInstance.getListInvokeProcessing()) {
						for (String eachWaiting : entry.getValue()) {
							if (eachWaiting.contains(eachProcess)) {
								otherWaitSize++;
							}
						}
					}
					allWaitSize += entry.getValue().size();
				}
				otherWaitSize = allWaitSize - otherWaitSize;
			}

			log.append("\n");
			log.append("\n=======================================================================");
			log.append("\n****************************** INSTANCE *******************************");
			log.append("\n=======================================================================");
			log.append("\n     InvokeProcessing: ").append(appInstance.getListInvokeProcessing().size());
			log.append("\n     InvokeWaiting: ").append(appInstance.getListWaitInquirySub().size() + otherWaitSize);
			log.append("\n     AmountTransactionID: ").append(appInstance.getMapTimeoutOfTransactionID().size());
			log.append("\n");

			/** TRANSACTION ID **/
			if (appInstance.getMapTimeoutOfTransactionID().size() != 0) {
				log.append("\n=======================================================================");
				log.append("\n*********************** TRANSACTION ID INSTANCE ***********************");
				log.append("\n=======================================================================");
				log.append(getListTransactionId().toString());
				log.append("\n=======================================================================");
			}
			
			Log.d(log.toString());
		}
	}

	public void initialGssoSubStateLog(final EquinoxRawData equinoxRawData) {
		if (isProcess) {

			String invoke = "";
			if (equinoxRawData.getType().toLowerCase().equals(EventAction.REQUEST)) {
				if (GssoDataManagement.containService(idleService, equinoxRawData.getOrig())
						|| GssoDataManagement.containService(drService, equinoxRawData.getOrig())
						|| GssoDataManagement.containService(drRoamingService, equinoxRawData.getOrig())) {
					try {
						String[] invokeIn = equinoxRawData.getInvoke().split("@");
						invoke = invokeIn[0];
					}
					catch (Exception e) {
						invoke = equinoxRawData.getInvoke();
					}
				}
				else {
					invoke = equinoxRawData.getInvoke();
				}
			}
			else if (equinoxRawData.getType().toLowerCase().equals(EventAction.RESPONSE)) {
				if (GssoDataManagement.containService(idleService, equinoxRawData.getTo())) {
					try {
						String[] invokeOut = equinoxRawData.getInvoke().split("@");
						invoke = invokeOut[0];
					}
					catch (Exception e) {
						invoke = equinoxRawData.getInvoke();
					}
				}
				else {
					invoke = equinoxRawData.getInvoke();
				}
			}
			else {
				invoke = equinoxRawData.getInvoke();
			}

			this.debugSubStateLog.append("\n=======================================================================");
			this.debugSubStateLog.append("\n************************* SUB STATE " + this.subState + " ************************");
			this.debugSubStateLog.append("\n=======================================================================");
			this.debugSubStateLog.append("\n     RET: ").append(equinoxRawData.getRet());
			this.debugSubStateLog.append("\n     INVOKE: ").append(invoke);
			this.debugSubStateLog.append("\n     TYPE: ").append(equinoxRawData.getType());
			if (equinoxRawData.getCType().equalsIgnoreCase(EventCtype.PLAIN)) {

				DataBean dataBean = new DataBean();
				String val = "";

				try {
					val = equinoxRawData.getRawDataAttribute(EquinoxAttribute.VAL);
				}
				catch (Exception e) {
					val = "";
				}
				
				if(StringUtils.isNotEmpty(val)){
					val = val.replace("&", "&amp;");
					val = val.replace("\"", "&quot;");
					val = val.replace("<", "&lt;");
					val = val.replace(">", "&gt;");
				}
				
				dataBean.setRawData(val);
				
				this.debugSubStateLog.append("\n     URL: ").append(equinoxRawData.getRawDataAttribute(EquinoxAttribute.URL));
				this.debugSubStateLog.append("\n     MESSAGE: ").append(dataBean.getRawData());
			}
			else if (equinoxRawData.getCType().equalsIgnoreCase(EventCtype.XML)) {
				this.debugSubStateLog.append("\n     MESSAGE: ").append(equinoxRawData.getRawDataMessage());
			}
			else if (equinoxRawData.getCType().equalsIgnoreCase(EventCtype.SMS)) {
				this.debugSubStateLog.append("\n     MESSAGE: ").append(equinoxRawData.getRawDataMessage());
			}
			else if (equinoxRawData.getCType().equalsIgnoreCase(EventCtype.DR)) {
				this.debugSubStateLog.append("\n     MESSAGE: ").append(equinoxRawData.getRawDataMessage());
			}
			this.debugSubStateLog.append("\n");
			this.debugSubStateLog.append("\n     MESSAGE VALIDATOR: ").append(this.messageValidator);
			this.debugSubStateLog.append("\n     FAILURE AVP: ").append(this.failureAvp);

			if (this.messageResponse.length() > 0) {
				if (this.messageValidator.equalsIgnoreCase(EventName.INCOMPLETE) && this.messageResponse.contains("RESULT: Failed")) {
					this.debugSubStateLog.append(this.resultFailed);
				}
				else {
					this.debugSubStateLog.append(this.messageResponse);
				}
			}

			if (this.confirmResult.length() > 0) {
				if (this.messageValidator.equalsIgnoreCase(EventName.INCOMPLETE) && this.confirmResult.contains("RESULT: Failed")) {
					this.debugSubStateLog.append(this.resultFailed);
				}
				else {
					this.debugSubStateLog.append(this.confirmResult);
				}
			}

			if (this.drResult.length() > 0) {
				if (this.messageValidator.equalsIgnoreCase(EventName.INCOMPLETE) && this.drResult.contains("RESULT: Failed")) {
					this.debugSubStateLog.append(this.resultFailed);
				}
				else {
					this.debugSubStateLog.append(this.drResult);
				}
			}

			if (!equinoxRawData.getRet().equals(EquinoxEvent.NORMAL.getCode())) {
				if (equinoxRawData.getRet().equals(EquinoxEvent.REJECT.getCode())) {
					this.debugSubStateLog.append("\n     RESULT: Failed (equinox reject)");
				}
				if (equinoxRawData.getRet().equals(EquinoxEvent.ABORT.getCode())) {
					this.debugSubStateLog.append("\n     RESULT: Failed (equinox abort)");
				}
				if (equinoxRawData.getRet().equals(EquinoxEvent.ERROR.getCode())) {
					this.debugSubStateLog.append("\n     RESULT: Failed (equinox error)");
				}
				if (equinoxRawData.getRet().equals(EquinoxEvent.TIMEOUT.getCode())) {
					this.debugSubStateLog.append("\n     RESULT: Failed (equinox timeout)");
				}
			}

			if (!passkeyDecode.isEmpty() && !passkeyEncode.isEmpty()) {
				this.debugSubStateLog.append("\n");
				this.debugSubStateLog.append("\n     NORMAL PASSKEY: ").append(this.passkeyDecode);
				this.debugSubStateLog.append("\n     ENCODE PASSKEY: ").append(this.passkeyEncode);
			}
			if (seedKeyNotFound) {
				this.debugSubStateLog.append("\n");
				this.debugSubStateLog.append("\n     NORMAL PASSKEY: ").append("Seedkey Not Found");
				this.debugSubStateLog.append("\n     ENCODE PASSKEY: ").append("-");
			}

			this.debugSubStateLog.append("\n");
			if (statisticIn.size() > 0) {
				for (String statisticInEach : statisticIn) {
					if(StringUtils.isNotEmpty(statisticInEach)) this.debugSubStateLog.append("\n     STATISTIC IN: ").append(statisticInEach);
				}
			}
			if (statisticOut.size() > 0) {
				for (String statisticOutEach : statisticOut) {
					if(StringUtils.isNotEmpty(statisticOutEach)) this.debugSubStateLog.append("\n     STATISTIC IN: ").append(statisticOutEach);
				}
			}
		}

	}

	public void initialGssoSubStateLogE01Res(final AbstractAF abstractAF, final EquinoxRawData equinoxRawData, final E01Data e01Data) {
		if (isProcess) {
			String invoke = "";
			String ret = "";

			GlobalData e01Datas = abstractAF.getEquinoxUtils().getGlobalData();
			invoke = e01Data.getId();

			if (e01Datas.getGlobaldataEventType().equalsIgnoreCase(GlobaldataEventType.TIMEOUT)) {
				ret = "4";
				this.failureAvp = "-";
				if (invoke.equalsIgnoreCase("default_invoke")) {
					try {
						String[] invokeIn = equinoxRawData.getInvoke().split("@");
						invoke = invokeIn[0];
					}
					catch (Exception e) {
						invoke = equinoxRawData.getInvoke();
					}
				}
			}
			else if (e01Datas.getGlobaldataEventType().equalsIgnoreCase(GlobaldataEventType.ERROR)) {
				ret = "1";
				this.failureAvp = "-";
				if (invoke.equalsIgnoreCase("default_invoke")) {
					try {
						String[] invokeIn = equinoxRawData.getInvoke().split("@");
						invoke = invokeIn[0];
					}
					catch (Exception e) {
						invoke = equinoxRawData.getInvoke();
					}
				}
			}
			else if (e01Datas.getGlobaldataEventType().equalsIgnoreCase(GlobaldataEventType.ABORT)) {
				ret = "3";
				this.failureAvp = "-";
				if (invoke.equalsIgnoreCase("default_invoke")) {
					try {
						String[] invokeIn = equinoxRawData.getInvoke().split("@");
						invoke = invokeIn[0];
					}
					catch (Exception e) {
						invoke = equinoxRawData.getInvoke();
					}
				}
			}
			else if (e01Datas.getGlobaldataEventType().equalsIgnoreCase(GlobaldataEventType.REJECT)) {
				ret = "2";
				this.failureAvp = "-";
				if (invoke.equalsIgnoreCase("default_invoke")) {
					try {
						String[] invokeIn = equinoxRawData.getInvoke().split("@");
						invoke = invokeIn[0];
					}
					catch (Exception e) {
						invoke = equinoxRawData.getInvoke();
					}
				}
			}
			else if (e01Datas.getGlobaldataEventType().equalsIgnoreCase(GlobaldataEventType.NORMAL)) {
				ret = "0";
			}
			else {
				ret = e01Datas.getGlobaldataEventType();
			}

			DataBean dataBean = new DataBean();
			String val = "";

			try {
				val = e01Data.getData();
			}
			catch (Exception e) {
				val = "";
			}
			
			if(StringUtils.isNotEmpty(val)){
				val = val.replace("&", "&amp;");
				val = val.replace("\"", "&quot;");
				val = val.replace("<", "&lt;");
				val = val.replace(">", "&gt;");
			}
			
			dataBean.setRawData(val);

			this.debugSubStateLog.append("\n=======================================================================");
			this.debugSubStateLog.append("\n************************* SUB STATE " + this.subState + " ************************");
			this.debugSubStateLog.append("\n=======================================================================");
			this.debugSubStateLog.append("\n     RET: ").append(ret);
			this.debugSubStateLog.append("\n     INVOKE: ").append(invoke);
			this.debugSubStateLog.append("\n     TYPE: ").append(EventAction.RESPONSE);
			this.debugSubStateLog.append("\n     MESSAGE: ").append(dataBean.getRawData());
			this.debugSubStateLog.append("\n");
			this.debugSubStateLog.append("\n     MESSAGE VALIDATOR: ").append(this.messageValidator);
			this.debugSubStateLog.append("\n     FAILURE AVP: ").append(this.failureAvp);
			if (this.messageResponse.length() > 0) {
				this.debugSubStateLog.append(this.messageResponse);
			}

			if (e01Datas.getGlobaldataEventType().equalsIgnoreCase(GlobaldataEventType.TIMEOUT)) {
				this.debugSubStateLog.append("\n     RESULT: Failed (E01 timeout)");
			}
			else if (e01Datas.getGlobaldataEventType().equalsIgnoreCase(GlobaldataEventType.REJECT)) {
				this.debugSubStateLog.append("\n     RESULT: Failed (E01 reject)");
			}
			else if (e01Datas.getGlobaldataEventType().equalsIgnoreCase(GlobaldataEventType.ABORT)) {
				this.debugSubStateLog.append("\n     RESULT: Failed (E01 abort)");
			}
			else if (e01Datas.getGlobaldataEventType().equalsIgnoreCase(GlobaldataEventType.ERROR)) {
				this.debugSubStateLog.append("\n     RESULT: Failed (E01 error)");
			}

			if (!passkeyDecode.isEmpty() && !passkeyEncode.isEmpty()) {
				this.debugSubStateLog.append("\n");
				this.debugSubStateLog.append("\n     NORMAL PASSKEY: ").append(this.passkeyDecode);
				this.debugSubStateLog.append("\n     ENCODE PASSKEY: ").append(this.passkeyEncode);
			}

			this.debugSubStateLog.append("\n");
			if (statisticIn.size() > 0) {
				for (String statisticInEach : statisticIn) {
					if(StringUtils.isNotEmpty(statisticInEach)) this.debugSubStateLog.append("\n     STATISTIC IN: ").append(statisticInEach);
				}
			}
			if (statisticOut.size() > 0) {
				for (String statisticOutEach : statisticOut) {
					if(StringUtils.isNotEmpty(statisticOutEach)) this.debugSubStateLog.append("\n     STATISTIC IN: ").append(statisticOutEach);
				}
			}
		}
	}

	public void writeDebugSubStateLog() {
		if (isProcess) {
			if (this.debugSubStateLog.length() > 0) {
				this.debugSubStateLog.append("\n=======================================================================");
			}
			Log.d(getDebugSubStateLog().toString());
		}
	}

	public StringBuilder getListTransactionId() {
		return listTransactionId;
	}

	public void setListTransactionId(final StringBuilder listTransactionId) {
		this.listTransactionId = listTransactionId;
	}

	public StringBuilder getDebugSubStateLog() {
		return debugSubStateLog;
	}

	public void setDebugSubStateLog(final StringBuilder debugSubStateLog) {
		this.debugSubStateLog = debugSubStateLog;
	}

	public String getMessageValidator() {
		return messageValidator;
	}

	public void setMessageValidator(final String messageValidator) {
		if (isProcess) {
			this.messageValidator = messageValidator;
		}
	}

	public String getFailureAvp() {
		return failureAvp;
	}

	public void setFailureAvp(final String failureAvp) {
		if (isProcess) {
			this.failureAvp = failureAvp;
		}
	}

	public String getSubState() {
		return subState;
	}

	public void setSubState(final String subState) {
		if (isProcess) {
			this.subState = subState;
		}
	}

	public void addStatisticIn(final String statisticIn) {
		if (isProcess) {
			this.statisticIn.add(statisticIn);
		}
	}

	public void addStatisticOut(final String statisticOut) {
		if (isProcess) {
			this.statisticOut.add(statisticOut);
		}
	}

	public void confirmServiceFailure() {
		if (isProcess) {
			this.confirmResult = "\n     RESULT: Failed (service is mismatch)";
		}
	}

	public void confirmTransactionFailure(final String transactionID) {
		if (isProcess) {
			this.confirmResult = "\n     RESULT: Failed (transactionID " + transactionID + " is mismatch)";
		}
	}

	public void confirmOverLimit(final String transactionID) {
		if (isProcess) {
			this.confirmResult = "\n     RESULT: Failed (transactionID " + transactionID + " is match, pwd is over limit)";
		}
	}

	public void confirmPwdSuccess(final String transactionID) {
		if (isProcess) {
			this.confirmResult = "\n     RESULT: Success (transactionID " + transactionID + " is match, pwd is match)";
		}
	}

	public void confirmPwdFailure(final String transactionID) {
		if (isProcess) {
			this.confirmResult = "\n     RESULT: Failed (transactionID " + transactionID + " is match, pwd is mismatch)";
		}
	}

	public void confirmPwdExpire(final String transactionID) {
		if (isProcess) {
			this.confirmResult = "\n     RESULT: Failed (transactionID " + transactionID + " is match, pwd is expired)";
		}
	}

	public void drIDMissing() {
		if (isProcess) {
			this.drResult = "\n     RESULT: Failed (id is missing)";
		}
	}

	public void drIDInvalid() {
		if (isProcess) {
			this.drResult = "\n     RESULT: Failed (id is invalid)";
		}
	}

	public void drIDMismatch(final String id) {
		if (isProcess) {
			this.drResult = "\n     RESULT: Failed (id " + id + " is mismatch)";
		}
	}

	public void drIDMatchAndError(final String id, final String errCode) {
		if (isProcess) {
			this.drResult = "\n     RESULT: Failed (id " + id + " is match, err is " + errCode + ")";
		}
	}

	public void drIDMatch(final String id) {
		if (isProcess) {
			this.drResult = "\n     RESULT: Success (id " + id + " is match, err is 000)";
		}
	}

	public void messageResponseSuccess(final String resultCode) {
		if (isProcess) {
			this.messageValidator = EventName.COMPLETE;
			this.messageResponse = "\n     RESULT: Success (result code is " + resultCode + ")";
		}
	}

	public void messageResponseFailed(final String resultCode) {
		if (isProcess) {
			this.messageValidator = EventName.COMPLETE;
			this.messageResponse = "\n     RESULT: Failed (result code is " + resultCode + ")";
		}
	}

	public void serviceTemplateMisMatch() {
		if (isProcess) {
			this.messageValidator = EventName.COMPLETE;
			this.messageResponse = "\n     RESULT: Failed (oper is not found)";
		}
	}

	public void smsResponseSuccess(final String resultCode) {
		if (isProcess) {
			this.messageValidator = EventName.COMPLETE;
			this.messageResponse = "\n     RESULT: Success (message_id is " + resultCode + ")";
		}
	}

	public void smsResponseFailed(final String resultCode) {
		if (isProcess) {
			this.messageValidator = EventName.COMPLETE;
			this.messageResponse = "\n     RESULT: Failed (message_id is " + resultCode + ")";
		}
	}

	public void transactionIsOverLimit() {
		if (isProcess) {
			this.messageValidator = "-";
			this.failureAvp = "-";
			this.messageResponse = "\n     TRANSACTION: Is Over limit";
		}
	}

	public String getPasskeyDecode() {
		return passkeyDecode;
	}

	public void setPasskeyDecode(final String passkeyDecode) {
		if (isProcess) {
			this.passkeyDecode = passkeyDecode;
		}
	}

	public String getPasskeyEncode() {
		return passkeyEncode;
	}

	public void setPasskeyEncode(final String passkeyEncode) {
		if (isProcess) {
			this.passkeyEncode = passkeyEncode;
		}
	}

	public void isSeedKeyNotFound() {
		if (isProcess) {
			this.seedKeyNotFound = true;
		}
	}

	public ArrayList<String> getStatisticIn() {
		return statisticIn;
	}

	
}
