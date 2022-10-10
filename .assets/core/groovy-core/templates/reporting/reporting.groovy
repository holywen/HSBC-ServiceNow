import com.cloudbees.flowpdf.*
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import com.cloudbees.flowpdf.components.reporting.Dataset
import com.cloudbees.flowpdf.components.reporting.Metadata
import com.cloudbees.flowpdf.components.reporting.Reporting

/**
 * User implementation of the reporting classes
 */
class {{reportingClassName}} extends Reporting {

    @Override
    int compareMetadata(Metadata param1, Metadata param2) {
        def value1 = param1.getValue()
        def value2 = param2.getValue()

        def pluginObject = this.getPluginObject()
        // Return 1 if there are newer records than record to which metadata is pointing.
        throw new NotImplementedException()
    }


    @Override
    List<Map<String, Object>> initialGetRecords(FlowPlugin flowPlugin, int i = 10) {
        def params = flowPlugin.getContext().getRuntimeParameters().getAsMap()
        throw new NotImplementedException()
        return records
    }

    @Override
    List<Map<String, Object>> getRecordsAfter(FlowPlugin flowPlugin, Metadata metadata) {
        def params = flowPlugin.getContext().getRuntimeParameters().getAsMap()
        def metadataValues = metadata.getValue()

        def log = flowPlugin.getLog()
        log.info("Got metadata value in getRecordsAfter:  ${metadataValues.toString()}")

        throw NotImplementedException()
        log.info("Records after GetRecordsAfter ${records.toString()}")
        return records
    }

    @Override
    Map<String, Object> getLastRecord(FlowPlugin flowPlugin) {
        def params = flowPlugin.getContext().getRuntimeParameters().getAsMap()
        def log = flowPlugin.getLog()
        log.info("Last record runtime params: ${params.toString()}")
        throw new NotImplementedException()
    }

    @Override
    Dataset buildDataset(FlowPlugin plugin, List<Map> records) {
        def dataset = this.newDataset(['your report object type'], [])
        def context = plugin.getContext()
        def params = context.getRuntimeParameters().getAsMap()

        def log = plugin.getLog()
        log.info("Start procedure buildDataset")

        log.info("buildDataset received params: ${params}")
        throw new NotImplementedException()
        return dataset
    }
}
