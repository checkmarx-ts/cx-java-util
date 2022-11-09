package com.checkmarx.util;

import com.checkmarx.util.cmd.ProjectCommand;
import com.checkmarx.util.cmd.ResultsCommand;
import com.checkmarx.util.cmd.RoleCommand;
import com.checkmarx.util.cmd.TeamCommand;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;
import java.util.Arrays;
import java.util.concurrent.Callable;

@Component
@Command(name = "java -jar <util jar>")
public class CheckmarxUtilRunner implements Callable<Integer>, CommandLineRunner, ExitCodeGenerator {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CheckmarxUtilRunner.class);
    private final ProjectCommand projectCommand;
    private final ResultsCommand reportCommand;
    private final RoleCommand roleCommand;
    private final TeamCommand teamCommand;
    private int exitCode = 0;

    @Spec
    private CommandSpec spec;

    public CheckmarxUtilRunner(ProjectCommand projectCommand, ResultsCommand reportCommand, RoleCommand roleCommand,
                               TeamCommand teamCommand) {
        this.projectCommand = projectCommand;
        this.reportCommand = reportCommand;
        this.roleCommand = roleCommand;
        this.teamCommand = teamCommand;
    }

    @Override
    public void run(String[] args) {
        log.debug("run: starting");

        // Strip out arguments used to configure the SDK
        args = Arrays.stream(args)
                .filter(s -> !s.startsWith("--checkmarx."))
                .toArray(String[]::new);

        exitCode = new CommandLine(this)
                .addSubcommand(projectCommand)
                .addSubcommand(reportCommand)
                .addSubcommand(roleCommand)
                .addSubcommand(teamCommand)
                .execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Dummy implementation of the call method to implement the Callable
     * interface.
     *
     * @return CommandLine.ExitCode.USAGE
     */
    @Override
    public Integer call() {
        CommandLine.usage(spec, System.err);
        return CommandLine.ExitCode.USAGE;
    }
}
