package com.github.happylynx.prick.cli;

import com.github.happylynx.prick.lib.commands.Sync;

// TODO https://picocli.info/
class Command {

    void run(String[] args) {
        final String firstArgument = args[0];
        if (firstArgument == null || "help".equals(firstArgument) || "--help".equals(firstArgument)) {
            helpCommand();
            return;
        }
        if ("--version".equals(firstArgument)) {
            versionOptionCommand(args);
            return;
        }
        if ("sync".equals(firstArgument)) {
            syncCommand(args);
            return;
        }
        unknownCommand(args);
    }

    private void syncCommand(String[] args) {

    }

    private void versionOptionCommand(String[] args) {
        System.out.println("0.1");
    }

    private void unknownCommand(String[] args) {
        System.out.println("Unknown commands: " + String.join(" ", args));
        helpCommand();
    }

    private void helpCommand() {
        System.out.println("Usage: price [--help] [--version]");
        System.out.println("             commands [args...]");
        System.out.println("Commands:");
        System.out.println();
        System.out.println("    help    Print command usage help");
        System.out.println("    init    Initialize directory to be a synchronization root");
        System.out.println("    sync    Synchronizes selected directories");
    }

}
