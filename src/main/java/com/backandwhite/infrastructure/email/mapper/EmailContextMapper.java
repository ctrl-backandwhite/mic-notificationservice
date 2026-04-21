package com.backandwhite.infrastructure.email.mapper;

import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationTemplate;
import java.util.Locale;
import java.util.Map;
import org.mapstruct.Mapper;
import org.thymeleaf.context.Context;

/**
 * MapStruct mapper responsible for translating a {@link Notification} domain
 * object into the artifacts needed by the Thymeleaf email engine (locale-aware
 * {@link Context} and target template file path).
 *
 * <p>
 * MapStruct generates an empty implementation — the actual logic is supplied
 * through {@code default} methods because the transformation is not a plain
 * field-to-field mapping.
 */
@Mapper(componentModel = "spring")
public interface EmailContextMapper {

    // Spanish is the primary locale of the storefront (es-ES + LatAm). Use
    // it as the fallback when the caller doesn't specify a language,
    // otherwise the Thymeleaf resolver picks messages_en.properties and the
    // customer sees English bundle keys mixed with Spanish hardcoded parts
    // (subject, customer name, product names…).
    String DEFAULT_LANG = "es";
    String DEFAULT_TEMPLATE_FILE = "email/default";

    default Context toContext(Notification notification) {
        Context context = new Context(resolveLocale(notification));
        Map<String, Object> variables = notification != null ? notification.getVariables() : null;
        if (variables != null) {
            variables.forEach(context::setVariable);
        }
        return context;
    }

    default Locale resolveLocale(Notification notification) {
        if (notification == null || notification.getVariables() == null) {
            return Locale.forLanguageTag(DEFAULT_LANG);
        }
        Object lang = notification.getVariables().get("lang");
        if (lang == null) {
            return Locale.forLanguageTag(DEFAULT_LANG);
        }
        String langTag = lang.toString();
        if (langTag.isBlank()) {
            return Locale.forLanguageTag(DEFAULT_LANG);
        }
        return Locale.forLanguageTag(langTag);
    }

    default String resolveTemplateFile(Notification notification) {
        if (notification == null) {
            return DEFAULT_TEMPLATE_FILE;
        }
        NotificationTemplate template = notification.getTemplate();
        return template != null && template.getTemplateFile() != null
                ? template.getTemplateFile()
                : DEFAULT_TEMPLATE_FILE;
    }
}
