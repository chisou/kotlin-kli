package de.csou.arch.kli

import sun.plugin2.message.GetAppletMessage
import java.io.PrintWriter

abstract class Formatter {
    abstract fun formatOptions (options:List<Option>) : String
}

class StandardFormatter : Formatter() {
    override fun formatOptions (options:List<Option>) : String {

        return ""
    }
}

abstract class HelpOption (description: String, shortIds:List<Char>, longIds:List<String>) :
    FlagOption(description, shortIds, longIds)
{
    internal abstract fun printShortHelp (options:List<Option>, writer:PrintWriter)
    internal abstract fun printLongHelp (options:List<Option>, writer:PrintWriter)
}

class StandardHelpOption (private val formatter:Formatter=StandardFormatter()) :
    HelpOption("Display this help screen.", listOf('h', '?'), listOf("help"))
{
    private val INDENT = 3
    private val GAP = 3

    private var title = ""
    private var footer = ""
    private val usages = mutableListOf<String>()
    private val examples = mutableListOf<Pair<String,String>>()

    fun setTitle (title:String) : StandardHelpOption { this.title = title; return this }
    fun addUsage (usage:String) : StandardHelpOption { usages.add(usage); return this }
    fun addExample (example:String, description:String) : StandardHelpOption { examples.add(Pair(example, description)); return this }
    fun setFooter (footer:String) : StandardHelpOption { this.footer = footer; return this }

    override fun printShortHelp (options:List<Option>, writer:PrintWriter) {
        printUsages(writer)
        writer.println( "Try option '-h' or '--help' for more information." )
    }

    override fun printLongHelp (options:List<Option>, writer:PrintWriter) {
        // todo: print title
        printUsages(writer)
        if ( usages.isNotEmpty() ) writer.println()
        printExamples(writer)
        if ( examples.isNotEmpty() ) writer.println()
        printOptions(options, writer)
    }

    private fun printUsages (writer:PrintWriter) {
        if ( usages.size == 1)
            writer.println( "Usage: ${usages.first()}" )
        if ( usages.size > 1 ) {
            writer.println("Usages:")
            val indent = "           ".substring(0,INDENT)
            usages.forEach { writer.println(indent + it) }
        }
    }

    private fun printExamples (writer:PrintWriter) {
        if (examples.isNotEmpty()) {
            writer.println("Examples:")
            val offset = INDENT + examples.map{ it.first.length }.max()!! + GAP
            val indent = "           ".substring(0,INDENT)
            examples.forEach { (command, description) ->
                writer.print((indent + command).padEnd(offset))
                writer.println(description)
            }
        }
    }

    private fun printOptions (options:List<Option>, writer:PrintWriter) {

    }



















    private fun printHelp_Lexical (options:List<Option>) {
        val builder = StringBuilder()
        fun optionSyntax ( option:Option ) : String {
            val syntaxBuilder = StringBuilder()
            val valueString = if ( option is ValueOption<*> ) "<${option.type}>" else null
            if ( option.shortIds.isNotEmpty() ) {
                builder.append( '-' )
                builder.append( option.shortIds )
                if ( valueString != null ) {
                    builder.append(' ')
                    builder.append(valueString)
                }
            }
            builder.append( "  " )
            if ( option.longIds.isNotEmpty() ) {
                builder.append(  "--" )
                builder.append( option.longIds )
                if ( valueString != null ) {
                    builder.append( '=' )
                    builder.append( valueString )
                }
            }
            return syntaxBuilder.toString()
        }

        val optionSyntaxes = options.map( ::optionSyntax )
        val maxLength = optionSyntaxes.map{ it.length }.max() ?: 0

        options.forEachIndexed { i, option ->
            builder.append( "  " ) // indent
            builder.append( optionSyntaxes[i].padEnd( maxLength, ' ' ) )
            builder.append( "  " ) // space
            builder.append( option.description )
        }

//        return builder.toString()
    }

}
