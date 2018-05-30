package ru.mail.colloquium.service;

import android.support.v4.util.LongSparseArray;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import ru.mail.colloquium.api.model.GsonAnswers;
import ru.mail.colloquium.api.model.GsonQuestionResponse;
import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.model.entities.Answer;
import ru.mail.colloquium.model.entities.Question;

import static ru.mail.colloquium.App.app;
import static ru.mail.colloquium.App.dateTimeService;
import static ru.mail.colloquium.diagnostics.DebugUtils.safeThrow;

public class MergeHelper {

    public static void merge(AppData appData, Question dst, GsonQuestionResponse.GsonQuestion src) {
        merge(dst, src);
        appData.questions.save(dst);
    }

    public static void merge(Question dst, GsonQuestionResponse.GsonQuestion src) {
        dst.serverId = src.id;
        dst.emoji = src.emoji;
        dst.question = src.question;
        try {
            dst.createdAt = dateTimeService().parseServerTime(src.created_at);
        } catch (ParseException e) {
            safeThrow(e);
        }
        try {
            dst.updatedAt = dateTimeService().parseServerTime(src.updated_at);
        } catch (ParseException e) {
            safeThrow(e);
        }
    }

    public static void merge(Answer dst, GsonQuestionResponse.GsonQuestion src) {
        dst.questionServerId = src.id;
        dst.questionEmoji = src.emoji;
        dst.questionText = src.question;
        try {
            dst.questionCreatedAt = dateTimeService().parseServerTime(src.created_at);
        } catch (ParseException e) {
            safeThrow(e);
        }
        try {
            dst.questionUpdatedAt = dateTimeService().parseServerTime(src.updated_at);
        } catch (ParseException e) {
            safeThrow(e);
        }
    }

    public static void merge(AppData appData, Answer dst, GsonAnswers.GsonAnswer src) {
        merge(dst, src);
        appData.answers.save(dst);
    }

    public static void merge(Answer dst, GsonAnswers.GsonAnswer src) {
        dst.serverId = src.id;
        dst.allPhones = src.all_phones;
        try {
            dst.createdAt = dateTimeService().parseServerTime(src.created_at);
        } catch (ParseException e) {
            safeThrow(e);
        }
        dst.flags.set(Answer.FLAG_VIEWED, src.is_viewed);
        dst.selectedPhone = src.selected_phone;
        dst.gender = src.sex;

        merge(dst, src.question);
    }

    public static void merge(AppData appData, GsonAnswers.GsonAnswer[] src) {
        Map<String, Answer> answers = appData.answers.select(src).toMap(a -> a.serverId);
        try (AppData.Transaction tx = appData.beginTransaction()) {
            for (GsonAnswers.GsonAnswer gsonAnswer : src) {
                Answer answer = answers.remove(gsonAnswer.id);
                if (answer == null) {
                    answer = new Answer();
                    merge(appData, answer, gsonAnswer);
                }
            }

            tx.commit();
        }
    }
}
