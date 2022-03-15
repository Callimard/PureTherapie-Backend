package puretherapie.crm.api.v1.reporting.service;

import com.lowagie.text.DocumentException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import puretherapie.crm.StorageConfiguration;
import puretherapie.crm.api.v1.util.StorageService;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class PDFReportGeneratorService {

    // Variables.

    private final TemplateEngine templateEngine;
    private final StorageConfiguration storageConfiguration;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Path.of(storageConfiguration.getRoot()));
            Files.createDirectories(Path.of(storageReportRootPath()));
        } catch (IOException e) {
            throw new StorageService.StorageException(e);
        }
    }

    // Methods.

    public void generatePdfFile(String templateName, Map<String, Object> data, String pdfPath) {
        createIfNotExistPathParent(pdfPath);
        pdfPath = storageReportRootPath() + pdfPath;

        Context context = new Context();
        context.setVariables(data);

        String htmlContent = templateEngine.process(templateName, context);

        try {
            Files.deleteIfExists(Path.of(pdfPath));
            try (FileOutputStream fileOutputStream = new FileOutputStream(pdfPath)) {
                ITextRenderer renderer = new ITextRenderer();
                renderer.setDocumentFromString(htmlContent);
                renderer.layout();
                renderer.createPDF(fileOutputStream, false);
                renderer.finishPDF();
            }
        } catch (DocumentException | IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void createIfNotExistPathParent(String pdfPath) {
        Path p = Path.of(pdfPath);
        Path parent = p.getParent();
        Path rootPath = Path.of(storageReportRootPath() + parent.toString());
        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new StorageService.StorageException(e);
        }
    }

    private String storageReportRootPath() {
        return storageConfiguration.getRoot() + storageConfiguration.getReports();
    }

}
