package com.sample.mix.demo.jsbridge

import com.sample.mix.demo.repository.data.response.JSResponse

interface IJSSyncRequest {
    fun onRequest(params: String): JSResponse<Any>
}