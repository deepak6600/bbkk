package com.safe.setting.app.ui.widget.toolbar

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Animatable
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.safe.setting.app.R
import com.safe.setting.app.ui.animation.AnimationUtils.animateAlpha
import com.safe.setting.app.utils.ConstFun.isAndroidM
import com.safe.setting.app.utils.ConstFun.isShow
import com.safe.setting.app.utils.ConstFun.showKeyboard
// import com.pawegio.kandroid.hide // **** पुराना इम्पोर्ट हटा दिया गया ****
// import com.pawegio.kandroid.runDelayedOnUiThread // **** पुराना इम्पोर्ट हटा दिया गया ****
// import com.pawegio.kandroid.show // **** पुराना इम्पोर्ट हटा दिया गया ****
import kotlin.math.ceil

class CustomToolbar : FrameLayout, View.OnClickListener, Animation.AnimationListener, View.OnFocusChangeListener, TextView.OnEditorActionListener, TextWatcher {

    private lateinit var inputContainer: LinearLayout
    private lateinit var navIcon: ImageView
    private lateinit var menuIcon: ImageView
    private lateinit var clearIcon: ImageView
    private lateinit var searchEditText: EditText
    private lateinit var placeHolderView: TextView
    private lateinit var stateView : ImageView
    private lateinit var progress : ProgressBar
    private lateinit var actionDelete : ImageView
    private lateinit var last : RelativeLayout
    private lateinit var recyclerView : RecyclerView
    private lateinit var linearRecord : LinearLayout
    private lateinit var viewRecord : ImageView
    private lateinit var timerRecord : TextView

    private var onToolbarActionListener: OnToolbarActionListener? = null
    private var alphaRecord : AlphaAnimation?=null

    var enableSearch:Boolean = true
    private var enableStatePermission:Boolean = false

    var statePermission : Boolean = false
        set(state) {
            // **** बदला हुआ कोड ****
            stateView.visibility = View.VISIBLE
            // stateView.setImageResource(if (state) R.drawable.ic_status_key_enable_24dp else R.drawable.ic_status_key_disable_24dp)
            field = state
        }

    var showProgress : Boolean = false
        set(state) {
            progress.isShow(state)
            field = state
        }

    private var isActionEnabled:Boolean = false
    private var isSearchEnabled:Boolean = false
    private var isSearchDisabledForAction : Boolean = false
    private var clickClearText : Boolean = false
    private var isSuggestionsVisible: Boolean = false
    private var density: Float = 0.toFloat()
    var menu: PopupMenu? = null
        private set
    private var navIconShown = true

    var setTitle : String
        get() = placeHolderView.text.toString()
        set(title) {
            placeHolderView.text = title
            hint = title
        }

    var hint: String
        get() = searchEditText.hint.toString()
        set(hint) {
            searchEditText.hint = hint
        }

    var text: String
        get() = searchEditText.text.toString()
        set(text) = searchEditText.append(text)

    var timer : String
        get() = timerRecord.text.toString()
        set(timer) {
            timerRecord.text = timer
        }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        density = resources.displayMetrics.density
        recyclerView.layoutManager = LinearLayoutManager(context)

        setOnClickListener(this)
        clearIcon.setOnClickListener(this)
        stateView.setOnClickListener(this)
        actionDelete.setOnClickListener(this)
        searchEditText.onFocusChangeListener = this
        searchEditText.setOnEditorActionListener(this)
        searchEditText.addTextChangedListener(this)
        navIcon.setOnClickListener(this)

        navIcon.layoutParams.width = (50 * density).toInt()
        (inputContainer.layoutParams as RelativeLayout.LayoutParams).leftMargin = (50 * density).toInt()
        navIcon.requestLayout()
        placeHolderView.requestLayout()

        setupIconRippleStyle()
        inflateMenu()
        topView()
    }

    fun setOnToolbarActionListener(listener: OnToolbarActionListener) {
        this.onToolbarActionListener = listener
    }

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    private fun topView(){
        val top :Int
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        top = if (resourceId > 0) resources.getDimensionPixelSize(resourceId)
        else ceil(((if (isAndroidM())  24 else 25) * density).toDouble()).toInt()
        setPadding(0,top,0,0)
    }

    private fun setupIconRippleStyle() {
        val rippleStyle = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, rippleStyle, true)
        navIcon.setBackgroundResource(rippleStyle.resourceId)
        menuIcon.setBackgroundResource(rippleStyle.resourceId)
        clearIcon.setBackgroundResource(rippleStyle.resourceId)
    }

    private fun inflateMenu() {
        val paramsDelete = actionDelete.layoutParams as RelativeLayout.LayoutParams
        paramsDelete.rightMargin = (48 * density).toInt()
        actionDelete.layoutParams = paramsDelete
        // **** बदला हुआ कोड ****
        menuIcon.visibility = View.VISIBLE
        menuIcon.setOnClickListener(this)
        menu = PopupMenu(context, menuIcon)
        menu!!.gravity = Gravity.END
    }

    fun enableAction(){
        isActionEnabled = true
        enableSearch = false
        if (navIconShown) animateNavIcon()
        val leftIn = AnimationUtils.loadAnimation(context, R.anim.fade_in_left)
        val leftOut = AnimationUtils.loadAnimation(context, R.anim.fade_out_left)
        leftIn.setAnimationListener(this)
        if (isSearchEnabled){
            isSearchDisabledForAction = true
            isSearchEnabled = false
            val `in` = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            val out = AnimationUtils.loadAnimation(context, R.anim.fade_out)
            // **** बदला हुआ कोड ****
            placeHolderView.visibility = View.VISIBLE
            placeHolderView.startAnimation(`in`)
            inputContainer.startAnimation(out)
        }else{
            if (enableStatePermission) stateView.startAnimation(leftOut)
        }
        // **** बदला हुआ कोड ****
        actionDelete.visibility = View.VISIBLE
        actionDelete.startAnimation(leftIn)
        if (listenerExists()) onToolbarActionListener!!.onActionStateChanged(true)
    }

    @SuppressLint("SetTextI18n")
    fun disableAction(){
        isActionEnabled = false
        animateNavIcon()
        val out = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        out.setAnimationListener(this)
        actionDelete.startAnimation(out)
        if (listenerExists()){
            if (isSearchDisabledForAction && (searchEditText.text.toString() != "" || clickClearText)) {
                isSearchDisabledForAction = false
                clickClearText = false
                onToolbarActionListener!!.onButtonClicked(BUTTON_BACK)
                searchEditText.setText("")
            }
            onToolbarActionListener!!.onActionStateChanged(false)
        }
    }

    @SuppressLint("SetTextI18n")
    fun disableSearch() {
        animateNavIcon()
        isSearchEnabled = false
        val out = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        val `in` = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        out.setAnimationListener(this)
        inputContainer.startAnimation(out)
        // **** बदला हुआ कोड ****
        placeHolderView.visibility = View.VISIBLE
        placeHolderView.startAnimation(`in`)

        if (listenerExists()){
            if (searchEditText.text.toString() != "" || clickClearText){
                clickClearText = false
                onToolbarActionListener!!.onButtonClicked(BUTTON_BACK)
                searchEditText.setText("")
            }
            onToolbarActionListener!!.onSearchStateChanged(false)
        }
    }

    private fun enableSearch() {
        animateNavIcon()
        when {
            isSearchEnabled -> {}
            else -> {}
        }
        isSearchEnabled = true
        val leftIn = AnimationUtils.loadAnimation(context, R.anim.fade_in_left)
        val leftOut = AnimationUtils.loadAnimation(context, R.anim.fade_out_left)
        leftIn.setAnimationListener(this)
        // **** बदला हुआ कोड ****
        placeHolderView.visibility = View.GONE
        inputContainer.visibility = View.VISIBLE
        inputContainer.startAnimation(leftIn)
        if (listenerExists()) {
            onToolbarActionListener!!.onSearchStateChanged(true)
        }
        if (enableStatePermission) stateView.startAnimation(leftOut)
    }

    private fun animateNavIcon() {
        when {
            navIconShown -> {}
            else -> {}
        }
        val mDrawable = navIcon.drawable
        if (mDrawable is Animatable) {
            (mDrawable as Animatable).start()
        }
        navIconShown = !navIconShown
    }

    fun animateRecord(record:Boolean){
        if (record) alphaRecord = viewRecord.animateAlpha()
        else { alphaRecord?.cancel() ; alphaRecord?.reset() }
        val lp = linearRecord.layoutParams
        val animator = if (record) ValueAnimator.ofInt(0,(35 * density).toInt())
        else ValueAnimator.ofInt((35 * density).toInt(),0)
        animator.duration = 200
        animator.addUpdateListener { animation ->
            lp.height = animation.animatedValue as Int
            linearRecord.layoutParams = lp
        }
        animator.start()
        // **** बदला हुआ कोड ****
        Handler(Looper.getMainLooper()).postDelayed({
            if (listenerExists()) onToolbarActionListener?.onChangeHeight()
        }, 205)
    }

    private fun listenerExists(): Boolean {
        return onToolbarActionListener != null
    }

    override fun onAnimationStart(animation: Animation) {}
    override fun onAnimationEnd(animation: Animation?) {
        TODO("Not yet implemented")
    }
    override fun onAnimationRepeat(animation: Animation) {}

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        searchEditText.showKeyboard(hasFocus)
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        val filter = searchEditText.text.toString()
        when {
            filter!="" -> {}
        }
        searchEditText.showKeyboard(false)
        return true
    }

    private fun textSearchConfirmed(text: String){
        if (listenerExists()) onToolbarActionListener!!.onSearchConfirmed(text)
    }

    override fun afterTextChanged(s: Editable) {
        if (s.isEmpty()) textSearchConfirmed(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        // **** बदला हुआ कोड ****
        if (s.isNotEmpty()) { textSearchConfirmed(s.toString()) ; clearIcon.visibility = View.VISIBLE }
        else clearIcon.visibility = View.GONE
    }

    interface OnToolbarActionListener {
        fun onSearchStateChanged(enabled: Boolean)
        fun onSearchConfirmed(text: CharSequence)
        fun onButtonClicked(buttonCode: Int)
        fun onActionStateChanged(enabled: Boolean)
        fun onChangeHeight()
    }

    companion object {
        const val BUTTON_CHILD_USER = 1
        const val BUTTON_NAVIGATION = 2
        const val BUTTON_BACK = 3
        const val BUTTON_STATE = 4
        const val BUTTON_ACTION_DELETE = 5
    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }
}
