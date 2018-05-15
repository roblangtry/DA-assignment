package algorithms;
public abstract class Experiment{
    public static int SERVER_REFRESH_RATE = 200;
    public static int MESSAGE_DELAY = 100;
    public static int LONG_DELAY = 10000;
    public static boolean PERIODIC_ELECTIONS = true;
    public static boolean CONNECTION_ELECTIONS = true;
    public static boolean DEBUG_CONNECTIONS = false;
    public static AbstractAlgorithm FAILURE_ALGORITHM = new Bully();
    public static AbstractAlgorithm PERIODIC_ALGORITHM = new EnhancedBully();
    public static AbstractAlgorithm CONNECTION_ALGORITHM = new ChangRoberts();
}