// === step template ===
    /**
     * Auto-generated method for the procedure {{ step.procedure.name }}/{{step.name}}
     * Add your code into this method and it will be called when step runs
     {%- for parameter in  step.procedure.parameters -%}
     * Parameter: {{ parameter.name }}
     {%- endfor %}
     */
    def {{stepMethodName}}(StepParameters p, StepResult sr) {
        {{parametersClassName}} sp = {{parametersClassName}}.initParameters(p)
        {%- set getClientMethod = "gen" ~ restClassName %}
        {{restClassName}} rest = {{getClientMethod}}()
        Map restParams = [:]
        Map requestParams = p.asMap
        {%- for param in step.procedure.parameters -%}
        {%- if param.restParameter %}
        restParams.put('{{ param.restParameter.parameterName}}', requestParams.get('{{param.name}}'))
        {%- endif -%}
        {% endfor %}

        Object response = rest.{{step.procedure.restEndpoint.methodName}}(restParams)
        log.info "Got response from server: $response"
        //TODO step result output parameters


        {%- for op in step.procedure.outputParameters %}
        {% if op.rest -%}
        sr.setOutputParameter('{{op.name}}', JsonOutput.toJson(response))
        {%- endif -%}
        {%- endfor %}
        sr.apply()
    }
    // === step template ends ===
    // === rest client template ===

    /**
     * This method returns REST Client object
     */
    {{restClassName}} gen{{restClassName}}() {
        Context context = getContext()
        {{restClassName}} rest = {{restClassName}}.fromConfig(context.getConfigValues(), this)
        return rest
    }
// === rest client template ends ===
