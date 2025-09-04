package com.safe.setting.app.utils.async

import android.text.TextUtils
import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class AsyncTaskRunCommand(
    private val onPreFunc: (() -> Unit)? = null,
    private val onPostFunc: ((result: Boolean) -> Unit)? = null
) {



    @OptIn(DelicateCoroutinesApi::class)
    fun execute(command: String) {
        onPreFunc?.invoke()

        GlobalScope.launch(Dispatchers.IO) {

            val result: Boolean = executeCommandWithRoot(command).result

            launch(Dispatchers.Main) {
                onPostFunc?.invoke(result)
            }
        }
    }

    private fun executeCommandWithRoot(command: String): Result {
        val builder = Result.newBuilder()
        try {
            if (TextUtils.isEmpty(command)) {
                return builder.setFailed().build()
            }
            val process = Runtime.getRuntime().exec("su")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()

            // Write command to output stream
            process.outputStream.write((command + "\n").toByteArray())
            process.outputStream.flush()
            process.outputStream.write("exit\n".toByteArray())
            process.outputStream.flush()

            // Read command output
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }

            // Wait for the process to finish
            process.waitFor()

            // Handle command output
            val result = output.toString()
            Log.d("RootCommand", "Result: $result")
        } catch (e: IOException) {
            e.printStackTrace()
            builder.setCommandFailed()
        } catch (e: InterruptedException) {
            e.printStackTrace()
            builder.setCommandFailedInterrupted()
        }
        return builder.build()
    }
}


class Result private constructor() {
    /* members */
    var message: String? = null
        private set
    var statusCode = 0
        private set
    val result: Boolean
        get() {
            Log.d("Status Code","$statusCode")
            return if (statusCode == 0) {
                true
            } else if (statusCode <= 100) {
                true
            } else {
                false
            }
        }

    enum class ResultEnum(val statusCode: Int, var message: String) {
        COMMAND_SUCCESS(90, "Command Executed Successfully"),
        COMMAND_FAILED_TIMEOUT(401, "Run Command Timeout"),
        COMMAND_FAILED_DENIED(402, "Run Command Permission Denied"),
        COMMAND_FAILED_INTERRUPTED(403, "Run Command Interrupted"),
        COMMAND_FAILED(409, "Run Command Failed"),
        INSTALL_SUCCESS(80, "Application installed Successfully"),
        INSTALL_FAILED_NOSPACE(404, "Install Failed because of no enough space"),
        INSTALL_FAILED_WRONGCONTAINER(405, "Install Failed Wrong container"),
        INSTALL_FAILED_WRONGCER(406, "Install Failed Wrong Cer or version"),
        INSTALL_FIALED(407, "Install Failed"),
        UNINSTALL_SUCCESS(70, "Application uninstall Successfully"),
        UNINSTALL_FAILED(408, "Uninstall App Failed"),
        FAILED(409, "Illegal Parameters or State"),
        CUSTOM(0, "");

        fun setCustomMessage(customMessage: String) {
            message = customMessage
        }
    }

    class ResultBuilder {
        private var inEnum: ResultEnum? = null
        fun setCommandSuccess(): ResultBuilder {
            inEnum = ResultEnum.COMMAND_SUCCESS
            return this
        }

        fun setCommandFailedTimeout(): ResultBuilder {
            inEnum = ResultEnum.COMMAND_FAILED_TIMEOUT
            return this
        }

        fun setCommandFailedDenied(): ResultBuilder {
            inEnum = ResultEnum.COMMAND_FAILED_DENIED
            return this
        }

        fun setCommandFailedInterrupted(): ResultBuilder {
            inEnum = ResultEnum.COMMAND_FAILED_INTERRUPTED
            return this
        }

        fun setCommandFailed(): ResultBuilder {
            inEnum = ResultEnum.COMMAND_FAILED
            return this
        }

        fun setInstallSuccess(): ResultBuilder {
            inEnum = ResultEnum.INSTALL_SUCCESS
            return this
        }

        fun setInsallFailedNoSpace(): ResultBuilder {
            inEnum = ResultEnum.INSTALL_FAILED_NOSPACE
            return this
        }

        fun setInstallFailedWrongContainer(): ResultBuilder {
            inEnum = ResultEnum.INSTALL_FAILED_WRONGCONTAINER
            return this
        }

        fun setInstallFailedWrongCer(): ResultBuilder {
            inEnum = ResultEnum.INSTALL_FAILED_WRONGCER
            return this
        }

        fun setInstallFailed(): ResultBuilder {
            inEnum = ResultEnum.INSTALL_FIALED
            return this
        }

        fun setUninstallSuccess(): ResultBuilder {
            inEnum = ResultEnum.UNINSTALL_SUCCESS
            return this
        }

        fun setUninstallFailed(): ResultBuilder {
            inEnum = ResultEnum.UNINSTALL_FAILED
            return this
        }

        fun setFailed(): ResultBuilder {
            inEnum = ResultEnum.FAILED
            return this
        }

        fun setCustomMessage(customMessage: String): ResultBuilder {
            inEnum = ResultEnum.CUSTOM
            inEnum!!.setCustomMessage(customMessage)
            return this
        }

        fun build(): Result {
            checkNotNull(inEnum) {
                "Get a empty or null error message during command execution, can not " +
                        "generate result object"
            }
            val re = Result()
            re.message = inEnum!!.message
            re.statusCode = inEnum!!.statusCode
            return re
        }
    }

    companion object {
        fun newBuilder(): ResultBuilder {
            return ResultBuilder()
        }
    }
}