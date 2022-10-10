import com.cloudbees.flowpdf.*
import com.cloudbees.flowpdf.client.HTTPRequest
import com.cloudbees.flowpdf.client.REST
import com.cloudbees.flowpdf.client.RESTConfig
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.InheritConstructors
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.http.HttpException
import org.apache.http.HttpResponse
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials

class HSBCServiceNowCustomHeadersRESTClient extends HSBCServiceNowRESTClient{

    private static String BEARER_PREFIX = 'Bearer'
    private static String USER_AGENT = 'HSBCServiceNowCustomHeadersRESTClient REST Client'
    private static String CONTENT_TYPE = 'application/json'
    private static OAUTH1_SIGNATURE_METHOD = 'RSA-SHA1'

    private Log log
    private REST rest
    private ProxyConfig proxyConfig
    private customHeaders

    HSBCServiceNowCustomHeadersRESTClient(String endpoint, RESTConfig restConfig, FlowPlugin plugin, customHeaders) {
        super(endpoint, restConfig, plugin)
        this.endpoint = endpoint
        this.log = plugin.log
        this.rest = new REST(restConfig)
        this.customHeaders = customHeaders
    }

    /**
     * Will create a HSBCServiceNowCustomHeadersRESTClient object from the plugin Config object.
     * Convenient as it can use pre-defined configuration fields.
     */
    static HSBCServiceNowCustomHeadersRESTClient fromConfig(Config config, FlowPlugin plugin) {
        Map params = [:]
        String endpoint = config.getRequiredParameter('endpoint').value.toString()
        Log log = plugin.log
        Credential credential
        RESTConfig restConfig = new RESTConfig()
        restConfig.endpoint = endpoint
        restConfig.authScheme = 'anonymous'

        if (config.isParameterHasValue('httpProxyUrl')) {
            String proxyUrl = config.getParameter('httpProxyUrl').value
            restConfig.httpProxyUrl = proxyUrl
            log.debug "Using proxy $proxyUrl"
            if ((credential = config.getCredential('proxy_credential'))) {
                restConfig.proxyCredentialUsername = credential.userName
                restConfig.proxyCredentialPassword = credential.secretValue
                log.debug "Using proxy authorization"
            }
        }

        def jsonSlurper = new JsonSlurper()
        def customHeaders = jsonSlurper.parseText(config.getCredential('authHeaders_credential').secretValue) 
        return new HSBCServiceNowCustomHeadersRESTClient(endpoint, restConfig, plugin, customHeaders)
    }

    HTTPRequest augmentRequest(HTTPRequest request) {
        super.augmentRequest(request)
        request.headers.putAll(customHeaders)
        return request
    }

}