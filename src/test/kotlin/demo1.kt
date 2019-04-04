import de.csou.arch.kli.*

fun main (args:Array<String>) {

    val kli = object:Kli() {
        val stringOption = StringOption(type="STRING", description="The string to process.", shortId='s', longId="input-string")
        val progressiveFlag = FlagOption("Whether to process the string in progressive mode", 'p', "progressive")
        val modernFlag = FlagOption("Whether to process the string in modern mode.", 'm', "modern")
    }

    kli.parse(args)
    println("Input file: ${kli.stringOption.value}")
    println("Progressive: ${kli.progressiveFlag.isDefined}")
    println("Modern: ${kli.modernFlag.isDefined}")
}