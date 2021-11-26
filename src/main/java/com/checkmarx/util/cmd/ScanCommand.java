package com.checkmarx.util.cmd;

import com.checkmarx.sdk.config.CxProperties;
import com.checkmarx.sdk.dto.cx.CxCustomField;
import com.checkmarx.sdk.dto.cx.CxProject;
import com.checkmarx.sdk.dto.cx.CxScanParams;
import com.checkmarx.sdk.dto.cx.CxTeam;
import com.checkmarx.sdk.exception.CheckmarxException;
import com.checkmarx.sdk.service.CxService;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.util.HashMap;
import java.util.Map;

@Component
@Command(name = "scan")
public class ScanCommand {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ScanCommand.class);
    private final CxService cxService;
    private final CxProperties cxProperties;

    @Spec
    private CommandSpec spec;

    /**
     * ScanCommand Constructor for scan based operations against Checkmarx
     * @param cxService the SDK client
     * @param cxProperties the SDK configuration
     */
    public ScanCommand(CxService cxService, CxProperties cxProperties) {
        this.cxService = cxService;
        this.cxProperties = cxProperties;
    }

    /**
     * Dummy implementation of the call method to implement the Callable
     * interface.
     *
     * @return CommandLine.ExitCode.USAGE
     * @throws Exception never
     */
    public Integer call() throws Exception {
        log.info("Calling scan command");

        CommandLine.usage(spec, System.err);
        return CommandLine.ExitCode.USAGE;
    }

    /**
     * Creates a scan
     *
     * @param strict fail if an unrecognized custom field is supplied
     * @param team the team to which the project belongs
     * @param units the units by which the duration is measured (if null, days are used)
     * @param project the project
     * @throws CheckmarxException
     */
    @Command(name = "create", description = "Create a scan")
    private void createScan(
            @Option(names = {"-b", "--branch"}, description = "The branch to scan") String branch,
            @Option(names = {"-c", "--comment"}, description = "The scan comment") String comment,
            @Option(names = {"-f", "--filePath"}, description = "The source code path") String filePath,
            @Option(names = {"-g", "--gitUrl"}, description = "The Git repository URL") String gitUrl,
            @Option(names = {"-i", "--incremental"}, description = "Perform an incremental scan") Boolean incrementalScan,
            @Option(names = {"-p", "--private"}, description = "Create a private scan") Boolean privateScan,
            @Option(names = {"-t", "--team"}, description = "The team the project is assigned to") String team,
            @Parameters(paramLabel = "Project", description = "The project name, optionally qualified by the team") String project,
            @Parameters(paramLabel = "Custom Field", arity = "0..*", description = "One or more name=value pairs") String[] customFields
    ) throws CheckmarxException {
        log.info("Calling the scan create command");
        log.debug("createScan: team: {}, project: {}, customfields: {}",
                team, project, customFields);
        Map<String, String> customFieldMap = new HashMap<>();
        if (customFields != null) {
            for (String customField : customFields) {
                String[] nvp = customField.split("=", 2);
                if (nvp.length != 2) {
                    continue;
                }
                customFieldMap.put(nvp[0], nvp[1]);
            }
        }
        CxScanParams cxScanParams = new CxScanParams();
        cxScanParams.setProjectName(project);
        cxScanParams.setTeamName(team);
        if (filePath != null) {
            cxScanParams.setSourceType(CxScanParams.Type.FILE);
            cxScanParams.setFilePath(filePath);
        } else if (gitUrl != null) {
            if (branch == null) {
                log.error("The --branch option must be provided if the --gitUrl option is provided");
                throw new CheckmarxException("The --branch option must be provided if the --gitUrl option is provided");
            }
            cxScanParams.setSourceType(CxScanParams.Type.GIT);
            cxScanParams.setGitUrl(gitUrl);
            cxScanParams.setBranch(branch);
        } else {
            log.error("One of the --filePath and --gitUrl options must be provided");
            throw new CheckmarxException("One of the --filePath and --gitUrl options must be provided");
        }
        if (incrementalScan != null && incrementalScan) {
            cxScanParams.setIncremental(true);
        }
        if (privateScan != null && privateScan) {
            cxScanParams.setPublic(false);
        }
        cxScanParams.setCustomFields(customFieldMap);
        cxService.createScan(cxScanParams, comment);
    }
}
