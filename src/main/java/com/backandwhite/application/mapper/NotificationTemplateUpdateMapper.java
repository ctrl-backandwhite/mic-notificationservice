package com.backandwhite.application.mapper;

import com.backandwhite.domain.model.NotificationTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NotificationTemplateUpdateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateFromModel(NotificationTemplate source, @MappingTarget NotificationTemplate target);
}
