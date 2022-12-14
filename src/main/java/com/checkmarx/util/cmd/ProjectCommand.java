package com.checkmarx.util.cmd;

import com.checkmarx.sdk.config.CxProperties;
import com.checkmarx.sdk.dto.cx.CxCustomField;
import com.checkmarx.sdk.dto.cx.CxProject;
import com.checkmarx.sdk.dto.cx.CxProjectBranchingStatus;
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

    // In some cases the SDK returns an integer, in others a string ...
    private final static int UNKNOWN_INT = -1;
    private final static String UNKNOWN_STR = "-1";

    @Spec
    private CommandSpec spec;

    public enum ExitStatus {
        FULL_SCAN_REQUIRED(0),
        FULL_SCAN_NOT_REQUIRED(1);
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
     *
     * @param cxService    the SDK client
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
     *
     * @param strict  fail if an unrecognized custom field is supplied
     * @param team    the team to which the project belongs
     * @param units   the units by which the duration is measured (if null, days are used)
     * @param project the project
     * @throws CheckmarxException
     */
    @Command(name = "set-custom-fields", description = "Set a project's custom fields")
    private void setCustomFields(
            @Option(names = {"-s", "--strict"}, description = "Fail if unrecognised custom field specified") Boolean strict,
            @Option(names = {"-t", "--team"}, description = "The team to which the project belongs") String team,
            @Parameters(paramLabel = "Project", description = "The project name, optionally qualified by the team") String project,
            @Parameters(paramLabel = "Custom fields", arity = "1..*", description = "One or more name=value pairs") String[] customFields
    ) throws CheckmarxException {
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
     * Check whether a full scan should be forced for the specified project.
     *
     * @param duration the maximum amount of elapsed time since the last full scan
     * @param team     the team to which the project belongs
     * @param units    the units by which the duration is measured (if null, days are used)
     * @param project  the project
     * @throws CheckmarxException if more than one matching project is found
     */
    @Command(name = "force-full-scan", description = "Indicate if a full scan is required")
    private int forceFullScan(
            @Option(names = {"-d", "--duration"}, description = "The duration since the last full scan") Integer duration,
            @Option(names = {"-t", "--team"}, description = "The team to which the project belongs") String team,
            @Option(names = {"-u", "--units"}, description = "The duration units (default is days)") String units,
            @Parameters(paramLabel = "Project") String project
    ) throws CheckmarxException {
        log.info("Calling project force-full-scan command");
        // Currently, duration must be specified but, maybe, in the future,
        // we will want to add other criteria for forcing a full scan which
        // is why it is an option and not a parameter.
        if (duration == null) {
            throw new CheckmarxException("forceFullScan: duration must be specified");
        }
        List<CxProject> cxProjects = getCxProjects(project, team);
        CxProject cxProject = null;
        switch (cxProjects.size()) {
            case 0:
                // The assumption is that this program is being called as part of
                // a larger process that will create the project if it does not
                // exist in which case, by definition, it will not hae been scanned
                // and so a full scan will be required.
                log.info("forceFullScan: project not found: full scan required");
                return ExitStatus.FULL_SCAN_REQUIRED.getExitStatus();
            case 1:
                cxProject = cxProjects.get(0);
                break;
            default:
                throw new CheckmarxException(String.format("Expected zero or one matches for \"%s\" (found %d)", project, cxProjects.size()));
        }
        ChronoUnit chronoUnit = ChronoUnit.DAYS;
        if (units != null) {
            chronoUnit = ChronoUnit.valueOf(units.toUpperCase());
        }
        log.debug("forceFullScan: chronoUnit: {}", chronoUnit);
        LocalDateTime lastScanDate = cxService.getLastScanDate(cxProject.id);
        log.info("forceFullScan: Last scan date: {}", lastScanDate);
        if (lastScanDate == null) {
            log.info("forceFullScan: no last scan date: full scan required");
            return ExitStatus.FULL_SCAN_REQUIRED.getExitStatus();
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
     * Retrieve the branching status of a project.
     *
     * @param team     the team to which the project belongs
     * @param project  the project
     * @throws CheckmarxException if more than one matching project is found
     */
    @Command(name = "get-branching-status", description = "Returns the branching status of the project")
    private void getBranchStatus(
            @Option(names = {"-t", "--team"}, description = "The team to which the project belongs") String team,
            @Parameters(paramLabel = "Project") String project
    ) throws CheckmarxException {
        log.info("Calling project get-branch-status command");
        List<CxProject> cxProjects = getCxProjects(project, team);
        CxProject cxProject = null;
        switch (cxProjects.size()) {
            case 0:
                log.info("getBranchStatus: {}: project not found", project);
                return;
            case 1:
                cxProject = cxProjects.get(0);
                break;
            default:
                throw new CheckmarxException(String.format("Expected zero or one matches for \"%s\" (found %d)", project, cxProjects.size()));
        }
        CxProjectBranchingStatus projectBranchingStatus = cxService.getProjectBranchingStatus(cxProject.id);
        log.info("getBranchStatus: project branch status: {}", projectBranchingStatus);
    }

    /**
     * Given a project name and an optional team name, return the project.
     *
     * @param project the project name (possibly qualified by the team name)
     * @param team    the team name
     * @return the project
     * @throws CheckmarxException if the project cannot be found or there are multiple matching projects
     */
    private CxProject getCxProject(String project, String team) throws CheckmarxException {
        log.debug("getCxProject: project: {}, team: {}", project, team);
        CxProject cxProject = null;
        List<CxProject> projects = getCxProjects(project, team);
        switch (projects.size()) {
            case 0:
                throw new CheckmarxException(String.format("getCxProject: %s: no matching project", project));
            case 1:
                cxProject = projects.get(0);
                break;
            default:
                throw new CheckmarxException(String.format("getCxProject: %s: project name is not unique", project));
        }

        log.debug("getCxProject: project with id {} found", cxProject.getId());
        return cxProject;
    }

    /**
     * Given a project name and an optional team name, return a list of
     * matching projects.
     *
     * @param project the project name (possibly qualified by the team name)
     * @param team    the team name
     * @return the list of projects (which may be empty)
     * @throws CheckmarxException if the underlying SDK throws this exception
     */
    private List<CxProject> getCxProjects(String project, String team) throws CheckmarxException {
        log.debug("getCxProjects: project: {}, team: {}", project, team);
        CxProject cxProject = null;

        // If the project has been provided as <team>/<project>, split it.
        int index = project.lastIndexOf(cxProperties.getTeamPathSeparator());
        if (index >= 0) {
            team = project.substring(0, index);
            project = project.substring(index + 1);
        }
        log.debug("getCxProjects: project: {}, team: {}", project, team);

        List<CxProject> cxProjects;
        if (team != null) {
            team = addTeamPathSeparatorPrefix(cxProperties, team);
            String teamId = getTeamId(team);
            if (UNKNOWN_STR.equals(teamId)) {
                throw new CheckmarxException(String.format("getCxProjects: %s: no matching team", team));
            }
            cxProjects = new ArrayList<>();
            Integer projectId = cxService.getProjectId(teamId, project);
            if (UNKNOWN_INT != projectId) {
                cxProject = cxService.getProject(projectId);
                if (cxProject != null) {
                    cxProjects.add(cxProject);
                }
            }
        } else {
            cxProjects = cxService.getProjects();
        }

        log.debug("getCxProjects: found {} matching projects", cxProjects.size());
        return cxProjects;
    }

    private String getTeamId(String teamPath) throws CheckmarxException {
        try {
            List<CxTeam> teams = cxService.getTeams();
            if (teams == null) {
                throw new CheckmarxException("Error obtaining Team Id");
            }
            for (CxTeam team : teams) {
                if (team.getFullName().equalsIgnoreCase(teamPath)) {
                    log.debug("getTeamId: found team {} with ID {}", teamPath, team.getId());
                    return team.getId();
                }
            }
        } catch (Exception e) {
            log.error("getTeamId: error retrieving teams", e);
        }
        log.info("No team was found for {}", teamPath);
        return UNKNOWN_STR;

    }
}
