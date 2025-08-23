package ghac

import de.hybris.platform.scripting.engine.ScriptExecutionResult
import de.hybris.platform.scripting.engine.content.impl.SimpleScriptContent
import de.hybris.platform.scripting.engine.exception.ScriptExecutionException
import de.hybris.platform.scripting.engine.impl.PrecompiledExecutable
import groovy.json.JsonOutput
import org.apache.catalina.startup.Bootstrap
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils
import org.springframework.web.servlet.FrameworkServlet

import javax.script.Compilable
import java.nio.charset.StandardCharsets

import static de.hybris.platform.hac.scripting.impl.DefaultScriptingLanguageExecutor.*

static Map<String,WebApplicationContext> getSpringWeb() {
    Map<String,WebApplicationContext> map = [:]
    def server = Bootstrap.daemon.catalinaDaemon.server
    server.findServices().each { service ->
        service.container.findChildren().each { host ->
            host.findChildren().each { context ->
                def contextKey = []
                if (service.name != 'Catalina') contextKey << service.name
                if (host.name != service.container.defaultHost) contextKey << host.name
                contextKey << context.baseName

                // spring mvc servlets:
                def mvcServletWrappers = context.findChildren().findAll { it.servlet instanceof FrameworkServlet }
                def rootWebContext = WebApplicationContextUtils.getWebApplicationContext(context.servletContext)

                if (mvcServletWrappers) {
                    mvcServletWrappers.each { mvcServletWrapper ->
                        def attributeName = mvcServletWrapper.servlet.servletContextAttributeName
                        def springContextKey = []
                        if (mvcServletWrappers.size() > 1) {
                            springContextKey = [attributeName - FrameworkServlet.SERVLET_CONTEXT_PREFIX]
                        }
                        map << [('/' + (contextKey + springContextKey).join('/')):context.servletContext.getAttribute(attributeName)]
                    }
                } else if (rootWebContext) {
                    map << [('/' + contextKey.join('/')):rootWebContext]
                }
            }
        }
    }
    map
}

def result = [:]
def outputStream = new ByteArrayOutputStream()
def stackTraceWriter = new StringWriter()

try {

    def outputPrintStream = new PrintStream(outputStream, true, 'UTF-8')
    def outputWriter = new PrintWriter(outputPrintStream)

    def globalContext = scriptingLanguagesExecutor.prepareScriptContext(outputPrintStream)

    def springWeb = getSpringWeb()
    globalContext['springWeb'] = springWeb

    ScriptExecutionResult scriptExecutionResult = null
    String stackTraceText = null

    def decodedScript = new String(Base64.decoder.decode('$hacEncodedScript'), StandardCharsets.UTF_8)

    try {

        def scriptContent = new SimpleScriptContent('groovy', decodedScript)

        def hacSpringWebContext = '$hacSpringWebContext'
        def applicationContext = springWeb[hacSpringWebContext]

        if (hacSpringWebContext != 'default' && !applicationContext) {
            println "invalid hacSpringWebContext: ${hacSpringWebContext}, valid contexts are:"
            springWeb.keySet().each{println it}
            return [(OUTPUT_TEXT_KEY):outputStream.toString('UTF-8'), (EXECUTION_RESULT_KEY): ""]
        }

        scriptExecutionResult = scriptingLanguagesService.with {

            def scriptExecutable

            if (hacSpringWebContext == 'default') {
                scriptExecutable = getExecutableByContent(new SimpleScriptContent('groovy', decodedScript))
            } else {
                def engine = getEngine(scriptContent.engineName) as Compilable
                def scriptBody = engine.compile(scriptContent.content)
                scriptExecutable = new PrecompiledExecutable(scriptBody, globalContext, applicationContext, null)
            }

            scriptExecutable.execute(globalContext, outputWriter, stackTraceWriter)

        }

    } catch (ScriptExecutionException ex) {
        def clean = false
        def scriptException = ex.cause.cause ?: ex.cause
        def stw = new StringWriter()
        scriptException.printStackTrace(new PrintWriter(stw))
        if (clean) {
            def lines = stw.toString().readLines()
            def cleanedLines = [] as List<String>
            def append = true
            def scriptClassName = this.class.name
            lines.each { line ->
                if (line.contains(scriptClassName)) {
                    // cleanedLines << line
                    cleanedLines << '\t... hac stack'
                    append = false
                } else if (line.startsWith('Caused by:')) {
                    cleanedLines << line
                    append = true
                } else if (append) {
                    cleanedLines << line
                }
            }
            stackTraceText = cleanedLines.join('\n')
        } else {
            stackTraceText = stw.toString()
        }
    }

    result[OUTPUT_TEXT_KEY] = outputStream.toString('UTF-8')
    result[STACKTRACE_TEXT_KEY] = stackTraceText ?: stackTraceWriter.toString()
    result[EXECUTION_RESULT_KEY] = scriptExecutionResult?.scriptResult?.toString()
    return JsonOutput.toJson(result)

} catch (Throwable t) {

    result[EXECUTION_RESULT_KEY] = ''
    result[OUTPUT_TEXT_KEY] = outputStream.toString('UTF-8')

    if (!stackTraceWriter && !stackTraceWriter.toString()) {
        result[STACKTRACE_TEXT_KEY] = stackTraceWriter.toString()
    } else {
        result[STACKTRACE_TEXT_KEY] = new StringWriter().withWriter {t.printStackTrace(new PrintWriter(it)); it}.toString()
    }
    return mapObject(result)

}