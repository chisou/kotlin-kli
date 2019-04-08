package org.chisou.arch.kli

import java.io.File
import java.io.RandomAccessFile

abstract class Option (
	val type:String,
	val description:String,
	internal val shortId:Char?,
	internal val longId:String?,
	val isMandatory:Boolean)
{
	init {
		if (shortId==null && longId==null)
			throw InstantiationError("Option instances must define at least one of short ID or long ID.")
	}

	var isDefined : Boolean = false
		internal set
}

open class FlagOption ( description:String, shortId:Char?=null, longId:String?=null ) :
	Option( "", description, shortId, longId, false) // flags don't have a type and are always optional

abstract class ValueOption<T>( name:String, description:String, shortId:Char?, longId:String?, isMandatory:Boolean )	:
	Option( name, description, shortId, longId, isMandatory )
{
	var value : T? = null
		private set
	internal fun parseValue (value:String) {
		this.value = parse(value)
		isDefined = true
	}
	internal open fun isValid () : Boolean = true
	internal open  fun invalidityHint() : String = ""
	protected abstract fun parse (value:String ) : T
}

open class StringOption ( type:String, description:String, shortId:Char?=null, longId:String?=null, isMandatory:Boolean=false )
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
	override fun invalidityHint() = if (isValid()) "" else "No such file."
}

class ReadableFileOption (description:String, shortId:Char?=null, longId:String?=null, isMandatory:Boolean=false) :
	FileOption(description, shortId, longId, isMandatory)
{
	override fun isValid () = super.isValid() && value?.canRead()!!
	override fun invalidityHint() = if (!super.isValid()) super.invalidityHint() else "Not readable."
}

class WritableFileOption (description:String, shortId:Char?=null, longId:String?=null, isMandatory:Boolean=false) :
	FileOption(description, shortId, longId, isMandatory)
{
	override fun isValid () = super.isValid() && value?.canWrite()!!
	override fun invalidityHint() = if (!super.isValid()) super.invalidityHint() else "Not writable."
}

enum class RandomAccessMode { READ, READ_WRITE }

class RandomAccessFileOption (val mode:RandomAccessMode, description:String, shortId:Char?, longId:String?, isMandatory:Boolean=false) :
	ValueOption<RandomAccessFile>("FILE", description, shortId, longId, isMandatory)
{
	override fun parse (value:String ) : RandomAccessFile = RandomAccessFile(value, resolveModeString(mode))
	override fun isValid() = isDefined && value?.fd?.valid()!!
	override fun invalidityHint() = if (isValid()) "" else "Unable to open file."

	private fun resolveModeString (mode:RandomAccessMode) = when (mode) {
		RandomAccessMode.READ -> "r"
		RandomAccessMode.READ_WRITE -> "rw"
	}
}

abstract class DirectoryOption (description:String, shortId:Char?, longId:String?, isMandatory:Boolean=false) :
	ValueOption<File>("DIR", description, shortId, longId, isMandatory)
{
	override fun parse (value:String ) : File = File(value)
	override fun isValid() =
	// isValid is only called after parse so this should never be null
		isDefined && value?.isDirectory!!
	override fun invalidityHint() = if (isValid()) "" else "No such directory."
}

class ReadableDirectoryOption (description:String, shortId:Char?, longId:String?, isMandatory:Boolean=false) :
	DirectoryOption(description, shortId, longId, isMandatory)
{
	override fun parse (value:String ) : File = File(value)
	override fun isValid() = super.isValid() && value?.canRead()!!
	override fun invalidityHint() = if (!super.isValid()) super.invalidityHint() else "Unable to read from directory."
}

class WritableDirectoryOption (description:String, shortId:Char?, longId:String?, isMandatory:Boolean=false) :
	DirectoryOption(description, shortId, longId, isMandatory)
{
	override fun parse (value:String ) : File = File(value)
	override fun isValid() = super.isValid() && value?.canWrite()!!
	override fun invalidityHint() = if (!super.isValid()) super.invalidityHint() else "Unable to write to directory."
}

