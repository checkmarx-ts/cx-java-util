package com.checkmarx.util.cmd;

import com.checkmarx.sdk.config.CxProperties;
import com.checkmarx.sdk.dto.ScanResults;
import com.checkmarx.sdk.exception.CheckmarxException;
import com.checkmarx.sdk.service.CxService;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Command for report based operations within Checkmarx
 */
@Component
@Command(name = "results")
public class ResultsCommand {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ResultsCommand.class);
    private final CxService cxService;
    private final CxProperties cxProperties;

    public enum OutputFormat {
        JSON
    }

    @Spec
    private CommandSpec spec;

    /**
     * ResultsCommand Constructor for results operations against Checkmarx
     *
     * @param cxService    the SDK client
     * @param cxProperties the SDK configuration
     */
    public ResultsCommand(CxService cxService, CxProperties cxProperties) {
        this.cxService = cxService;
        this.cxProperties = cxProperties;
    }

    /**
     * Dummy implementation of the call method to implement the Callable
     * interface.
     *
     * @return CommandLine.ExitCode.USAGE
     * @throws Exception if an underlying method throws an exception
     */
    public Integer call() throws Exception {
        log.info("Calling results command");

        CommandLine.usage(spec, System.err);
        return CommandLine.ExitCode.USAGE;
    }


    /**
     * Retrieve a report
     *
     * @param reportId the report identifier
     * @throws CheckmarxException if the SDK throws an exception
     */
    @Command(name = "get", description = "Get results")
    private void getResults(
            @Option(names = {"-f", "--format"}, description = "The output format") OutputFormat outputFormat,
            @Option(names = {"-o", "--output-pathname"}, description = "The output pathname") String outputPathname,
            @Option(names = {"-p", "--project"}, description = "The project name") String projectName,
            @Option(names = {"-r", "--report-id"}, description = "The report identifier") Integer reportId,
            @Option(names = {"-s", "--scan-id"}, description = "The scan identifier") Integer scanId
    ) throws CheckmarxException {
        log.info("Calling results get command");
        log.debug("getReport: outputFormat: {}, projectName: {}, reportId: {}, scanId: {}",
                outputFormat, projectName, reportId, scanId);

        if (outputFormat == null) {
            outputFormat = OutputFormat.JSON;
        }

        if (projectName == null && reportId == null && scanId == null) {
            log.error("Either the project or the report or the scan must be specified");
            return;
        }

        Writer writer;
        if (outputPathname != null) {
            File outputFile = new File(outputPathname);
            try {
                writer = new FileWriter(outputFile);
            } catch (IOException ioe) {
                log.error("Error opening {} for writing: {}", outputPathname, ioe.getMessage(), ioe);
                return;
            }
        } else {
            writer = new OutputStreamWriter(System.out);
        }
        ScanResults scanResults = cxService.getReportContent(reportId, null);
        log.debug("scanResults: {}", scanResults);
        switch (outputFormat) {
            case JSON:
                printJsonResults(scanResults, writer);
                break;
        }
    }

    private void printJsonResults(ScanResults scanResults, Writer writer) {
        log.debug("printJsonResults");
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(writer, scanResults);
        } catch (IOException ioe) {
            log.error("Error writing JSON results: {}", ioe.getMessage(), ioe);
        }
    }
}
