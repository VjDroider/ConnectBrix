package com.edbrix.connectbrix.utils;

public class Constants  {

    // TODO Change it to your web domain
    public final static String WEB_DOMAIN ="";
    // TODO Change it to your APP Key
    public final static String APP_KEY__ = "QVBAMTIjMllIRC1TREFTNUQtNUFTRksyMjEhZWRicml4QDE4";
    // TODO Change it to your APP Secret
    public final static String APP_SECRET__ = "MjQ1QDEyIzJZSEQtODVEQTJTM0RFQTg1Mz1JRTVCNEE1IWVkYnJpeEAxOA==";

    private final static String BASE_URL = "http://services.edbrix.net/";

    public final static String userLogin = BASE_URL.concat("auth/login");  // authenticate
    public final static String organizationList = BASE_URL.concat("auth/authorganizationlistbyuseremail");
    public final static String resetPassword = BASE_URL.concat("auth/resetpassword");

}
