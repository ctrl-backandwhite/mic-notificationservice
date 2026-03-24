package com.backandwhite.api.controller;

import com.backandwhite.api.BaseApi;
import com.backandwhite.api.dto.in.NotificationDtoIn;
import com.backandwhite.api.dto.out.NotificationDtoOut;
import com.backandwhite.api.mapper.NotificationDtoMapper;
import com.backandwhite.api.validation.CreateValidation;
import com.backandwhite.application.service.EmailService;
import com.backandwhite.application.usecase.NotificationTemplateUseCase;
import com.backandwhite.application.usecase.NotificationUseCase;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
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
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification Operations.", description = "Operations related to notifications.")
public class NotificationController implements BaseApi<NotificationDtoIn, NotificationDtoOut, Long> {

    private final NotificationDtoMapper mapper;
    private final NotificationUseCase useCase;
    private final NotificationTemplateUseCase templateUseCase;
    private final EmailService emailService;

    @Override
    @PostMapping
    public ResponseEntity<NotificationDtoOut> create(
            @Validated({ Default.class, CreateValidation.class }) @RequestBody NotificationDtoIn dto) {
        Notification notification = mapper.toDomain(dto);

        if (dto.getTemplateId() != null) {
            notification.setTemplate(templateUseCase.getById(dto.getTemplateId()));
        }

        Notification saved = useCase.save(notification);
        emailService.sendEmail(saved);
        return new ResponseEntity<>(mapper.toDtoOut(saved), HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<NotificationDtoOut> getById(@PathVariable Long id) {
        return new ResponseEntity<>(mapper.toDtoOut(useCase.getById(id)), HttpStatus.OK);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<NotificationDtoOut>> findAll() {
        return new ResponseEntity<>(mapper.toDtoOutList(useCase.findAll()), HttpStatus.OK);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        useCase.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<NotificationDtoOut>> findByStatus(@PathVariable NotificationStatus status) {
        return new ResponseEntity<>(mapper.toDtoOutList(useCase.findByStatus(status)), HttpStatus.OK);
    }

    @GetMapping("/recipient/{recipient}")
    public ResponseEntity<List<NotificationDtoOut>> findByRecipient(@PathVariable String recipient) {
        return new ResponseEntity<>(mapper.toDtoOutList(useCase.findByRecipient(recipient)), HttpStatus.OK);
    }
}
