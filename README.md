# kotlin-kli
The kotlin-kli libray offers sleak command line parsing for the Kotlin programming language. It's primary goal is ease-of-use with minimal configuration overhead.

## Features

The key features are:
* Ability to parse standard POSIX command line options
  (both long versions `--long-option` and short options`-x`)
* Using introspection to build the option parser based on your application code
* Automatic generation of  usage and help texts (not yet implemented)
* Automatic validation of mandatory options (not yet implemented)
* Handling unspecified options

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

  val kli = object Kli {
    val fileOption = StringOption(name="File", description="The input file to read.", shortOption='f', longOption="input-file")
    val progressiveFlag = FlagOption("Progressive", "Whether to read the file in progressive mode", 'p', "progressive")
    val modernFlag = FlagOption("Modern", "Whether to read the file in modern mode.", 'm', "modern")
  }
  
  kli.parse(args)
  println("Input file: ${kli.fileOption.value}")
  println("Progressive: ${kli.progressiveFlag.isDefined}")
  println("Modern: ${kli.modern.isDefined}")
}
```
