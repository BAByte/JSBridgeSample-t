package com.sample.mix.demo.repository


import android.content.Context
import com.sample.mix.demo.repository.impl.NativeRepository
import kotlin.properties.Delegates

object RepositoryLoader {

    private var context: Context by Delegates.notNull()

    //this method must call in application to ensure repository load successful
    @JvmStatic
    fun init(context: Context) {
        this.context = context.applicationContext
    }

    @JvmStatic
    fun provideNativeRepository(): INativeRepository {
        return NativeRepository()
    }
}