package com.xyoye.common_component.utils

import android.text.TextUtils
import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


/**
 * Created by xyoye on 2022/1/14
 */
object EntropyUtils {

    /**
     * md5加密字符串
     */
    fun string2Md5(string: String?): String {
        if (TextUtils.isEmpty(string))
            return ""

        val messageDigest = MessageDigest.getInstance("MD5")
        messageDigest.update(string!!.toByteArray())
        return buffer2Hex(messageDigest.digest())
    }

    /**
     * AES加密字符串
     */
    fun aesEncode(key: String, content: String, base64Flag: Int = Base64.DEFAULT): String? {
        try {
            val secretKey = SecretKeySpec(key.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(ByteArray(16)))

            val byteEncode = content.toByteArray(Charsets.UTF_8)
            val byteAES = cipher.doFinal(byteEncode)
            return Base64.encodeToString(byteAES, base64Flag)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * AES解密字符串
     */
    fun aesDecode(key: String, content: String, base64Flag: Int = Base64.DEFAULT): String? {
        try {
            val secretKey = SecretKeySpec(key.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ByteArray(16)))

            val byteContent = Base64.decode(content, base64Flag)
            val byteDecode = cipher.doFinal(byteContent)

            return String(byteDecode, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}