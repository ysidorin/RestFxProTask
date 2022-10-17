package com.fxpro.ipblocking.helper;


public class Helper {

    protected static final String VALIDIPLEFT = "0.0.0.0";
    protected static final String VALIDIP = "192.168.1.22";
    protected static final String VALIDIPRIGHT = "255.255.255.255";
    protected static final String INVALIDIP = "256.255.255.255";

    protected static final String INVALIDIP2 = "0155.255.255.255";
    protected static final String WHITELISTEDIP = "104.192.138.232";
    public static final String IP = "The IP ";
    public static final String BLOCKED = " is blocked";
    public static final String BLOCK = "block";
    public static final String BLOCKAGAIN = "blockagain";
    public static final String ALREADYBLOCKED = " is already blocked";
    public static final String ISINVALID = " is invalid";
    public static final String INVALIDCOMMAND = "Command is not valid";
    public static final String WHITELISTEDIPCOMMAND = "The IP can't be blocked. It's in the whitelist.";
    public static final String REQUESTBODY = "{qwerty123!@#}}";

    public String getPositiveResponseStringWithIp(String ipNum, String blocked){
        return IP + ipNum + blocked;
    }

    public String getNegativeResponseStringWithIp(String ipNum){
        return IP + ipNum + ISINVALID;
    }

}
