    /**
    * Procedure parameters:
    {% for parameter in procedure.parameters -%}
    * @param {{parameter.name}}
    {% endfor %}
    */
    def collectReportingData(StepParameters paramsStep, StepResult sr) {
        def params = paramsStep.getAsMap()

        throw new NotImplementedException()

        if (params['debug']) {
            log.setLogLevel(log.LOG_DEBUG)
        }

        {% if payloads.size() > 1 %}
        // Multiple Payloads
        {% for payloadName, payload in payloads %}
        if (params['reportObjectType'] == '{{payloadName}}') {
            {{reportingClassName}} reporting = ({{reportingClassName}}) ComponentManager.loadComponent({{reportingClassName}}.class, [
                reportObjectTypes  : ['{{payloadName}}'],
                metadataUniqueKey  : 'fill me in',
                payloadKeys        : ['fill me in'],
            ], this)
            reporting.collectReportingData()
        }
        {% endfor %}
        {% else %}
        {%- for payloadName, payload in payloads %}
        Reporting reporting = (Reporting) ComponentManager.loadComponent({{reportingClassName}}.class, [
                reportObjectTypes  : ['{{payloadName}}'],
                metadataUniqueKey  : 'fill me in',
                payloadKeys        : ['fill me in'],
        ], this)
        reporting.collectReportingData()
        {% endfor -%}
        {% endif %}
    }
