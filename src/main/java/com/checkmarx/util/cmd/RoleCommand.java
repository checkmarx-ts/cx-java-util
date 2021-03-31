package com.checkmarx.util.cmd;

import com.checkmarx.sdk.config.CxProperties;
import com.checkmarx.sdk.exception.CheckmarxException;
import com.checkmarx.sdk.service.CxService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Unmatched;
import picocli.CommandLine.Parameters;
import java.util.concurrent.Callable;


/**
 * Command for Role Ldap Mapping based operations within Checkmarx
 */
@Component
@Command
public class RoleCommand implements Callable<Integer> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(RoleCommand.class);
    private final CxService cxService;
    private final CxProperties cxProperties;

    @Option(names = {"-command","--command"}, description = "Command name")
    private String command;
    @Option(names = {"-action","--action"}, description = "Action to execute - add-ldap, remove-ldap,")
    private String action;
    @Option(names = {"-s","--ldap-server"}, description = "LDAP Server Name")
    private String ldapServer;
    @Option(names = {"-r","--role"}, description = "Role Name")
    private String role;
    @Option(names = {"-m","--ldap-dn"}, description = "Add LDAP DN Mapping")
    private String ldapDn;
    @Parameters
    private String[] remainder;
    @Unmatched
    private String[] unknown;

    /**
     * TeamCommand Constructor for team based operations against Checkmarx
     * @param cxService
     * @param cxProperties
     */
    public RoleCommand(CxService cxService, CxProperties cxProperties) {
        this.cxService = cxService;
        this.cxProperties = cxProperties;
    }

    /**
     * Entry point for Command to execute
     * @return 0 if success, or throws exception if failure
     * @throws Exception
     */
    public Integer call() throws Exception {
        log.info("Calling Role Command");

        if ("ADD-LDAP".equals(action.toUpperCase())) {
            addLdapMapping();
        } else if ("REMOVE-LDAP".equals(action.toUpperCase())) {
            removeLdapMapping();
        }

        return 0;
    }

    /**
     * Map a ldap group dn to a role
     * @throws CheckmarxException
     */
    private void addLdapMapping() throws CheckmarxException{
        Integer roleId = cxService.getRoleId(role);
        if(roleId.equals(-1)){
            log.error("Could not find role {}", role);
            throw new CheckmarxException("Could not find role ".concat(role));
        }
        if(StringUtils.isNotEmpty(ldapServer)) {
            Integer serverId = cxService.getLdapServerId(ldapServer);
            if(serverId > 0) {
                cxService.mapRoleLdap(serverId, roleId, ldapDn);
                log.info("Ldap mapping {} has been added to role {}", ldapDn, role);
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
     * Remove an Ldap dn mapping for a role
     * @throws CheckmarxException
     */
    private void removeLdapMapping() throws CheckmarxException{
        Integer roleId = cxService.getRoleId(role);
        if(roleId.equals(-1)){
            log.error("Could not find role {}", role);
            throw new CheckmarxException("Could not find role ".concat(role));
        }
        if(StringUtils.isNotEmpty(ldapServer)) {
            Integer serverId = cxService.getLdapServerId(ldapServer);
            if(serverId > 0) {
                cxService.removeRoleLdap(serverId, roleId, ldapDn);
                log.info("Ldap mapping {} has been removed from role {}", ldapDn, role);
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
}
