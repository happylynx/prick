package com.github.happylynx.prick.lib.verbosity;

public interface ProgressReceiver {
    void message(String message);
    void estimation(float percentage, int etaSeconds);
}
