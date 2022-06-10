package com.ais.eqx.gsso.instances;

import java.util.ArrayList;
import java.util.HashMap;

import com.ais.eqx.gsso.enums.GssoCommand;

import ec02.af.data.EquinoxRawData;

public class OrigInvokeProfile {

	private EquinoxRawData			origEquinoxRawData;

	/* WS OTP REQUEST */
	private SendWSOTPRequest		sendWSOTPRequest;
	private GssoCommand 			gssoOrigCommand = GssoCommand.UNKNOWN;
	private String 					messageType;
	
	private boolean					sendUSMPSecond = false;
	private GssoOTPRequest			gssoOTPRequest;
	private GssoServiceTemplate		gssoServiceTemplate;
	private String					cmdName					= "";
	private String					scenarioName			= "";

	/* WRITE LOG */
	private String					subState;
	private String					messageValidator		= "Complete";
	private String					failureAvp;
	private String					activeType				= "normal";

	private boolean					isSMPPRoaming			= false;

	private long					startTimeOfInvoke		= 0;

	/* ^^^ WRITE LOG ^^^ */
	private String					transactionID;
	private String					serviceKey;
	private String					orderRefLog;

	/* LITRY LIMIT */
	private int						emailRetryLimit			= 0;
	private int						smsRetryLimit			= 0;

	private String					smMessageId				= "";

	private long					submitSmRequestTime;
	private long					submitSmRespTime;

	private boolean					isAuthAndMissingSeedKey	= false;
	private String					realSMSCDeliveryReceipt	= "";
	private String					refundFlag	= "";

	private String					incomingMessageType		= "";
	private String 					detailsService			= "unknown";

	/** isDR will used when isSMS is active **/
	private HashMap<String, String>	mapSentOTPResult		= new HashMap<String, String>();

	private int smsOutgoing ;
	private int smsIncoming;
	private int drIncoming;

	private ArrayList<EquinoxRawData> rawDatasOut = new ArrayList<EquinoxRawData>();
	private ArrayList <String> msgIdList  = new ArrayList<String>();
	private boolean					isBypassUSMP	= false;
	private ArrayList<EquinoxRawData> rawDatasOutStateDr = new ArrayList<EquinoxRawData>();


	public long getSubmitSmRequestTime() {
		return submitSmRequestTime;
	}

	public void setSubmitSmRequestTime(long submitSmRequestTime) {
		this.submitSmRequestTime = submitSmRequestTime;
	}

	public long getSubmitSmRespTime() {
		return submitSmRespTime;
	}

	public void setSubmitSmRespTime(long submitSmRespTime) {
		this.submitSmRespTime = submitSmRespTime;
	}

	public EquinoxRawData getOrigEquinoxRawData() {
		return origEquinoxRawData;
	}

	public void setOrigEquinoxRawData(EquinoxRawData orgEquinoxRawData) {
		this.origEquinoxRawData = orgEquinoxRawData;
	}

	public String getSubState() {
		return subState;
	}

	public void setSubState(String subState) {
		this.subState = subState;
	}

	public String getMessageValidator() {
		return messageValidator;
	}

	public void setMessageValidator(String messageValidator) {
		this.messageValidator = messageValidator;
	}

	public String getFailureAvp() {
		return failureAvp;
	}

	public void setFailureAvp(String failureAvp) {
		this.failureAvp = failureAvp;
	}

	public String getActiveType() {
		return activeType;
	}

	public void setActiveType(String activeType) {
		this.activeType = activeType;
	}

	public long getStartTimeOfInvoke() {
		return startTimeOfInvoke;
	}

	public void setStartTimeOfInvoke(long startTimeOfInvoke) {
		this.startTimeOfInvoke = startTimeOfInvoke;
	}

	public GssoOTPRequest getGssoOTPRequest() {
		return gssoOTPRequest;
	}

	public void setGssoOTPRequest(GssoOTPRequest gssoOTPRequest) {
		this.gssoOTPRequest = gssoOTPRequest;
	}

	public String getSmMessageId() {
		return smMessageId;
	}

	public void setSmMessageId(String smMessageId) {
		this.smMessageId = smMessageId;
	}

	public HashMap<String, String> getMapSentOTPResult() {
		return mapSentOTPResult;
	}

	public void setMapSentOTPResult(HashMap<String, String> mapSentOTPResult) {
		this.mapSentOTPResult = mapSentOTPResult;
	}

	public String getTransactionID() {
		return transactionID;
	}

	public void setTransactionID(String transactionID) {
		this.transactionID = transactionID;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public int getEmailRetryLimit() {
		return emailRetryLimit;
	}

	public void setEmailRetryLimit(int emailRetryLimit) {
		this.emailRetryLimit = emailRetryLimit;
	}

	public void increaseEmailRetryLimit() {
		this.emailRetryLimit += 1;
	}

	public void increaseSMSRetryLimit() {
		this.smsRetryLimit += 1;
	}

	public int getSmsRetryLimit() {
		return smsRetryLimit;
	}

	public void setSmsRetryLimit(int smsRetryLimit) {
		this.smsRetryLimit = smsRetryLimit;
	}

	public GssoServiceTemplate getGssoServiceTemplate() {
		return gssoServiceTemplate;
	}

	public void setGssoServiceTemplate(GssoServiceTemplate gssoServiceTemplate) {
		this.gssoServiceTemplate = gssoServiceTemplate;
	}

	public String getCmdName() {
		return cmdName;
	}

	public void setCmdName(String cmdName) {
		this.cmdName = cmdName;
	}

	public String getScenarioName() {
		return scenarioName;
	}

	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}

	public boolean isSMPPRoaming() {
		return isSMPPRoaming;
	}

	public void setSMPPRoaming(boolean isSMPPRoaming) {
		this.isSMPPRoaming = isSMPPRoaming;
	}

	public String getOrderRefLog() {
		return orderRefLog;
	}

	public void setOrderRefLog(String orderRefLog) {
		this.orderRefLog = orderRefLog;
	}

	public boolean isAuthAndMissingSeedKey() {
		return isAuthAndMissingSeedKey;
	}

	public void setAuthAndMissingSeedKey(boolean isAuthAndMissingSeedKey) {
		this.isAuthAndMissingSeedKey = isAuthAndMissingSeedKey;
	}

	public String getRealSMSCDeliveryReceipt() {
		return realSMSCDeliveryReceipt;
	}

	public void setRealSMSCDeliveryReceipt(String realSMSCDeliveryReceipt) {
		this.realSMSCDeliveryReceipt = realSMSCDeliveryReceipt;
	}

	public String getIncomingMessageType() {
		return incomingMessageType;
	}

	public void setIncomingMessageType(String incomingMessageType) {
		this.incomingMessageType = incomingMessageType;
	}

	public String getDetailsService() {
		return detailsService;
	}

	public void setDetailsService(String detailsService) {
		this.detailsService = detailsService;
	}

	public SendWSOTPRequest getSendWSOTPRequest() {
		return sendWSOTPRequest;
	}

	public void setSendWSOTPRequest(SendWSOTPRequest sendWSOTPRequest) {
		this.sendWSOTPRequest = sendWSOTPRequest;
	}

	public GssoCommand getGssoOrigCommand() {
		return gssoOrigCommand;
	}

	public void setGssoOrigCommand(GssoCommand gssoOrigCommand) {
		this.gssoOrigCommand = gssoOrigCommand;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public boolean isSendUSMPSecond() {
		return sendUSMPSecond;
	}

	public void setSendUSMPSecond(boolean sendUSMPSecond) {
		this.sendUSMPSecond = sendUSMPSecond;
	}

	public String getRefundFlag() {
		return refundFlag;
	}

	public void setRefundFlag(String refundFlag) {
		this.refundFlag = refundFlag;
	}

	public int getSmsOutgoing() {
		return smsOutgoing;
	}

	public void setSmsOutgoing(int smsOutgoing) {
		this.smsOutgoing = smsOutgoing;
	}

	public int getSmsIncoming() {
		return smsIncoming;
	}

	public void setSmsIncoming(int smsIncoming) {
		this.smsIncoming = smsIncoming;
	}

	public int getDrIncoming() {
		return drIncoming;
	}

	public void setDrIncoming(int drIncoming) {
		this.drIncoming = drIncoming;
	}

	public ArrayList<EquinoxRawData> getRawDatasOut() {
		return rawDatasOut;
	}

	public void setRawDatasOut(ArrayList<EquinoxRawData> rawDatasOut) {
		this.rawDatasOut = rawDatasOut;
	}

	public ArrayList<String> getMsgIdList() {
		return msgIdList;
	}

	public void setMsgIdList(ArrayList<String> msgIdList) {
		this.msgIdList = msgIdList;
	}

	public boolean isBypassUSMP() {
		return isBypassUSMP;
	}

	public void setBypassUSMP(boolean bypassUSMP) {
		isBypassUSMP = bypassUSMP;
	}

	public ArrayList<EquinoxRawData> getRawDatasOutStateDr() {
		return rawDatasOutStateDr;
	}

	public void setRawDatasOutStateDr(ArrayList<EquinoxRawData> rawDatasOutStateDr) {
		this.rawDatasOutStateDr = rawDatasOutStateDr;
	}
}
