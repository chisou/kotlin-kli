package org.chisou.arch.kli

import com.nhaarman.mockitokotlin2.*
import io.kotlintest.specs.FreeSpec
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should contain all`
import org.amshove.kluent.`should contain none`
import org.amshove.kluent.`should equal`
import org.chisou.arch.kli.KliMock.Companion.kli
import org.mockito.Mockito
import org.slf4j.impl.MockitoLoggerFactory
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue


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

	val mockLogger = MockitoLoggerFactory.getSingleton().getLogger(Kli.LOGGER_NAME)

    fun flag (short:Char, long:String?=null) = FlagOption(short.toString(),short,long)
	fun flag (long:String) = FlagOption(long,null,long)
	fun string (short:Char, long:String?=null) = StringOption("",short.toString(),short,long)
	fun string (long:String) = StringOption("",long,null,long)
	fun int (short:Char, long:String?=null) = IntegerOption(short.toString(),short,long)
	fun int (long:String) = IntegerOption(long,null,long)
	fun mstring (short:Char, long:String?=null) = StringOption("",short.toString(),short,long,true)
	fun mstring (long:String) = StringOption("",long,null,long,true)

	class TestData( val line:String, val desc:String ) {
		val args = line.toArgs()
	}

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
		"Given an parse end (--) token" - {
			"It should collect all arguments after the token" {
				val kli = kli(flag('a'),string("bb"))
				kli.parse("-a --bb=b -- -x --y z".toArgs())
				kli.option["a"]?.isDefined `should equal` true
				kli.option["bb"]?.isDefined `should equal` true
				kli.values `should contain all` listOf("-x", "--y", "z")
			}
			"It should also work with only the end token" {
				val kli = object : Kli() {}
				kli.parse("--".toArgs())
				kli.values.isEmpty() `should equal` true

			}
			"It should also work with zero arguments after the token" {
				val kli = kli(flag('a'),string("bb"))
				kli.parse("-a --bb=b --".toArgs())
				kli.option["a"]?.isDefined `should equal` true
				kli.option["bb"]?.isDefined `should equal` true
				kli.values.isEmpty() `should equal` true
			}
		}
	}

	"Parsing one-character (short) options" - {

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
					Mockito.reset(mockLogger)
					kli.parse(testData.args)
					verify(mockLogger).warn(argThat{contains("x")})
					verify(mockLogger, never()).error(any())
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
					Mockito.reset(mockLogger)
					kli.parse(testData.args)
					verify(mockLogger).warn(argThat{contains("x")})
					verify(mockLogger, never()).error(any())
					kli.a.isDefined `should be` true
					kli.b.isDefined `should be` true
					kli.a.value `should equal` "1"
					kli.b.value `should equal` "3"
				}
			}
		}

	}

	"Parsing long options" - {

		"Given a couple of flag options ..." - {

			"Flags should be detected in any order:" - {
				val lines = listOf( "--aaa --bbb", "--bbb --aaa" )
				lines.forEach{ line ->
					"Line '$line'" {
						val kli = kli(flag("aaa"), flag("bbb"))
						kli.parse(line.toArgs())
						kli.option["aaa"]?.isDefined `should be` true
						kli.option["bbb"]?.isDefined `should be` true
					}
				}
			}

			"Missing flags should be marked as undefined:" - {
				val lines = listOf( "--aaa --ccc", "--ccc --aaa" )
				lines.forEach{ line ->
					"Line '$line'" {
						val kli = kli(flag("aaa"), flag("bbb"), flag("ccc"))
						kli.parse(line.toArgs())
						kli.option["aaa"]?.isDefined `should be` true
						kli.option["bbb"]?.isDefined `should be` false
						kli.option["ccc"]?.isDefined `should be` true
					}
				}
			}

			"Unknown options/flags should be ignored and reported/logged:" - {
				val lines = listOf( "--aaa --xxx --ccc", "--xxx --ccc --aaa","--aaa --ccc --xxx",
					"--xxx=abc --aaa --ccc", "--aaa --xxx abc --ccc" )
				lines.forEach{ line ->
					"Line '$line'" {
						val kli = kli(flag("aaa"), flag("bbb"), flag("ccc"))
						Mockito.reset(mockLogger)
						kli.parse(line.toArgs())
						verify(mockLogger).warn(argThat{contains("xxx") && contains("ignore")})
						verify(mockLogger, never()).error(any())
						kli.option["aaa"]?.isDefined `should be` true
						kli.option["bbb"]?.isDefined `should be` false
						kli.option["ccc"]?.isDefined `should be` true
					}
				}
			}

//			"uncalled for values to flags"

			"Value options should be parsed in all styles and order:" - {
				val lines = listOf( "--aaa=a --bbb b", "--bbb=b --aaa a" )
				lines.forEach{ line ->
					"Line '$line'" {
						val aaa = string("aaa")
						val bbb = string("bbb")
						val kli = kli(aaa, bbb)

						kli.parse(line.toArgs())
						aaa.isDefined `should be` true
						bbb.isDefined `should be` true
						aaa.value `should equal` "a"
						bbb.value `should equal` "b"
					}
				}
			}

			"Options with with missing values are reported & ignored:" - {
				val lines = listOf( "--aaa= --bbb", "--bbb= --aaa" )
				lines.forEach{ line ->
					"Line '$line'" {
						val aaa = string("aaa")
						val bbb = string("bbb")
						val kli = kli(aaa, bbb)

						Mockito.reset(mockLogger)
						kli.parse(line.toArgs())
						verify(mockLogger).error(argThat{toUpperCase().contains("AAA") && toUpperCase().contains("IGNORE")})
						aaa.isDefined `should be` false
						bbb.isDefined `should be` false
					}
				}

				"And suspicious looking values are reported (but used)" {
					val aaa = string("aaa")
					val kli = kli(aaa)

					Mockito.reset(mockLogger)
					kli.parse("--aaa --bbb".toArgs())
					verify(mockLogger).info(argThat{toUpperCase().contains("AAA") && toUpperCase().contains("--BBB")})
					aaa.isDefined `should be` true
					aaa.value `should equal` "--bbb"
				}

			}

			"Missing mandatory options ..." - {

				"should be ignored when the 'validate' argument is disabled" {
					val kli = kli(mstring("aaa"), mstring("bbb"))
					kli.parse("".toArgs(), validate=false)
					kli.isValid() `should be` true
				}

				"should invalidate the parse result when using the 'validate' argument" {
					val kli = kli(mstring("aaa"), mstring("bbb"))
					kli.parse("".toArgs(), validate=true)
					kli.isValid() `should be` false
				}

				"should cause the parse to fail when using the 'fail' argument" {
					val kli = kli(mstring("aaa"))

					Mockito.reset(mockLogger)
					try {
						kli.parse("".toArgs(), validate=true, fail=true)
						assertTrue(false)
					} catch (ex:RuntimeException) {
						verify(mockLogger).error(ex.message)
					} catch (ex:Throwable) {
						assertTrue(false)
					}
				}
			}

			"Options that cannot be parsed ..." - {

				fun kli() = kli(int("aaa"), int("bbb"))
				val args = "--aaa=x --bbb y".toArgs()

				"Should be completely ignored during parsing" {
					val kli = kli()
					Mockito.reset(mockLogger)
					kli.parse(args)
					kli.isValid() `should be` true
					kli.option["aaa"]?.isDefined `should be` false
					kli.option["bbb"]?.isDefined `should be` false
					verify(mockLogger, never()).error(any())
				}

				"Should be reported during parsing and invalidate the result" {
					val kli = kli()
					Mockito.reset(mockLogger)
					kli.parse(args, validate=true)
					kli.isValid() `should be` false
					verify(mockLogger).error(argThat{containsAll("aaa", "nvalid")})
				}

				"Should cause the parsing to exit when using the 'fail' argument" {
					val kli = kli()
					Mockito.reset(mockLogger)
					val ex = assertFailsWith<RuntimeException> {
						kli.parse(args, validate=true, fail=true)
					}
					verify(mockLogger).error(ex.message)
					ex.message!! `should contain none` listOf("bbb")
				}

			}

		}

	}

})