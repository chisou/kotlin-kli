import de.csou.arch.kli.*

fun main (args:Array<String>) {

    val kli = object:Kli() {
        val helpOption = StandardHelpOption()
            .addUsage("app [flags] -f FILE")
            .addExample("app -f FILE", "Processing file FILE in default mode.")
            .addExample("app -pf FILE", "Processing file FILE in progressive mode.")
        val stringOption = StringOption(type="STRING", description="The string to process.", shortId='s', longId="input-string", isMandatory=true)
        val progressiveFlag = FlagOption("Whether to process the string in progressive mode", 'p', "progressive")
        val modernFlag = FlagOption("Whether to process the string in modern mode.", 'm', "modern")
    }
    kli.parse(args)//, validate=true)
    if( kli.helpOption.isDefined ) kli.helpOption.printLongHelp(kli.options)
}