package com.ais.eqx.gsso.utils;

import java.util.ArrayList;
import java.util.Map;

import com.ais.eqx.gsso.enums.ConfigName;

public class ConfigureTool {

	private static Map<String, ArrayList<String>>	warmConfig;

	private ConfigureTool() {

	}

	public static void initConfigureTool(Map<String, ArrayList<String>> hmWarmConfig) {
		warmConfig = hmWarmConfig;
	}

	public static String getConfigure(ConfigName configNameLog) {
		try {
			return warmConfig.get(configNameLog.getName()).get(0);
		}
		catch (Exception e) {
			return null;
		}
	}

	public static String getConfigureBoolean(ConfigName configNameLog) {
		try {
			if ("true".equalsIgnoreCase(warmConfig.get(configNameLog.getName()).get(0))) {
				return "true";
			}
			else {
				return "false";
			}
		}
		catch (Exception e) {
			return "false";
		}
	}

	public static int getConfigureInteger(ConfigName configNameLog) {
		try {
			return Integer.parseInt(warmConfig.get(configNameLog.getName()).get(0));
		}
		catch (Exception e) {
			return 0;
		}
	}

	public static String getConfigureSMPP(ConfigName configNameLog) {
		try {
			return warmConfig.get(configNameLog.getName()).get(0);
		}
		catch (Exception e) {
			return "NULL";
		}
	}

	public static ArrayList<String> getConfigureArray(ConfigName configName) {
		try {
			return warmConfig.get(configName.getName());
		}
		catch (Exception e) {
			return null;
		}
	}

	public static String getConfigureLogName(String logName) {
		try {
			return warmConfig.get(logName).get(0);
		}
		catch (Exception e) {
			return logName;
		}
	}

	public static boolean isWriteLog(ConfigName configNameLog) {
		try {
			String isWrite = getConfigure(configNameLog);
			if ("true".equalsIgnoreCase(isWrite)) {
				return true;
			}
			else {
				return false;
			}
		}
		catch (Exception e) {
			return false;
		}
	}

	public static int isWriteLogDetails(ConfigName configNameLog) {
		String isWrite = getConfigure(configNameLog);
		if ("0".equals(isWrite)) {
			return 0;
		}
		else if ("1".equals(isWrite)) {
			return 1;
		}
		else if ("2".equals(isWrite)) {
			return 2;
		}
		else {
			return 0;
		}
	}

}
