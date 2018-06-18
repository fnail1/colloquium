package ru.mail.colloquium.service;

import java.security.interfaces.DSAKey;
import java.util.List;
import java.util.Map;

import ru.mail.colloquium.api.model.GsonAnswers;
import ru.mail.colloquium.api.model.GsonProfileResponse;
import ru.mail.colloquium.api.model.GsonQuestionResponse;
import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.model.entities.Answer;
import ru.mail.colloquium.model.entities.Question;
import ru.mail.colloquium.model.types.Profile;
import ru.mail.colloquium.utils.Utils;

import static ru.mail.colloquium.App.dateTimeService;
import static ru.mail.colloquium.App.prefs;

public class MergeHelper {

    public static void merge(AppData appData, Question dst, GsonQuestionResponse.GsonQuestion src, int questionCycle) {
        merge(dst, src, questionCycle);
        appData.questions.save(dst);
    }

    public static void merge(Question dst, GsonQuestionResponse.GsonQuestion src, int questionCycle) {
        dst.serverId = src.id;
        dst.uniqueId = src.id + ':' + questionCycle;
        dst.emojiUrl = prefs().getApiSet().fixSslForSandbox(src.url);
        dst.emojiText = src.alt;
        if (dst.emojiUrl == null) {
            dst.emojiUrl = "https://emojipedia-us.s3.amazonaws.com/thumbs/144/apple/129/grinning-face_1f600.png";
            dst.emojiText = "\uD83D\uDE00";
        }
        dst.question = src.question;
        dst.createdAt = dateTimeService().parseServerTime(src.created_at);
        dst.updatedAt = dateTimeService().parseServerTime(src.updated_at);
    }

    public static void merge(Answer dst, GsonQuestionResponse.GsonQuestion src) {
        dst.questionServerId = src.id;
        dst.questionEmoji = prefs().getApiSet().fixSslForSandbox(src.url);
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
        dst.variantA = src.variantA;
        dst.variantB = src.variantB;
        dst.variantC = src.variantC;
        dst.variantD = src.variantD;
        dst.answer = src.selected_variant;
        dst.answerName = src.selected_name;
        dst.createdAt = dateTimeService().parseServerTime(src.created_at);
        dst.flags.set(Answer.FLAG_VIEWED, src.is_viewed);
        dst.gender = src.sex;
        dst.age = src.user_education;

        merge(dst, src.question);
    }

    public static void merge(AppData appData, GsonAnswers.GsonAnswer[] src, List<Answer> outNew) {
        Map<String, Answer> answers = appData.answers.select(src).toMap(a -> a.serverId);
        try (AppData.Transaction tx = appData.beginTransaction()) {
            for (GsonAnswers.GsonAnswer gsonAnswer : src) {
                Answer answer = answers.remove(gsonAnswer.id);
                if (answer == null) {
                    answer = new Answer();
                    merge(appData, answer, gsonAnswer);
                    if (outNew != null)
                        outNew.add(answer);
                }
            }

            tx.commit();
        }
    }

    public static void merge(Profile dst, GsonProfileResponse.GsonUser src) {
        dst.createdAt = dateTimeService().parseServerTime(src.created_at);
        dst.updatedAt = dateTimeService().parseServerTime(src.updated_at);
        dst.phone = src.phone;
        dst.serverId = Utils.md5(src.phone);
        dst.gender = src.sex;
        dst.age = src.education;


    }
}
