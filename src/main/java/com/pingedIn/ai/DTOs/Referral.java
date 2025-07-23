package com.pingedIn.ai.DTOs;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class Referral {
    private String JobId;
    private String JobDescription;
    private String condidateResumeContent;
    private MultipartFile file;
}
