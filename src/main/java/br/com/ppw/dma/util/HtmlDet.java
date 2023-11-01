package br.com.ppw.dma.util;

import br.com.ppw.dma.system.Arquivos;
import lombok.val;

import java.io.File;

public abstract class HtmlDet {

    public static final String TEMPLATE_HTML = "template/template-p1.html";
    public static final String TEMPLATE_JS = "template/template-p1.html";
    public static final String TEMPLATE_CSS = "template/template-p1.html";
    public static final String TAG_JS = "<script src=\"./template-content.js\"></script>";
    public static final String TAG_CSS = "<link rel=\"stylesheet\" href=\"./template.css\">";

    public static File createNewDet() {
        val arquivoHtml = new File(TEMPLATE_HTML);
        val conteudoHtml = Arquivos.lerArquivo(arquivoHtml);

    }

}
