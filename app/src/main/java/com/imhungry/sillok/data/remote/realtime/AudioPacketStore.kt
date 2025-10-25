// 오디오 WebSocket 전송/재전송을 위해 회의별 PCM 패킷을 로컬에 저장/조회하는 저장소
package com.imhungry.sillok.data.remote.realtime

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

// 오디오 패킷을 읽고/쓰고/탐색하기 위한 인터페이스를 정의합니다.
interface AudioPacketStore {
    // 회의별 패킷 저장 디렉터리를 반환합니다(없으면 생성 대상 경로).
    fun getPacketDir(meetingId: Long): File
    // 로컬에 저장된 마지막 chunkId를 반환합니다(없으면 -1).
    fun findLastLocalChunkId(meetingId: Long): Long
    // 특정 chunkId 파일을 바이트 배열로 읽어옵니다.
    fun readPacket(meetingId: Long, chunkId: Long): ByteArray?
    // 특정 chunkId 파일을 디스크에 기록합니다.
    fun writePacket(meetingId: Long, chunkId: Long, bytes: ByteArray)
}

@Singleton
class FileAudioPacketStore @Inject constructor(
    @ApplicationContext private val appContext: Context
) : AudioPacketStore {

    // 앱 캐시 디렉터리 하위에 회의별 오디오 패킷 폴더를 지정합니다.
    override fun getPacketDir(meetingId: Long): File {
        return File(appContext.cacheDir, "audio_packets/$meetingId")
    }

    // 디렉터리의 파일명을 스캔하여 가장 큰 chunkId를 찾아 반환합니다.
    override fun findLastLocalChunkId(meetingId: Long): Long {
        val dir = getPacketDir(meetingId)
        dir.mkdirs()
        return dir.listFiles()
            ?.mapNotNull { it.name.removePrefix("packet_").removeSuffix(".bin").toLongOrNull() }
            ?.maxOrNull() ?: -1L
    }

    // packet_<id>.bin 파일을 읽어 바이트 배열로 반환합니다(없으면 null).
    override fun readPacket(meetingId: Long, chunkId: Long): ByteArray? {
        val dir = getPacketDir(meetingId)
        val file = File(dir, "packet_${chunkId}.bin")
        return if (file.exists()) runCatching { file.readBytes() }.getOrNull() else null
    }

    // packet_<id>.bin 파일로 오디오 패킷을 저장합니다.
    override fun writePacket(meetingId: Long, chunkId: Long, bytes: ByteArray) {
        val dir = getPacketDir(meetingId)
        dir.mkdirs()
        val file = File(dir, "packet_${chunkId}.bin")
        runCatching {
            file.outputStream().use { it.write(bytes) }
        }
    }
}


