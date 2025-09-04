package com.safe.setting.app.data.rxFirebase

import android.content.Context
import com.safe.setting.app.utils.Consts.USER
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import javax.inject.Inject
import com.safe.setting.app.data.rxFirebase.RxFirebaseDatabase.rxObserveValueEvent
import com.safe.setting.app.data.rxFirebase.RxFirebaseDatabase.rxObserveSingleValueEvent
import com.safe.setting.app.data.rxFirebase.RxFirebaseAuth.rxSignInWithEmailAndPassword
import com.safe.setting.app.data.rxFirebase.RxFirebaseAuth.rxCreateUserWithEmailAndPassword
import com.safe.setting.app.data.preference.DataSharePreference.childSelected
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe

// StorageReference को कंस्ट्रक्टर से हटा दिया गया है
class DevelopFirebase @Inject constructor(private val context: Context,
                                          private val auth: FirebaseAuth,
                                          private val dataRef: DatabaseReference) : InterfaceFirebase {

    override fun getUser(): FirebaseUser? = auth.currentUser

    override fun signIn(email: String, password: String): Maybe<AuthResult> =
        auth.rxSignInWithEmailAndPassword(email, password)

    override fun signUp(email: String, password: String): Maybe<AuthResult> =
        auth.rxCreateUserWithEmailAndPassword(email, password)

    override fun signOut() {
        auth.signOut()
    }

    override fun getDatabaseReference(child: String): DatabaseReference =
        getDatabaseAcount().child(context.childSelected).child(child)

    override fun getDatabaseAcount(): DatabaseReference {
        val reference = dataRef.child(USER).child(getUser()!!.uid)
        reference.keepSynced(true)
        return reference
    }

    override fun valueEventAccount(): Flowable<DataSnapshot> =
        getDatabaseAcount().rxObserveValueEvent(auth)

    override fun valueEvent(child: String): Flowable<DataSnapshot> =
        getDatabaseReference(child).rxObserveValueEvent(auth)

    override fun <T : Any> valueEventModel(child: String, clazz: Class<T>): Flowable<T> =
        getDatabaseReference(child).rxObserveValueEvent(auth).map { it.getValue(clazz)!! }


    override fun queryValueEventSingle(child: String, value: String, id: String): Maybe<DataSnapshot> =
        getDatabaseReference(child).orderByChild(value).equalTo(id).rxObserveSingleValueEvent()

    // Storage से जुड़े सभी फंक्शन्स यहाँ से हटा दिए गए हैं
}
