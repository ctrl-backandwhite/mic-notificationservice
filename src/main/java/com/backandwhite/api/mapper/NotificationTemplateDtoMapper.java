package com.backandwhite.api.mapper;

import com.backandwhite.api.dto.in.NotificationTemplateDtoIn;
import com.backandwhite.api.dto.out.NotificationTemplateDtoOut;
import com.backandwhite.domain.model.NotificationTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationTemplateDtoMapper {

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
    NotificationTemplateDtoOut toDtoOut(NotificationTemplate model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    NotificationTemplate toDomain(NotificationTemplateDtoIn dtoIn);

    List<NotificationTemplate> toDomainList(List<NotificationTemplateDtoIn> dtos);

    List<NotificationTemplateDtoOut> toDtoOutList(List<NotificationTemplate> models);
}
