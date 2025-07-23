package com.pingedIn.ai.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pingedIn.ai.DTOs.Message;
import com.pingedIn.ai.DTOs.Referral;
import com.pingedIn.ai.Services.PingedInService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;


@RestController()
@RequestMapping("/api/")
@CrossOrigin("localhost//:3000")
public class PingedInController {
    @Autowired
    private PingedInService pingedInService;

    @GetMapping("ping")
    public ResponseEntity<?> getMethodName(@RequestParam String param) {
        return ResponseEntity.ok().body("PingedIn Server is running fine.");
    }

    @PostMapping("message")
    public ResponseEntity<?> generatemessage(@RequestBody Message message ) {
        return ResponseEntity.ok().body(pingedInService.generateMessage(message));
    }

   @PostMapping(value = "/referral", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> getReferral(@RequestPart("file") MultipartFile file,@RequestPart("JobId") String jobId, @RequestPart("JobDescription") String jobDescription, @RequestPart("condidateResumeContent") String resumeContent) {
        Referral referral = new Referral();
        referral.setFile(file);
        referral.setJobId(jobId);
        referral.setJobDescription(jobDescription);
        referral.setCondidateResumeContent(resumeContent);

        return ResponseEntity.ok(pingedInService.generateReferral(referral));
    }

    
    
}
