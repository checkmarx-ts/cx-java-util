package com.checkmarx.util.cmd;

import com.checkmarx.sdk.config.CxProperties;

public class CmdUtil {

    public static String addTeamPathSeparatorPrefix(CxProperties cxProperties, String team) {
        if (!team.startsWith(cxProperties.getTeamPathSeparator())) {
            team = cxProperties.getTeamPathSeparator().concat(team);
        }

        return team;
    }
}
