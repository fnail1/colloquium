package ru.mail.colloquium.service;

import java.util.Map;

import ru.mail.colloquium.api.model.GsonAnswers;
import ru.mail.colloquium.api.model.GsonProfileResponse;
import ru.mail.colloquium.api.model.GsonQuestionResponse;
import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.model.entities.Answer;
import ru.mail.colloquium.model.entities.Question;
import ru.mail.colloquium.model.types.Profile;

import static ru.mail.colloquium.App.dateTimeService;

public class MergeHelper {

    public static void merge(AppData appData, Question dst, GsonQuestionResponse.GsonQuestion src) {
        merge(dst, src);
        appData.questions.save(dst);
    }

    public static void merge(Question dst, GsonQuestionResponse.GsonQuestion src) {
        dst.serverId = src.id;
//        dst.emoji = src.emoji;
        dst.emoji = "https://emojipedia-us.s3.amazonaws.com/thumbs/144/apple/129/grinning-face_1f600.png";
        dst.question = src.question;
        dst.createdAt = dateTimeService().parseServerTime(src.created_at);
        dst.updatedAt = dateTimeService().parseServerTime(src.updated_at);
    }

    public static void merge(Answer dst, GsonQuestionResponse.GsonQuestion src) {
        dst.questionServerId = src.id;
        dst.questionEmoji = src.emoji;
        dst.questionText = src.question;
        dst.questionCreatedAt = dateTimeService().parseServerTime(src.created_at);
        dst.questionUpdatedAt = dateTimeService().parseServerTime(src.updated_at);
    }

    public static void merge(AppData appData, Answer dst, GsonAnswers.GsonAnswer src) {
        merge(dst, src);
        appData.answers.save(dst);
    }

    public static void merge(Answer dst, GsonAnswers.GsonAnswer src) {
        dst.serverId = src.id;
        dst.allPhones = src.all_phones;
        dst.createdAt = dateTimeService().parseServerTime(src.created_at);
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

    public static void merge(Profile dst, GsonProfileResponse.GsonUser src) {
        dst.createdAt = dateTimeService().parseServerTime(src.created_at);
        dst.updatedAt = dateTimeService().parseServerTime(src.updated_at);
        dst.phone = src.phone;
        dst.gender = src.sex;
        dst.age = src.info;


    }
}