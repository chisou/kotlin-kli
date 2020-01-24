package org.chisou.arch.kli

fun String.containsAll (vararg strings:String) : Boolean = !strings.map{this.contains(it)}.contains(false)

fun String.toArgs() = this.split( " " ).toTypedArray()
