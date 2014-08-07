package com.pryv.utils;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Logger
 *
 * @author ik
 *
 */
public class Logger {

  private static Logger logger = new Logger();
  private PrintStream stream;

  public Logger() {
    stream = System.out;
  }

  /**
   * Returns the Logger singleton
   *
   * @return
   */
  public Logger getInstance() {
    return logger;
  }

  /*
   * writes message in the defined PrintStream (system.out by default)
   */
  public void log(String message) {
    stream.print(message);
  }

  /**
   * Assigns a custom OutputStream
   *
   * @param outputStream
   */
  public synchronized void setOutputStream(OutputStream outputStream) {
    stream = new PrintStream(outputStream);
  }
}
