package com.leox.self.myloves.utils

import com.leox.self.myloves.MyApp
import org.python.core.PyFunction
import org.python.core.PyObject
import org.python.util.PythonInterpreter
import java.io.*
import java.util.*
import org.python.core.Py




object PythonCaller {
    val dir = File(MyApp.instance.filesDir, "python")

    init {
        val props = Properties()
//        props.put("python.home", jythonHome)
        props.put("python.console.encoding", "UTF-8")
        props.put("python.security.respectJavaAccessibility", "false")
        props.put("python.import.site", "false")
        val preprops = System.getProperties()
        PythonInterpreter.initialize(preprops, props, arrayOfNulls(0))
//        val sys = Py.getSystemState()
//        sys.path.add(dir)
        initDir()
    }

    private fun initDir() {
        if (!dir.exists() || dir.isFile) {
            dir.delete()
            dir.mkdirs()
        }
    }

    fun callPythonFileFunc(src: String, funcName: String, vararg args: PyObject) {
        ThreadManager.exec {
            val realPath = checkFile(src)
            val interpreter = PythonInterpreter()
            interpreter.execfile(realPath)
            val func = interpreter.get(funcName,
                    PyFunction::class.java)
            val pyobj = func.__call__(args)
        }
    }

    private fun checkFile(src: String): String {
        val file = File(dir, src)
        return if (file.exists() && file.isFile && file.canRead() && file.length() > 0) {
            file.absolutePath
        } else {
            val bufferedReader = BufferedReader(InputStreamReader(MyApp.instance.assets.open(src)))
            val bufferedWriter = BufferedWriter(FileWriter(file))
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                bufferedWriter.write(line)
                bufferedWriter.newLine()
                line = bufferedReader.readLine()
            }
            bufferedReader.close()
            bufferedWriter.close()
            file.absolutePath
        }
    }
}