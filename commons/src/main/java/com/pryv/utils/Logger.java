package com.pryv.utils;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Logger
 */
public class Logger {

  private static Logger logger;
  private PrintStream stream;

  public Logger() {
    stream = System.out;
  }

  /**
   * Returns the Logger singleton
   *
   * @return
   */
  public static Logger getInstance() {
    if (logger == null) {
      logger = new Logger();
    }
    return logger;
  }

  /**
   * writes message in the defined PrintStream (system.out by default)
   *
   * @param message
   */
  public void log(String message) {
    stream.println(message + " - Thread:" + Thread.currentThread().getName());
  }

  /**
   * Assigns a custom OutputStream
   *
   * @param outputStream
   */
  public synchronized void setOutputStream(OutputStream outputStream) {
    stream = new PrintStream(outputStream);
  }

  /**
   * turn off logging
   */
  public void turnOff() {
    stream = new PrintStream(new OutputStream() {
      @Override
      public void write(int b) {
        // do nothing
      }
    });
  }
}
