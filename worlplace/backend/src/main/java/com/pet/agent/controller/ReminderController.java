package com.pet.agent.controller;

import com.pet.agent.entity.Reminder;
import com.pet.agent.service.ReminderService;
import com.pet.common.result.Result;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/reminders")
@CrossOrigin(origins = "*")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping
    public Result<List<Reminder>> list(HttpServletRequest req,
                                       @RequestParam(required = false) Long petId) {
        Long userId = (Long) req.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<Reminder> reminders;
        if (petId != null) {
            reminders = reminderService.getRemindersByPet(userId, petId);
        } else {
            reminders = reminderService.getReminders(userId);
        }
        return Result.success(reminders);
    }

    @PostMapping
    public Result<Reminder> create(@RequestBody Reminder reminder, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        reminder.setUserId(userId);
        Reminder created = reminderService.createReminder(reminder);
        return Result.success(created);
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        reminderService.markAsRead(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reminderService.deleteReminder(id);
        return Result.success();
    }
}
