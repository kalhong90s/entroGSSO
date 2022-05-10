package com.ais.eqx.gsso.utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EquinoxEvent;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.interfaces.EquinoxAttribute;
import com.ais.eqx.gsso.interfaces.EventAction;
import com.ais.eqx.gsso.interfaces.EventCtype;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import ais.mmt.sand.comlog.DetailsLogPrototype;
import ais.mmt.sand.comlog.bean.DataBean;
import ais.mmt.sand.comlog.bean.DataBean.TYPE;
import ais.mmt.sand.comlog.exception.CommonLogException;
import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;
import ec02.common.data.E01Data;
import ec02.enums.EE01MessageType;

public class GssoComposeDetailsLog {

	private DetailsLogPrototype					detail;
	private EquinoxRawData						rawDataOrig;
	private HashMap<String, ArrayList<String>>	mapInitInvokeAndNextState;
	private String								scenario;
	private boolean								idleFlow			= false;
	private boolean								unknownFlow			= false;
	private long								detailTimeIncoming	= 0;
	private long								detailTimeOutgoing	= 0;

	public GssoComposeDetailsLog(final APPInstance appInstance, final String currentState, final AbstractAF abstractAF) {
		super();

		String appName = abstractAF.getEquinoxProperties().getApplicationName();
		String session = abstractAF.getEquinoxProperties().getSession();

		mapInitInvokeAndNextState = new HashMap<String, ArrayList<String>>();

		/** INITIATE APP-Detail **/
		this.detail = DetailsLogPrototype.getInstance(appName);
		this.detail.setSession(session);
		this.detail.setInputTimestamp(appInstance.getTimeStampIncoming());

		this.detail.setEnableData(ConfigureTool.isWriteLog(ConfigName.LOG_DETAIL_DATA));
		this.detail.setEnableRawData(ConfigureTool.isWriteLog(ConfigName.LOG_DETAIL_RAWDATA));

		this.detail.setEnableCurrentState(false);
		this.detail.setEnableNextState(false);
 	}

	public void setIdentity(final String identity) {
		
		try {
			if (identity == null || identity.isEmpty()) {
				this.detail.setIdentity("unknown");
			}
			else {
				this.detail.setIdentity(identity);
			}
		}
		catch (Exception e) {
			this.detail.setIdentity("unknown");
		}
		
	}
	
	public void setDataOrig(final String origInvoke, final EquinoxRawData equinoxRawData, final APPInstance appInstance) {

		this.rawDataOrig = new EquinoxRawData();
		try {
			this.rawDataOrig = appInstance.getMapOrigProfile().get(origInvoke).getOrigEquinoxRawData();
		}
		catch (Exception e) {
			this.rawDataOrig = equinoxRawData;
		}

	}

	public void initialIncoming(final EquinoxRawData equinoxRawData, final APPInstance appInstance) throws Exception {
		DataBean dataBean = new DataBean();
		String incomeType = equinoxRawData.getType();
		String event = "";
		long resTime = 0;
		String node;

		String val = "";
		if (equinoxRawData.getCType().equalsIgnoreCase(EventCtype.PLAIN)) {
			String rawDataString = equinoxRawData.getRawDataAttribute(EquinoxAttribute.VAL);
			try {
				val = (equinoxRawData.getRawDataAttribute(EquinoxAttribute.VAL) == null || equinoxRawData.getRawDataAttribute(
						EquinoxAttribute.VAL).isEmpty()) ? "" : rawDataString;
			}
			catch (Exception e) {
				val = rawDataString;
			}
		}
		else if (equinoxRawData.getCType().equalsIgnoreCase(EventCtype.XML)
				|| equinoxRawData.getCType().equalsIgnoreCase(EventCtype.DR)
					|| equinoxRawData.getCType().equalsIgnoreCase(EventCtype.SMS)) {
				String rawDataString = equinoxRawData.getRawDataMessage();
				try {
					val = (equinoxRawData.getRawDataMessage() == null || equinoxRawData.getRawDataMessage().isEmpty()) ? ""
							: rawDataString;
				}
				catch (Exception e) {
					val = rawDataString;
				}
			}
		else {
			val = "";
		}
		
		if(StringUtils.isNotEmpty(val)){
			val = val.replace("\"", "\\\"");
		}

		String incomeUrl = (equinoxRawData.getRawDataAttribute(EquinoxAttribute.URL) == null || equinoxRawData.getRawDataAttribute(
				EquinoxAttribute.URL).isEmpty()) ? "" : equinoxRawData.getRawDataAttribute(EquinoxAttribute.URL);

		String originInvoke;
		if (unknownFlow) {
			originInvoke = equinoxRawData.getInvoke();
		}
		else {
			originInvoke = InvokeFilter.getOriginInvoke(equinoxRawData.getInvoke());
		}
		if (appInstance.getMapOrigInvokeEventDetailInput().containsKey(originInvoke)) {
			try {
				String[] eventLog = appInstance.getMapOrigInvokeEventDetailInput().remove(originInvoke).split("\\.");

				node = eventLog[0];
				event = eventLog[1];
			}
			catch (Exception e) {
				node = "GSSO";
				event = "";
			}
		}
		else {
			try {
				String[] eventLog = appInstance.getMapOrigInvokeEventDetailInput().remove(val).split("\\.");

				node = eventLog[0];
				event = eventLog[1];
			}
			catch (Exception e) {
				node = "GSSO";
				event = "";
			}
		}

		TYPE type;
		JsonObject data = new JsonObject();
		EquinoxEvent e = EquinoxEvent.getEquinoxEventFrom(equinoxRawData.getRet());
		switch (e) {
		case NORMAL:
			type = (incomeType.toUpperCase().equals(EventAction.REQUEST.toUpperCase())) ? TYPE.REQ : TYPE.RES;

			if (equinoxRawData.getCType().equalsIgnoreCase(EventCtype.PLAIN)) {
				if (!incomeUrl.isEmpty()) {
					data.add(EquinoxAttribute.URL, new JsonPrimitive(incomeUrl));
				}
			}
			else if (equinoxRawData.getCType().equalsIgnoreCase(EventCtype.XML)) {
			}

			break;

		case REJECT:
			data.add("Ret", new JsonPrimitive("2"));
			data.add("Description", new JsonPrimitive("Reject"));
			resTime = System.currentTimeMillis();
			if (this.idleFlow) {
				type = TYPE.REQ;
			}
			else {
				type = TYPE.RES;
			}
			break;

		case ABORT:
			data.add("Ret", new JsonPrimitive("3"));
			data.add("Description", new JsonPrimitive("Abort"));
			resTime = System.currentTimeMillis();
			if (this.idleFlow) {
				type = TYPE.REQ;
			}
			else {
				type = TYPE.RES;
			}
			break;

		case ERROR:
			data.add("Ret", new JsonPrimitive("1"));
			data.add("Description", new JsonPrimitive("Error"));
			resTime = System.currentTimeMillis();
			if (this.idleFlow) {
				type = TYPE.REQ;
			}
			else {
				type = TYPE.RES;
			}
			break;

		case TIMEOUT:
			data.add("Ret", new JsonPrimitive("4"));
			data.add("Description", new JsonPrimitive("Timeout"));
			resTime = System.currentTimeMillis();
			if (this.idleFlow || appInstance.isTimeoutOfConfirmReq()) {
				type = TYPE.REQ;
			}
			else {
				type = TYPE.RES;
			}
			break;

		default:
			type = TYPE.REQ;
			break;

		}

		if (incomeType.toUpperCase().equals(EventAction.REQUEST.toUpperCase())
				&& (equinoxRawData.getRet().equals(EquinoxEvent.NORMAL.getCode()) || equinoxRawData.getRet().equals(
						EquinoxEvent.TIMEOUT.getCode()))) {
			if (equinoxRawData.getInvoke() == null || equinoxRawData.getInvoke().isEmpty()) {
				dataBean.setInvoke(equinoxRawData.getRawDataAttribute(EquinoxAttribute.VAL));
			}
			else {
				try {
					String[] invoke = equinoxRawData.getInvoke().split("@");
					dataBean.setInvoke(invoke[0]);
				}
				catch (Exception ex) {
					dataBean.setInvoke(equinoxRawData.getInvoke());
				}
			}
			dataBean.setDirector(node);
			dataBean.setCommandName(event);
			dataBean.setType(type);
			dataBean.setJsonData(data);
			dataBean.setRawData(val);
			this.setDetailTimeIncoming(appInstance.getTimeStampIncoming());
			this.detail.addInput(dataBean);
		}
		else {
			// RESPONSE TIME//
			try {
				resTime = appInstance.getTimeStampIncoming()
						- appInstance.getMapInvokeByOutputTime().remove(equinoxRawData.getInvoke());
			}
			catch (Exception e2) {
				resTime = System.currentTimeMillis();
			}

			if (equinoxRawData.getInvoke() == null || equinoxRawData.getInvoke().isEmpty()) {
				dataBean.setInvoke(equinoxRawData.getRawDataAttribute(EquinoxAttribute.VAL));
			}
			else {
				dataBean.setInvoke(equinoxRawData.getInvoke());
			}
			dataBean.setDirector(node);
			dataBean.setCommandName(event);
			dataBean.setType(type);
			dataBean.setJsonData(data);
			dataBean.setRawData(val);
			if (this.unknownFlow) {
				dataBean.setRespTime(1);
			}
			else {
				dataBean.setRespTime(resTime);
			}
			this.setDetailTimeIncoming(appInstance.getTimeStampIncoming());
			this.detail.addInput(dataBean);
		}
	}

	public void initialIncomingFromE01(final AbstractAF abstractAF, final APPInstance appInstance,
			final EquinoxRawData equinoxRawData, final E01Data e01Data) throws Exception {
		DataBean dataBean = new DataBean();
		String event = "";
		long resTime = 0;
		String node;
		String val = "";
		String invoke = null;

		String originInvoke = InvokeFilter.getOriginInvoke(e01Data.getId());
		if (originInvoke.toUpperCase().equals("default_invoke".toUpperCase())) {
			if (equinoxRawData.getInvoke() == null || equinoxRawData.getInvoke().isEmpty()) {
				invoke = e01Data.getId();
			}
			else {
				invoke = equinoxRawData.getInvoke();
			}
		}
		else {
			invoke = e01Data.getId();
		}

		try {
			val = e01Data.getData();
		}
		catch (Exception e) {
			val = "";
		}

		if (appInstance.getMapOrigInvokeEventDetailInput().containsKey(originInvoke)) {
			try {
				String[] eventLog = appInstance.getMapOrigInvokeEventDetailInput().remove(originInvoke).split("\\.");

				node = eventLog[0];
				event = eventLog[1];
			}
			catch (Exception e) {
				node = "GSSO";
				event = "";
			}
		}
		else {
			try {
				String[] eventLog = appInstance.getMapOrigInvokeEventDetailInput().remove(val).split("\\.");

				node = eventLog[0];
				event = eventLog[1];
			}
			catch (Exception e) {
				node = "GSSO";
				event = "";
			}
		}
		
		if(StringUtils.isNotEmpty(val)){
			val = val.replace("\"", "\\\"");
		}

		TYPE type;
		JsonObject data = new JsonObject();
		if (!e01Data.getId().toUpperCase().equals("default_invoke".toUpperCase())) {
			EE01MessageType r = abstractAF.getEquinoxUtils().getGlobalData().getGlobaldataMessageType();
			switch (r) {
			case R0:
				type = TYPE.RES;
				break;

			case R2:
				data.add("Ret", new JsonPrimitive("2"));
				data.add("Description", new JsonPrimitive("Reject"));
				resTime = System.currentTimeMillis();
				type = TYPE.RES;

				break;

			case R3:
				data.add("Ret", new JsonPrimitive("3"));
				data.add("Description", new JsonPrimitive("Abort"));
				resTime = System.currentTimeMillis();
				type = TYPE.RES;
				break;

			case R1:
				data.add("Ret", new JsonPrimitive("1"));
				data.add("Description", new JsonPrimitive("Error"));
				resTime = System.currentTimeMillis();
				type = TYPE.RES;
				break;

			case R4:
				data.add("Ret", new JsonPrimitive("4"));
				data.add("Description", new JsonPrimitive("Timeout"));
				resTime = System.currentTimeMillis();
				type = TYPE.RES;
				break;

			default:
				type = TYPE.RES;
				break;

			}
		}
		else {
			EquinoxEvent e = EquinoxEvent.getEquinoxEventFrom(equinoxRawData.getRet());
			switch (e) {
			case NORMAL:
				type = TYPE.RES;
				break;

			case REJECT:
				data.add("Ret", new JsonPrimitive("2"));
				data.add("Description", new JsonPrimitive("Reject"));
				resTime = System.currentTimeMillis();
				type = TYPE.RES;
				break;

			case ABORT:
				data.add("Ret", new JsonPrimitive("3"));
				data.add("Description", new JsonPrimitive("Abort"));
				resTime = System.currentTimeMillis();
				type = TYPE.RES;
				break;

			case ERROR:
				data.add("Ret", new JsonPrimitive("1"));
				data.add("Description", new JsonPrimitive("Error"));
				resTime = System.currentTimeMillis();
				type = TYPE.RES;
				break;

			case TIMEOUT:
				data.add("Ret", new JsonPrimitive("4"));
				data.add("Description", new JsonPrimitive("Timeout"));
				resTime = System.currentTimeMillis();
				type = TYPE.RES;
				break;

			default:
				type = TYPE.REQ;
				break;
			}
		}

		// RESPONSE TIME//
		try {
			resTime = appInstance.getTimeStampIncoming() - appInstance.getMapInvokeByOutputTime().remove(invoke);
		}
		catch (Exception e2) {
			resTime = System.currentTimeMillis();
		}

		dataBean.setInvoke(invoke);
		dataBean.setDirector(node);
		dataBean.setCommandName(event);
		dataBean.setType(type);
		dataBean.setJsonData(data);
		dataBean.setRawData(val);
		dataBean.setRespTime(resTime);
		this.setDetailTimeIncoming(appInstance.getTimeStampIncoming());
		this.detail.addInput(dataBean);

	}

	public void addScenarioFromE01(final APPInstance appInstance, final E01Data e01Data, final String currentState,
			final String nextState, EquinoxRawData equinoxRawData) {

		String origInvoke = "";
		String realInvoke = "";

		if (this.unknownFlow) {
			realInvoke = e01Data.getId();
			origInvoke = e01Data.getId();
		}
		else {
			try {
				if (e01Data.getId().toUpperCase().equals("default_invoke".toUpperCase())) {
					origInvoke = InvokeFilter.getOriginInvoke(equinoxRawData.getInvoke());
				}
				else {
					origInvoke = InvokeFilter.getOriginInvoke(e01Data.getId());
				}
			}
			catch (Exception e) {
				origInvoke = InvokeFilter.getOriginInvoke(e01Data.getId());
			}

			try {
				String[] invoke = origInvoke.split("-");
				realInvoke = invoke[0];
			}
			catch (Exception e) {
				realInvoke = origInvoke;
			}
			try {
				String[] invoke = realInvoke.split("@");
				realInvoke = invoke[0];
			}
			catch (Exception e) {
			}
		}

		this.rawDataOrig = new EquinoxRawData();
		try {
			this.rawDataOrig = appInstance.getMapOrigProfile().get(origInvoke).getOrigEquinoxRawData();
		}
		catch (Exception e) {
		}

		/** NORMAL **/
		if (appInstance.getMapOrigInvokeDetailScenario().get(origInvoke) != null
				&& !appInstance.getMapOrigInvokeDetailScenario().get(origInvoke).isEmpty()) {
			try {
				this.scenario = appInstance.getMapOrigInvokeDetailScenario().remove(origInvoke);
			}
			catch (Exception e) {
				this.scenario = "UNKNOWN";
			}
		}
		else {
			this.scenario = "UNKNOWN";
		}

		if (realInvoke.isEmpty()) {
			try {
				this.detail.addScenario(this.scenario, this.rawDataOrig.getInvoke(), currentState, nextState);
			}
			catch (CommonLogException e) {
				Log.e(e.getMessage());
			}
		}
		else {
			try {
				this.detail.addScenario(this.scenario, realInvoke, currentState, nextState);
			}
			catch (CommonLogException e) {
				Log.e(e.getMessage());
			}
		}
	}
	
	public void addScenario(final APPInstance appInstance, final EquinoxRawData equinoxRawData, final String nextState) {
		String origInvoke = "";
		String realInvoke = "";

		if (this.unknownFlow) {
			origInvoke = equinoxRawData.getInvoke();

			try {
				String[] invoke = equinoxRawData.getInvoke().split("@");
				realInvoke = invoke[0];

			}
			catch (Exception e) {
				realInvoke = equinoxRawData.getInvoke();
			}
		}
		else {
			origInvoke = InvokeFilter.getOriginInvoke(equinoxRawData.getInvoke());

			try {
				String[] invoke = origInvoke.split("-");
				realInvoke = invoke[0];

			}
			catch (Exception e) {
			}
			try {
				String[] invoke = realInvoke.split("@");
				realInvoke = invoke[0];
			}
			catch (Exception e) {
			}
		}

		this.rawDataOrig = new EquinoxRawData();
		try {
			this.rawDataOrig = appInstance.getMapOrigProfile().get(origInvoke).getOrigEquinoxRawData();
		}
		catch (Exception e) {
			this.rawDataOrig = equinoxRawData;
		}

		/** NORMAL **/
		if (appInstance.getMapOrigInvokeDetailScenario().get(origInvoke) != null
				&& !appInstance.getMapOrigInvokeDetailScenario().get(origInvoke).isEmpty()) {
			try {
				this.scenario = appInstance.getMapOrigInvokeDetailScenario().remove(origInvoke);
			}
			catch (Exception e) {
				this.scenario = "UNKNOWN";
			}
		}
		else {
			this.scenario = "UNKNOWN";
		}

		if (realInvoke.isEmpty()) {
			if (equinoxRawData.getCType().equalsIgnoreCase(EventCtype.PLAIN)) {
				if (!equinoxRawData.getRawDataAttribute(EquinoxAttribute.VAL).isEmpty()) {
					String val = equinoxRawData.getRawDataAttribute(EquinoxAttribute.VAL);
					/** FOUND INIT INVOKE **/
					if (mapInitInvokeAndNextState.get(val) != null) {
						if (!mapInitInvokeAndNextState.get(val).contains(nextState)) {
							mapInitInvokeAndNextState.get(val).add(nextState);
							try {
								this.detail.addScenario(this.scenario, val, "", "");
							}
							catch (CommonLogException e) {
								Log.e(e.getMessage());
							}
						}
					}
					/** INIT INVOKE NOT FOUND **/
					else {
						/** NEW AND ADD VALUE **/
						mapInitInvokeAndNextState.put(val, new ArrayList<String>());
						mapInitInvokeAndNextState.get(val).add(nextState);
						try {
							this.detail.addScenario(this.scenario, val, "", "");
						}
						catch (CommonLogException e) {
							Log.e(e.getMessage());
						}
					}
				}
			}
			else if (equinoxRawData.getCType().equalsIgnoreCase(EventCtype.XML)) {
				if (!equinoxRawData.getRawDataMessage().isEmpty()) {
					String val = equinoxRawData.getRawDataMessage();
					/** FOUND INIT INVOKE **/
					if (mapInitInvokeAndNextState.get(val) != null) {
						if (!mapInitInvokeAndNextState.get(val).contains(nextState)) {
							mapInitInvokeAndNextState.get(val).add(nextState);
							try {
								this.detail.addScenario(this.scenario, val, "", "");
							}
							catch (CommonLogException e) {
								Log.e(e.getMessage());
							}
						}
					}
					/** INIT INVOKE NOT FOUND **/
					else {
						/** NEW AND ADD VALUE **/
						mapInitInvokeAndNextState.put(val, new ArrayList<String>());
						mapInitInvokeAndNextState.get(val).add(nextState);
						try {
							this.detail.addScenario(this.scenario, val, "", "");
						}
						catch (CommonLogException e) {
							Log.e(e.getMessage());
						}
					}
				}
			}
		}
		else {
			try {
				this.detail.addScenario(this.scenario, realInvoke, "", "");
			}
			catch (CommonLogException e) {
				Log.e(e.getMessage());
			}
		}
	}

	public void initialOutgoing(final EquinoxRawData equinoxRawData, final APPInstance appInstance, final int outPutSize)
			throws Exception {
		DataBean dataBean = new DataBean();
		String val = "";
		String event = "";
		String node;

		if (equinoxRawData.getCType().equalsIgnoreCase(EventCtype.PLAIN)) {
			String rawDataString = equinoxRawData.getRawDataAttribute(EquinoxAttribute.VAL);
			try {
				val = (equinoxRawData.getRawDataAttribute(EquinoxAttribute.VAL) == null || equinoxRawData.getRawDataAttribute(
						EquinoxAttribute.VAL).isEmpty()) ? "" : rawDataString;
			}
			catch (Exception e) {
				val = rawDataString;
			}
		}
		else if (equinoxRawData.getCType().equalsIgnoreCase(EventCtype.XML)
				|| equinoxRawData.getCType().equalsIgnoreCase(EventCtype.DR)
				|| equinoxRawData.getCType().equalsIgnoreCase(EventCtype.SMS)) {
			String rawDataString = equinoxRawData.getRawDataMessage();
			try {
				val = (equinoxRawData.getRawDataMessage() == null || equinoxRawData.getRawDataMessage().isEmpty()) ? ""
						: rawDataString;
			}
			catch (Exception e) {
				val = rawDataString;
			}
		}
		else {
			val = "";
		}

		String outgoUrl = (equinoxRawData.getRawDataAttribute(EquinoxAttribute.URL) == null || equinoxRawData.getRawDataAttribute(
				EquinoxAttribute.URL).isEmpty()) ? "" : equinoxRawData.getRawDataAttribute(EquinoxAttribute.URL);

		String origInvoke = InvokeFilter.getOriginInvoke(equinoxRawData.getInvoke());

		if (appInstance.getMapOrigInvokeEventDetailOutput().containsKey(equinoxRawData.getInvoke())) {
			try {
				String[] eventLog = appInstance.getMapOrigInvokeEventDetailOutput().remove(equinoxRawData.getInvoke()).split("\\.");

				node = eventLog[0];
				event = eventLog[1];
			}
			catch (Exception e) {
				node = "GSSO";
				event = "";
			}
		}
		else {
			node = "GSSO";
			event = "";
		}
		
		if(StringUtils.isNotEmpty(val)){
			val = val.replace("\"", "\\\"");
		}

		TYPE type = (equinoxRawData.getType().toUpperCase().equals(EventAction.REQUEST.toUpperCase())) ? TYPE.REQ : TYPE.RES;

		JsonObject data = new JsonObject();

		if (equinoxRawData.getCType().equalsIgnoreCase(EventCtype.PLAIN)) {
			if (!outgoUrl.isEmpty()) {
				data.add(EquinoxAttribute.URL, new JsonPrimitive(outgoUrl));
			}
		}
		else if (equinoxRawData.getCType().equalsIgnoreCase(EventCtype.XML)) {
		}

		if (outPutSize > 1) {
			try {
				String[] invoke = origInvoke.split("-");
				origInvoke = invoke[0];
			}
			catch (Exception e) {
			}
			try {
				String[] invoke = origInvoke.split("@");
				origInvoke = invoke[0];
			}
			catch (Exception e) {
			}
			dataBean.setInitialInvoke(origInvoke);
		}

		if (equinoxRawData.getType().toUpperCase().equals(EventAction.REQUEST.toUpperCase())) {
			dataBean.setInvoke(equinoxRawData.getInvoke());
		}
		else {
			try {
				String[] invoke = equinoxRawData.getInvoke().split("@");
				dataBean.setInvoke(invoke[0]);
			}
			catch (Exception e) {
				dataBean.setInvoke(equinoxRawData.getInvoke());
			}
		}
		dataBean.setDirector(node);
		dataBean.setCommandName(event);
		dataBean.setType(type);
		dataBean.setJsonData(data);
		if (equinoxRawData.getCType().equalsIgnoreCase(EventCtype.PLAIN)) {
			dataBean.setRawData(val);
		}
		else if (equinoxRawData.getCType().equalsIgnoreCase(EventCtype.XML)
				|| equinoxRawData.getCType().equalsIgnoreCase(EventCtype.DR)
				|| equinoxRawData.getCType().equalsIgnoreCase(EventCtype.SMS)) {
			dataBean.setRawData(val);
		}
		this.setDetailTimeOutgoing(System.currentTimeMillis());
		this.detail.addOutput(dataBean);
	}

	public void initialOutgoingToE01(final AbstractAF abstractAF, final APPInstance appInstance) throws Exception {
		DataBean dataBean = new DataBean();
		String val = "";
		String event = "";
		String node;

		/** ADD NEW TIME E01 OUT **/
		if (abstractAF.getEquinoxUtils().getDataBuffer().getE01Commands().size() != 0) {
			/** SET OUT TIME FOR REQ **/
			for (E01Data e01Data : abstractAF.getEquinoxUtils().getDataBuffer().getE01Commands()) {
				dataBean = new DataBean();
				String origInvoke = InvokeFilter.getOriginInvoke(e01Data.getId());

				if (appInstance.getMapOrigInvokeEventDetailOutput().containsKey(e01Data.getId())) {
					try {
						String[] eventLog = appInstance.getMapOrigInvokeEventDetailOutput().remove(e01Data.getId()).split("\\.");

						node = eventLog[0];
						event = eventLog[1];

					}
					catch (Exception e) {
						node = "GSSO";
						event = "";
					}
				}
				else {
					node = "GSSO";
					event = "";
				}
				
				if(StringUtils.isNotEmpty(val)){
					val = val.replace("\"", "\\\"");
				}

				TYPE type = TYPE.REQ;

				JsonObject data = new JsonObject();

				try {
					String[] invoke = origInvoke.split("-");
					origInvoke = invoke[0];
				}
				catch (Exception e) {
				}
				try {
					String[] invoke = origInvoke.split("@");
					origInvoke = invoke[0];
				}
				catch (Exception e) {
				}
				dataBean.setInitialInvoke(origInvoke);
				dataBean.setInvoke(e01Data.getId());
				dataBean.setDirector(node);
				dataBean.setCommandName(event);
				dataBean.setType(type);
				dataBean.setJsonData(data);
				dataBean.setRawData(val);
				this.setDetailTimeOutgoing(System.currentTimeMillis());
				this.detail.addOutput(dataBean);

			}
		}
	}

	public DetailsLogPrototype getDetailsLog() {
		return this.detail;
	}

	public void thisIdleState() {
		this.idleFlow = true;
	}

	public void thisUnknownState() {
		this.unknownFlow = true;
	}

	public long getDetailTimeIncoming() {
		return detailTimeIncoming;
	}

	public void setDetailTimeIncoming(final long detailTimeIncoming) {
		this.detailTimeIncoming = detailTimeIncoming;
	}

	public long getDetailTimeOutgoing() {
		return detailTimeOutgoing;
	}

	public void setDetailTimeOutgoing(final long detailTimeOutgoing) {
		this.detailTimeOutgoing = detailTimeOutgoing;
	}

}
