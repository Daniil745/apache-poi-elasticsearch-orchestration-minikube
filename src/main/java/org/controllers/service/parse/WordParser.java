package org.controllers.service.parse;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class WordParser implements DocumentParse {


    @Override
    public boolean supports(String extension) {
        return extension != null &&
                (extension.equalsIgnoreCase("docx") ||
                        extension.equalsIgnoreCase("doc"));
    }

    @Override
    public String parse(InputStream inputStream) throws Exception {
        String extension = getExtensionFromStream(inputStream);

        if ("docx".equalsIgnoreCase(extension)) {
            return parseDocx(inputStream);
        } else {
            return parseDoc(inputStream);
        }
    }

    private String parseDocx(InputStream inputStream) throws Exception {
        StringBuilder content = new StringBuilder();

        try (XWPFDocument document = new XWPFDocument(inputStream)) {

            document.getParagraphs().forEach(paragraph -> {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    content.append(text.trim()).append("\n");
                }
            });

            document.getTables().forEach(table -> {
                table.getRows().forEach(row -> {
                    StringBuilder rowContent = new StringBuilder();
                    row.getTableCells().forEach(cell -> {
                        String cellText = cell.getText();
                        if (cellText != null && !cellText.trim().isEmpty()) {
                            rowContent.append(cellText.trim()).append("\t");
                        }
                    });
                    if (rowContent.length() > 0) {
                        content.append(rowContent.toString().trim()).append("\n");
                    }
                });
            });
        }

        return content.toString();
    }

    private String parseDoc(InputStream inputStream) throws Exception {
        try (HWPFDocument document = new HWPFDocument(inputStream)) {
            WordExtractor extractor = new WordExtractor(document);
            return extractor.getText();
        }
    }

    private String getExtensionFromStream(InputStream inputStream) {
        return "docx";
    }

    @Override
    public String[] getSupportedExtensions() {
        return new String[]{"docx", "doc"};
    }


}
