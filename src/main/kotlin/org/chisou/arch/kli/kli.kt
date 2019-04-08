package org.chisou.arch.kli

import com.sun.javaws.exceptions.InvalidArgumentException
import org.slf4j.LoggerFactory
import kotlin.reflect.full.declaredMemberProperties
import kotlin.system.exitProcess

abstract class Kli  {

	private enum class ExitCode(val n:Int) { ANY_ERROR(1), CLI_ERROR(2)	}
	private val logger = LoggerFactory.getLogger("de.csou.arch.kli")

	private val mutableValues = mutableListOf<String>()
	val values:List<String> = mutableValues

	val options:List<Option> by lazy {
		this.javaClass.kotlin.declaredMemberProperties.map{ it.get(this) }.filterIsInstance<Option>() }

	fun parse (args:Array<String>, validate:Boolean=false) {

		var valid = true
		mutableValues.clear()

		// all options are defined as first class members of a derived instance
		val optionsByShort = options.associateBy { it.shortId } // todo: how does this work for nulls
		val optionsByLong = options.associateBy { it.longId } // todo: how does this work for nulls

		// all option handling sub routines potentially move the pointer (index)
		// within the arguments array. Therefore they are fed with the entire array
		// and they will return the new current pointer when done.

		fun handleLongOption ( args:Array<String>, index:Int, optionString:String ) : Int {
			val optionKey = optionString.substringBefore('=')

			val option = optionsByLong[optionKey]
			if (option == null) {
				logger.warn("Unknown option '$optionString' ignored.")
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
							// todo: check for AIOOB
							args[j]
						}
				if (optionValue.isBlank()) {
					logger.error("Unable to find value string for option '${option.longId}'. Ignored.")
				}
				option.isDefined = true
				option.parseValue(optionValue)
				if(validate && !option.isValid()) {
					val hint = with(option.invalidityHint()) { if (isNotEmpty()) ": " + this else "." }
					logger.error("Invalid value for option '${option.longId}'" + hint )
					valid = false
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
					if ( argsIndex >= args.size ) {
						logger.error("Unable to find value string for option '${option.type}'. Ignored.")
						return argsIndex + 1
					}
					option.parseValue( args[argsIndex + 1] )
					option.isDefined = true
					if(validate && !option.isValid()) {
						val hint = with(option.invalidityHint()) { if (isNotEmpty()) ": " + this else "." }
						logger.error("Invalid value for option '${option.longId}'" + hint )
						valid = false
					}
					return argsIndex + 2
				}
				logger.error( "Unexpected option class: ${option.javaClass.kotlin}. Parsing result might be im corrupted." )
				optionIndex += 1
			}
			return argsIndex + 1
		}

		var i = 0
		while ( i < args.size ) {
			val arg = args[i]
			if ( arg[0] == '-' ) {
				if ( arg[1] == '-' ) {
					if ( arg.length == 2 )
						return
					i = handleLongOption( args, i, arg.substring(2) )
				} else {
					i = handleShortOption( args, i )
				}
			} else {
				mutableValues += arg
				i+=1
			}
		}

		/** Option validation & help text */

		if (validate) {
			// (1) verify whether all mandatory options have been provided
			options.filter{ it.isMandatory && !it.isDefined }.forEach { option ->
				// at least one of long ID or short ID is defined
				val id = if (option.longId!=null) option.longId else option.shortId!!
				logger.error( "Option '$id' must be defined." )
				valid = false
			}
			val helpOption = options.find{ it is HelpOption } as HelpOption?
			if (!valid) {
				helpOption?.printShortHelp(options)
				exitProcess(2)
			}
			if ( helpOption!=null && helpOption.isDefined ) {
				helpOption.printLongHelp(options)
				exitProcess(0)
			}
		}

	}
}