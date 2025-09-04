package com.safe.setting.app.data.rxFirebase

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe

interface InterfaceFirebase {

    fun getUser(): FirebaseUser?

    fun signIn(email: String, password: String): Maybe<AuthResult>

    fun signUp(email: String, password: String): Maybe<AuthResult>

    fun signOut()

    fun valueEvent(child: String): Flowable<DataSnapshot>

    fun valueEventAccount(): Flowable<DataSnapshot>

    fun queryValueEventSingle(child: String,value:String,id:String): Maybe<DataSnapshot>

    fun <T : Any> valueEventModel(child: String, clazz: Class<T>): Flowable<T>

    fun getDatabaseReference(child: String): DatabaseReference

    fun getDatabaseAcount(): DatabaseReference

    // Storage से जुड़े सभी फंक्शन्स यहाँ से हटा दिए गए हैं
    // fun getStorageReference(...)
    // fun getFile(...)
    // fun putFile(...)

}
