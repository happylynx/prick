package com.github.happylynx.prick.cli;

import static com.github.happylynx.prick.cli.CommandKt.syncCommand;

// TODO https://picocli.info/
class Command {

    private static final int GENERAL_ERROR_CODE = 1;

    void run(String[] args) {
        if (args.length == 0) {
            helpCommand();
            return;
        }
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
        if ("init".equals(firstArgument)) {
            CommandKt.initCommand(args);
            return;
        }
        unknownCommand(args);
    }

    private void versionOptionCommand(String[] args) {
        System.out.println("0.1");
    }

    private void unknownCommand(String[] args) {
        System.out.println("Unknown command: " + String.join(" ", args));
        helpCommand();
        CliUtils.INSTANCE.die();
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
