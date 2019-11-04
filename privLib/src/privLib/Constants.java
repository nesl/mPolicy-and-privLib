package privLib;

public class Constants {
	
	public static String POLICY = "POLICY";
	public static String ENTITY = "ENTITY";
	public static String ENTITY_TYPE = "ENTITY-TYPE";
	public static String DATA_STREAM_TYPE = "DATA-STREAM-TYPE";
	public static String DATA_WINDOW = "DATA-WINDOW";
	public static String DATA_METHODS = "DATA-METHODS";
	public static String ALLOWED_OPS_INTENTS = "ALLOWED-OPERATIONS-INTENTS";
	public static String DOWNSTREAM = "DOWNSTREAM";
	
	public static String[] FIELDS = {POLICY, ENTITY, ENTITY_TYPE,
			DATA_STREAM_TYPE, DATA_WINDOW, DATA_METHODS,
			ALLOWED_OPS_INTENTS, DOWNSTREAM};
	
	public static String ALL = "ALL";
	public static String ANY = "ANY";
	public static String NONE = "NONE";
	public static String[] LOGICS = {ALL, ANY, NONE};
	public static String NOT = "NOT";
	
	public static String STREAM_TYPE_GPS = "GPS";
	public static String STREAM_TYPE_IMU = "IMU";
}
