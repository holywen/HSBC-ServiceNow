import com.cloudbees.flowpdf.*

/**
* HSBCServiceNow
*/
class HSBCServiceNow extends FlowPlugin {

    @Override
    Map<String, Object> pluginInfo() {
        return [
                pluginName     : '@PLUGIN_KEY@',
                pluginVersion  : '@PLUGIN_VERSION@',
                configFields   : ['config'],
                configLocations: ['ec_plugin_cfgs'],
                defaultConfigValues: [:]
        ]
    }
// === check connection ends ===
/**
     * Auto-generated method for the procedure CHG_GET_NEW/CHG_GET_NEW
     * Add your code into this method and it will be called when step runs* Parameter: config* Parameter: change
     */
    def cHG_GET_NEW(StepParameters p, StepResult sr) {
        CHG_GET_NEWParameters sp = CHG_GET_NEWParameters.initParameters(p)
        HSBCServiceNowCustomHeadersRESTClient rest = genHSBCServiceNowCustomHeadersRESTClient()
        Map restParams = [:]
        Map requestParams = p.asMap
        restParams.put('change', requestParams.get('change'))

        Object response = rest.CHG_GET_NEW(restParams)
        log.info "Got response from server: $response"
        //TODO step result output parameters
        sr.apply()
    }
/**
     * This method returns REST Client object
     */
    HSBCServiceNowCustomHeadersRESTClient genHSBCServiceNowCustomHeadersRESTClient() {
        Context context = getContext()
        HSBCServiceNowCustomHeadersRESTClient rest = HSBCServiceNowCustomHeadersRESTClient.fromConfig(context.getConfigValues(), this)
        return rest
    }
// === step ends ===

}