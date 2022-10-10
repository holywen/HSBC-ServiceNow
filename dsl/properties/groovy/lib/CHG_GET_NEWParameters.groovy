
// DO NOT EDIT THIS BLOCK BELOW=== Parameters starts ===
// PLEASE DO NOT EDIT THIS FILE

import com.cloudbees.flowpdf.StepParameters

class CHG_GET_NEWParameters {
    /**
    * Label: Change, type: entry
    */
    String change

    static CHG_GET_NEWParameters initParameters(StepParameters sp) {
        CHG_GET_NEWParameters parameters = new CHG_GET_NEWParameters()

        def change = sp.getRequiredParameter('change').value
        parameters.change = change

        return parameters
    }
}
// DO NOT EDIT THIS BLOCK ABOVE ^^^=== Parameters ends, checksum: 77892e3ce90cc942cd8845ce807a44b9 ===