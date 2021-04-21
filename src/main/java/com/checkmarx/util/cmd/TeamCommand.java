package com.checkmarx.util.cmd;

import com.checkmarx.sdk.config.CxProperties;
import com.checkmarx.sdk.exception.CheckmarxException;
import com.checkmarx.sdk.service.CxService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import java.util.concurrent.Callable;


/**
 * Command for Team based operations within Checkmarx
 */
@Component
@Command(name = "team")
public class TeamCommand implements Callable<Integer> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TeamCommand.class);
    private final CxService cxService;
    private final CxProperties cxProperties;

    @Spec
    private CommandSpec spec;

    /**
     * TeamCommand Constructor for team based operations against Checkmarx
     * @param cxService
     * @param cxProperties
     */
    public TeamCommand(CxService cxService, CxProperties cxProperties) {
        this.cxService = cxService;
        this.cxProperties = cxProperties;
    }

    /**
     * Dummy implementation of the call method to implement the Callable
     * interface.
     *
     * @return CommandLine.ExitCode.USAGE
     */
    public Integer call() {
        log.info("Calling team command");

	CommandLine.usage(spec, System.err);
        return CommandLine.ExitCode.USAGE;
    }

    /**
     * Map a team to an ldap group dn
     * If the team does not exist, and the the create flag is set, it will be created first
     * @throws CheckmarxException
     */
    @Command(name = "add-ldap")
    private void addLdapMapping(
	    @Option(names = {"-create","--create"},
	    description = "Create team if it does not exist (parent team must exist)") Boolean create,
	    @Parameters(paramLabel = "Team") String team,
	    @Parameters(paramLabel = "LDAP Server") String ldapServer,
	    @Parameters(paramLabel = "LDAP Mapping") String addLdapDn
	    ) throws CheckmarxException{
        log.info("Calling team add-ldap command");
        if(create){
            log.info("Creating team if it does not exits.");
            createTeam(team);
        }
        String teamId = cxService.getTeamId(team);
        String teamName = getTeamName(team);
        if(teamId.equals("-1")){
            log.error("Could not find team {}", team);
            throw new CheckmarxException("Could not find team ".concat(team));
        }
        if(StringUtils.isNotEmpty(ldapServer)) {
            Integer serverId = cxService.getLdapServerId(ldapServer);
            if(serverId > 0) {
                cxService.mapTeamLdapWS(serverId, teamId, teamName, addLdapDn);
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
    @Command(name = "remove-ldap")
    private void removeLdapMapping(
	    @Parameters(paramLabel = "Team") String team,
	    @Parameters(paramLabel = "LDAP Server") String ldapServer,
	    @Parameters(paramLabel = "LDAP Mapping") String addLdapDn
	    ) throws CheckmarxException{
        log.info("Calling team remove-ldap command");
	addPathSeparatorPrefix(team);
        String teamId = cxService.getTeamId(team);
        String teamName = getTeamName(team);
        if(teamId.equals("-1")){
            log.error("Could not find team {}", team);
            throw new CheckmarxException("Could not find team ".concat(team));
        }
        if(StringUtils.isNotEmpty(ldapServer)) {
            Integer serverId = cxService.getLdapServerId(ldapServer);
            if(serverId > 0) {
                cxService.removeTeamLdapWS(serverId, teamId, teamName, addLdapDn);
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
    @Command(name = "create")
    private void createTeam(
	    @Parameters(paramLabel = "Team") String team
	    ) throws CheckmarxException {
        log.info("Calling team create command");
	addPathSeparatorPrefix(team);
        //check if the team exists
        if(!cxService.getTeamId(team).equals("-1")){
            log.warn("Team already exists...");
            return;
        }
        //get the parent and create the team
        int idx = team.lastIndexOf(this.cxProperties.getTeamPathSeparator());
        String parentPath = team.substring(0, idx);
        String teamName = getTeamName(team);
        log.info("Parent path: {}", parentPath);
        String parentId = cxService.getTeamId(parentPath);
        log.info(parentId);
        cxService.createTeam(parentId, teamName);
    }

    /**
     * Delete a given team
     *
     * @throws CheckmarxException
     */
    @Command(name = "delete")
    private void deleteTeam(
	    @Parameters(paramLabel = "Team") String team
	    ) throws CheckmarxException{
        log.info("Calling team delete command");
	addPathSeparatorPrefix(team);
        String teamId = cxService.getTeamId(team);
        if(teamId.equals("-1")){
            log.warn("Could not find team {}", team);
        }
        else {
            log.info("Deleting team {} with Id {}", team, teamId);
            cxService.deleteTeam(teamId);
        }
    }

    /**
     * Get the teamname from the full path
     * @return
     */
    private String getTeamName(String team){
        int idx = team.lastIndexOf(this.cxProperties.getTeamPathSeparator());
        return team.substring(idx+1);
    }

    /**
     * Add the path separator to the start of the team name if it does
     * not already start with it.
     *
     * @param team the team name
     * @return the team name, starting with the path separator
     */
    private String addPathSeparatorPrefix(String team) {
        if(!team.startsWith(this.cxProperties.getTeamPathSeparator())){
            team = this.cxProperties.getTeamPathSeparator().concat(team);
        }

        return team;
    }
}
