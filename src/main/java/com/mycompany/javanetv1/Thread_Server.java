/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

    private boolean isChanged;

    public class ClientHandler implements Runnable {

        BufferedReader reader;
        Socket socket;

        public ClientHandler(Socket ClientSocket) {
            try {
                socket = ClientSocket;
                InputStreamReader iReader = new InputStreamReader(socket.getInputStream());
                reader = new BufferedReader(iReader);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void run() {
            String clientMessage;

            try {
                while (((clientMessage = reader.readLine()) != null) || (isChanged = true)) {


                    if (((clientMessage = reader.readLine()) != null)) {
                        System.out.println("Client: " + clientMessage);
                    }

                    if (isChanged = true) {
                        System.out.println("Server: " + dispMsg);
                        notifyStatusChange();
                        isChanged = false;
                    }
                    // sleep(250);

                }
            } catch (IOException ex) {
                Logger.getLogger(Thread_Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    ServerSocket ss;
    static Socket s;
    private String dispMsg;
    private DataOutputStream dos;
    private ArrayList<PrintWriter> clientOutputStream;

    public void goServer() {

        try {
            ss = new ServerSocket(25003);

            while (true) {
                s = ss.accept();
                
                PrintWriter writer = new PrintWriter(s.getOutputStream());
                clientOutputStream.add(writer);
                Thread t = new Thread(new ClientHandler((s)));
                t.start();
                System.out.println("Got another connection! \n");
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
        isChanged = true;
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
    }

    public void notifyStatusChange() {

        Iterator it = clientOutputStream.iterator();


        try {

            while (it.hasNext()) {
                PrintWriter writer = (PrintWriter) it.next();
                writer.println(dispMsg);
                writer.flush();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public String getDispMsg() {

        return dispMsg;
    }

    public static void main(String[] args) {



        try {
            Path dir = Paths.get("c:\\\\test");
            WatcherDir wDir = new WatcherDir(dir, true);
            Thread_Server thSrv = new Thread_Server();
            if (dir == null) {
                WatcherDir.usage();
            } else {
               thSrv.goServer();
                wDir.addPropertyChangeListener(thSrv);
                wDir.start();
                
            }
        } catch (IOException ex) {
            Logger.getLogger(Thread_Server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}