package org.chisou.arch.kli

import com.nhaarman.mockitokotlin2.*
import io.kotlintest.specs.FreeSpec
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.chisou.arch.kli.KliMock.Companion.kli
import org.mockito.Mockito
import org.slf4j.impl.MockLoggerFactory


class KliMock ( override val options:List<Option>) : Kli() {
	companion object {
		fun kli ( vararg options:Option ) : KliMock {
			return KliMock(options.asList())
		}
	}
	val option = options.associateBy { it.description }
	val flagOption : Map<String,FlagOption> = options.filterIsInstance<FlagOption>().associateBy { it.description }
	val stringOption : Map<String,StringOption> = options.filterIsInstance<StringOption>().associateBy { it.description }
}

class KliSpecs : FreeSpec ({

    fun flag (short:Char, long:String?=null) = FlagOption(short.toString(),short,long)
	fun flag (long:String) = FlagOption(long,null,long)
	fun string (short:Char, long:String?=null) = StringOption("",short.toString(),short,long)
	fun string (long:String) = StringOption("",long,null,long)
	fun mstring (short:Char, long:String?=null) = StringOption("",short.toString(),short,long,true)
	fun mstring (long:String) = StringOption("",long,null,long,true)

	fun String.toArgs() = this.split( " " ).toTypedArray()


	"Parsing non-option values" - {

		"Given an empty arg list" - {
			"it should give no values" {
				val kli = object : Kli() {}
				kli.parse( args=arrayOf() )
				assert( kli.values.isEmpty() )
			}
		}
		"Given an non-option arg list" - {
			"It should give no values" {
				val kli = object : Kli() {}
				kli.parse(args=arrayOf( "value1", "value2" ))
				assert(kli.values == listOf("value1", "value2"))
			}
		}
	}

	"Parsing one-character options" - {

		class TestData( val line:String, val desc:String ) {
			val args = line.toArgs()
		}

		"Given a couple of defined flags" - {

			val tests = listOf(
					TestData( line="-ab", desc = "It should parse a concatenated list of flags" ),
					TestData( line="-a -b", desc="It should parse a list of single character flags" )
				)
			tests.forEach{ testData ->
				testData.desc  {
					val kli = object : Kli() {
						val a = FlagOption("", 'a', "")
						val b = FlagOption("", 'b', "")
					}
					kli.parse(testData.args)
					assert( kli.a.isDefined )
					assert( kli.b.isDefined )
				}
			}
		}

		"Missing values will be reported ..." - {

			val tests = listOf(
				TestData( line="-a -b", desc = "... when they are the last option" ),
				TestData( line="-ab", desc=".. when they are the last option in a concatenated option sequence" )
			)

			tests.forEach{ test ->
				test.desc {
					val kli = kli(flag('a'),string('b'))
					kli.parse(test.args)
					kli.option["a"]?.isDefined `should be` true
					kli.option["b"]?.isDefined `should be` false
				}
			}

			"... when the value appears to be a option" {
				val kli = kli(string('a'))
				val mockLogger = MockLoggerFactory.instance.getLogger(Kli.LOGGER_NAME)
				Mockito.reset(mockLogger) // have to do this as it is a single logger for all instances
				kli.parse("-a -b".toArgs())
				verify(mockLogger).info(argThat{contains("'-b'")})
				verify(mockLogger, never()).error(any())
				kli.option["a"]?.isDefined `should be` true
				kli.stringOption["a"]?.value `should equal` "-b"

			}
		}

		"Given a couple of defined value options" - {

			val tests = listOf(
					TestData( line="-a1 -b2", desc = "It should parse concatenated options" ),
					TestData( line="-a 1 -b 2", desc="It should parse split options" )
				)

			tests.forEach{ testData ->
				testData.desc {
					val kli = object : Kli() {
						val a = StringOption("a", "", 'a', "")
						val b = StringOption("b", "", 'b', "")
					}
					kli.parse(testData.args)
					kli.a.isDefined `should be` true
					kli.b.isDefined `should be` true
					kli.a.value `should equal` "1"
					kli.b.value `should equal` "2"
				}
			}

		}

		"Given a couple of mixed options" - {
			val tests = listOf(
					TestData( line="-a -b 2", desc = "It should parse split options" ),
					TestData( line="-ab2", desc="It should parse concatenated options" ),
					TestData( line="-a -b2", desc="It should parse mixed options" )
			)
			tests.forEach { testData ->
				testData.desc {
					val kli = object : Kli() {
						val a = FlagOption("", 'a', "")
						val b = StringOption("b", "", 'b', "")
					}
					kli.parse(testData.args)
					assert(kli.a.isDefined)
					assert(kli.b.isDefined)
					assert(kli.b.value == "2")
				}
			}
		}

		"Unknown flag options will be ignored ..." - {
			val tests = listOf(
				TestData(line="-a -x -b", desc="... using single options"),
				TestData(line="-axb", desc="... using concatenated options"),
				TestData(line="-a -xb", desc="... using mixed options")
			)
			tests.forEach { testData ->
				testData.desc {
					val kli = object : Kli() {
						val a = FlagOption("", 'a', "")
						val b = FlagOption("", 'b', "")
					}
					kli.parse(testData.args)
					assert(kli.a.isDefined)
					assert(kli.b.isDefined)
				}
			}
		}

		"Unknown value options will be ignored ..." - {
			val tests = listOf(
				TestData(line="-a1 -x2 -b3", desc="... using single options"),
				TestData(line="-a1 -xb3", desc="... using concatenated options")
			)
			tests.forEach { testData ->
				testData.desc {
					val kli = object : Kli() {
						val a = StringOption("", "", 'a', "")
						val b = StringOption("", "", 'b', "")
					}
					kli.parse(testData.args)
					kli.a.isDefined `should be` true
					kli.b.isDefined `should be` true
					kli.a.value `should equal` "1"
					kli.b.value `should equal` "3"
				}
			}
		}

	}

})