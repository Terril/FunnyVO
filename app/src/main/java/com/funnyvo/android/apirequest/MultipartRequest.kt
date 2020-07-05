package com.funnyvo.android.apirequest

import com.android.volley.*
import com.funnyvo.android.simpleclasses.Functions
import dagger.hilt.android.scopes.ServiceScoped
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.util.CharsetUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import javax.inject.Inject

@ServiceScoped
class MultipartRequest @Inject constructor(url: String?, errorListener: Response.ErrorListener?,
                                           private val mListener: Response.Listener<String>, private val mFilePart: Map<String, File>,
                                           private val stringPart: Map<String, String>?,
                                           private val headerParams: MutableMap<String, String>) : Request<String?>(Method.POST, url, errorListener) {
    var entity: MultipartEntityBuilder = MultipartEntityBuilder.create()
    var httpentity: HttpEntity

    private fun buildMultipartEntity() {
        for ((key, value) in mFilePart) {
            entity.addPart(key, FileBody(value, ContentType.MULTIPART_FORM_DATA, value.name + Functions.getRandomString()))
        }
        if (stringPart != null) {
            for ((key, value) in stringPart) {
                entity.addTextBody(key, value)
            }
        }
    }

    override fun getBodyContentType(): String {
        return httpentity.contentType.value
    }

    override fun getHeaders(): MutableMap<String, String> {
        return headerParams
    }

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray {
        val bos = ByteArrayOutputStream()
        try {
            httpentity.writeTo(bos)
        } catch (e: IOException) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream")
        }
        return bos.toByteArray()
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<String?>? {
        return try {
//          System.out.println("Network Response "+ new String(response.data, "UTF-8"));
            Response.success(String(response.data, Charset.defaultCharset()),
                    cacheEntry)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            // fuck it, it should never happen though
            Response.success(String(response.data), cacheEntry)
        }
    }

    init {
        entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
        try {
            entity.setCharset(CharsetUtils.get("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        buildMultipartEntity()
        httpentity = entity.build()
    }

    override fun deliverResponse(response: String?) {
        mListener.onResponse(response)
    }
}