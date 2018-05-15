package main;
import client_handler.*;
import trader.*;
import java.net.*;
import java.util.Scanner;

import java.io.*;

public class Main {
    public static void main(String[] args) {
        ClientHandler clientHandler = null;
        BufferedReader terminal = new BufferedReader(
            new InputStreamReader(System.in));
        int clientPort = 10000;
        int serverPort = 10001;
        int hostPort = 10001;
        String hostAddress;
        String message;
        ITrader trader = new MockTrader();
        //setting up
        System.out.println("[ \u001B[36minternal\u001B[0m ] Starting...");
        System.out.printf("Enter client port number: ");
        if(args.length >= 1){
            System.out.println(args[0]);
            clientPort = Integer.parseInt(args[0]);
        }
        else{
            try {
                clientPort = Integer.parseInt(terminal.readLine());
            } catch (IOException e){
                System.out.println("[ \u001B[36minternal\u001B[0m ] IO Error in obtaining client port number!");
                System.exit(0);
            }
        }
        System.out.printf("Enter server port number: ");
        if(args.length >= 2){
            System.out.println(args[1]);
            serverPort = Integer.parseInt(args[1]);
        }
        else{
            try {
                serverPort = Integer.parseInt(terminal.readLine());
            } catch (IOException e){
                System.out.println("[ \u001B[36minternal\u001B[0m ] IO Error in obtaining server port number!");
                System.exit(0);
            }
        }
        System.out.printf("Proxy server? (y/n): ");
        try {

            if(args.length >= 3){
                System.out.println(args[2]);
                message = args[2];
            }
            else{
                message = terminal.readLine();
            }

            if(message.charAt(0) == 'y')
            {
                System.out.printf("Enter IP to connect to: ");
                if(args.length >= 4){
                    System.out.println(args[3]);
                    hostAddress = args[3];
                }
                else{
                    hostAddress = terminal.readLine();
                }
                System.out.printf("Enter port to connect to: ");
                if(args.length >= 5){
                    System.out.println(args[4]);
                    hostPort = Integer.parseInt(args[4]);
                }
                else{
                    hostPort = Integer.parseInt(terminal.readLine());
                }
                System.out.println("[ \u001B[36minternal\u001B[0m ] Connecting to " + hostAddress + ":" + hostPort);
                trader = new Trader(hostAddress, hostPort, serverPort);
            }
            else
            {
                System.out.println("[ \u001B[36minternal\u001B[0m ] Server setup");
                trader = new Trader(serverPort);
            }
        } catch (IOException e){
            System.out.println("[ \u001B[36minternal\u001B[0m ] IO Error in obtaining proxy status!");
            System.exit(0);
        }
        try {
            clientHandler = new ClientHandler(clientPort, trader);
        } catch (IOException e){
            System.out.println("[ \u001B[36minternal\u001B[0m ] IO Error in ClientHandler setup!");
            System.exit(0);
        }
        System.out.println("[ \u001B[36minternal\u001B[0m ] Finished set up");
        //running
        clientHandler.run();
        //shut down
        System.out.println("[ \u001B[36minternal\u001B[0m ] Shutting down...");
        try {
            clientHandler.shutdown();
        } catch (IOException e){
            System.out.println("[ \u001B[36minternal\u001B[0m ] IO Error in ClientHandler shutdown!");
            System.exit(0);
        }
        System.out.println("[ \u001B[36minternal\u001B[0m ] Finished shut down");
    }
}