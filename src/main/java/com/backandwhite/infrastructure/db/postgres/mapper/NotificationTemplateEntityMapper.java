package com.backandwhite.infrastructure.db.postgres.mapper;

import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.infrastructure.db.postgres.entity.NotificationTemplateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationTemplateEntityMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "templateFile", source = "templateFile")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    NotificationTemplate toDomain(NotificationTemplateEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "templateFile", source = "templateFile")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    NotificationTemplateEntity toEntity(NotificationTemplate model);

    List<NotificationTemplate> toDomainList(List<NotificationTemplateEntity> entities);

    List<NotificationTemplateEntity> toEntityList(List<NotificationTemplate> models);
}
