package com.backandwhite.api.controller;

import com.backandwhite.api.BaseApi;
import com.backandwhite.api.dto.in.NotificationTemplateDtoIn;
import com.backandwhite.api.dto.out.NotificationTemplateDtoOut;
import com.backandwhite.api.mapper.NotificationTemplateDtoMapper;
import com.backandwhite.api.validation.CreateValidation;
import com.backandwhite.api.validation.UpdateValidation;
import com.backandwhite.application.usecase.NotificationTemplateUseCase;
import com.backandwhite.domain.model.NotificationTemplate;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification-templates")
@Tag(name = "Notification Template Operations.", description = "Operations related to notification templates.")
public class NotificationTemplateController implements BaseApi<NotificationTemplateDtoIn, NotificationTemplateDtoOut, Long> {

    private final NotificationTemplateDtoMapper mapper;
    private final NotificationTemplateUseCase useCase;

    @Override
    @PostMapping
    public ResponseEntity<NotificationTemplateDtoOut> create(
            @Validated({ Default.class, CreateValidation.class }) @RequestBody NotificationTemplateDtoIn dto) {
        NotificationTemplate saved = useCase.save(mapper.toDomain(dto));
        return new ResponseEntity<>(mapper.toDtoOut(saved), HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<NotificationTemplateDtoOut> update(
            @Validated({ Default.class, UpdateValidation.class }) @RequestBody NotificationTemplateDtoIn dto,
            @PathVariable Long id) {
        NotificationTemplate updated = useCase.update(mapper.toDomain(dto), id);
        return new ResponseEntity<>(mapper.toDtoOut(updated), HttpStatus.OK);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        useCase.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<NotificationTemplateDtoOut> getById(@PathVariable Long id) {
        return new ResponseEntity<>(mapper.toDtoOut(useCase.getById(id)), HttpStatus.OK);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<NotificationTemplateDtoOut>> findAll() {
        return new ResponseEntity<>(mapper.toDtoOutList(useCase.findAll()), HttpStatus.OK);
    }
}
