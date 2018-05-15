package algorithms;
public abstract class Experiment{
    public static boolean PERIODIC_ELECTIONS = true;
    public static boolean CONNECTION_ELECTIONS = true;
    public static AbstractAlgorithm FAILURE_ALGORITHM = new Bully();
    public static AbstractAlgorithm PERIODIC_ALGORITHM = new Bully();
    public static AbstractAlgorithm CONNECTION_ALGORITHM = new ChangRoberts();
}