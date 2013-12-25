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
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class Thread_Server extends Thread implements PropertyChangeListener {

    ServerSocket ss;
    static Socket s;
    private String dispMsg;
    private DataOutputStream dos;

    public Thread_Server() {
        System.out.println("Server is to watch.....");

        if (dispMsg == null) {
            dispMsg = "Waiting for a change...";
        }
        try {
            ss = new ServerSocket(25003);
            s = ss.accept();
        } catch (Exception e) {
            System.out.println(e);
        }
        start();
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {


        dispMsg = "Event to String: " + pce.toString() + "\n"
                + "Property Name: " + pce.getPropertyName() + "\n"
                + "Old Value" + pce.getOldValue() + "\n";


        try {
            dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF(this.getDispMsg());
       //     dos.close();
        //    System.out.println("dos closed! \n");
        } catch (IOException ex) {
            Logger.getLogger(Thread_Server.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    public String getDispMsg() {

        return dispMsg;
    }

    public void run() {
        try {
            DataInputStream dis;
            dis = new DataInputStream(s.getInputStream());

            while (true) {
                System.out.println("Client: " + dis.readUTF());

                sleep(250);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {



        try {
            Path dir = Paths.get("c:\\\\test");
            WatcherDir wDir = new WatcherDir(dir, true);
            Thread_Server thSrv = new Thread_Server();
            if (dir == null) {
                WatcherDir.usage();
            } else {

                wDir.addPropertyChangeListener(thSrv);
                wDir.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Thread_Server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}