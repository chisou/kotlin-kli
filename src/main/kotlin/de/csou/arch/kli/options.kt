package de.csou.arch.kli


abstract class Option (
	val name:String,
	val description:String,
	internal val shortIds:List<Char>,
	internal val longIds:List<String>,
	val isMandatory:Boolean)
{
	var isDefined : Boolean = false
		internal set
}

class FlagOption ( name:String, description:String, shortIds:List<Char>, longIds:List<String> ) :
	Option( name, description, shortIds, longIds, false )
{
	constructor (name:String, description: String, shortId:Char?, longId:String?) :
			this(name, description, if(shortId!=null) listOf(shortId) else listOf(), if(longId!=null) listOf(longId) else listOf() )
}

class MandatoryFlagOption ( name:String, description:String, shortIds:List<Char>, longIds:List<String> ) :
	Option(name, description, shortIds, longIds, true)
{
	constructor (name:String, description: String, shortId:Char?, longId:String?) :
			this(name, description, if(shortId!=null) listOf(shortId) else listOf(), if(longId!=null) listOf(longId) else listOf() )
}

abstract class ValueOption<T>( name:String, description:String, shortIds:List<Char>, longIds:List<String>, isMandatory:Boolean )	:
	Option( name, description, shortIds, longIds, isMandatory )
{
	var value : T? = null
		private set
	internal fun parseValue ( value:String ) {
		this.value = parse( value )
	}
	protected abstract fun parse (value:String ) : T
}

class StringOption ( name:String, description:String, shortIds:List<Char>, longIds:List<String> )
	: ValueOption<String>( name, description, shortIds, longIds, false ) {
	constructor (name:String, description: String, shortId:Char?, longId:String?) :
			this(name, description, if(shortId!=null) listOf(shortId) else listOf(), if(longId!=null) listOf(longId) else listOf() )
	override fun parse ( value:String ) : String = value
}

class MandatoryStringOption ( name:String, description:String, shortIds:List<Char>, longIds:List<String> )
	: ValueOption<String>( name, description, shortIds, longIds, true ) {
	constructor (name:String, description: String, shortId:Char?, longId:String?) :
			this(name, description, if(shortId!=null) listOf(shortId) else listOf(), if(longId!=null) listOf(longId) else listOf() )
	override fun parse ( value:String ) : String = value
}




