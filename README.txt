Election Trading System showcasing election algorithms
    Distributed Algorithms Assignment
        May 2018

Authors:
    * Kai Huang
    * Ryan Henderson
    * Khanh Tan Nguyen
    * Robert Langtry

To compile client:
    Run `make client'

To compile server:
    Run `make server'

To compile server and client:
    Run `make' or `make all'

To begin a network:
    First start the server.jar and use the prompts to enter:
        * The client port - used for clients to connect to
        * The server port - used for other servers to connect to
        * The proxy status - enter `n' as this is a root server
    For ease of use these can be entered as arguments to the file i.e. :
        java -jar server.jar <client-port> <server-port> n
    This will start a standalone server that can manage a network

To connect new servers into the network:
    First start the server.jar and use the prompts to enter:
        * The client port - used for clients to connect to
        * The server port - used for other servers to connect to
        * The proxy status - enter `y' as this is a proxy server that forwards commands into the network
        * The server IP - The ip address of the server whose network you wish to attach too
        * The server port - the server port number of the given server you are attempting to attach too
    For ease of use these can be entered as arguments to the file i.e. :
        java -jar server.jar <client-port> <server-port> y <other-server-ip> <other-server-port>
    This will connect you too the network the other server is attached too either by directly connecting
    you to other server if other server is the leader of the network or by redirecting your server to attach
    itself to leader

To connect a client to the network:
    First start the client.jar and use the prompts to enter:
        * Desired username - The username of the account on the network this client wishes to create/login to
        * Server ip - The IP address of the server you wish to connect to
        * Server port - The port of the server you wish to connect to
    After this is done the user should be logged into the network and any necessary registration carried out.

To alter various characteristics of the experiment;
    In order to change the characteristics/algorithms under observation a number of options are available in the
    Experiment class in the `server/algorithms' folder.
    The available algorithms are:
        * Chang & Roberts -> ChangRoberts()
        * Franklin -> Franklin()
        * Bully -> Bully()
        * Modified Bully -> ModifiedBully()
        * Enhanced Bully -> EnhancedBully()
    It is recommended to avoid Franklin and ChangRoberts as choices for an on failure algorithm as they will fail
    due to the ring nature of their communication.