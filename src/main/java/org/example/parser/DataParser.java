package org.example.parser;

import org.example.dto.input.CompetitorExportDto;
import java.io.IOException;

public interface DataParser {
    CompetitorExportDto parse(String filePath) throws IOException;
}
