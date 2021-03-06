package org.chisou.arch.kli

import java.io.File
import java.io.FileNotFoundException
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

	val name : String = longId ?: shortId?.toString()!!

	var isDefined : Boolean = false
		internal set
}

open class FlagOption ( description:String, shortId:Char?=null, longId:String?=null ) :
	Option( "", description, shortId, longId, false) // flags don't have a type and are always optional

class ParseResult<T> (val value:T?, val hint:String?) {
	fun isValid() = value != null
}

abstract class ValueParser<T> {
	abstract fun parse(value:String) : ParseResult<T>
}

abstract class ValueOption<T>( name:String, description:String, shortId:Char?, longId:String?, isMandatory:Boolean, private val parser:ValueParser<T> )	:
	Option( name, description, shortId, longId, isMandatory )
{
	private var parseResult : ParseResult<T> = ParseResult(null,"Not parsed.")

	val value : T
		get() = parseResult.value!!

	internal fun parseValue (value:String) {
		parseResult = parser.parse(value)
		isDefined = parseResult.isValid()
	}
	internal fun isValid () : Boolean = parseResult.value != null
	internal fun invalidityHint() : String = parseResult.hint!!
}

class StringParser: ValueParser<String>() {
	override fun parse(value: String) = ParseResult(value, null)
}

open class StringOption ( type:String, description:String, shortId:Char?=null, longId:String?=null, isMandatory:Boolean=false )	:
	ValueOption<String>( type, description, shortId, longId, isMandatory, StringParser() )

class IntegerParser: ValueParser<Int>() {
	override fun parse(value: String) = with(value.toIntOrNull()) {
		ParseResult(this, if(this==null) "Invalid format." else null ) }
}

open class IntegerOption (description:String, shortId:Char?=null, longId:String?=null, isMandatory:Boolean=false) :
	ValueOption<Int>( "NUM", description, shortId, longId, isMandatory, IntegerParser() )

class DoubleParser: ValueParser<Double>() {
	override fun parse (value: String) = with(value.toDoubleOrNull()) {
		ParseResult(this, if(this==null) "Invalid format." else null ) }
}

open class DoubleOption (description:String, shortId:Char?=null, longId:String?=null, isMandatory:Boolean=false) :
	ValueOption<Double>( "DECIMAL", description, shortId, longId, isMandatory, DoubleParser() )

open class FileOptionParser: ValueParser<File>() {
	override fun parse (value:String ) = with(File(value)) {
		if (this.isFile) ParseResult(this, null) else ParseResult<File>(null, "No such file.")
	}
}

abstract class FileOption (description:String, shortId:Char?, longId:String?, isMandatory:Boolean=false, fileOptionParser:FileOptionParser) :
		ValueOption<File>("FILE", description, shortId, longId, isMandatory, fileOptionParser)

class ReadableFileOptionParser: FileOptionParser() {
	override fun parse (value:String ) = with(super.parse(value)) {
		if (this.isValid() && !this.value?.canRead()!! ) ParseResult<File>(null, "Not readable.")
		else this // for both invalid and valud files
	}
}

class ReadableFileOption (description:String, shortId:Char?=null, longId:String?=null, isMandatory:Boolean=false) :
	FileOption(description, shortId, longId, isMandatory, ReadableFileOptionParser())

class WritableFileOptionParser: FileOptionParser() {
	override fun parse (value:String ) = with(super.parse(value)) {
		if (this.isValid() && !this.value?.canWrite()!! ) ParseResult<File>(null, "Not writable.")
		else this // for both invalid and valud files
	}
}

class WritableFileOption (description:String, shortId:Char?=null, longId:String?=null, isMandatory:Boolean=false) :
	FileOption(description, shortId, longId, isMandatory, WritableFileOptionParser())

enum class RandomAccessMode { READ, READ_WRITE }

class RandomAccessFileOptionParser(private val mode:RandomAccessMode): ValueParser<RandomAccessFile>() {
	private fun resolveModeString (mode:RandomAccessMode) = when (mode) {
		RandomAccessMode.READ -> "r"
		RandomAccessMode.READ_WRITE -> "rw"
	}

	override fun parse (value:String ) = try {
		ParseResult(RandomAccessFile(value, resolveModeString(mode)), null)
	} catch ( e:SecurityException ) {
		ParseResult<RandomAccessFile>(null, "Not writable.")
	} catch ( e: FileNotFoundException) {
		ParseResult<RandomAccessFile>(null, "No such file.")
	}
}

class RandomAccessFileOption (mode:RandomAccessMode, description:String, shortId:Char?, longId:String?, isMandatory:Boolean=false) :
	ValueOption<RandomAccessFile>("FILE", description, shortId, longId, isMandatory, RandomAccessFileOptionParser(mode))

open class DirectoryOptionParser: ValueParser<File>() {
	override fun parse (value:String ) = with(File(value)) {
		if (this.isDirectory) ParseResult(this, null) else ParseResult<File>(null, "No such directory.")
	}
}

abstract class DirectoryOption (description:String, shortId:Char?, longId:String?, isMandatory:Boolean=false, directoryOptionParser:DirectoryOptionParser) :
	ValueOption<File>("DIR", description, shortId, longId, isMandatory, directoryOptionParser)

class ReadableDirectoryOptionParser:DirectoryOptionParser() {
	override fun parse (value:String ) = with(super.parse(value)) {
		if (this.isValid() && !this.value?.canRead()!! ) ParseResult<File>(null, "Not readable.")
		else this // for both invalid and valid directories
	}
}

class ReadableDirectoryOption (description:String, shortId:Char?, longId:String?, isMandatory:Boolean=false) :
	DirectoryOption(description, shortId, longId, isMandatory, ReadableDirectoryOptionParser())

class WritableDirectoryOptionParser:DirectoryOptionParser() {
	override fun parse (value:String ) = with(super.parse(value)) {
		if (this.isValid() && !this.value?.canWrite()!! ) ParseResult<File>(null, "Not writable.")
		else this // for both invalid and valid directories
	}
}

class WritableDirectoryOption (description:String, shortId:Char?, longId:String?, isMandatory:Boolean=false) :
	DirectoryOption(description, shortId, longId, isMandatory, WritableDirectoryOptionParser())