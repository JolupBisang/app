package com.imhungry.sillok.presentation.service

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.imhungry.sillok.presentation.util.DateTimeUtils
import kotlinx.coroutines.delay
import okhttp3.WebSocket
import okio.ByteString.Companion.toByteString
import org.json.JSONObject
import java.io.File
import java.nio.ByteBuffer

/**
 * 청크 재전송을 담당하는 클래스
 */
@RequiresApi(Build.VERSION_CODES.O)
class ChunkRetransmitter(
    private val packetDir: File?,
    private val getCurrentTimestamp: () -> String
) {
    companion object {
        private const val TAG = "ChunkRetransmitter"
    }

    /**
     * 저장된 청크 정보를 담는 데이터 클래스
     */
    data class SavedChunkInfo(
        val chunkId: Long,
        val file: File
    )

    /**
     * 재전송이 필요한 청크 파일 목록 조회
     */
    fun getSavedChunksForRetransmission(lastProcessedChunkId: Long?): List<SavedChunkInfo> {
        return try {
            val dir = packetDir ?: return emptyList()
            if (!dir.exists()) {
                Log.d(TAG, "저장된 청크 디렉토리가 없습니다")
                return emptyList()
            }

            // PCM 파일 목록 조회
            val pcmFiles = dir.listFiles { file ->
                file.extension == "pcm" && file.name.startsWith("chunk_")
            } ?: emptyArray()

            if (pcmFiles.isEmpty()) {
                Log.d(TAG, "저장된 청크 파일이 없습니다")
                return emptyList()
            }

            Log.d(TAG, "로컬에 저장된 총 청크 파일: ${pcmFiles.size}개")

            // 청크 파일명에서 ID 추출하여 리스트 생성
            val savedChunks = pcmFiles.mapNotNull { file ->
                try {
                    // "chunk_123.pcm" → 123
                    val chunkId = file.nameWithoutExtension.substringAfter("chunk_").toLongOrNull()
                    if (chunkId != null) {
                        SavedChunkInfo(chunkId, file)
                    } else {
                        Log.w(TAG, "⚠청크 ID 파싱 실패: ${file.name}")
                        null
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "청크 파일 처리 실패: ${file.name}", e)
                    null
                }
            }.sortedBy { it.chunkId }

            // 서버가 받지 못한 청크만 필터링
            val missingChunks = if (lastProcessedChunkId == null) {
                // 서버가 아무것도 받지 못한 경우 → 모든 청크 재전송
                savedChunks
            } else {
                // lastProcessedChunkId보다 큰 청크만 재전송
                savedChunks.filter { it.chunkId > lastProcessedChunkId }
            }

            if (missingChunks.isNotEmpty()) {
                Log.d(TAG, "재전송 대상 청크:")
                missingChunks.forEach { chunk ->
                    Log.d(
                        TAG,
                        "  - 청크 ID: ${chunk.chunkId}, 파일: ${chunk.file.name}, 크기: ${chunk.file.length()} bytes"
                    )
                }
            }

            missingChunks

        } catch (e: Exception) {
            Log.e(TAG, "재전송 청크 조회 실패", e)
            emptyList()
        }
    }

    /**
     * 누락된 청크 재전송
     */
    suspend fun retransmitMissingChunks(
        webSocket: WebSocket,
        chunks: List<SavedChunkInfo>
    ) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "청크 재전송 시작")
        Log.d(TAG, "총 ${chunks.size}개 청크 재전송 예정")
        Log.d(TAG, "========================================")

        var successCount = 0
        var failCount = 0

        chunks.forEach { chunkInfo ->
            try {
                // 파일에서 오디오 데이터 읽기
                val audioData = chunkInfo.file.readBytes()

                // 메타데이터 파일도 읽기 (있으면)
                val dir = packetDir ?: return@forEach
                val metaFile = File(dir, "chunk_${chunkInfo.chunkId}.json")
                val timestamp = if (metaFile.exists()) {
                    try {
                        val metaJson = JSONObject(metaFile.readText())
                        metaJson.optString(
                            "timestamp",
                            DateTimeUtils.getCurrentUtcTime()
                        )
                    } catch (e: Exception) {
                        getCurrentTimestamp()
                    }
                } else {
                    getCurrentTimestamp()
                }

                // 메타데이터 생성
                val metaJson = JSONObject().apply {
                    put("type", "AUDIO_CHUNK")
                    put("chunkId", chunkInfo.chunkId)
                    put("encoding", "audio/pcm")
                    put("timestamp", timestamp)
                }

                val metaBytes = metaJson.toString().toByteArray(Charsets.UTF_8)
                val metaLength = metaBytes.size

                // 바이너리 메시지 조립
                val buffer = ByteBuffer.allocate(4 + metaLength + audioData.size)
                buffer.putInt(metaLength)
                buffer.put(metaBytes)
                buffer.put(audioData)

                val totalSize = buffer.position()
                val binaryMessage = buffer.array().toByteString(0, totalSize)

                // WebSocket으로 전송
                val success = webSocket.send(binaryMessage)

                if (success) {
                    successCount++
                    Log.d(
                        TAG,
                        "재전송 성공 - ID: ${chunkInfo.chunkId}, 크기: ${audioData.size} bytes (${successCount}/${chunks.size})"
                    )
                } else {
                    failCount++
                    Log.w(TAG, "재전송 실패 - ID: ${chunkInfo.chunkId}")
                    // 큐가 가득 찬 경우 잠시 대기
                    delay(100)
                }

                // 전송 간 짧은 딜레이 (서버 부하 방지)
                delay(10)

            } catch (e: Exception) {
                failCount++
                Log.e(TAG, "청크 재전송 실패 - ID: ${chunkInfo.chunkId}", e)
            }
        }

        Log.d(TAG, "========================================")
        Log.d(TAG, "청크 재전송 완료")
        Log.d(TAG, "성공: ${successCount}개, 실패: ${failCount}개")
        Log.d(TAG, "========================================")
    }

    /**
     * 청크 파일 디렉토리 삭제
     */
    fun deleteChunkFiles() {
        try {
            packetDir?.let { dir ->
                if (dir.exists()) {
                    val deletedCount = dir.listFiles()?.count { it.delete() } ?: 0
                    if (dir.delete()) {
                        Log.d(TAG, "청크 파일 디렉토리 삭제 완료: ${dir.absolutePath} (파일 ${deletedCount}개)")
                    } else {
                        Log.w(TAG, "청크 파일 디렉토리 삭제 실패: ${dir.absolutePath}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "청크 파일 디렉토리 삭제 중 에러 발생", e)
        }
    }
}

