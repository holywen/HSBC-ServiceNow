import com.cloudbees.flowpdf.*

/**
* {{pluginClassName}}
*/
class {{pluginClassName}} extends FlowPlugin {

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
// === check connection template ===
    /** This is a special method for checking connection during configuration creation
    */
    def checkConnection(StepParameters p, StepResult sr) {
        // Use this pre-defined method to check connection parameters
        try {
            // Put some checks here
            def config = context.configValues
            log.info(config)
            // Getting parameters:
            {% for p in step.procedure.parameters -%}
            // log.info config.asMap.get('{{p.name}}')
            {% endfor %}
            // assert config.getRequiredCredential("credential").secretValue == "secret"
        }  catch (Throwable e) {
            // Set this property to show the error in the UI
            sr.setOutcomeProperty("/myJob/configError", e.message + System.lineSeparator() + "Please change the code of checkConnection method to incorporate your own connection checking logic")
            sr.apply()
            throw e
        }
    }
// === check connection template ends ===
// === check connection ends ===
// === step template ===

    /**
    * {{stepMethodName}} - {{step.procedure.name}}/{{step.name}}
    * Add your code into this method and it will be called when the step runs
    {% for p in step.procedure.parameters -%}
    * @param {{p.name}} (required: {{p.required}})
    {% endfor %}
    */
    def {{stepMethodName}}(StepParameters p, StepResult sr) {
        {% if parametersClassName -%}
        // Use this parameters wrapper for convenient access to your parameters
        {{parametersClassName}} sp = {{parametersClassName}}.initParameters(p)
        {%- endif %}

        // Calling logger:
        {% for p in step.procedure.parameters -%}
        log.info p.asMap.get('{{p.name}}')
        {% endfor %}

        // Setting job step summary to the config name
        sr.setJobStepSummary(p.getParameter('config')?.getValue() ?: 'null')

        sr.setReportUrl("Sample Report", 'https://cloudbees.com')
        sr.apply()
        log.info("step {{step.name}} has been finished")
    }

// === step template ends ===
// === step ends ===
// === feature step template ===
{{featureStepCode}}
// === feature step template ends ===

}
