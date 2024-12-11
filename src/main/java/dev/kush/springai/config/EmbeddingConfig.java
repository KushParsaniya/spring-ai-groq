package dev.kush.springai.config;

import com.knuddels.jtokkit.api.EncodingType;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Configuration
public class EmbeddingConfig {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingConfig.class);
    private final VectorStore vectorStore;

    public EmbeddingConfig(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Bean
    public BatchingStrategy batchingStrategy() {
        return new TokenCountBatchingStrategy(
                EncodingType.CL100K_BASE,  // Specify the encoding type
                7000,                      // Set the maximum input token count
                0.1                        // Set the reserve percentage
        );
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
            e.getMetadata().put("userId", "kush");
            vectorStore.accept(List.of(e));
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

}
