package org.chisou.arch.kli

import io.kotlintest.specs.FreeSpec
import org.amshove.kluent.*
import java.io.PrintWriter
import java.io.StringWriter


class HelpSpecs : FreeSpec({

    fun getShortHelp (helpOption: StandardHelpOption) =
        with(StringWriter()) {
            helpOption.printShortHelp(listOf(), PrintWriter(this))
            this.toString().toLowerCase()
        }

    fun getLongHelp (helpOption: StandardHelpOption, options:List<Option>) =
        with(StringWriter()) {
            helpOption.printLongHelp(options, PrintWriter(this))
            this.toString().toLowerCase()
        }

    "Given a default StandardHelpOption ..." - {
        val helpOption = StandardHelpOption()
        val shortText = getShortHelp(helpOption)
        val longText = getLongHelp(helpOption, listOf())
        "Tt should only display a general help message." {
            shortText `should contain` "--help"
            shortText `should contain` "-h"
        }
        "The long help text should be empty." {
            longText `should equal` ""
        }
        "There should be no examples in the help." {
            longText `should not contain` "example"
        }
    }

    "Given a StandardHelpOption with a single usages ..." - {
        val helpOption = StandardHelpOption().addUsage("usage1")
        val shortText = getShortHelp(helpOption)
        "It should have a total if two lines" {
            shortText.lines().size `should equal` 3 // there is a final newline, therefore 3
            shortText.lines()[2] `should equal` "" // but this 3rd line is blank
        }
        "It should write a single usage line" {
            shortText.lines()[0] `should contain` "usage:"
            shortText.lines()[0] `should contain` "usage1"
        }
        "It should also print the default help line" {
            shortText.lines()[1] `should contain` "--help"
        }
    }

    "Given a StandardHelpOption with multiple usages ..." - {
        val helpOption = StandardHelpOption().addUsage("text1").addUsage("text2")
        val shortText = getShortHelp(helpOption)
        "Tt should write the usages" {
            shortText `should contain` "text1"
            shortText `should contain` "text2"
        }
        "It should write the default help line." {
            shortText `should contain` "--help"
        }
        "It should indent the usage lines." {
            shortText.lines().filter{ it.contains("text") }.forEach {
                it `should start with` " "
            }
        }
        "It should print a separate header line." {
            val headerLine = shortText.lines().find{ it.contains("usage") }!!
            headerLine `should not contain` "text"
        }
    }

    "Given a StandardHelpOption with a single example ..." - {
        val helpOption = StandardHelpOption().addExample("EXAMPLE-HEAD", "EXAMPLE-DESC")
        val shortText = getShortHelp(helpOption)
        val longText = getLongHelp(helpOption, listOf())
        "The short long help text should not list the example." {
            shortText `should not contain` "example"
        }
        "The long help text should contain the example." {
            longText `should contain` "example"
        }
        "The long help text should have an example heading." {
            longText.lines().first() `should end with` ":"
        }
        "The example lines should be indented." {
            longText.lines().filter{ it.contains("example-head") }.forEach {
                it `should start with` " "
            }
        }
    }

    "Given a StandardHelpOption with a multiple example ..." - {
        val helpOption = StandardHelpOption()
            .addExample("head-1", "description-1")
            .addExample("longer-head-2", "description-2")
        val longText = getLongHelp(helpOption, listOf())
        val lines = longText.lines().filter{ it.contains("head") }
        "There should be two example lines." {
            lines.size `should equal` 2
        }
        "The example lines should be indented." {
            lines.forEach {
                it `should start with` " "
            }
        }
        "The example descriptions should be aligned." {
            val offsets = lines.map{ it.indexOf("description") }.distinct()
            offsets.size `should equal` 1
        }
    }

    "Given a single option ..." - {
        val helpOption = StandardHelpOption()
        val longText = getLongHelp(helpOption, listOf(
            StringOption(
                "string",
                "description",
                's',
                "long"
            )
        ))
        "There should be a options header" {
            longText.lines().first() `should end with` ":"
        }
        // description will be printed:
        val line = longText.lines().find{ it.contains("description") }!!
        "The short option ID should be printed" {
            line `should contain` "-s"
        }
        "The long option ID should be printed" {
            line `should contain` "--long"
        }
        "The line should be indented" {
            line `should start with` " "
        }
    }

    "Given multiple options ..." - {
        val helpOption = StandardHelpOption()
        val options = listOf(
            StringOption("", "description1", 'a', "a"),
            StringOption("", "description2", 'b', "option-b")
        )
        val longText = getLongHelp(helpOption, options)
        val lines = longText.lines().filter{ it.contains("description") }
        "The description texts should be aligned" {
            val offsets = lines.map{ it.indexOf("description") }.distinct()
            offsets.size `should equal` 1
        }
    }

})