package com.pingedIn.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class PingedInApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load(); // Ignore if .env is missing

        setSystemProperty("GEMINI_API_URL", dotenv);
        setSystemProperty("GEMINI_API_KEY", dotenv);
        setSystemProperty("OCR_API_KEY", dotenv);
        setSystemProperty("FRONTEND_URL", dotenv);
		SpringApplication.run(PingedInApplication.class, args);
	}

	private static void setSystemProperty(String key, Dotenv dotenv) {
        String value = dotenv.get(key);
        if (value != null && !value.isEmpty()) {
            System.setProperty(key, value);
            System.out.println(key +"-------------------------------- "+ System.getProperty(key));
        } else {
            System.err.println("Warning: Environment variable " + key + " is missing or empty!");
        }
    }

}
