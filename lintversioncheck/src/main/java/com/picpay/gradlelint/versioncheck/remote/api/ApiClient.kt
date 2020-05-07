package com.picpay.gradlelint.versioncheck.remote.api

import com.android.tools.lint.client.api.LintClient
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URLConnection
import java.nio.charset.StandardCharsets

@Suppress("UnstableApiUsage")
internal class ApiClient(private val client: LintClient) {

    fun executeRequest(request: MavenRemoteRequest): MavenRemoteResponse? {
        var response: String? = null
        try {
            val connection: URLConnection = client.openConnection(request.query) ?: return null

            try {
                val inputStream: InputStream = connection.getInputStream() ?: return null
                val bufferedReader = BufferedReader(
                    InputStreamReader(inputStream, StandardCharsets.UTF_8)
                )

                response = bufferedReader.use { reader ->
                    val sb = StringBuilder(1024)
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line)
                        sb.append('\n')
                    }
                    sb.toString()
                }
            } finally {
                client.closeConnection(connection)
            }
        } catch (ioe: IOException) {
            client.log(
                ioe, "Could not connect to ${request.query.host} to look up the " +
                        "latest available version"
            )
        }
        return response?.let { MavenRemoteResponse(response) }
    }
}