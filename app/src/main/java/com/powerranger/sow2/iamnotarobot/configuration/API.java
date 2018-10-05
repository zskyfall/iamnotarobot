package com.powerranger.sow2.iamnotarobot.configuration;

public final class API {
    private API() {}

    public class Server {
        public static final String BASE_URL_API = "http://192.168.0.100:3000/";
        public static final String LOGIN = BASE_URL_API + "login";
        public static final String SEARCH = BASE_URL_API + "search/";
    }
}
