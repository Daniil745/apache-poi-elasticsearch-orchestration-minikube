package org.controllers.service.parse;

import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.usermodel.TextShape;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class PptParser implements DocumentParse {


    @Override
    public boolean supports(String extension) {
        return extension != null &&
                (extension.equalsIgnoreCase("pptx") ||
                        extension.equalsIgnoreCase("ppt"));
    }

    @Override
    public String parse(InputStream inputStream) throws Exception {
        // Пробуем сначала новый формат
        try {
            return parsePptx(inputStream);
        } catch (Exception e) {
            return parsePpt(inputStream);
        }
    }

    private String parsePptx(InputStream inputStream) throws Exception {
        StringBuilder content = new StringBuilder();

        try (XMLSlideShow ppt = new XMLSlideShow(inputStream)) {
            int slideNumber = 1;

            for (XSLFSlide slide : ppt.getSlides()) {
                content.append("Slide ").append(slideNumber).append(":\n");

                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        String text = textShape.getText();
                        if (text != null && !text.trim().isEmpty()) {
                            content.append(text.trim()).append("\n");
                        }
                    }
                }

                if (slide.getNotes() != null) {
                    for (XSLFShape noteShape : slide.getNotes().getShapes()) {
                        if (noteShape instanceof XSLFTextShape) {
                            String notesText = ((XSLFTextShape) noteShape).getText();
                            if (notesText != null && !notesText.trim().isEmpty()) {
                                content.append("Notes: ").append(notesText.trim()).append("\n");
                            }
                        }
                    }
                }

                content.append("\n");
                slideNumber++;
            }
        }

        return content.toString();
    }


    private String parsePpt(InputStream inputStream) throws Exception {
        StringBuilder content = new StringBuilder();

        try (HSLFSlideShow ppt = new HSLFSlideShow(inputStream)) {
            int slideNumber = 1;

            for (org.apache.poi.hslf.usermodel.HSLFSlide slide : ppt.getSlides()) {
                content.append("Slide ").append(slideNumber).append(":\n");

                slide.getShapes().forEach(shape -> {
                    if (shape instanceof TextShape) {
                        TextShape textShape = (TextShape) shape;
                        String text = textShape.getText();
                        if (text != null && !text.trim().isEmpty()) {
                            content.append(text.trim()).append("\n");
                        }
                    }
                });

                content.append("\n");
                slideNumber++;
            }
        }

        return content.toString();
    }

    @Override
    public String[] getSupportedExtensions() {
        return new String[]{"pptx", "ppt"};
    }
}
