package com.gridpadel.infrastructure.persistence.mapper;

import com.gridpadel.domain.model.vo.MatchResult;
import com.gridpadel.infrastructure.persistence.dto.MatchResultDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = SetResultDtoMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class MatchResultDtoMapper {

    public abstract MatchResultDto toDto(MatchResult result);

    public abstract MatchResult fromDto(MatchResultDto dto);
}
