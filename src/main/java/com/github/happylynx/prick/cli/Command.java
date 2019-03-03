package com.github.happylynx.prick.cli;

import com.github.happylynx.prick.lib.commands.SyncComamnd;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        unknownCommand(args);
    }

    private void syncCommand(String[] args) {
        if (args.length < 3) {
            HelpKt.sync();
            System.exit(GENERAL_ERROR_CODE);
        }
        final List<Path> paths = Stream.of(args)
                .skip(1) // "sync"
                .map(arg -> Path.of(arg).toAbsolutePath())
                .collect(Collectors.toList());
        new SyncComamnd(paths, Path.of(".")).run();
    }

    private void versionOptionCommand(String[] args) {
        System.out.println("0.1");
    }

    private void unknownCommand(String[] args) {
        System.out.println("Unknown commands: " + String.join(" ", args));
        helpCommand();
        System.exit(GENERAL_ERROR_CODE);
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
