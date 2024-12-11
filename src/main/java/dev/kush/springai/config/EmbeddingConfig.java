package dev.kush.springai.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmbeddingConfig {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingConfig.class);
    private final VectorStore vectorStore;

    public EmbeddingConfig(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

//    @PostConstruct
    public void etlPipeline() {
        ParagraphPdfDocumentReader pdfReader = new ParagraphPdfDocumentReader("classpath:pdf/Spring-Persistence-with-Hibernate.pdf",
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0)
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0)
                                .build())
                        .withPagesPerDocument(1)
                        .build());
        log.info("Reading PDF document...{}", pdfReader.read().size());
        pdfReader.read().forEach(e ->
        {
            vectorStore.accept(List.of(e));
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

}
