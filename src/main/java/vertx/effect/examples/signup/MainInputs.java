package vertx.effect.examples.signup;

public class MainInputs {
    static final int MONGODB_MAX_QUERY_TIME = 5000;
    static final int MONGODB_INSERT_FAILURE_ATTEMPTS = 3;
    static final int MONGODB_QUERY_FAILURE_ATTEMPTS = 3;
    static final String MONGODB_DATABASE = "test";
    static final String MONGODB_CLIENT_COLLECTION = "Client";
    static final String MONGODB_CONNECTION_STR = "mongodb://localhost:27017/?connectTimeoutMS=2000&socketTimeoutMS=2000&serverSelectionTimeoutMS=2000";

    static final String GEOCODE_API_KEY = System.getProperty("GEOCODE_API_KEY","");
    static final int GEOCODE_API_CONNECT_TIMEOUT = 3000;
    static final int GEOCODE_API_REQ_TIMEOUT = 2000;
    static final String GEOCODE_API_HOST = "maps.googleapis.com";
    static final int GEOCODE_API_FAILURE_ATTEMPTS = 3;
    static final int GEOCODE_API_NOT_2XX_ATTEMPTS = 2;

    static final String EMAIL_FROM = "rafamg13@gmail.com";
    static final String EMAIL_API_HOST = "email-smtp.us-east-2.amazonaws.com";
    static final String EMAIL_API_USER = System.getProperty("EMAIL_API_USER","");
    static final String EMAIL_API_PASSWORD = System.getProperty("EMAIL_API_PASSWORD","");
    static final int EMAIL_VERTICLE_INSTANCES = 2;
    static final String EMAIL_FROM_NAME = "Rafael Merino García";
    static final int EMAIL_API_CONNECTION_TIMEOUT = 2000;
    static final int EMAIL_API_PORT = 587;
    static final int EMAIL_API_TIMEOUT = 3000;
    static final int TELNET_PORT = 4000;
    static final int HTTP_SERVER_PORT = 7890;


}
