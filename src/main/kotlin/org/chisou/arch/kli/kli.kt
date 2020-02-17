package org.chisou.arch.kli

import org.slf4j.Logger
import kotlin.reflect.full.declaredMemberProperties

import org.slf4j.LoggerFactory
import java.lang.RuntimeException

abstract class Kli()  {

	companion object {
		internal const val LOGGER_NAME = "org.chisou.arch.kli" // we need this available to be able to mock the logger
	}

	/** Standard POSIX error codes. */
	object ExitCode {
		const val NO_ERROR  = 0
		const val ANY_ERROR = 1
		const val CLI_ERROR = 2
	}

	private val logger:Logger = LoggerFactory.getLogger(LOGGER_NAME)

	private val mutableValues = mutableListOf<String>()
	private var valid:Boolean = true

	val values:List<String> = mutableValues
	val firstValue:String
		get() = values.first()
	val lastValue:String
		get() = values.last()

	internal val internalHelpOptionReference:HelpOption? by lazy {
		this.options.find{ it is HelpOption } as HelpOption? }

	open val options:List<Option> by lazy {
		this.javaClass.kotlin.declaredMemberProperties.map{ it.get(this) }.filterIsInstance<Option>() }

	/** Parse a command line given as array of string tokens.
	 *
	 *  By default, this function will only parse and automatically validate. The behavior can be changed through flags.
	 *  The result of the parse process can be checked through the [isValid] property.
	 *
	 *  @param validate enable/disable validation of command line options during parse
	 *  	If enabled, will both check if mandatory options are provided and validate the type of parsed options.
	 *  @param strict  enable/disable strict parse mode
	 *  	If enabled, parsing will be strict and report wrongly formatted option strings.
	 *  @param fail enable/disable immediate failing in case of errors
	 *  	If enabled, any error during parsing will result in an immediate [RuntimeException] being through.
	 */
	fun parse (args:Array<String>, validate:Boolean=false, strict:Boolean=false, fail:Boolean=false)  {
		valid = true
		mutableValues.clear()

		// all options are defined as first class members of a derived instance
		val optionsByShort = options.associateBy { it.shortId } // todo: how does this work for nulls
		val optionsByLong = options.associateBy { it.longId } // todo: how does this work for nulls

		fun warn (message:String) {
			if(strict) valid = false
			warn(message, strict, fail)
		}

		fun error (message:String) {
			valid = false
			error(message, fail)
		}

		// all option handling sub routines potentially move the pointer (index)
		// within the arguments array. Therefore they are fed with the entire array
		// and they will return the new current pointer when done.

		fun handleLongOption ( args:Array<String>, index:Int, optionString:String ) : Int {
			val optionKey = optionString.substringBefore('=')

			val option = optionsByLong[optionKey]
			if (option == null) {
				warn("Unknown option '$optionString' ignored.")
				return index + 1
			}
			if (option is FlagOption) {
				option.isDefined = true
				return index + 1
			}
			if (option is ValueOption<*>) {
				var j = index
				val optionValue =
						if (optionString.contains('=')) {
							optionString.substringAfter('=')
						} else {
							j += 1
							if ( j < args.size) args[j] else ""
						}
				if (optionValue.isBlank()) {
					error("Unable to find value string for option '${option.name}'. Ignored.")
				} else {
					if (optionValue.startsWith('-'))
						logger.info("Suspicious value '$optionValue' for option '${option.name}'. Looks like another option.")
					option.isDefined = true
					option.parseValue(optionValue)
					if (validate && !option.isValid()) {
						error("Invalid value for option '${option.name}': ${option.invalidityHint()}")
					}
				}
				return j + 1
			}
			return index + 1
		}

		fun handleShortOption ( args:Array<String>, argsIndex:Int ) : Int {

			val arg = args[argsIndex]
			var optionIndex = 1
			while (optionIndex < arg.length) {
				// read option character
				val optionCharacter = arg[optionIndex]
				val option = optionsByShort[optionCharacter]
				if (option == null) {
					logger.warn("Unknown option character '$optionCharacter' ignored.")
					optionIndex += 1
					continue
				}
				// when the option is a flag, we will check for more options (within this
				// argument string); otherwise we will resolve a value and are done for
				// this argument string.
				if (option is FlagOption) {
					option.isDefined = true
					optionIndex += 1
					continue
				}
				if ( option is ValueOption<*>){
					// when the argument string contains more characters they are assumed
					// to be the actual option value; otherwise the next argument will be taken
					// as the option value string.
					if (arg.length > optionIndex+1) {
						option.parseValue( arg.substring(optionIndex+1) )
						// todo: test when this substring is empty
						option.isDefined = true
						return argsIndex + 1
					}
					// check if there is no additional argument to use as option value
					// (to prevent a 'out of bounds' exception)
					if ( argsIndex >= args.size-1 ) {
						warn("Unable to find value string for option '${option.name}'. Ignored.")
						return argsIndex + 1
					}
					// parse the next argument as a whole as the value
					val value = args[argsIndex + 1]
					if (value.startsWith('-'))
						logger.info("Suspicious option value '$value'. Looks like another option.")
					option.parseValue( value )
					option.isDefined = true
					if(validate && !option.isValid()) {
						error("Invalid value for option '${option.name}': ${option.invalidityHint()}" )
					}
					return argsIndex + 2
				}
				error( "Unexpected option class: ${option.javaClass.kotlin}. Parsing result might be corrupted." )
				optionIndex += 1
			}
			return argsIndex + 1
		}

		// Walk through the individual arguments one by one; each may be a
		// short option string, a long option string, an option value or a
		// value unaccounted for

		var i = 0
		while ( i < args.size ) {
			val arg = args[i]
			if ( arg.isNotEmpty() && arg[0] == '-' ) {
				if ( arg[1] == '-' ) {
					if ( arg.length == 2 ) {
						mutableValues.addAll(args.copyOfRange(i + 1, args.size))
						i = args.size
					}
					i = handleLongOption( args, i, arg.substring(2) )
				} else {
					i = handleShortOption( args, i )
				}
			} else {
				mutableValues += arg
				i+=1
			}
		}

		// After parsing we can validate whether all mandatory options
		// were provided. The option value validation has already happened
		// during the parse process above

		if (validate) {
			// (1) verify whether all mandatory options have been provided
			options.filter { it.isMandatory && !it.isDefined }.forEach { option ->
				// at least one of long ID or short ID is defined
				error("Option '${option.name}' must be defined.")
			}
		}
	}

	/** Determine if the last parse process was successful.
	 */
	fun isValid () : Boolean = valid

	/** Print a brief help message.
	 *  This will result in a [NullPointerException] if no help option was specified.
	 */
	fun printShortHelp () =
		internalHelpOptionReference?.printShortHelp(options)

	/** Print a detailled help message.
	 *  This will result in a [NullPointerException] if no help option was specified.
	 */
	fun printLongHelp () =
		internalHelpOptionReference?.printLongHelp(options)

	/** Check positional arguments.
	 *  All tokens provided after the last option are considered to be _positional arguments_.
	 *  This function is used to verify the number of such arguments provided.
	 *
	 *  @param names Sequence of expected arguments
	 *  	The provided names are used within error messages only.
	 */
	fun checkArguments (vararg names:String, fail:Boolean=false) {
		if (values.isEmpty()) {
			error("Missing positional arguments.", fail)
		} else if (values.size < names.size) {
			val n_missing = names.size - values.size
			if (n_missing == 1) { // just one argument missing
				error("Argument '${names.last()}' must be provided.", fail)
			} else { // multiple arguments are missing
				val missingNames = names.copyOfRange(names.size - n_missing, names.size)
				error("Missing positional arguments: ${missingNames.joinToString()}", fail)
			}
		}
	}

	/** Parse positional arguments.
	 *
	 *  @param fromIndex start index of the arguments to parse (inclusive)
	 *  @param toIndex end index of the arguments to parse (exclusive)
	 *  @param parser value parser to use (instance of [ValueParser])
	 *  @param fail enable/disable immadiate failing in case of errors
	 *  	If enabled, a [RuntimeException] will be thrown in case of parse errors
	 *
	 *  @return a typed list of the parsed arguments. `null` in case of errors.
	 */
	fun <T> parseArguments (fromIndex:Int, toIndex:Int, parser:ValueParser<T>, fail:Boolean):List<T>? {
		val results = mutableListOf<T>()
		values.subList(fromIndex, toIndex).forEach {
			val result = parser.parse(it)
			if(!result.isValid()) {
				error("Invalid value '$it': ${result.hint}", fail)
				return null
			}
			results.add(result.value!!)
		}
		return results
	}

	/** Parse all provided positional arguments.
	 *
	 *  @param parser value parser to use (instance of [ValueParser])
	 *  @param fail enable/disable immadiate failing in case of errors
	 *  	If enabled, a [RuntimeException] will be thrown in case of parse errors
	 *
	 *  @return a typed list of the parsed arguments. `null` in case of errors.
	 */
	fun <T> parseAllArguments (parser:ValueParser<T>, fail:Boolean=false) : List<T>? =
		parseArguments(0, values.size, parser, fail)

	/** Parse all but the last provided positional arguments.
	 *
	 *  @param parser value parser to use (instance of [ValueParser])
	 *  @param fail enable/disable immadiate failing in case of errors
	 *  	If enabled, a [RuntimeException] will be thrown in case of parse errors
	 *
	 *  @return a typed list of the parsed arguments. `null` in case of errors.
	 */
	fun <T> parseHeadArguments (parser:ValueParser<T>, fail:Boolean=false) : List<T>? =
		parseArguments(0, values.size-1, parser, fail)

	/** Parse all but the first provided positional arguments.
	 *
	 *  @param parser value parser to use (instance of [ValueParser])
	 *  @param fail enable/disable immadiate failing in case of errors
	 *  	If enabled, a [RuntimeException] will be thrown in case of parse errors
	 *
	 *  @return a typed list of the parsed arguments. `null` in case of errors.
	 */
	fun <T> parseTailArguments (parser:ValueParser<T>, fail:Boolean=false) : List<T>? =
		parseArguments(1, values.size, parser, fail)

	/** Parse the first provided positional argument.
	 *
	 *  @param parser value parser to use (instance of [ValueParser])
	 *  @param fail enable/disable immadiate failing in case of errors
	 *  	If enabled, a [RuntimeException] will be thrown in case of parse errors
	 *
	 *  @return a typed list of the parsed arguments. `null` in case of errors.
	 */
	fun <T> parseFirstArgument (parser:ValueParser<T>, fail:Boolean=false) : T? =
		parseArguments(0, 1, parser, fail)?.first()

	/** Parse the last provided positional argument.
	 *
	 *  @param parser value parser to use (instance of [ValueParser])
	 *  @param fail enable/disable immadiate failing in case of errors
	 *  	If enabled, a [RuntimeException] will be thrown in case of parse errors
	 *
	 *  @return a typed list of the parsed arguments. `null` in case of errors.
	 */
	fun <T> parseLastArgument (parser:ValueParser<T>, fail:Boolean=false) : T? =
		parseArguments(values.size-1, values.size, parser, fail)?.first()

	internal fun error (message:String, fail:Boolean) {
		logger.error(message)
		if (fail) throw RuntimeException(message)
	}

	internal fun warn (message:String, strict:Boolean, fail:Boolean) {
		logger.warn(message)
		if (strict && fail) throw RuntimeException(message)
	}

}