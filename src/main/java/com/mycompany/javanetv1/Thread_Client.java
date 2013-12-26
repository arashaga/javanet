/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.javanetv1;

/**
 *
 * @author arash
 */
import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.ScreenWriter;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

class Thread_Client extends Thread {

    //test
    InputStreamReader isr;
    BufferedReader br;
    // DataOutputStream dos;
    PrintWriter pWriter;
    Terminal terminal;
    Screen screen;
    ScreenWriter writer;
    static Socket s;
    DataInputStream dis;
    int x;
    int y;

    public Thread_Client() {
        try {

            System.out.println("Client is ready...(Type ur message):\\n");

            // writeOnScreen paramters
            x = 5;
            y = 3;

// old code
//            s = new Socket("Localhost", 25003);
//            dis = new DataInputStream(s.getInputStream());
//            //test
//            isr = new InputStreamReader(System.in);
//
//            br = new BufferedReader(isr);
//
//            dos = new DataOutputStream(s.
//                    getOutputStream());
            
            // new code
            s = new Socket("Localhost", 25003);

            isr = new InputStreamReader(System.in);
            br = new BufferedReader(isr);
            pWriter = new PrintWriter(s.getOutputStream());
            
            


            terminal = TerminalFacade.createTerminal(System.in, System.out, Charset.forName("UTF8"));
            screen = new Screen(terminal);
            writer = new ScreenWriter(screen);
            screen.startScreen();
            writeOnScreen("Watching.............");
        } catch (Exception ie) {
            ie.printStackTrace();
        }
        // start();
    }

    private void writeOnScreen(String string) {



        if (this.y >= 3 || this.y <= 150) {

            writer.setForegroundColor(Terminal.Color.BLACK);
            writer.setBackgroundColor(Terminal.Color.WHITE);
            writer.drawString(5, this.y, string);
            screen.refresh();
            this.y += 3;
        } else {
            this.y = 3;
            screen.clear();
            writeOnScreen(string);
        }

    }

    @Override
    @SuppressWarnings("empty-statement")
    public void run() {
        String msg = "";

        while (!Thread.currentThread().isInterrupted()) {
            try {

                //test
                if (br.ready()) {
                    msg = br.readLine();
                }

                if (msg.equals("Bye Bye")) {
                    System.exit(0);
                }
                
                pWriter.println(msg);
           //     dos.writeUTF(msg);
            //    dos.close();
            //    System.out.println("Client's dos closed! \n");

                writeOnScreen("Server: " + dis.readUTF());
                synchronized (this) {
                    wait(1000);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (IOException ex) {
                Logger.getLogger(Thread_Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static void main(String[] args) {
        (new Thread_Client()).start();


    }
}