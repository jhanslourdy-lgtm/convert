/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.Convertir.controller;


import com.example.Convertir.service.ConversionService;
import com.example.Convertir.service.ConversionService.ConversionResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class ConversionController {

    @Autowired
    private ConversionService conversionService;

    // Récupère ou crée le dictionnaire de fichiers en session
    private Map<String, StoredFiles> getStore(HttpSession session) {
        Map<String, StoredFiles> store = (Map<String, StoredFiles>) session.getAttribute("FILE_STORE");
        if (store == null) {
            store = new HashMap<>();
            session.setAttribute("FILE_STORE", store);
        }
        return store;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/convert")
    public String handleConversion(@RequestParam("file") MultipartFile file,
                                   @RequestParam("mode") String mode,
                                   HttpSession session,
                                   Model model) {
        try {
            // 1. Création d'un répertoire temporaire sécurisé
            Path tempDir = Files.createTempDirectory("aspose_conv_");
            File inputFile = new File(tempDir.toFile(), file.getOriginalFilename());
            file.transferTo(inputFile);

            // 2. Appel du service de conversion
            ConversionResult result = conversionService.convert(inputFile, mode);

            // 3. Génération d'un token unique pour sécuriser l'accès au fichier
            String token = UUID.randomUUID().toString();
            getStore(session).put(token, new StoredFiles(result.outputFile, result.previewPdf));

            // 4. Envoi des données à la vue result.html
            model.addAttribute("token", token);
            model.addAttribute("fileName", result.outputFile.getName());
            model.addAttribute("previewAvailable", result.previewPdf != null);
            
            return "result";

        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la conversion : " + e.getMessage());
            return "index";
        }
    }

    @GetMapping("/download/{token}")
    public ResponseEntity<Resource> download(@PathVariable String token, HttpSession session) {
        StoredFiles stored = getStore(session).get(token);
        if (stored == null) return ResponseEntity.notFound().build();

        File file = stored.output;
        String contentType = conversionService.guessMimeType(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(new FileSystemResource(file));
    }

    @GetMapping("/preview/{token}")
    public ResponseEntity<Resource> preview(@PathVariable String token, HttpSession session) {
        StoredFiles stored = getStore(session).get(token);
        if (stored == null || stored.previewPdf == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"preview.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new FileSystemResource(stored.previewPdf));
    }

    // Classe interne pour structurer le stockage en session
    private static class StoredFiles {
        File output;
        File previewPdf;
        StoredFiles(File o, File p) {
            this.output = o;
            this.previewPdf = p;
        }
    }
}
//
///**
// *
// * @author AQUARIAN
// */
//
//
//import com.example.Convertir.service.ConversionService;
//import jakarta.annotation.Resource;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import java.io.File;
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//@Controller
//public class ConversionController {
//
//    @Autowired
//    private ConversionService conversionService;
//
//    @GetMapping("/")
//    public String index() {
//        return "index"; // Renvoie vers index.html dans /templates/
//    }
//
//    @PostMapping("/convert")
//    public String convertFile(@RequestParam("file") MultipartFile file, 
//                              @RequestParam("mode") String mode, 
//                              Model model) throws Exception {
//        
//        // Créer un dossier temporaire
//        Path tempDir = Files.createTempDirectory("aspose_");
//        File inputFile = new File(tempDir.toFile(), file.getOriginalFilename());
//        file.transferTo(inputFile);
//
//        // Appel au service
//        var result = conversionService.convert(inputFile, mode);
//
//        // Passer les données à la vue result.html
//        model.addAttribute("fileName", result.outputFile.getName());
//        model.addAttribute("previewAvailable", result.previewPdf != null);
//        
//        return "result"; 
//    }
//
//}
