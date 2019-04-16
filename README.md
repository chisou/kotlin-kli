# kotlin-kli
The kotlin-kli libray offers sleak command line parsing for the Kotlin programming language. It uses class introspection to automatically discover defined options in your code.

It's primary goal is ease-of-use with minimal configuration overhead.

## Features

The key features are:
* Ability to parse standard POSIX command line options
  (both long versions `--long-option` and short options`-x`)
* Using introspection to build the option parser based on your application code
* Handling unspecified options
* Automatic generation of  usage and help texts
* Automatic validation of mandatory options
* Automatic validation of option values (numbers, files, directories)

## Out-of-scope (for now)

* Parsing Windows-style options (`/h` and alike)

## Example

The following example code can parse the following command lines:
* `app -finput.txt -pm` i.e. "abbreviated old-school short options"
* `app -f input.txt -p -m` i.e. "regular single-character options"
* `app --input-file=input.txt --progressive --modern` i.e. "modern GNU style options"

It can also parse combinations of the above, like
* `app -pm --input-file=input.txt`
* `app --modern -p --input-file input.txt`

```kotlin
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
```

## Validation

kotlin-kli can automatically validate the option values during or after the argument parsing by passing the option
validate parameter, e.g.
```kotlin
kli.parse(args, validate=true)
```

Supported validation rules are:
* Mandatory/option values 
* Value classes: Integers, Decimals 
* Readable/Writable files
* Readable/Writable directories

## Help text

kotlin-kli includes a predefined `HelpOption` class which, when added to your `Kli` object can automatically
print a help text to the console.

The following code 

```kotlin
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
    kli.parse(args, validate=true)
}
``` 

will print the following help text when invoked with `-h` or `--help`:

```
Usage: app [flags] -f FILE

Examples:
   app -f FILE    Processing file FILE in default mode.
   app -pf FILE   Processing file FILE in progressive mode.

Options:
   -h, --help           Display this help screen.
   -m, --modern         Whether to process the string in modern mode.
   -p, --progressive    Whether to process the string in progressive mode
   -s, --input-string   The string to process.
```
