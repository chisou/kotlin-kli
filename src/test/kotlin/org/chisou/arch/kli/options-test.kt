package org.chisou.arch.kli

import io.kotlintest.specs.FreeSpec
import org.amshove.kluent.*
import java.io.File
import java.nio.file.Files

class OptionSpecs : FreeSpec({

    "Options cannot exist without at least one of short ID or long ID" {
        invoking { FlagOption("", null, null) } `should throw` InstantiationError::class
    }

    "Given a ReadableFileOption ..." - {
        "It can parse any File (even non existing) as value." {
            val option = ReadableFileOption("description", 'x', "xyz")
            option.parseValue("/any")
        }
        "It will be valid for a readable file." {
            val option = WritableFileOption("description", 'x', "xyz")
            val f = File.createTempFile( "kli", ".tmp" )
            f.deleteOnExit()
            option.parseValue(f.absolutePath)
            option.value `should equal` f
            option.isValid() `should be` true
        }
        "When handling a non-existing files ..." - {
            val option = ReadableFileOption("description", 'x', "xyz")
            option.parseValue("/non-existing")
            "It should be flagged as invalid." {
                option.isValid() `should be` false
            }
            "The invalidity hint should label the file as not existing." {
                option.invalidityHint().toLowerCase() `should contain` "no such"
            }
        }
        "When handling a not readable file ..." - {
            val option = ReadableFileOption("description", 'x', "xyz")
            val f = File.createTempFile( "kli", ".tmp" )
            f.deleteOnExit()
            f.setReadable(false)
            option.parseValue(f.absolutePath)
            "It should be flagged as invalid." {
                option.isValid() `should be` false
            }
            "The invalidity hint should label the file as not readable." {
                option.invalidityHint().toLowerCase() `should contain` "read"
            }
        }
        "When providing a directory instead ..." - {
            val option = ReadableFileOption("description", 'x', "xyz")
            option.parseValue("/") // this goes to a directory, can't be read
            "It should be flagged as invalid." {
                option.isValid() `should be` false
            }
            "The invalidity hint should point at it being a directory." {
                option.invalidityHint().toLowerCase() `should contain` "no such"
            }
        }
    }

    "Given a WritableFileOption ..." - {
        "It can parse any File (even non existing) as value." {
            val option = WritableFileOption("description", 'x', "xyz")
            option.parseValue("/any")
        }
        "It will be valid for a writable file." {
            val option = WritableFileOption("description", 'x', "xyz")
            val f = File.createTempFile( "kli", ".tmp" )
            f.deleteOnExit()
            f.setWritable(true)
            option.parseValue(f.absolutePath)
            option.value `should equal` f
            option.isValid() `should be` true
        }
        "When handling a non-existing file ..." - {
            val option = WritableFileOption("description", 'x', "xyz")
            option.parseValue("/non-existing")
            "It should be flagged as invalid." {
                option.isValid() `should be` false
            }
            "The invalidity hint should label the file as not existing." {
                option.invalidityHint().toLowerCase() `should contain` "no such"
            }
        }
        "When handling a not writable file ..." - {
            val option = WritableFileOption("description", 'x', "xyz")
            val f = File.createTempFile( "kli", ".tmp" )
            f.deleteOnExit()
            f.setWritable(false)
            option.parseValue(f.absolutePath)
            option.value `should equal` f
            "It should be flagged as invalid." {
                option.isValid() `should be` false
            }
            "The invalidity hint should label the file as not writable." {
                option.invalidityHint().toLowerCase() `should contain` "writ"
            }
        }
        "When providing a directory instead ..." - {
            val option = WritableFileOption("description", 'x', "xyz")
            option.parseValue("/") // this goes to a directory, can't be read
            "It should be flagged as invalid." {
                option.isValid() `should be` false
            }
            "The invalidity hint should point at it being a directory." {
                option.invalidityHint().toLowerCase() `should contain` "no such"
            }
        }
    }

    "Given a ReadableDirectoryOption ..." - {
        "It can parse any File (even non existing) as value." {
            val option = ReadableDirectoryOption("description", 'x', "xyz")
            option.parseValue("/any")
        }
        "It will be valid for a directory." {
            val option = ReadableDirectoryOption("description", 'x', "xyz")
            val d = Files.createTempDirectory( "kli").toFile()
            d.deleteOnExit()
            option.parseValue(d.absolutePath)
            option.value `should equal` d
            option.isValid() `should be` true
        }
        "When handling a non-existing directory ..." - {
            val option = ReadableDirectoryOption("description", 'x', "xyz")
            option.parseValue("/non-existing")
            "It should be flagged as invalid." {
                option.isValid() `should be` false
            }
            "The invalidity hint should label the directory as not existing." {
                option.invalidityHint().toLowerCase() `should contain` "no such"
            }
        }
        "When handling a not readable directory ..." - {
            val option = ReadableDirectoryOption("description", 'x', "xyz")
            val d = Files.createTempDirectory( "kli").toFile()
            d.deleteOnExit()
            d.setReadable(false)
            option.parseValue(d.absolutePath)
            option.value `should equal` d
            "It should be flagged as invalid." {
                option.isValid() `should be` false
            }
            "The invalidity hint should label the file as not writable." {
                option.invalidityHint().toLowerCase() `should contain` "read"
            }
        }
    }

    "Given a WritableDirectoryOption ..." - {
        "It can parse any File (even non existing) as value." {
            val option = WritableDirectoryOption("description", 'x', "xyz")
            option.parseValue("/any")
        }
        "It will be valid for a directory." {
            val option = WritableDirectoryOption("description", 'x', "xyz")
            val d = Files.createTempDirectory( "kli").toFile()
            d.deleteOnExit()
            d.setWritable(true)
            option.parseValue(d.absolutePath)
            option.value `should equal` d
            option.isValid() `should be` true
        }
        "When handling a non-existing directory ..." - {
            val option = WritableDirectoryOption("description", 'x', "xyz")
            option.parseValue("/non-existing")
            "It should be flagged as invalid." {
                option.isValid() `should be` false
            }
            "The invalidity hint should label the directory as not existing." {
                option.invalidityHint().toLowerCase() `should contain` "no such"
            }
        }
        "When handling a not writable directory ..." - {
            val option = WritableDirectoryOption("description", 'x', "xyz")
            val d = Files.createTempDirectory( "kli").toFile()
            d.deleteOnExit()
            d.setWritable(false)
            option.parseValue(d.absolutePath)
            option.value `should equal` d
            "It should be flagged as invalid." {
                option.isValid() `should be` false
            }
            "The invalidity hint should label the file as not writable." {
                option.invalidityHint().toLowerCase() `should contain` "read"
            }
        }
    }

    "Given a RandomAccessFileOption ..." - {
        "It can parse any File (even non existing) as value." {
            val option = WritableFileOption("description", 'x', "xyz")
            option.parseValue("/any")
        }
        "It will be valid for a random access file." {
            val option = WritableFileOption("description", 'x', "xyz")
            val f = File.createTempFile( "kli", ".tmp" )
            f.deleteOnExit()
            f.setWritable(true)
            option.parseValue(f.absolutePath)
            option.value `should equal` f
            option.isValid() `should be` true
        }
        "When handling a non-existing file ..." - {
            val option = WritableFileOption("description", 'x', "xyz")
            option.parseValue("/non-existing")
            "It should be flagged as invalid." {
                option.isValid() `should be` false
            }
            "The invalidity hint should label the file as not existing." {
                option.invalidityHint().toLowerCase() `should contain` "no such"
            }
        }
        "When handling a not writable file ..." - {
            val option = WritableFileOption("description", 'x', "xyz")
            val f = File.createTempFile( "kli", ".tmp" )
            f.deleteOnExit()
            f.setWritable(false)
            option.parseValue(f.absolutePath)
            option.value `should equal` f
            "It should be flagged as invalid." {
                option.isValid() `should be` false
            }
            "The invalidity hint should label the file as not writable." {
                option.invalidityHint().toLowerCase() `should contain` "writ"
            }
        }
        "When providing a directory instead ..." - {
            val option = WritableFileOption("description", 'x', "xyz")
            option.parseValue("/") // this goes to a directory, can't be read
            "It should be flagged as invalid." {
                option.isValid() `should be` false
            }
            "The invalidity hint should point at it being a directory." {
                option.invalidityHint().toLowerCase() `should contain` "no such"
            }
        }
    }

})