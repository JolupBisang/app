package com.imhungry.jjongseol.util

import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit

fun convertPcmToOpus(inputPath: String, outputPath: String) {
    val cmd = "-f s16le -ar 16000 -ac 1 -i $inputPath -c:a libopus $outputPath"
    Log.d("Audio", "ffmpeg 실행: $cmd")
    val session = FFmpegKit.execute(cmd)

    if (session.returnCode.isValueSuccess) {
        Log.d("Audio", "PCM → Opus 변환 성공: $outputPath")
    } else {
        Log.e("Audio", "Opus 변환 실패: ${session.failStackTrace}")
    }
}

