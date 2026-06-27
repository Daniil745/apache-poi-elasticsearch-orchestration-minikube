package org.controllers.service.parse;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

@Component
@Slf4j
public class ImageParser implements DocumentParse {

    private final Tesseract tesseract;

    public ImageParser() {
        tesseract = new Tesseract();

        String datapath = System.getenv("TESSDATA_PREFIX");
        if (datapath == null || datapath.isEmpty()) {
            datapath = "/usr/share/tessdata";
        }

        tesseract.setDatapath(datapath);
        tesseract.setLanguage("rus+eng+bel");
        tesseract.setPageSegMode(3);

        log.info("ImageParser initialized. TESSDATA_PREFIX: {}", datapath);
    }

    @Override
    public boolean supports(String extension) {
        return extension != null && (
                extension.equalsIgnoreCase("png") ||
                        extension.equalsIgnoreCase("jpg") ||
                        extension.equalsIgnoreCase("jpeg") ||
                        extension.equalsIgnoreCase("tiff") ||
                        extension.equalsIgnoreCase("bmp") ||
                        extension.equalsIgnoreCase("webp")
        );
    }

    @Override
    public String parse(InputStream inputStream) throws Exception {
        BufferedImage image = ImageIO.read(inputStream);

        if (image == null) {
            throw new Exception("Failed to read image");
        }

        log.info("Processing image: {}x{}", image.getWidth(), image.getHeight());

        try {
            String text = tesseract.doOCR(image);
            log.info("OCR extracted {} characters", text.length());
            return text;
        } catch (TesseractException e) {
            log.error("OCR failed: {}", e.getMessage());
            throw new Exception("OCR failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String[] getSupportedExtensions() {
        return new String[]{"png", "jpg", "jpeg", "tiff", "bmp", "webp"};
    }
}