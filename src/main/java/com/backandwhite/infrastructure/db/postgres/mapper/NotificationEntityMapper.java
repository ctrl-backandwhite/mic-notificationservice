package com.backandwhite.infrastructure.db.postgres.mapper;

import com.backandwhite.domain.model.Notification;
import com.backandwhite.infrastructure.db.postgres.entity.NotificationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { NotificationTemplateEntityMapper.class })
public interface NotificationEntityMapper {

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
    Notification toDomain(NotificationEntity entity);

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
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    NotificationEntity toEntity(Notification model);

    List<Notification> toDomainList(List<NotificationEntity> entities);

    List<NotificationEntity> toEntityList(List<Notification> models);
}
