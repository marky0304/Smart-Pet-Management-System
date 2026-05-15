package com.pet.controller;

import com.pet.agent.core.ActionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
@Profile("dev")
public class TestController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ActionExecutor actionExecutor;

    @GetMapping("/hash")
    public Map<String, Object> generateHash(@RequestParam String password) {
        Map<String, Object> result = new HashMap<>();

        String hash = passwordEncoder.encode(password);
        result.put("password", password);
        result.put("hash", hash);
        result.put("verify", passwordEncoder.matches(password, hash));

        return result;
    }

    @PostMapping("/action")
    public Map<String, Object> testAction(@RequestBody Map<String, Object> body) {
        String llmReply = (String) body.get("llmReply");
        Long userId = body.get("userId") != null
                ? ((Number) body.get("userId")).longValue() : 1L;

        ActionExecutor.ActionResult result = actionExecutor.process(llmReply, userId);

        Map<String, Object> output = new HashMap<>();
        output.put("cleanReply", result.getCleanReply());
        output.put("confirmation", result.getConfirmation());
        output.put("actionExecuted", result.isActionExecuted());
        output.put("navigate", result.getNavigate());
        output.put("navigateParams", result.getNavigateParams());
        return output;
    }
}
