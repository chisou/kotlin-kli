package org.chisou.arch.kli

import com.nhaarman.mockitokotlin2.*
import io.kotlintest.specs.FreeSpec
import org.amshove.kluent.*
import org.mockito.Mockito
import org.slf4j.impl.MockitoLoggerFactory
import java.lang.RuntimeException
import kotlin.test.assertFailsWith

class KliArgumentsSpec : FreeSpec({

    val mockLogger = MockitoLoggerFactory.getSingleton().getLogger(Kli.LOGGER_NAME)

    "The presence of arguments can be checked explicitly" {
        val kli = object : Kli() {}
        kli.parse(args=arrayOf("1", "2", "3"))
        kli.checkArguments("arg1", "arg2", "arg3")
        kli.isValid() `should be` true
    }

    "Additional arguments are not considered an error" {
        val kli = object : Kli() {}
        kli.parse(args=arrayOf("1", "2", "3"))
        kli.checkArguments("arg1")
        kli.isValid() `should be` true
    }

    "Missing positional arguments are reported and considered invalid" {
        val kli = object : Kli() {}
        Mockito.reset(mockLogger)
        kli.parse(args=arrayOf())
        kli.checkArguments("arg1", "arg2", "arg3")
        kli.isValid() `should equal` false
        verify(mockLogger).error(argThat { contains("issing") })
    }

    "Partially missing positional arguments are reported and considered invalid" {
        val kli = object : Kli() {}
        Mockito.reset(mockLogger)
        kli.parse(args=arrayOf("1"))
        kli.checkArguments("arg1", "arg2", "arg3")
        kli.isValid() `should equal` false
        verify(mockLogger).error(argThat{containsAll("arg2", "issing")})
        verify(mockLogger).error(argThat{containsAll("arg3", "issing")})
    }

    "A single missing positional arguments is reported by its' name" {
        val kli = object : Kli() {}
        Mockito.reset(mockLogger)
        kli.parse("1 2".toArgs())
        kli.checkArguments("arg1", "arg2", "arg3")
        kli.isValid() `should equal` false
        verify(mockLogger).error(argThat{containsAll("arg3", "must be provided")})
    }

    "When checking positional arguments with the 'fail' argument an exception will be thrown" {
        val kli = object : Kli() {}
        Mockito.reset(mockLogger)
        val ex = assertFailsWith<RuntimeException> {
            kli.parse(args=arrayOf("1"))
            kli.checkArguments("arg1", "arg2", "arg3", fail=true)
        }
        ex.message!! `should contain all` listOf("arg2", "arg2", "issing")
        verify(mockLogger).error(ex.message)
    }

    "Positional arguments are available after parsing" {
        val kli = object : Kli() {}
        kli.parse(args=arrayOf("a", "b", "c"))
        kli.isValid() `should be` true
        kli.firstValue `should equal` "a"
        kli.lastValue `should equal` "c"
        kli.values `should contain same` listOf("a","b","c")
    }

    "Identically typed arguments can be parse in one go" {
        val kli = object : Kli() {}
        kli.parse(args=arrayOf("1", "2", "3"))
        val args = kli.parseAllArguments(IntegerParser())!!
        args[0] `should be` 1
        args[1] `should be` 2
        args[2] `should be` 3
    }

    "First and last argument can be parsed separately" {
        val kli = object : Kli() {}
        kli.parse(args=arrayOf("1", "x", "2.0"))
        val first = kli.parseFirstArgument(IntegerParser())!!
        val last = kli.parseLastArgument(DoubleParser())!!
        first `should be` 1
        last `should equal` 2.0
    }

    "The head arguments can be parsed separately" {
        val kli = object : Kli() {}
        kli.parse(args=arrayOf("1", "2", "x"))
        val head = kli.parseHeadArguments(IntegerParser())!!
        head `should equal` listOf(1,2)
    }

    "The tail arguments can be parsed separately" {
        val kli = object : Kli() {}
        kli.parse(args=arrayOf("x", "1", "2"))
        val tail = kli.parseTailArguments(IntegerParser())!!
        tail `should equal` listOf(1,2)
    }

    "Parse errors will be flagged with a null result" {
        val kli = object : Kli() {}
        kli.parse(args=arrayOf("1", "xyz", "abc"))
        Mockito.reset(mockLogger)
        val args = kli.parseAllArguments(IntegerParser())
        args `should be` null
        verify(mockLogger).error(argThat{contains("xyz")})
        verify(mockLogger,never()).error(argThat{contains("abc")})
    }

    "The 'fail' argument Parse errors will be flagged with a null result" {
        val kli = object : Kli() {}
        kli.parse(args=arrayOf("xyz"))
        Mockito.reset(mockLogger)
        val ex = assertFailsWith<RuntimeException> {
            kli.parseAllArguments(IntegerParser(), fail=true)
        }
        ex.message!! `should contain` "xyz"
        verify(mockLogger).error(ex.message)
    }

})