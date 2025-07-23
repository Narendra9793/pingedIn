package com.pingedIn.ai.Services;


import java.util.Map;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pingedIn.ai.DTOs.Message;
import com.pingedIn.ai.DTOs.Referral;

@Service
public class PingedInService {

    @Value("${gemini.api.url}")
    private String geminiApIUrl;
    @Value("${gemini.api.key}")
    private String geminiApIKey;
    @Value("${ocr.api.key}")
    private String ocrApIKey;

    @Value("${ocr.api.url}")
    private String ocrApIUrl;

    private final WebClient webClient;

    public PingedInService(WebClient.Builder webClientBuilder ){
        this.webClient=webClientBuilder.build();
    }
    
    public String generateMessage(Message message){
        try {
            String prompt= buildPrompt(message);
            System.out.println(prompt);
            Map<String, Object> request= Map.of(
                "contents" , new Object[]{
                    Map.of(
                        "parts", new Object[]{
                            Map.of("text", prompt)
                        }
                    )
                }
            );

            String response = webClient.post()
            .uri(geminiApIUrl + "?key=" + geminiApIKey)
            .header("Content-Type" , "application/json" )
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String.class)
            .block();

            System.out.println(response);
            return extractResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing the request: " + e.getMessage();

        }
    }

    public String generateReferral(Referral referral){
         try {
            System.out.println("In referral" );
            String reusmeContent=getResumeContent(referral.getFile());
            System.out.println("This is resume Content: "+ reusmeContent);
            referral.setCondidateResumeContent(reusmeContent.toString());
            String prompt= buildReferralPrompt(referral);
            System.out.println(prompt);
            Map<String, Object> request= Map.of(
                "contents" , new Object[]{
                    Map.of(
                        "parts", new Object[]{
                            Map.of("text", prompt)
                        }
                    )
                }
            );

            String response = webClient.post()
            .uri(geminiApIUrl + "?key=" + geminiApIKey)
            .header("Content-Type" , "application/json" )
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String.class)
            .block();

            System.out.println(response);
            return extractResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing the request: " + e.getMessage();

        }
    }

private String getResumeContent(MultipartFile file) {
    try {
        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename(); // Prevents "filename=null" issue
            }
        };

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("file", resource);
        formData.add("language", "eng");
        formData.add("isOverlayRequired", "false");

        return webClient.post()
                .uri(ocrApIUrl)
                .header("apikey", ocrApIKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block(); // Synchronously block and return the response

    } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Could not parse the resume: " + e.getMessage());
        return null;
    }
}


    private String extractResponse(String response) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode=mapper.readTree(response);
                return rootNode
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing the request: " + e.getMessage();
        }
    }

    private String buildPrompt(Message message) {
        StringBuilder prompt = new StringBuilder();
        System.out.println("this is type:" + message.getMessageType());
        if(message.getMessageType().equals("reply") ){
            prompt.append("Generate a professional reply for the provided linkedIn message. There is no need of Subject line as it is for LinkendIn message reply. \n Original message: \n").append(message.getMessageContent());
        }
        else{
            prompt.append("Generate a professional followUp message for the provided linkedIn message. There is no need of Subject line as it is for LinkendIn message folow Up. \n Original message: \n").append(message.getMessageContent());
        }
        return prompt.toString();
    }

    private String buildReferralPrompt(Referral referral) {
        StringBuilder prompt = new StringBuilder();
        System.out.println("this is Refrral:" + referral);
        prompt.append("Generate a professional  job referral request for linkedIn. \n Following are the Job description , Job id and My resume details:\n Job desription:\n").append(referral.getJobDescription()).append("This is Job Id: \n").append(referral.getJobId()).append("This is my resume details: \n");
        if(referral.getCondidateResumeContent() == null)referral.setCondidateResumeContent("");
        prompt.append(referral.getCondidateResumeContent()).append("\n Do not give any option just prepare a single lean template based above provided data. Create a professional, short and crisp refrral request.");
        return prompt.toString();
    }


}
