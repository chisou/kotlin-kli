package de.csou.arch.kli


abstract class Option (
		val name:String,
		val description:String,
		val shortId:Char? = null,
		val longId:String? = null
) {
	var isDefined : Boolean = false
		internal set
}

class FlagOption ( name:String, description:String, shortId:Char, longId:String ) : Option( name, description, shortId, longId )

abstract class ValueOption<T>( name:String, description:String, shortId:Char, longId:String )
	: Option( name, description, shortId, longId )
{
	var value : T? = null
		private set
	internal fun parseValue ( value:String ) {
		this.value = parse( value )
	}
	abstract protected fun parse ( value:String ) : T
}

class StringOption ( name:String, description:String, shortId:Char, longId:String )
	: ValueOption<String>( name, description, shortId, longId ) {
	override fun parse ( value:String ) : String = value
}





