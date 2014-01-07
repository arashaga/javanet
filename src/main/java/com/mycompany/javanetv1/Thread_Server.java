/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


//LAST STATUS: 12/30/13 I have to work on the noftistatuschange function since I get 
//at com.mycompany.javanetv1.Thread_Server.notifyStatusChange(Thread_Server.java:141)
package com.mycompany.javanetv1;

/**
 *
 * @author arash
 */
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import static java.lang.Thread.sleep;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class Thread_Server implements PropertyChangeListener {
    
    private volatile boolean isChanged;
    int counter = 1;
    InputStreamReader isr;
    ServerSocket ss;
    private Socket s;
    private String dispMsg;
    private PrintWriter pwDispMsg;
    private DataOutputStream dos;
    private ArrayList<PrintWriter> clientOutputStreams;

    Thread t;
    
    public class ClientHandler implements Runnable {
        
        BufferedReader reader;
        Socket socket;
        InputStreamReader iReader;
        
        public ClientHandler(Socket ClientSocket) {
            try {
                socket = ClientSocket;
                iReader = new InputStreamReader(socket.getInputStream());
                reader = new BufferedReader(iReader);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        

        
        public void run() {
            String clientMessage;
            System.out.println("Thread: " + Thread.currentThread().getName() + "\n");
            try {
                while (true) {
                    
                    
                    if (((reader.ready()) && (clientMessage = reader.readLine()) != null)) {
                        System.out.println("Client: " + clientMessage);
                    }
                    
                    if (isChanged) {
                        System.out.println("Server1: " + dispMsg);
                        notifyStatusChange();
                        synchronized (this){
                            wait(250);
                        }
                        isChanged = false;
                    }
                    
                    
                }
            } catch (IOException ex) {
                Logger.getLogger(Thread_Server.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(Thread_Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void goServer() {
        clientOutputStreams = new ArrayList();
        try {
            ss = new ServerSocket(25003);
            
            while (true) {
                s = ss.accept();
                // isr = new InputStreamReader(s.getInputStream());

                PrintWriter writer = new PrintWriter(s.getOutputStream(), true);
                clientOutputStreams.add(writer);
                //TODO: add this later on do not delete this part
//                writer.println("Hello," + s.getInetAddress().getHostName() + "! You are now"
//                        + " connected!\n ");
                t = new Thread(new ClientHandler((s)), "Thread for Client #" + counter);
                t.start();
                
                System.out.println("Connection " + counter + " received from: "
                        + s.getInetAddress().getHostName());
                counter++;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                ss.close();
            } catch (IOException ex) {
                Logger.getLogger(Thread_Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public Thread_Server() {
        System.out.println("Server is to watch.....");
        isChanged = false;
        if (dispMsg == null) {
            dispMsg = "Waiting for a change...";
        }
        
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        
        isChanged = true;
        dispMsg = "Event to String: " + pce.toString() + "\n"
                + "Property Name: " + pce.getPropertyName() + "\n"
                + "Old Value" + pce.getOldValue() + "\n";
        // System.out.println(t.getName()+"\n");

        System.out.println("Thread: " + Thread.currentThread().getName() + "\n");
        
        
    }

    // Changed 12/29/13 
    public void notifyStatusChange() throws IOException {

         Iterator it = clientOutputStreams.iterator();
       while(it.hasNext()) {
          try {
             PrintWriter writer = (PrintWriter) it.next();
             writer.println(dispMsg);
             writer.flush();
           } catch(Exception ex) {
               ex.printStackTrace();
           }

       } 
    }
    public String getDispMsg() {
        
        return dispMsg;
    }
    
    public static void main(String[] args) {
        
        
        try {
            if (args.length != 1) {
            
                
               
                System.out.println("Please provide the fully qualified path as an argument.\n"
                        + "Example: Javanet /path/to/the/directory");
                return;
            }
            
             //Enable this for testing porpuses and disable the code above 
             // Path dir = Paths.get("c:\\\\test");
            Path dir = Paths.get(args[0]);
                
            
           // Path dir = Paths.get("c:\\\\test");
            WatcherDir wDir = new WatcherDir(dir, true);
            System.out.println("Thread: " + Thread.currentThread().getName() + "\n");
            Thread_Server thSrv = new Thread_Server();
            if (dir == null) {
                WatcherDir.usage();
            } else {
                
                wDir.addPropertyChangeListener(thSrv);
                wDir.start();
                
                System.out.println("Thread: " + Thread.currentThread().getName() + "\n");
                thSrv.goServer();
                System.out.println("Thread: " + Thread.currentThread().getName() + "\n");
                wDir.join();
                
            }
        } catch (IOException ex) {
            Logger.getLogger(Thread_Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Thread_Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}