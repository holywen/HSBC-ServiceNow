// === rest client imports starts ===
// === rest client imports template ===
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
// === rest client imports template ends ===
// === rest client imports ends ===
// Place for the custom user imports, e.g. import groovy.xml.*
// === rest client starts ===
// === rest client template ===


@InheritConstructors
class InvalidRestClientException extends Exception {

}


class ProxyConfig {
    String url
    String userName
    String password
}


class {{restClassName}} {


    private static String BEARER_PREFIX = '{% if rest.bearerPrefix %}{{rest.bearerPrefix}}{% else %}Bearer{% endif %}'
    private static String USER_AGENT = '{% if rest.userAgent %}{{rest.userAgent}}{% else %}{{restClassName}} REST Client{% endif %}'
    private static String CONTENT_TYPE = '{% if rest.contentType %}{{rest.contentType}}{% else %}application/json{% endif %}'
    private static OAUTH1_SIGNATURE_METHOD = '{% if rest.oauthSignatureMethod %}{{rest.oauthSignatureMethod}}{% else %}RSA-SHA1{% endif %}'

    String endpoint
    String method
    Map<String, String> methodParameters

    private Log log
    private REST rest
    private ProxyConfig proxyConfig

    {{restClassName}}(String endpoint, RESTConfig restConfig, FlowPlugin plugin) {
        this.endpoint = endpoint
        this.log = plugin.log
        this.rest = new REST(restConfig)
    }

    /**
     * Will create a {{restClassName}} object from the plugin Config object.
     * Convenient as it can use pre-defined configuration fields.
     */
    static {{restClassName}} fromConfig(Config config, FlowPlugin plugin) {
        Map params = [:]
        String endpoint = config.getRequiredParameter('endpoint').value.toString()
        Log log = plugin.log
        Credential credential
        RESTConfig restConfig = new RESTConfig()
        restConfig.endpoint = endpoint
        if ((credential = config.getCredential('bearer_credential')) && credential.secretValue) {
            restConfig.authScheme = 'bearer'
            restConfig.bearerToken = credential.secretValue
            restConfig.bearerPrefix = BEARER_PREFIX
            log.debug "Using bearer credential in REST Client"
        }
        else if ((credential = config.getCredential('basic_credential'))) {
            restConfig.authScheme = 'basic'
            restConfig.basicCredentialUsername = credential.userName
            restConfig.basicCredentialPassword = credential.secretValue
        }
        else if (config.isParameterHasValue('authScheme') && config.getParameter('authScheme').value == 'anonymous') {
            log.debug "Using anonymous auth scheme"
        }
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
        return new {{restClassName}}(endpoint, restConfig, plugin)
    }

    // Handles templates like {{ value }}, taking values from the params
    private static String renderOneLineTemplate(String uri, Map params) {
        for (String key in params.keySet()) {
            Object value = params.get(key)
            if (uri =~ /\{\{$key\}\}/) {
                if (value) {
                    uri = uri.replaceAll(/\{\{$key\}\}/, value as String)
                } else {
                    throw new InvalidRestClientException("A field $key is empty in params but required in the template")
                }
            }
        }
        return uri
    }


    /**
     * This is the main request method
     * methodString - GET|POST|PUT - request method
     * url - uri path (without the endpoint)
     * query - uri.query
     * payload - payload for POST/PUT requests
     * h - headers for the request
     */
    Object makeRequest(String methodString, String url, Map query, def payload, Map h) {
        Method method = Method.valueOf(methodString)

        HTTPRequest request = new HTTPRequest(
            method: method,
            path: url,
            query: query,
            headers: h
        )

        if (payload) {
            if (payload instanceof byte[]) {
                request.setContentBytes(payload)
            }
            else {
                request.setContentString(encodePayload(payload))
            }
        }

        if (method == Method.POST || method == Method.PUT || method == Method.PATCH) {
            if (!request.requestContentType) {
                request.requestContentType = ContentType.JSON
            }
        }
        request = rest.prepareRequest(request)
        request = augmentRequest(request)

        HTTPBuilder builder = rest.httpBuilder
        boolean success = true
        Tuple2 responseTuple = builder.request(method) {
            if (request.path) {
                def path = uri.path ?: ""
                path += '/' + request.path
                path = path.replaceAll(/\/+/, '/')
                uri.path = path
                log.trace("Set request path: $uri.path")
            }
            if (request.query) {
                uri.query = request.query
            }
            request.headers.each { headerName, headerValue ->
                headers.put(headerName, headerValue)
                log.trace "Added request header $headerName -> $headerValue"
            }
            if (request.contentString) {
                send request.requestContentType, request.contentString
                log.trace "Request body is $body"
            }

            if (request.contentBytes) {
                send request.requestContentType, request.contentBytes
            }

            response.success = { HttpResponse resp, decoded ->
                log.trace "Got response: $resp"
                log.trace "Got content: $decoded"

                return new Tuple2(resp, decoded)
            }
            response.failure = { HttpResponse resp, body ->
                log.trace "Request failed: $resp"
                log.trace "$body"
                success = false

                return new Tuple2(resp, body)
            }
        }
        HttpResponse response = responseTuple.first
        Object body = responseTuple.second

        Object processedResponse = processResponse(response, body)
        if (processedResponse) {
            return processedResponse
        }
        if (!success) {
            throw new HttpException("Request for uri $url failed: ${response.statusLine.statusCode}, ${body}")
        }
        Object parsed = parseResponse(response, body)
        return parsed
    }

    private static payloadFromTemplate(String template, Map params) {
        Object object = new JsonSlurper().parseText(template)
        object = fillFields(object, params)
        return object
    }

    private static def fillFields(def o, Map params) {
        def retval
        if (o instanceof Map) {
            retval = [:]
            for(String key in o.keySet()) {
                key = renderOneLineTemplate(key, params)
                def value = o.get(key)
                if (value instanceof String) {
                    value = renderOneLineTemplate(value, params)
                }
                else {
                    value = fillFields(value, params)
                }
                retval.put(key, value)
            }
        }
        else if (o instanceof List) {
            retval = []
            for (def i in o) {
                i = fillFields(i, params)
                retval.add(i)
            }
        }
        else if (o instanceof String) {
            o = renderOneLineTemplate(o, params)
            retval = o
        }
        else if (o instanceof Integer || o instanceof Boolean) {
            retval = o
        }
        else {
            throw new NotImplementedException()
        }
        return retval
    }

    {% for endpoint in rest.endpoints %}

    /** Generated code for the endpoint {{endpoint.urlTemplate}}
    * Do not change this code
    {%- for p in endpoint.parameters %}
    * {{p.parameterName}}: in {{p.placement}}
    {%- endfor %}
    */
    def {{endpoint.methodName}}(Map<String, Object> params) {
        this.method = '{{endpoint.methodName}}'
        this.methodParameters = params

        String uri = '{{endpoint.urlTemplate}}'
        log.debug("URI template $uri")
        uri = renderOneLineTemplate(uri, params)

        Map query = [:]
        {% for p in endpoint.parameters %}
        {% if p.placement == 'query' %}
        query.put('{{p.parameterName}}', params.get('{{p.parameterName}}'))
        {% endif %}
        {% endfor %}
        log.debug "Query: ${query}"

        Object payload
        {%- for p in endpoint.parameters %}
        {%- if p.placement == 'body' %}
        {%- set isMap = "true" %}
        {#- the object must be initialized, but it also may be a List #}
        {%- endif %}
        {%- endfor %}
        {% if isMap %}payload = [:]{%- endif %}
        {% for p in endpoint.parameters %}
        {% if p.placement == 'raw body' %}
        payload = new JsonSlurper().parseText(params.get('{{p.parameterName}}'))
        log.debug "Raw body payload: $payload"
        {% endif %}
        {% if p.placement == 'body' %}
        payload.put('{{p.parameterName}}', params.get('{{p.parameterName}}'))
        {% endif %}
        {% endfor %}
        String jsonTemplate = '''{{endpoint.jsonTemplate}}'''
        if (jsonTemplate) {
            payload = payloadFromTemplate(jsonTemplate, params)
            log.debug("Payload from template: $payload")
        }
        //TODO clean empty fields
        Map headers = [:]
        {%- for p in endpoint.parameters %}
        {%- if p.placement == 'header' %}
        headers.put('{{p.parameterName}}', params.get('{{p.parameterName}}'))
        {%- endif %}
        {%- endfor %}
        {%- for headerName, headerValue in endpoint.headers %}
        headers.put('{{headerName}}', renderOneLineTemplate('{{headerValue}}', params))
        {%- endfor %}
        return makeRequest('{{endpoint.HTTPMethod}}', uri, query, payload, headers)
    }
{% endfor %}

// === rest client template ends ===
// === rest client ends ===
    /**
     * Use this method for any request pre-processing: adding custom headers, binary files, etc.
     */
    HTTPRequest augmentRequest(HTTPRequest request) {
        return request
    }

    /**
     * Use this method to provide a custom encoding for you payload (XML, yaml etc)
     */
    Object encodePayload(def payload) {
        return JsonOutput.toJson(payload)
    }

    /**
     * Use this method to parse/alter response from the server
     */
    def parseResponse(HttpResponse response, Object body) {
        //Relying on http builder content type processing
        return body
    }

    /**
     * Use this method to alter default server response processing.
     * The response from this method will be returned as is, if any.
     * To disable response, just return null.
     */
    def processResponse(HttpResponse response, Object body) {
        return null
    }

}

