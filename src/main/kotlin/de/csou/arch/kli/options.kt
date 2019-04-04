package de.csou.arch.kli

import java.io.File

abstract class Option (
	val type:String,
	val description:String,
	internal val shortIds:List<Char>,
	internal val longIds:List<String>,
	val isMandatory:Boolean)
{
	var isDefined : Boolean = false
		internal set
}

open class FlagOption ( description:String, shortIds:List<Char>, longIds:List<String> ) :
	Option( "", description, shortIds, longIds, false) // flags don't have a type and are always optional
{
	constructor (description: String, shortId:Char?, longId:String?) :
			this(description, if(shortId!=null) listOf(shortId) else listOf(), if(longId!=null) listOf(longId) else listOf() )
}

abstract class ValueOption<T>( name:String, description:String, shortIds:List<Char>, longIds:List<String>, isMandatory:Boolean )	:
	Option( name, description, shortIds, longIds, isMandatory )
{
	var value : T? = null
		private set
	internal fun parseValue (value:String) {
		this.value = parse(value)
	}
	internal open fun isValid () : Boolean = true
	protected abstract fun parse (value:String ) : T
}

open class StringOption ( type:String, description:String, shortIds:List<Char>, longIds:List<String>, isMandatory:Boolean=false )
	: ValueOption<String>( type, description, shortIds, longIds, isMandatory )
{
	constructor (type:String, description: String, shortId:Char?, longId:String?, isMandatory:Boolean=false) :
			this(type, description, if(shortId!=null) listOf(shortId) else listOf(), if(longId!=null) listOf(longId) else listOf(), isMandatory )
	override fun parse ( value:String ) : String = value
}

abstract class FileOption (description:String, shortIds:List<Char>, longIds:List<String>, isMandatory:Boolean=false) :
		ValueOption<File>("FILE", description, shortIds, longIds, isMandatory)
{
	override fun parse (value:String ) : File = File(value)
	override fun isValid() =
		// isValid is only called after parse so this should never be null
		isDefined && value?.isFile!!
}

class ReadableFileOption (description:String, shortIds:List<Char>, longIds:List<String>, isMandatory:Boolean=false) :
	FileOption(description, shortIds, longIds, isMandatory)
{
	constructor (description: String, shortId:Char?, longId:String?, isMandatory:Boolean=false) :
			this(description, if(shortId!=null) listOf(shortId) else listOf(), if(longId!=null) listOf(longId) else listOf(), isMandatory )
	override fun isValid () =
		// isValid is only called after parse so this should never be null
		super.isValid() && value?.canRead()!!
}

class WritableFileOption (description:String, shortIds:List<Char>, longIds:List<String>, isMandatory:Boolean=false) :
	FileOption(description, shortIds, longIds, isMandatory)
{
	constructor (description: String, shortId:Char?, longId:String?, isMandatory:Boolean=false) :
			this(description, if(shortId!=null) listOf(shortId) else listOf(), if(longId!=null) listOf(longId) else listOf(), isMandatory )
	override fun isValid () =
		// isValid is only called after parse so this should never be null
		super.isValid() && value?.canWrite()!!
}

class RandomAccessFileOption (description:String, shortIds:List<Char>, longIds:List<String>, isMandatory:Boolean=false) :
	FileOption(description, shortIds, longIds, isMandatory)
{
	constructor (description: String, shortId:Char?, longId:String?, isMandatory:Boolean=false) :
			this(description, if(shortId!=null) listOf(shortId) else listOf(), if(longId!=null) listOf(longId) else listOf(), isMandatory )
	override fun isValid () =
		// isValid is only called after parse so this should never be null
		super.isValid() && value?.canWrite()!! && value?.canRead()!!
}



