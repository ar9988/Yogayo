package com.d104.data.mapper

interface Mapper<I, O> {
    fun map(input: I): O
}