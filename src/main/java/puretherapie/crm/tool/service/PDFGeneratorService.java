package puretherapie.crm.tool.service;

import com.lowagie.text.DocumentException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class PDFGeneratorService {

    // Variables.

    private final TemplateEngine templateEngine;

    // Methods.

    public void generatePdfFile(String templateName, Map<String, Object> data, String pdfPath) {
        Context context = new Context();
        context.setVariables(data);

        String htmlContent = templateEngine.process(templateName, context);
        try (FileOutputStream fileOutputStream = new FileOutputStream(pdfPath)) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(fileOutputStream, false);
            renderer.finishPDF();
        } catch (DocumentException | IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}
