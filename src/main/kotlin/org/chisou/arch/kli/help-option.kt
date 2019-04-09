package org.chisou.arch.kli

import java.io.PrintWriter

abstract class HelpOption (description: String, shortId:Char?, longId:String?) :
    FlagOption(description, shortId, longId)
{
    internal abstract fun printShortHelp (options:List<Option>, writer:PrintWriter=PrintWriter(System.out))
    internal abstract fun printLongHelp (options:List<Option>, writer:PrintWriter=PrintWriter(System.out))
}

class StandardHelpOption :
    HelpOption("Display this help screen.", 'h', "help")
{
    private val INDENT_LEN = 3
    private val GAP_LEN = 3
    private val INDENT_STRING = spaces(INDENT_LEN)

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
        writer.flush()
    }

    override fun printLongHelp (options:List<Option>, writer:PrintWriter) {
        if ( title.isNotEmpty() ) {
            writer.println(title)
            writer.println()
        }
        printUsages(writer)
        if ( usages.isNotEmpty() ) writer.println()
        printExamples(writer)
        if ( examples.isNotEmpty() ) writer.println()
        printOptions(options, writer)
        if ( footer.isNotEmpty() ) {
            writer.println()
            writer.println(footer)
        }
        writer.flush()
    }

    private fun printUsages (writer:PrintWriter) {
        if ( usages.size == 1)
            writer.println( "Usage: ${usages.first()}" )
        if ( usages.size > 1 ) {
            writer.println("Usages:")
            usages.forEach { writer.println(INDENT_STRING + it) }
        }
    }

    private fun printExamples (writer:PrintWriter) {
        if (examples.isNotEmpty()) {
            writer.println("Examples:")
            val offset = INDENT_LEN + examples.map{ it.first.length }.max()!! + GAP_LEN
            examples.forEach { (command, description) ->
                writer.print((INDENT_STRING + command).padEnd(offset))
                writer.println(description)
            }
        }
    }

    private fun printOptions (options:List<Option>, writer:PrintWriter) {

        if (options.isEmpty()) return

        //  General printing layout
        //   -x, --long-option   Description

        fun calculateLength(option: Option) : Int =
            option.longId?.length!! + if ( option.type.isNotEmpty() ) option.type.length + 1 else 0

        // determine the maximum long option length to be able to align descriptions
        val maxLongOptionLength = options.filter{ it.longId!=null }.map(::calculateLength).max()!! + 2

        // the filler/gap to fill when there is no short option
        val shortFiller = spaces(INDENT_LEN+4)
        // the filler/gap to fill when there is no long option
        val longFiller = spaces(2+maxLongOptionLength+GAP_LEN)

        writer.println("Options:")
        options.forEach { option ->
            // print the short ID or a filler
            if (option.shortId != null) {
                writer.print(INDENT_STRING)
                writer.print("-${option.shortId}, ")
            } else {
                writer.print(shortFiller)
            }
            // print the long ID or a filler (plus alignment spaces)
            if (option.longId != null) {
                val optionString = "--" + option.longId + if (option.type.isNotEmpty()) "=" + option.type else ""
                writer.print(optionString)
                val gap = (maxLongOptionLength - optionString.length) + GAP_LEN
                writer.print(spaces(gap))
            } else {
                writer.print(longFiller)
            }
            // print the description
            writer.println(option.description)
        }


    }

    private fun spaces ( n:Int ) : String {
        return "                                            ".substring(0, n)
    }

}
