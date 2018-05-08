package client;

import java.net.*;
import java.util.Scanner;
import java.io.*;

public class Client {
    public static void main(String[] args) {
        final boolean debug = true;
        int port;
        char c;
        String userId;
        String address;
        String command;
        String[] components;
        Socket socket = null;
        boolean running = true;
        BufferedReader terminal = new BufferedReader(
            new InputStreamReader(System.in));
        PrintWriter out = null;
        BufferedReader in = null;
        try{
        System.out.printf("Input username: ");
        if(args.length >= 1){
            userId = args[0];
            System.out.println(args[0]);
        }
        else userId = terminal.readLine();
        System.out.printf("Input IP address: ");
        if(args.length >= 2){
            address = args[1];
            System.out.println(args[1]);
        }
        else address = terminal.readLine();
        System.out.printf("Input port: ");
        if(args.length >= 3){
            port = Integer.parseInt(args[2]);
            System.out.println(args[2]);
        }
        else port = Integer.parseInt(terminal.readLine());



        // Setup
        try{
            System.out.println("[ \u001B[36minternal\u001B[0m ] Setting up Socket on port " + port);
            socket = new Socket(address, port);
            System.out.println("[ \u001B[36minternal\u001B[0m ] Socket is set up");

            System.out.println("[ \u001B[36minternal\u001B[0m ] Setting up PrintWriter on socket");
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("[ \u001B[36minternal\u001B[0m ] PrintWriter is set up");

            System.out.println("[ \u001B[36minternal\u001B[0m ] Setting up BufferedReader on socket");
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("[ \u001B[36minternal\u001B[0m ] BufferedReader is set up");


            System.out.println("[ \u001B[36minternal\u001B[0m ] Transmitting username \"" + userId + "\" to server");
            out.println(userId);
            if(in.read() != 'A') throw new IOException();
            System.out.println("[ \u001B[36minternal\u001B[0m ] Acknowledgement received");
        } catch (IOException e){
            System.out.println("[ \u001B[36minternal\u001B[0m ] Error in setting up Socket");
            running = false;
        }

        try{
            while(running)
            {
                System.out.printf("\u001B[33m>>\u001B[0m ");
                command = terminal.readLine();
                components = command.split("\\s+");
                switch(components[0])
                {
                    case "list":
                        out.print('L');
                        out.flush();
                        components = in.readLine().split("#");
                        System.out.printf("[ \u001B[36mserver\u001B[0m ] +==================+\n");
                        for(String s: components)
                        {
                            System.out.printf("[ \u001B[36mserver\u001B[0m ] %20s\n", s);
                        }
                        System.out.printf("[ \u001B[36mserver\u001B[0m ] +==================+\n");
                        break;
                    case "buy":
                        out.print('B');
                        out.print(components[1] + " ");
                        out.println(components[2]);
                        out.flush();
                        c = (char)in.read();
                        if(c == 'S') System.out.println("[ \u001B[36mserver\u001B[0m ] Bought " + components[2] + " " + components[1]);
                        else{
                            System.out.printf("[ \u001B[36mserver\u001B[0m ] Buy unsucessful: ");
                            
                            if(c == 'I')
                                System.out.println("There does not exist enough of this object");
                            else if(c == 'U')
                                System.out.println("There exists no object with that ID");
                            else
                                System.out.println("There was an unknown error");
                        }
                        break;
                    case "sell":
                        out.print('S');
                        out.print(components[1] + " ");
                        out.println(components[2]);
                        out.flush();
                        if(in.read() == 'S') System.out.println("[ \u001B[36mserver\u001B[0m ] Sold " + components[2] + " " + components[1]);
                        else System.out.println("[ \u001B[36mserver\u001B[0m ] Sell unsucessful");
                        break;
                    case "exit":
                    case "Exit":
                    case "quit":
                    case "Quit":
                    case "leave":
                    case "Leave":
                        running = false;
                        break;
                    case "prices":
                        out.print('P');
                        out.flush();
                        components = in.readLine().split("#");
                        System.out.printf("[ \u001B[36mserver\u001B[0m ] +==================+\n");
                        for(String s: components)
                        {
                            System.out.printf("[ \u001B[36mserver\u001B[0m ] %20s\n", s);
                        }
                        System.out.printf("[ \u001B[36mserver\u001B[0m ] +==================+\n");
                        break;
                    case "inventory":
                        out.print('I');
                        out.flush();
                        components = in.readLine().split("#");
                        System.out.printf("[ \u001B[36mserver\u001B[0m ] +==================+\n");
                        for(String s: components)
                        {
                            System.out.printf("[ \u001B[36mserver\u001B[0m ] %20s\n", s);
                        }
                        System.out.printf("[ \u001B[36mserver\u001B[0m ] +==================+\n");
                        break;
                    case "balance":
                        out.print('A');
                        out.flush();
                        c = (char)in.read();
                        if(c == 'S'){
                            System.out.printf("[ \u001B[36mserver\u001B[0m ] %s\n", in.readLine());
                        }
                        else {
                            System.out.printf("[ \u001B[36mserver\u001B[0m ] Balance error\n");
                        }
                        break;
                    case "elect":
                        out.print('E');
                        out.print(components[1].charAt(0));
                        out.flush();
                        c = (char)in.read();
                        if(c == 'S'){
                            System.out.printf("[ \u001B[36mserver\u001B[0m ] Election started\n");
                        }
                        else {
                            System.out.printf("[ \u001B[36mserver\u001B[0m ] Couldn't initialise election\n");
                        }
                        break;
                }
            }
        } catch (IOException e){
            System.out.println("[ \u001B[36minternal\u001B[0m ] Error in reading commands");
        }

        // Shut down
        try{
            System.out.println("[ \u001B[36minternal\u001B[0m ] Shutting down Socket");
            socket.close();
            System.out.println("[ \u001B[36minternal\u001B[0m ] Socket is shut down");

        } catch (IOException e){
            System.out.println("[ \u001B[36minternal\u001B[0m ] Error in shutting down Socket");
        }
        }
        catch(IOException e){
            System.out.println("[ \u001B[36minternal\u001B[0m ] Error in acquiring start up data");
        }
    }
}