custom chat input component —— InputView
===

### 1、xml add InputView

```
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <cn.mtjsoft.inputview.InputView
        android:id="@+id/bottom_inputview"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:minHeight="48dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```
### 2、activity use demo

```
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputView = findViewById<InputView>(R.id.bottom_inputview)

        // 功能面板示例数据
        val functionData = LinkedList<FunctionEntity>()
        val ids = listOf(
            cn.mtjsoft.inputview.R.mipmap.btn_skb_record,
            cn.mtjsoft.inputview.R.mipmap.btn_skb_picture,
            cn.mtjsoft.inputview.R.mipmap.btn_skb_file
        )
        val names = listOf("拍照", "相册", "文件")
        ids.mapIndexed { index, i ->
            functionData.add(FunctionEntity(i, names[index]))
        }

        inputView
            // 设置功能面板数据
            .setFuncationData(functionData)
            // 设置功能面板点击回调
            .setFuncationClickListener(object : AdapterItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    Toast.makeText(baseContext, "功能点击：${functionData[position].name}", Toast.LENGTH_SHORT).show()
                }
            })
            // 设置发送按钮点击回调
            .setSendClickListener(object : SendClickListener {
                override fun onSendClick(view: View, content: String) {
                    Toast.makeText(baseContext, "发送内容：$content", Toast.LENGTH_SHORT).show()
                }
            })
            // 设置录音完成回调
            .setVoiceOverListener(object : VoiceOverListener {
                override fun onOver(fileName: String, filePath: String, duration: Int) {
                    Log.e("mtj", "PCM录音保存地址：$filePath PCM录音时长：$duration 秒")
                    // 播放PCM格式音频
                    PCMAudioPlayer.instance.startPlay(filePath)
                }
            })
    }

    override fun onPause() {
        super.onPause()
        // 释放音频播放
        PCMAudioPlayer.instance.release()
    }
}
```


