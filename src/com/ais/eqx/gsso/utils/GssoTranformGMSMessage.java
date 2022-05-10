package com.ais.eqx.gsso.utils;

import java.util.ArrayList;

public class GssoTranformGMSMessage {

	public static ArrayList<String> tranformMessageSending(String messageText, boolean isThai) {
        ArrayList<String> msg = new ArrayList<String>();

        String message[] = messageText.split("|", -1);
        String shortmsg = "";

        if (isThai) {
            int count = 0;
            shortmsg = "";
            if (messageText.length() <= 70) {
                msg.add(convertToUnicode(messageText));
            } else {
                for (int i = 0; i < message.length; i++) {

                    if (count == 67) {
                        count = 0;
                        shortmsg = shortmsg + message[i];
                        msg.add(convertToUnicode(shortmsg));
                        shortmsg = "";
                    } else {
                        shortmsg = shortmsg + message[i];
                    }
                    count++;
                }
                if (!shortmsg.isEmpty()) {
                    msg.add(convertToUnicode(shortmsg));
                }
            }
        } else {

            int count = 1;
            String char338 = StringToOctetGSM(messageText);
            StringBuilder shortmsgSB = new StringBuilder();
            // 160 * 2 = 320
            if (char338.length() <= 320) {
                msg.add(char338);
            } else {
                int realLength = char338.length();
                for (int i = 0; i < realLength; i += 2) {
                    if (count == 153) {
                        count = 0;
                        shortmsgSB.append(char338.substring(i, i + 2));
                        msg.add(shortmsgSB.toString());

                        shortmsgSB = new StringBuilder();
                    } else {
                        shortmsgSB.append(char338.substring(i, i + 2));
                    }
                    count++;
                }
                if (shortmsgSB.length() > 0) {
                    msg.add(shortmsgSB.toString());
                }
            }
        }
        return msg;
    }
	
	public static String convertToUnicode(String str) {
        StringBuilder ostr = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            String hex = Integer.toHexString(str.charAt(i) & 0xFFFF);
            switch (hex.length()) {
                case 1:
                    ostr.append("000").append(hex.toUpperCase());
                    break;
                case 2:
                    ostr.append("00").append(hex.toUpperCase());
                    break;
                case 3:
                    ostr.append("0").append(hex.substring(0, 1)).append(hex.substring(1, 3));
                    break;
                default:
                    ostr.append(hex.substring(0, 2)).append(hex.substring(2, 4));
                    break;
            }
        }
        return (ostr.toString().toUpperCase());
    }

    public static String StringToOctetGSM(String input) {
        StringBuilder octet_str = new StringBuilder();
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            short j = (short) c;
            switch (j) {
                case 12:
                    octet_str.append("1B0A");  //20                 
                    break;
                case 94:
                    octet_str.append("1B14");  //20
                    break;
                case 123:
                    octet_str.append("1B28"); // 40
                    break;
                case 125:
                    octet_str.append("1B29"); // 41
                    break;
                case 92:
                    octet_str.append("1B2F"); // 47
                    break;
                case 91:
                    octet_str.append("1B3C"); // 60
                    break;
                case 126:
                    octet_str.append("1B3D"); // 61
                    break;
                case 93:
                    octet_str.append("1B3E"); //62
                    break;
                case 124:
                    octet_str.append("1B40"); //64                         
                    break;
                case 164:
                    octet_str.append("1B65"); //101                 
                    break;
                default:
                    octet_str.append(Integer.toHexString(j).toUpperCase());
                    break;
            }
        }
        return octet_str.toString();
    }
	
}
