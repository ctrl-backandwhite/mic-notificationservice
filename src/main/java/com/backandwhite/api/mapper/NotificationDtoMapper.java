package com.backandwhite.api.mapper;

import com.backandwhite.api.dto.in.NotificationDtoIn;
import com.backandwhite.api.dto.out.NotificationDtoOut;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { NotificationTemplateDtoMapper.class })
public interface NotificationDtoMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "recipient", source = "recipient")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "template", source = "template")
    @Mapping(target = "variables", source = "variables")
    @Mapping(target = "errorMessage", source = "errorMessage")
    @Mapping(target = "retryCount", source = "retryCount")
    @Mapping(target = "sentAt", source = "sentAt")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    NotificationDtoOut toDtoOut(Notification model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(com.backandwhite.domain.model.NotificationStatus.PENDING)")
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "retryCount", constant = "0")
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Notification toDomain(NotificationDtoIn dtoIn);

    List<NotificationDtoOut> toDtoOutList(List<Notification> models);
}
