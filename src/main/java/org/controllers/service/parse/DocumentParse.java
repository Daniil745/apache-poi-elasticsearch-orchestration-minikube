package org.controllers.service.parse;

import java.io.InputStream;

public interface DocumentParse {
    boolean supports(String extension);

    String parse(InputStream inputStream) throws Exception;

    String[] getSupportedExtensions();
}
