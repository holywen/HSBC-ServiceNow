import groovy.json.JsonSlurper

def retval = []

def trigger = args.trigger
Map<String, String> headers = args.headers
String method = args.method
String body = args.body
String url = args.url
def query = args.query

retval << "Headers: $headers"
retval << "Method: $method"
retval << "Body: $body"
retval << "URL: $url"
retval << "Query: $query"
retval << "Trigger: $trigger"

def pluginParameters = [:]
trigger.pluginParameters.properties.each { k, v ->
    pluginParameters[k] = v['value']
}
{% for parameter in parameters %}
def {{parameter.varName}} = pluginParameters['{{parameter.spec.name}}']
retval << "Parameter ${{parameter.varName}}"
{% endfor %}
def event = 'push'

String message = retval.join("\n")

def response = [
    eventType      : event,
    launchWebhook  : true,
    branch         : 'master',
    responseMessage: message.toString(),
    webhookData: 'some webhook data',
]

return response
