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
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import java.util.concurrent.Callable;


/**
 * Command for role LDAP mapping based operations within Checkmarx
 */
@Component
@Command(name = "role")
public class RoleCommand implements Callable<Integer> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(RoleCommand.class);
    private final CxService cxService;
    @SuppressWarnings("unused")
    private final CxProperties cxProperties;

    @Spec
    private CommandSpec spec;

    /**
     * TeamCommand Constructor for team based operations against Checkmarx
     * @param cxService the SDK client
     * @param cxProperties the SDK configuration
     */
    public RoleCommand(CxService cxService, CxProperties cxProperties) {
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
     * Map an LDAP group dn to a role
     * @throws CheckmarxException
     */
    @Command(name = "add-ldap")
    private void addLdapMapping(
	    @Parameters(paramLabel = "LDAP Server") String ldapServer,
	    @Parameters(paramLabel = "Role") String role,
	    @Parameters(paramLabel = "Mapping") String ldapDn
	    ) throws CheckmarxException{
        log.info("Calling role add-ldap command");
        Integer roleId = cxService.getRoleId(role);
        if(roleId.equals(-1)){
            log.error("Could not find role {}", role);
            throw new CheckmarxException("Could not find role ".concat(role));
        }
        if(StringUtils.isNotEmpty(ldapServer)) {
            Integer serverId = cxService.getLdapServerId(ldapServer);
            if(serverId > 0) {
                cxService.mapRoleLdap(serverId, roleId, ldapDn);
                log.info("LDAP mapping {} has been added to role {}", ldapDn, role);
            }
            else {
                log.error("LDAP Server {} not found ", ldapServer);
                throw new CheckmarxException("LDAP Server not found");
            }
        }
        else{
            log.error("No LDAP Server provided");
            throw new CheckmarxException("LDAP Server not provided");
        }
    }

    /**
     * Remove an LDAP dn mapping for a role
     * @throws CheckmarxException
     */
    @Command(name = "remove-ldap")
    private void removeLdapMapping(
	    @Parameters(paramLabel = "LDAP Server") String ldapServer,
	    @Parameters(paramLabel = "Role") String role,
	    @Parameters(paramLabel = "Mapping") String ldapDn
) throws CheckmarxException{
        log.info("Calling role remove-ldap command");
        Integer roleId = cxService.getRoleId(role);
        if(roleId.equals(-1)){
            log.error("Could not find role {}", role);
            throw new CheckmarxException("Could not find role ".concat(role));
        }
        if(StringUtils.isNotEmpty(ldapServer)) {
            Integer serverId = cxService.getLdapServerId(ldapServer);
            if(serverId > 0) {
                cxService.removeRoleLdap(serverId, roleId, ldapDn);
                log.info("LDAP mapping {} has been removed from role {}", ldapDn, role);
            }
            else {
                log.error("LDAP Server {} not found ", ldapServer);
                throw new CheckmarxException("LDAP Server not found");
            }
        }
        else{
            log.error("No LDAP Server provided");
            throw new CheckmarxException("LDAP Server not provided");
        }
    }
}
