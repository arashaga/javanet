/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.javanetv1;

/**
 *
 * @author arash
 */
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
//import javaexperiment.server.TestClientWacherBean;

/**
 * Example to watch a directory (or tree) for changes to files.
 */
public class WatcherDir extends Thread {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final boolean recursive;
    private boolean trace = false;
    private String sStatus;
    private String str;
    static int i;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }
    private PropertyChangeSupport mPcs =
            new PropertyChangeSupport(this);

    public String getStatus() {
        return sStatus;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
                String str = "register: " + dir.toAbsolutePath();
                sStatus = str;
                mPcs.firePropertyChange("sStatus",
                        str, sStatus);

            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                    String str = "update: " + prev.toAbsolutePath() + dir.toAbsolutePath();
                    sStatus = str;
                    mPcs.firePropertyChange("sStatus",
                            str, sStatus);
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
           
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {

                System.out.println("Phase " + i + ": " + dir.getFileName());
                i++;
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    WatcherDir(Path dir, boolean recursive) throws IOException {
        super.setName("Watcher Thread");
        this.watcher = FileSystems.getDefault().newWatchService();
        
        this.keys = new HashMap<WatchKey, Path>();
        this.recursive = recursive;

        this.i = 0;
        str = "First Time!";
        if (recursive) {
            // System.out.format("Scanning :", dir);
            String str = "Scanning %s ...\n" + dir.toAbsolutePath();
            sStatus = str;
            mPcs.firePropertyChange("sStatus",
                    str, sStatus);
            registerAll(dir);
            //  System.out.println("Done.");
        } else {
            String str = "Registering :" + dir.toAbsolutePath();
            sStatus = str;
            mPcs.firePropertyChange("sStatus",
                    str, sStatus);
            register(dir);
        }

        // enable trace after initial registration
        this.trace = true;
    }

    /**
     * Process all events for keys queued to the watcher
     */
    @Override
    public void run() {
        
        processEvents();
    
    }

    void processEvents() {
        
            for (;;) {

                // wait for key to be signalled
                WatchKey key;
                try {
                    key = watcher.take();
                    
                } catch (InterruptedException x) {
                    return;
                }

                Path dir = keys.get(key);
                if (dir == null) {
                    System.err.println("WatchKey not recognized!!");
                    sStatus = "WatchKey not recognized!!";

                    mPcs.firePropertyChange("sStatus",
                            str, sStatus);
                    str = sStatus;
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind kind = event.kind();

                    // TBD - provide example of how OVERFLOW event is handled
                    if (kind == OVERFLOW) {
                        continue;
                    }

                    // Context for directory entry event is the file name of entry
                    WatchEvent<Path> ev = cast(event);
                    Path name = ev.context();
                    Path child = dir.resolve(name);

                    // print out event
                    // System.out.format("%s: %s\n", event.kind().name(), child);
                    sStatus = event.kind().name() + child;

                    mPcs.firePropertyChange("sStatus",
                            str, sStatus);

                    str = sStatus;

                    // if directory is created, and watching recursively, then
                    // register it and its sub-directories
                    if (recursive && (kind == ENTRY_CREATE)) {
                        try {
                            if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                                registerAll(child);
                            }
                        } catch (IOException x) {
                            // ignore to keep sample readbale
                        }
                    }
                }

                // reset key and remove from set if directory no longer accessible
                boolean valid = key.reset();
                if (!valid) {
                    keys.remove(key);

                    // all directories are inaccessible
                    if (keys.isEmpty()) {
                        break;
                    }
                }
            }
        
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        mPcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        mPcs.removePropertyChangeListener(listener);
    }

    public static String usage() {
        System.err.println("usage: java WatchDir [-r] dir");
        return "The Directory is no good!";

    }
//    public static void main(String[] args) throws IOException {
//        // parse arguments
//
//
//        
//        Path dir = Paths.get("c:\\\\test");
//        if (dir == null) {
//            WatcherDir.usage();
//        }
//        boolean recursive = true;
//        
//        WatcherDir wDir = new WatcherDir(dir, recursive);
//        TestClientWacherBean tcwBean = new TestClientWacherBean();
//        
//        wDir.addPropertyChangeListener(tcwBean);
//
//        // register directory and process its events
//        
//        wDir.processEvents();
//    }
}