package org.example.parser;

import org.example.dto.input.ExportDataDto;
import java.io.IOException;

public interface DataParser {
    ExportDataDto parse(String filePath) throws IOException;
}
