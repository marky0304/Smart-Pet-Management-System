package com.pet.entity;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 兼容多种日期格式的 LocalDate 反序列化器
 * 支持：yyyy-MM-dd、yyyy-MM-dd HH:mm:ss、ISO 8601 (2026-04-05T16:00:00.000Z)
 */
public class FlexibleLocalDateDeserializer extends StdDeserializer<LocalDate> {

    public FlexibleLocalDateDeserializer() {
        super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        String value = p.getText().trim();
        if (value.isEmpty()) return null;

        // 1. 标准日期格式 yyyy-MM-dd
        try {
            return LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException ignored) {}

        // 2. 带时间 yyyy-MM-dd HH:mm:ss
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .toLocalDate();
        } catch (DateTimeParseException ignored) {}

        // 3. ISO 8601 含 T 和 Z，如 2026-04-05T16:00:00.000Z
        try {
            String normalized = value.replace("Z", "").replaceAll("\\.\\d+$", "");
            return LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                    .toLocalDate();
        } catch (DateTimeParseException ignored) {}

        // 4. 只有 T 分隔，无毫秒
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate();
        } catch (DateTimeParseException ignored) {}

        throw new IOException("无法解析日期格式: " + value);
    }
}
