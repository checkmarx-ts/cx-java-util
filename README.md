# Java Utility for Checkmarx
 _Built leveraging the Checkmarx Spring boot SDK found here -> https://github.com/checkmarx-ltd/checkmarx-spring-boot-java-sdk_

Initial implementation is for Team Creation and Ldap Management.  More to follow.

# Project Management
#### Set a Project’s Custom Fields
```
java -jar <util jar> project set-custom-fields netgoat varA=valA varB=ValB
```

# Team Management
#### Create a team:
```
java -jar <util jar> team create "CxServer\SP\Checkmarx\NewTeam"
```
#### Delete a team:
```
java -jar <util jar> team delete "CxServer\SP\Checkmarx\NewTeam"
```
#### Adding an Ldap Mapping (create team if it doesn't exist)
```
java -jar <util jar> team add-ldap -create "CxServer\SP\Checkmarx\NewTeam" "checkmarx.local" "CN=CX_USERS,DC=checkmarx,DC=local"
```
#### Remove an Ldap Mapping 
```
java -jar <util jar> team remove-ldap "CxServer\SP\Checkmarx\NewTeam" "checkmarx.local" "CN=CX_USERS,DC=checkmarx,DC=local"
```

# Role Management

#### Map Role to LDAP DN:
```
java -jar <util jar> role add-ldap "checkmarx.local" "Admin" "CN=CX_USERS,DC=checkmarx,DC=local"
```
#### Remove LDAP Role Mapping:
```
java -jar <util jar> role remove-ldap "checkmarx.local" "Admin" "CN=CX_USERS,DC=checkmarx,DC=local"
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
