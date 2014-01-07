/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.javanetv1;

/**
 *
 * @author arash
 */

//TODO: 1/1/13the client cannot exit gracefull, also the client will not receive the notification
//unless I hit enter. The notification from the srever side needs to be fixed as well to produce
// more sensible message. The client's screen needs to be fixed completely. when the size of the
//terminal is resized , the whole thing gets messed up.
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
    BufferedReader stdIn;
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

            System.out.println("Client is ready...(Type ur message):\n");

            // writeOnScreen paramters
            x = 5;
            y = 3;

// old code
//            s = new Socket("Localhost", 25003);
            //           dis = new DataInputStream(s.getInputStream());
//            //test
//            isr = new InputStreamReader(System.in);
//
//            br = new BufferedReader(isr);
//
//            dos = new DataOutputStream(s.
//                    getOutputStream());

            // new code
            s = new Socket("Localhost", 25003);

            isr = new InputStreamReader(s.getInputStream());
            br = new BufferedReader(isr);
            pWriter = new PrintWriter(s.getOutputStream());

            stdIn = new BufferedReader(new InputStreamReader(System.in));


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
            writer.drawString(2, this.y, string);
            screen.refresh();
            this.y += 1;
        } else {
            this.y = 1;
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
                if (stdIn.ready()) {
                    msg = stdIn.readLine();
                }

                if (msg.equals("Bye Bye")) {
                    System.exit(0);
                }

                pWriter.println(msg + "\n");

                if (br.ready()) {
                    writeOnScreen("Server: " + br.readLine());
                }

                synchronized (this) {
                    wait(250);
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