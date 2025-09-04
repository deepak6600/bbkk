package com.safe.setting.app.data.rxFirebase

import android.util.Log
import com.safe.setting.app.utils.Consts.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
// import com.pawegio.kandroid.e // **** पुराना इम्पोर्ट हटा दिया गया ****
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.exceptions.OnErrorNotImplementedException

object RxFirebaseDatabase {

    fun Query.rxObserveValueEvent(auth : FirebaseAuth): Flowable<DataSnapshot> {
        return Flowable.create({ emitter ->
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) = emitter.onNext(dataSnapshot)

                override fun onCancelled(error: DatabaseError) {
                    try { if (auth.currentUser!=null) if (!emitter.isCancelled) emitter.onError(Throwable(error.message)) }
                    // **** बदला हुआ कोड: kandroid.e को Log.e से बदलें ****
                    catch (er: OnErrorNotImplementedException){ Log.e(TAG, er.message.toString()) }
                    catch (t: Throwable) { Log.e(TAG, t.message.toString()) }
                    // **** बदलाव समाप्त ****
                }
            }
            emitter.setCancellable { removeEventListener(valueEventListener) }
            addValueEventListener(valueEventListener)
        }, BackpressureStrategy.DROP)
    }

    fun Query.rxObserveSingleValueEvent(): Maybe<DataSnapshot> {
        return Maybe.create { emitter ->
            addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) = emitter.onSuccess(dataSnapshot)
                override fun onCancelled(error: DatabaseError) { if (!emitter.isDisposed) emitter.onError(Throwable(error.message)) }
            })
        }
    }

}
