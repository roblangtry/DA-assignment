package algorithms;
public abstract class Experiment{
    public static int SERVER_REFRESH_RATE = 200; // The rate at which ServerSocket connections will yield
    public static int MESSAGE_DELAY = 100; // A delay to make the server's wait before replying to messages
    public static int LONG_DELAY = 10000; // A long delay value for checking for connection timeout's
    public static boolean PERIODIC_ELECTIONS = true; // Periodically run an election on the leader
    public static boolean CONNECTION_ELECTIONS = true;  // Run an election automatically when connecting as a higher
                                                        // valued address than existing leader
    public static boolean DEBUG_CONNECTIONS = false; // Turn on various debug messages
    public static AbstractAlgorithm FAILURE_ALGORITHM = new Bully(); // Algorithms for when node failures are detected
    public static AbstractAlgorithm PERIODIC_ALGORITHM = new EnhancedBully(); // Algorithm for periodic elections
    public static AbstractAlgorithm CONNECTION_ALGORITHM = new ChangRoberts(); // Algorithm for on connect elections
}