import org.chisou.arch.kli.*
import kotlin.system.exitProcess

fun main (args:Array<String>) {

    val cp = object: Kli() {
        val targetOption = WritableDirectoryOption( "The target directory to copy source files to.", 't', "target" )
        val helpOption = StandardHelpOption()
            .addUsage("cp SOURCE DEST")
            .addUsage("cp SOURCE ... DEST")
            .addUsage("cp -t DEST SOURCE ...")
    }

    try {
        cp.parse(args, validate=true, strict=true, fail=true)
        cp.checkArguments("SOURCE", "DEST", fail=true)
        if (cp.targetOption.isDefined && cp.values.size == 1)
            cp.checkArguments("SOURCE", fail=true)
        val sourceFiles = if (cp.targetOption.isDefined) {
                cp.parseAllArguments(ReadableFileOptionParser(), fail=true)!!
            } else {
                cp.parseHeadArguments(ReadableFileOptionParser(), fail=true)!!
            }
        val targetFile = if (cp.targetOption.isDefined) {
                cp.targetOption.value
            } else {
                cp.parseLastArgument(WritableFileOptionParser())
            }
        println("Copying $sourceFiles to $targetFile")
    } catch (ex:RuntimeException) {
        cp.error(ex.message!!, fail=false)
        exitProcess(Kli.ExitCode.CLI_ERROR)
    }

}