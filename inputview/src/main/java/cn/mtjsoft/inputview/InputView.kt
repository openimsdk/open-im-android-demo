package cn.mtjsoft.inputview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.TextUtils
import android.util.ArrayMap
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.mtjsoft.inputview.adapter.EmojiAdapter
import cn.mtjsoft.inputview.adapter.EmojiTypeAdapter
import cn.mtjsoft.inputview.adapter.FuncationAdapter
import cn.mtjsoft.inputview.entity.EmojiEntry
import cn.mtjsoft.inputview.entity.FunctionEntity
import cn.mtjsoft.inputview.iml.AdapterItemClickListener
import cn.mtjsoft.inputview.iml.SendClickListener
import cn.mtjsoft.inputview.iml.VoiceOverListener
import cn.mtjsoft.inputview.view.LongPressTextView
import java.util.*


/**
 * 自定义IM输入控件
 * 包含表情库、操作面板
 */
class InputView : LinearLayout {

    private val EMOJI_ASSERT_SRC = "emoji"
    private val EMOJI_SUFFIX = ".png"
    private val FACE_TYPE = "face"

    // 输入框布局
    private lateinit var inputView: LinearLayout
    private lateinit var mEtInput: EditText
    private lateinit var openEmojiView: ImageView

    // 表情/功能面板
    private lateinit var mRecyclerView: RecyclerView

    // 功能数据
    private lateinit var gridLayoutManagerSpan4: GridLayoutManager
    private val functionData = LinkedList<FunctionEntity>()
    private lateinit var funcationAdapter: FuncationAdapter

    // 表情数据
    private lateinit var gridLayoutManagerSpan6: GridLayoutManager
    private val emojiGroup = ArrayMap<String, LinkedList<EmojiEntry>>()
    private val emojiData = LinkedList<EmojiEntry>()
    private var emojiAdapter: EmojiAdapter? = null

    // 表情类型
    private lateinit var emojiTypeView: LinearLayout
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var emojiTypeAdapter: EmojiTypeAdapter

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        orientation = VERTICAL
        initView()
        initEmojiTypeData()
        initEmojiData(false)
        initFunctionData()
        // 监听生命周期，隐藏面板
        if (context is androidx.appcompat.app.AppCompatActivity) {
            context.lifecycle.addObserver(object : LifecycleObserver {

                @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                private fun pauseMeter() {
                    hideEmojiView(false)
                }
            })
        }
    }

    private val MAIN_HANDLER = Handler(Looper.getMainLooper())

    private fun initView() {
        // 输入控制面板
        inputView =
            LayoutInflater.from(context).inflate(R.layout.input_view, this, true)
                .findViewById(R.id.inputLayout)
        // 表情库、常用面板
        mRecyclerView =
            LayoutInflater.from(context)
                .inflate(R.layout.symbols_emoji_recycleview, this, true)
                .findViewById(R.id.gv_symbols_emoji)
        // 表情类型
        emojiTypeView =
            LayoutInflater.from(context)
                .inflate(R.layout.emoji_type_view, this, true)
                .findViewById(R.id.ll_symbols_emoji_type_item)
        inputViewChildView()
    }

    /**
     * 输入控制面板 子view
     */
    private fun inputViewChildView() {
        mEtInput = findViewById(R.id.et_chat_input)
        mEtInput.setOnClickListener {
            hideEmojiView(true)
        }
        mEtInput.setOnFocusChangeListener { view, b ->
            // 面板已显示时，获取光标
            if (b && mRecyclerView.visibility == VISIBLE) {
                hideEmojiView(true)
            }
        }
        val voiceImageView = findViewById<ImageView>(R.id.iv_voice)
        val mRecordView = findViewById<LongPressTextView>(R.id.long_press_tv)
        val sendBtn = findViewById<TextView>(R.id.bt_chat_send)
        val addImageView = findViewById<ImageView>(R.id.iv_add_image)
        mEtInput.addTextChangedListener {
            it?.let {
                sendBtn.visibility = if (TextUtils.isEmpty(it.toString())) GONE else VISIBLE
                addImageView.visibility = if (TextUtils.isEmpty(it.toString())) VISIBLE else GONE
            }
        }
        // 发送点击
        sendBtn.setOnClickListener {
            // 点击发送消息
            sendClickListener?.onSendClick(it, mEtInput.text.toString())
            mEtInput.setText("")
        }
        // 语音点击
        voiceImageView.setOnClickListener {
            voiceImageView.setImageResource(if (mRecordView.visibility == VISIBLE) R.mipmap.ic_read_voice else R.mipmap.icon_keyboard)
            mEtInput.visibility = if (mRecordView.visibility == VISIBLE) VISIBLE else GONE
            hideEmojiView(mRecordView.visibility == VISIBLE)
            if (mRecordView.visibility == GONE) {
                hideKeyboard(it.windowToken)
                mEtInput.setText("")
            }
            mRecordView.visibility = if (mRecordView.visibility == VISIBLE) GONE else VISIBLE
        }
        // 表情显示/隐藏
        openEmojiView = findViewById(R.id.iv_emoji)
        openEmojiView.setOnClickListener {
            // 还原输入框
            voiceImageView.setImageResource(R.mipmap.ic_read_voice)
            mRecordView.visibility = GONE
            mEtInput.visibility = VISIBLE
            // 展开或隐藏表情面板
            if (emojiTypeView.visibility == GONE) {
                // 展开
                openEmojiView.setImageResource(R.mipmap.icon_keyboard)
                hideKeyboard(it.windowToken)
                MAIN_HANDLER.postDelayed({
                    initEmojiData(true)
                }, 100)
            } else {
                hideEmojiView(true)
            }
        }
        // 开启/关闭功能面板
        addImageView.setOnClickListener {
            // 还原输入框
            voiceImageView.setImageResource(R.mipmap.ic_read_voice)
            mRecordView.visibility = GONE
            mEtInput.visibility = VISIBLE
            openEmojiView.setImageResource(R.mipmap.ic_emjio)
            // 点击添加，切换功能面板
            if (mRecyclerView.layoutManager == gridLayoutManagerSpan4 && mRecyclerView.visibility == VISIBLE) {
                // 已显示功能面板，再次点击，隐藏
                hideEmojiView(true)
            } else {
                // 显示功能面板
                hideKeyboard(it.windowToken)
                MAIN_HANDLER.postDelayed({
                    if (mRecyclerView.layoutManager == gridLayoutManagerSpan4) {
                        funcationAdapter.notifyDataChanged()
                    } else {
                        mRecyclerView.layoutManager = gridLayoutManagerSpan4
                        mRecyclerView.adapter = funcationAdapter
                    }
                    emojiTypeView.visibility = GONE
                    mRecyclerView.visibility = VISIBLE
                }, 100)
            }
        }

        // 设置录音监听
        mRecordView.setOnLongPressListener(object : LongPressTextView.onLongPressListener {
            override fun onRecordOver(currentDur: Long, fileName: String, filePath: String) {
                voiceOverListener?.onOver(fileName, filePath, (currentDur / 1000).toInt())
            }
        })
    }

    /**
     * 初始化emoji数据
     */
    private fun initEmojiData(showEmojiView: Boolean) {
        val typeEns = context.resources.getStringArray(R.array.emoji_types_en)
        if (emojiGroup.size == typeEns.size) {
            // 数据已初始化过了
            setDefaultFaceEmoji(showEmojiView)
        } else {
            Constant.DEFAULT_EXECUTOR.execute {
                typeEns.mapIndexed { _, dir ->
                    val tempFiles: Array<String>? = context.assets.list("$EMOJI_ASSERT_SRC/$dir")
                    val tempEmojis = LinkedList<EmojiEntry>()
                    tempFiles?.mapIndexed { _, s ->
                        tempEmojis.add(
                            EmojiEntry(
                                s.substringBefore(EMOJI_SUFFIX),
                                s,
                                "$EMOJI_ASSERT_SRC/$dir/$s"
                            )
                        )
                    }
                    emojiGroup.put(dir, tempEmojis)
                }
                MAIN_HANDLER.post {
                    setDefaultFaceEmoji(showEmojiView)
                }
            }
        }
    }

    /**
     * 设置默认黄脸表情
     */
    private fun setDefaultFaceEmoji(showEmojiView: Boolean) {
        if (emojiAdapter == null) {
            emojiData.clear()
            emojiGroup[FACE_TYPE]?.let {
                emojiData.addAll(it)
            }
            emojiAdapter = EmojiAdapter(context, emojiData, object : AdapterItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    // emoji点击，添加入输入框
                    val result = getEmoji(emojiData[position].code)
                    mEtInput.text.insert(mEtInput.selectionStart, result)
                }
            })
            gridLayoutManagerSpan6 = GridLayoutManager(context, 6)
            mRecyclerView.layoutManager = gridLayoutManagerSpan6
            mRecyclerView.adapter = emojiAdapter
        } else {
            setEmojiByType(FACE_TYPE)
        }
        // 显示
        mRecyclerView.visibility = if (showEmojiView) VISIBLE else GONE
        emojiTypeView.visibility = if (showEmojiView) VISIBLE else GONE
        //
        emojiTypeAdapter.setClickPosition(0)
        linearLayoutManager.scrollToPosition(0)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setEmojiByType(type: String) {
        emojiData.clear()
        emojiGroup[type]?.let {
            emojiData.addAll(it)
        }
        if (mRecyclerView.layoutManager == gridLayoutManagerSpan4) {
            mRecyclerView.layoutManager = gridLayoutManagerSpan6
            mRecyclerView.adapter = emojiAdapter
        } else {
            emojiAdapter?.notifyDataSetChanged()
        }
        mRecyclerView.visibility = VISIBLE
        emojiTypeView.visibility = VISIBLE
        gridLayoutManagerSpan6.scrollToPosition(0)
    }

    /**
     * 初始化emoji类型数据
     */
    private fun initEmojiTypeData() {
        val recyclerView: RecyclerView = findViewById(R.id.rv_symbols_emoji_type)
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = linearLayoutManager
        val typeEns = context.resources.getStringArray(R.array.emoji_types_en)
        emojiTypeAdapter = EmojiTypeAdapter(
            context,
            context.resources.getStringArray(R.array.emoji_types).toList(),
            object : AdapterItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    setEmojiByType(typeEns[position])
                }
            })
        recyclerView.adapter = emojiTypeAdapter
        findViewById<ImageView>(R.id.ib_symbols_emoji_type_back).setOnClickListener {
            // 返回按键
            hideEmojiView(true)
        }

        findViewById<ImageView>(R.id.iv_symbols_emoji_type_delete).setOnClickListener {
            // 删除按键
            deleteEmoji(mEtInput)
        }
    }

    /**
     * 初始化功能面板数据
     */
    private fun initFunctionData() {
        gridLayoutManagerSpan4 = GridLayoutManager(context, 4)
        val ids = listOf(R.mipmap.btn_skb_record, R.mipmap.btn_skb_picture, R.mipmap.btn_skb_file)
        val names = listOf("拍照", "相册", "文件")
        functionData.clear()
        ids.mapIndexed { index, i ->
            functionData.add(FunctionEntity(i, names[index]))
        }
        // adapter初始化
        funcationAdapter =
            FuncationAdapter(context, functionData, object : AdapterItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    // 点击功能面板
                    funcationClickListener?.onItemClick(view, position)
                }
            })
    }

    /**
     * 隐藏表情面板
     * 并设置是否弹出软键盘
     */
    private fun hideEmojiView(showSoftInput: Boolean) {
        openEmojiView.setImageResource(R.mipmap.ic_emjio)
        mRecyclerView.visibility = GONE
        emojiTypeView.visibility = GONE
        if (showSoftInput) {
            // 获取焦点
            mEtInput.requestFocus()
            val manager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            manager?.showSoftInput(mEtInput, 0)
        }
    }

    /**
     * 获取InputMethodManager，隐藏软键盘
     */
    private fun hideKeyboard(token: IBinder) {
        val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        im?.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    /**
     * 根据code 获取 Emoji
     */
    private fun getEmoji(code: String): String {
        val codes = code.split("_")
        val result = StringBuilder()
        for (s in codes) {
            result.append(String(Character.toChars(s.toInt(16))))
        }
        return result.toString()
    }

    /**
     * 使用系统的输入框中删除方法，
     * 防止Emoji删除出错
     *
     * @param mEtInput 输入框
     */
    private fun deleteEmoji(mEtInput: EditText) {
        val keyCode = KeyEvent.KEYCODE_DEL
        val keyEventDown = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val keyEventUp = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        mEtInput.onKeyDown(keyCode, keyEventDown)
        mEtInput.onKeyUp(keyCode, keyEventUp)
    }


    /**
     * ========================== 设置各种参数 ==========================
     */

    /**
     * 发送回调
     */
    private var sendClickListener: SendClickListener? = null

    fun setSendClickListener(sendClickListener: SendClickListener): InputView {
        this.sendClickListener = sendClickListener
        return this
    }

    /**
     * 语音录制完成回调
     */
    private var voiceOverListener: VoiceOverListener? = null

    fun setVoiceOverListener(voiceOverListener: VoiceOverListener): InputView {
        this.voiceOverListener = voiceOverListener
        return this
    }

    /**
     * 设置功能面板点击回调
     */
    private var funcationClickListener: AdapterItemClickListener? = null

    fun setFuncationClickListener(itemClickListener: AdapterItemClickListener): InputView {
        this.funcationClickListener = itemClickListener
        return this
    }

    /**
     * 设置功能面板数据
     */
    fun setFuncationData(data: List<FunctionEntity>) {
        functionData.clear()
        functionData.addAll(data)
        funcationAdapter.notifyDataChanged()
    }
}