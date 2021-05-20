package com.checkmarx.util.cmd;

import com.checkmarx.sdk.config.CxProperties;
import com.checkmarx.sdk.dto.cx.CxCustomField;
import com.checkmarx.sdk.dto.cx.CxProject;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.checkmarx.util.cmd.CmdUtil.addTeamPathSeparatorPrefix;

/**
 * Command for project based operations within Checkmarx
 */
@Component
@Command(name = "project")
public class ProjectCommand implements Callable<Integer> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ProjectCommand.class);
    private final CxService cxService;
    private final CxProperties cxProperties;

    @Spec
    private CommandSpec spec;

    public enum ExitStatus {
	FULL_SCAN_REQUIRED(0),
	FULL_SCAN_NOT_REQUIRED(2);
	private int exitStatus;
	public int getExitStatus() {
	    return exitStatus;
	}
	private ExitStatus(int exitStatus) {
	    this.exitStatus = exitStatus;
	}
    }
    /**
     * TeamCommand Constructor for team based operations against Checkmarx
     * @param cxService the SDK client
     * @param cxProperties the SDK configuration
     */
    public ProjectCommand(CxService cxService, CxProperties cxProperties) {
        this.cxService = cxService;
        this.cxProperties = cxProperties;
    }

    /**
     * Dummy implementation of the call method to implement the Callable
     * interface.
     *
     * @return CommandLine.ExitCode.USAGE
     */
    public Integer call() throws Exception {
        log.info("Calling role command");

	CommandLine.usage(spec, System.err);
        return CommandLine.ExitCode.USAGE;
    }

    /**
     * Set a project's custom fields
     * @throws CheckmarxException
     */
    @Command(name = "set-custom-fields", description = "Set a project's custom fields")
    private void setCustomFields(
	    @Option(names = {"-s", "--strict"}, description = "Fail if unrecognised custom field specified") Boolean strict,
	    @Option(names = {"-t", "--team"}, description = "The team to which the project belongs") String team,
	    @Parameters(paramLabel = "Project", description = "The project name, optionally qualified by the team") String project,
	    @Parameters(paramLabel = "Custom fields", arity = "1..*", description = "One or more name=value pairs") String[] customFields
	    ) throws CheckmarxException{
        log.info("Calling project set-custom-fields command");
        log.debug("setCustomFields: strict: {}, team: {}, project: {}, customFields: {}",
        	strict, team, project, customFields);

        CxProject cxProject = getCxProject(project, team);

	List<CxCustomField> cxCustomFields = cxService.getCustomFields();
	log.debug("setCustomFields: cxCustomFields: {}", cxCustomFields);
	List<CxProject.CustomField> customFieldList = new ArrayList<>();
	for (String customField : customFields) {
	    String[] parts = customField.split("=", 2);
	    String customFieldName = parts[0];
	    String customFieldValue = parts[1];
	    CxProject.CustomField cf = new CxProject.CustomField();
	    for (CxCustomField ccf : cxCustomFields) {
		if (ccf.name.equalsIgnoreCase(customFieldName)) {
		    cf.id = ccf.id;
		    break;
		}
	    }
	    if (cf.id == null) {
		if (strict != null && strict) {
		    throw new CheckmarxException(String.format("%s: unrecognised custom field", customFieldName));
		} else {
		    log.warn("{}: skipping unrecognised custom field", customFieldName);
		    continue;
		}
	    }
	    cf.value = customFieldValue;
	    customFieldList.add(cf);
	}

	if (!customFieldList.isEmpty()) {
	    cxProject.customFields = customFieldList;
	    cxService.updateProjectCustomFields(cxProject);
	} else {
	    log.info("No valid custom fields provided");
	}
    }

    /**
     * Set a project's custom fields
     * @throws CheckmarxException
     */
    @Command(name = "force-full-scan", description = "Indicate if a full scan is required")
    private int forceFullScan(
	    @Option(names = {"-d", "--duration"}, description = "The duration since the last full scan") Integer duration,
	    @Option(names = {"-t", "--team"}, description = "The team to which the project belongs") String team,
	    @Option(names = {"-u", "--units"}, description = "The duration units (default is days)") String units,
	    @Parameters(paramLabel = "Project") String project
	    ) throws CheckmarxException{
        log.info("Calling project force-full-scan command");
        // Currently, duration must be specified but, maybe, in the future,
        // we will want to add other criteria for forcing a full scan which
        // is why it is an option and not a parameter.
        if (duration == null) {
            throw new CheckmarxException("forceFullScan: duration must be specified");
        }
        CxProject cxProject = getCxProject(project, team);
        ChronoUnit chronoUnit = ChronoUnit.DAYS;
        if (units != null) {
            chronoUnit = ChronoUnit.valueOf(units.toUpperCase());
        }
        log.debug("forceFullScan: chronoUnit: {}", chronoUnit);
        LocalDateTime lastScanDate = cxService.getLastScanDate(cxProject.id);
        log.info("forceFullScan: Last scan date: {}", lastScanDate);
        if (lastScanDate == null) {
            throw new CheckmarxException(String.format("forceFullScan: cannot find last scan date for \"%s\"", project));
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime then = now.minus(duration, chronoUnit);
        log.debug("forceFullScan: comparing last scan date with {}", then);
        if (then.isAfter(lastScanDate)) {
            log.info("forceFullScan: full scan required");
            return ExitStatus.FULL_SCAN_REQUIRED.getExitStatus();
        } else {
            log.info("forceFullScan: full scan not required");
            return ExitStatus.FULL_SCAN_NOT_REQUIRED.getExitStatus();
        }
    }

    /**
     * Given a project name and an optional team name, return the project.
     *
     * @param project the project name (possibly qualified by the team name)
     * @param team the team name
     * @return the project
     * @throws CheckmarxException if the project cannot be found or there are multiple matching projects
     */
    private CxProject getCxProject(String project, String team) throws CheckmarxException {
	log.debug("getProject: project: {}, team: {}", project, team);
        CxProject cxProject = null;

        // If the project has been provided as <team>/<project>, split it.
        int index = project.lastIndexOf(cxProperties.getTeamPathSeparator());
        if (index >= 0) {
            team = project.substring(0, index);
            project = project.substring(index + 1);
        }
        log.debug("setCustomFields: project: {}, team: {}", project, team);

        if (team != null) {
            team = addTeamPathSeparatorPrefix(cxProperties, team);
            String teamId = cxService.getTeamId(team);
            Integer projectId = cxService.getProjectId(teamId, project);
            cxProject = cxService.getProject(projectId);
        } else {
            List<CxProject> projects = cxService.getProjects();
            if (projects.isEmpty()) {
        	throw new CheckmarxException("getProject: no projects found");
            }
            for (CxProject p : projects) {
        	if (p.name.equalsIgnoreCase(project)) {
        	    if (cxProject == null) {
        		cxProject = p;
        	    } else {
        		throw new CheckmarxException(String.format("getProject: %s: project name is not unique", project));
        	    }
        	}
            }
        }

        if (cxProject == null) {
            throw new CheckmarxException(String.format("getProject: %s: canot find project", project));
        }

        log.debug("getProject: project with id {} found", cxProject.getId());
        return cxProject;
    }
}
