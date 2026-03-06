/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.Convertir.service;

/**
 *
 * @author AQUARIAN
 */

import com.aspose.pdf.SaveFormat;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.UUID;

@Service // Indique à Spring que c'est un composant de la couche métier
public class ConversionService {

    // Classe interne pour transporter le résultat
    public static class ConversionResult {
        public final File outputFile;
        public final File previewPdf;
        public ConversionResult(File outputFile, File previewPdf){
            this.outputFile = outputFile;
            this.previewPdf = previewPdf;
        }
    }

    // --- Utilitaires (non statiques pour Spring) ---
    
    private String baseNameWithoutExt(String name){
        int dot = name.lastIndexOf('.');
        return (dot > 0) ? name.substring(0, dot) : name;
    }

    private String safeExt(String filename){
        int dot = filename.lastIndexOf('.');
        return (dot > 0) ? filename.substring(dot + 1).toLowerCase() : "";
    }

    private String unique(String prefix){
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Méthode principale de conversion
     */
    public ConversionResult convert(File inputFile, String mode) throws Exception {
        String originalName = inputFile.getName();
        String base = baseNameWithoutExt(originalName);
        File outDir = inputFile.getParentFile();

        switch (mode) {
            case "WORD_TO_PDF": {
                File out = new File(outDir, unique(base) + ".pdf");
                com.aspose.words.Document doc = new com.aspose.words.Document(inputFile.getAbsolutePath());
                doc.save(out.getAbsolutePath(), com.aspose.words.SaveFormat.PDF);
                return new ConversionResult(out, out);
            }

            case "PDF_TO_WORD": {
                File out = new File(outDir, unique(base) + ".docx");
                com.aspose.pdf.Document pdf = new com.aspose.pdf.Document(inputFile.getAbsolutePath());
                pdf.save(out.getAbsolutePath(), SaveFormat.DocX);

                // Tentative d'aperçu PDF
                File preview = new File(outDir, unique(base) + "-preview.pdf");
                try {
                    com.aspose.words.Document docx = new com.aspose.words.Document(out.getAbsolutePath());
                    docx.save(preview.getAbsolutePath(), com.aspose.words.SaveFormat.PDF);
                    return new ConversionResult(out, preview);
                } catch (Throwable t){
                    return new ConversionResult(out, null);
                }
            }

            case "PDF_TO_EXCEL": {
                File out = new File(outDir, unique(base) + ".xlsx");
                com.aspose.pdf.Document pdf = new com.aspose.pdf.Document(inputFile.getAbsolutePath());
                pdf.save(out.getAbsolutePath(), SaveFormat.Excel);

                File preview = new File(outDir, unique(base) + "-preview.pdf");
                try {
                    com.aspose.cells.Workbook wb = new com.aspose.cells.Workbook(out.getAbsolutePath());
                    wb.save(preview.getAbsolutePath(), com.aspose.cells.SaveFormat.PDF);
                    return new ConversionResult(out, preview);
                } catch (Throwable t){
                    return new ConversionResult(out, null);
                }
            }

            default:
                throw new IllegalArgumentException("Mode de conversion inconnu: " + mode);
        }
    }

    /**
     * Utilitaire pour déterminer le type MIME lors du téléchargement
     */
    public String guessMimeType(File file){
        String ext = safeExt(file.getName());
        switch (ext){
            case "pdf": return "application/pdf";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default: return "application/octet-stream";
        }
    }
}