package com.pet.agent.service;

import com.pet.agent.entity.Reminder;

import java.util.List;

public interface ReminderService {

    List<Reminder> getReminders(Long userId);

    List<Reminder> getRemindersByPet(Long userId, Long petId);

    Reminder createReminder(Reminder reminder);

    void markAsRead(Long id);

    void deleteReminder(Long id);

    List<Reminder> getUnsentReminders();
}
