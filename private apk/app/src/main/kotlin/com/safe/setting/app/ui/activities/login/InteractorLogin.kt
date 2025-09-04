package com.safe.setting.app.ui.activities.login

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.safe.setting.app.R
import com.safe.setting.app.data.rxFirebase.InterfaceFirebase
import com.safe.setting.app.ui.activities.base.BaseInteractor
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class InteractorLogin<V : InterfaceViewLogin> @Inject constructor(
    supportFragment: FragmentManager,
    context: Context,
    firebase: InterfaceFirebase
) : BaseInteractor<V>(supportFragment, context, firebase), InterfaceInteractorLogin<V> {

    override fun signInDisposable(email: String, pass: String) {
        getView()?.addDisposable(firebase().signIn(email, pass)
            .map { authResult -> authResult.user != null }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            // **** बदला हुआ कोड: नए प्रोग्रेस डायलॉग को कॉल करें ****
            .doOnSubscribe {
                if (isNullView()) getView()?.showProgressDialog(null, getContext().getString(R.string.logging_in))
            }
            // **** बदलाव समाप्त ****
            .subscribe({ if (isNullView()) getView()?.successResult(it) },
                { if (isNullView()) getView()?.failedResult(it) })
        )
    }
}
