# Java Utility for Checkmarx
 _Built leveraging the Checkmarx Spring boot SDK found here -> https://github.com/checkmarx-ts/checkmarx-spring-boot-java-sdk_

Initial implementation is for Team Creation and Ldap Management.  More to follow.

# Team Management
#### Create a team:
```
java -jar <util jar> -command=team -action=add-ldap -create -t="CxServer\SP\Checkmarx\NewTeam" -s="checkmarx.local" -m="CN=CX_USERS,DC=checkmarx,DC=local"
```
#### Delete a team:
```
java -jar <util jar> -command=team -action=add-ldap -create -t="CxServer\SP\Checkmarx\NewTeam" -s="checkmarx.local" -m="CN=CX_USERS,DC=checkmarx,DC=local"
```
#### Adding an Ldap Mapping (create team if it doesn't exist)
```
java -jar <util jar> -command=team -action=add-ldap -create -t="CxServer\SP\Checkmarx\NewTeam" -s="checkmarx.local" -m="CN=CX_USERS,DC=checkmarx,DC=local"
```
#### Remove an Ldap Mapping 
```
java -jar <util jar> -command=team -action=remove-ldap -t="CxServer\SP\Checkmarx\NewTeam" -s="checkmarx.local" -m="CN=CX_USERS,DC=checkmarx,DC=local"
```

#### Command line Options:
```
    -command, --command -> Command name    
    -action, --action -> Action to execute - create, delete, add-ldap, remove-ldap
    -t, --team -> Checkmarx Team
    -create, --create -> Create team if it does not exist (parent team must exist)
    -s, --ldap-server -> LDAP Server Name (only applicable for add/remove-ldap)
    -m, --add-ldap-map -> Add LDAP DN Mapping (only applicable for add/remove-ldap)
    -r, --remove-ldap-map -> Remove LDAP DN Mapping (only applicable for add/remove-ldap)
```

# Role Management

#### Map Role to LDAP DN:
```
java -jar <util jar> -command=role -action=add-ldap -s="checkmarx.local" -r="Admin" -m="CN=CX_USERS,DC=checkmarx,DC=local"
```
#### Remove LDAP Role Mapping:
```
java -jar <util jar> -command=role -action=remove-ldap -s="checkmarx.local" -r="Admin" -m="CN=CX_USERS,DC=checkmarx,DC=local"
```
#### Checkmarx Properties 8.x:
```yaml
checkmarx:
  username: xxxx #Checkmarx service account user
  password: xxxx #Checkmarx service account password
  client-secret: 014DF517-39D1-4453-B7B3-9930C563627C
  base-url: https://cx.local #Checkmarx base url
  url: ${checkmarx.base-url}/cxrestapi
  portal-url: ${checkmarx.base-url}/cxwebinterface/Portal/CxWebService.asmx
  sdk-url: ${checkmarx.base-url}/cxwebinterface/SDK/CxSDKWebService.asmx
  portal-wsdl: ${checkmarx.base-url}/Portal/CxWebService.asmx?wsdl
  sdk-wsdl: ${checkmarx.base-url}/SDK/CxSDKWebService.asmx?wsdl
  #NOTE: Teams have Windows path pattern \My\Team
```

#### Checkmarx Properties 9.0+:
```yaml
checkmarx:
  version: 9.0
  username: xxxx #Checkmarx service account user
  password: xxxx #Checkmarx service account password
  client-id: resource_owner_client
  client-secret: 014DF517-39D1-4453-B7B3-9930C563627C #Alternatively, register OIDC client
  scope: access_control_api sast_rest_api
  base-url: https://cx.local #Checkmarx base url
  url: ${checkmarx.base-url}/cxrestapi
  portal-url: ${checkmarx.base-url}/cxwebinterface/Portal/CxWebService.asmx
  sdk-url: ${checkmarx.base-url}/cxwebinterface/SDK/CxSDKWebService.asmx
  portal-wsdl: ${checkmarx.base-url}/Portal/CxWebService.asmx?wsdl
  sdk-wsdl: ${checkmarx.base-url}/SDK/CxSDKWebService.asmx?wsdl
  #NOTE: Teams have unix path pattern /My/Team
```
