package de.csou.arch.kli

import org.slf4j.LoggerFactory
import kotlin.reflect.full.declaredMemberProperties

abstract class Kli  {

	private val logger = LoggerFactory.getLogger( "de.csou.arch.kli.Kli" )

	private val mutableValues = mutableListOf<String>()
	val values:List<String> = mutableValues

	fun  buildHelp() : String {
		val options = this.javaClass.kotlin.declaredMemberProperties.map{ it.get(this) }.filterIsInstance<Option>()
		val builder = StringBuilder()

		fun optionSyntax ( option:Option ) : String {
			val syntaxBuilder = StringBuilder()
			val valueString = if ( option is ValueOption<*> ) "<${option.name}>" else null
			if ( option.shortId != null ) {
				builder.append( '-' )
				builder.append( option.shortId )
				if ( valueString != null ) {
					builder.append(' ')
					builder.append(valueString)
				}
			}
			builder.append( "  " )
			if ( option.longId != null ) {
				builder.append(  "--" )
				builder.append( option.longId )
				if ( valueString != null ) {
					builder.append( '=' )
					builder.append( valueString )
				}
			}
			return syntaxBuilder.toString()
		}

		val optionSytaxes = options.map( ::optionSyntax )
		val maxLength = optionSytaxes.map{ it.length }.max() ?: 0

		options.forEachIndexed { i, option ->
			builder.append( "  " ) // indent
			builder.append( optionSytaxes[i].padEnd( maxLength, ' ' ) )
			builder.append( "  " ) // space
			builder.append( option.description )
		}

		return builder.toString()
	}

	fun parse ( args:Array<String> ) {
		mutableValues.clear()
		// all options are defined as first class members of a derived instance
		val options = this.javaClass.kotlin.declaredMemberProperties.map{ it.get(this) }.filterIsInstance<Option>()
		val optionsByShort = options.associateBy { it.shortId }
		val optionsByLong = options.associateBy { it.longId }

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
							args[j]
							// todo: check for AIOOB
						}
				if (optionValue.isBlank()) {
					logger.error("Unable to find value string for option '${option.name}'. Ignored.")
				}
				option.isDefined = true
				option.parseValue(optionValue)
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
				if ( option is ValueOption<*> ){
					option.isDefined = true
					// when the argument string contains more characters they are assumed
					// to be the actual option value; otherwise the next argument will be taken
					// as the option value string.
					if (arg.length > argsIndex) {
						option.parseValue( arg.substring(optionIndex+1) )
						// todo: test when this substring is empty
						return argsIndex + 1
					}
					if ( argsIndex >= (args.size-1) ) {
						logger.error("Unable to find value string for option '${option.name}'. Ignored.")
						return argsIndex + 1
					}
					option.parseValue( args[argsIndex + 1] )
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

	}
}