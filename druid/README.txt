=== Plugin for Druid ===
 
This plugin supports integrating Ranger authorization with Druid ( http://druid.io/ ). 
Supported: 
 * granting read / write permissions based on user names and groups, and source IP address in the druid broker process.
 * listing druid datasources in the ranger admin UI for the autocomplete field. (For this to function properly, 
      a whitelisting is added, so any user can list all the datasources, even the ones,
      where they don't have rights to read)
Important:
 * The user name/group names is sent in the http request, without any password or encryption, so any rogue client could claim, that they are 'admin', 
   It is assumed, that working authentication is deployed in druid (e.g. kerberos), so the plugin could rely on that.
The Ranger base plugin handles the configuration a bit differently than other druid extensions do, so it's a bit intricate to customize it.

 = Configuring Ranger Server = 
 
1. Upload the druid service def to the ranger server: 
 curl -u admin:admin -X POST -H "Accept: application/json" -H "Content-Type: application/json" \
    –d @agents-common/src/main/resources/service-defs/ranger-servicedef-druid.json \
    http://localhost:8080/service/plugins/definitions
2. Create a new service in the ranger web UI for druid. Only the broker url is relevant, no username/password is needed to specify

 = Installation =

1. Create a directory ${DRUID}/extensions/ranger for the plugin
2. Copy the ranger-druid-plugin-${VERSION}.jar from ${RANGER}/druid/plugin-druid/target to ${DRUID}/extensions/ranger
3. Create a config jar based on the xmls found in ${RANGER}/druid/plugin-druid/conf after customizing to the actual deployment.
4. Copy the config jar to druid: ${DRUID}/extensions/ranger
5. Start the druid broker process with flags: "-Ddruid.extensions.loadList=[\"ranger\"] -Dranger.config.unsecureMode=true -Ddruid.auth.enabled=true"

 = Testing = 

If the wikiticker example datasource is added to the cluster the following should work : 
 curl -X POST 'http://localhost:8082/druid/v2/?user.name=admin' -H 'Content-Type:application/json' -d @wikiticker-select.json