package com.github.happylynx.prick.lib.verbosity;

import java.io.Closeable;

public interface ProgressReceiver extends Closeable {
    default void message(String message) {
        message(message, Verbosity.INFO);
    }
    void message(String message, Verbosity verbosity);
//    void eta(int seconds);
    void partDone(int percentage);
    ProgressReceiver subTask(String subTaskName, float weight);

    enum Verbosity {
        DEBUG,
        INFO,
        WARNING,
        ERROR,
    }
}
