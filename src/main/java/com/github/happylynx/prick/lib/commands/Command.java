package com.github.happylynx.prick.lib.commands;

import com.github.happylynx.prick.lib.verbosity.ProgressReceiver;
import com.github.happylynx.prick.lib.verbosity.Registration;

import java.util.Map;

public interface Command {

    default Registration addProgressListener(ProgressReceiver progressReceiver) {
        return getProgressReceivers()
                .computeIfAbsent(progressReceiver, pr -> (() -> getProgressReceivers().remove(pr)));
    }

    void run();

    Map<ProgressReceiver, Registration> getProgressReceivers();

    default void progressMessage(String message) {
        getProgressReceivers().forEach((receiver, registration) -> receiver.message(message));
    }

    default void progressEstimation(float percentage, int etaSeconds) {
        getProgressReceivers().forEach((receiver, registration) -> receiver.estimation(percentage, etaSeconds));
    }
}
