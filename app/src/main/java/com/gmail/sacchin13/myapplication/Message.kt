package com.gmail.sacchin13.myapplication

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Message : RealmObject() {
    open var user: String? = null
    open var time: Long = 0L
    open var message: String? = null
}

