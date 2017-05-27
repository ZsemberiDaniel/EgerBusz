package com.zsemberidaniel.egerbuszuj.misc

/**
 * Created by zsemberi.daniel on 2017. 05. 27..
 */

/**
 * Formats a number to two digits. For example 2 -> 02, 5 -> 05, 23 -> 23
 * If number is above two digits it will just be returned as a string
 */
fun Int.formatToTwoDigits(): String {
    return "${if (this < 10) "0" else ""}$this"
}