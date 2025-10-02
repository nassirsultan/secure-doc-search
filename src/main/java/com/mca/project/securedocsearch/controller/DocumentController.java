package com.mca.project.securedocsearch.controller;

import com.mca.project.securedocsearch.dto.FileResource;
import com.mca.project.securedocsearch.dto.SearchResult;
import com.mca.project.securedocsearch.exception.NotFoundException;
import com.mca.project.securedocsearch.model.Document;
import com.mca.project.securedocsearch.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/")
    public String root() {
        // show dashboard if logged in (handled by security); otherwise login page will be enforced
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("username", principal != null ? principal.getName() : "anonymous");
        return "dashboard";
    }

    /* ===== Upload ===== */
    @GetMapping("/upload")
    public String uploadForm() {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        String uploadedBy = principal != null ? principal.getName() : "anonymous";
        try {
            Document saved = documentService.store(file, uploadedBy);
            redirectAttributes.addFlashAttribute("message", "Uploaded: " + saved.getDocName());
            return "redirect:/upload";
        } catch (Exception ex) {
            log.error("Upload failed for user={} file={} : {}", uploadedBy, file.getOriginalFilename(), ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("message", "Upload failed: " + ex.getMessage());
            return "redirect:/upload";
        }
    }

    /* ===== Search ===== */
    @GetMapping("/search")
    public String searchForm() {
        return "search";
    }

    @GetMapping("/doSearch")
    public String doSearch(@RequestParam("keyword") String keyword,
                           Principal principal,
                           Model model) {
        String username = principal != null ? principal.getName() : "anonymous";
        List<SearchResult> results = documentService.searchKeyword(keyword, username);
        model.addAttribute("results", results);
        model.addAttribute("keyword", keyword);
        return "search";
    }

    /* ===== Download (admin-only) ===== */
    @GetMapping("/download/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
        try {
            FileResource fr = documentService.loadAsResource(id);
            // Encode filename for Content-Disposition
            String encoded = URLEncoder.encode(fr.filename(), StandardCharsets.UTF_8);
            String disposition = "attachment; filename*=UTF-8''" + encoded;

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fr.contentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                    .body(fr.resource());
        } catch (NotFoundException nf) {
            log.warn("Download requested for missing doc id={}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            log.error("Error streaming download for id={}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.status(500).build();
        }
    }
}
