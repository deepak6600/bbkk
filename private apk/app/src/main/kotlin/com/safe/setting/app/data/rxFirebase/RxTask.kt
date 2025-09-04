package com.safe.setting.app.data.rxFirebase

import android.util.Log
import com.safe.setting.app.utils.Consts.TAG
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
// import com.pawegio.kandroid.e // **** पुराना इम्पोर्ट हटा दिया गया ****
import io.reactivex.rxjava3.core.MaybeEmitter

class RxTask<T : Any>(private val emitter: MaybeEmitter<in T>) : OnSuccessListener<T>,
    OnFailureListener, OnCompleteListener<T> {

    override fun onSuccess(res: T?) {
        if (res != null) emitter.onSuccess(res)
        else emitter.onError(Throwable("Observables can't emit null values"))
    }

    override fun onComplete(task: Task<T>) {
        emitter.onComplete()
    }

    override fun onFailure(e: Exception) {
        if (!emitter.isDisposed) {
            emitter.onError(e)
        }
    }

    companion object {
        fun <T : Any> assignOnTask(emitter: MaybeEmitter<in T>, task: Task<T>) {
            val rxTask = RxTask(emitter)
            task.addOnSuccessListener(rxTask)
            task.addOnFailureListener(rxTask)

            try {
                task.addOnCompleteListener(rxTask)
            } catch (t: Throwable) {
                // **** बदला हुआ कोड: kandroid.e को Log.e से बदलें ****
                Log.e(TAG, t.message.toString())
                // **** बदलाव समाप्त ****
            }
        }
    }
}
