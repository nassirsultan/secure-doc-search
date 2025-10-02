package com.mca.project.securedocsearch.dto;

import org.springframework.core.io.Resource;

public record FileResource(Resource resource, String filename, String contentType) {}
