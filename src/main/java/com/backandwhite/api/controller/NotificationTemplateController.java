package com.backandwhite.api.controller;

import com.backandwhite.api.BaseApi;
import com.backandwhite.api.dto.in.NotificationTemplateDtoIn;
import com.backandwhite.api.dto.out.NotificationTemplateDtoOut;
import com.backandwhite.api.mapper.NotificationTemplateDtoMapper;
import com.backandwhite.api.validation.CreateValidation;
import com.backandwhite.api.validation.UpdateValidation;
import com.backandwhite.application.usecase.NotificationTemplateUseCase;
import com.backandwhite.common.security.annotation.NxAdmin;
import com.backandwhite.domain.model.NotificationTemplate;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification-templates")
@Tag(name = "Notification Template Operations.", description = "Operations related to notification templates.")
public class NotificationTemplateController
        implements
            BaseApi<NotificationTemplateDtoIn, NotificationTemplateDtoOut, Long> {

    private final NotificationTemplateDtoMapper mapper;
    private final NotificationTemplateUseCase useCase;
    private final TemplateEngine templateEngine;

    @NxAdmin
    @Override
    @PostMapping
    public ResponseEntity<NotificationTemplateDtoOut> create(
            @Validated({Default.class, CreateValidation.class}) @RequestBody NotificationTemplateDtoIn dto) {
        NotificationTemplate saved = useCase.save(mapper.toDomain(dto));
        return new ResponseEntity<>(mapper.toDtoOut(saved), HttpStatus.CREATED);
    }

    @NxAdmin
    @Override
    @PutMapping("/{id}")
    public ResponseEntity<NotificationTemplateDtoOut> update(
            @Validated({Default.class, UpdateValidation.class}) @RequestBody NotificationTemplateDtoIn dto,
            @PathVariable Long id) {
        NotificationTemplate updated = useCase.update(mapper.toDomain(dto), id);
        return new ResponseEntity<>(mapper.toDtoOut(updated), HttpStatus.OK);
    }

    @NxAdmin
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        useCase.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @NxAdmin
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<NotificationTemplateDtoOut> getById(@PathVariable Long id) {
        return new ResponseEntity<>(mapper.toDtoOut(useCase.getById(id)), HttpStatus.OK);
    }

    @NxAdmin
    @Override
    @GetMapping
    public ResponseEntity<List<NotificationTemplateDtoOut>> findAll() {
        return new ResponseEntity<>(mapper.toDtoOutList(useCase.findAll()), HttpStatus.OK);
    }

    /**
     * NX036SHOP-472 IMPL-07 — render template preview with sample variables. Lets
     * the admin verify the rendered HTML before sending real emails. Body is an
     * arbitrary {@code Map<String,Object>} of Thymeleaf variables; defaults are
     * injected for the common ones (userName, orderNumber, totalAmount,
     * currencyCode) so a quick "render with empty body" already produces a
     * meaningful preview.
     */
    @NxAdmin
    @PostMapping(value = "/{id}/preview", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> preview(@PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> vars) {
        NotificationTemplate tpl = useCase.getById(id);
        Context ctx = new Context();
        ctx.setVariable("userName", "Cliente Demo");
        ctx.setVariable("orderNumber", "ORD-PREVIEW-0001");
        ctx.setVariable("totalAmount", "199.99");
        ctx.setVariable("currencyCode", "USD");
        ctx.setVariable("subject", tpl.getSubject() == null ? "" : tpl.getSubject());
        if (vars != null) {
            vars.forEach(ctx::setVariable);
        }
        String html = templateEngine.process(tpl.getTemplateFile(), ctx);
        return ResponseEntity.ok(html);
    }
}
