package com.ais.eqx.gsso.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.exception.ValidationException;
import com.ais.eqx.gsso.instances.InquirySubscriber;
import com.ais.eqx.gsso.instances.InquiryVASSubscriber;
import com.ais.eqx.gsso.instances.ListOfHop;
import com.ais.eqx.gsso.instances.PortCheckResponse;
import com.ais.eqx.gsso.interfaces.USMPCode;
import com.ais.eqx.gsso.utils.ConfigureTool;
import com.ais.eqx.gsso.utils.GssoDataManagement;

public class VerifyUSMPMessage {

	public static void inquiryVASSubscriberValidator(final InquiryVASSubscriber inquiryVASSubscriber, final String rawMessage) throws ValidationException {

		String mandatoryPath = null;
		String isNull = "Null";
		String isMissing = "Missing";
		String isInvalid = "Invalid";

		String rawMessageWithoutSpace = rawMessage.replaceAll("\\s","");
//		old code
//		mandatoryPath = "InquirySubscriberResponse";
		mandatoryPath = "InquiryVASSubscriberResponse";
		
//		if (!rawMessageWithoutSpace.contains("InquirySubscriberResponse"))
		if (!rawMessageWithoutSpace.contains("InquiryVASSubscriberResponse")) {
			throw new ValidationException(mandatoryPath, isMissing, isMissing);
		}
		
//		mandatoryPath = "InquirySubscriberResult";
		mandatoryPath = "InquiryVASSubscriberResult";
		if (!rawMessageWithoutSpace.contains("InquiryVASSubscriberResult")) {
			throw new ValidationException(mandatoryPath, isMissing, isMissing);
		}
		
		mandatoryPath = "OperationStatus";
		if (!rawMessageWithoutSpace.contains("</OperationStatus>") && !rawMessageWithoutSpace.contains("<OperationStatus>")) {
			throw new ValidationException(mandatoryPath, isMissing, isMissing);
		}
		
//		mandatoryPath = "InquirySubscriberResponse";
		mandatoryPath = "InquiryVASSubscriberResponse";
		if (inquiryVASSubscriber == null) {

			throw new ValidationException(mandatoryPath, isMissing, isMissing);
		}
		else {

//			mandatoryPath = "inquirySubscriberResult";
			mandatoryPath = "InquiryVASSubscriberResult";
			if (inquiryVASSubscriber.getInquiryVASSubscriberResult() == null) {
				throw new ValidationException(mandatoryPath, isMissing, isMissing);
			}
			else {
				mandatoryPath = "OperationStatus";
				if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getOperationStatus() == null) {
					throw new ValidationException(mandatoryPath, isMissing, isMissing);
				}
				else {
					/** IsSuccess **/
					mandatoryPath = "IsSuccess";
					if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getOperationStatus().getIsSuccess() == null) {
						throw new ValidationException(mandatoryPath, isMissing, isMissing);
					}
					else {
						if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getOperationStatus().getIsSuccess().isEmpty()) {
							throw new ValidationException(mandatoryPath, isMissing, isNull);
						}
					}
					
					/** Code **/
					mandatoryPath = "Code";
					if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getOperationStatus().getCode() == null) {
						throw new ValidationException(mandatoryPath, isMissing, isMissing);
					}
					else {
						if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getOperationStatus().getCode().isEmpty()) {
							throw new ValidationException(mandatoryPath, isNull, isNull);
						}
					}

					/** Description **/
					mandatoryPath = "Description";
					if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getOperationStatus().getDescription() == null) {

						throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
								.getOperationStatus().getCode(), isMissing);
					}
					else {

						if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getOperationStatus().getDescription().isEmpty()) {

							throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
									.getOperationStatus().getCode(), isNull);
						}
					}

					/** TransactionID **/
					mandatoryPath = "TransactionID";
					if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getOperationStatus().getTransactionID() == null) {

						throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
								.getOperationStatus().getCode(), isMissing);
					}
					else {

						if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getOperationStatus().getTransactionID().isEmpty()) {

							throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
									.getOperationStatus().getCode(), isNull);
						}
					}

					/** OrderRef **/
					mandatoryPath = "OrderRef";
					if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getOperationStatus().getOrderRef() == null) {

						throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
								.getOperationStatus().getCode(), isMissing);
					}
					else {

						if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getOperationStatus().getOrderRef().isEmpty()) {

							throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
									.getOperationStatus().getCode(), isNull);
						}
					}
				}

				if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getOperationStatus().getCode() != null
						&& inquiryVASSubscriber.getInquiryVASSubscriberResult().getOperationStatus().getCode()
								.equals(USMPCode.VSMP_00000000)) {

					mandatoryPath = "Subscriber";
					if (!rawMessageWithoutSpace.contains("</Subscriber>") && !rawMessageWithoutSpace.contains("<Subscriber>")) {
						throw new ValidationException(mandatoryPath, isMissing, isMissing);
					}
					
					mandatoryPath = "Subscriber";
					if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber() == null) {
						throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
								.getOperationStatus().getCode(), isMissing);
					}
					else {

						if (!inquiryVASSubscriber.getInquiryVASSubscriberResult().getOperationStatus().getCode()
								.equals(USMPCode.VSMP_00000000)) {

						}
						else {
							/************* NEW ***************/
							/** Msisdn **/
//							mandatoryPath = "Msisdn";
							mandatoryPath = "msisdn";
							int msisdnDigitLength = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MSISDN_DIGIT_LENGTH));
							String[] countryCodeList = GssoDataManagement.configToArray(ConfigureTool
									.getConfigure(ConfigName.DOMESTIC_COUNTRY_CODE_LIST));
							if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber().getMsisdn() == null)
								throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
										.getOperationStatus().getCode(), isMissing);

							else if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber().getMsisdn().isEmpty())
								throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
										.getOperationStatus().getCode(), isNull);

							else if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber().getMsisdn().length() < msisdnDigitLength)
								throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
										.getOperationStatus().getCode(), isInvalid);

							String msisdn = inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber().getMsisdn();
							Pattern typePatternMsisdn = null;
							Matcher typeMatcherMsisdn = null;
							boolean foundCountry = false;
							try {
								for (String countryCode : countryCodeList) {
									String msisdnPrefix = msisdn.substring(0, countryCode.length());
									if (msisdnPrefix.contains(countryCode)) {

										String realMsisdn = msisdn.substring(countryCode.length(), msisdn.length());

										String msisdnPattern = "[0-9]+";
										typePatternMsisdn = Pattern.compile(msisdnPattern);
										typeMatcherMsisdn = typePatternMsisdn.matcher(realMsisdn);
										if (typeMatcherMsisdn.matches() && realMsisdn.length() == msisdnDigitLength) {
											foundCountry = true;
											break;
										}
										else {
											throw new ValidationException(mandatoryPath, inquiryVASSubscriber
													.getInquiryVASSubscriberResult().getOperationStatus().getCode(), isInvalid);
										}
									}
								}
							}
							catch (Exception e) {
								throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
										.getOperationStatus().getCode(), isInvalid);
							}
							if (!foundCountry) {
								throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
										.getOperationStatus().getCode(), isInvalid);
							}

							/** CustomerID **/
//							mandatoryPath = "CustomerID";
							mandatoryPath = "customerId";
							if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber().getCustomerId() == null) {

								throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
										.getOperationStatus().getCode(), isMissing);
							}
							else {

								if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber().getCustomerId().isEmpty()) {

									throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
											.getOperationStatus().getCode(), isNull);
								}
//								else {
//									String msisdnPattern = "[6][6][0-9]{13}";
//									Pattern typePattern = Pattern.compile(msisdnPattern);
//
//									if (!typePattern.matcher(
//											inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber().getCustomerId())
//											.matches()) {
//
//										throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
//												.getOperationStatus().getCode(), isInvalid);
//									}
//								}
							}

							/** State **/
//							mandatoryPath = "State";
							mandatoryPath = "state";
							if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber().getState() == null) {

								throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
										.getOperationStatus().getCode(), isMissing);
							}
							else {

								if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber().getState().isEmpty()) {

									throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
											.getOperationStatus().getCode(), isNull);
								}
								else {

									try {
										int i = Integer.parseInt(inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber()
												.getState());
										if (!(i >= 0 && i <= 99)) {

											throw new ValidationException(mandatoryPath, inquiryVASSubscriber
													.getInquiryVASSubscriberResult().getOperationStatus().getCode(), isInvalid);
										}

									}
									catch (Exception e) {
										throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
												.getOperationStatus().getCode(), isInvalid);
									}

								}
							}

							/** Lang **/
//							mandatoryPath = "Lang";
							mandatoryPath = "language";
							if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber().getLanguage() == null) {

								throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
										.getOperationStatus().getCode(), isMissing);
							}
							else {

								if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber().getLanguage().isEmpty()) {

									throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
											.getOperationStatus().getCode(), isNull);
								}
								else {

									String numberPattern = "-?\\d+(\\.\\d+)?";
									Pattern typePattern = Pattern.compile(numberPattern);

									if (!typePattern.matcher(
											inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber().getLanguage()).matches()) {

										throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
												.getOperationStatus().getCode(), isInvalid);
									}

								}
							}

							/** Cos **/
//							mandatoryPath = "Cos";
							mandatoryPath = "cos";
							if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber().getCos() == null) {

								throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
										.getOperationStatus().getCode(), isMissing);
							}
							else {

								if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber().getCos().isEmpty()) {

									throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
											.getOperationStatus().getCode(), isNull);
								}
							}

							/** spName **/
							mandatoryPath = "spName";
							if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber().getSpName() == null) {

								throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
										.getOperationStatus().getCode(), isMissing);
							}
							else {

								if (inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber().getSpName().isEmpty()) {

									throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
											.getOperationStatus().getCode(), isNull);
								}
								else {

									if (!(inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber().getSpName().length() <= 15)) {
										throw new ValidationException(mandatoryPath, inquiryVASSubscriber.getInquiryVASSubscriberResult()
												.getOperationStatus().getCode(), isInvalid);
									}

								}
							}
						}
					}
				}
			}
		}
	}

	public static void inquirySubscriberValidator(final InquirySubscriber inquirySubscriber, final String rawMessage) throws ValidationException {

		String mandatoryPath = null;
		String isNull = "Null";
		String isMissing = "Missing";
		String isInvalid = "Invalid";

		String rawMessageWithoutSpace = rawMessage.replaceAll("\\s","");
//		old code
		mandatoryPath = "InquirySubscriberResponse";
		
		if (!rawMessageWithoutSpace.contains("InquirySubscriberResponse")) {
			throw new ValidationException(mandatoryPath, isMissing, isMissing);
		}
		
		mandatoryPath = "InquirySubscriberResult";
		if (!rawMessageWithoutSpace.contains("InquirySubscriberResult")) {
			throw new ValidationException(mandatoryPath, isMissing, isMissing);
		}
		
		mandatoryPath = "OperationStatus";
		if (!rawMessageWithoutSpace.contains("</OperationStatus>") && !rawMessageWithoutSpace.contains("<OperationStatus>")) {
			throw new ValidationException(mandatoryPath, isMissing, isMissing);
		}
		
		mandatoryPath = "InquirySubscriberResponse";
		if (inquirySubscriber == null) {

			throw new ValidationException(mandatoryPath, isMissing, isMissing);
		}
		else {

			mandatoryPath = "InquirySubscriberResult";
			if (inquirySubscriber.getInquirySubscriberResult() == null) {
				throw new ValidationException(mandatoryPath, isMissing, isMissing);
			}
			else {
				mandatoryPath = "OperationStatus";
				if (inquirySubscriber.getInquirySubscriberResult().getOperationStatus() == null) {
					throw new ValidationException(mandatoryPath, isMissing, isMissing);
				}
				else {
					/** IsSuccess **/
					mandatoryPath = "IsSuccess";
					if (inquirySubscriber.getInquirySubscriberResult().getOperationStatus().getIsSuccess() == null) {
						throw new ValidationException(mandatoryPath, isMissing, isMissing);
					}
					else {
						if (inquirySubscriber.getInquirySubscriberResult().getOperationStatus().getIsSuccess().isEmpty()) {
							throw new ValidationException(mandatoryPath, isMissing, isNull);
						}
					}
					
					/** Code **/
					mandatoryPath = "Code";
					if (inquirySubscriber.getInquirySubscriberResult().getOperationStatus().getCode() == null) {
						throw new ValidationException(mandatoryPath, isMissing, isMissing);
					}
					else {
						if (inquirySubscriber.getInquirySubscriberResult().getOperationStatus().getCode().isEmpty()) {
							throw new ValidationException(mandatoryPath, isNull, isNull);
						}
					}

					/** Description **/
					mandatoryPath = "Description";
					if (inquirySubscriber.getInquirySubscriberResult().getOperationStatus().getDescription() == null) {

						throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
								.getOperationStatus().getCode(), isMissing);
					}
					else {

						if (inquirySubscriber.getInquirySubscriberResult().getOperationStatus().getDescription().isEmpty()) {

							throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
									.getOperationStatus().getCode(), isNull);
						}
					}

					/** TransactionID **/
					mandatoryPath = "TransactionID";
					if (inquirySubscriber.getInquirySubscriberResult().getOperationStatus().getTransactionID() == null) {

						throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
								.getOperationStatus().getCode(), isMissing);
					}
					else {

						if (inquirySubscriber.getInquirySubscriberResult().getOperationStatus().getTransactionID().isEmpty()) {

							throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
									.getOperationStatus().getCode(), isNull);
						}
					}

					/** OrderRef **/
					mandatoryPath = "OrderRef";
					if (inquirySubscriber.getInquirySubscriberResult().getOperationStatus().getOrderRef() == null) {

						throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
								.getOperationStatus().getCode(), isMissing);
					}
					else {

						if (inquirySubscriber.getInquirySubscriberResult().getOperationStatus().getOrderRef().isEmpty()) {

							throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
									.getOperationStatus().getCode(), isNull);
						}
					}
				}

				if (inquirySubscriber.getInquirySubscriberResult().getOperationStatus().getCode() != null
						&& inquirySubscriber.getInquirySubscriberResult().getOperationStatus().getCode()
								.equals(USMPCode.VSMP_00000000)) {

					mandatoryPath = "Subscriber";
					if (!rawMessageWithoutSpace.contains("</Subscriber>") && !rawMessageWithoutSpace.contains("<Subscriber>")) {
						throw new ValidationException(mandatoryPath, isMissing, isMissing);
					}
					
					mandatoryPath = "Subscriber";
					if (inquirySubscriber.getInquirySubscriberResult().getSubscriber() == null) {
						throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
								.getOperationStatus().getCode(), isMissing);
					}
					else {

						if (!inquirySubscriber.getInquirySubscriberResult().getOperationStatus().getCode()
								.equals(USMPCode.VSMP_00000000)) {

						}
						else {
							/************* NEW ***************/
							/** Msisdn **/
							mandatoryPath = "Msisdn";
							int msisdnDigitLength = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MSISDN_DIGIT_LENGTH));
							String[] countryCodeList = GssoDataManagement.configToArray(ConfigureTool
									.getConfigure(ConfigName.DOMESTIC_COUNTRY_CODE_LIST));
							if (inquirySubscriber.getInquirySubscriberResult().getSubscriber().getMsisdn() == null)
								throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
										.getOperationStatus().getCode(), isMissing);

							else if (inquirySubscriber.getInquirySubscriberResult().getSubscriber().getMsisdn().isEmpty())
								throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
										.getOperationStatus().getCode(), isNull);

							else if (inquirySubscriber.getInquirySubscriberResult().getSubscriber().getMsisdn().length() < msisdnDigitLength)
								throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
										.getOperationStatus().getCode(), isInvalid);

							String msisdn = inquirySubscriber.getInquirySubscriberResult().getSubscriber().getMsisdn();
							Pattern typePatternMsisdn = null;
							Matcher typeMatcherMsisdn = null;
							boolean foundCountry = false;
							try {
								for (String countryCode : countryCodeList) {
									String msisdnPrefix = msisdn.substring(0, countryCode.length());
									if (msisdnPrefix.contains(countryCode)) {

										String realMsisdn = msisdn.substring(countryCode.length(), msisdn.length());

										String msisdnPattern = "[0-9]+";
										typePatternMsisdn = Pattern.compile(msisdnPattern);
										typeMatcherMsisdn = typePatternMsisdn.matcher(realMsisdn);
										if (typeMatcherMsisdn.matches() && realMsisdn.length() == msisdnDigitLength) {
											foundCountry = true;
											break;
										}
										else {
											throw new ValidationException(mandatoryPath, inquirySubscriber
													.getInquirySubscriberResult().getOperationStatus().getCode(), isInvalid);
										}
									}
								}
							}
							catch (Exception e) {
								throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
										.getOperationStatus().getCode(), isInvalid);
							}
							if (!foundCountry) {
								throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
										.getOperationStatus().getCode(), isInvalid);
							}

							/** CustomerID **/
							mandatoryPath = "CustomerID";
							if (inquirySubscriber.getInquirySubscriberResult().getSubscriber().getCustomerId() == null) {

								throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
										.getOperationStatus().getCode(), isMissing);
							}
							else {

								if (inquirySubscriber.getInquirySubscriberResult().getSubscriber().getCustomerId().isEmpty()) {

									throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
											.getOperationStatus().getCode(), isNull);
								}
//								else {
//									String msisdnPattern = "[6][6][0-9]{13}";
//									Pattern typePattern = Pattern.compile(msisdnPattern);
//
//									if (!typePattern.matcher(
//											inquirySubscriber.getInquirySubscriberResult().getSubscriber().getCustomerId())
//											.matches()) {
//
//										throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
//												.getOperationStatus().getCode(), isInvalid);
//									}
//								}
							}

							/** State **/
							mandatoryPath = "State";
							if (inquirySubscriber.getInquirySubscriberResult().getSubscriber().getState() == null) {

								throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
										.getOperationStatus().getCode(), isMissing);
							}
							else {

								if (inquirySubscriber.getInquirySubscriberResult().getSubscriber().getState().isEmpty()) {

									throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
											.getOperationStatus().getCode(), isNull);
								}
								else {

									try {
										int i = Integer.parseInt(inquirySubscriber.getInquirySubscriberResult().getSubscriber()
												.getState());
										if (!(i >= 0 && i <= 99)) {

											throw new ValidationException(mandatoryPath, inquirySubscriber
													.getInquirySubscriberResult().getOperationStatus().getCode(), isInvalid);
										}

									}
									catch (Exception e) {
										throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
												.getOperationStatus().getCode(), isInvalid);
									}

								}
							}

							/** Lang **/
							mandatoryPath = "Lang";
							if (inquirySubscriber.getInquirySubscriberResult().getSubscriber().getLanguage() == null) {

								throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
										.getOperationStatus().getCode(), isMissing);
							}
							else {

								if (inquirySubscriber.getInquirySubscriberResult().getSubscriber().getLanguage().isEmpty()) {

									throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
											.getOperationStatus().getCode(), isNull);
								}
								else {

									String numberPattern = "-?\\d+(\\.\\d+)?";
									Pattern typePattern = Pattern.compile(numberPattern);

									if (!typePattern.matcher(
											inquirySubscriber.getInquirySubscriberResult().getSubscriber().getLanguage()).matches()) {

										throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
												.getOperationStatus().getCode(), isInvalid);
									}

								}
							}

							/** Cos **/
							mandatoryPath = "Cos";
							if (inquirySubscriber.getInquirySubscriberResult().getSubscriber().getCos() == null) {

								throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
										.getOperationStatus().getCode(), isMissing);
							}
							else {

								if (inquirySubscriber.getInquirySubscriberResult().getSubscriber().getCos().isEmpty()) {

									throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
											.getOperationStatus().getCode(), isNull);
								}
							}

							/** spName **/
							mandatoryPath = "spName";
							if (inquirySubscriber.getInquirySubscriberResult().getSubscriber().getSpName() == null) {

								throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
										.getOperationStatus().getCode(), isMissing);
							}
							else {

								if (inquirySubscriber.getInquirySubscriberResult().getSubscriber().getSpName().isEmpty()) {

									throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
											.getOperationStatus().getCode(), isNull);
								}
								else {

									if (!(inquirySubscriber.getInquirySubscriberResult().getSubscriber().getSpName().length() <= 15)) {
										throw new ValidationException(mandatoryPath, inquirySubscriber.getInquirySubscriberResult()
												.getOperationStatus().getCode(), isInvalid);
									}

								}
							}
						}
					}
				}
			}
		}
	}

	public static void portCheckValidator(final PortCheckResponse portCheckResponse, final String rawMessage) throws ValidationException {

		String mandatoryPath = null;
		String isNull = "Null";
		String isMissing = "Missing";

		String rawMessageWithoutSpace = rawMessage.replaceAll("\\s","");
		
		mandatoryPath = "PortcheckResponse";
		if (!rawMessageWithoutSpace.contains("PortcheckResponse")) {
			throw new ValidationException(mandatoryPath, isMissing, isMissing);
		}
		
		mandatoryPath = "PortcheckResult";
		if (!rawMessageWithoutSpace.contains("PortcheckResult")) {
			throw new ValidationException(mandatoryPath, isMissing, isMissing);
		}
		
		mandatoryPath = "OperationStatus";
		if (!rawMessageWithoutSpace.contains("</OperationStatus>") && !rawMessageWithoutSpace.contains("<OperationStatus>")) {
			throw new ValidationException(mandatoryPath, isMissing, isMissing);
		}
		
		mandatoryPath = "PortcheckResult";
		if (portCheckResponse == null) {
			throw new ValidationException(mandatoryPath, isMissing, isMissing);
		}

		mandatoryPath = "OperationStatus";
		if (portCheckResponse.getOperationStatus() == null) {
			throw new ValidationException(mandatoryPath, isMissing, isMissing);
		}
		else {
			/** IsSuccess **/
			mandatoryPath = "IsSuccess";
			if (portCheckResponse.getOperationStatus().getIsSuccess() == null) {
				throw new ValidationException(mandatoryPath, isMissing, isMissing);
			}
			else {
				if (portCheckResponse.getOperationStatus().getIsSuccess().isEmpty()) {
					throw new ValidationException(mandatoryPath, isMissing, isNull);
				}
			}
			
			/** Code **/
			mandatoryPath = "Code";
			if (portCheckResponse.getOperationStatus().getCode() == null) {
				throw new ValidationException(mandatoryPath, isMissing, isMissing);
			}
			else {
				if (portCheckResponse.getOperationStatus().getCode().isEmpty()) {
					throw new ValidationException(mandatoryPath, isNull, isNull);
				}
			}

			/** Description **/
			mandatoryPath = "Description";
			if (portCheckResponse.getOperationStatus().getDescription() == null) {

				throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isMissing);
			}
			else {

				if (portCheckResponse.getOperationStatus().getDescription().isEmpty()) {

					throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isNull);
				}
			}

			/** TransactionId **/
			mandatoryPath = "TransactionId";
			if (portCheckResponse.getOperationStatus().getTransactionID() == null) {

				throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isMissing);
			}
			else {

				if (portCheckResponse.getOperationStatus().getTransactionID().isEmpty()) {

					throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isNull);
				}
			}

			/** OrderRef **/
			mandatoryPath = "OrderRef";
			if (portCheckResponse.getOperationStatus().getOrderRef() == null) {

				throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isMissing);
			}
			else {

				if (portCheckResponse.getOperationStatus().getOrderRef().isEmpty()) {

					throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isNull);
				}
			}

		}

		if (portCheckResponse.getOperationStatus().getCode().equals(USMPCode.VSMP_08030000)) {
			/** Journal **/
			mandatoryPath = "Journal";
			if (!rawMessageWithoutSpace.contains("</Journal>") && !rawMessageWithoutSpace.contains("<Journal>")) {
				throw new ValidationException(mandatoryPath, isMissing, isMissing);
			}
			
			mandatoryPath = "Journal";
			if (portCheckResponse.getPortJournal() == null) {
				throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isMissing);
			}
			else {

				mandatoryPath = "RoutingId";
				if (portCheckResponse.getPortJournal().getRoutingId() == null) {
					throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isMissing);
				}
				else {

					if (portCheckResponse.getPortJournal().getRoutingId().isEmpty()) {

						throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isNull);
					}

				}

				mandatoryPath = "SpName";
				if (portCheckResponse.getPortJournal().getSpName() == null) {
					throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isMissing);
				}
				else {

					if (portCheckResponse.getPortJournal().getSpName().isEmpty()) {

						throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isNull);
					}

				}

				mandatoryPath = "OriginalSp";
				if (portCheckResponse.getPortJournal().getOriginalSP() == null) {
					throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isMissing);
				}
				else {

					if (portCheckResponse.getPortJournal().getOriginalSP().isEmpty()) {

						throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isNull);
					}

				}

				mandatoryPath = "ListOfHops";
				if (portCheckResponse.getPortJournal().getListOfHops() == null) {
					throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isMissing);
				}
				else {

					if (portCheckResponse.getPortJournal().getListOfHops().getListOfHop() == null) {

						throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isNull);
					}

				}
				
				mandatoryPath = "ListOfHop";
				if (portCheckResponse.getPortJournal().getListOfHops().getListOfHop().size() != 0) {
					for (ListOfHop listOfHop : portCheckResponse.getPortJournal().getListOfHops().getListOfHop()) {
						if (listOfHop == null) {
							throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isMissing);
						}
						else {
							mandatoryPath = "Spname";
							if (listOfHop.getSpName() == null) {
								throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isMissing);
							}
							else {

								if (listOfHop.getSpName().isEmpty()) {

									throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isNull);
								}

							}

							mandatoryPath = "HopDateTime";
							if (listOfHop.getHopDateTime() == null) {
								throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isMissing);
							}
							else {

								if (listOfHop.getHopDateTime().isEmpty()) {

									throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isNull);
								}
							}
						}
					}
				}
				else {
					throw new ValidationException(mandatoryPath, portCheckResponse.getOperationStatus().getCode(), isMissing);
				}
			}
		}
	}

}
