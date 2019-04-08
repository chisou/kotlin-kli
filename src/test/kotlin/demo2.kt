import org.chisou.arch.kli.*

fun main (args:Array<String>) {

    val kli = object: Kli() {
        val helpOption = StandardHelpOption()
            .setTitle("The kotlin-kli demo application #2.")
            .addUsage("app [flags] -f FILE")
            .addExample("app -f FILE", "Processing file FILE in default mode.")
            .addExample("app -pf FILE", "Processing file FILE in progressive mode.")
            .setFooter("""Report bugs on: https://github.com/chisou/kotlin-kli/issues
                         |Released under the Apache License 2.0""".trimMargin())
        val stringOption = StringOption(type="STRING", description="The string to process.", shortId='s', longId="input-string", isMandatory=true)
        val progressiveFlag = FlagOption("Whether to process the string in progressive mode", null, "progressive")
        val modernFlag = FlagOption("Whether to process the string in modern mode.", 'm')
    }
    // alternative 1, automatic help + options checks
    kli.parse(args, validate=true)
    // alternative 2, explicit help + option checks
    if( kli.helpOption.isDefined ) kli.helpOption.printLongHelp(kli.options)
}