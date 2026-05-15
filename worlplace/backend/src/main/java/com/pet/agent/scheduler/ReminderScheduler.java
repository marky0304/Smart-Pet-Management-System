package com.pet.agent.scheduler;

import com.pet.agent.entity.Reminder;
import com.pet.agent.service.ReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final ReminderService reminderService;

    public ReminderScheduler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void scanAndPushReminders() {
        log.info("开始扫描待推送提醒...");
        List<Reminder> unsent = reminderService.getUnsentReminders();
        log.info("发现 {} 条待推送提醒", unsent.size());
        for (Reminder reminder : unsent) {
            // TODO: 接入实际推送渠道（微信/短信/App推送）
            log.info("推送提醒: userId={}, petId={}, title={}, date={}",
                    reminder.getUserId(), reminder.getPetId(),
                    reminder.getTitle(), reminder.getRemindDate());
            reminder.setIsSent(1);
        }
    }
}
