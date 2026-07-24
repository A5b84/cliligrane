package io.github.a5b84.cliligrane;


import lombok.extern.slf4j.Slf4j;

import picocli.CommandLine;

@Slf4j
public class Main {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new CliligraneCommand()).execute(args);
        System.exit(exitCode);
    }
}
