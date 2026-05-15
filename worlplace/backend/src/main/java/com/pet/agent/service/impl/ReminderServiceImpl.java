package com.pet.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.agent.entity.Reminder;
import com.pet.agent.mapper.ReminderMapper;
import com.pet.agent.service.ReminderService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReminderServiceImpl implements ReminderService {

    private final ReminderMapper reminderMapper;

    public ReminderServiceImpl(ReminderMapper reminderMapper) {
        this.reminderMapper = reminderMapper;
    }

    @Override
    public List<Reminder> getReminders(Long userId) {
        return reminderMapper.selectList(
                new LambdaQueryWrapper<Reminder>()
                        .eq(Reminder::getUserId, userId)
                        .eq(Reminder::getStatus, 1)
                        .orderByAsc(Reminder::getRemindDate));
    }

    @Override
    public List<Reminder> getRemindersByPet(Long userId, Long petId) {
        return reminderMapper.selectList(
                new LambdaQueryWrapper<Reminder>()
                        .eq(Reminder::getUserId, userId)
                        .eq(Reminder::getPetId, petId)
                        .eq(Reminder::getStatus, 1)
                        .orderByAsc(Reminder::getRemindDate));
    }

    @Override
    public Reminder createReminder(Reminder reminder) {
        reminder.setIsSent(0);
        reminder.setIsRead(0);
        reminder.setStatus(1);
        reminderMapper.insert(reminder);
        return reminder;
    }

    @Override
    public void markAsRead(Long id) {
        Reminder reminder = reminderMapper.selectById(id);
        if (reminder != null) {
            reminder.setIsRead(1);
            reminderMapper.updateById(reminder);
        }
    }

    @Override
    public void deleteReminder(Long id) {
        Reminder reminder = reminderMapper.selectById(id);
        if (reminder != null) {
            reminder.setStatus(0);
            reminderMapper.updateById(reminder);
        }
    }

    @Override
    public List<Reminder> getUnsentReminders() {
        return reminderMapper.selectList(
                new LambdaQueryWrapper<Reminder>()
                        .eq(Reminder::getStatus, 1)
                        .eq(Reminder::getIsSent, 0)
                        .le(Reminder::getRemindDate, LocalDate.now()));
    }
}
