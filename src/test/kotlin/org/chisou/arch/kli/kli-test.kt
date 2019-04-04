package org.chisou.arch.kli

import io.kotlintest.specs.FreeSpec
import org.amshove.kluent.*


class KliSpecs : FreeSpec ({

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

		class TestData(
				val args:String,
				val desc:String
		)

		"Given a couple of defined flags" - {

			val tests = listOf(
					TestData( args="-ab", desc = "It should parse a concatenated list of flags" ),
					TestData( args="-a -b", desc="It should parse a list of single character flags" )
				)
			tests.forEach{ testData ->
				testData.desc  {
					val kli = object : Kli() {
						val a = FlagOption("", 'a', "")
						val b = FlagOption("", 'b', "")
					}
					kli.parse ( args=testData.args.split( " " ).toTypedArray() )
					assert( kli.a.isDefined )
					assert( kli.b.isDefined )
				}
			}
		}

		"Given a couple of defined value options" - {

			val tests = listOf(
					TestData( args="-a1 -b2", desc = "It should parse concatenated options" ),
					TestData( args="-a 1 -b 2", desc="It should parse split options" )
				)

			tests.forEach{ testData ->
				testData.desc {
					val kli = object : Kli() {
						val a = StringOption("a", "", 'a', "")
						val b = StringOption("b", "", 'b', "")
					}
					kli.parse ( args=testData.args.split( " " ).toTypedArray() )
					kli.a.isDefined `should be` true
					kli.b.isDefined `should be` true
					kli.a.value `should equal` "1"
					kli.b.value `should equal` "2"
				}
			}
		}

		"Given a couple of mixed options" - {
			val tests = listOf(
					TestData( args="-a -b 2", desc = "It should parse split options" ),
					TestData( args="-ab2", desc="It should parse concatenated options" ),
					TestData( args="-a -b2", desc="It should parse mixed options" )
			)
			tests.forEach { testData ->
				testData.desc {
					val kli = object : Kli() {
						val a = FlagOption("", 'a', "")
						val b = StringOption("b", "", 'b', "")
					}
					kli.parse(args = testData.args.split(" ").toTypedArray())
					assert(kli.a.isDefined)
					assert(kli.b.isDefined)
					assert(kli.b.value == "2")
				}
			}

		}

	}

})