package br.com.ppw.dma;

import com.jayway.jsonpath.JsonPath;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public enum WorkValidationFilter {

    CONTAINS,
    REGEX,
    JSON_PATH,
    XML_PATH;

//    public final BiFunction<String, String, String> action;


//    WorkValidationFilter(BiFunction<String, String, String> action) {
//        this.action = action;
//    }


    private static BiFunction<String, String, String> textAction() {
        return (text, value) -> (text != null && text.contains(value)) ? text : null;
    }

    private static BiFunction<String, String, String> xmlAction() {
        return (xml, path) -> {
            if(xml == null || path == null) return null;
            try {
                var doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
                var xpath = XPathFactory.newInstance().newXPath();
                return xpath.evaluate(path, doc);
            }
            catch(Exception e) {
                return null;
            }
        };
    }

    private static BiFunction<String, String, String> jsonAction() {
        return (json, path) -> {
            if(json == null || path == null) return null;
            try {
                return JsonPath.parse(json).read(path, String.class);
            }
            catch(Exception e) {
                return null;
            }
        };
    }

    private static BiFunction<String, String, String> regexAction() {
        return (text, regex) -> {
            if(text == null || regex == null) return null;
            try {
                var matcher = Pattern.compile(regex).matcher(text);
                return matcher.find() ? matcher.group() : null;
            }
            catch(Exception e) {
                return null;
            }
        };
    }

    public WorkValidation value(String valorEsperado) {
        return new WorkValidation(this, valorEsperado);
    }

    public WorkValidation value(String valorEsperado, String variavel) {
        return new WorkValidation(this, valorEsperado, variavel);
    }
}
