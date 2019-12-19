package com.checkmarx.util.cmd;

import com.checkmarx.sdk.config.CxProperties;
import com.checkmarx.sdk.dto.cx.CxTeam;
import com.checkmarx.sdk.exception.CheckmarxException;
import com.checkmarx.sdk.service.CxClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Unmatched;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.Callable;


/**
 * Command for Team based operations within Checkmarx
 */
@Component
@Command
public class TeamCommand implements Callable<Integer> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TeamCommand.class);
    private static final String TEAM_PATH_SEPARATOR = "\\";
    private final CxClient cxClient;
    private final CxProperties cxProperties;

    @Option(names = {"-command","--command"}, description = "Command name")
    private String command;
    @Option(names = {"-action","--action"}, description = "Action to execute - create, delete, move, add-ldap, remove-ldap,")
    private String action;
    @Option(names = {"-t","--team"}, description = "Checkmarx Team")
    private String team;
    @Option(names = {"-d","--destination-team"}, description = "Checkmarx Destination Team")
    private String destTeam;
    @Option(names = {"-create","--create"}, description = "Create team if it does not exist (parent team must exist)")
    private boolean create;
    @Option(names = {"-s","--ldap-server"}, description = "LDAP Server Name")
    private String ldapServer;
    @Option(names = {"-m","--add-ldap-map"}, description = "Add LDAP DN Mapping")
    private String addLdapDn;
    @Option(names = {"-r","--remove-ldap-map"}, description = "Remove LDAP DN Mapping")
    private String removeLdapDn;
    @Parameters
    private String[] remainder;
    @Unmatched
    private String[] unknown;

    private static final String UNKNOWN = "-1";
    private static final Integer UNKNOWN_INT = -1;

    /**
     * TeamCommand Constructor for team based operations against Checkmarx
     * @param cxClient
     * @param cxProperties
     */
    public TeamCommand(CxClient cxClient, CxProperties cxProperties) {
        this.cxClient = cxClient;
        this.cxProperties = cxProperties;
    }

    /**
     * Entry point for Command to execute
     * @return
     * @throws Exception
     */
    public Integer call() throws Exception {
        log.info("Calling Team Command");
        if(!team.startsWith(TEAM_PATH_SEPARATOR)){
            team = TEAM_PATH_SEPARATOR.concat(team);
        }
        switch (action.toUpperCase()){
            case "CREATE":
                createTeam();
                break;
            case "DELETE":
                deleteTeam();
                break;
            case "MOVE":
                moveTeam();
                break;
            case "ADD-LDAP":
                addLdapMapping();
                break;
            case "REMOVE-LDAP":
                removeLdapMapping();
                break;
        }

        return 0;
    }

    /**
     * Map a team to an ldap group dn
     * If the team does not exist, and the the create flag is set, it will be created first
     * @throws CheckmarxException
     */
    private void addLdapMapping() throws CheckmarxException{
        if(create){
            log.info("Creating team if it does not exits.");
            createTeam();
        }
        String teamId = cxClient.getTeamId(team);
        String teamName = getTeamName();
        if(teamId.equals("-1")){
            log.error("Could not find team {}", team);
            throw new CheckmarxException("Could not find team ".concat(team));
        }
        if(StringUtils.isNotEmpty(ldapServer)) {
            Integer serverId = cxClient.getLdapServerId(ldapServer);
            if(serverId > 0) {
                cxClient.mapTeamLdapWS(serverId, teamId, teamName, addLdapDn);
                log.info("Ldap mapping {} has been added to team {}", addLdapDn, team);
            }
            else {
                log.error("Ldap Server {} not found ", ldapServer);
                throw new CheckmarxException("Ldap Server not found");
            }
        }
        else{
            log.error("No Ldap Server provided");
            throw new CheckmarxException("Ldap Server not provided");
        }
    }

    /**
     * Remove an Ldap groupd dn mapping for a team
     * @throws CheckmarxException
     */
    private void removeLdapMapping() throws CheckmarxException{
        String teamId = cxClient.getTeamId(team);
        String teamName = getTeamName();
        if(teamId.equals("-1")){
            log.error("Could not find team {}", team);
            throw new CheckmarxException("Could not find team ".concat(team));
        }
        if(StringUtils.isNotEmpty(ldapServer)) {
            Integer serverId = cxClient.getLdapServerId(ldapServer);
            if(serverId > 0) {
                cxClient.removeTeamLdapWS(serverId, teamId, teamName, addLdapDn);
                log.info("Ldap mapping {} has been removed from team {}", addLdapDn, team);
            }
            else {
                log.error("Ldap Server {} not found ", ldapServer);
                throw new CheckmarxException("Ldap Server not found");
            }
        }
        else{
            log.error("No Ldap Server provided");
            throw new CheckmarxException("Ldap Server not provided");
        }
    }

    /**
     * Create a team (if it doesn't exist)
     * @throws CheckmarxException
     */
    private void createTeam() throws CheckmarxException {
        //check if the team exists
        if(!cxClient.getTeamId(team).equals("-1")){
            log.warn("Team already exists...");
            return;
        }
        //get the parent and create the team
        int idx = team.lastIndexOf(TEAM_PATH_SEPARATOR);
        String parentPath = team.substring(0, idx);
        String teamName = getTeamName();
        log.info("Parent path: {}", parentPath);
        String parentId = cxClient.getTeamId(parentPath);
        log.info(parentId);
        cxClient.createTeamWS(parentId, teamName);
    }

    /**
     * Delete a given team
     *
     * @throws CheckmarxException
     */
    private void deleteTeam() throws CheckmarxException{
        String teamId = cxClient.getTeamId(team);
        if(teamId.equals("-1")){
            log.warn("Could not find team {}", team);
        }
        else {
            log.info("Deleting team {} with Id {}", team, teamId);
            cxClient.deleteTeamWS(teamId);
        }
    }

    /**
     * Move a team to destTeam
     * @throws CheckmarxException
     */
    private void moveTeam() throws CheckmarxException {
        //Make sure we have the destination
        if(StringUtils.isNotEmpty(destTeam)) {
            if(!destTeam.startsWith(TEAM_PATH_SEPARATOR)){
                destTeam = TEAM_PATH_SEPARATOR.concat(destTeam);
            }
            log.info("Moving team {} under {}", team, destTeam);
            // Get a list of teams and use it to lookup two IDs
            List<CxTeam> teams = cxClient.getTeams();
            if (teams == null) {
                throw new CheckmarxException("Error obtaining Team Id for " + team);
            }
            String teamId = UNKNOWN;
            for (CxTeam cxteam : teams) {
                if (cxteam.getFullName().equals(team)) {
                    log.info("Found team {} with ID {}", team, cxteam.getId());
                    teamId = cxteam.getId();
                    break;
                }
            }

            if(StringUtils.equals(teamId, UNKNOWN)) {
                log.error("Error moving team {}: not found", team);
            }
            else {
                String destTeamId = UNKNOWN;
                for (CxTeam cxteam : teams) {
                    if (cxteam.getFullName().equals(destTeam)) {
                        log.info("Found team {} with ID {}", destTeam, cxteam.getId());
                        destTeamId = cxteam.getId();
                        break;
                    }
                }
                if(StringUtils.equals(destTeamId, UNKNOWN)) {
                    log.error("Error moving team {}: new parent team {} not found", team, destTeam);
                } else {
                    cxClient.moveTeam(teamId, destTeamId);
                    log.info("Move successful for team {}", team);
                }
            }
        }
    }

    /**
     * Get the team name from the full path
     * @return Team name (text to the right of the last separator)
     */
    private String getTeamName(){
        int idx = team.lastIndexOf(TEAM_PATH_SEPARATOR);
        return team.substring(idx+1);
    }

    /**
     * Get the team name from the full path
     * @param inTeam Full path of the team to parse
     * @return inTeam name (text to the right of the last separator)
     */
    private String getTeamName(String inTeam){
        int idx = inTeam.lastIndexOf(TEAM_PATH_SEPARATOR);
        return inTeam.substring(idx+1);
    }

    /**
     * Get the parent team from the full path
     * @param inTeam Full path of the team to parse
     * @return Parent team (inTeam path without  name)
     */
    private String getParentTeam(String inTeam) {
        int idx = inTeam.lastIndexOf(TEAM_PATH_SEPARATOR);
        if(idx > 0)
            return inTeam.substring(0, idx);
        else
            return "";
    }
}
