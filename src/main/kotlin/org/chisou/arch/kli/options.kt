package org.chisou.arch.kli

import java.io.File

abstract class Option (
	val type:String,
	val description:String,
	internal val shortId:Char?,
	internal val longId:String?,
	val isMandatory:Boolean)
{
	var isDefined : Boolean = false
		internal set
}

open class FlagOption ( description:String, shortId:Char?, longId:String? ) :
	Option( "", description, shortId, longId, false) // flags don't have a type and are always optional

abstract class ValueOption<T>( name:String, description:String, shortId:Char?, longId:String?, isMandatory:Boolean )	:
	Option( name, description, shortId, longId, isMandatory )
{
	var value : T? = null
		private set
	internal fun parseValue (value:String) {
		this.value = parse(value)
	}
	internal open fun isValid () : Boolean = true
	protected abstract fun parse (value:String ) : T
}

open class StringOption ( type:String, description:String, shortId:Char?, longId:String?, isMandatory:Boolean=false )
	: ValueOption<String>( type, description, shortId, longId, isMandatory )
{
	override fun parse ( value:String ) : String = value
}

abstract class FileOption (description:String, shortId:Char?, longId:String?, isMandatory:Boolean=false) :
		ValueOption<File>("FILE", description, shortId, longId, isMandatory)
{
	override fun parse (value:String ) : File = File(value)
	override fun isValid() =
		// isValid is only called after parse so this should never be null
		isDefined && value?.isFile!!
}

class ReadableFileOption (description:String, shortId:Char?, longId:String?, isMandatory:Boolean=false) :
	FileOption(description, shortId, longId, isMandatory)
{
	override fun isValid () =
		// isValid is only called after parse so this should never be null
		super.isValid() && value?.canRead()!!
}

class WritableFileOption (description:String, shortId:Char?, longId:String?, isMandatory:Boolean=false) :
	FileOption(description, shortId, longId, isMandatory)
{
	override fun isValid () =
		// isValid is only called after parse so this should never be null
		super.isValid() && value?.canWrite()!!
}

class RandomAccessFileOption (description:String, shortId:Char?, longId:String?, isMandatory:Boolean=false) :
	FileOption(description, shortId, longId, isMandatory)
{
	override fun isValid () =
		// isValid is only called after parse so this should never be null
		super.isValid() && value?.canWrite()!! && value?.canRead()!!
}



