package com.powerranger.sow2.iamnotarobot.configuration;

public final class API {
    private API() {}

    public class Server {
        private static final String BASE_IP = "http://192.168.0.103";
        public static final String BASE_URL_API = BASE_IP + ":3000/";
        public static final String SOCKET_URL = BASE_IP + ":3001";
        public static final String LOGIN = BASE_URL_API + "login";
        public static final String SEARCH = BASE_URL_API + "search/";
    }
}
