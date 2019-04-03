import de.csou.arch.kli.*

fun main (args:Array<String>) {

    val kli = object:Kli() {
        val fileOption = StringOption(name="File", description="The input file to read.", shortId='f', longId="input-file")
        val progressiveFlag = FlagOption("Progressive", "Whether to read the file in progressive mode", 'p', "progressive")
        val modernFlag = FlagOption("Modern", "Whether to read the file in modern mode.", 'm', "modern")
    }

    kli.parse(args)
    println("Input file: ${kli.fileOption.value}")
    println("Progressive: ${kli.progressiveFlag.isDefined}")
    println("Modern: ${kli.modernFlag.isDefined}")
}