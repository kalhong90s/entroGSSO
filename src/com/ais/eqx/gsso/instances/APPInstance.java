package com.ais.eqx.gsso.instances;

import java.util.ArrayList;
import java.util.HashMap;

import com.ais.eqx.gsso.utils.TimeoutCalculator;

import ais.mmt.sand.comlog.SummaryLogPrototype;

public class APPInstance {

	private long								timeStampIncoming	= 0;
	private long								timeStampOutgoing	= 0;
	
	private long								firstTimeStampIncoming	= 0;
	
	private String 	outgoingInvoke;
	private String 	origInvoke;
	private String 	orig;
	
	/*
	 * ListOfInvoke In Process
	 */
	private ArrayList<String>					listInvokeProcessing;

	/*
	 * map invoke for incoming in IDLE
	 */
	private HashMap<String, String>				mapInvokeOrig;

	/*
	 * map timeout invoke in process
	 */
	private HashMap<String, TimeoutCalculator>	mapTimeoutOfInvokePending;

	/*
	 * map timeout TransactionID in process
	 */
	private HashMap<String, TimeoutCalculator>	mapTimeoutOfTransactionID;

	/*
	 * map timeout waitDR in process
	 */
	private HashMap<String, TimeoutCalculator>	mapTimeoutOfWaitDR;

	/*
	 * map timeout waitDR in process
	 */
	private HashMap<String, TimeoutCalculator>	mapTimeoutOfWaitRefund;

	/*
	 * key is OrigInvoke value is Info of Invoke
	 */
	private HashMap<String, OrigInvokeProfile>	mapOrigProfile;

	/*
	 * key is service value is E01 Data
	 */
	private HashMap<String, GssoE01Datas>		mapE01dataofService;

	/*
	 * key is transactionID value is pwd and service
	 */
	private HashMap<String, TransactionData>	transactionidData;

	/* *
	 * list of orderRef, OTP, RefNumber in period
	 */
	private ArrayList<String>					listOrderReference;
	private ArrayList<String>					listOTP;
	private ArrayList<String>					listReferenceNumber;

	/*
	 * USMP PROFILE
	 */
	private GssoProfile							profile;

	/*
	 * listWaitQuiryService
	 */
	private HashMap<String, ArrayList<String>>	maplistWQuiryService;

	/*
	 * key is origInvoke value is EventDetailLog
	 */
	private HashMap<String, String>				mapOrigInvokeEventDetailInput;
	private HashMap<String, String>				mapOrigInvokeEventDetailOutput;
	private HashMap<String, String>				mapOrigInvokeDetailScenario;

	private HashMap<String, Long>				mapInvokeByOutputTime;
	private ArrayList<MapDetailsAndConfigType>	listDetailsLog;
	private ArrayList<SummaryLogPrototype>		listSummaryLog;
	private HashMap<String, String> 			mapOrigInvokeTransactionID;

	private boolean								isWaitInquirySub		= false;
	private boolean								isWaitPortCheck			= false;
	private boolean								isInquirySubSuccess		= false;
	private boolean								isInquiryVasSubscriber	= false;

	private HashMap<String, MapDestinationBean>	mapDestinationBean		= new HashMap<String, MapDestinationBean>();

	private ArrayList<String>					listWaitInqSub;
	
	/*
	 * WSDL
	 */
	private String								url;
	
	private HashMap<String, Refund> 			mapInvokeOfRefund;	
	private int 								refundRetryLimit;
	private String 								origCommand				= "UNKNOWN";
	private boolean 							isTimeoutOfConfirmReq	=  false;
	private boolean 							isTransaction			=  false;

	public APPInstance() {
		this.setMapTimeoutOfInvokePending(new HashMap<String, TimeoutCalculator>());
		this.setMapTimeoutOfTransactionID(new HashMap<String, TimeoutCalculator>());
		this.setMapTimeoutOfWaitDR(new HashMap<String, TimeoutCalculator>());
		this.setMapTimeoutOfWaitRefund(new HashMap<String, TimeoutCalculator>());

		this.setListInvokeProcessing(new ArrayList<String>());
		this.setMapInvokeOrig(new HashMap<String, String>());
		this.setMapOrigProfile(new HashMap<String, OrigInvokeProfile>());
		this.setTransactionidData(new HashMap<String, TransactionData>());
		this.setMapE01dataofService(new HashMap<String, GssoE01Datas>());
		this.setListOrderReference(new ArrayList<String>());
		this.setListOTP(new ArrayList<String>());
		this.setListReferenceNumber(new ArrayList<String>());
		this.setProfile(new GssoProfile());
		this.setMaplistWQuiryService(new HashMap<String, ArrayList<String>>());
		this.setMapInvokeOfRefund(new HashMap<String, Refund>());

		this.setListWaitInquirySub(new ArrayList<String>());

		this.setMapOrigInvokeEventDetailInput(new HashMap<String, String>());
		this.setMapOrigInvokeEventDetailOutput(new HashMap<String, String>());
		this.setMapOrigInvokeDetailScenario(new HashMap<String, String>());
		this.setMapOrigInvokeTransactionID(new HashMap<String, String>());

		this.setMapInvokeByOutputTime(new HashMap<String, Long>());
		this.setListDetailsLog(new ArrayList<MapDetailsAndConfigType>());
		this.setListSummaryLog(new ArrayList<SummaryLogPrototype>());
	}

	public ArrayList<String> getListInvokeProcessing() {
		return listInvokeProcessing;
	}

	public void setListInvokeProcessing(ArrayList<String> listInvokeProcessing) {
		this.listInvokeProcessing = listInvokeProcessing;
	}

	public HashMap<String, String> getMapInvokeOrig() {
		return mapInvokeOrig;
	}

	public void setMapInvokeOrig(HashMap<String, String> mapInvokeOrig) {
		this.mapInvokeOrig = mapInvokeOrig;
	}

	public HashMap<String, TimeoutCalculator> getMapTimeoutOfInvokePending() {
		return mapTimeoutOfInvokePending;
	}

	public void setMapTimeoutOfInvokePending(HashMap<String, TimeoutCalculator> mapTimeoutOfInvokePending) {
		this.mapTimeoutOfInvokePending = mapTimeoutOfInvokePending;
	}

	public HashMap<String, TimeoutCalculator> getMapTimeoutOfWaitDR() {
		return mapTimeoutOfWaitDR;
	}

	public void setMapTimeoutOfWaitDR(HashMap<String, TimeoutCalculator> mapTimeoutOfWaitDR) {
		this.mapTimeoutOfWaitDR = mapTimeoutOfWaitDR;
	}

	public HashMap<String, OrigInvokeProfile> getMapOrigProfile() {
		return mapOrigProfile;
	}

	public void setMapOrigProfile(HashMap<String, OrigInvokeProfile> mapOrigProfile) {
		this.mapOrigProfile = mapOrigProfile;
	}

	public HashMap<String, TransactionData> getTransactionidData() {
		return transactionidData;
	}

	public void setTransactionidData(HashMap<String, TransactionData> transactionidData) {
		this.transactionidData = transactionidData;
	}

	public ArrayList<String> getListOrderReference() {
		return listOrderReference;
	}

	public void setListOrderReference(ArrayList<String> listOrderReference) {
		this.listOrderReference = listOrderReference;
	}

	public ArrayList<String> getListOTP() {
		return listOTP;
	}

	public void setListOTP(ArrayList<String> listOTP) {
		this.listOTP = listOTP;
	}

	public ArrayList<String> getListReferenceNumber() {
		return listReferenceNumber;
	}

	public void setListReferenceNumber(ArrayList<String> listReferenceNumber) {
		this.listReferenceNumber = listReferenceNumber;
	}

	public HashMap<String, GssoE01Datas> getMapE01dataofService() {
		return mapE01dataofService;
	}

	public void setMapE01dataofService(HashMap<String, GssoE01Datas> mapE01dataofService) {
		this.mapE01dataofService = mapE01dataofService;
	}

	public HashMap<String, ArrayList<String>> getMaplistWQuiryService() {
		return maplistWQuiryService;
	}

	public void setMaplistWQuiryService(HashMap<String, ArrayList<String>> maplistWQuiryService) {
		this.maplistWQuiryService = maplistWQuiryService;
	}

	public GssoProfile getProfile() {
		return profile;
	}

	public void setProfile(GssoProfile profile) {
		this.profile = profile;
	}

	public long getTimeStampIncoming() {
		return timeStampIncoming;
	}

	public void setTimeStampIncoming(long timeStampIncoming) {
		this.timeStampIncoming = timeStampIncoming;
	}

	public long getTimeStampOutgoing() {
		return timeStampOutgoing;
	}

	public void setTimeStampOutgoing(long timeStampOutgoing) {
		this.timeStampOutgoing = timeStampOutgoing;
	}

	public HashMap<String, TimeoutCalculator> getMapTimeoutOfTransactionID() {
		return mapTimeoutOfTransactionID;
	}

	public void setMapTimeoutOfTransactionID(HashMap<String, TimeoutCalculator> mapTimeoutOfTransactionID) {
		this.mapTimeoutOfTransactionID = mapTimeoutOfTransactionID;
	}

	public boolean isWaitInquirySub() {
		return isWaitInquirySub;
	}

	public void setWaitInquirySub(boolean isWaitInquirySub) {
		this.isWaitInquirySub = isWaitInquirySub;
	}

	public boolean isWaitPortCheck() {
		return isWaitPortCheck;
	}

	public void setWaitPortCheck(boolean isWaitPortCheck) {
		this.isWaitPortCheck = isWaitPortCheck;
	}

	public ArrayList<String> getListWaitInquirySub() {
		return listWaitInqSub;
	}

	public void setListWaitInquirySub(ArrayList<String> listWaitInqSub) {
		this.listWaitInqSub = listWaitInqSub;
	}

	public boolean isInquirySubSuccess() {
		return isInquirySubSuccess;
	}

	public void setInquirySubSuccess(boolean isInquirySubSuccess) {
		this.isInquirySubSuccess = isInquirySubSuccess;
	}

	public HashMap<String, Long> getMapInvokeByOutputTime() {
		return mapInvokeByOutputTime;
	}

	public void setMapInvokeByOutputTime(HashMap<String, Long> mapInvokeByOutputTime) {
		this.mapInvokeByOutputTime = mapInvokeByOutputTime;
	}

	public HashMap<String, MapDestinationBean> getMapDestinationBean() {
		return mapDestinationBean;
	}

	public void setMapDestinationBean(HashMap<String, MapDestinationBean> mapDestinationBean) {
		this.mapDestinationBean = mapDestinationBean;
	}

	public ArrayList<MapDetailsAndConfigType> getListDetailsLog() {
		return listDetailsLog;
	}

	public void setListDetailsLog(ArrayList<MapDetailsAndConfigType> listDetailsLog) {
		this.listDetailsLog = listDetailsLog;
	}

	public ArrayList<SummaryLogPrototype> getListSummaryLog() {
		return listSummaryLog;
	}

	public void setListSummaryLog(ArrayList<SummaryLogPrototype> listSummaryLog) {
		this.listSummaryLog = listSummaryLog;
	}

	public HashMap<String, String> getMapOrigInvokeEventDetailInput() {
		return mapOrigInvokeEventDetailInput;
	}

	public void setMapOrigInvokeEventDetailInput(HashMap<String, String> mapOrigInvokeEventDetailInput) {
		this.mapOrigInvokeEventDetailInput = mapOrigInvokeEventDetailInput;
	}

	public HashMap<String, String> getMapOrigInvokeEventDetailOutput() {
		return mapOrigInvokeEventDetailOutput;
	}

	public void setMapOrigInvokeEventDetailOutput(HashMap<String, String> mapOrigInvokeEventDetailOutput) {
		this.mapOrigInvokeEventDetailOutput = mapOrigInvokeEventDetailOutput;
	}

	public HashMap<String, String> getMapOrigInvokeDetailScenario() {
		return mapOrigInvokeDetailScenario;
	}

	public void setMapOrigInvokeDetailScenario(HashMap<String, String> mapOrigInvokeDetailScenario) {
		this.mapOrigInvokeDetailScenario = mapOrigInvokeDetailScenario;
	}

	public String getOutgoingInvoke() {
		return outgoingInvoke;
	}

	public void setOutgoingInvoke(String outgoingInvoke) {
		this.outgoingInvoke = outgoingInvoke;
	}

	public String getOrigInvoke() {
		return origInvoke;
	}

	public void setOrigInvoke(String origInvoke) {
		this.origInvoke = origInvoke;
	}

	public String getOrig() {
		return orig;
	}

	public void setOrig(String orig) {
		this.orig = orig;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isInquiryVasSubscriber() {
		return isInquiryVasSubscriber;
	}

	public void setInquiryVasSubscriber(boolean isInquiryVasSubscriber) {
		this.isInquiryVasSubscriber = isInquiryVasSubscriber;
	}

	public HashMap<String, TimeoutCalculator> getMapTimeoutOfWaitRefund() {
		return mapTimeoutOfWaitRefund;
	}

	public void setMapTimeoutOfWaitRefund(HashMap<String, TimeoutCalculator> mapTimeoutOfWaitRefund) {
		this.mapTimeoutOfWaitRefund = mapTimeoutOfWaitRefund;
	}

	public HashMap<String, Refund> getMapInvokeOfRefund() {
		return mapInvokeOfRefund;
	}

	public void setMapInvokeOfRefund(HashMap<String, Refund> mapInvokeOfRefund) {
		this.mapInvokeOfRefund = mapInvokeOfRefund;
	}
	
	public int getRefundRetryLimit() {
		return refundRetryLimit;
	}

	public void setRefundRetryLimit(int refundRetryLimit) {
		this.refundRetryLimit = refundRetryLimit;
	}

	public void increaseRefundRetryLimit() {
		this.refundRetryLimit += 1;
	}

	public long getFirstTimeStampIncoming() {
		return firstTimeStampIncoming;
	}

	public void setFirstTimeStampIncoming(long firstTimeStampIncoming) {
		this.firstTimeStampIncoming = firstTimeStampIncoming;
	}

	public String getOrigCommand() {
		return origCommand;
	}

	public void setOrigCommand(String origCommand) {
		this.origCommand = origCommand;
	}

	public boolean isTimeoutOfConfirmReq() {
		return isTimeoutOfConfirmReq;
	}

	public void setTimeoutOfConfirmReq(boolean isTimeoutOfConfirmReq) {
		this.isTimeoutOfConfirmReq = isTimeoutOfConfirmReq;
	}

	public HashMap<String, String> getMapOrigInvokeTransactionID() {
		return mapOrigInvokeTransactionID;
	}

	public void setMapOrigInvokeTransactionID(HashMap<String, String> mapOrigInvokeTransactionID) {
		this.mapOrigInvokeTransactionID = mapOrigInvokeTransactionID;
	}

	public boolean isTransaction() {
		return isTransaction;
	}

	public void setTransaction(boolean isTransaction) {
		this.isTransaction = isTransaction;
	}


}