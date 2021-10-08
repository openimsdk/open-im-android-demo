package cn.mtjsoft.inputview.iml

/**
 * 语音录制完成接口
 *
 * @param fileName 保存的文件名称
 * @param filePath 保存的文件路径，发送后根据路径自己删除本地文件
 * @param duration 保存的文件时长（单位秒）
 */
interface VoiceOverListener {
    fun onOver(fileName: String, filePath: String, duration: Int)
}