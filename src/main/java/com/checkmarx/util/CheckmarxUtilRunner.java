package com.checkmarx.util;

import com.checkmarx.util.cmd.TeamCommand;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

import java.util.Arrays;
import java.util.List;

import static java.lang.System.exit;

@Component
public class CheckmarxUtilRunner implements CommandLineRunner, ExitCodeGenerator {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CheckmarxUtilRunner.class);
    private final CommandLine.IFactory factory;
    private final TeamCommand teamCommand;
    private int exitCode = 0;

    public CheckmarxUtilRunner(IFactory factory, TeamCommand teamCommand) {
        this.factory = factory;
        this.teamCommand = teamCommand;
    }

    @Override
    public void run(String[] args) {
        if(args == null && args.length == 0) {
            exitCode = -1;
            exit(-1);
        }
        List<String> argsList = Arrays.asList(args);
        /*Team Command*/

        if(argsList.contains("-command=team") || argsList.contains("--command=team")){
            exitCode = new CommandLine(teamCommand, factory).execute(args);
        }
        else{
            log.info("No valid option given");
        }
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}



