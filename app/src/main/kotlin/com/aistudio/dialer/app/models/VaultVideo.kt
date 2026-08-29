package com.aistudio.dialer.app.models

data class VaultVideo(
    val fileName: String,
    val clipFileName: String,
    val clipFileId: String,
    val localPath: String,
    val timestamp: Long,
    val fullFileId: String? = null
)
