package com.github.happylynx.prick.cli;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        // TODO remove
        System.out.println("args " + Arrays.toString(args));
        new Command().run(args);
    }
}
