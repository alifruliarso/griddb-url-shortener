package com.galapea.demogriddb.config;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import com.galapea.demogriddb.model.ShortUrlResponse;
import com.galapea.demogriddb.model.UrlModel;

@Mapper(componentModel = "spring")
public abstract class ShortUrlMapper {

    public static final ShortUrlMapper SHORT_URL_MAPPER_INSTANCE =
            Mappers.getMapper(ShortUrlMapper.class);

    @Mapping(source = "shortUrl", target = "shortUrl")
    @Mapping(source = "originalUrl", target = "url")
    public abstract ShortUrlResponse mapEntityToResponse(UrlModel urlModel);
}
